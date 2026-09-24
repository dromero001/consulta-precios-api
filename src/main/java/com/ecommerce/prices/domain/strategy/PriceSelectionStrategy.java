package com.ecommerce.prices.domain.strategy;

import com.ecommerce.prices.domain.model.Price;
import java.util.List;
import java.util.Optional;

public interface PriceSelectionStrategy {

    Optional<Price> select(List<Price> candidates);
}
