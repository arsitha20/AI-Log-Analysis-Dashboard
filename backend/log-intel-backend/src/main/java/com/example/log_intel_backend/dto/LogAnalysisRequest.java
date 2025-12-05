package com.example.log_intel_backend.dto;

import lombok.Data;

@Data
public class LogAnalysisRequest {
    // could be a filter later, for now just analyze all
    private String serviceName;
}
