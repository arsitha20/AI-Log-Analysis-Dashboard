package com.example.log_intel_backend.util;

import com.example.log_intel_backend.model.LogEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogParser {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LogEntry parseLine(String line, String serviceName) {
        try {
            // naive split: "timestamp level rest-of-line"
            // 2025-12-05 10:15:30 ERROR OrderService - Failed to connect

            String timestampStr = line.substring(0, 19); // first 19 chars
            LocalDateTime ts = LocalDateTime.parse(timestampStr, FORMATTER);

            String remaining = line.substring(20).trim(); // after space
            String[] parts = remaining.split(" ", 2);

            String level = parts[0];
            String message = parts.length > 1 ? parts[1] : "";

            return LogEntry.builder()
                    .timestamp(ts)
                    .level(level)
                    .serviceName(serviceName)
                    .message(message)
                    .rawLine(line)
                    .build();
        } catch (Exception e) {
            // If parsing fails, store as UNKNOWN
            return LogEntry.builder()
                    .timestamp(LocalDateTime.now())
                    .level("UNKNOWN")
                    .serviceName(serviceName)
                    .message(line)
                    .rawLine(line)
                    .build();
        }
    }
}