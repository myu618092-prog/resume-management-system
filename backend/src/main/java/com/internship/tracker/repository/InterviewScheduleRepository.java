package com.internship.tracker.repository;

import com.internship.tracker.entity.InterviewSchedule;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    List<InterviewSchedule> findByUserIdAndInterviewTimeBetweenOrderByInterviewTimeAsc(
            Long userId,
            OffsetDateTime from,
            OffsetDateTime to
    );
}
