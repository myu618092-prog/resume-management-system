package com.internship.tracker.dto;

import com.internship.tracker.entity.Resume;
import java.time.OffsetDateTime;

public final class ResumeDtos {
    private ResumeDtos() {
    }

    public record ResumeResponse(
            Long id,
            String title,
            String originalFilename,
            String contentType,
            Long fileSize,
            String structuredContent,
            OffsetDateTime createdAt
    ) {
        public static ResumeResponse from(Resume resume) {
            return new ResumeResponse(
                    resume.getId(),
                    resume.getTitle(),
                    resume.getOriginalFilename(),
                    resume.getContentType(),
                    resume.getFileSize(),
                    resume.getStructuredContent(),
                    resume.getCreatedAt()
            );
        }
    }
}
