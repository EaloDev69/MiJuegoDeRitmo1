/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Modelo;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;

/**
 * Direcciones de las flechas - SISTEMA DE CONVERGENCIA CENTRAL
 * ✅ TECLAS CORREGIDAS PARA ARRIBA Y ABAJO
 */
public enum Direccion {

    // ⭐ CONFIGURACIÓN CORREGIDA
    IZQUIERDA(
        new Vector3f(-1, 0, 0),
        ColorRGBA.Red,
        "assets/Texture/flecha_roja.png",
        270f,
        "izquierda"  // ✅ Nombre de mapeo
    ),

    ABAJO(
        new Vector3f(0, 1, 0),
        ColorRGBA.Blue,
        "assets/Texture/flecha_azul.png",
        180f,
        "abajo"  // ✅ Nombre de mapeo
    ),

    ARRIBA(
        new Vector3f(0, -1, 0),
        ColorRGBA.Blue,
        "assets/Texture/flecha_azul.png",
        0f,
        "arriba"  // ✅ CORREGIDO: antes era "w"
    ),

    DERECHA(
        new Vector3f(1, 0, 0),
        ColorRGBA.Red,
        "assets/Texture/flecha_roja.png",
        90f,
        "derecha"  // ✅ Nombre de mapeo
    ),

    ESPACIO(
        new Vector3f(0, 0, 0),
        ColorRGBA.White,
        "assets/Texture/flecha_especial_luna.png",
        0f,
        "espacio"  // ✅ Nombre de mapeo
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

    // ==================== GETTERS BÁSICOS ====================

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

    // ==================== LÓGICA DE DIRECCIÓN OPUESTA ====================

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

    // ==================== 🎯 TARGET CENTRAL ====================

    public Vector3f getPosicionTarget(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float centroY = altoVentana / 2;
        return new Vector3f(centroX, centroY, 0);
    }

    // ==================== 📍 SPAWN DESDE LOS LADOS ====================

    public Vector3f getPosicionSpawn(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float centroY = altoVentana / 2;
        float distanciaSpawn = 700f;

        switch(this) {
            case IZQUIERDA:
                return new Vector3f(centroX - distanciaSpawn, centroY, 0);
            case DERECHA:
                return new Vector3f(centroX + distanciaSpawn, centroY, 0);
            case ARRIBA:
                return new Vector3f(centroX, centroY + distanciaSpawn, 0);
            case ABAJO:
                return new Vector3f(centroX, centroY - distanciaSpawn, 0);
            case ESPACIO:
                return getSpawnAleatorioParaEspacio(centroX, centroY, distanciaSpawn);
            default:
                return new Vector3f(centroX - distanciaSpawn, centroY, 0);
        }
    }

    private Vector3f getSpawnAleatorioParaEspacio(float centroX, float centroY, float distancia) {
        int lado = (int)(Math.random() * 4);
        switch(lado) {
            case 0: return new Vector3f(centroX, centroY + distancia, 0);
            case 1: return new Vector3f(centroX, centroY - distancia, 0);
            case 2: return new Vector3f(centroX - distancia, centroY, 0);
            case 3: return new Vector3f(centroX + distancia, centroY, 0);
            default: return new Vector3f(centroX, centroY + distancia, 0);
        }
    }

    // ==================== 🎨 MÉTODOS DE UTILIDAD VISUAL ====================

    public float calcularRotacionHaciaCentro(Vector3f posicionActual, Vector3f posicionTarget) {
        Vector3f direccionAlCentro = posicionTarget.subtract(posicionActual).normalize();
        float anguloRadianes = (float) Math.atan2(direccionAlCentro.y, direccionAlCentro.x);
        float anguloGrados = (float) Math.toDegrees(anguloRadianes);
        return anguloGrados - 90f;
    }

    // ==================== 📊 MÉTODOS DE CLASIFICACIÓN ====================

    public boolean esEspecial() {
        return this == ESPACIO;
    }

    public static Direccion getAleatoria() {
        Direccion[] valores = {ARRIBA, ABAJO, IZQUIERDA, DERECHA};
        int index = (int)(Math.random() * valores.length);
        return valores[index];
    }

    public static Direccion getAleatoriaConEspacio() {
        Direccion[] valores = {ARRIBA, ABAJO, IZQUIERDA, DERECHA, ESPACIO};
        int index = (int)(Math.random() * valores.length);
        return valores[index];
    }

    public boolean esVertical() {
        return this == ARRIBA || this == ABAJO;
    }

    public boolean esHorizontal() {
        return this == IZQUIERDA || this == DERECHA;
    }

    // ==================== 🔧 DEBUG Y UTILIDADES ====================

    @Override
    public String toString() {
        switch(this) {
            case ARRIBA: return "↑ ARRIBA (W/↑)";
            case ABAJO: return "↓ ABAJO (S/↓)";
            case IZQUIERDA: return "← IZQUIERDA (A/←)";
            case DERECHA: return "→ DERECHA (D/→)";
            case ESPACIO: return "☾ LUNA (ESPACIO)";
            default: return super.toString();
        }
    }

    public String getInfoDetallada() {
        return String.format(
            "Direccion[%s, tecla=%s, rotacion=%.1f°, color=%s]",
            this.name(),
            tecla,
            rotacion,
            color.toString()
        );
    }

    public String getSimbolo() {
        switch(this) {
            case ARRIBA: return "↑";
            case ABAJO: return "↓";
            case IZQUIERDA: return "←";
            case DERECHA: return "→";
            case ESPACIO: return "☾";
            default: return "?";
        }
    }

    public String getNombreTecla() {
        switch(this) {
            case ARRIBA: return "W/↑";
            case ABAJO: return "S/↓";
            case IZQUIERDA: return "A/←";
            case DERECHA: return "D/→";
            case ESPACIO: return "ESPACIO";
            default: return tecla;
        }
    }
}