/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */package States;

import Modelo.PlaylistManager;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;
import com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.SwingUtilities;

public class MenuAppState extends BaseAppState {

    private MiJuegoDeRitmo juego;
    private Container contenedorMenu;
    private PlaylistManager playlistManager;
    
    // NUEVO: Guardamos la selección de canciones aquí
    private final Set<String> cancionesSeleccionadas = new HashSet<>();

    // UI
    private Button btnEmpezar, btnSalir, btnInstrucciones, btnAbrirCarpeta, btnSeleccionarCanciones, btnVolumen;
    private Label lblTitulo, lblInstrucciones, lblSeleccionCount;
    
    // Volumen guardado como entero (0-100)
    private int volumenActual = 100;

    public MenuAppState() {
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
        
        // Cargar playlist al inicio
        cargarPlaylistAsync();
    }

    private void crearContenedorMenu() {
        contenedorMenu = new Container();
        float ancho = juego.getCamera().getWidth();
        float alto = juego.getCamera().getHeight();
        contenedorMenu.setLocalTranslation(ancho / 2f - 220f, alto / 2f + 240f, 0f);
    }

    private void crearComponentesUI() {
        // Título
        lblTitulo = new Label("=== JUEGO DE RITMO ===");
        lblTitulo.setFontSize(24f);
        lblTitulo.setColor(ColorRGBA.Cyan);
        lblTitulo.setInsets(new Insets3f(10, 10, 15, 10));
        contenedorMenu.addChild(lblTitulo);
        
                // Botón empezar juego
        btnEmpezar = new Button("EMPEZAR JUEGO");
        btnEmpezar.addClickCommands(src -> iniciarJuego());
        btnEmpezar.setColor(ColorRGBA.Green);
        btnEmpezar.setFontSize(18f);
        btnEmpezar.setInsets(new Insets3f(10, 20, 10, 20));
        contenedorMenu.addChild(btnEmpezar);
        
           // Contador de canciones seleccionadas
        lblSeleccionCount = new Label("Canciones seleccionadas: 0");
        lblSeleccionCount.setColor(ColorRGBA.Gray);
        lblSeleccionCount.setInsets(new Insets3f(5, 5, 10, 5));
        contenedorMenu.addChild(lblSeleccionCount);
        
          btnSeleccionarCanciones = new Button("Seleccionar Canciones");
        btnSeleccionarCanciones.addClickCommands(src -> abrirPantallaSeleccion());
        btnSeleccionarCanciones.setColor(ColorRGBA.Yellow);
        btnSeleccionarCanciones.setFontSize(16f);
        btnSeleccionarCanciones.setInsets(new Insets3f(8, 15, 8, 15));
        contenedorMenu.addChild(btnSeleccionarCanciones);

        btnAbrirCarpeta = new Button("Abrir Carpeta Canciones");
        btnAbrirCarpeta.addClickCommands(src -> abrirCarpetaCanciones());
        contenedorMenu.addChild(btnAbrirCarpeta);

        // NUEVO: Botón para abrir ventana de volumen
        btnVolumen = new Button("Configurar Volumen");
        btnVolumen.addClickCommands(src -> abrirVentanaVolumen());
        contenedorMenu.addChild(btnVolumen);



        
           btnInstrucciones = new Button("Instrucciones");
        btnInstrucciones.addClickCommands(src -> mostrarInstrucciones());
        contenedorMenu.addChild(btnInstrucciones);

        lblInstrucciones = new Label("");
        lblInstrucciones.setInsets(new Insets3f(10, 10, 10, 10));
        contenedorMenu.addChild(lblInstrucciones);
        
        
        
        btnSalir = new Button("Salir");
        btnSalir.addClickCommands(src -> juego.stop());
        contenedorMenu.addChild(btnSalir);
         // NUEVO: Botón para abrir pantalla de selección
    }

    /**
     * Carga la playlist en background
     */
    private void cargarPlaylistAsync() {
        System.out.println("Cargando playlist...");
        
        new Thread(() -> {
            try {
                playlistManager.cargarPlaylist();
                List<String> canciones = playlistManager.getCanciones();
                
                System.out.println("Playlist cargada: " + canciones.size() + " canciones");
                
            } catch (Exception ex) {
                System.err.println("Error al cargar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, "PlaylistLoader").start();
    }

    /**
     * NUEVO: Abre ventana Swing separada para selección
     */
    private void abrirPantallaSeleccion() {
        System.out.println("\n>>> Abriendo ventana de seleccion...");
        
        // Ejecutar en el thread de AWT (Swing)
        SwingUtilities.invokeLater(() -> {
            // Obtener el Frame padre (ventana principal de JME)
            java.awt.Frame parentFrame = null;
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof java.awt.Frame) {
                    parentFrame = (java.awt.Frame) window;
                    break;
                }
            }
            
            // Abrir ventana modal (bloquea hasta que se cierre)
            Set<String> nuevaSeleccion = UI.VentanaSeleccionCanciones.mostrarDialogo(
                parentFrame, 
                playlistManager, 
                cancionesSeleccionadas
            );
            
            // Actualizar selección en el thread de JME
            juego.enqueue(() -> {
                cancionesSeleccionadas.clear();
                cancionesSeleccionadas.addAll(nuevaSeleccion);
                actualizarContadorSeleccion();
                return null;
            });
        });
    }

    /**
     * NUEVO: Abre ventana Swing para configurar volumen
     */
    private void abrirVentanaVolumen() {
        System.out.println("\n>>> Abriendo ventana de volumen...");
        
        SwingUtilities.invokeLater(() -> {
            // Obtener el Frame padre
            java.awt.Frame parentFrame = null;
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof java.awt.Frame) {
                    parentFrame = (java.awt.Frame) window;
                    break;
                }
            }
            
            // Abrir ventana modal
            int nuevoVolumen = UI.VentanaVolumen.mostrarDialogo(parentFrame, volumenActual);
            
            // Actualizar volumen en el thread de JME
            juego.enqueue(() -> {
                volumenActual = nuevoVolumen;
                juego.setMasterVolume(volumenActual / 100.0f);
                System.out.println("Volumen actualizado a: " + volumenActual + "%");
                return null;
            });
        });
    }

    /**
     * NUEVO: Actualiza el label del contador
     */
    private void actualizarContadorSeleccion() {
        int count = cancionesSeleccionadas.size();
        lblSeleccionCount.setText("Canciones seleccionadas: " + count);
        
        if (count == 0) {
            lblSeleccionCount.setColor(ColorRGBA.Gray);
        } else {
            lblSeleccionCount.setColor(ColorRGBA.Green);
        }
        
        System.out.println("Contador actualizado: " + count + " canciones");
    }

    private void mostrarInstrucciones() {
        String instrucciones =
                "1. Coloca archivos .wav en:\n" +
                "   MiJuegoDeRitmo1/assets/canciones                                        " + "2. Presiona 'Seleccionar Canciones'\n                                                                                    para elegir cuales jugar" +
                "   \n\n" +
                "\n" +
          "4. Presiona 'EMPEZAR JUEGO'";
        
 
        
        lblInstrucciones.setText(instrucciones);
        lblInstrucciones.setFontSize(15);
        lblInstrucciones.setColor(ColorRGBA.Cyan);
        System.out.println("\n" + instrucciones);
    }

    public void abrirCarpetaCanciones() {
        try {
            String rutaCarpeta = "assets/canciones/";
            File carpeta = new File(rutaCarpeta);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
                System.out.println("Carpeta creada");
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(carpeta);
                System.out.println("Abriendo: " + carpeta.getAbsolutePath());
            } else {
                System.out.println("Desktop no soportado");
            }

            // Recargar después de 1 segundo
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    cargarPlaylistAsync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicia el juego con las canciones seleccionadas (o todas)
     */
    private void iniciarJuego() {
        if (juego != null) {
            List<String> lista = cancionesSeleccionadas.isEmpty()
                    ? playlistManager.getCanciones()
                    : new ArrayList<>(cancionesSeleccionadas);
            
            if (lista.isEmpty()) {
                System.err.println("ERROR: No hay canciones!");
                lblInstrucciones.setText("ERROR: No hay canciones .wav\nAgrega archivos a assets/canciones/");
                lblInstrucciones.setColor(ColorRGBA.Red);
                return;
            }
            
            System.out.println("\n=== INICIANDO JUEGO ===");
            System.out.println("Canciones a jugar: " + lista.size());
            for (int i = 0; i < lista.size(); i++) {
                System.out.println("  " + (i+1) + ". " + Paths.get(lista.get(i)).getFileName());
            }
            
            juego.startGameplay(lista);
        }
    }

    @Override
    public void update(float tpf) {
        // El volumen ahora se maneja desde la ventana separada
    }

    @Override
    protected void cleanup(Application app) {
        limpiarMenu(app);
    }

    public void limpiarMenu(Application app) {
        if (contenedorMenu != null && contenedorMenu.getParent() != null) {
            juego.getGuiNode().detachChild(contenedorMenu);
        }
        System.out.println("Menu limpiado");
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
