package top.swjtuhc.accounting_management_api.controller.admin.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BillPageResp {

    private Long id;

    private Long userId;

    private String title;

    private Integer type;

    private BigDecimal amount;

    private Date createdTime;

    private Date updatedTime;
}
