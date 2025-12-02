package top.swjtuhc.accounting_management_api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    private Long id;

    private String username;

    private String password;

    private Integer role;

    private Integer status;

    private Date createdTime;

    private Date updatedTime;
}