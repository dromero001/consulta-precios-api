package com.ecommerce.prices.webapp.controller;

import com.ecommerce.prices.domain.exception.AmbiguousPriceException;
import com.ecommerce.prices.domain.exception.BrandNotFoundException;
import com.ecommerce.prices.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BrandNotFoundException.class)
    public ProblemDetail handleBrandNotFound(BrandNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Brand not found", exception.getMessage());
    }

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Price not found", exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", describe(exception));
    }

    @ExceptionHandler(InvalidApplicationDateException.class)
    public ProblemDetail handleInvalidApplicationDate(InvalidApplicationDateException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
    }

    @ExceptionHandler(AmbiguousPriceException.class)
    public ProblemDetail handleAmbiguousPrice(AmbiguousPriceException exception) {
        LOGGER.error("Ambiguous price configuration: {}", exception.getMessage());
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Ambiguous price", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error");
    }

    private static String describe(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(violation -> parameterName(violation) + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private static String parameterName(ConstraintViolation<?> violation) {
        return StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                .reduce((first, second) -> second)
                .map(Path.Node::getName)
                .orElse("request");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
