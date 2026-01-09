package top.swjtuhc.accounting_management_api.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import top.swjtuhc.accounting_management_api.controller.admin.req.*;
import top.swjtuhc.accounting_management_api.controller.admin.resp.AdminPageResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserRegisterResp;
import top.swjtuhc.accounting_management_api.entity.User;
import top.swjtuhc.accounting_management_api.enums.StatusEnum;
import top.swjtuhc.accounting_management_api.enums.UserRoleEnum;
import top.swjtuhc.accounting_management_api.exception.BusinessException;
import top.swjtuhc.accounting_management_api.service.UserService;
import top.swjtuhc.accounting_management_api.mapper.UserMapper;
import org.springframework.stereotype.Service;

import top.swjtuhc.accounting_management_api.util.ExceptionMessage;
import top.swjtuhc.accounting_management_api.util.PageResponse;
import top.swjtuhc.accounting_management_api.util.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
* @author luojunjie
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-11-30 15:09:28
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;



    @Override
    public UserLoginResp login(UserLoginReq req) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,req.getUsername());
        if(!userMapper.exists(queryWrapper)){
            throw new BusinessException(ExceptionMessage.USER_NOT_FOUND);
        }
        User user = userMapper.selectOne(queryWrapper);
        if (!PasswordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ExceptionMessage.PASSWORD_ERROR);
        }
        /*
        StpUtil.login方法会自动生成token，并完成token<——>user.getId(loginId)的绑定
        这让后面想要获取当前登录人信息的时候可以直接通过getLoginIdAsLong()获取到
         */
        StpUtil.login(user.getId());

        //把登录后生成的token值以及user的信息存入sa_token自带的session，这里是jvm框架自带的内存机制，配置redis之后能自动进入redis，此时jvm和redis都存了一份
        SaSession session = StpUtil.getSessionByLoginId(user.getId());
        session.set("userId",user.getId());
        session.set("role",user.getRole());
        session.set("userName",user.getUsername());
        session.set("tokenName",StpUtil.getTokenName());
        session.set("tokenValue",StpUtil.getTokenValue());

        UserLoginResp resp=BeanUtil.copyProperties(user, UserLoginResp.class);
        resp.setTokenName(StpUtil.getTokenInfo().getTokenName());
        resp.setTokenValue(StpUtil.getTokenInfo().getTokenValue());
        return resp;
    }

    @Override
    public UserRegisterResp register(UserRegisterReq req) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,req.getUsername());
        if(userMapper.exists(queryWrapper)){
            throw new BusinessException(ExceptionMessage.USER_ALREADY_EXISTS);
        }
        String password = PasswordEncoder.encode(req.getPassword());
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(password);
        user.setStatus(StatusEnum.ENABLE.getCode());
        user.setRole(req.getRole());
        userMapper.insert(user);
        return BeanUtil.copyProperties(user, UserRegisterResp.class);

    }

    @Override
    public PageResponse<AdminPageResp> adminPage(AdminPageReq req) {
        //getLoginIdAsLong()在StpUtil.login方法执行后就获取到了
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        Integer currentRole = (Integer) session.get("role");
        //设置redis的key
        String key = "admin:page:"+"currentRole:"+currentRole+":"+"current:"+req.getCurrent()+":"+"size:"+req.getSize()+":"+"keyword:"+req.getKeyword();
        //第一次查redis看有无缓存
        List<AdminPageResp> cached = (List<AdminPageResp>) redisTemplate.opsForValue().get(key);
        //用cache.isEmpty()只判断是否为空，容易报错空指针null异常
        if (CollectionUtils.isNotEmpty(cached)) {
            return new PageResponse<>(req.getCurrent(), (long) cached.size(), req.getSize(), cached);
        }
        /*
        1.synchronized作用是一次只能让一个线程通过，避免数据库被打爆（缓存击穿）
        2.必须写.intern()不然每次String lockKey = "lock:"+key;都会new 新对象，导致lockKey不同，即使内容相同对象也不同，synchronized ((lockKey))会导致多个线程同时进入
        3.出synchronized会自动解锁给下一个线程，不用单独释放锁
        */
        synchronized (key.intern()){
            //二次查redis，万一上个线程写了缓存没看不是白写了吗
            List<AdminPageResp> reCached = (List<AdminPageResp>) redisTemplate.opsForValue().get(key);
            if (CollectionUtils.isNotEmpty(reCached)) {
                return new PageResponse<>(req.getCurrent(), (long) reCached.size(), req.getSize(), reCached);
            }
            Page<User> page = new Page<>(req.getCurrent(), req.getSize());
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            //不能用CollectionUtils.isEmpty(keyword)，因为这是collection工具方法，只能判断List，这里是String
            if(req.getKeyword()!=null && !req.getKeyword().isEmpty()){
                wrapper.like(User::getUsername,req.getKeyword()).or().like(User::getId,req.getKeyword());
            }
            wrapper.eq(User::getStatus,StatusEnum.ENABLE.getCode());
            wrapper.orderByDesc(User::getId);
            if(currentRole.equals(UserRoleEnum.ADMIN.getCode())){
                wrapper.eq(User::getRole,UserRoleEnum.USER.getCode());
            } else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
                wrapper.in(User::getRole,UserRoleEnum.USER.getCode(),UserRoleEnum.ADMIN.getCode());
            }
            Page<User> result = page(page,wrapper);
            List<User> records = result.getRecords();
            List<AdminPageResp> respList = BeanUtil.copyToList(records, AdminPageResp.class);
            if(CollectionUtils.isEmpty(respList)){
                log.info("redis缓存写入空值，key = {}", key);
                //把空值存入redis，下次查直接返回空值，不用每次查数据库,防止查空值被打爆
                redisTemplate.opsForValue().set(key,Collections.emptyList(),300,TimeUnit.SECONDS);
                return new PageResponse<>(result, Collections.emptyList());
            }
            log.info("准备写入 Redis，key = {}", key);
            //把查出来的List对象存入redis，下次查可以直接去redis，不用每次查数据库
            redisTemplate.opsForValue().set(key,respList,300,TimeUnit.SECONDS);
            return new PageResponse<>(result, respList);

        }

    }

    @Override
    public void addUser(UserAddReq req) {
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        Integer currentRole = (Integer) session.get("role");
        if(currentRole.equals(UserRoleEnum.ADMIN.getCode())){
            User user = BeanUtil.copyProperties(req,User.class);
            user.setPassword(PasswordEncoder.encode(req.getPassword()));
            user.setRole(UserRoleEnum.USER.getCode());
            save(user);
        } else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
            User user = BeanUtil.copyProperties(req,User.class);
            user.setPassword(PasswordEncoder.encode(req.getPassword()));
            save(user);
        }
    }

    @Override
    public void updateUser(UserUpdateReq req) {
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        Integer currentRole = (Integer) session.get("role");
        if (currentRole.equals(UserRoleEnum.ADMIN.getCode())) {
            if (req.getRole().equals(UserRoleEnum.USER.getCode())) {
                User user = BeanUtil.copyProperties(req, User.class);
                if(!req.getPassword().isEmpty()){
                    user.setPassword(PasswordEncoder.encode(req.getPassword()));
                }else{
                    throw new BusinessException(ExceptionMessage.PASSWORD_EMPTY);
                }
                userMapper.updateById(user);
            }
        } else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
            User user = BeanUtil.copyProperties(req, User.class);
            if(!req.getPassword().isEmpty()){
                user.setPassword(PasswordEncoder.encode(req.getPassword()));
            }else{
                throw new BusinessException(ExceptionMessage.PASSWORD_EMPTY);
            }
            userMapper.updateById(user);
        }
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }


}








