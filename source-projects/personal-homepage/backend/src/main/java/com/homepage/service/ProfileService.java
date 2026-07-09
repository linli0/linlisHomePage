package com.homepage.service;

import com.homepage.dto.*;
import com.homepage.entity.Project;
import com.homepage.entity.Skill;
import com.homepage.repository.ProjectRepository;
import com.homepage.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;

    @Value("${app.profile.name:Developer}")
    private String profileName;

    @Value("${app.profile.title:Full Stack Developer}")
    private String profileTitle;

    @Value("${app.profile.email:}")
    private String profileEmail;

    @Value("${app.profile.github:}")
    private String profileGithub;

    public ProfileDTO getProfile() {
        List<Skill> skills = skillRepository.findAllByOrderByCategoryAscSortOrderAsc();
        List<Project> projects = projectRepository.findAllByOrderBySortOrderAsc();

        Map<String, List<SkillDTO>> skillsByCategory = skills.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() != null ? s.getCategory() : "Other",
                        Collectors.mapping(this::convertSkillToDTO, Collectors.toList())
                ));

        return ProfileDTO.builder()
                .name(profileName)
                .title(profileTitle)
                .email(profileEmail)
                .github(profileGithub)
                .skills(skills.stream().map(this::convertSkillToDTO).collect(Collectors.toList()))
                .skillsByCategory(skillsByCategory)
                .projects(projects.stream().map(this::convertProjectToDTO).collect(Collectors.toList()))
                .featuredProjects(projects.stream()
                        .filter(Project::getIsFeatured)
                        .map(this::convertProjectToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAllByOrderByCategoryAscSortOrderAsc()
                .stream()
                .map(this::convertSkillToDTO)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::convertProjectToDTO)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getFeaturedProjects() {
        return projectRepository.findByIsFeaturedTrueOrderBySortOrderAsc()
                .stream()
                .map(this::convertProjectToDTO)
                .collect(Collectors.toList());
    }

    public SkillDTO createSkill(SkillDTO dto) {
        Skill skill = new Skill();
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        skill.setProficiencyLevel(dto.getProficiencyLevel());
        skill.setIcon(dto.getIcon());
        skill.setSortOrder(dto.getSortOrder());
        Skill saved = skillRepository.save(skill);
        return convertSkillToDTO(saved);
    }

    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = new Project();
        updateProjectFromDTO(project, dto);
        Project saved = projectRepository.save(project);
        return convertProjectToDTO(saved);
    }

    public SkillDTO updateSkill(Long id, SkillDTO dto) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        skill.setProficiencyLevel(dto.getProficiencyLevel());
        skill.setIcon(dto.getIcon());
        skill.setSortOrder(dto.getSortOrder());
        Skill saved = skillRepository.save(skill);
        return convertSkillToDTO(saved);
    }

    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        updateProjectFromDTO(project, dto);
        Project saved = projectRepository.save(project);
        return convertProjectToDTO(saved);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    private void updateProjectFromDTO(Project project, ProjectDTO dto) {
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setUrl(dto.getUrl());
        project.setGithubUrl(dto.getGithubUrl());
        project.setImage(dto.getImage());
        project.setCategory(dto.getCategory());
        project.setTechnologies(dto.getTechnologies() != null ? 
                String.join(",", dto.getTechnologies()) : null);
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setIsFeatured(dto.getIsFeatured());
        project.setSortOrder(0);
    }

    private SkillDTO convertSkillToDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .proficiencyLevel(skill.getProficiencyLevel())
                .icon(skill.getIcon())
                .build();
    }

    private ProjectDTO convertProjectToDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .url(project.getUrl())
                .githubUrl(project.getGithubUrl())
                .image(project.getImage())
                .category(project.getCategory())
                .technologies(project.getTechnologies() != null ? 
                        Arrays.asList(project.getTechnologies().split(",")) : null)
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .isFeatured(project.getIsFeatured())
                .build();
    }
}
