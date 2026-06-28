package view.Secretaria;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class Boletim extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JComboBox<String> cbAluno;
  private JTabbedPane abasNotas;

  private JLabel lblAluno,
      lblTurma,
      lblCurso,
      lblTurno,
      lblUnidade,
      lblMunicipio,
      lblMedia,
      lblFaltas,
      lblSituacao;

  private String alunoAtual = "PEDRO HENRIQUE MENDES DOS SANTOS";
  private String mediaGeralAtual = "8,0";
  private String totalFaltasAtual = "53";
  private String situacaoAtual = "Aprovado(a)";

  private BaseFont fonteNormalPdf;
  private BaseFont fonteNegritoPdf;

  private final String[][] dadosBoletimAprovado = {
    {"BIL", "BIOLOGIA", "7,5", "3", "10,0", "0", "9,0", "2", "8,8", "", "8,8", "5"},
    {"MAT", "MATEMÁTICA", "8,5", "0", "8,5", "1", "5,0", "5", "7,3", "", "7,3", "6"},
    {"GEO", "GEOGRAFIA", "8,5", "0", "7,5", "2", "8,0", "5", "8,0", "", "8,0", "7"},
    {"HIS", "HISTÓRIA", "4,5", "0", "7,0", "0", "8,0", "4", "6,5", "", "6,5", "4"},
    {"EFI", "EDUCAÇÃO FÍSICA", "7,5", "0", "7,5", "1", "7,5", "2", "7,5", "", "7,5", "3"},
    {
      "LEE",
      "LÍNGUA ESTRANGEIRA - ESPANHOL",
      "9,5",
      "0",
      "10,0",
      "0",
      "8,0",
      "3",
      "9,2",
      "",
      "9,2",
      "3"
    },
    {
      "LPL",
      "LÍNGUA PORTUGUESA E LITERATURA",
      "8,0",
      "0",
      "8,5",
      "2",
      "7,5",
      "6",
      "8,0",
      "",
      "8,0",
      "8"
    },
    {"SOC", "SOCIOLOGIA", "7,0", "0", "7,0", "0", "9,0", "5", "7,7", "", "7,7", "5"},
    {"FIS", "FÍSICA", "9,0", "0", "9,5", "1", "8,5", "2", "9,0", "", "9,0", "3"},
    {"QUI", "QUÍMICA", "9,0", "0", "9,0", "0", "6,0", "1", "8,0", "", "8,0", "1"},
    {"FIL", "FILOSOFIA", "8,0", "0", "7,5", "0", "6,0", "5", "7,2", "", "7,2", "5"},
    {"ATE", "ARTE", "9,0", "0", "8,5", "0", "4,5", "3", "7,3", "", "7,3", "3"}
  };

  private final String[][] dadosBoletimSemSegundoTrimestre = {
    {"BIL", "BIOLOGIA", "7,5", "3", "", "", "9,0", "2", "8,2", "", "8,2", "5"},
    {"MAT", "MATEMÁTICA", "8,5", "0", "", "", "5,0", "5", "6,8", "", "6,8", "5"},
    {"GEO", "GEOGRAFIA", "8,5", "0", "", "", "8,0", "5", "8,2", "", "8,2", "5"},
    {"HIS", "HISTÓRIA", "4,5", "0", "", "", "8,0", "4", "6,2", "", "6,2", "4"},
    {"EFI", "EDUCAÇÃO FÍSICA", "7,5", "0", "", "", "7,5", "2", "7,5", "", "7,5", "2"},
    {"LEE", "LÍNGUA ESTRANGEIRA - ESPANHOL", "9,5", "0", "", "", "8,0", "3", "8,7", "", "8,7", "3"},
    {
      "LPL", "LÍNGUA PORTUGUESA E LITERATURA", "8,0", "0", "", "", "7,5", "6", "7,7", "", "7,7", "6"
    },
    {"SOC", "SOCIOLOGIA", "7,0", "0", "", "", "9,0", "5", "8,0", "", "8,0", "5"},
    {"FIS", "FÍSICA", "9,0", "0", "", "", "8,5", "2", "8,7", "", "8,7", "2"},
    {"QUI", "QUÍMICA", "9,0", "0", "", "", "6,0", "1", "7,5", "", "7,5", "1"},
    {"FIL", "FILOSOFIA", "8,0", "0", "", "", "6,0", "5", "7,0", "", "7,0", "5"},
    {"ATE", "ARTE", "9,0", "0", "", "", "4,5", "3", "6,7", "", "6,7", "3"}
  };

  private final String[][] dadosBoletimReprovado = {
    {"BIL", "BIOLOGIA", "4,0", "8", "5,0", "6", "4,5", "7", "4,5", "", "4,5", "21"},
    {"MAT", "MATEMÁTICA", "3,5", "10", "4,0", "8", "4,5", "9", "4,0", "", "4,0", "27"},
    {"GEO", "GEOGRAFIA", "5,0", "6", "5,5", "5", "4,0", "7", "4,8", "", "4,8", "18"},
    {"HIS", "HISTÓRIA", "4,5", "4", "5,0", "5", "5,0", "6", "4,8", "", "4,8", "15"},
    {"EFI", "EDUCAÇÃO FÍSICA", "6,0", "2", "5,5", "3", "5,0", "4", "5,5", "", "5,5", "9"},
    {
      "LEE",
      "LÍNGUA ESTRANGEIRA - ESPANHOL",
      "4,0",
      "7",
      "4,5",
      "7",
      "5,0",
      "6",
      "4,5",
      "",
      "4,5",
      "20"
    },
    {
      "LPL",
      "LÍNGUA PORTUGUESA E LITERATURA",
      "3,5",
      "8",
      "4,0",
      "9",
      "4,0",
      "7",
      "3,8",
      "",
      "3,8",
      "24"
    },
    {"SOC", "SOCIOLOGIA", "5,0", "5", "5,0", "4", "4,5", "5", "4,8", "", "4,8", "14"},
    {"FIS", "FÍSICA", "4,0", "7", "4,5", "6", "4,0", "8", "4,1", "", "4,1", "21"},
    {"QUI", "QUÍMICA", "3,5", "8", "4,0", "8", "4,5", "7", "4,0", "", "4,0", "23"},
    {"FIL", "FILOSOFIA", "5,0", "4", "5,0", "5", "4,0", "6", "4,6", "", "4,6", "15"},
    {"ATE", "ARTE", "6,0", "3", "5,5", "4", "5,0", "5", "5,5", "", "5,5", "12"}
  };

  private String[][] dadosBoletim = dadosBoletimAprovado;

  public Boletim() {
    setTitle("Boletim do Aluno");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Boletim.png"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int margem = 30;
    int larguraInterno = areaUtil.width - margem * 2 + 20;
    int alturaInterno = areaUtil.height - margem * 2;

    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(1200, 720));
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
    JLabel titulo = new JLabel("Boletim do Aluno");
    titulo.setForeground(textos);
    titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 36));
    titulo.setBounds(35, 35, 500, 45);
    interno.add(titulo);

    JLabel subtitulo =
        new JLabel("Visualize as notas por trimestre e exporte o boletim oficial em PDF.");
    subtitulo.setForeground(new Color(220, 210, 255));
    subtitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 17));
    subtitulo.setBounds(35, 80, 900, 25);
    interno.add(subtitulo);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(larguraInterno - 170, 35, 120, 38);
    estilizarBotaoAcao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    interno.add(btnVoltar);

    JPanel painelAcao = criarPainelArredondado(corCampo, corBorda, 24);
    painelAcao.setLayout(null);
    painelAcao.setBounds(35, 125, larguraInterno - 70, 100);
    interno.add(painelAcao);

    JLabel lbAluno = new JLabel("Selecionar aluno");
    lbAluno.setForeground(corLabel);
    lbAluno.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
    lbAluno.setBounds(30, 20, 200, 22);
    painelAcao.add(lbAluno);

    cbAluno =
        criarCombo(
            new String[] {
              "PEDRO HENRIQUE MENDES DOS SANTOS",
              "JOÃO SILVA",
              "MARIA OLIVEIRA",
              "ANA OLIVEIRA",
              "LUCAS ALMEIDA"
            });

    cbAluno.setBounds(30, 48, 520, 38);
    painelAcao.add(cbAluno);

    JButton btnAbrir = new JButton("Abrir boletim");
    btnAbrir.setBounds(575, 48, 170, 38);
    estilizarBotaoAcao(btnAbrir);
    painelAcao.add(btnAbrir);

    JButton btnExportar = new JButton("Exportar PDF");
    btnExportar.setBounds(765, 48, 170, 38);
    estilizarBotaoAcao(btnExportar);
    painelAcao.add(btnExportar);

    JPanel painelDados = criarPainelArredondado(corCampo, corBorda, 24);
    painelDados.setLayout(null);
    painelDados.setBounds(35, 245, larguraInterno - 70, 135);
    interno.add(painelDados);

    lblAluno = criarRotuloInfo("Aluno", alunoAtual, 30, 20, 390);
    lblTurma = criarRotuloInfo("Turma", "202", 425, 20, 140);
    lblCurso = criarRotuloInfo("Curso", "ENSINO MÉDIO", 565, 20, 210);
    lblTurno = criarRotuloInfo("Turno", "MATUTINO", 775, 20, 180);
    lblUnidade = criarRotuloInfo("Unidade Escolar", "E.E.B SOLO FIRME", 30, 78, 300);
    lblMunicipio = criarRotuloInfo("Município", "8265 - PORTO BELO", 330, 78, 260);
    lblMedia = criarRotuloInfo("Média Geral", mediaGeralAtual, 590, 78, 150);
    lblFaltas = criarRotuloInfo("Total de Faltas", totalFaltasAtual, 740, 78, 150);
    lblSituacao = criarRotuloInfo("Situação", situacaoAtual, 890, 78, 200);

    painelDados.add(lblAluno);
    painelDados.add(lblTurma);
    painelDados.add(lblCurso);
    painelDados.add(lblTurno);
    painelDados.add(lblUnidade);
    painelDados.add(lblMunicipio);
    painelDados.add(lblMedia);
    painelDados.add(lblFaltas);
    painelDados.add(lblSituacao);

    JPanel painelNotas = criarPainelArredondado(corCampo, corBorda, 24);
    painelNotas.setLayout(null);
    painelNotas.setBounds(35, 405, larguraInterno - 70, alturaInterno - 435);
    interno.add(painelNotas);

    JLabel lbNotas = new JLabel("Notas por Trimestre");
    lbNotas.setForeground(corLabel);
    lbNotas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 17));
    lbNotas.setBounds(25, 15, 300, 25);
    painelNotas.add(lbNotas);

    abasNotas = criarAbasNotas();
    abasNotas.setBounds(25, 50, 1760, 467);
    painelNotas.add(abasNotas);

    btnAbrir.addActionListener(e -> carregarAluno());
    btnExportar.addActionListener(e -> exportarPdf());
  }

  private JTabbedPane criarAbasNotas() {
    JTabbedPane abas = new JTabbedPane();
    abas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
    abas.setForeground(textos);
    abas.setBackground(corInterna);

    abas.addTab("1º Trimestre", criarScrollTabela(criarTabelaNotas(1)));
    abas.addTab("2º Trimestre", criarScrollTabela(criarTabelaNotas(2)));
    abas.addTab("3º Trimestre", criarScrollTabela(criarTabelaNotas(3)));

    return abas;
  }

  private JTable criarTabelaNotas(int trimestre) {
    String[] colunas = {
      "Cód.", "Disciplina", "Nota", "Faltas", "Média", "Média Final", "Total Faltas", "Situação"
    };

    Object[][] dados = new Object[dadosBoletim.length][8];

    for (int i = 0; i < dadosBoletim.length; i++) {
      String nota = "";
      String faltas = "";

      if (trimestre == 1) {
        nota = dadosBoletim[i][2];
        faltas = dadosBoletim[i][3];
      } else if (trimestre == 2) {
        nota = dadosBoletim[i][4];
        faltas = dadosBoletim[i][5];
      } else {
        nota = dadosBoletim[i][6];
        faltas = dadosBoletim[i][7];
      }

      dados[i][0] = dadosBoletim[i][0];
      dados[i][1] = abreviarDisciplina(dadosBoletim[i][1]);
      dados[i][2] = valorOuVazio(nota);
      dados[i][3] = valorOuVazio(faltas);
      dados[i][4] = dadosBoletim[i][8];
      dados[i][5] = dadosBoletim[i][10];
      dados[i][6] = dadosBoletim[i][11];
      dados[i][7] = situacaoAtual;
    }

    DefaultTableModel modelo =
        new DefaultTableModel(dados, colunas) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    JTable tabela = new JTable(modelo);
    tabela.setRowHeight(34);
    tabela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
    tabela.setForeground(textos);
    tabela.setBackground(new Color(31, 10, 90));
    tabela.setGridColor(new Color(90, 50, 170));
    tabela.setSelectionBackground(new Color(80, 40, 160));
    tabela.setSelectionForeground(textos);
    tabela.setShowGrid(true);
    tabela.setFillsViewportHeight(true);

    JTableHeader header = tabela.getTableHeader();
    header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
    header.setBackground(new Color(45, 15, 120));
    header.setForeground(textos);
    header.setReorderingAllowed(false);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);
    centro.setBackground(new Color(31, 10, 90));
    centro.setForeground(textos);

    for (int i = 0; i < tabela.getColumnCount(); i++) {
      tabela.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    tabela.getColumnModel().getColumn(0).setPreferredWidth(60);
    tabela.getColumnModel().getColumn(1).setPreferredWidth(260);
    tabela.getColumnModel().getColumn(7).setPreferredWidth(130);

    return tabela;
  }

  private JScrollPane criarScrollTabela(JTable tabela) {
    JScrollPane scroll = new JScrollPane(tabela);
    scroll.getViewport().setBackground(corCampo);
    scroll.setBorder(new LineBorder(corBorda, 1, true));
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    return scroll;
  }

  private void carregarAluno() {
    alunoAtual = cbAluno.getSelectedItem().toString();

    if (alunoAtual.equals("PEDRO HENRIQUE MENDES DOS SANTOS")) {
      dadosBoletim = dadosBoletimAprovado;
      mediaGeralAtual = "8,0";
      totalFaltasAtual = "53";
      situacaoAtual = "Aprovado(a)";
    } else if (alunoAtual.equals("JOÃO SILVA")) {
      dadosBoletim = dadosBoletimSemSegundoTrimestre;
      mediaGeralAtual = "7,5";
      totalFaltasAtual = "46";
      situacaoAtual = "Em andamento";
    } else if (alunoAtual.equals("MARIA OLIVEIRA")) {
      dadosBoletim = dadosBoletimReprovado;
      mediaGeralAtual = "4,6";
      totalFaltasAtual = "219";
      situacaoAtual = "Reprovado(a)";
    } else {
      dadosBoletim = dadosBoletimAprovado;
      mediaGeralAtual = "8,0";
      totalFaltasAtual = "53";
      situacaoAtual = "Aprovado(a)";
    }

    lblAluno.setText("<html><b style='color:#ff78dc'>Aluno</b><br>" + alunoAtual + "</html>");
    lblMedia.setText(
        "<html><b style='color:#ff78dc'>Média Geral</b><br>" + mediaGeralAtual + "</html>");
    lblFaltas.setText(
        "<html><b style='color:#ff78dc'>Total de Faltas</b><br>" + totalFaltasAtual + "</html>");

    String corSituacao =
        situacaoAtual.contains("Reprovado")
            ? "#ff4b4b"
            : situacaoAtual.contains("andamento") ? "#ffd84b" : "#3cff76";

    lblSituacao.setText(
        "<html><b style='color:#ff78dc'>Situação</b><br><span style='color:"
            + corSituacao
            + "'>"
            + situacaoAtual
            + "</span></html>");

    atualizarTabelaNotas();
  }

  private void atualizarTabelaNotas() {
    if (abasNotas == null) {
      return;
    }

    abasNotas.removeAll();
    abasNotas.addTab("1º Trimestre", criarScrollTabela(criarTabelaNotas(1)));
    abasNotas.addTab("2º Trimestre", criarScrollTabela(criarTabelaNotas(2)));
    abasNotas.addTab("3º Trimestre", criarScrollTabela(criarTabelaNotas(3)));
    abasNotas.revalidate();
    abasNotas.repaint();
  }

  private void exportarPdf() {
    String nomeArquivo = "boletim(" + limparNomeArquivo(alunoAtual) + ").pdf";
    File pasta = new File("Boletins");

    if (!pasta.exists()) {
      pasta.mkdirs();
    }

    File arquivo = new File(pasta, nomeArquivo);

    try {
      Document document = new Document(PageSize.A4.rotate(), 25, 25, 25, 25);
      PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivo));
      document.open();

      fonteNormalPdf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
      fonteNegritoPdf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);

      PdfContentByte cb = writer.getDirectContent();

      desenharBoletimPdf(cb, document.getPageSize().getWidth(), document.getPageSize().getHeight());

      document.close();

      JOptionPane.showMessageDialog(
          this,
          "Boletim exportado com sucesso!\n\nLocal: " + arquivo.getAbsolutePath(),
          "PDF Gerado",
          JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          this, "Erro ao gerar PDF:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void desenharBoletimPdf(PdfContentByte cb, float pageW, float pageH) throws Exception {
    Color azul = new Color(63, 10, 117);
    Color grade = new Color(120, 116, 165);
    Color fundoAlternado = new Color(247, 247, 252);

    float margem = 18;
    float x = margem;
    float y = margem;
    float w = pageW - margem * 2;
    float h = pageH - margem * 2;
    float top = y + h;

    cb.setColorStroke(azul);
    cb.setLineWidth(1.1f);
    cb.rectangle(x, y, w, h);
    cb.stroke();

    float headerH = 122;
    float headerBottom = top - headerH;
    linha(cb, x, headerBottom, x + w, headerBottom);

    File logoFile = new File("resources/Images/Solo Firme.png");

    if (logoFile.exists()) {
      com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoFile.getPath());
      logo.scaleToFit(150, 120);
      logo.setAbsolutePosition(x + 5, top - 110);
      cb.addImage(logo);
    }

    escrever(cb, "E.E.B Solo Firme", x + 180, top - 40, 24, true, Element.ALIGN_LEFT);
    escrever(cb, "S.E.D Santa Catarina", x + 190, top - 66, 15, true, Element.ALIGN_LEFT);

    escreverCor(
        cb,
        "Solo Firme: A Base para Salto do Seu Filho.",
        x + 270,
        top - 95,
        12,
        true,
        Element.ALIGN_CENTER,
        corLabel);

    float divisorX = x + 400;
    linha(cb, divisorX, top - 14, divisorX, headerBottom + 15);

    float dadosX = divisorX + 25;
    float valorX = dadosX + 70;
    float dadosY = top - 32;

    escrever(cb, "ALUNO:", dadosX, dadosY, 10, true, Element.ALIGN_LEFT);
    escreverLimitado(cb, alunoAtual, valorX, dadosY, 10, false, Element.ALIGN_LEFT, 270);

    escrever(cb, "TURMA:", dadosX, dadosY - 26, 10, true, Element.ALIGN_LEFT);
    escrever(cb, "202", valorX, dadosY - 26, 10, false, Element.ALIGN_LEFT);

    escrever(cb, "TURNO:", dadosX, dadosY - 52, 10, true, Element.ALIGN_LEFT);
    escrever(cb, "Matutino", valorX, dadosY - 52, 10, false, Element.ALIGN_LEFT);

    escrever(cb, "MUNICÍPIO:", dadosX, dadosY - 78, 10, true, Element.ALIGN_LEFT);
    escrever(cb, "8265 - PORTO BELO", valorX, dadosY - 78, 10, false, Element.ALIGN_LEFT);

    float dados2X = x + w - 240;

    escrever(cb, "CURSO:", dados2X, dadosY - 26, 10, true, Element.ALIGN_LEFT);
    escrever(cb, "Ensino Médio", dados2X + 60, dadosY - 26, 10, false, Element.ALIGN_LEFT);

    escrever(cb, "UNIDADE:", dados2X, dadosY - 52, 10, true, Element.ALIGN_LEFT);
    escrever(cb, "E.E.B SOLO FIRME", dados2X + 60, dadosY - 52, 10, false, Element.ALIGN_LEFT);

    float tabelaTop = headerBottom - 12;
    float tabelaBottom =
        desenharTabelaPdf(cb, x + 7, tabelaTop, w - 14, azul, grade, fundoAlternado);

    float footerTop = tabelaBottom - 12;
    float footerH = 48;

    cb.setColorStroke(azul);
    cb.rectangle(x + 7, footerTop - footerH, w - 14, footerH);
    cb.stroke();

    escrever(cb, "SITUAÇÃO:", x + 22, footerTop - 31, 13, true, Element.ALIGN_LEFT);
    escrever(
        cb, situacaoAtual.toUpperCase(), x + 100, footerTop - 31, 14, true, Element.ALIGN_LEFT);

    float blocoX = x + w - 360;

    linha(cb, blocoX, footerTop - 8, blocoX, footerTop - footerH + 8);
    linha(cb, blocoX + 115, footerTop - 8, blocoX + 115, footerTop - footerH + 8);
    linha(cb, blocoX + 235, footerTop - 8, blocoX + 235, footerTop - footerH + 8);

    escrever(cb, "MÉDIA GERAL:", blocoX + 58, footerTop - 19, 9, true, Element.ALIGN_CENTER);
    escrever(cb, mediaGeralAtual, blocoX + 58, footerTop - 38, 16, true, Element.ALIGN_CENTER);

    escrever(cb, "TOTAL DE FALTAS:", blocoX + 175, footerTop - 19, 9, true, Element.ALIGN_CENTER);
    escrever(cb, totalFaltasAtual, blocoX + 175, footerTop - 38, 16, true, Element.ALIGN_CENTER);

    escrever(cb, "FREQUÊNCIA TOTAL:", blocoX + 295, footerTop - 19, 9, true, Element.ALIGN_CENTER);
    escrever(
        cb,
        calcularFrequenciaFinal(totalFaltasAtual, 2000),
        blocoX + 295,
        footerTop - 38,
        16,
        true,
        Element.ALIGN_CENTER);
  }

  private float desenharTabelaPdf(
      PdfContentByte cb,
      float x,
      float topY,
      float w,
      Color azul,
      Color grade,
      Color fundoAlternado)
      throws Exception {
    Color azulSubcabecalho = new Color(31, 18, 150);

    float headerGrupoH = 29;
    float headerSubH = 23;
    float headerH = headerGrupoH + headerSubH;
    float rowH = 19;

    float codW = 46;
    float discW = 120;
    float notaW = 44;
    float faltaW = 40;
    float freqW = 52;
    float ccW = 56;
    float mediaFinalW = 62;
    float totalFaltasW = 64;
    float freqFinalW = 72;

    float somaColunas =
        codW
            + discW
            + ((notaW + faltaW + freqW) * 3)
            + ccW
            + mediaFinalW
            + totalFaltasW
            + freqFinalW;

    discW += w - somaColunas;

    float y = topY - headerH;

    float c0 = x;
    float c1 = c0 + codW;
    float c2 = c1 + discW;
    float c3 = c2 + notaW;
    float c4 = c3 + faltaW;
    float c5 = c4 + freqW;
    float c6 = c5 + notaW;
    float c7 = c6 + faltaW;
    float c8 = c7 + freqW;
    float c9 = c8 + notaW;
    float c10 = c9 + faltaW;
    float c11 = c10 + freqW;
    float c12 = c11 + ccW;
    float c13 = c12 + mediaFinalW;
    float c14 = c13 + totalFaltasW;
    float c15 = c14 + freqFinalW;

    cb.setColorFill(azul);
    cb.rectangle(x, y, w, headerH);
    cb.fill();

    cb.setColorFill(azulSubcabecalho);
    cb.rectangle(c2, y, c11 - c2, headerSubH);
    cb.fill();

    cb.setColorStroke(grade);
    cb.rectangle(x, y, w, headerH);
    cb.stroke();

    float[] colunasPrincipais = {c1, c2, c5, c8, c11, c12, c13, c14};
    float[] colunasSubcabecalho = {c3, c4, c6, c7, c9, c10};
    float[] colunasDados = {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14};

    for (float coluna : colunasPrincipais) {
      linha(cb, coluna, y, coluna, y + headerH);
    }

    for (float coluna : colunasSubcabecalho) {
      linha(cb, coluna, y, coluna, y + headerSubH);
    }

    linha(cb, c2, y + headerSubH, c11, y + headerSubH);

    cb.setColorFill(textos);

    float grupoY = y + headerSubH + 10;
    float subY = y + 8;

    escrever(cb, "Cód.", c0 + codW / 2, grupoY, 9, true, Element.ALIGN_CENTER);
    escrever(cb, "Disciplina", c1 + 8, grupoY, 11, true, Element.ALIGN_LEFT);
    escrever(cb, "1º Trimestre", (c2 + c5) / 2, grupoY, 10, true, Element.ALIGN_CENTER);
    escrever(cb, "2º Trimestre", (c5 + c8) / 2, grupoY, 10, true, Element.ALIGN_CENTER);
    escrever(cb, "3º Trimestre", (c8 + c11) / 2, grupoY, 10, true, Element.ALIGN_CENTER);
    escrever(cb, "CC", (c11 + c12) / 2, grupoY, 10, true, Element.ALIGN_CENTER);

    escrever(cb, "Média", (c12 + c13) / 2, y + headerSubH + 17, 9, true, Element.ALIGN_CENTER);
    escrever(cb, "Final", (c12 + c13) / 2, y + headerSubH + 6, 9, true, Element.ALIGN_CENTER);

    escrever(cb, "Total", (c13 + c14) / 2, y + headerSubH + 18, 9, true, Element.ALIGN_CENTER);
    escrever(cb, "Faltas", (c13 + c14) / 2, y + headerSubH + 6, 9, true, Element.ALIGN_CENTER);

    escrever(
        cb, "Freq. Final", (c14 + c15) / 2, y + headerSubH + 18, 8, true, Element.ALIGN_CENTER);
    escrever(cb, "Disciplina", (c14 + c15) / 2, y + headerSubH + 6, 8, true, Element.ALIGN_CENTER);

    escreverSubcabecalho(cb, c2, c3, c4, subY, notaW, faltaW, freqW);
    escreverSubcabecalho(cb, c5, c6, c7, subY, notaW, faltaW, freqW);
    escreverSubcabecalho(cb, c8, c9, c10, subY, notaW, faltaW, freqW);

    cb.setColorFill(Color.BLACK);

    float rowY = y - rowH;

    for (int i = 0; i < dadosBoletim.length; i++) {
      float atualY = rowY - i * rowH;

      if (i % 2 == 1) {
        cb.setColorFill(fundoAlternado);
        cb.rectangle(x, atualY, w, rowH);
        cb.fill();
        cb.setColorFill(Color.BLACK);
      }

      cb.setColorStroke(grade);
      cb.rectangle(x, atualY, w, rowH);
      cb.stroke();

      for (float coluna : colunasDados) {
        linha(cb, coluna, atualY, coluna, atualY + rowH);
      }

      float textY = atualY + 6;

      String faltas1 = valorOuVazio(dadosBoletim[i][3]);
      String faltas2 = valorOuVazio(dadosBoletim[i][5]);
      String faltas3 = valorOuVazio(dadosBoletim[i][7]);
      String faltasFinal = valorOuVazio(dadosBoletim[i][11]);

      escrever(cb, dadosBoletim[i][0], c0 + codW / 2, textY, 8, true, Element.ALIGN_CENTER);

      escreverLimitado(
          cb,
          abreviarDisciplina(dadosBoletim[i][1]),
          c1 + 7,
          textY,
          8,
          false,
          Element.ALIGN_LEFT,
          discW - 12);

      escreverLinhaTrimestre(
          cb,
          c2,
          c3,
          c4,
          textY,
          notaW,
          faltaW,
          freqW,
          dadosBoletim[i][2],
          faltas1,
          calcularFrequencia(faltas1, 67));

      escreverLinhaTrimestre(
          cb,
          c5,
          c6,
          c7,
          textY,
          notaW,
          faltaW,
          freqW,
          dadosBoletim[i][4],
          faltas2,
          calcularFrequencia(faltas2, 67));

      escreverLinhaTrimestre(
          cb,
          c8,
          c9,
          c10,
          textY,
          notaW,
          faltaW,
          freqW,
          dadosBoletim[i][6],
          faltas3,
          calcularFrequencia(faltas3, 67));

      escrever(
          cb,
          valorOuTraco(dadosBoletim[i][9]),
          c11 + ccW / 2,
          textY,
          8,
          false,
          Element.ALIGN_CENTER);
      escrever(
          cb,
          valorOuVazio(dadosBoletim[i][10]),
          c12 + mediaFinalW / 2,
          textY,
          8,
          true,
          Element.ALIGN_CENTER);
      escrever(cb, faltasFinal, c13 + totalFaltasW / 2, textY, 8, true, Element.ALIGN_CENTER);
      escrever(
          cb,
          calcularFrequenciaFinal(faltasFinal, 200),
          c14 + freqFinalW / 2,
          textY,
          8,
          true,
          Element.ALIGN_CENTER);
    }

    return rowY - dadosBoletim.length * rowH;
  }

  private void escreverSubcabecalho(
      PdfContentByte cb,
      float notaX,
      float faltaX,
      float freqX,
      float y,
      float notaW,
      float faltaW,
      float freqW) {
    escrever(cb, "Nota", notaX + notaW / 2, y, 8, true, Element.ALIGN_CENTER);
    escrever(cb, "Falta", faltaX + faltaW / 2, y, 8, true, Element.ALIGN_CENTER);
    escrever(cb, "Freq.", freqX + freqW / 2, y, 8, true, Element.ALIGN_CENTER);
  }

  private void escreverLinhaTrimestre(
      PdfContentByte cb,
      float notaX,
      float faltaX,
      float freqX,
      float y,
      float notaW,
      float faltaW,
      float freqW,
      String nota,
      String faltas,
      String frequencia) {
    escrever(cb, valorOuVazio(nota), notaX + notaW / 2, y, 8, false, Element.ALIGN_CENTER);
    escrever(cb, valorOuVazio(faltas), faltaX + faltaW / 2, y, 8, false, Element.ALIGN_CENTER);
    escrever(cb, frequencia, freqX + freqW / 2, y, 8, false, Element.ALIGN_CENTER);
  }

  private String calcularFrequencia(String faltasTexto, int totalAulas) {
    String faltas = valorOuVazio(faltasTexto);

    if (faltas.isEmpty()) {
      return "-";
    }

    try {
      int quantidadeFaltas = Integer.parseInt(faltas);
      double frequencia = ((double) (totalAulas - quantidadeFaltas) / totalAulas) * 100;
      return String.format("%.1f%%", Math.max(0, frequencia)).replace('.', ',');
    } catch (NumberFormatException e) {
      return "-";
    }
  }

  private String calcularFrequenciaFinal(String faltasTexto, int totalAulas) {
    return calcularFrequencia(faltasTexto, totalAulas);
  }

  private String valorOuTraco(String valor) {
    String texto = valorOuVazio(valor);
    return texto.isEmpty() ? "-" : texto;
  }

  private void escrever(
      PdfContentByte cb,
      String texto,
      float x,
      float y,
      int tamanho,
      boolean negrito,
      int alinhamento) {
    try {
      cb.beginText();
      cb.setFontAndSize(negrito ? fonteNegritoPdf : fonteNormalPdf, tamanho);
      cb.showTextAligned(alinhamento, texto == null ? "" : texto, x, y, 0);
      cb.endText();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void escreverCor(
      PdfContentByte cb,
      String texto,
      float x,
      float y,
      int tamanho,
      boolean negrito,
      int alinhamento,
      Color cor) {
    try {
      cb.beginText();
      cb.setColorFill(cor);
      cb.setFontAndSize(negrito ? fonteNegritoPdf : fonteNormalPdf, tamanho);
      cb.showTextAligned(alinhamento, texto == null ? "" : texto, x, y, 0);
      cb.endText();
      cb.setColorFill(Color.BLACK);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void escreverLimitado(
      PdfContentByte cb,
      String texto,
      float x,
      float y,
      int tamanho,
      boolean negrito,
      int alinhamento,
      float larguraMaxima) {
    String textoAjustado =
        ajustarTextoPdf(texto, negrito ? fonteNegritoPdf : fonteNormalPdf, tamanho, larguraMaxima);
    escrever(cb, textoAjustado, x, y, tamanho, negrito, alinhamento);
  }

  private String ajustarTextoPdf(String texto, BaseFont fonte, int tamanho, float larguraMaxima) {
    if (texto == null) {
      return "";
    }

    String limpo = texto.trim();

    if (limpo.isEmpty()) {
      return "";
    }

    if (fonte.getWidthPoint(limpo, tamanho) <= larguraMaxima) {
      return limpo;
    }

    while (limpo.length() > 3 && fonte.getWidthPoint(limpo + "...", tamanho) > larguraMaxima) {
      limpo = limpo.substring(0, limpo.length() - 1);
    }

    return limpo.trim() + "...";
  }

  private void linha(PdfContentByte cb, float x1, float y1, float x2, float y2) {
    cb.moveTo(x1, y1);
    cb.lineTo(x2, y2);
    cb.stroke();
  }

  private JLabel criarRotuloInfo(String titulo, String valor, int x, int y, int largura) {
    JLabel label =
        new JLabel("<html><b style='color:#ff78dc'>" + titulo + "</b><br>" + valor + "</html>");
    label.setForeground(textos);
    label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
    label.setBounds(x, y, largura, 45);
    return label;
  }

  private JComboBox<String> criarCombo(String[] itens) {
    JComboBox<String> combo = new JComboBox<>(itens);
    combo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
    combo.setForeground(textos);
    combo.setBackground(new Color(38, 15, 110));
    combo.setBorder(new LineBorder(corBorda, 1, true));
    combo.setFocusable(false);
    return combo;
  }

  private void estilizarBotaoAcao(JButton botao) {
    botao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
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

  private String abreviarDisciplina(String nome) {
    if (nome == null) {
      return "";
    }

    return nome.replace("BIOLOGIA", "BIOLOGIA")
        .replace("MATEMÁTICA", "MATEMÁTICA")
        .replace("GEOGRAFIA", "GEOGRAFIA")
        .replace("HISTÓRIA", "HISTÓRIA")
        .replace("EDUCAÇÃO FÍSICA", "ED. FÍSICA")
        .replace("LÍNGUA ESTRANGEIRA - ESPANHOL", "ESPANHOL")
        .replace("LÍNGUA PORTUGUESA E LITERATURA", "PORTUGUÊS")
        .replace("SOCIOLOGIA", "SOCIOLOGIA")
        .replace("FILOSOFIA", "FILOSOFIA")
        .replace("FÍSICA", "FÍSICA")
        .replace("QUÍMICA", "QUÍMICA");
  }

  private String limparNomeArquivo(String nome) {
    return nome.replaceAll("[\\\\/:*?\"<>|]", "").trim();
  }

  private String valorOuVazio(String valor) {
    return valor == null ? "" : valor.trim();
  }
}
