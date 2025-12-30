package com.university.dorm.module.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.university.dorm.module.dormitory.entity.Dormitory;

import java.util.List;
import java.util.Map;

public interface DormitoryService extends IService<Dormitory> {

    /**
     * 获取按院区分组的楼栋列表
     * @return {"北院": ["北5栋"...], "南院": ["南23栋"...]}
     */
    Map<String, List<String>> getBuildingsGrouped();

    /**
     * 根据楼栋名称获取所有宿舍
     * @param buildingName 楼栋名称
     * @return 宿舍列表
     */
    List<Dormitory> getDormsByBuilding(String buildingName);
}
