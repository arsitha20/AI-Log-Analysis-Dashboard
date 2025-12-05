package com.example.log_intel_backend.controller;

import com.example.log_intel_backend.dto.LogAnalysisResponse;
import com.example.log_intel_backend.service.LogAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final LogAnalysisService logAnalysisService;

    public AnalysisController(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    // GET /api/analysis
    @GetMapping
    public LogAnalysisResponse analyzeAll() {
        return logAnalysisService.analyzeAllLogs();
    }
}

