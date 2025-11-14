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
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;

/**
 * GameplayUI - SISTEMA CON OBJETO CENTRAL BRILLANTE
 * 
 * ✅ Objeto central con 4 indicadores en sus lados
 * ✅ Sistema de brillo para mecánica ESPACIO
 * ✅ Efectos visuales mejorados
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class GameplayUI {

    private final SimpleApplication app;
    private final Node guiNode;
    private final BitmapFont font;
    private final float ancho;
    private final float alto;
    
    private Node uiRootNode;
    
    // ==================== OBJETO CENTRAL ====================
    private Geometry objetoCentral;
    private Material materialObjetoCentral;
    private float tiempoBrilloEspacio = 0f;
    private boolean brillandoEspacio = false;
    private ColorRGBA colorBaseObjetoCentral;
    
    // Efecto de pulso constante
    private float tiempoPulso = 0f;
    private static final float VELOCIDAD_PULSO = 2.0f;
    
    // ==================== INDICADORES EN LOS LADOS ====================
    private Node nodoIndicadoresDireccionales;
    
    // ==================== UI TRADICIONAL ====================
    private Geometry barraVidaFondo;
    private Geometry barraVidaActual;
    private BitmapText txtVidaPorcentaje;
    private BitmapText txtScore;
    private BitmapText txtCombo;
    private BitmapText txtCancion;
    
    private int vidaMaxima = 100;
    private int vidaActual = 100;
    
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
        
        System.out.println("\n🎨 Inicializando GameplayUI con objeto central...");
        System.out.println("  Dimensiones: " + ancho + "x" + alto);
        
        inicializarUI();
        verificarVisibilidad();
        
        System.out.println("✓ GameplayUI inicializado correctamente\n");
    }

    private void inicializarUI() {
        uiRootNode = new Node("GameplayUIRoot");
        
        crearObjetoCentral();
        crearIndicadoresDireccionales();
        crearBarraVida();
        crearTextoScore();
        crearTextoCombo();
        crearTextoCancion();
        crearBotonPausa();
        
        guiNode.attachChild(uiRootNode);
        System.out.println("  ✓ GameplayUIRoot añadido al GuiNode");
    }

    // ==================== OBJETO CENTRAL ====================
    
    private void crearObjetoCentral() {
        System.out.println("\n  ⭐ Creando objeto central...");
        
        float tamano = 150f;
        float centroX = ancho / 2;
        float centroY = alto / 2;
        
        String rutaTextura = "assets/Texture/Protagonista/astronautaPoseDefault.png";
        
        try {
            System.out.println("  → Cargando textura: " + rutaTextura);
            
            Texture texture = app.getAssetManager().loadTexture(rutaTextura);
            
            materialObjetoCentral = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
            materialObjetoCentral.setTexture("ColorMap", texture);
            materialObjetoCentral.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            
            colorBaseObjetoCentral = new ColorRGBA(1.1f, 1.1f, 1.1f, 1.0f);
            materialObjetoCentral.setColor("Color", colorBaseObjetoCentral);
            
            objetoCentral = new Geometry("ObjetoCentral", new Quad(tamano, tamano));
            objetoCentral.setMaterial(materialObjetoCentral);
            objetoCentral.setLocalTranslation(centroX - tamano/2, centroY - tamano/2, 5);
            
            uiRootNode.attachChild(objetoCentral);
            
            System.out.println("  ✓ Objeto central creado exitosamente");
            
        } catch (Exception e) {
            System.err.println("  ❌ Error cargando textura: " + e.getMessage());
            System.err.println("  ⚠ Creando objeto central de respaldo...");
            crearObjetoCentralFallback(centroX, centroY, tamano);
        }
    }

    private void crearObjetoCentralFallback(float centroX, float centroY, float tamano) {
        materialObjetoCentral = new Material(app.getAssetManager(),
            "Common/MatDefs/Misc/Unshaded.j3md");
        
        colorBaseObjetoCentral = new ColorRGBA(1.2f, 1.2f, 1.4f, 0.9f);
        materialObjetoCentral.setColor("Color", colorBaseObjetoCentral);
        materialObjetoCentral.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        objetoCentral = new Geometry("ObjetoCentral-Fallback", new Quad(tamano, tamano));
        objetoCentral.setMaterial(materialObjetoCentral);
        objetoCentral.setLocalTranslation(centroX - tamano/2, centroY - tamano/2, 5);
        
        uiRootNode.attachChild(objetoCentral);
        
        System.out.println("  ✓ Objeto central de respaldo creado (cuadrado brillante)");
    }

    // ==================== SISTEMA DE BRILLO ====================
    
    public void activarBrilloEspacio() {
        brillandoEspacio = true;
        tiempoBrilloEspacio = 0f;
        
        if (materialObjetoCentral != null) {
            materialObjetoCentral.setColor("Color", new ColorRGBA(3.0f, 3.0f, 4.0f, 1.0f));
        }
        
        System.out.println("✨ Objeto central BRILLANDO - Mecánica ESPACIO activada");
    }

    public void actualizarBrilloObjetoCentral(float tpf) {
        if (objetoCentral == null || materialObjetoCentral == null) return;
        
        if (brillandoEspacio) {
            tiempoBrilloEspacio += tpf;
            float duracionBrillo = 0.4f;
            
            if (tiempoBrilloEspacio < duracionBrillo) {
                float progreso = tiempoBrilloEspacio / duracionBrillo;
                float intensidad = 1.0f + (3.0f * (1.0f - progreso));
                
                float r = intensidad;
                float g = intensidad;
                float b = intensidad + (3.0f * (1.0f - progreso));
                
                materialObjetoCentral.setColor("Color", new ColorRGBA(r, g, b, 1.0f));
            } else {
                brillandoEspacio = false;
                materialObjetoCentral.setColor("Color", colorBaseObjetoCentral);
            }
        } else {
            tiempoPulso += tpf * VELOCIDAD_PULSO;
            float intensidadPulso = 1.0f + 0.15f * FastMath.sin(tiempoPulso);
            ColorRGBA colorPulso = colorBaseObjetoCentral.mult(intensidadPulso);
            materialObjetoCentral.setColor("Color", colorPulso);
        }
    }

    public void activarBrilloHit(ColorRGBA colorFlecha) {
        if (materialObjetoCentral == null) return;
        
        ColorRGBA colorMezclado = colorBaseObjetoCentral.add(colorFlecha).mult(0.7f);
        colorMezclado.a = 1.0f;
        
        materialObjetoCentral.setColor("Color", colorMezclado);
        
        System.out.println("💫 Hit registrado - Brillo de color");
    }

    // ==================== INDICADORES DIRECCIONALES EN LOS LADOS ====================
    
    private void crearIndicadoresDireccionales() {
        System.out.println("\n  📍 Creando indicadores direccionales en los lados del objeto central...");
        
        nodoIndicadoresDireccionales = new Node("IndicadoresDireccionales");
        
        float centroX = ancho / 2f;
        float centroY = alto / 2f;
        
        // Tamaño del objeto central y los indicadores
        float tamanoObjetoCentral = 150f;
        float radioObjeto = tamanoObjetoCentral / 2f; // 75px
        float tamanoIndicador = 40f;
        
        // ⭐ IZQUIERDA - En el lado izquierdo del cuadrado
        crearIndicadorEnLado("←", 
            centroX - radioObjeto, 
            centroY, 
            tamanoIndicador, 
            new ColorRGBA(1f, 0.3f, 0.3f, 0.6f));
        
        // ⭐ DERECHA - En el lado derecho del cuadrado
        crearIndicadorEnLado("→", 
            centroX + radioObjeto, 
            centroY, 
            tamanoIndicador, 
            new ColorRGBA(1f, 0.3f, 0.3f, 0.6f));
        
        // ⭐ ARRIBA - En el lado superior del cuadrado
        crearIndicadorEnLado("↑", 
            centroX, 
            centroY + radioObjeto, 
            tamanoIndicador, 
            new ColorRGBA(0.3f, 0.3f, 1f, 0.6f));
        
        // ⭐ ABAJO - En el lado inferior del cuadrado
        crearIndicadorEnLado("↓", 
            centroX, 
            centroY - radioObjeto, 
            tamanoIndicador, 
            new ColorRGBA(0.3f, 0.3f, 1f, 0.6f));
        
        uiRootNode.attachChild(nodoIndicadoresDireccionales);
        System.out.println("  ✓ Indicadores direccionales creados en los 4 lados");
    }

    private void crearIndicadorEnLado(String simbolo, float x, float y, 
                                      float tamano, ColorRGBA color) {
        // Crear cuadro del indicador
        Quad quad = new Quad(tamano, tamano);
        Geometry indicador = new Geometry("Indicador-" + simbolo, quad);
        
        Material mat = new Material(app.getAssetManager(), 
            "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        indicador.setMaterial(mat);
        // Centrar el indicador en las coordenadas dadas
        indicador.setLocalTranslation(x - tamano/2, y - tamano/2, 0.2f);
        
        nodoIndicadoresDireccionales.attachChild(indicador);
        
        // Añadir símbolo de texto
        BitmapText txtSimbolo = new BitmapText(font);
        txtSimbolo.setSize(24f);
        txtSimbolo.setColor(ColorRGBA.White);
        txtSimbolo.setText(simbolo);
        
        // Centrar el texto dentro del cuadro
        float anchoTexto = txtSimbolo.getLineWidth();
        txtSimbolo.setLocalTranslation(
            x - anchoTexto/2, 
            y + 8f,  // Ajuste vertical para centrar
            0.3f
        );
        
        nodoIndicadoresDireccionales.attachChild(txtSimbolo);
    }

    // ==================== VERIFICACIÓN ====================
    
    private void verificarVisibilidad() {
        System.out.println("\n🔍 Verificando visibilidad de elementos:");
        
        int elementosVisibles = 0;
        int elementosTotales = 0;
        
        if (objetoCentral != null && objetoCentral.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Objeto central: VISIBLE");
        } else {
            System.out.println("  ✗ Objeto central: NO VISIBLE");
        }
        elementosTotales++;
        
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
        
        if (txtScore != null && txtScore.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Texto score: VISIBLE");
        } else {
            System.out.println("  ✗ Texto score: NO VISIBLE");
        }
        elementosTotales++;
        
        if (nodoIndicadoresDireccionales != null && nodoIndicadoresDireccionales.getParent() != null) {
            elementosVisibles++;
            System.out.println("  ✓ Indicadores: VISIBLES (" + 
                nodoIndicadoresDireccionales.getChildren().size() + " elementos)");
        }
        elementosTotales++;
        
        System.out.println("\n  📊 Resumen: " + elementosVisibles + "/" + 
            elementosTotales + " elementos visibles");
    }

    // ==================== BARRA DE VIDA ====================
    
    private void crearBarraVida() {
        float anchoBarraMax = 500f;
        float altoBarraMax = 35f;
        float posX = (ancho - anchoBarraMax) / 2;
        float posY = 60f;
        
        System.out.println("  💚 Creando barra de vida en: (" + posX + ", " + posY + ")");
        
        Quad fondoQuad = new Quad(anchoBarraMax, altoBarraMax);
        barraVidaFondo = new Geometry("BarraVidaFondo", fondoQuad);
        
        Material matFondo = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matFondo.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 0.9f));
        matFondo.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        barraVidaFondo.setMaterial(matFondo);
        barraVidaFondo.setLocalTranslation(posX, posY, 0);
        guiNode.attachChild(barraVidaFondo);
        
        Quad vidaQuad = new Quad(anchoBarraMax, altoBarraMax);
        barraVidaActual = new Geometry("BarraVidaActual", vidaQuad);
        
        Material matVida = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matVida.setColor("Color", COLOR_VIDA_ALTA);
        matVida.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        barraVidaActual.setMaterial(matVida);
        barraVidaActual.setLocalTranslation(posX, posY, 0.1f);
        guiNode.attachChild(barraVidaActual);
        
        txtVidaPorcentaje = new BitmapText(font);
        txtVidaPorcentaje.setSize(22f);
        txtVidaPorcentaje.setColor(ColorRGBA.White);
        txtVidaPorcentaje.setText("100%");
        txtVidaPorcentaje.setLocalTranslation(posX + anchoBarraMax / 2 - 30f, posY + 23f, 0.2f);
        guiNode.attachChild(txtVidaPorcentaje);
        
        System.out.println("  ✓ Barra de vida creada");
    }

    public void actualizarVida(int nuevaVida) {
        vidaActual = Math.max(0, Math.min(vidaMaxima, nuevaVida));
        float porcentaje = (float) vidaActual / vidaMaxima;
        
        float anchoBarraMax = 500f;
        float nuevoAncho = anchoBarraMax * porcentaje;
        
        Quad vidaQuad = new Quad(nuevoAncho, 35f);
        barraVidaActual.setMesh(vidaQuad);
        
        Material mat = barraVidaActual.getMaterial();
        if (porcentaje > 0.6f) {
            mat.setColor("Color", COLOR_VIDA_ALTA);
        } else if (porcentaje > 0.3f) {
            mat.setColor("Color", COLOR_VIDA_MEDIA);
        } else {
            mat.setColor("Color", COLOR_VIDA_BAJA);
        }
        
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
        guiNode.attachChild(txtScore);
        
        System.out.println("  ✓ Texto score añadido");
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
        guiNode.attachChild(txtCombo);
        
        System.out.println("  ✓ Texto combo añadido");
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
        guiNode.attachChild(txtCancion);
        
        System.out.println("  ✓ Texto canción añadido");
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
        
        Quad quad = new Quad(tamano, tamano);
        btnPausa = new Geometry("BotonPausa", quad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        btnPausa.setMaterial(mat);
        btnPausa.setLocalTranslation(posX, posY, 10);
        guiNode.attachChild(btnPausa);
        
        txtPausa = new BitmapText(font);
        txtPausa.setSize(40f);
        txtPausa.setColor(ColorRGBA.White);
        txtPausa.setText("||");
        txtPausa.setLocalTranslation(posX + 15f, posY + 42f, 11);
        guiNode.attachChild(txtPausa);
        
        System.out.println("  ✓ Botón de pausa creado");
    }

    // ==================== FEEDBACK TEMPORAL ====================
    
    public void mostrarFeedback(String mensaje, ColorRGBA color) {
        BitmapText txtFeedback = new BitmapText(font);
        txtFeedback.setSize(40f);
        txtFeedback.setColor(color);
        txtFeedback.setText(mensaje);
        
        float anchoTexto = txtFeedback.getLineWidth();
        txtFeedback.setLocalTranslation(ancho / 2 - anchoTexto / 2, alto / 2 + 150f, 1f);
        
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

    // ==================== ⭐ MÉTODOS NUEVOS PARA SISTEMA MEJORADO ====================
    
    public void mostrarBonusPuntos(int puntos) {
        BitmapText txtBonus = new BitmapText(font);
        txtBonus.setSize(32f);
        txtBonus.setColor(new ColorRGBA(1f, 0.8f, 0f, 1f));
        txtBonus.setText("+" + puntos);
        
        float anchoTexto = txtBonus.getLineWidth();
        txtBonus.setLocalTranslation(
            ancho / 2 - anchoTexto / 2, 
            alto / 2 + 100f, 
            2f
        );
        
        guiNode.attachChild(txtBonus);
        mensajesFeedback.add(new MensajeFeedback(txtBonus, 0f));
        
        System.out.println("💰 Bonus de puntos mostrado: +" + puntos);
    }

    public void mostrarAdvertenciaVidaBaja() {
        if (barraVidaActual != null) {
            Material mat = barraVidaActual.getMaterial();
            ColorRGBA colorActual = mat.getParamValue("Color");
            
            if (colorActual != null) {
                if (colorActual.r > 0.8f) {
                    mat.setColor("Color", new ColorRGBA(0.6f, 0.1f, 0.1f, 1f));
                } else {
                    mat.setColor("Color", new ColorRGBA(1f, 0.2f, 0.2f, 1f));
                }
            }
        }
        
        mostrarFeedback("¡VIDA BAJA!", ColorRGBA.Red);
        
        if (materialObjetoCentral != null) {
            materialObjetoCentral.setColor("Color", new ColorRGBA(2f, 0.5f, 0.5f, 1f));
            
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    if (materialObjetoCentral != null && colorBaseObjetoCentral != null) {
                        materialObjetoCentral.setColor("Color", colorBaseObjetoCentral);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        System.out.println("⚠️ ADVERTENCIA: Vida baja activada");
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
        
        if (txtScore != null && txtScore.getParent()== null) {
            guiNode.attachChild(txtScore);
        }
        
        if (txtCombo != null && txtCombo.getParent() == null) {
            guiNode.attachChild(txtCombo);
        }
        
        if (txtCancion != null && txtCancion.getParent() == null) {
            guiNode.attachChild(txtCancion);
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
        
        if (objetoCentral != null) {
            objetoCentral.removeFromParent();
            objetoCentral = null;
        }
        
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