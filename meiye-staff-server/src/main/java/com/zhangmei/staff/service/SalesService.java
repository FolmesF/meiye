package com.zhangmei.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.common.PageResult;
import com.zhangmei.staff.dto.ProspectCard;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.Followup;
import com.zhangmei.staff.entity.Prospect;
import com.zhangmei.staff.entity.VisitTemplate;
import com.zhangmei.staff.mapper.FollowupMapper;
import com.zhangmei.staff.mapper.ProspectMapper;
import com.zhangmei.staff.mapper.VisitTemplateMapper;
import com.zhangmei.staff.security.StaffContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesService {

    private static final Set<String> STATUSES = Set.of("NEW", "FOLLOWING", "DONE");
    private static final Set<String> REQUIRED_KEYS = Set.of("mobile", "company", "address", "content", "photo", "location");

    private final VisitTemplateMapper templateMapper;
    private final ProspectMapper prospectMapper;
    private final FollowupMapper followupMapper;

    public Long saveTemplate(Long id, StaffForms.TemplateSave request) {
        VisitTemplate template = id == null ? new VisitTemplate() : requireTemplate(id);
        template.setEmployeeId(StaffContext.requireEmployeeId());
        template.setTitle(request.getTitle().trim());
        template.setContent(request.getContent().trim());
        template.setRequiredFields(normalizeRequired(request.getRequiredFields()));
        if (id == null) {
            templateMapper.insert(template);
        } else {
            templateMapper.updateById(template);
        }
        return template.getId();
    }

    public java.util.List<VisitTemplate> templates() {
        return templateMapper.selectList(new LambdaQueryWrapper<VisitTemplate>()
                .eq(VisitTemplate::getEmployeeId, StaffContext.requireEmployeeId())
                .orderByDesc(VisitTemplate::getId));
    }

    public void deleteTemplate(Long id) {
        requireTemplate(id);
        templateMapper.deleteById(id);
    }

    public Long saveProspect(Long id, StaffForms.ProspectSave request) {
        VisitTemplate template = request.getTemplateId() == null ? null : requireTemplate(request.getTemplateId());
        assertRequired(template, request);
        Prospect prospect = id == null ? new Prospect() : requireProspect(id);
        prospect.setEmployeeId(StaffContext.requireEmployeeId());
        prospect.setTemplateId(request.getTemplateId());
        prospect.setName(request.getName().trim());
        prospect.setMobile(request.getMobile());
        prospect.setCompany(request.getCompany());
        prospect.setAddress(blankToNull(request.getAddress()));
        prospect.setContent(blankToNull(request.getContent()));
        prospect.setPhotos(blankToNull(request.getPhotos()));
        prospect.setLongitude(request.getLongitude());
        prospect.setLatitude(request.getLatitude());
        if (id == null) {
            prospect.setStatus("NEW");
            prospectMapper.insert(prospect);
        } else {
            prospectMapper.updateById(prospect);
        }
        return prospect.getId();
    }

    public PageResult<ProspectCard> prospects(String status, long page, long size) {
        Page<Prospect> result = prospectMapper.selectPage(new Page<>(page < 1 ? 1 : page, size < 1 ? 20 : Math.min(size, 100)),
                new LambdaQueryWrapper<Prospect>()
                        .eq(Prospect::getEmployeeId, StaffContext.requireEmployeeId())
                        .eq(status != null && !status.isBlank(), Prospect::getStatus, status)
                        .orderByDesc(Prospect::getCreatedAt)
                        .orderByDesc(Prospect::getId));
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), toCards(result.getRecords()));
    }

    @Transactional
    public Long follow(StaffForms.FollowupCreate request) {
        Prospect prospect = requireProspect(request.getProspectId());
        Followup followup = new Followup();
        followup.setProspectId(prospect.getId());
        followup.setEmployeeId(StaffContext.requireEmployeeId());
        followup.setContent(request.getContent().trim());
        followup.setNextFollowAt(request.getNextFollowAt());
        followupMapper.insert(followup);
        if ("NEW".equals(prospect.getStatus())) {
            prospect.setStatus("FOLLOWING");
            prospectMapper.updateById(prospect);
        }
        return followup.getId();
    }

    public java.util.List<Followup> followups(Long prospectId) {
        requireProspect(prospectId);
        return followupMapper.selectList(new LambdaQueryWrapper<Followup>()
                .eq(Followup::getProspectId, prospectId)
                .orderByDesc(Followup::getId));
    }

    public void changeProspectStatus(Long id, String status) {
        if (!STATUSES.contains(status)) {
            throw new BizException("客户状态只允许 NEW、FOLLOWING、DONE");
        }
        Prospect prospect = requireProspect(id);
        prospect.setStatus(status);
        prospectMapper.updateById(prospect);
    }

    private List<ProspectCard> toCards(List<Prospect> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        List<Long> ids = records.stream().map(Prospect::getId).toList();
        Map<Long, Followup> latest = followupMapper.selectList(new LambdaQueryWrapper<Followup>()
                        .in(Followup::getProspectId, ids)
                        .orderByDesc(Followup::getCreatedAt)
                        .orderByDesc(Followup::getId))
                .stream()
                .collect(Collectors.toMap(Followup::getProspectId, item -> item, (first, ignored) -> first));
        return records.stream().map(item -> {
            ProspectCard card = new ProspectCard();
            card.setId(item.getId());
            card.setName(item.getName());
            card.setStatus(item.getStatus());
            card.setVisitedAt(item.getCreatedAt());
            Followup followup = latest.get(item.getId());
            card.setRemark(followup == null ? item.getContent() : followup.getContent());
            return card;
        }).toList();
    }

    private void assertRequired(VisitTemplate template, StaffForms.ProspectSave request) {
        if (template == null || template.getRequiredFields() == null || template.getRequiredFields().isBlank()) {
            return;
        }
        for (String key : template.getRequiredFields().split(",")) {
            boolean missing = switch (key) {
                case "mobile" -> !hasText(request.getMobile());
                case "company" -> !hasText(request.getCompany());
                case "address" -> !hasText(request.getAddress());
                case "content" -> !hasText(request.getContent());
                case "photo" -> !hasText(request.getPhotos()) || "[]".equals(request.getPhotos().trim());
                case "location" -> request.getLongitude() == null || request.getLatitude() == null;
                default -> false;
            };
            if (missing) {
                throw new BizException("请填写模板要求的" + requiredLabel(key));
            }
        }
    }

    private String normalizeRequired(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(REQUIRED_KEYS::contains)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private String requiredLabel(String key) {
        return switch (key) {
            case "mobile" -> "手机号";
            case "company" -> "单位";
            case "address" -> "地址";
            case "content" -> "拜访内容";
            case "photo" -> "现场图片";
            case "location" -> "定位";
            default -> "必填项";
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private VisitTemplate requireTemplate(Long id) {
        VisitTemplate template = templateMapper.selectById(id);
        if (template == null || !template.getEmployeeId().equals(StaffContext.requireEmployeeId())) {
            throw new BizException(404, "陌拜模板不存在");
        }
        return template;
    }

    private Prospect requireProspect(Long id) {
        Prospect prospect = prospectMapper.selectById(id);
        if (prospect == null || !prospect.getEmployeeId().equals(StaffContext.requireEmployeeId())) {
            throw new BizException(404, "陌拜客户不存在");
        }
        return prospect;
    }
}
