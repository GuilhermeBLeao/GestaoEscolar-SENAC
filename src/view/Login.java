package view;

import controller.UsuarioController;
import model.Usuario;
import util.SessaoUsuario;
import variaveisEnum.TipoUsuario;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;
	private JFormattedTextField txtCpf;
	private JPasswordField txtSenha;
	private JCheckBox chkMostrarSenha;

	public Login() {
		setTitle("Login - Gestão Escolar");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		Dimension tamanhoTela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();

		int largura = 500;
		int altura = 600;
		int x = (tamanhoTela.width - largura) / 2;
		int y = (tamanhoTela.height - altura) / 2;
		setBounds(x, y, largura, altura);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(externo);

		JPanel interno = criarPainelArredondado(corInterna, corBorda, 30);
		interno.setLayout(null);
		interno.setBounds(15, 15, largura - 30, altura - 55);
		externo.add(interno);

		JLabel titulo = new JLabel("Gestão Escolar");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBounds(30, 55, 410, 45);
		interno.add(titulo);

		JLabel subtitulo = new JLabel("Acesso ao sistema");
		subtitulo.setForeground(textos);
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
		subtitulo.setBounds(30, 105, 410, 30);
		interno.add(subtitulo);

		JLabel lblCpf = new JLabel("CPF");
		lblCpf.setForeground(corLabel);
		lblCpf.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblCpf.setBounds(55, 175, 350, 25);
		interno.add(lblCpf);

		txtCpf = criarCampoCpf();
		txtCpf.setBounds(55, 205, 350, 42);
		interno.add(txtCpf);

		JLabel lblSenha = new JLabel("Senha");
		lblSenha.setForeground(corLabel);
		lblSenha.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblSenha.setBounds(55, 270, 350, 25);
		interno.add(lblSenha);

		txtSenha = new JPasswordField();
		txtSenha.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtSenha.setForeground(textos);
		txtSenha.setCaretColor(textos);
		txtSenha.setBackground(corCampo);
		txtSenha.setBorder(new LineBorder(corBorda, 1, true));
		txtSenha.setBounds(55, 300, 350, 42);
		interno.add(txtSenha);

		chkMostrarSenha = new JCheckBox("Mostrar senha");
		chkMostrarSenha.setForeground(textos);
		chkMostrarSenha.setBackground(corInterna);
		chkMostrarSenha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		chkMostrarSenha.setFocusPainted(false);
		chkMostrarSenha.setBounds(55, 350, 150, 30);
		interno.add(chkMostrarSenha);
		chkMostrarSenha.addActionListener(e -> {
			if (chkMostrarSenha.isSelected()) {
				txtSenha.setEchoChar((char) 0);
			} else {
				txtSenha.setEchoChar('\u2022');
			}
		});

		JButton btnEntrar = new JButton("Entrar");
		estilizarBotao(btnEntrar);
		btnEntrar.setBounds(55, 405, 350, 45);
		interno.add(btnEntrar);

		JButton btnCancelar = new JButton("Cancelar");
		estilizarBotao(btnCancelar);
		btnCancelar.setBounds(55, 465, 350, 40);
		interno.add(btnCancelar);

		btnEntrar.addActionListener(e -> autenticar());
		btnCancelar.addActionListener(e -> {
			SessaoUsuario.encerrarSessao();
			dispose();
		});

		txtCpf.addActionListener(e -> txtSenha.requestFocus());
		txtSenha.addActionListener(e -> autenticar());
		getRootPane().setDefaultButton(btnEntrar);
		setVisible(true);
	}

	private void autenticar() {
		String cpf = txtCpf.getText().replaceAll("\\D", "");
		String senha = new String(txtSenha.getPassword());
		autenticar(cpf, senha);
	}

	private void autenticar(String cpf, String senha) {
		try {
			UsuarioController controller = new UsuarioController();
			Usuario usuario = controller.autenticarLogin(cpf, senha);
			if (usuario == null || !usuario.isAtivo()) {
				JOptionPane.showMessageDialog(this, "CPF ou senha inválidos.", "Login", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (SessaoUsuario.existeUsuarioLogado()) {
				SessaoUsuario.encerrarSessao();
			}
			TipoUsuario perfil = escolherPerfil(usuario);
			if (perfil == null) {
				return;
			}
			usuario.setTipoUsuario(perfil);
			SessaoUsuario.setUsuarioLogado(usuario);
			controller.atualizarUltimoLogin(usuario.getIdUsuario(), LocalDateTime.now());
			abrirTelaInicial(perfil);
			dispose();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Não foi possível realizar a autenticação. " + "Tente novamente.\n\n" + ex.getMessage(),
					"Erro de autenticação", JOptionPane.ERROR_MESSAGE);
		}
	}

	private TipoUsuario escolherPerfil(Usuario usuario) {
		boolean temProfessor = usuario.getProfessorId() > 0;
		boolean temResponsavel = usuario.getPaiId() > 0;
		if (temProfessor && temResponsavel) {
			SelecaoPerfil selecao = new SelecaoPerfil(this, "CPF " + usuario.getCpf(), true, true);
			selecao.setVisible(true);

			String perfilSelecionado = selecao.getPerfilSelecionado();
			if ("PROFESSOR".equals(perfilSelecionado)) {
				return TipoUsuario.PROFESSOR;
			}
			if ("RESPONSAVEL".equals(perfilSelecionado)) {
				return TipoUsuario.RESPONSAVEL;
			}
			return null;
		}
		return usuario.getTipoUsuario();
	}

	private void abrirTelaInicial(TipoUsuario perfil) {
		if (perfil == null) {
			return;
		}
		switch (perfil) {
		case PROFESSOR:
			new TelaInicialProfessor().setVisible(true);
			break;
		case RESPONSAVEL:
			new TelaInicialPais().setVisible(true);
			break;
		case ALUNO:
			new TelaInicialAluno().setVisible(true);
			break;
		case SECRETARIA:
		case DIRECAO:
		case PEDAGOGICO:
			new TelaInicialFuncionario().setVisible(true);
			break;
		default:
			JOptionPane.showMessageDialog(this, "O perfil do usuário não possui " + "uma tela inicial configurada.",
					"Perfil inválido", JOptionPane.WARNING_MESSAGE);
			SessaoUsuario.encerrarSessao();
			break;
		}
	}

	private JFormattedTextField criarCampoCpf() {
		try {
			MaskFormatter mascara = new MaskFormatter("###.###.###-##");
			mascara.setPlaceholderCharacter('_');

			JFormattedTextField campo = new JFormattedTextField(mascara);
			campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			campo.setForeground(textos);
			campo.setCaretColor(textos);
			campo.setBackground(corCampo);
			campo.setBorder(new LineBorder(corBorda, 1, true));
			campo.setFocusLostBehavior(JFormattedTextField.COMMIT);
			campo.addFocusListener(new java.awt.event.FocusAdapter() {
				@Override
				public void focusGained(java.awt.event.FocusEvent e) {
					SwingUtilities.invokeLater(() -> {
						campo.setCaretPosition(0);
					});
				}
			});
			campo.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (campo.getText().trim().isEmpty()) {
						SwingUtilities.invokeLater(() -> campo.setCaretPosition(0));
					}
				}
			});
			return campo;
		} catch (java.text.ParseException ex) {
			throw new IllegalStateException("Não foi possível configurar a máscara de CPF.", ex);
		}
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Login().setVisible(true));
	}
}