package com.university.dorm.module.bigscreen.controller;

import com.university.dorm.module.bigscreen.dto.*;
import com.university.dorm.module.bigscreen.service.BigScreenService;
import com.university.dorm.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/big-screen")
public class BigScreenController {

    @Autowired
    private BigScreenService bigScreenService;

    @GetMapping("/summary")
    public BigScreenSummaryDTO getSummary() {
        return bigScreenService.getSummary();
    }

    @GetMapping("/area-compare")
    public AreaCompareDTO getAreaCompare() {
        return bigScreenService.getAreaCompare();
    }

    @GetMapping("/latest-records")
    public List<LatestRecordDTO> getLatestRecords() {
        return bigScreenService.getLatestRecords();
    }

    @GetMapping("/building-rank")
    public List<BuildingRankDTO> getBuildingRank() {
        return bigScreenService.getBuildingRank();
    }

    @GetMapping("/issue-top10")
    public List<IssueTop10DTO> getIssueTop10() {
        return bigScreenService.getIssueTop10();
    }

    @GetMapping("/trend-30d")
    public List<DailyTrendDTO> getTrend30d() {
        return bigScreenService.getTrend30d();
    }

    @GetMapping("/score-distribution")
    public ScoreDistributionDTO getScoreDistribution() {
        return bigScreenService.getScoreDistribution();
    }

    @GetMapping("/excellent-dorms")
    public List<ExcellentDormDTO> getExcellentDorms() {
        return bigScreenService.getExcellentDorms();
    }

    @GetMapping("/rectification-dorms")
    public List<LatestRecordDTO> getRectificationDorms() {
        return bigScreenService.getRectificationDorms();
    }

    @GetMapping("/structure")
    public List<String> getStructure() {
        return bigScreenService.getStructure();
    }

    @GetMapping("/query-score")
    public Object getQueryScore(String building, Integer floor, String room) {
        // If room is specified, return score + issues
        if (room != null && !room.isEmpty()) {
            return bigScreenService.getQueryScoreWithIssues(building, floor, room);
        }
        // Otherwise return average score
        return bigScreenService.getQueryScore(building, floor, room);
    }

    @GetMapping("/rank-data")
    public Result<List<RankChartDTO>> getRankData(
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String room) {
        return Result.success(bigScreenService.getRankData(campus, building, floor, room));
    }
}
