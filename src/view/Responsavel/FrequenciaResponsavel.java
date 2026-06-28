package view.Responsavel;

import java.awt.Color;
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import util.DadosSistema;

public class FrequenciaResponsavel extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color textos = Color.WHITE;

  private JTable tabelaFrequencia;
  private DefaultTableModel modeloTabela;

  private static final int LARGURA_CARD = 1220;

  public FrequenciaResponsavel() {
    setTitle("Frequência Escolar");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Frequencia.png"));
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setResizable(false);

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);

    Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int largura = area.width - 60;
    int altura = area.height - 60;

    JPanel interno = new JPanel(null);
    interno.setBackground(corInterna);
    interno.setBounds(20, 20, largura, altura);
    externo.add(interno);

    criarTela(interno, largura, altura);
  }

  private void criarTela(JPanel painel, int largura, int altura) {
    JPanel topo = criarTopo();
    topo.setBounds(30, 25, largura - 60, 160);
    painel.add(topo);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(35, 30, 150, 40);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 100);

    JPanel conteudo = new JPanel(null);
    conteudo.setBackground(corInterna);
    // Último card: y=1310, altura=320, margem=40 → total=1670
    conteudo.setPreferredSize(new Dimension(larguraConteudo, 1670));

    JScrollPane scroll = new JScrollPane(conteudo);
    scroll.setBounds(30, 210, largura - 60, altura - 250);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    painel.add(scroll);

    criarResumo(conteudo, larguraConteudo);
    criarDadosAluno(conteudo, larguraConteudo);
    criarFiltros(conteudo, larguraConteudo);
    criarTabelaFrequencia(conteudo, larguraConteudo);
    criarEstatisticas(conteudo, larguraConteudo);
    criarObservacoes(conteudo, larguraConteudo);
  }

  private int centerX(int larguraConteudo) {
    return (larguraConteudo - LARGURA_CARD) / 2;
  }

  private JPanel criarTopo() {
    JPanel topo =
        new JPanel(null) {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
          }
        };
    topo.setOpaque(false);

    JLabel titulo = new JLabel("Frequência Escolar");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 75, 500, 45);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Visualização da frequência do aluno");
    subtitulo.setFont(new Font("Dialog", Font.BOLD, 20));
    subtitulo.setForeground(new Color(240, 240, 255));
    subtitulo.setBounds(45, 110, 500, 50);
    topo.add(subtitulo);

    return topo;
  }

  private void criarResumo(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Resumo");
    card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

    adicionarIndicador(
        card,
        "Presencas",
        String.valueOf(DadosSistema.totalPresencasAlunoAtual()),
        60,
        50,
        Color.GREEN);
    adicionarIndicador(
        card, "Faltas", String.valueOf(DadosSistema.totalFaltasAlunoAtual()), 350, 50, Color.RED);
    adicionarIndicador(
        card,
        "Justificadas",
        String.valueOf(DadosSistema.totalFaltasJustificadasAlunoAtual()),
        650,
        50,
        Color.ORANGE);
    adicionarIndicador(
        card, "Frequencia", DadosSistema.frequenciaAlunoAtual(), 950, 50, Color.CYAN);
    painel.add(card);
  }

  private void criarDadosAluno(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Dados do Aluno");
    card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 220);

    Object[] dadosAluno = DadosSistema.dadosAlunoAtualLinha();
    adicionarCampoAluno(card, "Nome", String.valueOf(dadosAluno[0]), 30, 60);
    adicionarCampoAluno(card, "Matricula", String.valueOf(dadosAluno[9]), 430, 60);
    adicionarCampoAluno(card, "Turma", String.valueOf(dadosAluno[1]), 830, 60);
    adicionarCampoAluno(card, "Curso", "Ensino Medio", 30, 140);
    adicionarCampoAluno(card, "Turno", String.valueOf(dadosAluno[10]), 430, 140);
    adicionarCampoAluno(card, "Situacao", String.valueOf(dadosAluno[2]), 830, 140);
    painel.add(card);
  }

  private void criarFiltros(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Filtros");
    card.setBounds(centerX(larguraConteudo), 430, LARGURA_CARD, 180);

    JLabel lblPeriodo = criarLabel("Período");
    lblPeriodo.setBounds(30, 50, 120, 25);
    card.add(lblPeriodo);

    JComboBox<String> cbPeriodo =
        new JComboBox<>(
            new String[] {
              "Ano Letivo", "1º Bimestre", "2º Bimestre", "3º Bimestre", "4º Bimestre"
            });
    cbPeriodo.setBounds(30, 80, 220, 35);
    estilizarCombo(cbPeriodo);
    card.add(cbPeriodo);

    JLabel lblMes = criarLabel("Mês");
    lblMes.setBounds(300, 50, 120, 25);
    card.add(lblMes);

    JComboBox<String> cbMes =
        new JComboBox<>(
            new String[] {
              "Todos",
              "Janeiro",
              "Fevereiro",
              "Março",
              "Abril",
              "Maio",
              "Junho",
              "Julho",
              "Agosto",
              "Setembro",
              "Outubro",
              "Novembro",
              "Dezembro"
            });
    cbMes.setBounds(300, 80, 220, 35);
    estilizarCombo(cbMes);
    card.add(cbMes);

    JButton btnFiltrar = new JButton("Filtrar");
    btnFiltrar.setBounds(620, 78, 180, 40);
    estilizarBotao(btnFiltrar);
    card.add(btnFiltrar);
    painel.add(card);
  }

  private void criarTabelaFrequencia(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Registro de Frequência");
    card.setBounds(centerX(larguraConteudo), 630, LARGURA_CARD, 420);

    String[] colunas = {"Data", "Disciplina", "Professor", "Status", "Observação"};

    modeloTabela = new DefaultTableModel(colunas, 0);
    DadosSistema.preencher(modeloTabela, DadosSistema.frequenciaResponsavelAlunoLinhas());

    tabelaFrequencia = new JTable(modeloTabela);
    tabelaFrequencia.setRowHeight(30);
    tabelaFrequencia.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    tabelaFrequencia.setForeground(textos);
    tabelaFrequencia.setBackground(corInterna);
    tabelaFrequencia.setGridColor(new Color(90, 50, 170));
    tabelaFrequencia.setSelectionBackground(new Color(80, 40, 160));
    tabelaFrequencia.setSelectionForeground(textos);
    tabelaFrequencia.setShowGrid(true);
    tabelaFrequencia.setShowHorizontalLines(true);
    tabelaFrequencia.setShowVerticalLines(true);
    tabelaFrequencia.setIntercellSpacing(new Dimension(1, 1));
    tabelaFrequencia.setFillsViewportHeight(false);
    tabelaFrequencia.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    JTableHeader header = tabelaFrequencia.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 16));
    header.setBackground(corCampo);
    header.setForeground(textos);
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setPreferredSize(new Dimension(header.getWidth(), 34));

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);
    centro.setVerticalAlignment(SwingConstants.CENTER);
    centro.setBackground(corCampo);
    centro.setForeground(textos);
    centro.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < tabelaFrequencia.getColumnCount(); i++) {
      tabelaFrequencia.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    JScrollPane scroll = new JScrollPane(tabelaFrequencia);
    scroll.setBounds(25, 60, 1170, 300);
    scroll.getViewport().setBackground(corInterna);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void criarEstatisticas(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Estatísticas");
    card.setBounds(centerX(larguraConteudo), 1070, LARGURA_CARD, 220);

    adicionarIndicador(
        card,
        "Total de Aulas",
        String.valueOf(DadosSistema.totalRegistrosFrequenciaAlunoAtual()),
        60,
        60,
        Color.CYAN);
    adicionarIndicador(
        card,
        "Presencas",
        String.valueOf(DadosSistema.totalPresencasAlunoAtual()),
        350,
        60,
        Color.GREEN);
    adicionarIndicador(
        card, "Faltas", String.valueOf(DadosSistema.totalFaltasAlunoAtual()), 650, 60, Color.RED);
    adicionarIndicador(
        card, "Percentual", DadosSistema.frequenciaAlunoAtual(), 950, 60, Color.ORANGE);
    painel.add(card);
  }

  private void criarObservacoes(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Observações da Escola");
    card.setBounds(centerX(larguraConteudo), 1310, LARGURA_CARD, 320);

    JTextArea area = new JTextArea();
    area.setText("Nenhuma observação registrada.");
    area.setEditable(false);
    area.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setBackground(corCampo);
    area.setForeground(textos);

    JScrollPane scroll = new JScrollPane(area);
    scroll.setBounds(25, 60, 1170, 230);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void adicionarCampoAluno(JPanel painel, String titulo, String valor, int x, int y) {
    JLabel lbl = criarLabel(titulo);
    lbl.setBounds(x, y, 200, 20);
    painel.add(lbl);

    JTextField campo = new JTextField(valor);
    campo.setEditable(false);
    campo.setBounds(x, y + 25, 320, 35);
    estilizarCampo(campo);
    painel.add(campo);
  }

  private JPanel criarCard(String titulo) {
    JPanel painel = criarPainelArredondado();
    painel.setLayout(null);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lblTitulo.setBounds(30, 15, 500, 30);
    painel.add(lblTitulo);
    return painel;
  }

  private JLabel criarLabel(String texto) {
    JLabel lbl = new JLabel(texto);
    lbl.setForeground(corLabel);
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
    return lbl;
  }

  private void estilizarCampo(JTextField campo) {
    campo.setBackground(corCampo);
    campo.setForeground(textos);
    campo.setCaretColor(textos);
    campo.setBorder(new LineBorder(corBorda));
  }

  private void estilizarCombo(JComboBox<String> combo) {
    combo.setBackground(corCampo);
    combo.setForeground(textos);
    combo.setBorder(new LineBorder(corBorda));
  }

  private void estilizarBotao(JButton botao) {
    botao.setBackground(corCampo);
    botao.setForeground(textos);
    botao.setFocusPainted(false);
    botao.setBorder(new LineBorder(corBorda));
  }

  private void adicionarIndicador(
      JPanel painel, String titulo, String valor, int x, int y, Color cor) {
    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(textos);
    lblTitulo.setBounds(x, y, 200, 20);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(cor);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
    lblValor.setBounds(x, y + 20, 200, 40);
    painel.add(lblValor);
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
      }
    };
  }
}
