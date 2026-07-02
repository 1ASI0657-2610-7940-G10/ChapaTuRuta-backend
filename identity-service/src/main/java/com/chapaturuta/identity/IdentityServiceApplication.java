package com.chapaturuta.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE identity_schema.users ADD COLUMN IF NOT EXISTS route_id UUID;");
                System.out.println("SQL Migration Success: added route_id to identity_schema.users");
            } catch (Exception e) {
                System.err.println("SQL Migration Error (identity_schema.users): " + e.getMessage());
            }
            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS route_id UUID;");
                System.out.println("SQL Migration Success: added route_id to public.users");
            } catch (Exception e) {
                System.err.println("SQL Migration Error (public.users): " + e.getMessage());
            }
        };
    }
}
