package com.toyproject.studygroup.toyprojectstudygroup.zone;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @PostConstruct // 해당 Annotation이 속한 Bean이 초기화된 이후 실행되는 영역
    public void initZoneData() throws IOException {
        if (zoneRepository.count() == 0){
            Resource resource = new ClassPathResource("korea_zone_data.csv");
            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8)
                    .stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                    })
                    .collect(Collectors.toList())
            ;
            zoneRepository.saveAll(zoneList);
        }
    }

}
