package com.ecommerce.prices.domain.exception;

public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(long brandId) {
        super("Brand %d not found".formatted(brandId));
    }
}
