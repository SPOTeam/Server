package com.example.spot.config;

import com.example.spot.domain.study.Study;
import jakarta.persistence.criteria.Predicate;
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
}
