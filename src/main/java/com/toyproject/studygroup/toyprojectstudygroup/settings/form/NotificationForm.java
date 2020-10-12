package com.toyproject.studygroup.toyprojectstudygroup.settings.form;

import lombok.Data;

@Data
public class NotificationForm {
    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;
    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

}
