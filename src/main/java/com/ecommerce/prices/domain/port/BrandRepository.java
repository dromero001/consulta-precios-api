package com.ecommerce.prices.domain.port;

import com.ecommerce.prices.domain.model.BrandId;

public interface BrandRepository {

    boolean exists(BrandId brandId);
}
