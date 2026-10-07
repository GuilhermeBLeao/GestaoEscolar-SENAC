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
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import model.Usuario;
import util.SessaoUsuario;
import variaveisEnum.TipoUsuario;

public class MenuCadastro extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;

	public MenuCadastro() {
		setTitle("Menu de Cadastros");

		Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		setMaximizedBounds(area);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		setContentPane(externo);

		JPanel interno = new JPanel(null);
		interno.setBounds(20, 20, area.width - 40, area.height - 40);
		interno.setBackground(corInterna);
		externo.add(interno);
		criarTela(interno);
	}

	private void criarTela(JPanel painel) {
		if (!validarAcesso()) {
			return;
		}

		JPanel topo = criarTopo();
		topo.setBounds(30, 25, painel.getWidth() - 60, 160);
		painel.add(topo);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(35, 35, 150, 40);
		estilizarBotao(btnVoltar);
		btnVoltar.addActionListener(e -> dispose());
		topo.add(btnVoltar);

		int larguraCard = 720;
		int alturaCard = 430;

		JPanel card = criarCard("Selecione o Cadastro");
		card.setBounds((painel.getWidth() - larguraCard) / 2, 245, larguraCard, alturaCard);

		JButton btnAluno = new JButton("Cadastro de Aluno");
		btnAluno.setBounds(115, 75, 490, 52);
		estilizarBotao(btnAluno);
		btnAluno.addActionListener(e -> abrirCadastroAluno());

		JButton btnDisciplina = new JButton("Cadastro de Disciplina");
		btnDisciplina.setBounds(115, 145, 490, 52);
		estilizarBotao(btnDisciplina);
		btnDisciplina.addActionListener(e -> abrirCadastroDisciplina());

		JButton btnProfessor = new JButton("Cadastro de Professor");
		btnProfessor.setBounds(115, 215, 490, 52);
		estilizarBotao(btnProfessor);
		btnProfessor.addActionListener(e -> abrirCadastroProfessor());

		JButton btnFuncionario = new JButton("Cadastro de Funcionário");
		btnFuncionario.setBounds(115, 285, 490, 52);
		estilizarBotao(btnFuncionario);
		btnFuncionario.addActionListener(e -> abrirCadastroFuncionario());

		card.add(btnAluno);
		card.add(btnDisciplina);
		card.add(btnProfessor);
		card.add(btnFuncionario);
		painel.add(card);
	}

	private boolean validarAcesso() {
		if (!SessaoUsuario.existeUsuarioLogado()) {
			JOptionPane.showMessageDialog(this, "É necessário estar autenticado para acessar o menu de cadastros.",
					"Acesso não autorizado", JOptionPane.WARNING_MESSAGE);
			SwingUtilities.invokeLater(this::dispose);
			return false;
		}

		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null || usuario.getTipoUsuario() == null) {
			JOptionPane.showMessageDialog(this, "Não foi possível identificar o perfil do usuário logado.",
					"Acesso não autorizado", JOptionPane.WARNING_MESSAGE);
			SwingUtilities.invokeLater(this::dispose);
			return false;
		}

		if (!usuario.isAtivo()) {
			JOptionPane.showMessageDialog(this, "O usuário logado está inativo.", "Acesso não autorizado",
					JOptionPane.WARNING_MESSAGE);
			SwingUtilities.invokeLater(this::dispose);
			return false;
		}
		if (!usuarioPodeAcessarCadastros(usuario)) {
			JOptionPane.showMessageDialog(this, "Seu perfil não possui autorização para acessar os cadastros.",
					"Acesso não autorizado", JOptionPane.WARNING_MESSAGE);
			SwingUtilities.invokeLater(this::dispose);
			return false;
		}
		return true;
	}

	private boolean usuarioPodeAcessarCadastros(Usuario usuario) {
		TipoUsuario tipo = usuario.getTipoUsuario();
		return tipo == TipoUsuario.ADMINISTRADOR || tipo == TipoUsuario.SECRETARIA || tipo == TipoUsuario.DIRECAO || tipo == TipoUsuario.PEDAGOGICO;
	}

	private void abrirCadastroDisciplina() {
		if (!validarAcessoAoAbrir()) {
			return;
		}
		new CadastroDisciplina().setVisible(true);
		dispose();
	}
	
	private void abrirCadastroAluno() {
		if (!validarAcessoAoAbrir()) {
			return;
		}
		new CadastroAluno().setVisible(true);
		dispose();
	}

	private void abrirCadastroProfessor() {
		if (!validarAcessoAoAbrir()) {
			return;
		}
		new CadastroProfessor().setVisible(true);
		dispose();
	}

	private void abrirCadastroFuncionario() {
		if (!validarAcessoAoAbrir()) {
			return;
		}
		new CadastroFuncionario().setVisible(true);
		dispose();
	}

	private boolean validarAcessoAoAbrir() {
		if (!SessaoUsuario.existeUsuarioLogado()) {
			JOptionPane.showMessageDialog(this, "Sua sessão não está mais ativa.", "Sessão encerrada",
					JOptionPane.WARNING_MESSAGE);
			dispose();
			return false;
		}

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getTipoUsuario() == null) {
			JOptionPane.showMessageDialog(this, "Não foi possível identificar o usuário logado.", "Erro",
					JOptionPane.ERROR_MESSAGE);
			dispose();
			return false;
		}

		if (!usuario.isAtivo()) {
			JOptionPane.showMessageDialog(this, "O usuário logado está inativo.", "Acesso não autorizado",
					JOptionPane.WARNING_MESSAGE);
			dispose();
			return false;
		}
		return usuarioPodeAcessarCadastros(usuario);
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				g2.dispose();
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Menu de Cadastros");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 78, 600, 45);
		topo.add(titulo);

		JLabel subtitulo = new JLabel("Selecione o cadastro desejado");
		subtitulo.setForeground(new Color(240, 240, 255));
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		subtitulo.setBounds(45, 120, 700, 25);
		topo.add(subtitulo);
		return topo;
	}

	private JPanel criarCard(String titulo) {
		JPanel painel = criarPainelArredondado();
		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitulo.setBounds(30, 20, 350, 30);
		painel.add(lblTitulo);
		return painel;
	}

	private void estilizarBotao(JButton botao) {
		botao.setBackground(corCampo);
		botao.setForeground(textos);
		botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
		botao.setFocusPainted(false);
		botao.setBorder(new LineBorder(corBorda, 1, true));
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
			}
		};
	}
}