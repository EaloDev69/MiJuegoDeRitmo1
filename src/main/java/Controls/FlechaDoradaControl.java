package Controls;

import Modelo.Direccion;
import Modelo.TipoFlecha;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;

public class FlechaDoradaControl extends AbstractControl {
    private final TipoFlecha tipo;
    private final Direccion direccion;
    private final Vector3f posicionInicial;
    private final Vector3f target;
    private final float velocidad;
    private final float beatTime;
    private final Material material;

    private boolean invertido = false;
    private float distanciaInversion;
    private float distanciaRecorrida = 0f;
    private boolean enOrbita = false;
    private boolean enSalida = false;
    private float anguloOrbita = 0f;
    private float radioOrbita = 40f;
    private float velocidadAngular = 3.5f;
    private float tiempoOrbita = 0f;
    private float duracionOrbitaCfg = 1.2f;
    private float viewportWidth = 1280f;
    private float viewportHeight = 720f;

    private Vector3f direccionMovimiento;
    private Vector3f direccionMovimientoInvertida;
    private Vector3f offsetCentro = new Vector3f(0, 0, 0);

    private boolean brilloActivo = false;
    private float tiempoBrillo = 0f;
    private static final float DURACION_BRILLO = 0.3f;
    private float tiempoBrilloPulsante = 0f;
    private ColorRGBA colorBase;

    private boolean fueGolpeada = false;
    private boolean fueErrada = false;

    public FlechaDoradaControl(TipoFlecha tipo, Direccion direccion, Vector3f posicionInicial,
                               Vector3f target, float velocidad, float beatTime, Material material) {
        this.tipo = tipo;
        this.direccion = direccion;
        this.posicionInicial = posicionInicial.clone();
        this.target = target.clone();
        this.velocidad = velocidad;
        this.beatTime = beatTime;
        this.material = material;

        this.direccionMovimiento = target.subtract(posicionInicial).normalize();
        this.distanciaInversion = posicionInicial.distance(target) / 2f;
        this.direccionMovimientoInvertida = direccionMovimiento.negate();

        this.colorBase = material.getParamValue("Color");
        if (this.colorBase == null) {
            this.colorBase = ColorRGBA.White.clone();
        }
        material.setColor("Color", colorBase.mult(1.3f));
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (spatial == null || fueErrada) return;

        if (offsetCentro.equals(Vector3f.ZERO)) {
            if (spatial instanceof Geometry) {
                Geometry g = (Geometry) spatial;
                com.jme3.bounding.BoundingBox bb = (com.jme3.bounding.BoundingBox) g.getMesh().getBound();
                float w = bb.getXExtent() * 2f;
                float h = bb.getYExtent() * 2f;
                offsetCentro.set(w / 2f, h / 2f, 0f);
            }
        }

        actualizarEfectos(tpf);

        if (fueGolpeada) {
            tiempoBrillo += tpf;
            if (tiempoBrillo < DURACION_BRILLO) {
                float escala = 1f - (tiempoBrillo / DURACION_BRILLO);
                escala = Math.max(escala, 0.1f);
                spatial.setLocalScale(escala);
            } else {
                spatial.removeFromParent();
            }
            return;
        }

        mover(tpf);
        verificarMiss();
    }

    private void actualizarEfectos(float tpf) {
        if (brilloActivo) {
            tiempoBrillo += tpf;
            if (tiempoBrillo >= DURACION_BRILLO) {
                brilloActivo = false;
                tiempoBrillo = 0f;
                material.setColor("Color", colorBase.mult(1.3f));
            }
            return;
        }
        tiempoBrilloPulsante += tpf * 3f;
        float intensidad = 1.2f + 0.3f * FastMath.sin(tiempoBrilloPulsante);
        material.setColor("Color", colorBase.mult(intensidad));
    }

    private void mover(float tpf) {
        Vector3f posEsquina = spatial.getLocalTranslation();
        Vector3f posCentro = posEsquina.add(offsetCentro);

        if (enOrbita) {
            tiempoOrbita += tpf;
            anguloOrbita += velocidadAngular * tpf;
            float x = target.x + FastMath.cos(anguloOrbita) * radioOrbita;
            float y = target.y + FastMath.sin(anguloOrbita) * radioOrbita;
            Vector3f nueva = new Vector3f(x, y, posCentro.z).subtract(offsetCentro);
            spatial.setLocalTranslation(nueva);
            if (tiempoOrbita >= duracionOrbitaCfg) {
                enSalida = true;
                enOrbita = false;
            }
            return;
        }

        if (enSalida) {
            Vector3f dirSalida = posCentro.subtract(target).normalizeLocal();
            float d = velocidad * tpf;
            Vector3f nueva = posEsquina.add(dirSalida.mult(d));
            spatial.setLocalTranslation(nueva);
            distanciaRecorrida += d;
            return;
        }

        Vector3f dir = invertido ? direccionMovimientoInvertida : direccionMovimiento;
        float dMov = velocidad * tpf;
        Vector3f toTarget = target.subtract(posCentro);
        float distTarget = toTarget.length();
        if (distTarget <= dMov) {
            Vector3f nueva = target.subtract(offsetCentro);
            spatial.setLocalTranslation(nueva);
            enOrbita = true;
            invertido = false;
            distanciaRecorrida = posicionInicial.distance(target);
            Vector3f v = posCentro.subtract(target);
            radioOrbita = Math.max(28f, Math.min(60f, v.length()));
            anguloOrbita = FastMath.atan2(v.y, v.x);
            tiempoOrbita = 0f;
        } else {
            distanciaRecorrida += dMov;
            Vector3f nueva = posEsquina.add(dir.mult(dMov));
            spatial.setLocalTranslation(nueva);
            if (!invertido && distanciaRecorrida >= distanciaInversion) {
                ejecutarInversion();
            }
        }
    }

    private void ejecutarInversion() {
        invertido = true;
        spatial.rotate(0, 0, FastMath.PI);
        material.setColor("Color", ColorRGBA.White.mult(2f));
    }

    private void verificarMiss() {
        if (fueGolpeada) return;
        Vector3f p = spatial.getLocalTranslation();
        boolean out = false;
        if (enOrbita) {
            out = false;
        } else if (enSalida) {
            float m = 100f;
            if (p.x < -m || p.x > viewportWidth + m || p.y < -m || p.y > viewportHeight + m) {
                out = true;
            }
        } else {
            float distanciaTotal = posicionInicial.distance(target);
            if (distanciaRecorrida > distanciaTotal * 1.5f) {
                out = true;
            }
        }
        if (out) {
            fueErrada = true;
            material.setColor("Color", ColorRGBA.DarkGray);
        }
    }

    public void configurarViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void configurarOrbita(float radio, float velAngular, float duracion) {
        this.radioOrbita = radio;
        this.velocidadAngular = velAngular;
        this.duracionOrbitaCfg = duracion;
    }

    public void brillar() {
        brilloActivo = true;
        tiempoBrillo = 0f;
        fueGolpeada = true;
        material.setColor("Color", ColorRGBA.White.mult(2.5f));
    }

    public void marcarComoErrada() { fueErrada = true; }

    public void marcarComoGolpeada() {
        if (fueGolpeada) return;
        fueGolpeada = true;
        brilloActivo = true;
        tiempoBrillo = 0f;
        material.setColor("Color", ColorRGBA.White.mult(2.5f));
    }

    public float calcularDelay(float tiempoActual) { return Math.abs(tiempoActual - beatTime); }
    public boolean estaEnRangoDeGolpe(float tiempoActual, float rangoPermitido) { return calcularDelay(tiempoActual) < rangoPermitido; }
    public float getDistanciaAlTarget() { if (spatial == null) return Float.MAX_VALUE; return spatial.getLocalTranslation().add(offsetCentro).distance(target); }
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

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}