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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import util.DadosSistema;

public class MeusDadosProfessor extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  public MeusDadosProfessor() {
    setTitle("Meus Dados - Professor");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Meus Dados Professor.png"));
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
    estilizarBotaoAcao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    JPanel painelRolagem = new JPanel(null);
    painelRolagem.setBackground(corInterna);

    int larguraConteudo = 1220;
    int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

    if (xInicial < 0) {
      xInicial = 0;
    }
    Object[] dadosProfessor = DadosSistema.dadosProfessorAtualLinha();

    painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 900));

    JScrollPane scroll = new JScrollPane(painelRolagem);
    scroll.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(corInterna);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(26);
    interno.add(scroll);

    JPanel cardPerfil = criarPainelArredondado();
    cardPerfil.setBounds(xInicial, 0, 360, 300);
    cardPerfil.setLayout(null);
    painelRolagem.add(cardPerfil);

    JLabel foto = new JLabel("PROF.", SwingConstants.CENTER);
    foto.setOpaque(true);
    foto.setBackground(corCampo);
    foto.setForeground(textos);
    foto.setFont(new Font("Segoe UI", Font.BOLD, 28));
    foto.setBorder(new LineBorder(corBorda, 2));
    foto.setBounds(105, 25, 150, 150);
    cardPerfil.add(foto);

    JLabel nomeProfessor = new JLabel(String.valueOf(dadosProfessor[0]), SwingConstants.CENTER);
    nomeProfessor.setForeground(textos);
    nomeProfessor.setFont(new Font("Segoe UI", Font.BOLD, 21));
    nomeProfessor.setBounds(15, 190, 330, 30);
    cardPerfil.add(nomeProfessor);

    JLabel cargoProfessor = new JLabel("Professor de Matemática", SwingConstants.CENTER);
    cargoProfessor.setForeground(textos);
    cargoProfessor.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    cargoProfessor.setBounds(15, 225, 330, 25);
    cardPerfil.add(cargoProfessor);

    JLabel statusProfessor = new JLabel("Situação: Ativo", SwingConstants.CENTER);
    statusProfessor.setForeground(new Color(120, 255, 170));
    statusProfessor.setFont(new Font("Segoe UI", Font.BOLD, 16));
    statusProfessor.setBounds(20, 255, 320, 25);
    cardPerfil.add(statusProfessor);

    JPanel cardDadosPessoais = criarCardSecao("Dados Pessoais");
    cardDadosPessoais.setBounds(xInicial + 390, 0, 830, 300);
    painelRolagem.add(cardDadosPessoais);

    adicionarCampo(cardDadosPessoais, "Nome completo:", String.valueOf(dadosProfessor[0]), 25, 65);
    adicionarCampo(cardDadosPessoais, "CPF:", String.valueOf(dadosProfessor[1]), 25, 115);
    adicionarCampo(cardDadosPessoais, "RG:", String.valueOf(dadosProfessor[2]), 25, 165);
    adicionarCampo(
        cardDadosPessoais, "Data de nascimento:", String.valueOf(dadosProfessor[3]), 25, 215);

    adicionarCampo(cardDadosPessoais, "Sexo:", "Masculino", 440, 65);
    adicionarCampo(cardDadosPessoais, "Telefone:", String.valueOf(dadosProfessor[5]), 440, 115);
    adicionarCampo(cardDadosPessoais, "E-mail:", "-", 440, 165);
    adicionarCampo(cardDadosPessoais, "Status:", String.valueOf(dadosProfessor[7]), 440, 215);

    int yCard = 330;

    JPanel cardProfissional = criarCardSecao("Dados Profissionais");
    cardProfissional.setBounds(xInicial, yCard, 390, 270);
    painelRolagem.add(cardProfissional);

    adicionarCampo(
        cardProfissional, "Matricula:", String.valueOf(DadosSistema.professorIdAtual()), 25, 65);
    adicionarCampo(cardProfissional, "Cargo:", "Professor", 25, 115);
    adicionarCampo(
        cardProfissional, "Area:", DadosSistema.primeiraDisciplinaProfessorAtual(), 25, 165);
    adicionarCampo(cardProfissional, "Turno:", "Conforme turmas", 25, 215);

    JPanel cardTurmas = criarCardSecao("Turmas e Disciplinas");
    cardTurmas.setBounds(xInicial + 415, yCard, 390, 270);
    painelRolagem.add(cardTurmas);

    adicionarCampo(
        cardTurmas,
        "Disciplina principal:",
        DadosSistema.primeiraDisciplinaProfessorAtual(),
        25,
        65);
    adicionarCampo(
        cardTurmas, "Turmas:", String.valueOf(DadosSistema.totalTurmasProfessorAtual()), 25, 115);
    adicionarCampo(cardTurmas, "Carga horaria:", "-", 25, 165);
    adicionarCampo(cardTurmas, "Contrato:", "Efetivo", 25, 215);

    JPanel cardEndereco = criarCardSecao("Endereço");
    cardEndereco.setBounds(xInicial + 830, yCard, 390, 270);
    painelRolagem.add(cardEndereco);

    adicionarCampo(cardEndereco, "Rua:", String.valueOf(dadosProfessor[8]), 25, 65);
    adicionarCampo(cardEndereco, "Número:", "540", 25, 115);
    adicionarCampo(cardEndereco, "Bairro:", String.valueOf(dadosProfessor[10]), 25, 165);
    adicionarCampo(cardEndereco, "Cidade/UF:", String.valueOf(dadosProfessor[11]), 25, 215);

    JPanel cardDadosExtras = criarCardSecao("Informações Complementares");
    cardDadosExtras.setBounds(xInicial, 620, 600, 225);
    painelRolagem.add(cardDadosExtras);

    adicionarCampo(cardDadosExtras, "CEP:", String.valueOf(dadosProfessor[12]), 25, 65);
    adicionarCampo(cardDadosExtras, "Complemento:", String.valueOf(dadosProfessor[13]), 25, 115);
    adicionarCampo(cardDadosExtras, "Formacao:", String.valueOf(dadosProfessor[6]), 25, 165);

    adicionarCampo(cardDadosExtras, "Admissão:", "05/02/2020", 315, 65);
    adicionarCampo(cardDadosExtras, "Cadastro atualizado:", "02/06/2026", 315, 115);
    adicionarCampo(cardDadosExtras, "Pendências:", "Nenhuma", 315, 165);

    JPanel cardObservacoes = criarCardSecao("Observações");
    cardObservacoes.setBounds(xInicial + 630, 620, 590, 225);
    painelRolagem.add(cardObservacoes);

    JLabel obs =
        new JLabel(
            "<html>Professor sem pendências cadastrais no momento.<br><br>"
                + "Caso encontre alguma informação incorreta, procure a secretaria "
                + "ou solicite a atualização dos dados pelo botão abaixo.</html>");

    obs.setForeground(textos);
    obs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    obs.setBounds(25, 65, 530, 90);
    cardObservacoes.add(obs);

    JButton btnSolicitarAtualizacao = new JButton("Solicitar atualização de dados");
    btnSolicitarAtualizacao.setBounds(25, 165, 320, 42);
    estilizarBotaoAcao(btnSolicitarAtualizacao);
    cardObservacoes.add(btnSolicitarAtualizacao);
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

    JLabel titulo = new JLabel("Meus Dados");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
    titulo.setBounds(40, 80, 500, 45);
    topo.add(titulo);

    JLabel sub =
        new JLabel(
            "Consulte suas informações pessoais, profissionais, endereço e turmas vinculadas.");
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
    lblTitulo.setBounds(25, 18, 400, 30);
    painel.add(lblTitulo);

    JSeparator linha = new JSeparator();
    linha.setBounds(25, 52, 530, 1);
    linha.setForeground(corBorda);
    painel.add(linha);

    return painel;
  }

  private void adicionarCampo(JPanel painel, String titulo, String valor, int x, int y) {

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblTitulo.setBounds(x, y, 210, 22);
    painel.add(lblTitulo);

    JLabel lblValor = new JLabel(valor);
    lblValor.setForeground(textos);
    lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lblValor.setBounds(x, y + 22, 330, 24);
    painel.add(lblValor);
  }

  private void estilizarBotaoAcao(JButton botao) {
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
