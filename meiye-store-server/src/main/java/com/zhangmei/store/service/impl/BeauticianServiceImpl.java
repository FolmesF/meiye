package com.zhangmei.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.booking.BookingClient;
import com.zhangmei.store.dto.BeauticianBoardRow;
import com.zhangmei.store.dto.BeauticianForms;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.BeauticianSchedule;
import com.zhangmei.store.entity.BeauticianServiceBind;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.mapper.BeauticianMapper;
import com.zhangmei.store.mapper.BeauticianScheduleMapper;
import com.zhangmei.store.mapper.BeauticianServiceBindMapper;
import com.zhangmei.store.mapper.ServiceItemMapper;
import com.zhangmei.store.service.BeauticianService;
import com.zhangmei.store.service.Checks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BeauticianServiceImpl implements BeauticianService {

    private static final Set<String> STATUSES = Set.of("ACTIVE", "LEFT");
    private static final Set<String> SHIFTS = Set.of("ON_DUTY", "OFF", "LEAVE");

    private final BeauticianMapper beauticianMapper;
    private final BeauticianServiceBindMapper bindMapper;
    private final BeauticianScheduleMapper scheduleMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final BookingClient bookingClient;

    @Override
    public Long create(BeauticianForms.Save request) {
        Checks.gender(request.getGender());
        Beautician beautician = new Beautician();
        beautician.setStoreId(request.getStoreId());
        beautician.setName(request.getName().trim());
        beautician.setMobile(request.getMobile());
        beautician.setGender(request.getGender());
        beautician.setLevel(request.getLevel());
        beautician.setStatus("ACTIVE");
        beautician.setEntryDate(request.getEntryDate() == null ? LocalDate.now() : request.getEntryDate());
        beautician.setSessionFee(request.getSessionFee() == null ? new BigDecimal("128.00") : request.getSessionFee());
        beautician.setRemark(request.getRemark());
        beauticianMapper.insert(beautician);
        return beautician.getId();
    }

    @Override
    public void update(Long id, BeauticianForms.Update request) {
        Beautician beautician = mustGet(id);
        Checks.gender(request.getGender());
        if (!STATUSES.contains(request.getStatus())) {
            throw new BizException("在职状态只允许 ACTIVE 或 LEFT");
        }
        beautician.setName(request.getName().trim());
        beautician.setMobile(request.getMobile());
        beautician.setGender(request.getGender());
        beautician.setLevel(request.getLevel());
        beautician.setStatus(request.getStatus());
        beautician.setEntryDate(request.getEntryDate());
        beautician.setRemark(request.getRemark());
        beauticianMapper.updateById(beautician);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mustGet(id);
        List<Long> serviceIds = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceBind>()
                        .eq(BeauticianServiceBind::getBeauticianId, id))
                .stream()
                .map(BeauticianServiceBind::getServiceItemId)
                .distinct()
                .toList();
        bindMapper.delete(new LambdaQueryWrapper<BeauticianServiceBind>()
                .eq(BeauticianServiceBind::getBeauticianId, id));
        beauticianMapper.deleteById(id);
        for (Long serviceId : serviceIds) {
            ServiceItem item = serviceItemMapper.selectById(serviceId);
            if (item == null) {
                continue;
            }
            List<Long> staffIds = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceBind>()
                            .eq(BeauticianServiceBind::getServiceItemId, serviceId))
                    .stream()
                    .map(BeauticianServiceBind::getBeauticianId)
                    .toList();
            List<Beautician> staff = staffIds.isEmpty() ? List.of() : beauticianMapper.selectBatchIds(staffIds);
            bookingClient.syncProject(item, staff);
        }
    }

    @Override
    public Beautician detail(Long id) {
        return mustGet(id);
    }

    @Override
    public PageResult<Beautician> page(Long storeId, String status, String keyword, long page, long size) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        LambdaQueryWrapper<Beautician> wrapper = new LambdaQueryWrapper<Beautician>()
                .eq(Beautician::getStoreId, storeId)
                .eq(StringUtils.hasText(status), Beautician::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w.like(Beautician::getName, keyword).or().like(Beautician::getMobile, keyword))
                .orderByDesc(Beautician::getId);
        Page<Beautician> result = beauticianMapper.selectPage(new Page<>(Checks.page(page), Checks.size(size)), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public List<BeauticianBoardRow> board(Long storeId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        Map<Long, BigDecimal> scores = bookingClient.beauticianScores();
        return beauticianMapper.selectList(new LambdaQueryWrapper<Beautician>()
                        .eq(Beautician::getStoreId, storeId)
                        .orderByAsc(Beautician::getId))
                .stream()
                .map(item -> {
                    BeauticianBoardRow row = new BeauticianBoardRow();
                    row.setId(item.getId());
                    row.setName(item.getName());
                    row.setEntryDate(item.getEntryDate());
                    row.setMobile(item.getMobile());
                    row.setLevel(item.getLevel());
                    row.setScore(scores.get(item.getId()));
                    row.setSessionFee(item.getSessionFee() == null ? new BigDecimal("128.00") : item.getSessionFee());
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional
    public void bindServices(Long beauticianId, List<Long> serviceItemIds) {
        Beautician beautician = mustGet(beauticianId);
        Set<Long> itemIds = new LinkedHashSet<>(serviceItemIds == null ? List.of() : serviceItemIds);
        List<ServiceItem> items = itemIds.isEmpty() ? List.of() : serviceItemMapper.selectBatchIds(itemIds);
        if (items.size() != itemIds.size()) {
            throw new BizException("存在无效的服务项目");
        }
        for (ServiceItem item : items) {
            if (!item.getStoreId().equals(beautician.getStoreId())) {
                throw new BizException("服务项目与美容师不属于同一门店");
            }
        }
        bindMapper.delete(new LambdaQueryWrapper<BeauticianServiceBind>()
                .eq(BeauticianServiceBind::getBeauticianId, beauticianId));
        for (Long itemId : itemIds) {
            BeauticianServiceBind bind = new BeauticianServiceBind();
            bind.setStoreId(beautician.getStoreId());
            bind.setBeauticianId(beauticianId);
            bind.setServiceItemId(itemId);
            bindMapper.insert(bind);
        }
    }

    @Override
    public List<ServiceItem> listServices(Long beauticianId) {
        mustGet(beauticianId);
        List<Long> itemIds = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceBind>()
                        .eq(BeauticianServiceBind::getBeauticianId, beauticianId))
                .stream()
                .map(BeauticianServiceBind::getServiceItemId)
                .toList();
        if (itemIds.isEmpty()) {
            return List.of();
        }
        return serviceItemMapper.selectBatchIds(itemIds);
    }

    @Override
    @Transactional
    public BeauticianSchedule saveSchedule(Long beauticianId, BeauticianForms.Schedule request) {
        Beautician beautician = mustGet(beauticianId);
        if (!SHIFTS.contains(request.getShiftStatus())) {
            throw new BizException("排班状态只允许 ON_DUTY、OFF、LEAVE");
        }
        if ("ON_DUTY".equals(request.getShiftStatus())) {
            if (request.getShiftStart() == null || request.getShiftEnd() == null
                    || !request.getShiftEnd().isAfter(request.getShiftStart())) {
                throw new BizException("在岗排班需要有效的起止时间");
            }
        }
        BeauticianSchedule existing = scheduleMapper.selectOne(new LambdaQueryWrapper<BeauticianSchedule>()
                .eq(BeauticianSchedule::getBeauticianId, beauticianId)
                .eq(BeauticianSchedule::getWorkDate, request.getWorkDate()));
        if (existing == null) {
            existing = new BeauticianSchedule();
            existing.setStoreId(beautician.getStoreId());
            existing.setBeauticianId(beauticianId);
            existing.setWorkDate(request.getWorkDate());
        }
        existing.setShiftStatus(request.getShiftStatus());
        existing.setShiftStart(request.getShiftStart());
        existing.setShiftEnd(request.getShiftEnd());
        existing.setRemark(request.getRemark());
        if (existing.getId() == null) {
            scheduleMapper.insert(existing);
            return existing;
        }
        scheduleMapper.update(null, new LambdaUpdateWrapper<BeauticianSchedule>()
                .eq(BeauticianSchedule::getId, existing.getId())
                .set(BeauticianSchedule::getShiftStatus, request.getShiftStatus())
                .set(BeauticianSchedule::getShiftStart, request.getShiftStart())
                .set(BeauticianSchedule::getShiftEnd, request.getShiftEnd())
                .set(BeauticianSchedule::getRemark, request.getRemark()));
        return existing;
    }

    @Override
    public List<BeauticianSchedule> listSchedules(Long beauticianId, LocalDate from, LocalDate to) {
        mustGet(beauticianId);
        if (from == null || to == null || to.isBefore(from)) {
            throw new BizException("排班查询日期无效");
        }
        return scheduleMapper.selectList(new LambdaQueryWrapper<BeauticianSchedule>()
                .eq(BeauticianSchedule::getBeauticianId, beauticianId)
                .ge(BeauticianSchedule::getWorkDate, from)
                .le(BeauticianSchedule::getWorkDate, to)
                .orderByAsc(BeauticianSchedule::getWorkDate));
    }

    private Beautician mustGet(Long id) {
        Beautician beautician = beauticianMapper.selectById(id);
        if (beautician == null) {
            throw new BizException(404, "美容师不存在");
        }
        return beautician;
    }
}
