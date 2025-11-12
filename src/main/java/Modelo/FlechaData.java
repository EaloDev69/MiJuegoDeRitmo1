/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 * Clase de datos que representa una flecha a generar en el juego
 * Contiene toda la información necesaria para crear y configurar una flecha
 * 
 * Esta clase es inmutable (los valores no cambian después de la creación)
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaData {
    
    private final float beatTime;      // Tiempo exacto del beat (en segundos)
    private final Direccion direccion; // Dirección de la flecha
    private final TipoFlecha tipo;     // Tipo de flecha (NORMAL, RAPIDA, DORADA)
    private final float velocidad;     // Velocidad de movimiento (píxeles/segundo)
    
    /**
     * Constructor principal con velocidad automática según el tipo
     * 
     * @param beatTime Tiempo del beat en segundos desde el inicio de la canción
     * @param direccion Dirección de la flecha (ARRIBA, ABAJO, IZQUIERDA, DERECHA)
     * @param tipo Tipo de flecha (NORMAL, RAPIDA, DORADA)
     */
    public FlechaData(float beatTime, Direccion direccion, TipoFlecha tipo) {
        this.beatTime = beatTime;
        this.direccion = direccion;
        this.tipo = tipo;
        this.velocidad = tipo.getVelocidadBase(); // Velocidad según el tipo
    }
    
    /**
     * Constructor con velocidad personalizada
     * Útil para ajustar dificultad o crear patrones especiales
     * 
     * @param beatTime Tiempo del beat en segundos
     * @param direccion Dirección de la flecha
     * @param tipo Tipo de flecha
     * @param velocidad Velocidad personalizada en píxeles/segundo
     */
    public FlechaData(float beatTime, Direccion direccion, TipoFlecha tipo, float velocidad) {
        this.beatTime = beatTime;
        this.direccion = direccion;
        this.tipo = tipo;
        this.velocidad = velocidad;
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Obtiene el tiempo del beat en segundos
     * Este es el momento exacto en que la flecha debe llegar al centro
     * 
     * @return Tiempo en segundos desde el inicio de la canción
     */
    public float getBeatTime() {
        return beatTime;
    }
    
    /**
     * Obtiene la dirección de la flecha
     * 
     * @return Enum Direccion (ARRIBA, ABAJO, IZQUIERDA, DERECHA)
     */
    public Direccion getDireccion() {
        return direccion;
    }
    
    /**
     * Obtiene el tipo de flecha
     * 
     * @return Enum TipoFlecha (NORMAL, RAPIDA, DORADA)
     */
    public TipoFlecha getTipo() {
        return tipo;
    }
    
    /**
     * Obtiene la velocidad de movimiento
     * 
     * @return Velocidad en píxeles por segundo
     */
    public float getVelocidad() {
        return velocidad;
    }
    
    // ==================== MÉTODOS DE UTILIDAD ====================
    
    /**
     * Calcula el tiempo en que debe aparecer esta flecha en pantalla
     * Basado en el tiempo de anticipación del gameplay
     * 
     * @param tiempoAnticipacion Segundos de anticipación (ej: 2.0s)
     * @return Tiempo de spawn en segundos
     */
    public float getTiempoSpawn(float tiempoAnticipacion) {
        return beatTime - tiempoAnticipacion;
    }
    
    /**
     * Verifica si esta flecha es de tipo dorada (se invierte)
     * 
     * @return true si es dorada, false en caso contrario
     */
    public boolean esDorada() {
        return tipo == TipoFlecha.DORADA;
    }
    
    /**
     * Verifica si esta flecha es de tipo rápida
     * 
     * @return true si es rápida, false en caso contrario
     */
    public boolean esRapida() {
        return tipo == TipoFlecha.RAPIDA;
    }
    
    /**
     * Verifica si esta flecha es de tipo normal
     * 
     * @return true si es normal, false en caso contrario
     */
    public boolean esNormal() {
        return tipo == TipoFlecha.NORMAL;
    }
    
    /**
     * Obtiene el multiplicador de puntos de esta flecha
     * 
     * @return Multiplicador (1.0 para NORMAL, 1.5 para RAPIDA, 2.0 para DORADA)
     */
    public float getMultiplicadorPuntos() {
        return tipo.getMultiplicadorPuntos();
    }
    
    /**
     * Compara este FlechaData con otro para ordenar por tiempo
     * Útil para Collections.sort()
     * 
     * @param otro Otra FlechaData para comparar
     * @return Negativo si este es anterior, 0 si igual, positivo si posterior
     */
    public int compararPorTiempo(FlechaData otro) {
        return Float.compare(this.beatTime, otro.beatTime);
    }
    
    // ==================== OVERRIDE METHODS ====================
    
    /**
     * Representación en String para debugging
     * 
     * @return String con formato legible
     */
    @Override
    public String toString() {
        return String.format("Flecha[tiempo=%.2fs, dir=%s, tipo=%s, vel=%.1f px/s]", 
            beatTime, 
            direccion.name(), 
            tipo.name(), 
            velocidad
        );
    }
    
    /**
     * Representación detallada para logs
     * 
     * @return String con toda la información
     */
    public String toStringDetallado() {
        return String.format(
            "FlechaData {\n" +
            "  Beat Time: %.3f segundos\n" +
            "  Dirección: %s\n" +
            "  Tipo: %s (multiplicador x%.1f)\n" +
            "  Velocidad: %.1f px/s\n" +
            "  Se invierte: %s\n" +
            "}",
            beatTime,
            direccion,
            tipo,
            tipo.getMultiplicadorPuntos(),
            velocidad,
            tipo.seInvierte() ? "Sí" : "No"
        );
    }
    
    /**
     * Compara si dos FlechaData son iguales
     * 
     * @param obj Objeto a comparar
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FlechaData other = (FlechaData) obj;
        
        return Float.compare(other.beatTime, beatTime) == 0 &&
               direccion == other.direccion &&
               tipo == other.tipo;
    }
    
    /**
     * Genera un hash code para esta flecha
     * 
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(beatTime);
        result = 31 * result + direccion.hashCode();
        result = 31 * result + tipo.hashCode();
        return result;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Crea una flecha normal con parámetros por defecto
     * 
     * @param beatTime Tiempo del beat
     * @param direccion Dirección
     * @return Nueva FlechaData normal
     */
    public static FlechaData crearNormal(float beatTime, Direccion direccion) {
        return new FlechaData(beatTime, direccion, TipoFlecha.NORMAL);
    }
    
    /**
     * Crea una flecha rápida con parámetros por defecto
     * 
     * @param beatTime Tiempo del beat
     * @param direccion Dirección
     * @return Nueva FlechaData rápida
     */
    public static FlechaData crearRapida(float beatTime, Direccion direccion) {
        return new FlechaData(beatTime, direccion, TipoFlecha.RAPIDA);
    }
    
    /**
     * Crea una flecha dorada con parámetros por defecto
     * 
     * @param beatTime Tiempo del beat
     * @param direccion Dirección
     * @return Nueva FlechaData dorada
     */
    public static FlechaData crearDorada(float beatTime, Direccion direccion) {
        return new FlechaData(beatTime, direccion, TipoFlecha.DORADA);
    }
}
