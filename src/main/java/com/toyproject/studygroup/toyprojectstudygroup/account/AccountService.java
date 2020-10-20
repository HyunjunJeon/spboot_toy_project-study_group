package com.toyproject.studygroup.toyprojectstudygroup.account;

import com.toyproject.studygroup.toyprojectstudygroup.account.form.SignUpForm;
import com.toyproject.studygroup.toyprojectstudygroup.config.AppProperties;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Zone;
import com.toyproject.studygroup.toyprojectstudygroup.mail.EmailMessage;
import com.toyproject.studygroup.toyprojectstudygroup.mail.EmailService;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.NicknameForm;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.NotificationForm;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.ProfileForm;
import com.toyproject.studygroup.toyprojectstudygroup.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm){
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        variables.put("nickname", newAccount.getNickname());
        variables.put("linkName", "이메일 인증하기");
        variables.put("message", "스터디그룹 서비스를 사용하려면 링크를 클릭하세요.");
        variables.put("host", appProperties.getHost());

        Context context = new Context();
        context.setVariables(variables);

        String htmlMessage = templateEngine.process("mail/email-auth", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디그룹, 회원가입 인증")
                .message(htmlMessage)
                .build()
        ;
        emailService.sendEmail(emailMessage);
    }

    //TODO Spring Security 로그인쪽 공부를 더 해야함
    public void login(Account account) {
//  정석 -> 평문 비밀번호를 통해 인증을 얻어와야하는데 현재 구조상 그런게 불가능함
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                username,
//                password
//        );
//        Authentication authentication = authenticationManager.authenticate(token);
//        SecurityContext context = SecurityContextHolder.getContext();
//        context.setAuthentication(authentication);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account == null){
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if(account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotification(Account account, NotificationForm notificationForm) {
        modelMapper.map(notificationForm, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, NicknameForm nicknameForm) {
        modelMapper.map(nicknameForm, account);
        accountRepository.save(account);
        login(account); // 업데이트된 Nickname으로 로그인이 다시 되어야 View에서 Auth 정보를 참조할 수 있기 때문
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();

        Map<String, Object> variables = new HashMap<>();
        variables.put("link", "/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        variables.put("nickname", account.getNickname());
        variables.put("linkName", "이메일로 로그인하기");
        variables.put("message", "스터디그룹 서비스에 로그인하려면 아래 링크를 클릭하세요.");
        variables.put("host", appProperties.getHost());

        Context context = new Context();
        context.setVariables(variables);

        String htmlMessage = templateEngine.process("mail/email-auth", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디그룹, 이메일 로그인 링크")
                .message(htmlMessage)
                .build()
                ;
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}
