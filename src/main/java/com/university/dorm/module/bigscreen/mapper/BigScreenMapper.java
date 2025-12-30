package com.university.dorm.module.bigscreen.mapper;

import com.university.dorm.module.bigscreen.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BigScreenMapper {

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM inspection_record) as totalChecks, " +
            "(SELECT AVG(total_score) FROM inspection_record) as avgScore, " +
            "(SELECT COUNT(*) FROM inspection_record WHERE check_date = CURDATE()) as todayChecks, " +
            "(SELECT deduction_reason FROM inspection_detail WHERE deduction_reason IS NOT NULL GROUP BY deduction_reason ORDER BY COUNT(*) DESC LIMIT 1) as topIssue")
    BigScreenSummaryDTO getSummary();

    @Select("SELECT " +
            "AVG(CASE WHEN d.building_name LIKE '北%' OR d.building_name LIKE 'A%' OR d.building_name LIKE 'C%' THEN r.total_score ELSE NULL END) as northAvg, " +
            "AVG(CASE WHEN d.building_name LIKE '南%' OR d.building_name LIKE 'B%' OR d.building_name LIKE 'D%' THEN r.total_score ELSE NULL END) as southAvg " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id")
    AreaCompareDTO getAreaCompare();

    @Select("SELECT " +
            "CONCAT(d.building_name, '-', d.room_number) as dorm, " +
            "r.total_score as score, " +
            "r.inspector_name as inspectorName, " +
            "DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i:%s') as time, " +
            "COALESCE((SELECT GROUP_CONCAT(deduction_reason SEPARATOR ', ') FROM inspection_detail id WHERE id.record_id = r.id AND id.deduction_reason IS NOT NULL), '无') as issues " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "ORDER BY r.created_at DESC " +
            "LIMIT 8")
    List<LatestRecordDTO> getLatestRecords();

    @Select("SELECT " +
            "d.building_name as building, " +
            "AVG(r.total_score) as avgScore " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "GROUP BY d.building_name " +
            "ORDER BY avgScore DESC")
    List<BuildingRankDTO> getBuildingRank();

    @Select("SELECT " +
            "deduction_reason as issue, " +
            "COUNT(*) as count " +
            "FROM inspection_detail " +
            "WHERE deduction_reason IS NOT NULL " +
            "GROUP BY deduction_reason " +
            "ORDER BY count DESC " +
            "LIMIT 10")
    List<IssueTop10DTO> getIssueTop10();

    @Select("SELECT " +
            "check_date as date, " +
            "AVG(total_score) as avgScore " +
            "FROM inspection_record " +
            "WHERE check_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY check_date " +
            "ORDER BY check_date ASC")
    List<DailyTrendDTO> getTrend30d();

    @Select("SELECT " +
            "SUM(CASE WHEN total_score >= 90 THEN 1 ELSE 0 END) as excellentCount, " +
            "SUM(CASE WHEN total_score >= 80 AND total_score < 90 THEN 1 ELSE 0 END) as goodCount, " +
            "SUM(CASE WHEN total_score >= 60 AND total_score < 80 THEN 1 ELSE 0 END) as passCount, " +
            "SUM(CASE WHEN total_score < 60 THEN 1 ELSE 0 END) as failCount, " +
            "COUNT(*) as total " +
            "FROM inspection_record")
    Map<String, Object> getScoreDistributionRaw();

    @Select("SELECT " +
            "CONCAT(d.building_name, '-', d.room_number) as dorm, " +
            "r.total_score as score, " +
            "DATE_FORMAT(r.created_at, '%Y-%m-%d') as date " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE r.total_score >= 90 " +
            "ORDER BY r.created_at DESC " +
            "LIMIT 20")
    List<ExcellentDormDTO> getExcellentDorms();

    @Select("SELECT " +
            "CONCAT(d.building_name, '-', d.room_number) as dorm, " +
            "r.total_score as score, " +
            "r.inspector_name as inspectorName, " +
            "DATE_FORMAT(r.created_at, '%Y-%m-%d') as time, " +
            "COALESCE((SELECT GROUP_CONCAT(deduction_reason SEPARATOR ', ') FROM inspection_detail id WHERE id.record_id = r.id AND id.deduction_reason IS NOT NULL AND id.deduction_reason != ''), '无') as issues " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE r.is_notice = 1 " +
            "ORDER BY r.total_score ASC, r.created_at DESC " +
            "LIMIT 20")
    List<LatestRecordDTO> getRectificationDorms();

    @Select("SELECT DISTINCT CONCAT(TRIM(building_name), ',', floor, ',', TRIM(room_number)) FROM dormitory")
    List<String> getAllDorms();

    @Select("<script>" +
            "SELECT r.total_score as score, " +
            "COALESCE((SELECT GROUP_CONCAT(deduction_reason SEPARATOR ', ') FROM inspection_detail id WHERE id.record_id = r.id AND id.deduction_reason IS NOT NULL), '无') as issues " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE 1=1 " +
            "<if test='building != null'>AND TRIM(d.building_name) = #{building}</if> " +
            "<if test='floor != null'>AND d.floor = #{floor}</if> " +
            "<if test='room != null'>AND TRIM(d.room_number) = #{room}</if> " +
            "ORDER BY r.created_at DESC LIMIT 1" +
            "</script>")
    Map<String, Object> getQueryScoreWithIssues(@org.apache.ibatis.annotations.Param("building") String building, 
                                                @org.apache.ibatis.annotations.Param("floor") Integer floor, 
                                                @org.apache.ibatis.annotations.Param("room") String room);

    @Select("<script>" +
            "SELECT " +
            "<choose>" +
            "  <when test='floor != null'>CONCAT(d.room_number, '') as name, </when>" +
            "  <when test='building != null'>CONCAT(d.floor, '层') as name, </when>" +
            "  <otherwise>d.building_name as name, </otherwise>" +
            "</choose>" +
            "AVG(r.total_score) as value " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE 1=1 " +
            "<if test='campus != null'>" +
            "  AND (" +
            "    (#{campus} = 'North' AND (d.building_name LIKE '北%' OR d.building_name LIKE 'A%')) OR " +
            "    (#{campus} = 'South' AND (d.building_name LIKE '南%' OR d.building_name LIKE 'B%'))" +
            "  )" +
            "</if> " +
            "<if test='building != null'>AND TRIM(d.building_name) = #{building}</if> " +
            "<if test='floor != null'>AND d.floor = #{floor}</if> " +
            "<if test='room != null'>AND TRIM(d.room_number) = #{room}</if> " +
            "GROUP BY name " +
            "ORDER BY value DESC" +
            "</script>")
    List<RankChartDTO> getRankData(@org.apache.ibatis.annotations.Param("campus") String campus,
                                   @org.apache.ibatis.annotations.Param("building") String building,
                                   @org.apache.ibatis.annotations.Param("floor") Integer floor,
                                   @org.apache.ibatis.annotations.Param("room") String room);

    @Select("<script>" +
            "SELECT AVG(r.total_score) " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE 1=1 " +
            "<if test='building != null'>AND TRIM(d.building_name) = #{building}</if> " +
            "<if test='floor != null'>AND d.floor = #{floor}</if> " +
            "<if test='room != null'>AND TRIM(d.room_number) = #{room}</if> " +
            "</script>")
    Double getQueryScore(@org.apache.ibatis.annotations.Param("building") String building, 
                         @org.apache.ibatis.annotations.Param("floor") Integer floor, 
                         @org.apache.ibatis.annotations.Param("room") String room);
}
