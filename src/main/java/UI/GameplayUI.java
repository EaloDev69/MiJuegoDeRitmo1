/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import Modelo.Direccion;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;

/**
 * GameplayUI - VERSIÓN COMPLETAMENTE CORREGIDA
 * 
 * CORRECCIONES APLICADAS:
 * ✅ Targets creados con Quad + Unshaded.j3md
 * ✅ Transparencia activada (BlendMode.Alpha)
 * ✅ Rutas de texturas correctas
 * ✅ Anclaje explícito al GuiNode
 * ✅ Nodo raíz para toda la UI
 */
public class GameplayUI {

    private final SimpleApplication app;
    private final Node guiNode;
    private final BitmapFont font;
    private final float ancho;
    private final float alto;

    // ⭐ NUEVO: Nodo raíz de la UI para mejor control
    private Node uiRootNode;

    // Elementos de la UI
    private Geometry barraVidaFondo;
    private Geometry barraVidaActual;
    private BitmapText txtVidaPorcentaje;
    private BitmapText txtScore;
    private BitmapText txtCombo;
    private BitmapText txtCancion;
    private int vidaMaxima = 100;
    private int vidaActual = 100;

    // Targets (flechas vacías)
    private Node nodoTargets;

    // Feedback temporal
    private List<MensajeFeedback> mensajesFeedback;
    private static final float DURACION_FEEDBACK = 1.0f;

    // Botón de pausa
    private Geometry btnPausa;
    private BitmapText txtPausa;

    // Colores barra de vida
    private static final ColorRGBA COLOR_VIDA_ALTA = new ColorRGBA(0.2f, 1f, 0.3f, 1f);
    private static final ColorRGBA COLOR_VIDA_MEDIA = new ColorRGBA(1f, 0.8f, 0.2f, 1f);
    private static final ColorRGBA COLOR_VIDA_BAJA = new ColorRGBA(1f, 0.3f, 0.2f, 1f);

    public GameplayUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        this.mensajesFeedback = new ArrayList<>();

        System.out.println("\n🎨 Inicializando GameplayUI...");
        System.out.println("  Dimensiones: " + ancho + "x" + alto);

        inicializarUI();
        verificarVisibilidad();

        System.out.println("✓ GameplayUI inicializado correctamente\n");
    }

    /**
     * ⭐ NUEVO: Inicialización con nodo raíz explícito
     */
    private void inicializarUI() {
        // Crear nodo raíz para toda la UI del gameplay
        uiRootNode = new Node("GameplayUIRoot");

        // Crear todos los elementos
        crearTargetsFlechas();
        crearBarraVida();
        crearTextoScore();
        crearTextoCombo();
        crearTextoCancion();
        crearBotonPausa();

        // ⭐ CRÍTICO: Añadir el nodo raíz al GuiNode
        guiNode.attachChild(uiRootNode);
        System.out.println("  ✓ GameplayUIRoot añadido al GuiNode");
    }

    /**
     * ⭐ NUEVO: Verifica que todos los elementos estén correctamente anclados
     */
    private void verificarVisibilidad() {
        System.out.println("\n🔍 Verificando visibilidad de elementos:");
        int elementosVisibles = 0;
        int elementosTotales = 0;

        // Verificar cada elemento
        if (barraVidaFondo != null && barraVidaFondo.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Barra vida fondo: VISIBLE");
        } else {
            System.out.println("  ✗ Barra vida fondo: NO VISIBLE");
        }
        elementosTotales++;

        if (barraVidaActual != null && barraVidaActual.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Barra vida actual: VISIBLE");
        } else {
            System.out.println("  ✗ Barra vida actual: NO VISIBLE");
        }
        elementosTotales++;

        if (txtVidaPorcentaje != null && txtVidaPorcentaje.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Texto vida: VISIBLE");
        } else {
            System.out.println("  ✗ Texto vida: NO VISIBLE");
        }
        elementosTotales++;

        if (txtScore != null && txtScore.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Texto score: VISIBLE");
        } else {
            System.out.println("  ✗ Texto score: NO VISIBLE");
        }
        elementosTotales++;

        if (btnPausa != null && btnPausa.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Botón pausa: VISIBLE");
        } else {
            System.out.println("  ✗ Botón pausa: NO VISIBLE");
        }
        elementosTotales++;

        if (nodoTargets != null && nodoTargets.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Targets: VISIBLES (" + nodoTargets.getChildren().size() + " elementos)");
        } else {
            System.out.println("  ✗ Targets: NO VISIBLES");
        }
        elementosTotales++;

        System.out.println("\n  📊 Resumen: " + elementosVisibles + "/" + elementosTotales + " elementos visibles");
    }

    // ==================== TARGETS (FLECHAS VACÍAS) - CORREGIDO ====================

    /**
     * ⭐ COMPLETAMENTE REESCRITO: Crea los targets con Quad + Unshaded.j3md
     */
    private void crearTargetsFlechas() {
        System.out.println("\n  🎯 Creando targets de flechas...");

        // --- Configuración de Posición y Tamaño ---
        float tamano = 90f; // Tamaño de cada flecha (ajustado para que sean visibles)
        float espaciado = 100f; // Espacio entre flechas
        
        // Altura fija donde aparecerán los targets (parte superior de la pantalla)
        float alturaTarget = alto - 150f;
        
        // Calcular posición central
        float centroX = ancho / 2f;

        // NODO PRINCIPAL DE LOS TARGETS
        nodoTargets = new Node("TargetsFlechas");

        System.out.println("  - Altura targets: " + alturaTarget);
        System.out.println("  - Tamaño: " + tamano + "x" + tamano);
        System.out.println("  - Espaciado: " + espaciado);

        // --- Definición de la secuencia de Targets (Izquierda, Abajo, Arriba, Derecha) ---

        // 1. IZQUIERDA (LEFT)
        Vector3f posLeft = new Vector3f(centroX - espaciado * 1.5f, alturaTarget, 0.5f);
        crearSpriteTarget("LEFT", posLeft, tamano, 270f);

        // 2. ABAJO (DOWN)
        Vector3f posDown = new Vector3f(centroX - espaciado * 0.5f, alturaTarget, 0.5f);
        crearSpriteTarget("DOWN", posDown, tamano, 180f);
        
        // 3. ARRIBA (UP)
        Vector3f posUp = new Vector3f(centroX + espaciado * 0.5f, alturaTarget, 0.5f);
        crearSpriteTarget("UP", posUp, tamano, 0f);

        // 4. DERECHA (RIGHT)
        Vector3f posRight = new Vector3f(centroX + espaciado * 1.5f, alturaTarget, 0.5f);
        crearSpriteTarget("RIGHT", posRight, tamano, 90f);

        // 5. LUNA/ESPACIO (opcional, en el centro abajo)
        Vector3f posLuna = new Vector3f(centroX, 120f, 0.5f);
        crearSpriteTarget("LUNA", posLuna, tamano * 1.2f, 0f);

        // 6. ANCLAR EL NODO DE TARGETS AL NODO RAÍZ DE LA UI
        uiRootNode.attachChild(nodoTargets);
        System.out.println("  ✓ Nodo de targets añadido al UIRoot");
    }

    /**
     * ⭐ MÉTODO CRÍTICO: Crea y adjunta la Geometría (sprite) del target de la flecha
     * 
     * @param dirName Nombre de la dirección (ej: "LEFT"). Se usa para buscar la textura.
     * @param pos Posición (X, Y, Z) en el GuiNode.
     * @param tamano Tamaño del lado del Quad.
     * @param rotacion Rotación en grados (0, 90, 180, 270)
     */
    private void crearSpriteTarget(String dirName, Vector3f pos, float tamano, float rotacion) {
        // 1. DEFINIR RUTA DE LA TEXTURA
        String rutaTextura;
        
        switch (dirName) {
            case "LEFT":
                rutaTextura = "Textures/flecha_vacia.png"; // Flecha base, se rotará
                break;
            case "DOWN":
                rutaTextura = "Textures/flecha_vacia.png";
                break;
            case "UP":
                rutaTextura = "Textures/flecha_vacia.png";
                break;
            case "RIGHT":
                rutaTextura = "Textures/flecha_vacia.png";
                break;
            case "LUNA":
                rutaTextura = "Textures/flecha_especial_luna_vacia.png";
                break;
            default:
                rutaTextura = "Textures/flecha_vacia.png";
        }

        try {
            // 2. CARGAR TEXTURA Y CREAR MATERIAL
            Texture texture = app.getAssetManager().loadTexture(rutaTextura);
            Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setTexture("ColorMap", texture);
            
            // ⭐ CRÍTICO para PNGs con transparencia
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            
            // Color semi-transparente para los targets
            mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.7f));

            // 3. CREAR GEOMETRÍA (El sprite)
            Geometry target = new Geometry("Target-" + dirName, new Quad(tamano, tamano));
            target.setMaterial(mat);
            
            // Posicionar (centrado en la posición)
            target.setLocalTranslation(pos.x - tamano/2, pos.y - tamano/2, pos.z);
            
            // Rotar si es necesario
            if (rotacion != 0) {
                target.rotate(0, 0, rotacion * com.jme3.math.FastMath.DEG_TO_RAD);
            }
            
            // 4. ANCLAR AL NODO DE TARGETS
            nodoTargets.attachChild(target);
            
            System.out.println("    ✓ Target " + dirName + " cargado (rotación: " + rotacion + "°)");
            
        } catch (Exception e) {
            System.err.println("    ❌ ERROR al cargar target " + dirName);
            System.err.println("       Ruta intentada: " + rutaTextura);
            System.err.println("       Error: " + e.getMessage());
            
            // ⭐ FALLBACK: Crear un cuadrado de color si falla la textura
            crearTargetFallback(dirName, pos, tamano, rotacion);
        }
    }

    /**
     * ⭐ FALLBACK: Crea un target de color si no se encuentra la textura
     */
    private void crearTargetFallback(String dirName, Vector3f pos, float tamano, float rotacion) {
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        
        // Color según dirección
        ColorRGBA color;
        switch (dirName) {
            case "LEFT":
                color = new ColorRGBA(1f, 0f, 0f, 0.5f); // Rojo
                break;
            case "DOWN":
                color = new ColorRGBA(0f, 0f, 1f, 0.5f); // Azul
                break;
            case "UP":
                color = new ColorRGBA(0f, 1f, 0f, 0.5f); // Verde
                break;
            case "RIGHT":
                color = new ColorRGBA(1f, 1f, 0f, 0.5f); // Amarillo
                break;
            case "LUNA":
                color = new ColorRGBA(1f, 1f, 1f, 0.5f); // Blanco
                break;
            default:
                color = new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f);
        }
        
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry target = new Geometry("Target-" + dirName + "-Fallback", new Quad(tamano, tamano));
        target.setMaterial(mat);
        target.setLocalTranslation(pos.x - tamano/2, pos.y - tamano/2, pos.z);
        
        if (rotacion != 0) {
            target.rotate(0, 0, rotacion * com.jme3.math.FastMath.DEG_TO_RAD);
        }
        
        nodoTargets.attachChild(target);
        System.out.println("    ⚠ Target " + dirName + " creado como FALLBACK (cuadrado de color)");
    }

    // ==================== BARRA DE VIDA ====================

    private void crearBarraVida() {
        float anchoBarraMax = 500f;
        float altoBarraMax = 35f;
        float posX = (ancho - anchoBarraMax) / 2;
        float posY = 60f;

        System.out.println("  💚 Creando barra de vida en: (" + posX + ", " + posY + ")");

        // Fondo
        Quad fondoQuad = new Quad(anchoBarraMax, altoBarraMax);
        barraVidaFondo = new Geometry("BarraVidaFondo", fondoQuad);
        Material matFondo = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matFondo.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 0.9f));
        matFondo.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaFondo.setMaterial(matFondo);
        barraVidaFondo.setLocalTranslation(posX, posY, 0);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(barraVidaFondo);

        // Barra actual
        Quad vidaQuad = new Quad(anchoBarraMax, altoBarraMax);
        barraVidaActual = new Geometry("BarraVidaActual", vidaQuad);
        Material matVida = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matVida.setColor("Color", COLOR_VIDA_ALTA);
        matVida.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaActual.setMaterial(matVida);
        barraVidaActual.setLocalTranslation(posX, posY, 0.1f);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(barraVidaActual);

        // Texto porcentaje
        txtVidaPorcentaje = new BitmapText(font);
        txtVidaPorcentaje.setSize(22f);
        txtVidaPorcentaje.setColor(ColorRGBA.White);
        txtVidaPorcentaje.setText("100%");
        txtVidaPorcentaje.setLocalTranslation(posX + anchoBarraMax / 2 - 30f, posY + 23f, 0.2f);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(txtVidaPorcentaje);

        System.out.println("    ✓ Barra de vida creada y añadida al GuiNode");
    }

    public void actualizarVida(int nuevaVida) {
        vidaActual = Math.max(0, Math.min(vidaMaxima, nuevaVida));
        float porcentaje = (float) vidaActual / vidaMaxima;
        float anchoBarraMax = 500f;
        float nuevoAncho = anchoBarraMax * porcentaje;

        // Actualizar ancho
        Quad vidaQuad = new Quad(nuevoAncho, 35f);
        barraVidaActual.setMesh(vidaQuad);

        // Actualizar color
        Material mat = barraVidaActual.getMaterial();
        if (porcentaje > 0.6f) {
            mat.setColor("Color", COLOR_VIDA_ALTA);
        } else if (porcentaje > 0.3f) {
            mat.setColor("Color", COLOR_VIDA_MEDIA);
        } else {
            mat.setColor("Color", COLOR_VIDA_BAJA);
        }

        // Actualizar texto
        txtVidaPorcentaje.setText(String.format("%d%%", (int)(porcentaje * 100)));
        txtVidaPorcentaje.setColor(porcentaje <= 0.3f ? ColorRGBA.Red : ColorRGBA.White);
    }

    // ==================== SCORE ====================

    private void crearTextoScore() {
        txtScore = new BitmapText(font);
        txtScore.setSize(28f);
        txtScore.setColor(ColorRGBA.White);
        txtScore.setText("Score: 0");
        txtScore.setLocalTranslation(30f, alto - 30f, 0);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(txtScore);
        System.out.println("  ✓ Texto score añadido al GuiNode");
    }

    public void actualizarScore(int score) {
        if (txtScore != null) {
            txtScore.setText("Score: " + score);
        }
    }

    // ==================== COMBO ====================

    private void crearTextoCombo() {
        txtCombo = new BitmapText(font);
        txtCombo.setSize(36f);
        txtCombo.setColor(ColorRGBA.Cyan);
        txtCombo.setText("");
        txtCombo.setLocalTranslation(ancho / 2 - 80f, 130f, 0);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(txtCombo);
        System.out.println("  ✓ Texto combo añadido al GuiNode");
    }

    public void actualizarCombo(int combo) {
        if (txtCombo == null) return;

        if (combo > 1) {
            txtCombo.setText("COMBO x" + combo);
            if (combo >= 20) {
                txtCombo.setColor(new ColorRGBA(1f, 0f, 1f, 1f));
                txtCombo.setSize(44f);
            } else if (combo >= 10) {
                txtCombo.setColor(new ColorRGBA(1f, 0.5f, 0f, 1f));
                txtCombo.setSize(40f);
            } else {
                txtCombo.setColor(ColorRGBA.Cyan);
                txtCombo.setSize(36f);
            }
        } else {
            txtCombo.setText("");
        }
    }

    // ==================== CANCIÓN ====================

    private void crearTextoCancion() {
        txtCancion = new BitmapText(font);
        txtCancion.setSize(18f);
        txtCancion.setColor(new ColorRGBA(0.8f, 0.8f, 1f, 1f));
        txtCancion.setText("♪ Cargando...");
        txtCancion.setLocalTranslation(ancho - 350f, alto - 30f, 0);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(txtCancion);
        System.out.println("  ✓ Texto canción añadido al GuiNode");
    }

    public void mostrarCancion(String nombreCancion) {
        if (txtCancion == null) return;

        String nombre = nombreCancion;
        if (nombreCancion.contains("/")) {
            String[] partes = nombreCancion.split("/");
            nombre = partes[partes.length - 1];
        }

        if (nombre.endsWith(".wav")) {
            nombre = nombre.substring(0, nombre.length() - 4);
        }

        txtCancion.setText("♪ " + nombre);
    }

    // ==================== BOTÓN DE PAUSA ====================

    private void crearBotonPausa() {
        float tamano = 60f;
        float posX = ancho - tamano - 20f;
        float posY = alto - tamano - 80f;

        System.out.println("  ⏸ Creando botón de pausa en: (" + posX + ", " + posY + ")");

        // Fondo del botón
        Quad quad = new Quad(tamano, tamano);
        btnPausa = new Geometry("BotonPausa", quad);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        btnPausa.setMaterial(mat);
        btnPausa.setLocalTranslation(posX, posY, 10);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(btnPausa);

        // Símbolo de pausa
        txtPausa = new BitmapText(font);
        txtPausa.setSize(40f);
        txtPausa.setColor(ColorRGBA.White);
        txtPausa.setText("||");
        txtPausa.setLocalTranslation(posX + 15f, posY + 42f, 11);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(txtPausa);

        System.out.println("  ✓ Botón de pausa creado y añadido al GuiNode");
    }

    // ==================== FEEDBACK TEMPORAL ====================

    public void mostrarFeedback(String mensaje, ColorRGBA color) {
        BitmapText txtFeedback = new BitmapText(font);
        txtFeedback.setSize(40f);
        txtFeedback.setColor(color);
        txtFeedback.setText(mensaje);

        float anchoTexto = txtFeedback.getLineWidth();
        txtFeedback.setLocalTranslation(ancho / 2 - anchoTexto / 2, alto / 2 + 150f, 1f);

        // ⭐ CRÍTICO: Añadir al GuiNode
        guiNode.attachChild(txtFeedback);

        mensajesFeedback.add(new MensajeFeedback(txtFeedback, 0f));
    }

    public void actualizarFeedback(float tpf) {
        List<MensajeFeedback> mensajesAEliminar = new ArrayList<>();

        for (MensajeFeedback msg : mensajesFeedback) {
            msg.tiempo += tpf;
            float alpha = 1f - (msg.tiempo / DURACION_FEEDBACK);

            if (alpha > 0) {
                ColorRGBA colorActual = msg.texto.getColor().clone();
                colorActual.a = alpha;
                msg.texto.setColor(colorActual);

                Vector3f pos = msg.texto.getLocalTranslation();
                msg.texto.setLocalTranslation(pos.x, pos.y + 60f * tpf, pos.z);
            } else {
                msg.texto.removeFromParent();
                mensajesAEliminar.add(msg);
            }
        }

        mensajesFeedback.removeAll(mensajesAEliminar);
    }

    // ==================== OCULTAR/MOSTRAR ====================

    public void ocultarTemporalmente() {
        System.out.println("🔴 Ocultando UI del gameplay");
        if (barraVidaFondo != null) barraVidaFondo.removeFromParent();
        if (barraVidaActual != null) barraVidaActual.removeFromParent();
        if (txtVidaPorcentaje != null) txtVidaPorcentaje.removeFromParent();
        if (txtScore != null) txtScore.removeFromParent();
        if (txtCombo != null) txtCombo.removeFromParent();
        if (txtCancion != null) txtCancion.removeFromParent();
        if (nodoTargets != null) nodoTargets.removeFromParent();
        if (btnPausa != null) btnPausa.removeFromParent();
        if (txtPausa != null) txtPausa.removeFromParent();
        if (uiRootNode != null) uiRootNode.removeFromParent();
    }

    public void mostrarNuevamente() {
        System.out.println("🟢 Mostrando UI del gameplay");
        if (uiRootNode != null && uiRootNode.getParent() == null) {
            guiNode.attachChild(uiRootNode);
        }
        if (barraVidaFondo != null && barraVidaFondo.getParent() == null) {
            guiNode.attachChild(barraVidaFondo);
        }
        if (barraVidaActual != null && barraVidaActual.getParent() == null) {
            guiNode.attachChild(barraVidaActual);
        }
        if (txtVidaPorcentaje != null && txtVidaPorcentaje.getParent() == null) {
            guiNode.attachChild(txtVidaPorcentaje);
        }
        if (txtScore != null && txtScore.getParent() == null) {
            guiNode.attachChild(txtScore);
        }
        if (txtCombo != null && txtCombo.getParent() == null) {
            guiNode.attachChild(txtCombo);
        }
        if (txtCancion != null && txtCancion.getParent() == null) {
            guiNode.attachChild(txtCancion);
        }
        if (nodoTargets != null && nodoTargets.getParent() == null) {
            guiNode.attachChild(nodoTargets);
        }
        if (btnPausa != null && btnPausa.getParent() == null) {
            guiNode.attachChild(btnPausa);
        }
        if (txtPausa != null && txtPausa.getParent() == null) {
            guiNode.attachChild(txtPausa);
        }
    }

    // ==================== LIMPIEZA ====================

    public void limpiar() {
        System.out.println("🧹 Limpiando GameplayUI...");

        ocultarTemporalmente();

        for (MensajeFeedback msg : mensajesFeedback) {
            if (msg.texto.getParent() != null) {
                msg.texto.removeFromParent();
            }
        }
        mensajesFeedback.clear();

        if (uiRootNode != null) {
            uiRootNode.detachAllChildren();
            uiRootNode = null;
        }

        System.out.println("✓ GameplayUI limpiado");
    }

    // ==================== CLASE INTERNA ====================

    private static class MensajeFeedback {
        BitmapText texto;
        float tiempo;

        MensajeFeedback(BitmapText texto, float tiempo) {
            this.texto = texto;
            this.tiempo = tiempo;
        }
    }
}