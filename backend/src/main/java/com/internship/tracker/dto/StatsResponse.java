package com.internship.tracker.dto;

import java.io.Serializable;
import java.util.Map;

public record StatsResponse(
        long totalApplications,
        Map<String, Long> statusCounts,
        double interviewConversionRate,
        Map<String, Long> companyDistribution
) implements Serializable {
}
