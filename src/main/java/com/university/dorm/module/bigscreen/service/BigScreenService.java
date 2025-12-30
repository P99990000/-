package com.university.dorm.module.bigscreen.service;

import com.university.dorm.module.bigscreen.dto.*;
import java.util.List;
import java.util.Map;

public interface BigScreenService {
    BigScreenSummaryDTO getSummary();
    AreaCompareDTO getAreaCompare();
    List<LatestRecordDTO> getLatestRecords();
    List<BuildingRankDTO> getBuildingRank();
    List<IssueTop10DTO> getIssueTop10();
    List<DailyTrendDTO> getTrend30d();
    ScoreDistributionDTO getScoreDistribution();
    List<ExcellentDormDTO> getExcellentDorms();
    List<LatestRecordDTO> getRectificationDorms();
    List<String> getStructure();
    Double getQueryScore(String building, Integer floor, String room);
    Map<String, Object> getQueryScoreWithIssues(String building, Integer floor, String room);
    List<RankChartDTO> getRankData(String campus, String building, Integer floor, String room);
}
