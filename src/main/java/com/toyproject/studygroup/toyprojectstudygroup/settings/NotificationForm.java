package com.toyproject.studygroup.toyprojectstudygroup.settings;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
public class NotificationForm {
    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;
    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

//    public NotificationForm(Account account){
//        this.studyCreatedByEmail = account.isStudyCreatedByEmail();
//        this.studyCreatedByWeb = account.isStudyCreatedByWeb();
//
//        this.studyEnrollmentResultByEmail = account.isStudyEnrollmentResultByEmail();
//        this.studyEnrollmentResultByWeb = account.isStudyEnrollmentResultByWeb();
//
//        this.studyUpdatedByEmail = account.isStudyUpdateByEmail();
//        this.studyUpdatedByWeb = account.isStudyUpdateByWeb();
//    }
}
