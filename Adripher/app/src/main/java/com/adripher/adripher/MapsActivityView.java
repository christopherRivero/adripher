package com.adripher.adripher;

/**
 * Created by jmercadal on 10/03/2016.
 */
public interface MapsActivityView {
    /**
     * Metodo para inicializar mapa
     */
    void iniciarMapa();

    /**
     * Geolocaliza posicion en mapa del dispositivo
     * @param direccion
     * @return
     */
    String geolocalaiza(String direccion);
}
