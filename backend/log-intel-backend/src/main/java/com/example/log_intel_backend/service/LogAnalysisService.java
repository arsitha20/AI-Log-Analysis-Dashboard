package com.example.log_intel_backend.service;

import com.example.log_intel_backend.dto.LogAnalysisResponse;
import com.example.log_intel_backend.model.LogEntry;
import com.example.log_intel_backend.repository.LogEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class LogAnalysisService {

    private final LogEntryRepository logEntryRepository;
    private final WebClient webClient;

    public LogAnalysisService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8001")
                .build();
    }

    public LogAnalysisResponse analyzeAllLogs() {
        List<LogEntry> logs = logEntryRepository.findAll();

        Map<String, Object> requestBody = Map.of("logs", logs);

        Mono<LogAnalysisResponse> responseMono = webClient.post()
                .uri("/analyze")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(LogAnalysisResponse.class);

        return responseMono.block(); // block for simplicity (OK for this project)
    }
}

