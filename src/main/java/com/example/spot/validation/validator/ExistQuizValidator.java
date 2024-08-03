package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.QuizRepository;
import com.example.spot.validation.annotation.ExistQuiz;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistQuizValidator implements ConstraintValidator<ExistQuiz, Long> {

    private final QuizRepository quizRepository;

    @Override
    public void initialize(ExistQuiz constraintAnnotation) {}

    @Override
    public boolean isValid(Long quizId, ConstraintValidatorContext context) {

        boolean isValid = false;
        ErrorStatus errorStatus;

        if (quizId == null) {
            errorStatus = ErrorStatus._STUDY_QUIZ_ID_NULL;
        } else if (!quizRepository.existsById(quizId)) {
            errorStatus = ErrorStatus._STUDY_QUIZ_NOT_FOUND;
        } else {
            errorStatus = ErrorStatus._STUDY_QUIZ_NOT_FOUND; // ignore
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
