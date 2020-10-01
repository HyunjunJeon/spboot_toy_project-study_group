package com.toyproject.studygroup.toyprojectstudygroup.account;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
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

    private void sendSignUpConfirmEmail(Account savedAccount){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(savedAccount.getEmail());
        mailMessage.setSubject("스터디그룹, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token="+savedAccount.getEmailCheckToken()+"&email="+savedAccount.getEmail());

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
                account.getNickname(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }
}
