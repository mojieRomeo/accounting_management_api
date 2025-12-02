package top.swjtuhc.accounting_management_api.controller.admin.req;

import lombok.Data;

@Data
public class UserLoginReq {
    private String username;
    private String password;
    private String role;
    private String status;

}
