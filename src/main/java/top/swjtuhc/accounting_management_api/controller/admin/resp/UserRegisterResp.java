package top.swjtuhc.accounting_management_api.controller.admin.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class UserRegisterResp {

    private Long id;

    private String username;

    private Integer role;

    private Integer status;

}
