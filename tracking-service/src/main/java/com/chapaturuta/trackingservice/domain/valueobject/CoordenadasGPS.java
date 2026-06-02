package com.chapaturuta.trackingservice.domain.valueobject;

public record CoordenadasGPS(Double latitude, Double longitude) {

    public CoordenadasGPS {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("La latitud y longitud no pueden ser nulas");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90 grados");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180 grados");
        }
    }

    // Aquí podríamos agregar en el futuro métodos del dominio, como:
    // public double calcularDistancia(CoordenadasGPS otraCoordenada) { ... }
}