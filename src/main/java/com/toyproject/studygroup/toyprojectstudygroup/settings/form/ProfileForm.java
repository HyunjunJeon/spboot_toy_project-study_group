package com.toyproject.studygroup.toyprojectstudygroup.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ProfileForm {
    @Length(max = 35)
    private String bio;

    @Length(max = 50)
    private String url;

    @Length(max = 50)
    private String occupation;

    @Length(max = 20)
    private String location;

    private String profileImage;

}
