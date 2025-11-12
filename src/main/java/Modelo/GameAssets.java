/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class GameAssets {
    
    // ==================== TEXTURAS DE FONDO ====================
    public static final String FONDO_ESPACIO = "assets/Texture/espacio.png";
    
    // ==================== TEXTURAS DEL PROTAGONISTA ====================
    public static final String PROTAGONISTA_DEFAULT = "assets/Texture/Protagonista/astronautaPoseDefault.png";
    public static final String PROTAGONISTA_ARRIBA = "assets/Texture/Protagonista/astronautaPoseUp.png";
    public static final String PROTAGONISTA_ABAJO = "assets/Texture/Protagonista/astronautaPoseDown.png";
    public static final String PROTAGONISTA_IZQUIERDA = "assets/Texture/Protagonista/astronautaPoseLeft.png";
    public static final String PROTAGONISTA_DERECHA = "assets/Texture/Protagonista/astronautaPoseRight.png";
    
    // ==================== TEXTURAS DE FLECHAS NORMALES ====================
    public static final String FLECHA_ROJA = "assets/Texture/flecha_roja.png";
    public static final String FLECHA_AZUL = "assets/Texture/flecha_azul.png";
    public static final String FLECHA_VERDE = "assets/Texture/flecha_verde.png";
    public static final String FLECHA_AMARILLA = "assets/Texture/flecha_amarilla.png";
    
    // ==================== TEXTURAS DE FLECHAS ESPECIALES ====================
    public static final String FLECHA_DORADA = "assets/Texture/flecha_dorada.png";
    public static final String FLECHA_LUNA = "assets/Texture/flecha_especial_luna.png";
    public static final String FLECHA_RAPIDA = "assets/Texture/flecha_rapida.png";
    
    // ==================== TEXTURAS DE UI ====================
    public static final String UI_TARGET_CENTRO = "assets/Texture/UI/target_centro.png";
    public static final String UI_BARRA_VIDA = "assets/Texture/UI/barra_vida.png";
    public static final String UI_BOTON_PAUSA = "assets/Texture/UI/boton_pausa.png";
    
    // ==================== CARPETA DE CANCIONES ====================
    public static final String CARPETA_CANCIONES = "assets/canciones";
    
    // ==================== FUENTES ====================
    public static final String FONT_DEFAULT = "Interface/Fonts/Default.fnt";
    
    
    // ==================== MÉTODO PARA OBTENER TODAS LAS TEXTURAS ====================
    
    /**
     * Retorna un array con TODAS las rutas de texturas del juego
     * Útil para pre-carga masiva
     * 
     * @return Array de rutas de texturas
     */
    public static String[] getAllTextures() {
        return new String[] {
            // Fondos
            FONDO_ESPACIO,
            
            // Protagonista
            PROTAGONISTA_DEFAULT,
            PROTAGONISTA_ARRIBA,
            PROTAGONISTA_ABAJO,
            PROTAGONISTA_IZQUIERDA,
            PROTAGONISTA_DERECHA,
            
            // Flechas normales
            FLECHA_ROJA,
            FLECHA_AZUL,
            FLECHA_VERDE,
            FLECHA_AMARILLA,
            
            // Flechas especiales
            FLECHA_DORADA,
            FLECHA_LUNA,
            FLECHA_RAPIDA,
            
            // UI
            UI_TARGET_CENTRO,
            UI_BARRA_VIDA,
            UI_BOTON_PAUSA
        };
    }
    
    /**
     * Retorna solo las texturas esenciales para el gameplay
     * Carga rápida inicial
     */
    public static String[] getEssentialTextures() {
        return new String[] {
            FONDO_ESPACIO,
            PROTAGONISTA_DEFAULT,
            FLECHA_ROJA,
            FLECHA_AZUL,
            FLECHA_DORADA,
            FLECHA_LUNA
        };
    }
    
    /**
     * Retorna texturas por categoría
     */
    public static String[] getFlechasNormales() {
        return new String[] {
            FLECHA_ROJA,
            FLECHA_AZUL,
            FLECHA_VERDE,
            FLECHA_AMARILLA
        };
    }
    
    public static String[] getFlechasEspeciales() {
        return new String[] {
            FLECHA_DORADA,
            FLECHA_LUNA,
            FLECHA_RAPIDA
        };
    }
    
    public static String[] getProtagonistaSprites() {
        return new String[] {
            PROTAGONISTA_DEFAULT,
            PROTAGONISTA_ARRIBA,
            PROTAGONISTA_ABAJO,
            PROTAGONISTA_IZQUIERDA,
            PROTAGONISTA_DERECHA
        };
    }
    
    
    // ==================== VALIDACIÓN DE RUTAS ====================
    
    /**
     * Verifica si una ruta de asset es válida
     * Útil para debugging
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        // Verificar que empiece con "assets/"
        if (!path.startsWith("assets/")) {
            System.err.println("⚠️ Ruta inválida (debe empezar con 'assets/'): " + path);
            return false;
        }
        
        // Verificar extensión
        String[] validExtensions = {".png", ".jpg", ".jpeg", ".fnt"};
        boolean hasValidExtension = false;
        for (String ext : validExtensions) {
            if (path.toLowerCase().endsWith(ext)) {
                hasValidExtension = true;
                break;
            }
        }
        
        if (!hasValidExtension) {
            System.err.println("⚠️ Extensión no válida: " + path);
            return false;
        }
        
        return true;
    }
    
    
    // ==================== UTILIDADES ====================
    
    /**
     * Normaliza una ruta eliminando barras duplicadas
     */
    public static String normalizePath(String path) {
        if (path == null) return null;
        return path.replace("\\", "/").replaceAll("/+", "/");
    }
    
    /**
     * Extrae el nombre del archivo de una ruta
     * Ejemplo: "assets/Texture/flecha.png" -> "flecha.png"
     */
    public static String getFileName(String path) {
        if (path == null) return null;
        String normalized = normalizePath(path);
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }
    
    /**
     * Extrae el nombre sin extensión
     * Ejemplo: "flecha.png" -> "flecha"
     */
    public static String getFileNameWithoutExtension(String path) {
        String fileName = getFileName(path);
        if (fileName == null) return null;
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
}

