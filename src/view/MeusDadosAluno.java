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
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import controller.AlunoController;
import dao.AlunoDAO;
import dao.PaisAlunoDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Endereco;
import model.PaisAluno;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class MeusDadosAluno extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private final Color corAviso = new Color(120, 255, 170);
	private JTextField txtTelefone;
	private JTextField txtEmail;
	private JTextField txtCep;
	private JTextField txtRua;
	private JTextField txtNumero;
	private JTextField txtBairro;
	private JTextField txtCidade;
	private JTextField txtComplemento;
	private JLabel lblNome;
	private JLabel lblCpf;
	private JLabel lblRg;
	private JLabel lblSexo;
	private JLabel lblNascimento;
	private JLabel lblMatricula;
	private JLabel lblTurma;
	private JLabel lblTurno;
	private JLabel lblSituacao;
	private JLabel lblResponsavel;
	private JLabel lblTelefoneResponsavel;
	private JLabel lblEmailResponsavel;
	private JLabel lblEstado;
	private Aluno alunoAtual;

	public MeusDadosAluno() {
		setTitle("Meus Dados - Aluno");
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

		if (validarSessao()) {
			carregarDadosDoBanco();
		}
	}

	private boolean validarSessao() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Nenhum usuário está logado no sistema.", "Sessão não encontrada",
					JOptionPane.WARNING_MESSAGE);
			dispose();
			return false;
		}
		if (usuario.getAlunoId() <= 0) {
			JOptionPane.showMessageDialog(this, "O usuário logado não está vinculado a um cadastro de aluno.",
					"Acesso não autorizado", JOptionPane.WARNING_MESSAGE);
			dispose();
			return false;
		}
		return true;
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
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		scroll.getViewport().setBackground(corInterna);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		interno.add(scroll);

		criarCardPerfil(painelRolagem, xInicial);
		criarCardDadosPessoais(painelRolagem, xInicial);
		criarCardDadosEscolares(painelRolagem, xInicial);
		criarCardEndereco(painelRolagem, xInicial);
		criarCardResponsavel(painelRolagem, xInicial);
		criarCardOrientacoes(painelRolagem, xInicial);
	}

	private void criarCardPerfil(JPanel painelRolagem, int xInicial) {
		JPanel cardPerfil = criarPainelArredondado();
		cardPerfil.setBounds(xInicial + 5, 0, 390, 300);
		cardPerfil.setLayout(null);
		painelRolagem.add(cardPerfil);

		JLabel foto = new JLabel("ALUNO", SwingConstants.CENTER);
		foto.setOpaque(true);
		foto.setBackground(corCampo);
		foto.setForeground(textos);
		foto.setFont(new Font("Segoe UI", Font.BOLD, 28));
		foto.setBorder(new LineBorder(corBorda, 2));
		foto.setBounds(105, 25, 150, 150);
		cardPerfil.add(foto);

		lblNome = new JLabel("Não informado", SwingConstants.CENTER);
		lblNome.setForeground(textos);
		lblNome.setFont(new Font("Segoe UI", Font.BOLD, 21));
		lblNome.setBounds(15, 190, 360, 30);
		cardPerfil.add(lblNome);

		lblMatricula = new JLabel("Matrícula: Não informado", SwingConstants.CENTER);
		lblMatricula.setForeground(textos);
		lblMatricula.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		lblMatricula.setBounds(15, 225, 360, 25);
		cardPerfil.add(lblMatricula);

		lblSituacao = new JLabel("Situação: Não informado", SwingConstants.CENTER);
		lblSituacao.setForeground(corAviso);
		lblSituacao.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblSituacao.setBounds(20, 255, 350, 25);
		cardPerfil.add(lblSituacao);
	}

	private void criarCardDadosPessoais(JPanel painelRolagem, int xInicial) {
		JPanel card = criarCardSecao("Dados Pessoais");
		card.setBounds(xInicial + 410, 0, 790, 300);
		painelRolagem.add(card);

		lblNome = adicionarCampoSomenteLeitura(card, "Nome completo:", "Não informado", 25, 65);
		lblCpf = adicionarCampoSomenteLeitura(card, "CPF:", "Não informado", 25, 115);
		lblRg = adicionarCampoSomenteLeitura(card, "RG:", "Não informado", 25, 165);
		lblNascimento = adicionarCampoSomenteLeitura(card, "Data de nascimento:", "Não informado", 25, 215);
		lblSexo = adicionarCampoSomenteLeitura(card, "Sexo:", "Não informado", 440, 65);
		txtTelefone = adicionarCampoEditavel(card, "Telefone:", "", 440, 115);
		txtEmail = adicionarCampoEditavel(card, "E-mail:", "", 440, 165);

		JLabel aviso = new JLabel("Somente telefone e e-mail podem ser alterados aqui.");
		aviso.setForeground(corAviso);
		aviso.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		aviso.setBounds(440, 215, 320, 25);
		card.add(aviso);
	}

	private void criarCardDadosEscolares(JPanel painelRolagem, int xInicial) {
		JPanel card = criarCardSecao("Dados Escolares");
		card.setBounds(xInicial + 5, 325, 390, 270);
		painelRolagem.add(card);
		lblMatricula = adicionarCampoSomenteLeitura(card, "Matrícula:", "Não informado", 25, 65);
		lblTurma = adicionarCampoSomenteLeitura(card, "Turma:", "Não informado", 25, 115);
		adicionarCampoSomenteLeitura(card, "Curso:", "Não informado", 25, 165);
		lblTurno = adicionarCampoSomenteLeitura(card, "Turno:", "Não informado", 25, 215);
	}

	private void criarCardEndereco(JPanel painelRolagem, int xInicial) {
		JPanel card = criarCardSecao("Endereço");
		card.setBounds(xInicial + 410, 325, 390, 320);
		painelRolagem.add(card);
		txtCep = adicionarCampoEditavel(card, "CEP:", "", 25, 65);
		txtRua = adicionarCampoEditavel(card, "Rua:", "", 25, 115);
		txtNumero = adicionarCampoEditavel(card, "Número:", "", 25, 165);
		txtBairro = adicionarCampoEditavel(card, "Bairro:", "", 25, 215);
		txtCidade = adicionarCampoEditavel(card, "Cidade:", "", 25, 265);
		lblEstado = adicionarCampoSomenteLeitura(card, "Estado:", "Não informado", 210, 65);
		txtComplemento = adicionarCampoEditavel(card, "Complemento:", "", 210, 115);

		JLabel aviso = new JLabel("Os dados acima podem ser alterados pelo aluno.");
		aviso.setForeground(corAviso);
		aviso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		aviso.setBounds(210, 170, 165, 45);
		card.add(aviso);
	}

	private void criarCardResponsavel(JPanel painelRolagem, int xInicial) {
		JPanel card = criarCardSecao("Responsável");
		card.setBounds(xInicial + 815, 325, 390, 270);
		painelRolagem.add(card);
		lblResponsavel = adicionarCampoSomenteLeitura(card, "Nome:", "Não informado", 25, 65);
		adicionarCampoSomenteLeitura(card, "Parentesco:", "Não informado", 25, 115);
		lblTelefoneResponsavel = adicionarCampoSomenteLeitura(card, "Telefone:", "Não informado", 25, 165);
		lblEmailResponsavel = adicionarCampoSomenteLeitura(card, "E-mail:", "Não informado", 25, 215);
	}

	private void criarCardOrientacoes(JPanel painelRolagem, int xInicial) {
		JPanel card = criarCardSecao("Alteração de Dados Cadastrais");
		card.setBounds(xInicial + 5, 670, 1200, 190);
		painelRolagem.add(card);

		JLabel texto = new JLabel("<html>" + "Dados como <b>nome, CPF, RG, data de nascimento, sexo, matrícula, "
				+ "responsável, situação escolar e demais informações cadastrais</b> "
				+ "não podem ser alterados diretamente pelo aluno." + "<br><br>"
				+ "Caso alguma dessas informações esteja incorreta, abra uma "
				+ "<b>solicitação para a secretaria</b> informando o dado que precisa " + "ser corrigido." + "</html>");
		texto.setForeground(textos);
		texto.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		texto.setBounds(30, 60, 780, 90);
		card.add(texto);
		
		JButton btnAlterarSenha = new JButton("Alterar a Senha");
		btnAlterarSenha.setBounds(835, 10, 330, 45);
		estilizarBotaoAcao(btnAlterarSenha);
		btnAlterarSenha.addActionListener(e -> new AlterarSenha().setVisible(true));
		card.add(btnAlterarSenha);

		JButton btnSolicitarAtualizacao = new JButton("Solicitar alteração à secretaria");
		btnSolicitarAtualizacao.setBounds(835, 65, 330, 45);
		estilizarBotaoAcao(btnSolicitarAtualizacao);
		btnSolicitarAtualizacao.addActionListener(e -> abrirSuporte());
		card.add(btnSolicitarAtualizacao);

		JButton btnSalvar = new JButton("Salvar contato e endereço");
		btnSalvar.setBounds(835, 120, 330, 45);
		estilizarBotaoAcao(btnSalvar);
		btnSalvar.addActionListener(e -> salvarDadosPermitidos());
		card.add(btnSalvar);
	}

	private void carregarDadosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getAlunoId() <= 0) {
			mostrarErro("Não foi possível identificar o aluno logado.");
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			alunoAtual = alunoDAO.buscarPorId(usuario.getAlunoId());

			if (alunoAtual == null) {
				throw new IllegalArgumentException("Aluno não encontrado no banco de dados.");
			}
			preencherDadosAluno(conn);
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar seus dados: " + obterMensagemErro(ex));
		}
	}

	private void preencherDadosAluno(Connection conn) throws SQLException {
		lblNome.setText(valorOuNaoInformado(alunoAtual.getNome()));
		lblCpf.setText(formatarCpf(alunoAtual.getCpf()));
		lblRg.setText(valorOuNaoInformado(alunoAtual.getRg()));
		lblNascimento.setText(formatarData(alunoAtual.getDataNascimento()));
		lblSexo.setText(alunoAtual.getSexo() == null ? "Não informado" : alunoAtual.getSexo().name());
		txtTelefone.setText(valorVazio(alunoAtual.getTelefone()));
		txtEmail.setText(valorVazio(alunoAtual.getEmail()));
		lblMatricula.setText("Matrícula: " + valorOuNaoInformado(alunoAtual.getMatricula()));
		lblSituacao.setText("Situação: " + (alunoAtual.getSituacao() == null ? "Não informado" : alunoAtual.getSituacao().name()));

		Turma turma = new TurmaDAO(conn).buscarPorId(alunoAtual.getIdTurma());

		if (turma == null) {
			lblTurma.setText("Turma: Não informado");
			lblTurno.setText("Turno: Não informado");
		} else {
			lblTurma.setText("Turma: " + valorOuNaoInformado(turma.getDescricaoTurma()));
			lblTurno.setText("Turno: " + (turma.getTurno() == null ? "Não informado" : turma.getTurno().name()));
		}
		preencherEndereco();
		preencherResponsavel(conn);
	}

	private void preencherEndereco() {
		Endereco endereco = alunoAtual.getEndereco();

		if (endereco == null) {
			return;
		}
		txtCep.setText(valorVazio(endereco.getCep()));
		txtRua.setText(valorVazio(endereco.getRua()));
		txtNumero.setText(valorVazio(endereco.getNumero()));
		txtBairro.setText(valorVazio(endereco.getBairro()));
		txtCidade.setText(valorVazio(endereco.getCidade()));
		txtComplemento.setText(valorVazio(endereco.getComplemento()));
		lblEstado.setText(valorOuNaoInformado(endereco.getEstado() == null ? null : endereco.getEstado().name()));
	}

	private void preencherResponsavel(Connection conn) throws SQLException {
		if (alunoAtual.getIdPais() <= 0) {
			return;
		}
		PaisAluno pais = new PaisAlunoDAO(conn).buscarPorId(alunoAtual.getIdPais());

		if (pais == null) {
			return;
		}
		boolean possuiMae = pais.getNomeMae() != null && !pais.getNomeMae().isBlank();
		String nome = possuiMae ? pais.getNomeMae() : pais.getNomePai();
		String telefone = possuiMae ? pais.getTelefoneMae() : pais.getTelefonePai();
		String email = possuiMae ? pais.getEmailMae() : pais.getEmailPai();
		lblResponsavel.setText(valorOuNaoInformado(nome));
		lblTelefoneResponsavel.setText(valorOuNaoInformado(telefone));
		lblEmailResponsavel.setText(valorOuNaoInformado(email));
	}

	private void salvarDadosPermitidos() {
		if (!SessaoUsuario.existeUsuarioLogado()) {
			mostrarErro("Sua sessão foi encerrada. Faça login novamente.");
			dispose();
			return;
		}
		if (alunoAtual == null) {
			mostrarErro("Os dados do aluno ainda não foram carregados.");
			return;
		}
		try {
			Aluno atualizado = criarAlunoComDadosPermitidos();
			new AlunoController().atualizarAluno(atualizado);
			alunoAtual = atualizado;
			JOptionPane.showMessageDialog(this, "Telefone, e-mail e endereço foram atualizados com sucesso.",
					"Dados atualizados", JOptionPane.INFORMATION_MESSAGE);
			carregarDadosDoBanco();
		} catch (RuntimeException ex) {
			mostrarErro("Não foi possível salvar os dados: " + obterMensagemErro(ex));
		}
	}

	private Aluno criarAlunoComDadosPermitidos() {
		Aluno atualizado = new Aluno();
		atualizado.setIdAluno(alunoAtual.getIdAluno());
		atualizado.setNome(alunoAtual.getNome());
		atualizado.setCpf(alunoAtual.getCpf());
		atualizado.setMatricula(alunoAtual.getMatricula());
		atualizado.setDataNascimento(alunoAtual.getDataNascimento());
		atualizado.setDataCadastro(alunoAtual.getDataCadastro());
		atualizado.setSexo(alunoAtual.getSexo());
		atualizado.setSituacao(alunoAtual.getSituacao());
		atualizado.setIdPais(alunoAtual.getIdPais());
		atualizado.setIdTurma(alunoAtual.getIdTurma());
		atualizado.setTelefone(txtTelefone.getText());
		atualizado.setEmail(txtEmail.getText());
		atualizado.setRg(alunoAtual.getRg());
		atualizado.setObsSaude(alunoAtual.getObsSaude());
		atualizado.setEndereco(criarEnderecoAtualizado());
		return atualizado;
	}

	private Endereco criarEnderecoAtualizado() {
		Endereco enderecoOriginal = alunoAtual.getEndereco();
		Endereco endereco = new Endereco();

		if (enderecoOriginal != null && enderecoOriginal.getIdEndereco() > 0) {
			endereco.setIdEndereco(enderecoOriginal.getIdEndereco());
		}
		endereco.setRua(txtRua.getText());
		endereco.setNumero(txtNumero.getText());
		endereco.setComplemento(txtComplemento.getText());
		endereco.setBairro(txtBairro.getText());
		endereco.setCidade(txtCidade.getText());
		endereco.setCep(txtCep.getText());

		if (enderecoOriginal != null && enderecoOriginal.getEstado() != null) {
			endereco.setEstado(enderecoOriginal.getEstado());
		}
		return endereco;
	}

	private void abrirSuporte() {
		if (!SessaoUsuario.existeUsuarioLogado()) {
			mostrarErro("Sua sessão foi encerrada. Faça login novamente.");
			dispose();
			return;
		}
		new SuporteAluno().setVisible(true);
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

		JLabel subtitulo = new JLabel("Consulte suas informações pessoais, escolares, endereço e dados do responsável.");
		subtitulo.setForeground(new Color(245, 225, 255));
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		subtitulo.setBounds(42, 120, 900, 25);
		topo.add(subtitulo);
		return topo;
	}

	private JPanel criarCardSecao(String titulo) {
		JPanel painel = criarPainelArredondado();
		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitulo.setBounds(25, 18, 600, 30);
		painel.add(lblTitulo);

		JSeparator linha = new JSeparator();
		linha.setBounds(25, 52, 340, 1);
		linha.setForeground(corBorda);
		painel.add(linha);
		return painel;
	}

	private JTextField adicionarCampoEditavel(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel label = new JLabel(titulo);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		label.setBounds(x, y, 210, 22);
		painel.add(label);

		JTextField campo = new JTextField(valor);
		campo.setBounds(x, y + 22, 330, 28);
		campo.setEditable(true);
		campo.setForeground(textos);
		campo.setBackground(corCampo);
		campo.setCaretColor(textos);
		campo.setBorder(new LineBorder(corBorda));
		painel.add(campo);
		return campo;
	}

	private JLabel adicionarCampoSomenteLeitura(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel label = new JLabel(titulo);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		label.setBounds(x, y, 210, 22);
		painel.add(label);

		JLabel campo = new JLabel(valor);
		campo.setForeground(textos);
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		campo.setBounds(x, y + 22, 330, 24);
		painel.add(campo);
		return campo;
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

	private String formatarCpf(String cpf) {
		if (cpf == null || cpf.length() != 11) {
			return "Não informado";
		}
		return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
	}

	private String formatarData(java.time.LocalDate data) {
		if (data == null) {
			return "Não informado";
		}
		return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	private String valorOuNaoInformado(String valor) {
		if (valor == null || valor.isBlank()) {
			return "Não informado";
		}
		return valor;
	}

	private String valorVazio(String valor) {
		return valor == null ? "" : valor;
	}

	private String obterMensagemErro(Throwable erro) {
		if (erro == null) {
			return "Erro desconhecido.";
		}
		String mensagem = erro.getMessage();

		if (mensagem != null && !mensagem.isBlank()) {
			return mensagem;
		}
		if (erro.getCause() != null) {
			return obterMensagemErro(erro.getCause());
		}
		return "Erro desconhecido.";
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}
}