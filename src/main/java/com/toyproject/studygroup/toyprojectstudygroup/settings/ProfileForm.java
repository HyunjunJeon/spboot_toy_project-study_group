package com.toyproject.studygroup.toyprojectstudygroup.settings;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
//@NoArgsConstructor
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

//    public ProfileForm(Account account){
//        this.bio = account.getBio();
//        this.url = account.getUrl();
//        this.occupation = account.getOccupation();
//        this.location = account.getLocation();
//        this.profileImage = account.getProfileImage();
//    }


}
