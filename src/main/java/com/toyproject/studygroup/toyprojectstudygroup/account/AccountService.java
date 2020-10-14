package com.toyproject.studygroup.toyprojectstudygroup.account;

import com.toyproject.studygroup.toyprojectstudygroup.account.form.SignUpForm;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.NicknameForm;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.NotificationForm;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm){
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) // TODO Password Encoding 해야함
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdateByWeb(true)
                .build()
        ;

        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디그룹, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token="+newAccount.getEmailCheckToken()+
                "&email="+newAccount.getEmail());
        javaMailSender.send(mailMessage);
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
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디그룹, 회원 가입 인증");
        mailMessage.setText("/login-by-email?token="+account.getEmailCheckToken()+
                "&email="+account.getEmail());
        javaMailSender.send(mailMessage);
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
}
