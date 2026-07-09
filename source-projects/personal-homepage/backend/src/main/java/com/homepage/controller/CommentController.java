package com.homepage.controller;

import com.homepage.dto.CommentDTO;
import com.homepage.dto.Result;
import com.homepage.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/article/{articleId}")
    public Result<List<CommentDTO>> getCommentsByArticle(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }

    @PostMapping("/article/{articleId}")
    public Result<CommentDTO> createComment(@PathVariable Long articleId, @RequestBody CommentDTO dto) {
        return Result.success(commentService.createComment(articleId, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
