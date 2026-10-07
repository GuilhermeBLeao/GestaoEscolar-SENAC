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

import controller.ProfessorController;
import model.Endereco;
import model.Professor;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;

public class CadastroProfessor extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTextField txtNome;
	private JTextField txtRua;
	private JTextField txtNumero;
	private JTextField txtBairro;
	private JTextField txtCidade;
	private JTextField txtComplemento;
	private JTextField txtFormacao;
	private JFormattedTextField txtCpf;
	private JFormattedTextField txtRg;
	private JFormattedTextField txtNascimento;
	private JFormattedTextField txtTelefone;
	private JFormattedTextField txtCep;
	private JTabbedPane abas;
	private JComboBox<Sexo> cbSexo;
	private JComboBox<Estado> cbEstado;
	private final ProfessorController professorController = new ProfessorController();
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public CadastroProfessor() {
		setTitle("Cadastro de Professor");
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

		JLabel titulo = new JLabel("Cadastro de Professor");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
		titulo.setBounds(40, 45, 700, 55);
		interno.add(titulo);

		JLabel subtitulo = new JLabel("Registre dados pessoais, endereço, formação e disciplina do professor.");
		subtitulo.setForeground(textos);
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		subtitulo.setBounds(40, 105, 1100, 30);
		interno.add(subtitulo);

		JPanel formulario = criarPainelArredondado(corCampo, corBorda, 28);
		formulario.setLayout(null);
		formulario.setBounds(40, 160, larguraInterno - 80, alturaInterno - 220);
		interno.add(formulario);

		JLabel tituloForm = new JLabel("Informações do Professor");
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
		
		JButton btnLimpar = new JButton("Limpar Aba");
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
		setVisible(true);
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
		cbSexo = new JComboBox<>(Sexo.values());
		estilizarCombo(cbSexo);
		adicionarCampo(painel, "Sexo", cbSexo, 590, 130, 220);
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
		txtFormacao = criarCampoTexto();
		adicionarCampo(painel, "Formação", txtFormacao, 30, 35, 430);
		return painel;
	}

	private JPanel criarPainelAba() {
		JPanel painel = new JPanel(null);
		painel.setBackground(corCampo);
		painel.setBorder(new EmptyBorder(15, 15, 15, 15));
		return painel;
	}

	private void salvarCadastro() {
		if (txtNome.getText().trim().isEmpty() || txtCpf.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Preencha os campos principais: nome e CPF.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtCpf.getText()).length() != 11) {
			JOptionPane.showMessageDialog(this, "O CPF deve conter exatamente 11 números.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtRg.getText()).length() >= 12) {
			JOptionPane.showMessageDialog(this, "O RG não pode conter mais de 11 dígitos.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (somenteNumeros(txtNascimento.getText()).length() != 8) {
			JOptionPane.showMessageDialog(this, "Informe a data de nascimento no formato dd/mm/aaaa.", "Atenção",
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
			Professor professor = new Professor();
			professor.setNome(txtNome.getText());
			professor.setCpf(somenteNumeros(txtCpf.getText()));
			professor.setRg(somenteNumeros(txtRg.getText()));
			professor.setTelefone(somenteNumeros(txtTelefone.getText()));
			professor.setFormacao(txtFormacao.getText());
			professor.setSexo((Sexo) cbSexo.getSelectedItem());
			professor.setDataNascimento(LocalDate.parse(txtNascimento.getText().trim(), FORMATO_DATA));
			professor.setEndereco(criarEndereco());
			professorController.salvarProfessor(professor);
			JOptionPane.showMessageDialog(this, "Professor cadastrado no banco com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			limparTodosCampos();
		} catch (DateTimeParseException e) {
			JOptionPane.showMessageDialog(this, "Informe a data no formato dd/mm/aaaa.", "Atenção",
					JOptionPane.WARNING_MESSAGE);
		} catch (RuntimeException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Não foi possível cadastrar",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void limparCampos() {
		int abaSelecionada = abas.getSelectedIndex();
		if (abaSelecionada == 0) {
			txtNome.setText("");
			txtCpf.setValue(null);
			txtRg.setValue(null);
			txtNascimento.setValue(null);
			txtTelefone.setValue(null);
			cbSexo.setSelectedIndex(0);
		}

		if (abaSelecionada == 1) {
			txtCep.setValue(null);
			txtRua.setText("");
			txtNumero.setText("");
			txtBairro.setText("");
			txtCidade.setText("");
			cbEstado.setSelectedIndex(0);
			txtComplemento.setText("");
		}
		if (abaSelecionada == 2) {
			txtFormacao.setText("");
		}
	}

	private void limparTodosCampos() {
		txtNome.setText("");
		txtCpf.setValue(null);
		txtRg.setValue(null);
		txtNascimento.setValue(null);
		txtTelefone.setValue(null);
		txtCep.setValue(null);
		txtRua.setText("");
		txtNumero.setText("");
		txtBairro.setText("");
		txtCidade.setText("");
		txtComplemento.setText("");
		cbEstado.setSelectedIndex(0);
		txtFormacao.setText("");
		cbSexo.setSelectedIndex(0);
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

	private void adicionarCampo(JPanel painel, String label, JComponent campo, int x, int y, int largura) {
		JLabel lbl = new JLabel(label);
		lbl.setForeground(corLabel);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lbl.setBounds(x, y, largura, 24);
		painel.add(lbl);
		campo.setBounds(x, y + 30, largura, 40);
		painel.add(campo);
	}

	private String somenteNumeros(String texto) {
		return texto.replaceAll("\\D", "");
	}

	private JTextField criarCampoTexto() {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		campo.setForeground(textos);
		campo.setBackground(corCampo);
		campo.setCaretColor(textos);
		campo.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));
		return campo;
	}

	private JFormattedTextField criarCampoFormatado(String mascara) {
		try {
			MaskFormatter formatter = new MaskFormatter(mascara);
			formatter.setPlaceholderCharacter(' ');
			formatter.setAllowsInvalid(false);
			formatter.setOverwriteMode(true);

			JFormattedTextField campo = new JFormattedTextField(formatter);
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

	private void estilizarCombo(JComboBox<?> combo) {
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		combo.setForeground(textos);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
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