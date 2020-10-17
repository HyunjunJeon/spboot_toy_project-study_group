package com.toyproject.studygroup.toyprojectstudygroup.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyproject.studygroup.toyprojectstudygroup.WithAccount;
import com.toyproject.studygroup.toyprojectstudygroup.account.AccountRepository;
import com.toyproject.studygroup.toyprojectstudygroup.account.AccountService;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.TagForm;
import com.toyproject.studygroup.toyprojectstudygroup.tag.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
public class SettingsControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;
    
    @DisplayName("프로필 수정 - 폼화면 이동")
    @Test
    @WithAccount("tester")
    void updateProfile_moveView() throws Exception{
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
        ;
    }

    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    // @WithMockUser // 실제 DB에 저장된 정보로 인증된 Authentication이 필요하기 때문에 이건 맞지 않다
    // @WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION) // Spring Test가 JUnit5를 잘 지원하지 못해서 버그가 발생중
    @WithAccount("tester")
    void updateProfile_correct_input() throws Exception{
        String updateBio = "한 줄 소개를 수정함";
        mockMvc.perform(post("/settings/profile")
                .with(csrf())
                .param("bio", updateBio))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
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

        mockMvc.perform(post("/settings/profile")
                .with(csrf())
                .param("bio", updateBio))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
        ;

        Account testUser = accountRepository.findByNickname("tester");
        assertNull(testUser.getBio());
    }
    
    @DisplayName("패스워드 수정 - 폼화면 이동")
    @Test
    @WithAccount("tester")
    void updatePassword_moveView() throws Exception{
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
        ;
    }
    
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    @WithAccount("tester")
    void updatePassword_correct_input() throws Exception{
        mockMvc.perform(post("/settings/password")
                    .with(csrf())
                    .param("newPassword", "123456789")
                    .param("newPasswordConfirm", "123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"))
        ;
    }

    @DisplayName("패스워드 수정 - 입력값 불량")
    @Test
    @WithAccount("tester")
    void updatePassword_wrong_input() throws Exception{
        mockMvc.perform(post("/settings/password")
                .with(csrf())
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
        ;
    }

    @DisplayName("태그 수정 - 화면 이동")
    @Test
    @WithAccount("Tester")
    void updateTag_moveView() throws Exception{
        mockMvc.perform(get("/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"))
        ;
    }
    
    @DisplayName("태그 수정 - 태그 추가")
    @Test
    @WithAccount("Tester")
    void updateTag_add() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("add New Tag");

        mockMvc.perform(post("/settings/tags/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm)))
                .andExpect(status().isOk())
        ;

        Tag byTitle = tagRepository.findByTitle(tagForm.getTagTitle());
        assertNotNull(byTitle);
        assertTrue(accountRepository.findByNickname("Tester").getTags().contains(byTitle));
    }

    @DisplayName("태그 수정 - 태그 삭제")
    @Test
    @WithAccount("Tester")
    void updateTag_remove() throws Exception{
        // 계정에 태그 넣기
        Account tester = accountRepository.findByNickname("Tester");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(tester, newTag);

        assertTrue(tester.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle(newTag.getTitle());

        mockMvc.perform(post("/settings/tags/remove")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm)))
                .andExpect(status().isOk())
        ;

        assertFalse(tester.getTags().contains(newTag));

    }

    @AfterEach
    void afterTestMethods(){
        accountRepository.deleteAll();
    }
}
