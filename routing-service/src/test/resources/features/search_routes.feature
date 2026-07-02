Feature: Route Search Flow
  As a passenger planning a trip
  I want to search for available routes between my origin and destination districts
  So that I can view the price and estimated duration in ChapaTuRuta

  Scenario: Successful search for existing direct routes
    Given the system has available routes registered from "San Juan de Lurigancho" to "Ate"
    When a user searches for available routes from "San Juan de Lurigancho" to "Ate"
    Then the system returns a list of routes containing pricing and duration details
    And each route option has a valid price and duration

  Scenario: Search for routes with no coverage
    Given there are no routes registered from "Ancón" to "Chosica"
    When a user searches for available routes from "Ancón" to "Chosica"
    Then the system returns an empty list of routes

  Scenario: Search for routes with transfer
    Given the system has a transfer route from "Ate" via "La Victoria" to "Cercado de Lima"
    When a user searches for available routes from "Ate" to "Cercado de Lima"
    Then the system returns routes with multiple legs

  Scenario: Search for multiple available direct routes
    Given the system has 3 available routes from "Miraflores" to "San Isidro"
    When a user searches for available routes from "Miraflores" to "San Isidro"
    Then the system returns 3 available routes
    And each route option has a valid price and duration
