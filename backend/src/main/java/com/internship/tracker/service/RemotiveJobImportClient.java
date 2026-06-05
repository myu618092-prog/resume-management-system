package com.internship.tracker.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RemotiveJobImportClient implements JobImportClient {
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public List<ImportedJob> fetch(String keyword, int limit) {
        String uri = UriComponentsBuilder.fromUriString("https://remotive.com/api/remote-jobs")
                .queryParam("search", keyword)
                .queryParam("limit", limit)
                .build()
                .toUriString();
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            Object jobs = response == null ? null : response.get("jobs");
            if (!(jobs instanceof List<?> rawJobs)) {
                return List.of();
            }
            return rawJobs.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(this::mapRemoteJob)
                    .limit(limit)
                    .toList();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Failed to import jobs from Remotive. Please try again later.");
        }
    }

    private ImportedJob mapRemoteJob(Map<?, ?> job) {
        String title = text(job.get("title"));
        String companyName = text(job.get("company_name"));
        String location = text(job.get("candidate_required_location"));
        String description = stripHtml(text(job.get("description")));
        return new ImportedJob(
                localizeTitle(title),
                companyName,
                localizeLocation(location),
                text(job.get("url")),
                localizeSummary(title, companyName, location, description)
        );
    }

    private String text(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains("Ã") || text.contains("Â") || text.contains("â")) {
            try {
                text = new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            } catch (RuntimeException ignored) {
                // Keep the original string and clean common mojibake sequences below.
            }
        }
        return cleanMojibake(text);
    }

    private String stripHtml(String html) {
        String plain = html.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
        plain = cleanMojibake(plain);
        if (plain.length() > 1200) {
            return plain.substring(0, 1200) + "...";
        }
        return plain.isBlank() ? "Imported from Remotive public job API." : plain;
    }

    private String cleanMojibake(String text) {
        return text
                .replace("Â ", " ")
                .replace("Â", "")
                .replace("Ã³", "ó")
                .replace("Ã£", "ã")
                .replace("Ã¡", "á")
                .replace("Ã©", "é")
                .replace("Ã­", "í")
                .replace("Ãº", "ú")
                .replace("Ã§", "ç")
                .replace("â", "-")
                .replace("â", "-")
                .replace("â", "'")
                .replace("â", "\"")
                .replace("â", "\"")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String localizeTitle(String title) {
        String lower = title.toLowerCase();
        String role;
        if (lower.contains("ios")) {
            role = "iOS 开发工程师";
        } else if (lower.contains("android")) {
            role = "Android 开发工程师";
        } else if (lower.contains("full-stack") || lower.contains("full stack")) {
            role = "全栈开发工程师";
        } else if (lower.contains("backend") || lower.contains("back-end")) {
            role = "后端开发工程师";
        } else if (lower.contains("frontend") || lower.contains("front-end") || lower.contains("react")) {
            role = "前端开发工程师";
        } else if (lower.contains("ai") || lower.contains("machine learning") || lower.contains("ml")) {
            role = "AI 工程师";
        } else if (lower.contains("tech lead") || lower.contains("lead")) {
            role = "技术负责人";
        } else if (lower.contains("architect")) {
            role = "系统架构师";
        } else if (lower.contains("product engineer")) {
            role = "产品研发工程师";
        } else if (lower.contains("software")) {
            role = "软件工程师";
        } else if (lower.contains("developer")) {
            role = "开发工程师";
        } else {
            role = "技术岗位";
        }
        return role + "（" + title + "）";
    }

    private String localizeLocation(String location) {
        if (location == null || location.isBlank()) {
            return "远程";
        }
        String lower = location.toLowerCase();
        if (lower.contains("worldwide")) {
            return "全球远程";
        }
        if (lower.contains("usa") || lower.contains("united states")) {
            return "美国远程";
        }
        if (lower.contains("canada")) {
            return "加拿大远程";
        }
        if (lower.contains("brazil")) {
            return "巴西远程";
        }
        if (lower.contains("europe")) {
            return "欧洲远程";
        }
        if (lower.contains("asia")) {
            return "亚洲远程";
        }
        return location + "（远程）";
    }

    private String localizeSummary(String title, String companyName, String location, String description) {
        String lower = (title + " " + description).toLowerCase();
        String stack = inferStack(lower);
        String responsibility = inferResponsibility(lower);
        String original = description.length() > 420 ? description.substring(0, 420) + "..." : description;
        return """
                该岗位来自 Remotive 公开岗位 API，原始岗位为：%s。
                公司：%s。工作地点：%s。
                技术方向：%s。
                岗位职责：%s。
                适合用于练习英文 JD 阅读、岗位匹配分析和简历投递管理。

                原始 JD 摘要：%s
                """.formatted(title, companyName, localizeLocation(location), stack, responsibility, original);
    }

    private String inferStack(String text) {
        if (text.contains("swift") || text.contains("ios")) {
            return "移动端 iOS、Swift、SwiftUI";
        }
        if (text.contains("react")) {
            return "前端 React、Web 应用开发";
        }
        if (text.contains("rails") || text.contains("ruby")) {
            return "Ruby on Rails、全栈 Web 开发";
        }
        if (text.contains("ai") || text.contains("llm") || text.contains("machine learning")) {
            return "AI 应用、LLM、机器学习工程";
        }
        if (text.contains("java") || text.contains("spring")) {
            return "Java、Spring Boot、后端服务开发";
        }
        return "软件工程、系统设计、远程协作";
    }

    private String inferResponsibility(String text) {
        if (text.contains("api")) {
            return "设计和开发业务功能，参与 API 集成、代码评审和性能质量改进";
        }
        if (text.contains("llm") || text.contains("ai")) {
            return "构建 AI 驱动的产品功能，参与模型应用、检索、评估和工程化落地";
        }
        if (text.contains("mobile") || text.contains("ios") || text.contains("android")) {
            return "开发移动端功能，保障应用性能、用户体验和代码可维护性";
        }
        return "参与产品功能开发、技术方案设计、团队协作和线上问题改进";
    }
}
