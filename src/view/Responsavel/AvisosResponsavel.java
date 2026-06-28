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

public class AvisosResponsavel extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color textos = Color.WHITE;

  private JTable tabelaAvisos;
  private DefaultTableModel modeloTabela;

  private static final int LARGURA_CARD = 1220;

  public AvisosResponsavel() {
    setTitle("Avisos");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Avisos Responsavel.jpeg"));
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
    btnVoltar.setBounds(35, 35, 150, 40);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 100);

    JPanel conteudo = new JPanel(null);
    conteudo.setBackground(corInterna);
    conteudo.setPreferredSize(new Dimension(larguraConteudo, 1870));

    JScrollPane scroll = new JScrollPane(conteudo);
    scroll.setBounds(30, 210, largura - 60, altura - 250);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    painel.add(scroll);

    criarResumo(conteudo, larguraConteudo);
    criarFiltros(conteudo, larguraConteudo);
    criarTabelaAvisos(conteudo, larguraConteudo);
    criarDetalhesAviso(conteudo, larguraConteudo);
    criarAvisosImportantes(conteudo, larguraConteudo);
    criarComunicadosGerais(conteudo, larguraConteudo);
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

    JLabel titulo = new JLabel("Avisos");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 400, 45);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Avisos enviados pela escola");
    subtitulo.setForeground(new Color(240, 240, 255));
    subtitulo.setBounds(45, 120, 400, 25);
    topo.add(subtitulo);

    return topo;
  }

  private void criarResumo(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Resumo");
    card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

    adicionarIndicador(card, "Não Lidos", "4", 60, 50, Color.ORANGE);
    adicionarIndicador(card, "Lidos", "28", 350, 50, Color.GREEN);
    adicionarIndicador(card, "Importantes", "3", 650, 50, Color.RED);
    adicionarIndicador(card, "Total", "32", 950, 50, Color.CYAN);
    painel.add(card);
  }

  private void criarFiltros(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Filtros");
    card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 180);

    JLabel lblCategoria = criarLabel("Categoria");
    lblCategoria.setBounds(30, 50, 120, 25);
    card.add(lblCategoria);

    JComboBox<String> cbCategoria =
        new JComboBox<>(
            new String[] {
              "Todas", "Pedagógico", "Financeiro", "Eventos", "Disciplina", "Secretaria"
            });
    cbCategoria.setBounds(30, 80, 250, 35);
    estilizarCombo(cbCategoria);
    card.add(cbCategoria);

    JLabel lblStatus = criarLabel("Status");
    lblStatus.setBounds(330, 50, 120, 25);
    card.add(lblStatus);

    JComboBox<String> cbStatus = new JComboBox<>(new String[] {"Todos", "Lido", "Não Lido"});
    cbStatus.setBounds(330, 80, 220, 35);
    estilizarCombo(cbStatus);
    card.add(cbStatus);

    JButton btnPesquisar = new JButton("Pesquisar");
    btnPesquisar.setBounds(650, 78, 180, 40);
    estilizarBotao(btnPesquisar);
    card.add(btnPesquisar);
    painel.add(card);
  }

  private void criarTabelaAvisos(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Lista de Avisos");
    card.setBounds(centerX(larguraConteudo), 390, LARGURA_CARD, 400);

    String[] colunas = {"Data", "Categoria", "Título", "Prioridade", "Status"};

    modeloTabela = new DefaultTableModel(colunas, 0);
    modeloTabela.addRow(
        new Object[] {"10/06/2026", "Pedagógico", "Reunião de Pais", "Alta", "Não Lido"});
    modeloTabela.addRow(new Object[] {"08/06/2026", "Eventos", "Festa Junina", "Média", "Lido"});
    modeloTabela.addRow(
        new Object[] {"05/06/2026", "Financeiro", "Vencimento da Mensalidade", "Alta", "Lido"});

    tabelaAvisos = new JTable(modeloTabela);
    tabelaAvisos.setRowHeight(30);
    tabelaAvisos.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    tabelaAvisos.setForeground(textos);
    tabelaAvisos.setBackground(corInterna);
    tabelaAvisos.setGridColor(new Color(90, 50, 170));
    tabelaAvisos.setSelectionBackground(new Color(80, 40, 160));
    tabelaAvisos.setSelectionForeground(textos);
    tabelaAvisos.setShowGrid(true);
    tabelaAvisos.setShowHorizontalLines(true);
    tabelaAvisos.setShowVerticalLines(true);
    tabelaAvisos.setIntercellSpacing(new Dimension(1, 1));
    tabelaAvisos.setFillsViewportHeight(false);
    tabelaAvisos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    JTableHeader header = tabelaAvisos.getTableHeader();
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

    for (int i = 0; i < tabelaAvisos.getColumnCount(); i++) {
      tabelaAvisos.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    JScrollPane scroll = new JScrollPane(tabelaAvisos);
    scroll.setBounds(25, 60, 1170, 300);
    scroll.getViewport().setBackground(corInterna);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void criarDetalhesAviso(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Detalhes do Aviso");
    card.setBounds(centerX(larguraConteudo), 810, LARGURA_CARD, 380);

    JLabel lblTitulo = criarLabel("Título");
    lblTitulo.setBounds(30, 60, 120, 25);
    card.add(lblTitulo);

    JTextField txtTitulo = new JTextField("Reunião de Pais e Responsáveis");
    txtTitulo.setEditable(false);
    txtTitulo.setBounds(30, 90, 600, 35);
    estilizarCampo(txtTitulo);
    card.add(txtTitulo);

    JLabel lblData = criarLabel("Data");
    lblData.setBounds(700, 60, 120, 25);
    card.add(lblData);

    JTextField txtData = new JTextField("10/06/2026");
    txtData.setEditable(false);
    txtData.setBounds(700, 90, 220, 35);
    estilizarCampo(txtData);
    card.add(txtData);

    JTextArea area = new JTextArea();
    area.setEditable(false);
    area.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    area.setText(
        "Convocamos todos os responsáveis para "
            + "participarem da reunião escolar que ocorrerá "
            + "na próxima semana no auditório da escola.");
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setBackground(corCampo);
    area.setForeground(textos);

    JScrollPane scroll = new JScrollPane(area);
    scroll.setBounds(30, 150, 1160, 190);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void criarAvisosImportantes(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Avisos Importantes");
    card.setBounds(centerX(larguraConteudo), 1210, LARGURA_CARD, 250);

    JTextArea areaImportantes = new JTextArea();
    areaImportantes.setEditable(false);
    areaImportantes.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    areaImportantes.setText(
        "• Atualização cadastral obrigatória.\n\n"
            + "• Reunião de pais em 15/06/2026.\n\n"
            + "• Entrega dos boletins disponível no portal.");
    areaImportantes.setLineWrap(true);
    areaImportantes.setWrapStyleWord(true);
    areaImportantes.setBackground(corCampo);
    areaImportantes.setForeground(textos);

    JScrollPane scrollImportantes = new JScrollPane(areaImportantes);
    scrollImportantes.setBounds(25, 60, 1170, 160);
    scrollImportantes.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollImportantes);
    painel.add(card);
  }

  private void criarComunicadosGerais(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Comunicados Gerais");
    card.setBounds(centerX(larguraConteudo), 1480, LARGURA_CARD, 350);

    JTextArea area = new JTextArea();
    area.setEditable(false);
    area.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    area.setText(
        "A escola informa que todas as comunicações "
            + "oficiais serão disponibilizadas nesta área. "
            + "Mantenha seus dados atualizados para receber "
            + "avisos importantes e notificações relacionadas "
            + "ao desempenho escolar, eventos e atividades.");
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setBackground(corCampo);
    area.setForeground(textos);

    JScrollPane scroll = new JScrollPane(area);
    scroll.setBounds(25, 60, 1170, 250);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
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
