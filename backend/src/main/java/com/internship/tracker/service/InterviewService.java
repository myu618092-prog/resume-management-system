package com.internship.tracker.service;

import com.internship.tracker.dto.InterviewDtos.CreateInterviewRequest;
import com.internship.tracker.dto.InterviewDtos.InterviewResponse;
import com.internship.tracker.entity.Application;
import com.internship.tracker.entity.InterviewSchedule;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.ApplicationRepository;
import com.internship.tracker.repository.InterviewScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InterviewService {
    private final InterviewScheduleRepository scheduleRepository;
    private final ApplicationRepository applicationRepository;

    public InterviewService(
            InterviewScheduleRepository scheduleRepository,
            ApplicationRepository applicationRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<InterviewResponse> list(Long userId, OffsetDateTime from, OffsetDateTime to) {
        return scheduleRepository.findByUserIdAndInterviewTimeBetweenOrderByInterviewTimeAsc(userId, from, to)
                .stream()
                .map(InterviewResponse::from)
                .toList();
    }

    @Transactional
    public InterviewResponse create(User user, CreateInterviewRequest request) {
        Application application = applicationRepository.findByIdAndUserId(request.applicationId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        InterviewSchedule schedule = new InterviewSchedule();
        schedule.setUser(user);
        schedule.setApplication(application);
        schedule.setTitle(request.title());
        schedule.setInterviewTime(request.interviewTime());
        schedule.setLocation(request.location());
        schedule.setNotes(request.notes());
        return InterviewResponse.from(scheduleRepository.save(schedule));
    }
}
