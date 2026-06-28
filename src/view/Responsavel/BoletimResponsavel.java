package view.Responsavel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class BoletimResponsavel extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JComboBox<String> comboAluno;
  private JComboBox<String> comboTrimestre;

  public BoletimResponsavel() {

    setTitle("Boletim do Aluno - Responsável");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Boletim.png"));
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

    int larguraConteudo = 1220;
    int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

    if (xInicial < 0) {
      xInicial = 0;
    }

    JPanel cardResponsavel = criarPainelArredondado();
    cardResponsavel.setLayout(null);
    cardResponsavel.setBounds(30 + xInicial, 215, 1220, 150);
    interno.add(cardResponsavel);

    adicionarResumo(cardResponsavel, "Responsável", "Maria Oliveira dos Santos", 30, 35);
    adicionarResumo(cardResponsavel, "CPF", "987.654.321-00", 330, 35);
    adicionarResumo(
        cardResponsavel, "Aluno vinculado", "Pedro Henrique Mendes dos Santos", 560, 35);
    adicionarResumo(cardResponsavel, "Ano letivo", "2026", 940, 35);

    JLabel aviso = new JLabel("Selecione o aluno e exporte o boletim em PDF automaticamente.");
    aviso.setForeground(new Color(120, 255, 170));
    aviso.setFont(new Font("Segoe UI", Font.BOLD, 16));
    aviso.setBounds(30, 105, 850, 25);
    cardResponsavel.add(aviso);

    JPanel cardExportar = criarCardSecao("Exportar Boletim");
    cardExportar.setBounds(30 + xInicial, 395, 600, 310);
    interno.add(cardExportar);

    JLabel lblAluno = criarLabelCampo("Aluno:");
    lblAluno.setBounds(30, 75, 200, 25);
    cardExportar.add(lblAluno);

    comboAluno = new JComboBox<>(new String[] {"Pedro Henrique Mendes dos Santos"});
    comboAluno.setBounds(30, 105, 520, 42);
    estilizarCombo(comboAluno);
    cardExportar.add(comboAluno);

    JLabel lblTrimestre = criarLabelCampo("Trimestre:");
    lblTrimestre.setBounds(30, 165, 200, 25);
    cardExportar.add(lblTrimestre);

    comboTrimestre =
        new JComboBox<>(
            new String[] {"Boletim completo", "1º Trimestre", "2º Trimestre", "3º Trimestre"});
    comboTrimestre.setBounds(30, 195, 250, 42);
    estilizarCombo(comboTrimestre);
    cardExportar.add(comboTrimestre);

    JButton btnExportar = new JButton("Exportar boletim em PDF");
    btnExportar.setBounds(310, 195, 240, 42);
    estilizarBotao(btnExportar);
    btnExportar.addActionListener(e -> exportarBoletim());
    cardExportar.add(btnExportar);

    JPanel cardInfo = criarCardSecao("Informações do Boletim");
    cardInfo.setBounds(660 + xInicial, 395, 590, 310);
    interno.add(cardInfo);

    adicionarInfo(cardInfo, "Aluno:", "Pedro Henrique Mendes dos Santos", 30, 75);
    adicionarInfo(cardInfo, "Turma:", "302", 30, 125);
    adicionarInfo(cardInfo, "Curso:", "Ensino Médio", 30, 175);
    adicionarInfo(cardInfo, "Situação:", "Aprovado(a)", 30, 225);

    adicionarInfo(cardInfo, "Unidade:", "E.E.B SOLO FORTE", 310, 75);
    adicionarInfo(cardInfo, "Turno:", "Matutino", 310, 125);
    adicionarInfo(cardInfo, "Ano:", "2026", 310, 175);
    adicionarInfo(cardInfo, "Pendências:", "Nenhuma", 310, 225);
  }

  private void exportarBoletim() {

    String aluno = comboAluno.getSelectedItem().toString();

    try {
      chamarClasseBoletim(aluno);
      abrirPdfBoletim(aluno);
    } catch (Exception erro) {
      JOptionPane.showMessageDialog(
          this,
          "Não foi possível abrir o boletim automaticamente.\n\n"
              + "Verifique se a classe Boletim gera o PDF na pasta:\n"
              + "Boletins/("
              + aluno
              + ").pdf",
          "Aviso",
          JOptionPane.WARNING_MESSAGE);
    }
  }

  private void chamarClasseBoletim(String aluno) {

    try {
      Class<?> classeBoletim = Class.forName("view.Boletim");

      try {
        Method metodo = classeBoletim.getMethod("gerarBoletimResponsavel", String.class);
        metodo.invoke(null, aluno);
        return;
      } catch (Exception ignored) {
      }

      try {
        Method metodo = classeBoletim.getMethod("gerarBoletim", String.class);
        metodo.invoke(null, aluno);
        return;
      } catch (Exception ignored) {
      }

      try {
        Method metodo = classeBoletim.getMethod("main", String[].class);
        metodo.invoke(null, (Object) new String[] {});
      } catch (Exception ignored) {
      }

    } catch (Exception ignored) {
    }
  }

  private void abrirPdfBoletim(String aluno) throws Exception {

    File pasta = new File("boletims");

    File pdf1 = new File(pasta, "boletim(" + aluno + ").pdf");
    File pdf2 = new File(pasta, "boletim_" + aluno.replace(" ", "_") + ".pdf");
    File pdf3 = new File(pasta, "boletim.pdf");

    File arquivoParaAbrir = null;

    if (pdf1.exists()) {
      arquivoParaAbrir = pdf1;
    } else if (pdf2.exists()) {
      arquivoParaAbrir = pdf2;
    } else if (pdf3.exists()) {
      arquivoParaAbrir = pdf3;
    }

    if (arquivoParaAbrir == null) {
      throw new Exception("PDF não encontrado.");
    }

    if (!Desktop.isDesktopSupported()) {
      throw new Exception("Desktop não suportado.");
    }

    Desktop.getDesktop().open(arquivoParaAbrir);
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

    JLabel titulo = new JLabel("Boletim do Aluno");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 600, 45);
    topo.add(titulo);

    JLabel sub =
        new JLabel("Área do responsável para consultar e exportar o boletim escolar do aluno.");
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
    lblTitulo.setBounds(x, y, 220, 22);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(textos);
    lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblValor.setBounds(x, y + 24, 360, 28);
    painel.add(lblValor);
  }

  private void adicionarInfo(JPanel painel, String titulo, String valor, int x, int y) {

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblTitulo.setBounds(x, y, 200, 22);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(textos);
    lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lblValor.setBounds(x, y + 22, 260, 24);
    painel.add(lblValor);
  }

  private JLabel criarLabelCampo(String texto) {

    JLabel label = new JLabel(texto);
    label.setForeground(corLabel);
    label.setFont(new Font("Segoe UI", Font.BOLD, 14));
    return label;
  }

  private void estilizarCombo(JComboBox<String> combo) {

    combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    combo.setForeground(textos);
    combo.setBackground(new Color(25, 8, 80));
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
}
