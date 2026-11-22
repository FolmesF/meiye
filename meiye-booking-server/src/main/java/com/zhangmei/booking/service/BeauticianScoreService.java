package com.zhangmei.booking.service;

import com.zhangmei.booking.dto.BeauticianScore;
import com.zhangmei.booking.entity.Review;
import com.zhangmei.booking.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BeauticianScoreService {

    private final ReviewMapper reviewMapper;

    public List<BeauticianScore> all() {
        Map<Long, int[]> bucket = new LinkedHashMap<>();
        for (Review review : reviewMapper.selectList(null)) {
            if (review.getBeauticianId() == null || review.getBeauticianScore() == null) {
                continue;
            }
            int[] item = bucket.computeIfAbsent(review.getBeauticianId(), key -> new int[2]);
            item[0] += review.getBeauticianScore();
            item[1] += 1;
        }
        List<BeauticianScore> scores = new ArrayList<>();
        bucket.forEach((id, item) -> {
            BeauticianScore score = new BeauticianScore();
            score.setBeauticianId(id);
            score.setScore(BigDecimal.valueOf(item[0]).divide(BigDecimal.valueOf(item[1]), 1, RoundingMode.HALF_UP));
            scores.add(score);
        });
        return scores;
    }
}
