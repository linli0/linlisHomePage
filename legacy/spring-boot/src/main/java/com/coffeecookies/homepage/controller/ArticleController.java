package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.*;
import com.coffeecookies.homepage.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/public/list")
    public ResponseEntity<Result<PageResult<ArticleDTO>>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<ArticleDTO> articles = articleService.getPublishedArticles(pageable);
        
        PageResult<ArticleDTO> result = new PageResult<>(
                articles.getContent(),
                articles.getTotalElements(),
                articles.getTotalPages(),
                articles.getNumber(),
                articles.getSize(),
                articles.isFirst(),
                articles.isLast()
        );
        
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Result<ArticleDTO>> getArticleById(@PathVariable Long id) {
        // Increment view count
        articleService.incrementViewCount(id);
        return ResponseEntity.ok(Result.success(articleService.getArticleById(id)));
    }

    @GetMapping("/public/recent")
    public ResponseEntity<Result<List<ArticleDTO>>> getRecentArticles() {
        return ResponseEntity.ok(Result.success(articleService.getRecentArticles(5)));
    }

    @GetMapping("/public/popular")
    public ResponseEntity<Result<List<ArticleDTO>>> getPopularArticles() {
        return ResponseEntity.ok(Result.success(articleService.getPopularArticles(5)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<ArticleDTO>> createArticle(@RequestBody ArticleDTO dto) {
        return ResponseEntity.ok(Result.success(articleService.createArticle(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<ArticleDTO>> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO dto) {
        return ResponseEntity.ok(Result.success(articleService.updateArticle(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(Result.success());
    }
}
