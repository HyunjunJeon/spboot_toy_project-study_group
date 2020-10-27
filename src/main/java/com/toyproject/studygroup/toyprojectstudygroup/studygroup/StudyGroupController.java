package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.toyproject.studygroup.toyprojectstudygroup.account.CurrentUser;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupDescriptionForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.validator.StudyGroupFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupRepository studyGroupRepository;
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
    public String newStudyGroupSubmit(@CurrentUser Account account, @Valid StudyGroupForm studyGroupForm, Model model, Errors errors){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "studygroup/form";
        }

        StudyGroup newStudyGroup = studyGroupService.createNewStudyGroup(
                modelMapper.map(studyGroupForm, StudyGroup.class), account);


        return "redirect:/studygroup/" + URLEncoder.encode(newStudyGroup.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/studygroup/{path}")
    public String viewStudyGroup(@CurrentUser Account account, @PathVariable String path, Model model){
        model.addAttribute(account);
        model.addAttribute(studyGroupService.getStudyGroup(path));
        return "studygroup/view";
    }

    @GetMapping("/studygroup/{path}/members")
    public String viewStudyGroupMembers(@CurrentUser Account account, @PathVariable String path, Model model){
        model.addAttribute(account);
        model.addAttribute(studyGroupService.getStudyGroup(path));
        return "studygroup/members";
    }

    @GetMapping("/studygroup/{path}/settings/description")
    public String viewStudyGroupSettingDescription(@CurrentUser Account account, @PathVariable String path, Model model){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(studyGroup);
        model.addAttribute(modelMapper.map(studyGroup, StudyGroupDescriptionForm.class));

        return "studygroup/settings/description";
    }

    @PostMapping("/studygroup/{path}/settings/description")
    public String updateStudyGroupSettingDescription(@CurrentUser Account account, @PathVariable String path,
                                                     @Valid @ModelAttribute StudyGroupDescriptionForm studyGroupDescriptionForm, Model model, Errors errors,
                                                     RedirectAttributes attributes){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(studyGroup);
            return "studygroup/settings/description";
        }

        studyGroupService.updateStudyGroupDescription(studyGroup, studyGroupDescriptionForm);
        attributes.addFlashAttribute("message", "스터디그룹 소개를 수정했습니다.");

        return "redirect:/studygroup/"+URLEncoder.encode(path, StandardCharsets.UTF_8) +"/settings/description";
    }

    @GetMapping("/studygroup/{path}/settings/banner")
    public String viewStudyGroupSettingBanner(@CurrentUser Account account, @PathVariable String path, Model model){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(studyGroup);

        return "studygroup/settings/banner";
    }

    @PostMapping("/studygroup/{path}/settings/banner/enable")
    public String updateStudyGroupSettingBannerEnable(@CurrentUser Account account, @PathVariable String path){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        studyGroupService.enableStudyGroupBanner(studyGroup);

        return "redirect:/studygroup/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/settings/banner";
    }

    @PostMapping("/studygroup/{path}/settings/banner/disable")
    public String updateStudyGroupSettingBannerDisable(@CurrentUser Account account, @PathVariable String path){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        studyGroupService.disableStudyGroupBanner(studyGroup);

        return "redirect:/studygroup/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/settings/banner";
    }
}
