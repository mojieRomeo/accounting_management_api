package top.swjtuhc.accounting_management_api.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private Integer role;

    private Integer status;

    //实体类不能是MultipartFile类型，否则会报错
    private String avatar;

    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
}