package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private boolean published;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private CategoryDTO category;
    private Set<TagDTO> tags;
    private UserDTO author;
}
