package com.homepage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandResponse {

    private Long historyId;
    private String output;
    private Integer exitCode;
    private Boolean success;
    private Long executionTimeMs;
    private LocalDateTime executedAt;
}
