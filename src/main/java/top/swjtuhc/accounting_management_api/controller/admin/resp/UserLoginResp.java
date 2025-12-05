package top.swjtuhc.accounting_management_api.controller.admin.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
@Data
public class UserLoginResp {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private Integer role;

    private Integer status;

    private Date createdTime;

    private Date updatedTime;

    private String tokenName;

    private String tokenValue;
}
