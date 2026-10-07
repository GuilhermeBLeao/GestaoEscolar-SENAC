package view;

import controller.UsuarioController;
import model.Usuario;
import util.SessaoUsuario;

import java.awt.Font;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;

public class AlterarSenha extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel painelSenha;
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;
	private JTextField txtNomeUsuario;
	private JPasswordField txtSenhaAtual;
	private JPasswordField txtSenhaNova;
	private JCheckBox chkMostrarSenhaAtual;
	private JCheckBox chkMostrarSenhaNova;
	private UsuarioController usuarioController;
	private JButton btnSalvar;
	private Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();

	public AlterarSenha() {
		setTitle("Alterar Senha");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 900, 500);
		setResizable(false);
		setLocationRelativeTo(null);

		usuarioController = new UsuarioController();

		painelSenha = new JPanel();
		painelSenha.setBorder(null);
		painelSenha.setBackground(corExterna);
		setContentPane(painelSenha);
		painelSenha.setLayout(new BorderLayout());

		JPanel externo = new JPanel();
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(15, 15, 15, 15));
		externo.setLayout(new BorderLayout());
		painelSenha.add(externo, BorderLayout.CENTER);

		JPanel interno = criarPainelArredondado(corInterna, corBorda, 30);
		interno.setLayout(null);
		externo.add(interno, BorderLayout.CENTER);

		JLabel titulo = new JLabel("Altere a sua senha");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 40));
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBounds(200, 10, 410, 45);
		interno.add(titulo);

		JLabel lblNomeUsuario = new JLabel("Nome Usuário");
		lblNomeUsuario.setForeground(corLabel);
		lblNomeUsuario.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblNomeUsuario.setBounds(55, 65, 350, 25);
		interno.add(lblNomeUsuario);

		txtNomeUsuario = new JTextField();
		txtNomeUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtNomeUsuario.setForeground(textos);
		txtNomeUsuario.setCaretColor(textos);
		txtNomeUsuario.setBackground(corCampo);
		txtNomeUsuario.setBorder(new LineBorder(corBorda, 1, true));
		txtNomeUsuario.setBounds(55, 90, 755, 40);
		txtNomeUsuario.setColumns(10);
		txtNomeUsuario.setEditable(false);
		interno.add(txtNomeUsuario);

		JLabel lblSenhaAtual = new JLabel("Senha Atual");
		lblSenhaAtual.setForeground(corLabel);
		lblSenhaAtual.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblSenhaAtual.setBounds(55, 140, 350, 25);
		interno.add(lblSenhaAtual);

		txtSenhaAtual = new JPasswordField();
		txtSenhaAtual.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtSenhaAtual.setForeground(textos);
		txtSenhaAtual.setCaretColor(textos);
		txtSenhaAtual.setBackground(corCampo);
		txtSenhaAtual.setBorder(new LineBorder(corBorda, 1, true));
		txtSenhaAtual.setBounds(55, 165, 755, 40);
		interno.add(txtSenhaAtual);

		chkMostrarSenhaAtual = new JCheckBox("Mostrar senha atual");
		chkMostrarSenhaAtual.setForeground(textos);
		chkMostrarSenhaAtual.setBackground(corInterna);
		chkMostrarSenhaAtual.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		chkMostrarSenhaAtual.setFocusPainted(false);
		chkMostrarSenhaAtual.setBounds(55, 205, 180, 25);
		interno.add(chkMostrarSenhaAtual);
		chkMostrarSenhaAtual.addActionListener(e -> {
			if (chkMostrarSenhaAtual.isSelected()) {
				txtSenhaAtual.setEchoChar((char) 0);
			} else {
				txtSenhaAtual.setEchoChar('\u2022');
			}
		});

		JLabel lblSenhaNova = new JLabel("Nova Senha");
		lblSenhaNova.setForeground(corLabel);
		lblSenhaNova.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblSenhaNova.setBounds(55, 240, 350, 25);
		interno.add(lblSenhaNova);

		txtSenhaNova = new JPasswordField();
		txtSenhaNova.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtSenhaNova.setForeground(textos);
		txtSenhaNova.setCaretColor(textos);
		txtSenhaNova.setBackground(corCampo);
		txtSenhaNova.setBorder(new LineBorder(corBorda, 1, true));
		txtSenhaNova.setBounds(55, 265, 755, 40);
		interno.add(txtSenhaNova);

		chkMostrarSenhaNova = new JCheckBox("Mostrar nova senha");
		chkMostrarSenhaNova.setForeground(textos);
		chkMostrarSenhaNova.setBackground(corInterna);
		chkMostrarSenhaNova.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		chkMostrarSenhaNova.setFocusPainted(false);
		chkMostrarSenhaNova.setBounds(55, 305, 180, 30);
		interno.add(chkMostrarSenhaNova);
		chkMostrarSenhaNova.addActionListener(e -> {
			if (chkMostrarSenhaNova.isSelected()) {
				txtSenhaNova.setEchoChar((char) 0);
			} else {
				txtSenhaNova.setEchoChar('\u2022');
			}
		});

		btnSalvar = new JButton("Salvar");
		estilizarBotao(btnSalvar);
		btnSalvar.setBounds(55, 360, 350, 50);
		interno.add(btnSalvar);
		btnSalvar.setEnabled(false);
		btnSalvar.addActionListener(e -> salvarSenha());

		JButton btnCancelar = new JButton("Cancelar");
		estilizarBotao(btnCancelar);
		btnCancelar.setBounds(460, 360, 350, 50);
		interno.add(btnCancelar);
		btnCancelar.addActionListener(e -> dispose());
		
		buscarUsuario();

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
				super.paintComponent(g);

				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getBackground());
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
				g2.setColor(borda);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
				g2.dispose();
			}
		};
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	private void buscarUsuario() {
		try {
			handleUser(usuarioLogado);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao obter usuário logado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void handleUser(Usuario usuario) {
		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Usuário não encontrado na sessão!", "Erro", JOptionPane.ERROR_MESSAGE);
			txtNomeUsuario.setText("");
			btnSalvar.setEnabled(false);
			return;
		}
		
		String nomeExibicao = usuarioController.buscarNomeDoUsuario(usuario);
		txtNomeUsuario.setText(nomeExibicao);
		btnSalvar.setEnabled(true);
	}

	private void salvarSenha() {
		try {
			if (usuarioLogado == null) {
				JOptionPane.showMessageDialog(this, "Não há um usuário logado!", "Atenção", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			String senhaAtual = new String(txtSenhaAtual.getPassword());
			String senhaNova = new String(txtSenhaNova.getPassword());

			if (senhaAtual.isBlank() || senhaNova.isBlank()) {
				JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Atenção", JOptionPane.WARNING_MESSAGE);
				return;
			}
			Usuario usuario = usuarioController.autenticarLogin(usuarioLogado.getCpf(), senhaAtual);
			
			if (usuario == null || !usuario.isAtivo()) {
				JOptionPane.showMessageDialog(this, "Senha atual incorreta.", "Atenção", JOptionPane.WARNING_MESSAGE);
				return;
			}else {
				String hash = org.mindrot.jbcrypt.BCrypt.hashpw(senhaNova, org.mindrot.jbcrypt.BCrypt.gensalt());
				usuarioController.atualizarSenhaHash(usuarioLogado.getIdUsuario(), hash);
				JOptionPane.showMessageDialog(this, "Senha alterada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
				dispose();
			}
			dispose();
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
}