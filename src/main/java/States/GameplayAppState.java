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

/**
 * Estado del gameplay - Maneja la lógica del juego con el nuevo sistema de flechas
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class GameplayAppState extends BaseAppState {
    
    private MiJuegoDeRitmo app;
    private Node gameNode;
    private AssetManager assetManager;
    
    // UI
    private GameplayUI gameplayUI;
    
    // Sistema de flechas
    private FlechasGenerator flechasGenerator;
    private List<FlechaData> flechasAGenerar;
    private int indiceFlechaActual = 0;
    
    // Flechas activas en pantalla
    private List<Geometry> flechasActivas;
    
    // Canciones
    private List<String> canciones;
    private int cancionActual = 0;
    private AudioNode audioNode;
    private ResultadoAnalisis analisisActual;
    
    // Timing
    private float tiempoTranscurrido = 0f;
    private float tiempoAnticipacion = 2.0f; // Generar flechas 2 segundos antes
    
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
        this.flechasActivas = new ArrayList<>();
        this.flechasGenerator = new FlechasGenerator();
        
        // Cargar análisis de la primera canción
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
        
        // CRÍTICO: Deshabilitar FlyByCamera (causa error con cámara ortogonal)
        app.getFlyByCamera().setEnabled(false);
        
        // Calcular centro de pantalla
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        centroPantalla = new Vector3f(ancho / 2, alto / 2, 0);
        
        // Configurar cámara para 2D
        app.getCamera().setParallelProjection(true);
        float aspect = (float) app.getCamera().getWidth() / app.getCamera().getHeight();
        app.getCamera().setFrustum(-1000, 1000, -aspect * alto / 2, aspect * alto / 2, alto / 2, -alto / 2);
        
        // Crear área visual del juego
        crearAreaJuego();
        
        // Inicializar UI
        gameplayUI = new GameplayUI(app);
        
        // Inicializar inputs
        setupInputs();
        
        // Generar flechas para la canción actual
        generarFlechasCancion();
        
        // Reproducir música
        reproducirCancionActual();
        
        System.out.println("✓ Gameplay inicializado correctamente");
        System.out.println("  Centro: " + centroPantalla);
        System.out.println("  Cámara: " + ancho + "x" + alto);
    }
    
    /**
     * Crea el área visual del juego (centro donde llegan las flechas)
     */
    private void crearAreaJuego() {
        // Crear un círculo/cuadrado en el centro como objetivo
        Quad centroQuad = new Quad(80, 80);
        Geometry centroGeom = new Geometry("CentroObjetivo", centroQuad);
        
        Material matCentro = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matCentro.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.3f)); // Blanco semi-transparente
        matCentro.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        centroGeom.setMaterial(matCentro);
        
        // Centrar el cuadrado
        centroGeom.setLocalTranslation(centroPantalla.x - 40, centroPantalla.y - 40, -1);
        
        gameNode.attachChild(centroGeom);
        
        System.out.println("✓ Área de juego creada en el centro");
    }
    
    /**
     * Genera todas las flechas para la canción actual
     */
    private void generarFlechasCancion() {
        if (analisisActual == null) {
            System.err.println("ERROR: No hay análisis disponible");
            return;
        }
        
        System.out.println("\n=== GENERANDO FLECHAS PARA GAMEPLAY ===");
        flechasAGenerar = flechasGenerator.generarFlechasCompletas(analisisActual);
        indiceFlechaActual = 0;
        
        System.out.println("Total de flechas a generar: " + flechasAGenerar.size());
    }
    
    /**
     * Reproduce la canción actual
     */
    private void reproducirCancionActual() {
        if (cancionActual >= canciones.size()) {
            System.out.println("Todas las canciones completadas");
            return;
        }
        
        String rutaCancion = canciones.get(cancionActual);
        
        // Convertir ruta absoluta a ruta relativa desde assets/
        // Ejemplo: "C:/...../assets/canciones/song.wav" -> "canciones/song.wav"
        String rutaRelativa = rutaCancion;
        if (rutaCancion.contains("assets" + java.io.File.separator)) {
            int index = rutaCancion.indexOf("assets" + java.io.File.separator);
            rutaRelativa = rutaCancion.substring(index + 7); // 7 = longitud de "assets/"
        } else if (rutaCancion.contains("assets/")) {
            int index = rutaCancion.indexOf("assets/");
            rutaRelativa = rutaCancion.substring(index + 7);
        }
        
        // Normalizar las barras invertidas a barras normales
        rutaRelativa = rutaRelativa.replace("\\", "/");
        
        System.out.println("Ruta original: " + rutaCancion);
        System.out.println("Ruta relativa: " + rutaRelativa);
        
        try {
            audioNode = new AudioNode(assetManager, rutaRelativa, false);
            audioNode.setPositional(false);
            audioNode.setVolume(app.getMasterVolumen());
            audioNode.play();
            
            System.out.println("✓ Reproduciendo: " + rutaRelativa);
            
            // Mostrar nombre en UI
            if (gameplayUI != null) {
                gameplayUI.mostrarCancion(rutaRelativa);
            }
        } catch (Exception e) {
            System.err.println("ERROR al cargar audio: " + e.getMessage());
            e.printStackTrace();
            // Intentar siguiente canción si falla
            siguienteCancion();
        }
    }
    
    @Override
    public void update(float tpf) {
        tiempoTranscurrido += tpf;
        
        // Generar flechas que deben aparecer
        generarFlechasPorTiempo();
        
        // Verificar flechas erradas (que pasaron sin ser golpeadas)
        verificarMisses();
        
        // Verificar fin de canción
        if (audioNode != null && audioNode.getStatus() == com.jme3.audio.AudioSource.Status.Stopped) {
            siguienteCancion();
        }
    }
    
    /**
     * Genera flechas en pantalla según el tiempo de anticipación
     */
    private void generarFlechasPorTiempo() {
        while (indiceFlechaActual < flechasAGenerar.size()) {
            FlechaData flechaData = flechasAGenerar.get(indiceFlechaActual);
            
            // Verificar si es momento de generar esta flecha
            float tiempoGeneracion = flechaData.getBeatTime() - tiempoAnticipacion;
            
            if (tiempoTranscurrido >= tiempoGeneracion) {
                crearFlechaEnPantalla(flechaData);
                indiceFlechaActual++;
            } else {
                break; // Las siguientes flechas son más tarde
            }
        }
    }
    
    /**
     * Crea una flecha visible en pantalla
     */
    private void crearFlechaEnPantalla(FlechaData flechaData) {
        // Crear geometría de la flecha
        Quad quad = new Quad(50, 50);
        Geometry flechaGeom = new Geometry("Flecha", quad);
        
        // Material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // Color según tipo y dirección
        ColorRGBA color;
        if (flechaData.getTipo() == TipoFlecha.DORADA) {
            color = ColorRGBA.Yellow.mult(1.5f); // Dorado brillante
        } else {
            color = flechaData.getDireccion().getColor();
        }
        mat.setColor("Color", color);
        
        // Opcional: cargar textura
        // mat.setTexture("ColorMap", assetManager.loadTexture(flechaData.getDireccion().getTexturePath()));
        
        flechaGeom.setMaterial(mat);
        
        // Posición inicial (fuera de pantalla según dirección)
        float ancho = app.getCamera().getWidth();
        float alto = app.getCamera().getHeight();
        Vector3f posInicial = flechaData.getDireccion().getPosicionSpawn(ancho, alto);
        flechaGeom.setLocalTranslation(posInicial);
        
        // Rotación según dirección
        flechaGeom.rotate(0, 0, flechaData.getDireccion().getRotacion() * FastMath.DEG_TO_RAD);
        
        // Agregar control
        FlechaControl control = new FlechaControl(
            flechaData.getTipo(),
            flechaData.getDireccion(),
            posInicial,
            centroPantalla,
            flechaData.getVelocidad(),
            flechaData.getBeatTime(),
            mat
        );
        flechaGeom.addControl(control);
        
        // Agregar a la escena
        gameNode.attachChild(flechaGeom);
        flechasActivas.add(flechaGeom);
        
        System.out.println("Flecha generada: " + flechaData);
    }
    
    /**
     * Verifica flechas que fueron erradas (pasaron sin golpear)
     */
    private void verificarMisses() {
        Iterator<Geometry> iterator = flechasActivas.iterator();
        while (iterator.hasNext()) {
            Geometry flecha = iterator.next();
            FlechaControl control = flecha.getControl(FlechaControl.class);
            
            if (control != null && control.fueErrada() && !control.fueGolpeada()) {
                // Miss
                registrarMiss();
                flecha.removeFromParent();
                iterator.remove();
            } else if (control != null && control.fueGolpeada()) {
                // Remover flechas golpeadas después del efecto
                if (flecha.getParent() == null) {
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * Configura los inputs del jugador
     */
    private void setupInputs() {
        mappingTeclas = new HashMap<>();
        mappingTeclas.put(Direccion.ARRIBA, "Arriba");
        mappingTeclas.put(Direccion.ABAJO, "Abajo");
        mappingTeclas.put(Direccion.IZQUIERDA, "Izquierda");
        mappingTeclas.put(Direccion.DERECHA, "Derecha");
        
        // Mapear teclas
        app.getInputManager().addMapping("Arriba", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("Abajo", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("Derecha", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D));
        
        // Listener
        ActionListener inputListener = (name, isPressed, tpf) -> {
            if (!isPressed) return;
            
            // Buscar la dirección correspondiente
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
        };
        
        app.getInputManager().addListener(inputListener, "Arriba", "Abajo", "Izquierda", "Derecha");
    }
    
    /**
     * Procesa el input del jugador
     */
    private void procesarInput(Direccion direccion) {
        FlechaControl flechaMasCercana = null;
        float menorDelay = Float.MAX_VALUE;
        Geometry flechaGeometria = null;
        
        // Buscar la flecha más cercana en esa dirección
        for (Geometry flecha : flechasActivas) {
            FlechaControl control = flecha.getControl(FlechaControl.class);
            
            if (control == null || control.fueGolpeada() || control.fueErrada()) {
                continue;
            }
            
            // Verificar dirección (considerando inversión de doradas)
            Direccion direccionFlecha = control.getDireccion();
            if (control.getTipo() == TipoFlecha.DORADA && control.estaInvertida()) {
                direccionFlecha = direccionFlecha.getOpuesta();
            }
            
            if (direccionFlecha == direccion) {
                float delay = control.calcularDelay(tiempoTranscurrido);
                if (delay < menorDelay && delay < 0.3f) { // Ventana de 300ms
                    menorDelay = delay;
                    flechaMasCercana = control;
                    flechaGeometria = flecha;
                }
            }
        }
        
        // Si encontramos una flecha válida
        if (flechaMasCercana != null) {
            evaluarHit(flechaMasCercana, menorDelay);
            flechaMasCercana.brillar();
        }
    }
    
    /**
     * Evalúa la calidad del hit
     */
    private void evaluarHit(FlechaControl flecha, float delay) {
        int puntosBase;
        String feedback;
        ColorRGBA colorFeedback;
        
        if (delay < 0.05f) {
            // PERFECTO
            puntosBase = 100;
            feedback = "¡PERFECTO!";
            colorFeedback = new ColorRGBA(0, 1, 0.5f, 1); // Verde brillante
            perfectos++;
            combo++;
        } else if (delay < 0.15f) {
            // BUENO
            puntosBase = 50;
            feedback = "BUENO";
            colorFeedback = ColorRGBA.Yellow;
            buenos++;
            combo++;
        } else {
            // MALO
            puntosBase = 20;
            feedback = "MALO";
            colorFeedback = ColorRGBA.Orange;
            malos++;
            combo = 0;
        }
        
        // Multiplicador de tipo de flecha
        int puntos = (int)(puntosBase * flecha.getTipo().getMultiplicadorPuntos());
        
        // Multiplicador de combo
        if (combo > 5) {
            puntos = (int)(puntos * (1 + combo * 0.1f));
        }
        
        score += puntos;
        
        if (combo > maxCombo) {
            maxCombo = combo;
        }
        
        // Mostrar feedback en UI
        if (gameplayUI != null) {
            gameplayUI.mostrarFeedback(feedback, colorFeedback);
        }
        
        System.out.println(String.format("%s | +%d pts | Combo: %d | Delay: %.3fs", 
            feedback, puntos, combo, delay));
    }
    
    /**
     * Registra un miss
     */
    private void registrarMiss() {
        misses++;
        combo = 0;
        vida -= 10;
        
        // Mostrar feedback en UI
        if (gameplayUI != null) {
            gameplayUI.mostrarFeedback("MISS!", ColorRGBA.Red);
        }
        
        System.out.println("MISS! Vida: " + vida);
        
        if (vida <= 0) {
            gameOver();
        }
    }
    
    /**
     * Pasa a la siguiente canción
     */
    private void siguienteCancion() {
        // Pausar el juego para mostrar resultados
        if (audioNode != null) {
            audioNode.stop();
        }
        
        // Mostrar ventana de resultados
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            
            boolean continuar = VentanaResultados.mostrarResultados(
                parentFrame,
                canciones.get(cancionActual),
                score,
                maxCombo,
                perfectos,
                buenos,
                malos,
                misses,
                vida
            );
            
            app.enqueue(() -> {
                if (continuar && cancionActual + 1 < canciones.size()) {
                    // Siguiente canción
                    cancionActual++;
                    reiniciarEstadisticasCancion();
                    System.out.println("\n=== SIGUIENTE CANCIÓN ===");
                    tiempoTranscurrido = 0f;
                    flechasActivas.clear();
                    generarFlechasCancion();
                    reproducirCancionActual();
                } else {
                    // Fin del juego
                    finDelJuego();
                }
                return null;
            });
        });
    }
    
    /**
     * Reinicia las estadísticas para la siguiente canción (excepto score)
     */
    private void reiniciarEstadisticasCancion() {
        perfectos = 0;
        buenos = 0;
        malos = 0;
        misses = 0;
        combo = 0;
        // Score y vida se mantienen
    }
    
    /**
     * Obtiene el Frame padre
     */
    private java.awt.Frame obtenerFramePadre() {
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            if (window instanceof java.awt.Frame) {
                return (java.awt.Frame) window;
            }
        }
        return null;
    }
    
    /**
     * Game Over
     */
    private void gameOver() {
        System.out.println("\n=== GAME OVER ===");
        mostrarEstadisticas();
        app.volverAlMenu();
    }
    
    /**
     * Fin del juego (completado)
     */
    private void finDelJuego() {
        System.out.println("\n=== ¡JUEGO COMPLETADO! ===");
        mostrarEstadisticas();
        app.volverAlMenu();
    }
    
    /**
     * Muestra las estadísticas finales
     */
    private void mostrarEstadisticas() {
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
    }
    
    @Override
    protected void cleanup(Application app) {
        // Detener música
        if (audioNode != null) {
            audioNode.stop();
        }
        
        // Limpiar UI
        if (gameplayUI != null) {
            gameplayUI.limpiar();
        }
        
        // Limpiar inputs
        app.getInputManager().deleteMapping("Arriba");
        app.getInputManager().deleteMapping("Abajo");
        app.getInputManager().deleteMapping("Izquierda");
        app.getInputManager().deleteMapping("Derecha");
        
        // Limpiar escena
        gameNode.detachAllChildren();
        flechasActivas.clear();
    }
    
    @Override
    protected void onEnable() {
        ((MiJuegoDeRitmo)getApplication()).getRootNode().attachChild(gameNode);
    }
    
    @Override
    protected void onDisable() {
        gameNode.removeFromParent();
    }
}