/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class LoadingScreen {


/**
 * Pantalla de carga visual
 * Muestra progreso de pre-carga de assets
 * 
 * USO:
 * LoadingScreen screen = new LoadingScreen(app);
 * screen.mostrar();
 * screen.actualizarProgreso(0.5f, "Cargando texturas...");
 * screen.ocultar();
 * 
 * @author CamiLaNekoUwU_Gamer
 */
    private final SimpleApplication app;
    private final Node guiNode;
    private final BitmapFont font;
    private final float ancho;
    private final float alto;
    
    // Nodo de la pantalla
    private Node loadingNode;
    
    // Componentes visuales
    private Geometry overlay;
    private Geometry barraFondo;
    private Geometry barraProgreso;
    private BitmapText txtTitulo;
    private BitmapText txtProgreso;
    private BitmapText txtMensaje;
    
    // Estado
    private boolean visible = false;
    
    
    public LoadingScreen(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        
        crearPantallaCarga();
    }
    
    
    private void crearPantallaCarga() {
        loadingNode = new Node("LoadingScreen");
        
        // Overlay oscuro
        crearOverlay();
        
        // Título
        crearTitulo();
        
        // Barra de progreso
        crearBarraProgreso();
        
        // Texto de progreso
        crearTextoProgreso();
        
        // Mensaje
        crearTextoMensaje();
    }
    
    
    private void crearOverlay() {
        Quad overlayQuad = new Quad(ancho, alto);
        overlay = new Geometry("LoadingOverlay", overlayQuad);
        
        Material mat = new Material(app.getAssetManager(), 
            "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.95f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        overlay.setMaterial(mat);
        overlay.setLocalTranslation(0, 0, 100);
        
        loadingNode.attachChild(overlay);
    }
    
    
    private void crearTitulo() {
        txtTitulo = new BitmapText(font);
        txtTitulo.setSize(48f);
        txtTitulo.setColor(new ColorRGBA(0.3f, 1f, 1f, 1f)); // Cyan
        txtTitulo.setText("🎵 CARGANDO...");
        
        float anchoTexto = txtTitulo.getLineWidth();
        txtTitulo.setLocalTranslation(
            ancho / 2 - anchoTexto / 2,
            alto / 2 + 150f,
            102
        );
        
        loadingNode.attachChild(txtTitulo);
    }
    
    
    private void crearBarraProgreso() {
        float anchoBarra = 600f;
        float altoBarra = 40f;
        float posX = (ancho - anchoBarra) / 2;
        float posY = alto / 2 - 50f;
        
        // Fondo de la barra
        Quad fondoQuad = new Quad(anchoBarra, altoBarra);
        barraFondo = new Geometry("BarraFondo", fondoQuad);
        
        Material matFondo = new Material(app.getAssetManager(), 
            "Common/MatDefs/Misc/Unshaded.j3md");
        matFondo.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));
        matFondo.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        barraFondo.setMaterial(matFondo);
        barraFondo.setLocalTranslation(posX, posY, 101);
        loadingNode.attachChild(barraFondo);
        
        // Barra de progreso
        Quad progresoQuad = new Quad(10f, altoBarra); // Empieza pequeña
        barraProgreso = new Geometry("BarraProgreso", progresoQuad);
        
        Material matProgreso = new Material(app.getAssetManager(), 
            "Common/MatDefs/Misc/Unshaded.j3md");
        matProgreso.setColor("Color", new ColorRGBA(0.3f, 1f, 0.3f, 1f)); // Verde
        matProgreso.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        barraProgreso.setMaterial(matProgreso);
        barraProgreso.setLocalTranslation(posX, posY, 101.5f);
        loadingNode.attachChild(barraProgreso);
    }
    
    
    private void crearTextoProgreso() {
        txtProgreso = new BitmapText(font);
        txtProgreso.setSize(24f);
        txtProgreso.setColor(ColorRGBA.White);
        txtProgreso.setText("0%");
        
        txtProgreso.setLocalTranslation(
            ancho / 2 - 20f,
            alto / 2 - 90f,
            102
        );
        
        loadingNode.attachChild(txtProgreso);
    }
    
    
    private void crearTextoMensaje() {
        txtMensaje = new BitmapText(font);
        txtMensaje.setSize(18f);
        txtMensaje.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        txtMensaje.setText("Iniciando...");
        
        float anchoTexto = txtMensaje.getLineWidth();
        txtMensaje.setLocalTranslation(
            ancho / 2 - anchoTexto / 2,
            alto / 2 - 150f,
            102
        );
        
        loadingNode.attachChild(txtMensaje);
    }
    
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Muestra la pantalla de carga
     */
    public void mostrar() {
        if (!visible) {
            guiNode.attachChild(loadingNode);
            visible = true;
            System.out.println("⏳ Pantalla de carga mostrada");
        }
    }
    
    
    /**
     * Oculta la pantalla de carga
     */
    public void ocultar() {
        if (visible) {
            loadingNode.removeFromParent();
            visible = false;
            System.out.println("✓ Pantalla de carga ocultada");
        }
    }
    
    
    /**
     * Actualiza el progreso de la barra
     * 
     * @param progreso Valor entre 0.0 y 1.0
     * @param mensaje Mensaje a mostrar (opcional, puede ser null)
     */
    public void actualizarProgreso(float progreso, String mensaje) {
        progreso = Math.max(0f, Math.min(1f, progreso)); // Clamp 0-1
        
        // Actualizar barra visual
        float anchoBarra = 600f;
        float nuevoAncho = anchoBarra * progreso;
        
        Quad nuevaBarra = new Quad(nuevoAncho, 40f);
        barraProgreso.setMesh(nuevaBarra);
        
        // Actualizar texto de porcentaje
        txtProgreso.setText(String.format("%.0f%%", progreso * 100));
        
        // Centrar texto
        float anchoTexto = txtProgreso.getLineWidth();
        txtProgreso.setLocalTranslation(
            ancho / 2 - anchoTexto / 2,
            alto / 2 - 90f,
            102
        );
        
        // Actualizar mensaje si se proporciona
        if (mensaje != null && !mensaje.isEmpty()) {
            txtMensaje.setText(mensaje);
            
            // Centrar mensaje
            float anchoMensaje = txtMensaje.getLineWidth();
            txtMensaje.setLocalTranslation(
                ancho / 2 - anchoMensaje / 2,
                alto / 2 - 150f,
                102
            );
        }
        
        // Cambiar color de la barra según progreso
        Material mat = barraProgreso.getMaterial();
        if (progreso < 0.33f) {
            mat.setColor("Color", new ColorRGBA(1f, 0.3f, 0.3f, 1f)); // Rojo
        } else if (progreso < 0.66f) {
            mat.setColor("Color", new ColorRGBA(1f, 1f, 0.3f, 1f)); // Amarillo
        } else {
            mat.setColor("Color", new ColorRGBA(0.3f, 1f, 0.3f, 1f)); // Verde
        }
    }
    
    
    /**
     * Actualiza solo el mensaje sin cambiar el progreso
     */
    public void actualizarMensaje(String mensaje) {
        if (mensaje != null && !mensaje.isEmpty()) {
            txtMensaje.setText(mensaje);
            
            float anchoMensaje = txtMensaje.getLineWidth();
            txtMensaje.setLocalTranslation(
                ancho / 2 - anchoMensaje / 2,
                alto / 2 - 150f,
                102
            );
        }
    }
    
    
    /**
     * Verifica si la pantalla está visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    
    /**
     * Limpia los recursos de la pantalla
     */
    public void limpiar() {
        ocultar();
        
        if (loadingNode != null) {
            loadingNode.detachAllChildren();
        }
        
        System.out.println("🧹 LoadingScreen limpiada");
    }
}   