package com.internship.tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.internship.tracker.dto.ApplicationDtos.UpdateStatusRequest;
import com.internship.tracker.dto.JobDtos.UpsertJobRequest;
import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import com.internship.tracker.entity.Job;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.ApplicationRepository;
import com.internship.tracker.repository.JobRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJobAlsoCreatesTodoApplication() {
        User user = user(7L);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            ReflectionTestUtils.setField(job, "id", 11L);
            return job;
        });
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application application = invocation.getArgument(0);
            ReflectionTestUtils.setField(application, "id", 21L);
            return application;
        });

        var response = jobService.create(user, request());

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.applicationId()).isEqualTo(21L);
        assertThat(response.status()).isEqualTo(ApplicationStatus.TODO);

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getStatus()).isEqualTo(ApplicationStatus.TODO);
    }

    @Test
    void updateStatusSetsAppliedAtWhenMovingToApplied() {
        Application application = new Application();
        ReflectionTestUtils.setField(application, "id", 21L);
        application.setUser(user(7L));
        application.setJob(job(11L));
        application.setStatus(ApplicationStatus.TODO);

        when(applicationRepository.findByIdAndUserId(21L, 7L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = jobService.updateStatus(7L, 21L, new UpdateStatusRequest(ApplicationStatus.APPLIED, "sent"));

        assertThat(response.status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.appliedAt()).isNotNull();
        assertThat(application.getNotes()).isEqualTo("sent");
    }

    private UpsertJobRequest request() {
        return new UpsertJobRequest(
                "OpenAI",
                "Backend Intern",
                "Remote",
                "Build APIs with Spring and PostgreSQL",
                "https://example.com",
                LocalDate.now().plusDays(30)
        );
    }

    private User user(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername("demo");
        user.setEmail("demo@example.com");
        user.setPasswordHash("hash");
        return user;
    }

    private Job job(Long id) {
        Job job = new Job();
        ReflectionTestUtils.setField(job, "id", id);
        job.setUser(user(7L));
        job.setCompanyName("OpenAI");
        job.setPositionName("Backend Intern");
        job.setJobDescription("Build APIs");
        return job;
    }
}
