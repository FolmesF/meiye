package com.zhangmei.store.service;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.BeauticianBoardRow;
import com.zhangmei.store.dto.BeauticianForms;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.BeauticianSchedule;
import com.zhangmei.store.entity.ServiceItem;

import java.time.LocalDate;
import java.util.List;

public interface BeauticianService {

    Long create(BeauticianForms.Save request);

    void update(Long id, BeauticianForms.Update request);

    void delete(Long id);

    Beautician detail(Long id);

    PageResult<Beautician> page(Long storeId, String status, String keyword, long page, long size);

    List<BeauticianBoardRow> board(Long storeId);

    void bindServices(Long beauticianId, List<Long> serviceItemIds);

    List<ServiceItem> listServices(Long beauticianId);

    BeauticianSchedule saveSchedule(Long beauticianId, BeauticianForms.Schedule request);

    List<BeauticianSchedule> listSchedules(Long beauticianId, LocalDate from, LocalDate to);
}
