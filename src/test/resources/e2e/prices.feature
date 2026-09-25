Feature: Applicable price of a brand product at a given date

  Background:
    * url karate.properties['baseUrl']
    * path 'prices'

  Scenario Outline: <rule> (request at <applicationDate>)
    Given params { applicationDate: '<applicationDate>', productId: 35455, brandId: 1 }
    When method get
    Then status 200
    And match response ==
      """
      {
        productId: 35455,
        brandId: 1,
        priceList: <priceList>,
        startDate: '<startDate>',
        endDate: '<endDate>',
        price: <price>,
        currency: 'EUR'
      }
      """

    Examples:
      | rule                                                         | applicationDate     | priceList | startDate           | endDate             | price |
      | Only the base price list covers the date                     | 2020-06-14T10:00:00 | 1         | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50 |
      | A higher priority price list overrides the base one          | 2020-06-14T16:00:00 | 2         | 2020-06-14T15:00:00 | 2020-06-14T18:30:00 | 25.45 |
      | The base price list applies again once the override ends     | 2020-06-14T21:00:00 | 1         | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50 |
      | A same-day higher priority price list overrides the base one | 2020-06-15T10:00:00 | 3         | 2020-06-15T00:00:00 | 2020-06-15T11:00:00 | 30.50 |
      | A long-running higher priority price list overrides the base | 2020-06-16T21:00:00 | 4         | 2020-06-15T16:00:00 | 2020-12-31T23:59:59 | 38.95 |

  Scenario: No price applies before the first price list starts
    Given params { applicationDate: '2020-06-13T23:59:59', productId: 35455, brandId: 1 }
    When method get
    Then status 404
    And match responseHeaders['Content-Type'][0] == 'application/problem+json'
    And match response == { title: 'Not Found', status: 404, detail: 'No price applies to product 35455 of brand 1 at 2020-06-13T23:59:59', instance: '/prices' }

  Scenario: The brand does not exist
    Given params { applicationDate: '2020-06-14T10:00:00', productId: 35455, brandId: 99 }
    When method get
    Then status 404
    And match response == { title: 'Not Found', status: 404, detail: 'Brand 99 not found', instance: '/prices' }

  Scenario: A required parameter is missing
    Given params { applicationDate: '2020-06-14T10:00:00', productId: 35455 }
    When method get
    Then status 400
    And match response == { title: 'Bad Request', status: 400, detail: "Required parameter 'brandId' is not present.", instance: '/prices' }
