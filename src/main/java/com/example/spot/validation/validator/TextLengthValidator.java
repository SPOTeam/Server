package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.validation.annotation.TextLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class TextLengthValidator implements ConstraintValidator<TextLength, String> {

    private int min;
    private int max;

    @Override
    public void initialize(TextLength constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String text, ConstraintValidatorContext context) {

        boolean isValid = false;
        ErrorStatus errorStatus;

        if (text == null || text.isEmpty()) {
            errorStatus = ErrorStatus._NULL_VALUE;
        } else if (text.length() < min || text.length() > max) {
            errorStatus = ErrorStatus._VALUE_RANGE_EXCEEDED;
        } else {
            errorStatus = ErrorStatus._VALUE_RANGE_EXCEEDED;
            isValid = true;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage() + " 주어진 텍스트의 길이는 " + min + " 이상 " + max + " 이하여야 합니다.")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
