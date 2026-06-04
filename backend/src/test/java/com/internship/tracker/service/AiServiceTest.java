package com.internship.tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tracker.dto.AiDtos.JobMatchRequest;
import com.internship.tracker.entity.AiAnalysisRecord;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.AiAnalysisRecordRepository;
import com.internship.tracker.repository.JobRepository;
import com.internship.tracker.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {
    @Mock
    private AiAnalysisRecordRepository recordRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Test
    void mockModeStoresAnalysisRecord() throws Exception {
        AiService service = new AiService(
                recordRepository,
                jobRepository,
                resumeRepository,
                new ObjectMapper(),
                "",
                "https://api.openai.com/v1",
                "gpt-4.1-mini"
        );
        when(recordRepository.save(any(AiAnalysisRecord.class))).thenAnswer(invocation -> {
            AiAnalysisRecord record = invocation.getArgument(0);
            ReflectionTestUtils.setField(record, "id", 100L);
            return record;
        });

        var response = service.analyzeJobMatch(user(), new JobMatchRequest(
                null,
                null,
                "Need Java and PostgreSQL",
                "Java, Spring Boot, PostgreSQL"
        ));

        assertThat(response.recordId()).isEqualTo(100L);
        assertThat(response.provider()).isEqualTo("mock");
        assertThat(response.result()).contains("matchScore");
    }

    private User user() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setUsername("demo");
        user.setEmail("demo@example.com");
        user.setPasswordHash("hash");
        return user;
    }
}
