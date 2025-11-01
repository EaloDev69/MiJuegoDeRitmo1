/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mijuegoderitmo1;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.audio.AudioNode;
import States.MenuAppState;
import States.GameplayAppState;
import Modelo.AnalizadorCanciones;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import java.util.List;
import java.util.Map;

/**
 * Clase principal del juego de ritmo
 * Actualizada con el nuevo sistema de flechas
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class MiJuegoDeRitmo extends SimpleApplication {
    
    private MenuAppState menuAppState;
    private GameplayAppState gameplayAppState;
    private float masterVolume = 1.0f;
    private AudioNode currentSong;
    
    // Análisis de canciones (guardado para el gameplay)
    private Map<String, ResultadoAnalisis> analisisCache;
    
    public static void main(String[] args) {
        MiJuegoDeRitmo app = new MiJuegoDeRitmo();
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        this.setDisplayFps(true);
        this.setDisplayStatView(false);
        
        // Configurar cámara para 2D
        cam.setParallelProjection(true);
        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustum(-1000, 1000, 
                      -aspect * settings.getHeight() / 2, 
                      aspect * settings.getHeight() / 2, 
                      settings.getHeight() / 2, 
                      -settings.getHeight() / 2);
        
        // Iniciar con el menú
        menuAppState = new MenuAppState();
        stateManager.attach(menuAppState);
    }
    
    /**
     * Inicia el gameplay con las canciones seleccionadas
     * Este método es llamado desde MenuAppState después del análisis
     * 
     * @param cancionesSeleccionadas Lista de rutas de canciones
     * @param analisis Mapa con los análisis de cada canción
     */
    public void startGameplay(List<String> cancionesSeleccionadas, 
                             Map<String, ResultadoAnalisis> analisis) {
        System.out.println("\n=== INICIANDO GAMEPLAY ===");
        System.out.println("Canciones: " + cancionesSeleccionadas.size());
        
        // Guardar análisis
        this.analisisCache = analisis;
        
        // Limpiar el menú
        if (menuAppState != null) {
            stateManager.detach(menuAppState);
        }
        
        // Crear y adjuntar el gameplay
        gameplayAppState = new GameplayAppState(cancionesSeleccionadas, analisis);
        stateManager.attach(gameplayAppState);
        
        System.out.println("✓ Gameplay iniciado");
    }
    
    /**
     * Sobrecarga del método original (para compatibilidad)
     * Ahora requiere que se pase el análisis también
     */
    public void startGameplay(List<String> cancionesSeleccionadas) {
        System.err.println("ERROR: Debe usar startGameplay(canciones, analisis)");
        System.err.println("El análisis debe hacerse antes de iniciar el gameplay");
    }
    
    /**
     * Vuelve al menú principal
     */
    public void volverAlMenu() {
        System.out.println("\n=== VOLVIENDO AL MENÚ ===");
        
        // Detener gameplay
        if (gameplayAppState != null) {
            stateManager.detach(gameplayAppState);
            gameplayAppState = null;
        }
        
        // Recrear menú
        if (menuAppState == null) {
            menuAppState = new MenuAppState();
        }
        stateManager.attach(menuAppState);
        
        System.out.println("✓ Menú cargado");
    }
    
    /**
     * Ajusta el volumen maestro
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        if (currentSong != null) {
            currentSong.setVolume(masterVolume);
        }
        System.out.println("Volumen ajustado: " + (int)(masterVolume * 100) + "%");
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // Actualización manejada por los AppStates
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        // Renderizado manejado por los AppStates
    }
    
    public Node getGuiNode() {
        return guiNode;
    }
}


