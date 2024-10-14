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

    // 지역 데이터를 저장하는 RegionRepository와 테마 정보를 저장하는 ThemeRepository를 주입받습니다.
    private final RegionRepository regionRepository;
    private final ThemeRepository themeRepository;


    /**
     * 기본 지역 및 테마 데이터를 객체로 생성하여 데이터베이스에 저장합니다.
     * @param args incoming main method arguments
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        // 지역 데이터가 없을 경우, data/region_data.tsv 파일에서 지역 데이터를 읽어와 저장합니다.
        if (isDataAbsent()) {
            List<Region> regions = readRegionsFromFile("data/region_data.tsv");
            regionRepository.saveAll(regions);
        }
        // 테마 데이터가 없을 경우, 테마 정보를 저장합니다.
        if (themeRepository.count() == 0) {

            // 기본 테마 정보
            List<String> themeNames = Arrays.asList(
                "어학", "자격증", "취업", "시사뉴스", "자율학습",
                "토론", "프로젝트", "공모전", "전공및진로학습", "기타"
            );
            // 테마 정보를 저장합니다.
            for (String name : themeNames) {
                // Theme 객체를 생성합니다.
                Theme theme = Theme.builder()
                    .studyTheme(ThemeType.valueOf(name))
                    .build();
                // 생성한 테마 정보를 데이터베이스에 저장
                themeRepository.save(theme);
            }
        }
    }

    /**
     * 지역 데이터가 존재하는지 확인합니다.
     * @return 지역 데이터가 없으면 true, 있으면 false를 반환합니다.
     */
    private boolean isDataAbsent() {
        return regionRepository.count() == 0;
    }

    /**
     * 파일에서 지역 데이터를 읽어와 Region 객체로 변환합니다.
     * @param filePath 지역 데이터 파일 경로
     * @return 지역 데이터를 저장한 Region 객체 리스트
     * @throws IOException
     */
    private List<Region> readRegionsFromFile(String filePath) throws IOException {
        // 지역 데이터를 저장할 리스트를 생성합니다.
        List<Region> regions = new ArrayList<>();

        // 파일에서 지역 데이터를 읽어와 Region 객체로 변환합니다.
        try (InputStreamReader reader = new InputStreamReader(new ClassPathResource(filePath).getInputStream())) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.TDF.withFirstRecordAsHeader().withTrim());

            // CSV 파일의 각 레코드를 읽어와 Region 객체로 변환합니다.
            for (CSVRecord csvRecord : csvParser) {
                Region region = Region.builder()
                        .code(csvRecord.get("code"))
                        .province(csvRecord.get("province"))
                        .district(csvRecord.get("district"))
                        .neighborhood(csvRecord.get("neighborhood"))
                        .build();

                // 생성한 Region 객체를 리스트에 추가합니다.
                regions.add(region);
            }
        }
        // 지역 데이터를 저장한 리스트를 반환합니다.
        return regions;
    }
}