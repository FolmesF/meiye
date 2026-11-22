package com.zhangmei.booking.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.common.AccountNo;
import com.zhangmei.booking.entity.BeauticianSnapshot;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.entity.BookingUser;
import com.zhangmei.booking.entity.ServiceSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StoreClient {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestClient http;

    public StoreClient(@Value("${meiye.booking.store-base-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void syncAppointment(BookingAppointment appointment, BookingUser user,
                                BeauticianSnapshot beautician, ServiceSnapshot service,
                                String status, String cancelReason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bookingId", appointment.getId());
        payload.put("storeId", appointment.getStoreId());
        payload.put("customerName", customerName(user));
        payload.put("mobile", user.getMobile());
        payload.put("accountNo", accountNo(user));
        payload.put("gender", user.getGender());
        payload.put("memberLevel", user.getMemberLevel());
        payload.put("beauticianId", beautician.getId());
        payload.put("beauticianName", beautician.getName());
        payload.put("serviceItemId", service.getId());
        payload.put("serviceName", service.getName());
        payload.put("appointTime", ISO.format(appointment.getAppointTime()));
        payload.put("appointEnd", ISO.format(appointment.getAppointEnd()));
        payload.put("amount", appointment.getAmount());
        payload.put("status", status);
        payload.put("cancelReason", cancelReason);
        post("/api/appointments/booking-sync", payload, "预约已提交，但同步到门店失败，请稍后重试");
    }

    public void ensureCustomer(BookingUser user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accountNo", accountNo(user));
        payload.put("name", customerName(user));
        payload.put("mobile", user.getMobile());
        payload.put("memberLevel", user.getMemberLevel());
        post("/api/customers/register-sync", payload, "同步新顾客失败");
    }

    public void syncProfile(BookingUser user, List<String> tags) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accountNo", accountNo(user));
        payload.put("name", customerName(user));
        payload.put("mobile", user.getMobile());
        payload.put("gender", user.getGender());
        payload.put("birthday", user.getBirthday() == null ? null : user.getBirthday().toString());
        payload.put("address", user.getAddress());
        payload.put("memberLevel", user.getMemberLevel());
        payload.put("tags", tags == null ? List.of() : tags);
        post("/api/customers/profile-sync", payload, "同步顾客资料失败");
    }

    private String accountNo(BookingUser user) {
        if (user.getAccountNo() != null && !user.getAccountNo().isBlank()) {
            return user.getAccountNo();
        }
        return AccountNo.of(user.getMobile());
    }

    private void post(String path, Object payload, String fallback) {
        try {
            JsonNode body = http.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || body.path("code").asInt(-1) != 0) {
                String message = body == null ? "店管端无响应" : body.path("message").asText(fallback);
                throw new BizException(message);
            }
        } catch (BizException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("{}: {}", fallback, exception.getMessage());
            throw new BizException(fallback);
        }
    }

    private String customerName(BookingUser user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname().trim();
        }
        return user.getMobile();
    }
}
