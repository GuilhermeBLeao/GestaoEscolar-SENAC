package view.Professor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

public class ChamadaAluno extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabelaChamada;
  private DefaultTableModel modeloTabela;

  private JLabel lblTotalAlunos;
  private JLabel lblPresentes;
  private JLabel lblFaltas;
  private JLabel lblJustificadas;
  private JLabel lblAbonadas;

  private JTextField txtData;

  public ChamadaAluno() {

    setTitle("Chamada de Alunos");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Chamada.png"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int margem = 30;
    int larguraInterno = areaUtil.width - 60;
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

    JLabel titulo = new JLabel("Chamada de Alunos");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
    titulo.setHorizontalAlignment(SwingConstants.CENTER);
    titulo.setBounds(0, 45, larguraInterno, 55);
    interno.add(titulo);

    JLabel subtitulo =
        new JLabel("Registre presença, faltas, justificativas e observações por aula.");
    subtitulo.setForeground(textos);
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
    subtitulo.setBounds(0, 105, larguraInterno, 30);
    interno.add(subtitulo);

    int larguraResumo = 300;
    int margemLateral = 40;
    int larguraConteudo = larguraInterno - larguraResumo - (margemLateral * 3);

    JPanel filtros = criarPainelArredondado(corCampo, corBorda, 24);
    filtros.setLayout(null);
    filtros.setBounds(margemLateral, 160, larguraConteudo, 120);
    interno.add(filtros);

    int larguraConteudoFiltros = 1050;
    int inicioX = Math.max(30, (filtros.getWidth() - larguraConteudoFiltros) / 2);

    adicionarLabel(filtros, "Turma", inicioX, 20);
    JComboBox<String> cbTurma = criarCombo(new String[] {"ADS-1", "ADS-2", "ADS-3", "ADS-4"});
    cbTurma.setBounds(inicioX, 55, 170, 38);
    filtros.add(cbTurma);

    adicionarLabel(filtros, "Disciplina", inicioX + 195, 20);
    JComboBox<String> cbDisciplina =
        criarCombo(
            new String[] {
              "Matemática Aplicada",
              "Programação Orientada a Objetos",
              "Banco de Dados",
              "Engenharia de Software"
            });
    cbDisciplina.setBounds(inicioX + 195, 55, 280, 38);
    filtros.add(cbDisciplina);

    adicionarLabel(filtros, "Data", inicioX + 500, 20);
    txtData = criarCampoTexto();
    txtData.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
    txtData.setBounds(inicioX + 500, 55, 150, 38);
    filtros.add(txtData);

    adicionarLabel(filtros, "Aula/Horário", inicioX + 675, 20);
    JComboBox<String> cbHorario =
        criarCombo(
            new String[] {
              "08:00 - 09:40", "10:00 - 11:40", "14:00 - 15:40", "16:00 - 17:40", "19:00 - 20:40"
            });
    cbHorario.setBounds(inicioX + 675, 55, 180, 38);
    filtros.add(cbHorario);

    JButton btnCarregar = new JButton("Carregar");
    btnCarregar.setBounds(inicioX + 880, 55, 140, 38);
    estilizarBotao(btnCarregar);
    filtros.add(btnCarregar);

    JPanel tabelaPainel = criarPainelArredondado(corCampo, corBorda, 24);
    tabelaPainel.setLayout(null);
    tabelaPainel.setBounds(margemLateral, 305, larguraConteudo, alturaInterno - 365);
    interno.add(tabelaPainel);

    JLabel tituloTabela = new JLabel("Lista de Presença");
    tituloTabela.setForeground(textos);
    tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 26));
    tituloTabela.setBounds(30, 20, 400, 35);
    tabelaPainel.add(tituloTabela);

    criarTabela();

    int alturaTabelaReal =
        tabelaChamada.getTableHeader().getPreferredSize().height
            + (modeloTabela.getRowCount() * tabelaChamada.getRowHeight());

    JScrollPane scroll = new JScrollPane(tabelaChamada);

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

    int larguraBotao = 170;
    int alturaBotao = 42;
    int espacamento = 20;
    int totalBotoes = 5;

    int larguraTotal = (larguraBotao * totalBotoes) + (espacamento * (totalBotoes - 1));
    int inicioBotoes = (tabelaPainel.getWidth() - larguraTotal) / 2;
    int yBotoes = scroll.getY() + scroll.getHeight() + 30;

    JButton btnTodosPresentes = new JButton("Todos presentes");
    btnTodosPresentes.setBounds(inicioBotoes, yBotoes, larguraBotao, alturaBotao);
    estilizarBotao(btnTodosPresentes);
    tabelaPainel.add(btnTodosPresentes);

    JButton btnTodosFaltas = new JButton("Todos faltas");
    btnTodosFaltas.setBounds(
        inicioBotoes + (larguraBotao + espacamento), yBotoes, larguraBotao, alturaBotao);
    estilizarBotao(btnTodosFaltas);
    tabelaPainel.add(btnTodosFaltas);

    JButton btnSalvar = new JButton("Salvar chamada");
    btnSalvar.setBounds(
        inicioBotoes + ((larguraBotao + espacamento) * 2), yBotoes, larguraBotao, alturaBotao);
    estilizarBotao(btnSalvar);
    tabelaPainel.add(btnSalvar);

    JButton btnLimpar = new JButton("Limpar");
    btnLimpar.setBounds(
        inicioBotoes + ((larguraBotao + espacamento) * 3), yBotoes, larguraBotao, alturaBotao);
    estilizarBotao(btnLimpar);
    tabelaPainel.add(btnLimpar);

    JButton btnCancelar = new JButton("Cancelar");
    btnCancelar.setBounds(
        inicioBotoes + ((larguraBotao + espacamento) * 4), yBotoes, larguraBotao, alturaBotao);
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
    tituloResumo.setBounds(25, 25, 230, 35);
    resumo.add(tituloResumo);

    JPanel linha = new JPanel();
    linha.setBackground(corLabel);
    linha.setBounds(25, 70, 230, 3);
    resumo.add(linha);

    lblTotalAlunos =
        adicionarResumo(resumo, "Alunos", String.valueOf(modeloTabela.getRowCount()), 105);
    lblPresentes = adicionarResumo(resumo, "Presentes", "0", 180);
    lblFaltas = adicionarResumo(resumo, "Faltas", "0", 255);
    lblJustificadas = adicionarResumo(resumo, "Justificadas", "0", 330);
    lblAbonadas = adicionarResumo(resumo, "Abonadas", "0", 405);

    JLabel dica =
        new JLabel(
            "<html>Marque presença ou falta para cada aluno. Use justificativa quando houver motivo"
                + " informado.</html>");
    dica.setForeground(textos);
    dica.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    dica.setBounds(25, 480, 235, 110);
    resumo.add(dica);

    btnTodosPresentes.addActionListener(e -> marcarTodosPresentes());
    btnTodosFaltas.addActionListener(e -> marcarTodosFaltas());
    btnLimpar.addActionListener(e -> limparChamada());
    btnCancelar.addActionListener(e -> dispose());

    btnSalvar.addActionListener(
        e -> {
          atualizarResumo();
          salvarChamada();
        });

    atualizarResumo();

    setVisible(true);
  }

  private void criarTabela() {

    String[] colunas = {
      "Matrícula", "Aluno", "Presente", "Falta", "Justificada", "Abonada", "Motivo Falta Abonada"
    };

    Object[][] dados = DadosSistema.chamadaAlunosLinhas().toArray(new Object[0][]);

    modeloTabela =
        new DefaultTableModel(dados, colunas) {

          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return column >= 2;
          }

          @Override
          public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex >= 2 && columnIndex <= 5) {
              return Boolean.class;
            }
            return String.class;
          }

          @Override
          public void setValueAt(Object aValue, int row, int column) {

            super.setValueAt(aValue, row, column);

            if (column == 2 && Boolean.TRUE.equals(aValue)) {
              super.setValueAt(false, row, 3);
              super.setValueAt(false, row, 4);
              super.setValueAt(false, row, 5);
              super.setValueAt("", row, 6);
            }

            if (column == 3 && Boolean.TRUE.equals(aValue)) {
              super.setValueAt(false, row, 2);
              super.setValueAt(false, row, 5);
            }

            if (column == 4 && Boolean.TRUE.equals(aValue)) {
              super.setValueAt(true, row, 3);
              super.setValueAt(false, row, 2);
              super.setValueAt(false, row, 5);
            }

            if (column == 5 && Boolean.TRUE.equals(aValue)) {
              super.setValueAt(false, row, 2);
              super.setValueAt(false, row, 3);
              super.setValueAt(false, row, 4);
            }

            atualizarResumo();
          }
        };

    tabelaChamada = new JTable(modeloTabela);
    tabelaChamada.setRowHeight(50);
    tabelaChamada.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    tabelaChamada.setForeground(textos);
    tabelaChamada.setBackground(corInterna);
    tabelaChamada.setGridColor(new Color(90, 50, 170));
    tabelaChamada.setSelectionBackground(new Color(80, 40, 160));
    tabelaChamada.setSelectionForeground(textos);
    tabelaChamada.setShowGrid(true);
    tabelaChamada.setShowHorizontalLines(true);
    tabelaChamada.setShowVerticalLines(true);
    tabelaChamada.setIntercellSpacing(new Dimension(1, 1));
    tabelaChamada.setFillsViewportHeight(false);
    tabelaChamada.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    JTableHeader header = tabelaChamada.getTableHeader();
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
    centro.setBackground(new Color(31, 10, 90));
    centro.setForeground(textos);
    centro.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    tabelaChamada.getColumnModel().getColumn(0).setCellRenderer(centro);
    tabelaChamada.getColumnModel().getColumn(1).setCellRenderer(centro);
    tabelaChamada.getColumnModel().getColumn(6).setCellRenderer(centro);

    DefaultTableCellRenderer rendererBoolean =
        new DefaultTableCellRenderer() {

          private static final long serialVersionUID = 1L;

          @Override
          public Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {

            JCheckBox check = new JCheckBox();
            check.setHorizontalAlignment(SwingConstants.CENTER);
            check.setSelected(Boolean.TRUE.equals(value));
            check.setBackground(isSelected ? new Color(80, 40, 160) : new Color(31, 10, 90));
            check.setForeground(textos);
            check.setFocusPainted(false);

            return check;
          }
        };

    for (int i = 2; i <= 5; i++) {
      tabelaChamada.getColumnModel().getColumn(i).setCellRenderer(rendererBoolean);
    }

    JTextField campoMotivo = new JTextField();
    campoMotivo.setHorizontalAlignment(JTextField.CENTER);
    campoMotivo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    campoMotivo.setForeground(textos);
    campoMotivo.setCaretColor(textos);
    campoMotivo.setBackground(new Color(31, 10, 90));
    campoMotivo.setBorder(new LineBorder(corLabel, 1, true));

    tabelaChamada.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(campoMotivo));

    tabelaChamada.getColumnModel().getColumn(0).setPreferredWidth(120);
    tabelaChamada.getColumnModel().getColumn(1).setPreferredWidth(280);
    tabelaChamada.getColumnModel().getColumn(2).setPreferredWidth(100);
    tabelaChamada.getColumnModel().getColumn(3).setPreferredWidth(90);
    tabelaChamada.getColumnModel().getColumn(4).setPreferredWidth(110);
    tabelaChamada.getColumnModel().getColumn(5).setPreferredWidth(100);
    tabelaChamada.getColumnModel().getColumn(6).setPreferredWidth(340);
  }

  private void marcarTodosPresentes() {

    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
      modeloTabela.setValueAt(true, i, 2);
      modeloTabela.setValueAt(false, i, 3);
      modeloTabela.setValueAt(false, i, 4);
      modeloTabela.setValueAt(false, i, 5);
      modeloTabela.setValueAt("", i, 6);
    }

    atualizarResumo();
  }

  private void marcarTodosFaltas() {

    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
      modeloTabela.setValueAt(false, i, 2);
      modeloTabela.setValueAt(true, i, 3);
      modeloTabela.setValueAt(false, i, 5);
    }

    atualizarResumo();
  }

  private void limparChamada() {
    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
      modeloTabela.setValueAt(false, i, 2);
      modeloTabela.setValueAt(false, i, 3);
      modeloTabela.setValueAt(false, i, 4);
      modeloTabela.setValueAt(false, i, 5);
      modeloTabela.setValueAt("", i, 6);
    }

    atualizarResumo();
  }

  private void atualizarResumo() {

    if (modeloTabela == null || lblTotalAlunos == null) {
      return;
    }

    int total = modeloTabela.getRowCount();
    int presentes = 0;
    int faltas = 0;
    int justificadas = 0;
    int abonadas = 0;

    for (int i = 0; i < total; i++) {

      if (Boolean.TRUE.equals(modeloTabela.getValueAt(i, 2))) {
        presentes++;
      }

      if (Boolean.TRUE.equals(modeloTabela.getValueAt(i, 3))) {
        faltas++;
      }

      if (Boolean.TRUE.equals(modeloTabela.getValueAt(i, 4))) {
        justificadas++;
      }

      if (Boolean.TRUE.equals(modeloTabela.getValueAt(i, 5))) {
        abonadas++;
      }
    }

    lblTotalAlunos.setText(String.valueOf(total));
    lblPresentes.setText(String.valueOf(presentes));
    lblFaltas.setText(String.valueOf(faltas));
    lblJustificadas.setText(String.valueOf(justificadas));
    lblAbonadas.setText(String.valueOf(abonadas));
  }

  private void salvarChamada() {
    try {
      for (int i = 0; i < modeloTabela.getRowCount(); i++) {
        String matricula = String.valueOf(modeloTabela.getValueAt(i, 0));
        boolean presente = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 2));
        boolean falta = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 3));
        boolean justificada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 4));
        boolean abonada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 5));
        String motivo = String.valueOf(modeloTabela.getValueAt(i, 6));

        if (!presente && !falta && !justificada && !abonada) {
          continue;
        }

        DadosSistema.salvarPresenca(matricula, presente, justificada, abonada, motivo);
      }

      JOptionPane.showMessageDialog(
          this, "Chamada salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          this, mensagemErro(e), "Erro ao salvar chamada", JOptionPane.ERROR_MESSAGE);
    }
  }

  private String mensagemErro(Throwable e) {
    Throwable atual = e;
    while (atual.getCause() != null) {
      atual = atual.getCause();
    }
    return atual.getMessage() == null ? e.getMessage() : atual.getMessage();
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
    campo.setBackground(corCampo);
    campo.setCaretColor(textos);
    campo.setBorder(
        BorderFactory.createCompoundBorder(
            new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));

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

  private JLabel adicionarResumo(JPanel painel, String titulo, String valor, int y) {

    JPanel card = criarPainelArredondado(corCampo, corBorda, 20);
    card.setLayout(null);
    card.setBounds(25, y, 240, 55);

    JLabel lbTitulo = new JLabel(titulo);
    lbTitulo.setForeground(textos);
    lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    lbTitulo.setBounds(15, 8, 150, 18);
    card.add(lbTitulo);

    JLabel lbValor = new JLabel(valor);
    lbValor.setForeground(corLabel);
    lbValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lbValor.setBounds(15, 25, 150, 25);
    card.add(lbValor);

    painel.add(card);

    return lbValor;
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
