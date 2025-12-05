package com.example.log_intel_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. 2025-12-05T10:15:30
    private LocalDateTime timestamp;

    // INFO, WARN, ERROR, DEBUG, etc.
    private String level;

    // Optional: which service/module produced this log
    private String serviceName;

    // Parsed short message
    @Column(length = 2000)
    private String message;

    // full raw line to keep the original
    @Column(length = 4000)
    private String rawLine;
}

