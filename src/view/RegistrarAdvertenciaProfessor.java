package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import controller.AdvertenciaController;
import dao.AdvertenciaDAO;
import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Advertencia;
import model.AdvertenciaItem;
import model.Aluno;
import model.Disciplina;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class RegistrarAdvertenciaProfessor extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color FUNDO = new Color(24, 16, 35);
	private final Color CARD = new Color(39, 25, 55);
	private final Color CARD_SECUNDARIO = new Color(47, 30, 65);
	private final Color TEXTO = new Color(245, 240, 250);
	private final Color TEXTO_SECUNDARIO = new Color(190, 175, 200);
	private final Color DESTAQUE = new Color(190, 75, 180);
	private final Color BORDA = new Color(85, 60, 100);

	private final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private JComboBox<String> comboTurma;
	private JComboBox<String> comboDisciplina;
	private JComboBox<String> comboAluno;
	private JComboBox<String> comboMotivo;

	private JTextField campoData;
	private JTextArea campoDescricao;

	private JTable tabelaAdvertencias;
	private DefaultTableModel modeloTabela;

	private final List<Turma> turmasDoProfessor = new ArrayList<>();
	private final List<Disciplina> disciplinasDaTurma = new ArrayList<>();
	private final List<Aluno> alunosDaTurma = new ArrayList<>();

	private final Map<String, Integer> idsTurmas = new HashMap<>();
	private final Map<String, Integer> idsDisciplinas = new HashMap<>();
	private final Map<String, Integer> idsAlunos = new HashMap<>();

	private int professorLogadoId;

	public RegistrarAdvertenciaProfessor() {
		inicializarJanela();
		inicializarComponentes();
		carregarProfessorLogado();
	}

	private void inicializarJanela() {
		setTitle("Registrar Advertência");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(null);
		getContentPane().setBackground(FUNDO);

		Rectangle tela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		setBounds(tela);
		setResizable(false);
	}

	private void inicializarComponentes() {

		JPanel painelTopo = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				GradientPaint gradiente = new GradientPaint(0, 0, new Color(91, 39, 105), getWidth(), 0,
						new Color(183, 64, 155));

				g2.setPaint(gradiente);
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.dispose();
			}
		};

		painelTopo.setLayout(null);
		painelTopo.setBounds(0, 0, getWidth(), 90);

		JLabel titulo = new JLabel("Registrar Advertência");
		titulo.setForeground(Color.WHITE);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
		titulo.setBounds(40, 22, 500, 45);

		painelTopo.add(titulo);
		add(painelTopo);

		JPanel painelNovaAdvertencia = criarPainelCard();
		painelNovaAdvertencia.setBounds(35, 120, 570, 650);
		add(painelNovaAdvertencia);

		JLabel tituloNova = criarTitulo("Nova Advertência");
		tituloNova.setBounds(25, 20, 400, 35);
		painelNovaAdvertencia.add(tituloNova);

		JLabel labelTurma = criarLabel("Turma");
		labelTurma.setBounds(25, 75, 200, 25);
		painelNovaAdvertencia.add(labelTurma);

		comboTurma = criarCombo();
		comboTurma.setBounds(25, 103, 520, 38);
		painelNovaAdvertencia.add(comboTurma);

		JLabel labelDisciplina = criarLabel("Disciplina");
		labelDisciplina.setBounds(25, 155, 200, 25);
		painelNovaAdvertencia.add(labelDisciplina);

		comboDisciplina = criarCombo();
		comboDisciplina.setBounds(25, 183, 520, 38);
		painelNovaAdvertencia.add(comboDisciplina);

		JLabel labelAluno = criarLabel("Aluno");
		labelAluno.setBounds(25, 235, 200, 25);
		painelNovaAdvertencia.add(labelAluno);

		comboAluno = criarCombo();
		comboAluno.setBounds(25, 263, 520, 38);
		painelNovaAdvertencia.add(comboAluno);

		JLabel labelMotivo = criarLabel("Motivo");
		labelMotivo.setBounds(25, 315, 200, 25);
		painelNovaAdvertencia.add(labelMotivo);

		comboMotivo = criarCombo();
		comboMotivo.setBounds(25, 343, 520, 38);
		painelNovaAdvertencia.add(comboMotivo);

		JLabel labelData = criarLabel("Data");
		labelData.setBounds(25, 395, 200, 25);
		painelNovaAdvertencia.add(labelData);

		campoData = criarCampoTexto();
		campoData.setBounds(25, 423, 180, 38);
		campoData.setText(LocalDate.now().format(FORMATADOR_DATA));
		painelNovaAdvertencia.add(campoData);

		JLabel labelDescricao = criarLabel("Descrição");
		labelDescricao.setBounds(225, 395, 200, 25);
		painelNovaAdvertencia.add(labelDescricao);

		campoDescricao = new JTextArea();
		campoDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		campoDescricao.setForeground(TEXTO);
		campoDescricao.setBackground(CARD_SECUNDARIO);
		campoDescricao.setCaretColor(Color.WHITE);
		campoDescricao.setLineWrap(true);
		campoDescricao.setWrapStyleWord(true);
		campoDescricao.setBorder(new EmptyBorder(8, 8, 8, 8));

		JScrollPane scrollDescricao = new JScrollPane(campoDescricao);
		scrollDescricao.setBorder(new LineBorder(BORDA, 1, true));
		scrollDescricao.setBounds(225, 423, 320, 95);
		painelNovaAdvertencia.add(scrollDescricao);

		JButton botaoRegistrar = criarBotao("Registrar Advertência");
		botaoRegistrar.setBounds(25, 555, 220, 42);
		botaoRegistrar.addActionListener(e -> registrarAdvertencia());
		painelNovaAdvertencia.add(botaoRegistrar);

		JButton botaoLimpar = criarBotao("Limpar");
		botaoLimpar.setBounds(255, 555, 125, 42);
		botaoLimpar.addActionListener(e -> limparFormulario());
		painelNovaAdvertencia.add(botaoLimpar);

		JButton botaoCancelar = criarBotao("Cancelar");
		botaoCancelar.setBounds(390, 555, 155, 42);
		botaoCancelar.addActionListener(e -> dispose());
		painelNovaAdvertencia.add(botaoCancelar);

		JPanel painelHistorico = criarPainelCard();
		painelHistorico.setBounds(630, 120, 800, 650);
		add(painelHistorico);

		JLabel tituloHistorico = criarTitulo("Histórico de Advertências");
		tituloHistorico.setBounds(25, 20, 500, 35);
		painelHistorico.add(tituloHistorico);

		modeloTabela = new DefaultTableModel(new Object[] { "Data", "Turma", "Disciplina", "Aluno", "Motivo" }, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabelaAdvertencias = new JTable(modeloTabela);
		tabelaAdvertencias.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		tabelaAdvertencias.setForeground(TEXTO);
		tabelaAdvertencias.setBackground(CARD_SECUNDARIO);
		tabelaAdvertencias.setRowHeight(32);
		tabelaAdvertencias.setSelectionBackground(new Color(95, 50, 110));
		tabelaAdvertencias.setSelectionForeground(Color.WHITE);
		tabelaAdvertencias.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
		tabelaAdvertencias.getTableHeader().setForeground(Color.WHITE);
		tabelaAdvertencias.getTableHeader().setBackground(new Color(70, 40, 85));

		tabelaAdvertencias.getColumnModel().getColumn(0).setPreferredWidth(90);
		tabelaAdvertencias.getColumnModel().getColumn(1).setPreferredWidth(130);
		tabelaAdvertencias.getColumnModel().getColumn(2).setPreferredWidth(150);
		tabelaAdvertencias.getColumnModel().getColumn(3).setPreferredWidth(210);
		tabelaAdvertencias.getColumnModel().getColumn(4).setPreferredWidth(180);

		JScrollPane scrollTabela = new JScrollPane(tabelaAdvertencias);
		scrollTabela.setBounds(25, 75, 750, 535);
		scrollTabela.setBorder(new LineBorder(BORDA, 1, true));
		painelHistorico.add(scrollTabela);

		comboTurma.addActionListener(e -> carregarDadosDaTurma());
	}

	private JPanel criarPainelCard() {
		JPanel painel = new JPanel(null);
		painel.setBackground(CARD);
		painel.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(BORDA, 1, true), new EmptyBorder(1, 1, 1, 1)));
		return painel;
	}

	private JLabel criarTitulo(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(TEXTO);
		label.setFont(new Font("Segoe UI", Font.BOLD, 21));
		return label;
	}

	private JLabel criarLabel(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(TEXTO_SECUNDARIO);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		return label;
	}

	private JComboBox<String> criarCombo() {
		JComboBox<String> combo = new JComboBox<>();
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		combo.setForeground(TEXTO);
		combo.setBackground(CARD_SECUNDARIO);
		combo.setBorder(new LineBorder(BORDA, 1, true));
		return combo;
	}

	private JTextField criarCampoTexto() {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		campo.setForeground(TEXTO);
		campo.setBackground(CARD_SECUNDARIO);
		campo.setCaretColor(Color.WHITE);
		campo.setBorder(new LineBorder(BORDA, 1, true));
		return campo;
	}

	private JButton criarBotao(String texto) {
		JButton botao = new JButton(texto);
		botao.setFont(new Font("Segoe UI", Font.BOLD, 13));
		botao.setForeground(Color.WHITE);
		botao.setBackground(DESTAQUE);
		botao.setFocusPainted(false);
		botao.setBorder(new LineBorder(DESTAQUE, 1, true));
		botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return botao;
	}

	private void carregarProfessorLogado() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Nenhum usuário está logado.", "Erro", JOptionPane.ERROR_MESSAGE);
			dispose();
			return;
		}

		if (usuario.getProfessorId() <= 0) {
			JOptionPane.showMessageDialog(this, "O usuário logado não possui um professor associado.",
					"Acesso inválido", JOptionPane.ERROR_MESSAGE);
			dispose();
			return;
		}

		professorLogadoId = usuario.getProfessorId();

		carregarTurmasDoProfessor();
	}

	private void carregarTurmasDoProfessor() {

		comboTurma.removeAllItems();
		idsTurmas.clear();
		turmasDoProfessor.clear();

		comboTurma.addItem("Selecione");

		try (Connection conn = ConnectionFactory.getConnection()) {

			TurmaDAO turmaDAO = new TurmaDAO(conn);

			List<Turma> turmas = turmaDAO.listarPorProfessor(professorLogadoId);

			for (Turma turma : turmas) {

				if (turma == null || !turma.isAtivo()) {
					continue;
				}

				String nomeTurma = turma.getDescricaoTurma();

				if (nomeTurma == null || nomeTurma.trim().isEmpty()) {
					nomeTurma = "Turma " + turma.getIdTurma();
				}

				turmasDoProfessor.add(turma);
				idsTurmas.put(nomeTurma, turma.getIdTurma());
				comboTurma.addItem(nomeTurma);
			}

			carregarHistoricoAdvertencias();

		} catch (SQLException e) {

			JOptionPane.showMessageDialog(this, "Erro ao carregar as turmas do professor.\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarDadosDaTurma() {

		limparDisciplinas();
		limparAlunos();

		String turmaSelecionada = (String) comboTurma.getSelectedItem();

		if (turmaSelecionada == null || "Selecione".equals(turmaSelecionada)) {
			carregarHistoricoAdvertencias();
			return;
		}

		Integer turmaId = idsTurmas.get(turmaSelecionada);

		if (turmaId == null || turmaId <= 0) {
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			AlunoDAO alunoDAO = new AlunoDAO(conn);
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

			List<Aluno> alunos = alunoDAO.listarPorTurma(turmaId);

			for (Aluno aluno : alunos) {

				if (aluno == null || !aluno.isAtivo()) {
					continue;
				}

				String nomeAluno = aluno.getNome();

				if (nomeAluno == null || nomeAluno.trim().isEmpty()) {
					nomeAluno = "Aluno " + aluno.getIdAluno();
				}

				alunosDaTurma.add(aluno);
				idsAlunos.put(nomeAluno, aluno.getIdAluno());
				comboAluno.addItem(nomeAluno);
			}

			List<Disciplina> disciplinas = disciplinaDAO.listarPorProfessorETurma(professorLogadoId, turmaId);

			for (Disciplina disciplina : disciplinas) {

				if (disciplina == null || !disciplina.isAtivo()) {
					continue;
				}

				String descricao = disciplina.getDescricao();

				if (descricao == null || descricao.trim().isEmpty()) {
					descricao = "Disciplina " + disciplina.getIdDisciplina();
				}

				disciplinasDaTurma.add(disciplina);
				idsDisciplinas.put(descricao, disciplina.getIdDisciplina());
				comboDisciplina.addItem(descricao);
			}

			carregarHistoricoDaTurma(turmaId);

		} catch (SQLException e) {

			JOptionPane.showMessageDialog(this, "Erro ao carregar os dados da turma.\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void limparDisciplinas() {

		comboDisciplina.removeAllItems();
		comboDisciplina.addItem("Selecione");

		idsDisciplinas.clear();
		disciplinasDaTurma.clear();
	}

	private void limparAlunos() {

		comboAluno.removeAllItems();
		comboAluno.addItem("Selecione");

		idsAlunos.clear();
		alunosDaTurma.clear();
	}

	@SuppressWarnings("unused")
	private void carregarMotivos() {

		comboMotivo.removeAllItems();
		comboMotivo.addItem("Selecione");

		/*
		 * O modelo Advertencia não possui uma tabela/API específica de motivos.
		 * Portanto, não serão inventados motivos no código.
		 *
		 * Os motivos podem ser adicionados posteriormente caso exista no banco uma
		 * estrutura específica para isso.
		 */
	}

	private void registrarAdvertencia() {

		String turmaSelecionada = (String) comboTurma.getSelectedItem();
		String disciplinaSelecionada = (String) comboDisciplina.getSelectedItem();
		String alunoSelecionado = (String) comboAluno.getSelectedItem();
		String motivo = (String) comboMotivo.getSelectedItem();

		String descricao = campoDescricao.getText();

		if (turmaSelecionada == null || "Selecione".equals(turmaSelecionada)) {
			JOptionPane.showMessageDialog(this, "Selecione uma turma.", "Validação", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (disciplinaSelecionada == null || "Selecione".equals(disciplinaSelecionada)) {
			JOptionPane.showMessageDialog(this, "Selecione uma disciplina.", "Validação", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (alunoSelecionado == null || "Selecione".equals(alunoSelecionado)) {
			JOptionPane.showMessageDialog(this, "Selecione um aluno.", "Validação", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (motivo == null || "Selecione".equals(motivo)) {
			JOptionPane.showMessageDialog(this, "Selecione um motivo.", "Validação", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (descricao == null || descricao.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Informe a descrição da advertência.", "Validação",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		LocalDate data;

		try {
			data = LocalDate.parse(campoData.getText().trim(), FORMATADOR_DATA);
		} catch (DateTimeParseException e) {
			JOptionPane.showMessageDialog(this, "Informe uma data válida no formato dd/MM/yyyy.", "Validação",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (data.isAfter(LocalDate.now())) {
			JOptionPane.showMessageDialog(this, "A data da advertência não pode ser futura.", "Validação",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Integer turmaId = idsTurmas.get(turmaSelecionada);
		Integer alunoId = idsAlunos.get(alunoSelecionado);

		if (turmaId == null || turmaId <= 0) {
			JOptionPane.showMessageDialog(this, "Turma selecionada inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (alunoId == null || alunoId <= 0) {
			JOptionPane.showMessageDialog(this, "Aluno selecionado inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}

		AdvertenciaItem item = new AdvertenciaItem(alunoId, alunoSelecionado);

		item.setMotivo(motivo.trim());
		item.setDescricao(descricao.trim());

		Advertencia advertencia = new Advertencia();
		advertencia.setProfessorId(professorLogadoId);
		advertencia.setTurmaId(turmaId);
		advertencia.setDataAdvertencia(data);

		List<AdvertenciaItem> itens = new ArrayList<>();
		itens.add(item);
		advertencia.setItens(itens);

		try {

			AdvertenciaController controller = new AdvertenciaController();
			controller.salvarAdvertencia(advertencia);

			JOptionPane.showMessageDialog(this, "Advertência registrada com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);

			limparFormulario();
			carregarHistoricoAdvertencias();

		} catch (IllegalArgumentException e) {

			JOptionPane.showMessageDialog(this, e.getMessage(), "Não foi possível registrar",
					JOptionPane.WARNING_MESSAGE);

		} catch (RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Erro ao registrar a advertência.\n" + obterMensagemErro(e), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void limparFormulario() {

		comboTurma.setSelectedIndex(0);

		limparDisciplinas();
		limparAlunos();

		comboMotivo.setSelectedIndex(0);

		campoData.setText(LocalDate.now().format(FORMATADOR_DATA));
		campoDescricao.setText("");
	}

	private void carregarHistoricoAdvertencias() {

		modeloTabela.setRowCount(0);

		if (professorLogadoId <= 0) {
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			AdvertenciaDAO advertenciaDAO = new AdvertenciaDAO(conn);
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

			Map<Integer, Turma> mapaTurmas = new HashMap<>();

			for (Turma turma : turmasDoProfessor) {
				mapaTurmas.put(turma.getIdTurma(), turma);
			}

			for (Turma turma : turmasDoProfessor) {

				if (turma == null || turma.getIdTurma() <= 0) {
					continue;
				}

				List<Advertencia> advertencias = advertenciaDAO.listarPorTurma(turma.getIdTurma());

				adicionarAdvertenciasNaTabela(advertencias, alunoDAO, turmaDAO, disciplinaDAO, mapaTurmas);
			}

		} catch (SQLException e) {

			JOptionPane.showMessageDialog(this, "Erro ao carregar o histórico de advertências.\n" + e.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarHistoricoDaTurma(int turmaId) {

		modeloTabela.setRowCount(0);

		if (turmaId <= 0) {
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			AdvertenciaDAO advertenciaDAO = new AdvertenciaDAO(conn);
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

			List<Advertencia> advertencias = advertenciaDAO.listarPorTurma(turmaId);

			adicionarAdvertenciasNaTabela(advertencias, alunoDAO, turmaDAO, disciplinaDAO, new HashMap<>());

		} catch (SQLException e) {

			JOptionPane.showMessageDialog(this, "Erro ao carregar o histórico da turma.\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void adicionarAdvertenciasNaTabela(List<Advertencia> advertencias, AlunoDAO alunoDAO, TurmaDAO turmaDAO,
			DisciplinaDAO disciplinaDAO, Map<Integer, Turma> mapaTurmas) throws SQLException {

		for (Advertencia advertencia : advertencias) {

			if (advertencia == null) {
				continue;
			}

			if (advertencia.getProfessorId() != professorLogadoId) {
				continue;
			}

			Aluno aluno = alunoDAO.buscarPorId(advertencia.getAlunoId());

			Turma turma = mapaTurmas.get(advertencia.getTurmaId());

			if (turma == null) {
				turma = turmaDAO.buscarPorId(advertencia.getTurmaId());
			}

			String nomeAluno = aluno != null ? aluno.getNome() : "Não informado";

			String nomeTurma = turma != null ? turma.getDescricaoTurma() : "Não informado";

			String disciplina = obterDisciplinaDaAdvertencia(disciplinaDAO, advertencia.getTurmaId());

			String data = advertencia.getDataAdvertencia() != null
					? advertencia.getDataAdvertencia().format(FORMATADOR_DATA)
					: "Não informado";

			modeloTabela.addRow(new Object[] { data, valorOuNaoInformado(nomeTurma), disciplina,
					valorOuNaoInformado(nomeAluno), valorOuNaoInformado(advertencia.getMotivo()) });
		}
	}

	private String obterDisciplinaDaAdvertencia(DisciplinaDAO disciplinaDAO, int turmaId) throws SQLException {

		String disciplinaSelecionada = (String) comboDisciplina.getSelectedItem();

		if (disciplinaSelecionada != null && !"Selecione".equals(disciplinaSelecionada)
				&& idsDisciplinas.containsKey(disciplinaSelecionada)) {

			return disciplinaSelecionada;
		}

		List<Disciplina> disciplinas = disciplinaDAO.listarPorProfessorETurma(professorLogadoId, turmaId);

		if (disciplinas.size() == 1) {
			return disciplinas.get(0).getDescricao();
		}

		if (disciplinas.isEmpty()) {
			return "Não informado";
		}

		return "Não informado";
	}

	private String valorOuNaoInformado(String valor) {
		if (valor == null || valor.trim().isEmpty()) {
			return "Não informado";
		}
		return valor;
	}

	private String obterMensagemErro(Throwable erro) {

		Throwable atual = erro;

		while (atual.getCause() != null) {
			atual = atual.getCause();
		}

		if (atual.getMessage() == null || atual.getMessage().trim().isEmpty()) {
			return "Não foi possível concluir a operação.";
		}

		return atual.getMessage();
	}
}
