package com.example.log_intel_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class LogIngestRequest {
    // list of raw log lines pasted/uploaded by user
    private List<String> lines;

    // optional: name of the application/service
    private String serviceName;
}