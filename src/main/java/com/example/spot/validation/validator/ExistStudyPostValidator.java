package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.StudyPostRepository;
import com.example.spot.validation.annotation.ExistStudyPost;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistStudyPostValidator implements ConstraintValidator<ExistStudyPost, Long> {

    private final StudyPostRepository studyPostRepository;

    @Override
    public void initialize(ExistStudyPost constraintAnnotation) {}

    @Override
    public boolean isValid(Long studyPostId, ConstraintValidatorContext context) {

        boolean isValid = false;
        ErrorStatus errorStatus;

        if (studyPostId == null) {
            errorStatus = ErrorStatus._STUDY_POST_NULL;
        } else if (!studyPostRepository.existsById(studyPostId)) {
            errorStatus = ErrorStatus._STUDY_POST_NOT_FOUND;
        } else {
            errorStatus = ErrorStatus._STUDY_POST_NOT_FOUND; // ignore
            isValid = true;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
