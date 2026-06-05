package com.internship.tracker.service;

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
import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final AiAnalysisRecordRepository recordRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;
    private final AiProviderClient aiProviderClient;

    public AiService(
            AiAnalysisRecordRepository recordRepository,
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            ObjectMapper objectMapper,
            AiProviderClient aiProviderClient
    ) {
        this.recordRepository = recordRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
        this.aiProviderClient = aiProviderClient;
    }

    @Transactional
    public AiResponse analyzeJobMatch(User user, JobMatchRequest request) throws Exception {
        String prompt = """
                请作为实习求职教练，基于岗位 JD 和候选人背景，输出中文 JSON。
                评分要具体，建议要能直接用于修改简历或准备面试。
                必须包含字段：
                matchScore: 0-100 的整数
                strengths: 候选人与岗位匹配的优势，3-5 条
                gaps: 候选人短板或风险，3-5 条
                resumeAdvice: 简历修改建议，3-5 条
                actionPlan: 接下来 7 天准备计划，3-5 条

                岗位 JD:
                %s

                候选人背景:
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
                请基于岗位 JD 和候选人背景，输出中文 JSON 面试准备材料。
                问题要贴合岗位技术栈和候选人项目，不要泛泛而谈。
                必须包含字段：
                technicalQuestions: 技术面试题，5 条
                behavioralQuestions: 行为面试题，3 条
                projectDeepDive: 项目深挖问题，4 条
                preparationChecklist: 面试前准备清单，5 条

                岗位 JD:
                %s

                候选人背景:
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

        boolean openAiEnabled = aiProviderClient.enabled();
        String provider = openAiEnabled ? "openai" : "mock";
        String result = openAiEnabled ? aiProviderClient.analyze(type, prompt) : mock(type);

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

    private String mock(AiAnalysisType type) throws Exception {
        if (type == AiAnalysisType.INTERVIEW_QUESTIONS) {
            return objectMapper.writeValueAsString(Map.of(
                    "technicalQuestions", List.of("请解释你项目中的认证流程。", "PostgreSQL 索引如何优化查询？", "Redis 在项目中适合缓存哪些数据？", "JWT 过期和刷新如何设计？", "如何处理接口权限校验？"),
                    "behavioralQuestions", List.of("讲一次你推进困难任务的经历。", "如果项目临近截止但功能未完成，你会如何取舍？", "你如何向面试官解释自己的技术短板？"),
                    "projectDeepDive", List.of("为什么选择 Spring Security + JWT？", "如何保证用户数据隔离？", "Flyway 迁移失败时如何处理？", "岗位导入如何避免重复数据？"),
                    "preparationChecklist", List.of("复盘项目架构图", "准备数据库表设计说明", "练习接口演示", "准备一次故障排查案例", "整理 3 个可量化项目亮点")
            ));
        }
        return objectMapper.writeValueAsString(Map.of(
                "matchScore", 82,
                "strengths", List.of("后端项目结构完整", "包含 PostgreSQL、JWT、Docker 等实习常见技术", "具备真实岗位数据导入和 AI 分析展示"),
                "gaps", List.of("建议补充线上部署经验", "建议准备性能优化案例", "可以进一步说明 Redis 的使用场景"),
                "resumeAdvice", List.of("突出用户鉴权、数据隔离和 Flyway 迁移", "写清 AI 接口支持 mock 与真实 provider", "用数字说明导入岗位数量和测试覆盖"),
                "actionPlan", List.of("补充 README 截图", "准备 2 分钟项目演示流程", "整理 3 个后端技术亮点", "练习解释数据库设计")
        ));
    }
}
