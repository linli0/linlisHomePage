package com.coffeecookies.homepage.dto;

import lombok.Data;

@Data
public class AIStatusResponse {
    private String status;
    private String message;
    private String hint;
    private String url;             // 当前使用的 Ollama URL
    private Boolean localOnline;    // 本地 Ollama 是否在线
    private Boolean remoteOnline;   // 远程 Ollama 是否在线
    private Boolean remoteEnabled;  // 是否启用了远程切换
}