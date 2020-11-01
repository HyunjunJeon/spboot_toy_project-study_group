package com.toyproject.studygroup.toyprojectstudygroup.tag;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Tag;
import com.toyproject.studygroup.toyprojectstudygroup.settings.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNewTag(TagForm tagForm){
        Tag byTitle = tagRepository.findByTitle(tagForm.getTagTitle());
        if(byTitle == null){
            byTitle = tagRepository.save(Tag.builder()
                    .title(tagForm.getTagTitle())
                    .build()
            );
        }
        return byTitle;
    }
}
