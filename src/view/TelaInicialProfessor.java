package view;

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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.AlunoDAO;
import dao.AvisoDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Aviso;
import model.Turma;
import util.DadosSistema;
import util.SessaoUsuario;

public class TelaInicialProfessor extends JFrame {

	private static final long serialVersionUID = 1L;

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

	private List<Turma> turmasProfessor = new ArrayList<>();
	private List<Aviso> avisosProfessor = new ArrayList<>();

	private int totalAlunos;

	public TelaInicialProfessor() {

		setTitle("Professor");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		margem = 30;

		larguraInterno = areaUtil.width - (margem * 2) + 20;

		alturaInterno = areaUtil.height - (margem * 2);

		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);

		carregarDadosBanco();

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

		criarCabecalho(interno);

		criarMenu(interno);

		criarCards(interno);

		criarResumoTurmas(interno);

		criarAvisos(interno);

		JPanel calendario = criarCalendarioPremiumPersonalizado();

		calendario.setBounds(larguraInterno - 380, 300, 350, 520);

		interno.add(calendario);

		criarRodape(interno);

		setVisible(true);
	}

	/**
	 * Carrega as informações reais do professor.
	 */
	private void carregarDadosBanco() {

		int professorId = DadosSistema.professorIdAtual();

		if (professorId <= 0) {

			throw new IllegalStateException("Não foi possível identificar o professor logado.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			TurmaDAO turmaDAO = new TurmaDAO(conn);

			List<Turma> turmas = turmaDAO.listarPorProfessor(professorId);

			if (turmas != null) {
				turmasProfessor = turmas;
			} else {
				turmasProfessor = new ArrayList<>();
			}

			carregarQuantidadeAlunos();

			carregarAvisos(conn);

		} catch (SQLException e) {

			throw new IllegalStateException("Erro ao carregar os dados do professor.", e);
		}
	}

	/**
	 * Calcula a quantidade real de alunos das turmas do professor.
	 */
	private void carregarQuantidadeAlunos() {

		totalAlunos = 0;

		try (Connection conn = ConnectionFactory.getConnection()) {

			AlunoDAO alunoDAO = new AlunoDAO(conn);

			for (Turma turma : turmasProfessor) {

				if (turma == null) {
					continue;
				}

				List<Aluno> alunos = alunoDAO.listarPorTurma(turma.getIdTurma());

				if (alunos != null) {
					totalAlunos += alunos.size();
				}
			}

		} catch (SQLException e) {

			throw new IllegalStateException("Erro ao carregar os alunos das turmas.", e);
		}
	}

	/**
	 * Carrega os avisos existentes no banco.
	 */
	private void carregarAvisos(Connection conn) throws SQLException {

		AvisoDAO avisoDAO = new AvisoDAO(conn);

		List<Aviso> avisos = avisoDAO.listar();

		avisosProfessor = new ArrayList<>();

		if (avisos == null) {
			return;
		}

		for (Aviso aviso : avisos) {

			if (aviso == null) {
				continue;
			}

			String publico = aviso.getPublico();

			if (publico == null || publico.isBlank()) {
				continue;
			}

			String publicoNormalizado = publico.trim().toLowerCase();

			/*
			 * Aceita avisos destinados ao professor e avisos gerais.
			 */
			if (publicoNormalizado.contains("professor") || publicoNormalizado.contains("professores")
					|| publicoNormalizado.contains("todos") || publicoNormalizado.contains("geral")) {

				avisosProfessor.add(aviso);
			}
		}
	}

	private void criarCabecalho(JPanel interno) {

		JLabel lblLogoSoloFirme = new JLabel();

		// 1. Carrega a URL da imagem no Classpath (pasta de recursos)
		java.net.URL logoUrl = getClass().getResource("/Images/Solo-Firme.png");

		if (logoUrl != null) {
		    // 2. Instancia o ImageIcon a partir da URL
		    ImageIcon iconeSoloFirme = new ImageIcon(logoUrl);
		    
		    // 3. Redimensiona a imagem
		    Image imgPjp = iconeSoloFirme.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
		    
		    // 4. Atribui o novo ícone redimensionado ao JLabel
		    lblLogoSoloFirme.setIcon(new ImageIcon(imgPjp));
		} else {
		    System.err.println("Erro: Imagem '/Images/Solo-Firme.png' não encontrada no classpath!");
		}

		lblLogoSoloFirme.setBounds(6, 6, 220, 220);
		interno.add(lblLogoSoloFirme);

		JSeparator separador = new JSeparator();

		separador.setOrientation(SwingConstants.VERTICAL);

		separador.setBounds(230, 0, 2, alturaInterno);

		separador.setForeground(corBorda);

		interno.add(separador);

		String nomeProfessor = obterNomeProfessor();

		JLabel usuario = new JLabel("Olá, " + nomeProfessor);

		usuario.setForeground(textos);
		usuario.setFont(new Font("Segoe UI", Font.BOLD, 20));

		usuario.setBounds(larguraInterno - 400, 30, 360, 30);

		interno.add(usuario);

		JLabel bemVindo = new JLabel("Bem-vindo de volta,");

		bemVindo.setForeground(textos);
		bemVindo.setFont(new Font("Segoe UI", Font.BOLD, 28));

		bemVindo.setBounds(320, 100, 400, 40);

		interno.add(bemVindo);

		JLabel nome = new JLabel(nomeProfessor + "! 👋");

		nome.setForeground(corLabel);
		nome.setFont(new Font("Segoe UI", Font.BOLD, 50));

		nome.setBounds(320, 140, 700, 70);

		interno.add(nome);

		JLabel descricao = new JLabel("Gerencie suas turmas, aulas, notas, frequências e avisos em um só lugar.");

		descricao.setForeground(textos);
		descricao.setFont(new Font("Segoe UI", Font.PLAIN, 22));

		descricao.setBounds(320, 220, 900, 30);

		interno.add(descricao);
	}

	private String obterNomeProfessor() {

		String nome = DadosSistema.nomeUsuarioLogado();

		if (nome == null || nome.isBlank()) {

			return "Professor";
		}

		return nome;
	}

	private void criarMenu(JPanel interno) {

		String[] botoes = { "Meus Dados", "Minhas Turmas", "Disciplinas", "Lançar Notas", "Chamada", "Avisos" };

		int yBotao = 240;

		for (String textoBotao : botoes) {
			JButton botao = new JButton(textoBotao);
			botao.setBounds(12, yBotao, 200, 55);
			estilizarBotao(botao);
			
			botao.addActionListener(E ->{
				switch(textoBotao) {
					case "Meus Dados":
						new MeusDadosProfessor().setVisible(true);
						break;
					case "Minhas Turmas":
						new MinhasTurmasProfessor().setVisible(true);
						break;
					case "Disciplinas":
						new DisciplinasProfessor().setVisible(true);
						break;
					case "Lançar Notas":
						new LancamentoNota().setVisible(true);
						break;
					case "Chamada":
						new ChamadaAluno().setVisible(true);
						break;
					case "Avisos":
						new AvisosSecretaria().setVisible(true);
						break;
					default:
						break;
				}
			});
			
			interno.add(botao);

			yBotao += 65;
		}

		JButton btnSair = new JButton("Sair");

		btnSair.setBounds(12, alturaInterno - 90, 200, 60);

		estilizarBotao(btnSair);

		btnSair.addActionListener(e -> {

			SessaoUsuario.encerrarSessao();

			dispose();

			System.exit(0);
		});

		interno.add(btnSair);
	}

	private void criarCards(JPanel interno) {

		JPanel cardTurmas = criarCard("Turmas Ativas", String.valueOf(turmasProfessor.size()), obterDescricaoTurmas());

		cardTurmas.setBounds(320, 300, 370, 140);

		interno.add(cardTurmas);

		JPanel cardAlunos = criarCard("Alunos", String.valueOf(totalAlunos), "Total nas suas turmas");

		cardAlunos.setBounds(710, 300, 370, 140);

		interno.add(cardAlunos);

		JPanel cardAvisos = criarCard("Avisos", String.valueOf(avisosProfessor.size()), obterDescricaoAvisos());

		cardAvisos.setBounds(1100, 300, 370, 140);

		interno.add(cardAvisos);
	}

	private String obterDescricaoTurmas() {

		if (turmasProfessor.isEmpty()) {
			return "Nenhuma turma vinculada";
		}

		if (turmasProfessor.size() == 1) {
			return "1 turma vinculada";
		}

		return "Turmas vinculadas";
	}

	private String obterDescricaoAvisos() {

		if (avisosProfessor.isEmpty()) {
			return "Nenhum aviso cadastrado";
		}

		if (avisosProfessor.size() == 1) {
			return "1 aviso disponível";
		}

		return "Avisos disponíveis";
	}

	private void criarResumoTurmas(JPanel interno) {

		JPanel painel = criarPainelTitulo("Minhas Turmas");

		painel.setBounds(320, 470, 760, 350);

		interno.add(painel);

		if (turmasProfessor.isEmpty()) {

			JLabel vazio = new JLabel("Nenhuma turma vinculada ao professor.");

			vazio.setForeground(textos);
			vazio.setFont(new Font("Segoe UI", Font.PLAIN, 18));

			vazio.setBounds(30, 80, 680, 30);

			painel.add(vazio);

			return;
		}

		int y = 80;

		int limite = Math.min(turmasProfessor.size(), 4);

		for (int i = 0; i < limite; i++) {

			Turma turma = turmasProfessor.get(i);

			if (turma == null) {
				continue;
			}

			adicionarTurma(painel, turma, y);

			y += 60;
		}

		if (turmasProfessor.size() > limite) {

			JLabel mais = new JLabel("Mais " + (turmasProfessor.size() - limite) + " turma(s) vinculada(s).");

			mais.setForeground(corLabel);
			mais.setFont(new Font("Segoe UI", Font.PLAIN, 14));

			mais.setBounds(30, y, 650, 25);

			painel.add(mais);
		}
	}

	private void adicionarTurma(JPanel painel, Turma turma, int y) {

		String descricao = turma.getDescricaoTurma();

		if (descricao == null || descricao.isBlank()) {

			descricao = "Turma " + turma.getIdTurma();
		}

		JLabel nome = new JLabel(descricao);

		nome.setForeground(textos);
		nome.setFont(new Font("Segoe UI", Font.BOLD, 20));

		nome.setBounds(30, y, 400, 30);

		painel.add(nome);

		String turno = turma.getTurno() == null ? "Turno não informado" : turma.getTurno().toString();

		JLabel lbTurno = new JLabel(turno);

		lbTurno.setForeground(corLabel);
		lbTurno.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		lbTurno.setBounds(500, y, 200, 30);

		painel.add(lbTurno);
	}

	private void criarAvisos(JPanel interno) {

		JPanel painel = criarPainelTitulo("Avisos");

		painel.setBounds(1100, 470, 370, 350);

		interno.add(painel);

		if (avisosProfessor.isEmpty()) {

			JLabel vazio = new JLabel("<html>Nenhum aviso<br>" + "cadastrado.</html>");

			vazio.setForeground(textos);
			vazio.setFont(new Font("Segoe UI", Font.PLAIN, 16));

			vazio.setBounds(25, 80, 320, 55);

			painel.add(vazio);

			return;
		}

		int y = 80;

		int limite = Math.min(avisosProfessor.size(), 3);

		for (int i = 0; i < limite; i++) {

			Aviso aviso = avisosProfessor.get(i);

			if (aviso == null) {
				continue;
			}

			adicionarAvisoReal(painel, aviso, y);

			y += 85;
		}
	}

	private void adicionarAvisoReal(JPanel painel, Aviso aviso, int y) {

		String titulo = valorOuNaoInformado(aviso.getTitulo());

		JLabel lbTitulo = new JLabel(titulo);

		lbTitulo.setForeground(textos);
		lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));

		lbTitulo.setBounds(25, y, 320, 25);

		painel.add(lbTitulo);

		String descricao = valorOuNaoInformado(aviso.getDescricao());

		if (descricao.length() > 55) {

			descricao = descricao.substring(0, 52) + "...";
		}

		JLabel lbDescricao = new JLabel("<html>" + descricao + "</html>");

		lbDescricao.setForeground(textos);
		lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		lbDescricao.setBounds(25, y + 28, 320, 30);

		painel.add(lbDescricao);

		if (aviso.getDataAviso() != null) {

			JLabel data = new JLabel(formatarData(aviso.getDataAviso()));

			data.setForeground(corLabel);
			data.setFont(new Font("Segoe UI", Font.PLAIN, 11));

			data.setBounds(25, y + 55, 320, 20);

			painel.add(data);
		}
	}

	private JPanel criarCalendarioPremiumPersonalizado() {

		JPanel calendario = criarPainelArredondado(corCampo, corBorda, 28);

		calendario.setLayout(null);

		JLabel titulo = new JLabel("Calendário");

		titulo.setForeground(Color.WHITE);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));

		titulo.setBounds(25, 18, 250, 35);

		calendario.add(titulo);

		JLabel subtitulo = new JLabel("Avisos cadastrados");

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

		String[] diasSemana = { "D", "S", "T", "Q", "Q", "S", "S" };

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

		JLabel eventosTitulo = new JLabel("Próximos avisos");

		eventosTitulo.setForeground(Color.WHITE);
		eventosTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

		eventosTitulo.setBounds(25, 375, 250, 25);

		calendario.add(eventosTitulo);

		adicionarEventosReais(calendario);

		btnAnterior.addActionListener(e -> {

			mesAtual.add(Calendar.MONTH, -1);

			atualizarCalendario();
		});

		btnProximo.addActionListener(e -> {

			mesAtual.add(Calendar.MONTH, 1);

			atualizarCalendario();
		});

		atualizarCalendario();

		return calendario;
	}

	private void adicionarEventosReais(JPanel painel) {

		if (avisosProfessor.isEmpty()) {

			JLabel vazio = new JLabel("Nenhum evento cadastrado.");

			vazio.setForeground(textos);
			vazio.setFont(new Font("Segoe UI", Font.PLAIN, 13));

			vazio.setBounds(25, 410, 295, 22);

			painel.add(vazio);

			return;
		}

		int y = 410;

		int limite = Math.min(avisosProfessor.size(), 3);

		for (int i = 0; i < limite; i++) {

			Aviso aviso = avisosProfessor.get(i);

			if (aviso == null) {
				continue;
			}

			String data = aviso.getDataAviso() == null ? "--/--"
					: String.format("%02d/%02d", aviso.getDataAviso().getDayOfMonth(),
							aviso.getDataAviso().getMonthValue());

			adicionarEventoCalendario(painel, data, valorOuNaoInformado(aviso.getTitulo()), y);

			y += 35;
		}
	}

	private void atualizarCalendario() {

		gradeDias.removeAll();

		String[] meses = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro",
				"Outubro", "Novembro", "Dezembro" };

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

			boolean ehHoje = dia == hoje.get(Calendar.DAY_OF_MONTH) && mes == hoje.get(Calendar.MONTH)
					&& ano == hoje.get(Calendar.YEAR);

			boolean temEvento = existeAvisoNoDia(dia, mes, ano);

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

	private boolean existeAvisoNoDia(int dia, int mesCalendar, int ano) {

		int mes = mesCalendar + 1;

		for (Aviso aviso : avisosProfessor) {

			if (aviso == null || aviso.getDataAviso() == null) {
				continue;
			}

			LocalDate data = aviso.getDataAviso();

			if (data.getDayOfMonth() == dia && data.getMonthValue() == mes && data.getYear() == ano) {

				return true;
			}
		}

		return false;
	}

	private JPanel criarCelulaDia(int dia, boolean ehHoje, boolean temEvento) {

		Color fundoNormal = new Color(31, 10, 90);

		Color fundoHover = new Color(55, 20, 135);

		Color fundoHoje = new Color(255, 120, 220);

		JPanel celula = criarPainelArredondado(ehHoje ? fundoHoje : fundoNormal,
				temEvento ? corLabel : new Color(80, 45, 160), 16);

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

		celula.addMouseListener(new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {

				if (!ehHoje) {
					celula.setBackground(fundoHover);
				}
			}

			public void mouseExited(MouseEvent e) {

				if (!ehHoje) {
					celula.setBackground(fundoNormal);
				}
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

		JPanel card = criarPainelArredondado(corCampo, corBorda, 24);

		card.setLayout(null);

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

		JPanel painel = criarPainelArredondado(corCampo, corBorda, 24);

		painel.setLayout(null);

		JLabel label = new JLabel(titulo);

		label.setForeground(textos);

		label.setFont(new Font("Segoe UI", Font.BOLD, 28));

		label.setBounds(30, 20, 400, 30);

		painel.add(label);

		return painel;
	}

	private void criarRodape(JPanel interno) {

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

		JLabel texto = new JLabel(
				"Acompanhe suas turmas diariamente para manter as informações acadêmicas atualizadas.");

		texto.setForeground(textos);

		texto.setFont(new Font("Segoe UI", Font.PLAIN, 18));

		texto.setBounds(40, 60, 950, 30);

		rodape.add(texto);
	}

	private void estilizarBotao(JButton botao) {

		botao.setFont(new Font("Segoe UI", Font.BOLD, 18));

		botao.setForeground(textos);

		botao.setBackground(corCampo);

		botao.setBorder(new LineBorder(corBorda, 1, true));

		botao.setFocusPainted(false);

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private String valorOuNaoInformado(String valor) {

		if (valor == null || valor.isBlank()) {

			return "Não informado";
		}

		return valor;
	}

	private String formatarData(LocalDate data) {

		return String.format("%02d/%02d/%04d", data.getDayOfMonth(), data.getMonthValue(), data.getYear());
	}
}