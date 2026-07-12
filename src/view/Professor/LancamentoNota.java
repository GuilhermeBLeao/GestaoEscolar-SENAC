package view.Professor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import util.DadosSistema;

public class LancamentoNota extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaNotas;
  private DefaultTableModel modeloTabela;
  private JTextField txtAtividade;
  private JComboBox<String> cbTipoNota;

  public LancamentoNota() {
    setTitle("Lançamento de Notas");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Lancamento de Notas.png"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int margem = 30;
    int larguraInterno = areaUtil.width - (margem * 2);
    int alturaInterno = areaUtil.height - (margem * 2);

    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(1200, 720));
    setResizable(false);

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);

    JPanel interno = new JPanel(null);
    interno.setBounds(
        (areaUtil.width - larguraInterno) / 2,
        (areaUtil.height - alturaInterno) / 2,
        larguraInterno,
        alturaInterno);
    interno.setBackground(corInterna);
    externo.add(interno);

    int larguraResumo = 260;
    int margemLateral = 40;
    int larguraConteudo = larguraInterno - larguraResumo - (margemLateral * 3);

    JLabel titulo = new JLabel("Lançamento de Notas");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
    titulo.setHorizontalAlignment(SwingConstants.CENTER);
    titulo.setBounds(0, 45, larguraInterno, 55);
    interno.add(titulo);

    JLabel subtitulo =
        new JLabel("Registre, calcule e acompanhe o desempenho dos alunos por turma.");
    subtitulo.setForeground(textos);
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
    subtitulo.setBounds(0, 105, larguraInterno, 30);
    interno.add(subtitulo);

    JPanel filtros = criarPainelArredondado(corCampo, corBorda, 24);
    filtros.setLayout(null);
    filtros.setBounds(margemLateral, 160, larguraConteudo, 160);
    interno.add(filtros);

    int larguraConteudoFiltros = 850;
    int inicioFiltros = Math.max(30, (filtros.getWidth() - larguraConteudoFiltros) / 2);

    adicionarLabel(filtros, "Turma", inicioFiltros, 10);
    JComboBox<String> cbTurma = criarCombo(new String[] {"ADS-1", "ADS-2", "ADS-3", "ADS-4"});
    cbTurma.setBounds(inicioFiltros, 45, 160, 38);
    filtros.add(cbTurma);

    adicionarLabel(filtros, "Disciplina", inicioFiltros + 190, 10);
    JComboBox<String> cbDisciplina =
        criarCombo(
            new String[] {
              "Matemática Aplicada",
              "Programação Orientada a Objetos",
              "Banco de Dados",
              "Engenharia de Software"
            });
    cbDisciplina.setBounds(inicioFiltros + 190, 45, 300, 38);
    filtros.add(cbDisciplina);

    adicionarLabel(filtros, "Período", inicioFiltros + 520, 10);
    JComboBox<String> cbPeriodo =
        criarCombo(new String[] {"1º Bimestre", "2º Bimestre", "3º Bimestre", "4º Bimestre"});
    cbPeriodo.setBounds(inicioFiltros + 520, 45, 180, 38);
    filtros.add(cbPeriodo);

    adicionarLabel(filtros, "Tipo da Nota", inicioFiltros, 80);
    cbTipoNota =
        criarCombo(
            new String[] {
              "Prova", "Trabalho", "Avaliação", "Seminário", "Projeto", "Recuperação", "Outro"
            });
    cbTipoNota.setBounds(inicioFiltros, 105, 180, 38);
    filtros.add(cbTipoNota);

    adicionarLabel(filtros, "Descrição", inicioFiltros + 200, 80);
    txtAtividade = criarCampoTexto();
    txtAtividade.setBounds(inicioFiltros + 200, 105, 340, 38);
    filtros.add(txtAtividade);

    JButton btnCarregar = new JButton("Carregar alunos");
    btnCarregar.setBounds(inicioFiltros + 550, 105, 190, 38);
    estilizarBotao(btnCarregar);
    filtros.add(btnCarregar);

    JPanel tabelaPainel = criarPainelArredondado(corCampo, corBorda, 24);
    tabelaPainel.setLayout(null);
    tabelaPainel.setBounds(margemLateral, 335, larguraConteudo, alturaInterno - 395);
    interno.add(tabelaPainel);

    JLabel tituloTabela = new JLabel("Notas dos Alunos");
    tituloTabela.setForeground(textos);
    tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 26));
    tituloTabela.setBounds(30, 20, 400, 35);
    tabelaPainel.add(tituloTabela);

    criarTabela();

    int alturaTabelaReal =
        tabelaNotas.getTableHeader().getPreferredSize().height
            + (modeloTabela.getRowCount() * tabelaNotas.getRowHeight());

    JScrollPane scroll = new JScrollPane(tabelaNotas);

    int larguraScroll = tabelaPainel.getWidth() - 60;
    int alturaMaximaScroll = tabelaPainel.getHeight() - 145;
    int alturaScroll = Math.min(alturaTabelaReal + 2, alturaMaximaScroll);

    scroll.setBounds(30, 70, larguraScroll, alturaScroll);
    scroll.getViewport().setBackground(corInterna);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    tabelaPainel.add(scroll);

    int yBotoes = scroll.getY() + scroll.getHeight() + 15;

    JButton btnSalvar = new JButton("Salvar notas");
    btnSalvar.setBounds(30, yBotoes, 170, 42);
    estilizarBotao(btnSalvar);
    tabelaPainel.add(btnSalvar);

    JButton btnLimpar = new JButton("Limpar");
    btnLimpar.setBounds(220, yBotoes, 130, 42);
    estilizarBotao(btnLimpar);
    tabelaPainel.add(btnLimpar);

    JButton btnCancelar = new JButton("Cancelar");
    btnCancelar.setBounds(370, yBotoes, 130, 42);
    estilizarBotao(btnCancelar);
    tabelaPainel.add(btnCancelar);

    JPanel resumo = criarPainelArredondado(corCampo, corBorda, 28);
    resumo.setLayout(null);
    resumo.setBounds(
        larguraInterno - larguraResumo - margemLateral, 160, larguraResumo, alturaInterno - 300);
    interno.add(resumo);

    JLabel tituloResumo = new JLabel("Resumo");
    tituloResumo.setForeground(textos);
    tituloResumo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    tituloResumo.setBounds(25, 25, 220, 35);
    resumo.add(tituloResumo);

    JPanel linha = new JPanel();
    linha.setBackground(corLabel);
    linha.setBounds(25, 70, 210, 3);
    resumo.add(linha);

    adicionarResumo(resumo, "Alunos", String.valueOf(modeloTabela.getRowCount()), 105);

    JLabel dica =
        new JLabel(
            "<html>Digite a atividade, preencha uma nota por aluno e salve o lançamento.</html>");
    dica.setForeground(textos);
    dica.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    dica.setBounds(25, 180, 210, 90);
    resumo.add(dica);

    btnLimpar.addActionListener(e -> limparNotas());
    btnCancelar.addActionListener(e -> dispose());
    btnSalvar.addActionListener(e -> salvarNotas());

    setVisible(true);
  }

  private void criarTabela() {

    String[] colunas = {"Matrícula", "Aluno", "Nota"};

    Object[][] dados = DadosSistema.lancamentoNotasAlunosLinhas().toArray(new Object[0][]);

    modeloTabela =
        new DefaultTableModel(dados, colunas) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return column == 2;
          }
        };

    tabelaNotas = new JTable(modeloTabela);
    tabelaNotas.setRowHeight(50);
    tabelaNotas.setFont(new Font("Segoe UI", Font.PLAIN, 17));
    tabelaNotas.setForeground(textos);
    tabelaNotas.setBackground(corInterna);
    tabelaNotas.setGridColor(new Color(90, 50, 170));
    tabelaNotas.setSelectionBackground(new Color(80, 40, 160));
    tabelaNotas.setSelectionForeground(textos);
    tabelaNotas.setShowGrid(true);
    tabelaNotas.setShowHorizontalLines(true);
    tabelaNotas.setShowVerticalLines(true);
    tabelaNotas.setIntercellSpacing(new Dimension(1, 1));
    tabelaNotas.setFillsViewportHeight(false);
    tabelaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    JTableHeader header = tabelaNotas.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 16));
    header.setBackground(corInterna);
    header.setForeground(textos);
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setPreferredSize(new Dimension(header.getWidth(), 32));

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);
    centro.setVerticalAlignment(SwingConstants.CENTER);
    centro.setBackground(new Color(31, 10, 90));
    centro.setForeground(textos);
    centro.setFont(new Font("Segoe UI", Font.PLAIN, 17));

    for (int i = 0; i < tabelaNotas.getColumnCount(); i++) {
      tabelaNotas.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    JTextField campoNota = new JTextField();
    campoNota.setHorizontalAlignment(JTextField.CENTER);
    campoNota.setFont(new Font("Segoe UI", Font.BOLD, 17));
    campoNota.setForeground(textos);
    campoNota.setCaretColor(textos);
    campoNota.setBackground(new Color(31, 10, 90));
    campoNota.setBorder(new LineBorder(corLabel, 1, true));

    DefaultCellEditor editorNota = new DefaultCellEditor(campoNota);
    tabelaNotas.getColumnModel().getColumn(2).setCellEditor(editorNota);

    tabelaNotas.getColumnModel().getColumn(0).setPreferredWidth(170);
    tabelaNotas.getColumnModel().getColumn(1).setPreferredWidth(520);
    tabelaNotas.getColumnModel().getColumn(2).setPreferredWidth(170);
  }

  private void salvarNotas() {

    String atividade = txtAtividade.getText().trim();

    if (atividade.isEmpty()) {
      JOptionPane.showMessageDialog(
          this,
          "Informe a atividade antes de salvar.",
          "Atividade obrigatória",
          JOptionPane.WARNING_MESSAGE);
      txtAtividade.requestFocus();
      return;
    }

    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
      Object nota = modeloTabela.getValueAt(i, 2);

      if (nota == null || nota.toString().trim().isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Preencha todas as notas antes de salvar.",
            "Notas incompletas",
            JOptionPane.WARNING_MESSAGE);
        tabelaNotas.requestFocus();
        tabelaNotas.changeSelection(i, 2, false, false);
        return;
      }
    }

    try {
      for (int i = 0; i < modeloTabela.getRowCount(); i++) {
        String matricula = String.valueOf(modeloTabela.getValueAt(i, 0));
        double nota =
            Double.parseDouble(String.valueOf(modeloTabela.getValueAt(i, 2)).replace(',', '.'));
        DadosSistema.salvarNota(matricula, atividade, nota);
      }

      JOptionPane.showMessageDialog(
          this,
          "Notas da atividade \"" + atividade + "\" salvas com sucesso!",
          "Sucesso",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          this, "Informe notas numéricas válidas.", "Notas inválidas", JOptionPane.WARNING_MESSAGE);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          this, mensagemErro(e), "Erro ao salvar notas", JOptionPane.ERROR_MESSAGE);
    }
  }

  private String mensagemErro(Throwable e) {
    Throwable atual = e;
    while (atual.getCause() != null) {
      atual = atual.getCause();
    }
    return atual.getMessage() == null ? e.getMessage() : atual.getMessage();
  }

  private void limparNotas() {
    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
      modeloTabela.setValueAt("", i, 2);
    }
  }

  private void adicionarLabel(JPanel painel, String texto, int x, int y) {
    JLabel label = new JLabel(texto);
    label.setForeground(corLabel);
    label.setFont(new Font("Segoe UI", Font.BOLD, 16));
    label.setBounds(x, y, 180, 25);
    painel.add(label);
  }

  private JTextField criarCampoTexto() {
    JTextField campo = new JTextField();
    campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    campo.setForeground(textos);
    campo.setCaretColor(textos);
    campo.setBackground(corCampo);
    campo.setBorder(new LineBorder(corBorda, 1, true));
    return campo;
  }

  private JComboBox<String> criarCombo(String[] itens) {
    JComboBox<String> combo = new JComboBox<>(itens);
    combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    combo.setForeground(textos);
    combo.setBackground(corCampo);
    combo.setBorder(new LineBorder(corBorda, 1, true));
    combo.setFocusable(false);
    return combo;
  }

  private void adicionarResumo(JPanel painel, String titulo, String valor, int y) {
    JPanel card = criarPainelArredondado(corCampo, corBorda, 20);
    card.setLayout(null);
    card.setBounds(25, y, 210, 60);

    JLabel lbTitulo = new JLabel(titulo);
    lbTitulo.setForeground(textos);
    lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    lbTitulo.setBounds(15, 8, 150, 20);
    card.add(lbTitulo);

    JLabel lbValor = new JLabel(valor);
    lbValor.setForeground(corLabel);
    lbValor.setFont(new Font("Segoe UI", Font.BOLD, 25));
    lbValor.setBounds(15, 28, 150, 28);
    card.add(lbValor);

    painel.add(card);
  }

  private void estilizarBotao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 15));
    botao.setForeground(textos);
    botao.setBackground(corCampo);
    botao.setBorder(new LineBorder(corBorda, 1, true));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  private JPanel criarPainelArredondado(Color fundo, Color borda, int raio) {
    return new JPanel() {

      private static final long serialVersionUID = 1L;

      {
        setOpaque(false);
        setBackground(fundo);
      }

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);

        g2.setColor(borda);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);

        g2.dispose();
        super.paintComponent(g);
      }
    };
  }
}
