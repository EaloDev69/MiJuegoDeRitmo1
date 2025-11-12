/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Ventana SWING separada para configurar el volumen
 * Se abre como una ventana independiente del canvas de JME
 */
public class VentanaVolumen extends JDialog {
    
    private int volumenActual;
    private boolean confirmado = false;
    private JSlider sliderVolumen;
    private JLabel lblVolumenValor;

    public VentanaVolumen(Frame parent, int volumenInicial) {
        super(parent, "Configuración de Volumen", true); // Modal
        this.volumenActual = volumenInicial;
        
        inicializarUI();
        setLocationRelativeTo(parent); // Centrar en pantalla
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(15, 15));
        setSize(600, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Panel superior: Título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(40, 40, 40));
        JLabel lblTitulo = new JLabel("🔊 Control de Volumen");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.CYAN);
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);
        
        // Panel central: Slider y controles
        JPanel panelCentral = crearPanelCentral();
        add(panelCentral, BorderLayout.CENTER);
        
        // Panel inferior: Botones
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Label de valor actual
        lblVolumenValor = new JLabel("Volumen: " + volumenActual + "%");
        lblVolumenValor.setFont(new Font("Arial", Font.BOLD, 24));
        lblVolumenValor.setForeground(new Color(0, 150, 0));
        lblVolumenValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblVolumenValor);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Slider de volumen
        sliderVolumen = new JSlider(JSlider.HORIZONTAL, 0, 100, volumenActual);
        sliderVolumen.setMajorTickSpacing(25);
        sliderVolumen.setMinorTickSpacing(5);
        sliderVolumen.setPaintTicks(true);
        sliderVolumen.setPaintLabels(true);
        sliderVolumen.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Listener para actualizar el label en tiempo real
        sliderVolumen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int valor = sliderVolumen.getValue();
                lblVolumenValor.setText("Volumen: " + valor + "%");
                
                // Cambiar color según nivel
                if (valor == 0) {
                    lblVolumenValor.setForeground(Color.RED);
                } else if (valor < 30) {
                    lblVolumenValor.setForeground(Color.ORANGE);
                } else {
                    lblVolumenValor.setForeground(new Color(0, 150, 0));
                }
            }
        });
        
        panel.add(sliderVolumen);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Botones rápidos
        JPanel panelBotonesRapidos = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton btn0 = new JButton("0%");
        btn0.addActionListener(e -> sliderVolumen.setValue(0));
        panelBotonesRapidos.add(btn0);
        
        JButton btn25 = new JButton("25%");
        btn25.addActionListener(e -> sliderVolumen.setValue(25));
        panelBotonesRapidos.add(btn25);
        
        JButton btn50 = new JButton("50%");
        btn50.addActionListener(e -> sliderVolumen.setValue(50));
        panelBotonesRapidos.add(btn50);
        
        JButton btn75 = new JButton("75%");
        btn75.addActionListener(e -> sliderVolumen.setValue(75));
        panelBotonesRapidos.add(btn75);
        
        JButton btn100 = new JButton("100%");
        btn100.addActionListener(e -> sliderVolumen.setValue(100));
        panelBotonesRapidos.add(btn100);
        
        panel.add(panelBotonesRapidos);
        
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnAplicar = new JButton("✓ APLICAR");
        btnAplicar.setFont(new Font("Arial", Font.BOLD, 14));
        btnAplicar.setBackground(new Color(0, 150, 0));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setFocusPainted(false);
        btnAplicar.setPreferredSize(new Dimension(120, 35));
        btnAplicar.addActionListener(e -> aplicar());
        panel.add(btnAplicar);
        
        JButton btnCancelar = new JButton("✕ Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(150, 0, 0));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        btnCancelar.addActionListener(e -> cancelar());
        panel.add(btnCancelar);
        
        return panel;
    }

    private void aplicar() {
        volumenActual = sliderVolumen.getValue();
        confirmado = true;
        System.out.println("Volumen aplicado: " + volumenActual + "%");
        dispose();
    }

    private void cancelar() {
        confirmado = false;
        System.out.println("Cambios de volumen cancelados");
        dispose();
    }

    // Getters
    public int getVolumenSeleccionado() {
        return volumenActual;
    }

    public boolean fueConfirmado() {
        return confirmado;
    }

    /**
     * Método estático helper para abrir la ventana y esperar resultado
     */
    public static int mostrarDialogo(Frame parent, int volumenActual) {
        VentanaVolumen ventana = new VentanaVolumen(parent, volumenActual);
        ventana.setVisible(true); // Bloquea hasta que se cierre (modal)
        
        if (ventana.fueConfirmado()) {
            return ventana.getVolumenSeleccionado();
        } else {
            return volumenActual; // Mantener volumen anterior si canceló
        }
    }
}
