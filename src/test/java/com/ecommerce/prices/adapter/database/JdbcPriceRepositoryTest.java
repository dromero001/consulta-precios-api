package com.ecommerce.prices.adapter.database;

import static com.ecommerce.prices.domain.model.PriceFixtures.ANOTHER_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.ANOTHER_PRODUCT_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@Import(JdbcPriceRepository.class)
class JdbcPriceRepositoryTest {

    @Autowired
    private JdbcPriceRepository underTest;

    @Test
    void shouldFindEveryPriceWhoseRangeContainsTheApplicationDate() {
        assertThat(underTest.findApplicablePrices(queryAt("2020-06-14T16:00:00")))
                .extracting(Price::priceList)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldIncludePriceStartingExactlyAtTheApplicationDate() {
        assertThat(underTest.findApplicablePrices(queryAt("2020-06-14T15:00:00")))
                .extracting(Price::priceList)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldIncludePriceEndingExactlyAtTheApplicationDate() {
        assertThat(underTest.findApplicablePrices(queryAt("2020-06-14T18:30:00")))
                .extracting(Price::priceList)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldMapEveryColumnOfThePrice() {
        Price expected = new Price(A_BRAND_ID, A_PRODUCT_ID, 2L, LocalDateTime.parse("2020-06-14T15:00:00"), LocalDateTime.parse("2020-06-14T18:30:00"),
                1, new BigDecimal("25.45"), Currency.getInstance("EUR"));

        assertThat(underTest.findApplicablePrices(queryAt("2020-06-14T16:00:00"))).contains(expected);
    }

    @Test
    void shouldFindNothingBeforeAnyPriceStarts() {
        assertThat(underTest.findApplicablePrices(queryAt("2020-06-13T23:59:59"))).isEmpty();
    }

    @Test
    void shouldFindNothingForAnotherProductOfTheBrand() {
        assertThat(underTest.findApplicablePrices(new PriceQuery(A_BRAND_ID, ANOTHER_PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00")))).isEmpty();
    }

    @Test
    void shouldFindNothingForTheSameProductOfAnotherBrand() {
        assertThat(underTest.findApplicablePrices(new PriceQuery(ANOTHER_BRAND_ID, A_PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00")))).isEmpty();
    }

    private static PriceQuery queryAt(String applicationDate) {
        return new PriceQuery(A_BRAND_ID, A_PRODUCT_ID, LocalDateTime.parse(applicationDate));
    }
}
