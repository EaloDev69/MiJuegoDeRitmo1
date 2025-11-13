/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.jme3.texture.Texture2D; 
import java.nio.ByteBuffer;

/**
 * Generador procedural de texturas para flechas y zonas de impacto
 * ✅ Crea outlines de zonas de impacto en los bordes
 * ✅ Genera zona central para mecánica ESPACIO
 * ✅ Sistema de colores consistente con Direccion.java
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaProceduralGenerator {

    private final AssetManager assetManager;
    private final int TEXTURE_SIZE = 128; // Tamaño estándar para las texturas cuadradas

    public FlechaProceduralGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    // ==================== COLORES POR DIRECCIÓN ====================

    /**
     * ⭐ Obtiene el color del borde según la dirección
     * DEBE coincidir con los colores definidos en Direccion.java
     */
    public ColorRGBA obtenerBordePorDireccion(Direccion direccion) {
        switch (direccion) {
            case ARRIBA: 
                return new ColorRGBA(0.3f, 0.5f, 1.0f, 1.0f); // Azul (mismo que en Direccion)
            case ABAJO:
                return new ColorRGBA(0.3f, 0.5f, 1.0f, 1.0f); // Azul (mismo que en Direccion)
            case IZQUIERDA:
                return new ColorRGBA(1.0f, 0.3f, 0.3f, 1.0f); // Rojo (mismo que en Direccion)
            case DERECHA:
                return new ColorRGBA(1.0f, 0.3f, 0.3f, 1.0f); // Rojo (mismo que en Direccion)
            case ESPACIO:
                return new ColorRGBA(0.8f, 0.6f, 1.0f, 1.0f); // Violeta
            default:
                return ColorRGBA.White;
        }
    }
    
    // ==================== CREACIÓN DE TEXTURAS PARA ZONAS DE IMPACTO ====================

    /**
     * ⭐ Crea la textura de una zona de impacto (outline de flecha)
     * Dibuja un cuadrado con borde grueso y centro semi-transparente
     * 
     * @param direccion Dirección de la flecha (para colores específicos)
     * @param colorBorde Color del borde exterior
     * @param colorFondo Color del fondo interior (semi-transparente)
     * @return Textura procedural lista para usar
     */
    public Texture crearTexturaZonaFlecha(Direccion direccion, 
                                          ColorRGBA colorBorde, 
                                          ColorRGBA colorFondo) {
        
        ByteBuffer data = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        Image image = new Image(Image.Format.RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, data); 
        ImageRaster raster = ImageRaster.create(image);

        // Configuración del borde
        float grosorBorde = 0.10f * TEXTURE_SIZE; // 10% del tamaño (borde más visible)
        float grosorInterior = grosorBorde * 0.5f; // Borde interno para efecto de profundidad
        
        // Dibuja el cuadrado con efecto de outline
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                
                // Calcular distancia desde los bordes
                float distIzq = x;
                float distDer = TEXTURE_SIZE - x;
                float distArr = y;
                float distAba = TEXTURE_SIZE - y;
                float distMinima = Math.min(Math.min(distIzq, distDer), Math.min(distArr, distAba));
                
                if (distMinima < grosorBorde) {
                    // Borde exterior brillante
                    float intensidad = 1.0f - (distMinima / grosorBorde) * 0.5f;
                    ColorRGBA colorBordeBrillante = colorBorde.mult(intensidad);
                    colorBordeBrillante.a = colorBorde.a;
                    raster.setPixel(x, y, colorBordeBrillante);
                    
                } else if (distMinima < grosorBorde + grosorInterior) {
                    // Borde interior (transición)
                    float factor = (distMinima - grosorBorde) / grosorInterior;
                    ColorRGBA colorTransicion = interpolar(colorBorde, colorFondo, factor);
                    raster.setPixel(x, y, colorTransicion);
                    
                } else {
                    // Centro de la zona (fondo semi-transparente)
                    raster.setPixel(x, y, colorFondo);
                }
            }
        }
        
        return new Texture2D(image);
    }

    /**
     * ⭐ Crea la textura de la zona central (para la tecla ESPACIO)
     * Dibuja un círculo con borde violeta y centro semi-transparente
     * 
     * @param colorBorde Color del borde (violeta)
     * @param colorFondo Color del fondo interior
     * @return Textura circular lista para usar
     */
    public Texture crearTexturaZonaEspacio(ColorRGBA colorBorde, ColorRGBA colorFondo) {
        
        ByteBuffer data = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        Image image = new Image(Image.Format.RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, data);
        ImageRaster raster = ImageRaster.create(image);

        float centroX = TEXTURE_SIZE / 2.0f;
        float centroY = TEXTURE_SIZE / 2.0f;
        float radio = TEXTURE_SIZE / 2.0f - 2; // Pequeño margen
        float grosorBorde = 0.15f * radio; // 15% del radio (borde más grueso)
        
        // Dibuja el círculo con efecto de glow
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                
                float dx = x - centroX;
                float dy = y - centroY;
                float distancia = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distancia <= radio - grosorBorde) {
                    // Centro del círculo (fondo)
                    // Efecto de gradiente hacia el centro
                    float factor = distancia / (radio - grosorBorde);
                    ColorRGBA colorCentro = interpolar(colorFondo.mult(1.2f), colorFondo, factor);
                    raster.setPixel(x, y, colorCentro);
                    
                } else if (distancia <= radio) {
                    // Borde del círculo con glow
                    float factor = (distancia - (radio - grosorBorde)) / grosorBorde;
                    float intensidad = 1.0f + (1.0f - factor) * 0.5f; // Glow más intenso
                    ColorRGBA colorBordeBrillante = colorBorde.mult(intensidad);
                    colorBordeBrillante.a = colorBorde.a;
                    raster.setPixel(x, y, colorBordeBrillante);
                    
                } else if (distancia <= radio + 3) {
                    // Glow exterior suave
                    float factor = (distancia - radio) / 3.0f;
                    ColorRGBA colorGlow = colorBorde.mult(1.0f - factor);
                    colorGlow.a = colorBorde.a * (1.0f - factor);
                    raster.setPixel(x, y, colorGlow);
                    
                } else {
                    // Fuera del círculo (transparencia total)
                    raster.setPixel(x, y, new ColorRGBA(0, 0, 0, 0));
                }
            }
        }
        
        return new Texture2D(image);
    }
    
    // ==================== TEXTURAS ADICIONALES PARA FLECHAS ====================
    
    /**
     * ⭐ Crea una textura de flecha básica (outline)
     * Útil para flechas que no tienen textura cargada
     */
    public Texture crearTexturaFlechaBasica(Direccion direccion, ColorRGBA color) {
        ByteBuffer data = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        Image image = new Image(Image.Format.RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, data);
        ImageRaster raster = ImageRaster.create(image);
        
        int centro = TEXTURE_SIZE / 2;
        int grosor = 8;
        
        // Dibujar forma de flecha simple
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                
                boolean esFlechaPixel = false;
                
                // Cuerpo de la flecha (rectángulo vertical)
                if (x >= centro - grosor && x <= centro + grosor && y < centro + 20) {
                    esFlechaPixel = true;
                }
                
                // Punta de la flecha (triángulo)
                int distDesdeCentroX = Math.abs(x - centro);
                int distDesdePunta = y - (centro + 20);
                if (distDesdePunta > 0 && distDesdeCentroX < (30 - distDesdePunta)) {
                    esFlechaPixel = true;
                }
                
                if (esFlechaPixel) {
                    raster.setPixel(x, y, color);
                } else {
                    raster.setPixel(x, y, new ColorRGBA(0, 0, 0, 0));
                }
            }
        }
        
        return new Texture2D(image);
    }
    
    /**
     * ⭐ Crea una textura de brillo/glow para efectos visuales
     */
    public Texture crearTexturaBrillo(ColorRGBA color, float intensidad) {
        ByteBuffer data = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        Image image = new Image(Image.Format.RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, data);
        ImageRaster raster = ImageRaster.create(image);
        
        float centroX = TEXTURE_SIZE / 2.0f;
        float centroY = TEXTURE_SIZE / 2.0f;
        float radioMax = TEXTURE_SIZE / 2.0f;
        
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                float dx = x - centroX;
                float dy = y - centroY;
                float distancia = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distancia <= radioMax) {
                    float factor = 1.0f - (distancia / radioMax);
                    factor = (float) Math.pow(factor, 2); // Curva cuadrática para suavidad
                    
                    ColorRGBA colorBrillo = color.mult(intensidad);
                    colorBrillo.a = factor * color.a;
                    raster.setPixel(x, y, colorBrillo);
                } else {
                    raster.setPixel(x, y, new ColorRGBA(0, 0, 0, 0));
                }
            }
        }
        
        return new Texture2D(image);
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Interpola entre dos colores
     */
    private ColorRGBA interpolar(ColorRGBA color1, ColorRGBA color2, float factor) {
        factor = Math.max(0, Math.min(1, factor)); // Clamp entre 0 y 1
        
        float r = color1.r * (1 - factor) + color2.r * factor;
        float g = color1.g * (1 - factor) + color2.g * factor;
        float b = color1.b * (1 - factor) + color2.b * factor;
        float a = color1.a * (1 - factor) + color2.a * factor;
        
        return new ColorRGBA(r, g, b, a);
    }
    
    /**
     * Crea un color con brillo ajustado
     */
    private ColorRGBA ajustarBrillo(ColorRGBA color, float brillo) {
        return new ColorRGBA(
            Math.min(1.0f, color.r * brillo),
            Math.min(1.0f, color.g * brillo),
            Math.min(1.0f, color.b * brillo),
            color.a
        );
    }
    
    /**
     * Debug: Verifica que las texturas se están creando correctamente
     */
    public void testearCreacionTexturas() {
        System.out.println("\n🎨 TESTING FlechaProceduralGenerator...");
        
        for (Direccion dir : Direccion.values()) {
            ColorRGBA colorBorde = obtenerBordePorDireccion(dir);
            ColorRGBA colorFondo = new ColorRGBA(0.1f, 0.1f, 0.1f, 0.3f);
            
            try {
                Texture textura = crearTexturaZonaFlecha(dir, colorBorde, colorFondo);
                System.out.println("  ✓ Zona creada: " + dir + " (" + 
                                 textura.getImage().getWidth() + "x" + 
                                 textura.getImage().getHeight() + ")");
            } catch (Exception e) {
                System.err.println("  ❌ Error creando zona: " + dir);
                e.printStackTrace();
            }
        }
        
        // Test zona ESPACIO
        try {
            ColorRGBA colorBorde = new ColorRGBA(0.8f, 0.6f, 1.0f, 1.0f);
            ColorRGBA colorFondo = new ColorRGBA(0.3f, 0.1f, 0.4f, 0.5f);
            Texture textura = crearTexturaZonaEspacio(colorBorde, colorFondo);
            System.out.println("  ✓ Zona ESPACIO creada (" + 
                             textura.getImage().getWidth() + "x" + 
                             textura.getImage().getHeight() + ")");
        } catch (Exception e) {
            System.err.println("  ❌ Error creando zona ESPACIO");
            e.printStackTrace();
        }
        
        System.out.println("✓ Testing completado\n");
    }
}