package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.StudyRepository;
import com.example.spot.validation.annotation.ExistStudy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ExistStudyValidator implements ConstraintValidator<ExistStudy, Long> {

    private final StudyRepository studyRepository;
    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        boolean isValid;
        ErrorStatus errorStatus;
        // null is invalid
        if (value == null) {
            errorStatus = ErrorStatus._STUDY_ID_NULL;
            isValid = false;
        } else {
            errorStatus = ErrorStatus._STUDY_NOT_FOUND;
            isValid = studyRepository.findById(value).isPresent();
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage()).addConstraintViolation();
        }

        return isValid;
    }
}
