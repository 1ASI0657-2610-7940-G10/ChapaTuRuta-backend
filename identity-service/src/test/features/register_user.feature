Feature: User Registration Authentication Flow
  As a new passenger or driver
  I want to register an account with my personal details
  So that I can authenticate and access the ChapaTuRuta platform

  Scenario: Successful user registration
    Given a new user wants to register with email "juan.perez@example.com", name "Juan Perez", password "secure123", and role "PASSENGER"
    When the registration request is processed
    Then the account is successfully created returning the user details

  Scenario: Registration fails due to duplicate email
    Given an existing user is already registered with email "admin@example.com"
    When a new user attempts to register with the duplicate email "admin@example.com"
    Then the system rejects the request with a bad request error "El correo ya está registrado"