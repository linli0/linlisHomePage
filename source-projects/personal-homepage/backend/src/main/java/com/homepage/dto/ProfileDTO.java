package com.homepage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDTO {

    private String name;
    private String title;
    private String bio;
    private String email;
    private String github;
    private String linkedin;
    private String website;
    private String avatar;
    private List<SkillDTO> skills;
    private Map<String, List<SkillDTO>> skillsByCategory;
    private List<ProjectDTO> projects;
    private List<ProjectDTO> featuredProjects;
    private List<TimelineDTO> timeline;
}
