package com.internship.tracker.service;

import com.internship.tracker.entity.AiAnalysisType;

public interface AiProviderClient {
    boolean enabled();

    String analyze(AiAnalysisType type, String prompt);
}
