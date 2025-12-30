package com.university.dorm.module.admin.controller;

import com.university.dorm.common.result.Result;
import com.university.dorm.module.admin.dto.BuildingStatsDTO;
import com.university.dorm.module.inspection.service.InspectionService;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AiAdminController {

    @Autowired
    private InspectionService inspectionService;

    @Value("${zhipu.api-key}")
    private String apiKey;

    // Simple in-memory cache
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    private static class CacheEntry {
        String content;
        long timestamp;

        CacheEntry(String content, long timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    @PostMapping("/ai-report")
    public Result<String> generateAiReport() {
        System.out.println("Starting generateAiReport...");
        // Check cache
        CacheEntry cached = cache.get("report");
        if (cached != null && (System.currentTimeMillis() - cached.timestamp < CACHE_DURATION_MS)) {
            System.out.println("Returning cached report.");
            return Result.success(cached.content);
        }

        try {
            System.out.println("Gathering stats...");
            // 1. Gather Data
            List<BuildingStatsDTO> stats = inspectionService.getBuildingStats();
            System.out.println("Stats gathered: " + (stats == null ? "null" : stats.size()));
            List<Map<String, Object>> topIssues = inspectionService.getTopIssues();
            System.out.println("Top issues gathered: " + (topIssues == null ? "null" : topIssues.size()));

            if (stats == null || stats.isEmpty()) {
                // Mock data for testing if real data is missing
                System.out.println("No real data, using mock data for testing AI...");
                stats = new ArrayList<>();
                BuildingStatsDTO s1 = new BuildingStatsDTO();
                s1.setBuilding("Test Building");
                s1.setAvgScore(new BigDecimal("85.5"));
                s1.setTotalChecks(10);
                s1.setExcellentCount(5);
                stats.add(s1);
            }

            // Calculate Global Stats
            BigDecimal totalScoreSum = BigDecimal.ZERO;
            int totalChecks = 0;
            int totalExcellent = 0;

            for (BuildingStatsDTO stat : stats) {
                if (stat.getTotalChecks() != null) {
                    totalChecks += stat.getTotalChecks();
                    if (stat.getAvgScore() != null) {
                        totalScoreSum = totalScoreSum.add(stat.getAvgScore().multiply(BigDecimal.valueOf(stat.getTotalChecks())));
                    }
                    if (stat.getExcellentCount() != null) {
                        totalExcellent += stat.getExcellentCount();
                    }
                }
            }

            BigDecimal globalAvg = BigDecimal.ZERO;
            BigDecimal globalExcellentRate = BigDecimal.ZERO;

            if (totalChecks > 0) {
                globalAvg = totalScoreSum.divide(BigDecimal.valueOf(totalChecks), 1, RoundingMode.HALF_UP);
                globalExcellentRate = BigDecimal.valueOf(totalExcellent)
                        .divide(BigDecimal.valueOf(totalChecks), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            // Worst Building
            BuildingStatsDTO worstBuilding = stats.stream()
                    .filter(s -> s.getAvgScore() != null)
                    .min(Comparator.comparing(BuildingStatsDTO::getAvgScore))
                    .orElse(null);

            // Top Issues
            String issuesStr = topIssues.stream()
                    .map(m -> m.get("issue") + "(" + m.get("count") + "次)")
                    .collect(Collectors.joining("、"));

            // 2. Construct Prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一名高校后勤管理专家。以下是本校宿舍卫生最新统计数据：\n");
            prompt.append("- 全校平均分：").append(globalAvg).append("\n");
            prompt.append("- 优秀率（≥90）：").append(globalExcellentRate).append("%\n");
            if (worstBuilding != null) {
                prompt.append("- 最差楼栋：").append(worstBuilding.getBuilding())
                        .append("（平均分 ").append(worstBuilding.getAvgScore()).append("）\n");
            }
            prompt.append("- 最高频扣分项：").append(issuesStr).append("\n\n");
            prompt.append("请用一段话（≤150字）总结问题并给出可操作建议，语气专业简洁。");

            // 3. Call Zhipu AI
            ClientV4 client = new ClientV4.Builder(apiKey).build();
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt.toString());
            messages.add(chatMessage);

            String requestId = String.valueOf(System.currentTimeMillis());
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(Constants.ModelChatGLM4)
                    .stream(Boolean.FALSE)
                    .invokeMethod(Constants.invokeMethod)
                    .messages(messages)
                    .requestId(requestId)
                    .build();

            ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
            
            if (invokeModelApiResp.getData() != null && invokeModelApiResp.getData().getChoices() != null 
                    && !invokeModelApiResp.getData().getChoices().isEmpty()) {
                String content = invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
                // Clean up quotes if present
                if (content.startsWith("\"") && content.endsWith("\"")) {
                    content = content.substring(1, content.length() - 1);
                }
                
                // Update Cache
                cache.put("report", new CacheEntry(content, System.currentTimeMillis()));
                
                return Result.success(content);
            } else {
                return Result.error("AI 服务返回为空");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Error in generateAiReport: " + e.getMessage());
            return Result.error("AI 服务暂不可用: " + e.getMessage());
        }
    }
}
