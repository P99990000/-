package com.university.dorm.module.inspection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.university.dorm.module.inspection.entity.InspectionDetail;
import com.university.dorm.module.inspection.mapper.InspectionDetailMapper;
import com.university.dorm.module.inspection.service.InspectionDetailService;
import org.springframework.stereotype.Service;

@Service
public class InspectionDetailServiceImpl extends ServiceImpl<InspectionDetailMapper, InspectionDetail> implements InspectionDetailService {
}
