package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.PresencaController;
import dao.AlunoDAO;
import dao.AvisoDAO;
import dao.ChamadoDAO;
import dao.NotaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Aviso;
import model.Nota;
import model.Presenca;
import model.Usuario;
import util.DadosSistema;
import util.SessaoUsuario;

public class TelaInicialPais extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color COR_EXTERNA = new Color(27, 0, 69);
	private static final Color COR_INTERNA = new Color(38, 2, 92);
	private static final Color COR_CAMPO = new Color(25, 6, 75);
	private static final Color COR_BORDA = new Color(120, 70, 220);
	private static final Color COR_LABEL = new Color(255, 120, 220);
	private static final Color COR_ROXO = new Color(91, 45, 104);
	private static final Color COR_TEXTO = Color.WHITE;

	private final int larguraInterno;
	private final int alturaInterno;

	private Usuario usuarioLogado;
	private List<Aluno> alunos = new ArrayList<>();
	private List<Aviso> avisos = new ArrayList<>();

	private double mediaGeral;
	private double frequenciaGeral;
	private int pendencias;

	public TelaInicialPais() {

		setTitle("Responsável");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		larguraInterno = areaUtil.width - 40;
		alturaInterno = areaUtil.height - 40;

		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);

		carregarDadosBanco();

		JPanel externo = new JPanel();
		externo.setBackground(COR_EXTERNA);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		externo.setLayout(null);

		setContentPane(externo);

		JPanel interno = new JPanel();
		interno.setBackground(COR_INTERNA);
		interno.setLayout(null);
		interno.setBounds(25, 20, larguraInterno, alturaInterno);

		externo.add(interno);

		criarCabecalho(interno);
		criarMenu(interno);
		criarCards(interno);
		criarResumoAlunos(interno);
		criarComunicados(interno);
		criarCalendario(interno);
		criarRodape(interno);

		setVisible(true);
	}

	private void carregarDadosBanco() {

		usuarioLogado = SessaoUsuario.getUsuarioLogado();

		if (usuarioLogado == null) {

			throw new IllegalStateException("Nenhum usuário autenticado na sessão atual.");
		}

		if (usuarioLogado.getPaiId() <= 0) {

			throw new IllegalStateException("O usuário logado não possui um responsável vinculado.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			AlunoDAO alunoDAO = new AlunoDAO(conn);

			alunos = alunoDAO.listarPorPais(usuarioLogado.getPaiId());

			if (alunos == null) {
				alunos = new ArrayList<>();
			}

			carregarMedia();
			carregarFrequencia();
			carregarPendencias(conn);
			carregarAvisos(conn);

		} catch (SQLException e) {

			JOptionPane.showMessageDialog(this, "Erro ao carregar os dados do responsável:\n" + e.getMessage(),
					"Erro de banco de dados", JOptionPane.ERROR_MESSAGE);

			throw new IllegalStateException("Não foi possível carregar os dados do responsável.", e);
		}
	}

	private void carregarMedia() {

		if (alunos.isEmpty()) {
			mediaGeral = 0.0;
			return;
		}

		double soma = 0.0;
		int quantidadeNotas = 0;

		for (Aluno aluno : alunos) {

			if (aluno == null) {
				continue;
			}

			try (Connection conn = ConnectionFactory.getConnection()) {

				NotaDAO notaDAO = new NotaDAO(conn);

				List<Nota> notas = notaDAO.listarPorAluno(aluno.getIdAluno());

				if (notas == null) {
					continue;
				}

				for (Nota nota : notas) {

					if (nota == null) {
						continue;
					}

					soma += nota.getNota();
					quantidadeNotas++;
				}

			} catch (SQLException e) {

				throw new IllegalStateException("Erro ao consultar as notas do aluno " + aluno.getNome() + ".", e);
			}
		}

		if (quantidadeNotas == 0) {
			mediaGeral = 0.0;
		} else {
			mediaGeral = soma / quantidadeNotas;
		}
	}

	private void carregarFrequencia() {

		if (alunos.isEmpty()) {
			frequenciaGeral = 0.0;
			return;
		}

		int totalPresencas = 0;
		int totalPresentes = 0;

		PresencaController presencaController = new PresencaController();

		for (Aluno aluno : alunos) {

			if (aluno == null) {
				continue;
			}

			List<Presenca> presencas = presencaController.listarPresencasPorAluno(aluno.getIdAluno());

			if (presencas == null) {
				continue;
			}

			for (Presenca presenca : presencas) {

				if (presenca == null) {
					continue;
				}

				totalPresencas++;

				if (presenca.isPresente()) {
					totalPresentes++;
				}
			}
		}

		if (totalPresencas == 0) {
			frequenciaGeral = 0.0;
		} else {
			frequenciaGeral = (totalPresentes * 100.0) / totalPresencas;
		}
	}

	private void carregarPendencias(Connection conn) throws SQLException {

		ChamadoDAO chamadoDAO = new ChamadoDAO(conn);

		pendencias = 0;

		for (Aluno aluno : alunos) {

			if (aluno == null) {
				continue;
			}

			var chamados = chamadoDAO.listarPorAluno(aluno.getIdAluno());

			if (chamados == null) {
				continue;
			}

			chamados.forEach(chamado -> {

				if (chamado == null) {
					return;
				}

				String status = chamado.getStatus();

				if (status != null && status.equalsIgnoreCase("Aberto")) {

					pendencias++;
				}
			});
		}
	}

	private void carregarAvisos(Connection conn) throws SQLException {

		AvisoDAO avisoDAO = new AvisoDAO(conn);

		List<Aviso> avisosBanco = avisoDAO.listar();

		avisos = new ArrayList<>();

		if (avisosBanco == null) {
			return;
		}

		for (Aviso aviso : avisosBanco) {

			if (aviso == null) {
				continue;
			}

			String publico = aviso.getPublico();

			if (publico == null || publico.isBlank()) {
				continue;
			}

			String publicoNormalizado = publico.trim().toLowerCase();

			if (publicoNormalizado.contains("responsavel") || publicoNormalizado.contains("responsável")
					|| publicoNormalizado.contains("todos") || publicoNormalizado.contains("geral")) {

				avisos.add(aviso);
			}
		}
	}

	private void criarCabecalho(JPanel interno) {

		JLabel logo = new JLabel();

		// 1. Carrega a URL da imagem pelo Classpath
		java.net.URL logoUrl = getClass().getResource("/Images/Solo-Firme.png");

		if (logoUrl != null) {
		    // 2. Instancia o ImageIcon passando a URL
		    ImageIcon icone = new ImageIcon(logoUrl);
		    
		    // 3. Redimensiona a imagem
		    Image imagem = icone.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
		    
		    // 4. Define o ícone final no JLabel
		    logo.setIcon(new ImageIcon(imagem));
		} else {
		    System.err.println("Erro: Imagem '/Images/Solo-Firme.png' não encontrada no classpath!");
		}

		logo.setBounds(11, 6, 220, 220);
		interno.add(logo);

		JSeparator separador = new JSeparator();

		separador.setOrientation(SwingConstants.VERTICAL);

		separador.setBounds(235, 0, 2, alturaInterno);

		separador.setForeground(COR_BORDA);

		interno.add(separador);

		String nomeResponsavel = obterNomeResponsavel();

		JLabel usuario = new JLabel("Olá, " + nomeResponsavel);

		usuario.setForeground(COR_TEXTO);
		usuario.setFont(new Font("Segoe UI", Font.BOLD, 20));

		usuario.setBounds(larguraInterno - 450, 30, 410, 30);

		interno.add(usuario);

		JLabel titulo = new JLabel("Olá, responsável!");

		titulo.setForeground(COR_TEXTO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));

		titulo.setBounds(335, 90, 600, 40);

		interno.add(titulo);

		JLabel subtitulo = new JLabel(obterMensagemAlunos());

		subtitulo.setForeground(COR_LABEL);
		subtitulo.setFont(new Font("Segoe UI", Font.BOLD, 38));

		subtitulo.setBounds(335, 135, 720, 55);

		interno.add(subtitulo);

		JLabel descricao = new JLabel("Acompanhe o desempenho acadêmico dos alunos vinculados ao seu cadastro.");

		descricao.setForeground(COR_TEXTO);
		descricao.setFont(new Font("Segoe UI", Font.PLAIN, 18));

		descricao.setBounds(345, 205, 900, 30);

		interno.add(descricao);
	}

	private String obterNomeResponsavel() {

		try {

			String nome = DadosSistema.nomeUsuarioLogado();

			if (nome != null && !nome.isBlank()) {
				return nome;
			}

		} catch (Exception e) {
			// Usa o texto padrão caso o nome não esteja disponível.
		}

		return "Responsável";
	}

	private String obterMensagemAlunos() {

		if (alunos.isEmpty()) {
			return "Nenhum aluno vinculado";
		}

		if (alunos.size() == 1) {

			String nome = alunos.get(0).getNome();

			if (nome != null && !nome.isBlank()) {
				return nome;
			}
		}

		return alunos.size() + " alunos vinculados";
	}

	private void criarMenu(JPanel interno) {

		String[] botoes = { "Dados do aluno", "Meus dados", "Boletim", "Frequência", "Comunicados", "Suporte" };

		int y = 240;

		for (String texto : botoes) {

			JButton botao = criarBotaoMenu(texto);

			botao.setBounds(17, y, 200, 45);

			configurarAcaoBotao(botao, texto);

			interno.add(botao);

			y += 52;
		}

		JButton sair = criarBotaoMenu("Sair");

		sair.setBounds(17, alturaInterno - 80, 200, 50);

		sair.addActionListener(e -> {

			SessaoUsuario.encerrarSessao();

			dispose();

			System.exit(0);
		});

		interno.add(sair);
	}

	private void configurarAcaoBotao(JButton botao, String nome) {

		switch (nome) {

		case "Boletim":

			botao.addActionListener(e -> new NotasAluno().setVisible(true));

			break;

		case "Suporte":

			botao.addActionListener(e -> new SuporteResponsavel().setVisible(true));

			break;

		case "Dados do aluno":

			botao.addActionListener(e -> mostrarDadosAluno());

			break;

		case "Meus dados":

			botao.addActionListener(e -> new MeusDadosResponsavel().setVisible(true));

			break;

		case "Frequência":

			/*
			 * O botão apenas abre a tela responsável pela frequência. Nenhuma lógica de
			 * frequência é executada aqui.
			 */
			botao.addActionListener(e -> new FrequenciaResponsavel().setVisible(true));

			break;

		case "Comunicados":

			botao.addActionListener(e -> mostrarComunicados());

			break;

		default:
			break;
		}
	}

	private void mostrarDadosAluno() {

		if (alunos.isEmpty()) {

			JOptionPane.showMessageDialog(this, "Não existem alunos vinculados a este responsável.", "Dados do aluno",
					JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		StringBuilder mensagem = new StringBuilder();

		for (Aluno aluno : alunos) {

			if (aluno == null) {
				continue;
			}

			mensagem.append("Nome: ").append(valor(aluno.getNome())).append("\n");

			mensagem.append("Matrícula: ").append(valor(aluno.getMatricula())).append("\n");

			mensagem.append("Situação: ").append(aluno.getSituacao() == null ? "Não informado" : aluno.getSituacao())
					.append("\n\n");
		}

		JOptionPane.showMessageDialog(this, mensagem.toString(), "Dados dos alunos", JOptionPane.INFORMATION_MESSAGE);
	}

	private void mostrarComunicados() {

		if (avisos.isEmpty()) {

			JOptionPane.showMessageDialog(this, "Nenhum comunicado cadastrado.", "Comunicados",
					JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		StringBuilder mensagem = new StringBuilder();

		for (Aviso aviso : avisos) {

			mensagem.append(valor(aviso.getTitulo())).append("\n");

			mensagem.append(valor(aviso.getDescricao())).append("\n");

			if (aviso.getDataAviso() != null) {

				mensagem.append("Data: ").append(formatarData(aviso.getDataAviso())).append("\n");
			}

			mensagem.append("\n");
		}

		JOptionPane.showMessageDialog(this, mensagem.toString(), "Comunicados", JOptionPane.INFORMATION_MESSAGE);
	}

	private void criarCards(JPanel interno) {

		JPanel cardMedia = criarCard("Média Geral", formatarNota(mediaGeral), obterDescricaoMedia());

		cardMedia.setBounds(350, 275, 240, 135);

		interno.add(cardMedia);

		JPanel cardFrequencia = criarCard("Frequência", formatarPercentual(frequenciaGeral),
				obterDescricaoFrequencia());

		cardFrequencia.setBounds(610, 275, 240, 135);

		interno.add(cardFrequencia);

		JPanel cardPendencias = criarCard("Pendências", String.valueOf(pendencias), obterDescricaoPendencias());

		cardPendencias.setBounds(870, 275, 240, 135);

		interno.add(cardPendencias);
	}

	private String obterDescricaoMedia() {

		if (alunos.isEmpty()) {
			return "Sem alunos vinculados";
		}

		if (mediaGeral == 0.0) {
			return "Sem notas cadastradas";
		}

		if (mediaGeral >= 9.0) {
			return "Excelente desempenho";
		}

		if (mediaGeral >= 7.0) {
			return "Bom desempenho";
		}

		if (mediaGeral >= 5.0) {
			return "Atenção ao desempenho";
		}

		return "Necessita acompanhamento";
	}

	private String obterDescricaoFrequencia() {

		if (alunos.isEmpty()) {
			return "Sem alunos vinculados";
		}

		if (frequenciaGeral == 0.0) {
			return "Sem registros";
		}

		if (frequenciaGeral >= 90.0) {
			return "Frequência muito boa";
		}

		if (frequenciaGeral >= 75.0) {
			return "Frequência regular";
		}

		return "Atenção à frequência";
	}

	private String obterDescricaoPendencias() {

		if (pendencias == 0) {
			return "Nenhuma pendência aberta";
		}

		if (pendencias == 1) {
			return "1 atendimento em aberto";
		}

		return pendencias + " atendimentos em aberto";
	}

	private JPanel criarCard(String titulo, String valor, String descricao) {

		JPanel card = criarPainelArredondado(COR_CAMPO, COR_BORDA, 24);

		card.setLayout(null);

		JLabel lbTitulo = new JLabel(titulo, SwingConstants.CENTER);

		lbTitulo.setForeground(COR_TEXTO);
		lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));

		lbTitulo.setBounds(10, 12, 220, 25);

		card.add(lbTitulo);

		JLabel lbValor = new JLabel(valor, SwingConstants.CENTER);

		lbValor.setForeground(COR_TEXTO);
		lbValor.setFont(new Font("Segoe UI", Font.BOLD, 36));

		lbValor.setBounds(10, 42, 220, 45);

		card.add(lbValor);

		JLabel lbDescricao = new JLabel(descricao, SwingConstants.CENTER);

		lbDescricao.setForeground(COR_LABEL);
		lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		lbDescricao.setBounds(10, 95, 220, 25);

		card.add(lbDescricao);

		return card;
	}

	private void criarResumoAlunos(JPanel interno) {

		JPanel painel = criarPainelArredondado(COR_CAMPO, COR_BORDA, 24);

		painel.setLayout(null);

		painel.setBounds(350, 430, 760, 360);

		interno.add(painel);

		JLabel titulo = new JLabel("Resumo dos alunos");

		titulo.setForeground(COR_TEXTO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 27));

		titulo.setBounds(30, 20, 400, 35);

		painel.add(titulo);

		if (alunos.isEmpty()) {

			JLabel vazio = new JLabel("Nenhum aluno vinculado ao responsável.");

			vazio.setForeground(COR_TEXTO);
			vazio.setFont(new Font("Segoe UI", Font.PLAIN, 17));

			vazio.setBounds(30, 85, 650, 30);

			painel.add(vazio);

			return;
		}

		int y = 75;

		int limite = Math.min(alunos.size(), 5);

		for (int i = 0; i < limite; i++) {

			Aluno aluno = alunos.get(i);

			if (aluno == null) {
				continue;
			}

			adicionarAlunoResumo(painel, aluno, y);

			y += 55;
		}

		if (alunos.size() > limite) {

			JLabel mais = new JLabel("Mais " + (alunos.size() - limite) + " aluno(s) vinculado(s).");

			mais.setForeground(COR_LABEL);
			mais.setFont(new Font("Segoe UI", Font.PLAIN, 14));

			mais.setBounds(30, y + 5, 650, 25);

			painel.add(mais);
		}
	}

	private void adicionarAlunoResumo(JPanel painel, Aluno aluno, int y) {

		JLabel nome = new JLabel(valor(aluno.getNome()));

		nome.setForeground(COR_TEXTO);
		nome.setFont(new Font("Segoe UI", Font.BOLD, 17));

		nome.setBounds(30, y, 300, 25);

		painel.add(nome);

		JLabel matricula = new JLabel("Matrícula: " + valor(aluno.getMatricula()));

		matricula.setForeground(COR_LABEL);
		matricula.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		matricula.setBounds(330, y, 190, 25);

		painel.add(matricula);

		JLabel situacao = new JLabel(
				"Situação: " + (aluno.getSituacao() == null ? "Não informado" : aluno.getSituacao()));

		situacao.setForeground(COR_TEXTO);
		situacao.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		situacao.setBounds(520, y, 210, 25);

		painel.add(situacao);
	}

	private void criarComunicados(JPanel interno) {

		JPanel painel = criarPainelArredondado(COR_CAMPO, COR_BORDA, 24);

		painel.setLayout(null);

		painel.setBounds(1130, 430, 370, 360);

		interno.add(painel);

		JLabel titulo = new JLabel("Comunicados");

		titulo.setForeground(COR_TEXTO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 27));

		titulo.setBounds(25, 20, 300, 35);

		painel.add(titulo);

		if (avisos.isEmpty()) {

			JLabel vazio = new JLabel("<html>Nenhum comunicado<br>" + "cadastrado.</html>");

			vazio.setForeground(COR_TEXTO);
			vazio.setFont(new Font("Segoe UI", Font.PLAIN, 15));

			vazio.setBounds(25, 85, 320, 55);

			painel.add(vazio);

			return;
		}

		int y = 80;

		int limite = Math.min(avisos.size(), 3);

		for (int i = 0; i < limite; i++) {

			Aviso aviso = avisos.get(i);

			if (aviso == null) {
				continue;
			}

			adicionarComunicado(painel, aviso, y);

			y += 90;
		}
	}

	private void adicionarComunicado(JPanel painel, Aviso aviso, int y) {

		JLabel titulo = new JLabel(valor(aviso.getTitulo()));

		titulo.setForeground(COR_TEXTO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

		titulo.setBounds(25, y, 320, 25);

		painel.add(titulo);

		String descricao = valor(aviso.getDescricao());

		if (descricao.length() > 70) {
			descricao = descricao.substring(0, 67) + "...";
		}

		JLabel descricaoLabel = new JLabel("<html>" + descricao + "</html>");

		descricaoLabel.setForeground(COR_TEXTO);
		descricaoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		descricaoLabel.setBounds(25, y + 25, 320, 35);

		painel.add(descricaoLabel);

		if (aviso.getDataAviso() != null) {

			JLabel data = new JLabel(formatarData(aviso.getDataAviso()));

			data.setForeground(COR_LABEL);
			data.setFont(new Font("Segoe UI", Font.PLAIN, 11));

			data.setBounds(25, y + 60, 320, 20);

			painel.add(data);
		}
	}

	private void criarCalendario(JPanel interno) {

		JPanel calendario = criarPainelArredondado(COR_CAMPO, COR_BORDA, 24);

		calendario.setLayout(null);

		calendario.setBounds(1130, 275, 370, 140);

		interno.add(calendario);

		JLabel titulo = new JLabel("Próximos comunicados");

		titulo.setForeground(COR_TEXTO);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 21));

		titulo.setBounds(25, 18, 310, 30);

		calendario.add(titulo);

		JLabel descricao;

		if (avisos.isEmpty()) {

			descricao = new JLabel("Nenhum evento cadastrado.");

		} else {

			Aviso aviso = avisos.get(0);

			String data = aviso.getDataAviso() == null ? "Data não informada" : formatarData(aviso.getDataAviso());

			descricao = new JLabel("<html>" + data + " — " + valor(aviso.getTitulo()) + "</html>");
		}

		descricao.setForeground(COR_LABEL);
		descricao.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		descricao.setBounds(25, 60, 320, 45);

		calendario.add(descricao);
	}

	private void criarRodape(JPanel interno) {

		JPanel rodape = criarPainelArredondado(COR_CAMPO, COR_BORDA, 20);

		rodape.setLayout(null);

		rodape.setBounds(350, 805, 760, 90);

		interno.add(rodape);

		JLabel texto = new JLabel("Consulte regularmente as informações acadêmicas e os comunicados da escola.");

		texto.setForeground(COR_TEXTO);
		texto.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		texto.setBounds(25, 30, 710, 30);

		rodape.add(texto);
	}

	private JButton criarBotaoMenu(String texto) {

		JButton botao = new JButton(texto);

		botao.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		botao.setForeground(COR_TEXTO);
		botao.setBackground(COR_ROXO);

		botao.setFocusPainted(false);

		botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

		return botao;
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

	private String valor(String valor) {

		if (valor == null || valor.isBlank()) {
			return "Não informado";
		}

		return valor;
	}

	private String formatarNota(double nota) {

		return String.format("%.2f", nota).replace('.', ',');
	}

	private String formatarPercentual(double percentual) {

		return String.format("%.1f%%", percentual).replace('.', ',');
	}

	private String formatarData(LocalDate data) {

		return String.format("%02d/%02d/%04d", data.getDayOfMonth(), data.getMonthValue(), data.getYear());
	}
}