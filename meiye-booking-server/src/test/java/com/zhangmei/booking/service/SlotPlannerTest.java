package com.zhangmei.booking.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotPlannerTest {

    private final LocalDate day = LocalDate.of(2026, 11, 6);

    @Test
    void cutsSlotsByDurationAndStep() {
        List<LocalTime> slots = SlotPlanner.available(day, LocalTime.of(10, 0), LocalTime.of(12, 0), 60, 30, List.of());
        assertEquals(List.of(LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0)), slots);
    }

    @Test
    void removesOverlappingSlots() {
        List<LocalTime> slots = SlotPlanner.available(day, LocalTime.of(10, 0), LocalTime.of(12, 0), 60, 30,
                List.of(new SlotPlanner.TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0))));
        assertEquals(List.of(LocalTime.of(11, 0)), slots);
    }

    @Test
    void overlapTouchesAtBoundaryIsFree() {
        assertFalse(SlotPlanner.overlaps(LocalTime.of(11, 0), LocalTime.of(12, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertTrue(SlotPlanner.overlaps(LocalTime.of(10, 30), LocalTime.of(11, 30), LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }
}
