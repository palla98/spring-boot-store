package com.codewithmosh.store.users;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) //dico che deve essere applicata ai metodi
@Retention(RetentionPolicy.RUNTIME) // per dire che l annotazione deve essere lanciata a runtime
@Constraint(validatedBy = LowerCaseValidator.class) //diciamo che si riferisce alla validator lowercase class
public @interface LowerCase {
    String message() default "must be lower case";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
