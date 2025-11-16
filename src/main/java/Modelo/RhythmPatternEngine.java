package Modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RhythmPatternEngine {
    public enum Subdivision { ONE(1), HALF(2), QUARTER(4), EIGHTH(8), SIXTEENTH(16), TRIPLET(3); private final int denom; Subdivision(int d){denom=d;} public int denom(){return denom;} }
    public enum Pattern { BASICO, SINCOPADO, SEMICORCHEAS, MELODICO, PERCUSIVO, EXPLOSIVO, ALTERNADO }

    private final Random rng;
    private final float humanizeSec;

    public RhythmPatternEngine() { this(0.005f, System.nanoTime()); }
    public RhythmPatternEngine(float humanizeSec) { this(humanizeSec, System.nanoTime()); }
    public RhythmPatternEngine(float humanizeSec, long seed) { this.humanizeSec = Math.max(0f, humanizeSec); this.rng = new Random(seed); }

    public List<Float> generate(float beatTimeSec, float intervalSec, Pattern pattern) {
        List<Float> times = new ArrayList<>();
        if (intervalSec <= 0f) { times.add(beatTimeSec); return times; }
        switch (pattern) {
            case BASICO:
                times.add(h(beatTimeSec));
                break;
            case SINCOPADO:
                times.add(h(beatTimeSec + intervalSec * 0.5f));
                break;
            case SEMICORCHEAS:
                times.add(h(beatTimeSec));
                times.add(h(beatTimeSec + intervalSec * 0.25f));
                times.add(h(beatTimeSec + intervalSec * 0.5f));
                times.add(h(beatTimeSec + intervalSec * 0.75f));
                break;
            case MELODICO:
                times.add(h(beatTimeSec));
                times.add(h(beatTimeSec + intervalSec * 0.5f));
                break;
            case PERCUSIVO:
                times.add(h(beatTimeSec + intervalSec * 0.25f));
                times.add(h(beatTimeSec + intervalSec * 0.5f));
                times.add(h(beatTimeSec + intervalSec * 0.75f));
                break;
            case EXPLOSIVO:
                times.add(h(beatTimeSec));
                times.add(h(beatTimeSec + intervalSec * 0.25f));
                times.add(h(beatTimeSec + intervalSec * 0.375f));
                times.add(h(beatTimeSec + intervalSec * 0.5f));
                times.add(h(beatTimeSec + intervalSec * 0.625f));
                times.add(h(beatTimeSec + intervalSec * 0.75f));
                break;
            case ALTERNADO:
                times.add(h(beatTimeSec));
                break;
        }
        return times;
    }

    public List<Float> generateMeasure(float startSec, float intervalSec, int beatsPerBar, Pattern pattern, Subdivision subdivision) {
        List<Float> times = new ArrayList<>();
        if (intervalSec <= 0f || beatsPerBar <= 0) return times;
        for (int b = 0; b < beatsPerBar; b++) {
            float tBeat = startSec + b * intervalSec;
            if (subdivision == Subdivision.ONE) {
                times.addAll(generate(tBeat, intervalSec, pattern));
            } else {
                int denom = subdivision.denom();
                float step = intervalSec / denom;
                for (int s = 0; s < denom; s++) {
                    float t = tBeat + s * step;
                    switch (pattern) {
                        case BASICO:
                            if (s == 0) times.add(h(t));
                            break;
                        case SINCOPADO:
                            if (denom % 2 == 0 && s == denom / 2) times.add(h(t));
                            if (denom == 3 && s == 1) times.add(h(t));
                            break;
                        case SEMICORCHEAS:
                            times.add(h(t));
                            break;
                        case MELODICO:
                            if (s == 0 || (denom >= 2 && s == denom / 2)) times.add(h(t));
                            break;
                        case PERCUSIVO:
                            if (denom >= 4 && (s == denom/4 || s == denom/2 || s == (3*denom)/4)) times.add(h(t));
                            if (denom == 3 && (s == 1)) times.add(h(t));
                            break;
                        case EXPLOSIVO:
                            times.add(h(t));
                            break;
                        case ALTERNADO:
                            if (s == 0) times.add(h(t));
                            break;
                    }
                }
            }
        }
        times.sort(Float::compare);
        return times;
    }

    private float h(float t) {
        float d = (rng.nextFloat() * 2f - 1f) * humanizeSec;
        return t + d;
    }
}