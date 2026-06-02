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

import java.util.UUID;

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
        Role roleEnum = Role.valueOf(roleStr.toUpperCase());

        // Asignamos un companyId ficticio si el rol es DRIVER para pasar la nueva validación de negocio
        UUID companyId = (roleEnum == Role.DRIVER) ? UUID.randomUUID() : null;

        // Se añade el 5to parámetro
        request = new UserRegistrationRequest(name, email, password, roleEnum, companyId);
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
        try {
            // Se añade null como 5to parámetro para un PASSENGER
            registerUserUseCase.registerUser(
                    new UserRegistrationRequest("User Existente", duplicateEmail, "pass", Role.PASSENGER, null)
            );
        } catch (Exception ignored) {}
    }

    @When("a new user attempts to register with the duplicate email {string}")
    public void attemptDuplicateRegistration(String duplicateEmail) {
        // Se añade null como 5to parámetro
        request = new UserRegistrationRequest("Nuevo Intento", duplicateEmail, "pass123", Role.PASSENGER, null);
        processRegistration();
    }

    @Then("the system rejects the request with a bad request error {string}")
    public void verifyErrorMessage(String expectedMessage) {
        assertNotNull(caughtException, "Debió lanzarse una excepción por correo duplicado");
        assertTrue(caughtException instanceof IllegalArgumentException);
        assertEquals(expectedMessage, caughtException.getMessage());
    }
}