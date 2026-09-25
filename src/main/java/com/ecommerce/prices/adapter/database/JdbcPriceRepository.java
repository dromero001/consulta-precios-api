package com.ecommerce.prices.adapter.database;

import com.ecommerce.prices.domain.model.BrandId;
import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.model.ProductId;
import com.ecommerce.prices.domain.port.PriceRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPriceRepository implements PriceRepository {

    private static final String FIND_APPLICABLE_PRICES = """
            SELECT BRAND_ID, PRODUCT_ID, PRICE_LIST, START_DATE, END_DATE, PRIORITY, PRICE, CURR
            FROM PRICES
            WHERE BRAND_ID = :brandId
              AND PRODUCT_ID = :productId
              AND :applicationDate BETWEEN START_DATE AND END_DATE
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcPriceRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Price> findApplicablePrices(PriceQuery query) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("brandId", query.brandId().value())
                .addValue("productId", query.productId().value())
                .addValue("applicationDate", query.applicationDate());
        return jdbcTemplate.query(FIND_APPLICABLE_PRICES, parameters, (row, rowNumber) -> toPrice(row));
    }

    private static Price toPrice(ResultSet row) throws SQLException {
        return new Price(
                new BrandId(row.getLong("BRAND_ID")),
                new ProductId(row.getLong("PRODUCT_ID")),
                row.getLong("PRICE_LIST"),
                row.getObject("START_DATE", LocalDateTime.class),
                row.getObject("END_DATE", LocalDateTime.class),
                row.getInt("PRIORITY"),
                row.getBigDecimal("PRICE"),
                Currency.getInstance(row.getString("CURR")));
    }
}
