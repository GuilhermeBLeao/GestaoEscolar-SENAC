package view;

import dao.FuncionarioDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Funcionario;
import model.Usuario;
import util.SessaoUsuario;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

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

public class MeusDadosFuncionario extends JFrame {
private static final long serialVersionUID = 1L;

private final Color corExterna = new Color(27, 0, 69);
private final Color corInterna = new Color(38, 2, 92);
private final Color corCampo = new Color(25, 6, 75);
private final Color corBorda = new Color(120, 70, 220);
private final Color corLabel = new Color(255, 120, 220);
private final Color textos = Color.WHITE;
private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

public MeusDadosFuncionario() {
	setTitle("Meus Dados - Funcionário");
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
	scroll.getVerticalScrollBar().setUnitIncrement(26);
	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	interno.add(scroll);

	carregarDadosFuncionario(painelRolagem, xInicial);
}

private void carregarDadosFuncionario(JPanel painelRolagem, int xInicial) {
	Usuario usuario = SessaoUsuario.getUsuarioLogado();

	if (usuario == null) {
		JOptionPane.showMessageDialog(this, "Nenhum usuário está logado.",
				"Acesso negado", JOptionPane.WARNING_MESSAGE);
		dispose();
		return;
	}

	if (usuario.getFuncionarioId() <= 0) {
		JOptionPane.showMessageDialog(this, "O usuário logado não possui um funcionário associado.",
				"Dados do funcionário", JOptionPane.WARNING_MESSAGE);
		dispose();
		return;
	}

	try (Connection conn = ConnectionFactory.getConnection()) {
		FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
		Funcionario funcionario = funcionarioDAO.buscarPorId(usuario.getFuncionarioId());

		if (funcionario == null) {
			JOptionPane.showMessageDialog(this, "Não foi possível localizar os dados do funcionário no banco de dados.",
					"Dados do funcionário", JOptionPane.WARNING_MESSAGE);
			dispose();
			return;
		}
		criarCardsFuncionario(painelRolagem, xInicial, funcionario, usuario);
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(this, "Erro ao carregar os dados do funcionário:\n" + e.getMessage(),
				"Erro", JOptionPane.ERROR_MESSAGE);
		dispose();
	} catch (RuntimeException e) {
		JOptionPane.showMessageDialog(this, "Erro ao carregar os dados do funcionário:\n" + e.getMessage(),
				"Erro", JOptionPane.ERROR_MESSAGE);
		dispose();
	}
}

private void criarCardsFuncionario(JPanel painelRolagem, int xInicial, Funcionario funcionario, Usuario usuario) {
	String nome = valorOuPadrao(funcionario.getNome());
	String cpf = formatarCpf(funcionario.getCpf());
	String rg = valorOuPadrao(funcionario.getRg());
	String dataNascimento = formatarData(funcionario.getDataNascimento());
	String sexo = formatarEnum(funcionario.getSexo());
	String telefone = formatarTelefone(funcionario.getTelefone());
	String email = valorOuPadrao(funcionario.getEmail());
	String status = funcionario.isAtivo() ? "Ativo" : "Inativo";
	String cargo = valorOuPadrao(funcionario.getCargo());
	String setor = valorOuPadrao(funcionario.getSetor());
	String dataContratacao = formatarData(funcionario.getDataContratacao());
	String perfil = formatarEnum(funcionario.getPerfil());

	@SuppressWarnings("unused")
	String permissoes = "Nenhuma";
	if (funcionario.getPerfil() != null && funcionario.getPerfil().getPermissoes() != null
			&& !funcionario.getPerfil().getPermissoes().isEmpty()) {
		permissoes = funcionario.getPerfil()
				.getPermissoes()
				.stream()
				.map(Enum::name)
				.map(this::formatarEnumTexto)
				.sorted()
				.collect(Collectors.joining(", "));
	}
	String ultimoAcesso = formatarDataHora(usuario.getUltimoLogin());

	JPanel cardPerfil = criarPainelArredondado();
	cardPerfil.setBounds(xInicial + 5, 0, 390, 300);
	cardPerfil.setLayout(null);
	painelRolagem.add(cardPerfil);

	JLabel foto = new JLabel("FUNC.", SwingConstants.CENTER);
	foto.setOpaque(true);
	foto.setBackground(corCampo);
	foto.setForeground(textos);
	foto.setFont(new Font("Segoe UI", Font.BOLD, 28));
	foto.setBorder(new LineBorder(corBorda, 2));
	foto.setBounds(105, 25, 150, 150);
	cardPerfil.add(foto);

	JLabel nomeFuncionario = new JLabel(nome, SwingConstants.CENTER);
	nomeFuncionario.setForeground(Color.WHITE);
	nomeFuncionario.setFont(new Font("Segoe UI", Font.BOLD, 21));
	nomeFuncionario.setBounds(15, 190, 360, 30);
	cardPerfil.add(nomeFuncionario);

	JLabel cargoFuncionario = new JLabel(cargo, SwingConstants.CENTER);
	cargoFuncionario.setForeground(textos);
	cargoFuncionario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
	cargoFuncionario.setBounds(15, 225, 360, 25);
	cardPerfil.add(cargoFuncionario);

	JLabel statusFuncionario = new JLabel("Situação: " + status, SwingConstants.CENTER);
	statusFuncionario.setForeground(funcionario.isAtivo()
			? new Color(120, 255, 170) : new Color(255, 120, 120));
	statusFuncionario.setFont(new Font("Segoe UI", Font.BOLD, 16));
	statusFuncionario.setBounds(20, 255, 350, 25);
	cardPerfil.add(statusFuncionario);

	JPanel cardDadosPessoais = criarCardSecao("Dados Pessoais");
	cardDadosPessoais.setBounds(xInicial + 410, 0, 790, 300);
	painelRolagem.add(cardDadosPessoais);

	adicionarCampo(cardDadosPessoais, "Nome completo:", nome, 25, 65);
	adicionarCampo(cardDadosPessoais, "CPF:", cpf, 25, 115);
	adicionarCampo(cardDadosPessoais, "RG:", rg, 25, 165);
	adicionarCampo(cardDadosPessoais, "Data de nascimento:", dataNascimento, 25, 215);
	adicionarCampo(cardDadosPessoais, "Sexo:", sexo, 440, 65);
	adicionarCampo(cardDadosPessoais, "Telefone:", telefone, 440, 115);
	adicionarCampo(cardDadosPessoais, "E-mail:", email, 440, 165);
	adicionarCampo(cardDadosPessoais, "Status:", status, 440, 215);

	JPanel cardProfissional = criarCardSecao("Dados Profissionais");
	cardProfissional.setBounds(xInicial + 5, 325, 390, 270);
	painelRolagem.add(cardProfissional);

	adicionarCampo(cardProfissional, "Código do funcionário:", 
			String.valueOf(funcionario.getIdFuncionario()), 25, 65);
	adicionarCampo(cardProfissional, "Cargo:", cargo, 25, 115);
	adicionarCampo(cardProfissional, "Setor:", setor, 25, 165);
	adicionarCampo(cardProfissional, "Contratação:", dataContratacao, 25, 215);

	JPanel cardAcesso = criarCardSecao("Acesso e Função");
	cardAcesso.setBounds(xInicial + 410, 325, 390, 270);
	painelRolagem.add(cardAcesso);
	adicionarCampo(cardAcesso, "Perfil de acesso:", perfil, 25, 65);
	adicionarCampo(cardAcesso, "Permissões:", funcionario.getPerfil() == null
					? "Nenhuma" : funcionario.getPerfil().getPermissoes().size() + " cadastradas", 25, 115);
	adicionarCampo(cardAcesso, "CPF de acesso:", formatarCpf(usuario.getCpf()), 25, 165);
	adicionarCampo(cardAcesso, "Último acesso:", ultimoAcesso, 25, 215);

	JPanel cardEndereco = criarCardSecao("Endereço");
	cardEndereco.setBounds(xInicial + 815, 325, 390, 270);
	painelRolagem.add(cardEndereco);

	Endereco endereco = funcionario.getEndereco();
	String rua = endereco == null ? "Não informado" : valorOuPadrao(endereco.getRua());
	String numero = endereco == null ? "Não informado" : valorOuPadrao(endereco.getNumero());
	String bairro = endereco == null ? "Não informado" : valorOuPadrao(endereco.getBairro());
	String cidadeUf = endereco == null ? "Não informado"
			: valorOuPadrao(endereco.getCidade()) + " - " + formatarEnum(endereco.getEstado());
	adicionarCampo(cardEndereco, "Rua:", rua, 25, 65);
	adicionarCampo(cardEndereco, "Número:", numero, 25, 115);
	adicionarCampo(cardEndereco, "Bairro:", bairro, 25, 165);
	adicionarCampo(cardEndereco, "Cidade/UF:", cidadeUf, 25, 215);

	JPanel cardDadosExtras = criarCardSecao("Informações Complementares");
	cardDadosExtras.setBounds(xInicial + 5, 610, 605, 230);
	painelRolagem.add(cardDadosExtras);
	String cep = endereco == null ? "Não informado" : valorOuPadrao(endereco.getCep());
	String complemento = endereco == null ? "Não informado" : valorOuPadrao(endereco.getComplemento());
	adicionarCampo(cardDadosExtras, "CEP:", formatarCep(cep), 25, 65);
	adicionarCampo(cardDadosExtras, "Complemento:", complemento, 25, 115);
	adicionarCampo(cardDadosExtras, "Situação cadastral:", status, 25, 165);
	adicionarCampo(cardDadosExtras, "Admissão:", dataContratacao, 315, 65);
	adicionarCampo(cardDadosExtras, "ID do funcionário:",
			String.valueOf(funcionario.getIdFuncionario()), 315, 115);
	adicionarCampo(cardDadosExtras, "Perfil:", perfil, 315, 165);
	
	JPanel cardObservacoes = criarCardSecao("Informações de Acesso");
	cardObservacoes.setBounds(xInicial + 615, 610, 590, 230);
	painelRolagem.add(cardObservacoes);

	JLabel obs = new JLabel("<html>Os dados exibidos nesta tela foram carregados diretamente "
					+ "do banco de dados para o funcionário associado ao usuário "
					+ "atualmente logado.<br><br>"
					+ "Caso alguma informação esteja incorreta, procure a direção "
					+ "ou solicite a atualização dos dados.</html>");
	obs.setForeground(textos);
	obs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
	obs.setBounds(25, 65, 530, 90);
	cardObservacoes.add(obs);

	JButton btnSolicitarAtualizacao = new JButton("Solicitar atualização de dados");
	btnSolicitarAtualizacao.setBounds(25, 165, 250, 42);
	estilizarBotaoAcao(btnSolicitarAtualizacao);
	btnSolicitarAtualizacao.addActionListener(e -> new AtendimentoSuporte().setVisible(true));
	cardObservacoes.add(btnSolicitarAtualizacao);
	
	JButton btnAlterarSenha = new JButton("Alterar a senha");
	btnAlterarSenha.setBounds(310, 165, 250, 42);
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
			GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(),
					getHeight(), new Color(190, 35, 170));
			g2.setPaint(gp);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
			g2.dispose();
			super.paintComponent(g);
		}
	};
	topo.setOpaque(false);

	JLabel titulo = new JLabel("Meus Dados");
	titulo.setForeground(Color.WHITE);
	titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
	titulo.setBounds(40, 80, 500, 45);
	topo.add(titulo);

	JLabel sub = new JLabel("Consulte suas informações pessoais, profissionais, acesso, endereço e cadastro.");
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
	lblValor.setForeground(Color.WHITE);
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

private String valorOuPadrao(String valor) {
	if (valor == null || valor.isBlank()) {
		return "Não informado";
	}
	return valor;
}

private String formatarCpf(String cpf) {
	if (cpf == null || cpf.isBlank()) {
		return "Não informado";
	}

	String numero = cpf.replaceAll("\\D", "");

	if (numero.length() != 11) {
		return cpf;
	}
	return numero.substring(0, 3) + "."
			+ numero.substring(3, 6) + "."
			+ numero.substring(6, 9) + "-"
			+ numero.substring(9);
}

private String formatarTelefone(String telefone) {
	if (telefone == null || telefone.isBlank()) {
		return "Não informado";
	}

	String numero = telefone.replaceAll("\\D", "");

	if (numero.length() == 11) {
		return "(" + numero.substring(0, 2)
				+ ") " + numero.substring(2, 7)
				+ "-" + numero.substring(7);
	}

	if (numero.length() == 10) {
		return "(" + numero.substring(0, 2)
				+ ") " + numero.substring(2, 6)
				+ "-" + numero.substring(6);
	}
	return telefone;
}

private String formatarCep(String cep) {
	if (cep == null || cep.isBlank()) {
		return "Não informado";
	}

	String numero = cep.replaceAll("\\D", "");

	if (numero.length() != 8) {
		return cep;
	}
	return numero.substring(0, 5) + "-" + numero.substring(5);
}

private String formatarData(LocalDate data) {
	if (data == null) {
		return "Não informado";
	}
	return data.format(FORMATO_DATA);
}

private String formatarDataHora(LocalDateTime dataHora) {
	if (dataHora == null) {
		return "Não informado";
	}
	return dataHora.format(FORMATO_DATA_HORA);
}

private String formatarEnum(Object valor) {
	if (valor == null) {
		return "Não informado";
	}
	return formatarEnumTexto(valor.toString());
}

private String formatarEnumTexto(String texto) {
	if (texto == null || texto.isBlank()) {
		return "Não informado";
	}

	String[] partes = texto.toLowerCase().split("_");
	StringBuilder resultado = new StringBuilder();

	for (String parte : partes) {
		if (parte.isBlank()) {
			continue;
		}
		if (resultado.length() > 0) {
			resultado.append(" ");
		}
		resultado.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1));
	}
	return resultado.toString();
}
}