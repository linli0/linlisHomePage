package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.Result;
import com.coffeecookies.homepage.dto.TagDTO;
import com.coffeecookies.homepage.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Result<List<TagDTO>>> getAllTags() {
        return ResponseEntity.ok(Result.success(tagService.getAllTags()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<TagDTO>> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(tagService.getTagById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<TagDTO>> createTag(@RequestBody TagDTO dto) {
        return ResponseEntity.ok(Result.success(tagService.createTag(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<TagDTO>> updateTag(@PathVariable Long id, @RequestBody TagDTO dto) {
        return ResponseEntity.ok(Result.success(tagService.updateTag(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(Result.success());
    }
}
