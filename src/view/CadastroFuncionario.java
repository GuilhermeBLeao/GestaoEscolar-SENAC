package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

import controller.FuncionarioController;
import model.Endereco;
import model.Funcionario;
import variaveisEnum.Estado;
import variaveisEnum.Perfil;
import variaveisEnum.Sexo;

public class CadastroFuncionario extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTextField txtNome;
	private JTextField txtEmail;
	private JTextField txtRua;
	private JTextField txtNumero;
	private JTextField txtBairro;
	private JTextField txtCidade;
	private JTextField txtComplemento;
	private JTextField txtCargo;
	private JFormattedTextField txtCpf;
	private JFormattedTextField txtRg;
	private JFormattedTextField txtNascimento;
	private JFormattedTextField txtContratacao;
	private JFormattedTextField txtTelefone;
	private JFormattedTextField txtCep;
	private JComboBox<String> cbSetor;
	private JComboBox<Sexo> cbSexo;
	private JComboBox<Perfil> cbPerfil;
	private JComboBox<Estado> cbEstado;
	private final FuncionarioController funcionarioController = new FuncionarioController();
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private JTabbedPane abas;

	public CadastroFuncionario() {
		setTitle("Cadastro de Funcionário");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int margem = 30;
		int larguraInterno = areaUtil.width - (margem * 2) + 20;
		int alturaInterno = areaUtil.height - (margem * 2);
		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);

		JPanel interno = new JPanel(null);
		interno.setBounds(20, 20, larguraInterno, alturaInterno);
		interno.setBackground(corInterna);
		externo.add(interno);

		JLabel titulo = new JLabel("Cadastro de Funcionário");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
		titulo.setBounds(40, 45, 700, 55);
		interno.add(titulo);

		JLabel subtitulo = new JLabel("Registre dados pessoais, endereço e função do funcionário.");
		subtitulo.setForeground(textos);
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		subtitulo.setBounds(40, 105, 1100, 30);
		interno.add(subtitulo);

		JPanel formulario = criarPainelArredondado(corCampo, corBorda, 28);
		formulario.setLayout(null);
		formulario.setBounds(40, 160, larguraInterno - 80, alturaInterno - 220);
		interno.add(formulario);

		JLabel tituloForm = new JLabel("Informações do Funcionário");
		tituloForm.setForeground(textos);
		tituloForm.setFont(new Font("Segoe UI", Font.BOLD, 28));
		tituloForm.setBounds(35, 25, 500, 35);
		formulario.add(tituloForm);

		JPanel linha = new JPanel();
		linha.setBackground(corLabel);
		linha.setBounds(35, 70, 340, 3);
		formulario.add(linha);
		abas = new JTabbedPane();
		abas.setBounds(35, 95, larguraInterno - 150, alturaInterno - 430);
		abas.setFont(new Font("Segoe UI", Font.BOLD, 15));
		abas.setForeground(textos);
		abas.setBackground(corInterna);
		abas.addTab("Dados pessoais", criarAbaDadosPessoais());
		abas.addTab("Endereço", criarAbaEndereco());
		abas.addTab("Dados profissionais", criarAbaProfissional());
		formulario.add(abas);

		JButton btnSalvar = new JButton("Salvar cadastro");
		btnSalvar.setBounds(35, formulario.getHeight() - 70, 190, 42);
		estilizarBotaoAcao(btnSalvar);
		formulario.add(btnSalvar);

		JButton btnLimpar = new JButton("Limpar aba");
		btnLimpar.setBounds(245, formulario.getHeight() - 70, 120, 42);
		estilizarBotaoSecundario(btnLimpar);
		formulario.add(btnLimpar);
		
		JButton btnVoltar = new JButton("Voltar");
		btnVoltar.setBounds(385, formulario.getHeight() - 70, 120, 42);
		estilizarBotaoSecundario(btnVoltar);
		formulario.add(btnVoltar);
		btnSalvar.addActionListener(e -> salvarCadastro());
		btnLimpar.addActionListener(e -> limparCampos());
		btnVoltar.addActionListener(e -> dispose());
	}

	private JPanel criarAbaDadosPessoais() {
		JPanel painel = criarPainelAba();
		txtNome = criarCampoTexto();
		adicionarCampo(painel, "Nome completo", txtNome, 30, 35, 430);
		txtCpf = criarCampoFormatado("###.###.###-##");
		adicionarCampo(painel, "CPF", txtCpf, 490, 35, 220);
		txtRg = criarCampoFormatado("###########");
		adicionarCampo(painel, "RG", txtRg, 740, 35, 220);
		txtNascimento = criarCampoFormatado("##/##/####");
		adicionarCampo(painel, "Data de nascimento", txtNascimento, 30, 130, 250);
		txtTelefone = criarCampoFormatado("(##) #####-####");
		adicionarCampo(painel, "Telefone", txtTelefone, 310, 130, 250);
		txtEmail = criarCampoTexto();
		adicionarCampo(painel, "E-mail", txtEmail, 590, 130, 370);
		cbSexo = new JComboBox<>(Sexo.values());
		estilizarCombo(cbSexo);
		adicionarCampo(painel, "Sexo", cbSexo, 740, 225, 220);
		return painel;
	}

	private JPanel criarAbaEndereco() {
		JPanel painel = criarPainelAba();
		txtCep = criarCampoFormatado("#####-###");
		adicionarCampo(painel, "CEP", txtCep, 30, 35, 200);
		txtRua = criarCampoTexto();
		adicionarCampo(painel, "Rua", txtRua, 260, 35, 430);
		txtNumero = criarCampoTexto();
		adicionarCampo(painel, "Número", txtNumero, 720, 35, 140);
		txtBairro = criarCampoTexto();
		adicionarCampo(painel, "Bairro", txtBairro, 30, 130, 300);
		txtCidade = criarCampoTexto();
		adicionarCampo(painel, "Cidade", txtCidade, 360, 130, 300);
		cbEstado = new JComboBox<>(Estado.values());
		estilizarCombo(cbEstado);
		adicionarCampo(painel, "Estado", cbEstado, 690, 130, 170);
		txtComplemento = criarCampoTexto();
		adicionarCampo(painel, "Complemento", txtComplemento, 30, 225, 830);
		return painel;
	}

	private JPanel criarAbaProfissional() {
		JPanel painel = criarPainelAba();
		txtCargo = criarCampoTexto();
		adicionarCampo(painel, "Cargo", txtCargo, 30, 35, 300);
		cbSetor = criarCombo(new String[] { "Secretaria", "Financeiro", "Coordenação", "Biblioteca", "Recepção", "Administrativo" });
		adicionarCampo(painel, "Setor", cbSetor, 360, 35, 250);
		txtContratacao = criarCampoFormatado("##/##/####");
		adicionarCampo(painel, "Data de contratação", txtContratacao, 640, 35, 220);
		cbPerfil = new JComboBox<>(Perfil.values());
		estilizarCombo(cbPerfil);
		adicionarCampo(painel, "Perfil de acesso", cbPerfil, 30, 130, 250);
		return painel;
	}

	private void salvarCadastro() {
		if (txtNome.getText().trim().isEmpty() || somenteNumeros(txtCpf.getText()).length() != 11
				|| txtEmail.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Preencha corretamente os campos principais: nome, CPF e e-mail.",
					"Atenção", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtRg.getText()).length() >= 12) {
			JOptionPane.showMessageDialog(this, "O RG deve conter no máximo 11 dígitos.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtNascimento.getText()).length() != 8) {
			JOptionPane.showMessageDialog(this, "Informe a data de nascimento no formato dd/mm/aaaa.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtContratacao.getText()).length() != 8) {
			JOptionPane.showMessageDialog(this, "Informe a data de contratação no formato dd/mm/aaaa.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtTelefone.getText()).length() != 11) {
			JOptionPane.showMessageDialog(this, "Informe o telefone no formato (00) 00000-0000.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtCep.getText()).length() != 8) {
			JOptionPane.showMessageDialog(this, "Informe o CEP no formato 00000-000.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			Funcionario funcionario = new Funcionario();
			funcionario.setNome(txtNome.getText());
			funcionario.setCpf(somenteNumeros(txtCpf.getText()));
			funcionario.setRg(somenteNumeros(txtRg.getText()));
			funcionario.setTelefone(somenteNumeros(txtTelefone.getText()));
			funcionario.setEmail(txtEmail.getText().trim());
			funcionario.setCargo(txtCargo.getText());
			funcionario.setSetor(String.valueOf(cbSetor.getSelectedItem()));
			funcionario.setSexo((Sexo) cbSexo.getSelectedItem());
			funcionario.setPerfil((Perfil) cbPerfil.getSelectedItem());
			funcionario.setDataNascimento(parseData(txtNascimento));
			funcionario.setDataContratacao(parseData(txtContratacao));
			funcionario.setEndereco(criarEndereco());
			funcionarioController.salvarFuncionario(funcionario);
			JOptionPane.showMessageDialog(this, "Funcionário cadastrado no banco com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (DateTimeParseException e) {
			JOptionPane.showMessageDialog(this, "Informe as datas no formato dd/mm/aaaa.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		} catch (RuntimeException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Não foi possível cadastrar",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		limparTodosCampos();
	}

	private void limparCampos() {
		int abaSelecionada = abas.getSelectedIndex();

		if (abaSelecionada == 0) {
			txtNome.setText("");
			txtCpf.setValue(null);
			txtRg.setValue(null);
			txtNascimento.setValue(null);
			txtTelefone.setValue(null);
			txtEmail.setText("");
		}
		if (abaSelecionada == 1) {
			txtCep.setValue(null);
			txtRua.setText("");
			txtNumero.setText("");
			txtBairro.setText("");
			txtCidade.setText("");
			txtComplemento.setText("");
			cbEstado.setSelectedIndex(0);
		}
		if (abaSelecionada == 2) {
			txtCargo.setText("");
			txtContratacao.setValue(null);
			cbSetor.setSelectedIndex(0);
			cbPerfil.setSelectedIndex(0);
		}
	}

	private void limparTodosCampos() {
		txtNome.setText("");
		txtCpf.setValue(null);
		txtRg.setValue(null);
		txtNascimento.setValue(null);
		txtTelefone.setValue(null);
		txtEmail.setText("");
		txtCep.setValue(null);
		txtRua.setText("");
		txtNumero.setText("");
		txtBairro.setText("");
		txtCidade.setText("");
		txtComplemento.setText("");
		cbEstado.setSelectedIndex(0);
		txtContratacao.setValue(null);
		txtCargo.setText("");
		cbSetor.setSelectedIndex(0);
		cbSexo.setSelectedIndex(0);
		cbPerfil.setSelectedIndex(0);
	}

	private LocalDate parseData(JFormattedTextField campo) {
		return LocalDate.parse(campo.getText().trim(), FORMATO_DATA);
	}

	private Endereco criarEndereco() {
		Endereco endereco = new Endereco();
		endereco.setCep(somenteNumeros(txtCep.getText()));
		endereco.setRua(txtRua.getText());
		endereco.setNumero(txtNumero.getText());
		endereco.setComplemento(txtComplemento.getText());
		endereco.setBairro(txtBairro.getText());
		endereco.setCidade(txtCidade.getText());
		endereco.setEstado((Estado) cbEstado.getSelectedItem());
		return endereco;
	}

	private String somenteNumeros(String texto) {
		return texto.replaceAll("\\D", "");
	}

	private JFormattedTextField criarCampoFormatado(String mascara) {
		try {
			MaskFormatter formatador = new MaskFormatter(mascara);
			formatador.setPlaceholderCharacter('_');
			formatador.setAllowsInvalid(false);
			formatador.setOverwriteMode(true);

			JFormattedTextField campo = new JFormattedTextField(formatador);
			campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			campo.setForeground(textos);
			campo.setBackground(corCampo);
			campo.setCaretColor(textos);
			campo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(corBorda, 1, true),
					new EmptyBorder(0, 12, 0, 12)));
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
		} catch (ParseException e) {
			throw new RuntimeException("Erro ao criar máscara: " + mascara, e);
		}
	}

	private JTextField criarCampoTexto() {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		campo.setForeground(textos);
		campo.setBackground(corCampo);
		campo.setCaretColor(textos);
		campo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));
		return campo;
	}

	private JComboBox<String> criarCombo(String[] itens) {
		JComboBox<String> combo = new JComboBox<>(itens);
		estilizarCombo(combo);
		return combo;
	}

	private void estilizarCombo(JComboBox<?> combo) {
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		combo.setForeground(textos);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
	}

	private JPanel criarPainelAba() {
		JPanel painel = new JPanel(null);
		painel.setBackground(corCampo);
		painel.setBorder(new EmptyBorder(15, 15, 15, 15));
		return painel;
	}

	private void adicionarCampo(JPanel painel, String label, JComponent campo, int x, int y, int largura) {
		JLabel lbl = new JLabel(label);
		lbl.setForeground(corLabel);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lbl.setBounds(x, y, largura, 24);
		painel.add(lbl);
		campo.setBounds(x, y + 30, largura, 40);
		painel.add(campo);
	}

	private void estilizarBotaoAcao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 15));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void estilizarBotaoSecundario(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
}