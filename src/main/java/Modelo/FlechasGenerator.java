/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import java.util.*;

/**
 * Generador inteligente de flechas basado en el análisis de beats
 * Distribuye los tres tipos de flechas según el ritmo y intensidad
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class FlechasGenerator {
    
    private final Random random;
    
    // Configuración de probabilidades
    private float probabilidadRapida = 0.20f; // 20% de flechas rápidas
    private float probabilidadDorada = 0.15f; // 15% de flechas doradas
    
    // Configuración de patrones
    private boolean generarPatronesRitmicos = true;
    private int flechasConsecutivasMismaDireccion = 0;
    private Direccion ultimaDireccion = null;
    
    public FlechasGenerator() {
        this.random = new Random();
    }
    
    public FlechasGenerator(long seed) {
        this.random = new Random(seed);
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
    
    // ⭐ Usar beatsLentos para flechas de LUNA
    Set<Float> beatsLunaSet = new HashSet<>(resultado.beatsLentos);
    
    for (float beatTime : resultado.beatsNormales) {
        // ⭐ Verificar si es un beat de luna
        if (beatsLunaSet.contains(beatTime) && random.nextFloat() < 0.3f) {
            // 30% de probabilidad de ser luna si está en beatsLentos
            flechas.add(new FlechaData(beatTime, Direccion.ESPACIO, TipoFlecha.LUNA));
            continue;
        }
        
        TipoFlecha tipo = determinarTipoFlecha(beatTime, beatsRapidosSet, beatsDoradsSet);
        Direccion direccion = generarDireccionInteligente(tipo, beatTime);
        flechas.add(new FlechaData(beatTime, direccion, tipo));
    }
    
    flechas.sort(Comparator.comparing(FlechaData::getBeatTime));
    
    System.out.println("\nFlechas generadas:");
    System.out.println(" - Normales: " + contarTipo(flechas, TipoFlecha.NORMAL));
    System.out.println(" - Rápidas: " + contarTipo(flechas, TipoFlecha.RAPIDA));
    System.out.println(" - Doradas: " + contarTipo(flechas, TipoFlecha.DORADA));
    System.out.println(" - Luna: " + contarTipo(flechas, TipoFlecha.LUNA)); // ⭐ NUEVO
    System.out.println(" - Total: " + flechas.size());
    
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
        Direccion[] direcciones = Direccion.values();
        
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
}
