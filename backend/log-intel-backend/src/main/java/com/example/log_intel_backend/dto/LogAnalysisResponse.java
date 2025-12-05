package com.example.log_intel_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LogAnalysisResponse {

    private List<ErrorCluster> clusters;
    private String overallSummary;

    @Data
    @AllArgsConstructor
    public static class ErrorCluster {
        private String pattern;
        private long count;
        private String explanation;
        private String suggestedFix;
    }
}
