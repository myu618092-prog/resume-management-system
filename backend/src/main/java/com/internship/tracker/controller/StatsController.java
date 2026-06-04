package com.internship.tracker.controller;

import com.internship.tracker.dto.StatsResponse;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.service.StatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    StatsResponse stats(@AuthenticationPrincipal CurrentUser currentUser) {
        return statsService.stats(currentUser.id());
    }
}
