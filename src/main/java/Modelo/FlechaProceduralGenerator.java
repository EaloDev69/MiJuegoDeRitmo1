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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * Generador de Flechas Procedurales
 * Crea flechas dibujadas en código sin necesidad de imágenes externas
 * 
 * TIPOS DE FLECHAS:
 * 1. ROJA (Izquierda/Derecha)
 * 2. AZUL (Arriba/Abajo)
 * 3. VERDE (Flechas rápidas)
 * 4. DORADA (Flechas especiales/invertidas)
 * 5. LUNA (Especial - Violeta/Morado)
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaProceduralGenerator {
    
    private final AssetManager assetManager;
    
    // Tamaños
    private static final int TEXTURE_SIZE = 128; // 128x128 pixels
    
    // ==================== COLORES ESPECÍFICOS ====================
    
    // Archivo: Modelo.FlechaProceduralGenerator.java
    
    // ... tu código de importaciones y variables ...
    
    // ==================== COLORES ESPECÍFICOS ====================
    
    // Colores principales de flechas
    public static final ColorRGBA COLOR_ROJO = new ColorRGBA(1.0f, 0.2f, 0.2f, 1.0f);
    public static final ColorRGBA COLOR_AZUL = new ColorRGBA(0.2f, 0.5f, 1.0f, 1.0f);
    public static final ColorRGBA COLOR_VERDE = new ColorRGBA(0.2f, 1.0f, 0.3f, 1.0f);
    public static final ColorRGBA COLOR_DORADO = new ColorRGBA(1.0f, 0.85f, 0.1f, 1.0f);
    public static final ColorRGBA COLOR_LUNA = new ColorRGBA(0.7f, 0.4f, 1.0f, 1.0f); // Violeta
    
    // Colores de borde de flechas (más oscuros)
    public static final ColorRGBA BORDE_ROJO = new ColorRGBA(0.6f, 0.1f, 0.1f, 1.0f);
    public static final ColorRGBA BORDE_AZUL = new ColorRGBA(0.1f, 0.2f, 0.6f, 1.0f);
    public static final ColorRGBA BORDE_VERDE = new ColorRGBA(0.1f, 0.6f, 0.15f, 1.0f);
    public static final ColorRGBA BORDE_DORADO = new ColorRGBA(0.8f, 0.6f, 0.0f, 1.0f);
    public static final ColorRGBA BORDE_LUNA = new ColorRGBA(0.4f, 0.2f, 0.6f, 1.0f);
    
    
    // === COLORES PARA LAS ZONAS DE IMPACTO (Añadidos) ===
    
    public static final ColorRGBA ZONA_BORDE_DEFAULT = new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f); // Gris oscuro
    public static final ColorRGBA ZONA_FONDO_DEFAULT = new ColorRGBA(0.1f, 0.1f, 0.1f, 0.8f); // Negro semitransparente
    
    public static final ColorRGBA ZONA_BORDE_ESPACIO = new ColorRGBA(0.8f, 0.6f, 1.0f, 1.0f); // Borde violeta
    public static final ColorRGBA ZONA_FONDO_ESPACIO = new ColorRGBA(0.3f, 0.1f, 0.4f, 0.8f); // Fondo violeta oscuro
    
    // ==================== CONSTRUCTOR ====================
    
    // ... el resto de tu código ...

    // Colores principales

    // ==================== CONSTRUCTOR ====================
    
    public FlechaProceduralGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
        System.out.println("\n🎨 Generador de Flechas Procedurales inicializado");
    }
    
    // ==================== MÉTODOS PÚBLICOS DE CREACIÓN ====================
    
    /**
     * Crea una geometría de flecha del color especificado
     */
    // Archivo: Modelo.FlechaProceduralGenerator.java
// ...

/**
 * Crea una geometría de flecha del color especificado y la rota
 * según la dirección.
 */
public Geometry crearFlecha(TipoFlecha tipo, Direccion direccion) {
    ColorRGBA colorPrincipal = obtenerColorPrincipal(tipo);
    ColorRGBA colorBorde = obtenerColorBorde(tipo);
    
    // Crear textura procedural (la flecha base apunta ARRIBA)
    Texture textura = crearTexturaFlecha(colorPrincipal, colorBorde);
    
    // Crear material
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", textura);
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    
    // Crear geometría
    Mesh mesh = crearMeshCuadrado(70f); // 70x70 pixels
    Geometry geom = new Geometry("Flecha_" + tipo + "_" + direccion, mesh);
    geom.setMaterial(mat);
    
    // 💥 Lógica de ROTACIÓN AGREGADA 💥
    float rotacionZ = 0;
    
    switch (direccion) {
        case IZQUIERDA:
            rotacionZ = FastMath.HALF_PI; // 90 grados
            break;
        case DERECHA:
            rotacionZ = -FastMath.HALF_PI; // -90 grados
            break;
        case ABAJO:
            rotacionZ = FastMath.PI; // 180 grados (también puede ser -FastMath.PI)
            break;
        case ARRIBA:
            rotacionZ = -FastMath.PI; // 0 grados (apunta hacia arriba)
            break;
        case ESPACIO:
        default:
            rotacionZ = 0; // 0 grados (sin rotación)
            break;
    }
    
    // Aplicar la rotación alrededor del eje Z
    geom.setLocalRotation(new Quaternion().fromAngleAxis(rotacionZ, Vector3f.UNIT_Z));

    return geom;
}
    
    /**
     * Crea una textura procedural de flecha
     */
    public Texture crearTexturaFlecha(ColorRGBA colorPrincipal, ColorRGBA colorBorde) {
        // Crear buffer de imagen
        ByteBuffer buffer = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        
        // Dibujar flecha pixel por pixel
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                ColorRGBA pixel = calcularPixelFlecha(x, y, colorPrincipal, colorBorde);
                
                // Escribir RGBA
                buffer.put((byte) (pixel.r * 255));
                buffer.put((byte) (pixel.g * 255));
                buffer.put((byte) (pixel.b * 255));
                buffer.put((byte) (pixel.a * 255));
            }
        }
        
        buffer.flip();
        
        // Crear imagen
        Image image = new Image(
            Image.Format.RGBA8,
            TEXTURE_SIZE,
            TEXTURE_SIZE,
            buffer,
            ColorSpace.sRGB
        );
        
        // Crear textura (método correcto para jME 3.6+)
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        texture.setWrap(Texture.WrapMode.Repeat);
        
        return texture;
    }
    
    // ==================== CÁLCULO DE PÍXELES ====================
    // Archivo: Modelo.FlechaProceduralGenerator.java
    // ...
    
    // ==================== MÉTODOS PÚBLICOS DE CREACIÓN DE ZONAS ====================
    
    /**
     * Crea una textura procedural para una zona de impacto de flecha (outline)
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
     * Crea una textura procedural para la zona de impacto de ESPACIO (cuadrado redondeado)
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

    // ==================== CÁLCULO DE PÍXELES PARA ZONAS ====================

    private ColorRGBA calcularPixelZonaFlecha(int x, int y, Direccion direccion, ColorRGBA colorBorde, ColorRGBA colorFondo) {
        float nx = (float) x / TEXTURE_SIZE;
        float ny = (float) y / TEXTURE_SIZE;
        float cx = nx - 0.5f;
        float cy = ny - 0.5f;

        float rotacion = 0; // Por defecto ARRIBA
        if (direccion == Direccion.DERECHA) rotacion = -FastMath.HALF_PI;
        else if (direccion == Direccion.ABAJO) rotacion = -FastMath.PI;
        else if (direccion == Direccion.IZQUIERDA) rotacion = FastMath.HALF_PI;

        // Rotar coordenadas para la dirección deseada
        float cos = FastMath.cos(rotacion);
        float sin = FastMath.sin(rotacion);
        float rotatedCx = cx * cos - cy * sin;
        float rotatedCy = cx * sin + cy * cos;

        // Reutilizar la lógica de forma de flecha, pero solo para el outline
        boolean enPunta = rotatedCy > 0.1f && Math.abs(rotatedCx) < (0.5f - rotatedCy);
        boolean enCuerpo = rotatedCy >= -0.3f && rotatedCy <= 0.1f && Math.abs(rotatedCx) < 0.15f;
        boolean enBase = rotatedCy >= -0.4f && rotatedCy < -0.3f && Math.abs(rotatedCx) < 0.25f;
        
        boolean dentroFlecha = enPunta || enCuerpo || enBase;

        if (!dentroFlecha) {
            return new ColorRGBA(0, 0, 0, 0); // Transparente fuera
        }
        
        final float GROSOR_BORDE_ZONA = 0.04f; // Mismo grosor que las flechas que caen
        final float ANCHO_TRANSICION_ZONA = 0.02f;

        float distanciaBorde = calcularDistanciaBorde(rotatedCx, rotatedCy); // Usamos la misma lógica para el borde de la forma
        
        if (distanciaBorde < GROSOR_BORDE_ZONA) {
            return colorBorde; // Borde sólido
        } else if (distanciaBorde < (GROSOR_BORDE_ZONA + ANCHO_TRANSICION_ZONA)) {
            float t = (distanciaBorde - GROSOR_BORDE_ZONA) / ANCHO_TRANSICION_ZONA;
            return interpolarColor(colorBorde, colorFondo, t); // Transición al fondo
        } else {
            return colorFondo; // Fondo interior (semitransparente)
        }
    }

    private ColorRGBA calcularPixelZonaEspacio(int x, int y, ColorRGBA colorBorde, ColorRGBA colorFondo) {
        float nx = (float) x / TEXTURE_SIZE;
        float ny = (float) y / TEXTURE_SIZE;
        float cx = nx - 0.5f;
        float cy = ny - 0.5f;

        // Cuadrado con esquinas redondeadas
        final float radioEsquina = 0.15f; // Radio para redondear
        final float anchoInterior = 0.3f; // Ancho del cuadrado interior
        final float altoInterior = 0.3f; // Alto del cuadrado interior

        boolean dentroForma = false;
        
        // Región central del cuadrado
        if (Math.abs(cx) <= (anchoInterior - radioEsquina) && Math.abs(cy) <= altoInterior) {
            dentroForma = true;
        } else if (Math.abs(cy) <= (altoInterior - radioEsquina) && Math.abs(cx) <= anchoInterior) {
            dentroForma = true;
        } else { // Esquinas
            float dx = Math.abs(cx) - (anchoInterior - radioEsquina);
            float dy = Math.abs(cy) - (altoInterior - radioEsquina);
            if (dx > 0 && dy > 0 && (dx*dx + dy*dy) <= (radioEsquina*radioEsquina)) {
                dentroForma = true;
            }
        }
        
        if (!dentroForma) {
            return new ColorRGBA(0, 0, 0, 0); // Transparente fuera
        }

        // Calcular distancia al borde para el outline
        float distBordeEspacio = Float.MAX_VALUE;
        
        // Distancia a los bordes rectos (horizontales y verticales)
        distBordeEspacio = Math.min(distBordeEspacio, Math.abs(Math.abs(cx) - anchoInterior));
        distBordeEspacio = Math.min(distBordeEspacio, Math.abs(Math.abs(cy) - altoInterior));
        
        // Distancia a las esquinas redondeadas
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
    // ... resto de la clase ...
    /**
     * Calcula el color de un píxel en la forma de flecha
     */
    private ColorRGBA calcularPixelFlecha(int x, int y, ColorRGBA colorPrincipal, ColorRGBA colorBorde) {
        // Normalizar coordenadas (0.0 a 1.0)
        float nx = (float) x / TEXTURE_SIZE;
        float ny = (float) y / TEXTURE_SIZE;
        
        // Centrar coordenadas (-0.5 a 0.5)
        float cx = nx - 0.5f;
        float cy = ny - 0.5f;
        
        // ========== FORMA DE FLECHA ==========
        // La flecha apunta hacia ARRIBA por defecto
        
        // PUNTA DE LA FLECHA (triángulo superior)
        boolean enPunta = cy > 0.1f && Math.abs(cx) < (0.5f - cy);
        
        // CUERPO DE LA FLECHA (rectángulo central)
        boolean enCuerpo = cy >= -0.3f && cy <= 0.1f && Math.abs(cx) < 0.15f;
        
        // BASE DE LA FLECHA (rectángulo inferior)
        boolean enBase = cy >= -0.4f && cy < -0.3f && Math.abs(cx) < 0.25f;
        
        boolean dentroFlecha = enPunta || enCuerpo || enBase;
        
        if (!dentroFlecha) {
            // Transparente fuera de la flecha
            return new ColorRGBA(0, 0, 0, 0);
        }
        
        // ========== CÁLCULO DE BORDES ==========
        float distanciaBorde = calcularDistanciaBorde(cx, cy);
        
        if (distanciaBorde < 0.2f) {
            // Borde
            return colorBorde;
        } else if (distanciaBorde < 0.4f) {
            // Transición suave entre borde y color principal
            float t = (distanciaBorde - 0.2f) / 0.2f;
            return interpolarColor(colorBorde, colorPrincipal, t);
        } else {
            // Color principal con brillo gradual
            float brillo = 2.0f + (0.4f * (0.6f - Math.abs(cy))); // Más brillante al centro
            return colorPrincipal.mult(brillo);
        }
    }
    
    /**
     * Calcula la distancia al borde de la flecha
     */
    private float calcularDistanciaBorde(float cx, float cy) {
        float distMin = Float.MAX_VALUE;
        
        // Distancia a bordes de la punta
        if (cy > 0.01f) {
            float bordeIzq = Math.abs(cx - (cy - 0.05f));
            float bordeDer = Math.abs(cx - (0.05f - cy));
            distMin = Math.min(distMin, Math.min(bordeIzq, bordeDer));
        }
        
        // Distancia a bordes del cuerpo
        if (cy >= -0.03f && cy <= 0.01f) {
            distMin = Math.min(distMin, Math.abs(Math.abs(cx) - 0.015f));
        }
        
        // Distancia a bordes de la base
        if (cy >= -0.04f && cy < -0.03f) {
            distMin = Math.min(distMin, Math.abs(Math.abs(cx) - 0.025f));
        }
        
        return distMin;
    }
    
    /**
     * Interpola entre dos colores
     */
    private ColorRGBA interpolarColor(ColorRGBA c1, ColorRGBA c2, float t) {
        return new ColorRGBA(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t,
            c1.a + (c2.a - c1.a) * t
        );
    }
    
    // ==================== MESH CREATION ====================
    
    /**
     * Crea un mesh cuadrado con UVs correctos
     */
    private Mesh crearMeshCuadrado(float tamaño) {
        Mesh mesh = new Mesh();
        
        float half = tamaño / 2f;
        
        // Vértices del cuadrado
        float[] vertices = {
            -half, -half, 0,  // Bottom-left
             half, -half, 0,  // Bottom-right
             half,  half, 0,  // Top-right
            -half,  half, 0   // Top-left
        };
        
        // UVs (coordenadas de textura)
        float[] texCoords = {
            0, 0,  // Bottom-left
            1, 0,  // Bottom-right
            1, 1,  // Top-right
            0, 1   // Top-left
        };
        
        // Índices (dos triángulos)
        int[] indices = {
            0, 1, 2,  // Primer triángulo
            0, 2, 3   // Segundo triángulo
        };
        
        // Configurar mesh
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();
        
        return mesh;
    }
    
    // ==================== COLORES POR TIPO ====================
    
    private ColorRGBA obtenerColorPrincipal(TipoFlecha tipo) {
        switch (tipo) {
            case NORMAL:
                return COLOR_AZUL; // Por defecto azul
            case RAPIDA:
                return COLOR_VERDE;
            case DORADA:
                return COLOR_DORADO;
            case LUNA:
                return COLOR_LUNA;
            default:
                return ColorRGBA.White;
        }
    }
    
    private ColorRGBA obtenerColorBorde(TipoFlecha tipo) {
        switch (tipo) {
            case NORMAL:
                return BORDE_AZUL;
            case RAPIDA:
                return BORDE_VERDE;
            case DORADA:
                return BORDE_DORADO;
            case LUNA:
                return BORDE_LUNA;
            default:
                return ColorRGBA.Black;
        }
    }
    
    /**
     * Obtiene color por dirección (para flechas normales)
     */
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
    
    // ==================== MÉTODOS DE UTILIDAD ====================
    
    /**
     * Crea material simple con color
     */
    public Material crearMaterialColor(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        return mat;
    }
    
    /**
     * Test: Genera todas las flechas y las imprime
     */
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