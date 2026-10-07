package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import dao.AlunoDAO;
import dao.AvisoDAO;
import dao.DisciplinaDAO;
import dao.NotaDAO;
import dao.PresencaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Aviso;
import model.Nota;
import model.Presenca;
import model.Usuario;
import util.SessaoUsuario;

public class TelaInicialAluno extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Color ROXO_ESCURO = new Color(48, 25, 52);
	private static final Color ROXO = new Color(91, 45, 104);
	private static final Color ROSA = new Color(214, 93, 135);
	private static final Color FUNDO = new Color(245, 242, 247);
	private static final Color BRANCO = Color.WHITE;
	private static final Color CINZA_TEXTO = new Color(90, 90, 90);
	
	private final int margem;
	@SuppressWarnings("unused")
	private final int larguraInterno;
	@SuppressWarnings("unused")
	private final int alturaInterno;

	private Aluno aluno;
	private int qtdDisciplinasMatriculadas;
	private Double mediaGeral;
	private Double frequenciaMedia;
	private List<Aviso> avisosDoAluno = new ArrayList<>();

	public TelaInicialAluno() {
		setTitle("Aluno");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		margem = 30;
		larguraInterno = areaUtil.width - (margem * 2) + 20;
		alturaInterno = areaUtil.height - (margem * 2); 
		
		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);
		carregarDadosDoBanco();
				
		criarTela();
		setLocationRelativeTo(null);
	}

	private void carregarDadosDoBanco() {
		Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();
		if (usuarioLogado == null || usuarioLogado.getAlunoId() <= 0) {
			throw new IllegalStateException("Nenhum aluno autenticado na sessão atual.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			this.aluno = alunoDAO.buscarPorId(usuarioLogado.getAlunoId());
			if (this.aluno == null) {
				throw new IllegalStateException("Aluno da sessão atual não foi encontrado no banco.");
			}

			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
			this.qtdDisciplinasMatriculadas = disciplinaDAO.listarPorTurma(aluno.getIdTurma()).size();

			NotaDAO notaDAO = new NotaDAO(conn);
			List<Nota> notas = notaDAO.listarPorAluno(aluno.getIdAluno());
			this.mediaGeral = notas.isEmpty() ? null : notas.stream().mapToDouble(Nota::getNota).average().orElse(0);

			PresencaDAO presencaDAO = new PresencaDAO(conn);
			List<Presenca> presencas = presencaDAO.listarPorAluno(aluno.getIdAluno());
			if (presencas.isEmpty()) {
				this.frequenciaMedia = null;
			} else {
				long favoraveis = presencas.stream().filter(p -> p.isPresente() || p.isFaltaAbonada()).count();
				this.frequenciaMedia = (favoraveis * 100.0) / presencas.size();
			}

			AvisoDAO avisoDAO = new AvisoDAO(conn);
			this.avisosDoAluno = avisoDAO.listar().stream()
					.filter(a -> a.getPublico() != null && (a.getPublico().toLowerCase(Locale.ROOT).contains("alun")
							|| a.getPublico().toLowerCase(Locale.ROOT).contains("todos")))
					.filter(a -> a.getTurmaId() == null || a.getTurmaId() == aluno.getIdTurma()).limit(5).toList();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar dados do aluno: " + e.getMessage(),
					"Erro de banco de dados", JOptionPane.ERROR_MESSAGE);
			throw new IllegalStateException("Erro ao carregar dados do aluno.", e);
		}
	}

	private void criarTela() {
		JPanel painelPrincipal = new JPanel(new BorderLayout());
		painelPrincipal.setBackground(FUNDO);
		painelPrincipal.add(criarMenuLateral(), BorderLayout.WEST);
		painelPrincipal.add(criarConteudo(), BorderLayout.CENTER);
		setContentPane(painelPrincipal);
	}

	private JPanel criarMenuLateral() {
		JPanel menu = new JPanel();
		menu.setPreferredSize(new Dimension(230, 0));
		menu.setBackground(ROXO_ESCURO);
		menu.setLayout(new BorderLayout());

		JPanel painelMenu = new JPanel();
		painelMenu.setOpaque(false);
		painelMenu.setBorder(new EmptyBorder(30, 15, 20, 15));
		painelMenu.setLayout(new BoxLayout(painelMenu, BoxLayout.Y_AXIS));

		JLabel titulo = new JLabel("GESTÃO ESCOLAR");
		titulo.setForeground(BRANCO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
		titulo.setAlignmentX(CENTER_ALIGNMENT);

		JLabel subtitulo = new JLabel("Área do Aluno");
		subtitulo.setForeground(new Color(220, 200, 225));
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		subtitulo.setAlignmentX(CENTER_ALIGNMENT);

		painelMenu.add(titulo);
		painelMenu.add(Box.createVerticalStrut(5));
		painelMenu.add(subtitulo);
		painelMenu.add(Box.createVerticalStrut(35));

		String[] botoes = { "Meus Dados", "Disciplinas", "Notas", "Frequência", "Suporte" };

		for (String texto : botoes) {
			JButton botao = criarBotaoMenu(texto);

			botao.addActionListener(e -> {
				switch (texto) {
				case "Meus Dados":
					new MeusDadosAluno().setVisible(true);
					break;
				case "Disciplinas":
					new DisciplinasAluno().setVisible(true);
					break;
				case "Notas":
					new NotasAluno().setVisible(true);
					break;
				case "Frequência":
					new Frequencia().setVisible(true);
					break;
				case "Suporte":
					new SuporteAluno().setVisible(true);
					break;
				default:
					break;
				}
			});

			painelMenu.add(botao);
			painelMenu.add(Box.createVerticalStrut(10));
		}

		menu.add(painelMenu, BorderLayout.NORTH);

		JPanel painelSair = new JPanel(new FlowLayout(FlowLayout.CENTER));
		painelSair.setOpaque(false);
		painelSair.setBorder(new EmptyBorder(0, 10, 20, 10));

		JButton botaoSair = criarBotaoMenu("Sair");
		botaoSair.addActionListener(e -> {
			SessaoUsuario.encerrarSessao();
			dispose();
			System.exit(0);
		});

		painelSair.add(botaoSair);
		menu.add(painelSair, BorderLayout.SOUTH);
		return menu;
	}

	private JButton criarBotaoMenu(String texto) {
		JButton botao = new JButton(texto);
		botao.setPreferredSize(new Dimension(190, 42));
		botao.setMaximumSize(new Dimension(190, 42));
		botao.setMinimumSize(new Dimension(190, 42));
		botao.setAlignmentX(CENTER_ALIGNMENT);
		botao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		botao.setForeground(BRANCO);
		botao.setBackground(ROXO);
		botao.setFocusPainted(false);
		botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		botao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		return botao;
	}

	private JPanel criarConteudo() {
		JPanel painel = new JPanel(new BorderLayout());
		painel.setBackground(FUNDO);
		painel.add(criarCabecalho(), BorderLayout.NORTH);

		JPanel conteudo = new JPanel(new GridBagLayout());
		conteudo.setBackground(FUNDO);
		conteudo.setBorder(new EmptyBorder(20, 25, 25, 25));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;

		JPanel painelInterno = new JPanel();
		painelInterno.setBackground(FUNDO);
		painelInterno.setLayout(new BoxLayout(painelInterno, BoxLayout.Y_AXIS));
		painelInterno.add(criarSaudacao());
		painelInterno.add(Box.createVerticalStrut(20));
		painelInterno.add(criarCards());
		painelInterno.add(Box.createVerticalStrut(20));
		painelInterno.add(criarPainelInferior());

		JScrollPane scroll = new JScrollPane(painelInterno);
		scroll.setBorder(null);
		scroll.getViewport().setBackground(FUNDO);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		conteudo.add(scroll, gbc);
		painel.add(conteudo, BorderLayout.CENTER);
		return painel;
	}

	private JPanel criarCabecalho() {
		JPanel cabecalho = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				GradientPaint gradiente = new GradientPaint(0, 0, ROXO_ESCURO, getWidth(), 0, ROSA);
				g2.setPaint(gradiente);
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.dispose();
			}
		};

		cabecalho.setPreferredSize(new Dimension(0, 75));
		cabecalho.setLayout(new BorderLayout());
		cabecalho.setBorder(new EmptyBorder(10, 25, 10, 25));

		JLabel titulo = new JLabel("Área do Aluno");
		titulo.setForeground(BRANCO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

		JLabel usuario = new JLabel(aluno.getNome());
		usuario.setForeground(BRANCO);
		usuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		cabecalho.add(titulo, BorderLayout.WEST);
		cabecalho.add(usuario, BorderLayout.EAST);
		return cabecalho;
	}

	private JPanel criarSaudacao() {
		JPanel painel = new JPanel();
		painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
		painel.setBackground(BRANCO);
		painel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225, 220, 228)),
				new EmptyBorder(18, 22, 18, 22)));

		String primeiroNome = aluno.getNome().trim().split("\\s+")[0];
		JLabel saudacao = new JLabel("Olá, " + primeiroNome + "! 👋");
		saudacao.setFont(new Font("Segoe UI", Font.BOLD, 25));
		saudacao.setForeground(ROXO_ESCURO);
		JLabel texto = new JLabel("Seja bem-vindo à sua área acadêmica.");
		texto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		texto.setForeground(CINZA_TEXTO);
		painel.add(saudacao);
		painel.add(Box.createVerticalStrut(5));
		painel.add(texto);
		return painel;
	}

	private JPanel criarCards() {
		JPanel painel = new JPanel(new GridBagLayout());
		painel.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.insets = new Insets(0, 0, 0, 12);
		gbc.gridx = 0;
		gbc.gridx = 1;

		String textoMedia = mediaGeral == null ? "-" : String.format(Locale.of("pt", "BR"), "%.2f", mediaGeral);
		String descricaoMedia = mediaGeral == null ? "Nenhuma nota lançada" : avaliarMedia(mediaGeral);

		painel.add(criarCard("Disciplinas Matriculadas", String.valueOf(qtdDisciplinasMatriculadas), "Período atual"),
				gbc);
		painel.add(criarCard("Média Geral", textoMedia, descricaoMedia), gbc);
		gbc.gridx = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		String textoFrequencia = frequenciaMedia == null ? "-"
				: String.format(Locale.of("pt", "BR"), "%.0f%%", frequenciaMedia);
		String descricaoFrequencia = frequenciaMedia == null ? "Nenhum registro" : avaliarFrequencia(frequenciaMedia);
		painel.add(criarCard("Frequência Média", textoFrequencia, descricaoFrequencia), gbc);
		return painel;
	}

	private String avaliarMedia(double media) {
		if (media >= 9)
			return "Excelente!";
		if (media >= 7)
			return "Muito bom!";
		if (media >= 6)
			return "Aprovado";
		return "Atenção necessária";
	}

	private String avaliarFrequencia(double frequencia) {
		if (frequencia >= 90)
			return "Muito bom!";
		if (frequencia >= 75)
			return "Dentro do limite";
		return "Atenção: frequência baixa";
	}

	private JPanel criarCard(String titulo, String valor, String descricao) {
		JPanel card = new JPanel();
		card.setPreferredSize(new Dimension(250, 120));
		card.setBackground(BRANCO);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225, 220, 228)),
				new EmptyBorder(15, 18, 15, 18)));

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblTitulo.setForeground(CINZA_TEXTO);

		JLabel lblValor = new JLabel(valor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblValor.setForeground(ROXO);

		JLabel lblDescricao = new JLabel(descricao);
		lblDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblDescricao.setForeground(CINZA_TEXTO);

		card.add(lblTitulo);
		card.add(Box.createVerticalStrut(5));
		card.add(lblValor);
		card.add(Box.createVerticalStrut(2));
		card.add(lblDescricao);
		return card;
	}

	private JPanel criarPainelInferior() {
		JPanel painel = new JPanel(new GridBagLayout());
		painel.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.insets = new Insets(0, 0, 0, 10);
		gbc.gridx = 0;
		gbc.weightx = 0.6;

		painel.add(criarProximasAulas(), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.4;
		gbc.insets = new Insets(0, 0, 0, 0);
		painel.add(criarAvisos(), gbc);
		return painel;
	}

	private JPanel criarProximasAulas() {
		JPanel painel = criarPainelSecao("Próximas Aulas");

		String[][] aulas = {};

		for (String[] aula : aulas) {
			JPanel linha = new JPanel(new BorderLayout(15, 0));
			linha.setOpaque(false);
			linha.setBorder(new EmptyBorder(8, 5, 8, 5));

			JLabel horario = new JLabel(aula[0]);
			horario.setFont(new Font("Segoe UI", Font.BOLD, 14));
			horario.setForeground(ROSA);
			horario.setPreferredSize(new Dimension(55, 25));

			JLabel disciplina = new JLabel(aula[1]);
			disciplina.setFont(new Font("Segoe UI", Font.BOLD, 13));
			disciplina.setForeground(ROXO_ESCURO);

			JLabel sala = new JLabel(aula[2]);
			sala.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			sala.setForeground(CINZA_TEXTO);

			linha.add(horario, BorderLayout.WEST);
			linha.add(disciplina, BorderLayout.CENTER);
			linha.add(sala, BorderLayout.EAST);
			painel.add(linha);
		}
		return painel;
	}

	private JPanel criarAvisos() {
		JPanel painel = criarPainelSecao("Avisos Recentes");

		if (avisosDoAluno.isEmpty()) {
			JLabel vazio = new JLabel("Nenhum aviso no momento.");
			vazio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			vazio.setForeground(CINZA_TEXTO);
			painel.add(vazio);
			return painel;
		}

		for (Aviso aviso : avisosDoAluno) {
			JPanel linha = new JPanel();
			linha.setOpaque(false);
			linha.setLayout(new BoxLayout(linha, BoxLayout.Y_AXIS));
			linha.setBorder(new EmptyBorder(7, 5, 7, 5));

			JLabel titulo = new JLabel(aviso.getTitulo());
			titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
			titulo.setForeground(ROXO_ESCURO);

			JLabel descricao = new JLabel(aviso.getDescricao());
			descricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			descricao.setForeground(CINZA_TEXTO);

			linha.add(titulo);
			linha.add(Box.createVerticalStrut(3));
			linha.add(descricao);

			painel.add(linha);
		}
		return painel;
	}

	private JPanel criarPainelSecao(String titulo) {
		JPanel painel = new JPanel();
		painel.setBackground(BRANCO);
		painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
		painel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225, 220, 228)),
				new EmptyBorder(15, 18, 15, 18)));

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
		lblTitulo.setForeground(ROXO_ESCURO);
		lblTitulo.setAlignmentX(LEFT_ALIGNMENT);

		painel.add(lblTitulo);
		painel.add(Box.createVerticalStrut(10));
		return painel;
	}
}