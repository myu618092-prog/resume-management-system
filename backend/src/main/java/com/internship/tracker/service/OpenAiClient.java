package com.internship.tracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tracker.entity.AiAnalysisType;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class OpenAiClient implements AiProviderClient {
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public OpenAiClient(
            ObjectMapper objectMapper,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.base-url}") String baseUrl,
            @Value("${app.openai.model}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public boolean enabled() {
        return StringUtils.hasText(apiKey);
    }

    @Override
    public String analyze(AiAnalysisType type, String prompt) {
        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody(type, prompt))
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            validateJson(content);
            return content;
        } catch (RestClientException exception) {
            throw new IllegalArgumentException("OpenAI 调用失败：请检查 API Key、网络、baseUrl 和模型配置。");
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("OpenAI 返回内容不是合法 JSON，请稍后重试。");
        }
    }

    Map<String, Object> requestBody(AiAnalysisType type, String prompt) {
        return Map.of(
                "model", model,
                "response_format", jsonSchema(type),
                "messages", List.of(
                        Map.of("role", "system", "content", "你是实习求职教练。只输出符合 schema 的中文 JSON，不输出 Markdown。"),
                        Map.of("role", "user", "content", prompt)
                )
        );
    }

    private Map<String, Object> jsonSchema(AiAnalysisType type) {
        if (type == AiAnalysisType.INTERVIEW_QUESTIONS) {
            return Map.of(
                    "type", "json_schema",
                    "json_schema", Map.of(
                            "name", "interview_questions",
                            "strict", true,
                            "schema", Map.of(
                                    "type", "object",
                                    "additionalProperties", false,
                                    "required", List.of("technicalQuestions", "behavioralQuestions", "projectDeepDive", "preparationChecklist"),
                                    "properties", Map.of(
                                            "technicalQuestions", stringArray(),
                                            "behavioralQuestions", stringArray(),
                                            "projectDeepDive", stringArray(),
                                            "preparationChecklist", stringArray()
                                    )
                            )
                    )
            );
        }
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "job_match_analysis",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "required", List.of("matchScore", "strengths", "gaps", "resumeAdvice", "actionPlan"),
                                "properties", Map.of(
                                        "matchScore", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                        "strengths", stringArray(),
                                        "gaps", stringArray(),
                                        "resumeAdvice", stringArray(),
                                        "actionPlan", stringArray()
                                )
                        )
                )
        );
    }

    private Map<String, Object> stringArray() {
        return Map.of("type", "array", "items", Map.of("type", "string"));
    }

    private void validateJson(String content) throws JsonProcessingException {
        objectMapper.readTree(content);
    }
}
