package view.Secretaria;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

import controller.UsuarioController;
import model.Usuario;

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

	public AlterarSenhaUsuario() {
		usuarioController = new UsuarioController();
		initialize();
	}

	private void initialize() {

		setTitle("Alterar Senha do Usuário");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(540, 360);
		setLocationRelativeTo(null);
		setResizable(false);

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		setContentPane(contentPane);

		contentPane.add(criarCabecalho(), BorderLayout.NORTH);
		contentPane.add(criarFormulario(), BorderLayout.CENTER);
		contentPane.add(criarBotoes(), BorderLayout.SOUTH);

		adicionarEventos();
		atualizarEstadoInicial();
	}

	private JPanel criarCabecalho() {

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		JLabel titulo = new JLabel("ALTERAÇÃO DE SENHA");
		titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBorder(new EmptyBorder(5, 5, 15, 5));

		panel.add(titulo, BorderLayout.CENTER);
		return panel;
	}

	private JPanel criarFormulario() {

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(new Color(200, 200, 200)));

		GridBagConstraints g = new GridBagConstraints();
		g.insets = new Insets(8, 8, 8, 8);
		g.fill = GridBagConstraints.HORIZONTAL;

		try {
			MaskFormatter mask = new MaskFormatter("###.###.###-##");
			mask.setPlaceholderCharacter('_');
			txtCpf = new JFormattedTextField(mask);
		} catch (ParseException e) {
			txtCpf = new JFormattedTextField();
		}

		txtUsuario = new JTextField();
		txtUsuario.setEditable(false);

		txtNovaSenha = new JPasswordField();
		txtConfirmarSenha = new JPasswordField();

		btnBuscar = new JButton("Buscar");

		int y = 0;

		adicionarCampo(panel, g, "CPF:", txtCpf, y++);
		adicionarBotaoLinha(panel, g, btnBuscar, y++);
		adicionarCampo(panel, g, "Usuário:", txtUsuario, y++);
		adicionarCampo(panel, g, "Nova senha:", txtNovaSenha, y++);
		adicionarCampo(panel, g, "Confirmar senha:", txtConfirmarSenha, y++);

		return panel;
	}

	private JPanel criarBotoes() {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.setBackground(Color.WHITE);

		btnCancelar = new JButton("Cancelar");
		btnSalvar = new JButton("Salvar");

		estilizarBotao(btnCancelar, new Color(220, 220, 220));
		estilizarBotao(btnSalvar, new Color(180, 220, 180));

		panel.add(btnCancelar);
		panel.add(btnSalvar);

		return panel;
	}

	private void adicionarCampo(JPanel panel, GridBagConstraints g, String label, JComponent field, int y) {

		g.gridx = 0;
		g.gridy = y;
		g.weightx = 0;
		panel.add(new JLabel(label), g);

		g.gridx = 1;
		g.weightx = 1;
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
		btn.setFocusPainted(false);
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

			if (usuarioSelecionado == null) {
				JOptionPane.showMessageDialog(this, "Usuário não encontrado!", "Erro", JOptionPane.ERROR_MESSAGE);

				txtUsuario.setText("");
				btnSalvar.setEnabled(false);
				return;
			}

			txtUsuario.setText(usuarioSelecionado.getCpf());
			btnSalvar.setEnabled(true);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
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