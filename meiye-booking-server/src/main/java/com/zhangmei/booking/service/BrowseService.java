package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.dto.BeauticianCard;
import com.zhangmei.booking.dto.BeauticianDetail;
import com.zhangmei.booking.dto.ReviewItem;
import com.zhangmei.booking.dto.ServiceBrief;
import com.zhangmei.booking.entity.BeauticianServiceSnapshot;
import com.zhangmei.booking.entity.BeauticianSnapshot;
import com.zhangmei.booking.entity.BookingUser;
import com.zhangmei.booking.entity.Review;
import com.zhangmei.booking.entity.ServiceSnapshot;
import com.zhangmei.booking.entity.StoreSnapshot;
import com.zhangmei.booking.mapper.BeauticianServiceSnapshotMapper;
import com.zhangmei.booking.mapper.BeauticianSnapshotMapper;
import com.zhangmei.booking.mapper.BookingUserMapper;
import com.zhangmei.booking.mapper.ReviewMapper;
import com.zhangmei.booking.mapper.ServiceSnapshotMapper;
import com.zhangmei.booking.mapper.StoreSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrowseService {

    private final StoreSnapshotMapper storeMapper;
    private final ServiceSnapshotMapper serviceMapper;
    private final BeauticianSnapshotMapper beauticianMapper;
    private final BeauticianServiceSnapshotMapper bindMapper;
    private final ReviewMapper reviewMapper;
    private final BookingUserMapper userMapper;
    private final CatalogService catalogService;
    private final AvailabilityService availabilityService;

    public List<StoreSnapshot> stores() {
        return storeMapper.selectList(new LambdaQueryWrapper<StoreSnapshot>()
                .eq(StoreSnapshot::getStatus, "ENABLED")
                .orderByAsc(StoreSnapshot::getId));
    }

    public List<ServiceSnapshot> services(Long storeId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        return serviceMapper.selectList(new LambdaQueryWrapper<ServiceSnapshot>()
                .eq(ServiceSnapshot::getStoreId, storeId)
                .eq(ServiceSnapshot::getStatus, "ENABLED")
                .orderByAsc(ServiceSnapshot::getId));
    }

    public List<BeauticianCard> beauticians(Long storeId, Long serviceItemId, LocalDate date) {
        if (storeId == null || serviceItemId == null) {
            throw new BizException("请先选择门店和服务项目");
        }
        ServiceSnapshot service = catalogService.requireEnabledService(storeId, serviceItemId);
        LocalDate day = date == null ? LocalDate.now() : date;
        return catalogService.listBoundBeauticians(storeId, serviceItemId).stream()
                .map(beautician -> toCard(beautician, service, day))
                .toList();
    }

    public BeauticianDetail detail(Long beauticianId) {
        BeauticianSnapshot beautician = catalogService.requireActiveBeautician(beauticianId);
        BeauticianDetail detail = new BeauticianDetail();
        detail.setId(beautician.getId());
        detail.setStoreId(beautician.getStoreId());
        detail.setName(beautician.getName());
        detail.setAvatar(beautician.getAvatar());
        detail.setLevel(beautician.getLevel());
        detail.setYears(years(beautician.getEntryDate()));
        fillScore(detail, beautician.getId());
        detail.setServices(servicesOf(beautician));
        return detail;
    }

    public List<ReviewItem> reviews(Long beauticianId) {
        catalogService.requireActiveBeautician(beauticianId);
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                        .eq(Review::getBeauticianId, beauticianId)
                        .orderByDesc(Review::getCreatedAt)
                        .last("LIMIT 20"))
                .stream()
                .map(this::toReview)
                .toList();
    }

    private BeauticianCard toCard(BeauticianSnapshot beautician, ServiceSnapshot service, LocalDate date) {
        BeauticianCard card = new BeauticianCard();
        card.setId(beautician.getId());
        card.setStoreId(beautician.getStoreId());
        card.setName(beautician.getName());
        card.setAvatar(beautician.getAvatar());
        card.setLevel(beautician.getLevel());
        card.setYears(years(beautician.getEntryDate()));
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getBeauticianId, beautician.getId()));
        card.setReviewCount(reviews.size());
        card.setScore(average(reviews));
        card.setSkills(servicesOf(beautician).stream().map(ServiceBrief::getName).toList());
        var schedule = catalogService.findSchedule(beautician.getId(), date);
        boolean onDuty = schedule != null && "ON_DUTY".equals(schedule.getShiftStatus());
        if (!onDuty) {
            card.setBookable("OFF");
        } else {
            card.setBookable(availabilityService.openSlots(beautician.getId(), service, date).isEmpty() ? "FULL" : "OPEN");
        }
        return card;
    }

    private void fillScore(BeauticianDetail detail, Long beauticianId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getBeauticianId, beauticianId));
        detail.setReviewCount(reviews.size());
        detail.setScore(average(reviews));
    }

    private List<ServiceBrief> servicesOf(BeauticianSnapshot beautician) {
        List<Long> ids = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceSnapshot>()
                        .eq(BeauticianServiceSnapshot::getBeauticianId, beautician.getId()))
                .stream()
                .map(BeauticianServiceSnapshot::getServiceItemId)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return serviceMapper.selectList(new LambdaQueryWrapper<ServiceSnapshot>()
                        .in(ServiceSnapshot::getId, ids)
                        .eq(ServiceSnapshot::getStoreId, beautician.getStoreId())
                        .eq(ServiceSnapshot::getStatus, "ENABLED")
                        .orderByAsc(ServiceSnapshot::getId))
                .stream()
                .map(item -> {
                    ServiceBrief brief = new ServiceBrief();
                    brief.setId(item.getId());
                    brief.setName(item.getName());
                    brief.setPrice(item.getPrice());
                    brief.setDurationMin(item.getDurationMin());
                    return brief;
                })
                .toList();
    }

    private ReviewItem toReview(Review review) {
        ReviewItem item = new ReviewItem();
        item.setId(review.getId());
        item.setBeauticianScore(review.getBeauticianScore());
        item.setStoreScore(review.getStoreScore());
        item.setServiceScore(review.getServiceScore());
        item.setContent(review.getContent());
        item.setCreatedAt(review.getCreatedAt());
        BookingUser user = userMapper.selectById(review.getUserId());
        item.setNickname(user == null || user.getNickname() == null ? "用户" : user.getNickname());
        return item;
    }

    private BigDecimal average(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }
        int sum = reviews.stream().mapToInt(Review::getBeauticianScore).sum();
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
    }

    private Integer years(LocalDate entryDate) {
        if (entryDate == null || entryDate.isAfter(LocalDate.now())) {
            return null;
        }
        return Period.between(entryDate, LocalDate.now()).getYears();
    }
}
