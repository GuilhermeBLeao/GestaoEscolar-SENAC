package view;

import model.Aluno;
import model.Usuario;
import variaveisEnum.TipoUsuario;
import controller.AlunoController;
import controller.UsuarioController;

import java.awt.Font;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import javax.swing.JFormattedTextField;

public class AlterarCpfRg extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel painelAlteracao;
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;

	private JFormattedTextField txtConsultaUsuario;
	private JTextField txtNomeUsuario;
	private JFormattedTextField txtCpf;
	private JTextField txtRg;
	private JFormattedTextField txtNascimento;
	private JTextArea textSaude;

	private final UsuarioController usuarioController;
	private final AlunoController alunoController;
	private JButton btnSalvar;
	private JButton btnLimpar;
	private JButton btnCancelar;
	private Usuario usuarioBusca;

	public AlterarCpfRg() {
		setTitle("Atualizar Dados do Usuário");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 900, 800);
		setResizable(false);
		setLocationRelativeTo(null);

		this.usuarioController = new UsuarioController();
		this.alunoController = new AlunoController();

		painelAlteracao = new JPanel();
		painelAlteracao.setBorder(null);
		painelAlteracao.setBackground(corExterna);
		setContentPane(painelAlteracao);
		painelAlteracao.setLayout(new BorderLayout());

		JPanel externo = new JPanel();
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(15, 15, 15, 15));
		externo.setLayout(new BorderLayout());
		painelAlteracao.add(externo, BorderLayout.CENTER);

		JPanel interno = criarPainelArredondado(corInterna, corBorda, 30);
		interno.setLayout(null);
		externo.add(interno, BorderLayout.CENTER);

		JLabel titulo = new JLabel("Alterar Dados do Usuário");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBounds(125, 10, 600, 45);
		interno.add(titulo);

		JLabel lblConsulta = new JLabel("Digite o CPF do Usuário");
		lblConsulta.setForeground(corLabel);
		lblConsulta.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblConsulta.setBounds(55, 65, 350, 25);
		interno.add(lblConsulta);

		txtConsultaUsuario = criarCampoComMascara("###.###.###-##");
		txtConsultaUsuario.setBounds(55, 95, 755, 40);
		interno.add(txtConsultaUsuario);

		JButton btnBuscar = new JButton("Buscar Usuário");
		estilizarBotao(btnBuscar);
		btnBuscar.setBounds(85, 145, 695, 50);
		interno.add(btnBuscar);
		btnBuscar.addActionListener(e -> buscarUsuario());

		JLabel lblNomeUsuario = new JLabel("Nome Usuário");
		lblNomeUsuario.setForeground(corLabel);
		lblNomeUsuario.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblNomeUsuario.setBounds(55, 210, 350, 25);
		interno.add(lblNomeUsuario);

		txtNomeUsuario = new JTextField();
		txtNomeUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtNomeUsuario.setForeground(textos);
		txtNomeUsuario.setCaretColor(textos);
		txtNomeUsuario.setBackground(corCampo);
		txtNomeUsuario.setBorder(new LineBorder(corBorda, 1, true));
		txtNomeUsuario.setBounds(55, 240, 378, 40);
		interno.add(txtNomeUsuario);

		JLabel lblCpf = new JLabel("CPF (Imutável)");
		lblCpf.setForeground(corLabel);
		lblCpf.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblCpf.setBounds(454, 210, 350, 25);
		interno.add(lblCpf);

		txtCpf = criarCampoComMascara("###.###.###-##");
		txtCpf.setBounds(454, 240, 378, 40);
		txtCpf.setEditable(false);
		interno.add(txtCpf);

		JLabel lblRg = new JLabel("RG (Apenas Alunos)");
		lblRg.setForeground(corLabel);
		lblRg.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblRg.setBounds(55, 305, 350, 25);
		interno.add(lblRg);

		txtRg = new JTextField();
		txtRg.setForeground(Color.WHITE);
		txtRg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtRg.setCaretColor(Color.WHITE);
		txtRg.setBorder(new LineBorder(corBorda, 1, true));
		txtRg.setBackground(corCampo);
		txtRg.setBounds(55, 335, 378, 40);
		interno.add(txtRg);

		JLabel lblNascimento = new JLabel("Data de Nascimento (Apenas Alunos)");
		lblNascimento.setForeground(corLabel);
		lblNascimento.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblNascimento.setBounds(454, 305, 350, 25);
		interno.add(lblNascimento);

		txtNascimento = criarCampoComMascara("##/##/####");
		txtNascimento.setBounds(454, 335, 378, 40);
		interno.add(txtNascimento);

		JLabel lblSaude = new JLabel("Observações de Saúde (Apenas Alunos)");
		lblSaude.setForeground(corLabel);
		lblSaude.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblSaude.setBounds(55, 395, 400, 30);
		interno.add(lblSaude);

		textSaude = new JTextArea();
		textSaude.setBounds(55, 434, 780, 190);
		textSaude.setForeground(Color.WHITE);
		textSaude.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		textSaude.setCaretColor(Color.WHITE);
		textSaude.setBorder(new LineBorder(corBorda, 1, true));
		textSaude.setBackground(corCampo);
		textSaude.setLineWrap(true);
		textSaude.setWrapStyleWord(true);
		interno.add(textSaude);

		btnSalvar = new JButton("Salvar");
		estilizarBotao(btnSalvar);
		btnSalvar.setBounds(55, 643, 240, 50);
		interno.add(btnSalvar);
		btnSalvar.setEnabled(false);
		btnSalvar.addActionListener(e -> salvarCadastro());

		btnLimpar = new JButton("Limpar");
		estilizarBotao(btnLimpar);
		btnLimpar.setBounds(325, 643, 240, 50);
		interno.add(btnLimpar);
		btnLimpar.addActionListener(e -> limparFormularioCompleto());

		btnCancelar = new JButton("Cancelar");
		estilizarBotao(btnCancelar);
		btnCancelar.setBounds(595, 643, 240, 50);
		interno.add(btnCancelar);
		btnCancelar.addActionListener(e -> dispose());
	}

	private JFormattedTextField criarCampoComMascara(String mascara) {
		JFormattedTextField campo = new JFormattedTextField();
		try {
			MaskFormatter mask = new MaskFormatter(mascara);
			mask.setPlaceholderCharacter('_');
			mask.install(campo);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		campo.setForeground(textos);
		campo.setCaretColor(textos);
		campo.setBackground(corCampo);
		campo.setBorder(new LineBorder(corBorda, 1, true));
		return campo;
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

	private void limpaCampos() {
		txtNomeUsuario.setText("");
		txtCpf.setText("");
		txtRg.setText("");
		txtNascimento.setText("");
		textSaude.setText("");
	}

	private void limparFormularioCompleto() {
		txtConsultaUsuario.setText("");
		limpaCampos();
		usuarioBusca = null;
		btnSalvar.setEnabled(false);
		txtRg.setEnabled(true);
		txtNascimento.setEnabled(true);
		textSaude.setEnabled(true);
	}

	private void buscarUsuario() {
		try {
			String cpf = txtConsultaUsuario.getText().replaceAll("\\D", "");

			if (cpf.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Por favor, digite o CPF do usuário.", "Atenção",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			usuarioBusca = usuarioController.buscarUsuarioPorCpf(cpf);

			if (usuarioBusca == null || !usuarioBusca.isAtivo()) {
				JOptionPane.showMessageDialog(this, "Usuário não encontrado ou inativo. Por favor verifique o CPF digitado.", 
						"Erro", JOptionPane.ERROR_MESSAGE);
				limpaCampos();
				btnSalvar.setEnabled(false);
				return;
			}

			String nomeExibicao = usuarioController.buscarNomeDoUsuario(usuarioBusca);
			txtNomeUsuario.setText(nomeExibicao);
			txtCpf.setText(usuarioBusca.getCpf());

			boolean isAluno = (usuarioBusca.getTipoUsuario() == TipoUsuario.ALUNO || usuarioBusca.getAlunoId() > 0);

			if (isAluno && usuarioBusca.getAlunoId() > 0) {
				Aluno aluno = alunoController.buscarAlunoPorId(usuarioBusca.getAlunoId());

				if (aluno != null) {
					txtRg.setText(aluno.getRg() != null ? aluno.getRg() : "");

					if (aluno.getDataNascimento() != null) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						txtNascimento.setText(aluno.getDataNascimento().format(formatter));
					} else {
						txtNascimento.setText("");
					}

					textSaude.setText(aluno.getObsSaude() != null ? aluno.getObsSaude() : "");
				}

				txtRg.setEnabled(true);
				txtNascimento.setEnabled(true);
				textSaude.setEnabled(true);
			} else {
				txtRg.setText("");
				txtNascimento.setText("");
				textSaude.setText("");

				txtRg.setEnabled(false);
				txtNascimento.setEnabled(false);
				textSaude.setEnabled(false);
			}
			btnSalvar.setEnabled(true);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao obter usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
			limpaCampos();
			btnSalvar.setEnabled(false);
		}
	}

	private void salvarCadastro() {
		if (usuarioBusca == null) {
			JOptionPane.showMessageDialog(this, "Nenhum usuário selecionado para atualização.", "Atenção", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			String nome = txtNomeUsuario.getText();
			String rg = txtRg.isEnabled() ? txtRg.getText() : null;
			String dataNascimento = txtNascimento.isEnabled() ? txtNascimento.getText() : null;
			String obsSaude = textSaude.isEnabled() ? textSaude.getText() : null;
			usuarioController.atualizarUsuarioEPerfil(usuarioBusca, nome, rg, dataNascimento, obsSaude);

			JOptionPane.showMessageDialog(this, "Cadastro atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
			dispose();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao atualizar cadastro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
}