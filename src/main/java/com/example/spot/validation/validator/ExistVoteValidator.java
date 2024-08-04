package com.example.spot.validation.validator;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.repository.VoteRepository;
import com.example.spot.validation.annotation.ExistVote;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistVoteValidator implements ConstraintValidator<ExistVote, Long> {

    private final VoteRepository voteRepository;

    @Override
    public void initialize(ExistVote constraintAnnotation) {}

    @Override
    public boolean isValid(Long voteId, ConstraintValidatorContext context) {

        boolean isValid = false;
        ErrorStatus errorStatus;

        if (voteId == null) {
            errorStatus = ErrorStatus._STUDY_VOTE_NULL;
        } else if (!voteRepository.existsById(voteId)) {
            errorStatus = ErrorStatus._STUDY_VOTE_NOT_FOUND;
        } else {
            errorStatus = ErrorStatus._STUDY_VOTE_NOT_FOUND; // ignore
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
