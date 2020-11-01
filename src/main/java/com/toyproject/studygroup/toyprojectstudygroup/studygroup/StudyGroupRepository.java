package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    boolean existsByPath(String path);

    // 5번 쿼리나가던것 -> 1방에 무거운 쿼리로 해결. 전체 Join 걸어서 가져올 수 있게끔
    @EntityGraph(value = "StudyGroup.withAll", type = EntityGraph.EntityGraphType.LOAD)
    StudyGroup findByPath(String path);

    @EntityGraph(value = "StudyGroup.withTags", type = EntityGraph.EntityGraphType.FETCH)
    StudyGroup findStudyGroupWithTagsByPath(String path);

    @EntityGraph(value = "StudyGroup.withZones", type = EntityGraph.EntityGraphType.FETCH)
    StudyGroup findStudyGroupWithZonesByPath(String path);
}
