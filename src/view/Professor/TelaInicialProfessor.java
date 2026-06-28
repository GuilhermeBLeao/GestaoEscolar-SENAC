package view.Professor;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
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

public class TelaInicialProfessor extends JFrame {

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

  public TelaInicialProfessor() {
    setTitle("Professor");
    // setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Professor.png"));
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
    interno.setBounds(20, 20, larguraInterno, alturaInterno);
    interno.setBackground(corInterna);
    interno.setLayout(null);
    externo.add(interno);

    JLabel lblLogoSoloFirme = new JLabel();
    ImageIcon iconeSoloFirme = new ImageIcon("resources/Images/Solo Firme.png");
    Image imgPjp = iconeSoloFirme.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
    lblLogoSoloFirme.setIcon(new ImageIcon(imgPjp));
    lblLogoSoloFirme.setBounds(6, 6, 220, 220);
    interno.add(lblLogoSoloFirme);

    JSeparator separador = new JSeparator();
    separador.setOrientation(SwingConstants.VERTICAL);
    separador.setBounds(230, 0, 2, alturaInterno);
    separador.setForeground(corBorda);
    interno.add(separador);

    Object[][] botoes = {
      {"Meus dados", (Runnable) () -> new MeusDadosProfessor().setVisible(true)},
      {"Minhas turmas", (Runnable) () -> new MinhasTurmasProfessor().setVisible(true)},
      {"Disciplinas", (Runnable) () -> new DisciplinasProfessor().setVisible(true)},
      {"Lancar notas", (Runnable) () -> new LancamentoNota().setVisible(true)},
      {"Chamada", (Runnable) () -> new ChamadaAluno().setVisible(true)},
      {"Avisos", (Runnable) () -> new EmitirAvisos().setVisible(true)}
    };

    int yBotao = 240;

    for (Object[] item : botoes) {
      String textoBotao = (String) item[0];
      Runnable acao = (Runnable) item[1];
      JButton botao = new JButton(textoBotao);
      botao.setBounds(12, yBotao, 200, 55);
      estilizarBotao(botao);
      botao.addActionListener(e -> acao.run());
      interno.add(botao);
      yBotao += 65;
    }

    JButton btnSair = new JButton("Sair");
    btnSair.setBounds(12, alturaInterno - 90, 200, 60);
    estilizarBotao(btnSair);
    btnSair.addActionListener(
        e -> {
          SessaoUsuario.encerrarSessao();
          System.exit(EXIT_ON_CLOSE);
        });
    interno.add(btnSair);

    String nomeProfessor = DadosSistema.nomeUsuarioLogado();
    JLabel usuario = new JLabel("Olá, " + nomeProfessor);
    usuario.setForeground(textos);
    usuario.setFont(new Font("Segoe UI", Font.BOLD, 20));
    usuario.setBounds(larguraInterno - 320, 30, 280, 30);
    interno.add(usuario);

    JLabel bemVindo = new JLabel("Bem-vindo de volta,");
    bemVindo.setForeground(textos);
    bemVindo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    bemVindo.setBounds(320, 100, 400, 40);
    interno.add(bemVindo);

    JLabel nome = new JLabel(nomeProfessor + "!");
    nome.setForeground(corLabel);
    nome.setFont(new Font("Segoe UI", Font.BOLD, 50));
    nome.setBounds(320, 140, 650, 70);
    interno.add(nome);

    JLabel descricao =
        new JLabel("Gerencie suas turmas, aulas, notas, frequências e avisos em um só lugar.");
    descricao.setForeground(textos);
    descricao.setFont(new Font("Segoe UI", Font.PLAIN, 22));
    descricao.setBounds(320, 220, 900, 30);
    interno.add(descricao);

    JPanel cardTurmas = criarCard("Turmas Ativas", "4", "Em andamento");
    cardTurmas.setBounds(320, 300, 370, 140);
    interno.add(cardTurmas);

    JPanel cardAlunos = criarCard("Alunos", "128", "Total matriculados");
    cardAlunos.setBounds(710, 300, 370, 140);
    interno.add(cardAlunos);

    JPanel cardAulas = criarCard("Aulas Hoje", "3", "Programadas");
    cardAulas.setBounds(1100, 300, 370, 140);
    interno.add(cardAulas);

    JPanel aulas = criarPainelTitulo("Agenda de Aulas");
    aulas.setBounds(320, 470, 760, 350);
    interno.add(aulas);

    adicionarAula(aulas, "08:00", "Matemática Aplicada", "ADS-2", 80);
    adicionarAula(aulas, "10:00", "Programação Orientada a Objetos", "ADS-3", 150);
    adicionarAula(aulas, "14:00", "Banco de Dados", "ADS-1", 220);
    adicionarAula(aulas, "16:00", "Engenharia de Software", "ADS-4", 290);

    JPanel pendencias = criarPainelTitulo("Pendências");
    pendencias.setBounds(1100, 470, 370, 350);
    interno.add(pendencias);

    adicionarAviso(pendencias, "Lançar notas", "Turma ADS-2 até 25/05", 80);
    adicionarAviso(pendencias, "Registrar frequência", "Aula de hoje pendente", 170);
    adicionarAviso(pendencias, "Responder suporte", "2 solicitações abertas", 260);

    JPanel calendario = criarCalendarioPremiumPersonalizado();
    calendario.setBounds(larguraInterno - 380, 300, 350, 520);
    interno.add(calendario);

    JPanel rodape = new JPanel();
    rodape.setLayout(null);
    rodape.setBackground(corCampo);
    rodape.setBorder(new LineBorder(corBorda, 1, true));
    rodape.setBounds(320, alturaInterno - 140, larguraInterno - 350, 120);
    interno.add(rodape);

    JLabel fique = new JLabel("Organize sua rotina!");
    fique.setForeground(textos);
    fique.setFont(new Font("Segoe UI", Font.BOLD, 26));
    fique.setBounds(40, 10, 400, 30);
    rodape.add(fique);

    JLabel texto =
        new JLabel(
            "Acompanhe suas turmas diariamente para manter notas, frequências e avisos"
                + " atualizados.");
    texto.setForeground(textos);
    texto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    texto.setBounds(40, 60, 950, 30);
    rodape.add(texto);

    setVisible(true);
  }

  private void estilizarBotao(JButton botao) {
    botao.setFont(new Font("Segoe UI", Font.BOLD, 18));
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
    titulo.setBounds(25, 18, 250, 35);
    calendario.add(titulo);

    JLabel subtitulo = new JLabel("Agenda do professor");
    subtitulo.setForeground(textos);
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    subtitulo.setBounds(27, 55, 220, 22);
    calendario.add(subtitulo);

    JPanel linha = new JPanel();
    linha.setBackground(corLabel);
    linha.setBounds(25, 88, 280, 3);
    calendario.add(linha);

    JButton btnAnterior = criarBotaoCalendario("‹");
    btnAnterior.setBounds(25, 105, 42, 34);
    calendario.add(btnAnterior);

    lblMesAno = new JLabel("", SwingConstants.CENTER);
    lblMesAno.setForeground(Color.WHITE);
    lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 17));
    lblMesAno.setBounds(75, 105, 200, 34);
    calendario.add(lblMesAno);

    JButton btnProximo = criarBotaoCalendario("›");
    btnProximo.setBounds(280, 105, 42, 34);
    calendario.add(btnProximo);

    JPanel semana = new JPanel(new GridLayout(1, 7, 6, 0));
    semana.setOpaque(false);
    semana.setBounds(25, 155, 295, 25);
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
    gradeDias.setBounds(25, 185, 295, 210);
    calendario.add(gradeDias);

    JLabel eventosTitulo = new JLabel("Compromissos");
    eventosTitulo.setForeground(Color.WHITE);
    eventosTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
    eventosTitulo.setBounds(25, 375, 250, 25);
    calendario.add(eventosTitulo);

    adicionarEventoCalendario(calendario, "25/05", "Fechamento de notas", 410);
    adicionarEventoCalendario(calendario, "28/05", "Conselho de classe", 455);

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

      boolean temEvento = dia == 25 || dia == 28;

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
            ehHoje ? fundoHoje : fundoNormal, temEvento ? corLabel : new Color(80, 45, 160), 16);

    celula.setLayout(null);
    celula.setCursor(new Cursor(Cursor.HAND_CURSOR));

    JLabel numero = new JLabel(String.valueOf(dia), SwingConstants.CENTER);
    numero.setForeground(Color.WHITE);
    numero.setFont(new Font("Segoe UI", Font.BOLD, 13));
    numero.setBounds(0, 4, 36, 20);
    celula.add(numero);

    if (temEvento) {
      JPanel ponto = new JPanel();
      ponto.setBackground(ehHoje ? Color.WHITE : corLabel);
      ponto.setBounds(15, 27, 6, 6);
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
    botao.setBackground(corInterna);
    botao.setFont(new Font("Segoe UI", Font.BOLD, 24));
    botao.setBorder(new LineBorder(corBorda, 1, true));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return botao;
  }

  private void adicionarEventoCalendario(JPanel painel, String data, String titulo, int y) {
    JPanel evento = criarPainelArredondado(corCampo, corBorda, 18);
    evento.setLayout(null);
    evento.setBounds(25, y, 295, 32);

    JLabel lbData = new JLabel(data);
    lbData.setForeground(corLabel);
    lbData.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lbData.setBounds(12, 5, 55, 22);
    evento.add(lbData);

    JLabel lbTitulo = new JLabel(titulo);
    lbTitulo.setForeground(Color.WHITE);
    lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lbTitulo.setBounds(75, 5, 210, 22);
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
    JPanel card = new JPanel();
    card.setLayout(null);
    card.setBackground(corCampo);
    card.setBorder(new LineBorder(corBorda, 1, true));

    JLabel lbTitulo = new JLabel(titulo, SwingConstants.CENTER);
    lbTitulo.setForeground(textos);
    lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lbTitulo.setBounds(10, 15, 350, 25);
    card.add(lbTitulo);

    JLabel lbValor = new JLabel(valor, SwingConstants.CENTER);
    lbValor.setForeground(textos);
    lbValor.setFont(new Font("Segoe UI", Font.BOLD, 42));
    lbValor.setBounds(10, 45, 350, 45);
    card.add(lbValor);

    JLabel lbDescricao = new JLabel(descricao, SwingConstants.CENTER);
    lbDescricao.setForeground(corLabel);
    lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lbDescricao.setBounds(10, 100, 350, 20);
    card.add(lbDescricao);

    return card;
  }

  private JPanel criarPainelTitulo(String titulo) {
    JPanel painel = new JPanel();
    painel.setLayout(null);
    painel.setBackground(corCampo);
    painel.setBorder(BorderFactory.createLineBorder(corBorda));

    JLabel label = new JLabel(titulo);
    label.setForeground(textos);
    label.setFont(new Font("Segoe UI", Font.BOLD, 28));
    label.setBounds(30, 20, 400, 30);
    painel.add(label);

    return painel;
  }

  private void adicionarAula(JPanel painel, String hora, String materia, String turma, int y) {
    JLabel horario = new JLabel(hora);
    horario.setForeground(corLabel);
    horario.setFont(new Font("Segoe UI", Font.BOLD, 22));
    horario.setBounds(30, y, 120, 30);
    painel.add(horario);

    JLabel disciplina = new JLabel(materia);
    disciplina.setForeground(textos);
    disciplina.setFont(new Font("Segoe UI", Font.BOLD, 22));
    disciplina.setBounds(150, y, 400, 30);
    painel.add(disciplina);

    JLabel lbTurma = new JLabel(turma);
    lbTurma.setForeground(corLabel);
    lbTurma.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    lbTurma.setBounds(560, y, 140, 30);
    painel.add(lbTurma);
  }

  private void adicionarAviso(JPanel painel, String titulo, String descricao, int y) {
    JLabel lbTitulo = new JLabel(titulo);
    lbTitulo.setForeground(textos);
    lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
    lbTitulo.setBounds(25, y, 350, 30);
    painel.add(lbTitulo);

    JLabel lbDescricao = new JLabel(descricao);
    lbDescricao.setForeground(textos);
    lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    lbDescricao.setBounds(25, y + 35, 350, 20);
    painel.add(lbDescricao);
  }
}
