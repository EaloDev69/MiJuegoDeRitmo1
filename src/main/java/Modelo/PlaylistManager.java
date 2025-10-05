/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author CamiLaNekoUwU_Gamer
 */

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class PlaylistManager {

    private final Path folderPath;
    private final List<String> canciones = new ArrayList<>();

    public PlaylistManager(String folderPath) {
        this.folderPath = Paths.get(folderPath);
    }

    public List<String> getCanciones() {
        return new ArrayList<>(canciones);
    }


    public void cargarPlaylist() throws IOException {
        canciones.clear();
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(folderPath, "*.wav")) {
            for (Path p : ds) {
                canciones.add(p.toAbsolutePath().toString());
            }
        }
    }

    public String getCancion(int index) {
        return canciones.get(index);
    }

    public List<Float> analizarCancion(String path) { return new ArrayList<>(); }
    public List<Float> analizarCancionParaParpadeos(String path) { return new ArrayList<>(); }
    public List<Float> analizarCancionParaDoradas(String path) { return new ArrayList<>(); }
}
