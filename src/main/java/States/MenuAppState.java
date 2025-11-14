/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package States;

import Modelo.PlaylistManager;
import Modelo.AnalizadorCanciones;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.*;
import com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.SwingUtilities;
import UI.VentanaCargaCanciones;
import UI.VentanaInstrucciones;
import UI.VentanaSeleccionCanciones;

/**
 * MenuAppState actualizado con sistema de modo aleatorio
 * ✅ Selección de canciones con checkboxes
 * ✅ NUEVO: Soporte para modo aleatorio (shuffle)
 * ✅ Análisis de canciones con ventana de progreso
 * ✅ Configuración de volumen
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class MenuAppState extends BaseAppState {
    
    private MiJuegoDeRitmo juego;
    private Container contenedorMenu;
    private PlaylistManager playlistManager;
    private AnalizadorCanciones analizador;
    
    // Guardamos la selección de canciones
    private final Set<String> cancionesSeleccionadas = new HashSet<>();
    
    // ⭐ NUEVO: Flag para modo aleatorio
    private boolean modoAleatorioActivo = false;
    
    // UI
    private Button btnEmpezar, btnSalir, btnInstrucciones, btnAbrirCarpeta,
                   btnSeleccionarCanciones, btnVolumen, btnModoPractica;
    private Label lblTitulo, lblInstrucciones, lblSeleccionCount;
    
    // ⭐ NUEVO: Label para indicador de modo aleatorio
    private Label lblModoAleatorio;
    
    // Volumen guardado como entero (0-100)
    private int volumenActual = 100;
    
    public MenuAppState() {
        this.playlistManager = new PlaylistManager(resolverRutaCanciones());
    }
    
    @Override
    protected void initialize(Application app) {
        this.juego = (MiJuegoDeRitmo) app;
        this.analizador = new AnalizadorCanciones(app.getAssetManager());
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
        contenedorMenu.setLocalTranslation(ancho / 2f - 300f, alto / 2f + 300f, 0f);
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

        // ⭐ NUEVO: Botón modo práctica
        btnModoPractica = new Button("MODO PRÁCTICA");
        btnModoPractica.addClickCommands(src -> iniciarModoPractica());
        btnModoPractica.setColor(ColorRGBA.Cyan);
        btnModoPractica.setFontSize(16f);
        btnModoPractica.setInsets(new Insets3f(8, 15, 8, 15));
        contenedorMenu.addChild(btnModoPractica);
        
        // Contador de canciones seleccionadas
        lblSeleccionCount = new Label("Canciones seleccionadas: 0");
        lblSeleccionCount.setColor(ColorRGBA.Gray);
        lblSeleccionCount.setInsets(new Insets3f(5, 5, 5, 5));
        contenedorMenu.addChild(lblSeleccionCount);
        
        // ⭐ NUEVO: Indicador de modo aleatorio
        lblModoAleatorio = new Label("");
        lblModoAleatorio.setColor(ColorRGBA.Orange);
        lblModoAleatorio.setFontSize(14f);
        lblModoAleatorio.setInsets(new Insets3f(0, 5, 10, 5));
        contenedorMenu.addChild(lblModoAleatorio);
        
        // Botón seleccionar canciones
        btnSeleccionarCanciones = new Button("Seleccionar Canciones");
        btnSeleccionarCanciones.addClickCommands(src -> abrirPantallaSeleccion());
        btnSeleccionarCanciones.setColor(ColorRGBA.Yellow);
        btnSeleccionarCanciones.setFontSize(16f);
        btnSeleccionarCanciones.setInsets(new Insets3f(8, 15, 8, 15));
        contenedorMenu.addChild(btnSeleccionarCanciones);
        
        // Botón abrir carpeta
        btnAbrirCarpeta = new Button("Abrir Carpeta Canciones");
        btnAbrirCarpeta.addClickCommands(src -> abrirCarpetaCanciones());
        contenedorMenu.addChild(btnAbrirCarpeta);
        
        // Botón para abrir ventana de volumen
        btnVolumen = new Button("Configurar Volumen");
        btnVolumen.addClickCommands(src -> abrirVentanaVolumen());
        contenedorMenu.addChild(btnVolumen);
        
        // Botón instrucciones
        btnInstrucciones = new Button("Instrucciones");
        btnInstrucciones.addClickCommands(src -> mostrarInstrucciones());
        contenedorMenu.addChild(btnInstrucciones);
        
        // Label instrucciones
        lblInstrucciones = new Label("");
        lblInstrucciones.setInsets(new Insets3f(10, 10, 10, 10));
        contenedorMenu.addChild(lblInstrucciones);
        
        // Botón salir
        btnSalir = new Button("Salir");
        btnSalir.addClickCommands(src -> juego.stop());
        contenedorMenu.addChild(btnSalir);
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
     * ⭐ ACTUALIZADO: Ahora maneja el modo aleatorio
     */
    private void abrirPantallaSeleccion() {
        System.out.println("\n>>> Abriendo ventana de selección...");

        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            // Asegurar que la lista esté actualizada antes de mostrar la ventana
            try {
                playlistManager.cargarPlaylist();
            } catch (Exception e) {
                System.err.println("Error recargando playlist: " + e.getMessage());
            }
            
            // ⭐ NUEVO: Usar ResultadoSeleccion en vez de Set
            VentanaSeleccionCanciones.ResultadoSeleccion resultado =
                VentanaSeleccionCanciones.mostrarDialogo(
                    parentFrame,
                    playlistManager,
                    cancionesSeleccionadas
                );
            
            // Actualizar selección en el thread de JME
            juego.enqueue(() -> {
                cancionesSeleccionadas.clear();
                cancionesSeleccionadas.addAll(resultado.getCancionesComoSet());
                
                // ⭐ NUEVO: Actualizar estado del modo aleatorio
                modoAleatorioActivo = resultado.isModoAleatorio();
                
                actualizarContadorSeleccion();
                actualizarIndicadorModoAleatorio();
                
                // Log para debugging
                if (modoAleatorioActivo) {
                    System.out.println("✓ Modo aleatorio activado para " + resultado.size() + " canciones");
                } else {
                    System.out.println("✓ Modo normal - " + resultado.size() + " canciones seleccionadas");
                }
                
                return null;
            });
        });
    }
    
    /**
     * ⭐ NUEVO: Actualiza el indicador visual de modo aleatorio
     */
    private void actualizarIndicadorModoAleatorio() {
        if (modoAleatorioActivo && !cancionesSeleccionadas.isEmpty()) {
            lblModoAleatorio.setText("🔀 Modo Aleatorio: ACTIVO");
            lblModoAleatorio.setColor(ColorRGBA.Orange);
        } else {
            lblModoAleatorio.setText("");
        }
    }
    
    /**
     * Abre ventana Swing para configurar volumen
     */
    private void abrirVentanaVolumen() {
        System.out.println("\n>>> Abriendo ventana de volumen...");
        
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            
            int nuevoVolumen = UI.VentanaVolumen.mostrarDialogo(parentFrame, volumenActual);
            
            juego.enqueue(() -> {
                volumenActual = nuevoVolumen;
                juego.setMasterVolume(volumenActual / 100.0f);
                System.out.println("Volumen actualizado a: " + volumenActual + "%");
                return null;
            });
        });
    }
    
    /**
     * Obtiene el Frame padre de la aplicación
     */
    private java.awt.Frame obtenerFramePadre() {
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            if (window instanceof java.awt.Frame) {
                return (java.awt.Frame) window;
            }
        }
        return null;
    }
    
    /**
     * Actualiza el label del contador
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
    
    /**
     * Muestra las instrucciones del juego - ACTUALIZADO
     */
    private void mostrarInstrucciones() {
    SwingUtilities.invokeLater(() -> {
        java.awt.Frame parentFrame = obtenerFramePadre();
        VentanaInstrucciones.mostrar(parentFrame);
    });
}
    
    /**
     * Abre la carpeta de canciones en el explorador
     */
    public void abrirCarpetaCanciones() {
        try {
            String rutaCarpeta = resolverRutaCanciones();
            File carpeta = new File(rutaCarpeta);
            
            if (!carpeta.exists()) {
                carpeta.mkdirs();
                System.out.println("Carpeta creada: " + carpeta.getAbsolutePath());
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
     * Resuelve una ruta ABSOLUTA hacia assets/canciones independientemente del cwd.
     * Intenta partir desde la ubicación de las clases compiladas (target/classes)
     * y sube al directorio del proyecto.
     */
    private String resolverRutaCanciones() {
        try {
            java.net.URI uri = com.mycompany.mijuegoderitmo1.MiJuegoDeRitmo.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            Path classesDir = Paths.get(uri);
            // target/classes -> subir dos niveles para llegar al root del proyecto
            Path proyectoDir = classesDir.getParent().getParent();
            Path ruta = proyectoDir.resolve("assets").resolve("canciones");
            return ruta.toAbsolutePath().toString();
        } catch (Exception e) {
            // Fallback: usar ruta relativa y convertir a absoluta
            Path ruta = Paths.get("assets", "canciones").toAbsolutePath();
            return ruta.toString();
        }
    }
    
    /**
     * ⭐ ACTUALIZADO: Inicia el juego con o sin modo aleatorio
     */
    private void iniciarJuego() {
        if (juego == null) return;
        
        // Determinar qué canciones jugar
        List<String> lista;
        
        if (cancionesSeleccionadas.isEmpty()) {
            // Si no hay selección, usar todas las canciones
            lista = playlistManager.getCanciones();
            
            // ⭐ Si modo aleatorio está activo, mezclar
            if (modoAleatorioActivo && !lista.isEmpty()) {
                lista = new ArrayList<>(lista);
                Collections.shuffle(lista);
                System.out.println("🔀 Todas las canciones mezcladas");
            }
        } else {
            // Usar canciones seleccionadas
            lista = new ArrayList<>(cancionesSeleccionadas);
            
            // ⭐ Si modo aleatorio está activo, mezclar
            if (modoAleatorioActivo) {
                Collections.shuffle(lista);
                System.out.println("🔀 Canciones seleccionadas mezcladas");
            }
        }
        
        if (lista.isEmpty()) {
            System.err.println("ERROR: No hay canciones!");
            lblInstrucciones.setText("ERROR: No hay canciones .wav\nAgrega archivos a assets/canciones/");
            lblInstrucciones.setColor(ColorRGBA.Red);
            return;
        }
        
        System.out.println("\n=== PREPARANDO JUEGO ===");
        System.out.println("Canciones a analizar: " + lista.size());
        System.out.println("Modo aleatorio: " + (modoAleatorioActivo ? "SÍ 🔀" : "NO"));
        
        // ⭐ Mostrar orden de reproducción
        if (modoAleatorioActivo) {
            System.out.println("\n🔀 ORDEN DE REPRODUCCIÓN (ALEATORIO):");
        } else {
            System.out.println("\nORDEN DE REPRODUCCIÓN:");
        }
        
        for (int i = 0; i < lista.size(); i++) {
            String nombre = new File(lista.get(i)).getName();
            System.out.println("  " + (i + 1) + ". " + nombre);
        }
        
        // Hacer lista final (inmutable para el análisis)
        final List<String> listaFinal = new ArrayList<>(lista);
        
        // Abrir ventana de carga y análisis
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            
            // ⭐ Usar mostrarYAnalizarConResultados para obtener el Map
            Map<String, ResultadoAnalisis> resultados =
                VentanaCargaCanciones.mostrarYAnalizarConResultados(
                    parentFrame,
                    listaFinal,
                    analizador
                );
            
            // Iniciar gameplay en el thread de JME con los resultados
            juego.enqueue(() -> {
                if (!resultados.isEmpty()) {
                    System.out.println("\n✓ Análisis completado - Iniciando gameplay");
                    System.out.println("Resultados obtenidos: " + resultados.size() + " canciones");
                    
                    if (modoAleatorioActivo) {
                        System.out.println("🔀 Playlist en modo ALEATORIO");
                    }
                    
                    // ⭐ Llamar con la lista en el orden correcto (ya mezclada si aplica)
                    juego.startGameplay(listaFinal, resultados);
                } else {
                    System.out.println("\n✕ Análisis cancelado o falló");
                }
                return null;
            });
        });
    }

    /**
     * ⭐ NUEVO: Inicia el modo práctica (sin barra de vida, se juega hasta terminar)
     */
    private void iniciarModoPractica() {
        if (juego == null) return;

        // Determinar canciones (respeta selección y modo aleatorio)
        List<String> lista;
        if (cancionesSeleccionadas.isEmpty()) {
            lista = playlistManager.getCanciones();
            if (modoAleatorioActivo && !lista.isEmpty()) {
                lista = new ArrayList<>(lista);
                Collections.shuffle(lista);
            }
        } else {
            lista = new ArrayList<>(cancionesSeleccionadas);
            if (modoAleatorioActivo) {
                Collections.shuffle(lista);
            }
        }

        if (lista.isEmpty()) {
            System.err.println("ERROR: No hay canciones!");
            lblInstrucciones.setText("ERROR: No hay canciones .wav\nAgrega archivos a assets/canciones/");
            lblInstrucciones.setColor(ColorRGBA.Red);
            return;
        }

        final List<String> listaFinal = new ArrayList<>(lista);
        SwingUtilities.invokeLater(() -> {
            java.awt.Frame parentFrame = obtenerFramePadre();
            Map<String, ResultadoAnalisis> resultados =
                VentanaCargaCanciones.mostrarYAnalizarConResultados(
                    parentFrame,
                    listaFinal,
                    analizador
                );

            juego.enqueue(() -> {
                if (!resultados.isEmpty()) {
                    System.out.println("\n✓ Análisis completado - Iniciando Modo Práctica");
                    juego.startGameplayPractica(listaFinal, resultados);
                } else {
                    System.out.println("\n✕ Análisis cancelado o falló");
                }
                return null;
            });
        });
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
    
    // ==================== GETTERS Y SETTERS ====================
    
    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }
    
    public void setPlaylistManager(PlaylistManager pm) {
        this.playlistManager = pm;
    }
    
    /**
     * ⭐ NUEVO: Getter para el estado del modo aleatorio
     */
    public boolean isModoAleatorioActivo() {
        return modoAleatorioActivo;
    }
    
    /**
     * ⭐ NUEVO: Setter para el modo aleatorio (por si se quiere activar desde código)
     */
    public void setModoAleatorio(boolean activo) {
        this.modoAleatorioActivo = activo;
        actualizarIndicadorModoAleatorio();
    }
}