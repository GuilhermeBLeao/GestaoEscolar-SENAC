package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import controller.DisciplinaController;
import controller.ProfessorController;
import controller.ProfessorDisciplinaController;
import dao.TrimestreDAO;
import database.ConnectionFactory;
import model.Disciplina;
import model.Professor;
import model.ProfessorDisciplina;
import model.Trimestre;

public class VinculoProfessorDisciplina extends JFrame {
	private static final long serialVersionUID = 1L;

	// Controllers
	private final ProfessorDisciplinaController profDiscController = new ProfessorDisciplinaController();
	private final ProfessorController professorController = new ProfessorController();
	private final DisciplinaController disciplinaController = new DisciplinaController();

	// Objeto selecionado
	private Professor professorSelecionado;

	// Cores da Identidade Visual
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;

	// Componentes para Busca e Dados do Professor
	private JTextField txtPesquisa;
	private JTextField txtId;
	private JTextField txtNome;
	private JTextField txtCpf;
	private JTextField txtRg;
	private JTextField txtDataNascimento;
	private JTextField txtSexo;
	private JTextField txtFormacao;
	private JTextField txtTelefone;

	// Componentes para Seleção de Disciplina e Trimestre (Tipados com os objetos do
	// Modelo)
	private JComboBox<Object> cbDisciplina;
	private JComboBox<Object> cbTrimestre;

	// Botões
	private JButton btnSalvar;
	private JButton btnCancelar;
	private JButton btnLimpar;

	public VinculoProfessorDisciplina() {
		setTitle("Gestão de Vínculos - Professor X Disciplina");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(1280, 720);
		setResizable(false);
		setLocationRelativeTo(null);

		JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		mainPanel.setBackground(corExterna);

		JLabel lblTitulo = new JLabel("VÍNCULO MANUAL: PROFESSOR / DISCIPLINA");
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
		lblTitulo.setForeground(textos);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(lblTitulo, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		centerPanel.setOpaque(false);
		centerPanel.setBackground(corInterna);
		centerPanel.add(criarPainelProfessor());
		centerPanel.add(criarPainelDisciplina());
		mainPanel.add(centerPanel, BorderLayout.CENTER);

		mainPanel.add(criarPainelBotoes(), BorderLayout.SOUTH);

		add(mainPanel);

		// Carrega os dados nas ComboBoxes dinamicamente a partir do banco de dados
		carregarDisciplinasDoBanco();
		carregarTrimestresDoBanco();
	}

	private JPanel criarPainelProfessor() {
		JPanel panelProfessor = new JPanel();
		panelProfessor.setLayout(new BoxLayout(panelProfessor, BoxLayout.Y_AXIS));
		panelProfessor.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(corBorda), " DADOS DO PROFESSOR ",
						TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), textos),
				new EmptyBorder(10, 10, 10, 10)));
		panelProfessor.setBackground(corInterna);

		// Pesquisa realizada pelo NOME do professor ao pressionar ENTER
		txtPesquisa = new JTextField("");
		txtPesquisa.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					buscarProfessorPorNome();
				}
			}
		});

		panelProfessor.add(criarBlocoCampo("PESQUISAR PROFESSOR POR NOME (Pressione ENTER):", txtPesquisa));
		panelProfessor.add(Box.createVerticalStrut(8));

		txtId = criarCampoReadOnly("");
		txtNome = criarCampoReadOnly("");
		panelProfessor.add(criarLinhaDupla("ID:", txtId, "NOME COMPLETO:", txtNome));
		panelProfessor.add(Box.createVerticalStrut(8));

		txtCpf = criarCampoReadOnly("");
		txtRg = criarCampoReadOnly("");
		panelProfessor.add(criarLinhaDupla("CPF:", txtCpf, "RG:", txtRg));
		panelProfessor.add(Box.createVerticalStrut(8));

		txtDataNascimento = criarCampoReadOnly("");
		txtSexo = criarCampoReadOnly("");
		panelProfessor.add(criarLinhaDupla("DATA DE NASCIMENTO:", txtDataNascimento, "SEXO:", txtSexo));
		panelProfessor.add(Box.createVerticalStrut(8));

		txtFormacao = criarCampoReadOnly("");
		panelProfessor.add(criarBlocoCampo("FORMAÇÃO ACADÊMICA:", txtFormacao));
		panelProfessor.add(Box.createVerticalStrut(8));

		txtTelefone = criarCampoReadOnly("");
		panelProfessor.add(criarBlocoCampo("TELEFONE:", txtTelefone));

		return panelProfessor;
	}

	private JPanel criarPainelDisciplina() {
		JPanel painelDisciplina = new JPanel();
		painelDisciplina.setLayout(new BoxLayout(painelDisciplina, BoxLayout.Y_AXIS));
		painelDisciplina.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(corBorda), " VÍNCULO DA DISCIPLINA ",
						TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), textos),
				new EmptyBorder(10, 10, 10, 10)));
		painelDisciplina.setBackground(corInterna);

		cbDisciplina = new JComboBox<>();
		cbDisciplina.addItem("Selecione");
		painelDisciplina.add(criarBlocoCampo("SELECIONAR DISCIPLINA:", cbDisciplina));
		painelDisciplina.add(Box.createVerticalStrut(10));

		cbTrimestre = new JComboBox<>();
		cbTrimestre.addItem("Selecione");
		painelDisciplina.add(criarBlocoCampo("SELECIONAR TRIMESTRE:", cbTrimestre));

		painelDisciplina.add(Box.createVerticalGlue());

		return painelDisciplina;
	}

	private JPanel criarPainelBotoes() {
		JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		panelBotoes.setOpaque(false);

		btnSalvar = new JButton("SALVAR VÍNCULO");
		estilarBotao(btnSalvar, 180);
		btnSalvar.addActionListener(e -> salvarVinculo());

		btnLimpar = new JButton("LIMPAR CAMPOS");
		estilarBotao(btnLimpar, 140);
		btnLimpar.addActionListener(e -> limparCampos());

		btnCancelar = new JButton("CANCELAR");
		estilarBotao(btnCancelar, 140);
		btnCancelar.addActionListener(e -> dispose());

		panelBotoes.add(btnSalvar);
		panelBotoes.add(btnLimpar);
		panelBotoes.add(btnCancelar);

		return panelBotoes;

	}

	private void estilarBotao(JButton btn, int largura) {
		btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btn.setBackground(corCampo);
		btn.setForeground(textos);
		btn.setBorder(new LineBorder(corBorda, 1, true));
		btn.setFocusPainted(false);
		btn.setPreferredSize(new Dimension(largura, 40));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	// --- INTEGRADO COM OS CONTROLLERS E DAOS ---

	private void buscarProfessorPorNome() {
		String nomeDigitado = txtPesquisa.getText().trim();
		if (nomeDigitado.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Digite o nome do professor para realizar a busca.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			// Utiliza o método buscarProfessorPorNome do seu ProfessorController
			professorSelecionado = professorController.buscarProfessorPorNome(nomeDigitado);

			if (professorSelecionado != null && professorSelecionado.isAtivo()) {
				txtId.setText(String.valueOf(professorSelecionado.getIdProfessor()));
				txtNome.setText(professorSelecionado.getNome());
				txtCpf.setText(professorSelecionado.getCpf());
				txtRg.setText(professorSelecionado.getRg());

				if (professorSelecionado.getDataNascimento() != null) {
					txtDataNascimento.setText(
							professorSelecionado.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
				} else {
					txtDataNascimento.setText("");
				}

				txtSexo.setText(
						professorSelecionado.getSexo() != null ? professorSelecionado.getSexo().toString() : "");
				txtFormacao.setText(professorSelecionado.getFormacao());
				txtTelefone.setText(professorSelecionado.getTelefone());
			}
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.INFORMATION_MESSAGE);
			limparDadosProfessor();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao buscar professor por nome: " + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
			limparDadosProfessor();
		}
	}

	private void carregarDisciplinasDoBanco() {
		try {
			// Busca disciplinas ativas no banco através do DisciplinaController
			List<Disciplina> listaDisciplinas = disciplinaController.listarDisciplinasAtivas();

			cbDisciplina.removeAllItems();
			cbDisciplina.addItem("Selecione");

			for (Disciplina d : listaDisciplinas) {
				cbDisciplina.addItem(d);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao carregar lista de disciplinas: " + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarTrimestresDoBanco() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			// Busca os trimestres no banco através do TrimestreDAO
			TrimestreDAO trimestreDAO = new TrimestreDAO(conn);
			List<Trimestre> listaTrimestres = trimestreDAO.listarTodos();

			cbTrimestre.removeAllItems();
			cbTrimestre.addItem("Selecione");

			for (Trimestre t : listaTrimestres) {
				cbTrimestre.addItem(t);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao carregar lista de trimestres: " + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void salvarVinculo() {
		if (professorSelecionado == null) {
			JOptionPane.showMessageDialog(this, "Busque e selecione um professor antes de salvar.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (cbDisciplina.getSelectedIndex() <= 0 || !(cbDisciplina.getSelectedItem() instanceof Disciplina)) {
			JOptionPane.showMessageDialog(this, "Selecione uma disciplina válida.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (cbTrimestre.getSelectedIndex() <= 0 || !(cbTrimestre.getSelectedItem() instanceof Trimestre)) {
			JOptionPane.showMessageDialog(this, "Selecione um trimestre válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Disciplina disciplinaSelecionada = (Disciplina) cbDisciplina.getSelectedItem();
		Trimestre trimestreSelecionado = (Trimestre) cbTrimestre.getSelectedItem();

		try {
			ProfessorDisciplina vinculo = new ProfessorDisciplina();
			vinculo.setIdProfessor(professorSelecionado.getIdProfessor());
			vinculo.setIdDisciplina(disciplinaSelecionada.getIdDisciplina());
			vinculo.setDataVinculacao(trimestreSelecionado.getDataInicio());
			vinculo.setAtivo(true);

			profDiscController.vincularProfessorDisciplina(vinculo);

			JOptionPane.showMessageDialog(this,
					"Vínculo realizado com sucesso para o " + trimestreSelecionado.getNumero() + "º Trimestre!",
					"Sucesso", JOptionPane.INFORMATION_MESSAGE);
			limparCampos();

		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso de Validação", JOptionPane.WARNING_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao realizar o vínculo: " + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void limparCampos() {
		txtPesquisa.setText("");
		limparDadosProfessor();
		if (cbDisciplina.getItemCount() > 0)
			cbDisciplina.setSelectedIndex(0);
		if (cbTrimestre.getItemCount() > 0)
			cbTrimestre.setSelectedIndex(0);
	}

	private void limparDadosProfessor() {
		professorSelecionado = null;
		txtId.setText("");
		txtNome.setText("");
		txtCpf.setText("");
		txtRg.setText("");
		txtDataNascimento.setText("");
		txtSexo.setText("");
		txtFormacao.setText("");
		txtTelefone.setText("");
	}

	// --- MÉTODOS AUXILIARES DE UI ---

	private JPanel criarBlocoCampo(String labelTexto, JComponent componente) {
		JPanel bloco = new JPanel();
		bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
		bloco.setOpaque(false);
		bloco.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel label = new JLabel(labelTexto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);

		componente.setAlignmentX(Component.LEFT_ALIGNMENT);
		componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
		componente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		componente.setForeground(textos);
		componente.setBackground(corCampo);
		componente.setBorder(new LineBorder(corBorda, 1, true));

		bloco.add(label);
		bloco.add(Box.createVerticalStrut(3));
		bloco.add(componente);
		return bloco;
	}

	private JPanel criarLinhaDupla(String label1, JComponent comp1, String label2, JComponent comp2) {
		JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
		row.setOpaque(false);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

		row.add(criarBlocoCampo(label1, comp1));
		row.add(criarBlocoCampo(label2, comp2));

		return row;
	}

	private JTextField criarCampoReadOnly(String texto) {
		JTextField textField = new JTextField(texto);
		textField.setEditable(false);
		return textField;
	}
}