package com.toyproject.studygroup.toyprojectstudygroup.account;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
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
}
