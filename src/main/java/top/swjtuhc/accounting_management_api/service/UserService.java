package top.swjtuhc.accounting_management_api.service;

import org.springframework.web.multipart.MultipartFile;
import top.swjtuhc.accounting_management_api.controller.admin.req.*;
import top.swjtuhc.accounting_management_api.controller.admin.resp.AdminPageResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserRegisterResp;
import top.swjtuhc.accounting_management_api.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import top.swjtuhc.accounting_management_api.util.PageResponse;

import java.io.IOException;

/**
* @author luojunjie
* @description 针对表【user】的数据库操作Service
* @createDate 2025-11-30 15:09:28
*/
public interface UserService extends IService<User> {


    UserLoginResp login(UserLoginReq req);

    UserRegisterResp register(UserRegisterReq req);


    PageResponse<AdminPageResp> adminPage(AdminPageReq req);


    void addUser(UserAddReq req);

    void updateUser(UserUpdateReq req);


    void deleteUser(Long id);

    void updateUserInfo(String username, String password, MultipartFile avatar) throws IOException;
}
