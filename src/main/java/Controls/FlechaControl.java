/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controls;

import Modelo.Direccion;
import Modelo.TipoFlecha;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * Control que maneja el movimiento y comportamiento de las flechas
 * Implementa los tres tipos: NORMAL, RAPIDA y DORADA
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaControl extends AbstractControl {
    
    private final TipoFlecha tipo;
    private final Direccion direccion;
    private final Vector3f target; // Centro de la pantalla
    private final float velocidad;
    private final float beatTime;
    private final Material material;
    
    // Estado de inversión (para flechas doradas)
    private boolean invertido = false;
    private float distanciaInversion; // Punto medio donde se invierte
    private Vector3f direccionMovimiento;
    private float distanciaRecorrida = 0f;
    
    // Brillo al hacer hit
    private boolean brilloActivo = false;
    private float tiempoBrillo = 0f;
    private static final float DURACION_BRILLO = 0.3f;
    
    // Brillo pulsante para flechas doradas
    private float tiempoBrilloPulsante = 0f;
    private ColorRGBA colorBase;
    
    // Estado
    private boolean fueGolpeada = false;
    private boolean fueErrada = false;
    
    public FlechaControl(TipoFlecha tipo, Direccion direccion, Vector3f posicionInicial, 
                        Vector3f target, float velocidad, float beatTime, Material material) {
        this.tipo = tipo;
        this.direccion = direccion;
        this.target = target;
        this.velocidad = velocidad;
        this.beatTime = beatTime;
        this.material = material;
        
        // Calcular dirección de movimiento inicial
        this.direccionMovimiento = target.subtract(posicionInicial).normalize();
        
        // Para flechas doradas, calcular el punto de inversión (mitad del camino)
        if (tipo == TipoFlecha.DORADA) {
            this.distanciaInversion = posicionInicial.distance(target) / 2f;
        }
        
        // Guardar color base
        this.colorBase = material.getParamValue("Color");
        if (this.colorBase == null) {
            this.colorBase = ColorRGBA.White;
        }
        
        // Las doradas tienen brillo constante desde el inicio
        if (tipo == TipoFlecha.DORADA) {
            material.setColor("Color", colorBase.mult(1.3f));
        }
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null || fueErrada) return;
        
        // Actualizar brillo pulsante para flechas doradas
        if (tipo == TipoFlecha.DORADA && !brilloActivo) {
            actualizarBrilloPulsante(tpf);
        }
        
        // Actualizar brillo de hit
        if (brilloActivo) {
            tiempoBrillo += tpf;
            if (tiempoBrillo >= DURACION_BRILLO) {
                brilloActivo = false;
                tiempoBrillo = 0f;
                // Restaurar color base (o pulsante si es dorada)
                if (tipo == TipoFlecha.DORADA) {
                    material.setColor("Color", colorBase.mult(1.3f));
                } else {
                    material.setColor("Color", colorBase);
                }
            }
        }
        
        // No mover si fue golpeada (para que el efecto de brillo sea visible)
        if (fueGolpeada) {
            // Opcionalmente: hacer que la flecha se encoja o desaparezca
            float escala = 1f - (tiempoBrillo / DURACION_BRILLO);
            spatial.setLocalScale(escala);
            
            if (tiempoBrillo >= DURACION_BRILLO) {
                // Marcar para eliminación
                spatial.removeFromParent();
            }
            return;
        }
        
        // Movimiento normal
        Vector3f posicionActual = spatial.getLocalTranslation();
        float distanciaAlTarget = posicionActual.distance(target);
        
        // Movimiento basado en velocidad
        float distanciaMovimiento = velocidad * tpf;
        distanciaRecorrida += distanciaMovimiento;
        
        // Lógica de inversión para flechas doradas
        if (tipo == TipoFlecha.DORADA && !invertido && distanciaRecorrida >= distanciaInversion) {
            invertido = true;
            
            // Invertir dirección
            direccionMovimiento = direccionMovimiento.negate();
            
            // Efecto visual: rotar 180 grados
            float rotacionActual = spatial.getLocalRotation().toAngles(null)[2];
            spatial.rotate(0, 0, FastMath.PI); // 180 grados
            
            System.out.println("¡Flecha dorada invertida! Dirección: " + direccion + " → " + direccion.getOpuesta());
        }
        
        // Mover la flecha
        Vector3f nuevaPosicion = posicionActual.add(direccionMovimiento.mult(distanciaMovimiento));
        spatial.setLocalTranslation(nuevaPosicion);
        
        // Verificar si llegó al centro (y no fue golpeada = miss)
        if (distanciaAlTarget < 20f && !fueGolpeada) {
            fueErrada = true;
            // Opcionalmente: efecto visual de miss
            material.setColor("Color", ColorRGBA.DarkGray);
        }
        
        // Eliminar si está demasiado lejos (para flechas doradas invertidas)
        if (tipo == TipoFlecha.DORADA && invertido) {
            float distanciaDesdeTarget = posicionActual.distance(target);
            if (distanciaDesdeTarget > 800f) {
                spatial.removeFromParent();
            }
        }
    }
    
    /**
     * Efecto de brillo pulsante para flechas doradas
     */
    private void actualizarBrilloPulsante(float tpf) {
        tiempoBrilloPulsante += tpf * 3f; // Velocidad del pulso
        
        // Oscilación entre 1.1 y 1.5 de intensidad
        float intensidad = 1.2f + 0.3f * FastMath.sin(tiempoBrilloPulsante);
        material.setColor("Color", colorBase.mult(intensidad));
    }
    
    /**
     * Activa el brillo cuando la flecha es golpeada correctamente
     */
    public void brillar() {
        brilloActivo = true;
        tiempoBrillo = 0f;
        fueGolpeada = true;
        
        // Brillo intenso
        material.setColor("Color", ColorRGBA.White.mult(2f));
    }
    
    /**
     * Marca la flecha como errada
     */
    public void marcarComoErrada() {
        fueErrada = true;
    }
    
    // Getters
    public float getBeatTime() {
        return beatTime;
    }
    
    public TipoFlecha getTipo() {
        return tipo;
    }
    
    public Direccion getDireccion() {
        return direccion;
    }
    
    public boolean fueGolpeada() {
        return fueGolpeada;
    }
    
    public boolean fueErrada() {
        return fueErrada;
    }
    
    public boolean estaInvertida() {
        return invertido;
    }
    
    /**
     * Calcula el delay actual respecto al beat ideal
     */
    public float calcularDelay(float tiempoActual) {
        return Math.abs(tiempoActual - beatTime);
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita renderizado especial
    }
}
