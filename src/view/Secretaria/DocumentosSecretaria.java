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

public class DocumentosSecretaria extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaDocumentos;
  private DefaultTableModel modeloTabela;

  private JTextField txtNomeAluno;
  private JFormattedTextField txtMatricula;
  private JTextField txtTurma;

  private JComboBox<String> comboStatus;

  public DocumentosSecretaria() {
    setTitle("Documentos - Secretaria");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Documentos Secretaria.jpeg"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setResizable(false);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int larguraInterno = areaUtil.width - 60;
    int alturaInterno = areaUtil.height - 60;

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
    painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 1600));

    JScrollPane scroll = new JScrollPane(painelRolagem);
    scroll.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(corInterna);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    interno.add(scroll);

    criarResumoDocumentos(painelRolagem);
    criarPesquisa(painelRolagem);
    criarTabelaDocumentos(painelRolagem);
    criarDadosAluno(painelRolagem);
    criarDocumentosAluno(painelRolagem);
    criarObservacoes(painelRolagem);
  }

  private int centralizarCard(JPanel painel, int larguraCard) {
    return (painel.getPreferredSize().width - larguraCard) / 2;
  }

  private JPanel criarTopo() {
    JPanel topo =
        new JPanel(null) {
          private static final long serialVersionUID = 1L;

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

    JLabel titulo = new JLabel("Documentos");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 400, 45);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Gerenciamento documental dos alunos");
    subtitulo.setForeground(new Color(245, 225, 255));
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    subtitulo.setBounds(47, 120, 600, 25);
    topo.add(subtitulo);
    return topo;
  }

  private void criarResumoDocumentos(JPanel painel) {
    JPanel card = criarCardSecao("Resumo Geral");
    card.setBounds(centralizarCard(painel, 1220), 20, 1220, 150);

    adicionarIndicador(card, "Total de Alunos", "523", 60, 60, new Color(120, 200, 255));
    adicionarIndicador(card, "Documentação Completa", "480", 340, 60, new Color(120, 255, 170));
    adicionarIndicador(card, "Pendentes", "31", 650, 60, new Color(255, 180, 80));
    adicionarIndicador(card, "Vencidos", "12", 930, 60, new Color(255, 90, 90));
    painel.add(card);
  }

  private void criarPesquisa(JPanel painel) {
    JPanel card = criarCardSecao("Pesquisa");
    card.setBounds(centralizarCard(painel, 1220), 190, 1220, 170);

    JLabel lblNome = criarLabelCampo("Nome do Aluno");
    lblNome.setBounds(30, 50, 200, 25);

    txtNomeAluno = new JTextField();
    txtNomeAluno.setBounds(30, 80, 320, 35);
    estilizarCampo(txtNomeAluno);
    card.add(lblNome);
    card.add(txtNomeAluno);

    JLabel lblMatricula = criarLabelCampo("Matrícula");
    lblMatricula.setBounds(390, 50, 150, 25);

    txtMatricula = criarCampoMascara("##########");
    txtMatricula.setBounds(390, 80, 180, 35);
    card.add(lblMatricula);
    card.add(txtMatricula);

    JLabel lblTurma = criarLabelCampo("Turma");
    lblTurma.setBounds(610, 50, 150, 25);
    card.add(lblTurma);

    txtTurma = new JTextField();
    txtTurma.setBounds(610, 80, 160, 35);
    estilizarCampo(txtTurma);
    card.add(txtTurma);

    JLabel lblStatus = criarLabelCampo("Status");
    lblStatus.setBounds(810, 50, 150, 25);
    card.add(lblStatus);

    comboStatus = new JComboBox<>(new String[] {"Todos", "Completo", "Pendente", "Vencido"});
    comboStatus.setBounds(810, 80, 180, 35);
    estilizarCombo(comboStatus);
    card.add(comboStatus);

    JButton btnPesquisar = new JButton("Pesquisar");
    btnPesquisar.setBounds(1010, 45, 160, 35);
    estilizarBotao(btnPesquisar);
    card.add(btnPesquisar);

    JButton btnLimpar = new JButton("Limpar");
    btnLimpar.setBounds(1010, 90, 160, 35);
    estilizarBotao(btnLimpar);
    card.add(btnLimpar);
    painel.add(card);
  }

  private void criarTabelaDocumentos(JPanel painel) {
    JPanel card = criarCardSecao("Lista de Documentos");
    card.setBounds(centralizarCard(painel, 1220), 380, 1220, 330);

    String[] colunas = {
      "Matrícula",
      "Aluno",
      "RG",
      "CPF",
      "RG Responsável",
      "CPF Responsável",
      "Comprovante",
      "Status"
    };

    modeloTabela =
        new DefaultTableModel(colunas, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    modeloTabela.addRow(
        new Object[] {"202600145", "Pedro Henrique", "OK", "OK", "OK", "OK", "OK", "Completo"});
    modeloTabela.addRow(
        new Object[] {"202600146", "Ana Clara", "OK", "OK", "Pendente", "OK", "OK", "Pendente"});
    modeloTabela.addRow(
        new Object[] {"202600147", "Carlos Silva", "OK", "OK", "OK", "OK", "Vencido", "Vencido"});

    tabelaDocumentos = new JTableBase().createTable(modeloTabela);

    JScrollPane scrollTabela = new JScrollPane(tabelaDocumentos);
    scrollTabela.setBounds(25, 60, 1170, 250);
    scrollTabela.getViewport().setBackground(corInterna);
    scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollTabela);
    painel.add(card);
  }

  private void criarDadosAluno(JPanel painel) {
    JPanel card = criarCardSecao("Dados do Aluno");
    card.setBounds(centralizarCard(painel, 1220), 730, 1220, 250);

    adicionarCampoAluno(card, "Nome Completo", "Pedro Henrique Mendes", 30, 60);
    adicionarCampoAluno(card, "Matrícula", "202600145", 430, 60);
    adicionarCampoAluno(card, "Turma", "302", 830, 60);
    adicionarCampoAluno(card, "Curso", "Ensino Médio", 30, 140);
    adicionarCampoAluno(card, "Turno", "Matutino", 430, 140);
    painel.add(card);
  }

  private void criarDocumentosAluno(JPanel painel) {
    JPanel card = criarCardSecao("Documentos");
    card.setBounds(centralizarCard(painel, 1220), 1000, 1220, 300);

    adicionarLinhaDocumento(card, "RG do Aluno", 60);
    adicionarLinhaDocumento(card, "CPF do Aluno", 105);
    adicionarLinhaDocumento(card, "RG do Responsável", 150);
    adicionarLinhaDocumento(card, "CPF do Responsável", 195);
    adicionarLinhaDocumento(card, "Comprovante de Residência", 240);
    painel.add(card);
  }

  private void criarObservacoes(JPanel painel) {
    JPanel card = criarCardSecao("Observações");
    card.setBounds(centralizarCard(painel, 1220), 1320, 1220, 250);

    JTextArea area = new JTextArea();
    area.setText("Documentação conferida pela secretaria.");
    area.setBackground(corCampo);
    area.setForeground(textos);
    area.setCaretColor(textos);
    area.setFont(new Font("Segoe UI", Font.BOLD, 22));
    area.setBorder(new LineBorder(corBorda));

    JScrollPane scroll = new JScrollPane(area);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    scroll.setBounds(25, 60, 1170, 120);
    card.add(scroll);

    JButton btnSalvar = new JButton("Salvar Alterações");
    btnSalvar.setBounds(25, 195, 220, 40);
    estilizarBotao(btnSalvar);
    card.add(btnSalvar);

    JButton btnCompleto = new JButton("Marcar Completo");
    btnCompleto.setBounds(265, 195, 220, 40);
    estilizarBotao(btnCompleto);
    card.add(btnCompleto);

    JButton btnPendente = new JButton("Marcar Pendente");
    btnPendente.setBounds(505, 195, 220, 40);
    estilizarBotao(btnPendente);
    card.add(btnPendente);
    painel.add(card);
  }

  private void adicionarLinhaDocumento(JPanel painel, String nome, int y) {
    JLabel lbl = new JLabel(nome);
    lbl.setForeground(textos);
    lbl.setBounds(30, y, 300, 30);
    painel.add(lbl);

    JButton visualizar = new JButton("Visualizar");
    visualizar.setBounds(650, y, 180, 30);
    estilizarBotao(visualizar);
    painel.add(visualizar);

    JButton substituir = new JButton("Substituir");
    substituir.setBounds(860, y, 180, 30);
    estilizarBotao(substituir);
    painel.add(substituir);
  }

  private void adicionarCampoAluno(JPanel painel, String titulo, String valor, int x, int y) {
    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setBounds(x, y, 250, 20);
    painel.add(lblTitulo);

    JTextField campo = new JTextField(valor);
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
    lblTitulo.setBounds(30, 15, 400, 30);
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
    lblTitulo.setBounds(x, y, 220, 25);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(cor);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
    lblValor.setBounds(x, y + 25, 150, 35);
    painel.add(lblValor);
  }

  private JPanel criarPainelArredondado() {
    return new JPanel() {
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
