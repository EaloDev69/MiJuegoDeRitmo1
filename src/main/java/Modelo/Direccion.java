/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;

/**
 * Enum que define las cuatro direcciones de las flechas
 * Incluye propiedades visuales y de posicionamiento
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public enum Direccion {
    ARRIBA(new Vector3f(0, -1, 0), ColorRGBA.Red, "Textures/flecha_arriba.png", 0f),
    ABAJO(new Vector3f(0, 1, 0), ColorRGBA.Blue, "Textures/flecha_abajo.png", 180f),
    IZQUIERDA(new Vector3f(1, 0, 0), ColorRGBA.Green, "Textures/flecha_izquierda.png", 270f),
    DERECHA(new Vector3f(-1, 0, 0), ColorRGBA.Yellow, "Textures/flecha_derecha.png", 90f);
    
    private final Vector3f direccion;
    private final ColorRGBA color;
    private final String texturePath;
    private final float rotacion; // Rotación en grados
    
    /**
     * Constructor del enum
     * 
     * @param dir Vector de dirección de movimiento
     * @param color Color asociado a esta dirección
     * @param texturePath Ruta de la textura (opcional)
     * @param rotacion Rotación del sprite en grados
     */
    Direccion(Vector3f dir, ColorRGBA color, String texturePath, float rotacion) {
        this.direccion = dir;
        this.color = color;
        this.texturePath = texturePath;
        this.rotacion = rotacion;
    }
    
    /**
     * Obtiene el vector de dirección de movimiento (clonado para seguridad)
     */
    public Vector3f getDireccion() {
        return direccion.clone();
    }
    
    /**
     * Obtiene el color asociado (clonado para seguridad)
     */
    public ColorRGBA getColor() {
        return color.clone();
    }
    
    /**
     * Obtiene la ruta de la textura
     */
    public String getTexturePath() {
        return texturePath;
    }
    
    /**
     * Obtiene la rotación en grados
     */
    public float getRotacion() {
        return rotacion;
    }
    
    /**
     * Obtiene la dirección opuesta (usada para flechas doradas cuando se invierten)
     * 
     * @return La dirección contraria
     */
    public Direccion getOpuesta() {
        switch(this) {
            case ARRIBA: return ABAJO;
            case ABAJO: return ARRIBA;
            case IZQUIERDA: return DERECHA;
            case DERECHA: return IZQUIERDA;
            default: return this;
        }
    }
    
    /**
     * Obtiene la posición de spawn (aparición) según la dirección
     * Las flechas aparecen fuera de la pantalla y se mueven hacia el centro
     * 
     * @param anchoVentana Ancho de la ventana del juego
     * @param altoVentana Alto de la ventana del juego
     * @return Vector3f con la posición de spawn
     */
    public Vector3f getPosicionSpawn(float anchoVentana, float altoVentana) {
        float margen = 150f; // Distancia fuera de la pantalla
        
        switch(this) {
            case ARRIBA:
                // Aparece arriba, se mueve hacia abajo
                return new Vector3f(anchoVentana / 2, altoVentana + margen, 0);
                
            case ABAJO:
                // Aparece abajo, se mueve hacia arriba
                return new Vector3f(anchoVentana / 2, -margen, 0);
                
            case IZQUIERDA:
                // Aparece a la izquierda, se mueve hacia la derecha
                return new Vector3f(-margen, altoVentana / 2, 0);
                
            case DERECHA:
                // Aparece a la derecha, se mueve hacia la izquierda
                return new Vector3f(anchoVentana + margen, altoVentana / 2, 0);
                
            default:
                // Fallback: centro de pantalla
                return new Vector3f(anchoVentana / 2, altoVentana / 2, 0);
        }
    }
    
    /**
     * Obtiene una dirección aleatoria
     * 
     * @return Una dirección al azar
     */
    public static Direccion getAleatoria() {
        Direccion[] valores = values();
        int index = (int)(Math.random() * valores.length);
        return valores[index];
    }
    
    /**
     * Verifica si esta dirección es vertical (ARRIBA o ABAJO)
     */
    public boolean esVertical() {
        return this == ARRIBA || this == ABAJO;
    }
    
    /**
     * Verifica si esta dirección es horizontal (IZQUIERDA o DERECHA)
     */
    public boolean esHorizontal() {
        return this == IZQUIERDA || this == DERECHA;
    }
    
    @Override
    public String toString() {
        switch(this) {
            case ARRIBA: return "↑ ARRIBA";
            case ABAJO: return "↓ ABAJO";
            case IZQUIERDA: return "← IZQUIERDA";
            case DERECHA: return "→ DERECHA";
            default: return super.toString();
        }
    }
}
