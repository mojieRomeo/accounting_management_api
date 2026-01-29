package top.swjtuhc.accounting_management_api.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.swjtuhc.accounting_management_api.controller.admin.req.*;
import top.swjtuhc.accounting_management_api.controller.admin.resp.AdminPageResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserRegisterResp;
import top.swjtuhc.accounting_management_api.service.UserService;
import top.swjtuhc.accounting_management_api.util.PageResponse;
import top.swjtuhc.accounting_management_api.util.ResponseEntity;

import java.io.IOException;

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
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.ok();
    }

    @PostMapping(value = "/updateUserInfo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    /*
    Multpartfile类型不能用@RequestBody接收，需要用@RequestParam接收，因此不能用@RequestBody userInfoReq
     */
    public ResponseEntity<?> updateUserInfo(@RequestParam String username,
                                            @RequestParam String password,
                                             @RequestParam(required = false) MultipartFile avatar) throws IOException {
        userService.updateUserInfo(username,password,avatar);
        return ResponseEntity.ok();
    }




}
