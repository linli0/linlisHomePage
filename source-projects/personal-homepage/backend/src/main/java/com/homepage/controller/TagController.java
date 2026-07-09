package com.homepage.controller;

import com.homepage.dto.Result;
import com.homepage.dto.TagDTO;
import com.homepage.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagDTO>> getAllTags() {
        return Result.success(tagService.getAllTagsSimple());
    }

    @GetMapping("/with-count")
    public Result<List<TagDTO>> getAllTagsWithCount() {
        return Result.success(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public Result<TagDTO> getTag(@PathVariable Long id) {
        return Result.success(tagService.getTagById(id));
    }

    @PostMapping
    public Result<TagDTO> createTag(@RequestBody TagDTO dto) {
        return Result.success(tagService.createTag(dto));
    }

    @PutMapping("/{id}")
    public Result<TagDTO> updateTag(@PathVariable Long id, @RequestBody TagDTO dto) {
        return Result.success(tagService.updateTag(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
