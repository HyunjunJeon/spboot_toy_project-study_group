package com.toyproject.studygroup.toyprojectstudygroup.domain;

import com.toyproject.studygroup.toyprojectstudygroup.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@NamedEntityGraphs({
        @NamedEntityGraph(name = "StudyGroup.withAll", attributeNodes = {
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("zones"),
                @NamedAttributeNode("managers"),
                @NamedAttributeNode("members")
        }),
        @NamedEntityGraph(name = "StudyGroup.withTags", attributeNodes = {
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("managers")
        }),
        @NamedEntityGraph(name = "StudyGroup.withZones", attributeNodes = {
                @NamedAttributeNode("zones"),
                @NamedAttributeNode("managers")
        })
})
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class StudyGroup {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public String getEncodedPath(){
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){
        return this.managers.contains(userAccount.getAccount());
    }

    public boolean isJoinable(UserAccount userAccount){
        return this.isPublished() && this.isRecruiting()
                && !this.isMember(userAccount) && !this.isManager(userAccount);
    }

    public void publish() {
         if(!this.closed && !this.published){
             this.published = true;
             this.publishedDateTime = LocalDateTime.now();
         } else {
             throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다");
         }
    }

    public void close() {
        if(!this.closed && this.published){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else{
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다");
        }
    }

    public boolean canUpdateRecruiting(){
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void startRecruit(){
        if(canUpdateRecruiting()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 공개한 뒤 1시간이 지나고 다시 시도하세요");
        }
    }

    public boolean isRemovable(){
        return !this.published;
    }

    public void stopRecruit() {
        if(canUpdateRecruiting()){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 공개한 뒤 1시간이 지나고 다시 시도하세요");
        }
    }
}
