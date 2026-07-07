package com.chapaturuta.identity.interfaces.rest;

import com.chapaturuta.identity.application.dto.CompanyRegistrationRequest;
import com.chapaturuta.identity.application.dto.CompanyResponse;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.usecase.GetCompanyUseCase;
import com.chapaturuta.identity.application.usecase.ManageUserUseCase;
import com.chapaturuta.identity.application.usecase.RegisterCompanyUseCase;
import com.chapaturuta.identity.domain.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterCompanyUseCase registerCompanyUseCase;

    @MockitoBean
    private ManageUserUseCase manageUserUseCase;

    @MockitoBean
    private GetCompanyUseCase getCompanyUseCase;

    @Test
    void registerCompany_Returns201Created() throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        CompanyRegistrationRequest request = new CompanyRegistrationRequest("Empresa Lima", "20123456789", null, managerId);

        when(registerCompanyUseCase.registerCompany(any(CompanyRegistrationRequest.class)))
                .thenReturn(new CompanyResponse(companyId, "Empresa Lima", "20123456789"));

        mockMvc.perform(post("/api/v1/companies/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").value("Empresa Lima"))
                .andExpect(jsonPath("$.ruc").value("20123456789"));
    }

    @Test
    void getCompanyDrivers_ReturnsDrivers() throws Exception {
        UUID companyId = UUID.randomUUID();
        UserResponse driver = new UserResponse(
                UUID.randomUUID(),
                "Chofer Uno",
                "driver@mail.com",
                Role.DRIVER,
                companyId,
                UUID.randomUUID(),
                LocalDateTime.now()
        );

        when(manageUserUseCase.getDriversByCompany(companyId)).thenReturn(List.of(driver));

        mockMvc.perform(get("/api/v1/companies/{companyId}/drivers", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Chofer Uno"))
                .andExpect(jsonPath("$[0].role").value("DRIVER"))
                .andExpect(jsonPath("$[0].companyId").value(companyId.toString()));
    }

    @Test
    void getCompanyByManagerId_WhenNotFound_Returns404() throws Exception {
        UUID managerId = UUID.randomUUID();
        when(getCompanyUseCase.getCompanyByManagerId(managerId)).thenThrow(new IllegalArgumentException("No encontrada"));

        mockMvc.perform(get("/api/v1/companies/manager/{managerId}", managerId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No encontrada"));
    }
}
