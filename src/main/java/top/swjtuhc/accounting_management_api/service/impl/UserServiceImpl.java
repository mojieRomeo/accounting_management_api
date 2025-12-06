package top.swjtuhc.accounting_management_api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import top.swjtuhc.accounting_management_api.controller.admin.req.UserLoginReq;
import top.swjtuhc.accounting_management_api.controller.admin.req.UserRegisterReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserRegisterResp;
import top.swjtuhc.accounting_management_api.entity.User;
import top.swjtuhc.accounting_management_api.exception.BusinessException;
import top.swjtuhc.accounting_management_api.service.UserService;
import top.swjtuhc.accounting_management_api.mapper.UserMapper;
import org.springframework.stereotype.Service;

import top.swjtuhc.accounting_management_api.util.ExceptionMessage;
import top.swjtuhc.accounting_management_api.util.PasswordEncoder;

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
        StpUtil.login(user.getId());
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
        user.setStatus(1);
        user.setRole(0);
        userMapper.insert(user);
        UserRegisterResp resp = BeanUtil.copyProperties(user, UserRegisterResp.class);

        return resp;

    }

}








