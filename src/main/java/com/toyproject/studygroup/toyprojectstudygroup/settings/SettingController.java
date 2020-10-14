package com.toyproject.studygroup.toyprojectstudygroup.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyproject.studygroup.toyprojectstudygroup.account.AccountRepository;
import com.toyproject.studygroup.toyprojectstudygroup.account.AccountService;
import com.toyproject.studygroup.toyprojectstudygroup.account.CurrentUser;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.*;
import com.toyproject.studygroup.toyprojectstudygroup.settings.validator.NicknameValidator;
import com.toyproject.studygroup.toyprojectstudygroup.settings.validator.PasswordFormValidator;
import com.toyproject.studygroup.toyprojectstudygroup.tag.TagRepository;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new NicknameValidator(accountRepository));
    }

    @GetMapping("/profile")
    public String updateProfileForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute("profile", modelMapper.map(account, ProfileForm.class));

        return "settings/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/profile";
        }

        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");

        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String updatePasswordForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return "settings/password";
    }

    @PostMapping("/password")
    public String updatePassword(@CurrentUser Account account, @Valid @ModelAttribute PasswordForm passwordForm, Errors errors,
                                Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "비밀번호를 수정했습니다.");

        return "redirect:/settings/password";
    }

    @GetMapping("/notifications")
    public String updateNotificationForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationForm.class));

        return "settings/notifications";
    }

    @PostMapping("/notifications")
    public String updateNotification(@CurrentUser Account account, @Valid @ModelAttribute NotificationForm notificationForm, Errors errors,
                                     Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/notifications";
        }

        accountService.updateNotification(account, notificationForm);
        attributes.addFlashAttribute("message", "알림 설정을 수정했습니다.");

        return "redirect:/settings/notifications";
    }

    @GetMapping("/account")
    public String updateNicknameForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));

        return "settings/account";
    }

    @PostMapping("/account")
    public String updateNickname(@CurrentUser Account account, @Valid @ModelAttribute NicknameForm nicknameForm, Errors errors,
                                 Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/account";
        }

        accountService.updateNickname(account, nicknameForm);
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");

        return "redirect:/settings/account";
    }

    @GetMapping("/tags")
    public String updateTagForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String tagTitle = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(tagTitle);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }

        accountService.addTag(account, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String tagTitle = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(tagTitle);
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();
    }
}
