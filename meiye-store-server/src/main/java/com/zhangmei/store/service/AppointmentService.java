package com.zhangmei.store.service;

import com.zhangmei.store.dto.AppointmentForms;
import com.zhangmei.store.dto.AppointmentRecord;
import com.zhangmei.store.dto.AppointmentStatQuery;
import com.zhangmei.store.dto.AppointmentStatResponse;
import com.zhangmei.store.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    Long create(AppointmentForms.Save request);

    Appointment detail(Long id);

    void changeStatus(Long id, AppointmentForms.StatusChange request);

    List<AppointmentStatResponse> stats(AppointmentStatQuery query);

    List<AppointmentRecord> records(Long storeId, LocalDate startDate, LocalDate endDate,
                                    String serviceName, String beauticianName, String mobile, String phase);

    List<AppointmentRecord> workable(Long storeId);

    void start(Long id);

    void finish(Long id);

    void syncFromBooking(AppointmentForms.BookingSync request);
}
