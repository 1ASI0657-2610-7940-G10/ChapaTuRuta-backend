package com.chapaturuta.identity.steps;

import com.chapaturuta.identity.IdentityServiceApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = IdentityServiceApplication.class)
public class CucumberSpringConfiguration {

}