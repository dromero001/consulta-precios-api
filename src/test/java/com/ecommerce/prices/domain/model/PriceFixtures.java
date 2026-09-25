package com.ecommerce.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

public final class PriceFixtures {

    public static final BrandId A_BRAND_ID = new BrandId(1L);
    public static final BrandId ANOTHER_BRAND_ID = new BrandId(2L);
    public static final BrandId AN_UNKNOWN_BRAND_ID = new BrandId(99L);
    public static final ProductId A_PRODUCT_ID = new ProductId(35455L);
    public static final ProductId ANOTHER_PRODUCT_ID = new ProductId(99999L);
    public static final LocalDateTime AN_APPLICATION_DATE = LocalDateTime.parse("2020-06-14T16:00:00");

    private PriceFixtures() {
    }

    public static PriceBuilder aPrice() {
        return new PriceBuilder();
    }

    public static final class PriceBuilder {

        private long priceList = 1L;
        private int priority = 0;
        private BigDecimal amount = new BigDecimal("35.50");
        private LocalDateTime startDate = LocalDateTime.parse("2020-06-14T00:00:00");
        private LocalDateTime endDate = LocalDateTime.parse("2020-12-31T23:59:59");

        public PriceBuilder priceList(long priceList) {
            this.priceList = priceList;
            return this;
        }

        public PriceBuilder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public PriceBuilder amount(String amount) {
            this.amount = new BigDecimal(amount);
            return this;
        }

        public PriceBuilder between(String startDate, String endDate) {
            this.startDate = LocalDateTime.parse(startDate);
            this.endDate = LocalDateTime.parse(endDate);
            return this;
        }

        public Price build() {
            return new Price(A_BRAND_ID, A_PRODUCT_ID, priceList, startDate, endDate, priority, amount, Currency.getInstance("EUR"));
        }
    }
}
