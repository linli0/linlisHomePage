package com.homepage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private String url;
    private String githubUrl;
    private String image;
    private String category;
    private List<String> technologies;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isFeatured;
}
