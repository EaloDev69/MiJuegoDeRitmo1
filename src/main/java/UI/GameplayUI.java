/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package UI;

import Modelo.Direccion;
import Modelo.FlechaProceduralGenerator;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.ui.Picture;

/**
 * GameplayUI - Sistema de UI mejorado con:
 * ✅ Zonas de impacto visibles en los bordes
 * ✅ Objeto central reactivo con pulso rítmico
 * ✅ Feedback visual mejorado
 * ✅ Sistema de brillo sincronizado con la música
 */
public class GameplayUI {
    
    private SimpleApplication app;
    private Node uiRootNode;
    private Node guiNode;
    
    // Dimensiones
    private float ancho;
    private float alto;
    
    // ==================== ZONAS DE IMPACTO (NUEVAS) ====================
    private Geometry zonaArriba;
    private Geometry zonaAbajo;
    private Geometry zonaIzquierda;
    private Geometry zonaDerecha;
    private Geometry zonaEspacio; // Centro
    
    // ==================== OBJETO CENTRAL MEJORADO ====================
    private Geometry objetoCentral;
    private Material materialObjetoCentral;
    private ColorRGBA colorBaseObjetoCentral = new ColorRGBA(1f, 1f, 1f, 0.9f);
    
    // Sistema de pulso rítmico
    private float tiempoUltimoBeat = 0f;
    private float intervaloBPM = 0.5f; // Se actualizará con el BPM real
    private boolean pulsoActivado = false;
    private float tiempoPulso = 0f;
    private static final float VELOCIDAD_PULSO = 2f;
    
    // Sistema de brillo por ESPACIO
    private boolean brillandoEspacio = false;
    private float tiempoBrilloEspacio = 0f;
    
    // ==================== ELEMENTOS UI EXISTENTES ====================
    private Geometry barraVida;
    private Geometry barraVidaFondo;
    private Material materialVida;
    
    private BitmapText textoScore;
    private BitmapText textoCombo;
    private BitmapText textoFeedback;
    private BitmapText textoCancion;
    
    private Picture botonPausa;
    
    // Control de feedback
    private float tiempoFeedback = 0f;
    private boolean mostrandoFeedback = false;
    
    // ==================== CONSTRUCTOR ====================
    
    public GameplayUI(SimpleApplication app) {
        this.app = app;
        this.guiNode = app.getGuiNode();
        this.ancho = app.getCamera().getWidth();
        this.alto = app.getCamera().getHeight();
        
        inicializarUI();
    }
    
    // ==================== INICIALIZACIÓN ====================
    
    private void inicializarUI() {
        uiRootNode = new Node("GameplayUIRoot");
        
        crearObjetoCentral();
        crearZonasDeImpacto(); // ⭐ NUEVO
        
        crearBarraVida();
        crearTextoScore();
        crearTextoCombo();
        crearTextoCancion();
        crearBotonPausa();
        
        guiNode.attachChild(uiRootNode);
        
        System.out.println("✓ GameplayUI inicializado");
    }
    
    // ==================== OBJETO CENTRAL MEJORADO ====================
    
    private void crearObjetoCentral() {
        float tamano = 200f;
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", colorBaseObjetoCentral);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        objetoCentral = new Geometry("ObjetoCentral", new Quad(tamano, tamano));
        objetoCentral.setMaterial(mat);
        
        float posX = (ancho / 2f) - (tamano / 2f);
        float posY = (alto / 2f) - (tamano / 2f);
        objetoCentral.setLocalTranslation(posX, posY, 1f);
        
        materialObjetoCentral = mat;
        uiRootNode.attachChild(objetoCentral);
    }
    
    // ==================== ZONAS DE IMPACTO (NUEVO SISTEMA) ====================
    
    /**
     * ⭐ INVISIBLES: Zonas de impacto para lógica de hit solamente
     * Están en el centro pero NO se ven - solo el objeto central es visible
     */
    private void crearZonasDeImpacto() {
        System.out.println("\n🎯 Creando zonas de impacto (invisibles)...");
        
        float tamanoZona = 80f;
        float distanciaDelCentro = 120f;
        
        float centroX = ancho / 2;
        float centroY = alto / 2;
        
        // Obtener generador procedural
        FlechaProceduralGenerator generator = new FlechaProceduralGenerator(app.getAssetManager());
        
        // === ZONA ARRIBA (INVISIBLE) ===
        zonaArriba = crearZonaInvisible(
            Direccion.ARRIBA,
            centroX - tamanoZona/2,
            centroY + distanciaDelCentro - tamanoZona/2,
            tamanoZona
        );
        
        // === ZONA ABAJO (INVISIBLE) ===
        zonaAbajo = crearZonaInvisible(
            Direccion.ABAJO,
            centroX - tamanoZona/2,
            centroY - distanciaDelCentro - tamanoZona/2,
            tamanoZona
        );
        
        // === ZONA IZQUIERDA (INVISIBLE) ===
        zonaIzquierda = crearZonaInvisible(
            Direccion.IZQUIERDA,
            centroX - distanciaDelCentro - tamanoZona/2,
            centroY - tamanoZona/2,
            tamanoZona
        );
        
        // === ZONA DERECHA (INVISIBLE) ===
        zonaDerecha = crearZonaInvisible(
            Direccion.DERECHA,
            centroX + distanciaDelCentro - tamanoZona/2,
            centroY - tamanoZona/2,
            tamanoZona
        );
        
        System.out.println("✓ Zonas de impacto creadas (invisibles, solo lógica)");
    }
    
    /**
     * ⭐ NUEVO: Crea zona invisible para lógica de hit
     */
    private Geometry crearZonaInvisible(Direccion direccion, float x, float y, float tamano) {
        // Crear material completamente transparente
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0f)); // Totalmente transparente
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        // Crear geometría
        Quad quad = new Quad(tamano, tamano);
        Geometry zona = new Geometry("Zona_" + direccion, quad);
        zona.setMaterial(mat);
        zona.setLocalTranslation(x, y, 0.1f); // Z muy bajo, debajo del objeto central
        
        uiRootNode.attachChild(zona);
        
        return zona;
    }
    
    /**
     * ⭐ NUEVO: Crea una zona de impacto individual (outline de flecha)
     */
    private Geometry crearZonaImpacto(FlechaProceduralGenerator generator, 
                                      Direccion direccion, 
                                      float x, float y, float tamano) {
        
        // Colores para el outline (semi-transparente)
        ColorRGBA colorBorde = generator.obtenerBordePorDireccion(direccion);
        ColorRGBA colorFondo = new ColorRGBA(0.1f, 0.1f, 0.1f, 0.3f); // Oscuro semi-transparente
        
        // Crear textura del outline
        Texture textura = generator.crearTexturaZonaFlecha(direccion, colorBorde, colorFondo);
        
        // Crear material
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        // Crear geometría
        Quad quad = new Quad(tamano, tamano);
        Geometry zona = new Geometry("Zona_" + direccion, quad);
        zona.setMaterial(mat);
        zona.setLocalTranslation(x, y, 0.5f); // Z más bajo que las flechas
        
        uiRootNode.attachChild(zona);
        
        return zona;
    }
    
    /**
     * ⭐ NUEVO: Crea la zona central (para ESPACIO)
     */
    private Geometry crearZonaCentro(FlechaProceduralGenerator generator,
                                      float x, float y, float tamano) {
        
        ColorRGBA colorBorde = new ColorRGBA(0.8f, 0.6f, 1.0f, 1.0f); // Violeta
        ColorRGBA colorFondo = new ColorRGBA(0.3f, 0.1f, 0.4f, 0.5f); // Violeta oscuro semi-transparente
        
        Texture textura = generator.crearTexturaZonaEspacio(colorBorde, colorFondo);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        Quad quad = new Quad(tamano, tamano);
        Geometry zona = new Geometry("Zona_Centro", quad);
        zona.setMaterial(mat);
        zona.setLocalTranslation(x, y, 0.5f);
        
        uiRootNode.attachChild(zona);
        
        return zona;
    }
    
    /**
     * ⭐ MEJORADO: Hace brillar una zona cuando se presiona la tecla correcta
     * Para ESPACIO, brilla el objeto central directamente
     */
    public void activarZona(Direccion direccion) {
        Geometry zona = null;
        
        switch(direccion) {
            case ARRIBA: zona = zonaArriba; break;
            case ABAJO: zona = zonaAbajo; break;
            case IZQUIERDA: zona = zonaIzquierda; break;
            case DERECHA: zona = zonaDerecha; break;
            case ESPACIO: 
                // Para ESPACIO, brillar el objeto central directamente
                activarBrilloEspacio();
                return;
        }
        
        if (zona != null) {
            final Geometry zonaFinal = zona;
            final Material mat = zona.getMaterial();
            
            // Guardar color original
            ColorRGBA colorOriginal = mat.getParamValue("Color");
            if (colorOriginal == null) {
                colorOriginal = ColorRGBA.White;
            }
            final ColorRGBA colorFinal = colorOriginal.clone();
            
            // Flash brillante
            mat.setColor("Color", ColorRGBA.White.mult(2f));
            
            // Restaurar después de 0.15s
            new Thread(() -> {
                try {
                    Thread.sleep(150);
                    app.enqueue(() -> {
                        mat.setColor("Color", colorFinal);
                        return null;
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    // ==================== SISTEMA DE PULSO RÍTMICO ====================
    
    /**
     * ⭐ NUEVO: Configura el pulso según el BPM de la canción
     */
    public void configurarPulsoBPM(float bpm) {
        if (bpm > 0) {
            this.intervaloBPM = 60.0f / bpm; // Convertir BPM a intervalo en segundos
            System.out.println("🎵 Pulso configurado: " + bpm + " BPM (" + 
                             String.format("%.2f", intervaloBPM) + "s por beat)");
        }
    }
    
    /**
     * ⭐ NUEVO: Actualiza el brillo del objeto central
     * Ahora incluye pulso al ritmo de la música
     */
    public void actualizarBrilloObjetoCentral(float tpf, float tiempoCancion) {
        if (objetoCentral == null || materialObjetoCentral == null) return;
        
        // ========== EFECTO DE BRILLO DE ESPACIO (prioritario) ==========
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
        }
        // ========== PULSO RÍTMICO SINCRONIZADO CON LA MÚSICA ==========
        else {
            // Detectar beats
            float tiempoDesdeUltimoBeat = tiempoCancion - tiempoUltimoBeat;
            
            if (tiempoDesdeUltimoBeat >= intervaloBPM) {
                tiempoUltimoBeat = tiempoCancion;
                pulsoActivado = true;
                tiempoPulso = 0f;
            }
            
            // Aplicar efecto de pulso
            if (pulsoActivado) {
                tiempoPulso += tpf * 4f; // Velocidad del pulso
                
                if (tiempoPulso < 1.0f) {
                    // Pulso hacia afuera
                    float intensidad = 1.0f + (0.5f * FastMath.sin(tiempoPulso * FastMath.PI));
                    ColorRGBA colorPulso = colorBaseObjetoCentral.mult(intensidad);
                    materialObjetoCentral.setColor("Color", colorPulso);
                    
                    // Escalar ligeramente el objeto
                    float escala = 1.0f + (0.1f * FastMath.sin(tiempoPulso * FastMath.PI));
                    objetoCentral.setLocalScale(escala);
                } else {
                    // Restaurar
                    pulsoActivado = false;
                    materialObjetoCentral.setColor("Color", colorBaseObjetoCentral);
                    objetoCentral.setLocalScale(1.0f);
                }
            }
            // Pulso suave constante cuando no hay beat
            else {
                tiempoPulso += tpf * VELOCIDAD_PULSO;
                float intensidadPulso = 1.0f + 0.05f * FastMath.sin(tiempoPulso);
                ColorRGBA colorPulso = colorBaseObjetoCentral.mult(intensidadPulso);
                materialObjetoCentral.setColor("Color", colorPulso);
            }
        }
    }
    
    /**
     * ⭐ MEJORADO: Activa brillo más intenso al golpear flechas
     */
    public void activarBrilloHit(ColorRGBA colorFlecha, boolean perfectHit) {
        if (materialObjetoCentral == null) return;
        
        float intensidad = perfectHit ? 2.0f : 1.5f;
        ColorRGBA colorMezclado = colorBaseObjetoCentral.add(colorFlecha.mult(intensidad));
        colorMezclado.a = 1.0f;
        
        materialObjetoCentral.setColor("Color", colorMezclado);
        
        // Efecto de escala
        if (perfectHit) {
            objetoCentral.setLocalScale(1.2f);
            
            // Restaurar después de 0.1s
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    app.enqueue(() -> {
                        objetoCentral.setLocalScale(1.0f);
                        return null;
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * ⭐ NUEVO: Activa brillo especial cuando se presiona ESPACIO
     */
    public void activarBrilloEspacio() {
        brillandoEspacio = true;
        tiempoBrilloEspacio = 0f;
    }
    
    // ==================== BARRA DE VIDA ====================
    
    private void crearBarraVida() {
        float anchoTotal = 400f;
        float altoTotal = 30f;
        float margen = 20f;
        
        // Fondo de la barra
        Material matFondo = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matFondo.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f));
        matFondo.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        barraVidaFondo = new Geometry("BarraVidaFondo", new Quad(anchoTotal, altoTotal));
        barraVidaFondo.setMaterial(matFondo);
        barraVidaFondo.setLocalTranslation(margen, alto - altoTotal - margen, 2f);
        
        // Barra de vida
        materialVida = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        materialVida.setColor("Color", new ColorRGBA(0f, 1f, 0.3f, 0.9f));
        materialVida.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        barraVida = new Geometry("BarraVida", new Quad(anchoTotal, altoTotal));
        barraVida.setMaterial(materialVida);
        barraVida.setLocalTranslation(margen, alto - altoTotal - margen, 2.1f);
        
        uiRootNode.attachChild(barraVidaFondo);
        uiRootNode.attachChild(barraVida);
    }
    
    public void actualizarVida(int vida) {
        if (barraVida == null) return;
        
        float porcentaje = Math.max(0, Math.min(100, vida)) / 100f;
        float anchoTotal = 400f;
        float nuevoAncho = anchoTotal * porcentaje;
        
        barraVida.setMesh(new Quad(nuevoAncho, 30f));
        
        // Cambiar color según vida
        ColorRGBA color;
        if (vida > 60) {
            color = new ColorRGBA(0f, 1f, 0.3f, 0.9f); // Verde
        } else if (vida > 30) {
            color = new ColorRGBA(1f, 0.8f, 0f, 0.9f); // Amarillo
        } else {
            color = new ColorRGBA(1f, 0.2f, 0f, 0.9f); // Rojo
        }
        
        materialVida.setColor("Color", color);
    }
    
    // ==================== TEXTOS UI ====================
    
    private void crearTextoScore() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        textoScore = new BitmapText(font);
        textoScore.setSize(font.getCharSet().getRenderedSize() * 2);
        textoScore.setColor(ColorRGBA.White);
        textoScore.setText("Score: 0");
        textoScore.setLocalTranslation(ancho - 250, alto - 70, 3f);
        uiRootNode.attachChild(textoScore);
    }
    
    public void actualizarScore(int score) {
        if (textoScore != null) {
            textoScore.setText("Score: " + score);
        }
    }
    
    private void crearTextoCombo() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        textoCombo = new BitmapText(font);
        textoCombo.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        textoCombo.setColor(new ColorRGBA(1f, 0.8f, 0f, 1f));
        textoCombo.setText("Combo: 0");
        textoCombo.setLocalTranslation(20, alto - 100, 3f);
        uiRootNode.attachChild(textoCombo);
    }
    
    public void actualizarCombo(int combo) {
        if (textoCombo != null) {
            if (combo > 0) {
                textoCombo.setText("Combo: x" + combo);
                textoCombo.setColor(new ColorRGBA(1f, 0.8f, 0f, 1f));
            } else {
                textoCombo.setText("");
            }
        }
    }
    
    private void crearTextoCancion() {
        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        textoCancion = new BitmapText(font);
        textoCancion.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        textoCancion.setColor(ColorRGBA.White);
        textoCancion.setText("");
        textoCancion.setLocalTranslation(ancho / 2 - 100, alto - 30, 3f);
        uiRootNode.attachChild(textoCancion);
    }
    
    public void mostrarCancion(String nombre) {
        if (textoCancion != null) {
            textoCancion.setText("♪ " + nombre);
        }
    }
    
    // ==================== FEEDBACK VISUAL ====================
    
    public void mostrarFeedback(String texto, ColorRGBA color) {
        if (textoFeedback == null) {
            BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
            textoFeedback = new BitmapText(font);
            textoFeedback.setSize(font.getCharSet().getRenderedSize() * 3);
            uiRootNode.attachChild(textoFeedback);
        }
        
        textoFeedback.setText(texto);
        textoFeedback.setColor(color);
        textoFeedback.setLocalTranslation(
            ancho / 2 - textoFeedback.getLineWidth() / 2,
            alto / 2 + 150,
            4f
        );
        
        mostrandoFeedback = true;
        tiempoFeedback = 0f;
    }
    
    public void actualizarFeedback(float tpf) {
        if (mostrandoFeedback && textoFeedback != null) {
            tiempoFeedback += tpf;
            
            if (tiempoFeedback > 0.8f) {
                textoFeedback.setText("");
                mostrandoFeedback = false;
            } else {
                float alpha = 1.0f - (tiempoFeedback / 0.8f);
                ColorRGBA color = textoFeedback.getColor();
                color.a = alpha;
                textoFeedback.setColor(color);
            }
        }
    }
    
    // ==================== BOTÓN PAUSA ====================
    
    private void crearBotonPausa() {
        // ⭐ FIX: Crear botón de pausa con geometría simple en lugar de Picture
        float tamanoPausa = 40f;
        
        Material matPausa = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matPausa.setColor("Color", new ColorRGBA(0.9f, 0.9f, 0.9f, 0.8f));
        matPausa.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        Geometry botonPausaGeom = new Geometry("BotonPausa", new Quad(tamanoPausa, tamanoPausa));
        botonPausaGeom.setMaterial(matPausa);
        botonPausaGeom.setLocalTranslation(ancho - 60, alto - 60, 3f);
        
        uiRootNode.attachChild(botonPausaGeom);
        
        // Nota: El Picture original causaba error por falta de textura
        // Si quieres usar una textura personalizada, descomenta esto:
        /*
        botonPausa = new Picture("BotonPausa");
        botonPausa.setImage(app.getAssetManager(), "Textures/pausa_icon.png", true);
        botonPausa.setWidth(40);
        botonPausa.setHeight(40);
        botonPausa.setPosition(ancho - 60, alto - 60);
        uiRootNode.attachChild(botonPausa);
        */
    }
    
    // ==================== UTILIDADES ====================
    
    public void ocultarTemporalmente() {
        if (uiRootNode != null) {
            uiRootNode.removeFromParent();
        }
    }
    
    public void mostrarNuevamente() {
        if (uiRootNode != null && uiRootNode.getParent() == null) {
            guiNode.attachChild(uiRootNode);
        }
    }
    
    public void limpiar() {
        if (uiRootNode != null) {
            uiRootNode.removeFromParent();
            uiRootNode = null;
        }
    }
}