package top.swjtuhc.accounting_management_api.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.swjtuhc.accounting_management_api.controller.admin.req.*;
import top.swjtuhc.accounting_management_api.controller.admin.resp.AdminPageResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserRegisterResp;
import top.swjtuhc.accounting_management_api.service.UserService;
import top.swjtuhc.accounting_management_api.util.PageResponse;
import top.swjtuhc.accounting_management_api.util.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

   @PostMapping("/login")
    public  ResponseEntity<UserLoginResp> login(@RequestBody UserLoginReq req){
       return ResponseEntity.ok(userService.login(req));
   }

    @PostMapping("/register")
    public  ResponseEntity<UserRegisterResp> register(@RequestBody UserRegisterReq req){
        return ResponseEntity.ok(userService.register(req));
    }
    @PostMapping ("/logout")
    public  ResponseEntity<?> logout(){
        //StpUtil.logout只会清空sa_token的session，对前段localStorage和后段返回的resp无影响
        StpUtil.logout();
        return ResponseEntity.ok();

    }

    @PostMapping("/adminPage")
    public ResponseEntity<PageResponse<AdminPageResp>> adminPage(@RequestBody AdminPageReq req){
        return ResponseEntity.ok(userService.adminPage(req));
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody UserAddReq req){
        userService.addUser(req);
        return ResponseEntity.ok();
    }
    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateReq req){
        userService.updateUser(req);
        return ResponseEntity.ok();
    }
    @DeleteMapping("/deleteUser")
    public ResponseEntity<?> deleteUser(@RequestBody UserDeleteReq req){
        userService.deleteUser(req);
        return ResponseEntity.ok();
    }




}
