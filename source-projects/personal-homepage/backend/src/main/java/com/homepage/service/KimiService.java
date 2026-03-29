package com.homepage.service;

import com.homepage.dto.CommandRequest;
import com.homepage.dto.CommandResponse;
import com.homepage.entity.CommandHistory;
import com.homepage.repository.CommandHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KimiService {

    private final CommandHistoryRepository commandHistoryRepository;

    @Value("${app.kimi.cli-path:kimi}")
    private String kimiCliPath;

    @Value("${app.kimi.timeout-seconds:300}")
    private int timeoutSeconds;

    public CommandResponse executeKimiCommand(CommandRequest request, HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();
        String output = "";
        int exitCode = -1;
        boolean success = false;

        try {
            List<String> command = buildCommand(request);
            log.info("Executing kimi command: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                output = "Command timed out after " + timeoutSeconds + " seconds";
                exitCode = -1;
            } else {
                exitCode = process.exitValue();
                output = outputBuilder.toString();
                success = exitCode == 0;
            }

        } catch (Exception e) {
            log.error("Error executing kimi command", e);
            output = "Error: " + e.getMessage();
            exitCode = -1;
        }

        long executionTime = System.currentTimeMillis() - startTime;

        CommandHistory history = CommandHistory.builder()
                .commandType("kimi")
                .commandInput(request.getPrompt())
                .commandOutput(output)
                .exitCode(exitCode)
                .executionTimeMs(executionTime)
                .executedBy("anonymous")
                .ipAddress(getClientIp(httpRequest))
                .isSuccess(success)
                .build();

        CommandHistory saved = commandHistoryRepository.save(history);

        return CommandResponse.builder()
                .historyId(saved.getId())
                .output(output)
                .exitCode(exitCode)
                .success(success)
                .executionTimeMs(executionTime)
                .executedAt(saved.getCreatedAt())
                .build();
    }

    private List<String> buildCommand(CommandRequest request) {
        List<String> command = new ArrayList<>();
        command.add(kimiCliPath);
        command.add("ask");
        
        if (request.getModel() != null && !request.getModel().isEmpty()) {
            command.add("--model");
            command.add(request.getModel());
        }
        
        command.add(request.getPrompt());
        return command;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public List<CommandHistory> getCommandHistory(int limit) {
        return commandHistoryRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
