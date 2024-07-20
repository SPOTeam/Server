package com.example.spot.config;

import com.example.spot.RegionRepository;
import com.example.spot.domain.Region;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionDataLoader implements CommandLineRunner {

    private final RegionRepository regionRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Region> regions = readRegionsFromFile("data/region_data.tsv");
        regionRepository.saveAll(regions);
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