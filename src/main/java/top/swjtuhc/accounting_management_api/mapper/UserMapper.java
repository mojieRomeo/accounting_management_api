package top.swjtuhc.accounting_management_api.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.swjtuhc.accounting_management_api.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import javax.management.Query;

/**
* @author luojunjie
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-11-30 15:09:28
* @Entity top.swjtuhc.accounting_management_api.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




