package com.toyproject.studygroup.toyprojectstudygroup.settings;

import com.toyproject.studygroup.toyprojectstudygroup.WithAccount;
import com.toyproject.studygroup.toyprojectstudygroup.account.AccountRepository;
import com.toyproject.studygroup.toyprojectstudygroup.account.AccountService;
import com.toyproject.studygroup.toyprojectstudygroup.account.SignUpForm;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class SettingsControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;
    
    @DisplayName("프로필 수정 - 폼화면 이동")
    @Test
    @WithAccount("tester")
    void updateProfile_moveView() throws Exception{
        mockMvc.perform(get("/settings/profileForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profileForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
        ;
    }

    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    // @WithMockUser // 실제 DB에 저장된 정보로 인증된 Authentication이 필요하기 때문에 이건 맞지 않다
    // @WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION) // Spring Test가 JUnit5를 잘 지원하지 못해서 버그가 발생중
    @WithAccount("tester")
    void updateProfile_correct_input() throws Exception{
        String updateBio = "한 줄 소개를 수정함";
        mockMvc.perform(post("/settings/profileForm")
                .with(csrf())
                .param("bio", updateBio))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profileForm"))
                .andExpect(flash().attributeExists("message"))
        ;

        Account testUser = accountRepository.findByNickname("tester");
        assertEquals(updateBio, testUser.getBio());
    }

    @DisplayName("프로필 수정 - 입력값 불량")
    @Test
    @WithAccount("tester")
    void updateProfile_wrong_input() throws Exception{
        String updateBio = "한 줄 소개를 수정함 한 줄 소개를 수정함 한 줄 소개를 수정함 한 줄 소개를 수정함 한 줄 소개를 수정함";
        log.info("입력값 불량인 경우 bio 길이 - "+ updateBio.length());

        mockMvc.perform(post("/settings/profileForm")
                .with(csrf())
                .param("bio", updateBio))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profileForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
        ;

        Account testUser = accountRepository.findByNickname("tester");
        assertNull(testUser.getBio());
    }

    @AfterEach
    void afterTestMethods(){
        accountRepository.deleteAll();
    }
}
