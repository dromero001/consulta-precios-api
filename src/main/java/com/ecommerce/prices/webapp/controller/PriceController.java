package com.ecommerce.prices.webapp.controller;

import com.ecommerce.prices.domain.model.BrandId;
import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.model.ProductId;
import com.ecommerce.prices.domain.usecase.GetApplicablePriceUseCase;
import com.ecommerce.prices.webapp.controller.api.PricesApi;
import com.ecommerce.prices.webapp.controller.dto.PriceResponse;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController implements PricesApi {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        Price price = getApplicablePriceUseCase.execute(new PriceQuery(new BrandId(brandId), new ProductId(productId), applicationDate));
        return ResponseEntity.ok(toResponse(price));
    }

    private static PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.productId().value(),
                price.brandId().value(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.amount(),
                price.currency().getCurrencyCode());
    }
}
