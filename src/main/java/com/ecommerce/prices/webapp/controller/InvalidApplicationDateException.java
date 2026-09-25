package com.ecommerce.prices.webapp.controller;

public class InvalidApplicationDateException extends RuntimeException {

    public InvalidApplicationDateException(String applicationDate) {
        super("applicationDate: '%s' is not a valid date".formatted(applicationDate));
    }
}
