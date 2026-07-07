package com.chapaturuta.routing.interfaces.rest;

import com.chapaturuta.routing.application.dto.RouteRequest;
import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.application.dto.TripOptionResponse;
import com.chapaturuta.routing.application.usecase.ManageRouteUseCase;
import com.chapaturuta.routing.application.usecase.SearchRoutesUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@ActiveProfiles("test")
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SearchRoutesUseCase searchRoutesUseCase;

    @MockitoBean
    private ManageRouteUseCase manageRouteUseCase;

    @Test
    void searchRoutes_ReturnsTripOptions() throws Exception {
        UUID routeId = UUID.randomUUID();
        RouteResponse route = new RouteResponse(routeId, "Ate", "Lima", 3.50, 45, null);
        when(searchRoutesUseCase.searchAvailableRoutes("Ate", "Lima"))
                .thenReturn(List.of(new TripOptionResponse(List.of(route), 3.50, 45)));

        mockMvc.perform(get("/api/v1/routes/search")
                        .param("origin", "Ate")
                        .param("destination", "Lima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].legs[0].routeId").value(routeId.toString()))
                .andExpect(jsonPath("$[0].totalPrice").value(3.50))
                .andExpect(jsonPath("$[0].totalEstimatedDuration").value(45));
    }

    @Test
    void createRoute_Returns201Created() throws Exception {
        UUID routeId = UUID.randomUUID();
        RouteRequest request = new RouteRequest("Ate", "Lima", 3.50, 45, null);
        when(manageRouteUseCase.createRoute(any(RouteRequest.class)))
                .thenReturn(new RouteResponse(routeId, "Ate", "Lima", 3.50, 45, null));

        mockMvc.perform(post("/api/v1/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").value(routeId.toString()))
                .andExpect(jsonPath("$.origin").value("Ate"))
                .andExpect(jsonPath("$.destination").value("Lima"));
    }

    @Test
    void updateRoute_WhenRouteDoesNotExist_Returns404() throws Exception {
        UUID routeId = UUID.randomUUID();
        RouteRequest request = new RouteRequest("Ate", "Callao", 4.00, 55, null);

        when(manageRouteUseCase.updateRoute(eq(routeId), any(RouteRequest.class)))
                .thenThrow(new IllegalArgumentException("Ruta no encontrada"));

        mockMvc.perform(put("/api/v1/routes/{id}", routeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ruta no encontrada"));
    }

    @Test
    void deleteRoute_WhenRouteExists_Returns204NoContent() throws Exception {
        UUID routeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/routes/{id}", routeId))
                .andExpect(status().isNoContent());

        verify(manageRouteUseCase).deleteRoute(routeId);
    }

    @Test
    void deleteRoute_WhenRouteDoesNotExist_Returns404() throws Exception {
        UUID routeId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("Ruta no encontrada")).when(manageRouteUseCase).deleteRoute(routeId);

        mockMvc.perform(delete("/api/v1/routes/{id}", routeId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ruta no encontrada"));
    }
}
