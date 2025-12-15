package top.swjtuhc.accounting_management_api.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
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

/**
* @author luojunjie
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-11-30 15:09:28
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private final UserMapper userMapper;


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

        //把登录后生成的token值以及user的信息存入sa_token自带的session
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
        Page<User> page = new Page<>(req.getCurrent(), req.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus,StatusEnum.ENABLE.getCode());
        wrapper.orderByDesc(User::getId);
        Integer currentRole = (Integer) session.get("role");
        if(currentRole.equals(UserRoleEnum.ADMIN.getCode())){
            wrapper.eq(User::getRole,UserRoleEnum.USER.getCode());
        } else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
            wrapper.in(User::getRole,UserRoleEnum.USER.getCode(),UserRoleEnum.ADMIN.getCode());
        }
        Page<User> result = page(page,wrapper);
        List<User> records = result.getRecords();
        List<AdminPageResp> respList = BeanUtil.copyToList(records, AdminPageResp.class);
        if(respList.isEmpty()){
            return new PageResponse<>(result, Collections.emptyList());
        }
        return new PageResponse<>(result, respList);

    }

    @Override
    public void addUser(UserAddReq req) {
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        //
        Integer currentRole = (Integer) session.get("role");
        if(currentRole.equals(UserRoleEnum.ADMIN.getCode())){
            if(req.getRole().equals(UserRoleEnum.USER.getCode())){
                User user = BeanUtil.copyProperties(req,User.class);
                user.setPassword(PasswordEncoder.encode(req.getPassword()));
                save(user);
            }else{
                throw new BusinessException(ExceptionMessage.NO_PERMISSION_ADD);
            }
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
                user.setPassword(PasswordEncoder.encode(user.getPassword()));
                userMapper.updateById(user);
            } else {
                throw new BusinessException(ExceptionMessage.NO_PERMISSION_UPDATE);
            }
        }
        else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
            User user = BeanUtil.copyProperties(req, User.class);
            user.setPassword(PasswordEncoder.encode(user.getPassword()));
            userMapper.updateById(user);
        }
    }

    @Override
    public void deleteUser(UserDeleteReq req) {
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        if (session.get("role").equals(UserRoleEnum.ADMIN.getCode())){
            if (req.getRole().equals(UserRoleEnum.USER.getCode())){
                userMapper.deleteById(req.getId());
            }

        } else if (session.get("role").equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
            userMapper.deleteById(req.getId());

        }
        else {
            throw new BusinessException(ExceptionMessage.NO_PERMISSION_UPDATE);
        }

    }


}








