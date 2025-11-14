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
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.audio.plugins.WAVLoader;
import com.jme3.asset.AssetInfo;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * GameplayAppState - Sistema de juego completo con mejoras visuales
 * ✅ Sistema de input mejorado con WASD + Flechas
 * ✅ Objeto central reactivo con pulso rítmico
 * ✅ Feedback visual sincronizado con la música
 * ✅ Detección de hits precisa con ventanas configurables
 * ✅ TODAS LAS FIRMAS DE MÉTODOS CORREGIDAS
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class GameplayAppState extends BaseAppState implements ActionListener {
    
    private MiJuegoDeRitmo app;
    private Node gameNode;
    private AssetManager assetManager;
    
    // Referencias visuales
    private Geometry fondoGeometry;
    private Geometry protagonistaGeometry;
    
    // Control del juego
    private boolean juegoTerminado = false;
    private boolean resultadosMostrados = false;
    private boolean procesandoResultados = false;
    
    // UI
    private MenuPausa menuPausa;
    private GameplayUI gameplayUI;
    
    // Sistema de flechas
    private FlechasGenerator flechasGenerator;
    private List<FlechaData> flechasAGenerar;
    private int indiceFlechaActual = 0;
    private List<Geometry> flechasActivas;

    // Mecánica ESPACIO (parpadeos dobles sin flechas)
    private List<Float> eventosEspacio;
    private int indiceEventoEspacio = 0;
    private boolean blink1Disparado = false;
    private boolean blink2Disparado = false;
    
    // Sistema de playlist
    private List<String> canciones;
    private Map<String, ResultadoAnalisis> analisisCompleto;
    private int cancionActual = 0;
    private AudioNode audioNode;
    private ResultadoAnalisis analisisActual;
    
    // Timing
    private float tiempoTranscurrido = 0f;
    private float tiempoAnticipacion = 2.0f;
    
    // Puntuación y estadísticas
    private int score = 0;
    private int vida = 100;
    private int combo = 0;
    private int maxCombo = 0;
    private int perfectos = 0;
    private int buenos = 0;
    private int malos = 0;
    private int misses = 0;
    
    // Sistema de input mejorado
    private Map<Direccion, Boolean> teclasPresionadas;
    private Map<Direccion, Float> ultimoTiempoInput;
    private float cooldownInput = 0.1f;
    
    // Ventanas de timing configurables
    private static final float VENTANA_PERFECTA = 0.05f;  // 50ms
    private static final float VENTANA_BUENA = 0.15f;     // 150ms
    private static final float VENTANA_MALA = 0.30f;      // 300ms
    private static final float VENTANA_MISS = 0.40f;      // 400ms
    
    // Sistema de detección de hits mejorado
    private Set<FlechaControl> flechasProcesadas;
    
    // Otros
    private Vector3f centroPantalla;
    private Map<Direccion, String> mappingTeclas;
    // Modo práctica: sin barra de vida ni game over por vida
    private boolean modoPractica = false;
    
    // ==================== CONSTRUCTOR ====================
    
    public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> analisis) {
        this.canciones = canciones;
        this.analisisCompleto = analisis;
        this.flechasActivas = new ArrayList<>();
        this.flechasGenerator = new FlechasGenerator();
        this.teclasPresionadas = new EnumMap<>(Direccion.class);
        this.ultimoTiempoInput = new EnumMap<>(Direccion.class);
        this.flechasProcesadas = new HashSet<>();
        
        // Inicializar estados de teclas
        for (Direccion dir : Direccion.values()) {
            teclasPresionadas.put(dir, false);
            ultimoTiempoInput.put(dir, 0f);
        }
        
        if (!canciones.isEmpty()) {
            String primerCancion = canciones.get(0);
            this.analisisActual = analisis.get(primerCancion);
        }
    }

    // Sobrecarga para activar modo práctica
    public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> analisis, boolean modoPractica) {
        this(canciones, analisis);
        this.modoPractica = modoPractica;
    }
    
    // ==================== INICIALIZACIÓN ====================
    
    @Override
    protected void initialize(Application app) {
        this.app = (MiJuegoDeRitmo) app;
        this.assetManager = app.getAssetManager();
        this.gameNode = new Node("GameNode");
        
        // Deshabilitar cámara voladora
        this.app.getFlyByCamera().setEnabled(false);
        
        // Configurar cámara 2D
        configurarCamara2D();
        
        // Crear elementos visuales
        crearFondo();
        crearProtagonista();
        
        // Inicializar sistemas
        setupInputs();
        inicializarUI();
        // En modo práctica ocultar barra de vida
        if (modoPractica && gameplayUI != null) {
            gameplayUI.ocultarBarraVida();
        }
        generarFlechasCancion();
        inicializarEventosEspacio();
        reproducirCancionActual();
        
        System.out.println("✓ Gameplay inicializado correctamente");
    }
    
    private void configurarCamara2D() {
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        centroPantalla = new Vector3f(ancho / 2, alto / 2, 0);
        
        app.getCamera().setParallelProjection(true);
        
        // Ampliar el área visible (frustum más grande)
        float factorAmplificacion = 1.5f;
        float aspect = (float) app.getCamera().getWidth() / app.getCamera().getHeight();
        
        float altoVisible = alto * factorAmplificacion;
        float anchoVisible = ancho * factorAmplificacion;
        
        app.getCamera().setFrustum(
            -1000, 1000, 
            -aspect * altoVisible / 2,
            aspect * altoVisible / 2,
            altoVisible / 2,
            -altoVisible / 2
        );
        
        System.out.println("📐 Área de juego ampliada:");
        System.out.println("  Ventana física: " + ancho + "x" + alto);
        System.out.println("  Área visible: " + anchoVisible + "x" + altoVisible);
    }
    
    private void crearFondo() {
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fondoMat.setColor("Color", new ColorRGBA(0.05f, 0.05f, 0.15f, 1f));
        fondoMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        fondoGeometry = new Geometry("Fondo", new Quad(ancho, alto));
        fondoGeometry.setMaterial(fondoMat);
        fondoGeometry.setLocalTranslation(0, 0, -1f);
        
        app.getGuiNode().attachChild(fondoGeometry);
    }
    
    private void crearProtagonista() {
        // El objeto central se crea en GameplayUI y es reactivo
        System.out.println("✓ Objeto central gestionado por GameplayUI");
    }
    
    private void inicializarUI() {
        gameplayUI = new GameplayUI((SimpleApplication) app);
        gameplayUI.actualizarVida(vida);
        gameplayUI.actualizarScore(score);
        gameplayUI.actualizarCombo(combo);
        
        menuPausa = new MenuPausa(
            (SimpleApplication) app,
            () -> reanudarJuego(),
            () -> volverAlMenuPrincipal(),
            () -> toggleMusicaJuego(),
            (pausado) -> manejarCambioPausa(pausado)
        );
        
        System.out.println("✓ UI inicializada");
    }
    
    // ==================== CALLBACKS DEL MENÚ ====================
    
    private void manejarCambioPausa(boolean pausado) {
        if (audioNode == null) return;
        
        if (pausado) {
            if (audioNode.getStatus() == AudioSource.Status.Playing) {
                audioNode.pause();
                System.out.println("  ⏸ Audio pausado");
            }
        } else {
            if (audioNode.getStatus() == AudioSource.Status.Paused) {
                audioNode.play();
                System.out.println("  ▶ Audio reanudado");
            }
        }
    }
    
    private void reanudarJuego() {
        // Callback para cuando se reanuda
    }
    
    private void volverAlMenuPrincipal() {
        System.out.println("→ Volviendo al menú principal");
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
    
    // ==================== ✅ UPDATE PRINCIPAL CORREGIDO ====================
    
    @Override
    public void update(float tpf) {
        // Bloquear actualización si está pausado
        if (menuPausa != null && menuPausa.estaPausado()) {
            return;
        }
        
        if (!isEnabled()) {
            return;
        }
        
        // Mostrar resultados si el juego terminó
        if (juegoTerminado && !resultadosMostrados) {
            mostrarVentanaResultados();
            resultadosMostrados = true;
            return;
        }
        
        // Actualizar tiempo
        tiempoTranscurrido += tpf;
        
        // Procesar inputs en cada frame
        procesarInputsContinuos();
        
        // ✅ CORREGIDO: Actualizar UI con la firma correcta
        if (gameplayUI != null) {
            gameplayUI.actualizarFeedback(tpf);
            gameplayUI.actualizarCombo(combo);
            
            // ✅ CORREGIDO: Solo pasar TPF (1 parámetro)
            gameplayUI.actualizarBrilloObjetoCentral(tpf);
        }
        
        // Generar y verificar flechas
        generarFlechasPorTiempo();
        verificarMisses();

        // Actualizar mecánica de ESPACIO (parpadeos dobles y misses)
        actualizarMecanicaEspacio();
        
        // Verificar fin de canción
        if (audioNode != null && audioNode.getStatus() == AudioSource.Status.Stopped && !juegoTerminado) {
            terminarJuego("cancion_finalizada");
        }
        
        // Verificar game over (omitido en modo práctica)
        if (!modoPractica && vida <= 0 && !juegoTerminado) {
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
    
    // ==================== SISTEMA DE RESULTADOS ====================
    
    private void mostrarVentanaResultados() {
        if (procesandoResultados) return;
        procesandoResultados = true;
        
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
    
    // ==================== SISTEMA DE PLAYLIST ====================
    
    private void siguienteCancion() {
        System.out.println("\n=== SIGUIENTE CANCIÓN ===");
        
        cancionActual++;
        String siguienteCancion = canciones.get(cancionActual);
        analisisActual = analisisCompleto.get(siguienteCancion);
        
        reiniciarEstadoParaNuevaCancion();
        
        tiempoTranscurrido = 0f;
        flechasActivas.clear();
        flechasProcesadas.clear();
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
        
        // Resetear estados de input
        for (Direccion dir : Direccion.values()) {
            teclasPresionadas.put(dir, false);
            ultimoTiempoInput.put(dir, 0f);
        }
    }
    
    private void finalizarJuegoCompleto() {
        System.out.println("\n=== ¡PLAYLIST COMPLETADA! ===");
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
    
    // ==================== SISTEMA DE AUDIO ====================
    
    private void reproducirCancionActual() {
        if (cancionActual >= canciones.size()) {
            finalizarJuegoCompleto();
            return;
        }
        
        String rutaCancion = canciones.get(cancionActual);
        File archivoAudio = new File(rutaCancion);
        
        System.out.println("\n🎵 REPRODUCIENDO:");
        System.out.println("  Canción " + (cancionActual + 1) + "/" + canciones.size());
        System.out.println("  Archivo: " + archivoAudio.getName());
        
        try {
            if (!archivoAudio.exists()) {
                throw new Exception("Archivo no encontrado: " + rutaCancion);
            }
            
            audioNode = crearAudioNodeDesdeArchivo(archivoAudio);
            
            if (audioNode != null) {
                audioNode.setPositional(false);
                audioNode.setVolume(app.getMasterVolume());
                audioNode.setLooping(false);
                audioNode.play();
                
                float duracion = analisisActual.getDuracionTotal();
                System.out.println("  ⏱ Duración: " + formatearTiempo(duracion));
                System.out.println("  ✓ Reproduciendo correctamente");
                
                if (analisisActual != null) {
                    float bpm = (float) analisisActual.getBPMEstimado();
                    if (bpm > 0) {
                        System.out.println("  🎵 BPM: " + String.format("%.1f", bpm));
                    }
                }
                
                if (gameplayUI != null) {
                    String nombre = archivoAudio.getName().replace(".wav", "");
                    gameplayUI.mostrarCancion(nombre);
                }
            } else {
                throw new Exception("No se pudo crear AudioNode");
            }
            
        } catch (Exception e) {
            System.err.println("  ❌ Error: " + e.getMessage());
            e.printStackTrace();
            
            if (cancionActual + 1 < canciones.size()) {
                System.out.println("  ⏭ Saltando a la siguiente...");
                cancionActual++;
                reproducirCancionActual();
            } else {
                System.err.println("  ⚠ No hay más canciones");
                finalizarJuegoCompleto();
            }
        }
    }
    
    private AudioNode crearAudioNodeDesdeArchivo(File archivo) {
        try {
            WAVLoader loader = new WAVLoader();
            AudioKey audioKey = new AudioKey(archivo.getName(), false);
            
            AssetInfo assetInfo = new AssetInfo(assetManager, audioKey) {
                @Override
                public InputStream openStream() {
                    try {
                        return new FileInputStream(archivo);
                    } catch (Exception e) {
                        System.err.println("Error abriendo stream: " + e.getMessage());
                        return null;
                    }
                }
            };
            
            AudioData audioData = (AudioData) loader.load(assetInfo);
            AudioNode node = new AudioNode(audioData, audioKey);
            node.setPositional(false);
            node.setReverbEnabled(false);
            
            return node;
            
        } catch (Exception e) {
            System.err.println("Error creando AudioNode: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String formatearTiempo(float segundos) {
        int min = (int)(segundos / 60);
        int seg = (int)(segundos % 60);
        return min + ":" + String.format("%02d", seg);
    }
    
    // ==================== SISTEMA DE FLECHAS ====================
    
    private void generarFlechasCancion() {
        if (analisisActual == null) {
            System.err.println("ERROR: No hay análisis disponible");
            return;
        }
        
        flechasAGenerar = flechasGenerator.generarFlechasCompletas(analisisActual);
        indiceFlechaActual = 0;
        
        System.out.println("  Flechas generadas: " + flechasAGenerar.size());
    }

    private void inicializarEventosEspacio() {
        if (analisisActual == null) return;
        // Usar beats lentos como eventos de ESPACIO
        this.eventosEspacio = new ArrayList<>(analisisActual.beatsLentos);
        Collections.sort(this.eventosEspacio);
        this.indiceEventoEspacio = 0;
        this.blink1Disparado = false;
        this.blink2Disparado = false;
        System.out.println("  Eventos ESPACIO: " + this.eventosEspacio.size());
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
    
    private void crearFlechaEnPantalla(FlechaData flechaData) {
        float tamano = 70f;
        Geometry flechaGeom = new Geometry("Flecha", new Quad(tamano, tamano));
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", flechaData.getDireccion().getColor());
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
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
        
        // Calcular velocidad dinámica para llegar al target justo en el beat
        float distancia = posInicial.distance(posTarget);
        float tiempoRestante = flechaData.getBeatTime() - tiempoTranscurrido;
        // Evitar divisiones por cero o negativas por retrasos del frame
        tiempoRestante = Math.max(0.05f, tiempoRestante);
        float velocidadDinamica = distancia / tiempoRestante;

        FlechaControl control = new FlechaControl(
            flechaData.getTipo(),
            flechaData.getDireccion(),
            posInicial,
            posTarget,
            velocidadDinamica,
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
                if (!flechasProcesadas.contains(control)) {
                    registrarMiss();
                    flechasProcesadas.add(control);
                }
                flecha.removeFromParent();
                iterator.remove();
            } else if (control != null && control.fueGolpeada()) {
                if (flecha.getParent() == null) {
                    iterator.remove();
                }
            }
        }
    }
    
    // ==================== ✅ SISTEMA DE INPUT CORREGIDO ====================
    
    private void setupInputs() {
        mappingTeclas = new HashMap<>();
        mappingTeclas.put(Direccion.ARRIBA, "Arriba");
        mappingTeclas.put(Direccion.ABAJO, "Abajo");
        mappingTeclas.put(Direccion.IZQUIERDA, "Izquierda");
        mappingTeclas.put(Direccion.DERECHA, "Derecha");
        mappingTeclas.put(Direccion.ESPACIO, "Espacio");
        
        // Limpiar mappings previos
        try {
            for (String mapping : mappingTeclas.values()) {
                app.getInputManager().deleteMapping(mapping);
            }
        } catch (Exception e) {}
        
        // Configurar AMBOS esquemas (WASD + Flechas)
        app.getInputManager().addMapping("Arriba", 
            new KeyTrigger(KeyInput.KEY_W),
            new KeyTrigger(KeyInput.KEY_UP)
        );
        
        app.getInputManager().addMapping("Abajo", 
            new KeyTrigger(KeyInput.KEY_S),
            new KeyTrigger(KeyInput.KEY_DOWN)
        );
        
        app.getInputManager().addMapping("Izquierda", 
            new KeyTrigger(KeyInput.KEY_A),
            new KeyTrigger(KeyInput.KEY_LEFT)
        );
        
        app.getInputManager().addMapping("Derecha", 
            new KeyTrigger(KeyInput.KEY_D),
            new KeyTrigger(KeyInput.KEY_RIGHT)
        );
        
        app.getInputManager().addMapping("Espacio", 
            new KeyTrigger(KeyInput.KEY_SPACE)
        );
        
        app.getInputManager().addListener(this, 
            "Arriba", "Abajo", "Izquierda", "Derecha", "Espacio");
        
        System.out.println("✓ Inputs configurados:");
        System.out.println("  ARRIBA:    W o ↑");
        System.out.println("  ABAJO:     S o ↓");
        System.out.println("  IZQUIERDA: A o ←");
        System.out.println("  DERECHA:   D o →");
        System.out.println("  ESPECIAL:  ESPACIO");
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (menuPausa != null && menuPausa.estaPausado()) return;
        if (juegoTerminado) return;
        
        Direccion direccion = obtenerDireccionDesdeTecla(name);
        if (direccion != null) {
            teclasPresionadas.put(direccion, isPressed);
            
            if (isPressed) {
                procesarInputInmediato(direccion);
            }
        }
    }
    
    /**
     * ✅ CORREGIDO: Sin llamadas a métodos inexistentes
     */
    private void procesarInputInmediato(Direccion direccion) {
        // Verificar cooldown
        float ultimoTiempo = ultimoTiempoInput.get(direccion);
        if (tiempoTranscurrido - ultimoTiempo < cooldownInput) {
            return;
        }
        
        // ✅ CORREGIDO: Solo activar brillo ESPACIO (método que SÍ existe)
        if (gameplayUI != null && direccion == Direccion.ESPACIO) {
            gameplayUI.activarBrilloEspacio();
            // Evaluar golpe para evento de ESPACIO si dentro de ventana
            if (evaluarHitEspacioSiDentroVentana()) {
                ultimoTiempoInput.put(direccion, tiempoTranscurrido);
            }
            return; // No buscar flechas para ESPACIO
        }
        
        // Buscar flecha válida
        FlechaControl flechaObjetivo = encontrarMejorFlecha(direccion);
        
        if (flechaObjetivo != null) {
            float delay = flechaObjetivo.calcularDelay(tiempoTranscurrido);
            
            if (delay <= VENTANA_MALA) {
                evaluarHit(flechaObjetivo, delay);
                flechaObjetivo.brillar();
                ultimoTiempoInput.put(direccion, tiempoTranscurrido);
                flechasProcesadas.add(flechaObjetivo);
            }
        }
    }

    private void actualizarMecanicaEspacio() {
        if (eventosEspacio == null || indiceEventoEspacio >= eventosEspacio.size()) return;
        float evento = eventosEspacio.get(indiceEventoEspacio);
        float t = tiempoTranscurrido;
        
        // Programar doble parpadeo: 0.6s y 0.1s antes del evento
        if (!blink1Disparado && t >= evento - 0.6f) {
            if (gameplayUI != null) gameplayUI.activarBrilloEspacio();
            blink1Disparado = true;
        }
        if (!blink2Disparado && t >= evento - 0.1f) {
            if (gameplayUI != null) gameplayUI.activarBrilloEspacio();
            blink2Disparado = true;
        }
        
        // Registrar miss si pasó la ventana
        if (t > evento + VENTANA_MISS) {
            registrarMiss();
            avanzarEventoEspacio();
        }
    }

    private boolean evaluarHitEspacioSiDentroVentana() {
        if (eventosEspacio == null || indiceEventoEspacio >= eventosEspacio.size()) return false;
        float evento = eventosEspacio.get(indiceEventoEspacio);
        float delay = Math.abs(tiempoTranscurrido - evento);
        if (delay <= VENTANA_MALA) {
            evaluarHitEspacio(delay);
            avanzarEventoEspacio();
            return true;
        }
        return false;
    }

    private void avanzarEventoEspacio() {
        indiceEventoEspacio++;
        blink1Disparado = false;
        blink2Disparado = false;
    }

    private void evaluarHitEspacio(float delay) {
        delay = Math.abs(delay);
        int puntosBase;
        String feedback;
        ColorRGBA colorFeedback;
        boolean perfectHit = false;
        
        if (delay <= VENTANA_PERFECTA) {
            puntosBase = 100;
            feedback = "¡PERFECTO!";
            colorFeedback = new ColorRGBA(0.8f, 0.8f, 1f, 1);
            perfectos++;
            combo++;
            perfectHit = true;
            if (delay <= 0.02f) {
                puntosBase = 150;
                feedback = "¡¡IMPECABLE!!";
            }
        } else if (delay <= VENTANA_BUENA) {
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = ColorRGBA.Yellow;
            buenos++;
            combo++;
        } else if (delay <= VENTANA_MALA) {
            puntosBase = 20;
            feedback = "MALO";
            colorFeedback = ColorRGBA.Orange;
            malos++;
            combo = 0;
        } else {
            puntosBase = 10;
            feedback = "TARDÍO";
            colorFeedback = ColorRGBA.Red;
            malos++;
            combo = 0;
        }
        
        // Sin flecha: usar multiplicador base 1.0
        float multiplicador = 1.0f;
        if (combo > 5) {
            multiplicador *= (1 + combo * 0.05f);
        }
        int puntos = (int)(puntosBase * multiplicador);
        score += puntos;
        if (combo > maxCombo) maxCombo = combo;
        // Recuperar vida en perfect (si no es práctica)
        if (!modoPractica && delay <= VENTANA_PERFECTA) {
            vida = Math.min(100, vida + 2);
            if (gameplayUI != null) gameplayUI.actualizarVida(vida);
        }
        if (gameplayUI != null) {
            gameplayUI.actualizarScore(score);
            gameplayUI.mostrarFeedback(feedback, colorFeedback);
            gameplayUI.actualizarCombo(combo);
            gameplayUI.activarBrilloHit(ColorRGBA.White);
            if (perfectHit && puntos > 100) {
                gameplayUI.mostrarBonusPuntos(puntos);
            }
        }
    }
    
    private void procesarInputsContinuos() {
        for (Map.Entry<Direccion, Boolean> entry : teclasPresionadas.entrySet()) {
            if (entry.getValue()) {
                Direccion dir = entry.getKey();
                float ultimoTiempo = ultimoTiempoInput.get(dir);
                
                if (tiempoTranscurrido - ultimoTiempo >= cooldownInput) {
                    FlechaControl flecha = encontrarMejorFlecha(dir);
                    if (flecha != null && !flechasProcesadas.contains(flecha)) {
                        float delay = flecha.calcularDelay(tiempoTranscurrido);
                        if (delay <= VENTANA_MALA) {
                            evaluarHit(flecha, delay);
                            flecha.brillar();
                            ultimoTiempoInput.put(dir, tiempoTranscurrido);
                            flechasProcesadas.add(flecha);
                        }
                    }
                }
            }
        }
    }
    
    private FlechaControl encontrarMejorFlecha(Direccion direccion) {
        FlechaControl mejorFlecha = null;
        float menorDelay = Float.MAX_VALUE;
        
        for (Geometry flecha : flechasActivas) {
            FlechaControl control = flecha.getControl(FlechaControl.class);
            if (control == null || control.fueGolpeada() || control.fueErrada()) {
                continue;
            }
            
            if (flechasProcesadas.contains(control)) {
                continue;
            }
            
            Direccion direccionFlecha = control.getDireccion();
            
            // Manejar flechas invertidas (doradas)
            if (control.getTipo() == TipoFlecha.DORADA && control.estaInvertida()) {
                direccionFlecha = direccionFlecha.getOpuesta();
            }
            
            if (direccionFlecha == direccion) {
                float delay = Math.abs(control.calcularDelay(tiempoTranscurrido));
                
                if (delay <= VENTANA_MALA && delay < menorDelay) {
                    menorDelay = delay;
                    mejorFlecha = control;
                }
            }
        }
        
        return mejorFlecha;
    }
    
    private Direccion obtenerDireccionDesdeTecla(String nombreTecla) {
        for (Map.Entry<Direccion, String> entry : mappingTeclas.entrySet()) {
            if (entry.getValue().equals(nombreTecla)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // ==================== ✅ SISTEMA DE PUNTUACIÓN CORREGIDO ====================
    
    /**
     * ✅ CORREGIDO: Firma correcta con 1 solo parámetro
     */
    private void evaluarHit(FlechaControl flecha, float delay) {
        delay = Math.abs(delay);
        
        int puntosBase;
        String feedback;
        ColorRGBA colorFeedback;
        boolean perfectHit = false;
        
        if (delay <= VENTANA_PERFECTA) {
            puntosBase = 100;
            feedback = "¡PERFECTO!";
            colorFeedback = new ColorRGBA(0, 1, 0.5f, 1);
            perfectos++;
            combo++;
            perfectHit = true;
            
            if (delay <= 0.02f) {
                puntosBase = 150;
                feedback = "¡¡IMPECABLE!!";
            }
            
        } else if (delay <= VENTANA_BUENA) {
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = ColorRGBA.Yellow;
            buenos++;
            combo++;
            
        } else if (delay <= VENTANA_MALA) {
            puntosBase = 20;
            feedback = "MALO";
            colorFeedback = ColorRGBA.Orange;
            malos++;
            combo = 0;
            
        } else {
            puntosBase = 10;
            feedback = "TARDÍO";
            colorFeedback = ColorRGBA.Red;
            malos++;
            combo = 0;
        }
        
        // Sistema de multiplicadores
        float multiplicador = flecha.getTipo().getMultiplicadorPuntos();
        
        if (combo > 5) {
            multiplicador *= (1 + combo * 0.05f);
        }
        
        if (perfectos >= 10 && buenos == 0 && malos == 0) {
            multiplicador *= 1.5f;
            if (perfectos % 10 == 0) {
                feedback = "¡RACHA PERFECTA x" + perfectos + "!";
            }
        }
        
        int puntos = (int)(puntosBase * multiplicador);
        score += puntos;
        
        if (combo > maxCombo) {
            maxCombo = combo;
        }
        
        // Recuperar vida con perfectos consecutivos
        // Recuperar vida en perfect (si no es práctica)
        if (!modoPractica && delay <= VENTANA_PERFECTA) {
            vida = Math.min(100, vida + 2);
            if (gameplayUI != null) {
                gameplayUI.actualizarVida(vida);
            }
        }
        
        // ✅ CORREGIDO: Actualizar UI con la firma correcta
        if (gameplayUI != null) {
            gameplayUI.actualizarScore(score);
            gameplayUI.mostrarFeedback(feedback, colorFeedback);
            gameplayUI.actualizarCombo(combo);
            
            // ✅ CORREGIDO: Solo pasar ColorRGBA (1 parámetro)
            ColorRGBA colorFlecha = flecha.getDireccion().getColor();
            gameplayUI.activarBrilloHit(colorFlecha);
            
            // Si fue perfecto, mostrar bonus
            if (perfectHit && puntos > 100) {
                gameplayUI.mostrarBonusPuntos(puntos);
            }
        }
        
        // Marcar flecha como golpeada
        flecha.marcarComoGolpeada();
    }
    
    private void registrarMiss() {
        misses++;
        combo = 0;
        
        int penalizacion = 10;
        if (misses > 5) {
            penalizacion = 15;
        }
        if (misses > 10) {
            penalizacion = 20;
        }
        
        if (!modoPractica) {
            vida -= penalizacion;
            vida = Math.max(0, vida);
            if (gameplayUI != null) {
                gameplayUI.actualizarVida(vida);
                // Advertencia si la vida está baja
                if (vida <= 30 && vida > 0) {
                    gameplayUI.mostrarAdvertenciaVidaBaja();
                }
            }
        }
        if (gameplayUI != null) {
            gameplayUI.mostrarFeedback("MISS!", ColorRGBA.Red);
            gameplayUI.actualizarCombo(0);
        }
    }
    
    // ==================== UTILIDADES ====================
    
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
        System.out.println("Score Total: " + score);
        System.out.println("Combo Máximo: " + maxCombo);
        System.out.println("Perfectos: " + perfectos);
        System.out.println("Buenos: " + buenos);
        System.out.println("Malos: " + malos);
        System.out.println("Misses: " + misses);
        
        int totalNotas = perfectos + buenos + malos + misses;
        if (totalNotas > 0) {
            float precision = ((float)(perfectos + buenos) / totalNotas) * 100;
            System.out.println(String.format("Precisión: %.1f%%", precision));
            
            String ranking = calcularRanking(precision, perfectos, totalNotas);
            System.out.println("Ranking: " + ranking);
        }
        System.out.println("===========================\n");
    }
    
    private String calcularRanking(float precision, int perfectos, int totalNotas) {
        float ratioPerfectos = (float)perfectos / totalNotas * 100;
        
        if (precision >= 98 && ratioPerfectos >= 90) {
            return "SSS - ¡LEYENDA!";
        } else if (precision >= 95 && ratioPerfectos >= 80) {
            return "SS - ¡MAESTRO!";
        } else if (precision >= 90) {
            return "S - ¡EXCELENTE!";
        } else if (precision >= 85) {
            return "A - ¡MUY BIEN!";
        } else if (precision >= 75) {
            return "B - BIEN";
        } else if (precision >= 60) {
            return "C - REGULAR";
        } else if (precision >= 40) {
            return "D - NECESITAS PRACTICAR";
        } else {
            return "F - SIGUE INTENTANDO";
        }
    }
    
    // ==================== CLEANUP ====================
    
    @Override
    protected void cleanup(Application app) {
        System.out.println("🧹 Limpiando GameplayAppState...");
        
        if (audioNode != null) {
            audioNode.stop();
            audioNode = null;
        }
        
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
        
        try {
            app.getInputManager().removeListener(this);
            for (String mapping : mappingTeclas.values()) {
                app.getInputManager().deleteMapping(mapping);
            }
        } catch (Exception e) {
            System.err.println("Error limpiando inputs: " + e.getMessage());
        }
        
        gameNode.detachAllChildren();
        flechasActivas.clear();
        flechasProcesadas.clear();
        teclasPresionadas.clear();
        ultimoTiempoInput.clear();
        
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