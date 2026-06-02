package com.chapaturuta.trackingservice.domain.valueobject;

public record CoordenadasGPS(Double latitude, Double longitude) {

    private static final double RADIO_TIERRA_KM = 6371.0;

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

    /**
     * Calcula la distancia en metros entre esta coordenada y otra coordenada objetivo.
     * Utiliza la fórmula de Haversine, ideal para integraciones con Mapbox y GPS real.
     * * @param destino La coordenada hacia donde se mide la distancia
     * @return La distancia en metros (m)
     */
    public double calcularDistanciaEnMetros(CoordenadasGPS destino) {
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(destino.latitude());
        double lon1Rad = Math.toRadians(this.longitude);
        double lon2Rad = Math.toRadians(destino.longitude());

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.pow(Math.sin(deltaLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanciaKm = RADIO_TIERRA_KM * c;

        return distanciaKm * 1000;
    }
}