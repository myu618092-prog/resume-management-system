package com.internship.tracker.repository;

import com.internship.tracker.entity.AiAnalysisRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAnalysisRecordRepository extends JpaRepository<AiAnalysisRecord, Long> {
    List<AiAnalysisRecord> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
