package com.internship.tracker.service;

import com.internship.tracker.dto.StatsResponse;
import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import com.internship.tracker.repository.ApplicationRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private final ApplicationRepository applicationRepository;

    public StatsService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Cacheable(value = "userStats", key = "#userId")
    public StatsResponse stats(Long userId) {
        var applications = applicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long total = applications.size();
        Map<String, Long> statusCounts = Arrays.stream(ApplicationStatus.values())
                .collect(Collectors.toMap(Enum::name, status -> applicationRepository.countByUserIdAndStatus(userId, status)));
        long interviews = statusCounts.get(ApplicationStatus.INTERVIEW.name())
                + statusCounts.get(ApplicationStatus.OFFER.name());
        double conversionRate = total == 0 ? 0.0 : Math.round((interviews * 10000.0 / total)) / 100.0;
        Map<String, Long> companyDistribution = applications.stream()
                .collect(Collectors.groupingBy(
                        application -> application.getJob().getCompanyName(),
                        Collectors.counting()
                ));
        return new StatsResponse(total, statusCounts, conversionRate, companyDistribution);
    }

    @CacheEvict(value = "userStats", key = "#userId")
    public void evict(Long userId) {
    }
}
