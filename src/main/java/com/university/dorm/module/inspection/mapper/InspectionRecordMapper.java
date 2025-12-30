package com.university.dorm.module.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.dorm.module.admin.dto.BuildingStatsDTO;
import com.university.dorm.module.admin.dto.DormScoreDTO;
import com.university.dorm.module.inspection.entity.InspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InspectionRecordMapper extends BaseMapper<InspectionRecord> {

    @Select("SELECT " +
            "d.building_name as building, " +
            "AVG(r.total_score) as avgScore, " +
            "COUNT(r.id) as totalChecks, " +
            "SUM(CASE WHEN r.total_score >= 90 THEN 1 ELSE 0 END) as excellentCount, " +
            "SUM(CASE WHEN r.total_score < 60 THEN 1 ELSE 0 END) as failCount " +
            "FROM inspection_record r " +
            "LEFT JOIN dormitory d ON r.dorm_id = d.id " +
            "GROUP BY d.building_name")
    List<BuildingStatsDTO> selectBuildingStats();

    @Select("SELECT d.building_name as building, d.room_number as room, " +
            "(SELECT total_score FROM inspection_record r WHERE r.dorm_id = d.id ORDER BY r.created_at DESC LIMIT 1) as score " +
            "FROM dormitory d")
    List<DormScoreDTO> selectDormScores();
}
