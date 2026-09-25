package com.ecommerce.prices.adapter.database;

import com.ecommerce.prices.domain.model.BrandId;
import com.ecommerce.prices.domain.port.BrandRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBrandRepository implements BrandRepository {

    private static final String EXISTS_BRAND = "SELECT EXISTS (SELECT 1 FROM BRANDS WHERE ID = :brandId)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcBrandRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(BrandId brandId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("brandId", brandId.value());
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(EXISTS_BRAND, parameters, Boolean.class));
    }
}
