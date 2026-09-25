package com.ecommerce.prices.configuration;

import com.ecommerce.prices.domain.port.BrandRepository;
import com.ecommerce.prices.domain.port.PriceRepository;
import com.ecommerce.prices.domain.strategy.HighestPriorityPriceSelectionStrategy;
import com.ecommerce.prices.domain.strategy.PriceSelectionStrategy;
import com.ecommerce.prices.domain.usecase.GetApplicablePriceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public PriceSelectionStrategy priceSelectionStrategy() {
        return new HighestPriorityPriceSelectionStrategy();
    }

    @Bean
    public GetApplicablePriceUseCase getApplicablePriceUseCase(
            BrandRepository brandRepository, PriceRepository priceRepository, PriceSelectionStrategy priceSelectionStrategy) {
        return new GetApplicablePriceUseCase(brandRepository, priceRepository, priceSelectionStrategy);
    }
}
