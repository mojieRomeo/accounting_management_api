package top.swjtuhc.accounting_management_api.service;

import top.swjtuhc.accounting_management_api.controller.admin.req.UserLoginReq;
import top.swjtuhc.accounting_management_api.controller.admin.req.UserRegisterReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserRegisterResp;
import top.swjtuhc.accounting_management_api.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import top.swjtuhc.accounting_management_api.util.ResponseEntity;

/**
* @author luojunjie
* @description 针对表【user】的数据库操作Service
* @createDate 2025-11-30 15:09:28
*/
public interface UserService extends IService<User> {


    UserLoginResp login(UserLoginReq req);

    UserRegisterResp register(UserRegisterReq req);
}
