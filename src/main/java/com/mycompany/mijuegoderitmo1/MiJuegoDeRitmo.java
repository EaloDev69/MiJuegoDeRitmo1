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
import com.jme3.system.AppSettings;
import Modelo.PlaylistManager;
import Modelo.AnalizadorCanciones;
import java.util.LinkedHashMap;
import java.util.ArrayList;

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
    
    // ⭐ CONFIGURAR TAMAÑO DE VENTANA
    app.setShowSettings(false); // Desactivar ventana de configuración inicial
    
    AppSettings settings = new AppSettings(true);
    settings.setTitle("Juego de Ritmo");
    settings.setResolution(1280, 720);  // HD 720p
    // O para Full HD: settings.setResolution(1920, 1080);
    settings.setVSync(true); // Activar VSync
    settings.setFrameRate(60); // 60 FPS
    settings.setFullscreen(false); // Ventana (false) o pantalla completa (true)
    
    app.setSettings(settings);
    app.start();
}
    
    @Override
public void simpleInitApp() {
    this.setDisplayFps(true);
    this.setDisplayStatView(false);
    
    // ⭐ PARA TESTING: Comentar el menú y arrancar directo en gameplay
    /*
    menuAppStates = new MenuAppState();
    stateManager.attach(menuAppStates);
    */
    
    // ⭐ ARRANCAR DIRECTO EN GAMEPLAY (MODO TEST)
    iniciarGameplayTest();
}

/**
 * ⭐ MÉTODO TEMPORAL PARA TESTING - Inicia gameplay sin menú
 */
private void iniciarGameplayTest() {
    System.out.println("🧪 MODO TEST: Iniciando gameplay directo");
    
    // Crear análisis falso para testing
    Map<String, AnalizadorCanciones.ResultadoAnalisis> resultadosTest = new LinkedHashMap<>();
    
    // Buscar canciones en la carpeta
    PlaylistManager pm = new PlaylistManager("assets/canciones");
    try {
        pm.cargarPlaylist();
        List<String> canciones = pm.getCanciones();
        
        if (canciones.isEmpty()) {
            System.err.println("⚠ NO HAY CANCIONES EN assets/canciones/");
            System.err.println("⚠ Agrega archivos .wav para probar");
            return;
        }
        
        // Analizar solo la primera canción para test rápido
        String primeraCancion = canciones.get(0);
        AnalizadorCanciones analizador = new AnalizadorCanciones(assetManager);
        
        System.out.println("📊 Analizando: " + primeraCancion);
        AnalizadorCanciones.ResultadoAnalisis resultado = analizador.analizarCompleto(primeraCancion);
        resultadosTest.put(primeraCancion, resultado);
        
        // Iniciar gameplay
        List<String> listaTest = new ArrayList<>();
        listaTest.add(primeraCancion);
        
        startGameplay(listaTest, resultadosTest);
        
    } catch (Exception e) {
        System.err.println("❌ Error en modo test: " + e.getMessage());
        e.printStackTrace();
    }
}
    

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