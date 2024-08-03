package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.StudyLikedCommentRepository;
import com.example.spot.validation.annotation.ExistStudyLikedComment;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistStudyLikedCommentValidator implements ConstraintValidator<ExistStudyLikedComment, Long> {

    private final StudyLikedCommentRepository studyLikedCommentRepository;

    @Override
    public void initialize(ExistStudyLikedComment constraintAnnotation) {}

    @Override
    public boolean isValid(Long likedCommentId, ConstraintValidatorContext context) {

        boolean isValid = false;
        ErrorStatus errorStatus;

        if (likedCommentId == null) {
            errorStatus = ErrorStatus._STUDY_POST_COMMENT_REACTIOM_ID_NULL;
        } else if (!studyLikedCommentRepository.existsById(likedCommentId)) {
            errorStatus = ErrorStatus._STUDY_POST_COMMENT_REACTION_NOT_FOUND;
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
