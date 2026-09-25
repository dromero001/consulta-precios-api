package com.ecommerce.prices.adapter.database;

import static com.ecommerce.prices.domain.model.PriceFixtures.AN_UNKNOWN_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_BRAND_ID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@Import(JdbcBrandRepository.class)
class JdbcBrandRepositoryTest {

    @Autowired
    private JdbcBrandRepository underTest;

    @Test
    void shouldConfirmThatAnExistingBrandExists() {
        assertThat(underTest.exists(A_BRAND_ID)).isTrue();
    }

    @Test
    void shouldDenyThatAnUnknownBrandExists() {
        assertThat(underTest.exists(AN_UNKNOWN_BRAND_ID)).isFalse();
    }
}
