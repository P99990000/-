package com.university.dorm.module.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.university.dorm.module.dormitory.entity.Dormitory;
import com.university.dorm.module.dormitory.mapper.DormitoryMapper;
import com.university.dorm.module.dormitory.service.DormitoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DormitoryServiceImpl extends ServiceImpl<DormitoryMapper, Dormitory> implements DormitoryService {

    @Override
    public Map<String, List<String>> getBuildingsGrouped() {
        // 获取所有楼栋名称（去重）
        List<Dormitory> allDorms = this.list(new LambdaQueryWrapper<Dormitory>()
                .select(Dormitory::getBuildingName)
                .groupBy(Dormitory::getBuildingName));
        
        List<String> buildings = allDorms.stream()
                .map(Dormitory::getBuildingName)
                .sorted()
                .collect(Collectors.toList());
        
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("北院", new ArrayList<>());
        result.put("南院", new ArrayList<>());
        
        for (String b : buildings) {
            if (b.startsWith("北")) {
                result.get("北院").add(b);
            } else if (b.startsWith("南")) {
                result.get("南院").add(b);
            } else {
                // 处理其他情况，或者归类到其他
                // 暂时忽略或放入一个"其他"分类
                result.computeIfAbsent("其他", k -> new ArrayList<>()).add(b);
            }
        }
        
        return result;
    }

    @Override
    public List<Dormitory> getDormsByBuilding(String buildingName) {
        return this.list(new LambdaQueryWrapper<Dormitory>()
                .eq(Dormitory::getBuildingName, buildingName)
                .orderByAsc(Dormitory::getFloor)
                .orderByAsc(Dormitory::getRoomNumber));
    }
}
