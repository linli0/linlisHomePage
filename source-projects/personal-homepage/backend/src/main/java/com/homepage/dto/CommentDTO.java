package com.homepage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long id;
    private String author;
    private String email;
    private String content;
    private Long articleId;
    private String articleTitle;
    private Long parentId;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;
}
