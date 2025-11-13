/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controls;

import Modelo.Direccion;
import Modelo.TipoFlecha;
import static Modelo.TipoFlecha.DORADA;
import static Modelo.TipoFlecha.LUNA;
import static Modelo.TipoFlecha.RAPIDA;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * Control que maneja el movimiento y comportamiento de las flechas
 * Soporta 4 tipos: NORMAL, RAPIDA, DORADA (se invierte), LUNA (especial)
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaControl extends AbstractControl {
    // ==================== ATRIBUTOS BÁSICOS ====================
    private final TipoFlecha tipo;
    private final Direccion direccion;
    private final Vector3f posicionInicial;
    private final Vector3f target;
    private final float velocidad;
    private final float beatTime;
    private final Material material;
    
    // ==================== ESTADO DE INVERSIÓN ====================
    private boolean invertido = false;
    private float distanciaInversion;
    private float distanciaRecorrida = 0f;
    
    // ==================== EFECTOS VISUALES ====================
    private boolean brilloActivo = false;
    private float tiempoBrillo = 0f;
    private static final float DURACION_BRILLO = 0.3f;
    private float tiempoBrilloPulsante = 0f;
    private float tiempoBrilloLuna = 0f;
    private ColorRGBA colorBase;
    
    // ==================== ESTADO DEL JUEGO ====================
    private boolean fueGolpeada = false;
    private boolean fueErrada = false;
    
    // ==================== CONSTRUCTOR ====================
    public FlechaControl(TipoFlecha tipo, Direccion direccion, Vector3f posicionInicial,
                        Vector3f target, float velocidad, float beatTime, Material material) {
        this.tipo = tipo;
        this.direccion = direccion;
        this.posicionInicial = posicionInicial.clone();
        this.target = target.clone();
        this.velocidad = velocidad;
        this.beatTime = beatTime;
        this.material = material;
        
        if (tipo == TipoFlecha.DORADA) {
            this.distanciaInversion = posicionInicial.distance(target) / 2f;
        }
        
        this.colorBase = material.getParamValue("Color");
        if (this.colorBase == null) {
            this.colorBase = ColorRGBA.White.clone();
        }
        
        aplicarEfectoInicial();
    }
    
    // ==================== MÉTODOS PRINCIPALES ====================
    
    private void aplicarEfectoInicial() {
        switch (tipo) {
            case DORADA:
                material.setColor("Color", colorBase.mult(1.3f));
                break;
            case LUNA:
                material.setColor("Color", new ColorRGBA(1.2f, 1.2f, 1.2f, 1.0f));
                break;
            case RAPIDA:
                material.setColor("Color", colorBase.mult(1.1f));
                break;
            default:
                material.setColor("Color", colorBase);
                break;
        }
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null || fueErrada) {
            return;
        }
        
        actualizarEfectosVisuales(tpf);
        
        if (fueGolpeada) {
            manejarFlechaGolpeada(tpf);
            return;
        }
        
        moverFlecha(tpf);
        verificarMiss();
    }
    
    private void actualizarEfectosVisuales(float tpf) {
        if (brilloActivo) {
            tiempoBrillo += tpf;
            if (tiempoBrillo >= DURACION_BRILLO) {
                brilloActivo = false;
                tiempoBrillo = 0f;
                restaurarColorBase();
            }
            return;
        }
        
        switch (tipo) {
            case DORADA:
                actualizarBrilloPulsanteDorada(tpf);
                break;
            case LUNA:
                actualizarBrilloLuna(tpf);
                break;
        }
    }
    
    private void actualizarBrilloPulsanteDorada(float tpf) {
        tiempoBrilloPulsante += tpf * 3f;
        float intensidad = 1.2f + 0.3f * FastMath.sin(tiempoBrilloPulsante);
        material.setColor("Color", colorBase.mult(intensidad));
    }
    
    private void actualizarBrilloLuna(float tpf) {
        tiempoBrilloLuna += tpf * 2f;
        float intensidad = 1.15f + 0.15f * FastMath.sin(tiempoBrilloLuna);
        material.setColor("Color", new ColorRGBA(intensidad, intensidad, intensidad, 1.0f));
    }
    
    private void manejarFlechaGolpeada(float tpf) {
        tiempoBrillo += tpf;
        
        if (tiempoBrillo < DURACION_BRILLO) {
            float escala = 1f - (tiempoBrillo / DURACION_BRILLO);
            escala = Math.max(escala, 0.1f);
            spatial.setLocalScale(escala);
        } else {
            spatial.removeFromParent();
        }
    }
    
    private void moverFlecha(float tpf) {
        Vector3f posicionActual = spatial.getLocalTranslation();
        
        Vector3f direccionMovimiento;
        if (tipo == TipoFlecha.DORADA && invertido) {
            direccionMovimiento = posicionActual.subtract(target).normalize();
        } else {
            direccionMovimiento = target.subtract(posicionActual).normalize();
        }
        
        float distanciaMovimiento = velocidad * tpf;
        distanciaRecorrida += distanciaMovimiento;
        
        Vector3f nuevaPosicion = posicionActual.add(direccionMovimiento.mult(distanciaMovimiento));
        spatial.setLocalTranslation(nuevaPosicion);
        
        if (tipo == TipoFlecha.DORADA && !invertido && distanciaRecorrida >= distanciaInversion) {
            ejecutarInversion();
        }
    }
    
    private void ejecutarInversion() {
        invertido = true;
        spatial.rotate(0, 0, FastMath.PI);
        material.setColor("Color", ColorRGBA.White.mult(2f));
        
        new Thread(() -> {
            try {
                Thread.sleep(100);
                material.setColor("Color", colorBase.mult(1.3f));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        System.out.println("¡Flecha dorada invertida! " + direccion + " → " + direccion.getOpuesta());
    }
    
    private void verificarMiss() {
        Vector3f posicionActual = spatial.getLocalTranslation();
        boolean pasoElTarget;
        
        if (tipo == TipoFlecha.DORADA && invertido) {
            pasoElTarget = posicionActual.y < -200f || 
                          Math.abs(posicionActual.x - target.x) > 600f;
        } else {
            pasoElTarget = posicionActual.y > target.y + 50f;
        }
        
        if (pasoElTarget && !fueGolpeada) {
            fueErrada = true;
            material.setColor("Color", ColorRGBA.DarkGray);
        }
    }
    
    private void restaurarColorBase() {
        switch (tipo) {
            case DORADA:
                material.setColor("Color", colorBase.mult(1.3f));
                break;
            case LUNA:
                material.setColor("Color", new ColorRGBA(1.2f, 1.2f, 1.2f, 1.0f));
                break;
            case RAPIDA:
                material.setColor("Color", colorBase.mult(1.1f));
                break;
            default:
                material.setColor("Color", colorBase);
                break;
        }
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    public void brillar() {
        brilloActivo = true;
        tiempoBrillo = 0f;
        fueGolpeada = true;
        material.setColor("Color", ColorRGBA.White.mult(2.5f));
        System.out.println("✨ Flecha golpeada: " + tipo + " " + direccion);
    }
    
    public void marcarComoErrada() {
        fueErrada = true;
    }
    
    /**
     * ⭐ NUEVO: Marca la flecha como golpeada desde código externo
     * Usado por GameplayAppState para el nuevo sistema de input
     */
    public void marcarComoGolpeada() {
        if (fueGolpeada) {
            return; // Ya fue golpeada, evitar doble procesamiento
        }
        
        fueGolpeada = true;
        brilloActivo = true;
        tiempoBrillo = 0f;
        
        // Efecto visual inmediato
        material.setColor("Color", ColorRGBA.White.mult(2.5f));
        
        System.out.println("✅ Flecha marcada como golpeada: " + tipo + " " + direccion);
    }
    
    public float calcularDelay(float tiempoActual) {
        return Math.abs(tiempoActual - beatTime);
    }
    
    public boolean estaEnRangoDeGolpe(float tiempoActual, float rangoPermitido) {
        return calcularDelay(tiempoActual) < rangoPermitido;
    }
    
    public float getDistanciaAlTarget() {
        if (spatial == null) return Float.MAX_VALUE;
        return spatial.getLocalTranslation().distance(target);
    }
    
    // ==================== GETTERS ====================
    
    public float getBeatTime() { return beatTime; }
    public TipoFlecha getTipo() { return tipo; }
    public Direccion getDireccion() { return direccion; }
    public boolean fueGolpeada() { return fueGolpeada; }
    public boolean fueErrada() { return fueErrada; }
    public boolean estaInvertida() { return invertido; }
    public Vector3f getTarget() { return target.clone(); }
    public Vector3f getPosicionInicial() { return posicionInicial.clone(); }
    public float getVelocidad() { return velocidad; }
    public float getDistanciaRecorrida() { return distanciaRecorrida; }
    
    public float getPorcentajeRecorrido() {
        float distanciaTotal = posicionInicial.distance(target);
        if (distanciaTotal == 0) return 1.0f;
        return Math.min(distanciaRecorrida / distanciaTotal, 1.0f);
    }
    
    // ==================== MÉTODOS ABSTRACTOS ====================
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita renderizado especial
    }
    
    // ==================== DEBUG ====================
    
    @Override
    public String toString() {
        return String.format(
            "FlechaControl[tipo=%s, dir=%s, beat=%.2fs, invertida=%s, golpeada=%s, errada=%s]",
            tipo, direccion, beatTime, invertido, fueGolpeada, fueErrada
        );
    }
    
    public String getInfoDetallada() {
        return String.format(
            "=== FLECHA DEBUG ===\n" +
            "Tipo: %s\n" +
            "Dirección: %s\n" +
            "Beat Time: %.3fs\n" +
            "Velocidad: %.1f px/s\n" +
            "Distancia Recorrida: %.1f px\n" +
            "Progreso: %.1f%%\n" +
            "Distancia al Target: %.1f px\n" +
            "Invertida: %s\n" +
            "Golpeada: %s\n" +
            "Errada: %s\n" +
            "==================",
            tipo, direccion, beatTime, velocidad, distanciaRecorrida,
            getPorcentajeRecorrido() * 100, getDistanciaAlTarget(),
            invertido, fueGolpeada, fueErrada
        );
    }
}