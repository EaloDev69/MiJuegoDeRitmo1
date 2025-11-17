/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

/**
 * Ventana de resultados al finalizar una canción
 * ✅ Muestra estadísticas detalladas
 * ✅ Detecta GAME OVER (vida = 0)
 * ✅ Permite continuar o volver al menú
 */
public class VentanaResultados extends JDialog {
    
    private boolean continuar = false;
    
    public VentanaResultados(Frame parent, String nombreCancion, int score,
                            int maxCombo, int perfectos, int buenos,
                            int malos, int misses, int vidaRestante) {
        super(parent, "Resultados", true);
        
        // ⭐ Detectar si es GAME OVER
        boolean esGameOver = vidaRestante <= 0;
        
        inicializarUI(nombreCancion, score, maxCombo, perfectos, buenos, 
                     malos, misses, vidaRestante, esGameOver);
        setLocationRelativeTo(parent);
    }
    
    private void inicializarUI(String nombreCancion, int score, int maxCombo,
                               int perfectos, int buenos, int malos, int misses, 
                               int vidaRestante, boolean esGameOver) {
        setLayout(new BorderLayout(15, 15));
        setSize(600, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Panel superior: Título
        add(crearPanelTitulo(nombreCancion, esGameOver), BorderLayout.NORTH);
        
        // Panel central: Estadísticas
        add(crearPanelEstadisticas(score, maxCombo, perfectos, buenos, 
                                   malos, misses, vidaRestante, esGameOver), 
            BorderLayout.CENTER);
        
        // Panel inferior: Botones
        add(crearPanelBotones(esGameOver), BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelTitulo(String nombreCancion, boolean esGameOver) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // ⭐ Color de fondo según resultado
        if (esGameOver) {
            panel.setBackground(new Color(60, 20, 20)); // Rojo oscuro
        } else {
            panel.setBackground(new Color(40, 40, 40)); // Gris oscuro
        }
        
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // ⭐ Título principal
        JLabel lblEstado;
        if (esGameOver) {
            lblEstado = new JLabel("💀 GAME OVER");
            lblEstado.setFont(new Font("Arial", Font.BOLD, 48));
            lblEstado.setForeground(new Color(255, 80, 80)); // Rojo brillante
        } else {
            lblEstado = new JLabel("🎵 Canción Completada");
            lblEstado.setFont(new Font("Arial", Font.BOLD, 32));
            lblEstado.setForeground(Color.CYAN);
        }
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblEstado);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Nombre de la canción
        String nombre = nombreCancion;
        if (nombreCancion.contains("/")) {
            String[] partes = nombreCancion.split("/");
            nombre = partes[partes.length - 1];
        }
        if (nombre.endsWith(".wav")) {
            nombre = nombre.substring(0, nombre.length() - 4);
        }
        
        JLabel lblCancion = new JLabel(nombre);
        lblCancion.setFont(new Font("Arial", Font.PLAIN, 18));
        lblCancion.setForeground(Color.WHITE);
        lblCancion.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblCancion);
        
        return panel;
    }
    
    private JPanel crearPanelEstadisticas(int score, int maxCombo, int perfectos,
                                          int buenos, int malos, int misses, 
                                          int vidaRestante, boolean esGameOver) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.setBackground(Color.WHITE);
        
        // Calcular totales
        int totalNotas = perfectos + buenos + malos + misses;
        float precision = totalNotas > 0 ? ((float)(perfectos + buenos) / totalNotas) * 100 : 0f;
        
        // Score principal
        JLabel lblScore = new JLabel("SCORE: " + score);
        lblScore.setFont(new Font("Arial", Font.BOLD, 48));
        lblScore.setForeground(new Color(255, 215, 0)); // Dorado
        lblScore.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblScore);
        
        panel.add(Box.createVerticalStrut(30));
        
        // Precisión
        JLabel lblPrecision = new JLabel(String.format("Precisión: %.1f%%", precision));
        lblPrecision.setFont(new Font("Arial", Font.BOLD, 28));
        lblPrecision.setForeground(obtenerColorPrecision(precision));
        lblPrecision.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblPrecision);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Combo máximo
        agregarEstadistica(panel, "Combo Máximo:", maxCombo + "x", new Color(255, 140, 0));
        
        panel.add(Box.createVerticalStrut(30));
        
        // Separador
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(400, 2));
        panel.add(sep);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Detalles de notas
        agregarEstadistica(panel, "⭐ Perfectos:", String.valueOf(perfectos), new Color(0, 200, 100));
        agregarEstadistica(panel, "✓ Buenos:", String.valueOf(buenos), new Color(200, 200, 0));
        agregarEstadistica(panel, "~ Malos:", String.valueOf(malos), new Color(255, 140, 0));
        agregarEstadistica(panel, "✕ Misses:", String.valueOf(misses), new Color(200, 0, 0));
        
        panel.add(Box.createVerticalStrut(20));
        
        // Separador
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(400, 2));
        panel.add(sep2);
        
        panel.add(Box.createVerticalStrut(20));
        
        // ⭐ Vida restante con énfasis en GAME OVER
        JLabel lblVida;
        if (esGameOver) {
            lblVida = new JLabel("❌ SIN VIDA - GAME OVER");
            lblVida.setFont(new Font("Arial", Font.BOLD, 24));
            lblVida.setForeground(Color.RED);
        } else {
            lblVida = new JLabel("Vida Restante: " + vidaRestante + "%");
            lblVida.setFont(new Font("Arial", Font.BOLD, 20));
            lblVida.setForeground(obtenerColorVida(vidaRestante));
        }
        lblVida.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblVida);
        
        return panel;
    }
    
    private void agregarEstadistica(JPanel panel, String label, String valor, Color color) {
        JPanel lineaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        lineaPanel.setBackground(Color.WHITE);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        lblLabel.setForeground(Color.DARK_GRAY);
        
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Arial", Font.BOLD, 24));
        lblValor.setForeground(color);
        
        lineaPanel.add(lblLabel);
        lineaPanel.add(lblValor);
        panel.add(lineaPanel);
    }
    
    private Color obtenerColorPrecision(float precision) {
        if (precision >= 90) return new Color(0, 200, 100); // Verde
        if (precision >= 70) return new Color(200, 200, 0); // Amarillo
        if (precision >= 50) return new Color(255, 140, 0); // Naranja
        return new Color(200, 0, 0); // Rojo
    }
    
    private Color obtenerColorVida(int vida) {
        if (vida > 60) return new Color(0, 200, 100);
        if (vida > 30) return new Color(200, 200, 0);
        return new Color(200, 0, 0);
    }
    
    private JPanel crearPanelBotones(boolean esGameOver) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    if (esGameOver) {
        // ⭐ NUEVO: Botón "Reintentar" para Game Over
        JButton btnReintentar = new JButton("🔄 REINTENTAR");
        btnReintentar.setFont(new Font("Arial", Font.BOLD, 18));
        btnReintentar.setBackground(new Color(0, 150, 0));
        btnReintentar.setForeground(Color.WHITE);
        btnReintentar.setFocusPainted(false);
        btnReintentar.setPreferredSize(new Dimension(200, 50));
        btnReintentar.addActionListener(e -> {
            continuar = true; // ⭐ TRUE para reintentar
            dispose();
        });
        panel.add(btnReintentar);

        // Botón "Volver al Menú"
        JButton btnMenu = new JButton("⌂ VOLVER AL MENÚ");
        btnMenu.setFont(new Font("Arial", Font.BOLD, 18));
        btnMenu.setBackground(new Color(150, 0, 0));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setFocusPainted(false);
        btnMenu.setPreferredSize(new Dimension(200, 50));
        btnMenu.addActionListener(e -> {
            continuar = false;
            dispose();
        });
        panel.add(btnMenu);

    } else {
        // Si no es GAME OVER, mostrar ambos botones normales
        JButton btnContinuar = new JButton("▶ CONTINUAR");
        btnContinuar.setFont(new Font("Arial", Font.BOLD, 16));
        btnContinuar.setBackground(new Color(0, 150, 0));
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setFocusPainted(false);
        btnContinuar.setPreferredSize(new Dimension(180, 45));
        btnContinuar.addActionListener(e -> {
            continuar = true;
            dispose();
        });
        panel.add(btnContinuar);

        JButton btnMenu = new JButton("⌂ VOLVER AL MENÚ");
        btnMenu.setFont(new Font("Arial", Font.BOLD, 16));
        btnMenu.setBackground(new Color(100, 100, 100));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setFocusPainted(false);
        btnMenu.setPreferredSize(new Dimension(180, 45));
        btnMenu.addActionListener(e -> {
            continuar = false;
            dispose();
        });
        panel.add(btnMenu);
    }

    return panel;
}
    public boolean getContinuar() {
        return continuar;
    }
    
    /**
     * Método estático para mostrar la ventana y obtener respuesta
     */
    public static boolean mostrarResultados(Frame parent, String nombreCancion,
                                           int score, int maxCombo, int perfectos,
                                           int buenos, int malos, int misses, 
                                           int vidaRestante) {
        VentanaResultados ventana = new VentanaResultados(parent, nombreCancion,
                                                          score, maxCombo, perfectos,
                                                          buenos, malos, misses, 
                                                          vidaRestante);
        ventana.setVisible(true); // Bloquea hasta que se cierre
        return ventana.getContinuar();
    }
}