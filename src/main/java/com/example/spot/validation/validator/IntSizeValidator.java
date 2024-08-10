package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.validation.annotation.IntSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class IntSizeValidator implements ConstraintValidator<IntSize, Integer> {

    private long min;
    private long max;

    @Override
    public void initialize(IntSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        boolean isValid = false;
        ErrorStatus errorStatus;

        if (value == null) {
            errorStatus = ErrorStatus._NULL_VALUE;
        } else if (value < min || value > max) {
            errorStatus = ErrorStatus._VALUE_RANGE_EXCEEDED;
        } else {
            errorStatus = ErrorStatus._VALUE_RANGE_EXCEEDED;
            isValid = true;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage() + " 주어진 정수의 길이는 " + min + " 이상 " + max + " 이하여야 합니다.")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
