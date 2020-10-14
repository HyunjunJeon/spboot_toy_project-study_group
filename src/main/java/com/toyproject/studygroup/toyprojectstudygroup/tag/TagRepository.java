package com.toyproject.studygroup.toyprojectstudygroup.tag;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTitle(String title);
}
