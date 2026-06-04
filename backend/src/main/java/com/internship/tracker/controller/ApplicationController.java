package com.internship.tracker.controller;

import com.internship.tracker.dto.ApplicationDtos.ApplicationResponse;
import com.internship.tracker.dto.ApplicationDtos.UpdateStatusRequest;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.service.JobService;
import com.internship.tracker.service.StatsService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final JobService jobService;
    private final StatsService statsService;

    public ApplicationController(JobService jobService, StatsService statsService) {
        this.jobService = jobService;
        this.statsService = statsService;
    }

    @PatchMapping("/{id}/status")
    ApplicationResponse updateStatus(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        ApplicationResponse response = jobService.updateStatus(currentUser.id(), id, request);
        statsService.evict(currentUser.id());
        return response;
    }
}
