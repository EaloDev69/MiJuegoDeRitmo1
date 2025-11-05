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
import com.jme3.material.RenderState.BlendMode;
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
import com.jme3.texture.Texture;


public class GameplayAppState extends BaseAppState implements ActionListener {

    private MiJuegoDeRitmo app;
    private Node gameNode;
    private AssetManager assetManager;
    
    // Referencias al fondo y protagonista
    private Geometry fondoGeometry;
    private Geometry protagonistaGeometry;
    
    // Flags de control de ciclo de vida
    private boolean juegoTerminado = false;
    private boolean resultadosMostrados = false;
    private boolean procesandoResultados = false;
    
    private MenuPausa menuPausa;
    private GameplayUI gameplayUI;

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
        
        // Crear fondo y protagonista PRIMERO
        crearFondo();
        crearProtagonista();
        setupInputs();
        inicializarUI();

        generarFlechasCancion();
        reproducirCancionActual();
        
        System.out.println("✓ Gameplay inicializado correctamente");
        System.out.println("  - Vida: " + vida);
        System.out.println("  - Flechas a generar: " + flechasAGenerar.size());
        System.out.println("  - Fondo y protagonista cargados");
    }

    private void crearFondo() {
        System.out.println("\n🌌 Creando fondo espacial...");
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        String rutaFondo = "assets/Texture/espacio.png";
        
        try {
            System.out.println("  → Cargando fondo desde: " + rutaFondo);
            

            Texture fondoTexture = assetManager.loadTexture(rutaFondo);
            

            Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            fondoMat.setTexture("ColorMap", fondoTexture);
            fondoMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

            fondoGeometry = new Geometry("FondoGameplay", new Quad(ancho, alto));
            fondoGeometry.setMaterial(fondoMat);
            

            fondoGeometry.setLocalTranslation(0, 0, -1f);

            app.getGuiNode().attachChild(fondoGeometry);
            
            System.out.println("  ✓ Fondo cargado correctamente");
            System.out.println("  - Dimensiones: " + ancho + "x" + alto);
            
        } catch (Exception e) {
            System.err.println("  ❌ No se pudo cargar el fondo: " + e.getMessage());
            System.err.println("  ⚠ Creando fondo de respaldo...");
            crearFondoFallback(ancho, alto);
        }
    }

    private void crearFondoFallback(float ancho, float alto) {
        Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fondoMat.setColor("Color", new ColorRGBA(0.05f, 0.05f, 0.15f, 1f)); // Azul oscuro espacial
        fondoMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        fondoGeometry = new Geometry("FondoFallback", new Quad(ancho, alto));
        fondoGeometry.setMaterial(fondoMat);
        fondoGeometry.setLocalTranslation(0, 0, -1f);
        
        app.getGuiNode().attachChild(fondoGeometry);
        System.out.println("  ✓ Fondo de respaldo creado (azul oscuro espacial)");
    }

  
    private void crearProtagonista() {
        System.out.println("\n👨‍🚀 Creando protagonista...");
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        

        String rutaProtagonista = "assets/Texture/Protagonista/astronautaPoseDefault.png";
        
        try {
            System.out.println("  → Cargando protagonista desde: " + rutaProtagonista);

            Texture astroTexture = assetManager.loadTexture(rutaProtagonista);

            Material astroMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            astroMat.setTexture("ColorMap", astroTexture);
            
            astroMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

            float astroSize = 200f;

            protagonistaGeometry = new Geometry("Protagonista", new Quad(astroSize, astroSize));
            protagonistaGeometry.setMaterial(astroMat);
            
            float posX = (ancho / 2f) - (astroSize / 2f);
            float posY = 50f;
            protagonistaGeometry.setLocalTranslation(posX, posY, 0f);

            app.getGuiNode().attachChild(protagonistaGeometry);
            
            System.out.println("  ✓ Protagonista cargado correctamente");
            System.out.println("  - Tamaño: " + astroSize + "x" + astroSize);
            System.out.println("  - Posición: (" + posX + ", " + posY + ", 0)");
            
        } catch (Exception e) {
            System.err.println("  ❌ No se pudo cargar el protagonista: " + e.getMessage());
            System.err.println("  ⚠ Creando protagonista de respaldo...");
            crearProtagonistaFallback(ancho, alto);
        }
    }
    

    private void crearProtagonistaFallback(float ancho, float alto) {
        Material astroMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        astroMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.9f)); // Blanco semi-transparente
        astroMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        float astroSize = 200f;
        protagonistaGeometry = new Geometry("ProtagonistaFallback", new Quad(astroSize, astroSize));
        protagonistaGeometry.setMaterial(astroMat);
        
        float posX = (ancho / 2f) - (astroSize / 2f);
        float posY = 50f;
        protagonistaGeometry.setLocalTranslation(posX, posY, 0f);
        
        app.getGuiNode().attachChild(protagonistaGeometry);
        System.out.println("  ✓ Protagonista de respaldo creado (cuadrado blanco)");
    }

    // ==================== INICIALIZACIÓN DE UI ====================
    
    private void inicializarUI() {
        // 1. Crear GameplayUI
        gameplayUI = new GameplayUI((SimpleApplication) app);
        gameplayUI.actualizarVida(vida);
        gameplayUI.actualizarScore(score);
        gameplayUI.actualizarCombo(combo);
        
        // 2. Crear Menú de Pausa
        menuPausa = new MenuPausa(
            (SimpleApplication) app,
            () -> reanudarJuego(),
            () -> volverAlMenuPrincipal(),
            () -> toggleMusicaJuego(),
            (pausado) -> manejarCambioPausa(pausado)
        );
        
        System.out.println("✓ UI inicializada correctamente");
    }

    // ==================== CALLBACKS DEL MENÚ DE PAUSA ====================
    
    private void manejarCambioPausa(boolean pausado) {
        if (audioNode == null) return;
        
        if (pausado) {
            if (audioNode.getStatus() == AudioSource.Status.Playing) {
                audioNode.pause();
                System.out.println("  ✓ Audio pausado");
            }
        } else {
            if (audioNode.getStatus() == AudioSource.Status.Paused) {
                audioNode.play();
                System.out.println("  ✓ Audio reanudado");
            }
        }
    }
    
    private void reanudarJuego() {
        System.out.println("  → Reanudando juego");
    }
    
    private void volverAlMenuPrincipal() {
        System.out.println("  → Volviendo al menú principal");
        
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
        // ⭐ CRÍTICO: Bloquear si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
            return;
        }
        
        if (!isEnabled()) {
            return;
        }
        
        if (juegoTerminado && !resultadosMostrados) {
            mostrarVentanaResultados();
            resultadosMostrados = true;
            return;
        }
        
        tiempoTranscurrido += tpf;
        
        if (gameplayUI != null) {
            gameplayUI.actualizarFeedback(tpf);
            gameplayUI.actualizarCombo(combo);
        }
        
        generarFlechasPorTiempo();
        verificarMisses();
        
        if (audioNode != null && audioNode.getStatus() == AudioSource.Status.Stopped && !juegoTerminado) {
            terminarJuego("cancion_finalizada");
        }
        
        if (vida <= 0 && !juegoTerminado) {
            terminarJuego("game_over");
        }
    }
    
    private void terminarJuego(String razon) {
        if (juegoTerminado) return;
        
        juegoTerminado = true;
        System.out.println("\n=== JUEGO TERMINADO: " + razon + " ===");
        
        if (audioNode != null) {
            audioNode.stop();
        }
        
        this.setEnabled(false);
    }
    
    private void mostrarVentanaResultados() {
        if (procesandoResultados) return;
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
        
        if (gameplayUI != null) {
            gameplayUI.ocultarTemporalmente();
        }
        
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
    
    private void siguienteCancion() {
        cancionActual++;
        String siguienteCancion = canciones.get(cancionActual);
        analisisActual = analisisCompleto.get(siguienteCancion);
        
        reiniciarEstadoParaNuevaCancion();
        
        System.out.println("\n=== SIGUIENTE CANCIÓN ===");
        
        tiempoTranscurrido = 0f;
        flechasActivas.clear();
        gameNode.detachAllChildren();
        
        if (gameplayUI != null) {
            gameplayUI.mostrarNuevamente();
            gameplayUI.actualizarVida(vida);
            gameplayUI.actualizarScore(score);
            gameplayUI.actualizarCombo(combo);
        }
        
        generarFlechasCancion();
        
        juegoTerminado = false;
        resultadosMostrados = false;
        procesandoResultados = false;
        
        this.setEnabled(true);
        reproducirCancionActual();
    }
    
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
            System.err.println("⚠ ADVERTENCIA: No se generaron flechas");
        }
    }
    
    private void reproducirCancionActual() {
        if (cancionActual >= canciones.size()) {
            System.out.println("Todas las canciones completadas");
            return;
        }
        
        String rutaCancion = canciones.get(cancionActual);
        
        // ⭐ CORREGIR RUTA: Cambiar "assets/canciones/" por "assets/canciones/"
        String rutaRelativa = rutaCancion;
        if (rutaCancion.contains("assets" + java.io.File.separator)) {
            int index = rutaCancion.indexOf("assets" + java.io.File.separator);
            rutaRelativa = rutaCancion.substring(index);
        } else if (rutaCancion.contains("assets/")) {
            int index = rutaCancion.indexOf("assets/");
            rutaRelativa = rutaCancion.substring(index);
        }
        
        rutaRelativa = rutaRelativa.replace("\\", "/");
        
        System.out.println("Cargando audio:");
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
     * ⭐ CORREGIDO: Creación de flechas con rutas corregidas
     */
    private void crearFlechaEnPantalla(FlechaData flechaData) {
        float tamano = 70f;
        Quad quad = new Quad(tamano, tamano);
        Geometry flechaGeom = new Geometry("Flecha", quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // ⭐ RUTAS CORREGIDAS según tu estructura
        String rutaTextura;
        if (flechaData.getTipo() == TipoFlecha.LUNA) {
            rutaTextura = "assets/Texture/flecha_especial_luna.png";
        } else if (flechaData.getTipo() == TipoFlecha.DORADA) {
            rutaTextura = "assets/Texture/flecha_dorada.png";
        } else {
            // Para flechas normales, usar las rutas definidas en Direccion
            rutaTextura = flechaData.getDireccion().getTexturePath();
        }
        
        try {
            Texture textura = assetManager.loadTexture(rutaTextura);
            mat.setTexture("ColorMap", textura);
            
            // ⭐ CRÍTICO: Activar transparencia para las flechas
            mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            mat.setColor("Color", ColorRGBA.White);
            
        } catch (Exception e) {
            System.err.println("  ❌ ERROR cargando textura: " + rutaTextura);
            // Fallback: color sólido
            mat.setColor("Color", flechaData.getDireccion().getColor());
            mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        }
        
        flechaGeom.setMaterial(mat);
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        Vector3f posInicial = flechaData.getDireccion().getPosicionSpawn(ancho, alto);
        Vector3f posTarget = flechaData.getDireccion().getPosicionTarget(ancho, alto);
        
        flechaGeom.setLocalTranslation(posInicial.x - tamano/2, posInicial.y - tamano/2, 1);
        
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
     * ⭐ CORREGIDO: Setup de inputs con ESC para pausa
     */
    private void setupInputs() {
        mappingTeclas = new HashMap<>();
        mappingTeclas.put(Direccion.ARRIBA, "Arriba");
        mappingTeclas.put(Direccion.ABAJO, "Abajo");
        mappingTeclas.put(Direccion.IZQUIERDA, "Izquierda");
        mappingTeclas.put(Direccion.DERECHA, "Derecha");
        mappingTeclas.put(Direccion.ESPACIO, "Espacio");
        
        // Eliminar mapeos previos
        try {
            app.getInputManager().deleteMapping("Arriba");
            app.getInputManager().deleteMapping("Abajo");
            app.getInputManager().deleteMapping("Izquierda");
            app.getInputManager().deleteMapping("Derecha");
            app.getInputManager().deleteMapping("Espacio");
        } catch (Exception e) {
            // Ignorar
        }
        
        // Mapear teclas
        app.getInputManager().addMapping("Arriba", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("Abajo", new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("Derecha", new KeyTrigger(KeyInput.KEY_RIGHT));
        app.getInputManager().addMapping("Espacio", new KeyTrigger(KeyInput.KEY_SPACE));
        
        // Registrar listener
        app.getInputManager().addListener(this, "Arriba", "Abajo", "Izquierda", "Derecha", "Espacio");
        
        System.out.println("✓ Inputs configurados correctamente");
        System.out.println("  - ESC está manejado por MenuPausa");
    }

    /**
     * ⭐ Implementación de ActionListener
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        
        // ⭐ CRÍTICO: BLOQUEAR inputs si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
            return;
        }
        
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
        
        // Limpiar fondo y protagonista
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
        app.getGuiNode().attachChild(gameNode);
        System.out.println("▶ GameplayAppState habilitado");
    }

    @Override
    protected void onDisable() {
        gameNode.removeFromParent();
        System.out.println("⏸ GameplayAppState deshabilitado");
    }
}
