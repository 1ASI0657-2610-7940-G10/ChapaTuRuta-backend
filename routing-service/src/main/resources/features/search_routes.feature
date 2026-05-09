Feature: Route Search Flow
  As a passenger planning a trip
  I want to search for available routes between my origin and destination districts
  So that I can view the price and estimated duration in ChapaTuRuta

  Scenario: Successful search for existing routes
    Given the system has available routes registered from "San Juan de Lurigancho" to "Ate"
    When a user searches for available routes from "San Juan de Lurigancho" to "Ate"
    Then the system returns a list of routes containing pricing and duration details

  Scenario: Search for routes with no coverage
    Given there are no routes registered from "Ancón" to "Chosica"
    When a user searches for available routes from "Ancón" to "Chosica"
    Then the system returns an empty list of routes