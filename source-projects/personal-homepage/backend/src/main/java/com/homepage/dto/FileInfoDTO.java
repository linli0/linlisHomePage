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
public class FileInfoDTO {

    private Long id;
    private String name;
    private String path;
    private String url;
    private String shareToken;
    private String shareUrl;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private String extension;
    private Boolean isDirectory;
    private Boolean isPublic;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
