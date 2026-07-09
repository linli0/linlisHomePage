package com.homepage.repository;

import com.homepage.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findAllByOrderByCategoryAscSortOrderAsc();

    List<Skill> findByCategoryOrderBySortOrderAsc(String category);
}
