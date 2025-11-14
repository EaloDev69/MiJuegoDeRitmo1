/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Modelo;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;

/**
 * Direcciones de las flechas - SISTEMA DE CONVERGENCIA A LOS LADOS DEL OBJETO CENTRAL
 * 
 * ✅ Cada flecha viene desde el BORDE de la pantalla hacia un LADO del objeto central:
 *    - IZQUIERDA: desde borde izquierdo → centro del lado izquierdo del cuadrado
 *    - DERECHA: desde borde derecho → centro del lado derecho del cuadrado
 *    - ARRIBA: desde borde superior → centro del lado superior del cuadrado
 *    - ABAJO: desde borde inferior → centro del lado inferior del cuadrado
 *    - ESPACIO: ⚠️ NO USA FLECHAS - Mecánica especial de titilación del objeto central
 *               El objeto titila 2 veces (0.5s entre cada titilación) para avisar
 *               que el jugador debe presionar ESPACIO siguiendo el ritmo
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public enum Direccion {
    
    IZQUIERDA(
        new Vector3f(-1, 0, 0),
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
        new Vector3f(0, -1, 0),
        ColorRGBA.Blue, 
        "assets/Texture/flecha_azul.png", 
        0f,
        "w"
    ),
    
    DERECHA(
        new Vector3f(1, 0, 0),
        ColorRGBA.Red, 
        "assets/Texture/flecha_roja.png", 
        90f,
        "derecha"
    ),
    
    ESPACIO(
        new Vector3f(0, 0, 0),
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

    // ==================== 🎯 TARGET EN LOS LADOS DEL OBJETO CENTRAL ====================
    
    /**
     * ⭐ CORREGIDO: Cada flecha va al CENTRO de un LADO del objeto central
     * - IZQUIERDA: va al centro del lado izquierdo del cuadrado
     * - DERECHA: va al centro del lado derecho del cuadrado
     * - ARRIBA: va al centro del lado superior del cuadrado
     * - ABAJO: va al centro del lado inferior del cuadrado
     * - ESPACIO: NO USA FLECHAS - El objeto central titila como advertencia
     * 
     * @param anchoVentana Ancho de la ventana en píxeles
     * @param altoVentana Alto de la ventana en píxeles
     * @return Posición del lado correspondiente del objeto central
     */
    public Vector3f getPosicionTarget(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float centroY = altoVentana / 2;
        
        // Distancia desde el centro hasta el borde del objeto central
        // Objeto central tiene tamaño 150px, entonces radio = 75px
        float offsetTarget = 75f;
        
        switch(this) {
            case IZQUIERDA:
                // Target en el centro del lado IZQUIERDO del cuadrado
                return new Vector3f(centroX - offsetTarget, centroY, 0);
                
            case DERECHA:
                // Target en el centro del lado DERECHO del cuadrado
                return new Vector3f(centroX + offsetTarget, centroY, 0);
                
            case ARRIBA:
                // Target en el centro del lado SUPERIOR del cuadrado
                return new Vector3f(centroX, centroY + offsetTarget, 0);
                
            case ABAJO:
                // Target en el centro del lado INFERIOR del cuadrado
                return new Vector3f(centroX, centroY - offsetTarget, 0);
                
            case ESPACIO:
                // ⚠️ ESPACIO NO USA FLECHAS - retorna centro para compatibilidad
                // pero la mecánica real es: objeto central titila 2 veces
                return new Vector3f(centroX, centroY, 0);
                
            default:
                return new Vector3f(centroX, centroY, 0);
        }
    }

    // ==================== 📍 SPAWN DESDE LOS BORDES DE LA PANTALLA ====================
    
    /**
     * ⭐ CORREGIDO: Spawn desde el BORDE de la pantalla hacia el objeto central
     * Todas las flechas vienen desde fuera de la ventana
     * ⚠️ ESPACIO NO USA FLECHAS - el objeto central titila como advertencia
     * 
     * @param anchoVentana Ancho de la ventana en píxeles
     * @param altoVentana Alto de la ventana en píxeles
     * @return Posición inicial de spawn en el borde de la pantalla
     */
    public Vector3f getPosicionSpawn(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float centroY = altoVentana / 2;
        
        switch(this) {
            case IZQUIERDA:
                // Spawn en el BORDE IZQUIERDO de la pantalla, mismo Y que el centro
                return new Vector3f(0, centroY, 0);
                
            case DERECHA:
                // Spawn en el BORDE DERECHO de la pantalla, mismo Y que el centro
                return new Vector3f(anchoVentana, centroY, 0);
                
            case ARRIBA:
                // Spawn en el BORDE SUPERIOR de la pantalla, mismo X que el centro
                return new Vector3f(centroX, altoVentana, 0);
                
            case ABAJO:
                // Spawn en el BORDE INFERIOR de la pantalla, mismo X que el centro
                return new Vector3f(centroX, 0, 0);
                
            case ESPACIO:
                // ⚠️ ESPACIO NO USA FLECHAS - retorna posición fuera de pantalla
                // para compatibilidad, pero la mecánica real es diferente:
                // El objeto central titila 2 veces con 0.5s de diferencia
                return new Vector3f(-1000, -1000, 0); // Fuera de vista
                
            default:
                return new Vector3f(0, centroY, 0);
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
            case ARRIBA: return "↑ ARRIBA (W)";
            case ABAJO: return "↓ ABAJO (↓)";
            case IZQUIERDA: return "← IZQUIERDA (A)";
            case DERECHA: return "→ DERECHA (→)";
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
            case ARRIBA: return "W";
            case ABAJO: return "↓";
            case IZQUIERDA: return "A";
            case DERECHA: return "→";
            case ESPACIO: return "ESPACIO";
            default: return tecla;
        }
    }
}