package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.NotaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Disciplina;
import model.Nota;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class NotasAluno extends JFrame {

	private static final long serialVersionUID = 1L;

	// CORES
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	// COMPONENTES
	private JTable tabelaNotas;
	private DefaultTableModel modeloTabela;

	private JComboBox<String> comboTrimestre;
	private JComboBox<String> comboAluno;

	private JLabel lblAluno;
	private JLabel lblMatricula;
	private JLabel lblTurma;
	private JLabel lblCurso;
	private JLabel lblAno;

	// DADOS
	private final List<Nota> notas = new ArrayList<>();
	private final List<Aluno> alunosDisponiveis = new ArrayList<>();

	private final Map<Integer, String> nomesDisciplinas = new HashMap<>();

	private boolean carregandoAluno;

	// CONSTRUTOR
	public NotasAluno() {
		setTitle("Minhas Notas");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(false);

		JPanel fundo = new JPanel(new BorderLayout());
		fundo.setBackground(corExterna);
		fundo.setBorder(new EmptyBorder(25, 30, 25, 30));
		setContentPane(fundo);

		fundo.add(criarCabecalho(), BorderLayout.NORTH);
		fundo.add(criarConteudo(), BorderLayout.CENTER);

		carregarDadosDoBanco();
	}

	// =========================================================
	// CABEÇALHO
	// =========================================================

	private JPanel criarCabecalho() {
		JPanel painel = new JPanel(new BorderLayout());
		painel.setOpaque(false);
		painel.setBorder(new EmptyBorder(0, 0, 25, 0));

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setFocusPainted(false);
		btnVoltar.setBorderPainted(false);
		btnVoltar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnVoltar.setForeground(textos);
		btnVoltar.setBackground(corCampo);
		btnVoltar.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnVoltar.setPreferredSize(new Dimension(130, 42));
		btnVoltar.setBorder(new LineBorder(corBorda));
		btnVoltar.addActionListener(e -> dispose());

		JLabel titulo = new JLabel("MINHAS NOTAS");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));

		JLabel subtitulo = new JLabel("Visualize todas as notas lançadas pelos professores");
		subtitulo.setForeground(corLabel);
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		JPanel textosTitulo = new JPanel(new GridLayout(2, 1));
		textosTitulo.setOpaque(false);
		textosTitulo.add(titulo);
		textosTitulo.add(subtitulo);

		JPanel painelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		painelEsquerdo.setOpaque(false);
		painelEsquerdo.add(btnVoltar);
		painelEsquerdo.add(textosTitulo);

		// CARD DO ALUNO
		JPanel cardAluno = new JPanel(new GridLayout(2, 2, 20, 5));
		cardAluno.setBackground(corCampo);
		cardAluno.setBorder(new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(15, 20, 15, 20)));

		lblAluno = criarValorInfo();
		lblMatricula = criarValorInfo();
		lblTurma = criarValorInfo();
		lblCurso = criarValorInfo();
		lblAno = criarValorInfo();

		JPanel infoAluno = criarInfoComLabel("Aluno", lblAluno);
		JPanel infoMatricula = criarInfoComLabel("Matrícula", lblMatricula);
		JPanel infoTurma = criarInfoComLabel("Turma", lblTurma);
		JPanel infoCurso = criarInfoComLabel("Curso", lblCurso);

		cardAluno.add(infoAluno);
		cardAluno.add(infoMatricula);
		cardAluno.add(infoTurma);
		cardAluno.add(infoCurso);

		painel.add(painelEsquerdo, BorderLayout.WEST);
		painel.add(cardAluno, BorderLayout.EAST);

		return painel;
	}

	private JLabel criarValorInfo() {
		JLabel label = new JLabel("Carregando...");
		label.setForeground(textos);
		label.setFont(new Font("Segoe UI", Font.BOLD, 15));
		return label;
	}

	private JPanel criarInfoComLabel(String titulo, JLabel valor) {
		JPanel painel = new JPanel(new BorderLayout());
		painel.setOpaque(false);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));

		painel.add(lblTitulo, BorderLayout.NORTH);
		painel.add(valor, BorderLayout.CENTER);

		return painel;
	}

	// =========================================================
	// CONTEÚDO
	// =========================================================

	private JPanel criarConteudo() {
		JPanel painel = new JPanel(new BorderLayout());
		painel.setBackground(corInterna);
		painel.setBorder(new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(22, 25, 25, 25)));

		JPanel topoTabela = new JPanel(new BorderLayout());
		topoTabela.setOpaque(false);
		topoTabela.setBorder(new EmptyBorder(0, 0, 18, 0));

		JLabel tituloTabela = new JLabel("Notas lançadas");
		tituloTabela.setForeground(textos);
		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 22));

		JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		painelFiltro.setOpaque(false);

		/*
		 * Combo de aluno.
		 *
		 * Só será exibido para pai/responsável.
		 */
		comboAluno = new JComboBox<>();
		comboAluno.setFont(new Font("Segoe UI", Font.BOLD, 14));
		comboAluno.setForeground(textos);
		comboAluno.setBackground(new Color(55, 15, 130));
		comboAluno.setFocusable(false);
		comboAluno.setPreferredSize(new Dimension(240, 38));
		comboAluno.setVisible(false);

		JLabel lblAlunoFiltro = new JLabel("Aluno:");
		lblAlunoFiltro.setForeground(corLabel);
		lblAlunoFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblAlunoFiltro.setVisible(false);

		comboAluno.addActionListener(e -> {
			if (!carregandoAluno && comboAluno.getSelectedIndex() >= 0) {
				carregarAlunoSelecionado(comboAluno.getSelectedIndex());
			}
		});

		JLabel lblFiltro = new JLabel("Trimestre:");
		lblFiltro.setForeground(corLabel);
		lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));

		comboTrimestre = new JComboBox<>(
				new String[] { "Todos os trimestres", "1º Trimestre", "2º Trimestre", "3º Trimestre" });

		comboTrimestre.setFont(new Font("Segoe UI", Font.BOLD, 14));
		comboTrimestre.setForeground(textos);
		comboTrimestre.setBackground(new Color(55, 15, 130));
		comboTrimestre.setFocusable(false);
		comboTrimestre.setPreferredSize(new Dimension(210, 38));

		comboTrimestre.addActionListener(e -> carregarTabela(comboTrimestre.getSelectedIndex()));

		painelFiltro.add(lblAlunoFiltro);
		painelFiltro.add(comboAluno);
		painelFiltro.add(lblFiltro);
		painelFiltro.add(comboTrimestre);

		topoTabela.add(tituloTabela, BorderLayout.WEST);
		topoTabela.add(painelFiltro, BorderLayout.EAST);

		criarTabela();

		JScrollPane scroll = new JScrollPane(tabelaNotas);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getViewport().setBackground(corInterna);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		scroll.setBackground(corInterna);

		painel.add(topoTabela, BorderLayout.NORTH);
		painel.add(scroll, BorderLayout.CENTER);

		return painel;
	}

	// =========================================================
	// TABELA
	// =========================================================

	private void criarTabela() {
		modeloTabela = new DefaultTableModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabelaNotas = new JTable(modeloTabela);

		tabelaNotas.setRowHeight(48);
		tabelaNotas.setFont(new Font("Segoe UI", Font.BOLD, 15));
		tabelaNotas.setForeground(textos);
		tabelaNotas.setBackground(corInterna);
		tabelaNotas.setSelectionBackground(new Color(85, 35, 160));
		tabelaNotas.setSelectionForeground(textos);
		tabelaNotas.setGridColor(new Color(90, 45, 170));
		tabelaNotas.setShowVerticalLines(false);
		tabelaNotas.setShowHorizontalLines(true);
		tabelaNotas.setIntercellSpacing(new Dimension(0, 1));

		JTableHeader header = tabelaNotas.getTableHeader();
		header.setPreferredSize(new Dimension(0, 48));
		header.setBackground(new Color(60, 15, 135));
		header.setForeground(corLabel);
		header.setFont(new Font("Segoe UI", Font.BOLD, 14));
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);

		tabelaNotas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);

				cell.setOpaque(true);
				cell.setBorder(new EmptyBorder(0, 12, 0, 12));
				cell.setFont(new Font("Segoe UI", Font.BOLD, 15));

				if (isSelected) {
					cell.setBackground(new Color(95, 45, 180));
					cell.setForeground(textos);
				} else {
					cell.setBackground(corInterna);
					cell.setForeground(textos);
				}

				if (column == 0) {
					cell.setHorizontalAlignment(SwingConstants.LEFT);
					cell.setForeground(new Color(255, 210, 245));
				} else {
					cell.setHorizontalAlignment(SwingConstants.CENTER);
					cell.setForeground(corNota(value));
				}

				return cell;
			}
		});
	}

	// =========================================================
	// CARREGAMENTO PRINCIPAL
	// =========================================================

	private void carregarDadosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null) {
			JOptionPane.showMessageDialog(this, "Nenhum usuário está logado.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			AlunoDAO alunoDAO = new AlunoDAO(conn);

			/*
			 * ALUNO LOGADO
			 */
			if (usuario.getAlunoId() > 0) {
				Aluno aluno = alunoDAO.buscarPorId(usuario.getAlunoId());

				if (aluno == null) {
					JOptionPane.showMessageDialog(this, "Não foi possível localizar o aluno no banco de dados.", "Erro",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				alunosDisponiveis.clear();
				alunosDisponiveis.add(aluno);

				atualizarDadosCabecalho(conn, aluno);
				carregarNotasDoAluno(conn, aluno.getIdAluno());

				return;
			}

			/*
			 * PAI / RESPONSÁVEL LOGADO
			 */
			if (usuario.getPaiId() > 0) {

				List<Aluno> filhos = alunoDAO.listarPorPais(usuario.getPaiId());

				if (filhos.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Não existem alunos vinculados a este responsável.", "Notas",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				alunosDisponiveis.clear();
				alunosDisponiveis.addAll(filhos);

				preencherComboAlunos();

				Aluno primeiroAluno = alunosDisponiveis.get(0);

				atualizarDadosCabecalho(conn, primeiroAluno);
				carregarNotasDoAluno(conn, primeiroAluno.getIdAluno());

				return;
			}

			JOptionPane.showMessageDialog(this, "O usuário logado não possui um aluno ou responsável associado.",
					"Erro", JOptionPane.ERROR_MESSAGE);

		} catch (SQLException | RuntimeException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os dados das notas: " + ex.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	// =========================================================
	// COMBO DOS FILHOS
	// =========================================================

	private void preencherComboAlunos() {
		if (comboAluno == null) {
			return;
		}

		carregandoAluno = true;

		comboAluno.removeAllItems();

		for (Aluno aluno : alunosDisponiveis) {
			comboAluno.addItem(aluno.getNome());
		}

		comboAluno.setVisible(true);

		Component componente = comboAluno.getParent();

		if (componente != null && componente.getParent() != null) {
			/*
			 * Atualiza a interface depois de tornar o combo visível.
			 */
			componente.getParent().revalidate();
			componente.getParent().repaint();
		}

		carregandoAluno = false;

		if (!alunosDisponiveis.isEmpty()) {
			comboAluno.setSelectedIndex(0);
		}
	}

	// =========================================================
	// TROCA DO ALUNO SELECIONADO
	// =========================================================

	private void carregarAlunoSelecionado(int indice) {
		if (indice < 0 || indice >= alunosDisponiveis.size()) {
			return;
		}

		Aluno aluno = alunosDisponiveis.get(indice);

		try (Connection conn = ConnectionFactory.getConnection()) {

			atualizarDadosCabecalho(conn, aluno);
			carregarNotasDoAluno(conn, aluno.getIdAluno());

		} catch (SQLException | RuntimeException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar as notas do aluno: " + ex.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	// =========================================================
	// CARREGAR DADOS DO CABEÇALHO
	// =========================================================

	private void atualizarDadosCabecalho(Connection conn, Aluno aluno) throws SQLException {

		lblAluno.setText(valorOuNaoInformado(aluno.getNome()));
		lblMatricula.setText(valorOuNaoInformado(aluno.getMatricula()));

		String descricaoTurma = "Não informado";

		if (aluno.getIdTurma() > 0) {
			Turma turma = new TurmaDAO(conn).buscarPorId(aluno.getIdTurma());

			if (turma != null) {
				descricaoTurma = valorOuNaoInformado(turma.getDescricaoTurma());
			}
		}

		lblTurma.setText(descricaoTurma);

		/*
		 * O model Aluno/Turma fornecido não possui um campo de curso. Portanto não
		 * inventamos essa informação.
		 */
		lblCurso.setText("Não informado");

		lblAno.setText(String.valueOf(java.time.Year.now().getValue()));
	}

	// =========================================================
	// CARREGAR NOTAS
	// =========================================================

	private void carregarNotasDoAluno(Connection conn, int idAluno) throws SQLException {

		notas.clear();
		nomesDisciplinas.clear();

		NotaDAO notaDAO = new NotaDAO(conn);

		notas.addAll(notaDAO.listarPorAluno(idAluno));

		/*
		 * Carrega os nomes das disciplinas usando a mesma conexão. Assim evitamos abrir
		 * uma conexão para cada linha da tabela.
		 */
		DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

		for (Nota nota : notas) {

			int idDisciplina = nota.getIdDisciplina();

			if (nomesDisciplinas.containsKey(idDisciplina)) {
				continue;
			}

			Disciplina disciplina = disciplinaDAO.buscarPorId(idDisciplina);

			if (disciplina == null) {
				nomesDisciplinas.put(idDisciplina, "Não informado");
			} else {
				nomesDisciplinas.put(idDisciplina, valorOuNaoInformado(disciplina.getDescricao()));
			}
		}

		int filtro = comboTrimestre == null ? 0 : comboTrimestre.getSelectedIndex();

		carregarTabela(filtro);
	}

	// =========================================================
	// CARREGAR TABELA
	// =========================================================

	private void carregarTabela(int filtro) {

		if (modeloTabela == null) {
			return;
		}

		modeloTabela.setRowCount(0);
		modeloTabela.setColumnCount(0);

		modeloTabela.addColumn("Disciplina");
		modeloTabela.addColumn("Trimestre");
		modeloTabela.addColumn("Atividade");
		modeloTabela.addColumn("Nota");

		for (Nota nota : notas) {

			if (filtro > 0 && nota.getTrimestreId() != filtro) {
				continue;
			}

			String nomeDisciplina = nomesDisciplinas.get(nota.getIdDisciplina());

			if (nomeDisciplina == null || nomeDisciplina.trim().isEmpty()) {
				nomeDisciplina = "Não informado";
			}

			modeloTabela.addRow(new Object[] { nomeDisciplina, nota.getTrimestreId() + "º", nota.getAtividade(),
					String.format(Locale.forLanguageTag("pt-BR"), "%.1f", nota.getNota()) });
		}

		ajustarLarguras();
	}

	// =========================================================
	// AJUSTAR COLUNAS
	// =========================================================

	private void ajustarLarguras() {

		if (tabelaNotas == null || tabelaNotas.getColumnCount() == 0) {
			return;
		}

		tabelaNotas.getColumnModel().getColumn(0).setPreferredWidth(250);

		tabelaNotas.getColumnModel().getColumn(1).setPreferredWidth(100);

		tabelaNotas.getColumnModel().getColumn(2).setPreferredWidth(300);

		tabelaNotas.getColumnModel().getColumn(3).setPreferredWidth(100);
	}

	// =========================================================
	// COR DAS NOTAS
	// =========================================================

	private Color corNota(Object valor) {

		if (valor == null) {
			return textos;
		}

		String texto = valor.toString().replace(",", ".");

		if (texto.equals("-") || texto.isEmpty()) {
			return new Color(170, 150, 210);
		}

		try {

			double nota = Double.parseDouble(texto);

			if (nota >= 7) {
				return new Color(120, 255, 170);
			}

			if (nota >= 5) {
				return new Color(255, 220, 100);
			}

			return new Color(255, 120, 120);

		} catch (NumberFormatException ex) {
			return textos;
		}
	}

	// =========================================================
	// VALOR PADRÃO
	// =========================================================

	private String valorOuNaoInformado(String valor) {

		if (valor == null || valor.trim().isEmpty()) {
			return "Não informado";
		}

		return valor;
	}
}