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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class EmitirAvisos extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTextField campoTitulo;
  private JTextArea areaDescricao;
  private JSpinner spinnerData;
  private JComboBox<String> comboPrioridade;
  private JComboBox<String> comboTurma;

  private JCheckBox checkAlunos;
  private JCheckBox checkResponsaveis;
  private JCheckBox checkProfessores;
  private JCheckBox checkFuncionarios;

  private JTable tabelaAvisos;
  private DefaultTableModel modeloTabela;

  private static final List<AvisoCalendario> avisosCalendario = new ArrayList<>();

  public EmitirAvisos() {

    setTitle("Emitir Avisos - Professor");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Emitir Aviso.png"));
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

    painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 930));

    JScrollPane scroll = new JScrollPane(painelRolagem);
    scroll.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(corInterna);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    interno.add(scroll);

    JPanel cardResumo = criarPainelArredondado();
    cardResumo.setLayout(null);
    cardResumo.setBounds(xInicial, 0, 1220, 135);
    painelRolagem.add(cardResumo);

    adicionarResumo(cardResumo, "Professor", "Carlos Eduardo Martins", 30, 30);
    adicionarResumo(cardResumo, "Matrícula", "PROF20260012", 320, 30);
    adicionarResumo(cardResumo, "Disciplina", "Matemática", 570, 30);
    adicionarResumo(cardResumo, "Ano letivo", "2026", 800, 30);
    adicionarResumo(cardResumo, "Status", "Ativo", 1010, 30);

    JLabel avisoResumo =
        new JLabel(
            "Os avisos emitidos poderão aparecer nos calendários das telas iniciais do sistema.");
    avisoResumo.setForeground(new Color(120, 255, 170));
    avisoResumo.setFont(new Font("Segoe UI", Font.BOLD, 16));
    avisoResumo.setBounds(30, 95, 900, 25);
    cardResumo.add(avisoResumo);

    JPanel cardFormulario = criarCardSecao("Emitir Novo Aviso");
    cardFormulario.setBounds(xInicial, 165, 600, 600);
    painelRolagem.add(cardFormulario);

    criarFormulario(cardFormulario);

    JPanel cardHistorico = criarCardSecao("Avisos Emitidos");
    cardHistorico.setBounds(xInicial + 630, 165, 590, 600);
    painelRolagem.add(cardHistorico);

    criarTabela();

    JScrollPane scrollTabela = new JScrollPane(tabelaAvisos);
    scrollTabela.setBounds(25, 70, 540, 500);
    scrollTabela.setBorder(new LineBorder(corBorda));
    scrollTabela.getViewport().setBackground(corCampo);
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
    cardHistorico.add(scrollTabela);

    JPanel cardInfo = criarCardSecao("Como será usado no calendário");
    cardInfo.setBounds(xInicial, 795, 1220, 100);
    painelRolagem.add(cardInfo);

    JLabel textoInfo =
        new JLabel(
            "<html>Ao emitir um aviso, ele é salvo na lista <b>avisosCalendario</b>. "
                + "Depois, nas telas iniciais de aluno, professor, funcionário e responsável, "
                + "você poderá consultar essa lista para marcar as datas no calendário.</html>");

    textoInfo.setForeground(textos);
    textoInfo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    textoInfo.setBounds(25, 55, 1160, 35);
    cardInfo.add(textoInfo);
  }

  private void criarFormulario(JPanel painel) {

    JLabel lblTitulo = criarLabelCampo("Título do aviso:");
    lblTitulo.setBounds(25, 70, 200, 22);
    painel.add(lblTitulo);

    campoTitulo = new JTextField();
    campoTitulo.setBounds(25, 95, 535, 38);
    estilizarCampo(campoTitulo);
    painel.add(campoTitulo);

    JLabel lblData = criarLabelCampo("Data para marcar no calendário:");
    lblData.setBounds(25, 150, 260, 22);
    painel.add(lblData);

    spinnerData = new JSpinner(new SpinnerDateModel());
    spinnerData.setBounds(25, 175, 250, 38);
    spinnerData.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    spinnerData.setBorder(new LineBorder(corBorda));

    JSpinner.DateEditor editorData = new JSpinner.DateEditor(spinnerData, "dd/MM/yyyy");
    spinnerData.setEditor(editorData);

    JTextField campoData = editorData.getTextField();
    campoData.setBackground(corCampo);
    campoData.setForeground(Color.WHITE);
    campoData.setCaretColor(Color.WHITE);
    campoData.setFont(new Font("Segoe UI", Font.BOLD, 15));
    campoData.setBorder(new EmptyBorder(0, 10, 0, 10));

    painel.add(spinnerData);

    JLabel lblPrioridade = criarLabelCampo("Prioridade:");
    lblPrioridade.setBounds(310, 150, 200, 22);
    painel.add(lblPrioridade);

    comboPrioridade = new JComboBox<>(new String[] {"Baixa", "Média", "Alta", "Urgente"});
    comboPrioridade.setBounds(310, 175, 250, 38);
    estilizarCombo(comboPrioridade);
    painel.add(comboPrioridade);

    JLabel lblTurma = criarLabelCampo("Turma:");
    lblTurma.setBounds(25, 230, 200, 22);
    painel.add(lblTurma);

    comboTurma =
        new JComboBox<>(new String[] {"Todas as turmas", "101", "201", "302", "801", "902"});
    comboTurma.setBounds(25, 255, 250, 38);
    estilizarCombo(comboTurma);
    painel.add(comboTurma);

    JLabel lblPublico = criarLabelCampo("Enviar para:");
    lblPublico.setBounds(310, 230, 200, 22);
    painel.add(lblPublico);

    checkAlunos = criarCheck("Alunos");
    checkAlunos.setBounds(310, 255, 120, 25);
    checkAlunos.setSelected(true);
    painel.add(checkAlunos);

    checkResponsaveis = criarCheck("Responsáveis");
    checkResponsaveis.setBounds(430, 255, 140, 25);
    checkResponsaveis.setSelected(true);
    painel.add(checkResponsaveis);

    checkProfessores = criarCheck("Professores");
    checkProfessores.setBounds(310, 285, 140, 25);
    painel.add(checkProfessores);

    checkFuncionarios = criarCheck("Funcionários");
    checkFuncionarios.setBounds(430, 285, 140, 25);
    painel.add(checkFuncionarios);

    JLabel lblDescricao = criarLabelCampo("Descrição do aviso:");
    lblDescricao.setBounds(25, 325, 200, 22);
    painel.add(lblDescricao);

    areaDescricao = new JTextArea();
    areaDescricao.setLineWrap(true);
    areaDescricao.setWrapStyleWord(true);
    areaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    areaDescricao.setForeground(Color.WHITE);
    areaDescricao.setBackground(corCampo);
    areaDescricao.setCaretColor(Color.WHITE);
    areaDescricao.setBorder(new EmptyBorder(10, 10, 10, 10));

    JScrollPane scrollDescricao = new JScrollPane(areaDescricao);
    scrollDescricao.setBounds(25, 350, 535, 120);
    scrollDescricao.setBorder(new LineBorder(corBorda));
    scrollDescricao.getVerticalScrollBar().setUnitIncrement(26);
    scrollDescricao.getViewport().setBackground(corCampo);
    painel.add(scrollDescricao);

    JButton btnEmitir = new JButton("Emitir aviso");
    btnEmitir.setBounds(25, 500, 180, 42);
    estilizarBotao(btnEmitir);
    btnEmitir.addActionListener(e -> emitirAviso());
    painel.add(btnEmitir);

    JButton btnLimpar = new JButton("Limpar campos");
    btnLimpar.setBounds(220, 500, 180, 42);
    estilizarBotao(btnLimpar);
    btnLimpar.addActionListener(e -> limparCampos());
    painel.add(btnLimpar);
  }

  private void emitirAviso() {

    String titulo = campoTitulo.getText().trim();
    String descricao = areaDescricao.getText().trim();

    if (titulo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Informe o título do aviso.");
      return;
    }

    if (descricao.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Informe a descrição do aviso.");
      return;
    }

    if (!checkAlunos.isSelected()
        && !checkResponsaveis.isSelected()
        && !checkProfessores.isSelected()
        && !checkFuncionarios.isSelected()) {

      JOptionPane.showMessageDialog(this, "Selecione pelo menos um público para receber o aviso.");
      return;
    }

    Date data = (Date) spinnerData.getValue();
    String prioridade = comboPrioridade.getSelectedItem().toString();
    String turma = comboTurma.getSelectedItem().toString();
    String publico = montarPublico();

    AvisoCalendario aviso =
        new AvisoCalendario(
            titulo, descricao, data, prioridade, turma, publico, "Carlos Eduardo Martins");

    avisosCalendario.add(aviso);

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    modeloTabela.addRow(new Object[] {sdf.format(data), titulo, turma, publico, prioridade});

    JOptionPane.showMessageDialog(this, "Aviso emitido e marcado para aparecer no calendário.");

    limparCampos();
  }

  private String montarPublico() {

    StringBuilder publico = new StringBuilder();

    if (checkAlunos.isSelected()) {
      publico.append("Alunos, ");
    }

    if (checkResponsaveis.isSelected()) {
      publico.append("Responsáveis, ");
    }

    if (checkProfessores.isSelected()) {
      publico.append("Professores, ");
    }

    if (checkFuncionarios.isSelected()) {
      publico.append("Funcionários, ");
    }

    if (publico.length() >= 2) {
      publico.setLength(publico.length() - 2);
    }

    return publico.toString();
  }

  private void limparCampos() {

    campoTitulo.setText("");
    areaDescricao.setText("");
    spinnerData.setValue(new Date());
    comboPrioridade.setSelectedIndex(0);
    comboTurma.setSelectedIndex(0);
    checkAlunos.setSelected(true);
    checkResponsaveis.setSelected(true);
    checkProfessores.setSelected(false);
    checkFuncionarios.setSelected(false);
  }

  private void criarTabela() {

    String[] colunas = {"Data", "Título", "Turma", "Público", "Prioridade"};

    modeloTabela =
        new DefaultTableModel(colunas, 0) {

          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    modeloTabela.addRow(
        new Object[] {"10/06/2026", "Trabalho avaliativo", "302", "Alunos, Responsáveis", "Média"});

    modeloTabela.addRow(
        new Object[] {"15/06/2026", "Prova de Matemática", "201", "Alunos, Responsáveis", "Alta"});

    tabelaAvisos = new JTable(modeloTabela);
    tabelaAvisos.setRowHeight(42);
    tabelaAvisos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    tabelaAvisos.setForeground(Color.WHITE);
    tabelaAvisos.setBackground(corCampo);
    tabelaAvisos.setGridColor(corBorda);
    tabelaAvisos.setSelectionBackground(new Color(120, 40, 220));
    tabelaAvisos.setSelectionForeground(Color.WHITE);
    tabelaAvisos.setShowGrid(true);
    tabelaAvisos.setShowVerticalLines(true);
    tabelaAvisos.setShowHorizontalLines(true);
    tabelaAvisos.setFillsViewportHeight(true);

    JTableHeader header = tabelaAvisos.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    header.setForeground(Color.WHITE);
    header.setBackground(new Color(80, 25, 150));
    header.setPreferredSize(new Dimension(header.getWidth(), 42));
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);

    ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(JLabel.CENTER);
    centro.setVerticalAlignment(JLabel.CENTER);
    centro.setForeground(Color.WHITE);
    centro.setBackground(corCampo);

    for (int i = 0; i < tabelaAvisos.getColumnCount(); i++) {
      tabelaAvisos.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    tabelaAvisos.getColumnModel().getColumn(0).setPreferredWidth(90);
    tabelaAvisos.getColumnModel().getColumn(1).setPreferredWidth(170);
    tabelaAvisos.getColumnModel().getColumn(2).setPreferredWidth(90);
    tabelaAvisos.getColumnModel().getColumn(3).setPreferredWidth(160);
    tabelaAvisos.getColumnModel().getColumn(4).setPreferredWidth(100);
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

    JLabel titulo = new JLabel("Emitir Avisos");
    titulo.setForeground(Color.WHITE);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 600, 45);
    topo.add(titulo);

    JLabel sub =
        new JLabel("Cadastre avisos para aparecerem no calendário das telas iniciais do sistema.");
    sub.setForeground(new Color(245, 225, 255));
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    sub.setBounds(42, 120, 950, 25);
    topo.add(sub);

    return topo;
  }

  private JPanel criarCardSecao(String titulo) {

    JPanel painel = criarPainelArredondado();
    painel.setLayout(null);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lblTitulo.setBounds(25, 18, 500, 30);
    painel.add(lblTitulo);

    return painel;
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

  private JLabel criarLabelCampo(String texto) {

    JLabel label = new JLabel(texto);
    label.setForeground(corLabel);
    label.setFont(new Font("Segoe UI", Font.BOLD, 14));
    return label;
  }

  private JCheckBox criarCheck(String texto) {

    JCheckBox check = new JCheckBox(texto);
    check.setFont(new Font("Segoe UI", Font.BOLD, 13));
    check.setForeground(Color.WHITE);
    check.setBackground(corCampo);
    check.setFocusPainted(false);
    check.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return check;
  }

  private void estilizarCampo(JTextField campo) {

    campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    campo.setForeground(Color.WHITE);
    campo.setBackground(corCampo);
    campo.setCaretColor(Color.WHITE);
    campo.setBorder(new LineBorder(corBorda));
  }

  private void estilizarCombo(JComboBox<String> combo) {

    combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    combo.setForeground(Color.WHITE);
    combo.setBackground(corCampo);
    combo.setBorder(new LineBorder(corBorda));
    combo.setFocusable(false);
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

  public static List<AvisoCalendario> getAvisosCalendario() {
    return avisosCalendario;
  }

  public static class AvisoCalendario {

    private String titulo;
    private String descricao;
    private Date data;
    private String prioridade;
    private String turma;
    private String publico;
    private String professor;

    public AvisoCalendario(
        String titulo,
        String descricao,
        Date data,
        String prioridade,
        String turma,
        String publico,
        String professor) {

      this.titulo = titulo;
      this.descricao = descricao;
      this.data = data;
      this.prioridade = prioridade;
      this.turma = turma;
      this.publico = publico;
      this.professor = professor;
    }

    public String getTitulo() {
      return titulo;
    }

    public String getDescricao() {
      return descricao;
    }

    public Date getData() {
      return data;
    }

    public String getPrioridade() {
      return prioridade;
    }

    public String getTurma() {
      return turma;
    }

    public String getPublico() {
      return publico;
    }

    public String getProfessor() {
      return professor;
    }
  }
}
