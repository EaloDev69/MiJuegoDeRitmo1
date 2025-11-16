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
 * ✅ Inicia con el menú principal
 * ✅ Ruta de canciones: assets/canciones/
 */
public class MiJuegoDeRitmo extends SimpleApplication {
    
    private MenuAppState menuAppStates;
    private AppState gameplayAppState;
    private float masterVolume = 1.0f;
    private AudioNode currentSong;

    public static void main(String[] args) {
        MiJuegoDeRitmo app = new MiJuegoDeRitmo();
        
        // Configurar ventana
        app.setShowSettings(false);
        
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Juego de Ritmo");
        settings.setResolution(1280, 720);
        settings.setVSync(true);
        settings.setFrameRate(60);
        settings.setFullscreen(false);
        
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        this.setDisplayFps(true);
        this.setDisplayStatView(false);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎮 JUEGO DE RITMO - INICIANDO");
        System.out.println("=".repeat(60));
        
        // ⭐ INICIAR CON EL MENÚ PRINCIPAL
        iniciarConMenu();
        
        System.out.println("✓ Aplicación inicializada correctamente");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * ⭐ Inicia el juego mostrando el menú principal
     */
    private void iniciarConMenu() {
        System.out.println("🎬 Iniciando menú principal...");
        menuAppStates = new MenuAppState();
        stateManager.attach(menuAppStates);
        System.out.println("✓ MenuAppState iniciado");
    }

    /**
     * ⭐ MÉTODO OPCIONAL PARA TESTING - Inicia gameplay sin menú
     * Descomenta la llamada en simpleInitApp() para usar
     */
    private void iniciarGameplayTest() {
        System.out.println("🧪 MODO TEST: Iniciando gameplay directo");
        
        Map<String, AnalizadorCanciones.ResultadoAnalisis> resultadosTest = new LinkedHashMap<>();
        PlaylistManager pm = new PlaylistManager("assets/canciones");
        
        try {
            pm.cargarPlaylist();
            List<String> canciones = pm.getCanciones();
            
            if (canciones.isEmpty()) {
                System.err.println("⚠ NO HAY CANCIONES EN assets/canciones/");
                System.err.println("⚠ Agrega archivos .wav para probar");
                System.err.println("⚠ Ruta esperada: " + System.getProperty("user.dir") + "/assets/canciones/");
                return;
            }
            
            // Analizar solo la primera canción
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

    /**
     * Inicia el gameplay con las canciones seleccionadas
     * Llamado desde MenuAppState cuando el usuario presiona "Empezar"
     */
    public void startGameplay(List<String> cancionesSeleccionadas, 
                             Map<String, ResultadoAnalisis> resultados) {
        System.out.println("\n=== INICIANDO GAMEPLAY ===");
        System.out.println("Canciones seleccionadas: " + cancionesSeleccionadas.size());
        
        // Limpiar el menú
        if (menuAppStates != null) {
            System.out.println("  - Desvinculando menú...");
            stateManager.detach(menuAppStates);
        }
        
        // Crear GameplayAppState con los resultados del análisis
        gameplayAppState = new GameplayAppState(cancionesSeleccionadas, resultados);
        stateManager.attach(gameplayAppState);
        
        System.out.println("✓ Gameplay iniciado correctamente");
    }

    public void startGameplayConDificultad(List<String> cancionesSeleccionadas,
                                           Map<String, ResultadoAnalisis> resultados,
                                           Modelo.FlechasGenerator.Difficulty dificultad) {
        System.out.println("\n=== INICIANDO GAMEPLAY (con dificultad) ===");
        System.out.println("Canciones seleccionadas: " + cancionesSeleccionadas.size());
        if (menuAppStates != null) {
            System.out.println("  - Desvinculando menú...");
            stateManager.detach(menuAppStates);
        }
        gameplayAppState = new GameplayAppState(cancionesSeleccionadas, resultados, dificultad);
        stateManager.attach(gameplayAppState);
        System.out.println("✓ Gameplay iniciado correctamente");
    }

    /**
     * Inicia el gameplay en modo práctica (sin barra de vida ni game over por vida)
     */
    public void startGameplayPractica(List<String> cancionesSeleccionadas,
                                      Map<String, ResultadoAnalisis> resultados) {
        System.out.println("\n=== INICIANDO MODO PRÁCTICA ===");
        System.out.println("Canciones seleccionadas: " + cancionesSeleccionadas.size());

        // Limpiar el menú
        if (menuAppStates != null) {
            System.out.println("  - Desvinculando menú...");
            stateManager.detach(menuAppStates);
        }

        // Crear GameplayAppState con modo práctica activado
        gameplayAppState = new GameplayAppState(cancionesSeleccionadas, resultados, true);
        stateManager.attach(gameplayAppState);

        System.out.println("✓ Modo práctica iniciado correctamente");
    }

    /**
     * Vuelve al menú principal
     * Llamado desde GameplayAppState al terminar o salir
     */
    public void volverAlMenu() {
        System.out.println("\n=== VOLVIENDO AL MENÚ PRINCIPAL ===");
        
        // 1. Limpiar GameplayAppState
        if (gameplayAppState != null) {
            System.out.println("  - Desvinculando GameplayAppState...");
            stateManager.detach(gameplayAppState);
            gameplayAppState = null;
        }
        
        // 2. Limpiar completamente el GuiNode
        System.out.println("  - Limpiando GUI...");
        guiNode.detachAllChildren();
        
        // 3. Limpiar RootNode
        rootNode.detachAllChildren();
        
        // 4. Recrear MenuAppState desde cero
        System.out.println("  - Creando nuevo menú...");
        menuAppStates = new MenuAppState();
        
        // 5. Vincular menú
        stateManager.attach(menuAppStates);
        
        System.out.println("✓ Menú principal restaurado");
        System.out.println("===================================\n");
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
     * Expone el FlyByCamera para que GameplayAppState pueda deshabilitarlo
     */
    public com.jme3.input.FlyByCamera getFlyByCamera() {
        return flyCam;
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
    
    /**
     * Cleanup al cerrar la aplicación
     */
    @Override
    public void destroy() {
        System.out.println("\n🧹 Cerrando aplicación...");
        super.destroy();
        System.out.println("👋 ¡Hasta pronto!");
    }
}