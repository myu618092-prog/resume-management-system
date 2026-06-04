package com.internship.tracker.dto;

import jakarta.validation.constraints.NotBlank;

public final class AiDtos {
    private AiDtos() {
    }

    public record JobMatchRequest(
            Long jobId,
            Long resumeId,
            @NotBlank String jobDescription,
            @NotBlank String candidateProfile
    ) {
    }

    public record InterviewQuestionsRequest(
            Long jobId,
            @NotBlank String jobDescription,
            @NotBlank String candidateProfile
    ) {
    }

    public record AiResponse(
            Long recordId,
            String provider,
            String result
    ) {
    }
}
