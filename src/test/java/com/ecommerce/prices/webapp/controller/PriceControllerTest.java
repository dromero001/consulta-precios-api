package com.ecommerce.prices.webapp.controller;

import static com.ecommerce.prices.domain.model.PriceFixtures.A_PRODUCT_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.ZARA;
import static com.ecommerce.prices.domain.model.PriceFixtures.aPrice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.usecase.GetApplicablePriceUseCase;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private GetApplicablePriceUseCase getApplicablePriceUseCaseMock;

    @Test
    void shouldReturnTheApplicablePriceOfTheBrandProductAtTheApplicationDate() {
        PriceQuery query = new PriceQuery(ZARA, A_PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00"));
        when(getApplicablePriceUseCaseMock.execute(query)).thenReturn(
                aPrice().priceList(2L).priority(1).amount("25.45").between("2020-06-14T15:00:00", "2020-06-14T18:30:00").build());

        assertThat(mockMvc.get().uri("/prices")
                .param("applicationDate", "2020-06-14T16:00:00")
                .param("productId", "35455")
                .param("brandId", "1"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isStrictlyEqualTo("""
                        {
                          "productId": 35455,
                          "brandId": 1,
                          "priceList": 2,
                          "startDate": "2020-06-14T15:00:00",
                          "endDate": "2020-06-14T18:30:00",
                          "price": 25.45,
                          "currency": "EUR"
                        }
                        """);
    }
}
