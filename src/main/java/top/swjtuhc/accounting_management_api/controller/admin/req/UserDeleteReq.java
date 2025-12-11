package top.swjtuhc.accounting_management_api.controller.admin.req;

import lombok.Data;

@Data
public class UserDeleteReq {
    private Long id;

    private String username;

    private String password;

    private Integer role;

    private Integer status;
}
