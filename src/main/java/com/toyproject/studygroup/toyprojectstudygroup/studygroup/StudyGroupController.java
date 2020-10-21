package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.toyproject.studygroup.toyprojectstudygroup.account.CurrentUser;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.validator.StudyGroupFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final ModelMapper modelMapper;
    private final StudyGroupFormValidator studyGroupFormValidator;

    @InitBinder("studyGroupForm")
    public void studyGroupFormValidator(WebDataBinder webDataBinder){
        webDataBinder.setValidator(studyGroupFormValidator);
    }

    @GetMapping("/new-studygroup")
    public String newStudyGroupForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new StudyGroupForm());
        return "studygroup/form";
    }

    @PostMapping("/new-studygroup")
    public String newStudyGroupSubmit(@CurrentUser Account account, @Valid StudyGroupForm studyGroupForm, Errors errors){
        if(errors.hasErrors()){
            return "studygroup/form";
        }

        StudyGroup newStudyGroup = studyGroupService.createNewStudyGroup(
                modelMapper.map(studyGroupForm, StudyGroup.class), account);


        return "redirect:/studygroup/" + URLEncoder.encode(newStudyGroup.getPath(), StandardCharsets.UTF_8);
    }


}
