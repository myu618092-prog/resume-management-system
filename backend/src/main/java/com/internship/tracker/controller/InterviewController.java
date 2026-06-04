package com.internship.tracker.controller;

import com.internship.tracker.dto.InterviewDtos.CreateInterviewRequest;
import com.internship.tracker.dto.InterviewDtos.InterviewResponse;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.service.InterviewService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    List<InterviewResponse> list(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return interviewService.list(currentUser.id(), from, to);
    }

    @PostMapping
    InterviewResponse create(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        return interviewService.create(currentUser.user(), request);
    }
}
