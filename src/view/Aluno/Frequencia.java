package view.Aluno;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import util.DadosSistema;

public class Frequencia extends JFrame {

  private static final long serialVersionUID = 1L;

  // CORES
  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaFaltas;
  private DefaultTableModel modeloTabela;

  public Frequencia() {
    setTitle("Minhas Faltas - Aluno");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Frequencia.png"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int margem = 30;
    int larguraInterno = areaUtil.width - (margem * 2);
    int alturaInterno = areaUtil.height - (margem * 2);

    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(1280, 720));
    setResizable(false);

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);

    JPanel interno = new JPanel(null);
    interno.setBounds(20, 20, larguraInterno, alturaInterno);
    interno.setBackground(corInterna);
    externo.add(interno);

    criarConteudo(interno, larguraInterno, alturaInterno);
  }

  private void criarConteudo(JPanel interno, int larguraInterno, int alturaInterno) {
    JPanel topo = criarTopo();
    topo.setBounds(30, 25, larguraInterno - 60, 160);
    interno.add(topo);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(35, 35, 150, 40);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    JPanel painelRolagem = new JPanel(null);
    painelRolagem.setBackground(corInterna);

    int larguraConteudo = 1220;
    int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;
    if (xInicial < 0) {
      xInicial = 0;
    }

    JPanel painelRolagem1 = new JPanel(null);
    painelRolagem1.setBackground(corInterna);

    int larguraConteudo1 = 1220;
    int xInicial1 = ((larguraInterno - 60) - larguraConteudo1) / 2;
    if (xInicial1 < 0) {
      xInicial1 = 0;
    }
    painelRolagem1.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
    interno.add(painelRolagem1);

    JPanel cardResumo = criarPainelArredondado();
    cardResumo.setLayout(null);
    cardResumo.setBounds(xInicial, 0, 1220, 170);
    painelRolagem1.add(cardResumo);

    Object[] dadosAluno = DadosSistema.dadosAlunoAtualLinha();
    adicionarResumo(cardResumo, "Aluno", String.valueOf(dadosAluno[0]), 30, 30);
    adicionarResumo(cardResumo, "Matricula", String.valueOf(dadosAluno[9]), 300, 30);
    adicionarResumo(cardResumo, "Turma", String.valueOf(dadosAluno[1]), 520, 30);
    adicionarResumo(cardResumo, "Curso", "Ensino Medio", 680, 30);
    adicionarResumo(cardResumo, "Turno", String.valueOf(dadosAluno[10]), 930, 30);

    adicionarIndicador(
        cardResumo,
        "Total de faltas",
        String.valueOf(DadosSistema.totalFaltasAlunoAtual()),
        30,
        105,
        new Color(255, 170, 90));
    adicionarIndicador(
        cardResumo,
        "Faltas justificadas",
        String.valueOf(DadosSistema.totalFaltasJustificadasAlunoAtual()),
        280,
        105,
        new Color(120, 200, 255));
    adicionarIndicador(
        cardResumo,
        "Faltas nao justificadas",
        String.valueOf(
            DadosSistema.totalFaltasAlunoAtual()
                - DadosSistema.totalFaltasJustificadasAlunoAtual()),
        570,
        105,
        new Color(255, 100, 120));
    adicionarIndicador(
        cardResumo,
        "Presencas registradas",
        String.valueOf(DadosSistema.totalPresencasAlunoAtual()),
        900,
        105,
        new Color(120, 255, 170));

    JPanel cardTabela = criarPainelArredondado();
    cardTabela.setLayout(null);
    cardTabela.setBounds(xInicial, 180, 1220, 550);
    painelRolagem1.add(cardTabela);

    JLabel tituloTabela = new JLabel("Registro de Frequência");
    tituloTabela.setForeground(corLabel);
    tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 25));
    tituloTabela.setBounds(30, 22, 400, 35);
    cardTabela.add(tituloTabela);

    JLabel subtituloTabela =
        new JLabel(
            "Quando o professor justificar a falta, ela continuará aparecendo como falta, porém com"
                + " situação justificada.");
    subtituloTabela.setForeground(textos);
    subtituloTabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    subtituloTabela.setBounds(32, 58, 950, 25);
    cardTabela.add(subtituloTabela);

    criarTabela();

    JScrollPane scrollTabela = new JScrollPane(tabelaFaltas);
    scrollTabela.setBounds(30, 100, 1160, 422);
    scrollTabela.setBorder(new LineBorder(corBorda));
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
    scrollTabela.getViewport().setBackground(corCampo);
    cardTabela.add(scrollTabela);
  }

  private JPanel criarTopo() {
    JPanel topo =
        new JPanel(null) {
          private static final long serialVersionUID = 1L;

          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
            super.paintComponent(g);
          }
        };
    topo.setOpaque(false);

    JLabel titulo = new JLabel("Minhas Faltas");
    titulo.setForeground(Color.WHITE);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 550, 45);
    topo.add(titulo);

    JLabel sub =
        new JLabel(
            "Consulte suas presenças, faltas e faltas justificadas registradas pelos professores.");
    sub.setForeground(new Color(245, 225, 255));
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    sub.setBounds(42, 120, 950, 25);
    topo.add(sub);
    return topo;
  }

  private void criarTabela() {
    String[] colunas = {
      "Data", "Disciplina", "Professor", "Aula", "Registro", "Justificativa", "Situação"
    };

    modeloTabela =
        new DefaultTableModel(colunas, 0) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    DadosSistema.preencher(modeloTabela, DadosSistema.frequenciaAlunoAtualLinhas());
    tabelaFaltas = new JTable(modeloTabela);
    tabelaFaltas.setRowHeight(42);
    tabelaFaltas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    tabelaFaltas.setForeground(Color.WHITE);
    tabelaFaltas.setBackground(new Color(25, 8, 80));
    tabelaFaltas.setGridColor(corBorda);
    tabelaFaltas.setSelectionBackground(new Color(120, 40, 220));
    tabelaFaltas.setSelectionForeground(Color.WHITE);
    tabelaFaltas.setShowGrid(true);
    tabelaFaltas.setShowVerticalLines(true);
    tabelaFaltas.setShowHorizontalLines(true);
    tabelaFaltas.setRowSelectionAllowed(true);
    tabelaFaltas.setFillsViewportHeight(true);

    JTableHeader header = tabelaFaltas.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 15));
    header.setForeground(Color.WHITE);
    header.setBackground(new Color(80, 25, 150));
    header.setPreferredSize(new Dimension(header.getWidth(), 42));
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();

    centro.setHorizontalAlignment(JLabel.CENTER);
    centro.setVerticalAlignment(JLabel.CENTER);
    centro.setForeground(Color.WHITE);
    centro.setBackground(new Color(25, 8, 80));

    for (int i = 0; i < tabelaFaltas.getColumnCount(); i++) {
      tabelaFaltas.getColumnModel().getColumn(i).setCellRenderer(centro);
    }
    tabelaFaltas.getColumnModel().getColumn(0).setPreferredWidth(100);
    tabelaFaltas.getColumnModel().getColumn(1).setPreferredWidth(170);
    tabelaFaltas.getColumnModel().getColumn(2).setPreferredWidth(210);
    tabelaFaltas.getColumnModel().getColumn(3).setPreferredWidth(120);
    tabelaFaltas.getColumnModel().getColumn(4).setPreferredWidth(100);
    tabelaFaltas.getColumnModel().getColumn(5).setPreferredWidth(260);
    tabelaFaltas.getColumnModel().getColumn(6).setPreferredWidth(150);
  }

  private void adicionarResumo(JPanel painel, String titulo, String valor, int x, int y) {
    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblTitulo.setBounds(x, y, 200, 22);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(Color.WHITE);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblValor.setBounds(x, y + 24, 260, 28);
    painel.add(lblValor);
  }

  private void adicionarIndicador(
      JPanel painel, String titulo, String valor, int x, int y, Color corValor) {
    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(textos);
    lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblTitulo.setBounds(x, y, 230, 22);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(corValor);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 28));
    lblValor.setBounds(x, y + 25, 160, 35);
    painel.add(lblValor);
  }

  private void estilizarBotao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
    botao.setForeground(Color.WHITE);
    botao.setBackground(corCampo);
    botao.setBorder(new LineBorder(corBorda));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  private JPanel criarPainelArredondado() {
    return new JPanel() {
      private static final long serialVersionUID = 1L;

      {
        setOpaque(false);
      }

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(corCampo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
        g2.setColor(corBorda);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
        g2.dispose();
        super.paintComponent(g);
      }
    };
  }
}
