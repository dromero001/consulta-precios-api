package com.ecommerce.prices.adapter.database;

import static com.ecommerce.prices.domain.model.PriceFixtures.AN_UNKNOWN_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.prices.domain.model.BrandId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
class DatabaseSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadTheInitialBrandsInIdentifierOrder() {
        List<String> brandNames = jdbcTemplate.queryForList("SELECT NAME FROM BRANDS ORDER BY ID", String.class);

        assertThat(brandNames).containsExactly(
                "ZARA", "PULL&BEAR", "MASSIMO DUTTI", "BERSHKA", "STRADIVARIUS", "OYSHO", "ZARA HOME", "LEFTIES");
    }

    @Test
    void shouldLoadTheInitialPriceLists() {
        List<Long> priceLists = jdbcTemplate.queryForList("SELECT PRICE_LIST FROM PRICES ORDER BY PRICE_LIST", Long.class);

        assertThat(priceLists).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void shouldRejectPriceOverlappingAnotherOfTheSameProductWithTheSamePriority() {
        assertThatThrownBy(() -> insertPrice(A_BRAND_ID, "2020-07-01T00:00:00", "2020-07-31T23:59:59", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAcceptPriceOverlappingAnotherOfTheSameProductWithADifferentPriority() {
        insertPrice(A_BRAND_ID, "2020-07-01T00:00:00", "2020-07-31T23:59:59", 2);

        assertThat(countPricesWithPriority(2)).isEqualTo(1);
    }

    @Test
    void shouldAcceptPriceWithTheSamePriorityWhenRangesDoNotOverlap() {
        insertPrice(A_BRAND_ID, "2021-01-01T00:00:00", "2021-01-31T23:59:59", 0);

        assertThat(countPricesWithPriority(0)).isEqualTo(2);
    }

    @Test
    void shouldRejectPriceSharingOnlyTheBoundarySecondWithAnotherOfTheSamePriority() {
        assertThatThrownBy(() -> insertPrice(A_BRAND_ID, "2020-12-31T23:59:59", "2021-01-31T23:59:59", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldLetAMultiRowUpdateCreateATieBecauseTheCheckOnlyComparesTheRowBeingWritten() {
        jdbcTemplate.update("UPDATE PRICES SET PRIORITY = 7 WHERE PRICE_LIST IN (1, 2)");

        assertThat(countPricesWithPriority(7)).isEqualTo(2);
    }

    @Test
    void shouldRejectPriceEndingBeforeItStarts() {
        assertThatThrownBy(() -> insertPrice(A_BRAND_ID, "2021-02-01T00:00:00", "2021-01-01T00:00:00", 5))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectPriceOfAnUnknownBrand() {
        assertThatThrownBy(() -> insertPrice(AN_UNKNOWN_BRAND_ID, "2021-01-01T00:00:00", "2021-01-31T23:59:59", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertPrice(BrandId brandId, String startDate, String endDate, int priority) {
        jdbcTemplate.update(
                "INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                brandId.value(), LocalDateTime.parse(startDate), LocalDateTime.parse(endDate), 5L, A_PRODUCT_ID.value(), priority, new BigDecimal("10.00"), "EUR");
    }

    private Integer countPricesWithPriority(int priority) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRICES WHERE PRIORITY = ?", Integer.class, priority);
    }
}
