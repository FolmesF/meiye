package com.zhangmei.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.booking.BookingClient;
import com.zhangmei.store.dto.ServiceCard;
import com.zhangmei.store.dto.ServiceItemForms;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.BeauticianServiceBind;
import com.zhangmei.store.entity.Product;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.entity.ServiceMaterial;
import com.zhangmei.store.mapper.BeauticianMapper;
import com.zhangmei.store.mapper.BeauticianServiceBindMapper;
import com.zhangmei.store.mapper.ProductMapper;
import com.zhangmei.store.mapper.ServiceItemMapper;
import com.zhangmei.store.mapper.ServiceMaterialMapper;
import com.zhangmei.store.service.Checks;
import com.zhangmei.store.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");

    private final ServiceItemMapper serviceItemMapper;
    private final ServiceMaterialMapper serviceMaterialMapper;
    private final ProductMapper productMapper;
    private final BeauticianServiceBindMapper bindMapper;
    private final BeauticianMapper beauticianMapper;
    private final BookingClient bookingClient;

    @Override
    public Long create(ServiceItemForms.Save request) {
        ServiceItem item = new ServiceItem();
        item.setStoreId(request.getStoreId());
        item.setName(request.getName().trim());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        item.setDurationMin(request.getDurationMin());
        item.setStatus("ENABLED");
        item.setRemark(request.getRemark());
        serviceItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void update(Long id, ServiceItemForms.Update request) {
        ServiceItem item = mustGet(id);
        if (!STATUSES.contains(request.getStatus())) {
            throw new BizException("项目状态只允许 ENABLED 或 DISABLED");
        }
        item.setName(request.getName().trim());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        item.setDurationMin(request.getDurationMin());
        item.setStatus(request.getStatus());
        item.setRemark(request.getRemark());
        serviceItemMapper.updateById(item);
    }

    @Override
    public void delete(Long id) {
        mustGet(id);
        serviceItemMapper.deleteById(id);
    }

    @Override
    public ServiceItem detail(Long id) {
        return mustGet(id);
    }

    @Override
    public PageResult<ServiceItem> page(Long storeId, String status, String keyword, long page, long size) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStoreId, storeId)
                .eq(StringUtils.hasText(status), ServiceItem::getStatus, status)
                .like(StringUtils.hasText(keyword), ServiceItem::getName, keyword)
                .orderByDesc(ServiceItem::getId);
        Page<ServiceItem> result = serviceItemMapper.selectPage(new Page<>(Checks.page(page), Checks.size(size)), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public PageResult<ServiceCard> cards(Long storeId, long page, long size) {
        PageResult<ServiceItem> result = page(storeId, null, null, page, size);
        List<ServiceCard> cards = result.getRecords().stream().map(item -> {
            ServiceCard card = new ServiceCard();
            card.setId(item.getId());
            card.setName(item.getName());
            return card;
        }).toList();
        return PageResult.of(result.getTotal(), result.getPage(), result.getSize(), cards);
    }

    @Override
    public ServiceCard card(Long id) {
        ServiceItem item = mustGet(id);
        ServiceCard card = new ServiceCard();
        card.setId(item.getId());
        card.setName(item.getName());
        card.setDurationMin(item.getDurationMin());
        card.setPrice(item.getPrice());
        card.setIntroduction(item.getRemark() == null || item.getRemark().isBlank() ? "暂无介绍" : item.getRemark());
        List<Long> beauticianIds = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceBind>()
                        .eq(BeauticianServiceBind::getServiceItemId, id))
                .stream()
                .map(BeauticianServiceBind::getBeauticianId)
                .toList();
        String names = beauticianIds.isEmpty() ? "—" : beauticianMapper.selectBatchIds(beauticianIds).stream()
                .map(Beautician::getName)
                .collect(Collectors.joining("、"));
        card.setBeauticianIds(beauticianIds);
        card.setBeauticians(names.isBlank() ? "—" : names);
        List<ServiceMaterial> materials = serviceMaterialMapper.selectList(new LambdaQueryWrapper<ServiceMaterial>()
                .eq(ServiceMaterial::getServiceItemId, id));
        for (ServiceMaterial material : materials) {
            Product product = productMapper.selectById(material.getProductId());
            ServiceCard.MaterialLine line = new ServiceCard.MaterialLine();
            line.setProductName(product == null ? "耗材" : product.getName());
            line.setQuantity(material.getQuantity());
            card.getMaterials().add(line);
        }
        return card;
    }

    @Override
    @Transactional
    public void replaceBeauticians(Long serviceItemId, List<Long> beauticianIds) {
        ServiceItem item = mustGet(serviceItemId);
        List<Long> ids = beauticianIds == null ? List.of() : beauticianIds.stream().distinct().toList();
        List<Beautician> beauticians = ids.isEmpty() ? List.of() : beauticianMapper.selectBatchIds(ids);
        if (beauticians.size() != ids.size()) {
            throw new BizException("存在无效的美容师");
        }
        for (Beautician beautician : beauticians) {
            if (!item.getStoreId().equals(beautician.getStoreId())) {
                throw new BizException("美容师与项目不属于同一门店");
            }
        }
        bindMapper.delete(new LambdaQueryWrapper<BeauticianServiceBind>()
                .eq(BeauticianServiceBind::getServiceItemId, serviceItemId));
        for (Long beauticianId : ids) {
            BeauticianServiceBind bind = new BeauticianServiceBind();
            bind.setStoreId(item.getStoreId());
            bind.setBeauticianId(beauticianId);
            bind.setServiceItemId(serviceItemId);
            bindMapper.insert(bind);
        }
        bookingClient.syncProject(item, beauticians);
    }

    @Override
    public void publishAll() {
        List<ServiceItem> items = serviceItemMapper.selectList(new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, "ENABLED"));
        for (ServiceItem item : items) {
            List<Long> ids = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceBind>()
                            .eq(BeauticianServiceBind::getServiceItemId, item.getId()))
                    .stream()
                    .map(BeauticianServiceBind::getBeauticianId)
                    .toList();
            List<Beautician> beauticians = ids.isEmpty() ? List.of() : beauticianMapper.selectBatchIds(ids);
            bookingClient.syncProject(item, beauticians);
        }
    }

    @Override
    @Transactional
    public void replaceMaterials(Long serviceItemId, List<ServiceItemForms.Line> materials) {
        ServiceItem item = mustGet(serviceItemId);
        List<ServiceItemForms.Line> lines = materials == null ? List.of() : materials;
        Set<Long> productIds = new HashSet<>();
        for (ServiceItemForms.Line line : lines) {
            if (!productIds.add(line.getProductId())) {
                throw new BizException("同一耗材不能重复配置");
            }
            Product product = productMapper.selectById(line.getProductId());
            if (product == null || !product.getStoreId().equals(item.getStoreId())) {
                throw new BizException("耗材不存在或不属于该门店");
            }
        }
        serviceMaterialMapper.delete(new LambdaQueryWrapper<ServiceMaterial>()
                .eq(ServiceMaterial::getServiceItemId, serviceItemId));
        for (ServiceItemForms.Line line : lines) {
            ServiceMaterial material = new ServiceMaterial();
            material.setStoreId(item.getStoreId());
            material.setServiceItemId(serviceItemId);
            material.setProductId(line.getProductId());
            material.setQuantity(line.getQuantity());
            serviceMaterialMapper.insert(material);
        }
    }

    @Override
    public List<ServiceMaterial> listMaterials(Long serviceItemId) {
        mustGet(serviceItemId);
        return serviceMaterialMapper.selectList(new LambdaQueryWrapper<ServiceMaterial>()
                .eq(ServiceMaterial::getServiceItemId, serviceItemId)
                .orderByAsc(ServiceMaterial::getId));
    }

    private ServiceItem mustGet(Long id) {
        ServiceItem item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BizException(404, "服务项目不存在");
        }
        return item;
    }
}
