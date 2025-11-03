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

/**
 * Menú de pausa del gameplay
 * 
 * Características:
 * - Pausa el juego y la música
 * - Opciones: Reanudar, Silenciar música, Volver al menú
 * - Se activa con ESC o con botón en pantalla
 * - Overlay semi-transparente oscuro
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class MenuPausa {
    
    private final SimpleApplication app;
    private final Node guiNode;
    private final BitmapFont font;
    
    // Dimensiones
    private final float ancho;
    private final float alto;
    
    // Nodo contenedor del menú
    private Node menuPausaNode;
    
    // Botón de pausa (siempre visible durante gameplay)
    private Geometry btnPausaGeom;
    private BitmapText txtPausaBtn;
    
    // Estado
    private boolean pausado = false;
    private boolean musicaSilenciada = false;
    
    // Callbacks
    private Runnable onReanudar;
    private Runnable onVolverAlMenu;
    private Runnable onToggleMusica;
    
    // Botones del menú
    private List<BotonMenu> botones;
    private int botonSeleccionado = 0;
    
    /**
     * Constructor
     */
    public MenuPausa(SimpleApplication app, Runnable onReanudar, Runnable onVolverAlMenu, Runnable onToggleMusica) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        this.onReanudar = onReanudar;
        this.onVolverAlMenu = onVolverAlMenu;
        this.onToggleMusica = onToggleMusica;
        this.botones = new ArrayList<>();
        
        inicializar();
        
        System.out.println("✓ MenuPausa inicializado");
    }
    
    /**
     * Inicializa los elementos del menú
     */
    private void inicializar() {
        crearBotonPausa();
        crearMenuPausa();
        configurarInputs();
    }
    
    // ==================== BOTÓN DE PAUSA ====================
    
    /**
     * Crea el botón de pausa visible en la esquina superior derecha
     */
    private void crearBotonPausa() {
        float tamano = 50f;
        float posX = ancho - tamano - 20f;
        float posY = alto - tamano - 20f;
        
        // Fondo del botón
        Quad quad = new Quad(tamano, tamano);
        btnPausaGeom = new Geometry("BotonPausa", quad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.3f, 0.7f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        btnPausaGeom.setMaterial(mat);
        btnPausaGeom.setLocalTranslation(posX, posY, 10);
        
        // Texto del botón
        txtPausaBtn = new BitmapText(font);
        txtPausaBtn.setSize(32f);
        txtPausaBtn.setColor(ColorRGBA.White);
        txtPausaBtn.setText("||");
        txtPausaBtn.setLocalTranslation(posX + 12f, posY + 35f, 11);
        
        guiNode.attachChild(btnPausaGeom);
        guiNode.attachChild(txtPausaBtn);
        
        System.out.println("✓ Botón de pausa creado");
    }
    
    // ==================== MENÚ DE PAUSA ====================
    
    /**
     * Crea el menú de pausa (inicialmente oculto)
     */
    private void crearMenuPausa() {
        menuPausaNode = new Node("MenuPausa");
        
        // Overlay oscuro de fondo
        crearOverlay();
        
        // Panel del menú
        crearPanelMenu();
        
        // No agregar al guiNode todavía (se agrega al pausar)
        System.out.println("✓ Menú de pausa creado");
    }
    
    /**
     * Crea el overlay oscuro semi-transparente
     */
    private void crearOverlay() {
        Quad overlayQuad = new Quad(ancho, alto);
        Geometry overlayGeom = new Geometry("OverlayPausa", overlayQuad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.75f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        overlayGeom.setMaterial(mat);
        overlayGeom.setLocalTranslation(0, 0, 100);
        
        menuPausaNode.attachChild(overlayGeom);
    }
    
    /**
     * Crea el panel central del menú con opciones
     */
    private void crearPanelMenu() {
        float centroX = ancho / 2;
        float centroY = alto / 2;
        
        // Título "PAUSA"
        BitmapText txtTitulo = new BitmapText(font);
        txtTitulo.setSize(48f);
        txtTitulo.setColor(ColorRGBA.Cyan);
        txtTitulo.setText("PAUSA");
        float anchoTitulo = txtTitulo.getLineWidth();
        txtTitulo.setLocalTranslation(centroX - anchoTitulo / 2, centroY + 150f, 102);
        menuPausaNode.attachChild(txtTitulo);
        
        // Botones del menú
        float yInicial = centroY + 50f;
        float espaciado = 80f;
        
        crearBoton("▶ REANUDAR", centroX, yInicial, ColorRGBA.Green, () -> reanudar());
        crearBoton("🔊 SILENCIAR MÚSICA", centroX, yInicial - espaciado, ColorRGBA.Yellow, () -> toggleMusica());
        crearBoton("⌂ VOLVER AL MENÚ", centroX, yInicial - espaciado * 2, ColorRGBA.Red, () -> volverAlMenu());
        
        // Instrucciones
        BitmapText txtInstrucciones = new BitmapText(font);
        txtInstrucciones.setSize(14f);
        txtInstrucciones.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        txtInstrucciones.setText("ESC para pausar/reanudar | ↑↓ para navegar | ENTER para seleccionar");
        float anchoInstr = txtInstrucciones.getLineWidth();
        txtInstrucciones.setLocalTranslation(centroX - anchoInstr / 2, yInicial - espaciado * 3, 102);
        menuPausaNode.attachChild(txtInstrucciones);
    }
    
    /**
     * Crea un botón del menú
     */
    private void crearBoton(String texto, float centroX, float y, ColorRGBA color, Runnable accion) {
        BitmapText txtBoton = new BitmapText(font);
        txtBoton.setSize(28f);
        txtBoton.setColor(color);
        txtBoton.setText(texto);
        
        float anchoTexto = txtBoton.getLineWidth();
        txtBoton.setLocalTranslation(centroX - anchoTexto / 2, y, 102);
        
        menuPausaNode.attachChild(txtBoton);
        botones.add(new BotonMenu(txtBoton, texto, color, accion));
    }
    
    // ==================== CONFIGURACIÓN DE INPUTS ====================
    
    /**
     * Configura los inputs del menú de pausa
     */
    private void configurarInputs() {
        // ESC para pausar/reanudar
        app.getInputManager().addMapping("Pausa", new KeyTrigger(KeyInput.KEY_ESCAPE));
        
        // Navegación en el menú
        app.getInputManager().addMapping("MenuArriba", new KeyTrigger(KeyInput.KEY_UP));
        app.getInputManager().addMapping("MenuAbajo", new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping("MenuSeleccionar", new KeyTrigger(KeyInput.KEY_RETURN));
        
        ActionListener listener = (name, isPressed, tpf) -> {
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
        
        app.getInputManager().addListener(listener, "Pausa", "MenuArriba", "MenuAbajo", "MenuSeleccionar");
    }
    
    // ==================== LÓGICA DEL MENÚ ====================
    
    /**
     * Alterna entre pausado y no pausado
     */
    public void togglePausa() {
        if (pausado) {
            reanudar();
        } else {
            pausar();
        }
    }
    
    /**
     * Pausa el juego
     */
    public void pausar() {
        if (pausado) return;
        
        pausado = true;
        botonSeleccionado = 0;
        
        // Mostrar menú
        guiNode.attachChild(menuPausaNode);
        
        // Actualizar selección visual
        actualizarSeleccionVisual();
        
        // Callback externo (pausar música, gameplay, etc.)
        System.out.println("⏸ JUEGO PAUSADO");
    }
    
    /**
     * Reanuda el juego
     */
    public void reanudar() {
        if (!pausado) return;
        
        pausado = false;
        
        // Ocultar menú
        menuPausaNode.removeFromParent();
        
        // Callback externo
        if (onReanudar != null) {
            onReanudar.run();
        }
        
        System.out.println("▶ JUEGO REANUDADO");
    }
    
    /**
     * Silencia/activa la música
     */
    private void toggleMusica() {
        musicaSilenciada = !musicaSilenciada;
        
        // Actualizar texto del botón
        String nuevoTexto = musicaSilenciada ? "🔇 ACTIVAR MÚSICA" : "🔊 SILENCIAR MÚSICA";
        botones.get(1).texto.setText(nuevoTexto);
        
        // Re-centrar el texto
        float centroX = ancho / 2;
        float anchoTexto = botones.get(1).texto.getLineWidth();
        float yActual = botones.get(1).texto.getLocalTranslation().y;
        botones.get(1).texto.setLocalTranslation(centroX - anchoTexto / 2, yActual, 102);
        
        // Callback externo
        if (onToggleMusica != null) {
            onToggleMusica.run();
        }
        
        System.out.println(musicaSilenciada ? "🔇 Música silenciada" : "🔊 Música activada");
    }
    
    /**
     * Vuelve al menú principal
     */
    private void volverAlMenu() {
        pausado = false;
        menuPausaNode.removeFromParent();
        
        // Callback externo
        if (onVolverAlMenu != null) {
            onVolverAlMenu.run();
        }
        
        System.out.println("⌂ Volviendo al menú principal...");
    }
    
    // ==================== NAVEGACIÓN ====================
    
    /**
     * Navega entre opciones del menú
     */
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
    
    /**
     * Actualiza el color del botón seleccionado
     */
    private void actualizarSeleccionVisual() {
        for (int i = 0; i < botones.size(); i++) {
            BotonMenu boton = botones.get(i);
            if (i == botonSeleccionado) {
                // Botón seleccionado: más grande y brillante
                boton.texto.setSize(32f);
                boton.texto.setColor(ColorRGBA.White);
            } else {
                // Botón no seleccionado: tamaño normal y color original
                boton.texto.setSize(28f);
                boton.texto.setColor(boton.colorOriginal);
            }
            
            // Re-centrar después de cambiar tamaño
            float centroX = ancho / 2;
            float anchoTexto = boton.texto.getLineWidth();
            float yActual = boton.texto.getLocalTranslation().y;
            boton.texto.setLocalTranslation(centroX - anchoTexto / 2, yActual, 102);
        }
    }
    
    /**
     * Ejecuta la acción del botón seleccionado
     */
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
    
    /**
     * Limpia el menú de pausa
     */
    public void limpiar() {
        if (btnPausaGeom != null) btnPausaGeom.removeFromParent();
        if (txtPausaBtn != null) txtPausaBtn.removeFromParent();
        if (menuPausaNode != null) menuPausaNode.removeFromParent();
        
        // Limpiar inputs
        app.getInputManager().deleteMapping("Pausa");
        app.getInputManager().deleteMapping("MenuArriba");
        app.getInputManager().deleteMapping("MenuAbajo");
        app.getInputManager().deleteMapping("MenuSeleccionar");
        
        System.out.println("✓ MenuPausa limpiado");
    }
    
    // ==================== CLASE INTERNA ====================
    
    /**
     * Clase auxiliar para representar un botón del menú
     */
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