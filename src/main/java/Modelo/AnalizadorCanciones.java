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

/**
 * Analizador OPTIMIZADO de canciones para juego de ritmo
 * Ajustado para detectar más beats de manera confiable
 */
public class AnalizadorCanciones {
    private final AssetManager assetManager;
    
    // Configuración OPTIMIZADA para detectar más beats
    private static final int BUFFER_SIZE = 512;
    private static final int OVERLAP = 256;
    
    // Parámetros MÁS SENSIBLES para detectar más beats
    private static final double SENSITIVITY_NORMAL = 3.0;      // Más sensible
    private static final double THRESHOLD_NORMAL = 0.15;        // Umbral más bajo
    private static final double MIN_BEAT_DISTANCE_NORMAL = 0.15; // Permite beats más cercanos
    
    private static final double SENSITIVITY_RAPIDO = 2.0;
    private static final double THRESHOLD_RAPIDO = 0.1;
    private static final double MIN_BEAT_DISTANCE_RAPIDO = 0.08;
    
    private static final double SENSITIVITY_LENTO = 8.0;
    private static final double THRESHOLD_LENTO = 1.5;
    private static final double MIN_BEAT_DISTANCE_LENTO = 0.4;
    
    // ⭐ NUEVO: Modo híbrido con grid sintético
    private static final boolean USAR_GRID_SINTETICO = true;
    private static final int BPM_DEFAULT = 120; // BPM base si no se detectan suficientes beats
    
    public AnalizadorCanciones(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    /**
     * Analiza beats normales (flechas principales)
     */
    public List<Float> analizarCancion(String path) {
        System.out.println("\n📊 Analizando BEATS NORMALES: " + new File(path).getName());
        return analizarConDetectorHibridoOptimizado(path, SENSITIVITY_NORMAL, THRESHOLD_NORMAL, MIN_BEAT_DISTANCE_NORMAL, "NORMAL");
    }
    
    /**
     * Analiza beats rápidos (flechas rápidas)
     */
    public List<Float> analizarCancionParaParpadeos(String path) {
        System.out.println("\n⚡ Analizando BEATS RÁPIDOS: " + new File(path).getName());
        return analizarConDetectorHibridoOptimizado(path, SENSITIVITY_RAPIDO, THRESHOLD_RAPIDO, MIN_BEAT_DISTANCE_RAPIDO, "RÁPIDO");
    }
    
    /**
     * Analiza beats lentos (mecánica especial)
     */
    public List<Float> analizarCancionParaDoradas(String path) {
        System.out.println("\n🌟 Analizando BEATS LENTOS: " + new File(path).getName());
        return analizarConDetectorHibridoOptimizado(path, SENSITIVITY_LENTO, THRESHOLD_LENTO, MIN_BEAT_DISTANCE_LENTO, "LENTO");
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
            
            // === PASO 1: Detección real de beats ===
            List<Double> beatsDetectados = new ArrayList<>();
            
            // Detector de percusión
            List<Double> beatsPercusion = detectarConPercussion(audioURL, sensitivity, threshold);
            System.out.println(" Detector Percusión: " + beatsPercusion.size() + " beats");
            beatsDetectados.addAll(beatsPercusion);
            
            // Detector complejo
            List<Double> beatsComplex = detectarConComplex(audioURL, sensitivity, threshold);
            System.out.println(" Detector Complejo: " + beatsComplex.size() + " beats");
            beatsDetectados.addAll(beatsComplex);
            
            // Detector de energía
            List<Double> beatsEnergia = detectarPorEnergia(audioURL, threshold);
            System.out.println(" Detector Energía: " + beatsEnergia.size() + " beats");
            beatsDetectados.addAll(beatsEnergia);
            
            // Combinar y eliminar duplicados
            Set<Double> beatsCombinados = new TreeSet<>(beatsDetectados);
            System.out.println(" Total combinado: " + beatsCombinados.size() + " beats");
            
            // Filtrar beats muy cercanos
            List<Double> beatsFiltrados = filtrarBeatsCercanos(new ArrayList<>(beatsCombinados), minDistance);
            
            // === PASO 2: Grid sintético si es necesario ===
            if (USAR_GRID_SINTETICO && tipo.equals("NORMAL")) {
                double duracion = obtenerDuracionArchivo(path);
                int beatsEsperados = (int)((duracion / 60.0) * BPM_DEFAULT);
                
                if (beatsFiltrados.size() < beatsEsperados * 0.5) {
                    System.out.println(" ⚠ Pocos beats detectados (" + beatsFiltrados.size() + " de " + beatsEsperados + " esperados)");
                    System.out.println(" 🎵 Generando grid sintético complementario...");
                    beatsFiltrados = generarGridHibrido(beatsFiltrados, duracion, BPM_DEFAULT);
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
        
        // Generar grid sintético
        for (double t = 0.5; t < duracion - 0.5; t += intervalo) {
            // Solo agregar si no hay un beat detectado cercano
            if (!hayBeatCerca(beatsDetectados, t, intervalo * 0.3)) {
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
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, BUFFER_SIZE, OVERLAP);
            float sampleRate = dispatcher.getFormat().getSampleRate();
            
            PercussionOnsetDetector detector = new PercussionOnsetDetector(
                sampleRate, BUFFER_SIZE,
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
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, BUFFER_SIZE, OVERLAP);
            
            ComplexOnsetDetector detector = new ComplexOnsetDetector(
                BUFFER_SIZE,
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
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, BUFFER_SIZE, OVERLAP);
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
                new File(path).toURI().toURL(), BUFFER_SIZE, 0
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
        resultado.beatsNormales = analizarCancion(path);
        resultado.beatsRapidos = analizarCancionParaParpadeos(path);
        resultado.beatsLentos = analizarCancionParaDoradas(path);
        
        long duracion = System.currentTimeMillis() - inicio;
        
        System.out.println("\n📈 RESUMEN:");
        System.out.println(" Flechas normales: " + resultado.beatsNormales.size());
        System.out.println(" Flechas especiales: " + resultado.beatsRapidos.size());
        System.out.println(" Mecánica espacio: " + resultado.beatsLentos.size());
        System.out.println(" Tiempo: " + duracion + "ms");
        System.out.println("=".repeat(60) + "\n");
        
        return resultado;
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
            return maxTime;
        }
        
        public boolean esValido() {
            return !beatsNormales.isEmpty() || !beatsRapidos.isEmpty() || !beatsLentos.isEmpty();
        }
        
        @Override
        public String toString() {
            return String.format("ResultadoAnalisis[%s, normal=%d, rapido=%d, lento=%d, bpm=%.1f]",
                nombreCancion, beatsNormales.size(), beatsRapidos.size(), beatsLentos.size(), getBPMEstimado());
        }
    }
}