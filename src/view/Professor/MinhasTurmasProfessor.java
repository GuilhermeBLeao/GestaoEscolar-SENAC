package view.Professor;

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

public class MinhasTurmasProfessor extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaTurmas;
  private DefaultTableModel modeloTabela;

  public MinhasTurmasProfessor() {
    setTitle("Minhas Turmas - Professor");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Minhas Turmas.png"));
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

    painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 820));

    painelRolagem.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
    interno.add(painelRolagem);

    JPanel cardResumo = criarPainelArredondado();
    cardResumo.setLayout(null);
    cardResumo.setBounds(xInicial, 0, 1220, 150);
    painelRolagem.add(cardResumo);

    adicionarResumo(cardResumo, "Professor", DadosSistema.nomeUsuarioLogado(), 30, 30);
    adicionarResumo(
        cardResumo, "Matricula", String.valueOf(DadosSistema.professorIdAtual()), 300, 30);
    adicionarResumo(
        cardResumo, "Disciplina", DadosSistema.primeiraDisciplinaProfessorAtual(), 540, 30);
    adicionarResumo(cardResumo, "Turno", "Conforme turmas", 760, 30);
    adicionarResumo(cardResumo, "Ano letivo", "2026", 1010, 30);

    JLabel aviso =
        new JLabel(
            "Turmas vinculadas ao professor para lançamento de notas, chamada e acompanhamento.");
    aviso.setForeground(new Color(120, 255, 170));
    aviso.setFont(new Font("Segoe UI", Font.BOLD, 16));
    aviso.setBounds(30, 98, 900, 25);
    cardResumo.add(aviso);

    JPanel cardTotal =
        criarCardIndicador(
            "Total de Turmas",
            String.valueOf(DadosSistema.totalTurmasProfessorAtual()),
            new Color(120, 200, 255));
    cardTotal.setBounds(xInicial, 170, 265, 130);
    painelRolagem.add(cardTotal);

    JPanel cardAlunos =
        criarCardIndicador(
            "Total de Alunos",
            String.valueOf(DadosSistema.totalAlunosProfessorAtual()),
            new Color(120, 255, 170));
    cardAlunos.setBounds(xInicial + 312, 170, 285, 130);
    painelRolagem.add(cardAlunos);

    JPanel cardAulas = criarCardIndicador("Aulas Semanais", "-", new Color(255, 170, 90));
    cardAulas.setBounds(xInicial + 624, 170, 285, 130);
    painelRolagem.add(cardAulas);

    JPanel cardPendencias = criarCardIndicador("Pendencias", "0", new Color(255, 100, 120));
    cardPendencias.setBounds(xInicial + 936, 170, 285, 130);
    painelRolagem.add(cardPendencias);

    JPanel cardTabela = criarPainelArredondado();
    cardTabela.setLayout(null);
    cardTabela.setBounds(xInicial, 320, 1220, 420);
    painelRolagem.add(cardTabela);

    JLabel tituloTabela = new JLabel("Turmas Cadastradas");
    tituloTabela.setForeground(corLabel);
    tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 25));
    tituloTabela.setBounds(30, 22, 400, 35);
    cardTabela.add(tituloTabela);

    JLabel subtituloTabela =
        new JLabel(
            "Consulte as turmas vinculadas, quantidade de alunos, horários e situação da turma.");
    subtituloTabela.setForeground(textos);
    subtituloTabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    subtituloTabela.setBounds(32, 58, 900, 25);
    cardTabela.add(subtituloTabela);

    criarTabela();

    JScrollPane scrollTabela = new JScrollPane(tabelaTurmas);
    scrollTabela.setBounds(30, 100, 1160, 285);
    scrollTabela.setBorder(new LineBorder(corBorda));
    scrollTabela.getViewport().setBackground(corCampo);
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
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

    JLabel titulo = new JLabel("Minhas Turmas");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 600, 45);
    topo.add(titulo);

    JLabel sub = new JLabel("Visualize todas as turmas vinculadas ao seu cadastro de professor.");
    sub.setForeground(new Color(245, 225, 255));
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    sub.setBounds(42, 120, 850, 25);
    topo.add(sub);

    return topo;
  }

  private void criarTabela() {
    String[] colunas = {
      "Turma", "Curso", "Disciplina", "Turno", "Alunos", "Horário", "Sala", "Situação"
    };

    modeloTabela =
        new DefaultTableModel(colunas, 0) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    DadosSistema.preencher(modeloTabela, DadosSistema.turmasProfessorAtualLinhas());

    tabelaTurmas = new JTable(modeloTabela);
    tabelaTurmas.setRowHeight(42);
    tabelaTurmas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    tabelaTurmas.setForeground(textos);
    tabelaTurmas.setBackground(corCampo);
    tabelaTurmas.setGridColor(corBorda);
    tabelaTurmas.setSelectionBackground(new Color(120, 40, 220));
    tabelaTurmas.setSelectionForeground(textos);
    tabelaTurmas.setShowGrid(true);
    tabelaTurmas.setShowVerticalLines(true);
    tabelaTurmas.setShowHorizontalLines(true);
    tabelaTurmas.setRowSelectionAllowed(true);
    tabelaTurmas.setFillsViewportHeight(true);

    JTableHeader header = tabelaTurmas.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 15));
    header.setForeground(textos);
    header.setBackground(new Color(80, 25, 150));
    header.setPreferredSize(new Dimension(header.getWidth(), 73));
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(JLabel.CENTER);
    centro.setVerticalAlignment(JLabel.CENTER);
    centro.setForeground(textos);
    centro.setBackground(corCampo);

    for (int i = 0; i < tabelaTurmas.getColumnCount(); i++) {
      tabelaTurmas.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    tabelaTurmas.getColumnModel().getColumn(0).setPreferredWidth(80);
    tabelaTurmas.getColumnModel().getColumn(1).setPreferredWidth(160);
    tabelaTurmas.getColumnModel().getColumn(2).setPreferredWidth(160);
    tabelaTurmas.getColumnModel().getColumn(3).setPreferredWidth(120);
    tabelaTurmas.getColumnModel().getColumn(4).setPreferredWidth(90);
    tabelaTurmas.getColumnModel().getColumn(5).setPreferredWidth(190);
    tabelaTurmas.getColumnModel().getColumn(6).setPreferredWidth(120);
    tabelaTurmas.getColumnModel().getColumn(7).setPreferredWidth(100);
  }

  private JPanel criarCardIndicador(String titulo, String valor, Color corValor) {
    JPanel card = criarPainelArredondado();
    card.setLayout(null);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(textos);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblTitulo.setBounds(25, 25, 230, 25);
    card.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(corValor);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 40));
    lblValor.setBounds(25, 60, 180, 50);
    card.add(lblValor);

    return card;
  }

  private void adicionarResumo(JPanel painel, String titulo, String valor, int x, int y) {
    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblTitulo.setBounds(x, y, 200, 22);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(textos);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblValor.setBounds(x, y + 24, 260, 28);
    painel.add(lblValor);
  }

  private void estilizarBotao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
    botao.setForeground(textos);
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
