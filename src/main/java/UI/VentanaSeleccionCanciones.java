/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import Modelo.PlaylistManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 * Ventana SWING para seleccionar canciones
 * ✅ Sistema de selección con checkboxes
 * ✅ NUEVO: Modo aleatorio (shuffle) de playlist
 * ✅ Indicador visual de modo random activo
 */
public class VentanaSeleccionCanciones extends JDialog {
    
    private PlaylistManager playlistManager;
    private Set<String> cancionesSeleccionadas;
    private Map<String, JCheckBox> checkboxesCanciones;
    private JLabel lblContador;
    private boolean confirmado = false;
    
    // ⭐ NUEVO: Sistema de modo aleatorio
    private boolean modoAleatorio = false;
    private JToggleButton btnModoAleatorio;
    private JLabel lblEstadoRandom;
    
    public VentanaSeleccionCanciones(Frame parent, PlaylistManager pm, Set<String> seleccionPrevia) {
        super(parent, "Seleccionar Canciones", true);
        this.playlistManager = pm;
        this.cancionesSeleccionadas = new HashSet<>(seleccionPrevia);
        this.checkboxesCanciones = new HashMap<>();
        
        inicializarUI();
        setLocationRelativeTo(parent);
    }
    
    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 850); // ⭐ Aumentado para el nuevo botón
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Panel superior: Título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(40, 40, 40));
        JLabel lblTitulo = new JLabel("♪ Seleccionar Canciones ♪");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.YELLOW);
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);
        
        // Panel central: Lista de canciones
        JPanel panelCanciones = crearPanelCanciones();
        JScrollPane scrollPane = new JScrollPane(panelCanciones);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Canciones Disponibles"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior: Botones y contador
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelCanciones() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        List<String> canciones = playlistManager.getCanciones();
        
        if (canciones.isEmpty()) {
            JLabel lblVacio = new JLabel("⚠ No hay canciones .wav en la carpeta");
            lblVacio.setForeground(Color.ORANGE);
            lblVacio.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(lblVacio);
        } else {
            for (String ruta : canciones) {
                String nombre = Paths.get(ruta).getFileName().toString();
                JCheckBox checkbox = new JCheckBox(nombre);
                checkbox.setFont(new Font("Arial", Font.PLAIN, 14));
                
                // Marcar si estaba seleccionada previamente
                if (cancionesSeleccionadas.contains(ruta)) {
                    checkbox.setSelected(true);
                }
                
                // Listener para actualizar el contador
                checkbox.addItemListener(e -> {
                    if (checkbox.isSelected()) {
                        cancionesSeleccionadas.add(ruta);
                    } else {
                        cancionesSeleccionadas.remove(ruta);
                    }
                    actualizarContador();
                });
                
                panel.add(checkbox);
                panel.add(Box.createVerticalStrut(5));
                
                checkboxesCanciones.put(ruta, checkbox);
            }
        }
        
        return panel;
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ⭐ NUEVO: Panel superior con modo aleatorio
        JPanel panelModoRandom = crearPanelModoAleatorio();
        panel.add(panelModoRandom, BorderLayout.NORTH);
        
        // Panel de botones rápidos (centro)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnSeleccionarTodas = new JButton("✓ Seleccionar Todas");
        btnSeleccionarTodas.setFont(new Font("Arial", Font.PLAIN, 12));
        btnSeleccionarTodas.addActionListener(e -> seleccionarTodas());
        panelAcciones.add(btnSeleccionarTodas);
        
        JButton btnLimpiar = new JButton("✕ Limpiar");
        btnLimpiar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnLimpiar.addActionListener(e -> limpiarSeleccion());
        panelAcciones.add(btnLimpiar);
        
        panel.add(panelAcciones, BorderLayout.WEST);
        
        // Contador y botones principales (abajo)
        JPanel panelInferiorBotones = new JPanel(new BorderLayout());
        
        // Contador
        lblContador = new JLabel("Seleccionadas: 0");
        lblContador.setFont(new Font("Arial", Font.BOLD, 14));
        lblContador.setForeground(new Color(0, 150, 0));
        panelInferiorBotones.add(lblContador, BorderLayout.WEST);
        
        actualizarContador();
        
        // Botones principales
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnConfirmar = new JButton("✓ CONFIRMAR");
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfirmar.setBackground(new Color(0, 150, 0));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.addActionListener(e -> confirmar());
        panelBotones.add(btnConfirmar);
        
        JButton btnCancelar = new JButton("✕ Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(150, 0, 0));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(e -> cancelar());
        panelBotones.add(btnCancelar);
        
        panelInferiorBotones.add(panelBotones, BorderLayout.EAST);
        panel.add(panelInferiorBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ==================== ⭐ NUEVO: PANEL DE MODO ALEATORIO ====================
    
    /**
     * Crea el panel con el botón de modo aleatorio
     */
    private JPanel crearPanelModoAleatorio() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 200), 2),
            "🎲 Modo de Reproducción",
            0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 200)
        ));
        panel.setBackground(new Color(245, 245, 255));
        
        // Panel izquierdo: Botón toggle
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBoton.setBackground(new Color(245, 245, 255));
        
        btnModoAleatorio = new JToggleButton("🔀 MODO ALEATORIO");
        btnModoAleatorio.setFont(new Font("Arial", Font.BOLD, 14));
        btnModoAleatorio.setFocusPainted(false);
        btnModoAleatorio.setPreferredSize(new Dimension(180, 40));
        
        // Colores según estado
        actualizarEstiloBotonRandom();
        
        btnModoAleatorio.addActionListener(e -> toggleModoAleatorio());
        
        panelBoton.add(btnModoAleatorio);
        panel.add(panelBoton, BorderLayout.WEST);
        
        // Panel derecho: Estado y descripción
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(new Color(245, 245, 255));
        
        lblEstadoRandom = new JLabel("Estado: Orden normal");
        lblEstadoRandom.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstadoRandom.setForeground(new Color(80, 80, 80));
        panelInfo.add(lblEstadoRandom);
        
        JLabel lblDescripcion = new JLabel("Las canciones se reproducirán aleatoriamente");
        lblDescripcion.setFont(new Font("Arial", Font.ITALIC, 10));
        lblDescripcion.setForeground(new Color(120, 120, 120));
        panelInfo.add(lblDescripcion);
        
        panel.add(panelInfo, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Toggle del modo aleatorio
     */
    /**
 * Toggle del modo aleatorio
 */
private void toggleModoAleatorio() {
    modoAleatorio = btnModoAleatorio.isSelected();
    
    actualizarEstiloBotonRandom();
    
    if (modoAleatorio) {
        lblEstadoRandom.setText("Estado: 🔀 MODO ALEATORIO ACTIVADO");
        lblEstadoRandom.setForeground(new Color(0, 150, 0));
        System.out.println("🔀 Modo aleatorio ACTIVADO - Las canciones se mezclarán");
        
        // Efecto visual de confirmación
        javax.swing.Timer timer = new javax.swing.Timer(100, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                count++;
                if (count % 2 == 0) {
                    btnModoAleatorio.setBackground(new Color(0, 150, 0));
                } else {
                    btnModoAleatorio.setBackground(new Color(0, 200, 100));
                }
                if (count >= 6) {
                    ((javax.swing.Timer)e.getSource()).stop();
                    btnModoAleatorio.setBackground(new Color(0, 150, 0));
                }
            }
        });
        timer.start();
        
    } else {
        lblEstadoRandom.setText("Estado: Orden normal");
        lblEstadoRandom.setForeground(new Color(80, 80, 80));
        System.out.println("📋 Modo aleatorio DESACTIVADO - Orden original");
    }
}
    
    /**
     * Actualiza el estilo del botón según su estado
     */
    private void actualizarEstiloBotonRandom() {
        if (modoAleatorio) {
            btnModoAleatorio.setBackground(new Color(0, 150, 0));
            btnModoAleatorio.setForeground(Color.WHITE);
            btnModoAleatorio.setText("🔀 ALEATORIO: ON");
        } else {
            btnModoAleatorio.setBackground(new Color(200, 200, 200));
            btnModoAleatorio.setForeground(Color.BLACK);
            btnModoAleatorio.setText("🔀 MODO ALEATORIO");
        }
    }
    
    // ==================== MÉTODOS EXISTENTES ====================
    
    private void seleccionarTodas() {
        for (JCheckBox checkbox : checkboxesCanciones.values()) {
            checkbox.setSelected(true);
        }
    }
    
    private void limpiarSeleccion() {
        for (JCheckBox checkbox : checkboxesCanciones.values()) {
            checkbox.setSelected(false);
        }
    }
    
    private void actualizarContador() {
        int count = cancionesSeleccionadas.size();
        lblContador.setText("Seleccionadas: " + count);
        
        if (count == 0) {
            lblContador.setForeground(Color.GRAY);
        } else {
            lblContador.setForeground(new Color(0, 150, 0));
        }
    }
    
    private void confirmar() {
        confirmado = true;
        
        System.out.println("\n=== SELECCIÓN CONFIRMADA ===");
        System.out.println("Total: " + cancionesSeleccionadas.size());
        System.out.println("Modo aleatorio: " + (modoAleatorio ? "SÍ 🔀" : "NO"));
        
        if (modoAleatorio) {
            System.out.println("⚠ Las canciones se mezclarán antes de jugar");
        }
        
        for (String ruta : cancionesSeleccionadas) {
            System.out.println("  - " + Paths.get(ruta).getFileName());
        }
        
        dispose();
    }
    
    private void cancelar() {
        confirmado = false;
        System.out.println("=== Selección cancelada ===");
        dispose();
    }
    
    // ==================== GETTERS ====================
    
    public Set<String> getCancionesSeleccionadas() {
        return new HashSet<>(cancionesSeleccionadas);
    }
    
    /**
     * ⭐ NUEVO: Retorna las canciones en orden aleatorio si el modo está activo
     */
    public List<String> getCancionesOrdenadas() {
        List<String> lista = new ArrayList<>(cancionesSeleccionadas);
        
        if (modoAleatorio && !lista.isEmpty()) {
            Collections.shuffle(lista);
            System.out.println("\n🔀 PLAYLIST MEZCLADA:");
            for (int i = 0; i < lista.size(); i++) {
                System.out.println("  " + (i+1) + ". " + Paths.get(lista.get(i)).getFileName());
            }
        }
        
        return lista;
    }
    
    public boolean fueConfirmado() {
        return confirmado;
    }
    
    /**
     * ⭐ NUEVO: Getter para saber si el modo aleatorio está activo
     */
    public boolean isModoAleatorio() {
        return modoAleatorio;
    }
    
    // ==================== MÉTODO ESTÁTICO ACTUALIZADO ====================
    
    /**
     * ⭐ ACTUALIZADO: Ahora retorna un objeto con toda la información
     */
    public static ResultadoSeleccion mostrarDialogo(Frame parent, PlaylistManager pm, Set<String> seleccionPrevia) {
        VentanaSeleccionCanciones ventana = new VentanaSeleccionCanciones(parent, pm, seleccionPrevia);
        ventana.setVisible(true);
        
        if (ventana.fueConfirmado()) {
            return new ResultadoSeleccion(
                ventana.getCancionesOrdenadas(),
                ventana.isModoAleatorio()
            );
        } else {
            // Retornar selección previa sin cambios
            return new ResultadoSeleccion(
                new ArrayList<>(seleccionPrevia),
                false
            );
        }
    }
    
    // ==================== ⭐ CLASE INTERNA: RESULTADO DE SELECCIÓN ====================
    
    /**
     * Clase para retornar tanto la lista de canciones como el estado del modo aleatorio
     */
    public static class ResultadoSeleccion {
        private final List<String> canciones;
        private final boolean modoAleatorio;
        
        public ResultadoSeleccion(List<String> canciones, boolean modoAleatorio) {
            this.canciones = canciones;
            this.modoAleatorio = modoAleatorio;
        }
        
        public List<String> getCanciones() {
            return canciones;
        }
        
        public Set<String> getCancionesComoSet() {
            return new HashSet<>(canciones);
        }
        
        public boolean isModoAleatorio() {
            return modoAleatorio;
        }
        
        public boolean isEmpty() {
            return canciones.isEmpty();
        }
        
        public int size() {
            return canciones.size();
        }
    }
}