package com.university.dorm.module.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.dorm.module.inspection.entity.InspectionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface InspectionDetailMapper extends BaseMapper<InspectionDetail> {

    @Select("SELECT i.item_name as issue, COUNT(d.id) as count " +
            "FROM inspection_detail d " +
            "LEFT JOIN inspection_item i ON d.item_id = i.id " +
            "GROUP BY i.item_name " +
            "ORDER BY count DESC " +
            "LIMIT 5")
    List<Map<String, Object>> selectTopIssues();
}
