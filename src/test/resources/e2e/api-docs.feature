Feature: API documentation

  Background:
    * url karate.properties['baseUrl']

  Scenario: The OpenAPI contract is published as written
    Given path 'openapi', 'prices-api.yaml'
    When method get
    Then status 200
    * string contract = response
    And match contract contains 'operationId: getApplicablePrice'

  Scenario: Swagger UI is served and points to the published contract
    Given path 'swagger-ui', 'index.html'
    When method get
    Then status 200

    Given path 'v3', 'api-docs', 'swagger-config'
    When method get
    Then status 200
    And match response.url == '/openapi/prices-api.yaml'
