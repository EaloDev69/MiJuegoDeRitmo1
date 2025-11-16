package Modelo.audio;

import Modelo.AnalizadorCanciones;

public class SongClassifier {
    public enum PatternType { BASICO, SINCOPADO, SEMICORCHEAS }

    public PatternType classify(AnalizadorCanciones.ResultadoAnalisis r) {
        int n = r.beatsNormales != null ? r.beatsNormales.size() : 0;
        int f = r.beatsRapidos != null ? r.beatsRapidos.size() : 0;
        int l = r.beatsLentos != null ? r.beatsLentos.size() : 0;
        if (n == 0) return PatternType.BASICO;
        float ratioRapidos = n > 0 ? (float) f / n : 0f;
        float ratioLentos = n > 0 ? (float) l / n : 0f;
        if (ratioRapidos >= 0.35f) return PatternType.SEMICORCHEAS;
        if (ratioLentos >= 0.25f) return PatternType.SINCOPADO;
        return PatternType.BASICO;
    }
}