package com.zhangmei.booking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class SlotPlanner {

    private SlotPlanner() {
    }

    public record TimeRange(LocalTime start, LocalTime end) {
    }

    public static List<LocalTime> available(LocalDate day, LocalTime shiftStart, LocalTime shiftEnd,
                                            int durationMin, int stepMin, List<TimeRange> occupied) {
        List<LocalTime> result = new ArrayList<>();
        if (shiftStart == null || shiftEnd == null || !shiftEnd.isAfter(shiftStart) || durationMin <= 0 || stepMin <= 0) {
            return result;
        }
        LocalDateTime cursor = LocalDateTime.of(day, shiftStart);
        LocalDateTime limit = LocalDateTime.of(day, shiftEnd);
        List<TimeRange> busy = occupied == null ? List.of() : occupied;
        while (true) {
            LocalDateTime end = cursor.plusMinutes(durationMin);
            if (end.isAfter(limit)) {
                break;
            }
            LocalTime startTime = cursor.toLocalTime();
            LocalTime endTime = end.toLocalTime();
            if (busy.stream().noneMatch(range -> overlaps(startTime, endTime, range.start(), range.end()))) {
                result.add(startTime);
            }
            cursor = cursor.plusMinutes(stepMin);
        }
        return result;
    }

    public static boolean overlaps(LocalTime start, LocalTime end, LocalTime otherStart, LocalTime otherEnd) {
        return start.isBefore(otherEnd) && end.isAfter(otherStart);
    }
}
