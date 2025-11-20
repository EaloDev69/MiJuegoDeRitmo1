/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;

/**
 * ⭐ ASTRONAUTA v3.0 - ALTA DEFINICIÓN
 * - Cuerpo con el mismo nivel de detalle que el casco
 * - Sombras y brillos realistas
 * - Texturas y profundidad
 * - Costuras y detalles del traje
 */
public class AstronautaGenerator {
    
    private final AssetManager assetManager;
    private static final int TEXTURE_SIZE = 512;
    
    // ==================== PALETA DE COLORES EXPANDIDA ====================
    
    // Casco (sin cambios - ya está perfecto)
    private static final ColorRGBA CASCO_PRINCIPAL = new ColorRGBA(0.35f, 0.15f, 0.50f, 1f);
    private static final ColorRGBA CASCO_BRILLO = new ColorRGBA(0.50f, 0.30f, 0.65f, 1f);
    private static final ColorRGBA CASCO_SOMBRA = new ColorRGBA(0.20f, 0.08f, 0.30f, 1f);
    
    // Visor
    private static final ColorRGBA VISOR = new ColorRGBA(0.3f, 0.8f, 1.0f, 0.9f);
    private static final ColorRGBA VISOR_BRILLO = new ColorRGBA(0.7f, 1.0f, 1.0f, 1.0f);
    private static final ColorRGBA VISOR_REFLEJO = new ColorRGBA(1.0f, 1.0f, 1.0f, 0.6f);
    
    // ⭐ TRAJE - PALETA DETALLADA
    private static final ColorRGBA TRAJE_LUZ_DIRECTA = new ColorRGBA(0.95f, 0.95f, 1.0f, 1f);  // Brillo máximo
    private static final ColorRGBA TRAJE_ILUMINADO = new ColorRGBA(0.80f, 0.80f, 0.85f, 1f);   // Zonas iluminadas
    private static final ColorRGBA TRAJE_BASE = new ColorRGBA(0.65f, 0.65f, 0.70f, 1f);        // Tono base
    private static final ColorRGBA TRAJE_SOMBRA_SUAVE = new ColorRGBA(0.50f, 0.50f, 0.55f, 1f);// Sombra media
    private static final ColorRGBA TRAJE_SOMBRA = new ColorRGBA(0.35f, 0.35f, 0.40f, 1f);      // Sombra
    private static final ColorRGBA TRAJE_SOMBRA_OSCURA = new ColorRGBA(0.25f, 0.25f, 0.30f, 1f);// Sombra profunda
    
    // ⭐ COSTURAS Y DETALLES
    private static final ColorRGBA COSTURA = new ColorRGBA(0.20f, 0.20f, 0.25f, 1f);
    private static final ColorRGBA COSTURA_BRILLO = new ColorRGBA(0.40f, 0.40f, 0.45f, 1f);
    
    // Panel del pecho - MÁS DETALLADO
    private static final ColorRGBA PANEL_MARCO = new ColorRGBA(0.20f, 0.40f, 0.50f, 1f);
    private static final ColorRGBA PANEL_FONDO = new ColorRGBA(0.25f, 0.50f, 0.60f, 1f);
    private static final ColorRGBA PANEL_BRILLO = new ColorRGBA(0.40f, 0.80f, 0.90f, 1f);
    private static final ColorRGBA PANEL_DISPLAY = new ColorRGBA(0.2f, 0.9f, 1.0f, 1f);
    private static final ColorRGBA PANEL_LED_ROJO = new ColorRGBA(1.0f, 0.2f, 0.2f, 1f);
    private static final ColorRGBA PANEL_LED_VERDE = new ColorRGBA(0.2f, 1.0f, 0.3f, 1f);
    
    // Auriculares
    private static final ColorRGBA AURICULARES = new ColorRGBA(0.15f, 0.15f, 0.20f, 1f);
    private static final ColorRGBA AURICULARES_DETALLE = new ColorRGBA(0.8f, 0.3f, 0.3f, 1f);
    
    // ⭐ BOTAS Y GUANTES - MÁS DETALLE
    private static final ColorRGBA BOTAS_LUZ = new ColorRGBA(0.45f, 0.45f, 0.50f, 1f);
    private static final ColorRGBA BOTAS_BASE = new ColorRGBA(0.30f, 0.30f, 0.35f, 1f);
    private static final ColorRGBA BOTAS_SOMBRA = new ColorRGBA(0.20f, 0.20f, 0.25f, 1f);
    private static final ColorRGBA SUELA = new ColorRGBA(0.15f, 0.15f, 0.20f, 1f);
    
    // ⭐ ARTICULACIONES
    private static final ColorRGBA ARTICULACION_LUZ = new ColorRGBA(0.40f, 0.40f, 0.45f, 1f);
    private static final ColorRGBA ARTICULACION_BASE = new ColorRGBA(0.30f, 0.30f, 0.35f, 1f);
    private static final ColorRGBA ARTICULACION_SOMBRA = new ColorRGBA(0.20f, 0.20f, 0.25f, 1f);
    
    public AstronautaGenerator(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    public Node crearAstronauta(float tamano) {
        Node astronautaNode = new Node("Astronauta");
        
        Texture textura = crearTexturaAstronautaDetallada();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", textura);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setColor("Color", new ColorRGBA(1.15f, 1.15f, 1.15f, 1.0f));
        
        Quad quad = new Quad(tamano, tamano);
        Geometry geomAstronauta = new Geometry("AstronautaSprite", quad);
        geomAstronauta.setMaterial(mat);
        
        astronautaNode.attachChild(geomAstronauta);
        
        return astronautaNode;
    }
    
    private Texture crearTexturaAstronautaDetallada() {
        ByteBuffer buffer = BufferUtils.createByteBuffer(TEXTURE_SIZE * TEXTURE_SIZE * 4);
        
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                ColorRGBA pixel = calcularPixelDetallado(x, y);
                
                buffer.put((byte)(pixel.r * 255));
                buffer.put((byte)(pixel.g * 255));
                buffer.put((byte)(pixel.b * 255));
                buffer.put((byte)(pixel.a * 255));
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
        texture.setWrap(Texture.WrapMode.Clamp);
        
        return texture;
    }
    
    private ColorRGBA calcularPixelDetallado(int x, int y) {
    float nx = (float)x / TEXTURE_SIZE;
    float ny = (float)y / TEXTURE_SIZE;
    
    float cx = nx - 0.5f;
    float cy = ny - 0.5f;
    
    // ⭐ PRIMERO: Dibujar el casco completo (tiene prioridad)
    ColorRGBA casco = dibujarCascoCompleto(cx, cy);
    if (casco.a > 0) return casco;
    
    // CUERPO: Debajo del casco
    if (cy > -0.30f && cy <= 0.05f) {
        return dibujarCuerpoDetallado(cx, cy);
    }
    
    // PIERNAS
    if (cy <= -0.30f) {
        return dibujarPiernasDetalladas(cx, cy);
    }
    
    return new ColorRGBA(0, 0, 0, 0);
}
    /**
 * ⭐ CASCO COMPLETAMENTE REDONDO
 * Dibuja una esfera perfecta sin cortar la parte superior
 */
private ColorRGBA dibujarCascoCompleto(float cx, float cy) {
    // ===== PARÁMETROS DE LA ESFERA =====
    float centroX = 0.0f;
    float centroY = 0.28f;  // ⭐ AUMENTADO de 0.22f a 0.28f para separar del cuerpo
    float radioExterno = 0.35f;
    float radioVisor = 0.24f;

    // Calcular distancia desde el centro de la esfera
    float dx = cx - centroX;
    float dy = cy - centroY;
    float dist = (float)Math.sqrt(dx * dx + dy * dy);

    // ===== SI ESTÁ FUERA DE LA ESFERA, NO DIBUJAR =====
    if (dist > radioExterno) {
        return new ColorRGBA(0, 0, 0, 0);
    }

    // ===== PARTE INFERIOR DEL CASCO (CORTE PARA EL CUELLO) =====
    if (cy < 0.08f && Math.abs(cx) < 0.22f) {  // ⭐ AUMENTADO de 0.05f a 0.08f
        return new ColorRGBA(0, 0, 0, 0);
    }

    // ... resto del código sin cambios
    // ===== AURICULARES =====
    ColorRGBA auricular = dibujarAuricularCompleto(cx, cy, centroY);
    if (auricular.a > 0) return auricular;

    // ===== VISOR =====
    ColorRGBA visor = dibujarVisorCompleto(cx, cy, centroY, radioVisor);
    if (visor.a > 0) return visor;

    // ===== CASCO SÓLIDO CON ILUMINACIÓN =====
    return dibujarCascoDegradado(cx, cy, dist, radioExterno, centroY);
}
private ColorRGBA dibujarAuricularCompleto(float cx, float cy, float centroY) {
    float yAuricular = centroY;
    
    // Auricular izquierdo
    float distIzq = (float)Math.sqrt((cx + 0.33f) * (cx + 0.33f) + 
                                     (cy - yAuricular) * (cy - yAuricular));
    if (distIzq <= 0.09f) {
        // LED rojo
        float distLED = (float)Math.sqrt((cx + 0.31f) * (cx + 0.31f) + 
                                         (cy - (yAuricular - 0.02f)) * (cy - (yAuricular - 0.02f)));
        if (distLED <= 0.02f) {
            return AURICULARES_DETALLE;
        }
        return AURICULARES;
    }
    
    // Auricular derecho
    float distDer = (float)Math.sqrt((cx - 0.33f) * (cx - 0.33f) + 
                                     (cy - yAuricular) * (cy - yAuricular));
    if (distDer <= 0.09f) {
        // LED rojo
        float distLED = (float)Math.sqrt((cx - 0.31f) * (cx - 0.31f) + 
                                         (cy - (yAuricular - 0.02f)) * (cy - (yAuricular - 0.02f)));
        if (distLED <= 0.02f) {
            return AURICULARES_DETALLE;
        }
        return AURICULARES;
    }
    
    return new ColorRGBA(0, 0, 0, 0);
}
private ColorRGBA dibujarVisorCompleto(float cx, float cy, float centroY, float radioVisor) {
    float centroVisorY = centroY - 0.05f;
    
    float distVisor = (float)Math.sqrt(cx * cx + (cy - centroVisorY) * (cy - centroVisorY));
    
    // Limitar el visor a la parte frontal
    if (distVisor <= radioVisor && cy > 0.08f && cy < 0.40f) {
        
        // Reflejo superior
        if (cy > 0.32f && Math.abs(cx) < 0.12f) {
            float intensidad = 1.0f - ((cy - 0.32f) / 0.08f);
            intensidad = FastMath.clamp(intensidad, 0f, 1f);
            return interpolar(VISOR_REFLEJO, VISOR_BRILLO, intensidad);
        }
        
        // Brillo medio
        if (cy > 0.25f && Math.abs(cx) < 0.16f) {
            return VISOR_BRILLO;
        }
        
        // Gradiente del visor
        float factorVertical = (cy - 0.08f) / 0.32f;
        factorVertical = FastMath.clamp(factorVertical, 0f, 1f);
        
        ColorRGBA visorOscuro = new ColorRGBA(0.2f, 0.6f, 0.8f, 0.9f);
        return interpolar(visorOscuro, VISOR, factorVertical);
    }
    
    return new ColorRGBA(0, 0, 0, 0);
}
/**
 * ⭐ Iluminación esférica realista
 * Simula luz viniendo desde arriba-derecha
 */
private ColorRGBA dibujarCascoConIluminacion(float cx, float cy, float centroX, float centroY, 
                                              float dist, float radioExterno) {
    
    // ===== CALCULAR NORMAL DE LA ESFERA =====
    // Esto simula cómo la luz rebota en una superficie curva
    float nx = (cx - centroX) / radioExterno;  // Normal X
    float ny = (cy - centroY) / radioExterno;  // Normal Y
    
    // Calcular profundidad Z (como si la esfera saliera de la pantalla)
    float nz = (float)Math.sqrt(Math.max(0, 1.0f - nx*nx - ny*ny));
    
    // ===== FUENTE DE LUZ =====
    // Luz viniendo desde arriba-derecha-frente
    float luzX = 0.3f;   // Derecha
    float luzY = 0.5f;   // Arriba
    float luzZ = 0.8f;   // Hacia nosotros (frente)
    
    // Normalizar vector de luz
    float luzMag = (float)Math.sqrt(luzX*luzX + luzY*luzY + luzZ*luzZ);
    luzX /= luzMag;
    luzY /= luzMag;
    luzZ /= luzMag;
    
    // ===== CALCULAR ILUMINACIÓN (PRODUCTO PUNTO) =====
    float iluminacion = nx * luzX + ny * luzY + nz * luzZ;
    iluminacion = Math.max(0, iluminacion);  // No puede ser negativo
    
    // ===== BORDE DEL CASCO (MÁS OSCURO) =====
    if (dist > radioExterno * 0.92f) {
        return CASCO_SOMBRA;
    }
    
    // ===== APLICAR ILUMINACIÓN AL COLOR =====
    ColorRGBA colorFinal;
    
    if (iluminacion > 0.7f) {
        // Zona muy iluminada (brillo especular)
        float factor = (iluminacion - 0.7f) / 0.3f;
        colorFinal = interpolar(CASCO_BRILLO, new ColorRGBA(0.7f, 0.5f, 0.9f, 1f), factor);
    }
    else if (iluminacion > 0.4f) {
        // Zona iluminada normal
        float factor = (iluminacion - 0.4f) / 0.3f;
        colorFinal = interpolar(CASCO_PRINCIPAL, CASCO_BRILLO, factor);
    }
    else if (iluminacion > 0.15f) {
        // Zona en penumbra
        float factor = (iluminacion - 0.15f) / 0.25f;
        colorFinal = interpolar(CASCO_SOMBRA, CASCO_PRINCIPAL, factor);
    }
    else {
        // Zona en sombra
        colorFinal = CASCO_SOMBRA;
    }
    
    return colorFinal;
}

    
    private ColorRGBA dibujarCascoDegradado(float cx, float cy, float dist, float radioExterno, float centroY) {
    // Borde del casco (más oscuro)
    if (dist > radioExterno * 0.92f) {
        return CASCO_SOMBRA;
    }
    
    // Brillo en la parte superior
    if (cy > (centroY + 0.15f) && dist < radioExterno * 0.80f) {
        float factorBrillo = (cy - (centroY + 0.15f)) / 0.12f;
        factorBrillo = FastMath.clamp(factorBrillo, 0f, 1f);
        return interpolar(CASCO_PRINCIPAL, CASCO_BRILLO, factorBrillo);
    }
    
    // Brillo general en la parte superior
    if (cy > (centroY + 0.05f)) {
        float factorAltura = (cy - (centroY + 0.05f)) / 0.18f;
        factorAltura = FastMath.clamp(factorAltura, 0f, 0.5f);
        return interpolar(CASCO_PRINCIPAL, CASCO_BRILLO, factorAltura);
    }
    
    // Sombra en los lados
    if (Math.abs(cx) > 0.22f) {
        float factorSombra = (Math.abs(cx) - 0.22f) / 0.13f;
        factorSombra = FastMath.clamp(factorSombra, 0f, 1f);
        return interpolar(CASCO_PRINCIPAL, CASCO_SOMBRA, factorSombra);
    }
    
    return CASCO_PRINCIPAL;
}

    // ==================== ⭐ CUERPO DETALLADO ====================
    
    private ColorRGBA dibujarCuerpoDetallado(float cx, float cy) {
    float anchoTorso = 0.30f;
    
    if (Math.abs(cx) > anchoTorso + 0.08f) {
        return new ColorRGBA(0, 0, 0, 0);
    }
    
    // ===== CUELLO/TRANSICIÓN DEL CASCO =====
    // ⭐ NUEVO: Añadir cuello visible
    if (cy > 0.00f && cy <= 0.05f && Math.abs(cx) < 0.20f) {
        // Cuello con sombra (más oscuro)
        float factorCuello = (cy - 0.00f) / 0.05f;
        return interpolar(TRAJE_SOMBRA, TRAJE_BASE, factorCuello);
    }
    
    // ===== BRAZOS CON VOLUMEN =====
    ColorRGBA brazo = dibujarBrazosDetallados(cx, cy);
    if (brazo.a > 0) return brazo;
    
    // ===== PANEL DEL PECHO =====
    ColorRGBA panel = dibujarPanelPechoDetallado(cx, cy);
    if (panel.a > 0) return panel;
    
    // ===== COSTURAS HORIZONTALES =====
    if (esCostura(cy, 0.00f) || esCostura(cy, -0.10f)) {
        return COSTURA;
    }
    
    // ===== TORSO CON ILUMINACIÓN REALISTA =====
    return calcularIluminacionTorso(cx, cy, anchoTorso);
}
    
    private ColorRGBA dibujarBrazosDetallados(float cx, float cy) {
        // ===== BRAZO IZQUIERDO =====
        if (cx < -0.22f && cx > -0.38f && cy > -0.20f && cy < 0.08f) {
            return calcularIluminacionBrazo(cx, cy, -0.30f, true);
        }
        
        // ===== BRAZO DERECHO =====
        if (cx > 0.22f && cx < 0.38f && cy > -0.20f && cy < 0.08f) {
            return calcularIluminacionBrazo(cx, cy, 0.30f, false);
        }
        
        return new ColorRGBA(0, 0, 0, 0);
    }
    
    private ColorRGBA calcularIluminacionBrazo(float cx, float cy, float centroX, boolean esIzquierdo) {
        // Distancia al centro del brazo (para simular cilindro)
        float distCentro = Math.abs(cx - centroX);
        float radioMax = 0.08f;
        
        // ===== ARTICULACIÓN DEL HOMBRO =====
        if (cy > 0.00f && Math.abs(cx - centroX) < 0.06f) {
            float distArticulacion = (float)Math.sqrt((cx - centroX) * (cx - centroX) + (cy - 0.04f) * (cy - 0.04f));
            
            if (distArticulacion < 0.04f) {
                // Centro brillante
                return ARTICULACION_LUZ;
            } else if (distArticulacion < 0.05f) {
                return ARTICULACION_BASE;
            } else {
                return ARTICULACION_SOMBRA;
            }
        }
        
        // ===== ARTICULACIÓN DEL CODO =====
        if (cy > -0.12f && cy < -0.08f) {
            return ARTICULACION_BASE;
        }
        
        // ===== COSTURA VERTICAL DEL BRAZO =====
        if (esIzquierdo) {
            if (Math.abs(cx - (-0.26f)) < 0.005f) return COSTURA;
        } else {
            if (Math.abs(cx - 0.26f) < 0.005f) return COSTURA;
        }
        
        // ===== ILUMINACIÓN CILÍNDRICA =====
        float factorCilindro = distCentro / radioMax;
        
        // Luz frontal (centro del brazo)
        if (factorCilindro < 0.3f) {
            return TRAJE_ILUMINADO;
        }
        // Transición
        else if (factorCilindro < 0.6f) {
            float t = (factorCilindro - 0.3f) / 0.3f;
            return interpolar(TRAJE_ILUMINADO, TRAJE_BASE, t);
        }
        // Sombra lateral
        else if (factorCilindro < 0.85f) {
            float t = (factorCilindro - 0.6f) / 0.25f;
            return interpolar(TRAJE_BASE, TRAJE_SOMBRA, t);
        }
        // Borde oscuro
        else {
            return TRAJE_SOMBRA_OSCURA;
        }
    }
    
    private ColorRGBA dibujarPanelPechoDetallado(float cx, float cy) {
        if (Math.abs(cx) < 0.18f && cy > -0.08f && cy < 0.08f) {
            
            // ===== MARCO DEL PANEL (RELIEVE) =====
            if (Math.abs(cx) > 0.16f || cy < -0.06f || cy > 0.06f) {
                // Borde superior brillante
                if (cy > 0.055f && cy < 0.08f) {
                    return PANEL_BRILLO;
                }
                // Borde inferior oscuro
                if (cy < -0.055f && cy > -0.08f) {
                    return PANEL_MARCO;
                }
                return PANEL_MARCO;
            }
            
            // ===== DISPLAY CENTRAL =====
            if (Math.abs(cx) < 0.10f && cy > 0.00f && cy < 0.05f) {
                // Texto simulado del display
                if (Math.abs(cx) > 0.08f || Math.abs(cy - 0.025f) < 0.008f) {
                    return PANEL_DISPLAY;
                }
                
                // Píxeles brillantes
                float ruido = (float)Math.sin(cx * 100f) * 0.3f + 0.7f;
                return PANEL_BRILLO.mult(ruido);
            }
            
            // ===== LEDS INDICADORES =====
            // LED rojo (izquierda)
            float distLedRojo = (float)Math.sqrt((cx + 0.12f) * (cx + 0.12f) + (cy + 0.03f) * (cy + 0.03f));
            if (distLedRojo < 0.015f) {
                return PANEL_LED_ROJO;
            }
            
            // LED verde (derecha)
            float distLedVerde = (float)Math.sqrt((cx - 0.12f) * (cx - 0.12f) + (cy + 0.03f) * (cy + 0.03f));
            if (distLedVerde < 0.015f) {
                return PANEL_LED_VERDE;
            }
            
            // ===== LÍNEAS DECORATIVAS =====
            if (Math.abs(cy) < 0.01f || Math.abs(cy - 0.04f) < 0.01f || Math.abs(cy + 0.04f) < 0.01f) {
                return PANEL_BRILLO;
            }
            
            // ===== FONDO DEL PANEL CON DEGRADADO =====
            float factorVertical = (cy + 0.08f) / 0.16f;
            return interpolar(PANEL_FONDO, PANEL_MARCO, factorVertical * 0.3f);
        }
        
        return new ColorRGBA(0, 0, 0, 0);
    }
    
    private ColorRGBA calcularIluminacionTorso(float cx, float cy, float anchoTorso) {
        if (Math.abs(cx) > anchoTorso) {
            return new ColorRGBA(0, 0, 0, 0);
        }
        
        // ===== SIMULAR FORMA CILÍNDRICA DEL TORSO =====
        float factorCurvatura = Math.abs(cx) / anchoTorso;
        
        // Centro del torso (luz directa)
        if (Math.abs(cx) < 0.08f) {
            // Brillo especular en el centro superior
            if (cy > 0.02f && Math.abs(cx) < 0.05f) {
                return TRAJE_LUZ_DIRECTA;
            }
            return TRAJE_ILUMINADO;
        }
        
        // Zona intermedia
        if (factorCurvatura < 0.5f) {
            return TRAJE_BASE;
        }
        
        // Zona lateral (sombra suave)
        if (factorCurvatura < 0.7f) {
            float t = (factorCurvatura - 0.5f) / 0.2f;
            return interpolar(TRAJE_BASE, TRAJE_SOMBRA_SUAVE, t);
        }
        
        // Zona de sombra
        if (factorCurvatura < 0.85f) {
            float t = (factorCurvatura - 0.7f) / 0.15f;
            return interpolar(TRAJE_SOMBRA_SUAVE, TRAJE_SOMBRA, t);
        }
        
        // Borde muy oscuro
        return TRAJE_SOMBRA_OSCURA;
    }
    
    // ==================== ⭐ PIERNAS DETALLADAS ====================
    
    private ColorRGBA dibujarPiernasDetalladas(float cx, float cy) {
        float anchoPierna = 0.12f;
        float separacion = 0.05f;
        
        // ===== PIERNA IZQUIERDA =====
        if (cx > -anchoPierna - separacion && cx < -separacion && cy > -0.48f) {
            return calcularIluminacionPierna(cx, cy, -anchoPierna/2 - separacion, true);
        }
        
        // ===== PIERNA DERECHA =====
        if (cx < anchoPierna + separacion && cx > separacion && cy > -0.48f) {
            return calcularIluminacionPierna(cx, cy, anchoPierna/2 + separacion, false);
        }
        
        // ===== BOTAS DETALLADAS =====
        ColorRGBA bota = dibujarBotasDetalladas(cx, cy, anchoPierna, separacion);
        if (bota.a > 0) return bota;
        
        return new ColorRGBA(0, 0, 0, 0);
    }
    
    private ColorRGBA calcularIluminacionPierna(float cx, float cy, float centroX, boolean esIzquierda) {
        float distCentro = Math.abs(cx - centroX);
        float radioMax = 0.06f;
        
        // ===== ARTICULACIÓN DE LA RODILLA =====
        if (cy > -0.38f && cy < -0.33f) {
            float distArticulacion = (float)Math.sqrt((cx - centroX) * (cx - centroX) + (cy + 0.355f) * (cy + 0.355f));
            
            if (distArticulacion < 0.04f) {
                return ARTICULACION_LUZ;
            } else if (distArticulacion < 0.05f) {
                return ARTICULACION_BASE;
            } else {
                return ARTICULACION_SOMBRA;
            }
        }
        
        // ===== COSTURA VERTICAL =====
        if (esIzquierda) {
            if (Math.abs(cx - (-0.065f)) < 0.005f) return COSTURA;
        } else {
            if (Math.abs(cx - 0.065f) < 0.005f) return COSTURA;
        }
        
        // ===== ILUMINACIÓN CILÍNDRICA =====
        float factorCilindro = distCentro / radioMax;
        
        // Muslo (más claro)
        if (cy > -0.33f) {
            if (factorCilindro < 0.4f) {
                return TRAJE_ILUMINADO;
            } else if (factorCilindro < 0.7f) {
                float t = (factorCilindro - 0.4f) / 0.3f;
                return interpolar(TRAJE_ILUMINADO, TRAJE_BASE, t);
            } else {
                return TRAJE_SOMBRA;
            }
        }
        
        // Pantorrilla (más oscuro)
        else {
            if (factorCilindro < 0.4f) {
                return TRAJE_BASE;
            } else if (factorCilindro < 0.7f) {
                float t = (factorCilindro - 0.4f) / 0.3f;
                return interpolar(TRAJE_BASE, TRAJE_SOMBRA, t);
            } else {
                return TRAJE_SOMBRA_OSCURA;
            }
        }
    }
    
    private ColorRGBA dibujarBotasDetalladas(float cx, float cy, float anchoPierna, float separacion) {
        // ===== BOTA IZQUIERDA =====
        if (cx > -0.16f && cx < -0.01f && cy > -0.50f && cy <= -0.46f) {
            return calcularIluminacionBota(cx, cy, -0.085f, true);
        }
        
        // ===== BOTA DERECHA =====
        if (cx < 0.16f && cx > 0.01f && cy > -0.50f && cy <= -0.46f) {
            return calcularIluminacionBota(cx, cy, 0.085f, false);
        }
        
        return new ColorRGBA(0, 0, 0, 0);
    }
    
    private ColorRGBA calcularIluminacionBota(float cx, float cy, float centroX, boolean esIzquierda) {
        // ===== SUELA =====
        if (cy <= -0.48f) {
            // Grosor de la suela con textura
            if (Math.abs(cx - centroX) < 0.06f) {
                return SUELA;
            }
            return new ColorRGBA(0, 0, 0, 0);
        }
        
        // ===== BORDE SUPERIOR BRILLANTE =====
        if (cy > -0.47f && cy <= -0.46f) {
            return BOTAS_LUZ;
        }
        
        // ===== ILUMINACIÓN DE LA BOTA =====
        float distCentro = Math.abs(cx - centroX);
        float radioMax = 0.07f;
        float factorCilindro = distCentro / radioMax;
        
        // Centro brillante
        if (factorCilindro < 0.3f) {
            return BOTAS_LUZ;
        }
        // Transición
        else if (factorCilindro < 0.6f) {
            float t = (factorCilindro - 0.3f) / 0.3f;
            return interpolar(BOTAS_LUZ, BOTAS_BASE, t);
        }
        // Sombra
        else {
            float t = (factorCilindro - 0.6f) / 0.4f;
            return interpolar(BOTAS_BASE, BOTAS_SOMBRA, t);
        }
    }
    
    // ==================== UTILIDADES ====================
    
    private boolean esCostura(float valor, float posicion) {
        return Math.abs(valor - posicion) < 0.008f;
    }
    
    private ColorRGBA interpolar(ColorRGBA c1, ColorRGBA c2, float t) {
        t = FastMath.clamp(t, 0f, 1f);
        return new ColorRGBA(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t,
            c1.a + (c2.a - c1.a) * t
        );
    }
}