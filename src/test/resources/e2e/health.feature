Feature: Service health

  Background:
    * url karate.properties['baseUrl']

  Scenario: The service reports itself up, database included
    Given path 'actuator', 'health'
    When method get
    Then status 200
    And match response.status == 'UP'
