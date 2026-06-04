package com.internship.tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tracker.dto.ResumeDtos.ResumeResponse;
import com.internship.tracker.entity.Resume;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.ResumeRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final Path uploadDir;
    private final ObjectMapper objectMapper;

    public ResumeService(
            ResumeRepository resumeRepository,
            @Value("${app.upload-dir}") String uploadDir,
            ObjectMapper objectMapper
    ) {
        this.resumeRepository = resumeRepository;
        this.uploadDir = Path.of(uploadDir);
        this.objectMapper = objectMapper;
    }

    public List<ResumeResponse> list(Long userId) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ResumeResponse::from)
                .toList();
    }

    @Transactional
    public ResumeResponse upload(User user, String title, MultipartFile file, String skills, String education, String projects)
            throws IOException {
        Files.createDirectories(uploadDir);
        String safeName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path target = uploadDir.resolve(safeName);
        file.transferTo(target);

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setTitle(title);
        resume.setOriginalFilename(file.getOriginalFilename());
        resume.setContentType(file.getContentType());
        resume.setFileSize(file.getSize());
        resume.setFilePath(target.toString());
        resume.setStructuredContent(objectMapper.writeValueAsString(Map.of(
                "skills", value(skills),
                "education", value(education),
                "projects", value(projects)
        )));
        return ResumeResponse.from(resumeRepository.save(resume));
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
