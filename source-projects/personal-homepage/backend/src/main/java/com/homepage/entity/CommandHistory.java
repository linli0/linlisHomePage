package com.homepage.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "command_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_type", nullable = false, length = 50)
    private String commandType;

    @Column(name = "command_input", nullable = false, columnDefinition = "TEXT")
    private String commandInput;

    @Column(name = "command_output", columnDefinition = "LONGTEXT")
    private String commandOutput;

    @Column(name = "exit_code")
    private Integer exitCode;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "executed_by", length = 50)
    private String executedBy;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "is_success")
    @Builder.Default
    private Boolean isSuccess = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
