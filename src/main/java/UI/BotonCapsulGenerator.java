/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;

public class BotonCapsulGenerator {

    public static Texture2D crearBotonCapsula(int ancho, int alto, ColorRGBA colorBase) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(ancho * alto * 4);
        
        // Radio para las esquinas redondeadas (cápsula)
        float radioEsquina = alto / 2f;
        
        // Centro del botón
        float centroY = alto / 2f;
        
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                ColorRGBA pixel = calcularPixelBotonCapsula(
                    x, y, ancho, alto, 
                    radioEsquina, centroY, 
                    colorBase
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
            ancho,
            alto,
            buffer,
            ColorSpace.sRGB
        );
        
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        texture.setWrap(Texture.WrapMode.Clamp);
        
        return texture;
    }
    
    private static ColorRGBA calcularPixelBotonCapsula(
        int x, int y, int ancho, int alto,
        float radioEsquina, float centroY,
        ColorRGBA colorBase
    ) {
        float nx = (float) x / ancho;
        float ny = (float) y / alto;
        
        // ========== VERIFICAR SI ESTÁ DENTRO DE LA CÁPSULA ==========
        boolean dentroCapsula = false;
        
        // Zona central (rectángulo)
        if (x >= radioEsquina && x < ancho - radioEsquina) {
            dentroCapsula = true;
        }
        // Esquina izquierda (círculo)
        else if (x < radioEsquina) {
            float dx = x - radioEsquina;
            float dy = y - centroY;
            if (dx * dx + dy * dy <= radioEsquina * radioEsquina) {
                dentroCapsula = true;
            }
        }
        // Esquina derecha (círculo)
        else if (x >= ancho - radioEsquina) {
            float dx = x - (ancho - radioEsquina);
            float dy = y - centroY;
            if (dx * dx + dy * dy <= radioEsquina * radioEsquina) {
                dentroCapsula = true;
            }
        }
        
        if (!dentroCapsula) {
            return new ColorRGBA(0, 0, 0, 0); // Transparente
        }
        
        // ========== CALCULAR EFECTOS ==========
        
        // 1. DEGRADADO VERTICAL (más claro arriba, más oscuro abajo)
        float factorDegradado = 1.0f - (ny * 0.4f); // 40% de variación
        
        // 2. BRILLO SUPERIOR (efecto de luz reflejada)
        float brillo = 0f;
        if (ny < 0.35f) { // Tercio superior
            float distanciaBrillo = ny / 0.35f;
            brillo = (1f - distanciaBrillo) * 0.5f; // Hasta 50% más brillante
        }
        
        // 3. SOMBRA INFERIOR (profundidad)
        float sombra = 0f;
        if (ny > 0.7f) { // Parte inferior
            float distanciaSombra = (ny - 0.7f) / 0.3f;
            sombra = distanciaSombra * 0.3f; // Hasta 30% más oscuro
        }
        
        // 4. BORDES MÁS OSCUROS (contorno)
        float distanciaBorde = calcularDistanciaBorde(x, y, ancho, alto, radioEsquina, centroY);
        float oscurecimientoBorde = 0f;
        if (distanciaBorde < 3f) {
            oscurecimientoBorde = (3f - distanciaBorde) / 3f * 0.25f;
        }
        
        // ========== APLICAR COLOR FINAL ==========
        float r = colorBase.r * factorDegradado * (1f + brillo - sombra - oscurecimientoBorde);
        float g = colorBase.g * factorDegradado * (1f + brillo - sombra - oscurecimientoBorde);
        float b = colorBase.b * factorDegradado * (1f + brillo - sombra - oscurecimientoBorde);
        
        // Limitar valores entre 0 y 1
        r = FastMath.clamp(r, 0f, 1f);
        g = FastMath.clamp(g, 0f, 1f);
        b = FastMath.clamp(b, 0f, 1f);
        
        return new ColorRGBA(r, g, b, 1f);
    }
    
    /**
     * Calcula la distancia al borde más cercano de la cápsula
     */
    private static float calcularDistanciaBorde(
        int x, int y, int ancho, int alto,
        float radioEsquina, float centroY
    ) {
        float distMin = Float.MAX_VALUE;
        
        // Distancia a bordes superior e inferior
        distMin = Math.min(distMin, y);
        distMin = Math.min(distMin, alto - y);
        
        // Distancia a bordes izquierdo y derecho (zona central)
        if (x >= radioEsquina && x < ancho - radioEsquina) {
            // Ya tenemos las distancias verticales
        }
        // Distancia al círculo izquierdo
        else if (x < radioEsquina) {
            float dx = x - radioEsquina;
            float dy = y - centroY;
            float distanciaAlCentro = FastMath.sqrt(dx * dx + dy * dy);
            distMin = Math.min(distMin, radioEsquina - distanciaAlCentro);
        }
        // Distancia al círculo derecho
        else if (x >= ancho - radioEsquina) {
            float dx = x - (ancho - radioEsquina);
            float dy = y - centroY;
            float distanciaAlCentro = FastMath.sqrt(dx * dx + dy * dy);
            distMin = Math.min(distMin, radioEsquina - distanciaAlCentro);
        }
        
        return distMin;
    }
    
    /**
     * Colores predefinidos para diferentes tipos de botones
     */
    public static class ColoresBoton {
        // Morado (Play/Principal)
        public static final ColorRGBA MORADO = new ColorRGBA(0.58f, 0.2f, 0.8f, 1f);
        
        // Cyan (Opciones)
        public static final ColorRGBA CYAN = new ColorRGBA(0.2f, 0.7f, 0.9f, 1f);
        
        // Naranja/Coral (Setting)
        public static final ColorRGBA NARANJA = new ColorRGBA(0.95f, 0.5f, 0.3f, 1f);
        
        // Verde (Confirmar)
        public static final ColorRGBA VERDE = new ColorRGBA(0.3f, 0.8f, 0.4f, 1f);
        
        // Rojo (Salir/Cancelar)
        public static final ColorRGBA ROJO = new ColorRGBA(0.9f, 0.3f, 0.3f, 1f);
        
        // Amarillo (Advertencia)
        public static final ColorRGBA AMARILLO = new ColorRGBA(0.95f, 0.8f, 0.2f, 1f);
    }
}