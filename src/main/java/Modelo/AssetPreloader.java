/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.texture.Texture;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class AssetPreloader {

    
    private final AssetManager assetManager;
    
    // Caché de texturas pre-cargadas
    private final Map<String, Texture> textureCache;
    
    // Texturas que fallaron al cargar
    private final Set<String> failedTextures;
    
    // Estado de carga
    private boolean cargaCompletada = false;
    private int texturasCargadas = 0;
    private int texturasTotal = 0;
    
    // Callback de progreso
    private PreloadCallback callback;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public AssetPreloader(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.textureCache = new ConcurrentHashMap<>();
        this.failedTextures = Collections.synchronizedSet(new HashSet<>());
        
        System.out.println("📦 AssetPreloader inicializado");
    }
    
    
    // ==================== MÉTODOS DE CARGA ====================
    
    /**
     * Pre-carga TODAS las texturas del juego
     * Modo: SÍNCRONO (bloquea hasta completar)
     * Usar solo en pantalla de carga inicial
     */
    public void precargarTodo() {
        System.out.println("\n🔄 Iniciando pre-carga COMPLETA de assets...");
        long tiempoInicio = System.currentTimeMillis();
        
        String[] todasLasTexturas = GameAssets.getAllTextures();
        precargarTexturas(todasLasTexturas, true);
        
        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        
        System.out.println("\n✅ PRE-CARGA COMPLETADA");
        System.out.println("   ⏱ Tiempo: " + tiempoTotal + "ms");
        System.out.println("   ✓ Cargadas: " + texturasCargadas + "/" + texturasTotal);
        System.out.println("   ✗ Fallidas: " + failedTextures.size());
        
        if (!failedTextures.isEmpty()) {
            System.err.println("\n⚠️ Texturas que fallaron:");
            for (String ruta : failedTextures) {
                System.err.println("   - " + ruta);
            }
        }
    }
    
    /**
     * Pre-carga solo texturas esenciales
     * Modo: SÍNCRONO
     * Para inicio rápido del juego
     */
    public void precargarEsenciales() {
        System.out.println("\n⚡ Iniciando pre-carga ESENCIAL de assets...");
        
        String[] esenciales = GameAssets.getEssentialTextures();
        precargarTexturas(esenciales, true);
        
        System.out.println("✅ Pre-carga esencial completada");
    }
    
    /**
     * Pre-carga ASÍNCRONA con callback de progreso
     * No bloquea el thread principal
     * 
     * @param texturas Array de rutas a cargar
     * @param callback Callback para reportar progreso
     */
    public void precargarAsync(String[] texturas, PreloadCallback callback) {
        this.callback = callback;
        this.texturasTotal = texturas.length;
        this.texturasCargadas = 0;
        this.cargaCompletada = false;
        
        System.out.println("\n🔄 Iniciando pre-carga ASÍNCRONA de " + texturasTotal + " texturas...");
        
        // Usar thread separado para no bloquear JME
        new Thread(() -> {
            try {
                precargarTexturas(texturas, false);
                cargaCompletada = true;
                
                if (callback != null) {
                    callback.onComplete(texturasCargadas, failedTextures.size());
                }
            } catch (Exception e) {
                System.err.println("❌ Error en pre-carga asíncrona: " + e.getMessage());
                e.printStackTrace();
                
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }, "AssetPreloaderThread").start();
    }
    
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * Carga un array de texturas
     * 
     * @param texturas Array de rutas
     * @param verbose Si debe imprimir cada textura cargada
     */
    private void precargarTexturas(String[] texturas, boolean verbose) {
        texturasTotal = texturas.length;
        texturasCargadas = 0;
        
        for (String ruta : texturas) {
            if (cargarTextura(ruta, verbose)) {
                texturasCargadas++;
                
                // Notificar progreso
                if (callback != null) {
                    float progreso = (float) texturasCargadas / texturasTotal;
                    callback.onProgress(progreso, texturasCargadas, texturasTotal);
                }
            }
        }
    }
    
    /**
     * Carga una textura individual
     * 
     * @param ruta Ruta de la textura
     * @param verbose Si debe imprimir resultado
     * @return true si se cargó exitosamente
     */
    private boolean cargarTextura(String ruta, boolean verbose) {
        // Ya está en caché
        if (textureCache.containsKey(ruta)) {
            if (verbose) {
                System.out.println("   ♻️ En caché: " + GameAssets.getFileName(ruta));
            }
            return true;
        }
        
        // Ya falló antes
        if (failedTextures.contains(ruta)) {
            return false;
        }
        
        // Validar ruta
        if (!GameAssets.isValidPath(ruta)) {
            System.err.println("   ❌ Ruta inválida: " + ruta);
            failedTextures.add(ruta);
            return false;
        }
        
        try {
            // Cargar textura
            Texture texture = assetManager.loadTexture(ruta);
            
            if (texture != null) {
                textureCache.put(ruta, texture);
                
                if (verbose) {
                    System.out.println("   ✓ Cargada: " + GameAssets.getFileName(ruta));
                }
                
                return true;
            } else {
                throw new AssetNotFoundException("Textura retornó null");
            }
            
        } catch (AssetNotFoundException e) {
            System.err.println("   ❌ No encontrada: " + ruta);
            failedTextures.add(ruta);
            return false;
            
        } catch (Exception e) {
            System.err.println("   ❌ Error al cargar: " + ruta);
            System.err.println("      Razón: " + e.getMessage());
            failedTextures.add(ruta);
            return false;
        }
    }
    
    
    // ==================== MÉTODOS PÚBLICOS DE ACCESO ====================
    
    /**
     * Obtiene una textura del caché (RECOMENDADO)
     * Retorna null si no está cargada
     * 
     * @param ruta Ruta de la textura
     * @return Textura o null
     */
    public Texture getTexture(String ruta) {
        return textureCache.get(ruta);
    }
    
    /**
     * Obtiene una textura CON FALLBACK
     * Si no existe, carga una textura de respaldo
     * 
     * @param ruta Ruta principal
     * @param fallbackColor Color de respaldo si falla
     * @return Textura (nunca null)
     */
    public Texture getTextureOrFallback(String ruta, String fallbackRuta) {
        Texture texture = textureCache.get(ruta);
        
        if (texture != null) {
            return texture;
        }
        
        // Intentar cargar fallback
        System.out.println("⚠️ Textura no encontrada: " + ruta);
        System.out.println("   Usando fallback: " + fallbackRuta);
        
        if (fallbackRuta != null) {
            texture = textureCache.get(fallbackRuta);
            
            if (texture != null) {
                return texture;
            }
            
            // Cargar fallback si no está en caché
            cargarTextura(fallbackRuta, false);
            return textureCache.get(fallbackRuta);
        }
        
        return null;
    }
    
    /**
     * Verifica si una textura está cargada en caché
     */
    public boolean isLoaded(String ruta) {
        return textureCache.containsKey(ruta);
    }
    
    /**
     * Verifica si la carga completa terminó
     */
    public boolean isCargaCompletada() {
        return cargaCompletada;
    }
    
    /**
     * Obtiene el progreso de carga (0.0 a 1.0)
     */
    public float getProgreso() {
        if (texturasTotal == 0) return 0f;
        return (float) texturasCargadas / texturasTotal;
    }
    
    /**
     * Obtiene el número de texturas cargadas
     */
    public int getTexturasCargadas() {
        return texturasCargadas;
    }
    
    /**
     * Obtiene el total de texturas a cargar
     */
    public int getTexturasTotal() {
        return texturasTotal;
    }
    
    
    // ==================== MÉTODOS DE CACHÉ ====================
    
    /**
     * Limpia el caché de texturas
     * Útil para liberar memoria
     */
    public void limpiarCache() {
        System.out.println("🧹 Limpiando caché de texturas...");
        int cantidad = textureCache.size();
        textureCache.clear();
        failedTextures.clear();
        System.out.println("✓ " + cantidad + " texturas removidas del caché");
    }
    
    /**
     * Remueve una textura específica del caché
     */
    public void removerDelCache(String ruta) {
        if (textureCache.remove(ruta) != null) {
            System.out.println("♻️ Removida del caché: " + GameAssets.getFileName(ruta));
        }
    }
    
    /**
     * Obtiene el tamaño actual del caché
     */
    public int getCacheSize() {
        return textureCache.size();
    }
    
    
    // ==================== MÉTODOS DE DEBUGGING ====================
    
    /**
     * Imprime estadísticas del caché
     */
    public void imprimirEstadisticas() {
        System.out.println("\n📊 ESTADÍSTICAS DE CACHÉ");
        System.out.println("   Texturas en caché: " + textureCache.size());
        System.out.println("   Texturas fallidas: " + failedTextures.size());
        System.out.println("   Carga completada: " + (cargaCompletada ? "Sí" : "No"));
        System.out.println("   Progreso: " + String.format("%.1f%%", getProgreso() * 100));
    }
    
    /**
     * Lista todas las texturas en caché
     */
    public void listarCache() {
        System.out.println("\n📋 TEXTURAS EN CACHÉ:");
        for (String ruta : textureCache.keySet()) {
            System.out.println("   ✓ " + ruta);
        }
    }
    
    
    // ==================== INTERFAZ DE CALLBACK ====================
    
    /**
     * Interfaz para recibir notificaciones de progreso
     */
    public interface PreloadCallback {
        /**
         * Llamado cada vez que se carga una textura
         * 
         * @param progreso Progreso normalizado (0.0 a 1.0)
         * @param cargadas Número de texturas cargadas
         * @param total Total de texturas
         */
        void onProgress(float progreso, int cargadas, int total);
        
        /**
         * Llamado cuando la carga se completa
         * 
         * @param exitosas Texturas cargadas exitosamente
         * @param fallidas Texturas que fallaron
         */
        void onComplete(int exitosas, int fallidas);
        
        /**
         * Llamado si ocurre un error crítico
         */
        void onError(Exception e);
    }
    
    
    // ==================== IMPLEMENTACIÓN SIMPLE DE CALLBACK ====================
    
    /**
     * Callback simple que solo imprime en consola
     */
    public static class ConsoleCallback implements PreloadCallback {
        @Override
        public void onProgress(float progreso, int cargadas, int total) {
            System.out.printf("⏳ Progreso: %.1f%% (%d/%d)\n", 
                progreso * 100, cargadas, total);
        }
        
        @Override
        public void onComplete(int exitosas, int fallidas) {
            System.out.println("\n✅ CARGA COMPLETADA");
            System.out.println("   ✓ Exitosas: " + exitosas);
            System.out.println("   ✗ Fallidas: " + fallidas);
        }
        
        @Override
        public void onError(Exception e) {
            System.err.println("❌ ERROR EN CARGA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}  

