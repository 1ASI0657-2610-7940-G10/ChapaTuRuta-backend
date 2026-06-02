package com.chapaturuta.identity.domain.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Company {
    private UUID id;
    private String name;
    private String busPhotoUrl;
    private String ruc;
    private UUID managerId;
}