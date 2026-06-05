package com.internship.tracker.service;

import java.util.List;

public interface JobImportClient {
    List<ImportedJob> fetch(String keyword, int limit);

    record ImportedJob(
            String title,
            String companyName,
            String location,
            String url,
            String summary
    ) {
    }
}
