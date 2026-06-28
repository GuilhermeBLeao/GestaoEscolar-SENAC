package view.Professor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class RegistrarAdvertenciaProfessor extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  public RegistrarAdvertenciaProfessor() {

    setTitle("Registrar Advertência - Professor");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Registrar Advertencia.png"));

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int margem = 30;
    int larguraInterno = areaUtil.width - (margem * 2);
    int alturaInterno = areaUtil.height - (margem * 2);

    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setResizable(false);

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);

    JPanel interno = new JPanel(null);
    interno.setBounds(20, 20, larguraInterno, alturaInterno);
    interno.setBackground(corInterna);
    externo.add(interno);

    JPanel topo = criarTopo();
    topo.setBounds(30, 25, larguraInterno - 60, 160);
    interno.add(topo);

    int larguraConteudo = 1220;
    int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

    JPanel formulario = criarCard("Nova Advertência");
    formulario.setBounds(xInicial, 220, 580, 670);
    interno.add(formulario);

    JPanel historico = criarCard("Histórico de Advertências");
    historico.setBounds(xInicial + 600, 220, 620, 670);
    interno.add(historico);

    criarFormulario(formulario);
    criarHistorico(historico);
  }

  private JPanel criarTopo() {
    JPanel topo =
        new JPanel(null) {
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
          }
        };
    topo.setOpaque(false);

    JLabel titulo = new JLabel("Registrar Advertência");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 50, 700, 45);
    topo.add(titulo);

    JLabel sub =
        new JLabel("Registre advertências para os alunos das turmas nas quais você leciona.");
    sub.setForeground(new Color(245, 225, 255));
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    sub.setBounds(42, 100, 900, 25);
    topo.add(sub);

    return topo;
  }

  private JPanel criarCard(String titulo) {
    JPanel painel = new JPanel(null);
    painel.setBackground(corCampo);
    painel.setBorder(new LineBorder(corBorda));

    JLabel lbl = new JLabel(titulo);
    lbl.setForeground(corLabel);
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lbl.setBounds(20, 15, 350, 30);
    painel.add(lbl);

    return painel;
  }

  private void criarFormulario(JPanel p) {

    int y = 70;

    p.add(label("Turma:", y));
    p.add(combo(y + 25));

    y += 80;
    p.add(label("Disciplina:", y));
    p.add(combo(y + 25));

    y += 80;
    p.add(label("Aluno:", y));
    p.add(combo(y + 25));

    y += 80;
    p.add(label("Motivo da advertência:", y));
    p.add(combo(y + 25));

    y += 80;
    p.add(label("Data da advertência:", y));

    JTextField data =
        new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    data.setBounds(20, y + 25, 520, 40);
    data.setForeground(Color.WHITE);
    data.setBackground(corCampo);
    p.add(data);

    y += 80;
    p.add(label("Descrição da advertência:", y));

    JTextArea area = new JTextArea();
    JScrollPane sp = new JScrollPane(area);
    area.setForeground(Color.WHITE);
    area.setBackground(corCampo);
    sp.setBounds(20, y + 25, 520, 100);
    sp.getVerticalScrollBar().setUnitIncrement(26);
    p.add(sp);

    int yBotao = 610;

    JButton registrar = new JButton("Registrar Advertência");
    registrar.setBounds(20, yBotao, 180, 40);
    estilizarBotao(registrar);
    p.add(registrar);

    JButton limpar = new JButton("Limpar");
    limpar.setBounds(210, yBotao, 140, 40);
    estilizarBotao(limpar);
    p.add(limpar);

    JButton cancelar = new JButton("Cancelar");
    cancelar.setBounds(360, yBotao, 180, 40);
    estilizarBotao(cancelar);
    cancelar.addActionListener(e -> dispose());
    p.add(cancelar);
  }

  private void estilizarBotao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
    botao.setForeground(textos);
    botao.setBackground(corCampo);
    botao.setBorder(new LineBorder(corBorda));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  private void criarHistorico(JPanel p) {

    DefaultTableModel model =
        new DefaultTableModel(new String[] {"Data", "Turma", "Disciplina", "Aluno", "Motivo"}, 0);

    JTable tabela = new JTable(model);
    tabela.setBackground(corCampo);
    tabela.setForeground(Color.WHITE);
    tabela.setFillsViewportHeight(true);

    JScrollPane sp = new JScrollPane(tabela);
    sp.setBounds(20, 60, 580, 540);
    sp.getVerticalScrollBar().setUnitIncrement(26);
    sp.getViewport().setBackground(corCampo);

    p.add(sp);
  }

  private JLabel label(String texto, int y) {
    JLabel l = new JLabel(texto);
    l.setForeground(corLabel);
    l.setBounds(20, y, 250, 20);
    return l;
  }

  private JComboBox<String> combo(int y) {
    JComboBox<String> c = new JComboBox<>(new String[] {"Selecione"});
    c.setBounds(20, y, 520, 40);
    c.setForeground(Color.WHITE);
    c.setBackground(corCampo);
    return c;
  }
}
