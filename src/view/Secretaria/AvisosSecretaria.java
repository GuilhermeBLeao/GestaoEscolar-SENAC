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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import view.JTableBase;

public class AvisosSecretaria extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaAvisos;
  private DefaultTableModel modeloTabela;

  private JTextField txtPesquisaTitulo;
  private JComboBox<String> cbPesquisaCategoria;
  private JComboBox<String> cbPesquisaPrioridade;
  private JComboBox<String> cbPesquisaStatus;

  public AvisosSecretaria() {
    setTitle("Avisos - Secretaria");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Avisos Secretaria.jpeg"));
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
    interno.setBackground(corInterna);
    interno.setBounds(20, 20, largura, altura);
    externo.add(interno);

    criarConteudo(interno, largura, altura);
  }

  private void criarConteudo(JPanel interno, int largura, int altura) {
    JPanel topo = criarTopo();
    topo.setBounds(30, 25, largura - 60, 160);
    interno.add(topo);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(35, 35, 150, 40);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    JPanel painelRolagem = new JPanel(null);
    painelRolagem.setBackground(corInterna);
    painelRolagem.setPreferredSize(new Dimension(largura - 80, 1860));

    JScrollPane scroll = new JScrollPane(painelRolagem);
    scroll.setBounds(30, 210, largura - 60, altura - 240);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    interno.add(scroll);

    criarResumo(painelRolagem);
    criarPesquisa(painelRolagem);
    criarTabelaAvisos(painelRolagem);
    criarFormularioAviso(painelRolagem);
    criarConteudoAviso(painelRolagem);
    criarDestinatarios(painelRolagem);
    criarAcoes(painelRolagem);
  }

  private int centralizarCard(JPanel painel, int larguraCard) {
    return (painel.getPreferredSize().width - larguraCard) / 2;
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

    JLabel subtitulo = new JLabel("Gerenciamento de avisos da instituição");
    subtitulo.setForeground(new Color(245, 225, 255));
    subtitulo.setBounds(45, 120, 500, 25);
    topo.add(subtitulo);
    return topo;
  }

  private void criarResumo(JPanel painel) {
    JPanel card = criarCardSecao("Resumo Geral");
    card.setBounds(centralizarCard(painel, 1220), 20, 1220, 150);

    adicionarIndicador(card, "Total", "124", 60, 60, Color.CYAN);
    adicionarIndicador(card, "Ativos", "89", 340, 60, Color.GREEN);
    adicionarIndicador(card, "Expirados", "20", 650, 60, Color.ORANGE);
    adicionarIndicador(card, "Urgentes", "15", 930, 60, Color.RED);
    painel.add(card);
  }

  private void criarPesquisa(JPanel painel) {
    JPanel card = criarCardSecao("Pesquisa");
    card.setBounds(centralizarCard(painel, 1220), 190, 1220, 180);

    JLabel lblTitulo = criarLabelCampo("Título");
    lblTitulo.setBounds(30, 50, 120, 25);
    card.add(lblTitulo);

    txtPesquisaTitulo = new JTextField();
    txtPesquisaTitulo.setBounds(30, 80, 250, 35);
    estilizarCampo(txtPesquisaTitulo);
    card.add(txtPesquisaTitulo);

    JLabel lblCategoria = criarLabelCampo("Categoria");
    lblCategoria.setBounds(310, 50, 120, 25);
    card.add(lblCategoria);

    cbPesquisaCategoria =
        new JComboBox<>(
            new String[] {"Todas", "Evento", "Acadêmico", "Secretaria", "Financeiro", "Geral"});
    cbPesquisaCategoria.setBounds(310, 80, 180, 35);
    estilizarCombo(cbPesquisaCategoria);
    card.add(cbPesquisaCategoria);

    JLabel lblPrioridade = criarLabelCampo("Prioridade");
    lblPrioridade.setBounds(520, 50, 120, 25);
    card.add(lblPrioridade);

    cbPesquisaPrioridade =
        new JComboBox<>(new String[] {"Todas", "Baixa", "Média", "Alta", "Urgente"});
    cbPesquisaPrioridade.setBounds(520, 80, 180, 35);
    estilizarCombo(cbPesquisaPrioridade);
    card.add(cbPesquisaPrioridade);

    JLabel lblStatus = criarLabelCampo("Status");
    lblStatus.setBounds(730, 50, 120, 25);
    card.add(lblStatus);

    cbPesquisaStatus = new JComboBox<>(new String[] {"Todos", "Ativo", "Expirado"});
    cbPesquisaStatus.setBounds(730, 80, 180, 35);
    estilizarCombo(cbPesquisaStatus);
    card.add(cbPesquisaStatus);

    JButton btnPesquisar = new JButton("Pesquisar");
    btnPesquisar.setBounds(960, 45, 180, 35);
    estilizarBotao(btnPesquisar);
    card.add(btnPesquisar);

    JButton btnNovo = new JButton("Novo Aviso");
    btnNovo.setBounds(960, 90, 180, 35);
    estilizarBotao(btnNovo);
    card.add(btnNovo);
    painel.add(card);
  }

  private void criarTabelaAvisos(JPanel painel) {
    JPanel card = criarCardSecao("Lista de Avisos");
    card.setBounds(centralizarCard(painel, 1220), 390, 1220, 350);

    String[] colunas = {"ID", "Título", "Categoria", "Prioridade", "Publicação", "Status"};

    modeloTabela =
        new DefaultTableModel(colunas, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    modeloTabela.addRow(
        new Object[] {1, "Reunião de Pais", "Evento", "Alta", "15/06/2026", "Ativo"});
    modeloTabela.addRow(
        new Object[] {2, "Entrega de Boletins", "Acadêmico", "Média", "20/06/2026", "Ativo"});

    tabelaAvisos = new JTableBase().createTable(modeloTabela);

    JScrollPane scrollTabela = new JScrollPane(tabelaAvisos);
    scrollTabela.setBounds(25, 60, 1170, 250);
    scrollTabela.getViewport().setBackground(corInterna);
    scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollTabela);
    painel.add(card);
  }

  private void criarFormularioAviso(JPanel painel) {
    JPanel card = criarCardSecao("Cadastro do Aviso");
    card.setBounds(centralizarCard(painel, 1220), 760, 1220, 300);

    JLabel lblTitulo = criarLabelCampo("Título");
    lblTitulo.setBounds(30, 60, 150, 25);
    card.add(lblTitulo);

    JTextField txtTitulo = new JTextField();
    txtTitulo.setBounds(30, 90, 500, 35);
    estilizarCampo(txtTitulo);
    card.add(txtTitulo);

    JLabel lblCategoria = criarLabelCampo("Categoria");
    lblCategoria.setBounds(560, 60, 150, 25);
    card.add(lblCategoria);
    JComboBox<String> cbCategoria =
        new JComboBox<>(new String[] {"Evento", "Acadêmico", "Secretaria", "Financeiro", "Geral"});
    cbCategoria.setBounds(560, 90, 220, 35);
    estilizarCombo(cbCategoria);
    card.add(cbCategoria);

    JLabel lblPrioridade = criarLabelCampo("Prioridade");
    lblPrioridade.setBounds(820, 60, 150, 25);
    card.add(lblPrioridade);

    JComboBox<String> cbPrioridade =
        new JComboBox<>(new String[] {"Baixa", "Média", "Alta", "Urgente"});
    cbPrioridade.setBounds(820, 90, 220, 35);
    estilizarCombo(cbPrioridade);
    card.add(cbPrioridade);

    JLabel lblPublicacao = criarLabelCampo("Data Publicação");
    lblPublicacao.setBounds(30, 160, 180, 25);
    card.add(lblPublicacao);

    JFormattedTextField txtPublicacao = criarCampoMascara("##/##/####");
    txtPublicacao.setBounds(30, 190, 220, 35);
    card.add(txtPublicacao);

    JLabel lblExpiracao = criarLabelCampo("Data Expiração");
    lblExpiracao.setBounds(290, 160, 180, 25);
    card.add(lblExpiracao);

    JFormattedTextField txtExpiracao = criarCampoMascara("##/##/####");
    txtExpiracao.setBounds(290, 190, 220, 35);
    card.add(txtExpiracao);
    painel.add(card);
  }

  private void criarConteudoAviso(JPanel painel) {
    JPanel card = criarCardSecao("Conteúdo do Aviso");
    card.setBounds(centralizarCard(painel, 1220), 1080, 1220, 350);

    JTextArea areaConteudo = new JTextArea();
    areaConteudo.setLineWrap(true);
    areaConteudo.setWrapStyleWord(true);
    areaConteudo.setBackground(corCampo);
    areaConteudo.setForeground(textos);
    areaConteudo.setCaretColor(textos);

    JScrollPane scroll = new JScrollPane(areaConteudo);
    scroll.setBounds(25, 60, 1170, 250);
    card.add(scroll);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    painel.add(card);
  }

  private void criarDestinatarios(JPanel painel) {
    JPanel card = criarCardSecao("Destinatários");
    card.setBounds(centralizarCard(painel, 1220), 1450, 1220, 200);

    JCheckBox chkAlunos = new JCheckBox("Todos os Alunos");
    JCheckBox chkResponsaveis = new JCheckBox("Todos os Responsáveis");
    JCheckBox chkProfessores = new JCheckBox("Todos os Professores");
    JCheckBox chkFuncionarios = new JCheckBox("Todos os Funcionários");
    JCheckBox[] lista = {chkAlunos, chkResponsaveis, chkProfessores, chkFuncionarios};

    int y = 60;

    for (JCheckBox item : lista) {
      item.setBounds(40, y, 300, 30);
      item.setBackground(corCampo);
      item.setForeground(textos);
      card.add(item);
      y += 35;
    }
    painel.add(card);
  }

  private void criarAcoes(JPanel painel) {
    JPanel card = criarCardSecao("Ações");
    card.setBounds(centralizarCard(painel, 1220), 1670, 1220, 150);

    JButton btnPublicar = new JButton("Publicar Aviso");
    JButton btnSalvar = new JButton("Salvar Alterações");
    JButton btnExcluir = new JButton("Excluir Aviso");
    JButton btnLimpar = new JButton("Limpar Campos");
    JButton[] botoes = {btnPublicar, btnSalvar, btnExcluir, btnLimpar};

    int x = 30;

    for (JButton botao : botoes) {
      botao.setBounds(x, 60, 250, 40);
      estilizarBotao(botao);
      card.add(botao);
      x += 290;
    }
    painel.add(card);
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
