package com.toyproject.studygroup.toyprojectstudygroup.settings;

import com.toyproject.studygroup.toyprojectstudygroup.account.AccountService;
import com.toyproject.studygroup.toyprojectstudygroup.account.CurrentUser;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final AccountService accountService;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @GetMapping("/profile")
    public String updateProfileForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute("profile", new ProfileForm(account));

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
}
