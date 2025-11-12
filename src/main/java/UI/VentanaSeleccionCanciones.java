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

/**
 * Ventana SWING separada para seleccionar canciones
 * Se abre como una ventana independiente del canvas de JME
 */
public class VentanaSeleccionCanciones extends JDialog {
    
    private PlaylistManager playlistManager;
    private Set<String> cancionesSeleccionadas;
    private Map<String, JCheckBox> checkboxesCanciones;
    private JLabel lblContador;
    private boolean confirmado = false;

    public VentanaSeleccionCanciones(Frame parent, PlaylistManager pm, Set<String> seleccionPrevia) {
        super(parent, "Seleccionar Canciones", true); // Modal
        this.playlistManager = pm;
        this.cancionesSeleccionadas = new HashSet<>(seleccionPrevia);
        this.checkboxesCanciones = new HashMap<>();
        
        inicializarUI();
        setLocationRelativeTo(parent); // Centrar en pantalla
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Panel superior: Título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(40, 40, 40));
        JLabel lblTitulo = new JLabel("♪ Seleccionar Canciones ♪");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.YELLOW);
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);
        
        // Panel central: Lista de canciones con checkboxes
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
                panel.add(Box.createVerticalStrut(5)); // Espaciado
                checkboxesCanciones.put(ruta, checkbox);
            }
        }
        
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de botones rápidos
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnSeleccionarTodas = new JButton("Seleccionar Todas");
        btnSeleccionarTodas.addActionListener(e -> seleccionarTodas());
        panelAcciones.add(btnSeleccionarTodas);
        
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarSeleccion());
        panelAcciones.add(btnLimpiar);
        
        panel.add(panelAcciones, BorderLayout.WEST);
        
        // Contador
        lblContador = new JLabel("Seleccionadas: 0");
        lblContador.setFont(new Font("Arial", Font.BOLD, 14));
        lblContador.setForeground(new Color(0, 150, 0));
        panel.add(lblContador, BorderLayout.CENTER);
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
        
        panel.add(panelBotones, BorderLayout.EAST);
        
        return panel;
    }

    private void seleccionarTodas() {
        for (JCheckBox checkbox : checkboxesCanciones.values()) {
            checkbox.setSelected(true);
        }
        // El listener de cada checkbox actualizará el Set y el contador
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
        System.out.println("\n=== SELECCION CONFIRMADA (Swing) ===");
        System.out.println("Total: " + cancionesSeleccionadas.size());
        for (String ruta : cancionesSeleccionadas) {
            System.out.println("  - " + Paths.get(ruta).getFileName());
        }
        dispose();
    }

    private void cancelar() {
        confirmado = false;
        System.out.println("=== Seleccion cancelada ===");
        dispose();
    }

    // Getters
    public Set<String> getCancionesSeleccionadas() {
        return new HashSet<>(cancionesSeleccionadas);
    }

    public boolean fueConfirmado() {
        return confirmado;
    }

    /**
     * Método estático helper para abrir la ventana y esperar resultado
     */
    public static Set<String> mostrarDialogo(Frame parent, PlaylistManager pm, Set<String> seleccionPrevia) {
        VentanaSeleccionCanciones ventana = new VentanaSeleccionCanciones(parent, pm, seleccionPrevia);
        ventana.setVisible(true); // Bloquea hasta que se cierre (modal)
        
        if (ventana.fueConfirmado()) {
            return ventana.getCancionesSeleccionadas();
        } else {
            return seleccionPrevia; // Mantener selección anterior si canceló
        }
    }
}
