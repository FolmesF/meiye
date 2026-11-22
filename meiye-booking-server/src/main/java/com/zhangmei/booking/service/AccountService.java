package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.booking.common.AccountNo;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.common.PageResult;
import com.zhangmei.booking.common.UserContext;
import com.zhangmei.booking.dto.AppointmentView;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.dto.ProfileView;
import com.zhangmei.booking.entity.BeauticianSnapshot;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.entity.BookingConsumption;
import com.zhangmei.booking.entity.BookingUser;
import com.zhangmei.booking.entity.Review;
import com.zhangmei.booking.entity.ServiceSnapshot;
import com.zhangmei.booking.entity.UserTag;
import com.zhangmei.booking.mapper.BeauticianSnapshotMapper;
import com.zhangmei.booking.mapper.BookingAppointmentMapper;
import com.zhangmei.booking.mapper.BookingConsumptionMapper;
import com.zhangmei.booking.mapper.BookingUserMapper;
import com.zhangmei.booking.mapper.ReviewMapper;
import com.zhangmei.booking.mapper.ServiceSnapshotMapper;
import com.zhangmei.booking.mapper.UserTagMapper;
import com.zhangmei.booking.store.StoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Set<String> GENDERS = Set.of("MALE", "FEMALE", "UNKNOWN");
    private static final Map<String, List<String>> GROUPS = Map.of(
            "WAITING", List.of("PENDING", "CONFIRMED", "ARRIVED"),
            "DONE", List.of("COMPLETED"),
            "CANCELLED", List.of("CANCELLED")
    );

    private final BookingUserMapper userMapper;
    private final BookingAppointmentMapper appointmentMapper;
    private final BookingConsumptionMapper consumptionMapper;
    private final BeauticianSnapshotMapper beauticianMapper;
    private final ServiceSnapshotMapper serviceMapper;
    private final ReviewMapper reviewMapper;
    private final UserTagMapper userTagMapper;
    private final StoreClient storeClient;

    public ProfileView profile() {
        return toProfile(requireUser());
    }

    @Transactional
    public void updateProfile(BookingForms.ProfileUpdate request) {
        String gender = blankToNull(request.getGender());
        if (gender != null && !GENDERS.contains(gender)) {
            throw new BizException("性别只允许 MALE、FEMALE、UNKNOWN");
        }
        List<String> tags = normalizeTags(request.getTags());
        BookingUser user = requireUser();
        String accountNo = user.getAccountNo() == null || user.getAccountNo().isBlank()
                ? AccountNo.of(user.getMobile()) : user.getAccountNo();
        String mobile = request.getMobile().trim();
        if (!mobile.equals(user.getMobile())) {
            Long used = userMapper.selectCount(new LambdaQueryWrapper<BookingUser>().eq(BookingUser::getMobile, mobile));
            if (used > 0) {
                throw new BizException("手机号已被使用");
            }
        }
        userMapper.update(null, new LambdaUpdateWrapper<BookingUser>()
                .eq(BookingUser::getId, user.getId())
                .set(BookingUser::getAccountNo, accountNo)
                .set(BookingUser::getNickname, request.getNickname().trim())
                .set(BookingUser::getMobile, mobile)
                .set(BookingUser::getGender, gender)
                .set(BookingUser::getBirthday, request.getBirthday())
                .set(BookingUser::getAddress, blankToNull(request.getAddress())));
        userTagMapper.delete(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, user.getId()));
        for (String tag : tags) {
            UserTag row = new UserTag();
            row.setUserId(user.getId());
            row.setTagName(tag);
            userTagMapper.insert(row);
        }
        user.setAccountNo(accountNo);
        user.setNickname(request.getNickname().trim());
        user.setMobile(mobile);
        user.setGender(gender);
        user.setBirthday(request.getBirthday());
        user.setAddress(blankToNull(request.getAddress()));
        storeClient.syncProfile(user, tags);
    }

    public int syncMemberLevel(String mobile, String level) {
        String value = level == null ? "" : level.trim();
        if (value.isEmpty() || value.length() > 32) {
            throw new BizException("会员等级不能为空且不超过 32 字");
        }
        return userMapper.update(null, new LambdaUpdateWrapper<BookingUser>()
                .eq(BookingUser::getMobile, mobile)
                .set(BookingUser::getMemberLevel, value));
    }

    public PageResult<AppointmentView> appointments(String group, String status, long page, long size) {
        Long userId = UserContext.require();
        LambdaQueryWrapper<BookingAppointment> wrapper = new LambdaQueryWrapper<BookingAppointment>()
                .eq(BookingAppointment::getUserId, userId)
                .orderByDesc(BookingAppointment::getAppointTime);
        if (group != null && !group.isBlank()) {
            List<String> statuses = GROUPS.get(group);
            if (statuses == null) {
                throw new BizException("预约分组只允许 WAITING、DONE、CANCELLED");
            }
            wrapper.in(BookingAppointment::getStatus, statuses);
        } else if (status != null && !status.isBlank()) {
            wrapper.eq(BookingAppointment::getStatus, status);
        }
        Page<BookingAppointment> result = appointmentMapper.selectPage(new Page<>(current(page), pageSize(size)), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), toViews(userId, result.getRecords()));
    }

    public PageResult<BookingConsumption> consumptions(long page, long size) {
        Page<BookingConsumption> result = consumptionMapper.selectPage(new Page<>(current(page), pageSize(size)), new LambdaQueryWrapper<BookingConsumption>()
                .eq(BookingConsumption::getUserId, UserContext.require())
                .orderByDesc(BookingConsumption::getConsumedAt));
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    private List<AppointmentView> toViews(Long userId, List<BookingAppointment> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Set<Long> beauticianIds = records.stream().map(BookingAppointment::getBeauticianId).collect(Collectors.toSet());
        Set<Long> serviceIds = records.stream().map(BookingAppointment::getServiceItemId).collect(Collectors.toSet());
        Set<Long> appointmentIds = records.stream().map(BookingAppointment::getId).collect(Collectors.toSet());
        Map<Long, BeauticianSnapshot> beauticians = beauticianMapper.selectBatchIds(beauticianIds).stream()
                .collect(Collectors.toMap(BeauticianSnapshot::getId, Function.identity()));
        Map<Long, ServiceSnapshot> services = serviceMapper.selectBatchIds(serviceIds).stream()
                .collect(Collectors.toMap(ServiceSnapshot::getId, Function.identity()));
        Set<Long> reviewed = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .in(Review::getAppointmentId, appointmentIds))
                .stream()
                .map(Review::getAppointmentId)
                .collect(Collectors.toSet());
        return records.stream().map(item -> {
            AppointmentView view = new AppointmentView();
            view.setId(item.getId());
            view.setStatus(item.getStatus());
            view.setAppointTime(item.getAppointTime());
            view.setAppointEnd(item.getAppointEnd());
            view.setAmount(item.getAmount());
            view.setServiceItemId(item.getServiceItemId());
            view.setBeauticianId(item.getBeauticianId());
            ServiceSnapshot service = services.get(item.getServiceItemId());
            BeauticianSnapshot beautician = beauticians.get(item.getBeauticianId());
            view.setServiceName(service == null ? "服务项目" : service.getName());
            view.setBeauticianName(beautician == null ? "美容师" : beautician.getName());
            view.setBeauticianAvatar(beautician == null ? null : beautician.getAvatar());
            view.setReviewed(reviewed.contains(item.getId()));
            return view;
        }).toList();
    }

    private ProfileView toProfile(BookingUser user) {
        ProfileView view = new ProfileView();
        view.setId(user.getId());
        view.setNickname(user.getNickname());
        view.setMobile(user.getMobile());
        view.setAccountNo(user.getAccountNo() == null || user.getAccountNo().isBlank() ? AccountNo.of(user.getMobile()) : user.getAccountNo());
        view.setGender(user.getGender());
        view.setBirthday(user.getBirthday());
        view.setAddress(user.getAddress());
        view.setMemberLevel(user.getMemberLevel() == null || user.getMemberLevel().isBlank() ? "普通会员" : user.getMemberLevel());
        view.setTags(userTagMapper.selectList(new LambdaQueryWrapper<UserTag>()
                        .eq(UserTag::getUserId, user.getId())
                        .orderByAsc(UserTag::getId))
                .stream()
                .map(UserTag::getTagName)
                .toList());
        return view;
    }

    private List<String> normalizeTags(List<String> raw) {
        List<String> source = raw == null ? List.of() : raw;
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String item : source) {
            if (item == null) {
                continue;
            }
            String tag = item.trim();
            if (tag.isEmpty()) {
                continue;
            }
            if (tag.length() > 16) {
                throw new BizException("单个标签不超过 16 字");
            }
            unique.add(tag);
        }
        if (unique.size() > 8) {
            throw new BizException("标签最多 8 个");
        }
        return new ArrayList<>(unique);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BookingUser requireUser() {
        BookingUser user = userMapper.selectById(UserContext.require());
        if (user == null) {
            throw new BizException(401, "请先登录");
        }
        return user;
    }

    private long current(long page) {
        return page < 1 ? 1 : page;
    }

    private long pageSize(long size) {
        return size < 1 ? 20 : Math.min(size, 100);
    }
}
