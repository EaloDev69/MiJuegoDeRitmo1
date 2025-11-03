/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Menú de Pausa - CORREGIDO para no cerrar la aplicación
 * 
 * FUNCIONES:
 * - ⏸ Pausar el juego con ESC (sin cerrar app)
 * - ▶ Reanudar partida
 * - 🔊 Silenciar/Activar música
 * - ⌂ Volver al menú principal
 * - Navegación con ↑↓ y ENTER
 */
public class MenuPausa {

    private final SimpleApplication app;
    private final Node guiNode;
    private final BitmapFont font;
    private final float ancho;
    private final float alto;

    // Nodo del menú
    private Node menuPausaNode;

    // Estado
    private boolean pausado = false;
    private boolean musicaSilenciada = false;

    // Callbacks
    private Runnable onReanudar;
    private Runnable onVolverAlMenu;
    private Runnable onToggleMusica;
    private Consumer<Boolean> onCambioPausa; // ⭐ NUEVO: Callback para notificar cambio de pausa

    // Botones
    private List<BotonMenu> botones;
    private int botonSeleccionado = 0;

    // Listener de inputs
    private ActionListener inputListener;

    /**
     * Constructor ACTUALIZADO con callback de cambio de pausa
     */
    public MenuPausa(SimpleApplication app, Runnable onReanudar, Runnable onVolverAlMenu, 
                     Runnable onToggleMusica, Consumer<Boolean> onCambioPausa) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        
        this.onReanudar = onReanudar;
        this.onVolverAlMenu = onVolverAlMenu;
        this.onToggleMusica = onToggleMusica;
        this.onCambioPausa = onCambioPausa;
        
        this.botones = new ArrayList<>();

        inicializar();

        System.out.println("✓ MenuPausa inicializado");
    }

    /**
     * Constructor legacy (sin callback de pausa)
     */
    public MenuPausa(SimpleApplication app, Runnable onReanudar, Runnable onVolverAlMenu, Runnable onToggleMusica) {
        this(app, onReanudar, onVolverAlMenu, onToggleMusica, null);
    }

    private void inicializar() {
        crearMenuPausa();
        configurarInputs();
    }

    // ==================== CREACIÓN DEL MENÚ ====================

    private void crearMenuPausa() {
        menuPausaNode = new Node("MenuPausa");

        // Overlay oscuro
        crearOverlay();

        // Panel del menú
        crearPanelMenu();
    }

    private void crearOverlay() {
        Quad overlayQuad = new Quad(ancho, alto);
        Geometry overlayGeom = new Geometry("OverlayPausa", overlayQuad);

        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.85f)); // Más oscuro
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        overlayGeom.setMaterial(mat);
        overlayGeom.setLocalTranslation(0, 0, 100);

        menuPausaNode.attachChild(overlayGeom);
    }

    private void crearPanelMenu() {
        float centroX = ancho / 2;
        float centroY = alto / 2;

        // === TÍTULO "PAUSA" ===
        BitmapText txtTitulo = new BitmapText(font);
        txtTitulo.setSize(60f);
        txtTitulo.setColor(new ColorRGBA(1f, 0.3f, 0.5f, 1f)); // Rosa estilo FNF
        txtTitulo.setText("PAUSA");

        float anchoTitulo = txtTitulo.getLineWidth();
        txtTitulo.setLocalTranslation(centroX - anchoTitulo / 2, centroY + 200f, 102);

        menuPausaNode.attachChild(txtTitulo);

        // === BOTONES ===
        float yInicial = centroY + 80f;
        float espaciado = 90f;

        crearBoton("▶ REANUDAR", centroX, yInicial, new ColorRGBA(0.3f, 1f, 0.3f, 1f), () -> reanudar());
        crearBoton("🔊 SILENCIAR MÚSICA", centroX, yInicial - espaciado, new ColorRGBA(1f, 0.8f, 0.2f, 1f), () -> toggleMusica());
        crearBoton("⌂ VOLVER AL MENÚ", centroX, yInicial - espaciado * 2, new ColorRGBA(1f, 0.3f, 0.3f, 1f), () -> volverAlMenu());

        // === INSTRUCCIONES ===
        BitmapText txtInstrucciones = new BitmapText(font);
        txtInstrucciones.setSize(16f);
        txtInstrucciones.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        txtInstrucciones.setText("ESC = Pausar/Reanudar | ↑↓ = Navegar | ENTER = Seleccionar");

        float anchoInstr = txtInstrucciones.getLineWidth();
        txtInstrucciones.setLocalTranslation(centroX - anchoInstr / 2, yInicial - espaciado * 3, 102);

        menuPausaNode.attachChild(txtInstrucciones);
    }

    private void crearBoton(String texto, float centroX, float y, ColorRGBA color, Runnable accion) {
        BitmapText txtBoton = new BitmapText(font);
        txtBoton.setSize(32f);
        txtBoton.setColor(color);
        txtBoton.setText(texto);

        float anchoTexto = txtBoton.getLineWidth();
        txtBoton.setLocalTranslation(centroX - anchoTexto / 2, y, 102);

        menuPausaNode.attachChild(txtBoton);

        botones.add(new BotonMenu(txtBoton, texto, color, accion));
    }

    // ==================== CONFIGURACIÓN DE INPUTS ====================

    private void configurarInputs() {
        // ⭐ CRÍTICO: Limpiar mapeos previos de ESC si existen
        try {
            app.getInputManager().deleteMapping("SIMPLEAPP_Exit");
            System.out.println("✓ Mapeo por defecto 'SIMPLEAPP_Exit' eliminado");
        } catch (Exception e) {
            // No existía, ok
        }

        try {
            app.getInputManager().deleteMapping("Pausa");
        } catch (Exception e) {
            // No existía, ok
        }

        try {
            app.getInputManager().deleteMapping("MenuArriba");
            app.getInputManager().deleteMapping("MenuAbajo");
            app.getInputManager().deleteMapping("MenuSeleccionar");
        } catch (Exception e) {
            // No existían, ok
        }

        // Mapear teclas
        app.getInputManager().addMapping("Pausa", new KeyTrigger(KeyInput.KEY_ESCAPE));
        app.getInputManager().addMapping("MenuArriba", new KeyTrigger(KeyInput.KEY_UP));
        app.getInputManager().addMapping("MenuAbajo", new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping("MenuSeleccionar", new KeyTrigger(KeyInput.KEY_RETURN));

        // Listener
        inputListener = (name, isPressed, tpf) -> {
            if (!isPressed) return;

            switch (name) {
                case "Pausa":
                    togglePausa();
                    break;
                case "MenuArriba":
                    if (pausado) navegarMenu(-1);
                    break;
                case "MenuAbajo":
                    if (pausado) navegarMenu(1);
                    break;
                case "MenuSeleccionar":
                    if (pausado) ejecutarBotonSeleccionado();
                    break;
            }
        };

        app.getInputManager().addListener(inputListener, "Pausa", "MenuArriba", "MenuAbajo", "MenuSeleccionar");

        System.out.println("✓ Inputs de MenuPausa configurados");
        System.out.println("  - ESC ahora controla la pausa (no cierra la app)");
    }

    // ==================== LÓGICA DEL MENÚ ====================

    public void togglePausa() {
        if (pausado) {
            reanudar();
        } else {
            pausar();
        }
    }

    public void pausar() {
        if (pausado) return;

        pausado = true;
        botonSeleccionado = 0;

        // Mostrar menú
        guiNode.attachChild(menuPausaNode);
        actualizarSeleccionVisual();

        // ⭐ NUEVO: Notificar cambio de pausa al GameplayAppState
        if (onCambioPausa != null) {
            onCambioPausa.accept(true);
        }

        System.out.println("⏸ JUEGO PAUSADO");
    }

    public void reanudar() {
        if (!pausado) return;

        pausado = false;

        menuPausaNode.removeFromParent();

        // ⭐ NUEVO: Notificar cambio de pausa al GameplayAppState
        if (onCambioPausa != null) {
            onCambioPausa.accept(false);
        }

        if (onReanudar != null) {
            onReanudar.run();
        }

        System.out.println("▶ JUEGO REANUDADO");
    }

    private void toggleMusica() {
        musicaSilenciada = !musicaSilenciada;

        // Actualizar texto del botón
        String nuevoTexto = musicaSilenciada ? "🔈 ACTIVAR MÚSICA" : "🔊 SILENCIAR MÚSICA";
        botones.get(1).texto.setText(nuevoTexto);

        // Re-centrar
        float centroX = ancho / 2;
        float anchoTexto = botones.get(1).texto.getLineWidth();
        float yActual = botones.get(1).texto.getLocalTranslation().y;

        botones.get(1).texto.setLocalTranslation(centroX - anchoTexto / 2, yActual, 102);

        if (onToggleMusica != null) {
            onToggleMusica.run();
        }

        System.out.println(musicaSilenciada ? "🔇 Música silenciada" : "🔊 Música activada");
    }

    private void volverAlMenu() {
        pausado = false;
        menuPausaNode.removeFromParent();

        if (onVolverAlMenu != null) {
            onVolverAlMenu.run();
        }

        System.out.println("⌂ Volviendo al menú principal...");
    }

    // ==================== NAVEGACIÓN ====================

    private void navegarMenu(int direccion) {
        botonSeleccionado += direccion;

        // Wrap around
        if (botonSeleccionado < 0) {
            botonSeleccionado = botones.size() - 1;
        } else if (botonSeleccionado >= botones.size()) {
            botonSeleccionado = 0;
        }

        actualizarSeleccionVisual();
    }

    private void actualizarSeleccionVisual() {
        float centroX = ancho / 2;

        for (int i = 0; i < botones.size(); i++) {
            BotonMenu boton = botones.get(i);

            if (i == botonSeleccionado) {
                // Seleccionado: más grande y blanco
                boton.texto.setSize(40f);
                boton.texto.setColor(ColorRGBA.White);
            } else {
                // No seleccionado: normal
                boton.texto.setSize(32f);
                boton.texto.setColor(boton.colorOriginal);
            }

            // Re-centrar
            float anchoTexto = boton.texto.getLineWidth();
            float yActual = boton.texto.getLocalTranslation().y;
            boton.texto.setLocalTranslation(centroX - anchoTexto / 2, yActual, 102);
        }
    }

    private void ejecutarBotonSeleccionado() {
        if (botonSeleccionado >= 0 && botonSeleccionado < botones.size()) {
            botones.get(botonSeleccionado).accion.run();
        }
    }

    // ==================== GETTERS ====================

    public boolean estaPausado() {
        return pausado;
    }

    public boolean estaMusicaSilenciada() {
        return musicaSilenciada;
    }

    // ==================== LIMPIEZA ====================

    public void limpiar() {
        if (menuPausaNode != null) menuPausaNode.removeFromParent();

        // Limpiar inputs
        try {
            app.getInputManager().deleteMapping("Pausa");
            app.getInputManager().deleteMapping("MenuArriba");
            app.getInputManager().deleteMapping("MenuAbajo");
            app.getInputManager().deleteMapping("MenuSeleccionar");
        } catch (Exception e) {
            System.err.println("⚠ Error limpiando mapeos: " + e.getMessage());
        }

        if (inputListener != null) {
            app.getInputManager().removeListener(inputListener);
        }

        System.out.println("✓ MenuPausa limpiado");
    }

    // ==================== CLASE INTERNA ====================

    private static class BotonMenu {
        BitmapText texto;
        String textoOriginal;
        ColorRGBA colorOriginal;
        Runnable accion;

        BotonMenu(BitmapText texto, String textoOriginal, ColorRGBA color, Runnable accion) {
            this.texto = texto;
            this.textoOriginal = textoOriginal;
            this.colorOriginal = color.clone();
            this.accion = accion;
        }
    }
}