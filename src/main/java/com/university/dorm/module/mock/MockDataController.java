package com.university.dorm.module.mock;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.dormitory.entity.Dormitory;
import com.university.dorm.module.dormitory.service.DormitoryService;
import com.university.dorm.module.inspection.entity.InspectionDetail;
import com.university.dorm.module.inspection.entity.InspectionItem;
import com.university.dorm.module.inspection.entity.InspectionRecord;
import com.university.dorm.module.inspection.service.InspectionDetailService;
import com.university.dorm.module.inspection.service.InspectionItemService;
import com.university.dorm.module.inspection.service.InspectionService;
import com.university.dorm.module.student.entity.Student;
import com.university.dorm.module.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mock")
public class MockDataController {

    @Autowired
    private DormitoryService dormitoryService;
    @Autowired
    private InspectionService inspectionService;
    @Autowired
    private InspectionItemService inspectionItemService;
    @Autowired
    private InspectionDetailService inspectionDetailService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/init_student")
    public Result<String> initStudent() {
        if (studentService.count() > 0) {
            return Result.success("学生数据已存在");
        }

        // Find a dorm to assign
        List<Dormitory> dorms = dormitoryService.list();
        Long dormId = dorms.isEmpty() ? null : dorms.get(0).getId();

        Student s1 = new Student();
        s1.setStudentSn("2024001");
        s1.setName("测试学生");
        s1.setClassName("计算机2401");
        s1.setGender(1);
        s1.setDormId(dormId);
        s1.setCreatedAt(LocalDateTime.now());
        s1.setUpdatedAt(LocalDateTime.now());

        studentService.save(s1);
        return Result.success("创建测试学生成功：账号 2024001，密码 024001（学号后6位）");
    }

    @PostMapping("/clear")
    public Result<String> clear() {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            jdbcTemplate.execute("TRUNCATE TABLE inspection_detail");
            jdbcTemplate.execute("TRUNCATE TABLE inspection_record");
            // notification table might not exist, skipping
            // Keep basic data: sys_user, dormitory, inspection_item
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            return Result.success("业务数据已清空（保留基础数据）");
        } catch (Exception e) {
            return Result.error("清空失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        // 1. Fetch relevant dorms (North/South)
        List<Dormitory> allDorms = dormitoryService.list();
        if (allDorms.isEmpty()) {
            return Result.error("No basic data found");
        }

        List<Dormitory> targetDorms = allDorms.stream()
            .filter(d -> d.getBuildingName().contains("南") || d.getBuildingName().contains("北"))
            .collect(Collectors.toList());
            
        if (targetDorms.size() < 20) {
            // Fallback if not enough specific dorms
            targetDorms = allDorms;
        }
        
        // Shuffle to get random mix
        Collections.shuffle(targetDorms);
        
        // We need exactly 20 records
        int count = Math.min(20, targetDorms.size());
        List<InspectionRecord> records = new ArrayList<>();
        List<List<InspectionDetail>> allDetailsPending = new ArrayList<>();
        
        Random random = new Random();
        String[] inspectors = {"张老师", "李宿管", "王阿姨", "赵检查员", "陈主任"};
        String[] commonIssues = {"地面不洁", "垃圾未倒", "物品乱放", "违规电器", "空气异味"};
        List<InspectionItem> items = inspectionItemService.list();
        
        for (int i = 0; i < count; i++) {
            Dormitory dorm = targetDorms.get(i);
            
            InspectionRecord record = new InspectionRecord();
            record.setDormId(dorm.getId());
            record.setCheckDate(LocalDateTime.now());
            record.setInspectorName(inspectors[random.nextInt(inspectors.length)]);
            // Placeholder image
            record.setImageUrl("https://via.placeholder.com/300?text=Dorm+" + dorm.getBuildingName() + "-" + dorm.getRoomNumber());
            
            BigDecimal score;
            List<InspectionDetail> currentDetails = new ArrayList<>();
            
            if (i < 5) {
                // 5 records: Notice (通报批评)
                record.setIsNotice(1);
                record.setRectificationStatus(4); // Also needs rectification usually
                score = BigDecimal.valueOf(40 + random.nextInt(19)); // 40-59
                record.setRemark("通报批评：卫生状况极差");
            } else if (i < 15) {
                // 10 records: Need Rectification
                record.setIsNotice(0);
                record.setRectificationStatus(4); // 4: Need Rectification
                score = BigDecimal.valueOf(60 + random.nextInt(15)); // 60-75
                record.setRemark("需整改：" + commonIssues[random.nextInt(commonIssues.length)]);
            } else {
                // 5 records: Normal/Good
                record.setIsNotice(0);
                record.setRectificationStatus(0);
                score = BigDecimal.valueOf(90 + random.nextInt(11)); // 90-100
                if (score.compareTo(BigDecimal.valueOf(100)) > 0) score = BigDecimal.valueOf(100);
                record.setRemark("卫生状况良好");
            }
            
            // Generate details for non-perfect scores
            if (score.compareTo(BigDecimal.valueOf(90)) < 0 && !items.isEmpty()) {
                int pointsToDeduct = 100 - score.intValue();
                // Distribute points across 1-3 items
                int numItems = 1 + random.nextInt(3);
                for (int j = 0; j < numItems; j++) {
                    if (pointsToDeduct <= 0) break;
                    InspectionItem item = items.get(random.nextInt(items.size()));
                    int deduct = (j == numItems - 1) ? pointsToDeduct : (1 + random.nextInt(pointsToDeduct / 2 + 1));
                    pointsToDeduct -= deduct;
                    
                    InspectionDetail detail = new InspectionDetail();
                    detail.setItemId(item.getId());
                    detail.setScore(BigDecimal.valueOf(item.getMaxScore() - deduct));
                    detail.setDeductionReason(item.getItemName() + "扣分");
                    currentDetails.add(detail);
                }
            }
            
            record.setTotalScore(score);
            records.add(record);
            allDetailsPending.add(currentDetails);
        }
        
        // Save Records
        inspectionService.saveBatch(records);
        
        // Save Details
        List<InspectionDetail> finalDetails = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            Long recordId = records.get(i).getId();
            List<InspectionDetail> pending = allDetailsPending.get(i);
            for (InspectionDetail detail : pending) {
                detail.setRecordId(recordId);
                finalDetails.add(detail);
            }
        }
        inspectionDetailService.saveBatch(finalDetails);
        
        return Result.success("成功生成 " + records.size() + " 条模拟数据（5条通报，10条整改，5条正常），覆盖南北院。");
    }
}
