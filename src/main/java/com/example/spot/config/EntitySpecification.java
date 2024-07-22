//package com.example.spot.config;
//
//import com.example.spot.domain.Theme;
//import com.example.spot.domain.mapping.StudyTheme;
//import com.example.spot.domain.study.Study;
//import jakarta.persistence.criteria.CriteriaBuilder;
//import jakarta.persistence.criteria.Join;
//import jakarta.persistence.criteria.Predicate;
//import jakarta.persistence.criteria.Root;
//import jakarta.persistence.criteria.Subquery;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import org.springframework.data.jpa.domain.Specification;
//
//
//public class EntitySpecification {
//
//    public static Specification<Study> searchStudy(Map<String, Object> search) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (search.containsKey("gender")) {
//                Predicate genderPredicate = criteriaBuilder.equal(root.get("gender"), search.get("gender"));
//                predicates.add(genderPredicate);
//            }
//            if (search.containsKey("minAge")) {
//                Predicate minAgePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("minAge"), (Integer) search.get("minAge"));
//                predicates.add(minAgePredicate);
//            }
//            if (search.containsKey("maxAge")) {
//                Predicate maxAgePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("maxAge"), (Integer) search.get("maxAge"));
//                predicates.add(maxAgePredicate);
//            }
//            if (search.containsKey("isOnline")) {
//                Predicate isOnlinePredicate = criteriaBuilder.equal(root.get("isOnline"), search.get("isOnline"));
//                predicates.add(isOnlinePredicate);
//            }
//            if (search.containsKey("hasFee")) {
//                Predicate hasFeePredicate = criteriaBuilder.equal(root.get("hasFee"), search.get("hasFee"));
//                predicates.add(hasFeePredicate);
//            }
//            if (search.containsKey("fee")) {
//                Predicate feePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("fee"), (Integer) search.get("fee"));
//                predicates.add(feePredicate);
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//
//    public static Specification<Study> hasThemes(List<Theme> themes) {
//        return (root, query, criteriaBuilder) -> {
//            if (themes.isEmpty()) {
//                return criteriaBuilder.conjunction(); // No filter if the list is empty
//            }
//
//            // Join to the StudyTheme table
//            Join<Study, StudyTheme> studyThemeJoin = root.join("studyThemes");
//
//            // Join to the Theme table
//            Join<StudyTheme, Theme> themeJoin = studyThemeJoin.join("theme");
//
//            // Create an 'in' clause for themes
//            CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(themeJoin.get("id"));
//            for (Theme theme : themes) {
//                inClause.value(theme.getId());
//            }
//
//            return inClause;
//        };
//    }
//}
