package top.swjtuhc.accounting_management_api.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
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
import top.swjtuhc.accounting_management_api.controller.admin.resp.BillTrendResp;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
        String key = "bill:" + "getBillPage:" + "currentRole:" + currentRole + ":" + "current:" + req.getCurrent() + ":" + "size:" + req.getSize() + ":" + "costType:" + req.getCostType();
        //totalKey的作用是为了缓存每次查的total
        String totalKey = "bill:" + "getBillPage:" + "currentRole:" + currentRole + ":" + "costType:" + req.getCostType();
        List<BillPageResp> cached = (List<BillPageResp>) redisTemplate.opsForValue().get(key);
        /*
        1.为啥要用Number：避免java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.Long

        因为你目前redisConfig用的是GenericJackson2JsonRedisSerializer，它对数字存储一律是Integer，就算你set(totalKey,0L,300,TimeUnit.SECONDS)，也当作0存储而不是0L
        set(totalKey,result.getTotal(),300,TimeUnit.SECONDS)的result.getTotal()原本是long，但会被当成Integer存储,所以redisTemplate.opsForValue().get(totalKey)获取到的是Integer
        所以你要用Number，Number是Integer和Long的父类，Number numberToTotal = (Number) redisTemplate.opsForValue().get(totalKey);
        #redis缓存数字的时候一律用Number

        2.long total = numberToTotal == null ? 0L :numberToTotal.longValue();不能只写成long total = numberToTotal.longValue();
        因为当你第一次查或者totalKey过期了，numberToTotal为null，直接numberToTotal.longValue()会报空指针异常
         */
        Number numberToTotal = (Number) redisTemplate.opsForValue().get(totalKey);
        long total = numberToTotal == null ? 0L : numberToTotal.longValue();
        if (CollectionUtils.isNotEmpty(cached)) {
            return new PageResponse<>(req.getCurrent(), total, req.getSize(), cached);
        }
        synchronized (key.intern()) {
            List<BillPageResp> reCached = (List<BillPageResp>) redisTemplate.opsForValue().get(key);
            Number numberToRetotal = (Number) redisTemplate.opsForValue().get(totalKey);
            long reTotal = numberToRetotal == null ? 0L : numberToRetotal.longValue();
            if (CollectionUtils.isNotEmpty(reCached)) {
                return new PageResponse<>(req.getCurrent(), reTotal, req.getSize(), reCached);
            }
            Page<Bill> page = new Page<>(req.getCurrent(), req.getSize());
            LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Bill::getId);
            if (currentRole.equals(UserRoleEnum.ADMIN.getCode())) {
                List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, UserRoleEnum.USER.getCode())).stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(Bill::getUserId, userIds);
                //StringUtils.hasText(req.getCostType())意思为req.getCostType()有值就执行，null，“”，“ ”都为false
                if (StringUtils.hasText(req.getCostType())) {
                    wrapper.eq(Bill::getCostType, req.getCostType());
                }
            } else if (currentRole.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
                List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getRole, UserRoleEnum.USER.getCode(), UserRoleEnum.ADMIN.getCode())).stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(Bill::getUserId, userIds);
                if (StringUtils.hasText(req.getCostType())) {
                    wrapper.eq(Bill::getCostType, req.getCostType());
                }
            } else {
                wrapper.eq(Bill::getUserId, StpUtil.getLoginIdAsLong());
                if (StringUtils.hasText(req.getCostType())) {
                    wrapper.eq(Bill::getCostType, req.getCostType());
                }
            }
            Page<Bill> result = page(page, wrapper);
            List<Bill> record = result.getRecords();
            List<BillPageResp> respList = BeanUtil.copyToList(record, BillPageResp.class);
            if (respList.isEmpty()) {
                log.info("redis缓存写入空值key:{}", key);
                redisTemplate.opsForValue().set(key, Collections.emptyList(), 300, TimeUnit.SECONDS);
                log.info("redis写入空值totalKey:{}", totalKey);
                redisTemplate.opsForValue().set(totalKey, 0L, 300, TimeUnit.SECONDS);
                return new PageResponse<>(result, Collections.emptyList());
            }
            log.info("redis缓存写入key:{}", key);
            redisTemplate.opsForValue().set(key, respList, 300, TimeUnit.SECONDS);
            log.info("redis写入total总数totalKey:{}", totalKey);
            //set(totalKey,result.getTotal(),300,TimeUnit.SECONDS);不能写respList.size()因为这个是当前页总条数，而result.getTotal()才是当前查询的总条数
            redisTemplate.opsForValue().set(totalKey, result.getTotal(), 300, TimeUnit.SECONDS);
            return new PageResponse<>(result, respList);
        }
    }

    @Override
    public BillTrendResp getBillTrend() {
        SaSession session = StpUtil.getSessionByLoginId(StpUtil.getLoginIdAsLong());
        Integer role = (Integer) session.get("role");

        // 1. 时间轴（最近 7 天）
        List<String> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            days.add(today.minusDays(i).toString());
        }

        // 2. 查询条件
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();

        // 权限控制
        if (role.equals(UserRoleEnum.USER.getCode())) {
            // 普通用户只看自己
            wrapper.eq(Bill::getUserId, StpUtil.getLoginIdAsLong());
        } else if (role.equals(UserRoleEnum.ADMIN.getCode())) {
            // 管理员看所有普通用户
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getRole, UserRoleEnum.USER.getCode())
            ).stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(Bill::getUserId, userIds);
        } else if (role.equals(UserRoleEnum.SUPER_ADMIN.getCode())) {
            // 超管看普通用户 + 管理员
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getRole, UserRoleEnum.USER.getCode(), UserRoleEnum.ADMIN.getCode())
            ).stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(Bill::getUserId, userIds);
        }

        // 3. 最近 7 天数据
        wrapper.ge(Bill::getCreatedTime,
                Date.from(today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
        wrapper.select(Bill::getType, Bill::getCreatedTime, Bill::getAmount);

        List<Bill> bills = list(wrapper);

        // 4. 聚合
        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        for (Bill bill : bills) {
            String day = DateUtil.format(bill.getCreatedTime(), "yyyy-MM-dd");
            if (bill.getType() == 1) {
                incomeMap.merge(day, bill.getAmount(), BigDecimal::add);
            } else if (bill.getType() == 2) {
                expenseMap.merge(day, bill.getAmount(), BigDecimal::add);
            }
        }

        // 5. 对齐时间轴 + 补 0
        List<BigDecimal> income = new ArrayList<>();
        List<BigDecimal> expense = new ArrayList<>();
        for (String day : days) {
            income.add(incomeMap.getOrDefault(day, BigDecimal.ZERO));
            expense.add(expenseMap.getOrDefault(day, BigDecimal.ZERO));
        }

        BillTrendResp resp = new BillTrendResp();
        resp.setXAxis(days);
        resp.setIncome(income);
        resp.setExpense(expense);

        return resp;
    }
}




