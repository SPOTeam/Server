package com.example.spot.config;

import com.example.spot.domain.Theme;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;

public class EntitySpecification {

    public static Specification<Study> searchStudy(Map<String, Object> searchKey){
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for(String key : searchKey.keySet()){
                predicates.add(criteriaBuilder.equal(root.get(key), searchKey.get(key)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
    public static Specification<Study> hasThemes(List<Theme> themes) {
        return (root, query, criteriaBuilder) -> {
            if (themes == null || themes.isEmpty()) {
                return criteriaBuilder.conjunction(); // Return true for empty theme list
            }

            Join<Study, StudyTheme> studyThemeJoin = root.join("themes");
            Join<StudyTheme, Theme> themeJoin = studyThemeJoin.join("theme");

            return themeJoin.in(themes);
        };
    }
}
