package com.internship.tracker.repository;

import com.internship.tracker.entity.Job;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Job> findByIdAndUserId(Long id, Long userId);
}
