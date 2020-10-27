package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final ModelMapper modelMapper;

    public StudyGroup getStudyGroup(String path){
        StudyGroup byPath = studyGroupRepository.findByPath(path);
        if(byPath == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디 그룹이 없습니다.");
        }
        return byPath;
    }

    public StudyGroup createNewStudyGroup(StudyGroup studyGroup, Account account) {
        StudyGroup newStudy = studyGroupRepository.save(studyGroup);
        newStudy.addManager(account);
        return newStudy;
    }

    public StudyGroup getStudyGroupToUpdate(Account account, String path) {
        StudyGroup byPath = getStudyGroup(path);
        if(!byPath.getManagers().contains(account)){
            throw new AccessDeniedException("해당 기능을 사용할 권한이 없습니다.");
        }
        return byPath;
    }

    public void updateStudyGroupDescription(StudyGroup studyGroup, StudyGroupDescriptionForm studyGroupDescriptionForm){
        modelMapper.map(studyGroupDescriptionForm, studyGroup);
    }


    public void enableStudyGroupBanner(StudyGroup studyGroup) {
        studyGroup.setUseBanner(true);
    }

    public void disableStudyGroupBanner(StudyGroup studyGroup) {
        studyGroup.setUseBanner(false);
    }
}
