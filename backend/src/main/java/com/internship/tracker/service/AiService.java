package com.internship.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tracker.dto.AiDtos.AiResponse;
import com.internship.tracker.dto.AiDtos.InterviewQuestionsRequest;
import com.internship.tracker.dto.AiDtos.JobMatchRequest;
import com.internship.tracker.entity.AiAnalysisRecord;
import com.internship.tracker.entity.AiAnalysisType;
import com.internship.tracker.entity.Job;
import com.internship.tracker.entity.Resume;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.AiAnalysisRecordRepository;
import com.internship.tracker.repository.JobRepository;
import com.internship.tracker.repository.ResumeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class AiService {
    private final AiAnalysisRecordRepository recordRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public AiService(
            AiAnalysisRecordRepository recordRepository,
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            ObjectMapper objectMapper,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.base-url}") String baseUrl,
            @Value("${app.openai.model}") String model
    ) {
        this.recordRepository = recordRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Transactional
    public AiResponse analyzeJobMatch(User user, JobMatchRequest request) throws Exception {
        String prompt = """
                请作为求职教练，基于岗位 JD 和候选人背景输出 JSON。
                字段：matchScore(0-100), strengths(array), gaps(array), resumeAdvice(array), actionPlan(array)。
                岗位 JD：
                %s
                候选人背景：
                %s
                """.formatted(request.jobDescription(), request.candidateProfile());
        return analyze(user, request.jobId(), request.resumeId(), AiAnalysisType.JOB_MATCH, prompt, Map.of(
                "jobDescription", request.jobDescription(),
                "candidateProfile", request.candidateProfile()
        ));
    }

    @Transactional
    public AiResponse interviewQuestions(User user, InterviewQuestionsRequest request) throws Exception {
        String prompt = """
                请基于岗位 JD 和候选人背景输出 JSON。
                字段：technicalQuestions(array), behavioralQuestions(array), projectDeepDive(array), preparationChecklist(array)。
                岗位 JD：
                %s
                候选人背景：
                %s
                """.formatted(request.jobDescription(), request.candidateProfile());
        return analyze(user, request.jobId(), null, AiAnalysisType.INTERVIEW_QUESTIONS, prompt, Map.of(
                "jobDescription", request.jobDescription(),
                "candidateProfile", request.candidateProfile()
        ));
    }

    private AiResponse analyze(
            User user,
            Long jobId,
            Long resumeId,
            AiAnalysisType type,
            String prompt,
            Map<String, String> snapshot
    ) throws Exception {
        Job job = jobId == null ? null : jobRepository.findByIdAndUserId(jobId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        Resume resume = resumeId == null ? null : resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Resume not found"));

        String provider = StringUtils.hasText(apiKey) ? "openai-compatible" : "mock";
        String result = StringUtils.hasText(apiKey) ? callOpenAi(prompt) : mock(type);

        AiAnalysisRecord record = new AiAnalysisRecord();
        record.setUser(user);
        record.setJob(job);
        record.setResume(resume);
        record.setType(type);
        record.setProvider(provider);
        record.setInputSnapshot(objectMapper.writeValueAsString(snapshot));
        record.setResult(result);
        record = recordRepository.save(record);

        return new AiResponse(record.getId(), provider, result);
    }

    private String callOpenAi(String prompt) throws Exception {
        Map<String, Object> body = Map.of(
                "model", model,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", "你只输出合法 JSON，不输出 Markdown。"),
                        Map.of("role", "user", "content", prompt)
                )
        );
        String response = restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        JsonNode root = objectMapper.readTree(response);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private String mock(AiAnalysisType type) throws Exception {
        if (type == AiAnalysisType.INTERVIEW_QUESTIONS) {
            return objectMapper.writeValueAsString(Map.of(
                    "technicalQuestions", List.of("请解释你项目中的认证流程。", "PostgreSQL 索引如何优化查询？"),
                    "behavioralQuestions", List.of("讲一次你推进困难任务的经历。"),
                    "projectDeepDive", List.of("为什么选择 Spring Security + JWT？", "如何保证用户数据隔离？"),
                    "preparationChecklist", List.of("复盘项目架构图", "准备数据库表设计说明", "练习接口演示")
            ));
        }
        return objectMapper.writeValueAsString(Map.of(
                "matchScore", 82,
                "strengths", List.of("后端项目结构完整", "包含 PostgreSQL、JWT、Docker 等实习常见技术"),
                "gaps", List.of("建议补充线上部署经验", "建议准备性能优化案例"),
                "resumeAdvice", List.of("突出用户鉴权、数据隔离和 Flyway 迁移", "写清 AI 接口支持 mock 与真实 provider"),
                "actionPlan", List.of("补充 README 截图", "准备 2 分钟项目演示稿")
        ));
    }
}
