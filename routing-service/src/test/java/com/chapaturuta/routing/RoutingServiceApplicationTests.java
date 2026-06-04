package com.chapaturuta.routing;

import com.chapaturuta.routing.application.usecase.ManageRouteUseCase;
import com.chapaturuta.routing.application.usecase.SearchRoutesUseCase;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import com.chapaturuta.routing.interfaces.rest.RouteController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("RoutingServiceApplication - Integration Tests")
class RoutingServiceApplicationTests {

    @Autowired
    private RouteController routeController;

    @Autowired
    private SearchRoutesUseCase searchRoutesUseCase;

    @Autowired
    private ManageRouteUseCase manageRouteUseCase;

    @Autowired
    private RouteRepository routeRepository;

    @Test
    @DisplayName("El contexto debe cargar correctamente")
    void contextLoads() {
        assertThat(routeController).isNotNull();
    }

    @Test
    @DisplayName("SearchRoutesUseCase debe estar disponible")
    void searchRoutesUseCaseIsAvailable() {
        assertThat(searchRoutesUseCase).isNotNull();
    }

    @Test
    @DisplayName("ManageRouteUseCase debe estar disponible")
    void manageRouteUseCaseIsAvailable() {
        assertThat(manageRouteUseCase).isNotNull();
    }

    @Test
    @DisplayName("RouteRepository debe estar disponible")
    void routeRepositoryIsAvailable() {
        assertThat(routeRepository).isNotNull();
    }

    @Test
    @DisplayName("RouteController debe estar disponible")
    void routeControllerIsAvailable() {
        assertThat(routeController).isNotNull();
    }

}
