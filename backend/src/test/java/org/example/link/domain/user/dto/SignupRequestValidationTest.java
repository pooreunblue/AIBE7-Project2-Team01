package org.example.link.domain.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignupRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsPasswordShorterThanEightCharacters() {
        SignupRequest request = new SignupRequest("user@test.com", "1234567", "tester");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    void acceptsPasswordWithAtLeastEightCharacters() {
        SignupRequest request = new SignupRequest("user@test.com", "12345678", "tester");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
