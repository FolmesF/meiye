package com.zhangmei.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.EmployeeStore;
import com.zhangmei.staff.entity.Inspection;
import com.zhangmei.staff.entity.Store;
import com.zhangmei.staff.mapper.EmployeeStoreMapper;
import com.zhangmei.staff.mapper.InspectionMapper;
import com.zhangmei.staff.mapper.StoreMapper;
import com.zhangmei.staff.security.StaffContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreWorkService {

    private final StoreMapper storeMapper;
    private final EmployeeStoreMapper employeeStoreMapper;
    private final InspectionMapper inspectionMapper;
    private final LocationGuard locationGuard;

    @Transactional
    public Long createStore(StaffForms.StoreSave request) {
        Store store = fill(new Store(), request);
        store.setStatus("ENABLED");
        storeMapper.insert(store);
        EmployeeStore link = new EmployeeStore();
        link.setEmployeeId(StaffContext.requireEmployeeId());
        link.setStoreId(store.getId());
        link.setCreatedAt(LocalDateTime.now());
        employeeStoreMapper.insert(link);
        return store.getId();
    }

    public void updateStore(Long id, StaffForms.StoreSave request) {
        requireOwned(id);
        Store store = mustStore(id);
        fill(store, request);
        storeMapper.updateById(store);
    }

    public List<Store> myStores() {
        List<Long> storeIds = employeeStoreMapper.selectList(new LambdaQueryWrapper<EmployeeStore>()
                        .eq(EmployeeStore::getEmployeeId, StaffContext.requireEmployeeId()))
                .stream()
                .map(EmployeeStore::getStoreId)
                .toList();
        if (storeIds.isEmpty()) {
            return List.of();
        }
        return storeMapper.selectBatchIds(storeIds);
    }

    public Long inspect(StaffForms.InspectionCreate request) {
        Store store = requireOwned(request.getStoreId());
        locationGuard.assertNear(store.getLongitude(), store.getLatitude(), request.getLongitude(), request.getLatitude());
        Inspection inspection = new Inspection();
        inspection.setStoreId(store.getId());
        inspection.setEmployeeId(StaffContext.requireEmployeeId());
        inspection.setContent(request.getContent().trim());
        inspection.setLongitude(request.getLongitude());
        inspection.setLatitude(request.getLatitude());
        inspection.setVisitedAt(LocalDateTime.now());
        inspectionMapper.insert(inspection);
        return inspection.getId();
    }

    public List<Inspection> inspections(Long storeId) {
        requireOwned(storeId);
        return inspectionMapper.selectList(new LambdaQueryWrapper<Inspection>()
                .eq(Inspection::getStoreId, storeId)
                .orderByDesc(Inspection::getVisitedAt));
    }

    public Store requireOwned(Long storeId) {
        Store store = mustStore(storeId);
        Long count = employeeStoreMapper.selectCount(new LambdaQueryWrapper<EmployeeStore>()
                .eq(EmployeeStore::getEmployeeId, StaffContext.requireEmployeeId())
                .eq(EmployeeStore::getStoreId, storeId));
        if (count == 0) {
            throw new BizException(403, "无权操作该门店");
        }
        return store;
    }

    private Store mustStore(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BizException(404, "门店不存在");
        }
        return store;
    }

    private Store fill(Store store, StaffForms.StoreSave request) {
        if (request.getLongitude() != null || request.getLatitude() != null) {
            if (request.getLongitude() == null || request.getLatitude() == null) {
                throw new BizException("门店经纬度需要同时填写");
            }
            locationGuard.assertNear(null, null, request.getLongitude(), request.getLatitude());
        }
        store.setName(request.getName().trim());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setLongitude(request.getLongitude());
        store.setLatitude(request.getLatitude());
        return store;
    }
}
