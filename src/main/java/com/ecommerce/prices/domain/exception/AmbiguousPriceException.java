package com.ecommerce.prices.domain.exception;

import com.ecommerce.prices.domain.model.Price;
import java.util.List;

public class AmbiguousPriceException extends RuntimeException {

    public AmbiguousPriceException(List<Price> tiedPrices) {
        super(describe(tiedPrices));
    }

    private static String describe(List<Price> tiedPrices) {
        Price first = tiedPrices.get(0);
        return "Price lists %s of product %d of brand %d share the highest priority %d".formatted(
                priceLists(tiedPrices), first.productId().value(), first.brandId().value(), first.priority());
    }

    private static String priceLists(List<Price> tiedPrices) {
        List<String> ids = tiedPrices.stream().map(price -> String.valueOf(price.priceList())).toList();
        return String.join(", ", ids.subList(0, ids.size() - 1)) + " and " + ids.get(ids.size() - 1);
    }
}
