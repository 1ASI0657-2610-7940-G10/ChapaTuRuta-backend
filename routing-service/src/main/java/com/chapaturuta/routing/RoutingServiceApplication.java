package com.chapaturuta.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class RoutingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingServiceApplication.class, args);
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnBean(JdbcTemplate.class)
    public CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS route_stops (" +
                    "  id UUID PRIMARY KEY, " +
                    "  name VARCHAR(255) NOT NULL, " +
                    "  latitude DOUBLE PRECISION NOT NULL, " +
                    "  longitude DOUBLE PRECISION NOT NULL, " +
                    "  address VARCHAR(255), " +
                    "  stop_order INTEGER NOT NULL, " +
                    "  route_id UUID NOT NULL, " +
                    "  CONSTRAINT fk_route_stops_routes FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE" +
                    ");"
                );
                System.out.println("SQL Migration Success: created route_stops table if not exists");
            } catch (Exception e) {
                System.err.println("SQL Migration Error (route_stops): " + e.getMessage());
            }
        };
    }
}
