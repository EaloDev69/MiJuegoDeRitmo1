/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de URLs de assets remotos
 */
public class AssetURLConfig {
    
    // Base URL de tu API
    private static final String API_BASE_URL = "https://tu-api.com/api";
    
    // Endpoints de sprites
    public static final class Protagonista {
        public static final String DEFAULT = API_BASE_URL + "/sprites/protagonista/default.png";
        public static final String ARRIBA = API_BASE_URL + "/sprites/protagonista/up.png";
        public static final String ABAJO = API_BASE_URL + "/sprites/protagonista/down.png";
        public static final String IZQUIERDA = API_BASE_URL + "/sprites/protagonista/left.png";
        public static final String DERECHA = API_BASE_URL + "/sprites/protagonista/right.png";
    }
    
    public static final class Fondos {
        public static final String ESPACIO = API_BASE_URL + "/backgrounds/espacio.png";
        public static final String MENU = API_BASE_URL + "/backgrounds/menu.png";
    }
    
    public static final class Flechas {
        public static final String ROJA = API_BASE_URL + "/arrows/red.png";
        public static final String AZUL = API_BASE_URL + "/arrows/blue.png";
        public static final String DORADA = API_BASE_URL + "/arrows/golden.png";
        public static final String LUNA = API_BASE_URL + "/arrows/moon.png";
    }
    
    // Fallbacks locales
    public static final Map<String, String> FALLBACKS = new HashMap<String, String>() {{
        put(Protagonista.DEFAULT, "assets/Texture/Protagonista/astronautaPoseDefault.png");
        put(Fondos.ESPACIO, "assets/Texture/espacio.png");
        put(Flechas.ROJA, "assets/Texture/flecha_roja.png");
        // ... más fallbacks
    }};
    
    /**
     * Obtiene el fallback para una URL
     */
    public static String getFallback(String url) {
        return FALLBACKS.getOrDefault(url, null);
    }
}