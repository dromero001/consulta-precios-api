package com.ecommerce.prices.domain.strategy;

import static com.ecommerce.prices.domain.model.PriceFixtures.aPrice;
import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.prices.domain.model.Price;
import java.util.List;
import org.junit.jupiter.api.Test;

class HighestPriorityPriceSelectionStrategyTest {

    private final HighestPriorityPriceSelectionStrategy underTest = new HighestPriorityPriceSelectionStrategy();

    @Test
    void shouldSelectTheCandidateWithTheHighestPriorityWhateverItsPosition() {
        Price lowPriority = aPrice().priceList(1L).priority(0).build();
        Price highPriority = aPrice().priceList(2L).priority(1).build();
        Price lowestPriority = aPrice().priceList(3L).priority(-1).build();

        assertThat(underTest.select(List.of(lowPriority, highPriority, lowestPriority))).contains(highPriority);
    }

    @Test
    void shouldSelectNothingWhenThereAreNoCandidates() {
        assertThat(underTest.select(List.of())).isEmpty();
    }
}
