package com.example.spot.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {
    @PersistenceContext
    private EntityManager entityManager;


    /**
     *  JPAQueryFactory를 Bean으로 등록하여 Querydsl을 사용할 수 있도록 합니다.
     * @return JPAQueryFactory
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}