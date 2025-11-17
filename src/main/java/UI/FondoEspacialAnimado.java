/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fondo espacial animado con estrellas flotantes
 * Las estrellas se mueven lentamente y parpadean
 */
public class FondoEspacialAnimado {
    
    private final Application app;
    private final Node nodoFondo;
    private final List<EstrellaFlotante> estrellas;
    private Geometry fondoDegradado;
    
    private float anchoVentana;
    private float altoVentana;
    
    // Configuración
    private static final int CANTIDAD_ESTRELLAS = 200;
    private static final float VELOCIDAD_BASE = 15f;
    
    public FondoEspacialAnimado(Application app) {
        this.app = app;
        this.nodoFondo = new Node("FondoEspacialAnimado");
        this.estrellas = new ArrayList<>();
        this.anchoVentana = app.getCamera().getWidth();
        this.altoVentana = app.getCamera().getHeight();
        
        inicializar();
    }
    
    private void inicializar() {
        // 1. Crear fondo degradado
        crearFondoDegradado();
        
        // 2. Crear estrellas flotantes
        crearEstrellas();
        
        System.out.println("✓ Fondo espacial animado creado");
        System.out.println("  - Estrellas: " + CANTIDAD_ESTRELLAS);
    }
    
    // ==================== FONDO DEGRADADO ====================
    
    private void crearFondoDegradado() {
        // Crear textura de degradado morado
        Texture2D textura = crearTexturaDegradado(
            (int)anchoVentana, 
            (int)altoVentana,
            new ColorRGBA(129f/255f, 40f/255f, 224f/255f, 1f), // Morado arriba
            new ColorRGBA(25f/255f, 0f/255f, 51f/255f, 1f)      // Morado oscuro abajo
        );
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        
        fondoDegradado = new Geometry("FondoDegradado", new Quad(anchoVentana, altoVentana));
        fondoDegradado.setMaterial(mat);
        fondoDegradado.setLocalTranslation(0, 0, -10);
        
        nodoFondo.attachChild(fondoDegradado);
    }
    
    private Texture2D crearTexturaDegradado(int ancho, int alto, ColorRGBA colorSuperior, ColorRGBA colorInferior) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(ancho * alto * 4);
        
        for (int y = 0; y < alto; y++) {
            float t = (float)y / alto;
            ColorRGBA color = interpolarColor(colorSuperior, colorInferior, t);
            
            for (int x = 0; x < ancho; x++) {
                buffer.put((byte)(color.r * 255));
                buffer.put((byte)(color.g * 255));
                buffer.put((byte)(color.b * 255));
                buffer.put((byte)(color.a * 255));
            }
        }
        
        buffer.flip();
        
        Image image = new Image(
            Image.Format.RGBA8,
            ancho, alto,
            buffer,
            ColorSpace.sRGB
        );
        
        Texture2D textura = new Texture2D(image);
        textura.setMagFilter(Texture.MagFilter.Bilinear);
        textura.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        
        return textura;
    }
    
    private ColorRGBA interpolarColor(ColorRGBA c1, ColorRGBA c2, float t) {
        return new ColorRGBA(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t,
            c1.a + (c2.a - c1.a) * t
        );
    }
    
    // ==================== ESTRELLAS FLOTANTES ====================
    
    private void crearEstrellas() {
        Random random = new Random(12345);
        
        for (int i = 0; i < CANTIDAD_ESTRELLAS; i++) {
            // Posición aleatoria
            float x = random.nextFloat() * anchoVentana;
            float y = random.nextFloat() * altoVentana;
            
            // Tamaño aleatorio (estrellas pequeñas)
            float tamano = 1f + random.nextFloat() * 3f;
            
            // Velocidad aleatoria
            float velocidadX = (random.nextFloat() - 0.5f) * VELOCIDAD_BASE;
            float velocidadY = (random.nextFloat() - 0.5f) * VELOCIDAD_BASE;
            
            // Brillo aleatorio
            float brillo = 0.3f + random.nextFloat() * 0.7f;
            
            // Fase de parpadeo aleatoria
            float faseParpadeo = random.nextFloat() * FastMath.TWO_PI;
            
            // Crear estrella
            EstrellaFlotante estrella = new EstrellaFlotante(
                x, y, tamano, 
                velocidadX, velocidadY, 
                brillo, faseParpadeo
            );
            
            // Crear geometría
            Geometry geomEstrella = crearGeometriaEstrella(estrella);
            nodoFondo.attachChild(geomEstrella);
            
            estrella.geometria = geomEstrella;
            estrellas.add(estrella);
        }
    }
    
    private Geometry crearGeometriaEstrella(EstrellaFlotante estrella) {
        Quad quad = new Quad(estrella.tamano, estrella.tamano);
        Geometry geom = new Geometry("Estrella", quad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, estrella.brillo));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        geom.setMaterial(mat);
        geom.setLocalTranslation(estrella.x, estrella.y, -5);
        
        return geom;
    }
    
    // ==================== ACTUALIZACIÓN ====================
    
    /**
     * Actualiza la animación de las estrellas
     * Debe llamarse desde el update() del AppState
     */
    public void actualizar(float tpf) {
        for (EstrellaFlotante estrella : estrellas) {
            // Mover estrella
            estrella.x += estrella.velocidadX * tpf;
            estrella.y += estrella.velocidadY * tpf;
            
            // Wraparound (cuando sale por un borde, reaparece por el opuesto)
            if (estrella.x < -estrella.tamano) {
                estrella.x = anchoVentana;
            } else if (estrella.x > anchoVentana) {
                estrella.x = -estrella.tamano;
            }
            
            if (estrella.y < -estrella.tamano) {
                estrella.y = altoVentana;
            } else if (estrella.y > altoVentana) {
                estrella.y = -estrella.tamano;
            }
            
            // Actualizar posición de la geometría
            estrella.geometria.setLocalTranslation(estrella.x, estrella.y, -5);
            
            // Parpadeo suave
            estrella.tiempoParpadeo += tpf * 2f;
            float intensidad = estrella.brillo * (0.7f + 0.3f * FastMath.sin(estrella.tiempoParpadeo + estrella.faseParpadeo));
            
            Material mat = estrella.geometria.getMaterial();
            mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, intensidad));
        }
    }
    
    // ==================== GETTERS ====================
    
    public Node getNodo() {
        return nodoFondo;
    }
    
    public void limpiar() {
        nodoFondo.detachAllChildren();
        estrellas.clear();
        System.out.println("✓ Fondo espacial limpiado");
    }
    
    // ==================== CLASE INTERNA: ESTRELLA FLOTANTE ====================
    
    private static class EstrellaFlotante {
        float x, y;
        float tamano;
        float velocidadX, velocidadY;
        float brillo;
        float faseParpadeo;
        float tiempoParpadeo;
        Geometry geometria;
        
        EstrellaFlotante(float x, float y, float tamano, 
                        float velocidadX, float velocidadY, 
                        float brillo, float faseParpadeo) {
            this.x = x;
            this.y = y;
            this.tamano = tamano;
            this.velocidadX = velocidadX;
            this.velocidadY = velocidadY;
            this.brillo = brillo;
            this.faseParpadeo = faseParpadeo;
            this.tiempoParpadeo = 0f;
        }
    }
}