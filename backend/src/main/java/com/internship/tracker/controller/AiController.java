package com.internship.tracker.controller;

import com.internship.tracker.dto.AiDtos.AiResponse;
import com.internship.tracker.dto.AiDtos.InterviewQuestionsRequest;
import com.internship.tracker.dto.AiDtos.JobMatchRequest;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.service.AiService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze-job-match")
    AiResponse analyzeJobMatch(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody JobMatchRequest request
    ) throws Exception {
        return aiService.analyzeJobMatch(currentUser.user(), request);
    }

    @PostMapping("/interview-questions")
    AiResponse interviewQuestions(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody InterviewQuestionsRequest request
    ) throws Exception {
        return aiService.interviewQuestions(currentUser.user(), request);
    }
}
