package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.repository.RegionRepository;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.validation.annotation.ExistRegion;
import com.example.spot.validation.annotation.ExistTheme;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistRegionValidator implements ConstraintValidator<ExistRegion, String> {
    private final RegionRepository regionRepository;

    @Override
    public void initialize(ExistRegion constraintAnnotation) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid;
        ErrorStatus errorStatus;

        // null or empty values are considered invalid
        if (value == null || value.isEmpty()) {
            errorStatus = ErrorStatus._STUDY_REGION_IS_NULL;
            isValid = false;
        } else {
            errorStatus = ErrorStatus._STUDY_REGION_NOT_FOUND;
            isValid = regionRepository.existsByCode(value);
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage())
                .addConstraintViolation();
        }

        return isValid;
    }
}


