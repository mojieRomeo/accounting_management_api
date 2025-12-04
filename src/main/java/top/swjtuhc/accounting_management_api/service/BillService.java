package top.swjtuhc.accounting_management_api.service;

import top.swjtuhc.accounting_management_api.controller.admin.req.BillPageReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillPageResp;
import top.swjtuhc.accounting_management_api.entity.Bill;
import com.baomidou.mybatisplus.extension.service.IService;
import top.swjtuhc.accounting_management_api.util.PageResponse;

/**
* @author luojunjie
* @description 针对表【bill】的数据库操作Service
* @createDate 2025-11-30 15:10:18
*/
public interface BillService extends IService<Bill> {


    PageResponse<BillPageResp> getBillPage(BillPageReq req);
}
