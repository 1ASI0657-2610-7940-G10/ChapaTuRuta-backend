package com.chapaturuta.identity.bdd;

import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.usecase.RegisterUserUseCase;
import com.chapaturuta.identity.domain.model.Role;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest
public class RegisterUserSteps {

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    private UserRegistrationRequest request;
    private UserResponse response;
    private Exception caughtException;

    @Given("a new user wants to register with email {string}, name {string}, password {string}, and role {string}")
    public void prepareRegistrationRequest(String email, String name, String password, String roleStr) {
        // Convertimos el String al Enum Role correspondiente
        Role roleEnum = Role.valueOf(roleStr.toUpperCase());
        request = new UserRegistrationRequest(name, email, password, roleEnum);
    }

    @When("the registration request is processed")
    public void processRegistration() {
        try {
            response = registerUserUseCase.registerUser(request);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the account is successfully created returning the user details")
    public void verifySuccessfulRegistration() {
        assertNull(caughtException, "No debió ocurrir ninguna excepción");
        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
    }

    @Given("an existing user is already registered with email {string}")
    public void setupExistingEmail(String duplicateEmail) {
        // Usamos directamente la constante del enum Role.PASSENGER
        try {
            registerUserUseCase.registerUser(
                    new UserRegistrationRequest("User Existente", duplicateEmail, "pass", Role.PASSENGER)
            );
        } catch (Exception ignored) {}
    }

    @When("a new user attempts to register with the duplicate email {string}")
    public void attemptDuplicateRegistration(String duplicateEmail) {
        // Usamos directamente la constante del enum Role.PASSENGER
        request = new UserRegistrationRequest("Nuevo Intento", duplicateEmail, "pass123", Role.PASSENGER);
        processRegistration();
    }

    @Then("the system rejects the request with a bad request error {string}")
    public void verifyErrorMessage(String expectedMessage) {
        assertNotNull(caughtException, "Debió lanzarse una excepción por correo duplicado");
        assertTrue(caughtException instanceof IllegalArgumentException);
        assertEquals(expectedMessage, caughtException.getMessage());
    }
}