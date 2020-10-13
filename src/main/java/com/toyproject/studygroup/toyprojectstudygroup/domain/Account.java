package com.toyproject.studygroup.toyprojectstudygroup.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified; // 이메일 인증 받았는지

    private String emailCheckToken; // 이메일 인증 토큰 저장해서 빠른 비교

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio; // 자기소개

    private String url; // 개인 웹사이트

    private String occupation; // 직업

    private String location; // 살고있는 지역

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;
    
    /* 알람 관련 */
    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdateByEmail;

    private boolean studyUpdateByWeb;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        if(this.emailCheckTokenGeneratedAt == null){
            this.emailCheckTokenGeneratedAt = LocalDateTime.now();
            return true;
        }
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
