package com.ebanca.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {
    private BigDecimal min;
    private BigDecimal max;

    @Override
    public void initialize(ValidAmount constraintAnnotation) {
        this.min = new BigDecimal(constraintAnnotation.min());
        this.max = new BigDecimal(constraintAnnotation.max());
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
} 