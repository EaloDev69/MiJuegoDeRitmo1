/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
// ==================== FRAGMENTO DE Direccion.java ====================
// Reemplaza la definición del enum con estas rutas actualizadas:

package Modelo;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;

/**
 * Enum de direcciones con ROTACIONES INVERTIDAS
 * Las flechas ahora apuntan hacia DONDE VIENEN, no hacia donde van
 * 
 * Ejemplo: Una flecha que viene desde la IZQUIERDA apunta hacia la DERECHA (→)
 */
public enum Direccion {
    // ⭐ ROTACIONES INVERTIDAS - Las flechas apuntan hacia su origen
    IZQUIERDA(
        new Vector3f(-1, 0, 0),
        ColorRGBA.Red,
        "assets/Texture/flecha_roja.png",
        90f,  // ← Apunta DERECHA (→) porque viene de la izquierda
        "a"
    ),
    ABAJO(
        new Vector3f(0, 1, 0),
        ColorRGBA.Blue,
        "assets/Texture/flecha_azul.png",
        0f,   // ← Apunta ARRIBA (↑) porque viene de abajo
        "abajo"
    ),
    ARRIBA(
        new Vector3f(0, -1, 0),
        ColorRGBA.Blue,
        "assets/Texture/flecha_azul.png",
        180f, // ← Apunta ABAJO (↓) porque viene de arriba
        "w"
    ),
    DERECHA(
        new Vector3f(1, 0, 0),
        ColorRGBA.Red,
        "assets/Texture/flecha_roja.png",
        270f, // ← Apunta IZQUIERDA (←) porque viene de la derecha
        "derecha"
    ),
    ESPACIO(
        new Vector3f(0, 0, 0),
        ColorRGBA.White,
        "assets/Texture/flecha_especial_luna.png",
        0f,   // ← La flecha de luna se genera aleatoriamente
        "espacio"
    );

    // ==================== ATRIBUTOS ====================
    private final Vector3f direccion;
    private final ColorRGBA color;
    private final String texturePath;
    private final float rotacion;
    private final String tecla;

    // ==================== CONSTRUCTOR ====================
    Direccion(Vector3f dir, ColorRGBA color, String texturePath, float rotacion, String tecla) {
        this.direccion = dir;
        this.color = color;
        this.texturePath = texturePath;
        this.rotacion = rotacion;
        this.tecla = tecla;
    }

    // ==================== GETTERS ====================
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

    // ==================== DIRECCIÓN OPUESTA ====================
    /**
     * Obtiene la dirección opuesta
     * Usado para la mecánica de flechas doradas invertidas
     */
    public Direccion getOpuesta() {
        switch(this) {
            case ARRIBA:
                return ABAJO;
            case ABAJO:
                return ARRIBA;
            case IZQUIERDA:
                return DERECHA;
            case DERECHA:
                return IZQUIERDA;
            case ESPACIO:
                return ESPACIO; // La flecha especial no tiene opuesta
            default:
                return this;
        }
    }

    // ==================== POSICIONES ====================
    /**
     * Posición target (centro de la pantalla donde está el astronauta)
     */
    /**
 * ⭐ CORREGIDO: Devuelve la posición de la zona de impacto correspondiente
 */
public Vector3f getPosicionTarget(float anchoVentana, float altoVentana) {
    // Estos valores deben coincidir EXACTAMENTE con setupZonasImpacto()
    float posBaseY = (altoVentana / 4) + 50f;
    
    float tamañoZonaFlecha = 25f;
    float espacioEntreFlechas = 15f;
    
    // Calcular el punto de inicio X para centrar el bloque de 4 flechas
    float anchoTotalFlechas = (tamañoZonaFlecha * 4) + (espacioEntreFlechas * 3);
    float inicioX = (anchoVentana / 2) - (anchoTotalFlechas / 2) + (tamañoZonaFlecha / 2);
    
    switch(this) {
        case IZQUIERDA:
            return new Vector3f(inicioX, posBaseY, 0);
            
        case ABAJO:
            return new Vector3f(inicioX + tamañoZonaFlecha + espacioEntreFlechas, posBaseY, 0);
            
        case ARRIBA:
            return new Vector3f(inicioX + (tamañoZonaFlecha + espacioEntreFlechas) * 2, posBaseY, 0);
            
        case DERECHA:
            return new Vector3f(inicioX + (tamañoZonaFlecha + espacioEntreFlechas) * 3, posBaseY, 0);
            
        case ESPACIO:
            float tamañoZonaEspacio = 100f;
            return new Vector3f(anchoVentana / 2, posBaseY + tamañoZonaFlecha + 40f, 0);
            
        default:
            return new Vector3f(anchoVentana / 2, altoVentana / 2, 0);
    }
}
    /**
     * Posición de spawn (donde aparece la flecha)
     */
    public Vector3f getPosicionSpawn(float anchoVentana, float altoVentana) {
    // Obtener la posición del target (donde están las cajas)
    Vector3f target = getPosicionTarget(anchoVentana, altoVentana);
    
    float distanciaSpawn = 700f; // Distancia desde el target
    
    switch(this) {
        case IZQUIERDA:
            // Spawn a la IZQUIERDA del target → se mueve a la DERECHA
            return new Vector3f(target.x - distanciaSpawn, target.y, 0);
            
        case DERECHA:
            // Spawn a la DERECHA del target → se mueve a la IZQUIERDA
            return new Vector3f(target.x + distanciaSpawn, target.y, 0);
            
        case ARRIBA:
            // Spawn ARRIBA del target → se mueve hacia ABAJO
            return new Vector3f(target.x, target.y + distanciaSpawn, 0);
            
        case ABAJO:
            // Spawn ABAJO del target → se mueve hacia ARRIBA
            return new Vector3f(target.x, target.y - distanciaSpawn, 0);
            
        case ESPACIO:
            // Spawn aleatorio desde cualquier borde
            return getSpawnAleatorioParaEspacio(target.x, target.y, distanciaSpawn);
            
        default:
            return new Vector3f(target.x - distanciaSpawn, target.y, 0);
    }
}

    /**
     * Spawn aleatorio para la flecha ESPACIO (puede venir de cualquier lado)
     */
    private Vector3f getSpawnAleatorioParaEspacio(float centroX, float centroY, float distancia) {
        int lado = (int)(Math.random() * 4);
        switch(lado) {
            case 0: return new Vector3f(centroX, centroY + distancia, 0);  // Arriba
            case 1: return new Vector3f(centroX, centroY - distancia, 0);  // Abajo
            case 2: return new Vector3f(centroX - distancia, centroY, 0);  // Izquierda
            case 3: return new Vector3f(centroX + distancia, centroY, 0);  // Derecha
            default: return new Vector3f(centroX, centroY + distancia, 0);
        }
    }

    // ==================== UTILIDADES ====================
    public boolean esEspecial() {
        return this == ESPACIO;
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