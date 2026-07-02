package com.chapaturuta.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class DatabaseConfig {

    @Bean
    public CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE identity_schema.users ADD COLUMN IF NOT EXISTS route_id UUID;");
                System.out.println("SQL Migration Success: added route_id to identity_schema.users");
            } catch (Exception e) {
                System.err.println("SQL Migration Error (identity_schema.users): " + e.getMessage());
                try {
                    jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS route_id UUID;");
                    System.out.println("SQL Migration Success: added route_id to public.users");
                } catch (Exception ex) {
                    System.err.println("SQL Migration Error (public.users): " + ex.getMessage());
                }
            }
        };
    }
}

