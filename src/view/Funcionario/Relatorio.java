package view.Funcionario;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class Relatorio extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private static final Color PDF_AZUL = new Color(5, 28, 82);
  private static final Color PDF_CINZA = new Color(245, 247, 251);
  private static final Color PDF_LINHA = new Color(185, 194, 210);
  private static final Color PDF_VERDE = new Color(0, 130, 67);
  private static final Color PDF_VERMELHO = new Color(196, 20, 31);

  private JTable tabela;
  private DefaultTableModel modeloTabela;
  private TableRowSorter<DefaultTableModel> sorter;

  private JComboBox<String> cbTipo;
  private JComboBox<String> cbStatus;
  private JComboBox<String> cbAno;
  private JComboBox<String> cbTurmaSetor;

  private JLabel lblTotalAlunos;
  private JLabel lblTotalProfessores;
  private JLabel lblTotalFuncionarios;
  private JLabel lblAprovados;
  private JLabel lblReprovados;

  public Relatorio() {
    setTitle("Painel Administrativo");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Relatorios.png"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    java.awt.Rectangle areaUtil =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
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
    interno.setBounds(
        (areaUtil.width - larguraInterno) / 2,
        (areaUtil.height - alturaInterno) / 2,
        larguraInterno,
        alturaInterno);
    interno.setBackground(corInterna);
    externo.add(interno);

    criarConteudo(interno, larguraInterno, alturaInterno);
    atualizarCards();
  }

  private void criarConteudo(JPanel interno, int larguraInterno, int alturaInterno) {
    int margemConteudo = 40;
    int larguraConteudo = larguraInterno - (margemConteudo * 2);
    int espacamentoCard = 20;
    int larguraCard = (larguraConteudo - (espacamentoCard * 4)) / 5;

    JPanel topo =
        new JPanel() {
          private static final long serialVersionUID = 1L;

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(140, 30, 190));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
          }
        };
    topo.setBounds(margemConteudo, 30, larguraConteudo, 150);
    topo.setOpaque(false);
    topo.setLayout(null);
    interno.add(topo);

    JLabel titulo = new JLabel("Painel Administrativo");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 25, 700, 45);
    topo.add(titulo);

    JLabel sub = new JLabel("Visualize informações gerais da escola e gere relatórios completos.");
    sub.setForeground(new Color(240, 220, 255));
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    sub.setBounds(42, 78, 900, 30);
    topo.add(sub);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(larguraConteudo - 160, 52, 120, 42);
    estilizarBotaoAcao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    JPanel cardAlunos = criarCard("Total de Alunos", "0");
    cardAlunos.setBounds(margemConteudo, 210, larguraCard, 120);
    lblTotalAlunos = (JLabel) cardAlunos.getComponent(1);
    interno.add(cardAlunos);

    JPanel cardProfessores = criarCard("Professores", "0");
    cardProfessores.setBounds(
        margemConteudo + (larguraCard + espacamentoCard), 210, larguraCard, 120);
    lblTotalProfessores = (JLabel) cardProfessores.getComponent(1);
    interno.add(cardProfessores);

    JPanel cardFuncionarios = criarCard("Funcionários", "0");
    cardFuncionarios.setBounds(
        margemConteudo + ((larguraCard + espacamentoCard) * 2), 210, larguraCard, 120);
    lblTotalFuncionarios = (JLabel) cardFuncionarios.getComponent(1);
    interno.add(cardFuncionarios);

    JPanel cardAprovados = criarCard("Aprovação", "0%");
    cardAprovados.setBounds(
        margemConteudo + ((larguraCard + espacamentoCard) * 3), 210, larguraCard, 120);
    lblAprovados = (JLabel) cardAprovados.getComponent(1);
    interno.add(cardAprovados);

    JPanel cardReprovados = criarCard("Reprovação", "0%");
    cardReprovados.setBounds(
        margemConteudo + ((larguraCard + espacamentoCard) * 4), 210, larguraCard, 120);
    lblReprovados = (JLabel) cardReprovados.getComponent(1);
    interno.add(cardReprovados);

    JPanel filtros = criarPainel();
    filtros.setLayout(null);
    filtros.setBounds(margemConteudo, 360, larguraConteudo, 130);
    interno.add(filtros);

    JLabel lbFiltros = new JLabel("Filtros");
    lbFiltros.setForeground(corLabel);
    lbFiltros.setFont(new Font("Segoe UI", Font.BOLD, 20));
    lbFiltros.setBounds(25, 15, 300, 30);
    filtros.add(lbFiltros);

    cbTipo =
        criarCombo(
            new String[] {
              "Todos", "Aluno", "Professor", "Funcionário", "Secretária", "Responsável"
            });
    cbStatus = criarCombo(new String[] {"Todos", "Ativo", "Inativo", "Aprovado", "Reprovado"});
    cbAno = criarCombo(gerarAnos());
    cbTurmaSetor =
        criarCombo(
            new String[] {
              "Todas",
              "1º Ano",
              "2º Ano",
              "3º Ano",
              "Secretaria",
              "Limpeza",
              "História",
              "Matemática",
              "Responsável Aluno"
            });

    int espacoFiltro = 16;
    int larguraBotao = 120;
    int larguraBotaoExportar = 160;
    int larguraCampos =
        filtros.getWidth() - 50 - (espacoFiltro * 6) - (larguraBotao * 2) - larguraBotaoExportar;
    int larguraCombo = larguraCampos / 4;
    int xFiltro = 25;

    cbTipo.setBounds(xFiltro, 60, larguraCombo, 38);
    filtros.add(cbTipo);
    xFiltro += larguraCombo + espacoFiltro;

    cbStatus.setBounds(xFiltro, 60, larguraCombo, 38);
    filtros.add(cbStatus);
    xFiltro += larguraCombo + espacoFiltro;

    cbAno.setBounds(xFiltro, 60, larguraCombo, 38);
    filtros.add(cbAno);
    xFiltro += larguraCombo + espacoFiltro;

    cbTurmaSetor.setBounds(xFiltro, 60, larguraCombo, 38);
    filtros.add(cbTurmaSetor);
    xFiltro += larguraCombo + espacoFiltro;

    JButton btnGerar = new JButton("Gerar");
    btnGerar.setBounds(xFiltro, 60, larguraBotao, 38);
    estilizarBotaoAcao(btnGerar);
    filtros.add(btnGerar);
    xFiltro += larguraBotao + espacoFiltro;

    JButton btnLimpar = new JButton("Limpar");
    btnLimpar.setBounds(xFiltro, 60, larguraBotao, 38);
    estilizarBotaoAcao(btnLimpar);
    filtros.add(btnLimpar);
    xFiltro += larguraBotao + espacoFiltro;

    JButton btnExportar = new JButton("Exportar PDF");
    btnExportar.setBounds(xFiltro, 60, larguraBotaoExportar, 38);
    estilizarBotaoAcao(btnExportar);
    filtros.add(btnExportar);

    JPanel tabelaPainel = criarPainel();
    tabelaPainel.setLayout(null);
    tabelaPainel.setBounds(margemConteudo, 520, larguraConteudo, alturaInterno - 560);
    interno.add(tabelaPainel);

    JLabel tituloTabela = new JLabel("Usuários do Sistema");
    tituloTabela.setForeground(corLabel);
    tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 20));
    tituloTabela.setBounds(25, 15, 350, 30);
    tabelaPainel.add(tituloTabela);

    tabela = criarTabela();
    JScrollPane scroll = new JScrollPane(tabela);
    scroll.setBounds(25, 60, tabelaPainel.getWidth() - 50, tabelaPainel.getHeight() - 85);
    scroll.setBorder(new LineBorder(corBorda));
    scroll.getViewport().setBackground(corCampo);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    tabelaPainel.add(scroll);

    btnGerar.addActionListener(e -> aplicarFiltros());
    btnLimpar.addActionListener(e -> limparFiltros());
    btnExportar.addActionListener(e -> exportarPDFPremium());
  }

  private JTable criarTabela() {
    String[] colunas = {
      "ID", "Nome", "Tipo", "Turma / Setor", "Status", "Média", "Frequência", "Ano Letivo"
    };
    Object[][] dados = {
      {
        "1", "Pedro Henrique Mendes dos Santos", "Aluno", "3º Ano", "Aprovado", "8,5", "95%", "2026"
      },
      {"2", "Fernanda Lima", "Aluno", "1º Ano", "Aprovado", "9,2", "98%", "2026"},
      {"3", "João Carlos", "Aluno", "2º Ano", "Aprovado", "8,0", "96%", "2026"},
      {"4", "Ana Paula", "Secretária", "Secretaria", "Ativo", "-", "-", "2026"},
      {"5", "Carlos Silva", "Funcionário", "Limpeza", "Ativo", "-", "-", "2026"},
      {"6", "Marcos Vinícius", "Responsável", "Responsável Aluno", "Ativo", "-", "-", "2026"},
      {"7", "Roberto Alves", "Professor", "História", "Ativo", "-", "-", "2026"},
      {"8", "João Paulo", "Aluno", "3º Ano", "Aprovado", "7,8", "93%", "2026"},
      {"9", "Isabela Rocha", "Aluno", "2º Ano", "Aprovado", "8,9", "97%", "2026"},
      {"10", "Lucas Ferreira", "Aluno", "1º Ano", "Aprovado", "8,3", "95%", "2026"},
      {"11", "Maria Oliveira", "Aluno", "2º Ano", "Reprovado", "4,0", "61%", "2026"},
      {"12", "Paulo Martins", "Professor", "Matemática", "Ativo", "-", "-", "2026"}
    };

    modeloTabela =
        new DefaultTableModel(dados, colunas) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    JTable t = new JTable(modeloTabela);
    sorter = new TableRowSorter<>(modeloTabela);
    t.setRowSorter(sorter);
    t.setRowHeight(42);
    t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    t.setBackground(new Color(28, 12, 82));
    t.setForeground(textos);
    t.setGridColor(corBorda);
    t.setSelectionBackground(new Color(90, 40, 180));
    t.setSelectionForeground(textos);
    t.setShowGrid(true);

    JTableHeader header = t.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    header.setBackground(new Color(45, 15, 120));
    header.setForeground(textos);
    header.setPreferredSize(new Dimension(header.getWidth(), 42));

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);
    centro.setBackground(new Color(28, 12, 82));
    centro.setForeground(textos);

    DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
    esquerda.setHorizontalAlignment(SwingConstants.LEFT);
    esquerda.setBackground(new Color(28, 12, 82));
    esquerda.setForeground(textos);

    for (int i = 0; i < t.getColumnCount(); i++) {
      t.getColumnModel().getColumn(i).setCellRenderer(i == 1 ? esquerda : centro);
    }

    TableColumnModel cm = t.getColumnModel();
    cm.getColumn(0).setPreferredWidth(55);
    cm.getColumn(1).setPreferredWidth(300);
    cm.getColumn(2).setPreferredWidth(150);
    cm.getColumn(3).setPreferredWidth(170);
    cm.getColumn(4).setPreferredWidth(140);
    cm.getColumn(5).setPreferredWidth(110);
    cm.getColumn(6).setPreferredWidth(130);
    cm.getColumn(7).setPreferredWidth(120);
    return t;
  }

  private void aplicarFiltros() {
    RowFilter<DefaultTableModel, Object> filtro =
        new RowFilter<DefaultTableModel, Object>() {
          @Override
          public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
            String tipoSelecionado = cbTipo.getSelectedItem().toString();
            String statusSelecionado = cbStatus.getSelectedItem().toString();
            String anoSelecionado = cbAno.getSelectedItem().toString();
            String turmaSelecionada = cbTurmaSetor.getSelectedItem().toString();

            String tipo = entry.getStringValue(2);
            String turma = entry.getStringValue(3);
            String status = entry.getStringValue(4);
            String ano = entry.getStringValue(7);

            boolean tipoOk =
                tipoSelecionado.equals("Todos") || tipo.equalsIgnoreCase(tipoSelecionado);
            boolean statusOk =
                statusSelecionado.equals("Todos") || status.equalsIgnoreCase(statusSelecionado);
            boolean anoOk = anoSelecionado.equals("Todos") || ano.equalsIgnoreCase(anoSelecionado);
            boolean turmaOk =
                turmaSelecionada.equals("Todas") || turma.equalsIgnoreCase(turmaSelecionada);
            return tipoOk && statusOk && anoOk && turmaOk;
          }
        };
    sorter.setRowFilter(filtro);
    atualizarCards();
    if (tabela.getRowCount() == 0) {
      JOptionPane.showMessageDialog(
          this,
          "Nenhum registro encontrado com os filtros selecionados.",
          "Relatórios",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void limparFiltros() {
    cbTipo.setSelectedIndex(0);
    cbStatus.setSelectedIndex(0);
    cbAno.setSelectedIndex(0);
    cbTurmaSetor.setSelectedIndex(0);
    sorter.setRowFilter(null);
    atualizarCards();
  }

  private void atualizarCards() {
    int alunos = 0;
    int professores = 0;
    int funcionarios = 0;
    int aprovados = 0;
    int reprovados = 0;
    int totalAlunosComResultado = 0;

    for (int i = 0; i < tabela.getRowCount(); i++) {
      int modelRow = tabela.convertRowIndexToModel(i);
      String tipo = modeloTabela.getValueAt(modelRow, 2).toString();
      String status = modeloTabela.getValueAt(modelRow, 4).toString();
      if (tipo.equalsIgnoreCase("Aluno")) {
        alunos++;
        if (status.equalsIgnoreCase("Aprovado") || status.equalsIgnoreCase("Reprovado"))
          totalAlunosComResultado++;
        if (status.equalsIgnoreCase("Aprovado")) aprovados++;
        if (status.equalsIgnoreCase("Reprovado")) reprovados++;
      }
      if (tipo.equalsIgnoreCase("Professor")) professores++;
      if (tipo.equalsIgnoreCase("Funcionário") || tipo.equalsIgnoreCase("Secretária"))
        funcionarios++;
    }

    int percAprovados =
        totalAlunosComResultado == 0 ? 0 : (aprovados * 100) / totalAlunosComResultado;
    int percReprovados =
        totalAlunosComResultado == 0 ? 0 : (reprovados * 100) / totalAlunosComResultado;

    lblTotalAlunos.setText(String.valueOf(alunos));
    lblTotalProfessores.setText(String.valueOf(professores));
    lblTotalFuncionarios.setText(String.valueOf(funcionarios));
    lblAprovados.setText(percAprovados + "%");
    lblReprovados.setText(percReprovados + "%");
  }

  private void exportarPDFPremium() {
    try {
      File pasta = new File(System.getProperty("user.dir"), "Relatórios");
      if (!pasta.exists()) pasta.mkdirs();

      String dataArquivo =
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
      File arquivo = new File(pasta, "Relatorio_Administrativo_" + dataArquivo + ".pdf");

      Document documento = new Document(PageSize.A4.rotate(), 18, 18, 18, 18);
      PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(arquivo));
      writer.setPageEvent(new RodapePagina());
      documento.open();

      desenharMoldura(writer, documento);
      adicionarCabecalhoPremium(documento);
      adicionarSecaoFiltros(documento);
      adicionarSecaoResumo(documento);
      adicionarTabelaUsuarios(documento);
      adicionarRodapeVisual(documento);

      documento.close();

      JOptionPane.showMessageDialog(
          this,
          "Relatório exportado com sucesso!\n\n" + arquivo.getAbsolutePath(),
          "PDF Gerado",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          this, "Erro ao exportar PDF:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  private void desenharMoldura(PdfWriter writer, Document documento) {
    PdfContentByte cb = writer.getDirectContent();
    cb.setColorStroke(PDF_AZUL);
    cb.setLineWidth(0.8f);
    cb.rectangle(
        5, 5, documento.getPageSize().getWidth() - 10, documento.getPageSize().getHeight() - 10);
    cb.stroke();
  }

  private void adicionarCabecalhoPremium(Document doc) throws Exception {
    PdfPTable cabecalho = new PdfPTable(3);
    cabecalho.setWidthPercentage(100);
    cabecalho.setWidths(new float[] {3.1f, 4.8f, 2.2f});

    PdfPCell logoCell = new PdfPCell();
    logoCell.setBorder(Rectangle.NO_BORDER);
    logoCell.setPadding(4);
    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    try {
      Image logo = Image.getInstance("resources/Images/Solo Firme.png");
      logo.scaleToFit(300, 120);
      logoCell.addElement(logo);
    } catch (Exception e) {
      Paragraph semLogo =
          new Paragraph("E.E.B. Solo Firme", fontePdf(24, com.lowagie.text.Font.BOLD, PDF_AZUL));
      semLogo.setAlignment(Element.ALIGN_CENTER);
      logoCell.addElement(semLogo);
    }
    cabecalho.addCell(logoCell);

    PdfPCell centro = new PdfPCell();
    centro.setBorder(Rectangle.LEFT);
    centro.setBorderColor(PDF_LINHA);
    centro.setPaddingTop(10);
    centro.setPaddingLeft(20);
    centro.setPaddingRight(20);

    Paragraph titulo =
        new Paragraph(
            "RELATÓRIO ADMINISTRATIVO", fontePdf(25, com.lowagie.text.Font.BOLD, PDF_AZUL));
    titulo.setAlignment(Element.ALIGN_CENTER);
    titulo.setSpacingAfter(4);
    centro.addElement(titulo);

    Paragraph sub =
        new Paragraph(
            "SISTEMA DE GESTÃO ESCOLAR", fontePdf(13, com.lowagie.text.Font.NORMAL, PDF_AZUL));
    sub.setAlignment(Element.ALIGN_CENTER);
    sub.setSpacingAfter(7);
    centro.addElement(sub);

    Paragraph estrelas =
        new Paragraph(
            "───────   ★  ★  ★   ───────", fontePdf(15, com.lowagie.text.Font.BOLD, PDF_AZUL));
    estrelas.setAlignment(Element.ALIGN_CENTER);
    centro.addElement(estrelas);
    cabecalho.addCell(centro);

    PdfPCell info = criarBoxInfoEmissao();
    cabecalho.addCell(info);

    doc.add(cabecalho);
    adicionarEspaco(doc, 6);
  }

  private PdfPCell criarBoxInfoEmissao() {
    PdfPCell cell = new PdfPCell();
    cell.setPadding(10);
    cell.setBorderColor(PDF_AZUL);
    cell.setBorderWidth(0.7f);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

    Paragraph dataTitulo =
        new Paragraph("▣   DATA DE EMISSÃO:", fontePdf(9, com.lowagie.text.Font.BOLD, PDF_AZUL));
    dataTitulo.setAlignment(Element.ALIGN_CENTER);
    dataTitulo.setSpacingAfter(3);
    cell.addElement(dataTitulo);

    Paragraph data =
        new Paragraph(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss")),
            fontePdf(10, com.lowagie.text.Font.BOLD, Color.BLACK));
    data.setAlignment(Element.ALIGN_CENTER);
    data.setSpacingAfter(12);
    cell.addElement(data);

    Paragraph usuarioTitulo =
        new Paragraph("●   USUÁRIO:", fontePdf(9, com.lowagie.text.Font.BOLD, PDF_AZUL));
    usuarioTitulo.setAlignment(Element.ALIGN_CENTER);
    usuarioTitulo.setSpacingAfter(3);
    cell.addElement(usuarioTitulo);

    Paragraph usuario =
        new Paragraph(
            "Administrador do Sistema", fontePdf(10, com.lowagie.text.Font.BOLD, PDF_AZUL));
    usuario.setAlignment(Element.ALIGN_CENTER);
    cell.addElement(usuario);
    return cell;
  }

  private void adicionarSecaoFiltros(Document doc) throws Exception {
    adicionarTituloSecao(doc, "FILTROS UTILIZADOS");

    PdfPTable filtros = new PdfPTable(4);
    filtros.setWidthPercentage(100);
    filtros.setWidths(new float[] {2.3f, 2.3f, 2.3f, 2.3f});

    filtros.addCell(
        criarCelulaFiltroPremium("☷", "TIPO DE CADASTRO", cbTipo.getSelectedItem().toString()));
    filtros.addCell(
        criarCelulaFiltroPremium("✓", "SITUAÇÃO", cbStatus.getSelectedItem().toString()));
    filtros.addCell(
        criarCelulaFiltroPremium("▣", "ANO LETIVO", cbAno.getSelectedItem().toString()));
    filtros.addCell(
        criarCelulaFiltroPremium("▼", "TURMA / SETOR", cbTurmaSetor.getSelectedItem().toString()));

    doc.add(filtros);
    adicionarEspaco(doc, 7);
  }

  private PdfPCell criarCelulaFiltroPremium(String icone, String titulo, String valor)
      throws Exception {
    PdfPTable interno = new PdfPTable(2);
    interno.setWidthPercentage(100);
    interno.setWidths(new float[] {0.6f, 2.7f});

    PdfPCell cIcone =
        new PdfPCell(new Phrase(icone, fontePdf(20, com.lowagie.text.Font.BOLD, PDF_AZUL)));
    cIcone.setBorder(Rectangle.NO_BORDER);
    cIcone.setHorizontalAlignment(Element.ALIGN_CENTER);
    cIcone.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cIcone.setMinimumHeight(45);
    cIcone.setPadding(5);
    interno.addCell(cIcone);

    PdfPCell textosCell = new PdfPCell();
    textosCell.setBorder(Rectangle.NO_BORDER);
    textosCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    textosCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    textosCell.setMinimumHeight(45);
    textosCell.setPadding(5);
    Paragraph pTitulo = new Paragraph(titulo, fontePdf(8.5f, com.lowagie.text.Font.BOLD, PDF_AZUL));
    pTitulo.setAlignment(Element.ALIGN_CENTER);
    textosCell.addElement(pTitulo);
    Paragraph pValor = new Paragraph(valor, fontePdf(12, com.lowagie.text.Font.BOLD, PDF_AZUL));
    pValor.setAlignment(Element.ALIGN_CENTER);
    textosCell.addElement(pValor);
    interno.addCell(textosCell);

    PdfPCell cell = new PdfPCell(interno);
    cell.setPadding(9);
    cell.setBorderColor(PDF_AZUL);
    cell.setBorderWidth(0.7f);
    cell.setMinimumHeight(48);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    return cell;
  }

  private void adicionarSecaoResumo(Document doc) throws Exception {
    adicionarTituloSecao(doc, "RESUMO GERAL");

    PdfPTable resumo = new PdfPTable(6);
    resumo.setWidthPercentage(100);
    resumo.setWidths(new float[] {1.6f, 1.6f, 1.6f, 1.6f, 1.6f, 1.9f});

    resumo.addCell(
        criarCardResumoPremium("▰", "TOTAL DE ALUNOS", lblTotalAlunos.getText(), PDF_AZUL));
    resumo.addCell(
        criarCardResumoPremium("●", "PROFESSORES", lblTotalProfessores.getText(), PDF_AZUL));
    resumo.addCell(
        criarCardResumoPremium("■", "FUNCIONÁRIOS", lblTotalFuncionarios.getText(), PDF_AZUL));
    resumo.addCell(criarCardResumoPremium("👍", "APROVAÇÃO", lblAprovados.getText(), PDF_VERDE));
    resumo.addCell(
        criarCardResumoPremium("👎", "REPROVAÇÃO", lblReprovados.getText(), PDF_VERMELHO));
    resumo.addCell(
        criarCardResumoPremium(
            "▤", "REGISTROS ENCONTRADOS", String.valueOf(tabela.getRowCount()), PDF_AZUL));

    doc.add(resumo);
    adicionarEspaco(doc, 8);
  }

  private PdfPCell criarCardResumoPremium(String icone, String titulo, String valor, Color corValor)
      throws Exception {
    PdfPTable interno = new PdfPTable(2);
    interno.setWidthPercentage(100);
    interno.setWidths(new float[] {0.7f, 1.9f});

    PdfPCell cIcone =
        new PdfPCell(new Phrase(icone, fontePdf(17, com.lowagie.text.Font.BOLD, corValor)));
    cIcone.setBorder(Rectangle.NO_BORDER);
    cIcone.setHorizontalAlignment(Element.ALIGN_CENTER);
    cIcone.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cIcone.setMinimumHeight(50);
    cIcone.setPadding(5);
    interno.addCell(cIcone);

    PdfPCell cTexto = new PdfPCell();
    cTexto.setBorder(Rectangle.NO_BORDER);
    cTexto.setHorizontalAlignment(Element.ALIGN_CENTER);
    cTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cTexto.setMinimumHeight(50);
    cTexto.setPadding(5);
    Paragraph pTitulo = new Paragraph(titulo, fontePdf(7.7f, com.lowagie.text.Font.BOLD, PDF_AZUL));
    pTitulo.setAlignment(Element.ALIGN_CENTER);
    cTexto.addElement(pTitulo);
    Paragraph pValor = new Paragraph(valor, fontePdf(18, com.lowagie.text.Font.BOLD, corValor));
    pValor.setAlignment(Element.ALIGN_CENTER);
    cTexto.addElement(pValor);
    interno.addCell(cTexto);

    PdfPCell cell = new PdfPCell(interno);
    cell.setPadding(8);
    cell.setBorderColor(PDF_LINHA);
    cell.setBorderWidth(0.6f);
    cell.setMinimumHeight(52);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    return cell;
  }

  private void adicionarTabelaUsuarios(Document doc) throws Exception {
    adicionarTituloSecao(doc, "USUÁRIOS DO SISTEMA");

    PdfPTable tabelaPDF = new PdfPTable(tabela.getColumnCount());
    tabelaPDF.setWidthPercentage(100);
    tabelaPDF.setWidths(new float[] {0.55f, 2.45f, 1.3f, 1.75f, 1.35f, 1.2f, 1.5f, 1.3f});

    for (int i = 0; i < tabela.getColumnCount(); i++) {
      PdfPCell c =
          new PdfPCell(
              new Phrase(
                  tabela.getColumnName(i).toUpperCase(),
                  fontePdf(8.5f, com.lowagie.text.Font.BOLD, Color.WHITE)));
      c.setHorizontalAlignment(Element.ALIGN_CENTER);
      c.setVerticalAlignment(Element.ALIGN_MIDDLE);
      c.setBackgroundColor(PDF_AZUL);
      c.setBorderColor(PDF_AZUL);
      c.setPadding(7);
      tabelaPDF.addCell(c);
    }

    for (int i = 0; i < tabela.getRowCount(); i++) {
      for (int j = 0; j < tabela.getColumnCount(); j++) {
        String valor = String.valueOf(tabela.getValueAt(i, j));
        Color fonteCor = PDF_AZUL;
        int estilo = com.lowagie.text.Font.NORMAL;
        if (valor.equalsIgnoreCase("Aprovado")) {
          fonteCor = PDF_VERDE;
          estilo = com.lowagie.text.Font.BOLD;
        } else if (valor.equalsIgnoreCase("Reprovado")) {
          fonteCor = PDF_VERMELHO;
          estilo = com.lowagie.text.Font.BOLD;
        } else if (valor.equalsIgnoreCase("Ativo")) {
          fonteCor = new Color(0, 80, 180);
          estilo = com.lowagie.text.Font.BOLD;
        } else if (valor.equalsIgnoreCase("Inativo")) {
          fonteCor = PDF_VERMELHO;
          estilo = com.lowagie.text.Font.BOLD;
        }

        PdfPCell c = new PdfPCell(new Phrase(valor, fontePdf(8.2f, estilo, fonteCor)));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        c.setBorderColor(PDF_LINHA);
        c.setBorderWidth(0.35f);
        if (i % 2 == 1) c.setBackgroundColor(PDF_CINZA);
        tabelaPDF.addCell(c);
      }
    }
    doc.add(tabelaPDF);
  }

  private void adicionarRodapeVisual(Document doc) throws Exception {
    adicionarEspaco(doc, 7);
    PdfPTable rodape = new PdfPTable(3);
    rodape.setWidthPercentage(100);
    rodape.setWidths(new float[] {3.1f, 4.2f, 3.1f});

    PdfPCell total =
        criarCelulaRodape(
            "▤", "Total de registros encontrados:  " + tabela.getRowCount(), Element.ALIGN_CENTER);
    PdfPCell sistema =
        criarCelulaRodape(
            "▥",
            "Relatório gerado pelo Sistema de Gestão Escolar\n"
                + "Este documento não necessita de assinatura.",
            Element.ALIGN_CENTER);
    PdfPCell pagina = criarCelulaRodape("▣", "Página  1  de  1", Element.ALIGN_CENTER);

    rodape.addCell(total);
    rodape.addCell(sistema);
    rodape.addCell(pagina);
    doc.add(rodape);
  }

  private PdfPCell criarCelulaRodape(String icone, String texto, int alinhamento) {
    Paragraph p =
        new Paragraph(icone + "   " + texto, fontePdf(9, com.lowagie.text.Font.NORMAL, PDF_AZUL));
    p.setAlignment(alinhamento);
    PdfPCell cell = new PdfPCell();
    cell.setBorder(Rectangle.TOP);
    cell.setBorderColor(PDF_AZUL);
    cell.setPaddingTop(10);
    cell.setHorizontalAlignment(alinhamento);
    cell.addElement(p);
    return cell;
  }

  private void adicionarTituloSecao(Document doc, String titulo) throws Exception {
    PdfPTable faixa = new PdfPTable(1);
    faixa.setWidthPercentage(100);
    PdfPCell cell =
        new PdfPCell(
            new Phrase(
                "  " + titulo + "  ", fontePdf(10.5f, com.lowagie.text.Font.BOLD, Color.WHITE)));
    cell.setBackgroundColor(PDF_AZUL);
    cell.setBorderColor(PDF_AZUL);
    cell.setPadding(4);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    faixa.addCell(cell);
    doc.add(faixa);
  }

  private void adicionarEspaco(Document doc, int altura) throws Exception {
    Paragraph p = new Paragraph(" ");
    p.setLeading(altura);
    doc.add(p);
  }

  private com.lowagie.text.Font fontePdf(float tamanho, int estilo, Color cor) {
    return FontFactory.getFont(FontFactory.HELVETICA, tamanho, estilo, cor);
  }

  private JPanel criarCard(String titulo, String valor) {
    JPanel card = criarPainel();
    card.setLayout(new GridLayout(2, 1, 0, 6));
    card.setBorder(new EmptyBorder(18, 10, 14, 10));

    JLabel lbTitulo = new JLabel(titulo, SwingConstants.CENTER);
    lbTitulo.setForeground(corLabel);
    lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
    card.add(lbTitulo);

    JLabel lbValor = new JLabel(valor, SwingConstants.CENTER);
    lbValor.setForeground(textos);
    lbValor.setFont(new Font("Segoe UI", Font.BOLD, 38));
    card.add(lbValor);
    return card;
  }

  private String[] gerarAnos() {
    int anoAtual = LocalDate.now().getYear();
    return new String[] {
      "Todos",
      String.valueOf(anoAtual),
      String.valueOf(anoAtual - 1),
      String.valueOf(anoAtual - 2),
      String.valueOf(anoAtual - 3)
    };
  }

  private JComboBox<String> criarCombo(String[] itens) {
    JComboBox<String> combo = new JComboBox<>(itens);
    combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    combo.setForeground(textos);
    combo.setBackground(new Color(38, 15, 110));
    combo.setBorder(new LineBorder(corBorda));
    return combo;
  }

  private void estilizarBotaoAcao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 15));
    botao.setForeground(textos);
    botao.setBackground(corCampo);
    botao.setBorder(new LineBorder(corBorda));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  private JPanel criarPainel() {
    return new JPanel() {
      private static final long serialVersionUID = 1L;

      {
        setOpaque(false);
        setBackground(corCampo);
        setBorder(BorderFactory.createLineBorder(corBorda));
      }

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
        g2.setColor(corBorda);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
        g2.dispose();
        super.paintComponent(g);
      }
    };
  }

  private class RodapePagina extends PdfPageEventHelper {
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      try {
        PdfContentByte cb = writer.getDirectContent();
        cb.setColorStroke(PDF_AZUL);
        cb.setLineWidth(0.8f);
        cb.rectangle(
            5, 5, document.getPageSize().getWidth() - 10, document.getPageSize().getHeight() - 10);
        cb.stroke();

        BaseFont bf =
            BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        cb.beginText();
        cb.setFontAndSize(bf, 8);
        cb.setColorFill(PDF_AZUL);
        cb.showTextAligned(
            Element.ALIGN_RIGHT,
            "Página " + writer.getPageNumber(),
            document.getPageSize().getWidth() - 24,
            14,
            0);
        cb.endText();
      } catch (Exception ignored) {
      }
    }
  }
}
