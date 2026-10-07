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

import util.SessaoUsuario;

public class MeusDadosResponsavel extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	public MeusDadosResponsavel() {

		setTitle("Meus Dados - Responsável");
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

		JPanel cardPerfil = criarPainelArredondado();
		cardPerfil.setBounds(xInicial, 0, 360, 300);
		cardPerfil.setLayout(null);
		painelRolagem.add(cardPerfil);

		JLabel foto = new JLabel("RESP.", SwingConstants.CENTER);
		foto.setOpaque(true);
		foto.setBackground(corCampo);
		foto.setForeground(textos);
		foto.setFont(new Font("Segoe UI", Font.BOLD, 28));
		foto.setBorder(new LineBorder(corBorda, 2));
		foto.setBounds(105, 25, 150, 150);
		cardPerfil.add(foto);

		JLabel nomeResponsavel = new JLabel("Maria Oliveira dos Santos", SwingConstants.CENTER);
		nomeResponsavel.setForeground(textos);
		nomeResponsavel.setFont(new Font("Segoe UI", Font.BOLD, 21));
		nomeResponsavel.setBounds(15, 190, 330, 30);
		cardPerfil.add(nomeResponsavel);

		JLabel tipoResponsavel = new JLabel("Responsável Financeiro e Pedagógico", SwingConstants.CENTER);
		tipoResponsavel.setForeground(textos);
		tipoResponsavel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tipoResponsavel.setBounds(15, 225, 330, 25);
		cardPerfil.add(tipoResponsavel);

		JLabel statusResponsavel = new JLabel("Situação: Ativo", SwingConstants.CENTER);
		statusResponsavel.setForeground(new Color(120, 255, 170));
		statusResponsavel.setFont(new Font("Segoe UI", Font.BOLD, 16));
		statusResponsavel.setBounds(20, 255, 320, 25);
		cardPerfil.add(statusResponsavel);

		JPanel cardDadosPessoais = criarCardSecao("Dados Pessoais");
		cardDadosPessoais.setBounds(xInicial + 390, 0, 830, 300);
		painelRolagem.add(cardDadosPessoais);

		adicionarCampo(cardDadosPessoais, "Nome completo:", "Maria Oliveira dos Santos", 25, 65);
		adicionarCampo(cardDadosPessoais, "CPF:", "987.654.321-00", 25, 115);
		adicionarCampo(cardDadosPessoais, "RG:", "11.222.333-4", 25, 165);
		adicionarCampo(cardDadosPessoais, "Data de nascimento:", "20/08/1984", 25, 215);

		adicionarCampo(cardDadosPessoais, "Sexo:", "Feminino", 440, 65);
		adicionarCampo(cardDadosPessoais, "Estado civil:", "Casada", 440, 115);
		adicionarCampo(cardDadosPessoais, "Profissão:", "Auxiliar Administrativo", 440, 165);
		adicionarCampo(cardDadosPessoais, "Status:", "Ativo", 440, 215);

		JPanel cardContato = criarCardSecao("Contato");
		cardContato.setBounds(xInicial, 330, 390, 270);
		painelRolagem.add(cardContato);

		adicionarCampo(cardContato, "Telefone principal:", "(47) 98888-8888", 25, 65);
		adicionarCampo(cardContato, "Telefone alternativo:", "(47) 99999-9999", 25, 115);
		adicionarCampo(cardContato, "E-mail:", "responsavel@email.com", 25, 165);
		adicionarCampo(cardContato, "Preferência:", "WhatsApp", 25, 215);

		JPanel cardAluno = criarCardSecao("Aluno Vinculado");
		cardAluno.setBounds(xInicial + 415, 330, 390, 270);
		painelRolagem.add(cardAluno);

		adicionarCampo(cardAluno, "Aluno:", "Pedro Henrique Mendes", 25, 65);
		adicionarCampo(cardAluno, "Parentesco:", "Mãe", 25, 115);
		adicionarCampo(cardAluno, "Turma:", "302", 25, 165);
		adicionarCampo(cardAluno, "Turno:", "Matutino", 25, 215);

		JPanel cardEndereco = criarCardSecao("Endereço");
		cardEndereco.setBounds(xInicial + 830, 330, 390, 270);
		painelRolagem.add(cardEndereco);

		adicionarCampo(cardEndereco, "Rua:", "Rua das Flores", 25, 65);
		adicionarCampo(cardEndereco, "Número:", "125", 25, 115);
		adicionarCampo(cardEndereco, "Bairro:", "Centro", 25, 165);
		adicionarCampo(cardEndereco, "Cidade/UF:", "Porto Belo - SC", 25, 215);

		JPanel cardDadosExtras = criarCardSecao("Informações Complementares");
		cardDadosExtras.setBounds(xInicial, 620, 600, 230);
		painelRolagem.add(cardDadosExtras);

		adicionarCampo(cardDadosExtras, "CEP:", "88210-000", 25, 65);
		adicionarCampo(cardDadosExtras, "Complemento:", "Casa", 25, 115);
		adicionarCampo(cardDadosExtras, "Autorizado a buscar aluno:", "Sim", 25, 165);

		adicionarCampo(cardDadosExtras, "Recebe comunicados:", "Sim", 315, 65);
		adicionarCampo(cardDadosExtras, "Canal principal:", "WhatsApp", 315, 115);
		adicionarCampo(cardDadosExtras, "Cadastro atualizado:", "02/06/2026", 315, 165);

		JPanel cardObservacoes = criarCardSecao("Observações");
		cardObservacoes.setBounds(xInicial + 630, 620, 590, 230);
		painelRolagem.add(cardObservacoes);

		JLabel obs = new JLabel(
				"<html>Responsável sem pendências cadastrais no momento.<br><br>"
						+ "Caso encontre alguma informação incorreta, procure a secretaria da escola "
						+ "ou solicite a atualização dos dados pelo botão abaixo.</html>");

		obs.setForeground(textos);
		obs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		obs.setBounds(25, 65, 530, 90);
		cardObservacoes.add(obs);

		JButton btnSolicitarAtualizacao = new JButton("Solicitar atualização de dados");
		btnSolicitarAtualizacao.setBounds(25, 165, 250, 42);
		estilizarBotaoAcao(btnSolicitarAtualizacao);
		btnSolicitarAtualizacao.addActionListener(e -> abrirSuporte());
		cardObservacoes.add(btnSolicitarAtualizacao);
		
		JButton btnAlterarSenha = new JButton("Alterar a Senha");
		btnAlterarSenha.setBounds(300, 165, 250, 42);
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

				g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				GradientPaint gp = new GradientPaint(
						0,
						0,
						new Color(70, 20, 160),
						getWidth(),
						getHeight(),
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

		JLabel sub = new JLabel("Consulte suas informações pessoais, contato, endereço e vínculo com o aluno.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(42, 120, 900, 25);
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

				g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(corCampo);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);

				g2.setColor(corBorda);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);

				g2.dispose();

				super.paintComponent(g);
			}
		};
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
		new SuporteResponsavel().setVisible(true);
	}
}