/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import Modelo.FlechasGenerator;
import Modelo.FlechasGenerator.Difficulty;
import javax.swing.*;
import java.awt.*;

/**
 * Ventana para seleccionar la dificultad antes de iniciar el gameplay
 */
public class VentanaSeleccionDificultad extends JDialog {
    
    private Difficulty dificultadSeleccionada = Difficulty.NORMAL;
    private boolean confirmado = false;
    
    public VentanaSeleccionDificultad(Frame parent) {
        super(parent, "Seleccionar Dificultad", true);
        inicializarUI();
        setLocationRelativeTo(parent);
    }
    
    private void inicializarUI() {
        setLayout(new BorderLayout(15, 15));
        setSize(500, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Panel superior: Título
        add(crearPanelTitulo(), BorderLayout.NORTH);
        
        // Panel central: Botones de dificultad
        add(crearPanelDificultades(), BorderLayout.CENTER);
        
        // Panel inferior: Botón confirmar
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitulo = new JLabel("⚡ Selecciona la Dificultad");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.CYAN);
        panel.add(lblTitulo);
        
        return panel;
    }
    
    private JPanel crearPanelDificultades() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panel.setBackground(Color.WHITE);
        
        // Botones de dificultad
        crearBotonDificultad(panel, "🟢 FÁCIL", 
            "Menos flechas, más tiempo para reaccionar",
            Difficulty.FACIL,
            new Color(0, 200, 100));
            
        panel.add(Box.createVerticalStrut(20));
        
        crearBotonDificultad(panel, "🟡 NORMAL", 
            "Equilibrio entre diversión y desafío",
            Difficulty.NORMAL,
            new Color(255, 180, 0));
            
        panel.add(Box.createVerticalStrut(20));
        
        crearBotonDificultad(panel, "🔴 DIFÍCIL", 
            "Muchas flechas, velocidad alta",
            Difficulty.DIFICIL,
            new Color(255, 50, 50));
        
        return panel;
    }
    
    private void crearBotonDificultad(JPanel parent, String titulo, 
                                      String descripcion, 
                                      Difficulty dificultad,
                                      Color color) {
        JPanel panelBoton = new JPanel();
        panelBoton.setLayout(new BorderLayout(10, 5));
        panelBoton.setBackground(Color.WHITE);
        panelBoton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panelBoton.setMaximumSize(new Dimension(400, 80));
        panelBoton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Título del botón
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(color);
        panelBoton.add(lblTitulo, BorderLayout.NORTH);
        
        // Descripción
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDescripcion.setForeground(Color.DARK_GRAY);
        panelBoton.add(lblDescripcion, BorderLayout.CENTER);
        
        // Listener de click
        panelBoton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                seleccionarDificultad(dificultad);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panelBoton.setBackground(new Color(240, 240, 255));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panelBoton.setBackground(Color.WHITE);
            }
        });
        
        parent.add(panelBoton);
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnConfirmar = new JButton("✓ CONFIRMAR");
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 16));
        btnConfirmar.setBackground(new Color(0, 150, 0));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setPreferredSize(new Dimension(150, 40));
        btnConfirmar.addActionListener(e -> confirmar());
        panel.add(btnConfirmar);
        
        return panel;
    }
    
    private void seleccionarDificultad(Difficulty dificultad) {
        this.dificultadSeleccionada = dificultad;
        System.out.println("⚡ Dificultad seleccionada: " + dificultad);
    }
    
    private void confirmar() {
        confirmado = true;
        System.out.println("✓ Dificultad confirmada: " + dificultadSeleccionada);
        dispose();
    }
    
    // Getters
    public Difficulty getDificultadSeleccionada() {
        return dificultadSeleccionada;
    }
    
    public boolean fueConfirmado() {
        return confirmado;
    }
    
    /**
     * Método estático para mostrar la ventana y obtener resultado
     */
    public static Difficulty mostrarDialogo(Frame parent) {
        VentanaSeleccionDificultad ventana = new VentanaSeleccionDificultad(parent);
        ventana.setVisible(true); // Bloquea hasta que se cierre
        
        if (ventana.fueConfirmado()) {
            return ventana.getDificultadSeleccionada();
        } else {
            return Difficulty.NORMAL; // Default si cancela o cierra
        }
    }
}