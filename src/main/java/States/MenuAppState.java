/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package States;

import Modelo.PlaylistManager;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;
import com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuAppState extends BaseAppState {

    private MiJuegoDeRitmo juego;
    private Container contenedorMenu;
    private Container songListContainer;          
    private PlaylistManager playlistManager;
    private final Set<String> cancionesSeleccionadas = new HashSet<>();

    // UI
    private Button btnEmpezar, btnSalir, btnInstrucciones, btnAbrirCarpeta;
    private Slider sliderVolumen;
    private Label lblTitulo, lblVolumen, lblInstrucciones, lblSeleccionCount;
    private double ultimoVolumen = -1;


    public MenuAppState() {
        // ruta por defecto; ajusta si tu proyecto usa otro lugar
        this.playlistManager = new PlaylistManager("assets/canciones");
    }

    @Override
    protected void initialize(Application app) {
        this.juego = (MiJuegoDeRitmo) app;
        iniciarMenu(app);
    }

    public void iniciarMenu(Application app) {
         GuiGlobals.initialize(app);
         crearContenedorMenu();
         crearComponentesUI();
         juego.getGuiNode().attachChild(contenedorMenu);
         cargarYMostrarCancionesAsync();
}

    private void crearContenedorMenu() {
        contenedorMenu = new Container();
        float ancho = juego.getCamera().getWidth();
        float alto = juego.getCamera().getHeight();
        contenedorMenu.setLocalTranslation(ancho / 2f - 220f, alto / 2f + 240f, 0f);
    }

    private void crearComponentesUI() {
        lblTitulo = new Label("JUEGO DE RITMO");
        lblTitulo.setInsets(new Insets3f(5, 5, 5, 5));
        contenedorMenu.addChild(lblTitulo);

        // Lista de canciones (contenedor vertical)
        songListContainer = new Container();
        // opcional: envuelve en otro container con título
        contenedorMenu.addChild(new Label("Canciones:"));
        contenedorMenu.addChild(songListContainer);

        lblSeleccionCount = new Label("Seleccionadas: 0");
        contenedorMenu.addChild(lblSeleccionCount);

        // Botones
        btnEmpezar = new Button("Empezar Juego");
        btnEmpezar.addClickCommands(src -> iniciarJuego());
        contenedorMenu.addChild(btnEmpezar);

        btnInstrucciones = new Button("Instrucciones");
        btnInstrucciones.addClickCommands(src -> mostrarInstrucciones());
        contenedorMenu.addChild(btnInstrucciones);

        btnAbrirCarpeta = new Button("Abrir Carpeta Canciones");
        btnAbrirCarpeta.addClickCommands(src -> abrirCarpetaCanciones());
        contenedorMenu.addChild(btnAbrirCarpeta);

        lblVolumen = new Label("Volumen: 100%");
        contenedorMenu.addChild(lblVolumen);

        sliderVolumen = new Slider();
        sliderVolumen.getModel().setMinimum(0);
        sliderVolumen.getModel().setMaximum(100);
        sliderVolumen.getModel().setValue(100); 
        contenedorMenu.addChild(sliderVolumen);
        
        


        btnSalir = new Button("Salir");
        btnSalir.addClickCommands(src -> juego.stop());
        contenedorMenu.addChild(btnSalir);

        lblInstrucciones = new Label("");
        contenedorMenu.addChild(lblInstrucciones);
    }


    private void cargarYMostrarCancionesAsync() {
        new Thread(() -> {
            try {
                playlistManager.cargarPlaylist();
                List<String> canciones = playlistManager.getCanciones();

                juego.enqueue(() -> {
                    actualizarListaCancionesUI(canciones);
                    return null;
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "PlaylistLoader").start();
    }

    private void actualizarListaCancionesUI(List<String> canciones) {
        songListContainer.clearChildren();
        cancionesSeleccionadas.clear();
        for (String ruta : canciones) {
            String nombre = Paths.get(ruta).getFileName().toString();
            Button b = new Button(nombre);

            b.addClickCommands(src -> toggleSeleccion(ruta, b));
            songListContainer.addChild(b);
        }
        lblSeleccionCount.setText("Seleccionadas: " + cancionesSeleccionadas.size());
    }

    private void toggleSeleccion(String ruta, Button boton) {
        if (cancionesSeleccionadas.contains(ruta)) {
            cancionesSeleccionadas.remove(ruta);

            boton.setText(boton.getText()); 
        } else {
            cancionesSeleccionadas.add(ruta);

            boton.setText(boton.getText()); 
        }
        lblSeleccionCount.setText("Seleccionadas: " + cancionesSeleccionadas.size());
    }

    private void actualizarVolumen() {
       double val = sliderVolumen.getModel().getValue();
    lblVolumen.setText("Volumen: " + (int) val + "%");


    if (juego != null) {
        juego.setMasterVolume((float)(val / 100.0));
        }
   }

    private void mostrarInstrucciones() {
        String instrucciones =
                "1. Coloca archivos .wav en assets/canciones/\n" +
                "2. Selecciona las canciones que quieras jugar\n" +
                "3. Presiona 'Empezar Juego'";
        lblInstrucciones.setText(instrucciones);
        System.out.println(instrucciones);
    }

    public void abrirCarpetaCanciones() {
        try {
            String rutaCarpeta = "assets/canciones/";
            File carpeta = new File(rutaCarpeta);
            if (!carpeta.exists()) carpeta.mkdirs();

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(carpeta);
            } else {
                System.out.println("Desktop no soportado. Ruta: " + carpeta.getAbsolutePath());
            }

            cargarYMostrarCancionesAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciarJuego() {
        if (juego != null) {
            List<String> lista = cancionesSeleccionadas.isEmpty()
                    ? playlistManager.getCanciones()
                    : List.copyOf(cancionesSeleccionadas);
            juego.startGameplay(lista);
        }
    }

    @Override
    public void update(float tpf) {

    }

    @Override
    protected void cleanup(Application app) {
        limpiarMenu(app);
    }
    

    public void limpiarMenu(Application app) {
        if (contenedorMenu != null && contenedorMenu.getParent() != null) {
            juego.getGuiNode().detachChild(contenedorMenu);
        }
        System.out.println("Menú limpiado");
    }
    

    @Override
    protected void onEnable() {

        if (contenedorMenu != null && contenedorMenu.getParent() == null) {
            juego.getGuiNode().attachChild(contenedorMenu);
        }
    }

    @Override
    protected void onDisable() {

        if (contenedorMenu != null && contenedorMenu.getParent() != null) {
            juego.getGuiNode().detachChild(contenedorMenu);
        }
    }

    public PlaylistManager getPlaylistManager() { return playlistManager; }
    public void setPlaylistManager(PlaylistManager pm) { this.playlistManager = pm; }
}
