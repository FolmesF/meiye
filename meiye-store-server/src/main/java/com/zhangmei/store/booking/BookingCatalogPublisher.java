package com.zhangmei.store.booking;

import com.zhangmei.store.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCatalogPublisher implements ApplicationRunner {

    private final ServiceItemService serviceItemService;

    @Override
    public void run(ApplicationArguments args) {
        for (int attempt = 1; attempt <= 6; attempt++) {
            try {
                serviceItemService.publishAll();
                log.info("预约端项目已按项目管理同步");
                return;
            } catch (Exception ex) {
                log.warn("同步预约项目失败，第 {} 次：{}", attempt, ex.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
