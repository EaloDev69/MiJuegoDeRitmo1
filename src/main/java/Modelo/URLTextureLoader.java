/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class URLTextureLoader {
    
    private final AssetManager assetManager;
    private final ConcurrentHashMap<String, Texture> cache;
    private final AWTLoader awtLoader;
    
    public URLTextureLoader(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.cache = new ConcurrentHashMap<>();
        this.awtLoader = new AWTLoader();
    }
    
    /**
     * Carga una textura desde una URL
     * @param urlString URL de la imagen (PNG, JPG, etc.)
     * @return Texture cargada o null si falla
     */
    public Texture cargarTexturaDesdeURL(String urlString) {
        // Verificar caché primero
        if (cache.containsKey(urlString)) {
            System.out.println("♻️ Textura en caché: " + urlString);
            return cache.get(urlString);
        }
        
        try {
            System.out.println("🌐 Descargando textura desde: " + urlString);
            
            // Descargar imagen
            URL url = new URL(urlString);
            BufferedImage bufferedImage = ImageIO.read(url);
            
            if (bufferedImage == null) {
                System.err.println("❌ No se pudo leer la imagen desde: " + urlString);
                return null;
            }
            
            // Convertir BufferedImage a jME Image
            Image jmeImage = awtLoader.load(bufferedImage, false);
            
            // Crear textura
            Texture2D texture = new Texture2D(jmeImage);
            texture.setMagFilter(Texture.MagFilter.Bilinear);
            texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
            
            // Guardar en caché
            cache.put(urlString, texture);
            
            System.out.println("✅ Textura cargada exitosamente desde URL");
            return texture;
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando textura desde URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Carga asíncrona con callback
     */
    public void cargarTexturaAsync(String urlString, TextureCallback callback) {
        new Thread(() -> {
            Texture texture = cargarTexturaDesdeURL(urlString);
            if (callback != null) {
                callback.onTextureLoaded(texture, urlString);
            }
        }, "URLTextureLoader-" + urlString.hashCode()).start();
    }
    
    /**
     * Carga con fallback local
     */
    public Texture cargarConFallback(String urlString, String rutaLocal) {
        Texture texture = cargarTexturaDesdeURL(urlString);
        
        if (texture == null) {
            System.out.println("⚠️ Usando fallback local: " + rutaLocal);
            try {
                return assetManager.loadTexture(rutaLocal);
            } catch (Exception e) {
                System.err.println("❌ Fallback también falló: " + e.getMessage());
                return null;
            }
        }
        
        return texture;
    }
    
    /**
     * Pre-descarga múltiples texturas
     */
    public void precargarURLs(String[] urls, PreloadCallback callback) {
        int total = urls.length;
        int[] cargadas = {0};
        
        for (String url : urls) {
            cargarTexturaAsync(url, (texture, urlStr) -> {
                cargadas[0]++;
                if (callback != null) {
                    callback.onProgress((float) cargadas[0] / total, cargadas[0], total);
                    
                    if (cargadas[0] == total) {
                        callback.onComplete(cargadas[0], total - cargadas[0]);
                    }
                }
            });
        }
    }
    
    public void limpiarCache() {
        cache.clear();
        System.out.println("🧹 Caché de texturas URL limpiado");
    }
    
    // Interfaces
    public interface TextureCallback {
        void onTextureLoaded(Texture texture, String url);
    }
    
    public interface PreloadCallback {
        void onProgress(float progreso, int cargadas, int total);
        void onComplete(int exitosas, int fallidas);
    }
}

