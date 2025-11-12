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
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo;
import java.util.*;
import javax.swing.SwingUtilities;
import com.jme3.app.SimpleApplication;
import Modelo.GameAssets;
import Modelo.TipoFlecha;
import Modelo.Direccion;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.renderer.queue.RenderQueue;



public class GameplayAppState extends BaseAppState implements ActionListener {

    private MiJuegoDeRitmo app;
    private Node gameNode;
    private AssetManager assetManager;
    
    // Referencias al fondo y protagonista
    private Geometry fondoGeometry;
    private Geometry protagonistaGeometry;
    private FlechaProceduralGenerator flechaGenerator;
    // Flags de control de ciclo de vida
    private boolean juegoTerminado = false;
    private boolean resultadosMostrados = false;
    private boolean procesandoResultados = false;
    
    private MenuPausa menuPausa;
    private GameplayUI gameplayUI;
    private float duracionCancion = 0f;
    private boolean cancionTerminada = false;
    
    // ==================== ZONAS DE IMPACTO (AÑADIR) ====================

// Texturas para las zonas de impacto
    private Texture textureZonaUp;
    private Texture textureZonaDown;
    private Texture textureZonaLeft;
    private Texture textureZonaRight;
    private Texture textureZonaEspacio;

// Geometrías de las zonas
    private Geometry zonaUpGeom;
    private Geometry zonaDownGeom;
    private Geometry zonaLeftGeom;
    private Geometry zonaRightGeom;
    private Geometry zonaEspacioGeom;

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
    private Node rootNode;
    private Node guiNode;
    // Estadísticas
    private int perfectos = 0;
    private int buenos = 0;
    private int malos = 0;
    private int misses = 0;
    // Texturas para las zonas de impact
    // El generador procedural
    private Node flechasNode; 
    private URLTextureLoader urlLoader;
// ✅ Nodo para las flechas (minúscula)
    
    private final float VELOCIDAD_JUEGO = 500f; 
    private final float POSICION_INICIAL_Y = 650f;
    private final float POSICION_TARGET_Y = 100f;
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

    /**
 * ⭐ NUEVO: Configura las zonas de impacto donde llegarán las flechas
 */
private void setupZonasImpacto() {
    System.out.println("\n🎯 Creando zonas de impacto...");
    
    float anchoPantalla = app.getCamera().getWidth();
    float altoPantalla = app.getCamera().getHeight();
    
    // ==================== PARÁMETROS DE DISEÑO ====================
    float tamañoZonaFlecha = 80f;
    float tamañoZonaEspacio = 100f;
    
    // 💥 POSICIÓN CENTRADA: Las flechas estarán en el tercio inferior de la pantalla
    float posBaseY = (altoPantalla / 4) + 50f; // Ajuste para centrar visualmente
    
    float espacioEntreFlechas = 15f;
    
    // ==================== CENTRADO HORIZONTAL ====================
    // Calcular el punto de inicio X para centrar el bloque de 4 flechas
    float anchoTotalFlechas = (tamañoZonaFlecha * 4) + (espacioEntreFlechas * 3);
    float inicioX = (anchoPantalla / 2) - (anchoTotalFlechas / 2) + (tamañoZonaFlecha / 2);
    
    // ==================== CREAR TEXTURAS PROCEDURALES ====================
    System.out.println("  📐 Generando texturas de zonas...");
    
    textureZonaLeft = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.IZQUIERDA,
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT,
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaDown = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.ABAJO,
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT,
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaUp = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.ARRIBA,
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT,
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaRight = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.DERECHA,
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT,
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaEspacio = flechaGenerator.crearTexturaZonaEspacio(
        FlechaProceduralGenerator.ZONA_BORDE_ESPACIO,
        FlechaProceduralGenerator.ZONA_FONDO_ESPACIO
    );
    
    System.out.println("  ✓ Texturas de zonas generadas");
    
    // ==================== POSICIONAR LAS 4 FLECHAS ====================
    
    // 1. Zona IZQUIERDA
    float xLeft = inicioX;
    zonaLeftGeom = crearGeometriaZona(
        textureZonaLeft,
        new Vector3f(xLeft, posBaseY, 0),
        tamañoZonaFlecha
    );
    
    // 2. Zona ABAJO
    float xDown = xLeft + tamañoZonaFlecha + espacioEntreFlechas;
    zonaDownGeom = crearGeometriaZona(
        textureZonaDown,
        new Vector3f(xDown, posBaseY, 0),
        tamañoZonaFlecha
    );
    
    // 3. Zona ARRIBA
    float xUp = xDown + tamañoZonaFlecha + espacioEntreFlechas;
    zonaUpGeom = crearGeometriaZona(
        textureZonaUp,
        new Vector3f(xUp, posBaseY, 0),
        tamañoZonaFlecha
    );
    
    // 4. Zona DERECHA
    float xRight = xUp + tamañoZonaFlecha + espacioEntreFlechas;
    zonaRightGeom = crearGeometriaZona(
        textureZonaRight,
        new Vector3f(xRight, posBaseY, 0),
        tamañoZonaFlecha
    );
    
    // 5. Zona ESPACIO (Centrada arriba de las flechas)
    zonaEspacioGeom = crearGeometriaZona(
        textureZonaEspacio,
        new Vector3f(anchoPantalla / 2, posBaseY + tamañoZonaFlecha + 40f, 0),
        tamañoZonaEspacio
    );
    
    // ==================== AÑADIR AL GUI NODE ====================
    guiNode.attachChild(zonaLeftGeom);
    guiNode.attachChild(zonaDownGeom);
    guiNode.attachChild(zonaUpGeom);
    guiNode.attachChild(zonaRightGeom);
    guiNode.attachChild(zonaEspacioGeom);
    
    System.out.println("✓ Zonas de impacto creadas y posicionadas");
    System.out.println("  - Posición base Y: " + posBaseY);
    System.out.println("  - Centro X: " + (anchoPantalla / 2));
    System.out.println("  - Rango X flechas: " + xLeft + " a " + (xRight + tamañoZonaFlecha));
}

/**
 * Helper para crear la geometría de una zona de impacto
 */
    private Geometry crearGeometriaZona(Texture textura, Vector3f posicion, float tamaño) {
    Mesh mesh = crearMeshCuadrado(tamaño);
    Geometry zona = new Geometry("Zona", mesh);
    
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", textura);
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    zona.setMaterial(mat);
    
    // Centrar la geometría en la posición especificada
    zona.setLocalTranslation(posicion.x - tamaño/2, posicion.y - tamaño/2, 0.5f);
    zona.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Gui);
    return zona;
    }
    @Override
protected void initialize(Application app) {
    // ==================== PASO 1: REFERENCIAS BÁSICAS ====================
    this.app = (MiJuegoDeRitmo) app;
    this.assetManager = app.getAssetManager();
    this.rootNode = ((SimpleApplication) app).getRootNode();
    this.guiNode = ((SimpleApplication) app).getGuiNode();
    this.urlLoader = new URLTextureLoader(assetManager);
    
    System.out.println("\n🎮 Inicializando GameplayAppState...");
    
    // ==================== PASO 2: CREAR NODO DEL JUEGO ====================
    gameNode = new Node("GameplayNode");
    rootNode.attachChild(gameNode);
    System.out.println("✓ GameNode creado y añadido al RootNode");
    
    // ==================== PASO 3: GENERADOR PROCEDURAL ====================
    flechaGenerator = new FlechaProceduralGenerator(assetManager);
    System.out.println("✓ FlechaProceduralGenerator inicializado");
    
    // ==================== PASO 4: CARGAR TEXTURAS PROCEDURALES PARA ZONAS ====================
    // ⭐ ESTO ES LO QUE FALTABA - Cargar texturas de zonas de impacto
    System.out.println("\n📐 Cargando texturas procedurales para zonas de impacto...");
    
    // Cargar texturas para zonas direccionales (flechas)
    textureZonaUp = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.ARRIBA, 
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT, 
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaDown = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.ABAJO, 
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT, 
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaLeft = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.IZQUIERDA, 
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT, 
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    textureZonaRight = flechaGenerator.crearTexturaZonaFlecha(
        Direccion.DERECHA, 
        FlechaProceduralGenerator.ZONA_BORDE_DEFAULT, 
        FlechaProceduralGenerator.ZONA_FONDO_DEFAULT
    );
    
    // Cargar textura para zona de ESPACIO (cuadrado redondeado especial)
    textureZonaEspacio = flechaGenerator.crearTexturaZonaEspacio(
        FlechaProceduralGenerator.ZONA_BORDE_ESPACIO, 
        FlechaProceduralGenerator.ZONA_FONDO_ESPACIO
    );
    
    System.out.println("✓ Texturas de zonas de impacto cargadas proceduralmente");
    
    // ==================== PASO 5: CREAR ELEMENTOS VISUALES ====================
    crearFondo();
    crearProtagonista();
    setupZonasImpacto();// ⭐ Esto usa las texturas que acabamos de cargar
    
    System.out.println("✅ GameplayAppState inicializado correctamente\n");
    // ==================== PASO 6: CONFIGURAR GAMEPLAY ====================
    setupInputs();
    inicializarUI();
    
    // ==================== PASO 7: PREPARAR CANCIÓN ====================
    generarFlechasCancion();
    reproducirCancionActual();
    
    System.out.println("✅ GameplayAppState inicializado correctamente\n");
}

    private void crearFondo() {
    System.out.println("\n🌌 Creando fondo espacial...");
    
    float ancho = app.getCamera().getWidth();
    float alto = app.getCamera().getHeight();
    
    // ⭐ OPCIÓN 1: Desde URL con fallback local
    String urlFondo = "https://ejemplo.com/api/assets/fondo_espacio.png";
    String rutaFallback = "assets/Texture/espacio.png";
    
    try {
        System.out.println(" → Cargando fondo desde URL: " + urlFondo);
        
        Texture fondoTexture = urlLoader.cargarConFallback(urlFondo, rutaFallback);
        
        if (fondoTexture != null) {
            Material fondoMat = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
            fondoMat.setTexture("ColorMap", fondoTexture);
            fondoMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            
            fondoGeometry = new Geometry("FondoGameplay", new Quad(ancho, alto));
            fondoGeometry.setMaterial(fondoMat);
            fondoGeometry.setLocalTranslation(0, 0, -1f);
            
            app.getGuiNode().attachChild(fondoGeometry);
            System.out.println(" ✓ Fondo cargado desde URL correctamente");
        } else {
            crearFondoFallback(ancho, alto);
        }
    } catch (Exception e) {
        System.err.println(" ❌ Error cargando fondo desde URL: " + e.getMessage());
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
    
    // ⭐ OPCIÓN 2: Desde API REST
    String urlAPI = "https://tu-api.com/api/sprites/protagonista/default";
    String rutaFallback = "assets/Texture/Protagonista/astronautaPoseDefault.png";
    
    try {
        System.out.println(" → Cargando protagonista desde API: " + urlAPI);
        
        Texture astroTexture = urlLoader.cargarConFallback(urlAPI, rutaFallback);
        
        if (astroTexture != null) {
            Material astroMat = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
            astroMat.setTexture("ColorMap", astroTexture);
            astroMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            
            float astroSize = 200f;
            protagonistaGeometry = new Geometry("Protagonista", 
                new Quad(astroSize, astroSize));
            protagonistaGeometry.setMaterial(astroMat);
            
            float posX = (ancho / 2f) - (astroSize / 2f);
            float posY = 50f;
            protagonistaGeometry.setLocalTranslation(posX, posY, 0f);
            
            app.getGuiNode().attachChild(protagonistaGeometry);
            System.out.println(" ✓ Protagonista cargado desde API");
        } else {
            crearProtagonistaFallback(ancho, alto);
        }
    } catch (Exception e) {
        System.err.println(" ❌ Error: " + e.getMessage());
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
    
    // 1. Detener audio primero
    if (audioNode != null) {
        audioNode.stop();
    }
    
    // 2. ⭐ SOLUCIÓN MÁS SIMPLE: Dejar que volverAlMenu() se encargue de todo
    app.enqueue(() -> {
        app.volverAlMenu();
        return null;
    });
}
    
    private void toggleMusicaJuego() {
        if (audioNode != null) {
            if (menuPausa.estaMusicaSilenciada()) {
                audioNode.setVolume(0f);
            } else {
                audioNode.setVolume(app.getMasterVolumen());
            }
        }
    }

    // ==================== UPDATE ====================
    
    private void verificarFinCancion() {
    // Verificar si el audio terminó
    boolean audioTerminado = (audioNode != null && audioNode.getStatus() == AudioSource.Status.Stopped);
    
    // Verificar si el tiempo superó la duración + margen de seguridad
    boolean tiempoSuperado = (duracionCancion > 0 && tiempoTranscurrido >= duracionCancion + 3.0f);
    
    // Verificar si ya no hay más flechas
    boolean sinFlechasPendientes = (indiceFlechaActual >= flechasAGenerar.size() && flechasActivas.isEmpty());
    
    if ((audioTerminado || tiempoSuperado || sinFlechasPendientes) && !cancionTerminada) {
        cancionTerminada = true;
        
        System.out.println("\n=== CANCIÓN TERMINADA ===");
        System.out.println("Tiempo transcurrido: " + String.format("%.1f", tiempoTranscurrido) + "s");
        System.out.println("Duración canción: " + String.format("%.1f", duracionCancion) + "s");
        System.out.println("Flechas restantes: " + (flechasAGenerar.size() - indiceFlechaActual));
        System.out.println("Flechas activas: " + flechasActivas.size());
        
        // Esperar 2 segundos antes de mostrar resultados
        app.enqueue(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            terminarJuego("cancion_finalizada");
            return null;
        });
    }
}
    private String convertirRutaRelativa(String rutaAbsoluta) {
    System.out.println("🔍 Convirtiendo ruta: " + rutaAbsoluta);
    
    String rutaNormalizada = rutaAbsoluta.replace("\\", "/");
    
    int indexAssets = rutaNormalizada.toLowerCase().indexOf("assets");
    
    if (indexAssets != -1) {
        String rutaDesdeAssets = rutaNormalizada.substring(indexAssets + 7);
        System.out.println("✓ Ruta relativa extraída: " + rutaDesdeAssets);
        return rutaDesdeAssets;
    }
    
    java.io.File archivo = new java.io.File(rutaAbsoluta);
    String nombreArchivo = archivo.getName();
    String rutaFallback = "canciones/" + nombreArchivo;
    
    System.out.println("⚠ No se encontró 'assets' en la ruta");
    System.out.println("✓ Usando ruta fallback: " + rutaFallback);
    
    return rutaFallback;
}
    @Override
public void update(float tpf) {
    // Bloquear si está pausado
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

    // Actualizar tiempo
    tiempoTranscurrido += tpf;

    if (gameplayUI != null) {
        gameplayUI.actualizarFeedback(tpf);
        gameplayUI.actualizarCombo(combo);
        gameplayUI.actualizarProgreso(tiempoTranscurrido, duracionCancion); // ⭐ SI TIENES ESTE MÉTODO
    }

    // Generar flechas
    generarFlechasPorTiempo();
    
    // Verificar misses
    verificarMisses();

    // ⭐ VERIFICACIÓN MEJORADA DE FIN DE CANCIÓN
    verificarFinCancion();

    // Game Over por vida
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
        tiempoTranscurrido = 0f;
    cancionTerminada = false; // ⭐ AGREGAR ESTO
    indiceFlechaActual = 0;
    flechasActivas.clear();
    
    gameNode.detachAllChildren();
    
    System.out.println("✓ Estadísticas reiniciadas para nueva canción");
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
    
    // ⭐ CRÍTICO: Calcular duración de la canción
    duracionCancion = analisisActual.getDuracionTotal();
    cancionTerminada = false;
    
    System.out.println("Total de flechas a generar: " + flechasAGenerar.size());
    System.out.println("Duración de la canción: " + String.format("%.1f", duracionCancion) + "s");
    
    if (flechasAGenerar.isEmpty()) {
        System.err.println("⚠ ADVERTENCIA: No se generaron flechas");
    }
}
    
    private void reproducirCancionActual() {
    if (cancionActual >= canciones.size()) {
        System.out.println("Todas las canciones completadas");
        finalizarJuegoCompleto();
        return;
    }

    String rutaCancion = canciones.get(cancionActual);
    String rutaRelativa = convertirRutaRelativa(rutaCancion);
    
    System.out.println("Cargando audio:");
    System.out.println("  Ruta relativa: " + rutaRelativa);

    try {
        // ⭐ USAR Buffer PARA CANCIONES CORTAS (streaming da problemas de duración)
        audioNode = new AudioNode(assetManager, rutaRelativa, com.jme3.audio.AudioData.DataType.Buffer);
        audioNode.setPositional(false);
        audioNode.setVolume(app.getMasterVolumen());
        audioNode.setLooping(false);
        
        System.out.println("  ✓ Audio cargado (Buffer)");
        
        audioNode.play();
        System.out.println("✓ Reproduciendo: " + rutaRelativa);
        System.out.println("✓ Duración esperada: " + String.format("%.1f", duracionCancion) + "s");

        if (gameplayUI != null) {
            gameplayUI.mostrarCancion(rutaRelativa);
        }

    } catch (Exception e) {
        System.err.println("❌ ERROR al cargar audio: " + e.getMessage());
        e.printStackTrace();
        
        cancionActual++;
        if (cancionActual < canciones.size()) {
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
    /**
 * ⭐ CORREGIDO: Creación de flechas SIN sobrescribir texturas con colores
 */
/**
 * ⭐ VERSIÓN FINAL - Muestra sprites SIN sobrescribir con colores
 */
// ==================== FRAGMENTO ACTUALIZADO DE GAMEPLAYAPPSTATE ====================

/**
 * INSTRUCCIONES:
 * Reemplaza el método crearFlechaEnPantalla() en tu GameplayAppState
 * con esta versión que usa flechas procedurales
 */

// ⭐ AGREGAR ESTE ATRIBUTO AL INICIO DE LA CLASE

// ⭐ MÉTODO ACTUALIZADO: crearFlechaEnPantalla()
// ==================== FRAGMENTO A REEMPLAZAR EN GameplayAppState.java ====================

/**
 * ⭐ MODIFICADO: Rotación selectiva según tipo de flecha
 * - DORADA: Mantiene rotación original (mecánica especial)
 * - NORMAL, RAPIDA, LUNA: Rotan 180° adicionales (apuntan hacia su origen)
 */
private void crearFlechaEnPantalla(FlechaData flechaData) {
    System.out.println("🎯 Creando flecha: " + flechaData.getTipo() + " " + flechaData.getDireccion());

    // ========== PASO 1: DETERMINAR COLORES ==========
    ColorRGBA colorPrincipal;
    ColorRGBA colorBorde;
    
    if (flechaData.getTipo() == TipoFlecha.NORMAL) {
        colorPrincipal = flechaGenerator.obtenerColorPorDireccion(flechaData.getDireccion());
        colorBorde = flechaGenerator.obtenerBordePorDireccion(flechaData.getDireccion());
    } else {
        colorPrincipal = obtenerColorPorTipo(flechaData.getTipo());
        colorBorde = obtenerBordePorTipo(flechaData.getTipo());
    }

    // ========== PASO 2: CREAR TEXTURA PROCEDURAL ==========
    Texture texturaFlecha = flechaGenerator.crearTexturaFlecha(colorPrincipal, colorBorde);

    // ========== PASO 3: CREAR MATERIAL ==========
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", texturaFlecha);
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

    // ========== PASO 4: CREAR GEOMETRÍA ==========
    float tamano = 70f;
    Mesh mesh = crearMeshCuadrado(tamano);
    Geometry flechaGeom = new Geometry("Flecha_" + flechaData.getTipo(), mesh);
    flechaGeom.setMaterial(mat);

    // ========== PASO 5: POSICIONAR ==========
    float ancho = app.getCamera().getWidth();
    float alto = app.getCamera().getHeight();
    Vector3f posInicial = flechaData.getDireccion().getPosicionSpawn(ancho, alto);
    Vector3f posTarget = flechaData.getDireccion().getPosicionTarget(ancho, alto);
    flechaGeom.setLocalTranslation(posInicial.x - tamano/2, posInicial.y - tamano/2, 1);

    // ========== PASO 6: ROTAR SEGÚN DIRECCIÓN Y TIPO ⭐ MODIFICADO ==========
    float rotacion = 0f;

// Las flechas deben apuntar hacia donde van (hacia el target)
switch(flechaData.getDireccion()) {
    case IZQUIERDA:
        // Aparece desde la IZQUIERDA → va a la DERECHA → apunta a la DERECHA
        rotacion = 90f; // Apunta →
        break;
        
    case DERECHA:
        // Aparece desde la DERECHA → va a la IZQUIERDA → apunta a la IZQUIERDA
        rotacion = -90f; // Apunta ←
        break;
        
    case ARRIBA:
        // Aparece desde ARRIBA → va hacia ABAJO → apunta hacia ABAJO
        rotacion = 180f; // Apunta ↓
        break;
        
    case ABAJO:
        // Aparece desde ABAJO → va hacia ARRIBA → apunta hacia ARRIBA
        rotacion = 0f; // Apunta ↑ (sin rotación, es la posición por defecto)
        break;
        
    case ESPACIO:
        // Flecha especial, sin rotación específica
        rotacion = 0f;
        break;
}
    
    // ⭐ NUEVA LÓGICA: Solo invertir si NO es flecha dorada
    if (flechaData.getTipo() != TipoFlecha.DORADA) {
        // Para flechas normales, rápidas y luna: invertir 180 grados
        rotacion += 180f;
        System.out.println("  → Rotación invertida: " + rotacion + "° (apunta hacia origen)");
    } else {
        // Para flechas doradas: mantener rotación original (mecánica especial)
        System.out.println("  → Rotación original: " + rotacion + "° (flecha dorada, no invertir)");
    }
    
    if (rotacion != 0) {
    flechaGeom.rotate(0, 0, rotacion * FastMath.DEG_TO_RAD);
}


    // ========== PASO 7: AÑADIR CONTROL ==========
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

    // ========== PASO 8: AÑADIR A LA ESCENA ==========
    gameNode.attachChild(flechaGeom);
    flechasActivas.add(flechaGeom);
    
    System.out.println(" ✓ Flecha creada exitosamente en pos: " + posInicial);
}

private ColorRGBA obtenerColorPorTipo(TipoFlecha tipo) {
    switch (tipo) {
        case RAPIDA:
            return FlechaProceduralGenerator.COLOR_VERDE;
        case DORADA:
            return FlechaProceduralGenerator.COLOR_DORADO;
        case LUNA:
            return FlechaProceduralGenerator.COLOR_LUNA;
        default:
            return FlechaProceduralGenerator.COLOR_AZUL;
    }
}

/**
 * Obtiene color de borde por tipo de flecha
 */
private ColorRGBA obtenerBordePorTipo(TipoFlecha tipo) {
    switch (tipo) {
        case RAPIDA:
            return FlechaProceduralGenerator.BORDE_VERDE;
        case DORADA:
            return FlechaProceduralGenerator.BORDE_DORADO;
        case LUNA:
            return FlechaProceduralGenerator.BORDE_LUNA;
        default:
            return FlechaProceduralGenerator.BORDE_AZUL;
    }
}

/**
 * Crea un mesh cuadrado simple
 */
private Mesh crearMeshCuadrado(float tamano) {
    Mesh mesh = new Mesh();
    
    float half = tamano / 2f;
    
    // Vértices (4 esquinas del cuadrado)
    float[] vertices = new float[] {
        -half, -half, 0f,  // Bottom-left
         half, -half, 0f,  // Bottom-right
         half,  half, 0f,  // Top-right
        -half,  half, 0f   // Top-left
    };
    
    // UVs (coordenadas de textura)
    float[] texCoords = new float[] {
        0f, 0f,  // Bottom-left
        1f, 0f,  // Bottom-right
        1f, 1f,  // Top-right
        0f, 1f   // Top-left
    };
    
    // Índices (dos triángulos formando el cuadrado)
    int[] indices = new int[] {
        0, 1, 2,  // Primer triángulo (bottom-left, bottom-right, top-right)
        0, 2, 3   // Segundo triángulo (bottom-left, top-right, top-left)
    };
    
    // Configurar buffers del mesh*
    mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
    mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
    mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
    mesh.updateBound();
    mesh.setStatic();
    
    return mesh;

}

private String obtenerFallbackParaTipo(TipoFlecha tipo) {
    switch (tipo) {
        case DORADA:
            return GameAssets.FLECHA_AMARILLA;
        case LUNA:
            return GameAssets.FLECHA_AZUL;
        case RAPIDA:
            return GameAssets.FLECHA_ROJA;
        default:
            return GameAssets.FLECHA_AZUL;
 }
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
    
    // 1. Detener audio
    if (audioNode != null) {
        audioNode.stop();
        audioNode = null;
    }
    
    // 2. Limpiar fondo y protagonista
    if (fondoGeometry != null) {
        fondoGeometry.removeFromParent();
        fondoGeometry = null;
    }
    
    if (protagonistaGeometry != null) {
        protagonistaGeometry.removeFromParent();
        protagonistaGeometry = null;
    }
    
    // 3. ⭐ NUEVO: Limpiar zonas de impacto
    if (zonaUpGeom != null) {
        zonaUpGeom.removeFromParent();
        zonaUpGeom = null;
    }
    if (zonaDownGeom != null) {
        zonaDownGeom.removeFromParent();
        zonaDownGeom = null;
    }
    if (zonaLeftGeom != null) {
        zonaLeftGeom.removeFromParent();
        zonaLeftGeom = null;
    }
    if (zonaRightGeom != null) {
        zonaRightGeom.removeFromParent();
        zonaRightGeom = null;
    }
    if (zonaEspacioGeom != null) {
        zonaEspacioGeom.removeFromParent();
        zonaEspacioGeom = null;
    }
    
    // 4. ⭐ CRÍTICO: Limpiar GameplayUI completamente
    if (gameplayUI != null) {
        gameplayUI.limpiar();
        gameplayUI = null;
    }
    
    // 5. ⭐ CRÍTICO: Limpiar MenuPausa
    if (menuPausa != null) {
        menuPausa.limpiar();
        menuPausa = null;
    }
    
    // 6. Limpiar flechas activas
    if (flechasActivas != null) {
        for (Geometry flecha : flechasActivas) {
            if (flecha.getParent() != null) {
                flecha.removeFromParent();
            }
        }
        flechasActivas.clear();
    }
    
    // 7. ⭐ CRÍTICO: Limpiar gameNode DESPUÉS de todo lo demás
    if (gameNode != null) {
        gameNode.detachAllChildren();
        if (gameNode.getParent() != null) {
            gameNode.removeFromParent();
        }
        gameNode = null;
    }
    
    // 8. ⭐ NUEVO: Limpiar inputs del gameplay
    limpiarInputs();
    
    System.out.println("✓ GameplayAppState limpiado completamente");
}

/**
 * ⭐ NUEVO: Método para limpiar inputs del gameplay
 */
private void limpiarInputs() {
    try {
        app.getInputManager().deleteMapping("Arriba");
        app.getInputManager().deleteMapping("Abajo");
        app.getInputManager().deleteMapping("Izquierda");
        app.getInputManager().deleteMapping("Derecha");
        app.getInputManager().deleteMapping("Espacio");
        
        // Remover el listener
        app.getInputManager().removeListener(this);
        
        System.out.println("✓ Inputs del gameplay limpiados");
    } catch (Exception e) {
        System.err.println("⚠ Error limpiando inputs: " + e.getMessage());
    }

    // ... resto del código de limpieza ...
    }
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

