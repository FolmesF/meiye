package com.zhangmei.store.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.Campaign;
import com.zhangmei.store.entity.ServiceItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BookingClient {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestClient http;

    public BookingClient(@Value("${meiye.store.booking-base-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void completeAppointment(Long bookingId) {
        if (bookingId == null) {
            return;
        }
        try {
            JsonNode body = http.post()
                    .uri("/api/internal/appointments/{id}/complete", bookingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || body.path("code").asInt(-1) != 0) {
                String message = body == null ? "预约端无响应" : body.path("message").asText("完成预约失败");
                throw new BizException(message);
            }
        } catch (BizException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("同步预约端完成状态失败: {}", exception.getMessage());
            throw new BizException("项目已在店管完成，但同步预约端失败");
        }
    }

    public Map<Long, BigDecimal> beauticianScores() {
        Map<Long, BigDecimal> scores = new HashMap<>();
        try {
            JsonNode body = http.get().uri("/api/internal/beauticians/scores").retrieve().body(JsonNode.class);
            if (body == null || body.path("code").asInt(-1) != 0 || !body.path("data").isArray()) {
                return scores;
            }
            for (JsonNode item : body.path("data")) {
                long id = item.path("beauticianId").asLong(0);
                if (id > 0 && item.hasNonNull("score")) {
                    scores.put(id, item.path("score").decimalValue().setScale(1, RoundingMode.HALF_UP));
                }
            }
        } catch (RuntimeException exception) {
            log.warn("读取美容师评分失败: {}", exception.getMessage());
        }
        return scores;
    }

    public void syncProject(ServiceItem item, List<Beautician> beauticians) {
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("id", item.getId());
        service.put("storeId", item.getStoreId());
        service.put("name", item.getName());
        service.put("price", item.getPrice());
        service.put("durationMin", item.getDurationMin());
        service.put("status", item.getStatus());
        List<Map<String, Object>> staff = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (Beautician beautician : beauticians) {
            ids.add(beautician.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", beautician.getId());
            row.put("storeId", beautician.getStoreId());
            row.put("name", beautician.getName());
            row.put("level", beautician.getLevel());
            row.put("entryDate", beautician.getEntryDate() == null ? null : beautician.getEntryDate().toString());
            row.put("status", beautician.getStatus());
            staff.add(row);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", service);
        payload.put("beauticians", staff);
        payload.put("beauticianIds", ids);
        JsonNode body = post("/api/internal/service-staff", payload);
        if (body == null || body.path("code").asInt(-1) != 0) {
            String message = body == null ? "预约端无响应" : body.path("message").asText("同步预约项目失败");
            throw new BizException(message);
        }
    }

    public void syncMemberLevel(String mobile, String level) {
        if (mobile == null || !mobile.matches("^1\\d{10}$")) {
            return;
        }
        String value = level == null || level.isBlank() ? "普通会员" : level.trim();
        try {
            JsonNode body = post("/api/internal/members/level", Map.of("mobile", mobile, "level", value));
            if (body == null || body.path("code").asInt(-1) != 0) {
                log.warn("同步预约端会员等级未成功: {}", body);
            }
        } catch (RuntimeException ex) {
            log.warn("同步预约端会员等级失败: {}", ex.getMessage());
        }
    }

    public int pushCampaign(Campaign campaign) {
        if (campaign.getStartTime() == null || campaign.getEndTime() == null) {
            throw new BizException("活动时间不完整，无法推送");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("campaignId", campaign.getId());
        payload.put("name", campaign.getName());
        payload.put("campaignType", campaign.getCampaignType());
        payload.put("ruleText", campaign.getRuleText());
        payload.put("startTime", ISO.format(campaign.getStartTime()));
        payload.put("endTime", ISO.format(campaign.getEndTime()));
        JsonNode body = post("/api/internal/campaigns/notify", payload);
        if (body == null || body.path("code").asInt(-1) != 0) {
            String message = body == null ? "预约端无响应" : body.path("message").asText("推送失败");
            throw new BizException(message);
        }
        return body.path("data").asInt(0);
    }

    private JsonNode post(String path, Object payload) {
        return http.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
    }
}
