Feature: Tracking Controller API
  As a client of the tracking service
  I want to register check-ins and consult ETA information
  So that the controller behaves correctly for valid and invalid requests

  Scenario: Successful check-in processing
	Given a valid check-in request for the tracking controller
	When the client sends the check-in request
	Then the response status should be 202
	And the response body should confirm asynchronous processing

  Scenario: Check-in request rejected due to invalid coordinates
	Given an invalid check-in request with latitude -91.0 and longitude -76.9532
	When the client sends the check-in request
	Then the response status should be 400
	And the response body should describe the validation error

  Scenario: Successful ETA lookup for a route
	Given the ETA service returns a result for route "11111111-1111-1111-1111-111111111111"
	When the client requests the ETA for route "11111111-1111-1111-1111-111111111111" and passenger coordinates -12.0435 and -76.9532
	Then the ETA payload should contain the route identifier and current vehicle coordinates

  Scenario: ETA lookup returns not found when the route has no active vehicles
	Given the ETA service cannot resolve route "11111111-1111-1111-1111-111111111111"
	When the client requests the ETA for route "11111111-1111-1111-1111-111111111111" and passenger coordinates -12.0435 and -76.9532
	Then the response status should be 404

