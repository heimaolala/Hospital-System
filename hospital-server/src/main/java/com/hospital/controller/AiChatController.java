package com.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.common.Result;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private String apiUrl = "https://api-inference.modelscope.cn/v1/chat/completions";
    private String apiKey = "";
    private String model = "deepseek-ai/DeepSeek-R1-0528";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadConfig() {
        // 尝试从项目根目录 AI_api.txt 读取配置
        Path configPath = Path.of("AI_api.txt");
        if (!Files.exists(configPath)) {
            configPath = Path.of("../AI_api.txt");
        }
        if (Files.exists(configPath)) {
            try {
                List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("URL：") || line.startsWith("URL:")) {
                        apiUrl = line.replaceFirst("URL[：:]\\s*", "").trim();
                    } else if (line.startsWith("API Key：") || line.startsWith("API Key:")) {
                        apiKey = line.replaceFirst("API Key[：:]\\s*", "").trim();
                    } else if (line.startsWith("model：") || line.startsWith("model:")) {
                        model = line.replaceFirst("model[：:]\\s*", "").trim();
                    }
                }
                if (!apiUrl.endsWith("/chat/completions")) {
                    apiUrl = apiUrl.replaceAll("/+$", "") + "/chat/completions";
                }
                log.info("AI配置已从 AI_api.txt 加载: url={}, model={}", apiUrl, model);
            } catch (Exception e) {
                log.error("读取AI_api.txt失败", e);
            }
        } else {
            log.warn("AI_api.txt 未找到，使用默认配置");
        }
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一名由中国认证医疗机构支持的AI健康咨询助手，旨在为用户提供初步的健康信息参考和就医引导。你的职责包括：\n" +
            "1. 严格遵守中国法律法规：不提供任何未经国家药品监督管理局或国家卫生健康委员会批准的诊疗建议、药物推荐或疾病诊断。\n" +
            "2. 明确角色边界：你不是医生，不能替代面对面的医疗诊断。所有回答必须强调\"仅供参考，不能代替专业医疗意见\"。\n" +
            "3. 科学严谨，避免误导：基于权威医学指南（如中华医学会、国家卫健委发布的内容）提供信息，不传播未经证实的偏方、谣言或夸大疗效的说法。\n" +
            "4. 引导及时就医：当用户描述的症状可能涉及急症、重症（如胸痛、意识障碍、大出血等）时，必须立即建议其拨打120或前往最近医院急诊科就诊。\n" +
            "5. 保护隐私与伦理：不主动询问敏感个人信息（如身份证号、具体住址），对用户提供的健康信息严格保密，不用于任何非医疗目的。\n" +
            "6. 语言亲切、通俗易懂：使用普通话，避免过度专业术语；若需使用，请附带简明解释。\n" +
            "7. 不涉及处方与药品销售：不得推荐具体品牌药品，不引导用户在线购药，仅可说明\"常见治疗方式需由医生开具处方\"。\n" +
            "请始终以\"您的健康安全第一\"为原则，在提供帮助的同时，反复提醒用户：如有不适，请及时前往正规医疗机构就诊。");
        messages.add(systemMsg);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userMessages = (List<Map<String, Object>>) body.get("messages");
        if (userMessages != null) {
            for (Map<String, Object> m : userMessages) {
                Map<String, Object> clean = new LinkedHashMap<>();
                clean.put("role", m.get("role"));
                clean.put("content", m.get("content"));
                messages.add(clean);
            }
        }

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);

            String json = objectMapper.writeValueAsString(requestBody);
            log.info("AI request: {}", json.substring(0, Math.min(200, json.length())));

            HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int code = conn.getResponseCode();
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseStr = new String(is.readAllBytes());
            log.info("AI response code={}, body={}", code, responseStr);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(responseStr, Map.class);

            if (code >= 400) {
                Map<String, Object> err = (Map<String, Object>) responseBody.get("error");
                String errMsg = err != null ? (String) err.get("message") : "API调用失败(" + code + ")";
                throw new RuntimeException(errMsg);
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("role", message.get("role"));
            result.put("content", message.get("content"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("AI API调用失败", e);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("role", "assistant");
            fallback.put("content", "抱歉，AI服务暂时不可用（" + e.getMessage() + "）。如果是紧急医疗问题，请立即就医。");
            return Result.success(fallback);
        }
    }
}
