package com.ecommerce.prices.domain.strategy;

import com.ecommerce.prices.domain.exception.AmbiguousPriceException;
import com.ecommerce.prices.domain.model.Price;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HighestPriorityPriceSelectionStrategy implements PriceSelectionStrategy {

    @Override
    public Optional<Price> select(List<Price> candidates) {
        List<Price> highestPriorityPrices = highestPriorityPrices(candidates);
        if (highestPriorityPrices.size() > 1) {
            throw new AmbiguousPriceException(highestPriorityPrices);
        }
        return highestPriorityPrices.stream().findFirst();
    }

    private static List<Price> highestPriorityPrices(List<Price> candidates) {
        int highestPriority = candidates.stream().mapToInt(Price::priority).max().orElse(Integer.MIN_VALUE);
        return candidates.stream().filter(price -> price.priority() == highestPriority).toList();
    }
}
