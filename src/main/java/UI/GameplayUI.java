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
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.jme3.math.Vector2f;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.MouseInput;
import Modelo.AstronautaGenerator;


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
    private Node astronautaNode;
    private Geometry astronautaGeometry;
    private Material materialAstronauta;

    private float tiempoBrilloEspacio = 0f;
    private boolean brillandoEspacio = false;
    private ColorRGBA colorBaseObjetoCentral;
    private boolean wiggleActivo = false;
    private float tiempoWiggle = 0f;
    private int wiggleRepeticiones = 0;
    private float wiggleDuracion = 0.35f;
    private float wiggleAmplitud = 18f;
    private float baseX;
    private float baseY;
    private boolean baileActivo = false;
    private float tiempoBaile = 0f;
    private float baileDuracion = 0.8f;
    private float baileAmplitudX = 12f;
    private float baileAmplitudY = 8f;
    
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
    private Geometry objetoCentral;           // Referencia al objeto central
private Material materialObjetoCentral;   // Material del objeto central

    
    // Feedback temporal
    private List<MensajeFeedback> mensajesFeedback;
    private static final float DURACION_FEEDBACK = 1.0f;
    
    // Botón de pausa
    private Geometry btnPausa;
    private BitmapText txtPausa;
    private boolean animandoBotonPausa = false;
    private float tiempoAnimacionBoton = 0f;
    private static final float DURACION_ANIMACION_BOTON = 0.3f;
    private ColorRGBA colorNormalBoton = new ColorRGBA(57f/255f, 129f/255f, 191f/255f, 1f); // #3981BF
    private ColorRGBA colorClickBoton = new ColorRGBA(177f/255f, 35f/255f, 217f/255f, 1f); // #B123D9
    private ColorRGBA colorContornoBoton = new ColorRGBA(112f/255f, 15f/255f, 148f/255f, 1f); // #700F94 Morado oscuro
    
// ⭐ NUEVO: Variables para detección de clics
private com.jme3.input.InputManager inputManager;
private com.jme3.input.controls.ActionListener mouseListener;
private Runnable onClickBotonPausa;
// ⭐ NUEVO: Variables para animaciones del astronauta
private boolean astronautaEnError = false;
private float tiempoError = 0f;
private static final float DURACION_ERROR = 0.8f;

// Flotación
private float tiempoFlotacion = 0f;
private static final float VELOCIDAD_FLOTACION = 1.5f;
private static final float AMPLITUD_FLOTACION_X = 3f;
private static final float AMPLITUD_FLOTACION_Y = 5f;

// Temblor
private boolean temblando = false;
private float tiempoTemblor = 0f;
private static final float DURACION_TEMBLOR = 0.3f;
private static final float AMPLITUD_TEMBLOR = 8f;

// Color original del astronauta
private ColorRGBA colorOriginalAstronauta = new ColorRGBA(1.15f, 1.15f, 1.15f, 1.0f);
 // Callback para cuando se hace clic
    
    // Colores barra de vida
    private static final ColorRGBA COLOR_VIDA_ALTA = new ColorRGBA(0.2f, 1f, 0.3f, 1f);
    private static final ColorRGBA COLOR_VIDA_MEDIA = new ColorRGBA(1f, 0.8f, 0.2f, 1f);
    private static final ColorRGBA COLOR_VIDA_BAJA = new ColorRGBA(1f, 0.3f, 0.2f, 1f);
    
    // 🎨 PALETA DE COLORES PARA FEEDBACK
   private static final ColorRGBA COLOR_IMPECABLE = new ColorRGBA(17f/255f, 216f/255f, 197f/255f, 1f);
   private static final ColorRGBA COLOR_PERFECTO = new ColorRGBA(247f/255f, 181f/255f, 6f/255f, 1f);
   private static final ColorRGBA COLOR_BUENO = new ColorRGBA(242f/255f, 118f/255f, 1f/255f, 1f);
   private static final ColorRGBA COLOR_MALO = new ColorRGBA(255f/255f, 0f/255f, 132f/255f, 1f);

    public GameplayUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        this.mensajesFeedback = new ArrayList<>();
        this.inputManager = app.getInputManager();
        
        System.out.println("\n🎨 Inicializando GameplayUI con objeto central...");
        System.out.println("  Dimensiones: " + ancho + "x" + alto);
        
        inicializarUI();
        verificarVisibilidad();
        
        System.out.println("✓ GameplayUI inicializado correctamente\n");
    }
public void setOnClickBotonPausa(Runnable callback) {
    this.onClickBotonPausa = callback;
    configurarDeteccionClicBoton();
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
    System.out.println("\n ⭐ Creando astronauta protagonista...");
    
    float tamano = 150f;
    float centroX = ancho / 2f - 40f;
    float centroY = alto / 2f - 30f;
    
    try {
        // ⭐ USAR TU GENERADOR PROCEDURAL
        AstronautaGenerator astronautaGen = new AstronautaGenerator(app.getAssetManager());
        astronautaNode = astronautaGen.crearAstronauta(tamano);
        
        // Obtener la geometría del astronauta
        astronautaGeometry = (Geometry) astronautaNode.getChild("AstronautaSprite");
        materialAstronauta = astronautaGeometry.getMaterial();
        
        // Guardar color base para efectos
        colorBaseObjetoCentral = new ColorRGBA(1.1f, 1.1f, 1.1f, 1.0f);
        
        // Posicionar
        baseX = centroX - tamano/2;
        baseY = centroY - tamano/2;
        astronautaNode.setLocalTranslation(baseX, baseY, 5);
        
        // Agregar a la UI
        uiRootNode.attachChild(astronautaNode);
        
        System.out.println(" ✓ Astronauta creado exitosamente");
        System.out.println("   Posición: (" + baseX + ", " + baseY + ")");
        
    } catch (Exception e) {
        System.err.println(" ❌ Error creando astronauta: " + e.getMessage());
        crearObjetoCentralFallback(centroX, centroY, tamano);
    }
}


    private void crearObjetoCentralFallback(float centroX, float centroY, float tamano) {
    materialAstronauta = new Material(app.getAssetManager(),
            "Common/MatDefs/Misc/Unshaded.j3md");
    
    colorBaseObjetoCentral = new ColorRGBA(1.2f, 1.2f, 1.4f, 0.9f);
    materialAstronauta.setColor("Color", colorBaseObjetoCentral);
    materialAstronauta.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    
    // ⭐ CREAR GEOMETRÍA DE RESPALDO
    Geometry geometriaFallback = new Geometry("ObjetoCentral-Fallback", new Quad(tamano, tamano));
    geometriaFallback.setMaterial(materialAstronauta);
    
    baseX = centroX - tamano/2;
    baseY = centroY - tamano/2;
    geometriaFallback.setLocalTranslation(baseX, baseY, 5);
    
    // ⭐ CREAR NODO Y ADJUNTAR LA GEOMETRÍA
    astronautaNode = new Node("AstronautaFallback");
    astronautaNode.attachChild(geometriaFallback);
    astronautaNode.setLocalTranslation(baseX, baseY, 5);
    
    // ⭐ GUARDAR REFERENCIA A LA GEOMETRÍA
    astronautaGeometry = geometriaFallback;
    
    uiRootNode.attachChild(astronautaNode);
    
    System.out.println(" ✓ Objeto central de respaldo creado (cuadrado brillante)");
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
    if (astronautaNode == null || materialAstronauta == null) return;

    // ==================== FLOTACIÓN CONSTANTE ====================
    tiempoFlotacion += tpf * VELOCIDAD_FLOTACION;
    
    float offsetX = FastMath.sin(tiempoFlotacion) * AMPLITUD_FLOTACION_X;
    float offsetY = FastMath.cos(tiempoFlotacion * 0.7f) * AMPLITUD_FLOTACION_Y;
    
    // ==================== ESTADO DE ERROR ====================
    if (astronautaEnError) {
        tiempoError += tpf;
        
        // ===== TEMBLOR =====
        if (temblando && tiempoTemblor < DURACION_TEMBLOR) {
            tiempoTemblor += tpf;
            
            // Temblor random
            float shakeX = (FastMath.rand.nextFloat() * 2f - 1f) * AMPLITUD_TEMBLOR;
            float shakeY = (FastMath.rand.nextFloat() * 2f - 1f) * AMPLITUD_TEMBLOR;
            
            offsetX += shakeX;
            offsetY += shakeY;
            
            // Color rojo intenso
            materialAstronauta.setColor("Color", new ColorRGBA(2.5f, 0.3f, 0.3f, 1.0f));
        } else {
            temblando = false;
        }
        
        // ===== TRANSICIÓN DE VUELTA A NORMAL =====
        if (tiempoError >= DURACION_ERROR) {
            // Fade del rojo al color original
            float factorRecuperacion = (tiempoError - DURACION_ERROR) / 0.2f;
            factorRecuperacion = FastMath.clamp(factorRecuperacion, 0f, 1f);
            
            ColorRGBA colorRojo = new ColorRGBA(2.0f, 0.4f, 0.4f, 1.0f);
            ColorRGBA colorActual = interpolarColor(colorRojo, colorOriginalAstronauta, factorRecuperacion);
            materialAstronauta.setColor("Color", colorActual);
            
            if (factorRecuperacion >= 1.0f) {
                astronautaEnError = false;
                tiempoError = 0f;
            }
        }
    }
    // ==================== ESTADO NORMAL ====================
    else {
        // ===== BRILLO DE ESPACIO =====
        if (brillandoEspacio) {
            tiempoBrilloEspacio += tpf;
            float duracionBrillo = 0.4f;
            
            if (tiempoBrilloEspacio < duracionBrillo) {
                float progreso = tiempoBrilloEspacio / duracionBrillo;
                float intensidad = 1.0f + (3.0f * (1.0f - progreso));
                
                float r = intensidad;
                float g = intensidad;
                float b = intensidad + (3.0f * (1.0f - progreso));
                
                materialAstronauta.setColor("Color", new ColorRGBA(r, g, b, 1.0f));
            } else {
                brillandoEspacio = false;
                materialAstronauta.setColor("Color", colorOriginalAstronauta);
            }
        } 
        // ===== PULSO CONSTANTE =====
        else {
            tiempoPulso += tpf * VELOCIDAD_PULSO;
            float intensidadPulso = 1.0f + 0.15f * FastMath.sin(tiempoPulso);
            ColorRGBA colorPulso = colorOriginalAstronauta.mult(intensidadPulso);
            materialAstronauta.setColor("Color", colorPulso);
        }
    }
    
    // ==================== APLICAR POSICIÓN FINAL ====================
    astronautaNode.setLocalTranslation(
        baseX + offsetX,
        baseY + offsetY,
        astronautaNode.getLocalTranslation().z
    );

    // ===== WIGGLE (movimiento especial - sin cambios) =====
    if (wiggleActivo) {
        tiempoWiggle += tpf;
        float fase = (tiempoWiggle / wiggleDuracion) * FastMath.TWO_PI;
        float offsetWiggleX = wiggleAmplitud * FastMath.sin(fase);
        float offsetWiggleY = 6f * FastMath.sin(fase * 2f) * 0.2f;
        
        astronautaNode.setLocalTranslation(
            baseX + offsetX + offsetWiggleX,
            baseY + offsetY + offsetWiggleY,
            astronautaNode.getLocalTranslation().z
        );
        
        if (tiempoWiggle >= wiggleDuracion) {
            tiempoWiggle = 0f;
            wiggleRepeticiones--;
            if (wiggleRepeticiones <= 0) {
                wiggleActivo = false;
            }
        }
    }

    // ===== BAILE (sin cambios) =====
    else if (baileActivo) {
        tiempoBaile += tpf;
        float progreso = tiempoBaile / baileDuracion;
        float fase = progreso * FastMath.TWO_PI;
        float ease = 0.5f - 0.5f * FastMath.cos(FastMath.PI * Math.min(1f, progreso));
        
        float offsetBaileX = baileAmplitudX * FastMath.sin(fase) * ease;
        float offsetBaileY = baileAmplitudY * FastMath.sin(fase * 0.5f) * 0.6f * ease;
        
        astronautaNode.setLocalTranslation(
            baseX + offsetX + offsetBaileX,
            baseY + offsetY + offsetBaileY,
            astronautaNode.getLocalTranslation().z
        );
        
        if (tiempoBaile >= baileDuracion) {
            baileActivo = false;
            tiempoBaile = 0f;
        }
    }
}
    public void activarAnimacionError() {
    astronautaEnError = true;
    temblando = true;
    tiempoError = 0f;
    tiempoTemblor = 0f;
    
    System.out.println("💥 ANIMACIÓN DE ERROR ACTIVADA - Astronauta tiembla y se pone rojo");
}

/**
 * ⭐ NUEVO: Restaura el astronauta al estado normal inmediatamente
 */
public void restaurarAstronautaNormal() {
    if (astronautaEnError) {
        astronautaEnError = false;
        temblando = false;
        tiempoError = 0f;
        tiempoTemblor = 0f;
        
        if (materialAstronauta != null) {
            materialAstronauta.setColor("Color", colorOriginalAstronauta);
        }
        
        System.out.println("✅ Astronauta restaurado a estado normal");
    }
}

    public void activarBrilloHit(ColorRGBA colorFlecha) {
    if (materialAstronauta == null) return;
    
    // Mezclar color del astronauta con el de la flecha
    ColorRGBA colorMezclado = colorBaseObjetoCentral.add(colorFlecha).mult(0.7f);
    colorMezclado.a = 1.0f;
    
    materialAstronauta.setColor("Color", colorMezclado);
    
    System.out.println("💫 Astronauta brillando con color de hit");
}

    public void activarWiggleEspacio(int repeticiones) {
        wiggleActivo = true;
        wiggleRepeticiones = Math.max(1, repeticiones);
        tiempoWiggle = 0f;
        System.out.println("↔️ Objeto central WIGGLE x" + wiggleRepeticiones + " - Mecánica ESPACIO");
    }

    public void activarBaileEspacio(float duracion) {
        baileActivo = true;
        tiempoBaile = 0f;
        baileDuracion = Math.max(0.3f, duracion);
        System.out.println("〰️ Objeto central BAILE suave (" + baileDuracion + "s) - ESPACIO");
    }

    // ==================== INDICADORES DIRECCIONALES EN LOS LADOS ====================
    
    private void crearIndicadoresDireccionales() {
        System.out.println("\n  📍 Creando indicadores direccionales en los lados del objeto central...");
        
        nodoIndicadoresDireccionales = new Node("IndicadoresDireccionales");
        
    float centroX = ancho / 2f - 40f;  // Mismo centro que zonas de impacto
    float centroY = alto / 2f - 30f;
        
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
        float anchoBarra = (float) vidaActual / 100f;
        
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
    
    // ==================== BOTÓN DE PAUSA MEJORADO ====================

/**
 * ⭐ MEJORADO: Botón de pausa con estilo y animación
 */
private void crearBotonPausa() {
    float tamano = 60f;
    float posX = ancho - tamano - 20f;
    float posY = alto - tamano - 80f;

    System.out.println(" ⏸ Creando botón de pausa mejorado en: (" + posX + ", " + posY + ")");

    // ========== CREAR TEXTURA PROCEDURAL DEL BOTÓN ==========
    Texture texturaBtnPausa = crearTexturaBotonPausa((int)tamano);
    
    // ========== FONDO DEL BOTÓN CON TEXTURA ==========
    Quad quad = new Quad(tamano, tamano);
    btnPausa = new Geometry("BotonPausa", quad);
    
    Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", texturaBtnPausa);
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    btnPausa.setMaterial(mat);
    btnPausa.setLocalTranslation(posX, posY, 10);
    
    guiNode.attachChild(btnPausa);

    System.out.println(" ✓ Botón de pausa mejorado creado con contorno y líneas gruesas");
}
private Texture crearTexturaBotonPausa(int size) {
    ByteBuffer buffer = BufferUtils.createByteBuffer(size * size * 4);
    
    float grosorContorno = size * 0.08f; // 8% del tamaño para el contorno
    float grosorLinea = size * 0.15f; // 15% del tamaño para cada línea (más gruesas)
    float separacionLineas = size * 0.12f; // 12% de separación entre líneas
    
    // Calcular posiciones de las líneas (centradas)
    float centroX = size / 2f;
    float anchoTotalLineas = (grosorLinea * 2) + separacionLineas;
    float inicioLinea1 = centroX - (anchoTotalLineas / 2f);
    float finLinea1 = inicioLinea1 + grosorLinea;
    float inicioLinea2 = finLinea1 + separacionLineas;
    float finLinea2 = inicioLinea2 + grosorLinea;
    
    // Altura de las líneas (más altas, ocupan más espacio vertical)
    float margenVertical = size * 0.20f; // 20% de margen arriba y abajo
    float inicioLineaY = margenVertical;
    float finLineaY = size - margenVertical;
    
    for (int y = 0; y < size; y++) {
        for (int x = 0; x < size; x++) {
            ColorRGBA pixel = calcularPixelBotonPausa(
                x, y, size,
                grosorContorno,
                inicioLinea1, finLinea1,
                inicioLinea2, finLinea2,
                inicioLineaY, finLineaY
            );
            
            buffer.put((byte) (pixel.r * 255));
            buffer.put((byte) (pixel.g * 255));
            buffer.put((byte) (pixel.b * 255));
            buffer.put((byte) (pixel.a * 255));
        }
    }
    
    buffer.flip();
    
    Image image = new Image(
        Image.Format.RGBA8,
        size,
        size,
        buffer,
        ColorSpace.sRGB
    );
    
    Texture2D texture = new Texture2D(image);
    texture.setMagFilter(Texture.MagFilter.Bilinear);
    texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
    texture.setWrap(Texture.WrapMode.Clamp);
    
    return texture;
}
private ColorRGBA calcularPixelBotonPausa(
    int x, int y, int size,
    float grosorContorno,
    float inicioLinea1, float finLinea1,
    float inicioLinea2, float finLinea2,
    float inicioLineaY, float finLineaY
) {
    // Calcular si estamos en el borde
    boolean enBordeExterno = x < grosorContorno || x >= (size - grosorContorno) ||
                             y < grosorContorno || y >= (size - grosorContorno);
    
    if (enBordeExterno) {
        // Contorno morado oscuro
        return colorContornoBoton;
    }
    
    // Verificar si estamos dentro de las líneas de pausa
    boolean enLinea1 = x >= inicioLinea1 && x < finLinea1 && 
                       y >= inicioLineaY && y < finLineaY;
    boolean enLinea2 = x >= inicioLinea2 && x < finLinea2 && 
                       y >= inicioLineaY && y < finLineaY;
    
    if (enLinea1 || enLinea2) {
        // Líneas blancas
        return ColorRGBA.White;
    }
    
    // Fondo azul del botón
    return colorNormalBoton;
}
private void configurarDeteccionClicBoton() {
    if (inputManager == null || btnPausa == null) {
        System.err.println("⚠ No se puede configurar detección de clics: inputManager o btnPausa es null");
        return;
    }

    // Limpiar listener previo si existe
    if (mouseListener != null) {
        try {
            inputManager.removeListener(mouseListener);
            inputManager.deleteMapping("ClickBotonPausa");
        } catch (Exception e) {
            // No existía, ok
        }
    }

    // Crear listener de mouse usando la clase anónima
    mouseListener = new com.jme3.input.controls.ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("ClickBotonPausa") && isPressed) {
                // Verificar si el clic fue dentro del botón
                verificarClicEnBoton();
            }
        }
    };

    // Mapear clic izquierdo del mouse
    inputManager.addMapping("ClickBotonPausa", 
        new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addListener(mouseListener, "ClickBotonPausa");

    System.out.println("✓ Detección de clics en botón de pausa configurada");
}
/**
 * ⭐ NUEVO: Verifica si el clic del mouse fue dentro del área del botón
 */
/**
 * ⭐ NUEVO: Verifica si el clic del mouse fue dentro del área del botón
 */
private void verificarClicEnBoton() {
    if (btnPausa == null || inputManager == null) return;

    // Obtener posición del mouse
    Vector2f clickPos = inputManager.getCursorPosition();
    float mouseX = clickPos.x;
    float mouseY = clickPos.y;

    // Obtener bounds del botón
    Vector3f posBtnPausa = btnPausa.getLocalTranslation();
    float btnX = posBtnPausa.x;
    float btnY = posBtnPausa.y;
    float btnAncho = 60f; // Tamaño del botón
    float btnAlto = 60f;

    // Verificar si el clic está dentro del botón
    boolean dentroDelBoton = mouseX >= btnX && mouseX <= (btnX + btnAncho) &&
                              mouseY >= btnY && mouseY <= (btnY + btnAlto);

    if (dentroDelBoton) {
        System.out.println("🖱️ Clic detectado en botón de pausa!");
        
        // Activar animación
        animarClickBotonPausa();
        
        // Ejecutar callback (pausar el juego)
        if (onClickBotonPausa != null) {
            onClickBotonPausa.run();
        }
    }
}
public void animarClickBotonPausa() {
    if (btnPausa == null) return;
    
    animandoBotonPausa = true;
    tiempoAnimacionBoton = 0f;
    
    // Guardar color original temporalmente
    ColorRGBA colorOriginal = colorNormalBoton.clone();
    
    // Cambiar al color de click
    colorNormalBoton = colorClickBoton.clone();
    
    // Recrear textura con nuevo color
    Texture nuevaTextura = crearTexturaBotonPausa(60);
    Material mat = btnPausa.getMaterial();
    mat.setTexture("ColorMap", nuevaTextura);
    
    // Restaurar color original para futuras animaciones
    colorNormalBoton = colorOriginal;
    
    System.out.println("🎯 Animación de botón de pausa activada");
}
/**
 * ⭐ NUEVO: Actualiza la animación del botón de pausa
 * Debe llamarse desde actualizarFeedback() o desde un update general
 */
public void actualizarAnimacionBotonPausa(float tpf) {
    if (!animandoBotonPausa || btnPausa == null) return;
    
    tiempoAnimacionBoton += tpf;
    float progreso = tiempoAnimacionBoton / DURACION_ANIMACION_BOTON; // 0.0 a 1.0
    
    if (progreso <= 1.0f) {
        // ========== ANIMACIÓN DE REBOTE ==========
        float escala;
        float t = progreso;
        
        if (t < 0.5f) {
            // Primera mitad: comprimir (1.0 → 0.85)
            float t1 = t / 0.5f; // 0.0 a 1.0
            escala = 1.0f - (0.15f * easeInQuad(t1));
        } else {
            // Segunda mitad: expandir y rebotar (0.85 → 1.0 con rebote)
            float t2 = (t - 0.5f) / 0.5f; // 0.0 a 1.0
            escala = 0.85f + (0.15f * easeOutElastic(t2));
        }
        
        btnPausa.setLocalScale(escala);
        
        // ========== TRANSICIÓN DE COLOR ==========
        // Después del 40% del progreso, empezar a volver al color original
        if (progreso > 0.4f) {
            float colorProgreso = (progreso - 0.4f) / 0.6f; // 0.0 a 1.0
            ColorRGBA colorActual = interpolarColor(colorClickBoton, colorNormalBoton, colorProgreso);
            Material mat = btnPausa.getMaterial();
            mat.setColor("Color", colorActual);
        }
        
    } else {
        // ========== FINALIZAR ANIMACIÓN ==========
        animandoBotonPausa = false;
        btnPausa.setLocalScale(1.0f);
        
        Material mat = btnPausa.getMaterial();
        mat.setColor("Color", colorNormalBoton);
        
        System.out.println("✓ Animación de botón completada");
    }
}
private float easeInQuad(float t) {
    return t * t;
}

/**
 * Función de easing para el rebote elástico (expansión)
 */
private float easeOutElastic(float t) {
    if (t == 0f || t == 1f) return t;
    
    float p = 0.3f;
    float s = p / 4f;
    
    return (float) (Math.pow(2, -10 * t) * Math.sin((t - s) * (2 * Math.PI) / p) + 1);
}

/**
 * Interpola entre dos colores (ya existe pero la incluyo por si acaso)
 */
private ColorRGBA interpolarColor(ColorRGBA c1, ColorRGBA c2, float t) {
    return new ColorRGBA(
        c1.r + (c2.r - c1.r) * t,
        c1.g + (c2.g - c1.g) * t,
        c1.b + (c2.b - c1.b) * t,
        c1.a + (c2.a - c1.a) * t
    );
}

/**
 * ⭐ MEJORADO: Mensajes con contorno blanco para mejor visibilidad
 */
public void mostrarFeedback(String mensaje, ColorRGBA color) {
    // ⭐ TAMAÑO MÁS GRANDE según importancia
    float tamano;
    float escalaInicial;

    if (mensaje.contains("IMPECABLE") || mensaje.contains("PERFECTO")) {
        tamano = 60f;
        escalaInicial = 1.5f;
    } else if (mensaje.contains("BUENO")) {
        tamano = 50f;
        escalaInicial = 1.3f;
    } else if (mensaje.contains("MALO") || mensaje.contains("TARDÍO")) {
        tamano = 45f;
        escalaInicial = 1.2f;
    } else if (mensaje.contains("MISS")) {
        tamano = 55f;
        escalaInicial = 1.4f;
    } else {
        tamano = 50f;
        escalaInicial = 1.3f;
    }

    // ========== CREAR CONTORNO BLANCO ==========
    // El contorno se crea como un texto ligeramente más grande y desplazado
    float offsetContorno = tamano * 0.025f; // 4% del tamaño = contorno moderado
    
    // Crear 8 textos de contorno (en las 8 direcciones principales)
    ColorRGBA colorContorno = new ColorRGBA(0f, 0f, 0f, 0.85f); // Negro semi-transparente 
    
    float[][] offsetsContorno = {
        {-offsetContorno, 0}, {offsetContorno, 0},     // Izquierda, Derecha
        {0, -offsetContorno}, {0, offsetContorno},     // Abajo, Arriba
        {-offsetContorno, -offsetContorno}, {offsetContorno, -offsetContorno}, // Diagonales
        {-offsetContorno, offsetContorno}, {offsetContorno, offsetContorno}
    };
    
    List<BitmapText> textosBorde = new ArrayList<>();
    
    for (float[] offset : offsetsContorno) {
        BitmapText txtBorde = new BitmapText(font);
        txtBorde.setSize(tamano);
        txtBorde.setColor(colorContorno);
        txtBorde.setText(mensaje);
        
        float anchoTexto = txtBorde.getLineWidth();
        txtBorde.setLocalTranslation(
            ancho / 2 - anchoTexto / 2 + offset[0], 
            alto / 2 + 150f + offset[1], 
            1.9f // Ligeramente detrás del texto principal
        );
        
        guiNode.attachChild(txtBorde);
        textosBorde.add(txtBorde);
    }

    // ========== CREAR TEXTO PRINCIPAL ==========
    BitmapText txtFeedback = new BitmapText(font);
    txtFeedback.setSize(tamano);
    txtFeedback.setColor(color);
    txtFeedback.setText(mensaje);

    // Centrar texto principal
    float anchoTexto = txtFeedback.getLineWidth();
    txtFeedback.setLocalTranslation(ancho / 2 - anchoTexto / 2, alto / 2 + 150f, 2f);
    
    guiNode.attachChild(txtFeedback);

    // ========== GUARDAR FEEDBACK CON SU CONTORNO ==========
    MensajeFeedback msg = new MensajeFeedback(txtFeedback, 0f);
    msg.escalaInicial = escalaInicial;
    msg.textosBorde = textosBorde; // ⭐ NUEVO: Guardar referencias al contorno
    mensajesFeedback.add(msg);
}



   public void actualizarFeedback(float tpf) {
    List<MensajeFeedback> mensajesAEliminar = new ArrayList<>();
    actualizarAnimacionBotonPausa(tpf);
    
    for (MensajeFeedback msg : mensajesFeedback) {
        msg.tiempo += tpf;
        float progreso = msg.tiempo / DURACION_FEEDBACK; // 0.0 a 1.0

        if (progreso <= 1.0f) {
            // === FASE 1: Pop inicial (primeros 15%) ===
            float escala;
            if (progreso < 0.15f) {
                float t = progreso / 0.15f;
                escala = msg.escalaInicial - ((msg.escalaInicial - 1f) * easeOutBounce(t));
            } else {
                escala = 1.0f;
            }
            
            // ⭐ APLICAR ESCALA AL TEXTO PRINCIPAL Y AL CONTORNO
            msg.texto.setLocalScale(escala);
            for (BitmapText borde : msg.textosBorde) {
                borde.setLocalScale(escala);
            }

            // === FASE 2: Desvanecimiento (últimos 40%) ===
            float alpha;
            if (progreso < 0.6f) {
                alpha = 1.0f;
            } else {
                float fadeProgress = (progreso - 0.6f) / 0.4f;
                alpha = 1.0f - fadeProgress;
            }

            // ⭐ APLICAR TRANSPARENCIA AL TEXTO PRINCIPAL
            ColorRGBA colorActual = msg.colorOriginal.clone();
            colorActual.a = alpha;
            msg.texto.setColor(colorActual);
            
            // ⭐ APLICAR TRANSPARENCIA AL CONTORNO
            ColorRGBA colorBorde = new ColorRGBA(0f, 0f, 0f, alpha * 0.85f); // Negro
            for (BitmapText borde : msg.textosBorde) {
                borde.setColor(colorBorde);
            }

            // === FASE 3: Movimiento flotante ===
            Vector3f pos = msg.texto.getLocalTranslation();
            float velocidadY = 80f * (1f - progreso * 0.5f);
            float desplazamientoX = FastMath.sin(progreso * FastMath.PI * 2f) * 10f;

            // ⭐ MOVER TEXTO PRINCIPAL
            msg.texto.setLocalTranslation(
                pos.x + desplazamientoX * tpf,
                pos.y + velocidadY * tpf,
                pos.z
            );
            
            // ⭐ MOVER CONTORNO (mantener offset relativo)
            float offsetContorno = msg.texto.getSize() * 0.025f;
            float[][] offsetsContorno = {
                {-offsetContorno, 0}, {offsetContorno, 0},
                {0, -offsetContorno}, {0, offsetContorno},
                {-offsetContorno, -offsetContorno}, {offsetContorno, -offsetContorno},
                {-offsetContorno, offsetContorno}, {offsetContorno, offsetContorno}
            };
            
            for (int i = 0; i < msg.textosBorde.size() && i < offsetsContorno.length; i++) {
                BitmapText borde = msg.textosBorde.get(i);
                Vector3f posBorde = borde.getLocalTranslation();
                borde.setLocalTranslation(
                    pos.x + desplazamientoX * tpf + offsetsContorno[i][0],
                    pos.y + velocidadY * tpf + offsetsContorno[i][1],
                    posBorde.z
                );
            }
            
        } else {
            // ⭐ ELIMINAR MENSAJE Y SU CONTORNO
            msg.texto.removeFromParent();
            for (BitmapText borde : msg.textosBorde) {
                borde.removeFromParent();
            }
            mensajesAEliminar.add(msg);
        }
    }

    mensajesFeedback.removeAll(mensajesAEliminar);
}
    
    private float easeOutBounce(float t) {
    if (t < (1f / 2.75f)) {
        return 7.5625f * t * t;
    } else if (t < (2f / 2.75f)) {
        t -= (1.5f / 2.75f);
        return 7.5625f * t * t + 0.75f;
    } else if (t < (2.5f / 2.75f)) {
        t -= (2.25f / 2.75f);
        return 7.5625f * t * t + 0.9375f;
    } else {
        t -= (2.625f / 2.75f);
        return 7.5625f * t * t + 0.984375f;
    }
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
    
    public void ocultarBarraVida() {
        System.out.println("🔧 Ocultando barra de vida (modo práctica)");
        if (barraVidaFondo != null) barraVidaFondo.removeFromParent();
        if (barraVidaActual != null) barraVidaActual.removeFromParent();
        if (txtVidaPorcentaje != null) txtVidaPorcentaje.removeFromParent();
    }

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
    
    // Limpiar mensajes
    for (MensajeFeedback msg : mensajesFeedback) {
        if (msg.texto.getParent() != null) {
            msg.texto.removeFromParent();
        }
    }
    mensajesFeedback.clear();
    
    // ⭐ LIMPIAR ASTRONAUTA
    if (astronautaNode != null) {
        astronautaNode.removeFromParent();
        astronautaNode = null;
    }
    
    if (uiRootNode != null) {
        uiRootNode.detachAllChildren();
        uiRootNode = null;
    }
    
    // Limpiar listener de mouse
    if (mouseListener != null && inputManager != null) {
        try {
            inputManager.removeListener(mouseListener);
            inputManager.deleteMapping("ClickBotonPausa");
            System.out.println(" ✓ Listener de botón de pausa limpiado");
        } catch (Exception e) {
            System.err.println(" ⚠ Error limpiando listener: " + e.getMessage());
        }
    }
    
    System.out.println("✓ GameplayUI limpiado");
}

    // ==================== CLASE INTERNA ====================
    
   // ==================== CLASE INTERNA ====================
private static class MensajeFeedback {
    BitmapText texto;
    float tiempo;
    float escalaInicial;
    ColorRGBA colorOriginal;
    List<BitmapText> textosBorde; // ⭐ NUEVO: Referencias a los textos de contorno

    MensajeFeedback(BitmapText texto, float tiempo) {
        this.texto = texto;
        this.tiempo = tiempo;
        this.escalaInicial = 1.3f;
        this.colorOriginal = texto.getColor().clone();
        this.textosBorde = new ArrayList<>(); // ⭐ NUEVO
    }
}

}