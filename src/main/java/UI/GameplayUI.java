/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author CamiLaNekoUwU_Gamer
 */

/**
 * HUD del gameplay - Maneja toda la interfaz visual durante el juego
 * 
 * Incluye:
 * - Barra de vida con porcentaje
 * - Score y combo
 * - Nombre de la canción
 * - Mensajes de feedback temporales
 * - Indicadores de dirección
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class GameplayUI {

    
    private final Application app;
    private final Node guiNode;
    private final BitmapFont font;
    
    // Dimensiones de pantalla
    private final float ancho;
    private final float alto;
    
    // === BARRA DE VIDA ===
    private Geometry barraVidaFondo;
    private Geometry barraVidaActual;
    private BitmapText txtVidaPorcentaje;
    private int vidaMaxima = 100;
    private int vidaActual = 100;
    
    // Colores de la barra según el nivel de vida
    private static final ColorRGBA COLOR_VIDA_ALTA = new ColorRGBA(0.2f, 1f, 0.3f, 1f); // Verde
    private static final ColorRGBA COLOR_VIDA_MEDIA = new ColorRGBA(1f, 0.8f, 0.2f, 1f); // Amarillo
    private static final ColorRGBA COLOR_VIDA_BAJA = new ColorRGBA(1f, 0.3f, 0.2f, 1f); // Rojo
    
    // === TEXTOS DE INFORMACIÓN ===
    private BitmapText txtScore;
    private BitmapText txtCombo;
    private BitmapText txtCancion;
    
    // === MENSAJES DE FEEDBACK ===
    private List<MensajeFeedback> mensajesFeedback;
    private static final float DURACION_FEEDBACK = 1.0f;
    
    // === INDICADORES DE DIRECCIÓN ===
    private Node nodoDirecciones;
    
    /**
     * Constructor del HUD
     */
    public GameplayUI(Application app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        this.mensajesFeedback = new ArrayList<>();
        
        inicializarUI();
        
        System.out.println("✓ GameplayUI inicializado");
    }
    
    /**
     * Inicializa todos los elementos del HUD
     */
    private void inicializarUI() {
        crearBarraVida();
        crearTextoScore();
        crearTextoCombo();
        crearTextoCancion();
        crearIndicadoresDireccion();
    }
    
    // ==================== BARRA DE VIDA ====================
    
    /**
     * Crea la barra de vida con fondo y barra actual
     */
    private void crearBarraVida() {
        float anchoBarraMax = 400f;
        float altoBarraMax = 30f;
        float posX = 20f;
        float posY = alto - 50f;
        
        // === FONDO DE LA BARRA (gris oscuro) ===
        Quad fondoQuad = new Quad(anchoBarraMax, altoBarraMax);
        barraVidaFondo = new Geometry("BarraVidaFondo", fondoQuad);
        
        Material matFondo = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matFondo.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f));
        matFondo.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaFondo.setMaterial(matFondo);
        barraVidaFondo.setLocalTranslation(posX, posY, 0);
        
        guiNode.attachChild(barraVidaFondo);
        
        // === BARRA DE VIDA ACTUAL (verde) ===
        Quad vidaQuad = new Quad(anchoBarraMax, altoBarraMax);
        barraVidaActual = new Geometry("BarraVidaActual", vidaQuad);
        
        Material matVida = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matVida.setColor("Color", COLOR_VIDA_ALTA);
        matVida.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaActual.setMaterial(matVida);
        barraVidaActual.setLocalTranslation(posX, posY, 0.1f); // Z ligeramente adelante
        
        guiNode.attachChild(barraVidaActual);
        
        // === TEXTO DE PORCENTAJE ===
        txtVidaPorcentaje = new BitmapText(font);
        txtVidaPorcentaje.setSize(20f);
        txtVidaPorcentaje.setColor(ColorRGBA.White);
        txtVidaPorcentaje.setText("100%");
        txtVidaPorcentaje.setLocalTranslation(posX + anchoBarraMax + 15f, posY + 22f, 0.2f);
        
        guiNode.attachChild(txtVidaPorcentaje);
        
        // === ETIQUETA "VIDA" ===
        BitmapText lblVida = new BitmapText(font);
        lblVida.setSize(16f);
        lblVida.setColor(ColorRGBA.Cyan);
        lblVida.setText("VIDA:");
        lblVida.setLocalTranslation(posX, posY + altoBarraMax + 15f, 0);
        
        guiNode.attachChild(lblVida);
    }
    
    /**
     * Actualiza la barra de vida
     */
    public void actualizarVida(int nuevaVida) {
        vidaActual = Math.max(0, Math.min(vidaMaxima, nuevaVida));
        
        float porcentaje = (float) vidaActual / vidaMaxima;
        float anchoBarraMax = 400f;
        float nuevoAncho = anchoBarraMax * porcentaje;
        
        // Actualizar el ancho de la barra
        Quad vidaQuad = new Quad(nuevoAncho, 30f);
        barraVidaActual.setMesh(vidaQuad);
        
        // Actualizar color según el nivel de vida
        Material mat = barraVidaActual.getMaterial();
        if (porcentaje > 0.6f) {
            mat.setColor("Color", COLOR_VIDA_ALTA); // Verde
        } else if (porcentaje > 0.3f) {
            mat.setColor("Color", COLOR_VIDA_MEDIA); // Amarillo
        } else {
            mat.setColor("Color", COLOR_VIDA_BAJA); // Rojo
        }
        
        // Actualizar texto de porcentaje
        txtVidaPorcentaje.setText(String.format("%d%%", (int)(porcentaje * 100)));
        
        // Cambiar color del texto si está en peligro
        if (porcentaje <= 0.3f) {
            txtVidaPorcentaje.setColor(ColorRGBA.Red);
        } else {
            txtVidaPorcentaje.setColor(ColorRGBA.White);
        }
        
        System.out.println("Vida actualizada: " + vidaActual + "/" + vidaMaxima + " (" + (int)(porcentaje * 100) + "%)");
    }
    
    // ==================== SCORE ====================
    
    /**
     * Crea el texto del score
     */
    private void crearTextoScore() {
        txtScore = new BitmapText(font);
        txtScore.setSize(28f);
        txtScore.setColor(ColorRGBA.Yellow);
        txtScore.setText("SCORE: 0");
        txtScore.setLocalTranslation(20f, alto - 100f, 0);
        
        guiNode.attachChild(txtScore);
    }
    
    /**
     * Actualiza el score
     */
    public void actualizarScore(int score) {
        txtScore.setText("SCORE: " + score);
    }
    
    // ==================== COMBO ====================
    
    /**
     * Crea el texto del combo
     */
    private void crearTextoCombo() {
        txtCombo = new BitmapText(font);
        txtCombo.setSize(32f);
        txtCombo.setColor(ColorRGBA.Orange);
        txtCombo.setText("");
        // Centrado en la parte superior
        txtCombo.setLocalTranslation(ancho / 2 - 50f, alto - 30f, 0);
        
        guiNode.attachChild(txtCombo);
    }
    
    /**
     * Actualiza el combo
     */
    public void actualizarCombo(int combo) {
        if (combo > 1) {
            txtCombo.setText("COMBO x" + combo);
            
            // Cambiar color según el combo
            if (combo >= 20) {
                txtCombo.setColor(new ColorRGBA(1f, 0f, 1f, 1f)); // Magenta
                txtCombo.setSize(40f);
            } else if (combo >= 10) {
                txtCombo.setColor(new ColorRGBA(1f, 0.5f, 0f, 1f)); // Naranja brillante
                txtCombo.setSize(36f);
            } else {
                txtCombo.setColor(ColorRGBA.Orange);
                txtCombo.setSize(32f);
            }
        } else {
            txtCombo.setText("");
        }
    }
    
    // ==================== CANCIÓN ====================
    
    /**
     * Crea el texto de la canción actual
     */
    private void crearTextoCancion() {
        txtCancion = new BitmapText(font);
        txtCancion.setSize(18f);
        txtCancion.setColor(ColorRGBA.Cyan);
        txtCancion.setText("♪ Cargando...");
        // Esquina superior derecha
        txtCancion.setLocalTranslation(ancho - 350f, alto - 30f, 0);
        
        guiNode.attachChild(txtCancion);
    }
    
    /**
     * Muestra el nombre de la canción
     */
    public void mostrarCancion(String nombreCancion) {
        // Extraer solo el nombre del archivo
        String nombre = nombreCancion;
        if (nombreCancion.contains("/")) {
            String[] partes = nombreCancion.split("/");
            nombre = partes[partes.length - 1];
        }
        
        // Remover extensión
        if (nombre.endsWith(".wav")) {
            nombre = nombre.substring(0, nombre.length() - 4);
        }
        
        txtCancion.setText("♪ " + nombre);
    }
    
    // ==================== FEEDBACK ====================
    
    /**
     * Muestra un mensaje de feedback temporal
     */
    public void mostrarFeedback(String mensaje, ColorRGBA color) {
        BitmapText txtFeedback = new BitmapText(font);
        txtFeedback.setSize(36f);
        txtFeedback.setColor(color);
        txtFeedback.setText(mensaje);
        
        // Posición centrada
        float anchoTexto = txtFeedback.getLineWidth();
        txtFeedback.setLocalTranslation(ancho / 2 - anchoTexto / 2, alto / 2 + 100f, 1f);
        
        guiNode.attachChild(txtFeedback);
        
        // Agregar a la lista de mensajes activos
        mensajesFeedback.add(new MensajeFeedback(txtFeedback, 0f));
        
        System.out.println("Feedback: " + mensaje);
    }
    
    /**
     * Actualiza los mensajes de feedback (llamar en update)
     */
    public void actualizarFeedback(float tpf) {
        List<MensajeFeedback> mensajesAEliminar = new ArrayList<>();
        
        for (MensajeFeedback msg : mensajesFeedback) {
            msg.tiempo += tpf;
            
            // Fade out
            float alpha = 1f - (msg.tiempo / DURACION_FEEDBACK);
            if (alpha > 0) {
                ColorRGBA colorActual = msg.texto.getColor().clone();
                colorActual.a = alpha;
                msg.texto.setColor(colorActual);
                
                // Mover hacia arriba
                Vector3f pos = msg.texto.getLocalTranslation();
                msg.texto.setLocalTranslation(pos.x, pos.y + 50f * tpf, pos.z);
            } else {
                // Eliminar
                msg.texto.removeFromParent();
                mensajesAEliminar.add(msg);
            }
        }
        
        mensajesFeedback.removeAll(mensajesAEliminar);
    }
    
    // ==================== INDICADORES DE DIRECCIÓN ====================
    
    /**
     * Crea los indicadores de las 4 direcciones en el centro
     */
    private void crearIndicadoresDireccion() {
        nodoDirecciones = new Node("IndicadoresDireccion");
        
        float centroX = ancho / 2;
        float centroY = alto / 2;
        float distancia = 100f; // Distancia desde el centro
        float tamano = 40f;
        
        // ARRIBA
        crearIndicador(centroX - tamano/2, centroY + distancia, tamano, ColorRGBA.Red);
        
        // ABAJO
        crearIndicador(centroX - tamano/2, centroY - distancia - tamano, tamano, ColorRGBA.Blue);
        
        // IZQUIERDA
        crearIndicador(centroX - distancia - tamano, centroY - tamano/2, tamano, ColorRGBA.Green);
        
        // DERECHA
        crearIndicador(centroX + distancia, centroY - tamano/2, tamano, ColorRGBA.Yellow);
        
        guiNode.attachChild(nodoDirecciones);
    }
    
    /**
     * Crea un indicador individual
     */
    private void crearIndicador(float x, float y, float tamano, ColorRGBA color) {
        Quad quad = new Quad(tamano, tamano);
        Geometry geom = new Geometry("Indicador", quad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(color.r, color.g, color.b, 0.3f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        geom.setMaterial(mat);
        geom.setLocalTranslation(x, y, -0.5f);
        
        nodoDirecciones.attachChild(geom);
    }
    
    // ==================== LIMPIEZA ====================
    
    /**
     * Limpia toda la UI del HUD
     */
    public void limpiar() {
        if (barraVidaFondo != null) barraVidaFondo.removeFromParent();
        if (barraVidaActual != null) barraVidaActual.removeFromParent();
        if (txtVidaPorcentaje != null) txtVidaPorcentaje.removeFromParent();
        if (txtScore != null) txtScore.removeFromParent();
        if (txtCombo != null) txtCombo.removeFromParent();
        if (txtCancion != null) txtCancion.removeFromParent();
        if (nodoDirecciones != null) nodoDirecciones.removeFromParent();
        
        for (MensajeFeedback msg : mensajesFeedback) {
            msg.texto.removeFromParent();
        }
        mensajesFeedback.clear();
        
        System.out.println("✓ GameplayUI limpiado");
    }
    
    // ==================== CLASE INTERNA ====================
    
    /**
     * Clase auxiliar para manejar mensajes de feedback temporales
     */
    private static class MensajeFeedback {
        BitmapText texto;
        float tiempo;
        
        MensajeFeedback(BitmapText texto, float tiempo) {
            this.texto = texto;
            this.tiempo = tiempo;
        }
    }
} 
}
