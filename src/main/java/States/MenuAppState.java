/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package States;



import Modelo.PlaylistManager;
import Modelo.AnalizadorCanciones;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import Modelo.FlechasGenerator;
import Modelo.FlechasGenerator.Difficulty;  // ⭐ IMPORTANTE
import UI.VentanaSeleccionDificultad;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;  // ⭐ AÑADIR para los diálogos de error

import UI.VentanaCargaCanciones;
import UI.VentanaInstrucciones;
import UI.VentanaSeleccionCanciones;
import UI.VentanaSeleccionDificultad;  // ⭐ IMPORTANTE - nueva ventana
import UI.BotonCapsulGenerator;
import UI.FondoEspacialAnimado;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;

/**
 * MenuAppState con botones estilizados tipo cápsula brillante
 */
public class MenuAppState extends BaseAppState {
    private MiJuegoDeRitmo juego;
    private PlaylistManager playlistManager;
    private AnalizadorCanciones analizador;

    // Guardamos la selección de canciones
    private final Set<String> cancionesSeleccionadas = new HashSet<>();
    private boolean modoAleatorioActivo = false;

    // ⭐ NUEVO: Nodo para los botones
    private Node nodoBotones;
    private List<BotonMenuEstilizado> botonesMenu;
    private boolean animandoBoton = false;
private int botonAnimandoIndex = -1;
private float tiempoAnimacionBoton = 0f;
private static final float DURACION_ANIMACION_BOTON = 0.3f;

    // Título
    private List<BitmapText> textosTitulo = new ArrayList<>();
    
    // Labels informativos
    private BitmapText lblSeleccionCount;
    private BitmapText lblModoAleatorio;
    private FlechasGenerator flechasGenerator;

    private FondoEspacialAnimado fondoEspacial;
    // Volumen
    private int volumenActual = 100;
    
    // Input
    private ActionListener mouseListener;

    public MenuAppState() {
        this.playlistManager = new PlaylistManager(resolverRutaCanciones());
        this.botonesMenu = new ArrayList<>();
        this.flechasGenerator = new FlechasGenerator();
    }

    @Override
    protected void initialize(Application app) {
        this.juego = (MiJuegoDeRitmo) app;
        this.analizador = new AnalizadorCanciones(app.getAssetManager());
        iniciarMenu(app);
    }

    public void iniciarMenu(Application app) {
        nodoBotones = new Node("BotonesMenu");
        fondoEspacial = new FondoEspacialAnimado(app);
        juego.getGuiNode().attachChild(fondoEspacial.getNodo());
        
        crearTituloGrande();
        crearBotonesEstilizados();
        crearLabelsInformativos();
        configurarInputMouse();
        
        juego.getGuiNode().attachChild(nodoBotones);
        cargarPlaylistAsync();
        System.out.println("✓ Menú inicializado con fondo espacial animado");
    }

    // ==================== TÍTULO ====================
    
    private void crearTituloGrande() {
        BitmapFont fuente = juego.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        
        float anchoVentana = juego.getCamera().getWidth();
        float altoVentana = juego.getCamera().getHeight();
        
        String textoTitulo = "=== JUEGO DE RITMO ===";
        float tamanoFuente = 28f;
        
        // Sombra
        BitmapText sombra = new BitmapText(fuente);
        sombra.setSize(tamanoFuente);
        sombra.setColor(new ColorRGBA(0f, 0f, 0f, 0.8f));
        sombra.setText(textoTitulo);
        
        float anchoTexto = sombra.getLineWidth();
        float x = (anchoVentana - anchoTexto) / 2f;
        float y = altoVentana - 50f;
        
        sombra.setLocalTranslation(x + 3, y - 3, 0);
        juego.getGuiNode().attachChild(sombra);
        textosTitulo.add(sombra);
        
        // Bordes para grosor
        ColorRGBA colorBorde = new ColorRGBA(0.2f, 0.2f, 0.3f, 1f);
        float[][] offsets = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
        };
        
        for (float[] offset : offsets) {
            BitmapText borde = new BitmapText(fuente);
            borde.setSize(tamanoFuente);
            borde.setColor(colorBorde);
            borde.setText(textoTitulo);
            borde.setLocalTranslation(x + offset[0], y + offset[1], 1);
            juego.getGuiNode().attachChild(borde);
            textosTitulo.add(borde);
        }
        
        // Texto principal
        BitmapText principal = new BitmapText(fuente);
        principal.setSize(tamanoFuente);
        principal.setColor(ColorRGBA.Cyan);
        principal.setText(textoTitulo);
        principal.setLocalTranslation(x, y, 2);
        juego.getGuiNode().attachChild(principal);
        textosTitulo.add(principal);
        
        System.out.println("✓ Título creado");
    }

    // ==================== BOTONES ESTILIZADOS ====================
    
    private void crearBotonesEstilizados() {
        float anchoVentana = juego.getCamera().getWidth();
        float altoVentana = juego.getCamera().getHeight();
        
        // Dimensiones de los botones
        float anchoBoton = 200f;
        float altoBoton = 35f;
        
        // Posición inicial (centrado)
        float xCentro = (anchoVentana - anchoBoton) / 2f;
        float yInicial = altoVentana / 2f + 150f;
        float espaciado = 50f;
        
        BitmapFont fuente = juego.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        
        // ========== BOTÓN 1: EMPEZAR JUEGO (Morado) ==========
        crearBoton(
            "EMPEZAR JUEGO",
            xCentro, yInicial,
            anchoBoton, altoBoton,
            BotonCapsulGenerator.ColoresBoton.MORADO,
            fuente,
            14f,
            () -> iniciarJuego()
        );
        
        // ========== BOTÓN 2: MODO PRÁCTICA (Cyan) ==========
        crearBoton(
            "MODO PRÁCTICA",
            xCentro, yInicial - espaciado,
            anchoBoton, altoBoton,
            BotonCapsulGenerator.ColoresBoton.CYAN,
            fuente,
            14f,
            () -> iniciarModoPractica()
        );
        
        // ========== BOTÓN 3: SELECCIONAR CANCIONES (Amarillo) ==========
        crearBoton(
            "Seleccionar Canciones",
            xCentro, yInicial - espaciado * 2,
            anchoBoton, altoBoton,
            BotonCapsulGenerator.ColoresBoton.AMARILLO,
            fuente,
            14f,
            () -> abrirPantallaSeleccion()
        );
        
        // ========== BOTÓN 4: ABRIR CARPETA (Naranja) ==========
        crearBoton(
            "Abrir Carpeta Canciones",
            xCentro, yInicial - espaciado * 3,
            anchoBoton, altoBoton,
            BotonCapsulGenerator.ColoresBoton.NARANJA,
            fuente,
            14f,
            () -> abrirCarpetaCanciones()
        );
        
        // ========== BOTÓN 5: CONFIGURAR VOLUMEN (Verde) ==========
        crearBoton(
            "Configurar Volumen",
            xCentro, yInicial - espaciado * 4,
            anchoBoton, altoBoton,
            BotonCapsulGenerator.ColoresBoton.VERDE,
            fuente,
            14f,
            () -> abrirVentanaVolumen()
        );
        
        // ========== BOTÓN 6: INSTRUCCIONES (Cyan claro) ==========
        crearBoton(
            "Instrucciones",
            xCentro, yInicial - espaciado * 5,
            anchoBoton, altoBoton,
            new ColorRGBA(0.4f, 0.8f, 0.9f, 1f),
            fuente,
            14f,
            () -> mostrarInstrucciones()
        );
        
        // ========== BOTÓN 7: SALIR (Rojo) ==========
        crearBoton(
            "Salir",
            xCentro, yInicial - espaciado * 6,
            anchoBoton, altoBoton,
            BotonCapsulGenerator.ColoresBoton.ROJO,
            fuente,
            14f,
            () -> juego.stop()
        );
        
        System.out.println("✓ Botones estilizados creados: " + botonesMenu.size());
    }
    
    private void crearBoton(
        String texto, float x, float y,
        float ancho, float alto,
        ColorRGBA color, BitmapFont fuente,
        float tamanoTexto, Runnable accion
    ) {
        // Crear textura del botón
        Texture textura = BotonCapsulGenerator.crearBotonCapsula(
            (int)ancho, (int)alto, color
        );
        
        // Crear geometría del botón
        Quad quad = new Quad(ancho, alto);
        Geometry geomBoton = new Geometry("Boton_" + texto, quad);
        
        Material mat = new Material(juego.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geomBoton.setMaterial(mat);
        geomBoton.setLocalTranslation(x, y, 0);
        
        nodoBotones.attachChild(geomBoton);
        
        // Crear texto encima del botón
        BitmapText txtBoton = new BitmapText(fuente);
        txtBoton.setSize(tamanoTexto);
        txtBoton.setColor(ColorRGBA.White);
        txtBoton.setText(texto);
        
        // Centrar el texto en el botón
        float anchoTexto = txtBoton.getLineWidth();
        float xTexto = x + (ancho - anchoTexto) / 2f;
        float yTexto = y + (alto / 2f) + (tamanoTexto / 3f); // Centrado vertical
        
        txtBoton.setLocalTranslation(xTexto, yTexto, 1);
        nodoBotones.attachChild(txtBoton);
        
        // Guardar botón
        botonesMenu.add(new BotonMenuEstilizado(geomBoton, txtBoton, x, y, ancho, alto, accion));
    }

    // ==================== LABELS INFORMATIVOS ====================
    
    private void crearLabelsInformativos() {
        BitmapFont fuente = juego.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        float anchoVentana = juego.getCamera().getWidth();
        
        // Label de canciones seleccionadas
        lblSeleccionCount = new BitmapText(fuente);
        lblSeleccionCount.setSize(16f);
        lblSeleccionCount.setColor(ColorRGBA.Gray);
        lblSeleccionCount.setText("Canciones seleccionadas: 0");
        
        float anchoTexto = lblSeleccionCount.getLineWidth();
        lblSeleccionCount.setLocalTranslation((anchoVentana - anchoTexto) / 2f, 150f, 0);
        juego.getGuiNode().attachChild(lblSeleccionCount);
        
        // Label de modo aleatorio
        lblModoAleatorio = new BitmapText(fuente);
        lblModoAleatorio.setSize(16f);
        lblModoAleatorio.setColor(ColorRGBA.Orange);
        lblModoAleatorio.setText("");
        lblModoAleatorio.setLocalTranslation((anchoVentana - 200f) / 2f, 120f, 0);
        juego.getGuiNode().attachChild(lblModoAleatorio);
    }
    
    private void actualizarContadorSeleccion() {
        int count = cancionesSeleccionadas.size();
        lblSeleccionCount.setText("Canciones seleccionadas: " + count);
        lblSeleccionCount.setColor(count == 0 ? ColorRGBA.Gray : ColorRGBA.Green);
        
        // Recentrar
        float anchoVentana = juego.getCamera().getWidth();
        float anchoTexto = lblSeleccionCount.getLineWidth();
        lblSeleccionCount.setLocalTranslation((anchoVentana - anchoTexto) / 2f, 150f, 0);
    }
    
    private void actualizarIndicadorModoAleatorio() {
        if (modoAleatorioActivo && !cancionesSeleccionadas.isEmpty()) {
            lblModoAleatorio.setText("🔀 Modo Aleatorio: ACTIVO");
            
            float anchoVentana = juego.getCamera().getWidth();
            float anchoTexto = lblModoAleatorio.getLineWidth();
            lblModoAleatorio.setLocalTranslation((anchoVentana - anchoTexto) / 2f, 120f, 0);
        } else {
            lblModoAleatorio.setText("");
        }
    }

    // ==================== INPUT DEL MOUSE ====================
    
    private void animarClickBoton(int indiceBoton) {
    if (indiceBoton < 0 || indiceBoton >= botonesMenu.size()) return;
    
    animandoBoton = true;
    botonAnimandoIndex = indiceBoton;
    tiempoAnimacionBoton = 0f;
    
    System.out.println("🎯 Animando botón: " + botonesMenu.get(indiceBoton).texto.getText());
}
    private void configurarInputMouse() {
        mouseListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("ClickMenu") && isPressed) {
                    manejarClickEnBoton();
                }
            }
        };
        
        juego.getInputManager().addMapping("ClickMenu", 
            new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        juego.getInputManager().addListener(mouseListener, "ClickMenu");
    }
    
    private void manejarClickEnBoton() {
    Vector2f cursorPos = juego.getInputManager().getCursorPosition();
    
    for (int i = 0; i < botonesMenu.size(); i++) {
        BotonMenuEstilizado boton = botonesMenu.get(i);
        
        if (boton.contienePunto(cursorPos.x, cursorPos.y)) {
            System.out.println("🖱 Click en: " + boton.texto.getText());
            
            // ⭐ NUEVO: Activar animación antes de ejecutar acción
            animarClickBoton(i);
            
            // Ejecutar acción después de un pequeño delay para que se vea la animación
            new Thread(() -> {
                try {
                    Thread.sleep(150); // Mitad de la duración de la animación
                    juego.enqueue(() -> {
                        boton.accion.run();
                        return null;
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
            break;
        }
    }
}
    public FlechasGenerator getFlechasGenerator() {
    return flechasGenerator;
}

    // ==================== MÉTODOS DE ACCIÓN ====================
    
        private void iniciarJuego() {
    if (juego == null) return;
    
    // Validar canciones disponibles
    List<String> cancionesDisponibles = playlistManager.getCanciones();
    if (cancionesDisponibles.isEmpty()) {
        System.err.println("❌ ERROR: No hay canciones en la carpeta");
        
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            JOptionPane.showMessageDialog(
                parentFrame,
                "⚠️ No hay canciones .wav en la carpeta\n\n" +
                "Por favor:\n" +
                "1. Haz clic en 'Abrir Carpeta Canciones'\n" +
                "2. Agrega archivos .wav\n" +
                "3. Vuelve a intentar",
                "Sin Canciones",
                JOptionPane.WARNING_MESSAGE
            );
        });
        return;
    }
    
    // Mostrar selector de dificultad
    SwingUtilities.invokeLater(() -> {
        java.awt.Frame parentFrame = obtenerFramePadre();
        Difficulty dificultad = VentanaSeleccionDificultad.mostrarDialogo(parentFrame);
        
        System.out.println("🎮 Dificultad seleccionada: " + dificultad);
        
        // Continuar en el thread de JME
        juego.enqueue(() -> {
            ejecutarInicioJuego(dificultad);
            return null;
        });
    });
}
    private void ejecutarInicioJuego(Difficulty dificultad) {
    System.out.println("\n=== PREPARANDO JUEGO ===");
    
    // Limpiar estado anterior
    if (juego.getStateManager().getState(GameplayAppState.class) != null) {
        System.out.println("⚠ Detectado GameplayAppState anterior - limpiando...");
        GameplayAppState estadoAnterior = juego.getStateManager().getState(GameplayAppState.class);
        juego.getStateManager().detach(estadoAnterior);
    }
    
    juego.getGuiNode().detachAllChildren();
    juego.getRootNode().detachAllChildren();
    
    // Determinar qué canciones jugar
    List<String> lista;
    if (cancionesSeleccionadas.isEmpty()) {
        lista = playlistManager.getCanciones();
        if (modoAleatorioActivo && !lista.isEmpty()) {
            lista = new ArrayList<>(lista);
            Collections.shuffle(lista);
            System.out.println("🔀 Todas las canciones mezcladas");
        }
    } else {
        lista = new ArrayList<>(cancionesSeleccionadas);
        if (modoAleatorioActivo) {
            Collections.shuffle(lista);
            System.out.println("🔀 Canciones seleccionadas mezcladas");
        }
    }

    if (lista.isEmpty()) {
        System.err.println("ERROR: No hay canciones!");
        return;
    }

    System.out.println("Canciones a analizar: " + lista.size());
    System.out.println("Modo aleatorio: " + (modoAleatorioActivo ? "SÍ 🔀" : "NO"));
    System.out.println("Dificultad: " + dificultad);

    if (modoAleatorioActivo) {
        System.out.println("\n🔀 ORDEN DE REPRODUCCIÓN (ALEATORIO):");
    } else {
        System.out.println("\nORDEN DE REPRODUCCIÓN:");
    }
    for (int i = 0; i < lista.size(); i++) {
        String nombre = new File(lista.get(i)).getName();
        System.out.println("  " + (i + 1) + ". " + nombre);
    }

    final List<String> listaFinal = new ArrayList<>(lista);

    // ⭐ PASO 3: Abrir ventana de análisis
    SwingUtilities.invokeLater(() -> {
        java.awt.Frame parentFrame = obtenerFramePadre();

        Map<String, ResultadoAnalisis> resultados =
            VentanaCargaCanciones.mostrarYAnalizarConResultados(
                parentFrame,
                listaFinal,
                analizador
            );

        // ⭐ PASO 4: Iniciar gameplay con los resultados
        juego.enqueue(() -> {
            if (!resultados.isEmpty()) {
                System.out.println("\n✓ Análisis completado - Iniciando gameplay");
                System.out.println("Resultados obtenidos: " + resultados.size() + " canciones");
                System.out.println("⚡ Aplicando dificultad: " + dificultad);
                
                if (modoAleatorioActivo) {
                    System.out.println("🔀 Playlist en modo ALEATORIO");
                }

                // Limpiar menú
                if (juego.getStateManager().getState(MenuAppState.class) != null) {
                    juego.getStateManager().detach(juego.getStateManager().getState(MenuAppState.class));
                }
                
                // ⭐ Crear y iniciar GameplayAppState con dificultad
                GameplayAppState gameplay = new GameplayAppState(listaFinal, resultados, dificultad);
                juego.getStateManager().attach(gameplay);
                
                System.out.println("✓ Gameplay iniciado con dificultad: " + dificultad);
                
            } else {
                System.out.println("\n✕ Análisis cancelado o falló");
            }
            return null;
        });
    });
}
     private void iniciarModoPractica() {
    System.out.println("\n=== INICIANDO MODO PRÁCTICA ===");
    System.out.println("Canciones seleccionadas: " + cancionesSeleccionadas.size());
    
    // Determinar qué canciones jugar
    List<String> lista;
    if (cancionesSeleccionadas.isEmpty()) {
        lista = playlistManager.getCanciones();
        if (modoAleatorioActivo && !lista.isEmpty()) {
            lista = new ArrayList<>(lista);
            Collections.shuffle(lista);
            System.out.println("🔀 Todas las canciones mezcladas");
        }
    } else {
        lista = new ArrayList<>(cancionesSeleccionadas);
        if (modoAleatorioActivo) {
            Collections.shuffle(lista);
            System.out.println("🔀 Canciones seleccionadas mezcladas");
        }
    }
    
    if (lista.isEmpty()) {
        System.err.println("ERROR: No hay canciones!");
        return;
    }
    
    System.out.println("\n=== PREPARANDO MODO PRÁCTICA ===");
    System.out.println("Canciones a analizar: " + lista.size());
    System.out.println("Modo aleatorio: " + (modoAleatorioActivo ? "SÍ 🔀" : "NO"));
    
    if (modoAleatorioActivo) {
        System.out.println("\n🔀 ORDEN DE REPRODUCCIÓN (ALEATORIO):");
    } else {
        System.out.println("\nORDEN DE REPRODUCCIÓN:");
    }
    
    for (int i = 0; i < lista.size(); i++) {
        String nombre = new File(lista.get(i)).getName();
        System.out.println("  " + (i + 1) + ". " + nombre);
    }
    
    final List<String> listaFinal = new ArrayList<>(lista);
    
    // Abrir ventana de carga y análisis
    SwingUtilities.invokeLater(() -> {
        java.awt.Frame parentFrame = obtenerFramePadre();
        
        Map<String, ResultadoAnalisis> resultados = 
                VentanaCargaCanciones.mostrarYAnalizarConResultados(
                        parentFrame,
                        listaFinal,
                        analizador
                );
        
        // Iniciar gameplay en el thread de JME con los resultados
        juego.enqueue(() -> {
            if (!resultados.isEmpty()) {
                System.out.println("\n✓ Análisis completado - Iniciando Modo Práctica");
                System.out.println("Resultados obtenidos: " + resultados.size() + " canciones");
                
                // ⭐ CORREGIDO: Llamar con modo práctica TRUE
                juego.startGameplayPractica(listaFinal, resultados);
            } else {
                System.out.println("\n✕ Análisis cancelado o falló");
            }
            return null;
        });
    });
}
    
    private void abrirCarpetaCanciones() {
        System.out.println("📂 Abriendo carpeta de canciones...");
        try {
            String rutaCarpeta = resolverRutaCanciones();
            File carpeta = new File(rutaCarpeta);
            
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(carpeta);
            }
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    cargarPlaylistAsync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void abrirVentanaVolumen() {
        System.out.println("🔊 Abriendo configuración de volumen...");
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            int nuevoVolumen = UI.VentanaVolumen.mostrarDialogo(parentFrame, volumenActual);
            
            juego.enqueue(() -> {
                volumenActual = nuevoVolumen;
                juego.setMasterVolume(volumenActual / 100.0f);
                return null;
            });
        });
    }
    
    private void mostrarInstrucciones() {
        System.out.println("📖 Mostrando instrucciones...");
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            VentanaInstrucciones.mostrar(parentFrame);
        });
    }

    // ==================== UTILIDADES ====================
    /**
 * ⭐ NUEVO: Actualiza la animación del botón clickeado
 */
private void actualizarAnimacionBotones(float tpf) {
    if (!animandoBoton || botonAnimandoIndex < 0 || botonAnimandoIndex >= botonesMenu.size()) {
        return;
    }
    
    tiempoAnimacionBoton += tpf;
    float progreso = tiempoAnimacionBoton / DURACION_ANIMACION_BOTON; // 0.0 a 1.0
    
    BotonMenuEstilizado boton = botonesMenu.get(botonAnimandoIndex);
    
    if (progreso <= 1.0f) {
        // ========== ANIMACIÓN DE REBOTE ==========
        float escala;
        if (progreso < 0.5f) {
            // Primera mitad: comprimir (1.0 → 0.90)
            float t = progreso / 0.5f;
            escala = 1.0f - (0.10f * easeInQuad(t));
        } else {
            // Segunda mitad: expandir con rebote (0.90 → 1.0)
            float t = (progreso - 0.5f) / 0.5f;
            escala = 0.90f + (0.10f * easeOutElastic(t));
        }
        
        // Aplicar escala a geometría y texto
        boton.geometria.setLocalScale(escala, escala, 1f);
        boton.texto.setSize(14f * escala);
        
        // Recentrar texto después de escalar
        float anchoTexto = boton.texto.getLineWidth();
        float xTexto = boton.x + (boton.ancho - anchoTexto) / 2f;
        float yTexto = boton.y + (boton.alto / 2f) + ((14f * escala) / 3f);
        boton.texto.setLocalTranslation(xTexto, yTexto, 1);
        
    } else {
        // ========== FINALIZAR ANIMACIÓN ==========
        animandoBoton = false;
        botonAnimandoIndex = -1;
        
        // Restaurar escala normal
        boton.geometria.setLocalScale(1f, 1f, 1f);
        boton.texto.setSize(14f);
        
        // Recentrar
        float anchoTexto = boton.texto.getLineWidth();
        float xTexto = boton.x + (boton.ancho - anchoTexto) / 2f;
        float yTexto = boton.y + (boton.alto / 2f) + (14f / 3f);
        boton.texto.setLocalTranslation(xTexto, yTexto, 1);
    }
}

/**
 * ⭐ NUEVO: Función de easing cuadrática
 */
private float easeInQuad(float t) {
    return t * t;
}

/**
 * ⭐ NUEVO: Función de easing elástica para rebote
 */
private float easeOutElastic(float t) {
    if (t == 0f || t == 1f) return t;
    float p = 0.3f;
    float s = p / 4f;
    return (float) (Math.pow(2, -10 * t) * Math.sin((t - s) * (2 * Math.PI) / p) + 1);
}
    
    private java.awt.Frame obtenerFramePadre() {
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            if (window instanceof java.awt.Frame) {
                return (java.awt.Frame) window;
            }
        }
        return null;
    }
    
    private String resolverRutaCanciones() {
        try {
            java.net.URI uri = com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            Path classesDir = Paths.get(uri);
            Path proyectoDir = classesDir.getParent().getParent();
            Path ruta = proyectoDir.resolve("assets").resolve("canciones");
            return ruta.toAbsolutePath().toString();
        } catch (Exception e) {
            Path ruta = Paths.get("assets", "canciones").toAbsolutePath();
            return ruta.toString();
        }
    }
    
    private void cargarPlaylistAsync() {
        new Thread(() -> {
            try {
                playlistManager.cargarPlaylist();
                List<String> canciones = playlistManager.getCanciones();
                System.out.println("Playlist cargada: " + canciones.size() + " canciones");
            } catch (Exception ex) {
                System.err.println("Error al cargar: " + ex.getMessage());
            }
        }, "PlaylistLoader").start();
    }

    // ==================== LIFECYCLE ====================
    
    @Override
    public void update(float tpf) {
        if (fondoEspacial != null) {
            fondoEspacial.actualizar(tpf);
        }
         actualizarAnimacionBotones(tpf);
    }

    @Override
    protected void cleanup(Application app) {
        limpiarMenu(app);
    }

    public void limpiarMenu(Application app) {
        // Limpiar título
        for (BitmapText texto : textosTitulo) {
            if (texto != null && texto.getParent() != null) {
                juego.getGuiNode().detachChild(texto);
            }
        }
        textosTitulo.clear();
        
        // Limpiar botones
        if (nodoBotones != null) {
            nodoBotones.detachAllChildren();
            nodoBotones.removeFromParent();
        }
        botonesMenu.clear();
        
        // Limpiar labels
        if (lblSeleccionCount != null && lblSeleccionCount.getParent() != null) {
            juego.getGuiNode().detachChild(lblSeleccionCount);
        }
        
        if (lblModoAleatorio != null && lblModoAleatorio.getParent() != null) {
            juego.getGuiNode().detachChild(lblModoAleatorio);
        }
        
         if (fondoEspacial != null) {
            fondoEspacial.limpiar();
            fondoEspacial.getNodo().removeFromParent();
            fondoEspacial = null;
        }
        // Limpiar input
        if (mouseListener != null) {
            try {
                juego.getInputManager().removeListener(mouseListener);
                juego.getInputManager().deleteMapping("ClickMenu");
            } catch (Exception e) {
                System.err.println("Error limpiando inputs: " + e.getMessage());
            }
        }
        
        System.out.println("✓ Menu limpiado completamente");
    }

    @Override
    protected void onEnable() {
        if (nodoBotones != null && nodoBotones.getParent() == null) {
            juego.getGuiNode().attachChild(nodoBotones);
        }
    }

    @Override
    protected void onDisable() {
        if (nodoBotones != null && nodoBotones.getParent() != null) {
            nodoBotones.removeFromParent();
        }
    }

    // ==================== GETTERS Y SETTERS ====================
    
    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    public void setPlaylistManager(PlaylistManager pm) {
        this.playlistManager = pm;
    }

    public boolean isModoAleatorioActivo() {
        return modoAleatorioActivo;
    }

    public void setModoAleatorio(boolean activo) {
        this.modoAleatorioActivo = activo;
        actualizarIndicadorModoAleatorio();
    }

    /**
 * Abre la ventana de selección de canciones
 */
private void abrirPantallaSeleccion() {
    System.out.println("📋 Abriendo ventana de selección de canciones...");
    
    SwingUtilities.invokeLater(() -> {
        java.awt.Frame parentFrame = obtenerFramePadre();
        
        // Mostrar ventana de selección
        VentanaSeleccionCanciones.ResultadoSeleccion resultado = 
            VentanaSeleccionCanciones.mostrarDialogo(
                parentFrame, 
                playlistManager, 
                cancionesSeleccionadas
            );
        
        // Actualizar selección en el thread de JME
        juego.enqueue(() -> {
            if (resultado != null && !resultado.isEmpty()) {
                // Actualizar canciones seleccionadas
                cancionesSeleccionadas.clear();
                cancionesSeleccionadas.addAll(resultado.getCancionesComoSet());
                
                // Actualizar modo aleatorio
                setModoAleatorio(resultado.isModoAleatorio());
                
                // Actualizar contador visual
                actualizarContadorSeleccion();
                actualizarIndicadorModoAleatorio();
                
                System.out.println("✓ Selección actualizada:");
                System.out.println("  - Canciones: " + cancionesSeleccionadas.size());
                System.out.println("  - Modo aleatorio: " + (resultado.isModoAleatorio() ? "SÍ 🔀" : "NO"));
            } else {
                System.out.println("⚠ Selección cancelada o sin cambios");
            }
            return null;
        });
    });
}
    
    // ==================== CLASE INTERNA: BOTÓN ESTILIZADO ====================
    
    /**
     * Clase interna que representa un botón con geometría, texto y área clickeable
     */


    private static class BotonMenuEstilizado {
        Geometry geometria;
        BitmapText texto;
        float x, y, ancho, alto;
        Runnable accion;
        
        BotonMenuEstilizado(Geometry geom, BitmapText txt, float x, float y, 
                           float ancho, float alto, Runnable accion) {
            this.geometria = geom;
            this.texto = txt;
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.accion = accion;
        }
        
        /**
         * Verifica si un punto (x, y) está dentro del área del botón
         */
        boolean contienePunto(float px, float py) {
            return px >= x && px <= (x + ancho) && 
                   py >= y && py <= (y + alto);
        }
    }
}