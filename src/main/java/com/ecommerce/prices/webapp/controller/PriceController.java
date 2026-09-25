package com.ecommerce.prices.webapp.controller;

import com.ecommerce.prices.domain.model.BrandId;
import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.model.ProductId;
import com.ecommerce.prices.domain.usecase.GetApplicablePriceUseCase;
import com.ecommerce.prices.webapp.controller.api.PricesApi;
import com.ecommerce.prices.webapp.controller.dto.PriceResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController implements PricesApi {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(String applicationDate, Long productId, Long brandId) {
        PriceQuery query = new PriceQuery(new BrandId(brandId), new ProductId(productId), toLocalDateTime(applicationDate));
        return ResponseEntity.ok(toResponse(getApplicablePriceUseCase.execute(query)));
    }

    private static LocalDateTime toLocalDateTime(String applicationDate) {
        try {
            return LocalDateTime.parse(applicationDate, LOCAL_DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new InvalidApplicationDateException(applicationDate);
        }
    }

    private static PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.productId().value(),
                price.brandId().value(),
                price.priceList(),
                LOCAL_DATE_TIME_FORMAT.format(price.startDate()),
                LOCAL_DATE_TIME_FORMAT.format(price.endDate()),
                price.amount(),
                price.currency().getCurrencyCode());
    }
}
