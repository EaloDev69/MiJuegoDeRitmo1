/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import Modelo.RhythmPatternEngine.Pattern;
import Modelo.audio.BeatDetector;
import Modelo.audio.SongClassifier;
import java.util.*;

/**
 * Generador inteligente de flechas basado en el análisis de beats
 * Distribuye los tres tipos de flechas según el ritmo y intensidad
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechasGenerator {
    private final Random random;
    private final RhythmPatternEngine patternEngine;
    private final SongClassifier songClassifier;
    private final BeatDetector beatDetector;
    private float probabilidadRapida = 0.20f;
    private float probabilidadDorada = 0.15f;
    private boolean generarPatronesRitmicos = true;
    private int flechasConsecutivasMismaDireccion = 0;
    private Direccion ultimaDireccion = null;
    private float ventanaAPS = 1.0f;

    
    public enum Difficulty { FACIL, NORMAL, DIFICIL }
    private Difficulty difficulty = Difficulty.NORMAL;
    
    private Difficulty dificultad = Difficulty.NORMAL;
    private enum TimeSignature { FOUR_FOUR, THREE_FOUR }
    private int barrasIntensasConsecutivas = 0;

    public FlechasGenerator() {
        this.random = new Random();
        this.patternEngine = new RhythmPatternEngine(0.005f);
        this.songClassifier = new SongClassifier();
        this.beatDetector = new BeatDetector();
    }

    public FlechasGenerator(long seed) {
        this.random = new Random(seed);
        this.patternEngine = new RhythmPatternEngine(0.005f, seed);
        this.songClassifier = new SongClassifier();
        this.beatDetector = new BeatDetector();
    }
    /**
 * ⭐ Establece la dificultad
 */
public void setDifficulty(Difficulty difficulty) {
    this.difficulty = difficulty;
    
    // Ajustar probabilidades según dificultad
    switch(difficulty) {
        case FACIL:
            setProbabilidades(0.10f, 0.05f);
            break;
        case NORMAL:
            setProbabilidades(0.20f, 0.15f);
            break;
        case DIFICIL:
            setProbabilidades(0.35f, 0.25f);
            break;
    }
    
    System.out.println("⚙ Dificultad establecida: " + difficulty);
}

/**
 * ⭐ Obtiene la dificultad actual
 */
public Difficulty getDifficulty() {
    return difficulty;
}
    
    /**
     * Genera todas las flechas para una canción analizada
     * 
     * @param resultado El resultado del análisis de la canción
     * @return Lista ordenada de flechas a generar
     */
    public List<FlechaData> generarFlechasCompletas(ResultadoAnalisis resultado) {
        List<FlechaData> flechas = new ArrayList<>();
        System.out.println("\n=== GENERANDO FLECHAS ===");
        System.out.println("Beats normales: " + resultado.beatsNormales.size());
        System.out.println("Beats rápidos: " + resultado.beatsRapidos.size());
        System.out.println("Beats lentos: " + resultado.beatsLentos.size());

        Set<Float> beatsRapidosSet = new HashSet<>(resultado.beatsRapidos);
        Set<Float> beatsDoradsSet = new HashSet<>(resultado.beatsLentos);

        if (resultado.segmentosBpm != null && !resultado.segmentosBpm.isEmpty()) {
            for (ResultadoAnalisis.SegmentoBpm seg : resultado.segmentosBpm) {
                float intervalo = beatDetector.intervalFromBpm(seg.bpm);
                float maxAPS = calcularMaxAPS(seg.bpm);
                ajustarProbabilidadesPorBpm(seg.bpm);
                int beatsPorCompas = Math.max(1, seg.beatsPorCompas);
                float t = seg.inicio;
                while (t + intervalo * beatsPorCompas <= seg.fin + 0.0001f) {
                    ResultadoAnalisis.Intensidad inten = obtenerIntensidadEnTiempo(resultado, t);
                    Pattern pat = seleccionarPatronPorIntensidad(intervalo, inten);
                    RhythmPatternEngine.Subdivision sub = seleccionarSubdivisionPorIntensidad(inten);
                    List<Float> tiempos = patternEngine.generateMeasure(t, intervalo, beatsPorCompas, pat, sub);
                    tiempos = validarCompas(tiempos, t, intervalo * beatsPorCompas, maxAPS);
                    for (float nt : tiempos) {
                        if (nt < 0f) continue;
                        if (existeFlechaCerca(flechas, nt, 0.03f)) continue;
                        if (!permitirPorAPS(flechas, nt, maxAPS)) continue;
                        TipoFlecha tipo = determinarTipoFlecha(nt, beatsRapidosSet, beatsDoradsSet);
                        Direccion direccion = generarDireccionInteligente(tipo, nt);
                        flechas.add(new FlechaData(nt, direccion, tipo));
                    }
                    t += intervalo * beatsPorCompas;
                }
            }
            List<FlechaData> doradasCirculares = generarDoradasCircularesExtendidas(resultado);
            insertarEspeciales(flechas, doradasCirculares);
        } else {
            float bpm = (float) resultado.getBPMEstimado();
            float intervaloBeat = beatDetector.chooseInterval(resultado.beatsNormales, bpm);
            SongClassifier.PatternType pt = songClassifier.classify(resultado);
            Pattern pattern = ajustarPatternPorIntervalo(intervaloBeat, mapPattern(pt));
            float maxAPS = calcularMaxAPS(bpm);
            ajustarProbabilidadesPorBpm(bpm);
            if (intervaloBeat > 0f && !resultado.beatsNormales.isEmpty()) {
                List<Float> tiemposBarra = generarCompases(resultado.beatsNormales, intervaloBeat, seleccionarCompas(resultado, intervaloBeat), pattern, maxAPS);
                for (float t : tiemposBarra) {
                    if (t < 0f) continue;
                    if (existeFlechaCerca(flechas, t, 0.03f)) continue;
                    if (!permitirPorAPS(flechas, t, maxAPS)) continue;
                    TipoFlecha tipo = determinarTipoFlecha(t, beatsRapidosSet, beatsDoradsSet);
                    Direccion direccion = generarDireccionInteligente(tipo, t);
                    flechas.add(new FlechaData(t, direccion, tipo));
                }
            } else {
                float dur = Math.max(30f, resultado.getDuracionTotal());
                float bpmFallback = bpm > 0f ? bpm : 120f;
                float intervalo = beatDetector.intervalFromBpm(bpmFallback);
                int beatsPorCompas = 4;
                for (float inicio = 0f; inicio + intervalo * beatsPorCompas <= dur + 0.0001f; inicio += intervalo * beatsPorCompas) {
                    ResultadoAnalisis.Intensidad inten = obtenerIntensidadEnTiempo(resultado, inicio);
                    Pattern pat = seleccionarPatronPorIntensidad(intervalo, inten);
                    RhythmPatternEngine.Subdivision sub = seleccionarSubdivisionPorIntensidad(inten);
                    List<Float> tiempos = patternEngine.generateMeasure(inicio, intervalo, beatsPorCompas, pat, sub);
                    tiempos = validarCompas(tiempos, inicio, intervalo * beatsPorCompas, maxAPS);
                    for (float nt : tiempos) {
                        if (nt < 0f) continue;
                        if (existeFlechaCerca(flechas, nt, 0.03f)) continue;
                        if (!permitirPorAPS(flechas, nt, maxAPS)) continue;
                        TipoFlecha tipo = determinarTipoFlecha(nt, beatsRapidosSet, beatsDoradsSet);
                        Direccion direccion = generarDireccionInteligente(tipo, nt);
                        flechas.add(new FlechaData(nt, direccion, tipo));
                    }
                }
                List<FlechaData> doradasCirculares = generarDoradasCircularesExtendidas(resultado);
                insertarEspeciales(flechas, doradasCirculares);
            }
        }

        flechas.sort(Comparator.comparing(FlechaData::getBeatTime));
        System.out.println("\nFlechas generadas:");
        System.out.println(" - Normales: " + contarTipo(flechas, TipoFlecha.NORMAL));
        System.out.println(" - Rápidas: " + contarTipo(flechas, TipoFlecha.RAPIDA));
        System.out.println(" - Doradas: " + contarTipo(flechas, TipoFlecha.DORADA));
        System.out.println(" - Luna: " + contarTipo(flechas, TipoFlecha.LUNA));
        System.out.println(" - Total: " + flechas.size());
        if (flechas.isEmpty()) {
            System.out.println("⚠ No se generaron flechas con el método avanzado. Aplicando fallback básico...");
            flechas = generarFallbackBasico(resultado);
            flechas.sort(Comparator.comparing(FlechaData::getBeatTime));
            System.out.println("✓ Fallback generado. Total: " + flechas.size());
        }
        return flechas;
    }
    
    /**
     * Determina el tipo de flecha basándose en el análisis
     */
    private TipoFlecha determinarTipoFlecha(float beatTime, Set<Float> beatsRapidos, Set<Float> beatsDorados) {
        // Si el beat está en la lista de lentos/dorados, es candidato a dorada
        if (beatsDorados.contains(beatTime)) {
            if (random.nextFloat() < 0.7f) { // 70% de probabilidad si está en la lista
                return TipoFlecha.DORADA;
            }
        }
        
        // Si está cerca de un beat rápido, puede ser rápida
        if (estaCercaDeBeatsRapidos(beatTime, beatsRapidos)) {
            if (random.nextFloat() < probabilidadRapida) {
                return TipoFlecha.RAPIDA;
            }
        }
        
        // Probabilidad base de dorada incluso si no está en la lista
        if (random.nextFloat() < probabilidadDorada) {
            return TipoFlecha.DORADA;
        }
        
        return TipoFlecha.NORMAL;
    }
    
    /**
     * Genera una dirección inteligente evitando patrones monótonos
     */
    private Direccion generarDireccionInteligente(TipoFlecha tipo, float beatTime) {
        // Solo las cuatro direcciones de flechas, sin ESPACIO
        Direccion[] direcciones = new Direccion[] {
            Direccion.ARRIBA, Direccion.ABAJO, Direccion.IZQUIERDA, Direccion.DERECHA
        };
        
        // Para flechas doradas, preferir direcciones verticales (más visibles)
        if (tipo == TipoFlecha.DORADA && random.nextFloat() < 0.6f) {
            return random.nextBoolean() ? Direccion.ARRIBA : Direccion.ABAJO;
        }
        
        // Evitar más de 3 flechas consecutivas en la misma dirección
        if (flechasConsecutivasMismaDireccion >= 3 && ultimaDireccion != null) {
            List<Direccion> otras = new ArrayList<>(Arrays.asList(direcciones));
            otras.remove(ultimaDireccion);
            Direccion nueva = otras.get(random.nextInt(otras.size()));
            actualizarDireccion(nueva);
            return nueva;
        }
        
        // Dirección aleatoria normal
        Direccion nueva = direcciones[random.nextInt(direcciones.length)];
        actualizarDireccion(nueva);
        return nueva;
    }
    
    private void actualizarDireccion(Direccion nueva) {
        if (nueva == ultimaDireccion) {
            flechasConsecutivasMismaDireccion++;
        } else {
            flechasConsecutivasMismaDireccion = 1;
            ultimaDireccion = nueva;
        }
    }
    
    /**
     * Verifica si un beat está cerca de beats rápidos
     */
    private boolean estaCercaDeBeatsRapidos(float beatTime, Set<Float> beatsRapidos) {
        float umbral = 0.2f;
        for (float beatRapido : beatsRapidos) {
            if (Math.abs(beatTime - beatRapido) < umbral) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica si ya existe una flecha cerca de este tiempo
     */
    private boolean existeFlechaCerca(List<FlechaData> flechas, float beatTime, float umbral) {
        for (FlechaData flecha : flechas) {
            if (Math.abs(flecha.getBeatTime() - beatTime) < umbral) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Cuenta cuántas flechas hay de un tipo específico
     */
    
    
    private long contarTipo(List<FlechaData> flechas, TipoFlecha tipo) {
        return flechas.stream().filter(f -> f.getTipo() == tipo).count();
    }
    
    /**
     * Ajusta las probabilidades de generación
     */
    public void setProbabilidades(float probRapida, float probDorada) {
        this.probabilidadRapida = Math.max(0, Math.min(1, probRapida));
        this.probabilidadDorada = Math.max(0, Math.min(1, probDorada));
    }
    
    /**
     * Activa/desactiva la generación de patrones rítmicos
     */
    public void setGenerarPatronesRitmicos(boolean activar) {
        this.generarPatronesRitmicos = activar;
    }
    private Pattern mapPattern(SongClassifier.PatternType pt) {
        switch (pt) {
            case SINCOPADO: return Pattern.SINCOPADO;
            case SEMICORCHEAS: return Pattern.SEMICORCHEAS;
            default: return Pattern.BASICO;
        }
    }

    private Pattern ajustarPatternPorIntervalo(float intervaloBeat, Pattern original) {
        if (intervaloBeat <= 0f) return Pattern.BASICO;
        if (intervaloBeat < 0.375f) { // >160 BPM
            return Pattern.BASICO;
        } else if (intervaloBeat < 0.45f) { // >133 BPM
            return original == Pattern.SEMICORCHEAS ? Pattern.SINCOPADO : original;
        }
        if (dificultad == Difficulty.FACIL && original == Pattern.SEMICORCHEAS) return Pattern.SINCOPADO;
        if (dificultad == Difficulty.FACIL && original == Pattern.SINCOPADO && intervaloBeat < 0.5f) return Pattern.BASICO;
        return original;
    }

    private float calcularMaxAPS(float bpm) {
        if (bpm <= 0f) return 2.0f;
        float base;
        if (bpm > 150f) base = 1.5f;
        else if (bpm > 120f) base = 2.0f;
        else if (bpm > 90f) base = 2.5f;
        else base = 3.0f;
        if (dificultad == Difficulty.FACIL) return base * 0.8f;
        if (dificultad == Difficulty.DIFICIL) return base * 1.15f;
        return base;
    }

    private boolean permitirPorAPS(List<FlechaData> flechas, float tiempo, float maxAPS) {
        int enVentana = 0;
        float inicio = tiempo - ventanaAPS;
        for (FlechaData f : flechas) {
            float bt = f.getBeatTime();
            if (bt >= inicio && bt <= tiempo) {
                enVentana++;
            }
        }
        float apsActual = enVentana / ventanaAPS;
        return apsActual < maxAPS;
    }

    private TimeSignature seleccionarCompas(ResultadoAnalisis res, float intervaloBeat) {
        if (intervaloBeat <= 0f) return TimeSignature.FOUR_FOUR;
        if (dificultad == Difficulty.FACIL && intervaloBeat < 0.4f) return TimeSignature.FOUR_FOUR;
        return TimeSignature.FOUR_FOUR;
    }

    private List<Float> generarCompases(List<Float> beats, float intervaloBeat, TimeSignature ts, Pattern basePattern, float maxAPS) {
        List<Float> out = new ArrayList<>();
        if (intervaloBeat <= 0f || beats == null || beats.isEmpty()) return out;
        int beatsPorCompas = ts == TimeSignature.FOUR_FOUR ? 4 : 3;
        int idx = 0;
        Pattern current = basePattern;
        while (idx + beatsPorCompas <= beats.size()) {
            float inicio = beats.get(idx);
            List<Float> tiemposCompas = generarCompas(inicio, intervaloBeat, beatsPorCompas, current);
            tiemposCompas = validarCompas(tiemposCompas, inicio, intervaloBeat * beatsPorCompas, maxAPS);
            if (esIntenso(tiemposCompas, intervaloBeat * beatsPorCompas)) {
                barrasIntensasConsecutivas++;
                if (barrasIntensasConsecutivas > 2) {
                    tiemposCompas = generarCompas(inicio, intervaloBeat, beatsPorCompas, Pattern.BASICO);
                    barrasIntensasConsecutivas = 0;
                }
            } else {
                barrasIntensasConsecutivas = 0;
            }
            out.addAll(tiemposCompas);
            current = siguientePatron(current);
            idx += beatsPorCompas;
        }
        return out;
    }

    private List<Float> generarCompas(float inicio, float intervalo, int beatsPorCompas, Pattern pattern) {
        List<Float> tiempos = new ArrayList<>();
        for (int b = 0; b < beatsPorCompas; b++) {
            float tBeat = inicio + b * intervalo;
            tiempos.add(tBeat);
            if (pattern == Pattern.SINCOPADO) {
                tiempos.add(tBeat + intervalo * 0.5f);
            } else if (pattern == Pattern.SEMICORCHEAS) {
                tiempos.add(tBeat + intervalo * 0.25f);
                tiempos.add(tBeat + intervalo * 0.5f);
                tiempos.add(tBeat + intervalo * 0.75f);
            } else {
                if (dificultad != Difficulty.FACIL) tiempos.add(tBeat + intervalo * 0.5f);
            }
            if (dificultad == Difficulty.DIFICIL) {
                tiempos.add(tBeat + intervalo * (1f/3f));
                tiempos.add(tBeat + intervalo * (2f/3f));
            }
        }
        tiempos.sort(Comparator.naturalOrder());
        return tiempos;
    }

    private List<Float> validarCompas(List<Float> tiempos, float inicio, float duracionCompas, float maxAPS) {
        List<Float> filtrado = new ArrayList<>();
        float maxNotas = Math.max(1f, maxAPS * duracionCompas);
        float ultimo = -1e6f;
        for (float t : tiempos) {
            if (filtrado.size() >= (int)maxNotas) break;
            if (Math.abs(t - ultimo) < 0.03f) continue;
            filtrado.add(t);
            ultimo = t;
        }
        return filtrado;
    }

    private boolean esIntenso(List<Float> tiempos, float duracionCompas) {
        if (duracionCompas <= 0f) return false;
        return (tiempos.size() / duracionCompas) >= 2.5f;
    }

    private Pattern siguientePatron(Pattern actual) {
        if (actual == Pattern.BASICO) return Pattern.SINCOPADO;
        if (actual == Pattern.SINCOPADO) return Pattern.BASICO;
        if (actual == Pattern.SEMICORCHEAS) return Pattern.BASICO;
        return Pattern.BASICO;
    }

    private ResultadoAnalisis.Intensidad obtenerIntensidadEnTiempo(ResultadoAnalisis r, float tiempo) {
        if (r.secciones == null || r.secciones.isEmpty()) return ResultadoAnalisis.Intensidad.MEDIA;
        for (ResultadoAnalisis.SeccionMusical s : r.secciones) {
            if (tiempo >= s.inicio && tiempo < s.fin) return s.intensidad;
        }
        return ResultadoAnalisis.Intensidad.MEDIA;
    }

    private Pattern seleccionarPatronPorIntensidad(float intervaloBeat, ResultadoAnalisis.Intensidad intensidad) {
        switch (intensidad) {
            case BAJA: return ajustarPatternPorIntervalo(intervaloBeat, Pattern.BASICO);
            case MEDIA: return ajustarPatternPorIntervalo(intervaloBeat, Pattern.SINCOPADO);
            case ALTA: return ajustarPatternPorIntervalo(intervaloBeat, Pattern.PERCUSIVO);
            case EXTREMA: return ajustarPatternPorIntervalo(intervaloBeat, Pattern.EXPLOSIVO);
            default: return Pattern.BASICO;
        }
    }

    private RhythmPatternEngine.Subdivision seleccionarSubdivisionPorIntensidad(ResultadoAnalisis.Intensidad intensidad) {
        switch (intensidad) {
            case BAJA: return RhythmPatternEngine.Subdivision.ONE;
            case MEDIA: return RhythmPatternEngine.Subdivision.QUARTER;
            case ALTA: return RhythmPatternEngine.Subdivision.EIGHTH;
            case EXTREMA: return RhythmPatternEngine.Subdivision.TRIPLET;
            default: return RhythmPatternEngine.Subdivision.ONE;
        }
    }

    private List<FlechaData> generarDoradasCircularesExtendidas(ResultadoAnalisis r) {
        List<FlechaData> out = new ArrayList<>();
        float finGlobal = r.getDuracionTotal();
        if (finGlobal <= 0f) return out;
        Direccion[] cicloCW = new Direccion[] { Direccion.DERECHA, Direccion.ABAJO, Direccion.IZQUIERDA, Direccion.ARRIBA };
        int stepIdx = 0;
        float t = r.segmentosBpm != null && !r.segmentosBpm.isEmpty() ? r.segmentosBpm.get(0).inicio : 0f;
        while (t <= finGlobal) {
            ResultadoAnalisis.SegmentoBpm seg = segmentoEnTiempo(r, t);
            float bpmLocal = (seg != null && seg.bpm > 0f) ? seg.bpm : (float) r.getBPMEstimado();
            float interval = beatDetector.intervalFromBpm(bpmLocal);
            ResultadoAnalisis.Intensidad inten = obtenerIntensidadEnTiempo(r, t);
            int mult = seleccionarMultiplicador(inten);
            float extended = interval * mult;
            Direccion dir = cicloCW[stepIdx % cicloCW.length];
            out.add(new FlechaData(t, dir, TipoFlecha.DORADA));
            stepIdx++;
            if (extended <= 0f) break;
            t += extended;
        }
        return out;
    }

    private int seleccionarMultiplicador(ResultadoAnalisis.Intensidad intensidad) {
        switch (intensidad) {
            case BAJA: return 4;
            case MEDIA: return 3;
            case ALTA: return 2;
            case EXTREMA: return 2;
            default: return 3;
        }
    }

    private ResultadoAnalisis.SegmentoBpm segmentoEnTiempo(ResultadoAnalisis r, float tiempo) {
        if (r.segmentosBpm == null) return null;
        for (ResultadoAnalisis.SegmentoBpm s : r.segmentosBpm) {
            if (tiempo >= s.inicio && tiempo <= s.fin + 1e-4f) return s;
        }
        return null;
    }

    private void insertarEspeciales(List<FlechaData> base, List<FlechaData> especiales) {
        if (especiales == null || especiales.isEmpty()) return;
        for (FlechaData e : especiales) {
            float umbral = 0.04f;
            float apsMax = 4.0f;
            if (existeFlechaCerca(base, e.getBeatTime(), umbral)) continue;
            if (!permitirPorAPS(base, e.getBeatTime(), apsMax)) continue;
            base.add(e);
        }
    }

    private void ajustarProbabilidadesPorBpm(float bpm) {
        if (bpm > 140f) {
            probabilidadRapida = Math.max(0.1f, probabilidadRapida * 0.6f);
            probabilidadDorada = Math.max(0.1f, probabilidadDorada * 0.8f);
        } else if (bpm > 120f) {
            probabilidadRapida = Math.max(0.12f, probabilidadRapida * 0.8f);
        }
        if (dificultad == Difficulty.FACIL) {
            probabilidadRapida = Math.max(0.08f, probabilidadRapida * 0.8f);
            probabilidadDorada = Math.max(0.09f, probabilidadDorada * 0.85f);
        } else if (dificultad == Difficulty.DIFICIL) {
            probabilidadRapida = Math.min(0.35f, probabilidadRapida * 1.15f);
            probabilidadDorada = Math.min(0.22f, probabilidadDorada * 1.10f);
        }
    }

    private List<FlechaData> generarFallbackBasico(ResultadoAnalisis resultado) {
        List<FlechaData> out = new ArrayList<>();
        float dur = Math.max(30f, resultado.getDuracionTotal());
        float bpm = (float) resultado.getBPMEstimado();
        if (bpm <= 0f) bpm = 120f;
        float intervalo = beatDetector.intervalFromBpm(bpm);
        int beatsPorCompas = 4;
        float maxAPS = calcularMaxAPS(bpm);
        RhythmPatternEngine.Pattern[] ciclo = new RhythmPatternEngine.Pattern[] { Pattern.BASICO, Pattern.SINCOPADO, Pattern.BASICO, Pattern.SEMICORCHEAS };
        int idx = 0;
        RhythmPatternEngine.Subdivision sub = bpm < 95f ? RhythmPatternEngine.Subdivision.QUARTER : (bpm < 135f ? RhythmPatternEngine.Subdivision.EIGHTH : RhythmPatternEngine.Subdivision.ONE);
        for (float inicio = 0f; inicio + intervalo * beatsPorCompas <= dur + 0.0001f; inicio += intervalo * beatsPorCompas) {
            RhythmPatternEngine.Pattern pat = ciclo[idx % ciclo.length];
            List<Float> tiempos = patternEngine.generateMeasure(inicio, intervalo, beatsPorCompas, pat, sub);
            tiempos = validarCompas(tiempos, inicio, intervalo * beatsPorCompas, maxAPS);
            for (float t : tiempos) {
                Direccion dir = generarDireccionInteligente(TipoFlecha.NORMAL, t);
                out.add(new FlechaData(t, dir, TipoFlecha.NORMAL));
            }
            idx++;
        }
        List<FlechaData> doradas = generarDoradasCircularesExtendidas(resultado);
        insertarEspeciales(out, doradas);
        return out;
    }
    
}
