package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.ScheduleRepository;
import com.example.spot.validation.annotation.ExistSchedule;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistScheduleValidator implements ConstraintValidator<ExistSchedule, Long> {

    private final ScheduleRepository scheduleRepository;

    @Override
    public void initialize(ExistSchedule constraintAnnotation) {}

    @Override
    public boolean isValid(Long scheduleId, ConstraintValidatorContext context) {

        boolean isValid;
        ErrorStatus errorStatus;

        if (scheduleId == null) {
            isValid = false;
            errorStatus = ErrorStatus._STUDY_SCHEDULE_ID_NULL;
        } else {
            errorStatus = ErrorStatus._STUDY_SCHEDULE_NOT_FOUND;
            isValid = scheduleRepository.existsById(scheduleId);
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
