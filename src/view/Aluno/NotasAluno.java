package view.Aluno;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import util.DadosSistema;

public class NotasAluno extends JFrame {

  private static final long serialVersionUID = 1L;

  // CORES
  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  // COMPONENTES
  private JTable tabelaNotas;
  private DefaultTableModel modeloTabela;
  private JComboBox<String> comboTrimestre;

  // CONSTRUTOR
  public NotasAluno() {
    setTitle("Minhas Notas");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Notas Aluno.png"));
    // ABRIR EM TELA CHEIA
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    // DESABILITA MAXIMIZAR
    setResizable(false);
    JPanel fundo = new JPanel(new BorderLayout());
    fundo.setBackground(corExterna);
    fundo.setBorder(new EmptyBorder(25, 30, 25, 30));
    setContentPane(fundo);
    fundo.add(criarCabecalho(), BorderLayout.NORTH);
    fundo.add(criarConteudo(), BorderLayout.CENTER);
  }

  // CABEÇALHO
  private JPanel criarCabecalho() {
    JPanel painel = new JPanel(new BorderLayout());
    painel.setOpaque(false);
    painel.setBorder(new EmptyBorder(0, 0, 25, 0));

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setFocusPainted(false);
    btnVoltar.setBorderPainted(false);
    btnVoltar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnVoltar.setForeground(textos);
    btnVoltar.setBackground(corCampo);
    btnVoltar.setFont(new Font("Segoe UI", Font.BOLD, 15));
    btnVoltar.setPreferredSize(new Dimension(130, 42));
    btnVoltar.setBorder(new LineBorder(corBorda));
    btnVoltar.addActionListener(e -> dispose());

    // TÍTULOS
    JLabel titulo = new JLabel("MINHAS NOTAS");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));

    JLabel subtitulo = new JLabel("Visualize todas as notas lançadas pelos professores");
    subtitulo.setForeground(corLabel);
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    JPanel textos = new JPanel(new GridLayout(2, 1));
    textos.setOpaque(false);
    textos.add(titulo);
    textos.add(subtitulo);

    // PAINEL ESQUERDO
    JPanel painelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
    painelEsquerdo.setOpaque(false);
    painelEsquerdo.add(btnVoltar);
    painelEsquerdo.add(textos);

    // CARD ALUNO
    JPanel cardAluno = new JPanel(new GridLayout(2, 2, 20, 5));
    cardAluno.setBackground(corCampo);
    cardAluno.setBorder(
        new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(15, 20, 15, 20)));
    cardAluno.add(criarInfo("Aluno", DadosSistema.nomeAlunoAtual()));
    cardAluno.add(criarInfo("Turma", DadosSistema.turmaAlunoAtual()));
    cardAluno.add(criarInfo("Curso", "Ensino Médio"));
    cardAluno.add(criarInfo("Ano", String.valueOf(java.time.Year.now().getValue())));
    painel.add(painelEsquerdo, BorderLayout.WEST);
    painel.add(cardAluno, BorderLayout.EAST);
    return painel;
  }

  // INFO
  private JPanel criarInfo(String titulo, String valor) {
    JPanel painel = new JPanel(new BorderLayout());
    painel.setOpaque(false);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(textos);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 15));

    painel.add(lblTitulo, BorderLayout.NORTH);
    painel.add(lblValor, BorderLayout.CENTER);
    return painel;
  }

  // CONTEÚDO
  private JPanel criarConteudo() {
    JPanel painel = new JPanel(new BorderLayout());
    painel.setBackground(corInterna);
    painel.setBorder(
        new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(22, 25, 25, 25)));

    // TOPO
    JPanel topoTabela = new JPanel(new BorderLayout());
    topoTabela.setOpaque(false);
    topoTabela.setBorder(new EmptyBorder(0, 0, 18, 0));

    JLabel tituloTabela = new JLabel("Notas lançadas");
    tituloTabela.setForeground(textos);
    tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 22));

    // FILTRO
    JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    painelFiltro.setOpaque(false);

    JLabel lblFiltro = new JLabel("Trimestre:");
    lblFiltro.setForeground(corLabel);
    lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));

    comboTrimestre =
        new JComboBox<>(
            new String[] {"Todos os trimestres", "1º Trimestre", "2º Trimestre", "3º Trimestre"});
    comboTrimestre.setFont(new Font("Segoe UI", Font.BOLD, 14));
    comboTrimestre.setForeground(textos);
    comboTrimestre.setBackground(new Color(55, 15, 130));
    comboTrimestre.setFocusable(false);
    comboTrimestre.setPreferredSize(new Dimension(210, 38));
    comboTrimestre.addActionListener(e -> carregarTabela(comboTrimestre.getSelectedIndex()));

    painelFiltro.add(lblFiltro);
    painelFiltro.add(comboTrimestre);
    topoTabela.add(tituloTabela, BorderLayout.WEST);
    topoTabela.add(painelFiltro, BorderLayout.EAST);

    // TABELA
    criarTabela();

    JScrollPane scroll = new JScrollPane(tabelaNotas);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getViewport().setBackground(corInterna);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    scroll.setBackground(corInterna);

    painel.add(topoTabela, BorderLayout.NORTH);
    painel.add(scroll, BorderLayout.CENTER);

    carregarTabela(0);

    return painel;
  }

  // TABELA
  private void criarTabela() {
    modeloTabela =
        new DefaultTableModel() {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    tabelaNotas = new JTable(modeloTabela);
    tabelaNotas.setRowHeight(48);
    tabelaNotas.setFont(new Font("Segoe UI", Font.BOLD, 15));
    tabelaNotas.setForeground(textos);
    tabelaNotas.setBackground(corInterna);
    tabelaNotas.setSelectionBackground(new Color(85, 35, 160));
    tabelaNotas.setSelectionForeground(textos);
    tabelaNotas.setGridColor(new Color(90, 45, 170));
    tabelaNotas.setShowVerticalLines(false);
    tabelaNotas.setShowHorizontalLines(true);
    tabelaNotas.setIntercellSpacing(new Dimension(0, 1));

    JTableHeader header = tabelaNotas.getTableHeader();
    header.setPreferredSize(new Dimension(0, 48));
    header.setBackground(new Color(60, 15, 135));
    header.setForeground(corLabel);
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);

    tabelaNotas.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {

          @Override
          public Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {

            JLabel cell =
                (JLabel)
                    super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
            cell.setOpaque(true);
            cell.setBorder(new EmptyBorder(0, 12, 0, 12));
            cell.setFont(new Font("Segoe UI", Font.BOLD, 15));

            if (isSelected) {
              cell.setBackground(new Color(95, 45, 180));
              cell.setForeground(textos);
            } else {
              cell.setBackground(row % 2 == 0 ? corInterna : corInterna);
              cell.setForeground(textos);
            }

            if (column == 0) {
              cell.setHorizontalAlignment(SwingConstants.LEFT);
              cell.setForeground(new Color(255, 210, 245));
            } else {
              cell.setHorizontalAlignment(SwingConstants.CENTER);
              cell.setForeground(corNota(value));
            }
            return cell;
          }
        });
  }

  // CARREGAR TABELA
  private void carregarTabela(int filtro) {
    modeloTabela.setRowCount(0);
    modeloTabela.setColumnCount(0);

    if (filtro == 0) {
      modeloTabela.addColumn("Disciplina");
      modeloTabela.addColumn("1º AT");
      modeloTabela.addColumn("1º PR");
      modeloTabela.addColumn("1º AT2");
      modeloTabela.addColumn("1º RP");
      modeloTabela.addColumn("2º AT");
      modeloTabela.addColumn("2º PR");
      modeloTabela.addColumn("2º AT2");
      modeloTabela.addColumn("2º RP");
      modeloTabela.addColumn("3º AT");
      modeloTabela.addColumn("3º PR");
      modeloTabela.addColumn("3º AT2");
      modeloTabela.addColumn("3º RP");

      for (Object[] linha : DadosSistema.notasAlunoAtualLinhas()) {
        modeloTabela.addRow(linha);
      }
    } else {
      modeloTabela.addColumn("Disciplina");
      modeloTabela.addColumn("AT");
      modeloTabela.addColumn("PR");
      modeloTabela.addColumn("AT2");
      modeloTabela.addColumn("RP");

      int inicio = 1;
      if (filtro == 1) {
        inicio = 1;
      } else if (filtro == 2) {
        inicio = 5;
      } else if (filtro == 3) {
        inicio = 9;
      }

      for (Object[] linha : DadosSistema.notasAlunoAtualLinhas()) {
        modeloTabela.addRow(
            new Object[] {
              linha[0], linha[inicio], linha[inicio + 1], linha[inicio + 2], linha[inicio + 3]
            });
      }
    }
    ajustarLarguras();
  }

  // AJUSTAR COLUNAS
  private void ajustarLarguras() {
    if (tabelaNotas.getColumnCount() == 0) {
      return;
    }

    tabelaNotas.getColumnModel().getColumn(0).setPreferredWidth(250);
    for (int i = 1; i < tabelaNotas.getColumnCount(); i++) {
      tabelaNotas.getColumnModel().getColumn(i).setPreferredWidth(75);
    }
  }

  // COR DAS NOTAS
  private Color corNota(Object valor) {
    if (valor == null) {
      return textos;
    }

    String texto = valor.toString().replace(",", ".");
    if (texto.equals("-") || texto.isEmpty()) {
      return new Color(170, 150, 210);
    }
    try {
      double nota = Double.parseDouble(texto);

      if (nota >= 7) {
        return new Color(120, 255, 170);
      } else if (nota >= 5) {
        return new Color(255, 220, 100);
      } else {
        return new Color(255, 120, 120);
      }
    } catch (Exception e) {
      return textos;
    }
  }
}
