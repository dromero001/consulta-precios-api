package com.ecommerce.prices.webapp.controller;

import static com.ecommerce.prices.domain.model.PriceFixtures.AN_APPLICATION_DATE;
import static com.ecommerce.prices.domain.model.PriceFixtures.AN_UNKNOWN_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_BRAND_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.A_PRODUCT_ID;
import static com.ecommerce.prices.domain.model.PriceFixtures.aPrice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ecommerce.prices.domain.exception.AmbiguousPriceException;
import com.ecommerce.prices.domain.exception.BrandNotFoundException;
import com.ecommerce.prices.domain.exception.PriceNotFoundException;
import com.ecommerce.prices.domain.model.Price;
import com.ecommerce.prices.domain.model.PriceQuery;
import com.ecommerce.prices.domain.usecase.GetApplicablePriceUseCase;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private GetApplicablePriceUseCase getApplicablePriceUseCaseMock;

    @Test
    void shouldReturnTheApplicablePriceOfTheBrandProductAtTheApplicationDate() {
        PriceQuery query = new PriceQuery(A_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE);
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

    @Test
    void shouldAnswerNotFoundWhenTheBrandDoesNotExist() {
        when(getApplicablePriceUseCaseMock.execute(new PriceQuery(AN_UNKNOWN_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE)))
                .thenThrow(new BrandNotFoundException(AN_UNKNOWN_BRAND_ID));

        assertProblem(getPrice("2020-06-14T16:00:00", "35455", "99"), 404, "Not Found", "Brand 99 not found");
    }

    @Test
    void shouldAnswerNotFoundWhenNoPriceApplies() {
        PriceQuery query = new PriceQuery(A_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE);
        when(getApplicablePriceUseCaseMock.execute(query)).thenThrow(new PriceNotFoundException(query));

        assertProblem(getPrice("2020-06-14T16:00:00", "35455", "1"), 404, "Not Found",
                "No price applies to product 35455 of brand 1 at 2020-06-14T16:00:00");
    }

    @Test
    void shouldAnswerInternalServerErrorWhenSeveralPricesShareTheHighestPriority() {
        List<Price> tiedPrices = List.of(aPrice().priceList(2L).priority(1).build(), aPrice().priceList(3L).priority(1).build());
        when(getApplicablePriceUseCaseMock.execute(new PriceQuery(A_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE)))
                .thenThrow(new AmbiguousPriceException(tiedPrices));

        assertProblem(getPrice("2020-06-14T16:00:00", "35455", "1"), 500, "Ambiguous price",
                "Price lists 2 and 3 of product 35455 of brand 1 share the highest priority 1");
    }

    @Test
    void shouldAnswerBadRequestWhenAParameterIsMissing() {
        MvcTestResult result = mockMvc.get().uri("/prices")
                .param("applicationDate", "2020-06-14T16:00:00")
                .param("productId", "35455")
                .exchange();

        assertProblem(result, 400, "Bad Request", "Required parameter 'brandId' is not present.");
    }

    @Test
    void shouldAnswerBadRequestWhenAnIdentifierIsNotANumber() {
        assertProblem(getPrice("2020-06-14T16:00:00", "abc", "1"), 400, "Bad Request", "Failed to convert 'productId' with value: 'abc'");
    }

    @Test
    void shouldAnswerBadRequestWhenAnIdentifierIsNotPositive() {
        assertProblem(getPrice("2020-06-14T16:00:00", "35455", "0"), 400, "Bad Request", "brandId: must be greater than or equal to 1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020-06-14 16:00:00", "2020-06-14T16:00", "2020-06-14T16:00:00.5", "2020-06-14T16:00:00+02:00"})
    void shouldAnswerBadRequestWhenTheApplicationDateIsNotALocalDateTimeWithSeconds(String applicationDate) {
        assertProblem(getPrice(applicationDate, "35455", "1"), 400, "Bad Request",
                "applicationDate: must match \"^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$\"");
    }

    @Test
    void shouldAnswerBadRequestWhenTheApplicationDateDoesNotExistInTheCalendar() {
        assertProblem(getPrice("2020-02-30T10:00:00", "35455", "1"), 400, "Bad Request",
                "applicationDate: '2020-02-30T10:00:00' is not a valid date");
    }

    @Test
    void shouldAnswerInternalServerErrorWithoutLeakingDetailsWhenSomethingUnexpectedFails() {
        when(getApplicablePriceUseCaseMock.execute(new PriceQuery(A_BRAND_ID, A_PRODUCT_ID, AN_APPLICATION_DATE)))
                .thenThrow(new IllegalStateException("connection pool exhausted"));

        assertProblem(getPrice("2020-06-14T16:00:00", "35455", "1"), 500, "Internal Server Error", "Unexpected error");
    }

    private MvcTestResult getPrice(String applicationDate, String productId, String brandId) {
        return mockMvc.get().uri("/prices")
                .param("applicationDate", applicationDate)
                .param("productId", productId)
                .param("brandId", brandId)
                .exchange();
    }

    private static void assertProblem(MvcTestResult result, int status, String title, String detail) {
        assertThat(result)
                .hasStatus(status)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .isStrictlyEqualTo("""
                        {"title": "%s", "status": %d, "detail": "%s", "instance": "/prices"}
                        """.formatted(title, status, asJsonString(detail)));
    }

    private static String asJsonString(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
