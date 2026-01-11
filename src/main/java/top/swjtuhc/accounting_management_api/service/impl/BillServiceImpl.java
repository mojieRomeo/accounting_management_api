package top.swjtuhc.accounting_management_api.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import top.swjtuhc.accounting_management_api.controller.admin.req.BillPageReq;
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillPageResp;
import top.swjtuhc.accounting_management_api.entity.Bill;
import top.swjtuhc.accounting_management_api.entity.User;
import top.swjtuhc.accounting_management_api.enums.UserRoleEnum;
import top.swjtuhc.accounting_management_api.exception.BusinessException;
import top.swjtuhc.accounting_management_api.mapper.UserMapper;
import top.swjtuhc.accounting_management_api.service.BillService;
import top.swjtuhc.accounting_management_api.mapper.BillMapper;
import org.springframework.stereotype.Service;
import top.swjtuhc.accounting_management_api.util.ExceptionMessage;
import top.swjtuhc.accounting_management_api.util.PageRequest;
import top.swjtuhc.accounting_management_api.util.PageResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author luojunjie
* @description 针对表【bill】的数据库操作Service实现
* @createDate 2025-11-30 15:10:18
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill>
    implements BillService{

    private final BillMapper billMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public PageResponse<BillPageResp> getBillPage(BillPageReq req) {
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        Integer currentRole = (Integer) session.get("role");
        String key = "bill:"+"getBillPage:"+"currentRole:"+currentRole+":"+"current:"+req.getCurrent()+":"+"size:"+req.getSize()+":"+"costType:"+req.getCostType();
        List<BillPageResp> cached = (List<BillPageResp>) redisTemplate.opsForValue().get(key);
        if(CollectionUtils.isNotEmpty(cached)){
            return new PageResponse<>(req.getCurrent(), (long) cached.size(), req.getSize(), cached);
        }
        synchronized (key.intern()){
            List<BillPageResp> reCached = (List<BillPageResp>) redisTemplate.opsForValue().get(key);
            if(CollectionUtils.isNotEmpty(reCached)){
                return new PageResponse<>(req.getCurrent(), (long) reCached.size(), req.getSize(), reCached);
            }
            Page<Bill> page = new Page<>(req.getCurrent(), req.getSize());
            LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Bill::getId);
            if(currentRole.equals(UserRoleEnum.ADMIN.getCode())){
                List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole,UserRoleEnum.USER.getCode())).stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(Bill::getUserId,userIds);
                //StringUtils.hasText(req.getCostType())意思为req.getCostType()有值就执行，null，“”，“ ”都为false
                if(StringUtils.hasText(req.getCostType())){
                    wrapper.eq(Bill::getCostType,req.getCostType());
                }
            } else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
                List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getRole,UserRoleEnum.USER.getCode(),UserRoleEnum.ADMIN.getCode())).stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(Bill::getUserId,userIds);
                if(StringUtils.hasText(req.getCostType())){
                    wrapper.eq(Bill::getCostType,req.getCostType());
                }
            }
            Page<Bill> result = page(page, wrapper);
            List<Bill> record = result.getRecords();
            List<BillPageResp> respList = BeanUtil.copyToList(record, BillPageResp.class);
            if(respList.isEmpty()){
                log.info("redis缓存写入空值key:{}",key);
                redisTemplate.opsForValue().set(key, Collections.emptyList(),300, TimeUnit.SECONDS);
                return new PageResponse<>(result,Collections.emptyList());
            }
            log.info("redis缓存写入key:{}",key);
            redisTemplate.opsForValue().set(key,respList,300, TimeUnit.SECONDS);
            return new PageResponse<>(result, respList);
        }
        }
}




