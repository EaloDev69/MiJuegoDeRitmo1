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
 * Analizador MEJORADO de canciones para juego de ritmo
 * Usa múltiples detectores y técnicas para maximizar la detección de beats
 * CON NORMALIZACIÓN AUTOMÁTICA DE BPM Y SINCRONIZACIÓN ENTRE TIPOS
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class AnalizadorCanciones {
    
    private final AssetManager assetManager;
    
    // Configuración de análisis mejorada
    private static final int BUFFER_SIZE = 512;
    private static final int OVERLAP = 256;
    
    // === MODO AGRESIVO: Máxima detección ===
    private static final boolean MODO_AGRESIVO = true;
    
    // Parámetros para detección NORMAL (beats principales)
    private static final double SENSITIVITY_NORMAL = 1.5;
    private static final double THRESHOLD_NORMAL = 0.3;
    private static final double MIN_BEAT_DISTANCE_NORMAL = 0.2;
    
    // Parámetros para detección RÁPIDA (fills y dobles)
    private static final double SENSITIVITY_RAPIDO = 0.8;
    private static final double THRESHOLD_RAPIDO = 0.15;
    private static final double MIN_BEAT_DISTANCE_RAPIDO = 0.1;
    
    // Parámetros para detección LENTA (beats marcados)
    private static final double SENSITIVITY_LENTO = 5.0;
    private static final double THRESHOLD_LENTO = 2.0;
    private static final double MIN_BEAT_DISTANCE_LENTO = 0.4;
    
    // === NUEVO: Rangos de BPM válidos ===
    private static final double BPM_MIN_VALIDO = 30.0;
    private static final double BPM_MAX_VALIDO = 220.0;
    private static final double BPM_DENSIDAD_MINIMA = 0.5; // beats por segundo
    
    // BPM global compartido entre tipos
    private double bpmGlobalEstimado = 0.0;
    
    public AnalizadorCanciones(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    /**
     * NUEVO: Normaliza BPM anómalos multiplicando/dividiendo por potencias de 2
     */
    private double normalizarBPM(double bpm) {
        if (bpm <= 0) return 120.0; // Default seguro
        
        double bpmOriginal = bpm;
        
        // Subir BPM muy bajos
        if (bpm < 20) bpm *= 8;
        else if (bpm < 40) bpm *= 4;
        else if (bpm < 60) bpm *= 2;
        
        // Bajar BPM muy altos
        else if (bpm > 240) bpm /= 2;
        else if (bpm > 300) bpm /= 4;
        
        if (bpmOriginal != bpm) {
            System.out.println("   🔧 BPM normalizado: " + 
                String.format("%.1f → %.1f", bpmOriginal, bpm));
        }
        
        return bpm;
    }
    
    /**
     * NUEVO: Calcula BPM considerando densidad de beats
     */
    private double calcularBPMConDensidad(List<Float> beats, double duracionTotal) {
        if (beats.size() < 2) return 0.0;
        
        // BPM básico
        double duracion = beats.get(beats.size() - 1) - beats.get(0);
        double bpm = (beats.size() - 1) / (duracion / 60.0);
        
        // Verificar densidad (beats por segundo)
        double densidad = beats.size() / duracionTotal;
        
        if (densidad < BPM_DENSIDAD_MINIMA) {
            System.out.println("   ⚠️ Densidad baja detectada: " + 
                String.format("%.2f beats/s", densidad));
            bpm *= 4; // Multiplicar para compensar subdetección
        }
        
        return normalizarBPM(bpm);
    }
    
    /**
     * Analiza con detector MEJORADO que combina múltiples técnicas
     */
    public List<Float> analizarCancion(String path) {
        System.out.println("\n📊 Analizando BEATS NORMALES: " + new File(path).getName());
        return analizarConDetectorHibrido(path, SENSITIVITY_NORMAL, THRESHOLD_NORMAL, 
            MIN_BEAT_DISTANCE_NORMAL, "NORMAL");
    }
    
    public List<Float> analizarCancionParaParpadeos(String path) {
        System.out.println("\n⚡ Analizando BEATS RÁPIDOS: " + new File(path).getName());
        return analizarConDetectorHibrido(path, SENSITIVITY_RAPIDO, THRESHOLD_RAPIDO, 
            MIN_BEAT_DISTANCE_RAPIDO, "RÁPIDO");
    }
    
    public List<Float> analizarCancionParaDoradas(String path) {
        System.out.println("\n🌟 Analizando BEATS LENTOS: " + new File(path).getName());
        return analizarConDetectorHibrido(path, SENSITIVITY_LENTO, THRESHOLD_LENTO, 
            MIN_BEAT_DISTANCE_LENTO, "LENTO");
    }
    
    /**
     * DETECTOR HÍBRIDO: Combina PercussionOnsetDetector + ComplexOnsetDetector
     * para maximizar la detección de beats
     */
    private List<Float> analizarConDetectorHibrido(String path, double sensitivity, 
                                                    double threshold, double minDistance, 
                                                    String tipo) {
        List<Float> beats = new ArrayList<>();
        
        try {
            File audioFile = new File(path);
            if (!audioFile.exists()) {
                System.err.println("❌ Archivo no encontrado: " + path);
                return beats;
            }
            
            URL audioURL = audioFile.toURI().toURL();
            
            // === PRIMERA PASADA: Percussion Onset Detector ===
            List<Double> beatsPercusion = detectarConPercussion(audioURL, sensitivity, threshold);
            System.out.println("   Detector Percusión: " + beatsPercusion.size() + " beats");
            
            // === SEGUNDA PASADA: Complex Onset Detector ===
            List<Double> beatsComplex = detectarConComplex(audioURL, sensitivity, threshold);
            System.out.println("   Detector Complejo: " + beatsComplex.size() + " beats");
            
            // === TERCERA PASADA: Detector de Energía ===
            List<Double> beatsEnergia = detectarPorEnergia(audioURL, threshold);
            System.out.println("   Detector Energía: " + beatsEnergia.size() + " beats");
            
            // === COMBINAR TODOS LOS DETECTORES ===
            Set<Double> beatsCombinados = new TreeSet<>();
            beatsCombinados.addAll(beatsPercusion);
            beatsCombinados.addAll(beatsComplex);
            beatsCombinados.addAll(beatsEnergia);
            
            System.out.println("   Total combinado: " + beatsCombinados.size() + " beats");
            
            // === FILTRAR BEATS MUY CERCANOS ===
            List<Double> beatsFiltrados = filtrarBeatsCercanos(new ArrayList<>(beatsCombinados), minDistance);
            
            // === GENERAR BEATS SINTÉTICOS SI HAY MUY POCOS ===
            if (MODO_AGRESIVO && beatsFiltrados.size() < 20) {
                System.out.println("   ⚠️ Pocos beats detectados, generando beats sintéticos...");
                beatsFiltrados = generarBeatsSinteticos(beatsFiltrados, path, tipo);
            }
            
            // Convertir a Float
            for (Double beat : beatsFiltrados) {
                beats.add(beat.floatValue());
            }
            
            System.out.println("   ✓ Final: " + beats.size() + " beats");
            if (!beats.isEmpty()) {
                System.out.println("   Rango: " + String.format("%.2f", beats.get(0)) + "s - " + 
                                 String.format("%.2f", beats.get(beats.size() - 1)) + "s");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return beats;
    }
    
    /**
     * Detector de percusión tradicional
     */
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
    
    /**
     * Detector complejo (mejor para música no percusiva)
     */
    private List<Double> detectarConComplex(URL audioURL, double sensitivity, double threshold) {
        List<Double> beats = new ArrayList<>();
        
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, BUFFER_SIZE, OVERLAP);
            float sampleRate = dispatcher.getFormat().getSampleRate();
            
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
    
    /**
     * Detector basado en cambios de energía RMS
     */
    private List<Double> detectarPorEnergia(URL audioURL, double threshold) {
        List<Double> beats = new ArrayList<>();
        
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromURL(audioURL, BUFFER_SIZE, OVERLAP);
            
            final double[] energiaPrevia = {0.0};
            final double umbralEnergia = threshold * 10;
            
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] buffer = audioEvent.getFloatBuffer();
                    
                    // Calcular energía RMS
                    double energia = 0;
                    for (float sample : buffer) {
                        energia += sample * sample;
                    }
                    energia = Math.sqrt(energia / buffer.length);
                    
                    // Detectar pico de energía
                    if (energia > energiaPrevia[0] * (1.0 + umbralEnergia)) {
                        double tiempo = audioEvent.getTimeStamp();
                        beats.add(tiempo);
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
    
    /**
     * MEJORADO: Genera beats sintéticos usando BPM global si está disponible
     */
    private List<Double> generarBeatsSinteticos(List<Double> beatsOriginales, String path, String tipo) {
        List<Double> beatsExpandidos = new ArrayList<>(beatsOriginales);
        
        if (beatsOriginales.size() < 2) {
            // Usar BPM global si existe, sino default
            double bpmBase;
            if (bpmGlobalEstimado > 0) {
                bpmBase = bpmGlobalEstimado;
                System.out.println("   🔗 Usando BPM global: " + String.format("%.1f", bpmBase));
            } else {
                bpmBase = tipo.equals("RÁPIDO") ? 140 : (tipo.equals("LENTO") ? 80 : 120);
                System.out.println("   📐 Usando BPM predeterminado: " + bpmBase);
            }
            
            double duracion = obtenerDuracionArchivo(path);
            double intervalo = 60.0 / bpmBase;
            
            System.out.println("   Generando grid sintético a " + bpmBase + " BPM");
            for (double t = 1.0; t < duracion; t += intervalo) {
                beatsExpandidos.add(t);
            }
        } else {
            // Estimar BPM de beats existentes
            double duracion = beatsOriginales.get(beatsOriginales.size() - 1) - beatsOriginales.get(0);
            double bpmEstimado = (beatsOriginales.size() - 1) / (duracion / 60.0);
            
            // NUEVO: Normalizar BPM antes de usar
            bpmEstimado = normalizarBPM(bpmEstimado);
            
            double intervalo = 60.0 / bpmEstimado;
            
            System.out.println("   BPM estimado normalizado: " + String.format("%.1f", bpmEstimado));
            System.out.println("   Interpolando beats adicionales...");
            
            // Generar beats entre los detectados
            for (int i = 0; i < beatsOriginales.size() - 1; i++) {
                double inicio = beatsOriginales.get(i);
                double fin = beatsOriginales.get(i + 1);
                double distancia = fin - inicio;
                
                if (distancia > intervalo * 2) {
                    int beatsAGenerar = (int) (distancia / intervalo) - 1;
                    for (int j = 1; j <= beatsAGenerar; j++) {
                        beatsExpandidos.add(inicio + intervalo * j);
                    }
                }
            }
        }
        
        Collections.sort(beatsExpandidos);
        System.out.println("   Beats tras expansión: " + beatsExpandidos.size());
        return beatsExpandidos;
    }
    
    /**
     * Obtiene la duración del archivo de audio
     */
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
            return 180.0;
        }
    }
    
    /**
     * Filtra beats muy cercanos
     */
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
        
        System.out.println("   Filtrado: " + beats.size() + " → " + filtrados.size() + " beats");
        return filtrados;
    }
    
    /**
     * MEJORADO: Análisis completo con normalización y sincronización de BPM
     */
    public ResultadoAnalisis analizarCompleto(String path) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎵 ANÁLISIS MEJORADO V2: " + new File(path).getName());
        System.out.println("=".repeat(60));
        
        long inicio = System.currentTimeMillis();
        
        ResultadoAnalisis resultado = new ResultadoAnalisis();
        resultado.rutaCancion = path;
        resultado.nombreCancion = new File(path).getName();
        
        // Obtener duración total
        resultado.duracionTotal = (float) obtenerDuracionArchivo(path);
        
        // Analizar beats normales primero (establece el BPM base)
        resultado.beatsNormales = analizarCancion(path);
        
        // Calcular BPM base con normalización
        if (!resultado.beatsNormales.isEmpty()) {
            bpmGlobalEstimado = calcularBPMConDensidad(
                resultado.beatsNormales, 
                resultado.duracionTotal
            );
            resultado.bpmBase = bpmGlobalEstimado;
        }
        
        // Analizar otros tipos (pueden usar el BPM global)
        resultado.beatsRapidos = analizarCancionParaParpadeos(path);
        resultado.beatsLentos = analizarCancionParaDoradas(path);
        
        // NUEVO: Sincronizar BPM relativos
        if (resultado.bpmBase > 0) {
            resultado.bpmRapido = resultado.bpmBase * 2.0;
            resultado.bpmLento = resultado.bpmBase * 0.75;
        }
        
        long duracion = System.currentTimeMillis() - inicio;
        
        System.out.println("\n📈 RESUMEN:");
        System.out.println("   Flechas normales:    " + resultado.beatsNormales.size());
        System.out.println("   Flechas especiales:  " + resultado.beatsRapidos.size());
        System.out.println("   Mecánica espacio:    " + resultado.beatsLentos.size());
        System.out.println("   🕒 BPM base:         " + String.format("%.1f", resultado.bpmBase));
        System.out.println("   ⚡ BPM rápido:       " + String.format("%.1f", resultado.bpmRapido));
        System.out.println("   🌟 BPM lento:        " + String.format("%.1f", resultado.bpmLento));
        System.out.println("   ⏱️  Duración:         " + String.format("%.1fs", resultado.duracionTotal));
        System.out.println("   Tiempo análisis: " + duracion + "ms");
        System.out.println("=".repeat(60) + "\n");
        
        // Resetear BPM global para siguiente canción
        bpmGlobalEstimado = 0.0;
        
        return resultado;
    }
    
    /**
     * Análisis múltiple
     */
    public Map<String, ResultadoAnalisis> analizarMultiples(List<String> rutas) {
        Map<String, ResultadoAnalisis> resultados = new LinkedHashMap<>();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎼 ANÁLISIS BATCH MEJORADO V2: " + rutas.size() + " canciones");
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
     * MEJORADO: Clase de resultado con BPM sincronizados
     */
    public static class ResultadoAnalisis {
        public String rutaCancion;
        public String nombreCancion;
        public List<Float> beatsNormales = new ArrayList<>();
        public List<Float> beatsRapidos = new ArrayList<>();
        public List<Float> beatsLentos = new ArrayList<>();
        
        // NUEVO: BPM sincronizados
        public double bpmBase = 0.0;      // BPM normalizado de beats normales
        public double bpmRapido = 0.0;    // bpmBase * 2
        public double bpmLento = 0.0;     // bpmBase * 0.75
        public float duracionTotal = 0.0f;
        
        /**
         * DEPRECADO: Usar bpmBase en su lugar
         */
        @Deprecated
        public double getBPMEstimado() {
            return bpmBase;
        }
        
        public float getDuracionTotal() {
            return duracionTotal;
        }
        
        public boolean esValido() {
            return !beatsNormales.isEmpty() || !beatsRapidos.isEmpty() || !beatsLentos.isEmpty();
        }
        
        @Override
        public String toString() {
            return String.format(
                "ResultadoAnalisis[%s, normal=%d, rapido=%d, lento=%d, bpm=%.1f/%.1f/%.1f]",
                nombreCancion, 
                beatsNormales.size(), 
                beatsRapidos.size(), 
                beatsLentos.size(), 
                bpmBase, bpmRapido, bpmLento
            );
        }
    }
}