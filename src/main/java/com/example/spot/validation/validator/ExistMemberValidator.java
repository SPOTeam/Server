package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.MemberRepository;
import com.example.spot.validation.annotation.ExistMember;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExistMemberValidator implements ConstraintValidator<ExistMember, Long> {

    private final MemberRepository memberRepository;

    @Override
    public void initialize(ExistMember constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        boolean isValid;
        ErrorStatus errorStatus;
        // null is invalid
        if (userId == null) {
            errorStatus = ErrorStatus._MEMBER_ID_NULL;
            isValid = false;
        } else {
            errorStatus = ErrorStatus._MEMBER_NOT_FOUND;
            isValid = memberRepository.findById(userId).isPresent();
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.getMessage()).addConstraintViolation();
        }

        return isValid;
    }
}
