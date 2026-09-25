package com.ecommerce.prices.domain.usecase;

import static com.ecommerce.prices.domain.model.PriceFixtures.AN_APPLICATION_DATE;
import static com.ecommerce.prices.domain.model.PriceFixtures.AN_UNKNOWN_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_PRODUCT_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.aPrice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ecommerce.prices.domain.exception.BrandNotFoundException;
import com.ecommerce.prices.domain.exception.PriceNotFoundException;
import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.port.BrandRepository;
import com.ecommerce.prices.domain.port.PriceRepository;
import com.ecommerce.prices.domain.strategy.PriceSelectionStrategy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetApplicablePriceUseCaseTest {

    private static final PriceQuery A_QUERY = new PriceQuery(A_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE);

    private final BrandRepository brandRepositoryMock = mock(BrandRepository.class);
    private final PriceRepository priceRepositoryMock = mock(PriceRepository.class);
    private final PriceSelectionStrategy priceSelectionStrategyMock = mock(PriceSelectionStrategy.class);

    private final GetApplicablePriceUseCase underTest =
            new GetApplicablePriceUseCase(brandRepositoryMock, priceRepositoryMock, priceSelectionStrategyMock);

    @Test
    void shouldReturnThePriceSelectedAmongTheApplicableCandidates() {
        Price basePrice = aPrice().priceList(1L).priority(0).build();
        Price promotionalPrice = aPrice().priceList(2L).priority(1).build();
        List<Price> candidates = List.of(basePrice, promotionalPrice);
        when(brandRepositoryMock.exists(A_BRAND_ID)).thenReturn(true);
        when(priceRepositoryMock.findApplicablePrices(A_QUERY)).thenReturn(candidates);
        when(priceSelectionStrategyMock.select(candidates)).thenReturn(Optional.of(promotionalPrice));

        assertThat(underTest.execute(A_QUERY)).isEqualTo(promotionalPrice);
    }

    @Test
    void shouldFailWithBrandNotFoundWithoutLookingForPricesWhenTheBrandDoesNotExist() {
        PriceQuery queryOfUnknownBrand = new PriceQuery(AN_UNKNOWN_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE);
        when(brandRepositoryMock.exists(AN_UNKNOWN_BRAND_ID)).thenReturn(false);

        assertThatThrownBy(() -> underTest.execute(queryOfUnknownBrand))
                .isInstanceOf(BrandNotFoundException.class)
                .hasMessage("Brand 99 not found");
        verifyNoInteractions(priceRepositoryMock, priceSelectionStrategyMock);
    }

    @Test
    void shouldFailWithPriceNotFoundWhenNoCandidateIsSelected() {
        when(brandRepositoryMock.exists(A_BRAND_ID)).thenReturn(true);
        when(priceRepositoryMock.findApplicablePrices(A_QUERY)).thenReturn(List.of());
        when(priceSelectionStrategyMock.select(List.of())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.execute(A_QUERY))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessage("No price applies to product 35455 of brand 1 at 2020-06-14T16:00");
    }
}
