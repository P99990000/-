package com.university.dorm.module.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.university.dorm.module.inspection.entity.InspectionItem;
import com.university.dorm.module.inspection.mapper.InspectionItemMapper;
import com.university.dorm.module.inspection.service.InspectionItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionItemServiceImpl extends ServiceImpl<InspectionItemMapper, InspectionItem> implements InspectionItemService {

    @Override
    public List<InspectionItem> getActiveItems() {
        LambdaQueryWrapper<InspectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionItem::getIsEnabled, 1)
               .orderByAsc(InspectionItem::getSortOrder);
        return this.list(wrapper);
    }
}
