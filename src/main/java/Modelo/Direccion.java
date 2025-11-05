/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;

/**
 * Direcciones de las flechas - RUTAS CORREGIDAS
 * TODAS las flechas vienen desde ABAJO hacia los targets ARRIBA
 */
public enum Direccion {
    
    // ⭐ RUTAS CORREGIDAS según tu estructura: assets/Texture/
    IZQUIERDA(
        new Vector3f(0, 1, 0), 
        ColorRGBA.Red, 
        "assets/Texture/flecha_roja.png", 
        270f, 
        "a"
    ),
    
    ABAJO(
        new Vector3f(0, 1, 0), 
        ColorRGBA.Blue, 
        "assets/Texture/flecha_azul.png", 
        180f, 
        "abajo"
    ),
    
    ARRIBA(
        new Vector3f(0, 1, 0), 
        ColorRGBA.Blue, 
        "assets/Texture/flecha_azul.png", 
        0f, 
        "w"
    ),
    
    DERECHA(
        new Vector3f(0, 1, 0), 
        ColorRGBA.Red, 
        "assets/Texture/flecha_roja.png", 
        90f, 
        "derecha"
    ),
    
    ESPACIO(
        new Vector3f(0, 1, 0), 
        ColorRGBA.White, 
        "assets/Texture/flecha_especial_luna.png", 
        0f, 
        "espacio"
    );

    private final Vector3f direccion;
    private final ColorRGBA color;
    private final String texturePath;
    private final float rotacion;
    private final String tecla;

    Direccion(Vector3f dir, ColorRGBA color, String texturePath, float rotacion, String tecla) {
        this.direccion = dir;
        this.color = color;
        this.texturePath = texturePath;
        this.rotacion = rotacion;
        this.tecla = tecla;
    }

    public Vector3f getDireccion() {
        return direccion.clone();
    }

    public ColorRGBA getColor() {
        return color.clone();
    }

    public String getTexturePath() {
        return texturePath;
    }

    public float getRotacion() {
        return rotacion;
    }

    public String getTecla() {
        return tecla;
    }

    public Direccion getOpuesta() {
        switch(this) {
            case ARRIBA: return ABAJO;
            case ABAJO: return ARRIBA;
            case IZQUIERDA: return DERECHA;
            case DERECHA: return IZQUIERDA;
            case ESPACIO: return ESPACIO;
            default: return this;
        }
    }

    public Vector3f getPosicionTarget(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float espaciado = 100f;
        float targetY = altoVentana - 120f; // Arriba
        
        switch(this) {
            case IZQUIERDA:
                return new Vector3f(centroX - espaciado * 1.5f, targetY, 0);
            case ABAJO:
                return new Vector3f(centroX - espaciado * 0.5f, targetY, 0);
            case ARRIBA:
                return new Vector3f(centroX + espaciado * 0.5f, targetY, 0);
            case DERECHA:
                return new Vector3f(centroX + espaciado * 1.5f, targetY, 0);
            case ESPACIO:
                return new Vector3f(centroX, 120f, 0); // Luna abajo
            default:
                return new Vector3f(centroX, altoVentana / 2, 0);
        }
    }


    public Vector3f getPosicionSpawn(float anchoVentana, float altoVentana) {
        Vector3f target = getPosicionTarget(anchoVentana, altoVentana);
        // Spawn 150px ABAJO de la pantalla
        return new Vector3f(target.x, -150f, 0);
    }

    public boolean esEspecial() {
        return this == ESPACIO;
    }

    public static Direccion getAleatoria() {
        Direccion[] valores = {ARRIBA, ABAJO, IZQUIERDA, DERECHA};
        int index = (int)(Math.random() * valores.length);
        return valores[index];
    }

    public boolean esVertical() {
        return this == ARRIBA || this == ABAJO;
    }

    public boolean esHorizontal() {
        return this == IZQUIERDA || this == DERECHA;
    }

    @Override
    public String toString() {
        switch(this) {
            case ARRIBA: return "↑ ARRIBA (W)";
            case ABAJO: return "↓ ABAJO (↓)";
            case IZQUIERDA: return "← IZQUIERDA (A)";
            case DERECHA: return "→ DERECHA (→)";
            case ESPACIO: return "☾ LUNA (ESPACIO)";
            default: return super.toString();
        }
    }
}

