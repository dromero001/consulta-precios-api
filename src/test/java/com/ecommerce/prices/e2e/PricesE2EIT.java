package com.ecommerce.prices.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PricesE2EIT {

    @LocalServerPort
    private int port;

    @Test
    void shouldPassEveryEndToEndScenario() {
        Results results = Runner.path("classpath:e2e")
                .systemProperty("baseUrl", "http://localhost:" + port)
                .outputCucumberJson(false)
                .parallel(1);

        assertThat(results.getScenariosTotal()).as("executed scenarios").isPositive();
        assertThat(results.getFailCount()).as(results.getErrorMessages()).isZero();
    }
}
