package com.example.log_intel_backend.service;

import com.example.log_intel_backend.dto.LogIngestRequest;
import com.example.log_intel_backend.model.LogEntry;
import com.example.log_intel_backend.repository.LogEntryRepository;
import com.example.log_intel_backend.util.LogParser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final LogEntryRepository logEntryRepository;

    public LogService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public List<LogEntry> ingestLogs(LogIngestRequest request) {
        String serviceName = request.getServiceName() != null ?
                request.getServiceName() : "default-service";

        List<LogEntry> entries = request.getLines().stream()
                .map(line -> LogParser.parseLine(line, serviceName))
                .collect(Collectors.toList());

        return logEntryRepository.saveAll(entries);
    }

    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }
}
