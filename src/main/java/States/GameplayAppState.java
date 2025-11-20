/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package States;

import Modelo.*;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import Controls.FlechaControl;
import Controls.FlechaDoradaControl;
import com.jme3.scene.control.AbstractControl;
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
import com.jme3.texture.Texture;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.texture.Image;
import Modelo.FlechasGenerator.Difficulty;
import Modelo.AstronautaGenerator;

/**
 * GameplayAppState - Sistema de juego completo con mejoras visuales
 * ✅ CORREGIDO: Todos los errores de sintaxis y constructores
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
    private Node astronautaNode;


    
    // Control del juego
    private boolean juegoTerminado = false;
    private boolean resultadosMostrados = false;
    private boolean procesandoResultados = false;
    
    // UI
    private MenuPausa menuPausa;
    private GameplayUI gameplayUI;
    
    // Sistema de flechas
    private FlechasGenerator flechasGenerator;
    private Modelo.FlechasGenerator.Difficulty dificultadInicial = Modelo.FlechasGenerator.Difficulty.NORMAL;
    private List<FlechaData> flechasAGenerar;
    private int indiceFlechaActual = 0;
    private List<Geometry> flechasActivas;

    // Mecánica ESPACIO
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
    private FlechaProceduralGenerator flechaGenerator;
    
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
    private static final float VENTANA_PERFECTA_BASE = 0.05f;
    private static final float VENTANA_BUENA_BASE = 0.15f;
    private static final float VENTANA_MALA_BASE = 0.30f;
    private static final float VENTANA_MISS_BASE = 0.40f;
    
    // ✅ NUEVO: Constantes simplificadas para evaluarHit
    private static final float VENTANA_PERFECTA = 0.05f;
    private static final float VENTANA_BUENA = 0.15f;
    private static final float VENTANA_MALA = 0.30f;
    
    // Sistema de detección de hits mejorado
    private Set<AbstractControl> flechasProcesadas;
    
    // Otros
    private Vector3f centroPantalla;
    private Map<Direccion, String> mappingTeclas;
    
    // Modo práctica
    private boolean modoPractica = false;
    private float latenciaInput = 0f;
    
    // ✅ NUEVO: Variables para modo continuo
    private boolean modoContinuo = false;
    private boolean esperandoSiguienteCancion = false;
    private float tiempoEsperaEntreCancion = 2.0f;
    private float tiempoEsperaActual = 0f;
    
    // ✅ NUEVO: Estadísticas de sesión
    private int cancionesCompletadas = 0;
    private int scoreTotalSession = 0;
    
    private boolean audioEmpezado = false;
private boolean proteccionInicioActiva = true;
private float tiempoProteccionInicio = 2.0f; // 2 segundos de protección
private float tiempoDesdeInicio = 0f;

private float ancho;  // Ancho de la ventana
private float alto;   // Alto de la ventana
    
    // ==================== CONSTRUCTORES CORREGIDOS ====================

    /**
     * Constructor PRINCIPAL con todos los parámetros
     */
   /**
 * Constructor PRINCIPAL con todos los parámetros incluyendo dificultad
 */
 public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> 
analisis,  
                           boolean modoPractica, boolean modoContinuo) { 
        this.canciones = canciones; 
        this.analisisCompleto = analisis; 
        this.modoPractica = modoPractica; 
        this.modoContinuo = modoContinuo; 
         
        this.flechasActivas = new ArrayList<>(); 
        this.flechasGenerator = new FlechasGenerator(); 
        this.teclasPresionadas = new EnumMap<>(Direccion.class); 
        this.ultimoTiempoInput = new EnumMap<>(Direccion.class); 
        this.flechasProcesadas = new HashSet<>(); 
 
        for (Direccion dir : Direccion.values()) { 
            teclasPresionadas.put(dir, false); 
            ultimoTiempoInput.put(dir, 0f); 
        } 
 
        if (!canciones.isEmpty()) { 
            String primerCancion = canciones.get(0); 
            this.analisisActual = analisis.get(primerCancion); 
        } 
         
        System.out.println("GameplayAppState creado:"); 
        System.out.println("  Modo: " + (modoContinuo ? "PLAYLIST CONTINUA " : "CON RESULTADOS ")); 
        System.out.println("  Canciones: " + canciones.size()); 
    } 
 
    /** 
     * Constructor con dificultad 
     */ 
    public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> 
analisis,  
                           Modelo.FlechasGenerator.Difficulty dificultad) { 
        this(canciones, analisis, false, false); 
        if (dificultad != null) this.dificultadInicial = dificultad; 
    } 
 
    /** 
     * Constructor con modo práctica (sin modo continuo) 
     */ 
    public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> 
analisis,  
                           boolean modoPractica) { 
        this(canciones, analisis, modoPractica, false); 
    } 
 
    /** 
     * Constructor básico (sin modo práctica ni continuo) 
     */ 
    public GameplayAppState(List<String> canciones, Map<String, ResultadoAnalisis> 
analisis) { 
        this(canciones, analisis, false, false); 
    }
    @Override

protected void initialize(Application app) {
     this.app = (MiJuegoDeRitmo) app; 
        this.assetManager = app.getAssetManager(); 
        this.gameNode = new Node("GameNode"); 
        this.app.getGuiNode().attachChild(gameNode);
        this.flechaGenerator = new FlechaProceduralGenerator(assetManager); 
 
        this.app.getFlyByCamera().setEnabled(false); 
 
        configurarCamara2D(); 
        crearFondo(); 
        crearProtagonista(); 
        crearZonasDeImpacto(); 
 
        setupInputs(); 
        inicializarUI(); 
        flechasGenerator.setDifficulty(dificultadInicial); 
         
        if (modoPractica && gameplayUI != null) { 
            gameplayUI.ocultarBarraVida(); 
        } 
 
        generarFlechasCancion(); 
        inicializarEventosEspacio(); 
        reproducirCancionActual(); 
 
        System.out.println("✓ Gameplay inicializado correctamente"); 
   
}
    
    private void crearZonasDeImpacto() {
        Node nodoZonas = new Node("ZonasImpacto");
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        float centroX = ancho / 2f - 40f;  // Movido 40px a la izquierda
        float centroY = alto / 2f - 30f;   // Bajado 30px
        
        float tamanoZona = 80f;
        float separacion = 120f;
        
        System.out.println("🎯 Creando zonas de impacto...");
        
        crearZonaFlecha(nodoZonas, Direccion.ARRIBA, centroX, centroY + separacion, tamanoZona);
        crearZonaFlecha(nodoZonas, Direccion.ABAJO, centroX, centroY - separacion, tamanoZona);
        crearZonaFlecha(nodoZonas, Direccion.IZQUIERDA, centroX + separacion, centroY, tamanoZona);
        crearZonaFlecha(nodoZonas, Direccion.DERECHA, centroX - separacion, centroY, tamanoZona);
        crearZonaEspacio(nodoZonas, centroX, centroY, tamanoZona);
        
        app.getGuiNode().attachChild(nodoZonas);
        System.out.println("✓ Zonas de impacto creadas: 5 zonas");
    }
    
    private void crearZonaFlecha(Node parent, Direccion direccion, float x, float y, float tamano) {
        Texture textura = flechaGenerator.crearTexturaZonaFlecha(
            direccion,
            FlechaProceduralGenerator.ZONA_BORDE_DEFAULT,
            FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
        );
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        Geometry zona = new Geometry("Zona_" + direccion.name(), new Quad(tamano, tamano));
        zona.setMaterial(mat);
        zona.setLocalTranslation(x - tamano/2, y - tamano/2, 0.5f);
        
        parent.attachChild(zona);
    }
    
    private void crearZonaEspacio(Node parent, float x, float y, float tamano) {
        Texture textura = flechaGenerator.crearTexturaZonaEspacio(
            FlechaProceduralGenerator.ZONA_BORDE_ESPACIO,
            FlechaProceduralGenerator.ZONA_FONDO_ESPACIO
        );
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        Geometry zona = new Geometry("Zona_ESPACIO", new Quad(tamano, tamano));
        zona.setMaterial(mat);
        zona.setLocalTranslation(x - tamano/2, y - tamano/2, 0.5f);
        
        parent.attachChild(zona);
    }
    
    private void configurarCamara2D() {
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        centroPantalla = new Vector3f(ancho / 2, alto / 2, 0);
        app.getCamera().setParallelProjection(true);
        float factorAmplificacion = 1.5f;
        float aspect = (float) app.getCamera().getWidth() / app.getCamera().getHeight();
        float altoVisible = alto * factorAmplificacion;
        app.getCamera().setFrustum(-1000, 1000, -aspect * altoVisible / 2, aspect * altoVisible / 2, altoVisible / 2, -altoVisible / 2);
    }
    private void crearProtagonista() {
    System.out.println("👨‍🚀 Creando astronauta protagonista...");
    
     float tamano = 200f;
    float centroX = ancho / 2f - 40f; // Mismo ajuste que las zonas
    float centroY = alto / 2f - 30f;
    
    AstronautaGenerator astronautaGen = new AstronautaGenerator(assetManager);
    Node astronautaNode = astronautaGen.crearAstronauta(tamano);
    
    // Posicionar en el centro
    float baseX = centroX - tamano/2;
    float baseY = centroY - tamano/2;
    astronautaNode.setLocalTranslation(baseX, baseY, 5);
    
    app.getGuiNode().attachChild(astronautaNode);
    
    System.out.println(" ✓ Astronauta creado en posición central");
}
    
    private void crearFondo() {
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        
        System.out.println("🌌 Creando fondo espacial...");
        
        Texture texturaFondo = crearTexturaFondoEspacial((int)ancho, (int)alto);
        
        Material fondoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fondoMat.setTexture("ColorMap", texturaFondo);
        fondoMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        fondoGeometry = new Geometry("FondoEspacial", new Quad(ancho, alto));
        fondoGeometry.setMaterial(fondoMat);
        fondoGeometry.setLocalTranslation(0, 0, -1f);
        
        app.getGuiNode().attachChild(fondoGeometry);
        System.out.println("✓ Fondo espacial con estrellas creado");
    }
    
    private Texture crearTexturaFondoEspacial(int ancho, int alto) {
        System.out.println("  🎨 Generando textura espacial " + ancho + "x" + alto + "...");
        
        ByteBuffer buffer = BufferUtils.createByteBuffer(ancho * alto * 4);
        
        ColorRGBA colorSuperior = new ColorRGBA(129f/255f, 40f/255f, 224f/255f, 1f);
        ColorRGBA colorInferior = new ColorRGBA(25f/255f, 0f/255f, 51f/255f, 1f);
        
        Random random = new Random(12345);
        int cantidadEstrellas = (ancho * alto) / 800;
        List<Estrella> estrellas = new ArrayList<>();
        
        for (int i = 0; i < cantidadEstrellas; i++) {
            int x = random.nextInt(ancho);
            int y = random.nextInt(alto);
            float brillo = 0.5f + random.nextFloat() * 0.5f;
            float tamano = random.nextFloat() * 2f + 1f;
            estrellas.add(new Estrella(x, y, brillo, tamano));
        }
        
        System.out.println("  ⭐ Generando " + estrellas.size() + " estrellas...");
        
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                float t = (float)y / alto;
                ColorRGBA colorBase = interpolarColor(colorSuperior, colorInferior, t);
                
                ColorRGBA colorFinal = colorBase;
                for (Estrella estrella : estrellas) {
                    float distancia = FastMath.sqrt(
                        (x - estrella.x) * (x - estrella.x) + 
                        (y - estrella.y) * (y - estrella.y)
                    );
                    
                    if (distancia <= estrella.tamano) {
                        float intensidad = 1f - (distancia / estrella.tamano);
                        intensidad = FastMath.pow(intensidad, 2f);
                        
                        ColorRGBA colorEstrella = new ColorRGBA(
                            1f, 1f, 1f, 
                            intensidad * estrella.brillo
                        );
                        
                        colorFinal = mezclarColoresAditivo(colorBase, colorEstrella);
                    }
                }
                
                buffer.put((byte)(colorFinal.r * 255));
                buffer.put((byte)(colorFinal.g * 255));
                buffer.put((byte)(colorFinal.b * 255));
                buffer.put((byte)(colorFinal.a * 255));
            }
        }
        
        buffer.flip();
        
        Image image = new Image(
            Image.Format.RGBA8,
            ancho,
            alto,
            buffer,
            com.jme3.texture.image.ColorSpace.sRGB
        );
        
        Texture2D textura = new Texture2D(image);
        textura.setMagFilter(Texture.MagFilter.Bilinear);
        textura.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        textura.setWrap(Texture.WrapMode.Clamp);
        
        System.out.println("  ✓ Textura espacial generada exitosamente");
        return textura;
    }
    
    private ColorRGBA interpolarColor(ColorRGBA c1, ColorRGBA c2, float t) {
        return new ColorRGBA(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t,
            c1.a + (c2.a - c1.a) * t
        );
    }
    
    private ColorRGBA mezclarColoresAditivo(ColorRGBA base, ColorRGBA brillo) {
        return new ColorRGBA(
            Math.min(1f, base.r + brillo.r * brillo.a),
            Math.min(1f, base.g + brillo.g * brillo.a),
            Math.min(1f, base.b + brillo.b * brillo.a),
            1f
        );
    }
    
    private static class Estrella {
        final int x, y;
        final float brillo;
        final float tamano;
        
        Estrella(int x, int y, float brillo, float tamano) {
            this.x = x;
            this.y = y;
            this.brillo = brillo;
            this.tamano = tamano;
        }
    }
    
   
    
    private void inicializarUI() {
    gameplayUI = new GameplayUI((SimpleApplication) app);
    gameplayUI.actualizarVida(vida);  // ⭐ Debe recibir 100
    gameplayUI.actualizarScore(score);
    gameplayUI.actualizarCombo(combo);
    
    menuPausa = new MenuPausa(
        (SimpleApplication) app,
        () -> reanudarJuego(),
        () -> volverAlMenuPrincipal(),
        () -> toggleMusicaJuego(),
        (pausado) -> manejarCambioPausa(pausado),
        gameplayUI
    );
    
    // ⭐ Configurar callback del botón de pausa
    gameplayUI.setOnClickBotonPausa(() -> {
        if (menuPausa != null) {
            System.out.println("🖱 Botón de pausa clickeado - Toggling pausa");
            menuPausa.togglePausa();
        }
    });
    
    // ⭐ CRÍTICO: Ocultar barra de vida si es modo práctica
    if (modoPractica) {
        System.out.println("🎯 Modo práctica: ocultando barra de vida");
        gameplayUI.ocultarBarraVida();
    }
    
    System.out.println("✓ UI inicializada");
}
   private void manejarCambioPausa(boolean pausado) {
    System.out.println("🔄 manejarCambioPausa llamado: pausado=" + pausado);
    
    if (audioNode == null) {
        System.out.println("⚠ audioNode es null, no se puede pausar/reanudar música");
        return;
    }

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
    
    // ==================== UPDATE PRINCIPAL ====================
    
    @Override
    public void update(float tpf) {
        if (menuPausa != null && menuPausa.estaPausado()) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        if (esperandoSiguienteCancion) {
            tiempoEsperaActual += tpf;
            
            if (gameplayUI != null && tiempoEsperaActual < 0.5f) {
                gameplayUI.mostrarFeedback("♪ Siguiente Canción...", ColorRGBA.Cyan);
            }
            
            if (tiempoEsperaActual >= tiempoEsperaEntreCancion) {
                esperandoSiguienteCancion = false;
                tiempoEsperaActual = 0f;
                siguienteCancionContinua();
            }
            return;
        }

        if (juegoTerminado && !resultadosMostrados && !modoContinuo) {
            mostrarVentanaResultados();
            resultadosMostrados = true;
            return;
        }

        tiempoTranscurrido += tpf;
        if (proteccionInicioActiva) {
    tiempoDesdeInicio += tpf;
    if (tiempoDesdeInicio >= tiempoProteccionInicio) {
        proteccionInicioActiva = false;
        System.out.println("✓ Protección de inicio desactivada - Misses activos");
    }

        procesarInputsContinuos();

        if (gameplayUI != null) {
            gameplayUI.actualizarFeedback(tpf);
            gameplayUI.actualizarCombo(combo);
            gameplayUI.actualizarBrilloObjetoCentral(tpf);
        }

        generarFlechasPorTiempo();
        verificarMisses();

        actualizarMecanicaEspacio();

        if (audioNode != null && audioNode.getStatus() == AudioSource.Status.Stopped && !juegoTerminado) {
            terminarCancion();
        }
if (!modoPractica && vida <= 0 && !juegoTerminado) {
        terminarJuego("game_over");
    }
    }
    }

    
  private void terminarJuego(String razon) {
    if (juegoTerminado) return;
    
    juegoTerminado = true;
    System.out.println("\n=== JUEGO TERMINADO: " + razon + " ===");

    if (audioNode != null) {
        audioNode.stop();
    }

    this.setEnabled(false);  // ✅ Corregido: sin operador de comparación
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
                if (continuar) {
                    // ⭐ NUEVO: Si es Game Over y presionó Reintentar
                    if (vidaFinal <= 0) {
                        System.out.println("🔄 REINTENTANDO después de Game Over...");
                        reintentarCancionActual();
                    } else {
                        // Continuar a la siguiente canción normalmente
                        if (cancionActual + 1 < canciones.size()) {
                            siguienteCancion();
                        } else {
                            finalizarJuegoCompleto();
                        }
                    }
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
        audioEmpezado = false;
    proteccionInicioActiva = true;
    tiempoDesdeInicio = 0f;
    
    cancionActual++;
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
        inicializarEventosEspacio();
        
        juegoTerminado = false;
        resultadosMostrados = false;
        procesandoResultados = false;
        
        this.setEnabled(true);
        
        reproducirCancionActual();
    }
    /**
 * ⭐ NUEVO: Reinicia la canción actual después de un Game Over
 */
private void reintentarCancionActual() {
    System.out.println("\n=== REINTENTANDO CANCIÓN ACTUAL ===");
    
    // Reiniciar estadísticas
    score = 0;
    vida = 100;
    combo = 0;
    maxCombo = 0;
    perfectos = 0;
    buenos = 0;
    malos = 0;
    misses = 0;
    
    // Limpiar estado
    tiempoTranscurrido = 0f;
    flechasActivas.clear();
    flechasProcesadas.clear();
    gameNode.detachAllChildren();
    
    // Limpiar teclas presionadas
    for (Direccion dir : Direccion.values()) {
        teclasPresionadas.put(dir, false);
        ultimoTiempoInput.put(dir, 0f);
    }
    
    // Restaurar UI
    if (gameplayUI != null) {
        gameplayUI.mostrarNuevamente();
        gameplayUI.actualizarVida(vida);
        gameplayUI.actualizarScore(score);
        gameplayUI.actualizarCombo(combo);
    }
    
    // Regenerar flechas y eventos
    generarFlechasCancion();
    inicializarEventosEspacio();
    audioEmpezado = false;
    proteccionInicioActiva = true;
    tiempoDesdeInicio = 0f;
    
    cancionActual++;
    // Reiniciar flags
    juegoTerminado = false;
    resultadosMostrados = false;
    procesandoResultados = false;
    audioEmpezado = false;
    proteccionInicioActiva = true;
    tiempoDesdeInicio = 0f;
    // Habilitar gameplay
    this.setEnabled(true);
    
    
    // Reproducir canción nuevamente
    reproducirCancionActual();
    
    System.out.println("✓ Canción reiniciada - ¡Buena suerte!");
}
    private void reiniciarEstadoParaNuevaCancion() {
        perfectos = 0;
        buenos = 0;
        malos = 0;
        misses = 0;
        combo = 0;
        
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
    
    public void setModoContinuo(boolean continuo) {
        this.modoContinuo = continuo;
        System.out.println("🔁 Modo continuo: " + (continuo ? "ACTIVADO" : "DESACTIVADO"));
    }

    public void setTiempoEsperaEntreCancion(float segundos) {
        this.tiempoEsperaEntreCancion = Math.max(0.5f, segundos);
        System.out.println("⏱ Tiempo entre canciones: " + this.tiempoEsperaEntreCancion + "s");
    }

    public boolean isModoContinuo() {
        return modoContinuo;
    }
    
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
    
    // ⭐ NUEVO: Activar protección de inicio
    audioEmpezado = true;
    proteccionInicioActiva = true;
    tiempoDesdeInicio = 0f;
    
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

    private float bpmEnTiempo(float t) {
        if (analisisActual == null || analisisActual.segmentosBpm == null || analisisActual.segmentosBpm.isEmpty()) {
            return (float) analisisActual.getBPMEstimado();
        }
        for (ResultadoAnalisis.SegmentoBpm s : analisisActual.segmentosBpm) {
            if (t >= s.inicio && t <= s.fin + 1e-4f) return s.bpm;
        }
        return (float) analisisActual.getBPMEstimado();
    }

    private float ventanaPerfecta(float t) {
        float bpm = bpmEnTiempo(t);
        float interval = bpm > 0f ? 60f / bpm : VENTANA_PERFECTA_BASE;
        float v = interval * 0.08f;
        if (v < 0.03f) v = 0.03f;
        if (v > 0.08f) v = 0.08f;
        return v;
    }
    
    private float ventanaBuena(float t) {
        float bpm = bpmEnTiempo(t);
        float interval = bpm > 0f ? 60f / bpm : VENTANA_BUENA_BASE;
        float v = interval * 0.20f;
        if (v < 0.10f) v = 0.10f;
        if (v > 0.22f) v = 0.22f;
        return v;
    }
    
    private float ventanaMala(float t) {
        float bpm = bpmEnTiempo(t);
        float interval = bpm > 0f ? 60f / bpm : VENTANA_MALA_BASE;
        float v = interval * 0.32f;
        if (v < 0.20f) v = 0.20f;
        if (v > 0.35f) v = 0.35f;
        return v;
    }
    
    private float ventanaMiss(float t) {
        float bpm = bpmEnTiempo(t);
        float interval = bpm > 0f ? 60f / bpm : VENTANA_MISS_BASE;
        float v = interval * 0.42f;
        if (v < 0.30f) v = 0.30f;
        if (v > 0.50f) v = 0.50f;
        return v;
    }

    private void inicializarEventosEspacio() {
        if (analisisActual == null) return;
        
        List<Float> base = new ArrayList<>(analisisActual.beatsLentos);
        Collections.sort(base);
        List<Float> filtrados = new ArrayList<>();
        float minSep = 4.0f;
        float ultimo = -1e6f;
        for (float e : base) {
            if (e - ultimo >= minSep) {
                filtrados.add(e);
                ultimo = e;
            }
        }
        this.eventosEspacio = filtrados;
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
        Geometry flechaGeom = flechaGenerator.crearFlecha(
            flechaData.getTipo(),
            flechaData.getDireccion()
        );
        
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        Vector3f posInicial = flechaData.getDireccion().getPosicionSpawn(ancho, alto);
        Vector3f posTarget = flechaData.getDireccion().getPosicionTarget(ancho, alto);
        
        float tamano = 70f;
        flechaGeom.setLocalTranslation(posInicial.x - tamano/2, posInicial.y - tamano/2, 1);
        
        FlechaControl control = new FlechaControl(
            flechaData.getTipo(),
            flechaData.getDireccion(),
            posInicial,
            posTarget,
            flechaData.getVelocidad(),
            flechaData.getBeatTime(),
            flechaGeom.getMaterial()
        );
        
        flechaGeom.addControl(control);
        gameNode.attachChild(flechaGeom);
        flechasActivas.add(flechaGeom);
    }
    
    private void verificarMisses() {
        Iterator<Geometry> iterator = flechasActivas.iterator();
        while (iterator.hasNext()) {
            Geometry flecha = iterator.next();
            FlechaControl controlN = flecha.getControl(FlechaControl.class);
            FlechaDoradaControl controlD = flecha.getControl(FlechaDoradaControl.class);
            AbstractControl control = controlN != null ? controlN : controlD;
            
            if (controlN != null) {
                if (controlN.fueErrada() && !controlN.fueGolpeada()) {
                    if (!flechasProcesadas.contains(control)) {
                        registrarMiss();
                        flechasProcesadas.add(control);
                    }
                    flecha.removeFromParent();
                    iterator.remove();
                } else if (controlN.fueGolpeada()) {
                    if (flecha.getParent() == null) {
                        iterator.remove();
                    }
                }
            } else if (controlD != null) {
                if (controlD.fueErrada() && !controlD.fueGolpeada()) {
                    if (!flechasProcesadas.contains(control)) {
                        registrarMiss();
                        flechasProcesadas.add(control);
                    }
                    flecha.removeFromParent();
                    iterator.remove();
                } else if (controlD.fueGolpeada()) {
                    if (flecha.getParent() == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }
    
    // ==================== SISTEMA DE INPUT ====================
    
    private void setupInputs() {
        mappingTeclas = new HashMap<>();
        
        mappingTeclas.put(Direccion.ARRIBA, "arriba");
        mappingTeclas.put(Direccion.ABAJO, "abajo");
        mappingTeclas.put(Direccion.IZQUIERDA, "izquierda");
        mappingTeclas.put(Direccion.DERECHA, "derecha");
        mappingTeclas.put(Direccion.ESPACIO, "espacio");

        try {
            for (String mapping : mappingTeclas.values()) {
                app.getInputManager().deleteMapping(mapping);
            }
        } catch (Exception e) {}

        app.getInputManager().addMapping("arriba",
            new KeyTrigger(KeyInput.KEY_W),
            new KeyTrigger(KeyInput.KEY_UP)
        );

        app.getInputManager().addMapping("abajo",
            new KeyTrigger(KeyInput.KEY_S),
            new KeyTrigger(KeyInput.KEY_DOWN)
        );

        app.getInputManager().addMapping("izquierda",
            new KeyTrigger(KeyInput.KEY_A),
            new KeyTrigger(KeyInput.KEY_LEFT)
        );

        app.getInputManager().addMapping("derecha",
            new KeyTrigger(KeyInput.KEY_D),
            new KeyTrigger(KeyInput.KEY_RIGHT)
        );

        app.getInputManager().addMapping("espacio",
            new KeyTrigger(KeyInput.KEY_SPACE)
        );

        app.getInputManager().addListener(this,
            "arriba", "abajo", "izquierda", "derecha", "espacio");

        System.out.println("✓ Inputs configurados");
    }
    
    private void terminarCancion() {
        juegoTerminado = true;
        cancionesCompletadas++;
        scoreTotalSession += score;
        
        System.out.println("\n=== CANCIÓN COMPLETADA ===");
        System.out.println("  Canción: " + (cancionActual + 1) + "/" + canciones.size());
        System.out.println("  Score: " + score);
        System.out.println("  Vida: " + vida + "%");
        
        if (audioNode != null) {
            audioNode.stop();
        }

        if (modoContinuo) {
            if (cancionActual + 1 < canciones.size()) {
                System.out.println("  → Preparando siguiente canción...");
                esperandoSiguienteCancion = true;
                tiempoEsperaActual = 0f;
            } else {
                System.out.println("  🔁 Playlist completada - Reiniciando...");
                cancionActual = -1;
                esperandoSiguienteCancion = true;
                tiempoEsperaActual = 0f;
                mostrarEstadisticasSession();
            }
        } else {
            this.setEnabled(false);
        }
    }
    
    private void siguienteCancionContinua() {
        cancionActual++;
        
        if (cancionActual >= canciones.size()) {
            cancionActual = 0;
            System.out.println("🔁 Reiniciando playlist desde el inicio");
        }
        
        String siguienteCancion = canciones.get(cancionActual);
        analisisActual = analisisCompleto.get(siguienteCancion);

        reiniciarEstadoParaNuevaCancion();

        tiempoTranscurrido = 0f;
        flechasActivas.clear();
        flechasProcesadas.clear();
        gameNode.detachAllChildren();

        if (gameplayUI != null) {
            gameplayUI.actualizarVida(vida);
            gameplayUI.actualizarScore(score);
            gameplayUI.actualizarCombo(combo);
        }

        generarFlechasCancion();
        inicializarEventosEspacio();
        
        juegoTerminado = false;
        resultadosMostrados = false;
        procesandoResultados = false;
        
        this.setEnabled(true);
        
        reproducirCancionActual();
        
        System.out.println("✓ Canción " + (cancionActual + 1) + "/" + canciones.size() + " iniciada");
    }

    private void mostrarEstadisticasSession() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     📊 ESTADÍSTICAS DE SESIÓN         ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  Canciones completadas: " + String.format("%14d", cancionesCompletadas) + " ║");
        System.out.println("║  Score total acumulado: " + String.format("%14d", scoreTotalSession) + " ║");
        System.out.println("║  Score promedio/canción: " + String.format("%13d", 
            cancionesCompletadas > 0 ? scoreTotalSession / cancionesCompletadas : 0) + " ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (menuPausa != null && menuPausa.estaPausado()) return;
        if (juegoTerminado) return;

        if (isPressed) {
            if ("DificultadFacil".equals(name)) { 
                setDifficulty(Modelo.FlechasGenerator.Difficulty.FACIL); 
                return; 
            }
            if ("DificultadNormal".equals(name)) { 
                setDifficulty(Modelo.FlechasGenerator.Difficulty.NORMAL); 
                return; 
            }
            if ("DificultadDificil".equals(name)) { 
                setDifficulty(Modelo.FlechasGenerator.Difficulty.DIFICIL); 
                return; 
            }
        }

        Direccion direccion = obtenerDireccionDesdeTecla(name);
        if (direccion != null) {
            teclasPresionadas.put(direccion, isPressed);
            
            if (isPressed) {
                procesarInputInmediato(direccion);
            }
        }
    }

    public void setDifficulty(Modelo.FlechasGenerator.Difficulty d) {
        if (flechasGenerator == null || analisisActual == null) return;
        flechasGenerator.setDifficulty(d);
        List<FlechaData> nuevas = flechasGenerator.generarFlechasCompletas(analisisActual);
        int idx = 0;
        float umbral = tiempoTranscurrido - tiempoAnticipacion - 0.01f;
        while (idx < nuevas.size() && nuevas.get(idx).getBeatTime() < umbral) idx++;
        flechasAGenerar = nuevas;
        indiceFlechaActual = idx;
        System.out.println("  ⚙ Dificultad cambiada a " + d);
    }
    
    private void procesarInputInmediato(Direccion direccion) {
        float ultimoTiempo = ultimoTiempoInput.get(direccion);
        if (tiempoTranscurrido - ultimoTiempo < cooldownInput) {
            return;
        }
        
        if (gameplayUI != null && direccion == Direccion.ESPACIO) {
            gameplayUI.activarBrilloEspacio();
            if (evaluarHitEspacioSiDentroVentana()) {
                ultimoTiempoInput.put(direccion, tiempoTranscurrido);
            }
            return;
        }
        
        AbstractControl control = encontrarMejorFlechaControl(direccion);
        if (control != null) {
            if (control instanceof FlechaControl) {
                FlechaControl c = (FlechaControl) control;
                float delay = Math.abs(c.calcularDelay(tiempoTranscurrido + latenciaInput));
                if (delay <= ventanaMala(c.getBeatTime())) {
                    evaluarHit(c, delay);
                    c.brillar();
                    ultimoTiempoInput.put(direccion, tiempoTranscurrido);
                    flechasProcesadas.add(control);
                }
            } else if (control instanceof FlechaDoradaControl) {
                FlechaDoradaControl c = (FlechaDoradaControl) control;
                float delay = Math.abs(c.calcularDelay(tiempoTranscurrido + latenciaInput));
                if (delay <= ventanaMala(c.getBeatTime())) {
                    evaluarHitDorada(c, delay);
                    c.brillar();
                    ultimoTiempoInput.put(direccion, tiempoTranscurrido);
                    flechasProcesadas.add(control);
                }
            }
        }
    }

    private void actualizarMecanicaEspacio() {
        if (eventosEspacio == null || indiceEventoEspacio >= eventosEspacio.size()) return;
        float evento = eventosEspacio.get(indiceEventoEspacio);
        float t = tiempoTranscurrido;

        if (!blink1Disparado && t >= evento - 0.6f) {
            if (gameplayUI != null) {
                gameplayUI.activarBrilloEspacio();
                gameplayUI.activarBaileEspacio(0.8f);
            }
            blink1Disparado = true;
        }
        if (!blink2Disparado && t >= evento - 0.1f) {
            if (gameplayUI != null) {
                gameplayUI.activarBrilloEspacio();
                gameplayUI.activarBaileEspacio(0.6f);
            }
            blink2Disparado = true;
        }

        if (t > evento + ventanaMiss(evento)) {
            if (audioEmpezado && !proteccionInicioActiva) registrarMiss();
            avanzarEventoEspacio();
        }
    }

    private boolean evaluarHitEspacioSiDentroVentana() {
        if (eventosEspacio == null || indiceEventoEspacio >= eventosEspacio.size()) return false;
        float evento = eventosEspacio.get(indiceEventoEspacio);
        float delay = Math.abs((tiempoTranscurrido + latenciaInput) - evento);
        if (delay <= ventanaMala(evento)) {
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
        
        float ref = (eventosEspacio != null && indiceEventoEspacio < eventosEspacio.size()) 
            ? eventosEspacio.get(indiceEventoEspacio) : tiempoTranscurrido;
        
        if (delay <= ventanaPerfecta(ref)) {
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
        } else if (delay <= ventanaBuena(ref)) {
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = ColorRGBA.Yellow;
            buenos++;
            combo++;
        } else if (delay <= ventanaMala(ref)) {
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
        
        float multiplicador = 1.0f;
        if (combo > 5) {
            multiplicador *= (1 + combo * 0.05f);
        }
        int puntos = (int)(puntosBase * multiplicador);
        score += puntos;
        if (combo > maxCombo) maxCombo = combo;
        
        if (!modoPractica && delay <= ventanaPerfecta(ref)) {
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
                    AbstractControl control = encontrarMejorFlechaControl(dir);
                    if (control != null && !flechasProcesadas.contains(control)) {
                        if (control instanceof FlechaControl) {
                            FlechaControl c = (FlechaControl) control;
                            float delay = Math.abs(c.calcularDelay(tiempoTranscurrido + latenciaInput));
                            if (delay <= ventanaMala(c.getBeatTime())) {
                                evaluarHit(c, delay);
                                c.brillar();
                                ultimoTiempoInput.put(dir, tiempoTranscurrido);
                                flechasProcesadas.add(control);
                            }
                        } else if (control instanceof FlechaDoradaControl) {
                            FlechaDoradaControl c = (FlechaDoradaControl) control;
                            float delay = Math.abs(c.calcularDelay(tiempoTranscurrido + latenciaInput));
                            if (delay <= ventanaMala(c.getBeatTime())) {
                                evaluarHitDorada(c, delay);
                                c.brillar();
                                ultimoTiempoInput.put(dir, tiempoTranscurrido);
                                flechasProcesadas.add(control);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private AbstractControl encontrarMejorFlechaControl(Direccion direccion) {
        AbstractControl mejor = null;
        float menorDelay = Float.MAX_VALUE;
        
        for (Geometry flecha : flechasActivas) {
            FlechaControl controlN = flecha.getControl(FlechaControl.class);
            FlechaDoradaControl controlD = flecha.getControl(FlechaDoradaControl.class);
            
            if (controlN != null) {
                if (controlN.fueGolpeada() || controlN.fueErrada()) continue;
                if (flechasProcesadas.contains(controlN)) continue;
                
                Direccion dirF = controlN.getDireccion();
                if (controlN.getTipo() == TipoFlecha.DORADA && controlN.estaInvertida()) {
                    dirF = dirF.getOpuesta();
                }
                
                if (dirF == direccion) {
                    float delay = Math.abs(controlN.calcularDelay(tiempoTranscurrido + latenciaInput));
                    if (delay <= ventanaMala(controlN.getBeatTime()) && delay < menorDelay) {
                        menorDelay = delay;
                        mejor = controlN;
                    }
                }
            } else if (controlD != null) {
                if (controlD.fueGolpeada() || controlD.fueErrada()) continue;
                if (flechasProcesadas.contains(controlD)) continue;
                
                Direccion dirF = controlD.getDireccion();
                if (controlD.getTipo() == TipoFlecha.DORADA && controlD.estaInvertida()) {
                    dirF = dirF.getOpuesta();
                }
                
                if (dirF == direccion) {
                    float delay = Math.abs(controlD.calcularDelay(tiempoTranscurrido + latenciaInput));
                    if (delay <= ventanaMala(controlD.getBeatTime()) && delay < menorDelay) {
                        menorDelay = delay;
                        mejor = controlD;
                    }
                }
            }
        }
        return mejor;
    }
    
    private Direccion obtenerDireccionDesdeTecla(String nombreTecla) {
        for (Map.Entry<Direccion, String> entry : mappingTeclas.entrySet()) {
            if (entry.getValue().equals(nombreTecla)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // ==================== SISTEMA DE PUNTUACIÓN ====================
    
    private void evaluarHit(FlechaControl flecha, float delay) {
        delay = Math.abs(delay);
        
        int puntosBase;
        String feedback;
        ColorRGBA colorFeedback;
        boolean perfectHit = false;
        
        if (delay <= VENTANA_PERFECTA) {
            puntosBase = 100;
            feedback = "¡PERFECTO!";
            colorFeedback = new ColorRGBA(247f/255f, 181f/255f, 6f/255f, 1f);
            perfectos++;
            combo++;
            perfectHit = true;
            
            if (delay <= 0.02f) {
                puntosBase = 150;
                feedback = "¡¡IMPECABLE!!";
                colorFeedback = new ColorRGBA(17f/255f, 216f/255f, 197f/255f, 1f);
            }
        } else if (delay <= VENTANA_BUENA) {
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = new ColorRGBA(242f/255f, 118f/255f, 1f/255f, 1f);
            buenos++;
            combo++;
        } else if (delay <= VENTANA_MALA) {
            puntosBase = 20;
            feedback = "MALO";
            colorFeedback = new ColorRGBA(255f/255f, 0f/255f, 132f/255f, 1f);
            malos++;
            combo = 0;
        } else {
            puntosBase = 10;
            feedback = "TARDÍO";
            colorFeedback = ColorRGBA.Red;
            malos++;
            combo = 0;
        }
        
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
        
        if (!modoPractica && delay <= VENTANA_PERFECTA) {
            vida = Math.min(100, vida + 2);
            if (gameplayUI != null) {
                gameplayUI.actualizarVida(vida);
            }
        }
        
        if (gameplayUI != null) {
            gameplayUI.actualizarScore(score);
            gameplayUI.mostrarFeedback(feedback, colorFeedback);
            gameplayUI.actualizarCombo(combo);
            
            ColorRGBA colorFlecha = flecha.getDireccion().getColor();
            gameplayUI.activarBrilloHit(colorFlecha);
            
            if (perfectHit && puntos > 100) {
                gameplayUI.mostrarBonusPuntos(puntos);
            }
        }
        
        flecha.marcarComoGolpeada();
    }

    private void evaluarHitDorada(FlechaDoradaControl flecha, float delay) {
        delay = Math.abs(delay);
        int puntosBase;
        String feedback;
        ColorRGBA colorFeedback;
        boolean perfectHit = false;
        
        float vp = ventanaPerfecta(flecha.getBeatTime());
        float vb = ventanaBuena(flecha.getBeatTime());
        float vm = ventanaMala(flecha.getBeatTime());
        
        if (delay <= vp) {
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
        } else if (delay <= vb) {
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = ColorRGBA.Yellow;
            buenos++;
            combo++;
        } else if (delay <= vm) {
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
        
        if (!modoPractica && delay <= vp) {
            vida = Math.min(100, vida + 2);
            if (gameplayUI != null) {
                gameplayUI.actualizarVida(vida);
            }
        }
        
        if (gameplayUI != null) {
            gameplayUI.actualizarScore(score);
            gameplayUI.mostrarFeedback(feedback, colorFeedback);
            gameplayUI.actualizarCombo(combo);
            ColorRGBA colorFlecha = flecha.getDireccion().getColor();
            gameplayUI.activarBrilloHit(colorFlecha);
            if (perfectHit && puntos > 100) {
                gameplayUI.mostrarBonusPuntos(puntos);
            }
        }
        
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