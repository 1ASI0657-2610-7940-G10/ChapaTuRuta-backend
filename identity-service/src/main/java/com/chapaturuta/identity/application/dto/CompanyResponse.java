package com.chapaturuta.identity.application.dto;
import java.util.UUID;

public record CompanyResponse(UUID id, String name, String ruc) {}