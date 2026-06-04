package com.internship.tracker.repository;

import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    Optional<Application> findByJobIdAndUserId(Long jobId, Long userId);

    long countByUserIdAndStatus(Long userId, ApplicationStatus status);
}
