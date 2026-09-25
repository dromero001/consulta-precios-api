package com.ecommerce.prices.domain.usecase;

import com.ecommerce.prices.domain.exception.BrandNotFoundException;
import com.ecommerce.prices.domain.exception.PriceNotFoundException;
import com.ecommerce.prices.domain.model.BrandId;
import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.port.BrandRepository;
import com.ecommerce.prices.domain.port.PriceRepository;
import com.ecommerce.prices.domain.strategy.PriceSelectionStrategy;
import org.springframework.stereotype.Service;

@Service
public class GetApplicablePriceUseCase {

    private final BrandRepository brandRepository;
    private final PriceRepository priceRepository;
    private final PriceSelectionStrategy priceSelectionStrategy;

    public GetApplicablePriceUseCase(BrandRepository brandRepository, PriceRepository priceRepository, PriceSelectionStrategy priceSelectionStrategy) {
        this.brandRepository = brandRepository;
        this.priceRepository = priceRepository;
        this.priceSelectionStrategy = priceSelectionStrategy;
    }

    public Price execute(PriceQuery query) {
        ensureBrandExists(query.brandId());
        return priceSelectionStrategy.select(priceRepository.findApplicablePrices(query))
                .orElseThrow(() -> new PriceNotFoundException(query));
    }

    private void ensureBrandExists(BrandId brandId) {
        if (!brandRepository.exists(brandId)) {
            throw new BrandNotFoundException(brandId);
        }
    }
}
