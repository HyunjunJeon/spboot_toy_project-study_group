package com.toyproject.studygroup.toyprojectstudygroup.studygroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyproject.studygroup.toyprojectstudygroup.account.CurrentUser;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.StudyGroup;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Zone;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.TagForm;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.ZoneForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupDescriptionForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.form.StudyGroupForm;
import com.toyproject.studygroup.toyprojectstudygroup.studygroup.validator.StudyGroupFormValidator;
import com.toyproject.studygroup.toyprojectstudygroup.tag.TagRepository;
import com.toyproject.studygroup.toyprojectstudygroup.tag.TagService;
import com.toyproject.studygroup.toyprojectstudygroup.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StudyGroupController {

    private static final String STUDYGROUP_PATH_SETTINGS = "/studygroup/{path}/settings";

    private final StudyGroupService studyGroupService;
    private final ModelMapper modelMapper;
    private final StudyGroupFormValidator studyGroupFormValidator;
    private final ObjectMapper objectMapper;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;


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

    @GetMapping(STUDYGROUP_PATH_SETTINGS+"/description")
    public String viewStudyGroupSettingDescription(@CurrentUser Account account, @PathVariable String path, Model model){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(studyGroup);
        model.addAttribute(modelMapper.map(studyGroup, StudyGroupDescriptionForm.class));

        return "studygroup/settings/description";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/description")
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

    @GetMapping(STUDYGROUP_PATH_SETTINGS+"/banner")
    public String viewStudyGroupSettingBanner(@CurrentUser Account account, @PathVariable String path, Model model){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(studyGroup);

        return "studygroup/settings/banner";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/banner")
    public String studyGroupBannerImageSubmit(@CurrentUser Account account, @PathVariable String path, String image, RedirectAttributes attributes){
        StudyGroup studyGroup = studyGroupService.getStudyGroup(path);
        studyGroupService.updateStudyGroupImage(studyGroup, image);

        attributes.addFlashAttribute("message", "스터디그룹 배너 이미지를 수정했습니다");

        return "redirect:/studygroup/"+path+"/settings/banner";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/banner/enable")
    public String updateStudyGroupSettingBannerEnable(@CurrentUser Account account, @PathVariable String path){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        studyGroupService.enableStudyGroupBanner(studyGroup);

        return "redirect:/studygroup/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/settings/banner";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/banner/disable")
    public String updateStudyGroupSettingBannerDisable(@CurrentUser Account account, @PathVariable String path){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        studyGroupService.disableStudyGroupBanner(studyGroup);

        return "redirect:/studygroup/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/settings/banner";
    }

    @GetMapping(STUDYGROUP_PATH_SETTINGS+"/tags")
    public String studyGroupTagForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdateTag(account, path);
        model.addAttribute(account);
        model.addAttribute(studyGroup);
        model.addAttribute("tags",
                studyGroup.getTags()
                .stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList())
        );
        model.addAttribute("whitelist",
                objectMapper.writeValueAsString(
                        tagRepository.findAll()
                        .stream()
                        .map(Tag::getTitle)
                        .collect(Collectors.toList())
                )
        );

        return "studygroup/settings/tags";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/tags/add")
    @ResponseBody
    public ResponseEntity studyGroupTagAdd(@CurrentUser Account account, @PathVariable String path,
                                           @RequestBody TagForm tagForm){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNewTag(tagForm);
        studyGroupService.addTag(studyGroup, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/tags/remove")
    @ResponseBody
    public ResponseEntity studyGroupTagRemove(@CurrentUser Account account, @PathVariable String path,
                                           @RequestBody TagForm tagForm){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }
        studyGroupService.removeTag(studyGroup, tag);

        return ResponseEntity.ok().build();
    }

    @GetMapping(STUDYGROUP_PATH_SETTINGS+"/zones")
    public String studyGroupZoneForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdateZone(account, path);
        model.addAttribute(account);
        model.addAttribute(studyGroup);
        model.addAttribute("zones",
                studyGroup.getZones()
                        .stream()
                        .map(Zone::toString)
                        .collect(Collectors.toList())
        );
        model.addAttribute("whitelist",
                objectMapper.writeValueAsString(
                        zoneRepository.findAll()
                                .stream()
                                .map(Zone::toString)
                                .collect(Collectors.toList())
                )
        );

        return "studygroup/settings/zones";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/zones/add")
    @ResponseBody
    public ResponseEntity studyGroupZoneAdd(@CurrentUser Account account, @PathVariable String path,
                                           @RequestBody ZoneForm zoneForm){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvincename());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        studyGroupService.addZone(studyGroup, zone);

        return ResponseEntity.ok().build();
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/zones/remove")
    @ResponseBody
    public ResponseEntity studyGroupZoneRemove(@CurrentUser Account account, @PathVariable String path,
                                            @RequestBody ZoneForm zoneForm){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvincename());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        studyGroupService.removeZone(studyGroup, zone);

        return ResponseEntity.ok().build();
    }

    @GetMapping(STUDYGROUP_PATH_SETTINGS+"/studyinfo")
    public String viewStudyGroupStudyInfo(@CurrentUser Account account, @PathVariable String path, Model model){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(studyGroup);

        return "studygroup/settings/studyinfo";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/studyinfo/publish")
    public String publishStudyGroup(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        studyGroupService.publishStudyGroup(studyGroup);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");

        return "redirect:/studygroup/"+studyGroup.getEncodedPath()+"/studyinfo";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/studyinfo/close")
    public String closeStudyGroup(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        studyGroupService.closeStudyGroup(studyGroup);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");

        return "redirect:/studygroup/"+studyGroup.getEncodedPath()+"/studyinfo";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/recruit/start")
    public String startRecruitStudyGroup(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        if(!studyGroup.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "1시간 내에 여러번 인원모집을 할 수 없습니다.");
            return "redirect:/studygroup/"+studyGroup.getEncodedPath()+"/settings/studyinfo";
        }
        studyGroupService.startRecruit(studyGroup);
        attributes.addFlashAttribute("message", "");

        return "redirect:/studygroup/"+studyGroup.getEncodedPath()+"/settings/studyinfo";
    }

    @PostMapping(STUDYGROUP_PATH_SETTINGS+"/recruit/stop")
    public String stopRecruitStudyGroup(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes){
        StudyGroup studyGroup = studyGroupService.getStudyGroupToUpdate(account, path);
        if(!studyGroup.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "1시간 내에 여러번 인원모집을 할 수 없습니다.");
            return "redirect:/studygroup/"+studyGroup.getEncodedPath()+"/settings/studyinfo";
        }
        studyGroupService.stopRecruit(studyGroup);

        return "redirect:/studygroup/"+studyGroup.getEncodedPath()+"/settings/studyinfo";
    }
}
