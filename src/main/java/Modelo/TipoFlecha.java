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
    NORMAL,
    RAPIDA,
    DORADA,
    LUNA; // ⭐ NUEVO: Tipo especial para flecha de luna

    public float getMultiplicadorPuntos() {
        switch(this) {
            case NORMAL: return 1.0f;
            case RAPIDA: return 1.5f;
            case DORADA: return 2.0f;
            case LUNA: return 3.0f; // ⭐ ¡Más puntos por ser especial!
            default: return 1.0f;
        }
    }

    public float getVelocidadBase() {
        switch(this) {
            case NORMAL: return 200.0f;
            case RAPIDA: return 250.0f;
            case DORADA: return 180.0f;
            case LUNA: return 150.0f; // ⭐ Más lenta para que sea más fácil
            default: return 200.0f;
        }
    }

    public boolean seInvierte() {
        return this == DORADA;
    }

    public boolean esEspecial() {
        return this == LUNA;
    }
}