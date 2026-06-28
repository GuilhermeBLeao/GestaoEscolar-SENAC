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
import javax.swing.JCheckBox;
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

public class AdvertenciasResponsavel extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color textos = Color.WHITE;

  private JTable tabelaAdvertencias;
  private DefaultTableModel modeloTabela;

  // Largura fixa dos cards para centralização
  private static final int LARGURA_CARD = 1220;

  public AdvertenciasResponsavel() {
    setTitle("Advertências");
    setIconImage(
        Toolkit.getDefaultToolkit()
            .getImage("resources/Images/Icons/Advertencias Responsavel.jpeg"));
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);

    Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int largura = area.width - 60;
    int altura = area.height - 60;

    JPanel interno = new JPanel(null);
    interno.setBounds(20, 20, largura, altura);
    interno.setBackground(corInterna);
    externo.add(interno);

    criarTela(interno, largura, altura);
  }

  private void criarTela(JPanel painel, int largura, int altura) {
    // Topo: mantido sem alteração de posicionamento
    JPanel topo = criarTopo();
    topo.setBounds(30, 25, largura - 60, 160);
    painel.add(topo);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(35, 35, 150, 40);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 120);

    JPanel conteudo = new JPanel(null);
    conteudo.setBackground(corInterna);
    conteudo.setPreferredSize(new Dimension(larguraConteudo, 1920));

    JScrollPane scroll = new JScrollPane(conteudo);
    scroll.setBounds(30, 210, largura - 60, altura - 250);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    painel.add(scroll);

    criarResumo(conteudo, larguraConteudo);
    criarDadosAluno(conteudo, larguraConteudo);
    criarTabelaAdvertencias(conteudo, larguraConteudo);
    criarDetalhesAdvertencia(conteudo, larguraConteudo);
    criarConfirmacaoCiente(conteudo, larguraConteudo);
    criarHistoricoCiencia(conteudo, larguraConteudo);
  }

  /** Calcula o X de modo que o card fique centralizado no painel de conteúdo. */
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

    JLabel titulo = new JLabel("Advertências");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 400, 45);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Visualização e confirmação de ciência");
    subtitulo.setForeground(new Color(240, 240, 255));
    subtitulo.setBounds(45, 120, 500, 25);
    topo.add(subtitulo);

    return topo;
  }

  private void criarResumo(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Resumo");
    card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

    adicionarIndicador(
        card,
        "Pendentes",
        String.valueOf(DadosSistema.totalAdvertenciasAlunoAtual()),
        60,
        50,
        Color.RED);
    adicionarIndicador(card, "Cientes", "0", 350, 50, Color.GREEN);
    adicionarIndicador(
        card,
        "Total",
        String.valueOf(DadosSistema.totalAdvertenciasAlunoAtual()),
        650,
        50,
        Color.CYAN);
    adicionarIndicador(
        card,
        "Ano Atual",
        String.valueOf(DadosSistema.totalAdvertenciasAlunoAtual()),
        950,
        50,
        Color.ORANGE);

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
    adicionarCampoAluno(card, "Situacao", String.valueOf(dadosAluno[2]), 430, 140);

    painel.add(card);
  }

  private void criarTabelaAdvertencias(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Advertências Recebidas");
    card.setBounds(centerX(larguraConteudo), 430, LARGURA_CARD, 400);

    String[] colunas = {"Data", "Tipo", "Professor", "Gravidade", "Status"};
    modeloTabela = new DefaultTableModel(colunas, 0);
    DadosSistema.preencher(modeloTabela, DadosSistema.advertenciasResponsavelAlunoLinhas());

    tabelaAdvertencias = new JTable(modeloTabela);
    tabelaAdvertencias.setRowHeight(30);
    tabelaAdvertencias.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    tabelaAdvertencias.setForeground(textos);
    tabelaAdvertencias.setBackground(corInterna);
    tabelaAdvertencias.setGridColor(new Color(90, 50, 170));
    tabelaAdvertencias.setSelectionBackground(new Color(80, 40, 160));
    tabelaAdvertencias.setSelectionForeground(textos);
    tabelaAdvertencias.setShowGrid(true);
    tabelaAdvertencias.setShowHorizontalLines(true);
    tabelaAdvertencias.setShowVerticalLines(true);
    tabelaAdvertencias.setIntercellSpacing(new Dimension(1, 1));
    tabelaAdvertencias.setFillsViewportHeight(false);
    tabelaAdvertencias.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    JTableHeader header = tabelaAdvertencias.getTableHeader();
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

    for (int i = 0; i < tabelaAdvertencias.getColumnCount(); i++) {
      tabelaAdvertencias.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    JScrollPane scroll = new JScrollPane(tabelaAdvertencias);
    scroll.setBounds(25, 60, 1170, 300);
    scroll.getViewport().setBackground(corInterna);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void criarDetalhesAdvertencia(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Detalhes da Advertência");
    card.setBounds(centerX(larguraConteudo), 850, LARGURA_CARD, 420);

    JLabel lblData = criarLabel("Data");
    lblData.setBounds(30, 60, 120, 25);
    card.add(lblData);

    JTextField txtData = new JTextField("10/06/2026");
    txtData.setEditable(false);
    txtData.setBounds(30, 90, 220, 35);
    estilizarCampo(txtData);
    card.add(txtData);

    JLabel lblProfessor = criarLabel("Professor");
    lblProfessor.setBounds(300, 60, 120, 25);
    card.add(lblProfessor);

    JTextField txtProfessor = new JTextField("Carlos Silva");
    txtProfessor.setEditable(false);
    txtProfessor.setBounds(300, 90, 300, 35);
    estilizarCampo(txtProfessor);
    card.add(txtProfessor);

    JLabel lblGravidade = criarLabel("Gravidade");
    lblGravidade.setBounds(650, 60, 120, 25);
    card.add(lblGravidade);

    JTextField txtGravidade = new JTextField("Média");
    txtGravidade.setEditable(false);
    txtGravidade.setBounds(650, 90, 220, 35);
    estilizarCampo(txtGravidade);
    card.add(txtGravidade);

    JLabel lblMotivo = criarLabel("Motivo");
    lblMotivo.setBounds(30, 150, 120, 25);
    card.add(lblMotivo);

    JTextArea areaMotivo = new JTextArea();
    areaMotivo.setEditable(false);
    areaMotivo.setFont(new Font("Segoe UI", Font.BOLD, 20));
    areaMotivo.setText(
        "O aluno apresentou comportamento "
            + "inadequado durante a aula, desrespeitando "
            + "as orientações do professor e interrompendo "
            + "o andamento da atividade.");
    areaMotivo.setLineWrap(true);
    areaMotivo.setWrapStyleWord(true);
    areaMotivo.setBackground(corCampo);
    areaMotivo.setForeground(textos);

    JScrollPane scroll = new JScrollPane(areaMotivo);
    scroll.setBounds(30, 180, 1160, 180);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);

    painel.add(card);
  }

  private void criarConfirmacaoCiente(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Confirmação de Ciência");
    card.setBounds(centerX(larguraConteudo), 1290, LARGURA_CARD, 250);

    JCheckBox chkCiente = new JCheckBox("Declaro que estou ciente desta advertência.");
    chkCiente.setBounds(30, 70, 500, 30);
    chkCiente.setBackground(corCampo);
    chkCiente.setForeground(textos);
    card.add(chkCiente);

    JButton btnCiente = new JButton("Dar Ciência");
    btnCiente.setBounds(30, 130, 220, 40);
    estilizarBotao(btnCiente);
    card.add(btnCiente);

    JLabel lblStatus = new JLabel("Status Atual: Pendente");
    lblStatus.setForeground(Color.ORANGE);
    lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblStatus.setBounds(320, 135, 300, 30);
    card.add(lblStatus);

    painel.add(card);
  }

  private void criarHistoricoCiencia(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Histórico de Ciência");
    card.setBounds(centerX(larguraConteudo), 1560, LARGURA_CARD, 320);

    JTextArea areaHistorico = new JTextArea();
    areaHistorico.setEditable(false);
    areaHistorico.setText(
        "02/06/2026 - Ciência confirmada pelo responsável.\n\n"
            + "15/05/2026 - Ciência confirmada pelo responsável.\n\n"
            + "22/03/2026 - Ciência confirmada pelo responsável.");
    areaHistorico.setLineWrap(true);
    areaHistorico.setWrapStyleWord(true);
    areaHistorico.setBackground(corCampo);
    areaHistorico.setForeground(textos);
    areaHistorico.setFont(new Font("Segoe UI", Font.BOLD, 22));

    JScrollPane scroll = new JScrollPane(areaHistorico);
    scroll.setBounds(25, 60, 1170, 230);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);

    painel.add(card);
  }

  // -------------------------------------------------------------------------
  // Utilitários
  // -------------------------------------------------------------------------

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
    lblTitulo.setBounds(x, y, 180, 20);
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
