/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Cliente para API de assets
 */
public class AssetAPIClient {
    
    private final String baseURL;
    private final URLTextureLoader textureLoader;
    
    public AssetAPIClient(String baseURL, URLTextureLoader loader) {
        this.baseURL = baseURL;
        this.textureLoader = loader;
    }
    
    /**
     * Obtiene la URL del sprite desde la API
     * Ejemplo: GET /api/sprites/protagonista/default
     * Respuesta: { "url": "https://cdn.ejemplo.com/sprite.png" }
     */
    public String obtenerURLSprite(String categoria, String nombre) {
        try {
            String endpoint = baseURL + "/sprites/" + categoria + "/" + nombre;
            URL url = new URL(endpoint);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parsear JSON
                JSONObject json = new JSONObject(response.toString());
                return json.getString("url");
            } else {
                System.err.println("❌ API error: " + conn.getResponseCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error consultando API: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Carga sprite directamente desde la API
     */
    public Texture cargarSpriteDesdeAPI(String categoria, String nombre, String fallback) {
        String urlSprite = obtenerURLSprite(categoria, nombre);
        
        if (urlSprite != null) {
            return textureLoader.cargarConFallback(urlSprite, fallback);
        } else {
            System.out.println("⚠️ Usando fallback local: " + fallback);
            return textureLoader.cargarTexturaDesdeURL(fallback);
        }
    }
}