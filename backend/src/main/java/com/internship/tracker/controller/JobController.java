package com.internship.tracker.controller;

import com.internship.tracker.dto.JobDtos.ImportJobsRequest;
import com.internship.tracker.dto.JobDtos.ImportJobsResponse;
import com.internship.tracker.dto.JobDtos.JobResponse;
import com.internship.tracker.dto.JobDtos.UpsertJobRequest;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.service.JobService;
import com.internship.tracker.service.StatsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;
    private final StatsService statsService;

    public JobController(JobService jobService, StatsService statsService) {
        this.jobService = jobService;
        this.statsService = statsService;
    }

    @GetMapping
    List<JobResponse> list(@AuthenticationPrincipal CurrentUser currentUser) {
        return jobService.list(currentUser.id());
    }

    @PostMapping
    JobResponse create(@AuthenticationPrincipal CurrentUser currentUser, @Valid @RequestBody UpsertJobRequest request) {
        JobResponse response = jobService.create(currentUser.user(), request);
        statsService.evict(currentUser.id());
        return response;
    }

    @PostMapping("/import")
    ImportJobsResponse importJobs(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody ImportJobsRequest request
    ) {
        ImportJobsResponse response = jobService.importJobs(currentUser.user(), request);
        statsService.evict(currentUser.id());
        return response;
    }

    @PutMapping("/{id}")
    JobResponse update(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpsertJobRequest request
    ) {
        JobResponse response = jobService.update(currentUser.id(), id, request);
        statsService.evict(currentUser.id());
        return response;
    }

    @DeleteMapping("/{id}")
    void delete(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long id) {
        jobService.delete(currentUser.id(), id);
        statsService.evict(currentUser.id());
    }
}
