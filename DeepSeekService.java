package cn.edu.sdua.qingshaonian.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DeepSeek 大模型接入服务（OpenAI 兼容 Chat Completions 接口）
 * 同时统计模型调用次数、失败次数与平均耗时，供服务状态监控使用
 */
@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    private final AtomicLong callCount = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final AtomicLong totalLatencyMs = new AtomicLong();

    public DeepSeekService(@Value("${deepseek.api-key:}") String apiKey,
                           @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                           @Value("${deepseek.model:deepseek-chat}") String model,
                           @Value("${deepseek.timeout-seconds:60}") int timeoutSeconds) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(timeoutSeconds * 1000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public boolean available() {
        return !apiKey.isEmpty();
    }

    /**
     * 调用大模型对话，失败或未配置 Key 时返回 null（由上层降级为知识库回答）
     */
    public ChatResult chat(String systemPrompt, List<Map<String, String>> history) {
        if (!available()) {
            return null;
        }
        long start = System.currentTimeMillis();
        callCount.incrementAndGet();
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", merge(systemPrompt, history),
                    "stream", false,
                    "temperature", 1.0,
                    "max_tokens", 1024);
            String json = restClient.post().uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = mapper.readTree(json);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            int tokens = root.path("usage").path("total_tokens").asInt(0);
            long cost = System.currentTimeMillis() - start;
            totalLatencyMs.addAndGet(cost);
            if (content.isBlank()) {
                errorCount.incrementAndGet();
                return null;
            }
            return new ChatResult(content.trim(), tokens, (int) cost);
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.warn("DeepSeek 调用失败: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, String>> merge(String systemPrompt, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(history);
        return messages;
    }

    public Map<String, Object> metrics() {
        long calls = callCount.get();
        return Map.of(
                "configured", available(),
                "model", model,
                "callCount", calls,
                "errorCount", errorCount.get(),
                "avgLatencyMs", calls == 0 ? 0 : totalLatencyMs.get() / calls);
    }

    public record ChatResult(String content, int tokens, int latencyMs) {
    }
}
