package com.internship.tracker.dto;

import com.internship.tracker.entity.InterviewSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public final class InterviewDtos {
    private InterviewDtos() {
    }

    public record CreateInterviewRequest(
            @NotNull Long applicationId,
            @NotBlank String title,
            @NotNull OffsetDateTime interviewTime,
            String location,
            String notes
    ) {
    }

    public record InterviewResponse(
            Long id,
            Long applicationId,
            String companyName,
            String positionName,
            String title,
            OffsetDateTime interviewTime,
            String location,
            String notes
    ) {
        public static InterviewResponse from(InterviewSchedule schedule) {
            return new InterviewResponse(
                    schedule.getId(),
                    schedule.getApplication().getId(),
                    schedule.getApplication().getJob().getCompanyName(),
                    schedule.getApplication().getJob().getPositionName(),
                    schedule.getTitle(),
                    schedule.getInterviewTime(),
                    schedule.getLocation(),
                    schedule.getNotes()
            );
        }
    }
}
