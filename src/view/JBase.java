package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class JBase extends JFrame {
  public static final long serialVersionUID = 1L;
  public JPanel contentPane;
  public final int larguraInterno, alturaInterno, margem;

  // Definição das cores da interface
  public static final Color corExterna = new Color(27, 0, 69);
  public static final Color corInterna = new Color(38, 2, 92);
  public static final Color corBorda = new Color(120, 70, 220);
  public static final Color corLabel = new Color(255, 120, 220);
  public static final Color corCampo = new Color(25, 6, 75);
  public static final Color textos = Color.WHITE;

  public int getLarguraInterna() {
    return larguraInterno;
  }

  public int getAlturaInterna() {
    return alturaInterno;
  }

  public JBase() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setUndecorated(false);
    setMinimumSize(new Dimension(1200, 720));

    margem = 30;
    larguraInterno = areaUtil.width - (margem * 2) + 20;
    alturaInterno = areaUtil.height - (margem * 2);
  }

  public JButton estilizarBotao(JButton botao) {
    botao.setFont(new Font("Arial", Font.BOLD, 16));
    botao.setForeground(textos);
    botao.setBackground(corCampo);
    botao.setBorder(new LineBorder(corBorda, 1, true));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

    return botao;
  }
}
