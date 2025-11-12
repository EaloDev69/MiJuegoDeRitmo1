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
import Modelo.AssetPreloader;
import Modelo.GameAssets;
import java.util.List;
import java.util.Map;
import com.jme3.system.AppSettings;
import Modelo.PlaylistManager;
import Modelo.AnalizadorCanciones;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * Clase principal del juego de ritmo
 * ✅ ACTUALIZADO: Sistema de pre-carga de texturas
 */
public class MiJuegoDeRitmo extends SimpleApplication {

    private MenuAppState menuAppStates;
    private AppState gameplayAppState;
    private float masterVolume = 1.0f;
    private AudioNode currentSong;
    
    // ⭐ NUEVO: Sistema de pre-carga
    private AssetPreloader assetPreloader;
    private boolean assetsCargados = false;

    public static void main(String[] args) {
        MiJuegoDeRitmo app = new MiJuegoDeRitmo();
        
        // Configurar tamaño de ventana
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
        
        // ⭐ PASO 1: Crear sistema de pre-carga
        System.out.println("\n[PASO 1/3] Creando AssetPreloader...");
        assetPreloader = new AssetPreloader(assetManager);
        
        // ⭐ PASO 2: Pre-cargar texturas ESENCIALES primero
        System.out.println("\n[PASO 2/3] Pre-cargando texturas esenciales...");
        assetPreloader.precargarEsenciales();
        
        // ⭐ PASO 3: Cargar el resto en background
        System.out.println("\n[PASO 3/3] Iniciando carga completa en background...");
        assetPreloader.precargarAsync(
            GameAssets.getAllTextures(),
            new AssetPreloader.PreloadCallback() {
                @Override
                public void onProgress(float progreso, int cargadas, int total) {
                    // Actualizar barra de progreso si tienes UI
                    System.out.printf("⏳ Assets: %.0f%% (%d/%d)\r", 
                        progreso * 100, cargadas, total);
                }
                
                @Override
                public void onComplete(int exitosas, int fallidas) {
                    assetsCargados = true;
                    System.out.println("\n✅ Todos los assets cargados!");
                    System.out.println("   ✓ Exitosas: " + exitosas);
                    System.out.println("   ✗ Fallidas: " + fallidas);
                    
                    // Estadísticas del caché
                    assetPreloader.imprimirEstadisticas();
                }
                
                @Override
                public void onError(Exception e) {
                    System.err.println("❌ Error cargando assets: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        );
        
        // Iniciar menú o gameplay
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎬 Iniciando interfaz del juego...");
        System.out.println("=".repeat(60) + "\n");
        
        // MODO NORMAL: Iniciar con menú
        iniciarConMenu();
        
        // MODO TEST: Descomentar para ir directo al gameplay
        // iniciarGameplayTest();
    }
    
    /**
     * ⭐ NUEVO: Método para iniciar con el menú
     */
    private void iniciarConMenu() {
        menuAppStates = new MenuAppState();
        stateManager.attach(menuAppStates);
        System.out.println("✓ MenuAppState iniciado");
    }

    /**
     * Método temporal para testing - Inicia gameplay sin menú
     */
    private void iniciarGameplayTest() {
        System.out.println("🧪 MODO TEST: Iniciando gameplay directo");
        
        // Crear análisis falso para testing
        Map<String, AnalizadorCanciones.ResultadoAnalisis> resultadosTest = new LinkedHashMap<>();
        
        PlaylistManager pm = new PlaylistManager(GameAssets.CARPETA_CANCIONES);
        
        try {
            pm.cargarPlaylist();
            List<String> canciones = pm.getCanciones();
            
            if (canciones.isEmpty()) {
                System.err.println("⚠ NO HAY CANCIONES EN " + GameAssets.CARPETA_CANCIONES);
                System.err.println("⚠ Agrega archivos .wav para probar");
                System.err.println("⚠ Ruta esperada: " + System.getProperty("user.dir") + "/" + GameAssets.CARPETA_CANCIONES);
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

    /**
     * Inicia el gameplay con las canciones seleccionadas
     */
    public void startGameplay(List<String> cancionesSeleccionadas, 
                              Map<String, ResultadoAnalisis> resultados) {
        System.out.println("Iniciando gameplay con canciones: " + cancionesSeleccionadas);
        
        // Limpia el menú
        if (menuAppStates != null) {
            stateManager.detach(menuAppStates);
        }
        
        // Crear GameplayAppState con los resultados del análisis
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
    public float getMasterVolumen() {
        return masterVolume;
    }

    /**
     * Expone el FlyByCamera para que GameplayAppState pueda deshabilitarlo
     */
    public com.jme3.input.FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * Vuelve al menú principal
     */
    /**
 * Vuelve al menú principal
 */
public void volverAlMenu() {
    System.out.println("\n=== VOLVIENDO AL MENÚ PRINCIPAL ===");
    
    // 1. ⭐ Limpiar GameplayAppState si existe
    if (gameplayAppState != null) {
        System.out.println("  - Desvinculando GameplayAppState...");
        
        stateManager.detach(gameplayAppState);
        gameplayAppState = null;
    }
    
    // 2. ⭐ CRÍTICO: Limpiar completamente el GuiNode
    System.out.println("  - Limpiando GuiNode...");
    guiNode.detachAllChildren();
    
    // 3. ⭐ CRÍTICO: Limpiar RootNode también (por si acaso)
    rootNode.detachAllChildren();
    
    // 4. Recrear MenuAppState desde cero
    System.out.println("  - Creando nuevo MenuAppState...");
    menuAppStates = new MenuAppState();
    
    // 5. Vincular menú
    System.out.println("  - Vinculando MenuAppState...");
    stateManager.attach(menuAppStates);
    
    System.out.println("✓ Menú principal restaurado");
    System.out.println("===================================\n");
}
    
    /**
     * ⭐ NUEVO: Obtiene el sistema de pre-carga de assets
     */
    public AssetPreloader getAssetPreloader() {
        return assetPreloader;
    }
    
    /**
     * ⭐ NUEVO: Verifica si los assets están completamente cargados
     */
    public boolean isAssetsCargados() {
        return assetsCargados;
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
     * ⭐ NUEVO: Cleanup al cerrar la aplicación
     */
    @Override
    public void destroy() {
        System.out.println("\n🧹 Cerrando aplicación...");
        
        if (assetPreloader != null) {
            assetPreloader.limpiarCache();
        }
        
        super.destroy();
        System.out.println("👋 ¡Hasta pronto!");
    }
}
