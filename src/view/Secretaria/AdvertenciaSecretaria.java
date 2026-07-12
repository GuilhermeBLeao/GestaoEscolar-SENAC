package view.Secretaria;

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
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
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
import javax.swing.text.MaskFormatter;

public class AdvertenciaSecretaria extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaAdvertencias;
  private DefaultTableModel modeloTabela;

  private JTextField txtNomeAluno;
  private JFormattedTextField txtMatricula;

  private JComboBox<String> comboTurma;
  private JComboBox<String> comboStatus;

  private static final int LARGURA_CARD = 1220;

  public AdvertenciaSecretaria() {
    setTitle("Advertências - Secretaria");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Advertencia Secretaria.jpeg"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setResizable(false);

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

    criarConteudo(interno, largura, altura);
  }

  private void criarConteudo(JPanel interno, int largura, int altura) {
    JPanel topo = criarTopo();
    topo.setBounds(30, 25, largura - 60, 160);
    interno.add(topo);

    int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 80);

    JPanel painelRolagem = new JPanel(null);
    painelRolagem.setBackground(corInterna);
    // Último card: y=2120, altura=150, margem=40 → total=2310
    painelRolagem.setPreferredSize(new Dimension(larguraConteudo, 2310));

    JScrollPane scroll = new JScrollPane(painelRolagem);
    scroll.setBounds(30, 210, largura - 60, altura - 240);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    scroll.setBorder(null);
    interno.add(scroll);

    criarResumo(painelRolagem, larguraConteudo);
    criarPesquisa(painelRolagem, larguraConteudo);
    criarHistorico(painelRolagem, larguraConteudo);
    criarDadosAluno(painelRolagem, larguraConteudo);
    criarCadastroAdvertencia(painelRolagem, larguraConteudo);
    criarDescricao(painelRolagem, larguraConteudo);
    criarProvidencias(painelRolagem, larguraConteudo);
    criarAssinaturas(painelRolagem, larguraConteudo);
    criarAcoes(painelRolagem, larguraConteudo);
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

    JLabel titulo = new JLabel("Advertências");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 500, 45);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Registro disciplinar dos alunos");
    subtitulo.setForeground(new Color(245, 225, 255));
    subtitulo.setBounds(45, 120, 500, 25);
    topo.add(subtitulo);

    return topo;
  }

  private void criarResumo(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Resumo Geral");
    card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

    adicionarIndicador(card, "Total", "96", 60, 60, Color.CYAN);
    adicionarIndicador(card, "Este Mês", "18", 340, 60, Color.GREEN);
    adicionarIndicador(card, "Pendentes", "11", 650, 60, Color.ORANGE);
    adicionarIndicador(card, "Canceladas", "4", 930, 60, Color.RED);
    painel.add(card);
  }

  private void criarPesquisa(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Pesquisa");
    card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 180);

    JLabel lblNome = criarLabelCampo("Aluno");
    lblNome.setBounds(30, 50, 120, 25);
    card.add(lblNome);

    txtNomeAluno = new JTextField();
    txtNomeAluno.setBounds(30, 80, 280, 35);
    estilizarCampo(txtNomeAluno);
    card.add(txtNomeAluno);

    JLabel lblMatricula = criarLabelCampo("Matrícula");
    lblMatricula.setBounds(340, 50, 120, 25);
    card.add(lblMatricula);

    txtMatricula = criarCampoMascara("##########");
    txtMatricula.setBounds(340, 80, 180, 35);
    card.add(txtMatricula);

    JLabel lblTurma = criarLabelCampo("Turma");
    lblTurma.setBounds(550, 50, 120, 25);
    card.add(lblTurma);

    comboTurma = new JComboBox<>(new String[] {"Todas", "101", "102", "201", "202", "301", "302"});
    comboTurma.setBounds(550, 80, 150, 35);
    estilizarCombo(comboTurma);
    card.add(comboTurma);

    JLabel lblStatus = criarLabelCampo("Status");
    lblStatus.setBounds(730, 50, 120, 25);
    card.add(lblStatus);

    comboStatus = new JComboBox<>(new String[] {"Todos", "Ativa", "Assinada", "Cancelada"});
    comboStatus.setBounds(730, 80, 180, 35);
    estilizarCombo(comboStatus);
    card.add(comboStatus);

    JButton btnPesquisar = new JButton("Pesquisar");
    btnPesquisar.setBounds(960, 45, 180, 35);
    estilizarBotao(btnPesquisar);
    card.add(btnPesquisar);

    JButton btnNova = new JButton("Nova Advertência");
    btnNova.setBounds(960, 90, 180, 35);
    estilizarBotao(btnNova);
    card.add(btnNova);
    painel.add(card);
  }

  private void criarHistorico(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Histórico de Advertências");
    card.setBounds(centerX(larguraConteudo), 390, LARGURA_CARD, 350);

    String[] colunas = {"ID", "Matrícula", "Aluno", "Data", "Motivo", "Status"};

    modeloTabela =
        new DefaultTableModel(colunas, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    modeloTabela.addRow(
        new Object[] {1, "202600145", "Pedro Henrique", "15/06/2026", "Desrespeito", "Ativa"});
    modeloTabela.addRow(
        new Object[] {2, "202600145", "Pedro Henrique", "28/05/2026", "Atraso", "Assinada"});
    modeloTabela.addRow(
        new Object[] {3, "202600210", "Ana Clara", "12/04/2026", "Uniforme", "Cancelada"});

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

    JScrollPane scrollTabela = new JScrollPane(tabelaAdvertencias);
    scrollTabela.setBounds(25, 60, 1170, 250);
    scrollTabela.getViewport().setBackground(corInterna);
    scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollTabela);

    painel.add(card);
  }

  private void criarDadosAluno(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Dados do Aluno");
    card.setBounds(centerX(larguraConteudo), 760, LARGURA_CARD, 250);

    adicionarCampoAluno(card, "Nome Completo", "Pedro Henrique", 30, 60);
    adicionarCampoAluno(card, "Matrícula", "202600145", 430, 60);
    adicionarCampoAluno(card, "Turma", "302", 830, 60);
    adicionarCampoAluno(card, "Curso", "Ensino Médio", 30, 140);
    adicionarCampoAluno(card, "Responsável", "Maria da Silva", 430, 140);
    adicionarCampoAluno(card, "Telefone", "(47) 99999-9999", 830, 140);
    painel.add(card);
  }

  private void criarCadastroAdvertencia(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Cadastro da Advertência");
    card.setBounds(centerX(larguraConteudo), 1030, LARGURA_CARD, 280);

    JLabel lblData = criarLabelCampo("Data");
    lblData.setBounds(30, 60, 120, 25);
    card.add(lblData);

    JFormattedTextField txtData = criarCampoMascara("##/##/####");
    txtData.setBounds(30, 90, 220, 35);
    card.add(txtData);

    JLabel lblFuncionario = criarLabelCampo("Funcionário Responsável");
    lblFuncionario.setBounds(300, 60, 220, 25);
    card.add(lblFuncionario);

    JTextField txtFuncionario = new JTextField();
    txtFuncionario.setBounds(300, 90, 350, 35);
    estilizarCampo(txtFuncionario);
    card.add(txtFuncionario);

    JLabel lblTipo = criarLabelCampo("Tipo da Advertência");
    lblTipo.setBounds(700, 60, 200, 25);
    card.add(lblTipo);

    JComboBox<String> comboTipo =
        new JComboBox<>(
            new String[] {
              "Atraso",
              "Falta Disciplinar",
              "Desrespeito",
              "Uso Indevido de Equipamento",
              "Uniforme",
              "Outros"
            });
    comboTipo.setBounds(700, 90, 300, 35);
    estilizarCombo(comboTipo);
    card.add(comboTipo);

    painel.add(card);
  }

  private void criarDescricao(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Descrição da Ocorrência");
    card.setBounds(centerX(larguraConteudo), 1330, LARGURA_CARD, 300);

    JTextArea areaDescricao = new JTextArea();
    areaDescricao.setLineWrap(true);
    areaDescricao.setWrapStyleWord(true);
    areaDescricao.setBackground(corCampo);
    areaDescricao.setForeground(textos);
    areaDescricao.setCaretColor(textos);

    JScrollPane scroll = new JScrollPane(areaDescricao);
    scroll.setBounds(25, 60, 1170, 210);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);
    painel.add(card);
  }

  private void criarProvidencias(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Providências");
    card.setBounds(centerX(larguraConteudo), 1650, LARGURA_CARD, 250);

    JTextArea areaProvidencias = new JTextArea();
    areaProvidencias.setLineWrap(true);
    areaProvidencias.setWrapStyleWord(true);
    areaProvidencias.setBackground(corCampo);
    areaProvidencias.setForeground(textos);
    areaProvidencias.setCaretColor(textos);

    JScrollPane scroll = new JScrollPane(areaProvidencias);
    scroll.setBounds(25, 60, 1170, 160);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scroll);

    painel.add(card);
  }

  private void criarAssinaturas(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Assinaturas");
    card.setBounds(centerX(larguraConteudo), 1920, LARGURA_CARD, 180);

    JCheckBox chkAluno = new JCheckBox("Assinatura do Aluno Recebida");
    chkAluno.setBounds(40, 60, 350, 30);
    chkAluno.setBackground(corCampo);
    chkAluno.setForeground(textos);
    card.add(chkAluno);

    JCheckBox chkResponsavel = new JCheckBox("Assinatura do Responsável Recebida");
    chkResponsavel.setBounds(40, 100, 400, 30);
    chkResponsavel.setBackground(corCampo);
    chkResponsavel.setForeground(textos);
    card.add(chkResponsavel);
    painel.add(card);
  }

  private void criarAcoes(JPanel painel, int larguraConteudo) {
    JPanel card = criarCardSecao("Ações");
    card.setBounds(centerX(larguraConteudo), 2120, LARGURA_CARD, 150);

    JButton btnRegistrar = new JButton("Registrar");
    JButton btnSalvar = new JButton("Salvar");
    JButton btnImprimir = new JButton("Imprimir");
    JButton btnCancelar = new JButton("Cancelar");
    JButton[] botoes = {btnRegistrar, btnSalvar, btnImprimir, btnCancelar};

    int x = 30;
    for (JButton b : botoes) {
      b.setBounds(x, 60, 250, 40);
      estilizarBotao(b);
      card.add(b);
      x += 290;
    }
    painel.add(card);
    btnCancelar.addActionListener(e -> dispose());
  }

  private void adicionarCampoAluno(JPanel painel, String titulo, String valor, int x, int y) {
    JLabel lbl = new JLabel(titulo);
    lbl.setForeground(corLabel);
    lbl.setBounds(x, y, 250, 20);
    painel.add(lbl);

    JTextField campo = new JTextField(valor);
    campo.setEditable(false);
    campo.setBounds(x, y + 25, 320, 35);
    estilizarCampo(campo);
    painel.add(campo);
  }

  private JPanel criarCardSecao(String titulo) {
    JPanel painel = criarPainelArredondado();
    painel.setLayout(null);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lblTitulo.setBounds(30, 15, 500, 30);
    painel.add(lblTitulo);

    return painel;
  }

  private JLabel criarLabelCampo(String texto) {
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

  private JFormattedTextField criarCampoMascara(String mascara) {
    try {
      MaskFormatter formatter = new MaskFormatter(mascara);
      formatter.setPlaceholderCharacter(' ');
      JFormattedTextField campo = new JFormattedTextField(formatter);
      campo.setBackground(corCampo);
      campo.setForeground(textos);
      campo.setCaretColor(textos);
      campo.setBorder(new LineBorder(corBorda));
      return campo;
    } catch (ParseException e) {
      e.printStackTrace();
      return new JFormattedTextField();
    }
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
    lblValor.setBounds(x, y + 20, 150, 40);
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
