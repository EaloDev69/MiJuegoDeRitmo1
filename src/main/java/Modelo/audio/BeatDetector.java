package Modelo.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeatDetector {
    public float estimateBpm(List<Float> beats) {
        if (beats == null || beats.size() < 4) return 0f;
        List<Float> diffs = new ArrayList<>();
        for (int i = 1; i < beats.size(); i++) { diffs.add(beats.get(i) - beats.get(i - 1)); }
        Collections.sort(diffs);
        float median = diffs.get(diffs.size() / 2);
        if (median <= 0f) return 0f;
        return 60f / median;
    }

    public float estimateBpmRobusto(List<Float> beats) {
        if (beats == null || beats.size() < 6) return estimateBpm(beats);
        List<Float> diffs = new ArrayList<>();
        for (int i = 1; i < beats.size(); i++) diffs.add(beats.get(i) - beats.get(i - 1));
        Collections.sort(diffs);
        float median = diffs.get(diffs.size() / 2);
        if (median <= 0f) return 0f;
        float bestIntervalScan = median;
        float bestScoreScan = -1f;
        float minBpm = 50f;
        float maxBpm = 220f;
        float stepBpm = 0.5f;
        for (float bpm = minBpm; bpm <= maxBpm; bpm += stepBpm) {
            float cand = 60f / bpm;
            float s = scoreInterval(beats, cand);
            if (bpm < 60f || bpm > 180f) s *= 0.9f;
            if (s > bestScoreScan) { bestScoreScan = s; bestIntervalScan = cand; }
        }
        float[] hd = new float[] { bestIntervalScan * 0.5f, bestIntervalScan, bestIntervalScan * 2.0f };
        float bestInterval = bestIntervalScan;
        float bestScore = -1f;
        for (float cand : hd) {
            if (cand <= 0f) continue;
            float s = scoreInterval(beats, cand);
            float bpm = 60f / cand;
            if (bpm < 50f || bpm > 220f) s *= 0.9f;
            if (s > bestScore) { bestScore = s; bestInterval = cand; }
        }
        return bestInterval > 0f ? (60f / bestInterval) : 0f;
    }

    private float scoreInterval(List<Float> beats, float interval) {
        if (interval <= 0f || beats == null || beats.size() < 3) return 0f;
        float tol = interval * 0.12f;
        float start = beats.get(0);
        int aligned = 0;
        for (float b : beats) {
            float n = Math.round((b - start) / interval);
            float grid = start + n * interval;
            float err = Math.abs(b - grid);
            if (err <= tol) aligned++;
        }
        return (float) aligned / Math.max(1, beats.size());
    }

    public float intervalFromBpm(float bpm) {
        if (bpm <= 0f) return 0f;
        return 60f / bpm;
    }

    public float chooseInterval(List<Float> beats, float fallbackBpm) {
        float interval = intervalFromBpm(fallbackBpm);
        if (interval > 0f) return interval;
        if (beats == null || beats.size() < 2) return 0f;
        List<Float> diffs = new ArrayList<>();
        for (int i = 1; i < beats.size(); i++) { diffs.add(beats.get(i) - beats.get(i - 1)); }
        Collections.sort(diffs);
        float median = diffs.get(diffs.size() / 2);
        if (median <= 0f) return 0f;
        // Robust choice between half/double
        float[] candidates = new float[] { median, median * 0.5f, median * 2.0f };
        float best = median;
        float bestS = -1f;
        for (float cand : candidates) {
            float s = scoreInterval(beats, cand);
            if (s > bestS) { bestS = s; best = cand; }
        }
        return best;
    }
}