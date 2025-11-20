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
 * Control mejorado de flechas con movimiento recto
 * ✅ Movimiento en línea recta desde bordes hacia el centro
 * ✅ Flechas doradas invierten dirección correctamente
 * ✅ Mayor distancia de spawn para mejor tiempo de reacción
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
    private boolean enOrbita = false;
    private boolean enSalida = false;
    private float anguloOrbita = 0f;
    private float radioOrbita = 40f;
    private float velocidadAngular = 3.5f;
    private float tiempoOrbita = 0f;
    private static final float DURACION_ORBITA = 1.2f;
    private float duracionOrbitaCfg = DURACION_ORBITA;
    private float viewportWidth = 1280f;
    private float viewportHeight = 720f;
    private float inversionFlashTimer = 0f;
    
    // ⭐ NUEVO: Vector de movimiento calculado una sola vez
    private Vector3f direccionMovimiento;
    private Vector3f direccionMovimientoInvertida;

    // ⭐ NUEVO: Atributos para movimiento parabólico (curva de Bézier)
    private Vector3f controlPoint1;
    private Vector3f controlPoint2;
    private float tiempoTotalMovimiento;
    private float tiempoTranscurridoMovimiento = 0f;
    private boolean enMovimientoParabolico = false;
    private Vector3f velocidadLinealFinal;
    private float anguloOriginal;
    private boolean transicionActivada = false;
    private Vector3f orbitCenter = new Vector3f(0,0,0);
    private boolean faseRecta = true;
    private boolean faseGiro = false;
    private boolean faseFinalRecta = false;
    private float giroAnguloAcumulado = 0f;
    private float giroVelocidadAngular = 0f;
    private float giroRadio = 40f;
    private Direccion direccionPerpendicular;
    private Vector3f perpendicularTarget;
    private Vector3f direccionMovimientoFinal;
    private Vector3f offsetCentro = new Vector3f(0, 0, 0);
    
    // ==================== EFECTOS VISUALES ====================
    private boolean brilloActivo = false;
    private float tiempoBrillo = 0f;
    private static final float DURACION_BRILLO = 0.5f;
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
        
        // ⭐ NUEVO: Calcular dirección de movimiento una sola vez
        this.direccionMovimiento = target.subtract(posicionInicial).normalize();
        
        // Calcular tiempo total de movimiento
        this.tiempoTotalMovimiento = posicionInicial.distance(target) / velocidad;

        // Calcular ángulo original de la flecha
        this.anguloOriginal = FastMath.atan2(direccionMovimiento.y, direccionMovimiento.x);

        this.velocidadLinealFinal = direccionMovimiento.mult(velocidad);
        
        // Para flechas doradas: calcular dirección invertida
        if (tipo == TipoFlecha.DORADA) {
            this.distanciaInversion = posicionInicial.distance(target) * 0.4f;
            this.direccionMovimientoInvertida = direccionMovimiento.negate();
            this.direccionPerpendicular = calcularPerpendicular(direccion);
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
        if (offsetCentro.equals(Vector3f.ZERO)) {
            if (spatial instanceof com.jme3.scene.Geometry) {
                com.jme3.scene.Geometry g = (com.jme3.scene.Geometry) spatial;
                com.jme3.bounding.BoundingBox bb = (com.jme3.bounding.BoundingBox) g.getMesh().getBound();
                float w = bb.getXExtent() * 2f;
                float h = bb.getYExtent() * 2f;
                offsetCentro.set(w / 2f, h / 2f, 0f);
            }
        }
        
        actualizarEfectosVisuales(tpf);
        if (inversionFlashTimer > 0f) {
            inversionFlashTimer -= tpf;
            if (inversionFlashTimer <= 0f) {
                restaurarColorBase();
            }
        }
        
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
        float progreso = tiempoBrillo / DURACION_BRILLO; // 0.0 a 1.0
        
        // === EFECTO 1: Brillo que desvanece ===
        float intensidadBrillo;
        if (progreso < 0.2f) {
            // Primeros 20%: Máximo brillo (flash blanco)
            intensidadBrillo = 4.0f;
        } else {
            // Resto: Desvanecimiento gradual
            intensidadBrillo = 4.0f * (1f - ((progreso - 0.2f) / 0.8f));
        }
        
        // Aplicar color con brillo
        ColorRGBA colorBrillante = ColorRGBA.White.mult(intensidadBrillo);
        material.setColor("Color", colorBrillante);
        
        // === EFECTO 2: Escala que crece y luego desaparece ===
        float escala;
        if (progreso < 0.3f) {
            // Primeros 30%: Expandirse (1.0 → 1.5)
            escala = 1f + (progreso / 0.3f) * 0.5f;
        } else {
            // Resto: Contraerse hasta desaparecer (1.5 → 0.1)
            float t = (progreso - 0.3f) / 0.7f;
            escala = 1.5f - (t * 1.4f);
            escala = Math.max(escala, 0.1f);
        }
        spatial.setLocalScale(escala);
        
    } else {
        // Terminar animación
        spatial.removeFromParent();
    }
}
    
    /**
     * ⭐ MEJORADO: Movimiento en línea recta constante
     * - Usa el vector de dirección pre-calculado
     * - Movimiento uniforme y predecible
     * - Flechas doradas invierten dirección correctamente
     */
    private void moverFlecha(float tpf) {
        Vector3f posicionActual = spatial.getLocalTranslation();

        if (tipo != TipoFlecha.DORADA) {
            Vector3f nuevaPosicion = posicionActual.add(direccionMovimiento.mult(velocidad * tpf));
            spatial.setLocalTranslation(nuevaPosicion);
            distanciaRecorrida = posicionInicial.distance(nuevaPosicion);
            float anguloActual = FastMath.atan2(direccionMovimiento.y, direccionMovimiento.x);
            spatial.setLocalRotation(new com.jme3.math.Quaternion().fromAngleAxis(anguloActual, Vector3f.UNIT_Z));
            return;
        }

        if (faseRecta) {
            Vector3f nuevaPosicion = posicionActual.add(direccionMovimiento.mult(velocidad * tpf));
            spatial.setLocalTranslation(nuevaPosicion);
            distanciaRecorrida = posicionInicial.distance(nuevaPosicion);
            float anguloActual = FastMath.atan2(direccionMovimiento.y, direccionMovimiento.x);
            spatial.setLocalRotation(new com.jme3.math.Quaternion().fromAngleAxis(anguloActual, Vector3f.UNIT_Z));
            if (distanciaRecorrida >= distanciaInversion) {
                faseRecta = false;
                faseGiro = true;
                giroRadio = 80f;
                giroVelocidadAngular = 2.5f;
                giroAnguloAcumulado = 0f;
                float ang0 = FastMath.atan2(spatial.getLocalTranslation().y - orbitCenter.y, spatial.getLocalTranslation().x - orbitCenter.x);
                anguloOrbita = ang0;
                if (perpendicularTarget == null && direccionPerpendicular != null) {
                    perpendicularTarget = direccionPerpendicular.getPosicionTarget(viewportWidth, viewportHeight);
                }
            }
            return;
        }

        if (faseGiro) {
            giroAnguloAcumulado += giroVelocidadAngular * tpf;
            float ang = anguloOrbita + giroAnguloAcumulado;
            float x = orbitCenter.x + FastMath.cos(ang) * giroRadio;
            float y = orbitCenter.y + FastMath.sin(ang) * giroRadio;
            spatial.setLocalTranslation(x, y, posicionActual.z);
            Vector3f dirTangente = new Vector3f(-FastMath.sin(ang), FastMath.cos(ang), 0).normalizeLocal();
            float anguloActual = FastMath.atan2(dirTangente.y, dirTangente.x);
            spatial.setLocalRotation(new com.jme3.math.Quaternion().fromAngleAxis(anguloActual, Vector3f.UNIT_Z));
            if (giroAnguloAcumulado >= FastMath.HALF_PI) {
                faseGiro = false;
                faseFinalRecta = true;
                Vector3f p = spatial.getLocalTranslation();
                Vector3f tgt = perpendicularTarget != null ? perpendicularTarget : target;
                direccionMovimientoFinal = tgt.subtract(p).normalize();
            }
            return;
        }

        if (faseFinalRecta) {
            Vector3f nuevaPosicion = spatial.getLocalTranslation().add(direccionMovimientoFinal.mult(velocidad * tpf));
            spatial.setLocalTranslation(nuevaPosicion);
            float anguloActual = FastMath.atan2(direccionMovimientoFinal.y, direccionMovimientoFinal.x);
            spatial.setLocalRotation(new com.jme3.math.Quaternion().fromAngleAxis(anguloActual, Vector3f.UNIT_Z));
            return;
        }
    }
    
    /**
     * ⭐ MEJORADO: Inversión visual más clara
     */
    private void ejecutarInversion() {
        invertido = true;
        
        // Rotar 180 grados para que apunte en dirección opuesta
        spatial.rotate(0, 0, FastMath.PI);
        
        // Flash brillante para indicar inversión
        material.setColor("Color", ColorRGBA.White.mult(2f));
        
        inversionFlashTimer = 0.1f;
        
        System.out.println("⭐ Flecha dorada invertida: " + direccion + " → " + direccion.getOpuesta());
    }
    
    /**
     * ⭐ MEJORADO: Verificación de miss más robusta
     */
    private void verificarMiss() {
        if (fueGolpeada) return;
        
        Vector3f posicionActual = spatial.getLocalTranslation();
        boolean salioDePantalla = false;
        
        if (tipo == TipoFlecha.DORADA) {
            if (enOrbita) {
                salioDePantalla = false;
            } else if (enSalida) {
                float m = 100f;
                Vector3f p = spatial.getLocalTranslation();
                if (p.x < -m || p.x > viewportWidth + m || p.y < -m || p.y > viewportHeight + m) {
                    salioDePantalla = true;
                }
            } else {
                float distanciaTotal = posicionInicial.distance(target);
                if (distanciaRecorrida > distanciaTotal * 1.5f) {
                    salioDePantalla = true;
                }
            }
        } else {
            float distanciaTotal = posicionInicial.distance(target);
            if (distanciaRecorrida > distanciaTotal * 1.2f) {
                salioDePantalla = true;
            }
        }
        
        if (salioDePantalla) {
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

    public void configurarViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void configurarOrbitCenter(Vector3f center) {
        if (center != null) this.orbitCenter = center.clone();
    }

    private Direccion calcularPerpendicular(Direccion d) {
        switch (d) {
            case ARRIBA: return Direccion.IZQUIERDA;
            case ABAJO: return Direccion.DERECHA;
            case IZQUIERDA: return Direccion.ABAJO;
            case DERECHA: return Direccion.ARRIBA;
            default: return d;
        }
    }

    public void configurarOrbita(float radio, float velAngular, float duracion) {
        this.radioOrbita = radio;
        this.velocidadAngular = velAngular;
        this.duracionOrbitaCfg = duracion;
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    public void brillar() {
    brilloActivo = true;
    tiempoBrillo = 0f;
    fueGolpeada = true;
    
    // ⭐ BRILLO INICIAL SUPER INTENSO
    material.setColor("Color", ColorRGBA.White.mult(4.0f));
    
    // Aumentar tamaño para efecto "explosión"
    if (spatial != null) {
        spatial.setLocalScale(1.3f);
    }
    
    System.out.println("✨💥 Flecha golpeada con BRILLO INTENSO: " + tipo + " " + direccion);
}
    
    public void marcarComoErrada() {
        fueErrada = true;
    }
    
    public void marcarComoGolpeada() {
        if (fueGolpeada) {
            return;
        }
        
        fueGolpeada = true;
        brilloActivo = true;
        tiempoBrillo = 0f;
        
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
        return spatial.getLocalTranslation().add(offsetCentro).distance(target);
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
    public Direccion getDireccionActual() {
        if (tipo == TipoFlecha.DORADA && (faseGiro || faseFinalRecta) && direccionPerpendicular != null) {
            return direccionPerpendicular;
        }
        return direccion;
    }
    
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
