package top.swjtuhc.accounting_management_api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.swjtuhc.accounting_management_api.controller.admin.req.BillPageReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillPageResp;
import top.swjtuhc.accounting_management_api.entity.Bill;
import top.swjtuhc.accounting_management_api.service.BillService;
import top.swjtuhc.accounting_management_api.mapper.BillMapper;
import org.springframework.stereotype.Service;
import top.swjtuhc.accounting_management_api.util.PageRequest;
import top.swjtuhc.accounting_management_api.util.PageResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author luojunjie
* @description 针对表【bill】的数据库操作Service实现
* @createDate 2025-11-30 15:10:18
*/
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill>
    implements BillService{


    @Override
    public PageResponse<BillPageResp> getBillPage(BillPageReq req) {
        Page<Bill> page = new Page<>(req.getCurrent(), req.getSize());
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getKeyword,req.getKeyword());
        Page<Bill> result = page(page, wrapper);
        List<Bill> record = result.getRecords();
        List<BillPageResp> respList = record.stream().map(bill -> {
            BillPageResp resp = new BillPageResp();
            resp.setId(bill.getId());
            resp.setUserId(bill.getUserId());
            resp.setTitle(bill.getTitle());
            resp.setType(bill.getType());
            resp.setAmount(bill.getAmount());
            resp.setCreatedTime(bill.getCreatedTime());
            resp.setUpdatedTime(bill.getUpdatedTime());
            return resp;
        }).collect(Collectors.toList());
        if(respList.isEmpty()){
            return new PageResponse<>(result, Collections.emptyList());
        }
        return new PageResponse<>(result,respList);
    }
}




