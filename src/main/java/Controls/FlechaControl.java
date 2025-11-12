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

/**
 * Control que maneja el movimiento y comportamiento de las flechas
 * Implementa movimiento vertical desde abajo hacia arriba hasta los targets
 * Soporta 4 tipos: NORMAL, RAPIDA, DORADA (se invierte), LUNA (especial)
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechaControl extends AbstractControl {
    // ==================== ATRIBUTOS BÁSICOS ====================
    private final TipoFlecha tipo;
    private final Direccion direccion;
    private final Vector3f posicionInicial;
    private final Vector3f target; // Posición del target (flecha vacía)
    private final float velocidad;
    private final float beatTime;
    private final Material material;
    
    // ==================== ESTADO DE INVERSIÓN (FLECHAS DORADAS) ====================
    private boolean invertido = false;
    private float distanciaInversion; // Punto medio donde se invierte
    private float distanciaRecorrida = 0f;
    
    // ==================== EFECTOS VISUALES ====================
    // Brillo al hacer hit correcto
    private boolean brilloActivo = false;
    private float tiempoBrillo = 0f;
    private static final float DURACION_BRILLO = 0.3f;
    
    // Brillo pulsante para flechas doradas
    private float tiempoBrilloPulsante = 0f;
    
    // Brillo suave para flechas de luna
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
        
        // Para flechas doradas, calcular el punto de inversión (mitad del camino)
        if (tipo == TipoFlecha.DORADA) {
            this.distanciaInversion = posicionInicial.distance(target) / 2f;
        }
        
        // Guardar color base para efectos
        this.colorBase = material.getParamValue("Color");
        if (this.colorBase == null) {
            this.colorBase = ColorRGBA.White.clone();
        }
        
        // Aplicar efectos iniciales según el tipo
        aplicarEfectoInicial();
    }
    
    // ==================== MÉTODOS PRINCIPALES ====================
    
    /**
     * Aplica efectos visuales iniciales según el tipo de flecha
     */
    private void aplicarEfectoInicial() {
        switch (tipo) {
            case DORADA:
                // Brillo constante dorado desde el inicio
                material.setColor("Color", colorBase.mult(1.3f));
                break;
                
            case LUNA:
                // Brillo suave blanco para la luna
                material.setColor("Color", new ColorRGBA(1.2f, 1.2f, 1.2f, 1.0f));
                break;
                
            case RAPIDA:
                // Brillo sutil para las rápidas
                material.setColor("Color", colorBase.mult(1.1f));
                break;
                
            default:
                // Normal sin cambios
                material.setColor("Color", colorBase);
                break;
        }
    }
    
    /**
     * Update principal - Se ejecuta cada frame
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null || fueErrada) {
            return;
        }
        
        // ========== ACTUALIZAR EFECTOS VISUALES ==========
        actualizarEfectosVisuales(tpf);
        
        // ========== MANEJAR FLECHA GOLPEADA ==========
        if (fueGolpeada) {
            manejarFlechaGolpeada(tpf);
            return; // No mover más si fue golpeada
        }
        
        // ========== MOVIMIENTO DE LA FLECHA ==========
        moverFlecha(tpf);
        
        // ========== VERIFICAR SI PASÓ EL TARGET (MISS) ==========
        verificarMiss();
    }
    
    /**
     * Actualiza los efectos visuales según el tipo de flecha
     */
    private void actualizarEfectosVisuales(float tpf) {
        // Brillo de hit (para todas las flechas)
        if (brilloActivo) {
            tiempoBrillo += tpf;
            if (tiempoBrillo >= DURACION_BRILLO) {
                brilloActivo = false;
                tiempoBrillo = 0f;
                restaurarColorBase();
            }
            return; // No aplicar otros efectos durante el brillo de hit
        }
        
        // Efectos específicos por tipo
        switch (tipo) {
            case DORADA:
                actualizarBrilloPulsanteDorada(tpf);
                break;
                
            case LUNA:
                actualizarBrilloLuna(tpf);
                break;
                
            default:
                // NORMAL y RAPIDA sin efectos adicionales
                break;
        }
    }
    
    /**
     * Efecto de brillo pulsante para flechas doradas
     */
    private void actualizarBrilloPulsanteDorada(float tpf) {
        tiempoBrilloPulsante += tpf * 3f; // Velocidad del pulso
        
        // Oscilación entre 1.1 y 1.5 de intensidad
        float intensidad = 1.2f + 0.3f * FastMath.sin(tiempoBrilloPulsante);
        material.setColor("Color", colorBase.mult(intensidad));
    }
    
    /**
     * Efecto de brillo suave para flechas de luna
     */
    private void actualizarBrilloLuna(float tpf) {
        tiempoBrilloLuna += tpf * 2f;
        
        // Oscilación suave entre 1.0 y 1.3
        float intensidad = 1.15f + 0.15f * FastMath.sin(tiempoBrilloLuna);
        ColorRGBA colorLuna = new ColorRGBA(intensidad, intensidad, intensidad, 1.0f);
        material.setColor("Color", colorLuna);
    }
    
    /**
     * Maneja el comportamiento cuando la flecha fue golpeada
     */
    private void manejarFlechaGolpeada(float tpf) {
        tiempoBrillo += tpf;
        
        // Efecto de encogimiento/desaparición
        if (tiempoBrillo < DURACION_BRILLO) {
            float escala = 1f - (tiempoBrillo / DURACION_BRILLO);
            escala = Math.max(escala, 0.1f); // Mínimo 10% de tamaño
            spatial.setLocalScale(escala);
        } else {
            // Eliminar del juego
            spatial.removeFromParent();
        }
    }
    
    /**
     * Mueve la flecha hacia su target
     */
    private void moverFlecha(float tpf) {
        Vector3f posicionActual = spatial.getLocalTranslation();
        
        // ========== CALCULAR DIRECCIÓN DE MOVIMIENTO ==========
        Vector3f direccionMovimiento;
        
        if (tipo == TipoFlecha.DORADA && invertido) {
            // Flechas doradas invertidas: se alejan del target
            direccionMovimiento = posicionActual.subtract(target).normalize();
        } else {
            // Todas las demás: se acercan al target
            direccionMovimiento = target.subtract(posicionActual).normalize();
        }
        
        // ========== APLICAR MOVIMIENTO ==========
        float distanciaMovimiento = velocidad * tpf;
        distanciaRecorrida += distanciaMovimiento;
        
        Vector3f nuevaPosicion = posicionActual.add(direccionMovimiento.mult(distanciaMovimiento));
        spatial.setLocalTranslation(nuevaPosicion);
        
        // ========== LÓGICA DE INVERSIÓN PARA FLECHAS DORADAS ==========
        if (tipo == TipoFlecha.DORADA && !invertido && distanciaRecorrida >= distanciaInversion) {
            ejecutarInversion();
        }
    }
    
    /**
     * Ejecuta la inversión de una flecha dorada
     */
    private void ejecutarInversion() {
        invertido = true;
        
        // Efecto visual: rotar 180 grados
        spatial.rotate(0, 0, FastMath.PI);
        
        // Efecto visual: flash brillante
        material.setColor("Color", ColorRGBA.White.mult(2f));
        
        // Resetear después de 0.1 segundos
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
    
    /**
     * Verifica si la flecha pasó el target sin ser golpeada (MISS)
     */
    private void verificarMiss() {
        Vector3f posicionActual = spatial.getLocalTranslation();
        
        // Verificar si pasó el target verticalmente (según si está invertida o no)
        boolean pasoElTarget;
        
        if (tipo == TipoFlecha.DORADA && invertido) {
            // Flecha invertida: miss si sale de la pantalla hacia abajo o los lados
            pasoElTarget = posicionActual.y < -200f || 
                          Math.abs(posicionActual.x - target.x) > 600f;
        } else {
            // Flecha normal: miss si pasa 50px arriba del target
            pasoElTarget = posicionActual.y > target.y + 50f;
        }
        
        if (pasoElTarget && !fueGolpeada) {
            fueErrada = true;
            // Efecto visual de miss: oscurecer
            material.setColor("Color", ColorRGBA.DarkGray);
        }
    }
    
    /**
     * Restaura el color base según el tipo de flecha
     */
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
    
    /**
     * Activa el brillo cuando la flecha es golpeada correctamente
     */
    public void brillar() {
        brilloActivo = true;
        tiempoBrillo = 0f;
        fueGolpeada = true;
        
        // Brillo intenso blanco
        material.setColor("Color", ColorRGBA.White.mult(2.5f));
        
        System.out.println("✨ Flecha golpeada: " + tipo + " " + direccion);
    }
    
    /**
     * Marca la flecha como errada manualmente (para eliminarla sin efecto)
     */
    public void marcarComoErrada() {
        fueErrada = true;
    }
    
    /**
     * Calcula el delay actual respecto al beat ideal
     * @param tiempoActual Tiempo actual del juego
     * @return Delay en segundos (valor absoluto)
     */
    public float calcularDelay(float tiempoActual) {
        return Math.abs(tiempoActual - beatTime);
    }
    
    /**
     * Verifica si la flecha está dentro del rango de golpe
     * @param tiempoActual Tiempo actual del juego
     * @param rangoPermitido Rango de tiempo permitido (ej: 0.3s)
     * @return true si está dentro del rango
     */
    public boolean estaEnRangoDeGolpe(float tiempoActual, float rangoPermitido) {
        return calcularDelay(tiempoActual) < rangoPermitido;
    }
    
    /**
     * Obtiene la distancia actual al target en píxeles
     */
    public float getDistanciaAlTarget() {
        if (spatial == null) return Float.MAX_VALUE;
        Vector3f posActual = spatial.getLocalTranslation();
        return posActual.distance(target);
    }
    
    // ==================== GETTERS ====================
    
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
    
    public Vector3f getTarget() {
        return target.clone();
    }
    
    public Vector3f getPosicionInicial() {
        return posicionInicial.clone();
    }
    
    public float getVelocidad() {
        return velocidad;
    }
    
    public float getDistanciaRecorrida() {
        return distanciaRecorrida;
    }
    
    /**
     * Obtiene el porcentaje de camino recorrido (0.0 a 1.0)
     */
    public float getPorcentajeRecorrido() {
        float distanciaTotal = posicionInicial.distance(target);
        if (distanciaTotal == 0) return 1.0f;
        return Math.min(distanciaRecorrida / distanciaTotal, 1.0f);
    }
    
    // ==================== MÉTODOS ABSTRACTOS ====================
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // No se necesita renderizado especial
        // Los efectos visuales se manejan en controlUpdate()
    }
    
    // ==================== MÉTODOS DE DEBUG ====================
    
    @Override
    public String toString() {
        return String.format(
            "FlechaControl[tipo=%s, dir=%s, beat=%.2fs, invertida=%s, golpeada=%s, errada=%s]",
            tipo, direccion, beatTime, invertido, fueGolpeada, fueErrada
        );
    }
    
    /**
     * Información detallada para debugging
     */
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