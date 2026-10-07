package view;

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
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.ProfessorDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Professor;
import model.Usuario;
import util.SessaoUsuario;

public class MeusDadosProfessor extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	private final DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public MeusDadosProfessor() {
		setTitle("Meus Dados - Professor");
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

		painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 900));

		JScrollPane scroll = new JScrollPane(painelRolagem);
		scroll.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
		scroll.setBorder(null);
		scroll.getViewport().setBackground(corInterna);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		interno.add(scroll);

		carregarDadosProfessor(painelRolagem, xInicial);
	}

	private void carregarDadosProfessor(JPanel painelRolagem, int xInicial) {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Nenhum usuário está logado.", "Usuário não identificado",
					JOptionPane.WARNING_MESSAGE);

			dispose();
			return;
		}

		if (usuario.getProfessorId() <= 0) {
			JOptionPane.showMessageDialog(this, "O usuário logado não possui um professor vinculado.",
					"Professor não identificado", JOptionPane.WARNING_MESSAGE);

			dispose();
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			ProfessorDAO professorDAO = new ProfessorDAO(conn);
			Professor professor = professorDAO.buscarPorId(usuario.getProfessorId());

			if (professor == null) {
				JOptionPane.showMessageDialog(this,
						"Não foi possível localizar o cadastro do professor no banco de dados.",
						"Professor não encontrado", JOptionPane.WARNING_MESSAGE);

				dispose();
				return;
			}

			criarCardsProfessor(painelRolagem, xInicial, professor);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Erro ao carregar os dados do professor:\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);

			dispose();
		}
	}

	private void criarCardsProfessor(JPanel painelRolagem, int xInicial, Professor professor) {

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

		JLabel nomeProfessor = new JLabel(obterTexto(professor.getNome()), SwingConstants.CENTER);

		nomeProfessor.setForeground(textos);
		nomeProfessor.setFont(new Font("Segoe UI", Font.BOLD, 21));
		nomeProfessor.setBounds(15, 190, 330, 30);
		cardPerfil.add(nomeProfessor);

		JLabel cargoProfessor = new JLabel("Professor", SwingConstants.CENTER);

		cargoProfessor.setForeground(textos);
		cargoProfessor.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		cargoProfessor.setBounds(15, 225, 330, 25);
		cardPerfil.add(cargoProfessor);

		JLabel statusProfessor = new JLabel(obterSituacao(professor), SwingConstants.CENTER);

		statusProfessor.setForeground(professor.isAtivo() ? new Color(120, 255, 170) : new Color(255, 130, 130));

		statusProfessor.setFont(new Font("Segoe UI", Font.BOLD, 16));
		statusProfessor.setBounds(20, 255, 320, 25);
		cardPerfil.add(statusProfessor);

		JPanel cardDadosPessoais = criarCardSecao("Dados Pessoais");
		cardDadosPessoais.setBounds(xInicial + 390, 0, 830, 300);
		painelRolagem.add(cardDadosPessoais);

		adicionarCampo(cardDadosPessoais, "Nome completo:", obterTexto(professor.getNome()), 25, 65);

		adicionarCampo(cardDadosPessoais, "CPF:", formatarCpf(professor.getCpf()), 25, 115);

		adicionarCampo(cardDadosPessoais, "RG:", obterTexto(professor.getRg()), 25, 165);

		adicionarCampo(cardDadosPessoais, "Data de nascimento:", formatarData(professor.getDataNascimento()), 25, 215);

		adicionarCampo(cardDadosPessoais, "Sexo:",
				professor.getSexo() != null ? professor.getSexo().name() : "Não informado", 440, 65);

		adicionarCampo(cardDadosPessoais, "Telefone:", formatarTelefone(professor.getTelefone()), 440, 115);

		adicionarCampo(cardDadosPessoais, "Formação:", obterTexto(professor.getFormacao()), 440, 165);

		adicionarCampo(cardDadosPessoais, "Status:", professor.isAtivo() ? "Ativo" : "Inativo", 440, 215);

		int yCard = 330;

		JPanel cardProfissional = criarCardSecao("Dados Profissionais");
		cardProfissional.setBounds(xInicial, yCard, 390, 270);
		painelRolagem.add(cardProfissional);

		adicionarCampo(cardProfissional, "ID do professor:", String.valueOf(professor.getIdProfessor()), 25, 65);

		adicionarCampo(cardProfissional, "Cargo:", "Professor", 25, 115);

		adicionarCampo(cardProfissional, "Formação:", obterTexto(professor.getFormacao()), 25, 165);

		adicionarCampo(cardProfissional, "Situação:", professor.isAtivo() ? "Ativo" : "Inativo", 25, 215);

		JPanel cardTurmas = criarCardSecao("Disciplinas e Vínculos");
		cardTurmas.setBounds(xInicial + 415, yCard, 390, 270);
		painelRolagem.add(cardTurmas);

		adicionarCampo(cardTurmas, "Disciplina principal:", "Não informado", 25, 65);

		adicionarCampo(cardTurmas, "Turmas:", "Não informado", 25, 115);

		adicionarCampo(cardTurmas, "Vínculo:", "Não informado", 25, 165);

		adicionarCampo(cardTurmas, "Situação:", professor.isAtivo() ? "Ativo" : "Inativo", 25, 215);

		JPanel cardEndereco = criarCardSecao("Endereço");
		cardEndereco.setBounds(xInicial + 830, yCard, 390, 270);
		painelRolagem.add(cardEndereco);

		Endereco endereco = professor.getEndereco();

		if (endereco != null) {

			adicionarCampo(cardEndereco, "Rua:", obterTexto(endereco.getRua()), 25, 65);

			adicionarCampo(cardEndereco, "Número:", obterTexto(endereco.getNumero()), 25, 115);

			adicionarCampo(cardEndereco, "Bairro:", obterTexto(endereco.getBairro()), 25, 165);

			adicionarCampo(cardEndereco, "Cidade/UF:", obterCidadeEstado(endereco), 25, 215);

		} else {

			adicionarCampo(cardEndereco, "Rua:", "Não informado", 25, 65);

			adicionarCampo(cardEndereco, "Número:", "Não informado", 25, 115);

			adicionarCampo(cardEndereco, "Bairro:", "Não informado", 25, 165);

			adicionarCampo(cardEndereco, "Cidade/UF:", "Não informado", 25, 215);
		}

		JPanel cardDadosExtras = criarCardSecao("Informações Complementares");
		cardDadosExtras.setBounds(xInicial, 620, 600, 225);
		painelRolagem.add(cardDadosExtras);

		adicionarCampo(cardDadosExtras, "CEP:", endereco != null ? formatarCep(endereco.getCep()) : "Não informado", 25,
				65);

		adicionarCampo(cardDadosExtras, "Complemento:",
				endereco != null ? obterTexto(endereco.getComplemento()) : "Não informado", 25, 115);

		adicionarCampo(cardDadosExtras, "Formação:", obterTexto(professor.getFormacao()), 25, 165);

		adicionarCampo(cardDadosExtras, "ID cadastro:", String.valueOf(professor.getIdProfessor()), 315, 65);

		adicionarCampo(cardDadosExtras, "Situação:", professor.isAtivo() ? "Ativo" : "Inativo", 315, 115);

		adicionarCampo(cardDadosExtras, "Pendências:", "Não informado", 315, 165);

		JPanel cardObservacoes = criarCardSecao("Observações");
		cardObservacoes.setBounds(xInicial + 630, 620, 590, 225);
		painelRolagem.add(cardObservacoes);

		String textoObservacao;

		if (professor.isAtivo()) {
			textoObservacao = "Caso encontre alguma informação incorreta, procure a secretaria para solicitar a atualização dos dados.</html>";
		} else {
			textoObservacao = "<html>Este cadastro de professor está marcado como inativo.<br><br>"
					+ "Procure a secretaria caso seja necessário verificar ou reativar " + "o cadastro.</html>";
		}

		JLabel obs = new JLabel(textoObservacao);
		obs.setForeground(textos);
		obs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		obs.setBounds(25, 65, 530, 90);
		cardObservacoes.add(obs);

		JButton btnSolicitarAtualizacao = new JButton("Solicitar atualização de dados");
		btnSolicitarAtualizacao.setBounds(300, 165, 260, 40);
		estilizarBotaoAcao(btnSolicitarAtualizacao);
		btnSolicitarAtualizacao.addActionListener(e -> abrirSuporte());
		cardObservacoes.add(btnSolicitarAtualizacao);

		JButton btnAlterarSenha = new JButton("Alterar a Senha");
		btnAlterarSenha.setBounds(25, 165, 260, 40);
		estilizarBotaoAcao(btnAlterarSenha);
		btnAlterarSenha.addActionListener(e -> new AlterarSenha().setVisible(true));
		cardObservacoes.add(btnAlterarSenha);
	}

	private JPanel criarTopo() {

		JPanel topo = new JPanel(null) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(),
						new Color(190, 35, 170));

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

		JLabel sub = new JLabel("Consulte suas informações pessoais, profissionais, endereço e vínculos.");

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

	private String obterTexto(String valor) {

		if (valor == null || valor.isBlank()) {
			return "Não informado";
		}

		return valor;
	}

	private String formatarData(LocalDate data) {

		if (data == null) {
			return "Não informado";
		}

		return data.format(formatadorData);
	}

	private String formatarCpf(String cpf) {

		if (cpf == null || cpf.isBlank()) {
			return "Não informado";
		}

		String cpfTratado = cpf.replaceAll("\\D", "");

		if (cpfTratado.length() != 11) {
			return cpf;
		}

		return cpfTratado.substring(0, 3) + "." + cpfTratado.substring(3, 6) + "." + cpfTratado.substring(6, 9) + "-"
				+ cpfTratado.substring(9, 11);
	}

	private String formatarTelefone(String telefone) {

		if (telefone == null || telefone.isBlank()) {
			return "Não informado";
		}

		String telefoneTratado = telefone.replaceAll("\\D", "");

		if (telefoneTratado.length() == 11) {

			return "(" + telefoneTratado.substring(0, 2) + ") " + telefoneTratado.substring(2, 7) + "-"
					+ telefoneTratado.substring(7, 11);
		}

		if (telefoneTratado.length() == 10) {

			return "(" + telefoneTratado.substring(0, 2) + ") " + telefoneTratado.substring(2, 6) + "-"
					+ telefoneTratado.substring(6, 10);
		}

		return telefone;
	}

	private String formatarCep(String cep) {

		if (cep == null || cep.isBlank()) {
			return "Não informado";
		}

		String cepTratado = cep.replaceAll("\\D", "");

		if (cepTratado.length() != 8) {
			return cep;
		}

		return cepTratado.substring(0, 5) + "-" + cepTratado.substring(5, 8);
	}

	private String obterCidadeEstado(Endereco endereco) {

		if (endereco == null) {
			return "Não informado";
		}

		String cidade = obterTexto(endereco.getCidade());

		String estado = endereco.getEstado() != null ? endereco.getEstado().name() : "Não informado";

		if ("Não informado".equals(cidade) && "Não informado".equals(estado)) {
			return "Não informado";
		}

		return cidade + " - " + estado;
	}

	private String obterSituacao(Professor professor) {

		return professor.isAtivo() ? "Situação: Ativo" : "Situação: Inativo";
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void abrirSuporte() {
		if (!SessaoUsuario.existeUsuarioLogado()) {
			mostrarErro("Sua sessão foi encerrada. Faça login novamente.");
			dispose();
			return;
		}
		new SuporteAluno().setVisible(true);
	}
}