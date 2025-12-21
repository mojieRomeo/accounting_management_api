package top.swjtuhc.accounting_management_api.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @TableName bill
 */
@TableName(value ="bill")
@Data
public class Bill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String costType;

    private BigDecimal amount;

    private Integer type;

    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
}