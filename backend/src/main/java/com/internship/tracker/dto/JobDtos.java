package com.internship.tracker.dto;

import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import com.internship.tracker.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

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

    public record ImportJobsRequest(
            String source,
            @Min(1) @Max(30) Integer limit,
            String keyword
    ) {
        public String normalizedSource() {
            return source == null || source.isBlank() ? "REMOTIVE" : source.trim().toUpperCase();
        }

        public int normalizedLimit() {
            return limit == null ? 15 : Math.max(1, Math.min(30, limit));
        }

        public String normalizedKeyword() {
            return keyword == null || keyword.isBlank() ? "java spring" : keyword.trim();
        }
    }

    public record ImportJobsResponse(
            String source,
            int importedCount,
            int skippedCount,
            List<JobResponse> jobs
    ) {
    }
}
