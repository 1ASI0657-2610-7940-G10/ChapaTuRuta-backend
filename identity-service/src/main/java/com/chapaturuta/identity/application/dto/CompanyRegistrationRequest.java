package com.chapaturuta.identity.application.dto;
import java.util.UUID;

public record CompanyRegistrationRequest(String name, String ruc, String busPhotoUrl, UUID managerId) {}