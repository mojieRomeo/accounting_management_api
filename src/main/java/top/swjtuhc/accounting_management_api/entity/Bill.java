package top.swjtuhc.accounting_management_api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @TableName bill
 */
@TableName(value ="bill")
@Data
public class Bill {
    private Long id;

    private Long userId;

    private String title;

    private Integer type;

    private BigDecimal amount;

    private Date createdAt;

    private Date updatedAt;
}