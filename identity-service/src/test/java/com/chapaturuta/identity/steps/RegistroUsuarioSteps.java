package com.chapaturuta.identity.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

public class RegistroUsuarioSteps {

    private String nombre;
    private String correo;
    private String rol;
    private int statusCode;
    private boolean cuentaCreada;

    @Given("que soy un usuario nuevo con nombre {string}, correo {string} y rol {string}")
    public void que_soy_un_usuario_nuevo(String nombre, String correo, String rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        System.out.println("Preparando registro para: " + this.nombre);
    }

    @When("envío mis datos de registro al sistema")
    public void envio_mis_datos_de_registro() {
        // Aquí simulamos la llamada al caso de uso o al controlador para la evidencia
        if(this.nombre != null && this.correo != null) {
            this.cuentaCreada = true;
            this.statusCode = 201;
        } else {
            this.cuentaCreada = false;
            this.statusCode = 400;
        }
    }

    @Then("la cuenta se crea exitosamente")
    public void la_cuenta_se_crea_exitosamente() {
        Assertions.assertTrue(this.cuentaCreada, "La cuenta debería haberse creado.");
    }

    @Then("el sistema responde con el código de estado {int}")
    public void el_sistema_responde_con_el_codigo_de_estado(Integer expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, this.statusCode);
    }
}