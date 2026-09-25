package com.ecommerce.prices.domain.exception;

import com.ecommerce.prices.domain.model.PriceQuery;
import java.time.format.DateTimeFormatter;

public class PriceNotFoundException extends NotFoundException {

    private static final DateTimeFormatter APPLICATION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public PriceNotFoundException(PriceQuery query) {
        super("No price applies to product %d of brand %d at %s".formatted(
                query.productId().value(), query.brandId().value(), APPLICATION_DATE_FORMAT.format(query.applicationDate())));
    }
}
