package top.swjtuhc.accounting_management_api.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.swjtuhc.accounting_management_api.controller.admin.req.BillPageReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillPageResp;
import top.swjtuhc.accounting_management_api.service.BillService;
import top.swjtuhc.accounting_management_api.util.PageResponse;
import top.swjtuhc.accounting_management_api.util.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {
    private final BillService billService;
    @PostMapping("/page")
    public ResponseEntity<PageResponse<BillPageResp>> getBillPage(BillPageReq req) {
        return ResponseEntity.ok(billService.getBillPage(req));
    }
}
