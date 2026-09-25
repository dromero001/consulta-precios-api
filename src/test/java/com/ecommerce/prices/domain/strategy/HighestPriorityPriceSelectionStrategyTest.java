package com.ecommerce.prices.domain.strategy;

import static com.ecommerce.prices.domain.model.PriceFixtures.aPrice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.prices.domain.exception.AmbiguousPriceException;
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

    @Test
    void shouldRejectCandidatesThatShareTheHighestPriority() {
        Price basePrice = aPrice().priceList(1L).priority(0).build();
        Price onePromotion = aPrice().priceList(2L).priority(1).build();
        Price anotherPromotion = aPrice().priceList(3L).priority(1).build();

        assertThatThrownBy(() -> underTest.select(List.of(basePrice, onePromotion, anotherPromotion)))
                .isInstanceOf(AmbiguousPriceException.class)
                .hasMessage("Price lists 2 and 3 of product 35455 of brand 1 share the highest priority 1");
    }

    @Test
    void shouldIgnoreATieBelowTheHighestPriority() {
        Price oneBasePrice = aPrice().priceList(1L).priority(0).build();
        Price anotherBasePrice = aPrice().priceList(2L).priority(0).build();
        Price promotion = aPrice().priceList(3L).priority(1).build();

        assertThat(underTest.select(List.of(oneBasePrice, anotherBasePrice, promotion))).contains(promotion);
    }
}
