package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.ToDoListRepository;
import com.example.spot.validation.annotation.ExistToDoList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistToDoListValidator implements ConstraintValidator<ExistToDoList, Long>{

    private final ToDoListRepository toDoListRepository;
    @Override
    public void initialize(ExistToDoList constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        boolean isValid = false;
        ErrorStatus errorStatus;

        if (value == null) {
            errorStatus = ErrorStatus._STUDY_TODO_NULL;
        } else if (!toDoListRepository.existsById(value)) {
            errorStatus = ErrorStatus._STUDY_TODO_NOT_FOUND;
        } else {
            errorStatus = ErrorStatus._STUDY_TODO_NOT_FOUND; // ignore
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
