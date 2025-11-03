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
 * GameplayAppState - CORREGIDO CON:
 * ✅ Sistema de pausa funcional con ESC (sin cerrar la app)
 * ✅ Flechas ancladas correctamente al GuiNode
 * ✅ Materiales con transparencia (Alpha Blending)
 * ✅ ActionListener implementado
 */
public class GameplayAppState extends BaseAppState implements ActionListener {

    private MiJuegoDeRitmo app;
    private Node gameNode; // Para flechas en movimiento (2D sobre GUI)
    private AssetManager assetManager;

    // ⭐ NUEVO: Flags de control de ciclo de vida
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
        
        // ⭐ CRÍTICO: gameNode debe ser un Node 2D para sprites sobre GuiNode
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

        // Setup inputs ANTES de crear UI (para evitar conflictos)
        setupInputs();

        // ⭐ CRÍTICO: Inicializar UI después de inputs
        inicializarUI();

        // Generar flechas
        generarFlechasCancion();

        // Reproducir música
        reproducirCancionActual();

        System.out.println("✓ Gameplay inicializado correctamente");
        System.out.println("  - Vida: " + vida);
        System.out.println("  - Flechas a generar: " + flechasAGenerar.size());
    }

    /**
     * ⭐ NUEVO: Método separado para inicializar toda la UI
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
            (pausado) -> manejarCambioPausa(pausado) // ⭐ NUEVO: Callback para audio
        );

        System.out.println("✓ UI inicializada correctamente");
        System.out.println("  - GameplayUI: OK");
        System.out.println("  - MenuPausa: OK");
    }

    // ==================== CALLBACKS DEL MENÚ DE PAUSA ====================

    /**
     * ⭐ NUEVO: Maneja el cambio de estado de pausa para el audio
     */
    private void manejarCambioPausa(boolean pausado) {
        if (audioNode == null) return;
        
        if (pausado) {
            audioNode.pause();
            System.out.println("⏸ Audio pausado");
        } else {
            audioNode.play();
            System.out.println("▶ Audio reanudado");
        }
    }

    private void reanudarJuego() {
        System.out.println("▶ Gameplay reanudado");
    }

    private void volverAlMenuPrincipal() {
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

    // ==================== UPDATE ====================

    @Override
    public void update(float tpf) {
        // ⭐ CRÍTICO: No actualizar si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
            return;
        }

        // ⭐ CRÍTICO: No actualizar si no está habilitado
        if (!isEnabled()) {
            return;
        }

        // ⭐ NUEVO: Verificar si el juego terminó
        if (juegoTerminado && !resultadosMostrados) {
            mostrarVentanaResultados();
            resultadosMostrados = true;
            return;
        }

        tiempoTranscurrido += tpf;

        // Actualizar UI
        if (gameplayUI != null) {
            gameplayUI.actualizarFeedback(tpf);
            gameplayUI.actualizarCombo(combo);
        }

        generarFlechasPorTiempo();
        verificarMisses();

        // ⭐ CORREGIDO: Verificar fin de canción con flag
        if (audioNode != null && audioNode.getStatus() == AudioSource.Status.Stopped && !juegoTerminado) {
            terminarJuego("cancion_finalizada");
        }

        // Verificar game over
        if (vida <= 0 && !juegoTerminado) {
            terminarJuego("game_over");
        }
    }

    /**
     * ⭐ NUEVO: Método centralizado para terminar el juego
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

        // Pausar el gameplay
        this.setEnabled(false);
    }

    /**
     * ⭐ NUEVO: Mostrar ventana de resultados (SOLO UNA VEZ)
     */
    private void mostrarVentanaResultados() {
        if (procesandoResultados) {
            return; // Ya se está procesando
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
     * ⭐ NUEVO: Ir a la siguiente canción
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

        // Limpiar escena
        gameNode.detachAllChildren();

        // Restaurar UI
        if (gameplayUI != null) {
            gameplayUI.mostrarNuevamente();
            gameplayUI.actualizarVida(vida);
            gameplayUI.actualizarScore(score);
            gameplayUI.actualizarCombo(combo);
        }

        generarFlechasCancion();

        // ⭐ Resetear flags
        juegoTerminado = false;
        resultadosMostrados = false;
        procesandoResultados = false;

        this.setEnabled(true);

        reproducirCancionActual();
    }

    /**
     * ⭐ NUEVO: Reiniciar estadísticas para nueva canción
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
     * ⭐ NUEVO: Finalizar el juego completo (todas las canciones)
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

        // ⭐ VERIFICACIÓN: Si no hay flechas, advertir
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
     * ⭐ CORREGIDO: Creación de flechas con transparencia y anclaje al GuiNode
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

        System.out.println("📂 Cargando textura: " + rutaTextura);

        try {
            Texture textura = assetManager.loadTexture(rutaTextura);
            mat.setTexture("ColorMap", textura);
            
            // ⭐ CRÍTICO: Activar transparencia (Alpha Blending)
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.setColor("Color", ColorRGBA.White);

            System.out.println("  ✓ Textura cargada correctamente");

        } catch (Exception e) {
            System.err.println("  ❌ ERROR cargando textura: " + rutaTextura);
            System.err.println("  " + e.getMessage());

            // Fallback: color sólido
            mat.setColor("Color", flechaData.getDireccion().getColor());
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        flechaGeom.setMaterial(mat);

        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();

        Vector3f posInicial = flechaData.getDireccion().getPosicionSpawn(ancho, alto);
        Vector3f posTarget = flechaData.getDireccion().getPosicionTarget(ancho, alto);

        flechaGeom.setLocalTranslation(posInicial.x - tamano/2, posInicial.y - tamano/2, 0);

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
     * ⭐ CORREGIDO: Setup de inputs SIN ESC (lo maneja MenuPausa)
     */
    private void setupInputs() {
        mappingTeclas = new HashMap<>();
        mappingTeclas.put(Direccion.ARRIBA, "Arriba");
        mappingTeclas.put(Direccion.ABAJO, "Abajo");
        mappingTeclas.put(Direccion.IZQUIERDA, "Izquierda");
        mappingTeclas.put(Direccion.DERECHA, "Derecha");
        mappingTeclas.put(Direccion.ESPACIO, "Espacio");

        // ⭐ CRÍTICO: Eliminar mapeos previos si existen
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

        // ⭐ CRÍTICO: Registrar este AppState como ActionListener
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

        // No procesar si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
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