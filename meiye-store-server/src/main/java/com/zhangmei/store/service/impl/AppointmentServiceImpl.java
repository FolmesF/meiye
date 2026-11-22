package com.zhangmei.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangmei.store.booking.BookingClient;
import com.zhangmei.store.common.AccountNo;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.dto.AppointmentForms;
import com.zhangmei.store.dto.AppointmentRecord;
import com.zhangmei.store.dto.AppointmentStatQuery;
import com.zhangmei.store.dto.AppointmentStatResponse;
import com.zhangmei.store.dto.AppointmentStatRow;
import com.zhangmei.store.entity.Appointment;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.BeauticianServiceBind;
import com.zhangmei.store.entity.Customer;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.mapper.AppointmentMapper;
import com.zhangmei.store.mapper.AppointmentStatMapper;
import com.zhangmei.store.mapper.BeauticianMapper;
import com.zhangmei.store.mapper.BeauticianServiceBindMapper;
import com.zhangmei.store.mapper.CustomerMapper;
import com.zhangmei.store.mapper.ServiceItemMapper;
import com.zhangmei.store.service.AppointmentService;
import com.zhangmei.store.service.InventoryService;
import com.zhangmei.store.service.ManualFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final Set<String> DIMENSIONS = Set.of("DAY", "WEEK", "MONTH");
    private static final Set<String> STATUSES = Set.of(
            "PENDING", "CONFIRMED", "ARRIVED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "NO_SHOW");
    private static final Set<String> WAITING = Set.of("PENDING", "CONFIRMED");
    private static final Set<String> RUNNING = Set.of("IN_PROGRESS", "ARRIVED");

    private final AppointmentMapper appointmentMapper;
    private final AppointmentStatMapper appointmentStatMapper;
    private final CustomerMapper customerMapper;
    private final BeauticianMapper beauticianMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final BeauticianServiceBindMapper bindMapper;
    private final ManualFeeService manualFeeService;
    private final InventoryService inventoryService;
    private final BookingClient bookingClient;

    @Override
    public Long create(AppointmentForms.Save request) {
        Customer customer = customerMapper.selectById(request.getCustomerId());
        Beautician beautician = beauticianMapper.selectById(request.getBeauticianId());
        ServiceItem item = serviceItemMapper.selectById(request.getServiceItemId());
        if (customer == null || !customer.getStoreId().equals(request.getStoreId())) {
            throw new BizException("顾客不存在或不属于该门店");
        }
        if (beautician == null || !beautician.getStoreId().equals(request.getStoreId()) || !"ACTIVE".equals(beautician.getStatus())) {
            throw new BizException("美容师不可预约");
        }
        if (item == null || !item.getStoreId().equals(request.getStoreId()) || !"ENABLED".equals(item.getStatus())) {
            throw new BizException("服务项目不可预约");
        }
        Long bound = bindMapper.selectCount(new LambdaQueryWrapper<BeauticianServiceBind>()
                .eq(BeauticianServiceBind::getBeauticianId, beautician.getId())
                .eq(BeauticianServiceBind::getServiceItemId, item.getId()));
        if (bound == 0) {
            throw new BizException("该美容师未绑定此服务项目");
        }
        Appointment appointment = new Appointment();
        appointment.setStoreId(request.getStoreId());
        appointment.setCustomerId(customer.getId());
        appointment.setBeauticianId(beautician.getId());
        appointment.setServiceItemId(item.getId());
        appointment.setAppointTime(request.getAppointTime());
        appointment.setAmount(request.getAmount());
        appointment.setStatus("PENDING");
        appointmentMapper.insert(appointment);
        return appointment.getId();
    }

    @Override
    public Appointment detail(Long id) {
        return mustGet(id);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, AppointmentForms.StatusChange request) {
        if (!STATUSES.contains(request.getStatus())) {
            throw new BizException("预约状态无效");
        }
        Appointment appointment = mustGet(id);
        boolean becomeCompleted = "COMPLETED".equals(request.getStatus()) && !"COMPLETED".equals(appointment.getStatus());
        appointment.setStatus(request.getStatus());
        if ("CANCELLED".equals(request.getStatus())) {
            appointment.setCancelReason(request.getCancelReason());
        }
        appointmentMapper.updateById(appointment);
        if (becomeCompleted) {
            manualFeeService.settle(appointment);
        }
    }

    @Override
    public List<AppointmentStatResponse> stats(AppointmentStatQuery query) {
        if (!DIMENSIONS.contains(query.getDimension())) {
            throw new BizException("统计维度只允许 DAY、WEEK、MONTH");
        }
        if (query.getEndDate().isBefore(query.getStartDate())) {
            throw new BizException("结束日期不能早于开始日期");
        }
        if (ChronoUnit.DAYS.between(query.getStartDate(), query.getEndDate()) > 366) {
            throw new BizException("统计区间不能超过 366 天");
        }
        LocalDateTime start = query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate().plusDays(1).atStartOfDay();
        return appointmentStatMapper.aggregate(query.getStoreId(), query.getDimension(), start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    private AppointmentStatResponse toResponse(AppointmentStatRow row) {
        long total = row.getAppointmentCount() == null ? 0 : row.getAppointmentCount();
        long arrived = row.getArrivedCount() == null ? 0 : row.getArrivedCount();
        long cancelled = row.getCancelledCount() == null ? 0 : row.getCancelledCount();
        AppointmentStatResponse response = new AppointmentStatResponse();
        response.setPeriod(row.getPeriod());
        response.setAppointmentCount(total);
        response.setArrivedCount(arrived);
        response.setCancelledCount(cancelled);
        response.setArrivalRate(ratio(arrived, total));
        response.setCancelRate(ratio(cancelled, total));
        return response;
    }

    private BigDecimal ratio(long part, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.UNNECESSARY);
        }
        return BigDecimal.valueOf(part).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    @Override
    public List<AppointmentRecord> records(Long storeId, LocalDate startDate, LocalDate endDate,
                                           String serviceName, String beauticianName, String mobile, String phase) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        LocalDate end = endDate == null ? start : endDate;
        if (end.isBefore(start)) {
            throw new BizException("结束日期不能早于开始日期");
        }
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStoreId, storeId)
                .ge(Appointment::getAppointTime, start.atStartOfDay())
                .lt(Appointment::getAppointTime, end.plusDays(1).atStartOfDay())
                .orderByAsc(Appointment::getAppointTime);
        if (serviceName != null && !serviceName.isBlank()) {
            wrapper.like(Appointment::getServiceName, serviceName.trim());
        }
        if (beauticianName != null && !beauticianName.isBlank()) {
            wrapper.like(Appointment::getBeauticianName, beauticianName.trim());
        }
        if (mobile != null && !mobile.isBlank()) {
            wrapper.like(Appointment::getCustomerMobile, mobile.trim());
        }
        if (phase != null && !phase.isBlank()) {
            wrapper.in(Appointment::getStatus, statusesOf(phase.trim()));
        }
        return toRecords(appointmentMapper.selectList(wrapper));
    }

    @Override
    public List<AppointmentRecord> workable(Long storeId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        List<Appointment> rows = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStoreId, storeId)
                .in(Appointment::getStatus, "PENDING", "CONFIRMED", "ARRIVED", "IN_PROGRESS")
                .orderByAsc(Appointment::getAppointTime));
        return toRecords(rows);
    }

    @Override
    @Transactional
    public void start(Long id) {
        Appointment appointment = mustGet(id);
        if (!WAITING.contains(appointment.getStatus())) {
            throw new BizException("只有等待中的预约可以开始");
        }
        appointment.setStatus("IN_PROGRESS");
        appointmentMapper.updateById(appointment);
        if (appointment.getCustomerId() != null) {
            customerMapper.update(null, new LambdaUpdateWrapper<Customer>()
                    .eq(Customer::getId, appointment.getCustomerId())
                    .set(Customer::getLastVisitAt, LocalDateTime.now()));
        }
    }

    @Override
    @Transactional
    public void finish(Long id) {
        Appointment appointment = mustGet(id);
        if (!RUNNING.contains(appointment.getStatus())) {
            throw new BizException("只有进行中的预约可以结束");
        }
        inventoryService.consumeForService(appointment.getStoreId(), appointment.getServiceItemId(), appointment.getId());
        appointment.setStatus("COMPLETED");
        appointmentMapper.updateById(appointment);
        if (appointment.getBookingId() != null) {
            bookingClient.completeAppointment(appointment.getBookingId());
        }
    }

    @Override
    @Transactional
    public void syncFromBooking(AppointmentForms.BookingSync request) {
        if (!Set.of("PENDING", "CONFIRMED", "CANCELLED").contains(request.getStatus())) {
            throw new BizException("预约状态无效");
        }
        if (!request.getAppointEnd().isAfter(request.getAppointTime())) {
            throw new BizException("结束时间必须晚于开始时间");
        }
        String accountNo = request.getAccountNo() == null || request.getAccountNo().isBlank()
                ? AccountNo.of(request.getMobile()) : request.getAccountNo().trim();
        Customer customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getStoreId, request.getStoreId())
                .eq(Customer::getAccountNo, accountNo)
                .last("limit 1"));
        if (customer == null) {
            customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getStoreId, request.getStoreId())
                    .eq(Customer::getMobile, request.getMobile())
                    .last("limit 1"));
        }
        if (customer == null) {
            customer = new Customer();
            customer.setStoreId(request.getStoreId());
            customer.setName(request.getCustomerName().trim());
            customer.setMobile(request.getMobile());
            customer.setAccountNo(accountNo);
            customer.setGender(blankGender(request.getGender()));
            customer.setLevel(blankLevel(request.getMemberLevel()));
            customer.setSource("预约端");
            customer.setStatus("ACTIVE");
            customer.setTotalAmount(BigDecimal.ZERO);
            customer.setVisitCount(0);
            customerMapper.insert(customer);
        } else if (customer.getAccountNo() == null || customer.getAccountNo().isBlank()) {
            customer.setAccountNo(accountNo);
            customerMapper.updateById(customer);
        }
        Appointment appointment = appointmentMapper.selectOne(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getBookingId, request.getBookingId())
                .last("limit 1"));
        boolean created = appointment == null;
        if (created) {
            appointment = new Appointment();
            appointment.setBookingId(request.getBookingId());
            appointment.setStoreId(request.getStoreId());
        }
        appointment.setCustomerId(customer.getId());
        appointment.setBeauticianId(request.getBeauticianId());
        appointment.setServiceItemId(request.getServiceItemId());
        appointment.setAppointTime(request.getAppointTime());
        appointment.setAppointEnd(request.getAppointEnd());
        appointment.setCustomerName(request.getCustomerName().trim());
        appointment.setCustomerMobile(request.getMobile());
        appointment.setServiceName(request.getServiceName().trim());
        appointment.setBeauticianName(request.getBeauticianName().trim());
        appointment.setAmount(request.getAmount());
        boolean keepProgress = !created && ("IN_PROGRESS".equals(appointment.getStatus()) || "COMPLETED".equals(appointment.getStatus()))
                && "PENDING".equals(request.getStatus());
        if (!keepProgress) {
            appointment.setStatus(request.getStatus());
            appointment.setCancelReason("CANCELLED".equals(request.getStatus()) ? request.getCancelReason() : null);
        }
        if (created) {
            appointmentMapper.insert(appointment);
        } else {
            appointmentMapper.updateById(appointment);
        }
    }

    private List<AppointmentRecord> toRecords(List<Appointment> rows) {
        Set<Long> customerIds = rows.stream().map(Appointment::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Customer> customers = customerIds.isEmpty()
                ? Map.of()
                : customerMapper.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
        return rows.stream().map(row -> toRecord(row, customers.get(row.getCustomerId()))).toList();
    }

    private AppointmentRecord toRecord(Appointment row, Customer customer) {
        LocalDateTime end = row.getAppointEnd() == null ? row.getAppointTime() : row.getAppointEnd();
        AppointmentRecord record = new AppointmentRecord();
        record.setId(row.getId());
        record.setPhase(phaseOf(row.getStatus()));
        record.setAppointDate(DATE.format(row.getAppointTime()));
        record.setTimeRange(CLOCK.format(row.getAppointTime()) + "-" + CLOCK.format(end));
        record.setCustomerName(row.getCustomerName());
        record.setMobile(row.getCustomerMobile());
        String accountNo = customer == null ? null : customer.getAccountNo();
        if (accountNo == null || accountNo.isBlank()) {
            accountNo = row.getCustomerMobile() == null ? "" : AccountNo.of(row.getCustomerMobile());
        }
        record.setAccountNo(accountNo);
        record.setServiceName(row.getServiceName());
        record.setBeauticianName(row.getBeauticianName());
        record.setCancelled("CANCELLED".equals(row.getStatus()) || "NO_SHOW".equals(row.getStatus()));
        record.setExpired(!end.isAfter(LocalDateTime.now()));
        return record;
    }

    private String phaseOf(String status) {
        if (RUNNING.contains(status)) {
            return "进行中";
        }
        if ("COMPLETED".equals(status)) {
            return "已完成";
        }
        if ("CANCELLED".equals(status) || "NO_SHOW".equals(status)) {
            return "已取消";
        }
        return "等待中";
    }

    private Set<String> statusesOf(String phase) {
        return switch (phase) {
            case "等待中" -> WAITING;
            case "进行中" -> RUNNING;
            case "已取消" -> Set.of("CANCELLED", "NO_SHOW");
            case "已完成" -> Set.of("COMPLETED");
            default -> throw new BizException("预约状态只允许 等待中、进行中、已取消、已完成");
        };
    }

    private String blankGender(String gender) {
        if (gender == null || gender.isBlank() || !Set.of("MALE", "FEMALE", "UNKNOWN").contains(gender)) {
            return "UNKNOWN";
        }
        return gender;
    }

    private String blankLevel(String level) {
        return level == null || level.isBlank() ? "普通会员" : level.trim();
    }

    private Appointment mustGet(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        return appointment;
    }
}
