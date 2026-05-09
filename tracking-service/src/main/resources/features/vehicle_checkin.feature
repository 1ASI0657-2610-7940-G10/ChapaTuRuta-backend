Feature: Vehicle Check-In Tracking Flow
  As a driver navigating a route
  I want my active location to be stored at high speed and broadcasted asynchronously
  So that waiting passengers receive immediate updates without blocking my application

  Scenario: Successful high-speed Check-In processing
    Given an active driver transmits check-in coordinates latitude -12.0435 and longitude -76.9532 for a route
    When the check-in command is processed by the command service
    Then the system updates the vehicle location in cache and emits an async notification event