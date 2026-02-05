package top.swjtuhc.accounting_management_api.controller.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.swjtuhc.accounting_management_api.controller.admin.req.BillPageReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillPageResp;
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillTrendResp;
import top.swjtuhc.accounting_management_api.entity.Bill;
import top.swjtuhc.accounting_management_api.service.BillService;
import top.swjtuhc.accounting_management_api.util.PageResponse;
import top.swjtuhc.accounting_management_api.util.ResponseEntity;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;
    @PostMapping("/getBillPage")
    public ResponseEntity<PageResponse<BillPageResp>> getBillPage(@RequestBody BillPageReq req){
        return ResponseEntity.ok(billService.getBillPage(req));
    }


    @PostMapping("/getBillTrend")
    public ResponseEntity<BillTrendResp> getBillTrend(){
        return ResponseEntity.ok(billService.getBillTrend());

    }



}
