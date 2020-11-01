package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Zone;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.ZoneForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupDescriptionForm;
import com.toyproject.studygroup.toyprojectstudygroup.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final ModelMapper modelMapper;
    private final ZoneRepository zoneRepository;

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

    public StudyGroup getStudyGroupToUpdateTag(Account account, String path){
        StudyGroup studyGroup = studyGroupRepository.findStudyGroupWithTagsByPath(path);
        if(studyGroup == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디 그룹이 없습니다.");
        }
        if(!studyGroup.getManagers().contains(account)){
            throw new AccessDeniedException("해당 기능을 사용할 권한이 없습니다.");
        }
        return studyGroup;
    }

    public StudyGroup getStudyGroupToUpdateZone(Account account, String path){
        StudyGroup studyGroup = studyGroupRepository.findStudyGroupWithZonesByPath(path);
        if(studyGroup == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디 그룹이 없습니다.");
        }
        if(!studyGroup.getManagers().contains(account)){
            throw new AccessDeniedException("해당 기능을 사용할 권한이 없습니다.");
        }
        return studyGroup;
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

    public void updateStudyGroupImage(StudyGroup studyGroup, String image) {
        studyGroup.setImage(image);
    }

    public void addTag(StudyGroup studyGroup, Tag tag) {
        studyGroup.getTags().add(tag);
    }

    public void removeTag(StudyGroup studyGroup, Tag tag) {
        studyGroup.getTags().remove(tag);
    }

    public void addZone(StudyGroup studyGroup, Zone zone) {
        studyGroup.getZones().add(zone);
    }

    public void removeZone(StudyGroup studyGroup, Zone zone) {
        studyGroup.getZones().remove(zone);
    }

    public void publishStudyGroup(StudyGroup studyGroup) {
        studyGroup.publish();
    }

    public void closeStudyGroup(StudyGroup studyGroup) {
        studyGroup.close();
    }

    public void startRecruit(StudyGroup studyGroup) {
        studyGroup.startRecruit();
    }

    public void stopRecruit(StudyGroup studyGroup) {
        studyGroup.stopRecruit();
    }
}
