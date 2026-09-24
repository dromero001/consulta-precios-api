package com.ecommerce.prices.adapter.database;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@Import(JdbcBrandRepository.class)
class JdbcBrandRepositoryTest {

    private static final long ZARA = 1L;
    private static final long UNKNOWN_BRAND = 99L;

    @Autowired
    private JdbcBrandRepository underTest;

    @Test
    void shouldConfirmThatAnExistingBrandExists() {
        assertThat(underTest.exists(ZARA)).isTrue();
    }

    @Test
    void shouldDenyThatAnUnknownBrandExists() {
        assertThat(underTest.exists(UNKNOWN_BRAND)).isFalse();
    }
}
