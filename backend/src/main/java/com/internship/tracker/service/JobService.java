package com.internship.tracker.service;

import com.internship.tracker.dto.ApplicationDtos.ApplicationResponse;
import com.internship.tracker.dto.ApplicationDtos.UpdateStatusRequest;
import com.internship.tracker.dto.JobDtos.JobResponse;
import com.internship.tracker.dto.JobDtos.UpsertJobRequest;
import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import com.internship.tracker.entity.Job;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.ApplicationRepository;
import com.internship.tracker.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<JobResponse> list(Long userId) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(job -> JobResponse.from(job, applicationRepository.findByJobIdAndUserId(job.getId(), userId)
                        .orElseThrow(() -> new EntityNotFoundException("Application not found"))))
                .toList();
    }

    @Transactional
    public JobResponse create(User user, UpsertJobRequest request) {
        Job job = new Job();
        fill(job, request);
        job.setUser(user);
        job = jobRepository.save(job);

        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.TODO);
        application = applicationRepository.save(application);
        return JobResponse.from(job, application);
    }

    @Transactional
    public JobResponse update(Long userId, Long jobId, UpsertJobRequest request) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        fill(job, request);
        job.touch();
        Job saved = jobRepository.save(job);
        Application application = applicationRepository.findByJobIdAndUserId(jobId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));
        return JobResponse.from(saved, application);
    }

    @Transactional
    public void delete(Long userId, Long jobId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));
        jobRepository.delete(job);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long userId, Long applicationId, UpdateStatusRequest request) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));
        application.setStatus(request.status());
        application.setNotes(request.notes());
        if (request.status() == ApplicationStatus.APPLIED && application.getAppliedAt() == null) {
            application.setAppliedAt(OffsetDateTime.now());
        }
        application.touch();
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    private void fill(Job job, UpsertJobRequest request) {
        job.setCompanyName(request.companyName());
        job.setPositionName(request.positionName());
        job.setCity(request.city());
        job.setJobDescription(request.jobDescription());
        job.setSourceUrl(request.sourceUrl());
        job.setDeadline(request.deadline());
    }
}
