package com.university.dorm.module.bigscreen.service.impl;

import com.university.dorm.module.bigscreen.dto.*;
import com.university.dorm.module.bigscreen.mapper.BigScreenMapper;
import com.university.dorm.module.bigscreen.service.BigScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class BigScreenServiceImpl implements BigScreenService {

    @Autowired
    private BigScreenMapper bigScreenMapper;

    @Override
    public BigScreenSummaryDTO getSummary() {
        return bigScreenMapper.getSummary();
    }

    @Override
    public AreaCompareDTO getAreaCompare() {
        return bigScreenMapper.getAreaCompare();
    }

    @Override
    public List<LatestRecordDTO> getLatestRecords() {
        return bigScreenMapper.getLatestRecords();
    }

    @Override
    public List<BuildingRankDTO> getBuildingRank() {
        return bigScreenMapper.getBuildingRank();
    }

    @Override
    public List<IssueTop10DTO> getIssueTop10() {
        return bigScreenMapper.getIssueTop10();
    }

    @Override
    public List<DailyTrendDTO> getTrend30d() {
        return bigScreenMapper.getTrend30d();
    }

    @Override
    public ScoreDistributionDTO getScoreDistribution() {
        Map<String, Object> raw = bigScreenMapper.getScoreDistributionRaw();
        ScoreDistributionDTO dto = new ScoreDistributionDTO();
        
        if (raw == null) {
             dto.setExcellent("0%");
             dto.setGood("0%");
             dto.setPass("0%");
             dto.setFail("0%");
             return dto;
        }

        // Handle potential nulls in map values
        long total = raw.get("total") != null ? ((Number) raw.get("total")).longValue() : 0;
        
        if (total == 0) {
             dto.setExcellent("0%");
             dto.setGood("0%");
             dto.setPass("0%");
             dto.setFail("0%");
             return dto;
        }

        long excellent = raw.get("excellentCount") != null ? ((Number) raw.get("excellentCount")).longValue() : 0;
        long good = raw.get("goodCount") != null ? ((Number) raw.get("goodCount")).longValue() : 0;
        long pass = raw.get("passCount") != null ? ((Number) raw.get("passCount")).longValue() : 0;
        long fail = raw.get("failCount") != null ? ((Number) raw.get("failCount")).longValue() : 0;

        dto.setExcellent(calculatePercentage(excellent, total));
        dto.setGood(calculatePercentage(good, total));
        dto.setPass(calculatePercentage(pass, total));
        dto.setFail(calculatePercentage(fail, total));

        return dto;
    }

    @Override
    public List<ExcellentDormDTO> getExcellentDorms() {
        return bigScreenMapper.getExcellentDorms();
    }

    @Override
    public List<LatestRecordDTO> getRectificationDorms() {
        return bigScreenMapper.getRectificationDorms();
    }

    @Override
    public List<String> getStructure() {
        return bigScreenMapper.getAllDorms();
    }

    @Override
    public Double getQueryScore(String building, Integer floor, String room) {
        return bigScreenMapper.getQueryScore(building, floor, room);
    }

    @Override
    public Map<String, Object> getQueryScoreWithIssues(String building, Integer floor, String room) {
        return bigScreenMapper.getQueryScoreWithIssues(building, floor, room);
    }

    @Override
    public List<RankChartDTO> getRankData(String campus, String building, Integer floor, String room) {
        return bigScreenMapper.getRankData(campus, building, floor, room);
    }

    private String calculatePercentage(long count, long total) {
        if (total == 0) return "0%";
        BigDecimal pct = BigDecimal.valueOf(count)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
        
        return pct.toString() + "%";
    }
}
