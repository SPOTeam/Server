package com.example.spot.validation.annotation;

import com.example.spot.validation.validator.LongSizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LongSizeValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface LongSize {

    String message() default "정수의 크기가 지정된 범위를 초과합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    long min() default 0;
    long max() default Long.MAX_VALUE;
}
