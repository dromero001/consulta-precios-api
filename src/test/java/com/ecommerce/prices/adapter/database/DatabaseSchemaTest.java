package com.ecommerce.prices.adapter.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private static final long ZARA = 1L;
    private static final long A_PRODUCT_ID = 35455L;
    private static final long UNKNOWN_BRAND = 99L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadInditexBrandsWithZaraAsBrandOne() {
        List<String> brandNames = jdbcTemplate.queryForList("SELECT NAME FROM BRANDS ORDER BY ID", String.class);

        assertThat(brandNames).containsExactly(
                "ZARA", "PULL&BEAR", "MASSIMO DUTTI", "BERSHKA", "STRADIVARIUS", "OYSHO", "ZARA HOME", "LEFTIES");
    }

    @Test
    void shouldLoadTheFourPriceListsOfTheStatement() {
        List<Long> priceLists = jdbcTemplate.queryForList("SELECT PRICE_LIST FROM PRICES ORDER BY PRICE_LIST", Long.class);

        assertThat(priceLists).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void shouldRejectPriceOverlappingAnotherOfTheSameProductWithTheSamePriority() {
        assertThatThrownBy(() -> insertPrice(ZARA, "2020-07-01T00:00:00", "2020-07-31T23:59:59", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAcceptPriceOverlappingAnotherOfTheSameProductWithADifferentPriority() {
        insertPrice(ZARA, "2020-07-01T00:00:00", "2020-07-31T23:59:59", 2);

        assertThat(countPricesWithPriority(2)).isEqualTo(1);
    }

    @Test
    void shouldAcceptPriceWithTheSamePriorityWhenRangesDoNotOverlap() {
        insertPrice(ZARA, "2021-01-01T00:00:00", "2021-01-31T23:59:59", 0);

        assertThat(countPricesWithPriority(0)).isEqualTo(2);
    }

    @Test
    void shouldRejectPriceSharingOnlyTheBoundarySecondWithAnotherOfTheSamePriority() {
        assertThatThrownBy(() -> insertPrice(ZARA, "2020-12-31T23:59:59", "2021-01-31T23:59:59", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectPriceEndingBeforeItStarts() {
        assertThatThrownBy(() -> insertPrice(ZARA, "2021-02-01T00:00:00", "2021-01-01T00:00:00", 5))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectPriceOfAnUnknownBrand() {
        assertThatThrownBy(() -> insertPrice(UNKNOWN_BRAND, "2021-01-01T00:00:00", "2021-01-31T23:59:59", 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertPrice(long brandId, String startDate, String endDate, int priority) {
        jdbcTemplate.update(
                "INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                brandId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate), 5L, A_PRODUCT_ID, priority, new BigDecimal("10.00"), "EUR");
    }

    private Integer countPricesWithPriority(int priority) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRICES WHERE PRIORITY = ?", Integer.class, priority);
    }
}
