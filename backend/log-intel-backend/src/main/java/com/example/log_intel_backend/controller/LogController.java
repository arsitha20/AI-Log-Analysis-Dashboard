package com.example.log_intel_backend.controller;

import com.example.log_intel_backend.dto.LogIngestRequest;
import com.example.log_intel_backend.model.LogEntry;
import com.example.log_intel_backend.service.LogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*") // allow React dev server
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    // POST /api/logs/ingest
    @PostMapping("/ingest")
    public List<LogEntry> ingest(@RequestBody LogIngestRequest request) {
        return logService.ingestLogs(request);
    }

    // GET /api/logs
    @GetMapping
    public List<LogEntry> getAll() {
        return logService.getAllLogs();
    }
}
