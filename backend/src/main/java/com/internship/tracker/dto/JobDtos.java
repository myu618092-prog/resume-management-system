package com.internship.tracker.dto;

import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import com.internship.tracker.entity.Job;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class JobDtos {
    private JobDtos() {
    }

    public record UpsertJobRequest(
            @NotBlank String companyName,
            @NotBlank String positionName,
            String city,
            @NotBlank String jobDescription,
            String sourceUrl,
            LocalDate deadline
    ) {
    }

    public record JobResponse(
            Long id,
            Long applicationId,
            String companyName,
            String positionName,
            String city,
            String jobDescription,
            String sourceUrl,
            LocalDate deadline,
            ApplicationStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static JobResponse from(Job job, Application application) {
            return new JobResponse(
                    job.getId(),
                    application.getId(),
                    job.getCompanyName(),
                    job.getPositionName(),
                    job.getCity(),
                    job.getJobDescription(),
                    job.getSourceUrl(),
                    job.getDeadline(),
                    application.getStatus(),
                    job.getCreatedAt(),
                    job.getUpdatedAt()
            );
        }
    }
}
