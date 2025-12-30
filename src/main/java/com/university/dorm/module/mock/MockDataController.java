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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private JdbcTemplate jdbcTemplate;

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
        List<Dormitory> dorms = dormitoryService.list();
        List<InspectionItem> items = inspectionItemService.list();
        
        if (dorms.isEmpty() || items.isEmpty()) {
            return Result.error("No basic data found");
        }

        Random random = new Random();
        List<InspectionRecord> records = new ArrayList<>();
        List<List<InspectionDetail>> allDetailsPending = new ArrayList<>();

        // Generate at least one record for EACH dormitory within the past 14 days
        int days = 14;
        
        for (Dormitory dorm : dorms) {
            // Random date within the last 14 days
            LocalDate date = LocalDate.now().minusDays(random.nextInt(days + 1));
            
            InspectionRecord record = new InspectionRecord();
            record.setDormId(dorm.getId());
            record.setInspectorName("系统模拟");
            record.setCheckDate(date);
            record.setRemark("模拟数据");
            
            BigDecimal currentScore = BigDecimal.valueOf(100);
            List<InspectionDetail> currentDetails = new ArrayList<>();
            
            // 决定扣分项数量
            int issueCount = 0;
            double r = random.nextDouble();
            if (r < 0.4) {
                issueCount = 0; // 40% 满分/优秀
            } else if (r < 0.7) {
                issueCount = 1 + random.nextInt(2); // 30% 1-2项扣分
            } else if (r < 0.9) {
                issueCount = 3 + random.nextInt(2); // 20% 3-4项扣分
            } else {
                issueCount = 5 + random.nextInt(3); // 10% 严重扣分
            }
            
            // 生成明细
            for(int k=0; k<issueCount; k++) {
                InspectionItem item = items.get(random.nextInt(items.size()));
                BigDecimal deduct = BigDecimal.valueOf(2 + random.nextInt(4)); // 扣 2-5 分
                currentScore = currentScore.subtract(deduct);
                
                InspectionDetail detail = new InspectionDetail();
                detail.setItemId(item.getId());
                detail.setScore(BigDecimal.valueOf(item.getMaxScore()).subtract(deduct));
                detail.setDeductionReason(item.getItemName() + "不合格");
                currentDetails.add(detail);
            }
            
            if(currentScore.compareTo(BigDecimal.ZERO) < 0) currentScore = BigDecimal.ZERO;
            record.setTotalScore(currentScore);

            // Set isNotice based on score
            if (currentScore.compareTo(BigDecimal.valueOf(70)) < 0) {
                record.setIsNotice(1); // Score < 70: Always notify
            } else if (currentScore.compareTo(BigDecimal.valueOf(85)) < 0) {
                record.setIsNotice(random.nextBoolean() ? 1 : 0); // 70-85: 50% chance
            } else {
                record.setIsNotice(0); // Score >= 85: No notice
            }
            
            records.add(record);
            allDetailsPending.add(currentDetails);
        }
        
        // Batch save records
        // Process in chunks to avoid memory issues or packet size limits
        int batchSize = 1000;
        for (int i = 0; i < records.size(); i += batchSize) {
            int end = Math.min(i + batchSize, records.size());
            List<InspectionRecord> batch = records.subList(i, end);
            inspectionService.saveBatch(batch);
        }

        // Assign IDs and batch save details
        List<InspectionDetail> finalDetails = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            Long recordId = records.get(i).getId();
            List<InspectionDetail> pending = allDetailsPending.get(i);
            for (InspectionDetail detail : pending) {
                detail.setRecordId(recordId);
                finalDetails.add(detail);
            }
        }
        
        for (int i = 0; i < finalDetails.size(); i += batchSize) {
            int end = Math.min(i + batchSize, finalDetails.size());
            List<InspectionDetail> batch = finalDetails.subList(i, end);
            inspectionDetailService.saveBatch(batch);
        }
        
        return Result.success("成功生成 " + records.size() + " 条模拟数据，已覆盖所有宿舍。");
    }
}
