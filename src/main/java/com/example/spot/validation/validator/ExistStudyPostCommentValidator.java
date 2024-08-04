package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.StudyPostCommentRepository;
import com.example.spot.validation.annotation.ExistStudyPostComment;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistStudyPostCommentValidator implements ConstraintValidator<ExistStudyPostComment, Long> {

    private final StudyPostCommentRepository studyPostCommentRepository;

    @Override
    public void initialize(ExistStudyPostComment constraintAnnotation) {}

    @Override
    public boolean isValid(Long commentId, ConstraintValidatorContext context) {
        
        boolean isValid = false;
        ErrorStatus errorStatus;

        if (commentId == null) {
            errorStatus = ErrorStatus._STUDY_POST_COMMENT_NULL;
        } else if (!studyPostCommentRepository.existsById(commentId)) {
            errorStatus = ErrorStatus._STUDY_POST_COMMENT_NOT_FOUND;
        } else {
            errorStatus = ErrorStatus._STUDY_POST_COMMENT_NOT_FOUND; // ignore
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
