/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import java.nio.ByteBuffer;
import com.jme3.texture.Texture2D;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * Generador de Flechas Procedurales - VERSIÓN FUSIONADA FINAL
 * 
 * ✅ 100% funcionalidad del documento (zonas, detección, colores)
 * ✅ Solo mejoras visuales del código externo (forma de flecha más grande)
 * 
 * IMPORTANTE: Este generador SOLO crea las texturas visuales.
 * La lógica de gameplay (puntos, vida, detección) está en:
 * - GameplayAppState (input, puntuación)
 * - FlechaControl (movimiento, timing)
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaProceduralGenerator {
    
    private final AssetManager assetManager;
    private static final int TEXTURE_SIZE = 128;
    
    // ==================== COLORES (100% DEL DOCUMENTO) ====================
    
    public static final ColorRGBA COLOR_ROJO = new ColorRGBA(1.0f, 0.2f, 0.2f, 1.0f);
    public static final ColorRGBA COLOR_AZUL = new ColorRGBA(0.2f, 0.5f, 1.0f, 1.0f);
    public static final ColorRGBA COLOR_VERDE = new ColorRGBA(0.2f, 1.0f, 0.3f, 1.0f);
    public static final ColorRGBA COLOR_DORADO = new ColorRGBA(1.0f, 0.85f, 0.1f, 1.0f);
    public static final ColorRGBA COLOR_LUNA = new ColorRGBA(0.7f, 0.4f, 1.0f, 1.0f);
    
    public static final ColorRGBA BORDE_ROJO = new ColorRGBA(0.6f, 0.1f, 0.1f, 1.0f);
    public static final ColorRGBA BORDE_AZUL = new ColorRGBA(0.1f, 0.2f, 0.6f, 1.0f);
    public static final ColorRGBA BORDE_VERDE = new ColorRGBA(0.1f, 0.6f, 0.15f, 1.0f);
    public static final ColorRGBA BORDE_DORADO = new ColorRGBA(0.8f, 0.6f, 0.0f, 1.0f);
    public static final ColorRGBA BORDE_LUNA = new ColorRGBA(0.4f, 0.2f, 0.6f, 1.0f);
    
    public static final ColorRGBA ZONA_BORDE_DEFAULT = new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f);
    public static final ColorRGBA ZONA_FONDO_DEFAULT = new ColorRGBA(0.1f, 0.1f, 0.1f, 0.8f);
    public static final ColorRGBA ZONA_BORDE_ESPACIO = new ColorRGBA(0.8f, 0.6f, 1.0f, 1.0f);
    public static final ColorRGBA ZONA_FONDO_ESPACIO = new ColorRGBA(0.3f, 0.1f, 0.4f, 0.8f);
    
    public FlechaProceduralGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
        System.out.println("🎨 Generador de Flechas Procedurales inicializado");
    }
    
    // ==================== CREACIÓN DE FLECHAS ====================
    
    /**
     * Crea una geometría de flecha con textura procedural y rotación correcta
     * DEL DOCUMENTO + fix de rotación
     */
    public Geometry crearFlecha(TipoFlecha tipo, Direccion direccion) {
        ColorRGBA colorPrincipal = obtenerColorPrincipal(tipo);
        ColorRGBA colorBorde = obtenerColorBorde(tipo);
        
        Texture textura = crearTexturaFlecha(colorPrincipal, colorBorde);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        Mesh mesh = crearMeshCuadrado(70f);
        Geometry geom = new Geometry("Flecha_" + tipo + "_" + direccion, mesh);
        geom.setMaterial(mat);
        
        // ⭐ FIX: Rotación corregida (del documento pero arreglada)
        float rotacionZ = 0;
        switch (direccion) {
            case IZQUIERDA:
                rotacionZ = FastMath.HALF_PI;
                break;
            case DERECHA:
                rotacionZ = -FastMath.HALF_PI;
                break;
            case ABAJO:
                rotacionZ = FastMath.PI;
                break;
            case ARRIBA:
                rotacionZ = 0; // ⭐ CORREGIDO: antes era -FastMath.PI
                break;
            case ESPACIO:
            default:
                rotacionZ = 0;
                break;
        }
        
        geom.setLocalRotation(new Quaternion().fromAngleAxis(rotacionZ, Vector3f.UNIT_Z));
        
        return geom;
    }
    
    /**
     * Crea textura procedural de flecha
     * DEL DOCUMENTO - sin cambios en la firma
     */
    public Texture crearTexturaFlecha(ColorRGBA colorPrincipal, ColorRGBA colorBorde) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                ColorRGBA pixel = calcularPixelFlecha(x, y, colorPrincipal, colorBorde);
                
                buffer.put((byte) (pixel.r * 255));
                buffer.put((byte) (pixel.g * 255));
                buffer.put((byte) (pixel.b * 255));
                buffer.put((byte) (pixel.a * 255));
            }
        }
        
        buffer.flip();
        
        Image image = new Image(
            Image.Format.RGBA8,
            TEXTURE_SIZE,
            TEXTURE_SIZE,
            buffer,
            ColorSpace.sRGB
        );
        
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        texture.setWrap(Texture.WrapMode.Repeat);
        
        return texture;
    }
    
    // ==================== CÁLCULO DE PÍXELES - MEJORADO ====================
    
    /**
     * ⭐ MEJORADO: Forma de flecha más grande y visible
     * Mantiene la lógica del documento, solo ajusta proporciones
     */
    private ColorRGBA calcularPixelFlecha(int x, int y, ColorRGBA colorPrincipal, ColorRGBA colorBorde) {
        float nx = (float) x / TEXTURE_SIZE;
        float ny = (float) y / TEXTURE_SIZE;
        float cx = nx - 0.5f;
        float cy = ny - 0.5f;
        
        // ========== FORMA DE FLECHA (PROPORCIONES MEJORADAS) ==========
        // La flecha apunta hacia ARRIBA
        
        // PUNTA - más grande: 0.05 a 0.45 (antes 0.1 a 0.4)
        boolean enPunta = cy > 0.05f && cy < 0.45f && Math.abs(cx) < (0.45f - cy);
        
        // CUERPO - más ancho: 0.14 (antes 0.15)
        boolean enCuerpo = cy >= -0.30f && cy <= 0.05f && Math.abs(cx) < 0.14f;
        
        // BASE - más pronunciada: 0.24 (antes 0.25)
        boolean enBase = cy >= -0.42f && cy < -0.30f && Math.abs(cx) < 0.24f;
        
        boolean dentroFlecha = enPunta || enCuerpo || enBase;
        
        if (!dentroFlecha) {
            return new ColorRGBA(0, 0, 0, 0);
        }
        
        // ========== BORDES (DEL DOCUMENTO) ==========
        float distanciaBorde = calcularDistanciaBorde(cx, cy);
        
        if (distanciaBorde < 0.2f) {
            return colorBorde;
        } else if (distanciaBorde < 0.4f) {
            float t = (distanciaBorde - 0.2f) / 0.2f;
            return interpolarColor(colorBorde, colorPrincipal, t);
        } else {
            float brillo = 2.0f + (0.4f * (0.6f - Math.abs(cy)));
            return colorPrincipal.mult(brillo);
        }
    }
    
    /**
     * Calcula distancia al borde de la flecha
     * DEL DOCUMENTO - ajustado a nuevas proporciones
     */
    private float calcularDistanciaBorde(float cx, float cy) {
        float distMin = Float.MAX_VALUE;
        
        // Punta
        if (cy > 0.05f) {
            float bordeIzq = Math.abs(cx - (cy - 0.45f));
            float bordeDer = Math.abs(cx - (0.45f - cy));
            distMin = Math.min(distMin, Math.min(bordeIzq, bordeDer));
        }
        
        // Cuerpo
        if (cy >= -0.30f && cy <= 0.05f) {
            distMin = Math.min(distMin, Math.abs(Math.abs(cx) - 0.14f));
        }
        
        // Base
        if (cy >= -0.42f && cy < -0.30f) {
            distMin = Math.min(distMin, Math.abs(Math.abs(cx) - 0.24f));
        }
        
        return distMin;
    }
    
    /**
     * Interpola entre dos colores
     * DEL DOCUMENTO - sin cambios
     */
    private ColorRGBA interpolarColor(ColorRGBA c1, ColorRGBA c2, float t) {
        return new ColorRGBA(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t,
            c1.a + (c2.a - c1.a) * t
        );
    }
    
    // ==================== MESH (DEL DOCUMENTO) ====================
    
    private Mesh crearMeshCuadrado(float tamaño) {
        Mesh mesh = new Mesh();
        float half = tamaño / 2f;
        
        float[] vertices = {
            -half, -half, 0,
             half, -half, 0,
             half,  half, 0,
            -half,  half, 0
        };
        
        float[] texCoords = {
            0, 0,
            1, 0,
            1, 1,
            0, 1
        };
        
        int[] indices = {
            0, 1, 2,
            0, 2, 3
        };
        
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();
        
        return mesh;
    }
    
    // ==================== ZONAS DE IMPACTO (100% DEL DOCUMENTO) ====================
    
    /**
     * Crea textura para zona de impacto direccional
     * DEL DOCUMENTO - sin cambios
     */
    public Texture crearTexturaZonaFlecha(Direccion direccion, ColorRGBA colorBorde, ColorRGBA colorFondo) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                ColorRGBA pixel = calcularPixelZonaFlecha(x, y, direccion, colorBorde, colorFondo);
                buffer.put((byte) (pixel.r * 255));
                buffer.put((byte) (pixel.g * 255));
                buffer.put((byte) (pixel.b * 255));
                buffer.put((byte) (pixel.a * 255));
            }
        }
        buffer.flip();
        
        Image image = new Image(Image.Format.RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, buffer, ColorSpace.sRGB);
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        texture.setWrap(Texture.WrapMode.Repeat);
        return texture;
    }
    
    /**
     * Crea textura para zona de impacto de ESPACIO
     * DEL DOCUMENTO - sin cambios
     */
    public Texture crearTexturaZonaEspacio(ColorRGBA colorBorde, ColorRGBA colorFondo) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                ColorRGBA pixel = calcularPixelZonaEspacio(x, y, colorBorde, colorFondo);
                buffer.put((byte) (pixel.r * 255));
                buffer.put((byte) (pixel.g * 255));
                buffer.put((byte) (pixel.b * 255));
                buffer.put((byte) (pixel.a * 255));
            }
        }
        buffer.flip();
        
        Image image = new Image(Image.Format.RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, buffer, ColorSpace.sRGB);
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        texture.setWrap(Texture.WrapMode.Repeat);
        return texture;
    }
    
    /**
     * Calcula píxeles para zona direccional
     * DEL DOCUMENTO - sin cambios
     */
    private ColorRGBA calcularPixelZonaFlecha(int x, int y, Direccion direccion, ColorRGBA colorBorde, ColorRGBA colorFondo) {
        float nx = (float) x / TEXTURE_SIZE;
        float ny = (float) y / TEXTURE_SIZE;
        float cx = nx - 0.5f;
        float cy = ny - 0.5f;

        float rotacion = 0;
        if (direccion == Direccion.DERECHA) rotacion = -FastMath.HALF_PI;
        else if (direccion == Direccion.ABAJO) rotacion = -FastMath.PI;
        else if (direccion == Direccion.IZQUIERDA) rotacion = FastMath.HALF_PI;

        float cos = FastMath.cos(rotacion);
        float sin = FastMath.sin(rotacion);
        float rotatedCx = cx * cos - cy * sin;
        float rotatedCy = cx * sin + cy * cos;

        boolean enPunta = rotatedCy > 0.1f && Math.abs(rotatedCx) < (0.5f - rotatedCy);
        boolean enCuerpo = rotatedCy >= -0.3f && rotatedCy <= 0.1f && Math.abs(rotatedCx) < 0.15f;
        boolean enBase = rotatedCy >= -0.4f && rotatedCy < -0.3f && Math.abs(rotatedCx) < 0.25f;
        
        boolean dentroFlecha = enPunta || enCuerpo || enBase;

        if (!dentroFlecha) {
            return new ColorRGBA(0, 0, 0, 0);
        }
        
        final float GROSOR_BORDE_ZONA = 0.04f;
        final float ANCHO_TRANSICION_ZONA = 0.02f;

        float distanciaBorde = calcularDistanciaBorde(rotatedCx, rotatedCy);
        
        if (distanciaBorde < GROSOR_BORDE_ZONA) {
            return colorBorde;
        } else if (distanciaBorde < (GROSOR_BORDE_ZONA + ANCHO_TRANSICION_ZONA)) {
            float t = (distanciaBorde - GROSOR_BORDE_ZONA) / ANCHO_TRANSICION_ZONA;
            return interpolarColor(colorBorde, colorFondo, t);
        } else {
            return colorFondo;
        }
    }

    /**
     * Calcula píxeles para zona ESPACIO
     * DEL DOCUMENTO - sin cambios
     */
    private ColorRGBA calcularPixelZonaEspacio(int x, int y, ColorRGBA colorBorde, ColorRGBA colorFondo) {
        float nx = (float) x / TEXTURE_SIZE;
        float ny = (float) y / TEXTURE_SIZE;
        float cx = nx - 0.5f;
        float cy = ny - 0.5f;

        final float radioEsquina = 0.15f;
        final float anchoInterior = 0.3f;
        final float altoInterior = 0.3f;

        boolean dentroForma = false;
        
        if (Math.abs(cx) <= (anchoInterior - radioEsquina) && Math.abs(cy) <= altoInterior) {
            dentroForma = true;
        } else if (Math.abs(cy) <= (altoInterior - radioEsquina) && Math.abs(cx) <= anchoInterior) {
            dentroForma = true;
        } else {
            float dx = Math.abs(cx) - (anchoInterior - radioEsquina);
            float dy = Math.abs(cy) - (altoInterior - radioEsquina);
            if (dx > 0 && dy > 0 && (dx*dx + dy*dy) <= (radioEsquina*radioEsquina)) {
                dentroForma = true;
            }
        }
        
        if (!dentroForma) {
            return new ColorRGBA(0, 0, 0, 0);
        }

        float distBordeEspacio = Float.MAX_VALUE;
        
        distBordeEspacio = Math.min(distBordeEspacio, Math.abs(Math.abs(cx) - anchoInterior));
        distBordeEspacio = Math.min(distBordeEspacio, Math.abs(Math.abs(cy) - altoInterior));
        
        float dx = Math.abs(cx) - (anchoInterior - radioEsquina);
        float dy = Math.abs(cy) - (altoInterior - radioEsquina);
        if (dx > 0 && dy > 0) {
            distBordeEspacio = Math.min(distBordeEspacio, FastMath.sqrt(dx*dx + dy*dy) - radioEsquina);
        }

        final float GROSOR_BORDE_ZONA = 0.04f;
        final float ANCHO_TRANSICION_ZONA = 0.02f;

        if (distBordeEspacio < GROSOR_BORDE_ZONA) {
            return colorBorde;
        } else if (distBordeEspacio < (GROSOR_BORDE_ZONA + ANCHO_TRANSICION_ZONA)) {
            float t = (distBordeEspacio - GROSOR_BORDE_ZONA) / ANCHO_TRANSICION_ZONA;
            return interpolarColor(colorBorde, colorFondo, t);
        } else {
            return colorFondo;
        }
    }
    
    // ==================== COLORES (DEL DOCUMENTO) ====================
    
    private ColorRGBA obtenerColorPrincipal(TipoFlecha tipo) {
        switch (tipo) {
            case NORMAL: return COLOR_AZUL;
            case RAPIDA: return COLOR_VERDE;
            case DORADA: return COLOR_DORADO;
            case LUNA: return COLOR_LUNA;
            default: return ColorRGBA.White;
        }
    }
    
    private ColorRGBA obtenerColorBorde(TipoFlecha tipo) {
        switch (tipo) {
            case NORMAL: return BORDE_AZUL;
            case RAPIDA: return BORDE_VERDE;
            case DORADA: return BORDE_DORADO;
            case LUNA: return BORDE_LUNA;
            default: return ColorRGBA.Black;
        }
    }
    
    public ColorRGBA obtenerColorPorDireccion(Direccion direccion) {
        switch (direccion) {
            case IZQUIERDA:
            case DERECHA:
                return COLOR_ROJO;
            case ARRIBA:
            case ABAJO:
                return COLOR_AZUL;
            case ESPACIO:
                return COLOR_LUNA;
            default:
                return ColorRGBA.White;
        }
    }
    
    public ColorRGBA obtenerBordePorDireccion(Direccion direccion) {
        switch (direccion) {
            case IZQUIERDA:
            case DERECHA:
                return BORDE_ROJO;
            case ARRIBA:
            case ABAJO:
                return BORDE_AZUL;
            case ESPACIO:
                return BORDE_LUNA;
            default:
                return ColorRGBA.Black;
        }
    }
    
    // ==================== UTILIDADES (DEL DOCUMENTO) ====================
    
    public Material crearMaterialColor(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        return mat;
    }
    
    public void generarTodasLasFlechas() {
        System.out.println("\n🎨 Generando todas las flechas procedurales...");
        
        for (TipoFlecha tipo : TipoFlecha.values()) {
            for (Direccion dir : Direccion.values()) {
                Geometry flecha = crearFlecha(tipo, dir);
                System.out.println("  ✓ Creada: " + tipo + " " + dir);
            }
        }
        
        System.out.println("✅ Todas las flechas generadas exitosamente");
    }
}