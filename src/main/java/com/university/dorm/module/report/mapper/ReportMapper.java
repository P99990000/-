package com.university.dorm.module.report.mapper;

import com.university.dorm.module.report.dto.ReportDetailDTO;
import com.university.dorm.module.report.dto.ReportRankDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("<script>" +
            "SELECT " +
            "COUNT(DISTINCT dorm_id) as checkedDorms, " +
            "AVG(total_score) as avgScore, " +
            "SUM(CASE WHEN total_score &gt;= 60 THEN 1 ELSE 0 END) as passCount, " +
            "SUM(CASE WHEN total_score &lt; 60 THEN 1 ELSE 0 END) as failCount, " +
            "COUNT(*) as totalChecks " +
            "FROM inspection_record " +
            "WHERE check_date &gt;= #{startDate} " +
            "</script>")
    Map<String, Object> getStats(@Param("startDate") LocalDate startDate);

    @Select("SELECT COUNT(*) FROM dormitory")
    Integer getTotalDormCount();

    @Select("<script>" +
            "SELECT " +
            "CONCAT(d.building_name, '-', d.room_number) as dormName, " +
            "r.total_score as score, " +
            "r.inspector_name as inspector " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE r.check_date &gt;= #{startDate} " +
            "ORDER BY r.total_score ${sortOrder} " +
            "LIMIT 10" +
            "</script>")
    List<ReportRankDTO> getRank(@Param("startDate") LocalDate startDate, @Param("sortOrder") String sortOrder);

    @Select("<script>" +
            "SELECT " +
            "r.id, " +
            "CONCAT(d.building_name, '-', d.room_number) as dormName, " +
            "r.total_score as totalScore, " +
            "r.inspector_name as inspectorName, " +
            "r.check_date as checkDate, " +
            "COALESCE((SELECT GROUP_CONCAT(deduction_reason SEPARATOR ', ') FROM inspection_detail id WHERE id.record_id = r.id AND id.deduction_reason IS NOT NULL), '') as issues " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "WHERE r.check_date &gt;= #{startDate} " +
            "<if test='search != null and search != \"\"'>" +
            "AND (d.building_name LIKE CONCAT('%', #{search}, '%') OR d.room_number LIKE CONCAT('%', #{search}, '%') OR r.inspector_name LIKE CONCAT('%', #{search}, '%')) " +
            "</if> " +
            "ORDER BY r.check_date DESC" +
            "</script>")
    List<ReportDetailDTO> getDetails(@Param("startDate") LocalDate startDate, @Param("search") String search);
    
    @Select("<script>" +
            "SELECT AVG(total_score) " +
            "FROM inspection_record " +
            "WHERE check_date &gt;= #{startDate} AND check_date &lt; #{endDate} " +
            "</script>")
    Double getAvgScoreInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
