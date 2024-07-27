package com.example.spot.config;

import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.repository.RegionRepository;
import com.example.spot.domain.Region;
import com.example.spot.repository.ThemeRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionDataLoader implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final ThemeRepository themeRepository;



    @Override
    public void run(String... args) throws Exception {
        if (isDataAbsent()) {
            List<Region> regions = readRegionsFromFile("data/region_data.tsv");
            regionRepository.saveAll(regions);
        }
        if (themeRepository.count() == 0) {
            List<String> themeNames = Arrays.asList(
                "어학", "자격증", "취업", "시사뉴스", "자율학습",
                "토론", "프로젝트", "공모전", "전공및진로학습", "기타"
            );

            for (String name : themeNames) {
                Theme theme = Theme.builder()
                    .studyTheme(ThemeType.valueOf(name))
                    .build();
                themeRepository.save(theme);
            }
        }
    }

    private boolean isDataAbsent() {
        return regionRepository.count() == 0;
    }

    private List<Region> readRegionsFromFile(String filePath) throws IOException {
        List<Region> regions = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(new ClassPathResource(filePath).getInputStream())) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.TDF.withFirstRecordAsHeader().withTrim());

            for (CSVRecord csvRecord : csvParser) {
                Region region = Region.builder()
                        .code(csvRecord.get("code"))
                        .province(csvRecord.get("province"))
                        .district(csvRecord.get("district"))
                        .neighborhood(csvRecord.get("neighborhood"))
                        .build();

                regions.add(region);
            }
        }
        return regions;
    }
}