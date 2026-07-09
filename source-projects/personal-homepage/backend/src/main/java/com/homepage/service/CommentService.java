package com.homepage.service;

import com.homepage.dto.CommentDTO;
import com.homepage.entity.Article;
import com.homepage.entity.Comment;
import com.homepage.repository.ArticleRepository;
import com.homepage.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    public List<CommentDTO> getCommentsByArticleId(Long articleId) {
        List<Comment> comments = commentRepository.findByArticleIdAndIsApprovedTrueOrderByCreatedAtDesc(articleId);
        return buildCommentTree(comments);
    }

    @Transactional
    public CommentDTO createComment(Long articleId, CommentDTO dto) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor(dto.getAuthor());
        comment.setEmail(dto.getEmail());
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setIsApproved(true);

        Comment saved = commentRepository.save(comment);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    private List<CommentDTO> buildCommentTree(List<Comment> comments) {
        Map<Long, CommentDTO> commentMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, this::convertToDTO));

        return comments.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> {
                    CommentDTO dto = commentMap.get(c.getId());
                    List<CommentDTO> replies = comments.stream()
                            .filter(reply -> c.getId().equals(reply.getParentId()))
                            .map(reply -> commentMap.get(reply.getId()))
                            .collect(Collectors.toList());
                    dto.setReplies(replies);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private CommentDTO convertToDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .author(comment.getAuthor())
                .email(comment.getEmail())
                .content(comment.getContent())
                .articleId(comment.getArticle().getId())
                .articleTitle(comment.getArticle().getTitle())
                .parentId(comment.getParentId())
                .isApproved(comment.getIsApproved())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
