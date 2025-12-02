package top.swjtuhc.accounting_management_api.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.swjtuhc.accounting_management_api.controller.admin.req.UserLoginReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.UserLoginResp;
import top.swjtuhc.accounting_management_api.service.UserService;
import top.swjtuhc.accounting_management_api.util.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @PostMapping("/login")
    public ResponseEntity<UserLoginResp> login(@Validated @RequestBody UserLoginReq req) {
        return ResponseEntity.ok(userService.login(req));
    }

}
