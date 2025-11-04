/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package States;

import Modelo.*;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import Controls.FlechaControl;
import UI.GameplayUI;
import UI.VentanaResultados;
import UI.MenuPausa;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo;
import java.util.*;
import javax.swing.SwingUtilities;
import com.jme3.app.SimpleApplication;
import com.jme3.material.RenderState;
import com.jme3.texture.Texture;

/**
 * GameplayAppState - VERSIÓN ESTABILIZADA CON FONDO Y PROTAGONISTA
 * 
 * CORRECCIONES APLICADAS:
 * ✅ Fondo espacial (espacio.png)
 * ✅ Protagonista astronauta (astronautaPoseDefault.png)
 * ✅ Ciclo de vida estabilizado (NO vuelve al menú automáticamente)
 * ✅ Menú de pausa funcional con ESC
 * ✅ Update() bloqueado correctamente durante pausa
 */
public class GameplayAppState extends BaseAppState implements ActionListener {
    
    private MiJuegoDeRitmo app;
    private Node gameNode; // Para flechas en movimiento (2D sobre GUI)
    private AssetManager assetManager;
    
    // ⭐ NUEVO: Referencias al fondo y protagonista
    private Geometry fondoGeometry;
    private Geometry protagonistaGeometry;
    
    // Flags de control de ciclo de vida
    private boolean juegoTerminado = false;
    private boolean resultadosMostrados = false;
    private boolean procesandoResultados = false;
    
    // UI y Menú
    private MenuPausa menuPausa;
    private GameplayUI gameplayUI;
    
    // Sistema de flechas
    private FlechasGenerator flechasGenerator;
    private List<FlechaData> flechasAGenerar;
    private int indiceFlechaActual = 0;
    private List<Geometry> flechasActivas;
    
    // Canciones
    private List<String> canciones;
    private Map<String, ResultadoAnalisis> analisisCompleto;
    private int cancionActual = 0;
    private AudioNode audioNode;
    private ResultadoAnalisis analisisActual;
    
    // Timing
    private float tiempoTranscurrido = 0f;
    private float tiempoAnticipacion = 2.0f;
    
    // Puntuación
    private int score = 0;
    private int vida = 100;
    private int combo = 0;
    private int maxCombo = 0;
    
    // Estadísticas
    private int perfectos = 0;
    private int buenos = 0;
    private int malos = 0;
    private int misses = 0;
    
    // UI
    private Vector3f centroPantalla;
    
    // Input
    private Map<Direccion, String> mappingTeclas;
    
    public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> analisis) {
        this.canciones = canciones;
        this.analisisCompleto = analisis;
        this.flechasActivas = new ArrayList<>();
        this.flechasGenerator = new FlechasGenerator();
        
        if (!canciones.isEmpty()) {
            String primerCancion = canciones.get(0);
            this.analisisActual = analisis.get(primerCancion);
        }
    }
    
    @Override
    protected void initialize(Application app) {
        this.app = (MiJuegoDeRitmo) app;
        this.assetManager = app.getAssetManager();
        
        // gameNode debe ser un Node 2D para sprites sobre GuiNode
        this.gameNode = new Node("GameNode");
        
        // Deshabilitar FlyByCamera
        this.app.getFlyByCamera().setEnabled(false);
        
        // Configurar cámara 2D
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        centroPantalla = new Vector3f(ancho / 2, alto / 2, 0);
        
        app.getCamera().setParallelProjection(true);
        float aspect = (float) app.getCamera().getWidth() / app.getCamera().getHeight();
        app.getCamera().setFrustum(-1000, 1000, -aspect * alto / 2, aspect * alto / 2, alto / 2, -alto / 2);
        
        // ⭐ NUEVO: Crear fondo y protagonista PRIMERO
        crearFondo();
        crearProtagonista();
        
        // Setup inputs ANTES de crear UI (para evitar conflictos)
        setupInputs();
        
        // Inicializar UI después de inputs
        inicializarUI();
        
        // Generar flechas
        generarFlechasCancion();
        
        // Reproducir música
        reproducirCancionActual();
        
        System.out.println("✓ Gameplay inicializado correctamente");
        System.out.println("  - Vida: " + vida);
        System.out.println("  - Flechas a generar: " + flechasAGenerar.size());
        System.out.println("  - Fondo y protagonista cargados");
    }
    
    // ==================== 🎨 CREAR FONDO ====================
    
    /**
     * ⭐ NUEVO: Crea el fondo espacial (espacio.png)
     */
    private void crearFondo() {
        System.out.println("\n🌌 Creando fondo espacial...");
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        try {
            // 1. Cargar textura
            Texture fondoTexture = assetManager.loadTexture("Textures/espacio.png");
            
            // 2. Crear material
            Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            fondoMat.setTexture("ColorMap", fondoTexture);
            
            // 3. Crear geometría (Quad que cubra toda la pantalla)
            fondoGeometry = new Geometry("FondoGameplay", new Quad(ancho, alto));
            fondoGeometry.setMaterial(fondoMat);
            
            // 4. Posicionar (en Z = -1 para estar detrás de todo)
            fondoGeometry.setLocalTranslation(0, 0, -1f);
            
            // 5. Anclar al GuiNode (para que sea 2D)
            app.getGuiNode().attachChild(fondoGeometry);
            
            System.out.println("  ✓ Fondo espacial cargado correctamente");
            System.out.println("    - Dimensiones: " + ancho + "x" + alto);
            System.out.println("    - Posición Z: -1 (detrás de todo)");
            
        } catch (Exception e) {
            System.err.println("  ❌ ERROR al cargar fondo: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Fondo negro
            crearFondoFallback(ancho, alto);
        }
    }
    
    /**
     * Crea un fondo de color si no se encuentra la textura
     */
    private void crearFondoFallback(float ancho, float alto) {
        Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fondoMat.setColor("Color", new ColorRGBA(0.05f, 0.05f, 0.1f, 1f)); // Azul oscuro
        
        fondoGeometry = new Geometry("FondoFallback", new Quad(ancho, alto));
        fondoGeometry.setMaterial(fondoMat);
        fondoGeometry.setLocalTranslation(0, 0, -1f);
        
        app.getGuiNode().attachChild(fondoGeometry);
        System.out.println("  ⚠ Fondo creado como FALLBACK (azul oscuro)");
    }
    
    // ==================== 👨‍🚀 CREAR PROTAGONISTA ====================
    
    /**
     * ⭐ NUEVO: Crea el protagonista astronauta (astronautaPoseDefault.png)
     */
    private void crearProtagonista() {
        System.out.println("\n👨‍🚀 Creando protagonista...");
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        try {
            // 1. Cargar textura
            Texture astroTexture = assetManager.loadTexture("Textures/Protagonista/astronautaPoseDefault.png");
            
            // 2. Crear material con transparencia
            Material astroMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            astroMat.setTexture("ColorMap", astroTexture);
            astroMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            
            // 3. Tamaño del sprite
            float astroSize = 200f; // Ajusta según necesites
            
            // 4. Crear geometría
            protagonistaGeometry = new Geometry("Protagonista", new Quad(astroSize, astroSize));
            protagonistaGeometry.setMaterial(astroMat);
            
            // 5. Posicionar (parte inferior central, delante del fondo)
            float posX = (ancho / 2f) - (astroSize / 2f); // Centrado horizontalmente
            float posY = 50f; // Elevado del borde inferior
            protagonistaGeometry.setLocalTranslation(posX, posY, 0f); // Z=0 para estar delante del fondo
            
            // 6. Anclar al GuiNode
            app.getGuiNode().attachChild(protagonistaGeometry);
            
            System.out.println("  ✓ Protagonista cargado correctamente");
            System.out.println("    - Tamaño: " + astroSize + "x" + astroSize);
            System.out.println("    - Posición: (" + posX + ", " + posY + ", 0)");
            
        } catch (Exception e) {
            System.err.println("  ❌ ERROR al cargar protagonista: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Cuadrado blanco
            crearProtagonistaFallback(ancho, alto);
        }
    }
    
    /**
     * Crea un protagonista de color si no se encuentra la textura
     */
    private void crearProtagonistaFallback(float ancho, float alto) {
        Material astroMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        astroMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.8f)); // Blanco semi-transparente
        astroMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        float astroSize = 200f;
        protagonistaGeometry = new Geometry("ProtagonistaFallback", new Quad(astroSize, astroSize));
        protagonistaGeometry.setMaterial(astroMat);
        
        float posX = (ancho / 2f) - (astroSize / 2f);
        float posY = 50f;
        protagonistaGeometry.setLocalTranslation(posX, posY, 0f);
        
        app.getGuiNode().attachChild(protagonistaGeometry);
        System.out.println("  ⚠ Protagonista creado como FALLBACK (cuadrado blanco)");
    }
    
    // ==================== INICIALIZACIÓN DE UI ====================
    
    /**
     * Inicialización de toda la UI
     */
    private void inicializarUI() {
        // 1. Crear GameplayUI (se auto-añade al GuiNode)
        gameplayUI = new GameplayUI((SimpleApplication) app);
        gameplayUI.actualizarVida(vida);
        gameplayUI.actualizarScore(score);
        gameplayUI.actualizarCombo(combo);
        
        // 2. Crear Menú de Pausa con callback de pausa/reanudación
        menuPausa = new MenuPausa(
            (SimpleApplication) app,
            () -> reanudarJuego(),
            () -> volverAlMenuPrincipal(),
            () -> toggleMusicaJuego(),
            (pausado) -> manejarCambioPausa(pausado)
        );
        
        System.out.println("✓ UI inicializada correctamente");
        System.out.println("  - GameplayUI: OK");
        System.out.println("  - MenuPausa: OK");
    }
    
    // ==================== CALLBACKS DEL MENÚ DE PAUSA ====================
    
    /**
     * Maneja el cambio de estado de pausa para el audio
     */
    private void manejarCambioPausa(boolean pausado) {
        if (audioNode == null) return;
        
        if (pausado) {
            // Pausar audio
            if (audioNode.getStatus() == AudioSource.Status.Playing) {
                audioNode.pause();
                System.out.println("  ✓ Audio pausado (GameplayAppState)");
            }
        } else {
            // Reanudar audio
            if (audioNode.getStatus() == AudioSource.Status.Paused) {
                audioNode.play();
                System.out.println("  ✓ Audio reanudado (GameplayAppState)");
            }
        }
    }
    
    private void reanudarJuego() {
        System.out.println("  → Callback reanudarJuego() ejecutado");
        // El audio ya se maneja en manejarCambioPausa
    }
    
    private void volverAlMenuPrincipal() {
        System.out.println("  → Volviendo al menú principal desde pausa");
        if (audioNode != null) {
            audioNode.stop();
        }
        
        app.enqueue(() -> {
            cleanup(app);
            app.volverAlMenu();
            return null;
        });
    }
    
    private void toggleMusicaJuego() {
        if (audioNode != null) {
            if (menuPausa.estaMusicaSilenciada()) {
                audioNode.setVolume(0f);
            } else {
                audioNode.setVolume(app.getMasterVolume());
            }
        }
    }
    
    // ==================== ⭐ UPDATE CORREGIDO ====================
    
    @Override
    public void update(float tpf) {
        // ⭐ CRÍTICO: PRIMERA verificación - No actualizar si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
            return; // SALIR INMEDIATAMENTE - NO ejecutar NADA más
        }
        
        // ⭐ CRÍTICO: SEGUNDA verificación - No actualizar si no está habilitado
        if (!isEnabled()) {
            return;
        }
        
        // ⭐ TERCERA verificación: Verificar si el juego terminó
        if (juegoTerminado && !resultadosMostrados) {
            mostrarVentanaResultados();
            resultadosMostrados = true;
            return;
        }
        
        // === A PARTIR DE AQUÍ, EL JUEGO ESTÁ ACTIVO ===
        
        tiempoTranscurrido += tpf;
        
        // Actualizar UI
        if (gameplayUI != null) {
            gameplayUI.actualizarFeedback(tpf);
            gameplayUI.actualizarCombo(combo);
        }
        
        generarFlechasPorTiempo();
        verificarMisses();
        
        // Verificar fin de canción con flag
        if (audioNode != null && audioNode.getStatus() == AudioSource.Status.Stopped && !juegoTerminado) {
            terminarJuego("cancion_finalizada");
        }
        
        // Verificar game over
        if (vida <= 0 && !juegoTerminado) {
            terminarJuego("game_over");
        }
    }
    
    /**
     * ⭐ CRÍTICO: Método centralizado para terminar el juego
     * NO llama a volverAlMenuPrincipal() automáticamente
     */
    private void terminarJuego(String razon) {
        if (juegoTerminado) {
            return; // Ya se llamó antes
        }
        
        juegoTerminado = true;
        System.out.println("\n=== JUEGO TERMINADO: " + razon + " ===");
        
        // Detener música
        if (audioNode != null) {
            audioNode.stop();
        }
        
        // Pausar el gameplay (deshabilitarlo temporalmente)
        this.setEnabled(false);
        
        // ⭐ NO llamar a volverAlMenuPrincipal() aquí
        // La ventana de resultados decidirá qué hacer
    }
    
    /**
     * Mostrar ventana de resultados (SOLO UNA VEZ)
     */
    private void mostrarVentanaResultados() {
        if (procesandoResultados) {
            return;
        }
        
        procesandoResultados = true;
        System.out.println("\n=== MOSTRANDO RESULTADOS ===");
        
        final String nombreCancion = canciones.get(cancionActual);
        final int scoreFinal = score;
        final int comboFinal = maxCombo;
        final int perfectosFinal = perfectos;
        final int buenosFinal = buenos;
        final int malosFinal = malos;
        final int missesFinal = misses;
        final int vidaFinal = vida;
        
        // Ocultar UI del gameplay
        if (gameplayUI != null) {
            gameplayUI.ocultarTemporalmente();
        }
        
        // Esperar 1 segundo antes de mostrar resultados
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            SwingUtilities.invokeLater(() -> {
                java.awt.Frame parentFrame = obtenerFramePadre();
                
                boolean continuar = VentanaResultados.mostrarResultados(
                    parentFrame,
                    nombreCancion,
                    scoreFinal,
                    comboFinal,
                    perfectosFinal,
                    buenosFinal,
                    malosFinal,
                    missesFinal,
                    vidaFinal
                );
                
                app.enqueue(() -> {
                    if (continuar && cancionActual + 1 < canciones.size()) {
                        siguienteCancion();
                    } else {
                        finalizarJuegoCompleto();
                    }
                    return null;
                });
            });
        }, "ResultadosThread").start();
    }
    
    /**
     * Ir a la siguiente canción
     */
    private void siguienteCancion() {
        cancionActual++;
        String siguienteCancion = canciones.get(cancionActual);
        analisisActual = analisisCompleto.get(siguienteCancion);
        
        // Reiniciar estado
        reiniciarEstadoParaNuevaCancion();
        
        System.out.println("\n=== SIGUIENTE CANCIÓN ===");
        tiempoTranscurrido = 0f;
        flechasActivas.clear();
        
        // Limpiar escena (solo flechas, NO el fondo ni el protagonista)
        gameNode.detachAllChildren();
        
        // Restaurar UI
        if (gameplayUI != null) {
            gameplayUI.mostrarNuevamente();
            gameplayUI.actualizarVida(vida);
            gameplayUI.actualizarScore(score);
            gameplayUI.actualizarCombo(combo);
        }
        
        generarFlechasCancion();
        
        // Resetear flags
        juegoTerminado = false;
        resultadosMostrados = false;
        procesandoResultados = false;
        
        this.setEnabled(true);
        reproducirCancionActual();
    }
    
    /**
     * Reiniciar estadísticas para nueva canción
     */
    private void reiniciarEstadoParaNuevaCancion() {
        perfectos = 0;
        buenos = 0;
        malos = 0;
        misses = 0;
        combo = 0;
        juegoTerminado = false;
        resultadosMostrados = false;
        procesandoResultados = false;
    }
    
    /**
     * Finalizar el juego completo (todas las canciones)
     */
    private void finalizarJuegoCompleto() {
        System.out.println("\n=== ¡JUEGO COMPLETADO! ===");
        mostrarEstadisticas();
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            app.enqueue(() -> {
                cleanup(app);
                app.volverAlMenu();
                return null;
            });
        }, "FinJuegoThread").start();
    }
    
    private void generarFlechasCancion() {
        if (analisisActual == null) {
            System.err.println("ERROR: No hay análisis disponible");
            return;
        }
        
        System.out.println("\n=== GENERANDO FLECHAS PARA GAMEPLAY ===");
        flechasAGenerar = flechasGenerator.generarFlechasCompletas(analisisActual);
        indiceFlechaActual = 0;
        
        System.out.println("Total de flechas a generar: " + flechasAGenerar.size());
        
        if (flechasAGenerar.isEmpty()) {
            System.err.println("⚠ ADVERTENCIA: No se generaron flechas para esta canción");
        }
    }
    
    private void reproducirCancionActual() {
        if (cancionActual >= canciones.size()) {
            System.out.println("Todas las canciones completadas");
            return;
        }
        
        String rutaCancion = canciones.get(cancionActual);
        String rutaRelativa = rutaCancion;
        
        if (rutaCancion.contains("assets" + java.io.File.separator)) {
            int index = rutaCancion.indexOf("assets" + java.io.File.separator);
            rutaRelativa = rutaCancion.substring(index + 7);
        } else if (rutaCancion.contains("assets/")) {
            int index = rutaCancion.indexOf("assets/");
            rutaRelativa = rutaCancion.substring(index + 7);
        }
        
        rutaRelativa = rutaRelativa.replace("\\", "/");
        
        System.out.println("Cargando audio:");
        System.out.println("  Ruta original: " + rutaCancion);
        System.out.println("  Ruta relativa: " + rutaRelativa);
        
        try {
            audioNode = new AudioNode(assetManager, rutaRelativa, false);
            audioNode.setPositional(false);
            audioNode.setVolume(app.getMasterVolume());
            audioNode.play();
            
            System.out.println("✓ Reproduciendo: " + rutaRelativa);
            
            if (gameplayUI != null) {
                gameplayUI.mostrarCancion(rutaRelativa);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR al cargar audio: " + e.getMessage());
            e.printStackTrace();
            
            // Si falla la carga, intentar siguiente canción
            if (cancionActual + 1 < canciones.size()) {
                cancionActual++;
                reproducirCancionActual();
            } else {
                finalizarJuegoCompleto();
            }
        }
    }
    
    private void generarFlechasPorTiempo() {
        while (indiceFlechaActual < flechasAGenerar.size()) {
            FlechaData flechaData = flechasAGenerar.get(indiceFlechaActual);
            float tiempoGeneracion = flechaData.getBeatTime() - tiempoAnticipacion;
            
            if (tiempoTranscurrido >= tiempoGeneracion) {
                crearFlechaEnPantalla(flechaData);
                indiceFlechaActual++;
            } else {
                break;
            }
        }
    }
    
    /**
     * Creación de flechas con transparencia y anclaje al GuiNode
     */
    private void crearFlechaEnPantalla(FlechaData flechaData) {
        float tamano = 70f;
        Quad quad = new Quad(tamano, tamano);
        Geometry flechaGeom = new Geometry("Flecha", quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        String rutaTextura;
        if (flechaData.getTipo() == TipoFlecha.LUNA) {
            rutaTextura = "Textures/flecha_especial_luna.png";
        } else if (flechaData.getTipo() == TipoFlecha.DORADA) {
            rutaTextura = "Textures/flecha_dorada.png";
        } else {
            rutaTextura = flechaData.getDireccion().getTexturePath();
        }
        
        try {
            Texture textura = assetManager.loadTexture(rutaTextura);
            mat.setTexture("ColorMap", textura);
            
            // ⭐ CRÍTICO: Activar transparencia (Alpha Blending)
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.setColor("Color", ColorRGBA.White);
        } catch (Exception e) {
            System.err.println("  ❌ ERROR cargando textura: " + rutaTextura);
            // Fallback: color sólido
            mat.setColor("Color", flechaData.getDireccion().getColor());
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }
        
        flechaGeom.setMaterial(mat);
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        Vector3f posInicial = flechaData.getDireccion().getPosicionSpawn(ancho, alto);
        Vector3f posTarget = flechaData.getDireccion().getPosicionTarget(ancho, alto);
        
        flechaGeom.setLocalTranslation(posInicial.x - tamano/2, posInicial.y - tamano/2, 1); // Z=1 delante del protagonista
        
        float rotacion = flechaData.getDireccion().getRotacion();
        if (rotacion != 0) {
            flechaGeom.rotate(0, 0, rotacion * FastMath.DEG_TO_RAD);
        }
        
        FlechaControl control = new FlechaControl(
            flechaData.getTipo(),
            flechaData.getDireccion(),
            posInicial,
            posTarget,
            flechaData.getVelocidad(),
            flechaData.getBeatTime(),
            mat
        );
        
        flechaGeom.addControl(control);
        
        // ⭐ CRÍTICO: Anclar al gameNode (que se añade al GuiNode en onEnable)
        gameNode.attachChild(flechaGeom);
        flechasActivas.add(flechaGeom);
    }
    
    private void verificarMisses() {
        Iterator<Geometry> iterator = flechasActivas.iterator();
        while (iterator.hasNext()) {
            Geometry flecha = iterator.next();
            FlechaControl control = flecha.getControl(FlechaControl.class);
            
            if (control != null && control.fueErrada() && !control.fueGolpeada()) {
                registrarMiss();
                flecha.removeFromParent();
                iterator.remove();
            } else if (control != null && control.fueGolpeada()) {
                if (flecha.getParent() == null) {
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * Setup de inputs SIN ESC (lo maneja MenuPausa)
     */
    private void setupInputs() {
        mappingTeclas = new HashMap<>();
        mappingTeclas.put(Direccion.ARRIBA, "Arriba");
        mappingTeclas.put(Direccion.ABAJO, "Abajo");
        mappingTeclas.put(Direccion.IZQUIERDA, "Izquierda");
        mappingTeclas.put(Direccion.DERECHA, "Derecha");
        mappingTeclas.put(Direccion.ESPACIO, "Espacio");
        
        // Eliminar mapeos previos si existen
        try {
            app.getInputManager().deleteMapping("Arriba");
            app.getInputManager().deleteMapping("Abajo");
            app.getInputManager().deleteMapping("Izquierda");
            app.getInputManager().deleteMapping("Derecha");
            app.getInputManager().deleteMapping("Espacio");
        } catch (Exception e) {
            // Ignorar si no existían
        }
        
        // Mapear teclas de direcciones
        app.getInputManager().addMapping("Arriba", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("Abajo", new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("Derecha", new KeyTrigger(KeyInput.KEY_RIGHT));
        app.getInputManager().addMapping("Espacio", new KeyTrigger(KeyInput.KEY_SPACE));
        
        // Registrar este AppState como ActionListener
        app.getInputManager().addListener(this, "Arriba", "Abajo", "Izquierda", "Derecha", "Espacio");
        
        System.out.println("✓ Inputs configurados correctamente");
        System.out.println("  - ESC está manejado por MenuPausa");
    }
    
    /**
     * ⭐ IMPLEMENTACIÓN DE ActionListener.onAction
     * Maneja los inputs de las flechas
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        
        // ⭐ CRÍTICO: BLOQUEAR inputs si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
            return; // NO PROCESAR inputs de flechas durante pausa
        }
        
        // No procesar si el juego terminó
        if (juegoTerminado) {
            return;
        }
        
        Direccion direccionPresionada = null;
        for (Map.Entry<Direccion, String> entry : mappingTeclas.entrySet()) {
            if (entry.getValue().equals(name)) {
                direccionPresionada = entry.getKey();
                break;
            }
        }
        
        if (direccionPresionada != null) {
            procesarInput(direccionPresionada);
        }
    }
    
    private void procesarInput(Direccion direccion) {
        FlechaControl flechaMasCercana = null;
        float menorDelay = Float.MAX_VALUE;
        
        for (Geometry flecha : flechasActivas) {
            FlechaControl control = flecha.getControl(FlechaControl.class);
            
            if (control == null || control.fueGolpeada() || control.fueErrada()) {
                continue;
            }
            
            Direccion direccionFlecha = control.getDireccion();
            
            if (control.getTipo() == TipoFlecha.DORADA && control.estaInvertida()) {
                direccionFlecha = direccionFlecha.getOpuesta();
            }
            
            if (direccionFlecha == direccion) {
                float delay = control.calcularDelay(tiempoTranscurrido);
                if (delay < menorDelay && delay < 0.3f) {
                    menorDelay = delay;
                    flechaMasCercana = control;
                }
            }
        }
        
        if (flechaMasCercana != null) {
            evaluarHit(flechaMasCercana, menorDelay);
            flechaMasCercana.brillar();
        }
    }
    
    private void evaluarHit(FlechaControl flecha, float delay) {
        int puntosBase;
        String feedback;
        ColorRGBA colorFeedback;
        
        if (delay < 0.05f) {
            puntosBase = 100;
            feedback = "¡PERFECTO!";
            colorFeedback = new ColorRGBA(0, 1, 0.5f, 1);
            perfectos++;
            combo++;
        } else if (delay < 0.15f) {
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = ColorRGBA.Yellow;
            buenos++;
            combo++;
        } else {
            puntosBase = 20;
            feedback = "MALO";
            colorFeedback = ColorRGBA.Orange;
            malos++;
            combo = 0;
        }
        
        int puntos = (int)(puntosBase * flecha.getTipo().getMultiplicadorPuntos());
        
        if (combo > 5) {
            puntos = (int)(puntos * (1 + combo * 0.1f));
        }
        
        score += puntos;
        
        if (combo > maxCombo) {
            maxCombo = combo;
        }
        
        if (gameplayUI != null) {
            gameplayUI.actualizarScore(score);
            gameplayUI.mostrarFeedback(feedback, colorFeedback);
        }
        
        System.out.println(String.format("%s | +%d pts | Combo: %d | Delay: %.3fs", feedback, puntos, combo, delay));
    }
    
    private void registrarMiss() {
        misses++;
        combo = 0;
        vida -= 10;
        
        if (gameplayUI != null) {
            gameplayUI.actualizarVida(vida);
            gameplayUI.mostrarFeedback("MISS!", ColorRGBA.Red);
        }
        
        System.out.println("MISS! Vida: " + vida);
    }
    
    private java.awt.Frame obtenerFramePadre() {
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            if (window instanceof java.awt.Frame) {
                return (java.awt.Frame) window;
            }
        }
        return null;
    }
    
    private void mostrarEstadisticas() {
        System.out.println("\n=== ESTADÍSTICAS FINALES ===");
        System.out.println("Score Final: " + score);
        System.out.println("Combo Máximo: " + maxCombo);
        System.out.println("Perfectos: " + perfectos);
        System.out.println("Buenos: " + buenos);
        System.out.println("Malos: " + malos);
        System.out.println("Misses: " + misses);
        
        int totalNotas = perfectos + buenos + malos + misses;
        if (totalNotas > 0) {
            float precision = ((float)(perfectos + buenos) / totalNotas) * 100;
            System.out.println(String.format("Precisión: %.1f%%", precision));
        }
        
        System.out.println("===========================\n");
    }
    
    @Override
    protected void cleanup(Application app) {
        System.out.println("🧹 Limpiando GameplayAppState...");
        
        if (audioNode != null) {
            audioNode.stop();
            audioNode = null;
        }
        
        // ⭐ NUEVO: Limpiar fondo y protagonista
        if (fondoGeometry != null) {
            fondoGeometry.removeFromParent();
            fondoGeometry = null;
        }
        
        if (protagonistaGeometry != null) {
            protagonistaGeometry.removeFromParent();
            protagonistaGeometry = null;
        }
        
        if (gameplayUI != null) {
            gameplayUI.limpiar();
            gameplayUI = null;
        }
        
        if (menuPausa != null) {
            menuPausa.limpiar();
            menuPausa = null;
        }
        
        // Limpiar inputs
        try {
            app.getInputManager().removeListener(this);
            app.getInputManager().deleteMapping("Arriba");
            app.getInputManager().deleteMapping("Abajo");
            app.getInputManager().deleteMapping("Izquierda");
            app.getInputManager().deleteMapping("Derecha");
            app.getInputManager().deleteMapping("Espacio");
        } catch (Exception e) {
            System.err.println("Error limpiando inputs: " + e.getMessage());
        }
        
        gameNode.detachAllChildren();
        flechasActivas.clear();
        
        System.out.println("✓ GameplayAppState limpiado");
    }
    
    @Override
    protected void onEnable() {
        // ⭐ CRÍTICO: Anclar gameNode al GuiNode (para sprites 2D)
        app.getGuiNode().attachChild(gameNode);
        
        System.out.println("▶ GameplayAppState habilitado - gameNode añadido al GuiNode");
    }
    
    @Override
    protected void onDisable() {
        gameNode.removeFromParent();
        
        System.out.println("⏸ GameplayAppState deshabilitado");
    }
}