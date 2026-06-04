package com.internship.tracker.dto;

import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public final class ApplicationDtos {
    private ApplicationDtos() {
    }

    public record UpdateStatusRequest(
            @NotNull ApplicationStatus status,
            String notes
    ) {
    }

    public record ApplicationResponse(
            Long id,
            Long jobId,
            String companyName,
            String positionName,
            ApplicationStatus status,
            String notes,
            OffsetDateTime appliedAt,
            OffsetDateTime updatedAt
    ) {
        public static ApplicationResponse from(Application application) {
            return new ApplicationResponse(
                    application.getId(),
                    application.getJob().getId(),
                    application.getJob().getCompanyName(),
                    application.getJob().getPositionName(),
                    application.getStatus(),
                    application.getNotes(),
                    application.getAppliedAt(),
                    application.getUpdatedAt()
            );
        }
    }
}
