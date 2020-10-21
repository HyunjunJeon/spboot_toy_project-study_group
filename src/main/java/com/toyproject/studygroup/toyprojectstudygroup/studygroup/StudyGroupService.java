package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;

    public StudyGroup createNewStudyGroup(StudyGroup studyGroup, Account account) {
        StudyGroup newStudy = studyGroupRepository.save(studyGroup);
        newStudy.addManager(account);
        return newStudy;
    }
}
