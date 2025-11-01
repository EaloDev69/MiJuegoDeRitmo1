/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 * Enum que define los tres tipos de flechas en el juego
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public enum TipoFlecha {
    /**
     * Flechas normales - Se mueven en línea recta
     * Aparecen en beats regulares
     */
    NORMAL,
    
    /**
     * Flechas rápidas/especiales - Aparecen en secuencias rápidas
     * Requieren timing más preciso
     */
    RAPIDA,
    
    /**
     * Flechas doradas - Se invierten a mitad de camino (estilo Undyne)
     * Dan más puntos, aparecen en beats intensos
     */
    DORADA;
    
    /**
     * Obtiene el multiplicador de puntos según el tipo
     */
    public float getMultiplicadorPuntos() {
        switch(this) {
            case NORMAL: return 1.0f;
            case RAPIDA: return 1.5f;
            case DORADA: return 2.0f;
            default: return 1.0f;
        }
    }
    
    /**
     * Obtiene la velocidad base según el tipo
     */
    public float getVelocidadBase() {
        switch(this) {
            case NORMAL: return 200.0f;
            case RAPIDA: return 250.0f;
            case DORADA: return 180.0f; // Más lenta para dar tiempo a ver la inversión
            default: return 200.0f;
        }
    }
    
    /**
     * Verifica si este tipo se invierte
     */
    public boolean seInvierte() {
        return this == DORADA;
    }
}