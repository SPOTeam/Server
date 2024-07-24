package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.validation.annotation.ExistTheme;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistThemeValidator implements ConstraintValidator<ExistTheme, String> {

    private final ThemeRepository themeRepository;
    @Override
    public void initialize(ExistTheme constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid;
        ErrorStatus errorStatus;

        if (value == null) {
            errorStatus = ErrorStatus._STUDY_THEME_IS_NULL;
            isValid = false;
        } else {
            try {
                ThemeType themeType = ThemeType.valueOf(value.toUpperCase());
                isValid = themeRepository.existsByStudyTheme(themeType);
                errorStatus = isValid ? null : ErrorStatus._STUDY_THEME_NOT_FOUND;
            } catch (IllegalArgumentException e) {
                isValid = false;
                errorStatus = ErrorStatus._STUDY_THEME_NOT_FOUND;
            }
        }

        if (!isValid) {
            Objects.requireNonNull(context).disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.toString())
                .addConstraintViolation();
        }

        return isValid;
    }


}
