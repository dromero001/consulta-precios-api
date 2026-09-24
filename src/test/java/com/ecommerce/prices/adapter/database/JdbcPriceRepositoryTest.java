package com.ecommerce.prices.adapter.database;

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

    private static final long ZARA = 1L;
    private static final long PULL_AND_BEAR = 2L;
    private static final long A_PRODUCT_ID = 35455L;
    private static final long ANOTHER_PRODUCT_ID = 99999L;

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
        Price expected = new Price(ZARA, A_PRODUCT_ID, 2L, LocalDateTime.parse("2020-06-14T15:00:00"), LocalDateTime.parse("2020-06-14T18:30:00"),
                1, new BigDecimal("25.45"), Currency.getInstance("EUR"));

        assertThat(underTest.findApplicablePrices(queryAt("2020-06-14T16:00:00"))).contains(expected);
    }

    @Test
    void shouldFindNothingBeforeAnyPriceStarts() {
        assertThat(underTest.findApplicablePrices(queryAt("2020-06-13T23:59:59"))).isEmpty();
    }

    @Test
    void shouldFindNothingForAnotherProductOfTheBrand() {
        assertThat(underTest.findApplicablePrices(new PriceQuery(ZARA, ANOTHER_PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00")))).isEmpty();
    }

    @Test
    void shouldFindNothingForTheSameProductOfAnotherBrand() {
        assertThat(underTest.findApplicablePrices(new PriceQuery(PULL_AND_BEAR, A_PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00")))).isEmpty();
    }

    private static PriceQuery queryAt(String applicationDate) {
        return new PriceQuery(ZARA, A_PRODUCT_ID, LocalDateTime.parse(applicationDate));
    }
}
