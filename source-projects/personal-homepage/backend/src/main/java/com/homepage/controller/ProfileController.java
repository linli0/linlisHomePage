package com.homepage.controller;

import com.homepage.dto.ProfileDTO;
import com.homepage.dto.ProjectDTO;
import com.homepage.dto.Result;
import com.homepage.dto.SkillDTO;
import com.homepage.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public Result<ProfileDTO> getProfile() {
        return Result.success(profileService.getProfile());
    }

    @GetMapping("/skills")
    public Result<List<SkillDTO>> getAllSkills() {
        return Result.success(profileService.getAllSkills());
    }

    @GetMapping("/projects")
    public Result<List<ProjectDTO>> getAllProjects() {
        return Result.success(profileService.getAllProjects());
    }

    @GetMapping("/projects/featured")
    public Result<List<ProjectDTO>> getFeaturedProjects() {
        return Result.success(profileService.getFeaturedProjects());
    }

    @PostMapping("/skills")
    public Result<SkillDTO> createSkill(@RequestBody SkillDTO dto) {
        return Result.success(profileService.createSkill(dto));
    }

    @PostMapping("/projects")
    public Result<ProjectDTO> createProject(@RequestBody ProjectDTO dto) {
        return Result.success(profileService.createProject(dto));
    }

    @PutMapping("/skills/{id}")
    public Result<SkillDTO> updateSkill(@PathVariable Long id, @RequestBody SkillDTO dto) {
        return Result.success(profileService.updateSkill(id, dto));
    }

    @PutMapping("/projects/{id}")
    public Result<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO dto) {
        return Result.success(profileService.updateProject(id, dto));
    }

    @DeleteMapping("/skills/{id}")
    public Result<Void> deleteSkill(@PathVariable Long id) {
        profileService.deleteSkill(id);
        return Result.success();
    }

    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        profileService.deleteProject(id);
        return Result.success();
    }
}
