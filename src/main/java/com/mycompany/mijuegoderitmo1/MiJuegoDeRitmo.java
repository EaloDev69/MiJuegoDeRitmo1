/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mijuegoderitmo1;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.audio.AudioNode;
import States.MenuAppState;
import States.GameplayAppState;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import java.util.List;
import java.util.Map;

/**
 * Clase principal del juego de ritmo
 * ✨ ACTUALIZADO: Soporte para GameplayAppState con análisis completo
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class MiJuegoDeRitmo extends SimpleApplication {
    
    private MenuAppState menuAppStates;
    private AppState gameplayAppState;
    private float masterVolume = 1.0f;
    private AudioNode currentSong;
    
    public static void main(String[] args) {
        MiJuegoDeRitmo app = new MiJuegoDeRitmo();
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        this.setDisplayFps(true);
        this.setDisplayStatView(false);
        
        menuAppStates = new MenuAppState();
        stateManager.attach(menuAppStates);
    }
    
    /**
     * ✨ NUEVO: Método actualizado que recibe el Map completo de análisis
     * 
     * @param cancionesSeleccionadas Lista de rutas de canciones a jugar
     * @param resultados Map con el análisis completo de cada canción
     */
    public void startGameplay(List<String> cancionesSeleccionadas, Map<String, ResultadoAnalisis> resultados) {
        System.out.println("Iniciando gameplay con canciones: " + cancionesSeleccionadas);
        
        // Limpia el menú
        if (menuAppStates != null) {
            stateManager.detach(menuAppStates);
        }
        
        // ✨ NUEVO: Crear GameplayAppState con los resultados del análisis
        gameplayAppState = new GameplayAppState(cancionesSeleccionadas, resultados);
        stateManager.attach(gameplayAppState);
    }
    
    /**
     * Establece el volumen maestro del juego
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = volume;
        if (currentSong != null) {
            currentSong.setVolume(volume);
        }
    }
    
    /**
     * Obtiene el volumen maestro actual
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * ✨ NUEVO: Expone el FlyByCamera para que GameplayAppState pueda deshabilitarlo
     */
    public com.jme3.input.FlyByCamera getFlyByCamera() {
        return flyCam;
    }
    
    /**
     * Vuelve al menú principal
     */
    public void volverAlMenu() {
        if (gameplayAppState != null) {
            stateManager.detach(gameplayAppState);
            gameplayAppState = null;
        }
        
        if (menuAppStates == null) {
            menuAppStates = new MenuAppState();
        }
        
        stateManager.attach(menuAppStates);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // Lógica de actualización si es necesaria
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        // Renderizado personalizado si es necesario
    }
    
    /**
     * Obtiene el nodo GUI para la UI
     */
    public Node getGuiNode() {
        return guiNode;
    }
}