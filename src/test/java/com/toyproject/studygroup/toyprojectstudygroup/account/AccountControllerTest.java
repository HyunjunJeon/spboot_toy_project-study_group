package com.toyproject.studygroup.toyprojectstudygroup.account;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.mail.EmailMessage;
import com.toyproject.studygroup.toyprojectstudygroup.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @DisplayName("회원가입 화면 잘 보이는지")
    @Test
    void checkSignUpForm() throws Exception{
       mockMvc.perform(get("/sign-up"))
               .andExpect(status().isOk())
               .andExpect(view().name("account/sign-up"))
               .andExpect(model().attributeExists("signUpForm"))
               .andExpect(unauthenticated())
       ;
    }

    @DisplayName("회원 가입 처리 - 이메일 형식 틀림")
    @Test
    void checkSignUp_with_wrong_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "Hyunjun")
                        .param("email", "email...")
                        .param("password", "123456")
                        .with(csrf())) // 인증없이도 쓸 수 있도록 config를 구성하긴 했지만, CSRF 토큰이 없어서 안됌..
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated())
        ;
    }

    @DisplayName("회원 가입 처리 + 메일 보내기")
    @Test
    void checkSignUp_with_correct_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                .param("nickname", "hyunjun")
                .param("email", "jeonhj920@gmail.com")
                .param("password", "123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated())
        ;

        Account account = accountRepository.findByEmail("jeonhj920@gmail.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "123456789");
        assertNotNull(account.getEmailCheckToken());

        then(emailService).should().sendEmail(any(EmailMessage.class));

    }

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_wrong_input() throws Exception{
        mockMvc.perform(get("/check-email-token")
                        .param("token", "아무문자열")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
        ;
    }

    @DisplayName("인증 메일 확인 - 입력값 정상 ")
    @Test
    void checkEmailToken_correct_input() throws Exception{
        Account account = Account.builder()
                .email("test.test.com")
                .password("123456789")
                .nickname("tester")
                .build()
        ;

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
        ;
    }


}
