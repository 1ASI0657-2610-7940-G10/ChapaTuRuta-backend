package com.chapaturuta.routing.bdd;

import com.chapaturuta.routing.application.dto.RouteRequest;
import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.application.dto.StopDTO;
import com.chapaturuta.routing.application.usecase.ManageRouteUseCase;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CreateRouteSteps {

    @Autowired
    private ManageRouteUseCase manageRouteUseCase;

    private String currentUserRole;
    private RouteRequest currentRequest;
    private RouteResponse response;
    private List<StopDTO> stops;

    @Given("que soy un usuario autenticado con rol {string}")
    public void que_soy_un_usuario_autenticado_con_rol(String role) {
        this.currentUserRole = role;
        this.stops = new ArrayList<>();
    }

    @When("envío una petición para crear una ruta de {string} a {string} con precio {double}")
    public void envio_una_peticion_para_crear_una_ruta(String origin, String destination, Double price) {
        this.currentRequest = new RouteRequest(origin, destination, price, 45, stops);
    }

    @And("incluyo {int} paraderos en la petición")
    public void incluyo_paraderos_en_la_peticion(Integer numStops) {
        for (int i = 0; i < numStops; i++) {
            stops.add(new StopDTO(null, "Paradero " + i, -12.0, -77.0, "Av. Lima", i + 1));
        }
        this.currentRequest = new RouteRequest(
                currentRequest.originDistrict(),
                currentRequest.destinationDistrict(),
                currentRequest.price(),
                currentRequest.durationMin(),
                stops
        );
        this.response = manageRouteUseCase.createRoute(currentRequest);
    }

    @Then("la ruta debe ser creada exitosamente con código de estado {int}")
    public void la_ruta_debe_ser_creada_exitosamente_con_codigo_de_estado(Integer statusCode) {
        assertNotNull(response);
        assertNotNull(response.routeId());
    }

    @And("la ruta devuelta debe tener {int} paraderos asignados")
    public void la_ruta_devuelta_debe_tener_paraderos_asignados(Integer numStops) {
        assertNotNull(response.stops());
        assertEquals(numStops, response.stops().size());
    }
}

