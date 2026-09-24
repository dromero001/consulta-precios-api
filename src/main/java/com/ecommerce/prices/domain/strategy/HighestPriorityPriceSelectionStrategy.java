package com.ecommerce.prices.domain.strategy;

import com.ecommerce.prices.domain.model.Price;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HighestPriorityPriceSelectionStrategy implements PriceSelectionStrategy {

    @Override
    public Optional<Price> select(List<Price> candidates) {
        return candidates.stream().max(Comparator.comparingInt(Price::priority));
    }
}
