package com.university.dorm.module.inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.university.dorm.module.inspection.entity.InspectionItem;

import java.util.List;

public interface InspectionItemService extends IService<InspectionItem> {
    
    /**
     * 获取所有启用的检查项
     * @return 检查项列表
     */
    List<InspectionItem> getActiveItems();
}
