package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.MemberRepository;
import com.example.spot.validation.annotation.ExistMember;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistMemberValidator implements ConstraintValidator<ExistMember, Long> {

    private final MemberRepository memberRepository;

    @Override
    public void initialize(ExistMember constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
    @Override
    public boolean isValid(Long memberId, ConstraintValidatorContext context) {
        boolean isValid;
        ErrorStatus errorStatus;

        // null is invalid
        if (memberId == null) {
            errorStatus = ErrorStatus._MEMBER_NOT_FOUND;
            isValid = false;
        } else {
            errorStatus = ErrorStatus._MEMBER_NOT_FOUND;
            isValid = memberRepository.findById(memberId).isPresent();
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorStatus.toString()).addConstraintViolation();
        }

        return isValid;
    }
}
