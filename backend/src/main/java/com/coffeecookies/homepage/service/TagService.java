package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.TagDTO;
import com.coffeecookies.homepage.entity.Tag;
import com.coffeecookies.homepage.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TagDTO getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        return convertToDTO(tag);
    }

    @Transactional
    public TagDTO createTag(TagDTO dto) {
        Tag tag = new Tag();
        updateTagFromDTO(tag, dto);
        return convertToDTO(tagRepository.save(tag));
    }

    @Transactional
    public TagDTO updateTag(Long id, TagDTO dto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        updateTagFromDTO(tag, dto);
        return convertToDTO(tagRepository.save(tag));
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

    private void updateTagFromDTO(Tag tag, TagDTO dto) {
        tag.setName(dto.getName());
        tag.setColor(dto.getColor());
    }
}
