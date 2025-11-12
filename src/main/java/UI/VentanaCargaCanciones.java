/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import Modelo.AnalizadorCanciones;
import Modelo.AnalizadorCanciones.ResultadoAnalisis;

import javax.swing.*;
import javax.swing.Timer; // IMPORTANTE: Importar explícitamente javax.swing.Timer
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Ventana SWING que muestra el progreso del análisis de canciones
 * Ejecuta el análisis en un thread separado y actualiza la UI
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class VentanaCargaCanciones extends JDialog {
    
    private final List<String> canciones;
    private final AnalizadorCanciones analizador;
    private final Map<String, ResultadoAnalisis> resultados;
    
    // Estado
    private boolean completado = false;
    private boolean cancelado = false;
    private Thread threadAnalisis;
    
    // Componentes UI
    private JProgressBar progressBar;
    private JLabel lblEstado;
    private JLabel lblCancionActual;
    private JTextArea txtLog;
    private JButton btnCancelar;
    private JLabel lblProgreso;
    
    public VentanaCargaCanciones(Frame parent, List<String> canciones, AnalizadorCanciones analizador) {
        super(parent, "Analizando Canciones...", true);
        
        this.canciones = canciones;
        this.analizador = analizador;
        this.resultados = new LinkedHashMap<>();
        
        inicializarUI();
        configurarCierreVentana();
        setLocationRelativeTo(parent);
    }
    
    private void inicializarUI() {
        setLayout(new BorderLayout(15, 15));
        setSize(800, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        // Panel superior: Título y estado
        add(crearPanelSuperior(), BorderLayout.NORTH);
        
        // Panel central: Log de análisis
        add(crearPanelCentral(), BorderLayout.CENTER);
        
        // Panel inferior: Progreso y botón
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        panel.setBackground(new Color(40, 40, 40));
        
        // Título
        JLabel lblTitulo = new JLabel("🎵 Analizando Canciones");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.CYAN);
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Estado actual
        lblEstado = new JLabel("Preparando análisis...");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 16));
        lblEstado.setForeground(Color.WHITE);
        panel.add(lblEstado, BorderLayout.CENTER);
        
        // Canción actual
        lblCancionActual = new JLabel(" ");
        lblCancionActual.setFont(new Font("Arial", Font.BOLD, 14));
        lblCancionActual.setForeground(Color.YELLOW);
        panel.add(lblCancionActual, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Área de texto para log
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(Color.GREEN);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(txtLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Log de Análisis",
            0, 0, null, Color.LIGHT_GRAY
        ));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        // Panel de progreso
        JPanel panelProgreso = new JPanel(new BorderLayout(5, 5));
        
        lblProgreso = new JLabel("0 / " + canciones.size() + " canciones");
        lblProgreso.setFont(new Font("Arial", Font.BOLD, 12));
        panelProgreso.add(lblProgreso, BorderLayout.NORTH);
        
        progressBar = new JProgressBar(0, canciones.size());
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(600, 30));
        progressBar.setFont(new Font("Arial", Font.BOLD, 14));
        panelProgreso.add(progressBar, BorderLayout.CENTER);
        
        panel.add(panelProgreso, BorderLayout.CENTER);
        
        // Botón cancelar
        btnCancelar = new JButton("✕ CANCELAR");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(150, 0, 0));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(150, 35));
        btnCancelar.addActionListener(e -> cancelarAnalisis());
        
        panel.add(btnCancelar, BorderLayout.EAST);
        
        return panel;
    }
    
    private void configurarCierreVentana() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelarAnalisis();
            }
        });
    }
    
    /**
     * Inicia el análisis en un thread separado
     */
    public void iniciarAnalisis() {
        threadAnalisis = new Thread(() -> {
            try {
                analizarCanciones();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    agregarLog("❌ ERROR CRÍTICO: " + e.getMessage());
                    e.printStackTrace();
                    cancelado = true;
                    finalizarAnalisis();
                });
            }
        }, "AnalizadorThread");
        
        threadAnalisis.start();
    }
    
    /**
     * Proceso principal de análisis
     */
    private void analizarCanciones() {
        long tiempoInicio = System.currentTimeMillis();
        
        agregarLog("=".repeat(70));
        agregarLog("🎼 INICIANDO ANÁLISIS DE " + canciones.size() + " CANCIONES");
        agregarLog("=".repeat(70));
        agregarLog("");
        
        for (int i = 0; i < canciones.size(); i++) {
            if (cancelado) {
                agregarLog("\n⚠️ ANÁLISIS CANCELADO POR EL USUARIO");
                break;
            }
            
            String ruta = canciones.get(i);
            String nombre = Paths.get(ruta).getFileName().toString();
            
            final int indice = i + 1;
            
            // Actualizar UI
            SwingUtilities.invokeLater(() -> {
                lblEstado.setText("Analizando canción " + indice + " de " + canciones.size());
                lblCancionActual.setText("♪ " + nombre);
                lblProgreso.setText(indice + " / " + canciones.size() + " canciones");
                progressBar.setValue(indice);
            });
            
            agregarLog("\n[" + indice + "/" + canciones.size() + "] 📀 " + nombre);
            agregarLog("-".repeat(70));
            
            try {
                // Analizar la canción completa
                long tiempoCancionInicio = System.currentTimeMillis();
                ResultadoAnalisis resultado = analizador.analizarCompleto(ruta);
                long tiempoCancion = System.currentTimeMillis() - tiempoCancionInicio;
                
                // Guardar resultado
                resultados.put(ruta, resultado);
                
                // Mostrar resumen
                agregarLog(String.format("   ✓ Completado en %.2fs", tiempoCancion / 1000.0));
                agregarLog("   • Flechas normales: " + resultado.beatsNormales.size());
                agregarLog("   • Flechas especiales: " + resultado.beatsRapidos.size());
                agregarLog("   • Mecánica espacio: " + resultado.beatsLentos.size());
                
                if (resultado.getBPMEstimado() > 0) {
                    agregarLog(String.format("   • BPM estimado: %.1f", resultado.getBPMEstimado()));
                }
                
            } catch (Exception e) {
                agregarLog("   ❌ ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Finalizar
        if (!cancelado) {
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            
            agregarLog("");
            agregarLog("=".repeat(70));
            agregarLog("✅ ANÁLISIS COMPLETADO");
            agregarLog("=".repeat(70));
            agregarLog(String.format("   Tiempo total: %.2fs", tiempoTotal / 1000.0));
            agregarLog("   Canciones procesadas: " + resultados.size() + "/" + canciones.size());
            
            int totalBeats = 0;
            for (ResultadoAnalisis r : resultados.values()) {
                totalBeats += r.beatsNormales.size() + r.beatsRapidos.size() + r.beatsLentos.size();
            }
            agregarLog("   Total de beats detectados: " + totalBeats);
            agregarLog("=".repeat(70));
            
            completado = true;
        }
        
        SwingUtilities.invokeLater(() -> finalizarAnalisis());
    }
    
    /**
     * Agrega una línea al log (thread-safe)
     */
    private void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
        
        // También imprimir en consola
        System.out.println(mensaje);
    }
    
    /**
     * Cancela el análisis en progreso
     */
    private void cancelarAnalisis() {
        if (!completado && !cancelado) {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que quieres cancelar el análisis?\nSe perderá el progreso actual.",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (opcion == JOptionPane.YES_OPTION) {
                cancelado = true;
                agregarLog("\n⚠️ Cancelando análisis...");
                
                if (threadAnalisis != null && threadAnalisis.isAlive()) {
                    threadAnalisis.interrupt();
                }
            }
        }
    }
    
    /**
     * Finaliza el proceso y cierra la ventana
     */
    private void finalizarAnalisis() {
        if (completado) {
            lblEstado.setText("✅ Análisis completado exitosamente");
            lblEstado.setForeground(Color.GREEN);
            lblCancionActual.setText("Listo para jugar");
            
            btnCancelar.setText("✓ CONTINUAR");
            btnCancelar.setBackground(new Color(0, 150, 0));
            btnCancelar.removeActionListener(btnCancelar.getActionListeners()[0]);
            btnCancelar.addActionListener(e -> dispose());
            
        } else if (cancelado) {
            lblEstado.setText("⚠️ Análisis cancelado");
            lblEstado.setForeground(Color.ORANGE);
            lblCancionActual.setText("");
            
            btnCancelar.setText("✕ CERRAR");
            btnCancelar.removeActionListener(btnCancelar.getActionListeners()[0]);
            btnCancelar.addActionListener(e -> dispose());
        }
        
        // Esperar 2 segundos y cerrar automáticamente si completó
        if (completado) {
            Timer timer = new Timer(2000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    // Getters
    
    public boolean isCompletado() {
        return completado;
    }
    
    public boolean isCancelado() {
        return cancelado;
    }
    
    public Map<String, ResultadoAnalisis> getResultados() {
        return new LinkedHashMap<>(resultados);
    }
    
    /**
     * Método estático helper para abrir la ventana y ejecutar análisis
     * @return true si el análisis se completó, false si se canceló
     */
    public static boolean mostrarYAnalizar(Frame parent, List<String> canciones, AnalizadorCanciones analizador) {
        if (canciones == null || canciones.isEmpty()) {
            JOptionPane.showMessageDialog(
                parent,
                "No hay canciones para analizar",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        VentanaCargaCanciones ventana = new VentanaCargaCanciones(parent, canciones, analizador);
        
        // Iniciar análisis antes de mostrar la ventana
        ventana.iniciarAnalisis();
        
        // Mostrar ventana (bloqueante)
        ventana.setVisible(true);
        
        // Retornar resultado
        return ventana.isCompletado();
    }
    
    /**
     * Sobrecarga que también retorna los resultados del análisis
     */
    public static Map<String, ResultadoAnalisis> mostrarYAnalizarConResultados(
            Frame parent, 
            List<String> canciones, 
            AnalizadorCanciones analizador) {
        
        VentanaCargaCanciones ventana = new VentanaCargaCanciones(parent, canciones, analizador);
        ventana.iniciarAnalisis();
        ventana.setVisible(true);
        
        if (ventana.isCompletado()) {
            return ventana.getResultados();
        } else {
            return new LinkedHashMap<>();
        }
    }
}