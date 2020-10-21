package com.toyproject.studygroup.toyprojectstudygroup.studygroup.validator;

import com.toyproject.studygroup.toyprojectstudygroup.studygroup.StudyGroupRepository;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyGroupFormValidator implements Validator {

    private final StudyGroupRepository studyGroupRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return StudyGroupForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyGroupForm studyGroupForm = (StudyGroupForm) target;
        if(studyGroupRepository.existsByPath(studyGroupForm.getPath())){
            errors.rejectValue("path", "wrong.path", "스터디그룹 경로값을 사용할 수 없습니다.");
        }
    }
}
