package view.Funcionario;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import util.DadosSistema;
import util.SessaoUsuario;
import view.MenuCadastro;
import view.Secretaria.AlunosSecretaria;
import view.Secretaria.AvisosSecretaria;
import view.Secretaria.DocumentosSecretaria;

public class TelaInicialFuncionario extends JFrame {

  private static final long serialVersionUID = 1L;

  // Definição das cores da interface
  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private final int larguraInterno;
  private final int alturaInterno;
  private final int margem;

  private Calendar mesAtual = Calendar.getInstance();
  private JPanel gradeDias;
  private JLabel lblMesAno;

  public TelaInicialFuncionario() {

    setTitle("Funcionário");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Funcionario.png"));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    margem = 30;
    larguraInterno = areaUtil.width - (margem * 2) + 20;
    alturaInterno = areaUtil.height - (margem * 2);

    setMaximizedBounds(areaUtil);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(1200, 720));
    setResizable(false);

    JPanel externo = new JPanel();
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    externo.setLayout(null);
    setContentPane(externo);

    JPanel interno = new JPanel();
    interno.setBounds(25, 20, larguraInterno, alturaInterno);
    interno.setBackground(corInterna);
    interno.setLayout(null);
    externo.add(interno);

    JLabel lblLogoSoloFirme = new JLabel();
    ImageIcon iconeSoloFirme = new ImageIcon("resources/Images/Solo Firme.png");
    Image imgPjp = iconeSoloFirme.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
    lblLogoSoloFirme.setIcon(new ImageIcon(imgPjp));
    lblLogoSoloFirme.setBounds(11, 6, 220, 220);
    interno.add(lblLogoSoloFirme);

    JSeparator separador = new JSeparator();
    separador.setOrientation(SwingConstants.VERTICAL);
    separador.setBounds(235, 0, 2, alturaInterno);
    separador.setForeground(corBorda);
    interno.add(separador);

    Object[][] botoes = {
      {"Atendimentos", (Runnable) () -> new AtendimentoSuporte().setVisible(true)},
      {"Alunos", (Runnable) () -> new AlunosSecretaria().setVisible(true)},
      {"Avisos", (Runnable) () -> new AvisosSecretaria().setVisible(true)},
      {"Cadastros", (Runnable) () -> new MenuCadastro().setVisible(true)},
      {"Documentos", (Runnable) () -> new DocumentosSecretaria().setVisible(true)},
      {"Meus dados", (Runnable) () -> new MeusDadosFuncionario().setVisible(true)},
      {"Relatórios", (Runnable) () -> new Relatorio().setVisible(true)},
      {"Usuários", (Runnable) () -> new view.Secretaria.AlterarSenhaUsuario().setVisible(true)}
    };

    int yBotao = 240;

    for (Object[] item : botoes) {
      String textoBotao = (String) item[0];
      Runnable acao = (Runnable) item[1];
      JButton botao = new JButton(textoBotao);
      botao.setBounds(17, yBotao, 200, 52);
      estilizarBotao(botao);
      botao.addActionListener(e -> acao.run());
      interno.add(botao);
      yBotao += 65;
    }

    JButton btnSair = new JButton("Sair");
    btnSair.setBounds(17, alturaInterno - 90, 200, 60);
    estilizarBotao(btnSair);
    btnSair.addActionListener(
        e -> {
          SessaoUsuario.encerrarSessao();
          System.exit(EXIT_ON_CLOSE);
        });
    interno.add(btnSair);

    String nomeFuncionario = DadosSistema.nomeUsuarioLogado();
    JLabel usuario = new JLabel("Olá, " + nomeFuncionario);
    usuario.setForeground(textos);
    usuario.setFont(new Font("Segoe UI", Font.BOLD, 20));
    usuario.setBounds(larguraInterno - 320, 30, 280, 30);
    interno.add(usuario);

    JLabel bemVindo = new JLabel("Painel administrativo");
    bemVindo.setForeground(textos);
    bemVindo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    bemVindo.setBounds(335, 90, 500, 40);
    interno.add(bemVindo);

    JLabel nome = new JLabel(nomeFuncionario);
    nome.setForeground(corLabel);
    nome.setFont(new Font("Segoe UI", Font.BOLD, 48));
    nome.setBounds(335, 135, 720, 65);
    interno.add(nome);

    JLabel descricao =
        new JLabel(
            "Gerencie atendimentos, documentos, comunicados e rotinas internas da instituição.");
    descricao.setForeground(textos);
    descricao.setFont(new Font("Segoe UI", Font.PLAIN, 21));
    descricao.setBounds(345, 215, 950, 30);
    interno.add(descricao);

    JPanel cardAtendimentos = criarCard("Atendimentos Hoje", "12", "Secretaria");
    cardAtendimentos.setBounds(350, 290, 370, 140);
    interno.add(cardAtendimentos);

    JPanel cardDocumentos = criarCard("Documentos Pendentes", "7", "Aguardando análise");
    cardDocumentos.setBounds(740, 290, 370, 140);
    interno.add(cardDocumentos);

    JPanel cardTarefas = criarCard("Tarefas Internas", "5", "Em aberto");
    cardTarefas.setBounds(1130, 290, 370, 140);
    interno.add(cardTarefas);

    JPanel rotina = criarPainelTitulo("Rotina Administrativa");
    rotina.setBounds(350, 460, 760, 360);
    interno.add(rotina);

    adicionarLinhaResumo(rotina, "08:00", "Organizar documentos recebidos", 85);
    adicionarLinhaResumo(rotina, "10:00", "Atendimento aos responsáveis", 135);
    adicionarLinhaResumo(rotina, "13:30", "Atualização de cadastros", 185);
    adicionarLinhaResumo(rotina, "15:00", "Envio de comunicados internos", 235);
    adicionarLinhaResumo(rotina, "17:00", "Fechamento de relatórios", 285);

    JPanel alertas = criarPainelTitulo("Alertas Internos");
    alertas.setBounds(1130, 460, 370, 360);
    interno.add(alertas);

    adicionarAviso(alertas, "Documentos vencendo", "3 documentos exigem atenção", 80);
    adicionarAviso(alertas, "Novos chamados", "5 solicitações abertas", 170);
    adicionarAviso(alertas, "Reunião interna", "Hoje às 16:30", 260);

    JPanel calendario = criarCalendarioPremiumPersonalizado();
    calendario.setBounds(larguraInterno - 360, 290, 350, 530);
    interno.add(calendario);

    JPanel rodape = new JPanel();
    rodape.setLayout(null);
    rodape.setBackground(corCampo);
    rodape.setBorder(new LineBorder(corBorda, 1, true));
    rodape.setBounds(335, alturaInterno - 140, larguraInterno - 350, 120);
    interno.add(rodape);

    JLabel fique = new JLabel("Central de trabalho");
    fique.setForeground(textos);
    fique.setFont(new Font("Segoe UI", Font.BOLD, 28));
    fique.setBounds(45, 20, 500, 30);
    rodape.add(fique);

    JLabel texto =
        new JLabel(
            "Acompanhe suas tarefas e mantenha os processos administrativos sempre atualizados.");
    texto.setForeground(textos);
    texto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    texto.setBounds(45, 60, 950, 30);
    rodape.add(texto);

    setVisible(true);
  }

  private void estilizarBotao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 17));
    botao.setForeground(textos);
    botao.setBackground(corCampo);
    botao.setBorder(new LineBorder(corBorda, 1, true));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  private JPanel criarCalendarioPremiumPersonalizado() {

    JPanel calendario = criarPainelArredondado(corCampo, corBorda, 28);
    calendario.setLayout(null);

    JLabel titulo = new JLabel("Calendário");
    titulo.setForeground(Color.WHITE);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    titulo.setBounds(30, 18, 250, 35);
    calendario.add(titulo);

    JLabel subtitulo = new JLabel("Rotina institucional");
    subtitulo.setForeground(textos);
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    subtitulo.setBounds(32, 55, 220, 22);
    calendario.add(subtitulo);

    JPanel linha = new JPanel();
    linha.setBackground(corLabel);
    linha.setBounds(30, 88, 280, 3);
    calendario.add(linha);

    JButton btnAnterior = criarBotaoCalendario("‹");
    btnAnterior.setBounds(30, 105, 42, 34);
    btnAnterior.setBackground(corCampo);
    btnAnterior.setBorder(new LineBorder(corBorda, 1, true));
    calendario.add(btnAnterior);

    lblMesAno = new JLabel("", SwingConstants.CENTER);
    lblMesAno.setForeground(Color.WHITE);
    lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 17));
    lblMesAno.setBounds(80, 105, 200, 34);
    calendario.add(lblMesAno);

    JButton btnProximo = criarBotaoCalendario("›");
    btnProximo.setBounds(295, 105, 42, 34);
    btnProximo.setBackground(corCampo);
    btnProximo.setBorder(new LineBorder(corBorda, 1, true));
    calendario.add(btnProximo);

    JPanel semana = new JPanel(new GridLayout(1, 7, 6, 0));
    semana.setOpaque(false);
    semana.setBounds(30, 155, 295, 25);
    calendario.add(semana);

    String[] diasSemana = {"D", "S", "T", "Q", "Q", "S", "S"};

    for (String dia : diasSemana) {
      JLabel lbl = new JLabel(dia, SwingConstants.CENTER);
      lbl.setForeground(textos);
      lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
      semana.add(lbl);
    }

    gradeDias = new JPanel(new GridLayout(6, 7, 6, 6));
    gradeDias.setOpaque(false);
    gradeDias.setBounds(30, 185, 295, 210);
    calendario.add(gradeDias);

    JLabel eventosTitulo = new JLabel("Compromissos");
    eventosTitulo.setForeground(Color.WHITE);
    eventosTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
    eventosTitulo.setBounds(30, 375, 250, 25);
    calendario.add(eventosTitulo);

    adicionarEventoCalendario(calendario, "25/05", "Relatório mensal", 410);
    adicionarEventoCalendario(calendario, "28/05", "Entrega de documentos", 445);
    adicionarEventoCalendario(calendario, "30/05", "Reunião da equipe", 480);

    btnAnterior.addActionListener(
        e -> {
          mesAtual.add(Calendar.MONTH, -1);
          atualizarCalendario();
        });

    btnProximo.addActionListener(
        e -> {
          mesAtual.add(Calendar.MONTH, 1);
          atualizarCalendario();
        });

    atualizarCalendario();

    return calendario;
  }

  private void atualizarCalendario() {

    gradeDias.removeAll();

    String[] meses = {
      "Janeiro",
      "Fevereiro",
      "Março",
      "Abril",
      "Maio",
      "Junho",
      "Julho",
      "Agosto",
      "Setembro",
      "Outubro",
      "Novembro",
      "Dezembro"
    };

    int mes = mesAtual.get(Calendar.MONTH);
    int ano = mesAtual.get(Calendar.YEAR);

    lblMesAno.setText(meses[mes] + " " + ano);

    Calendar calendario = Calendar.getInstance();
    calendario.set(Calendar.YEAR, ano);
    calendario.set(Calendar.MONTH, mes);
    calendario.set(Calendar.DAY_OF_MONTH, 1);

    int primeiroDiaSemana = calendario.get(Calendar.DAY_OF_WEEK);
    int totalDias = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);

    Calendar hoje = Calendar.getInstance();
    int espacosAntes = primeiroDiaSemana - 1;

    for (int i = 0; i < espacosAntes; i++) {
      gradeDias.add(criarCelulaVazia());
    }

    for (int dia = 1; dia <= totalDias; dia++) {

      boolean ehHoje =
          dia == hoje.get(Calendar.DAY_OF_MONTH)
              && mes == hoje.get(Calendar.MONTH)
              && ano == hoje.get(Calendar.YEAR);

      boolean temEvento = dia == 25 || dia == 28 || dia == 30;

      gradeDias.add(criarCelulaDia(dia, ehHoje, temEvento));
    }

    int totalComponentes = espacosAntes + totalDias;

    while (totalComponentes < 42) {
      gradeDias.add(criarCelulaVazia());
      totalComponentes++;
    }

    gradeDias.revalidate();
    gradeDias.repaint();
  }

  private JPanel criarCelulaDia(int dia, boolean ehHoje, boolean temEvento) {

    Color fundoNormal = new Color(31, 10, 90);
    Color fundoHover = new Color(55, 20, 135);
    Color fundoHoje = new Color(255, 120, 220);

    JPanel celula =
        criarPainelArredondado(
            ehHoje ? fundoHoje : fundoNormal, temEvento ? corLabel : new Color(60, 140, 150), 16);

    celula.setLayout(null);
    celula.setCursor(new Cursor(Cursor.HAND_CURSOR));

    JLabel numero = new JLabel(String.valueOf(dia), SwingConstants.CENTER);
    numero.setForeground(Color.WHITE);
    numero.setFont(new Font("Segoe UI", Font.BOLD, 13));
    numero.setBounds(5, 4, 36, 20);
    celula.add(numero);

    if (temEvento) {
      JPanel ponto = new JPanel();
      ponto.setBackground(ehHoje ? Color.WHITE : corLabel);
      ponto.setBounds(20, 27, 6, 6);
      celula.add(ponto);
    }

    celula.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            if (!ehHoje) celula.setBackground(fundoHover);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            if (!ehHoje) celula.setBackground(fundoNormal);
          }
        });

    return celula;
  }

  private JPanel criarCelulaVazia() {
    JPanel vazio = new JPanel();
    vazio.setOpaque(false);
    return vazio;
  }

  private JButton criarBotaoCalendario(String texto) {
    JButton botao = new JButton(texto);
    botao.setForeground(Color.WHITE);
    botao.setBackground(textos);
    botao.setFont(new Font("Segoe UI", Font.BOLD, 24));
    botao.setBorder(new LineBorder(corBorda, 1, true));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return botao;
  }

  private void adicionarEventoCalendario(JPanel painel, String data, String titulo, int y) {

    JPanel evento = criarPainelArredondado(corCampo, corBorda, 18);
    evento.setLayout(null);
    evento.setBounds(30, y, 295, 32);

    JLabel lbData = new JLabel(data);
    lbData.setForeground(corLabel);
    lbData.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lbData.setBounds(17, 5, 55, 22);
    evento.add(lbData);

    JLabel lbTitulo = new JLabel(titulo);
    lbTitulo.setForeground(Color.WHITE);
    lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lbTitulo.setBounds(80, 5, 210, 22);
    evento.add(lbTitulo);

    painel.add(evento);
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

  private JPanel criarCard(String titulo, String valor, String descricao) {

    JPanel card = criarPainelArredondado(corCampo, corBorda, 24);
    card.setLayout(null);

    JLabel lbTitulo = new JLabel(titulo, SwingConstants.CENTER);
    lbTitulo.setForeground(textos);
    lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lbTitulo.setBounds(15, 15, 350, 25);
    card.add(lbTitulo);

    JLabel lbValor = new JLabel(valor, SwingConstants.CENTER);
    lbValor.setForeground(textos);
    lbValor.setFont(new Font("Segoe UI", Font.BOLD, 42));
    lbValor.setBounds(15, 45, 350, 45);
    card.add(lbValor);

    JLabel lbDescricao = new JLabel(descricao, SwingConstants.CENTER);
    lbDescricao.setForeground(corLabel);
    lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lbDescricao.setBounds(15, 100, 350, 20);
    card.add(lbDescricao);

    return card;
  }

  private JPanel criarPainelTitulo(String titulo) {

    JPanel painel = criarPainelArredondado(corCampo, corBorda, 24);
    painel.setLayout(null);

    JLabel label = new JLabel(titulo);
    label.setForeground(textos);
    label.setFont(new Font("Segoe UI", Font.BOLD, 28));
    label.setBounds(35, 20, 400, 30);
    painel.add(label);

    return painel;
  }

  private void adicionarLinhaResumo(JPanel painel, String horario, String atividade, int y) {

    JLabel lbHorario = new JLabel(horario);
    lbHorario.setForeground(corLabel);
    lbHorario.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lbHorario.setBounds(40, y, 120, 30);
    painel.add(lbHorario);

    JLabel lbAtividade = new JLabel(atividade);
    lbAtividade.setForeground(Color.WHITE);
    lbAtividade.setFont(new Font("Segoe UI", Font.PLAIN, 21));
    lbAtividade.setBounds(165, y, 540, 30);
    painel.add(lbAtividade);
  }

  private void adicionarAviso(JPanel painel, String titulo, String descricao, int y) {

    JLabel lbTitulo = new JLabel(titulo);
    lbTitulo.setForeground(textos);
    lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
    lbTitulo.setBounds(30, y, 350, 30);
    painel.add(lbTitulo);

    JLabel lbDescricao = new JLabel(descricao);
    lbDescricao.setForeground(textos);
    lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lbDescricao.setBounds(30, y + 35, 350, 20);
    painel.add(lbDescricao);
  }
}
