package com.homepage.service;

import com.homepage.dto.TagDTO;
import com.homepage.entity.Tag;
import com.homepage.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagDTO> getAllTags() {
        List<Object[]> results = tagRepository.findAllWithArticleCount();
        return results.stream()
                .map(result -> TagDTO.builder()
                        .id((Long) result[0])
                        .name((String) result[0])
                        .articleCount((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }

    public List<TagDTO> getAllTagsSimple() {
        return tagRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TagDTO getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        return convertToDTO(tag);
    }

    @Transactional
    public TagDTO createTag(TagDTO dto) {
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setColor(dto.getColor());
        Tag saved = tagRepository.save(tag);
        return convertToDTO(saved);
    }

    @Transactional
    public TagDTO updateTag(Long id, TagDTO dto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(dto.getName());
        tag.setColor(dto.getColor());
        Tag saved = tagRepository.save(tag);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    private TagDTO convertToDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
