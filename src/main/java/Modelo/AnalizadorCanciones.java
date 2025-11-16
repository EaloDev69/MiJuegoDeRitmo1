/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import com.jme3.asset.AssetManager;
import java.io.File;
import java.net.URL;
import java.util.*;
import Modelo.audio.BeatDetector;

/**
 * Analizador OPTIMIZADO de canciones para juego de ritmo
 * Ajustado para detectar más beats de manera confiable
 */
public class AnalizadorCanciones {
    private final AssetManager assetManager;
    private int bufferSize = 512;
    private int overlap = 256;
    private double sensitivityNormal = 3.0;
    private double thresholdNormal = 0.15;
    private double minBeatDistanceNormal = 0.15;
    private double sensitivityRapido = 2.0;
    private double thresholdRapido = 0.1;
    private double minBeatDistanceRapido = 0.08;
    private double sensitivityLento = 8.0;
    private double thresholdLento = 1.5;
    private double minBeatDistanceLento = 0.4;
    private boolean usarGridSintetico = true;
    private int bpmDefault = 120;
    private boolean adaptiveSensitivity = true;
    private double ultimoRms = 0.0;
    private final BeatDetector beatDetector = new BeatDetector();
    
    public AnalizadorCanciones(AssetManager assetManager) {
        this.assetManager = assetManager;
        
    }
    
    /**
     * Analiza beats normales (flechas principales)
     */
    public List<Float> analizarCancion(String path) {
        System.out.println("\n📊 Analizando BEATS NORMALES: " + new File(path).getName());
        return analizarConDetectorHibridoOptimizado(path, sensitivityNormal, thresholdNormal, minBeatDistanceNormal, "NORMAL");
    }
    
    /**
     * Analiza beats rápidos (flechas rápidas)
     */
    public List<Float> analizarCancionParaParpadeos(String path) {
        System.out.println("\n⚡ Analizando BEATS RÁPIDOS: " + new File(path).getName());
        return analizarConDetectorHibridoOptimizado(path, sensitivityRapido, thresholdRapido, minBeatDistanceRapido, "RÁPIDO");
    }
    
    /**
     * Analiza beats lentos (mecánica especial)
     */
    public List<Float> analizarCancionParaDoradas(String path) {
        System.out.println("\n🌟 Analizando BEATS LENTOS: " + new File(path).getName());
        return analizarConDetectorHibridoOptimizado(path, sensitivityLento, thresholdLento, minBeatDistanceLento, "LENTO");
    }
    
    /**
     * DETECTOR HÍBRIDO OPTIMIZADO
     * Combina detección real + grid sintético para garantizar jugabilidad
     */
    private List<Float> analizarConDetectorHibridoOptimizado(String path, double sensitivity, double threshold, double minDistance, String tipo) {
        List<Float> beats = new ArrayList<>();
        
        try {
            File audioFile = new File(path);
            if (!audioFile.exists()) {
                System.err.println("❌ Archivo no encontrado: " + path);
                return beats;
            }
            
            URL audioURL = audioFile.toURI().toURL();
            double rms = calcularRMS(path);
            ultimoRms = rms;
            ParamSet ps = ajustarParametros(tipo, sensitivity, threshold, minDistance, rms);
            
            // === PASO 1: Detección real de beats ===
            List<Double> beatsDetectados = new ArrayList<>();
            
            // Detector de percusión
            List<Double> beatsPercusion = detectarConPercussion(audioURL, ps.s, ps.t);
            System.out.println(" Detector Percusión: " + beatsPercusion.size() + " beats");
            beatsDetectados.addAll(beatsPercusion);
            
            // Detector complejo
            List<Double> beatsComplex = detectarConComplex(audioURL, ps.s, ps.t);
            System.out.println(" Detector Complejo: " + beatsComplex.size() + " beats");
            beatsDetectados.addAll(beatsComplex);
            
            // Detector de energía
            List<Double> beatsEnergia = detectarPorEnergia(audioURL, ps.t);
            System.out.println(" Detector Energía: " + beatsEnergia.size() + " beats");
            beatsDetectados.addAll(beatsEnergia);
            
            // Combinar y eliminar duplicados
            Set<Double> beatsCombinados = new TreeSet<>(beatsDetectados);
            System.out.println(" Total combinado: " + beatsCombinados.size() + " beats");
            
            // Filtrar beats muy cercanos
            List<Double> beatsFiltrados = filtrarBeatsCercanos(new ArrayList<>(beatsCombinados), ps.d);
            double intervaloEstimado = calcularIntervaloEstimado(beatsFiltrados);
            if (intervaloEstimado > 0) {
                beatsFiltrados = sanitizarPorIntervalo(beatsFiltrados, intervaloEstimado, tipo);
            }
            
            if (usarGridSintetico && tipo.equals("NORMAL")) {
                double duracion = obtenerDuracionArchivo(path);
                float bpmEstimado = estimateBpmFrom(beatsFiltrados);
                float bpmBase = bpmEstimado > 0 ? ajustarBpmRango(bpmEstimado) : bpmDefault;
                int beatsEsperados = (int)((duracion / 60.0) * bpmBase);
                if (beatsFiltrados.size() < beatsEsperados * 0.5) {
                    beatsFiltrados = generarGridHibrido(beatsFiltrados, duracion, (int)bpmBase);
                }
            }
            
            // Convertir a Float
            for (Double beat : beatsFiltrados) {
                beats.add(beat.floatValue());
            }
            
            System.out.println(" ✓ Final: " + beats.size() + " beats");
            if (!beats.isEmpty()) {
                System.out.println(" Rango: " + String.format("%.2f", beats.get(0)) + "s - " + String.format("%.2f", beats.get(beats.size() - 1)) + "s");
                
                // Mostrar densidad de beats
                if (beats.size() > 1) {
                    float duracionTotal = beats.get(beats.size() - 1) - beats.get(0);
                    float densidad = beats.size() / duracionTotal;
                    System.out.println(" Densidad: " + String.format("%.2f", densidad) + " beats/segundo");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return beats;
    }
    
    /**
     * Genera grid híbrido: combina beats detectados + grid sintético
     */
    private List<Double> generarGridHibrido(List<Double> beatsDetectados, double duracion, int bpmBase) {
        List<Double> gridHibrido = new ArrayList<>(beatsDetectados);
        double intervalo = 60.0 / bpmBase;
        double inicio = 0.0;
        if (!beatsDetectados.isEmpty()) {
            double primero = beatsDetectados.get(0);
            inicio = Math.floor(primero / intervalo) * intervalo;
        } else {
            inicio = intervalo * 0.5; // fase neutra
        }
        double fin = Math.max(0.0, duracion - intervalo * 0.25);
        for (double t = inicio; t <= fin; t += intervalo) {
            if (!hayBeatCerca(beatsDetectados, t, intervalo * 0.25)) {
                gridHibrido.add(t);
            }
        }
        
        Collections.sort(gridHibrido);
        System.out.println(" Grid híbrido: " + beatsDetectados.size() + " detectados + " + (gridHibrido.size() - beatsDetectados.size()) + " sintéticos = " + gridHibrido.size() + " total");
        
        return gridHibrido;
    }
    
    private boolean hayBeatCerca(List<Double> beats, double tiempo, double tolerancia) {
        for (Double beat : beats) {
            if (Math.abs(beat - tiempo) < tolerancia) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== DETECTORES ====================
    
    private List<Double> detectarConPercussion(URL audioURL, double sensitivity, double threshold) {
        List<Double> beats = new ArrayList<>();
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, bufferSize, overlap);
            float sampleRate = dispatcher.getFormat().getSampleRate();
            
            PercussionOnsetDetector detector = new PercussionOnsetDetector(
                sampleRate, bufferSize,
                (time, salience) -> beats.add(time),
                sensitivity, threshold
            );
            
            dispatcher.addAudioProcessor(detector);
            dispatcher.run();
        } catch (Exception e) {
            System.err.println("Error en detector de percusión: " + e.getMessage());
        }
        return beats;
    }
    
    private List<Double> detectarConComplex(URL audioURL, double sensitivity, double threshold) {
        List<Double> beats = new ArrayList<>();
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, bufferSize, overlap);
            
            ComplexOnsetDetector detector = new ComplexOnsetDetector(
                bufferSize,
                sensitivity / 2.0,
                threshold / 2.0
            );
            
            detector.setHandler((time, salience) -> beats.add(time));
            dispatcher.addAudioProcessor(detector);
            dispatcher.run();
        } catch (Exception e) {
            System.err.println("Error en detector complejo: " + e.getMessage());
        }
        return beats;
    }
    
    private List<Double> detectarPorEnergia(URL audioURL, double threshold) {
        List<Double> beats = new ArrayList<>();
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, bufferSize, overlap);
            final double[] energiaPrevia = {0.0};
            final double umbralEnergia = threshold * 8; // Ajustado
            
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] buffer = audioEvent.getFloatBuffer();
                    
                    double energia = 0;
                    for (float sample : buffer) {
                        energia += sample * sample;
                    }
                    energia = Math.sqrt(energia / buffer.length);
                    
                    if (energia > energiaPrevia[0] * (1.0 + umbralEnergia)) {
                        beats.add(audioEvent.getTimeStamp());
                    }
                    
                    energiaPrevia[0] = energia * 0.9 + energiaPrevia[0] * 0.1;
                    return true;
                }
                
                @Override
                public void processingFinished() {}
            });
            
            dispatcher.run();
        } catch (Exception e) {
            System.err.println("Error en detector de energía: " + e.getMessage());
        }
        return beats;
    }
    
    private List<Double> filtrarBeatsCercanos(List<Double> beats, double minDistance) {
        if (beats.isEmpty()) return new ArrayList<>();
        
        List<Double> filtrados = new ArrayList<>();
        Collections.sort(beats);
        
        double ultimoBeat = beats.get(0);
        filtrados.add(ultimoBeat);
        
        for (int i = 1; i < beats.size(); i++) {
            double beatActual = beats.get(i);
            if (beatActual - ultimoBeat >= minDistance) {
                filtrados.add(beatActual);
                ultimoBeat = beatActual;
            }
        }
        
        return filtrados;
    }
    
    private double obtenerDuracionArchivo(String path) {
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(
                new File(path).toURI().toURL(), bufferSize, 0
            );
            
            final double[] duracion = {0};
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    duracion[0] = audioEvent.getTimeStamp();
                    return true;
                }
                
                @Override
                public void processingFinished() {}
            });
            
            dispatcher.run();
            return duracion[0];
        } catch (Exception e) {
            return 180.0; // Default 3 minutos
        }
    }

    private double calcularIntervaloEstimado(List<Double> beats) {
        if (beats == null || beats.size() < 4) return 0.0;
        List<Float> bf = new ArrayList<>();
        for (Double d : beats) bf.add(d.floatValue());
        float bpm = beatDetector.estimateBpmRobusto(bf);
        if (bpm <= 0f) return 0.0;
        return 60.0 / bpm;
    }

    private List<Double> sanitizarPorIntervalo(List<Double> beats, double intervalo, String tipo) {
        Collections.sort(beats);
        List<Double> out = new ArrayList<>();
        double factorMin = 0.8; // NORMAL
        if ("RÁPIDO".equals(tipo)) factorMin = 0.5;
        if ("LENTO".equals(tipo)) factorMin = 1.2;
        double snapTol = intervalo * 0.15;
        double ultimo = -1e9;
        for (double b : beats) {
            if (b - ultimo >= intervalo * factorMin) {
                double n = Math.round(b / intervalo);
                double snapped = n * intervalo;
                double usar = Math.abs(snapped - b) <= snapTol ? snapped : b;
                out.add(usar);
                ultimo = usar;
            }
        }
        return out;
    }
    /**
     * Análisis completo con todos los detectores
     */
    public ResultadoAnalisis analizarCompleto(String path) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎵 ANÁLISIS OPTIMIZADO: " + new File(path).getName());
        System.out.println("=".repeat(60));
        
        long inicio = System.currentTimeMillis();
        
        ResultadoAnalisis resultado = new ResultadoAnalisis();
        resultado.rutaCancion = path;
        resultado.nombreCancion = new File(path).getName();
        resultado.duracionAudio = (float) obtenerDuracionArchivo(path);
        resultado.beatsNormales = analizarCancion(path);
        resultado.beatsRapidos = analizarCancionParaParpadeos(path);
        resultado.beatsLentos = analizarCancionParaDoradas(path);
        resultado.segmentosBpm = estimarSegmentosBpm(resultado.beatsNormales);
        resultado.secciones = estimarSecciones(resultado.beatsNormales, resultado.beatsRapidos, resultado.getDuracionTotal());
        
        long duracion = System.currentTimeMillis() - inicio;
        
        System.out.println("\n📈 RESUMEN:");
        System.out.println(" Flechas normales: " + resultado.beatsNormales.size());
        System.out.println(" Flechas especiales: " + resultado.beatsRapidos.size());
        System.out.println(" Mecánica espacio: " + resultado.beatsLentos.size());
        System.out.println(" Tiempo: " + duracion + "ms");
        System.out.println("=".repeat(60) + "\n");
        
        return resultado;
    }

    private List<ResultadoAnalisis.SegmentoBpm> estimarSegmentosBpm(List<Float> beats) {
        List<ResultadoAnalisis.SegmentoBpm> segmentos = new ArrayList<>();
        if (beats == null || beats.size() < 8) return segmentos;
        int w = 8;
        float prevInterval = 0f;
        float segInicio = beats.get(0);
        for (int i = w; i < beats.size(); i++) {
            List<Float> window = beats.subList(i - w, i);
            float bpm = beatDetector.estimateBpmRobusto(window);
            float interval = beatDetector.intervalFromBpm(bpm);
            if (prevInterval <= 0f) {
                prevInterval = interval;
                segInicio = window.get(0);
                continue;
            }
            float cambio = Math.abs(interval - prevInterval) / Math.max(0.0001f, prevInterval);
            if (cambio > 0.07f) {
                ResultadoAnalisis.SegmentoBpm seg = new ResultadoAnalisis.SegmentoBpm();
                seg.inicio = segInicio;
                seg.fin = window.get(0);
                seg.bpm = prevInterval > 0f ? 60f / prevInterval : 0f;
                seg.beatsPorCompas = detectarCompas(window, prevInterval);
                segmentos.add(seg);
                segInicio = window.get(0);
                prevInterval = interval;
            } else {
                prevInterval = (prevInterval * 0.7f) + (interval * 0.3f);
            }
        }
        ResultadoAnalisis.SegmentoBpm ultimo = new ResultadoAnalisis.SegmentoBpm();
        ultimo.inicio = segInicio;
        ultimo.fin = beats.get(beats.size() - 1);
        ultimo.bpm = prevInterval > 0f ? 60f / prevInterval : 0f;
        ultimo.beatsPorCompas = detectarCompas(beats, prevInterval);
        segmentos.add(ultimo);
        return segmentos;
    }

    private int detectarCompas(List<Float> beats, float interval) {
        if (beats == null || beats.size() < 6 || interval <= 0f) return 4;
        int c3 = scoreCompas(beats, interval, 3);
        int c4 = scoreCompas(beats, interval, 4);
        if (c3 > c4 * 1.15f) return 3;
        return 4;
    }

    private int scoreCompas(List<Float> beats, float interval, int beatsPorCompas) {
        if (beatsPorCompas <= 0) return 0;
        float bar = interval * beatsPorCompas;
        float start = beats.get(0);
        int aligned = 0;
        for (float b : beats) {
            float rel = b - start;
            float pos = rel % bar;
            float near = Math.round(pos / interval) * interval;
            float err = Math.abs(pos - near);
            if (err <= interval * 0.12f) aligned++;
        }
        return aligned;
    }

    private List<ResultadoAnalisis.SeccionMusical> estimarSecciones(List<Float> beatsNormales, List<Float> beatsRapidos, float duracion) {
        List<ResultadoAnalisis.SeccionMusical> secciones = new ArrayList<>();
        if (duracion <= 0f) return secciones;
        float[] puntos = new float[] { 0.0f, 0.12f, 0.35f, 0.60f, 0.82f, 0.94f, 1.0f };
        ResultadoAnalisis.TipoSeccion[] tipos = new ResultadoAnalisis.TipoSeccion[] {
            ResultadoAnalisis.TipoSeccion.INTRO,
            ResultadoAnalisis.TipoSeccion.VERSO,
            ResultadoAnalisis.TipoSeccion.CORO,
            ResultadoAnalisis.TipoSeccion.PUENTE,
            ResultadoAnalisis.TipoSeccion.CLIMAX,
            ResultadoAnalisis.TipoSeccion.FINAL
        };
        for (int i = 0; i < tipos.length; i++) {
            float ini = duracion * puntos[i];
            float fin = duracion * puntos[i + 1];
            ResultadoAnalisis.SeccionMusical s = new ResultadoAnalisis.SeccionMusical();
            s.inicio = ini;
            s.fin = fin;
            s.tipo = tipos[i];
            s.intensidad = estimarIntensidadLocal(beatsNormales, beatsRapidos, ini, fin);
            secciones.add(s);
        }
        return secciones;
    }

    private ResultadoAnalisis.Intensidad estimarIntensidadLocal(List<Float> normales, List<Float> rapidos, float ini, float fin) {
        int n = contarEnRango(normales, ini, fin);
        int r = contarEnRango(rapidos, ini, fin);
        float dur = Math.max(0.001f, fin - ini);
        float dps = n / dur;
        float rr = n > 0 ? (float) r / n : 0f;
        if (dps < 1.0f && rr < 0.15f) return ResultadoAnalisis.Intensidad.BAJA;
        if (dps < 2.0f && rr < 0.25f) return ResultadoAnalisis.Intensidad.MEDIA;
        if (dps < 3.0f || rr < 0.40f) return ResultadoAnalisis.Intensidad.ALTA;
        return ResultadoAnalisis.Intensidad.EXTREMA;
    }

    private int contarEnRango(List<Float> lista, float ini, float fin) {
        if (lista == null || lista.isEmpty()) return 0;
        int c = 0;
        for (float t : lista) { if (t >= ini && t < fin) c++; }
        return c;
    }
    
    public Map<String, ResultadoAnalisis> analizarMultiples(List<String> rutas) {
        Map<String, ResultadoAnalisis> resultados = new LinkedHashMap<>();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎼 ANÁLISIS BATCH: " + rutas.size() + " canciones");
        System.out.println("=".repeat(70));
        
        int progreso = 0;
        for (String ruta : rutas) {
            progreso++;
            System.out.println("\n[" + progreso + "/" + rutas.size() + "]");
            
            try {
                ResultadoAnalisis resultado = analizarCompleto(ruta);
                resultados.put(ruta, resultado);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ COMPLETADO: " + resultados.size() + " canciones");
        System.out.println("=".repeat(70) + "\n");
        
        return resultados;
    }
    
    /**
     * Clase de resultado
     */
    public static class ResultadoAnalisis {
        public String rutaCancion;
        public String nombreCancion;
        public List<Float> beatsNormales = new ArrayList<>();
        public List<Float> beatsRapidos = new ArrayList<>();
        public List<Float> beatsLentos = new ArrayList<>();
        public List<SegmentoBpm> segmentosBpm = new ArrayList<>();
        public List<SeccionMusical> secciones = new ArrayList<>();
        public float duracionAudio = 0f;
        
        public double getBPMEstimado() {
            if (beatsNormales.size() < 10) return 0.0;
            double duracion = beatsNormales.get(beatsNormales.size() - 1) - beatsNormales.get(0);
            return (beatsNormales.size() - 1) / (duracion / 60.0);
        }
        
        public float getDuracionTotal() {
            float maxTime = 0f;
            if (!beatsNormales.isEmpty()) maxTime = Math.max(maxTime, beatsNormales.get(beatsNormales.size() - 1));
            if (!beatsRapidos.isEmpty()) maxTime = Math.max(maxTime, beatsRapidos.get(beatsRapidos.size() - 1));
            if (!beatsLentos.isEmpty()) maxTime = Math.max(maxTime, beatsLentos.get(beatsLentos.size() - 1));
            return maxTime > 0f ? maxTime : duracionAudio;
        }
        
        public boolean esValido() {
            return !beatsNormales.isEmpty() || !beatsRapidos.isEmpty() || !beatsLentos.isEmpty();
        }
        
        @Override
        public String toString() {
            return String.format("ResultadoAnalisis[%s, normal=%d, rapido=%d, lento=%d, bpm=%.1f]",
                nombreCancion, beatsNormales.size(), beatsRapidos.size(), beatsLentos.size(), getBPMEstimado());
        }

        public static class SegmentoBpm {
            public float inicio;
            public float fin;
            public float bpm;
            public int beatsPorCompas;
        }

        public enum TipoSeccion { INTRO, VERSO, PUENTE, CORO, CLIMAX, FINAL }
        public enum Intensidad { BAJA, MEDIA, ALTA, EXTREMA }
        public static class SeccionMusical {
            public float inicio;
            public float fin;
            public TipoSeccion tipo;
            public Intensidad intensidad;
        }
    }

    public void setBufferParams(int bufferSize, int overlap) {
        this.bufferSize = Math.max(128, bufferSize);
        this.overlap = Math.max(0, Math.min(this.bufferSize - 1, overlap));
    }
    public void setDetectorParamsNormal(double sensitivity, double threshold, double minDistance) {
        this.sensitivityNormal = sensitivity;
        this.thresholdNormal = threshold;
        this.minBeatDistanceNormal = minDistance;
    }
    public void setDetectorParamsRapido(double sensitivity, double threshold, double minDistance) {
        this.sensitivityRapido = sensitivity;
        this.thresholdRapido = threshold;
        this.minBeatDistanceRapido = minDistance;
    }
    public void setDetectorParamsLento(double sensitivity, double threshold, double minDistance) {
        this.sensitivityLento = sensitivity;
        this.thresholdLento = threshold;
        this.minBeatDistanceLento = minDistance;
    }
    public void setUsarGridSintetico(boolean usar) { this.usarGridSintetico = usar; }
    public void setBpmDefault(int bpm) { this.bpmDefault = Math.max(40, Math.min(240, bpm)); }
    public void setAdaptiveSensitivity(boolean enabled) { this.adaptiveSensitivity = enabled; }
    public double getUltimoRms() { return ultimoRms; }

    private static class ParamSet { double s; double t; double d; ParamSet(double s,double t,double d){this.s=s;this.t=t;this.d=d;} }
    private ParamSet ajustarParametros(String tipo, double s, double t, double d, double rms) {
        if (!adaptiveSensitivity) return new ParamSet(s,t,d);
        double sMul = 1.0;
        double tMul = 1.0;
        double dAdd = 0.0;
        if (rms < 0.03) { sMul = 1.4; tMul = 0.7; dAdd = -0.02; }
        else if (rms < 0.06) { sMul = 1.2; tMul = 0.85; dAdd = -0.01; }
        else if (rms > 0.12) { sMul = 0.85; tMul = 1.25; dAdd = 0.02; }
        else if (rms > 0.08) { sMul = 0.95; tMul = 1.10; dAdd = 0.01; }
        if ("RÁPIDO".equals(tipo)) { sMul = 1.0 + (sMul - 1.0) * 0.7; tMul = 1.0 + (tMul - 1.0) * 0.7; dAdd *= 0.5; }
        if ("LENTO".equals(tipo)) { sMul = 1.0 + (sMul - 1.0) * 0.5; tMul = 1.0 + (tMul - 1.0) * 0.5; dAdd *= 0.3; }
        double ns = s * sMul;
        double nt = t * tMul;
        double nd = Math.max(0.06, d + dAdd);
        return new ParamSet(ns, nt, nd);
    }

    private double calcularRMS(String path) {
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(new File(path).toURI().toURL(), bufferSize, overlap);
            final double[] acc = {0.0};
            final int[] frames = {0};
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] buf = audioEvent.getFloatBuffer();
                    double e = 0.0;
                    for (float v : buf) e += v * v;
                    e = Math.sqrt(e / buf.length);
                    acc[0] += e;
                    frames[0] += 1;
                    return true;
                }
                @Override
                public void processingFinished() {}
            });
            dispatcher.run();
            return frames[0] > 0 ? acc[0] / frames[0] : 0.0;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private float estimateBpmFrom(List<Double> beats) {
        if (beats == null || beats.size() < 4) return 0f;
        List<Float> bf = new ArrayList<>();
        for (Double d : beats) bf.add(d.floatValue());
        return beatDetector.estimateBpmRobusto(bf);
    }
    private float ajustarBpmRango(float bpm) {
        if (bpm <= 0f) return 0f;
        if (bpm > 180f) return bpm * 0.5f;
        if (bpm < 70f) return bpm * 2f;
        return bpm;
    }
}