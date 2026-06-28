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

public class SuporteResponsavel extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color textos = Color.WHITE;

  private JTable tabelaChamados;
  private DefaultTableModel modeloTabela;

  private static final int LARGURA_CARD = 1220;

  public SuporteResponsavel() {
    setTitle("Suporte ao Responsável");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Suporte Responsavel.jpeg"));
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
    btnVoltar.setBounds(35, 35, 150, 40);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 100);

    JPanel conteudo = new JPanel(null);
    conteudo.setBackground(corInterna);
    conteudo.setPreferredSize(new Dimension(larguraConteudo, 2120));

    JScrollPane scroll = new JScrollPane(conteudo);
    scroll.setBounds(30, 210, largura - 60, altura - 250);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    scroll.setBorder(null);
    painel.add(scroll);

    criarResumo(conteudo, larguraConteudo);
    criarNovoChamado(conteudo, larguraConteudo);
    criarTabelaChamados(conteudo, larguraConteudo);
    criarDetalhesChamado(conteudo, larguraConteudo);
    criarFAQ(conteudo, larguraConteudo);
    criarContatos(conteudo, larguraConteudo);
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

    JLabel titulo = new JLabel("Central de Suporte");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 500, 45);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Atendimento aos responsáveis");
    subtitulo.setForeground(new Color(240, 240, 255));
    subtitulo.setBounds(45, 120, 500, 25);
    topo.add(subtitulo);
    return topo;
  }

  private void criarResumo(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Resumo");
    card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

    adicionarIndicador(card, "Abertos", "2", 60, 50, Color.ORANGE);
    adicionarIndicador(card, "Respondidos", "8", 350, 50, Color.GREEN);
    adicionarIndicador(card, "Em Análise", "1", 650, 50, Color.CYAN);
    adicionarIndicador(card, "Total", "11", 950, 50, Color.MAGENTA);
    painel.add(card);
  }

  private void criarNovoChamado(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Abrir Chamado");
    card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 350);

    JLabel lblAssunto = criarLabel("Assunto");
    lblAssunto.setBounds(30, 60, 120, 25);
    card.add(lblAssunto);

    JTextField txtAssunto = new JTextField();
    txtAssunto.setBounds(30, 90, 450, 35);
    estilizarCampo(txtAssunto);
    card.add(txtAssunto);

    JLabel lblCategoria = criarLabel("Categoria");
    lblCategoria.setBounds(520, 60, 120, 25);
    card.add(lblCategoria);

    JComboBox<String> cbCategoria =
        new JComboBox<>(
            new String[] {
              "Financeiro", "Matrícula", "Boletim", "Frequência", "Advertência", "Sistema", "Outros"
            });
    cbCategoria.setBounds(520, 90, 250, 35);
    estilizarCombo(cbCategoria);
    card.add(cbCategoria);

    JLabel lblMensagem = criarLabel("Mensagem");
    lblMensagem.setBounds(30, 145, 150, 25);
    card.add(lblMensagem);

    JTextArea areaMensagem = new JTextArea();
    areaMensagem.setLineWrap(true);
    areaMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    areaMensagem.setWrapStyleWord(true);
    areaMensagem.setBackground(corCampo);
    areaMensagem.setForeground(textos);

    JScrollPane scrollMensagem = new JScrollPane(areaMensagem);
    scrollMensagem.setBounds(30, 175, 900, 120);
    scrollMensagem.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollMensagem);

    JButton btnEnviar = new JButton("Enviar Chamado");
    btnEnviar.setBounds(980, 220, 200, 45);
    estilizarBotao(btnEnviar);
    card.add(btnEnviar);
    painel.add(card);
  }

  private void criarTabelaChamados(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Meus Chamados");
    card.setBounds(centerX(larguraConteudo), 560, LARGURA_CARD, 420);

    String[] colunas = {"Protocolo", "Data", "Categoria", "Assunto", "Status"};

    modeloTabela = new DefaultTableModel(colunas, 0);
    modeloTabela.addRow(
        new Object[] {"2026-001", "10/06/2026", "Boletim", "Erro ao visualizar notas", "Aberto"});
    modeloTabela.addRow(
        new Object[] {"2026-002", "08/06/2026", "Financeiro", "2ª via de boleto", "Respondido"});
    modeloTabela.addRow(
        new Object[] {"2026-003", "05/06/2026", "Frequência", "Falta registrada", "Em Análise"});

    tabelaChamados = new JTable(modeloTabela);
    tabelaChamados.setRowHeight(30);
    tabelaChamados.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    tabelaChamados.setForeground(textos);
    tabelaChamados.setBackground(corInterna);
    tabelaChamados.setGridColor(new Color(90, 50, 170));
    tabelaChamados.setSelectionBackground(new Color(80, 40, 160));
    tabelaChamados.setSelectionForeground(textos);
    tabelaChamados.setShowGrid(true);
    tabelaChamados.setShowHorizontalLines(true);
    tabelaChamados.setShowVerticalLines(true);
    tabelaChamados.setIntercellSpacing(new Dimension(1, 1));
    tabelaChamados.setFillsViewportHeight(false);
    tabelaChamados.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    JTableHeader header = tabelaChamados.getTableHeader();
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

    for (int i = 0; i < tabelaChamados.getColumnCount(); i++) {
      tabelaChamados.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    JScrollPane scroll = new JScrollPane(tabelaChamados);
    scroll.setBounds(25, 60, 1170, 300);
    scroll.getViewport().setBackground(corInterna);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void criarDetalhesChamado(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Detalhes do Chamado");
    card.setBounds(centerX(larguraConteudo), 1010, LARGURA_CARD, 420);

    JLabel lblProtocolo = criarLabel("Protocolo");
    lblProtocolo.setBounds(30, 60, 150, 25);
    card.add(lblProtocolo);

    JTextField txtProtocolo = new JTextField("2026-001");
    txtProtocolo.setEditable(false);
    txtProtocolo.setBounds(30, 90, 250, 35);
    estilizarCampo(txtProtocolo);
    card.add(txtProtocolo);

    JLabel lblStatus = criarLabel("Status");
    lblStatus.setBounds(350, 60, 120, 25);
    card.add(lblStatus);

    JTextField txtStatus = new JTextField("Aberto");
    txtStatus.setEditable(false);
    txtStatus.setBounds(350, 90, 220, 35);
    estilizarCampo(txtStatus);
    card.add(txtStatus);

    JLabel lblResposta = criarLabel("Resposta da Escola");
    lblResposta.setBounds(30, 150, 250, 25);
    card.add(lblResposta);

    JTextArea areaResposta = new JTextArea();
    areaResposta.setEditable(false);
    areaResposta.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    areaResposta.setText(
        "Seu chamado foi recebido e está " + "aguardando análise do setor responsável.");
    areaResposta.setLineWrap(true);
    areaResposta.setWrapStyleWord(true);
    areaResposta.setBackground(corCampo);
    areaResposta.setForeground(textos);

    JScrollPane scrollResposta = new JScrollPane(areaResposta);
    scrollResposta.setBounds(30, 180, 1160, 180);
    scrollResposta.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollResposta);
    painel.add(card);
  }

  private void criarFAQ(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Perguntas Frequentes");
    card.setBounds(centerX(larguraConteudo), 1460, LARGURA_CARD, 320);

    JTextArea areaFAQ = new JTextArea();
    areaFAQ.setEditable(false);
    areaFAQ.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    areaFAQ.setText(
        "• Como emitir a 2ª via do boleto?\n\n"
            + "Acesse o menu Financeiro.\n\n"
            + "• Como visualizar o boletim?\n\n"
            + "Acesse o menu Boletim Escolar.\n\n"
            + "• Como justificar faltas?\n\n"
            + "Entre em contato com a secretaria.\n\n"
            + "• Como alterar meus dados?\n\n"
            + "Solicite atualização junto à escola.");
    areaFAQ.setLineWrap(true);
    areaFAQ.setWrapStyleWord(true);
    areaFAQ.setBackground(corCampo);
    areaFAQ.setForeground(textos);

    JScrollPane scrollFAQ = new JScrollPane(areaFAQ);
    scrollFAQ.setBounds(25, 60, 1170, 230);
    scrollFAQ.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollFAQ);
    painel.add(card);
  }

  private void criarContatos(JPanel painel, int larguraConteudo) {
    JPanel card = criarCard("Contatos da Escola");
    card.setBounds(centerX(larguraConteudo), 1800, LARGURA_CARD, 300);

    JTextArea areaContato = new JTextArea();
    areaContato.setEditable(false);
    areaContato.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    areaContato.setText(
        "Telefone: (47) 3333-3333\n\n"
            + "WhatsApp: (47) 99999-9999\n\n"
            + "E-mail: secretaria@escola.com.br\n\n"
            + "Horário de Atendimento:\n"
            + "Segunda à Sexta\n"
            + "07:00 às 18:00");
    areaContato.setLineWrap(true);
    areaContato.setWrapStyleWord(true);
    areaContato.setBackground(corCampo);
    areaContato.setForeground(textos);

    JScrollPane scrollContato = new JScrollPane(areaContato);
    scrollContato.setBounds(25, 60, 1170, 200);
    scrollContato.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollContato);
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
