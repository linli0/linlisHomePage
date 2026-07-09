package com.homepage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDTO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private Integer viewCount;
    private Boolean isPublished;
    private Boolean isTop;
    private Set<TagDTO> tags;
    private CategoryDTO category;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
