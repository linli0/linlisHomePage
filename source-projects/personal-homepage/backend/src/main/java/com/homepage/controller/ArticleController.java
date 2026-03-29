package com.homepage.controller;

import com.homepage.dto.ArticleDTO;
import com.homepage.dto.PageResult;
import com.homepage.dto.Result;
import com.homepage.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleDTO>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        return Result.success(articleService.getArticles(page, size, categoryId, tagId));
    }

    @GetMapping("/{id}")
    public Result<ArticleDTO> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    @GetMapping("/search")
    public Result<PageResult<ArticleDTO>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.searchArticles(keyword, page, size));
    }

    @GetMapping("/latest")
    public Result<List<ArticleDTO>> getLatestArticles(
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(articleService.getLatestArticles(limit));
    }

    @GetMapping("/popular")
    public Result<List<ArticleDTO>> getPopularArticles(
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(articleService.getPopularArticles(limit));
    }

    @PostMapping
    public Result<ArticleDTO> createArticle(@RequestBody ArticleDTO dto) {
        return Result.success(articleService.createArticle(dto));
    }

    @PutMapping("/{id}")
    public Result<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO dto) {
        return Result.success(articleService.updateArticle(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }
}
