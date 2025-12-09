package top.swjtuhc.accounting_management_api.controller.admin.req;

import lombok.Data;

@Data
public class UserAddReq {

    private String username;

    private Integer role;

    private String password;

}
