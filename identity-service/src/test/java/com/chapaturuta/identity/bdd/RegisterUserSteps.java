package com.chapaturuta.identity.bdd;

import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class RegisterUserSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private UserRegistrationRequest request;
    private ResultActions response;

    @Given("a new user wants to register with email {string}, name {string}, password {string}, and role {string}")
    public void prepareRegistrationRequest(String email, String name, String password, String roleStr) {
        Role roleEnum = Role.valueOf(roleStr.toUpperCase());

        UUID companyId = (roleEnum == Role.DRIVER) ? UUID.randomUUID() : null;

        request = new UserRegistrationRequest(name, email, password, roleEnum, companyId);
    }

    @When("the registration request is processed")
    public void processRegistration() throws Exception {
        response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Then("the account is successfully created returning the user details")
    public void verifySuccessfulRegistration() throws Exception {
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.email").value(request.email()));
    }

    @Given("an existing user is already registered with email {string}")
    public void setupExistingEmail(String duplicateEmail) {
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .name("Usuario Existente")
                .email(duplicateEmail)
                .password("hashed_pass")
                .role(Role.PASSENGER)
                .createdAt(LocalDateTime.now())
                .build();

        if (userRepository.findByEmail(duplicateEmail).isEmpty()) {
            userRepository.save(existingUser);
        }
    }

    @When("a new user attempts to register with the duplicate email {string}")
    public void attemptDuplicateRegistration(String duplicateEmail) throws Exception {
        request = new UserRegistrationRequest("Nuevo Intento", duplicateEmail, "pass123", Role.PASSENGER, null);

        processRegistration();
    }

    @Given("a new driver attempts to register with email {string}, name {string}, password {string}, but no company ID")
    public void prepareInvalidDriverRequest(String email, String name, String password) {
        request = new UserRegistrationRequest(name, email, password, Role.DRIVER, null);
    }

    @Then("the system rejects the request with a bad request error {string}")
    public void verifyErrorMessage(String expectedMessage) throws Exception {
        response.andExpect(status().isBadRequest())
                .andExpect(content().string(expectedMessage));
    }
}