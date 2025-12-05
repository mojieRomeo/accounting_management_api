package top.swjtuhc.accounting_management_api.controller.admin.req;

import lombok.Data;

@Data
public class UserRegisterReq {
    private String username;
    private String password;

}
