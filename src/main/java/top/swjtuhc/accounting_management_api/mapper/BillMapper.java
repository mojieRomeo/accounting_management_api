package top.swjtuhc.accounting_management_api.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.swjtuhc.accounting_management_api.entity.Bill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author luojunjie
* @description 针对表【bill】的数据库操作Mapper
* @createDate 2025-11-30 15:10:18
* @Entity top.swjtuhc.accounting_management_api.entity.Bill
*/
@Mapper
public interface BillMapper extends BaseMapper<Bill> {

}




