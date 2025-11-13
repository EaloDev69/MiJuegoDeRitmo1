/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import javax.swing.*;
import java.awt.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 * Ventana SWING para mostrar las instrucciones del juego
 * ✅ Diseño limpio y legible
 * ✅ Pestañas para organizar información
 * ✅ Iconos y colores para mejor UX
 * 
 * @author CamiLaNekoUwU_Gamer
 */
public class VentanaInstrucciones extends JDialog {
    
    public VentanaInstrucciones(Frame parent) {
        super(parent, "Instrucciones - Juego de Ritmo", true);
        inicializarUI();
        setLocationRelativeTo(parent);
    }
    
    private void inicializarUI() {
        setLayout(new BorderLayout(15, 15));
        setSize(1100, 900);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Panel superior: Título
        add(crearPanelTitulo(), BorderLayout.NORTH);
        
        // Panel central: Pestañas con instrucciones
        add(crearPanelPestanas(), BorderLayout.CENTER);
        
        // Panel inferior: Botón cerrar
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }
    
    // ==================== PANEL TÍTULO ====================
    
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitulo = new JLabel("🎵 CÓMO JUGAR");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitulo.setForeground(Color.CYAN);
        
        panel.add(lblTitulo);
        return panel;
    }
    
    // ==================== PANEL DE PESTAÑAS ====================
    
    private JTabbedPane crearPanelPestanas() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Pestaña 1: Inicio Rápido
        tabbedPane.addTab("🚀 Inicio Rápido", crearPanelInicioRapido());
        
        // Pestaña 2: Controles
        tabbedPane.addTab("🎮 Controles", crearPanelControles());
        
        // Pestaña 3: Tipos de Flechas
        tabbedPane.addTab("⭐ Flechas", crearPanelFlechas());
        
        // Pestaña 4: Sistema de Puntuación
        tabbedPane.addTab("🏆 Puntuación", crearPanelPuntuacion());
        
        // Pestaña 5: Consejos
        tabbedPane.addTab("💡 Consejos", crearPanelConsejos());
        
        return tabbedPane;
    }
    
    // ==================== PESTAÑA 1: INICIO RÁPIDO ====================
    
    private JPanel crearPanelInicioRapido() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);
        
        agregarTitulo(panel, "📋 Pasos para Empezar");
        
        agregarPaso(panel, "1", "Agrega Canciones",
            "Coloca archivos .wav en la carpeta:\n" +
            "MiJuegoDeRitmo1/assets/canciones/\n\n" +
            "Puedes usar el botón 'Abrir Carpeta Canciones' del menú.");
        
        agregarPaso(panel, "2", "Selecciona Canciones",
            "Presiona 'Seleccionar Canciones' en el menú.\n" +
            "Marca las canciones que quieres jugar.\n" +
            "Opcional: Activa 🔀 MODO ALEATORIO para mezclarlas.");
        
        agregarPaso(panel, "3", "¡A Jugar!",
            "Presiona 'EMPEZAR JUEGO'.\n" +
            "El juego analizará las canciones y comenzará.\n\n" +
            "Las flechas caerán hacia el centro de la pantalla.\n" +
            "Presiona las teclas cuando lleguen al objetivo central.");
        
        agregarSeparador(panel);
        
        agregarNota(panel, "⚠️ IMPORTANTE",
            "Solo se aceptan archivos .WAV\n" +
            "Los archivos MP3 no son compatibles (por ahora).",
            new Color(255, 200, 0));
        
        return panel;
    }
    
    // ==================== PESTAÑA 2: CONTROLES ====================
    
    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);
        
        agregarTitulo(panel, "🎮 Controles del Juego");
        
        agregarSubtitulo(panel, "Controles Duales (elige el que prefieras):");
        
        JPanel panelControles = new JPanel(new GridLayout(5, 2, 20, 15));
        panelControles.setBackground(Color.WHITE);
        panelControles.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Fila 1: ARRIBA
        agregarControl(panelControles, "↑ ARRIBA", "W  o  ↑", new Color(100, 150, 255));
        
        // Fila 2: ABAJO
        agregarControl(panelControles, "↓ ABAJO", "S  o  ↓", new Color(100, 150, 255));
        
        // Fila 3: IZQUIERDA
        agregarControl(panelControles, "← IZQUIERDA", "A  o  ←", new Color(255, 100, 100));
        
        // Fila 4: DERECHA
        agregarControl(panelControles, "→ DERECHA", "D  o  →", new Color(255, 100, 100));
        
        // Fila 5: ESPECIAL
        agregarControl(panelControles, "☾ ESPECIAL", "ESPACIO", new Color(200, 150, 255));
        
        panel.add(panelControles);
        
        agregarSeparador(panel);
        
        agregarSubtitulo(panel, "Otros Controles:");
        agregarTextoSimple(panel, "• ESC: Pausar/Reanudar el juego");
        agregarTextoSimple(panel, "• En pausa: ↑↓ para navegar, ENTER para seleccionar");
        
        agregarSeparador(panel);
        
        agregarNota(panel, "💡 CONSEJO",
            "Usa el esquema de controles con el que te sientas más cómodo.\n" +
            "Ambos funcionan igual de bien.",
            new Color(100, 200, 100));
        
        return panel;
    }
    
    private void agregarControl(JPanel panel, String direccion, String teclas, Color color) {
        // Label de dirección
        JLabel lblDireccion = new JLabel(direccion);
        lblDireccion.setFont(new Font("Arial", Font.BOLD, 16));
        lblDireccion.setForeground(color);
        lblDireccion.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblDireccion);
        
        // Label de teclas
        JLabel lblTeclas = new JLabel(teclas);
        lblTeclas.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTeclas.setForeground(new Color(60, 60, 60));
        lblTeclas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        lblTeclas.setOpaque(true);
        lblTeclas.setBackground(new Color(245, 245, 245));
        panel.add(lblTeclas);
    }
    
    // ==================== PESTAÑA 3: TIPOS DE FLECHAS ====================
    
    private JPanel crearPanelFlechas() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);
        
        agregarTitulo(panel, "⭐ Tipos de Flechas");
        
        // Flecha NORMAL
        agregarTipoFlecha(panel, "🔵 FLECHAS NORMALES",
            "• Color según dirección (Rojo/Azul)\n" +
            "• Velocidad estándar\n" +
            "• Puntos base: x1.0",
            new Color(100, 150, 255));
        
        agregarSeparador(panel);
        
        // Flecha RÁPIDA
        agregarTipoFlecha(panel, "🟢 FLECHAS RÁPIDAS",
            "• Color más brillante que las normales\n" +
            "• Velocidad aumentada (x1.25)\n" +
            "• Puntos multiplicados: x1.5\n" +
            "• Requieren reflejos más rápidos",
            new Color(100, 255, 100));
        
        agregarSeparador(panel);
        
        // Flecha DORADA
        agregarTipoFlecha(panel, "🟡 FLECHAS DORADAS",
            "• Color dorado brillante\n" +
            "• ⚠️ ¡SE INVIERTE a mitad de camino!\n" +
            "• Presiona la dirección OPUESTA cuando se inviertan\n" +
            "• Puntos multiplicados: x2.0\n" +
            "• Ejemplo: Si viene ↑ y se invierte, presiona ↓",
            new Color(255, 200, 0));
        
        agregarSeparador(panel);
        
        // Flecha LUNA (ESPECIAL)
        agregarTipoFlecha(panel, "☾ FLECHAS DE LUNA",
            "• Color violeta/morado\n" +
            "• Usa la tecla ESPACIO (no direccionales)\n" +
            "• Puntos multiplicados: x3.0\n" +
            "• Velocidad más lenta para facilitar el hit\n" +
            "• Hace brillar el objeto central",
            new Color(200, 150, 255));
        
        return panel;
    }
    
    // ==================== PESTAÑA 4: PUNTUACIÓN ====================
    
    private JPanel crearPanelPuntuacion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);
        
        agregarTitulo(panel, "🏆 Sistema de Puntuación");
        
        agregarSubtitulo(panel, "Ventanas de Timing:");
        
        JPanel panelTiming = new JPanel(new GridLayout(4, 2, 10, 10));
        panelTiming.setBackground(Color.WHITE);
        panelTiming.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        agregarTiming(panelTiming, "¡IMPECABLE!", "< 20ms", "150 pts", new Color(0, 255, 150));
        agregarTiming(panelTiming, "¡PERFECTO!", "< 50ms", "100 pts", new Color(0, 200, 100));
        agregarTiming(panelTiming, "BUENO", "< 150ms", "50 pts", new Color(255, 200, 0));
        agregarTiming(panelTiming, "MALO", "< 300ms", "20 pts", new Color(255, 150, 0));
        
        panel.add(panelTiming);
        
        agregarSeparador(panel);
        
        agregarSubtitulo(panel, "Sistema de Combos:");
        agregarTextoSimple(panel, "• Mantén hits exitosos para aumentar tu combo");
        agregarTextoSimple(panel, "• Combo > 5: +5% puntos por cada combo adicional");
        agregarTextoSimple(panel, "• Combo ≥ 10 perfectos: +50% puntos extra");
        agregarTextoSimple(panel, "• Un MALO o MISS rompe el combo");
        
        agregarSeparador(panel);
        
        agregarSubtitulo(panel, "Bonificaciones:");
        agregarTextoSimple(panel, "✨ Racha de 10 perfectos: x1.5 multiplicador");
        agregarTextoSimple(panel, "💚 Combo ≥ 10 perfectos: +2 vida por hit");
        agregarTextoSimple(panel, "⭐ Tipos de flecha: NORMAL x1, RÁPIDA x1.5, DORADA x2, LUNA x3");
        
        agregarSeparador(panel);
        
        agregarSubtitulo(panel, "Sistema de Ranking:");
        JPanel panelRanking = new JPanel(new GridLayout(8, 1, 5, 5));
        panelRanking.setBackground(Color.WHITE);
        panelRanking.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        agregarRanking(panelRanking, "SSS", "98%+ precisión, 90%+ perfectos", new Color(255, 215, 0));
        agregarRanking(panelRanking, "SS", "95%+ precisión, 80%+ perfectos", new Color(255, 165, 0));
        agregarRanking(panelRanking, "S", "90%+ precisión", new Color(255, 100, 100));
        agregarRanking(panelRanking, "A", "85%+ precisión", new Color(100, 255, 100));
        agregarRanking(panelRanking, "B", "75%+ precisión", new Color(100, 200, 255));
        agregarRanking(panelRanking, "C", "60%+ precisión", new Color(150, 150, 150));
        agregarRanking(panelRanking, "D", "40%+ precisión", new Color(200, 150, 100));
        agregarRanking(panelRanking, "F", "< 40% precisión", new Color(150, 150, 150));
        
        panel.add(panelRanking);
        
        return panel;
    }
    
    private void agregarTiming(JPanel panel, String nombre, String ventana, String puntos, Color color) {
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setForeground(color);
        panel.add(lblNombre);
        
        JLabel lblInfo = new JLabel(ventana + " = " + puntos);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblInfo);
    }
    
    private void agregarRanking(JPanel panel, String rank, String requisito, Color color) {
        JLabel lbl = new JLabel("  " + rank + " - " + requisito);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(color);
        panel.add(lbl);
    }
    
    // ==================== PESTAÑA 5: CONSEJOS ====================
    
    private JPanel crearPanelConsejos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);
        
        agregarTitulo(panel, "💡 Consejos Profesionales");
        
        agregarConsejo(panel, "🎯 Enfócate en el Centro",
            "Las flechas convergen al centro de la pantalla.\n" +
            "Mantén tu vista en el objetivo central, no sigas las flechas con la mirada.");
        
        agregarConsejo(panel, "👀 Anticipación Visual",
            "Las flechas aparecen 2 segundos antes del hit.\n" +
            "Usa este tiempo para preparar tus dedos.");
        
        agregarConsejo(panel, "🎹 Usa Ambas Manos",
            "WASD con mano izquierda, ESPACIO con pulgar.\n" +
            "O usa las flechas del teclado con mano derecha.");
        
        agregarConsejo(panel, "⚡ Prioriza la Precisión",
            "Es mejor un PERFECTO que varios BUENOS.\n" +
            "Los combos largos dan muchos más puntos.");
        
        agregarConsejo(panel, "🟡 Cuidado con las Doradas",
            "Observa cuando se invierten (a mitad del camino).\n" +
            "El sprite gira 180° cuando cambia de dirección.");
        
        agregarConsejo(panel, "☾ Aprovecha las Lunas",
            "Dan x3 puntos y son más lentas.\n" +
            "No desperdicies estas oportunidades fáciles.");
        
        agregarConsejo(panel, "🔀 Practica con Modo Aleatorio",
            "Te ayuda a no memorizar patrones.\n" +
            "Mejora tus reflejos puros.");
        
        agregarConsejo(panel, "💚 Gestiona tu Vida",
            "Combos largos recuperan vida.\n" +
            "Los misses consecutivos quitan más vida.");
        
        agregarSeparador(panel);
        
        agregarNota(panel, "🎓 PARA PRINCIPIANTES",
            "Empieza con canciones lentas (60-90 BPM).\n" +
            "Practica hasta conseguir combos de 10+.\n" +
            "Luego sube la dificultad gradualmente.",
            new Color(100, 150, 255));
        
        return panel;
    }
    
    // ==================== PANEL INFERIOR ====================
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnCerrar = new JButton("✓ ENTENDIDO");
        btnCerrar.setFont(new Font("Arial", Font.BOLD, 16));
        btnCerrar.setBackground(new Color(0, 150, 0));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(200, 45));
        btnCerrar.addActionListener(e -> dispose());
        
        panel.add(btnCerrar);
        return panel;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private void agregarTitulo(JPanel panel, String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 24));
        lbl.setForeground(new Color(40, 40, 40));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(lbl);
    }
    
    private void agregarSubtitulo(JPanel panel, String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setForeground(new Color(60, 60, 60));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(lbl);
    }
    
    private void agregarTextoSimple(JPanel panel, String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 0));
        panel.add(lbl);
    }
    
    private void agregarPaso(JPanel panel, String numero, String titulo, String descripcion) {
        JPanel pasoPanel = new JPanel(new BorderLayout(10, 5));
        pasoPanel.setBackground(Color.WHITE);
        pasoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        pasoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pasoPanel.setMaximumSize(new Dimension(700, 150));
        
        // Número del paso
        JLabel lblNumero = new JLabel(numero);
        lblNumero.setFont(new Font("Arial", Font.BOLD, 36));
        lblNumero.setForeground(new Color(100, 150, 255));
        lblNumero.setPreferredSize(new Dimension(50, 50));
        lblNumero.setHorizontalAlignment(SwingConstants.CENTER);
        pasoPanel.add(lblNumero, BorderLayout.WEST);
        
        // Contenido
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(40, 40, 40));
        contenido.add(lblTitulo);
        
        JTextArea txtDescripcion = new JTextArea(descripcion);
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDescripcion.setForeground(new Color(80, 80, 80));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        contenido.add(txtDescripcion);
        
        pasoPanel.add(contenido, BorderLayout.CENTER);
        
        panel.add(pasoPanel);
        panel.add(Box.createVerticalStrut(10));
    }
    
    private void agregarTipoFlecha(JPanel panel, String titulo, String descripcion, Color color) {
        JPanel flechaPanel = new JPanel(new BorderLayout(10, 5));
        flechaPanel.setBackground(new Color(250, 250, 250));
        flechaPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        flechaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        flechaPanel.setMaximumSize(new Dimension(700, 120));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(color);
        flechaPanel.add(lblTitulo, BorderLayout.NORTH);
        
        JTextArea txtDescripcion = new JTextArea(descripcion);
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDescripcion.setForeground(new Color(60, 60, 60));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        flechaPanel.add(txtDescripcion, BorderLayout.CENTER);
        
        panel.add(flechaPanel);
        panel.add(Box.createVerticalStrut(5));
    }
    
    private void agregarConsejo(JPanel panel, String titulo, String descripcion) {
        JPanel consejoPanel = new JPanel(new BorderLayout(10, 5));
        consejoPanel.setBackground(new Color(255, 255, 240));
        consejoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 180, 100), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        consejoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        consejoPanel.setMaximumSize(new Dimension(700, 90));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(100, 80, 0));
        consejoPanel.add(lblTitulo, BorderLayout.NORTH);
        
        JTextArea txtDescripcion = new JTextArea(descripcion);
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDescripcion.setForeground(new Color(80, 80, 80));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        consejoPanel.add(txtDescripcion, BorderLayout.CENTER);
        
        panel.add(consejoPanel);
        panel.add(Box.createVerticalStrut(8));
    }
    
    private void agregarNota(JPanel panel, String titulo, String mensaje, Color color) {
        JPanel notaPanel = new JPanel(new BorderLayout(10, 5));
        notaPanel.setBackground(new Color(255, 255, 255));
        notaPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        notaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        notaPanel.setMaximumSize(new Dimension(700, 100));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setForeground(color);
        notaPanel.add(lblTitulo, BorderLayout.NORTH);
        
        JTextArea txtMensaje = new JTextArea(mensaje);
        txtMensaje.setFont(new Font("Arial", Font.PLAIN, 13));
        txtMensaje.setForeground(new Color(60, 60, 60));
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setEditable(false);
        txtMensaje.setOpaque(false);
        notaPanel.add(txtMensaje, BorderLayout.CENTER);
        
        panel.add(notaPanel);
        panel.add(Box.createVerticalStrut(10));
    }
    
    private void agregarSeparador(JPanel panel) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(700, 2));
        sep.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(sep);
    }
    
    // ==================== MÉTODO ESTÁTICO ====================
    
    /**
     * Método estático para mostrar la ventana
     */
    public static void mostrar(Frame parent) {
        VentanaInstrucciones ventana = new VentanaInstrucciones(parent);
        ventana.setVisible(true);
    }
}

