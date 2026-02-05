package top.swjtuhc.accounting_management_api.controller.admin.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillTrendResp {

    private List<String> xAxis;

    private List<BigDecimal> income;

    private List<BigDecimal> expense;

}
