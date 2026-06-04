Feature: Vehicle Check-In Tracking Flow
  As a driver navigating a route
  I want my active location to be stored and published asynchronously
  So that the tracking system can notify passengers without blocking the application

  Scenario: Successful Check-In processing without passengers waiting
    Given an active driver transmits check-in coordinates latitude -12.0435 and longitude -76.9532 for a route
    When the check-in command is processed by the command service
    Then the system updates the vehicle location in cache and emits an async notification event

  Scenario: Successful Check-In processing with waiting passengers at a stop
    Given an active driver transmits check-in coordinates latitude -12.0435 and longitude -76.9532 for a route
    And the route has waiting passengers at stop "11111111-1111-1111-1111-111111111111"
    When the check-in command is processed by the command service
    Then the system updates the vehicle location in cache and emits an async notification event
    And the waiting passengers at the stop receive a two-minute extension
