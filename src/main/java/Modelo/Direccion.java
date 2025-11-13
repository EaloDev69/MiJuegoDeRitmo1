/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;

/**
 * Direcciones de las flechas - SISTEMA DE CONVERGENCIA CENTRAL CON ZONAS DE IMPACTO
 * 
 * ✅ TODAS las flechas convergen al CENTRO de la pantalla
 * ✅ Spawn desde ZONAS DE IMPACTO visibles en los bordes
 * ✅ Cada flecha spawn desde su lado correspondiente:
 *    - ARRIBA: spawn desde arriba, baja al centro
 *    - ABAJO: spawn desde abajo, sube al centro
 *    - IZQUIERDA: spawn desde izquierda, va al centro
 *    - DERECHA: spawn desde derecha, va al centro
 *    - ESPACIO: spawn aleatorio, va al centro
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public enum Direccion {
    
    // ⭐ CONFIGURACIÓN DE FLECHAS CON RUTAS CORREGIDAS
    IZQUIERDA(
        new Vector3f(-1, 0, 0),  // Vector dirección: hacia la derecha (al centro)
        ColorRGBA.Red, 
        "assets/Texture/flecha_roja.png", 
        270f,  // Rotación para apuntar a la derecha
        "a"
    ),
    
    ABAJO(
        new Vector3f(0, 1, 0),   // Vector dirección: hacia arriba (al centro)
        ColorRGBA.Blue, 
        "assets/Texture/flecha_azul.png", 
        180f,  // Rotación para apuntar arriba
        "abajo"
    ),
    
    ARRIBA(
        new Vector3f(0, -1, 0),  // Vector dirección: hacia abajo (al centro)
        ColorRGBA.Blue, 
        "assets/Texture/flecha_azul.png", 
        0f,    // Sin rotación (apunta abajo naturalmente)
        "w"
    ),
    
    DERECHA(
        new Vector3f(1, 0, 0),   // Vector dirección: hacia la izquierda (al centro)
        ColorRGBA.Red, 
        "assets/Texture/flecha_roja.png", 
        90f,   // Rotación para apuntar a la izquierda
        "derecha"
    ),
    
    ESPACIO(
        new Vector3f(0, 0, 0),   // Sin dirección específica (spawn aleatorio)
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
    
    // ⭐ NUEVO: Margen ampliado para mejor tiempo de reacción
    private static final float MARGEN_ZONA = 150f; // Donde están las zonas visuales
    private static final float DISTANCIA_SPAWN = 1500f; // ⭐ MUY LEJOS - Fuera de pantalla visible

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
    
    /**
     * ⭐ CRÍTICO: TODAS las flechas van al MISMO punto central
     * Este es el objetivo donde el jugador debe presionar la tecla
     * 
     * @param anchoVentana Ancho de la ventana en píxeles
     * @param altoVentana Alto de la ventana en píxeles
     * @return Posición del centro de la pantalla
     */
    public Vector3f getPosicionTarget(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float centroY = altoVentana / 2;
        
        // ⭐ TODAS convergen al MISMO punto central
        return new Vector3f(centroX, centroY, 0);
    }

    // ==================== 📍 SPAWN DESDE LAS ZONAS DE IMPACTO ====================
    
    /**
     * ⭐ MEJORADO: Spawn más alejado para mejor tiempo de reacción
     * Las flechas aparecen desde los bordes, más allá de las zonas de impacto
     * 
     * @param anchoVentana Ancho de la ventana en píxeles
     * @param altoVentana Alto de la ventana en píxeles
     * @return Posición inicial de spawn alejada del centro
     */
    public Vector3f getPosicionSpawn(float anchoVentana, float altoVentana) {
        float centroX = anchoVentana / 2;
        float centroY = altoVentana / 2;
        
        switch(this) {
            case IZQUIERDA:
                // Spawn a la IZQUIERDA, fuera de pantalla
                return new Vector3f(centroX - DISTANCIA_SPAWN, centroY, 0);
                
            case DERECHA:
                // Spawn a la DERECHA, fuera de pantalla
                return new Vector3f(centroX + DISTANCIA_SPAWN, centroY, 0);
                
            case ARRIBA:
                // Spawn ARRIBA, fuera de pantalla
                return new Vector3f(centroX, centroY + DISTANCIA_SPAWN, 0);
                
            case ABAJO:
                // Spawn ABAJO, fuera de pantalla
                return new Vector3f(centroX, centroY - DISTANCIA_SPAWN, 0);
                
            case ESPACIO:
                // Flechas de espacio pueden venir de cualquier lado
                return getSpawnAleatorioParaEspacio(centroX, centroY, anchoVentana, altoVentana);
                
            default:
                return new Vector3f(centroX - DISTANCIA_SPAWN, centroY, 0);
        }
    }

    /**
     * ⭐ MEJORADO: Spawn aleatorio para ESPACIO con mayor distancia
     */
    private Vector3f getSpawnAleatorioParaEspacio(float centroX, float centroY, float ancho, float alto) {
        int lado = (int)(Math.random() * 4); // 0-3
        
        switch(lado) {
            case 0: // Desde arriba
                return new Vector3f(centroX, centroY + DISTANCIA_SPAWN, 0);
            case 1: // Desde abajo
                return new Vector3f(centroX, centroY - DISTANCIA_SPAWN, 0);
            case 2: // Desde izquierda
                return new Vector3f(centroX - DISTANCIA_SPAWN, centroY, 0);
            case 3: // Desde derecha
                return new Vector3f(centroX + DISTANCIA_SPAWN, centroY, 0);
            default:
                return new Vector3f(centroX, centroY + DISTANCIA_SPAWN, 0);
        }
    }

    // ==================== 🎨 MÉTODOS DE UTILIDAD VISUAL ====================

    /**
     * Calcula la rotación necesaria para que la flecha apunte hacia el centro
     * desde su posición actual
     * 
     * @param posicionActual Posición actual de la flecha
     * @param posicionTarget Posición del centro (target)
     * @return Ángulo en grados para rotar la flecha
     */
    public float calcularRotacionHaciaCentro(Vector3f posicionActual, Vector3f posicionTarget) {
        // Vector desde la flecha hacia el centro
        Vector3f direccionAlCentro = posicionTarget.subtract(posicionActual).normalize();
        
        // Calcular ángulo en radianes
        float anguloRadianes = (float) Math.atan2(direccionAlCentro.y, direccionAlCentro.x);
        
        // Convertir a grados y ajustar
        float anguloGrados = (float) Math.toDegrees(anguloRadianes);
        
        // Ajuste para que la flecha apunte correctamente
        // (depende de cómo esté orientado el sprite original)
        return anguloGrados - 90f;
    }

    // ==================== 📊 MÉTODOS DE CLASIFICACIÓN ====================

    /**
     * Verifica si esta dirección es especial (ESPACIO)
     */
    public boolean esEspecial() {
        return this == ESPACIO;
    }

    /**
     * Genera una dirección aleatoria (excluyendo ESPACIO)
     */
    public static Direccion getAleatoria() {
        Direccion[] valores = {ARRIBA, ABAJO, IZQUIERDA, DERECHA};
        int index = (int)(Math.random() * valores.length);
        return valores[index];
    }

    /**
     * Genera una dirección aleatoria INCLUYENDO ESPACIO
     */
    public static Direccion getAleatoriaConEspacio() {
        Direccion[] valores = {ARRIBA, ABAJO, IZQUIERDA, DERECHA, ESPACIO};
        int index = (int)(Math.random() * valores.length);
        return valores[index];
    }

    /**
     * Verifica si la dirección es vertical (arriba/abajo)
     */
    public boolean esVertical() {
        return this == ARRIBA || this == ABAJO;
    }

    /**
     * Verifica si la dirección es horizontal (izquierda/derecha)
     */
    public boolean esHorizontal() {
        return this == IZQUIERDA || this == DERECHA;
    }

    // ==================== 🔧 DEBUG Y UTILIDADES ====================

    /**
     * Representación en texto legible
     */
    @Override
    public String toString() {
        switch(this) {
            case ARRIBA: return "↑ ARRIBA (W)";
            case ABAJO: return "↓ ABAJO (S/↓)";
            case IZQUIERDA: return "← IZQUIERDA (A/←)";
            case DERECHA: return "→ DERECHA (D/→)";
            case ESPACIO: return "☾ LUNA (ESPACIO)";
            default: return super.toString();
        }
    }

    /**
     * Información detallada para debugging
     */
    public String getInfoDetallada() {
        return String.format(
            "Direccion[%s, tecla=%s, rotacion=%.1f°, color=%s, zona=%.0fpx]",
            this.name(),
            tecla,
            rotacion,
            color.toString(),
            MARGEN_ZONA
        );
    }

    /**
     * Obtiene el símbolo Unicode de la flecha
     */
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

    /**
     * Obtiene el nombre de la tecla en formato legible
     */
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