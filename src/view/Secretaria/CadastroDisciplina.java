package view.Secretaria;

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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import controller.DisciplinaController;
import model.Disciplina;

public class CadastroDisciplina extends JFrame {

  private static final long serialVersionUID = 1L;

  // =========================
  // CORES
  // =========================
  // Definição das cores da interface
  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  // =========================
  // COMPONENTES
  // =========================
  private JTextField txtDescricao;
  private JTextField txtCargaHoraria;
  private JTextField txtCodigo;
  private JComboBox<String> cbAtivo;

  // =========================
  // CONSTRUTOR
  // =========================
  public CadastroDisciplina() {

    setTitle("Cadastro de Disciplina");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Cadastro de Disciplina.png"));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(1200, 720));
    setResizable(false);

    // =========================
    // PAINEL EXTERNO
    // =========================
    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);

    // =========================
    // PAINEL INTERNO
    // =========================
    JPanel interno =
        new JPanel(null) {

          private static final long serialVersionUID = 1L;

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradiente =
                new GradientPaint(0, 0, corInterna, getWidth(), getHeight(), new Color(15, 0, 50));

            g2.setPaint(gradiente);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // EFEITOS
            g2.setColor(new Color(255, 120, 220, 18));
            g2.fillOval(-120, -120, 360, 360);

            g2.setColor(new Color(120, 70, 220, 20));
            g2.fillOval(getWidth() - 260, getHeight() - 260, 420, 420);

            g2.dispose();
          }
        };

    interno.setBounds(20, 20, areaUtil.width - 40, areaUtil.height - 40);
    externo.add(interno);

    criarConteudo(interno);
  }

  // =========================
  // CRIAR CONTEÚDO
  // =========================
  private void criarConteudo(JPanel interno) {

    int largura = interno.getWidth();
    int altura = interno.getHeight();

    // =========================
    // CARD CENTRAL
    // =========================
    int larguraCard = 960;
    int alturaCard = 500;

    int xCard = (largura - larguraCard) / 2;
    int yCard = (altura - alturaCard) / 2 + 40;

    // =========================
    // BOTÃO VOLTAR
    // =========================
    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(xCard, yCard - 85, 130, 42);
    botao(btnVoltar);

    btnVoltar.addActionListener(e -> dispose());

    interno.add(btnVoltar);

    // =========================
    // TÍTULO
    // =========================
    JLabel lblTitulo = new JLabel("Cadastro de Disciplina");
    lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
    lblTitulo.setForeground(Color.WHITE);
    lblTitulo.setBounds(0, yCard - 90, largura, 55);

    interno.add(lblTitulo);

    // =========================
    // SUBTÍTULO
    // =========================
    JLabel lblSubtitulo =
        new JLabel("Cadastre e organize as disciplinas utilizadas no sistema escolar.");

    lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
    lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    lblSubtitulo.setForeground(new Color(225, 220, 255));
    lblSubtitulo.setBounds(0, yCard - 45, largura, 30);

    interno.add(lblSubtitulo);

    // =========================
    // CARD PRINCIPAL
    // =========================
    JPanel cardPrincipal = painelArredondado(corCampo, corBorda, 34);

    cardPrincipal.setLayout(null);
    cardPrincipal.setBounds(xCard, yCard, larguraCard, alturaCard);

    interno.add(cardPrincipal);

    // =========================
    // TOPO CARD
    // =========================
    JPanel topoCard = painelArredondado(corCampo, corBorda, 30);

    topoCard.setLayout(null);
    topoCard.setBounds(30, 28, larguraCard - 60, 105);

    cardPrincipal.add(topoCard);

    JLabel lblIcone = new JLabel("📚");
    lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
    lblIcone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
    lblIcone.setBounds(25, 20, 75, 65);

    topoCard.add(lblIcone);

    JLabel lblNova = new JLabel("Nova disciplina");

    lblNova.setForeground(Color.WHITE);
    lblNova.setFont(new Font("Segoe UI", Font.BOLD, 25));
    lblNova.setBounds(115, 24, 350, 32);

    topoCard.add(lblNova);

    JLabel lblDescricao =
        new JLabel("Preencha os dados abaixo para criar uma disciplina ativa no sistema.");

    lblDescricao.setForeground(new Color(230, 225, 255));
    lblDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lblDescricao.setBounds(115, 58, 650, 24);

    topoCard.add(lblDescricao);

    JLabel badge = new JLabel("Obrigatório");

    badge.setHorizontalAlignment(SwingConstants.CENTER);
    badge.setForeground(textos);
    badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
    badge.setOpaque(true);
    badge.setBackground(corCampo);
    badge.setBounds(larguraCard - 220, 35, 130, 34);
    badge.setBorder(new LineBorder(corBorda, 1, true));

    topoCard.add(badge);

    // =========================
    // DESCRIÇÃO
    // =========================
    label(cardPrincipal, "Descrição da Disciplina *", 55, 170);

    txtDescricao = campo(cardPrincipal, "Exemplo: Matemática", 55, 198, 850);

    // =========================
    // CARGA HORÁRIA
    // =========================
    label(cardPrincipal, "Carga Horária *", 55, 290);

    txtCargaHoraria = campoNumero(cardPrincipal, "Exemplo: 80", 55, 318, 250);

    // =========================
    // CÓDIGO
    // =========================
    label(cardPrincipal, "Código *", 355, 290);

    txtCodigo = campoNumero(cardPrincipal, "Exemplo: 101", 355, 318, 250);

    // =========================
    // STATUS
    // =========================
    label(cardPrincipal, "Status *", 655, 290);

    cbAtivo = combo(cardPrincipal, new String[] {"Selecione", "Sim", "Não"}, 655, 318, 250);

    // =========================
    // AJUDAS
    // =========================
    JLabel ajuda1 = ajuda("Somente números. Exemplo: 80, 120 ou 200 horas.");

    ajuda1.setBounds(55, 370, 280, 22);

    cardPrincipal.add(ajuda1);

    JLabel ajuda2 = ajuda("Utilize um código numérico único. Exemplo: 101.");

    ajuda2.setBounds(355, 370, 280, 22);

    cardPrincipal.add(ajuda2);

    JLabel ajuda3 = ajuda("Define se a disciplina aparece no sistema.");

    ajuda3.setBounds(655, 370, 280, 22);

    cardPrincipal.add(ajuda3);

    // =========================
    // BOTÕES
    // =========================
    JButton btnLimpar = new JButton("Limpar");

    btnLimpar.setBounds(315, 425, 160, 48);

    botao(btnLimpar);

    cardPrincipal.add(btnLimpar);

    JButton btnCancelar = new JButton("Cancelar");

    btnCancelar.setBounds(495, 425, 160, 48);

    botao(btnCancelar);

    cardPrincipal.add(btnCancelar);

    JButton btnCadastrar = new JButton("Cadastrar Disciplina");

    btnCadastrar.setBounds(675, 425, 230, 48);

    botaoDestaque(btnCadastrar);

    cardPrincipal.add(btnCadastrar);

    // =========================
    // RODAPÉ
    // =========================
    JLabel rodape = new JLabel("E.E.B Solo Firme • Gestão Acadêmica");

    rodape.setHorizontalAlignment(SwingConstants.CENTER);
    rodape.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    rodape.setForeground(new Color(210, 200, 245));
    rodape.setBounds(0, yCard + alturaCard + 25, largura, 25);

    interno.add(rodape);

    // =========================
    // EVENTOS
    // =========================
    btnVoltar.addActionListener(e -> dispose());

    btnLimpar.addActionListener(e -> limparCampos());

    btnCancelar.addActionListener(e -> dispose());

    btnCadastrar.addActionListener(e -> cadastrarDisciplina());
  }

  // =========================
  // LIMPAR
  // =========================
  private void limparCampos() {

    txtDescricao.setText("");
    txtCargaHoraria.setText("");
    txtCodigo.setText("");
    cbAtivo.setSelectedIndex(0);

    txtDescricao.requestFocus();
  }

  // =========================
  // CADASTRAR
  // =========================
  private void cadastrarDisciplina() {

    String descricao = txtDescricao.getText().trim();
    String cargaHoraria = txtCargaHoraria.getText().replaceAll("[^0-9]", "").trim();
    String codigo = txtCodigo.getText().trim();
    String ativo = cbAtivo.getSelectedItem().toString();

    if (descricao.isEmpty()) {
      mensagem("Informe a descrição da disciplina.", "Campo obrigatório");
      txtDescricao.requestFocus();
      return;
    }

    if (cargaHoraria.isEmpty()) {
      mensagem("Informe a carga horária da disciplina.", "Campo obrigatório");
      txtCargaHoraria.requestFocus();
      return;
    }

    int carga;

    try {
      carga = Integer.parseInt(cargaHoraria);
    } catch (NumberFormatException e) {
      mensagem("A carga horária deve conter apenas números.", "Carga horária inválida");
      txtCargaHoraria.requestFocus();
      return;
    }

    if (carga <= 0) {
      mensagem("A carga horária precisa ser maior que zero.", "Carga horária inválida");
      txtCargaHoraria.requestFocus();
      return;
    }

    if (codigo.isEmpty()) {
      mensagem("Informe o código da disciplina.", "Campo obrigatório");
      txtCodigo.requestFocus();
      return;
    }

    if (ativo.equals("Selecione")) {
      mensagem("Selecione se a disciplina está ativa.", "Campo obrigatório");
      cbAtivo.requestFocus();
      return;
    }

    try {

      Disciplina disciplina = new Disciplina();

      disciplina.setDescricao(descricao);
      disciplina.setCargaHoraria(carga);
      disciplina.setCodigo(Integer.parseInt(codigo));
      disciplina.setAtivo(ativo.equals("Sim"));

      DisciplinaController controller = new DisciplinaController();
      controller.salvarDisciplina(disciplina);

      JOptionPane.showMessageDialog(
          this,
          "Disciplina cadastrada com sucesso!",
          "Cadastro concluído",
          JOptionPane.INFORMATION_MESSAGE);

      limparCampos();

    } catch (NumberFormatException ex) {

      JOptionPane.showMessageDialog(
          this,
          "O código da disciplina deve ser numérico.",
          "Código inválido",
          JOptionPane.WARNING_MESSAGE);

    } catch (Exception ex) {

      JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);

      ex.printStackTrace();
    }
  }

  // =========================
  // MENSAGEM
  // =========================
  private void mensagem(String texto, String titulo) {

    JOptionPane.showMessageDialog(this, texto, titulo, JOptionPane.WARNING_MESSAGE);
  }

  // =========================
  // LABEL
  // =========================
  private void label(JPanel painel, String texto, int x, int y) {

    JLabel label = new JLabel(texto);

    label.setForeground(corLabel);
    label.setFont(new Font("Segoe UI", Font.BOLD, 15));
    label.setBounds(x, y, 300, 22);

    painel.add(label);
  }

  // =========================
  // AJUDA
  // =========================
  private JLabel ajuda(String texto) {

    JLabel label = new JLabel(texto);

    label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    label.setForeground(new Color(210, 200, 245));

    return label;
  }

  // =========================
  // CAMPO
  // =========================
  private JTextField campo(JPanel painel, String dica, int x, int y, int largura) {

    JTextField campo = new JTextField();

    campo.setBounds(x, y, largura, 52);

    campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    campo.setForeground(Color.WHITE);

    campo.setBackground(corCampo);

    campo.setCaretColor(Color.WHITE);

    campo.setToolTipText(dica);

    campo.setSelectionColor(corBorda);

    campo.setSelectedTextColor(Color.WHITE);

    campo.setBorder(
        new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 16, 0, 16)));

    painel.add(campo);

    return campo;
  }

  // =========================
  // CAMPO NÚMERO
  // =========================
  private JTextField campoNumero(JPanel painel, String dica, int x, int y, int largura) {

    JTextField campo = campo(painel, dica, x, y, largura);

    campo.addKeyListener(
        new java.awt.event.KeyAdapter() {

          @Override
          public void keyTyped(java.awt.event.KeyEvent e) {

            char c = e.getKeyChar();

            if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {

              e.consume();
            }

            if (campo.getText().length() >= 5 && Character.isDigit(c)) {

              e.consume();
            }
          }
        });

    return campo;
  }

  // =========================
  // COMBO
  // =========================
  private JComboBox<String> combo(JPanel painel, String[] itens, int x, int y, int largura) {

    JComboBox<String> combo = new JComboBox<>(itens);

    combo.setBounds(x, y, largura, 52);

    combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    combo.setForeground(Color.WHITE);

    combo.setBackground(corCampo);

    combo.setBorder(new LineBorder(corBorda, 1, true));

    combo.setFocusable(false);

    painel.add(combo);

    return combo;
  }

  // =========================
  // BOTÃO NORMAL
  // =========================
  private void botao(JButton botao) {

    botao.setFont(new Font("Segoe UI", Font.BOLD, 15));

    botao.setForeground(textos);

    botao.setBackground(corCampo);

    botao.setBorder(new LineBorder(corBorda, 1, true));

    botao.setFocusPainted(false);

    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  // =========================
  // BOTÃO DESTAQUE
  // =========================
  private void botaoDestaque(JButton botao) {

    botao.setFont(new Font("Segoe UI", Font.BOLD, 16));

    botao.setForeground(textos);

    botao.setBackground(corCampo);

    botao.setBorder(new LineBorder(corBorda, 1, true));

    botao.setFocusPainted(false);

    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

    botao.addMouseListener(
        new java.awt.event.MouseAdapter() {

          @Override
          public void mouseEntered(java.awt.event.MouseEvent e) {

            botao.setBackground(corCampo);
          }

          @Override
          public void mouseExited(java.awt.event.MouseEvent e) {

            botao.setBackground(corCampo);
          }
        });
  }

  // =========================
  // PAINEL ARREDONDADO
  // =========================
  private JPanel painelArredondado(Color fundo, Color borda, int raio) {

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

        // SOMBRA
        g2.setColor(new Color(0, 0, 0, 50));

        g2.fillRoundRect(5, 7, getWidth() - 10, getHeight() - 10, raio, raio);

        // FUNDO
        g2.setColor(getBackground());

        g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, raio, raio);

        // BORDA
        g2.setColor(borda);

        g2.drawRoundRect(0, 0, getWidth() - 8, getHeight() - 8, raio, raio);

        g2.dispose();

        super.paintComponent(g);
      }
    };
  }
}
