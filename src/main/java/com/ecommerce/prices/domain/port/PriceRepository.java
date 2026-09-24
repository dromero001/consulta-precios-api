package com.ecommerce.prices.domain.port;

import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import java.util.List;

public interface PriceRepository {

    List<Price> findApplicablePrices(PriceQuery query);
}
