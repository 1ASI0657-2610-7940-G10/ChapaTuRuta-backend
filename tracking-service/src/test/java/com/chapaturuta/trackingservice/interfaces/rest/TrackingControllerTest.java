package com.chapaturuta.trackingservice.interfaces.rest;

import com.chapaturuta.trackingservice.application.dto.CheckInRequest;
import com.chapaturuta.trackingservice.application.dto.EtaQueryResponse;
import com.chapaturuta.trackingservice.application.usecase.TrackingCommandService;
import com.chapaturuta.trackingservice.application.usecase.TrackingQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackingController.class)
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrackingCommandService trackingCommandService;

    @MockitoBean
    private TrackingQueryService trackingQueryService;

    @Test
    void processCheckIn_Returns202Accepted() throws Exception {
        CheckInRequest request = new CheckInRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                -12.0435,
                -76.9532,
                System.currentTimeMillis()
        );

        mockMvc.perform(post("/api/v1/tracking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Check-in procesado asíncronamente"));

        verify(trackingCommandService).processCheckIn(any());
    }

    @Test
    void getEta_Returns200OkWithData() throws Exception {
        UUID routeId = UUID.randomUUID();
        EtaQueryResponse response = new EtaQueryResponse(
                routeId,
                -12.045,
                -76.954,
                "15 min"
        );

        when(trackingQueryService.getRouteEta(eq(routeId), eq(-12.0435), eq(-76.9532)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/tracking/eta/{routeId}", routeId)
                        .param("pasajeroLat", "-12.0435")
                        .param("pasajeroLng", "-76.9532"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value(routeId.toString()))
                .andExpect(jsonPath("$.currentLatitude").value(-12.045))
                .andExpect(jsonPath("$.currentLongitude").value(-76.954))
                .andExpect(jsonPath("$.estimatedTime").value("15 min"));
    }
}