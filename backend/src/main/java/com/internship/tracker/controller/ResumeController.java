package com.internship.tracker.controller;

import com.internship.tracker.dto.ResumeDtos.ResumeResponse;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.service.ResumeService;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping
    List<ResumeResponse> list(@AuthenticationPrincipal CurrentUser currentUser) {
        return resumeService.list(currentUser.id());
    }

    @PostMapping
    ResumeResponse upload(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam String title,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String projects
    ) throws IOException {
        return resumeService.upload(currentUser.user(), title, file, skills, education, projects);
    }
}
