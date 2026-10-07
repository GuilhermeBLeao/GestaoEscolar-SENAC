package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

import controller.UsuarioController;
import model.Usuario;
import util.SessaoUsuario;

public class AlterarSenhaUsuario extends JFrame {
	private static final long serialVersionUID = 1L;

	private JFormattedTextField txtCpf;
	private JTextField txtUsuario;
	private JPasswordField txtNovaSenha;
	private JPasswordField txtConfirmarSenha;
	private JButton btnBuscar;
	private JButton btnSalvar;
	private JButton btnCancelar;
	private UsuarioController usuarioController;
	private Usuario usuarioSelecionado;
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;
	private SessaoUsuario usuarioSessao;

	@SuppressWarnings("static-access")
	public AlterarSenhaUsuario() {
		usuarioController = new UsuarioController();
		initialize();
		handleUser(usuarioSessao.getUsuarioLogado());
	}

	private void initialize() {
		setTitle("Alterar Senha do Usuário");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(680, 500);
		setLocationRelativeTo(null);
		setResizable(false);

		JPanel contentPane = new JPanel(new BorderLayout(15, 15));
		contentPane.setBackground(corExterna);
		contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
		setContentPane(contentPane);
		contentPane.add(criarCabecalho(), BorderLayout.NORTH);
		contentPane.add(criarFormulario(), BorderLayout.CENTER);
		contentPane.add(criarBotoes(), BorderLayout.SOUTH);
		adicionarEventos();
		atualizarEstadoInicial();
	}

	private JPanel criarCabecalho() {
		JPanel panel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				GradientPaint gradiente = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(),
						new Color(190, 35, 170));
				g2.setPaint(gradiente);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
				g2.dispose();
			}
		};
		panel.setOpaque(false);
		panel.setPreferredSize(new java.awt.Dimension(0, 72));

		JLabel titulo = new JLabel("ALTERAÇÃO DE SENHA");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
		titulo.setForeground(textos);
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBorder(new EmptyBorder(5, 5, 15, 5));
		panel.add(titulo, BorderLayout.CENTER);
		return panel;
	}

	private JPanel criarFormulario() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(corCampo);
		panel.setBorder(new LineBorder(corBorda, 1, true));
		GridBagConstraints g = new GridBagConstraints();
		g.insets = new Insets(8, 8, 8, 8);
		g.fill = GridBagConstraints.HORIZONTAL;
		try {
			MaskFormatter mask = new MaskFormatter("###.###.###-##");
			mask.setPlaceholderCharacter('_');
			txtCpf = new JFormattedTextField(mask);
			configurarCampoMascara(txtCpf);
		} catch (ParseException e) {
			txtCpf = new JFormattedTextField();
		}
		txtUsuario = new JTextField();
		txtUsuario.setEditable(false);
		txtNovaSenha = new JPasswordField();
		txtConfirmarSenha = new JPasswordField();
		btnBuscar = new JButton("Buscar");
		estilizarBotao(btnBuscar, corCampo);
		int y = 0;
		adicionarCampo(panel, g, "CPF:", txtCpf, y++);
		adicionarBotaoLinha(panel, g, btnBuscar, y++);
		adicionarCampo(panel, g, "Usuário:", txtUsuario, y++);
		adicionarCampo(panel, g, "Nova senha:", txtNovaSenha, y++);
		adicionarCampo(panel, g, "Confirmar senha:", txtConfirmarSenha, y++);
		return panel;
	}

	private void configurarCampoMascara(JFormattedTextField campo) {
		campo.setBackground(corCampo);
		campo.setForeground(textos);
		campo.setCaretColor(textos);
		campo.setBorder(new LineBorder(corBorda));
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
	}

	private JPanel criarBotoes() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
		panel.setBackground(corExterna);
		btnCancelar = new JButton("Cancelar");
		btnSalvar = new JButton("Salvar");
		estilizarBotao(btnCancelar, corCampo);
		estilizarBotao(btnSalvar, corCampo);
		panel.add(btnCancelar);
		panel.add(btnSalvar);
		return panel;
	}

	private void adicionarCampo(JPanel panel, GridBagConstraints g, String label, JComponent field, int y) {
		g.gridx = 0;
		g.gridy = y;
		g.weightx = 0;

		JLabel labelComponent = new JLabel(label);
		labelComponent.setForeground(corLabel);
		labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(labelComponent, g);
		g.gridx = 1;
		g.weightx = 1;

		if (field instanceof JTextField) {
			estilizarCampo((JTextField) field);
		}
		panel.add(field, g);
	}

	private void adicionarBotaoLinha(JPanel panel, GridBagConstraints g, JButton btn, int y) {
		g.gridx = 1;
		g.gridy = y;
		g.anchor = GridBagConstraints.WEST;
		g.weightx = 0;
		panel.add(btn, g);
	}

	private void estilizarBotao(JButton btn, Color cor) {
		btn.setBackground(cor);
		btn.setForeground(textos);
		btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btn.setFocusPainted(false);
		btn.setBorder(new LineBorder(corBorda));
	}

	private void estilizarCampo(JTextField campo) {
		campo.setBackground(corCampo);
		campo.setForeground(textos);
		campo.setCaretColor(textos);
		campo.setBorder(new LineBorder(corBorda));
	}

	private void atualizarEstadoInicial() {
		txtUsuario.setEnabled(false);
		btnSalvar.setEnabled(false);
	}

	private void adicionarEventos() {
		btnCancelar.addActionListener(e -> dispose());
		btnBuscar.addActionListener(e -> buscarUsuario());
		btnSalvar.addActionListener(e -> salvarSenha());
	}

	private void buscarUsuario() {
		try {
			String cpf = txtCpf.getText().replaceAll("[^0-9]", "");

			if (cpf.isEmpty() || cpf.length() != 11) {
				JOptionPane.showMessageDialog(this, "Informe um CPF válido!", "Atenção", JOptionPane.WARNING_MESSAGE);
				return;
			}
			usuarioSelecionado = usuarioController.buscarUsuarioPorCpf(cpf);

			handleUser(usuarioSelecionado);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleUser(Usuario usuario) {
		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Usuário não encontrado!", "Erro", JOptionPane.ERROR_MESSAGE);
			txtUsuario.setText("");
			btnSalvar.setEnabled(false);
			return;
		}
		usuarioSelecionado = usuario;
		txtUsuario.setText(usuarioSelecionado.getCpf());
		btnSalvar.setEnabled(true);
	}

	private void salvarSenha() {
		try {
			if (usuarioSelecionado == null) {
				JOptionPane.showMessageDialog(this, "Busque um usuário primeiro!", "Atenção",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			String senha = new String(txtNovaSenha.getPassword());
			String confirmar = new String(txtConfirmarSenha.getPassword());

			if (senha.isBlank() || confirmar.isBlank()) {
				JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Atenção",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (!senha.equals(confirmar)) {
				JOptionPane.showMessageDialog(this, "As senhas não conferem!", "Erro", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String hash = org.mindrot.jbcrypt.BCrypt.hashpw(senha, org.mindrot.jbcrypt.BCrypt.gensalt());
			usuarioController.atualizarSenhaHash(usuarioSelecionado.getIdUsuario(), hash);
			JOptionPane.showMessageDialog(this, "Senha alterada com sucesso!", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			dispose();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
}