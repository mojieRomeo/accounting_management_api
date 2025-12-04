package top.swjtuhc.accounting_management_api.controller.admin.req;

import lombok.Data;
import top.swjtuhc.accounting_management_api.util.PageRequest;

import java.math.BigDecimal;

@Data
public class BillPageReq extends PageRequest {

    private Long id;

    private Long userId;

    private String title;

    private Integer type;

    private BigDecimal amount;

    private String keyword;

}
