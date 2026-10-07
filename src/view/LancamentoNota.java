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
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.NotaDAO;
import dao.ProfessorDAO;
import dao.TrimestreDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Disciplina;
import model.Nota;
import model.Professor;
import model.Trimestre;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class LancamentoNota extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	private JTable tabelaNotas;
	private DefaultTableModel modeloTabela;

	private JTextField txtAtividade;

	private JComboBox<String> cbTurma;
	private JComboBox<String> cbDisciplina;
	private JComboBox<String> cbPeriodo;
	private JComboBox<String> cbTipoNota;

	private JLabel lblResumoAlunos;
	private JLabel lblProfessor;

	private final List<Turma> turmasDisponiveis = new ArrayList<>();
	private final List<Disciplina> disciplinasDisponiveis = new ArrayList<>();
	private final List<Trimestre> trimestresDisponiveis = new ArrayList<>();
	private final List<Aluno> alunosSelecionados = new ArrayList<>();

	private int idProfessorLogado;
	private int idTurmaSelecionada;
	private int idDisciplinaSelecionada;
	private int idTrimestreSelecionado;

	public LancamentoNota() {

		setTitle("Lançamento de Notas");

		setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Lancamento de Notas.png"));

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int margem = 30;

		int larguraInterno = areaUtil.width - (margem * 2);

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

		interno.setBounds((areaUtil.width - larguraInterno) / 2, (areaUtil.height - alturaInterno) / 2, larguraInterno,
				alturaInterno);

		interno.setBackground(corInterna);

		externo.add(interno);

		int larguraResumo = 260;
		int margemLateral = 40;

		int larguraConteudo = larguraInterno - larguraResumo - (margemLateral * 3);

		JLabel titulo = new JLabel("Lançamento de Notas");

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));

		titulo.setHorizontalAlignment(SwingConstants.CENTER);

		titulo.setBounds(0, 45, larguraInterno, 55);

		interno.add(titulo);

		JLabel subtitulo = new JLabel("Registre, calcule e acompanhe o desempenho dos alunos por turma.");

		subtitulo.setForeground(textos);

		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));

		subtitulo.setHorizontalAlignment(SwingConstants.CENTER);

		subtitulo.setBounds(0, 105, larguraInterno, 30);

		interno.add(subtitulo);

		JPanel filtros = criarPainelArredondado(corCampo, corBorda, 24);

		filtros.setLayout(null);

		filtros.setBounds(margemLateral, 160, larguraConteudo, 160);

		interno.add(filtros);

		int larguraConteudoFiltros = 850;

		int inicioFiltros = Math.max(30, (filtros.getWidth() - larguraConteudoFiltros) / 2);

		adicionarLabel(filtros, "Turma", inicioFiltros, 10);

		cbTurma = criarCombo(new String[] {});

		cbTurma.setBounds(inicioFiltros, 45, 210, 38);

		filtros.add(cbTurma);

		adicionarLabel(filtros, "Disciplina", inicioFiltros + 230, 10);

		cbDisciplina = criarCombo(new String[] {});

		cbDisciplina.setBounds(inicioFiltros + 230, 45, 300, 38);

		filtros.add(cbDisciplina);

		adicionarLabel(filtros, "Período", inicioFiltros + 550, 10);

		cbPeriodo = criarCombo(new String[] {});

		cbPeriodo.setBounds(inicioFiltros + 550, 45, 180, 38);

		filtros.add(cbPeriodo);

		adicionarLabel(filtros, "Tipo da Nota", inicioFiltros, 80);

		cbTipoNota = criarCombo(
				new String[] { "Prova", "Trabalho", "Avaliação", "Seminário", "Projeto", "Recuperação", "Outro" });

		cbTipoNota.setBounds(inicioFiltros, 105, 180, 38);

		filtros.add(cbTipoNota);

		adicionarLabel(filtros, "Descrição", inicioFiltros + 200, 80);

		txtAtividade = criarCampoTexto();

		txtAtividade.setBounds(inicioFiltros + 200, 105, 340, 38);

		filtros.add(txtAtividade);

		JButton btnCarregar = new JButton("Carregar alunos");

		btnCarregar.setBounds(inicioFiltros + 550, 105, 190, 38);

		estilizarBotao(btnCarregar);

		filtros.add(btnCarregar);

		JPanel tabelaPainel = criarPainelArredondado(corCampo, corBorda, 24);

		tabelaPainel.setLayout(null);

		tabelaPainel.setBounds(margemLateral, 335, larguraConteudo, alturaInterno - 395);

		interno.add(tabelaPainel);

		JLabel tituloTabela = new JLabel("Notas dos Alunos");

		tituloTabela.setForeground(textos);

		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 26));

		tituloTabela.setBounds(30, 20, 400, 35);

		tabelaPainel.add(tituloTabela);

		criarTabela();

		JScrollPane scroll = new JScrollPane(tabelaNotas);

		int larguraScroll = tabelaPainel.getWidth() - 60;

		int alturaScroll = tabelaPainel.getHeight() - 145;

		scroll.setBounds(30, 70, larguraScroll, alturaScroll);

		scroll.getViewport().setBackground(corInterna);

		scroll.setBorder(new LineBorder(corBorda, 1, true));

		scroll.getVerticalScrollBar().setUnitIncrement(26);

		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		tabelaPainel.add(scroll);

		int yBotoes = scroll.getY() + scroll.getHeight() + 15;

		JButton btnSalvar = new JButton("Salvar notas");

		btnSalvar.setBounds(30, yBotoes, 170, 42);

		estilizarBotao(btnSalvar);

		tabelaPainel.add(btnSalvar);

		JButton btnLimpar = new JButton("Limpar");

		btnLimpar.setBounds(220, yBotoes, 130, 42);

		estilizarBotao(btnLimpar);

		tabelaPainel.add(btnLimpar);

		JButton btnCancelar = new JButton("Cancelar");

		btnCancelar.setBounds(370, yBotoes, 130, 42);

		estilizarBotao(btnCancelar);

		tabelaPainel.add(btnCancelar);

		JPanel resumo = criarPainelArredondado(corCampo, corBorda, 28);

		resumo.setLayout(null);

		resumo.setBounds(larguraInterno - larguraResumo - margemLateral, 160, larguraResumo, alturaInterno - 300);

		interno.add(resumo);

		JLabel tituloResumo = new JLabel("Resumo");

		tituloResumo.setForeground(textos);

		tituloResumo.setFont(new Font("Segoe UI", Font.BOLD, 28));

		tituloResumo.setBounds(25, 25, 220, 35);

		resumo.add(tituloResumo);

		JPanel linha = new JPanel();

		linha.setBackground(corLabel);

		linha.setBounds(25, 70, 210, 3);

		resumo.add(linha);

		adicionarResumo(resumo, "Alunos", "0", 105);

		lblResumoAlunos = obterLabelResumo(resumo);

		lblProfessor = new JLabel("Professor: não identificado");

		lblProfessor.setForeground(textos);

		lblProfessor.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		lblProfessor.setVerticalAlignment(SwingConstants.TOP);

		lblProfessor.setBounds(25, 190, 210, 70);

		resumo.add(lblProfessor);

		JLabel dica = new JLabel("<html>Selecione uma turma, uma disciplina e o período. "
				+ "Depois informe a atividade e as notas dos alunos.</html>");

		dica.setForeground(textos);

		dica.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		dica.setBounds(25, 285, 210, 130);

		resumo.add(dica);

		cbTurma.addActionListener(e -> atualizarDisciplinas());

		cbDisciplina.addActionListener(e -> atualizarIdsSelecionados());

		cbPeriodo.addActionListener(e -> atualizarIdsSelecionados());

		cbTipoNota.addActionListener(e -> atualizarModoRecuperacao());

		btnCarregar.addActionListener(e -> carregarAlunos());

		btnLimpar.addActionListener(e -> limparNotas());

		btnCancelar.addActionListener(e -> dispose());

		btnSalvar.addActionListener(e -> salvarNotas());

		carregarProfessorLogado();

		carregarTrimestres();

		atualizarModoRecuperacao();

		setVisible(true);
	}

	private void criarTabela() {

		String[] colunas = { "Matrícula", "Aluno", "Nota" };

		modeloTabela = new DefaultTableModel(new Object[][] {}, colunas) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {

				if (column != 2) {
					return false;
				}

				if (!isRecuperacao()) {
					return true;
				}

				return tabelaNotas != null && tabelaNotas.getSelectedRow() == row;
			}
		};

		tabelaNotas = new JTable(modeloTabela);

		tabelaNotas.setRowHeight(50);

		tabelaNotas.setFont(new Font("Segoe UI", Font.PLAIN, 17));

		tabelaNotas.setForeground(textos);

		tabelaNotas.setBackground(corInterna);

		tabelaNotas.setGridColor(new Color(90, 50, 170));

		tabelaNotas.setSelectionBackground(new Color(80, 40, 160));

		tabelaNotas.setSelectionForeground(textos);

		tabelaNotas.setShowGrid(true);

		tabelaNotas.setShowHorizontalLines(true);

		tabelaNotas.setShowVerticalLines(true);

		tabelaNotas.setIntercellSpacing(new Dimension(1, 1));

		tabelaNotas.setFillsViewportHeight(false);

		tabelaNotas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		tabelaNotas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

		JTableHeader header = tabelaNotas.getTableHeader();

		header.setFont(new Font("Segoe UI", Font.BOLD, 16));

		header.setBackground(corInterna);

		header.setForeground(textos);

		header.setReorderingAllowed(false);

		header.setResizingAllowed(false);

		header.setPreferredSize(new Dimension(header.getWidth(), 32));

		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();

		centro.setHorizontalAlignment(SwingConstants.CENTER);

		centro.setVerticalAlignment(SwingConstants.CENTER);

		centro.setBackground(new Color(31, 10, 90));

		centro.setForeground(textos);

		centro.setFont(new Font("Segoe UI", Font.PLAIN, 17));

		for (int i = 0; i < tabelaNotas.getColumnCount(); i++) {

			tabelaNotas.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JTextField campoNota = new JTextField();

		campoNota.setHorizontalAlignment(JTextField.CENTER);

		campoNota.setFont(new Font("Segoe UI", Font.BOLD, 17));

		campoNota.setForeground(textos);

		campoNota.setCaretColor(textos);

		campoNota.setBackground(new Color(31, 10, 90));

		campoNota.setBorder(new LineBorder(corLabel, 1, true));

		DefaultCellEditor editorNota = new DefaultCellEditor(campoNota);

		tabelaNotas.getColumnModel().getColumn(2).setCellEditor(editorNota);

		tabelaNotas.getColumnModel().getColumn(0).setPreferredWidth(170);

		tabelaNotas.getColumnModel().getColumn(1).setPreferredWidth(520);

		tabelaNotas.getColumnModel().getColumn(2).setPreferredWidth(170);

		tabelaNotas.getSelectionModel().addListSelectionListener(e -> {

			if (!e.getValueIsAdjusting() && isRecuperacao()) {

				tabelaNotas.repaint();
			}
		});
	}

	private void carregarProfessorLogado() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null) {

			JOptionPane.showMessageDialog(this, "Nenhum usuário está logado.", "Sessão inválida",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		idProfessorLogado = usuario.getProfessorId();

		if (idProfessorLogado <= 0) {

			JOptionPane.showMessageDialog(this, "O usuário logado não possui um professor associado.",
					"Professor inválido", JOptionPane.WARNING_MESSAGE);

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			Professor professor = new ProfessorDAO(conn).buscarPorId(idProfessorLogado);

			if (professor == null) {

				JOptionPane.showMessageDialog(this, "Não foi possível localizar o professor logado.",
						"Professor não encontrado", JOptionPane.WARNING_MESSAGE);

				return;
			}

			lblProfessor.setText("<html>Professor:<br>" + professor.getNome() + "</html>");

			carregarTurmas();

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os dados do professor.\n\n" + e.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarTurmas() {

		cbTurma.removeAllItems();

		turmasDisponiveis.clear();

		if (idProfessorLogado <= 0) {
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			TurmaDAO turmaDAO = new TurmaDAO(conn);

			List<Turma> turmas = turmaDAO.listarPorProfessor(idProfessorLogado);

			turmasDisponiveis.addAll(turmas);

			for (Turma turma : turmasDisponiveis) {

				cbTurma.addItem(turma.getDescricaoTurma());
			}

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar as turmas.\n\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (cbTurma.getItemCount() > 0) {

			cbTurma.setSelectedIndex(0);

			atualizarDisciplinas();

		} else {

			JOptionPane.showMessageDialog(this, "O professor logado não possui turmas vinculadas.", "Nenhuma turma",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void atualizarDisciplinas() {

		cbDisciplina.removeAllItems();

		disciplinasDisponiveis.clear();

		idTurmaSelecionada = 0;
		idDisciplinaSelecionada = 0;

		int indiceTurma = cbTurma.getSelectedIndex();

		if (indiceTurma < 0 || indiceTurma >= turmasDisponiveis.size()) {

			return;
		}

		Turma turma = turmasDisponiveis.get(indiceTurma);

		idTurmaSelecionada = turma.getIdTurma();

		try (Connection conn = ConnectionFactory.getConnection()) {

			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

			List<Disciplina> disciplinas = disciplinaDAO.listarPorProfessorETurma(idProfessorLogado,
					idTurmaSelecionada);

			disciplinasDisponiveis.addAll(disciplinas);

			for (Disciplina disciplina : disciplinasDisponiveis) {

				cbDisciplina.addItem(disciplina.getDescricao());
			}

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar as disciplinas.\n\n" + e.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (cbDisciplina.getItemCount() > 0) {

			cbDisciplina.setSelectedIndex(0);

			atualizarIdsSelecionados();

		} else {

			JOptionPane.showMessageDialog(this,
					"O professor não possui disciplinas vinculadas " + "à turma selecionada.", "Nenhuma disciplina",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void carregarTrimestres() {

		cbPeriodo.removeAllItems();

		trimestresDisponiveis.clear();

		try (Connection conn = ConnectionFactory.getConnection()) {

			TrimestreDAO trimestreDAO = new TrimestreDAO(conn);

			List<Trimestre> trimestres = trimestreDAO.listarTodos();

			trimestresDisponiveis.addAll(trimestres);

			for (Trimestre trimestre : trimestresDisponiveis) {

				cbPeriodo.addItem(trimestre.getNumero() + "º Trimestre - " + trimestre.getAnoLetivo());
			}

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os períodos letivos.\n\n" + e.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}

		if (cbPeriodo.getItemCount() > 0) {

			cbPeriodo.setSelectedIndex(0);

			atualizarIdsSelecionados();
		}
	}

	private void atualizarIdsSelecionados() {

		int indiceTurma = cbTurma.getSelectedIndex();

		int indiceDisciplina = cbDisciplina.getSelectedIndex();

		int indiceTrimestre = cbPeriodo.getSelectedIndex();

		idTurmaSelecionada = 0;
		idDisciplinaSelecionada = 0;
		idTrimestreSelecionado = 0;

		if (indiceTurma >= 0 && indiceTurma < turmasDisponiveis.size()) {

			idTurmaSelecionada = turmasDisponiveis.get(indiceTurma).getIdTurma();
		}

		if (indiceDisciplina >= 0 && indiceDisciplina < disciplinasDisponiveis.size()) {

			idDisciplinaSelecionada = disciplinasDisponiveis.get(indiceDisciplina).getIdDisciplina();
		}

		if (indiceTrimestre >= 0 && indiceTrimestre < trimestresDisponiveis.size()) {

			idTrimestreSelecionado = trimestresDisponiveis.get(indiceTrimestre).getIdTrimestre();
		}
	}

	private void atualizarModoRecuperacao() {

		if (tabelaNotas == null) {
			return;
		}

		if (tabelaNotas.isEditing()) {
			tabelaNotas.getCellEditor().stopCellEditing();
		}

		tabelaNotas.clearSelection();

		tabelaNotas.repaint();
	}

	private boolean isRecuperacao() {

		Object selecionado = cbTipoNota == null ? null : cbTipoNota.getSelectedItem();

		return selecionado != null && "Recuperação".equalsIgnoreCase(selecionado.toString().trim());
	}

	private void carregarAlunos() {

		atualizarIdsSelecionados();

		if (idTurmaSelecionada <= 0) {

			JOptionPane.showMessageDialog(this, "Selecione uma turma.", "Turma obrigatória",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (idDisciplinaSelecionada <= 0) {

			JOptionPane.showMessageDialog(this, "Selecione uma disciplina.", "Disciplina obrigatória",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (idTrimestreSelecionado <= 0) {

			JOptionPane.showMessageDialog(this, "Selecione um trimestre.", "Trimestre obrigatório",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (!isRecuperacao()) {

			String atividade = montarAtividade();

			if (atividade.isEmpty()) {

				JOptionPane.showMessageDialog(this, "Informe a descrição da atividade antes de carregar os alunos.",
						"Atividade obrigatória", JOptionPane.WARNING_MESSAGE);

				txtAtividade.requestFocus();

				return;
			}
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			AlunoDAO alunoDAO = new AlunoDAO(conn);

			List<Aluno> alunos = alunoDAO.listarPorTurma(idTurmaSelecionada);

			alunosSelecionados.clear();

			alunosSelecionados.addAll(alunos);

			modeloTabela.setRowCount(0);

			NotaDAO notaDAO = new NotaDAO(conn);

			for (Aluno aluno : alunos) {

				String valorNota = "";

				if (!isRecuperacao()) {

					String atividade = montarAtividade();

					Nota notaExistente = notaDAO.buscarPorAlunoDisciplinaAtividade(aluno.getIdAluno(),
							idDisciplinaSelecionada, idTrimestreSelecionado, atividade);

					if (notaExistente != null) {

						valorNota = formatarNota(notaExistente.getNota());
					}
				}

				modeloTabela.addRow(new Object[] { aluno.getMatricula(), aluno.getNome(), valorNota });
			}

			atualizarResumo();

			if (!alunos.isEmpty()) {

				if (isRecuperacao()) {

					JOptionPane.showMessageDialog(this,
							"Selecione exatamente um aluno na tabela " + "para lançar a recuperação.\n\n"
									+ "A nota da recuperação substituirá " + "a menor nota somente se for maior.",
							"Recuperação", JOptionPane.INFORMATION_MESSAGE);
				}

			} else {

				JOptionPane.showMessageDialog(this, "Não existem alunos ativos nesta turma.", "Nenhum aluno",
						JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os alunos.\n\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void salvarNotas() {

		if (tabelaNotas.isEditing()) {

			tabelaNotas.getCellEditor().stopCellEditing();
		}

		atualizarIdsSelecionados();

		if (idProfessorLogado <= 0 || idTurmaSelecionada <= 0 || idDisciplinaSelecionada <= 0
				|| idTrimestreSelecionado <= 0) {

			JOptionPane.showMessageDialog(this, "Selecione turma, disciplina e período antes de salvar.",
					"Dados incompletos", JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (isRecuperacao()) {

			salvarRecuperacao();

			return;
		}

		String atividade = montarAtividade();

		if (atividade.isEmpty()) {

			JOptionPane.showMessageDialog(this, "Informe a descrição da atividade antes de salvar.",
					"Atividade obrigatória", JOptionPane.WARNING_MESSAGE);

			txtAtividade.requestFocus();

			return;
		}

		if (modeloTabela.getRowCount() == 0) {

			JOptionPane.showMessageDialog(this, "Carregue os alunos antes de salvar as notas.", "Nenhum aluno",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (alunosSelecionados.size() != modeloTabela.getRowCount()) {

			JOptionPane.showMessageDialog(this,
					"A quantidade de alunos carregados não corresponde " + "à tabela de notas.", "Dados inconsistentes",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		List<Double> notas = new ArrayList<>();

		for (int i = 0; i < modeloTabela.getRowCount(); i++) {

			Object valor = modeloTabela.getValueAt(i, 2);

			if (valor == null || valor.toString().trim().isEmpty()) {

				JOptionPane.showMessageDialog(this, "Preencha todas as notas antes de salvar.", "Notas incompletas",
						JOptionPane.WARNING_MESSAGE);

				tabelaNotas.requestFocus();

				tabelaNotas.changeSelection(i, 2, false, false);

				return;
			}

			try {

				double nota = Double.parseDouble(valor.toString().trim().replace(",", "."));

				if (nota < 0 || nota > 10) {

					throw new NumberFormatException();
				}

				notas.add(nota);

			} catch (NumberFormatException e) {

				JOptionPane
						.showMessageDialog(this,
								"A nota do aluno " + modeloTabela.getValueAt(i, 1) + " é inválida.\n\n"
										+ "Informe um valor entre 0,0 e 10,0.",
								"Nota inválida", JOptionPane.WARNING_MESSAGE);

				tabelaNotas.changeSelection(i, 2, false, false);

				return;
			}
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			NotaDAO notaDAO = new NotaDAO(conn);

			LocalDate dataAtual = LocalDate.now();

			for (int i = 0; i < alunosSelecionados.size(); i++) {

				Aluno aluno = alunosSelecionados.get(i);

				double valorNota = notas.get(i);

				Nota notaExistente = notaDAO.buscarPorAlunoDisciplinaAtividade(aluno.getIdAluno(),
						idDisciplinaSelecionada, idTrimestreSelecionado, atividade);

				if (notaExistente == null) {

					Nota nota = new Nota();

					nota.setIdAluno(aluno.getIdAluno());

					nota.setIdDisciplina(idDisciplinaSelecionada);

					nota.setTrimestreId(idTrimestreSelecionado);

					nota.setAtividade(atividade);

					nota.setNota(valorNota);

					nota.setDataLancamento(dataAtual);

					notaDAO.inserir(nota);

				} else {

					notaExistente.setNota(valorNota);

					notaExistente.setDataLancamento(dataAtual);

					notaDAO.atualizar(notaExistente);
				}
			}

			JOptionPane.showMessageDialog(this, "Notas da atividade \"" + atividade + "\" salvas com sucesso!",
					"Sucesso", JOptionPane.INFORMATION_MESSAGE);

			atualizarResumo();

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível salvar as notas.\n\n" + e.getMessage(),
					"Erro ao salvar", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void salvarRecuperacao() {

		int linhaSelecionada = tabelaNotas.getSelectedRow();

		if (linhaSelecionada < 0) {

			JOptionPane.showMessageDialog(this,
					"Selecione exatamente um aluno na tabela " + "para lançar a recuperação.", "Aluno obrigatório",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (linhaSelecionada >= alunosSelecionados.size()) {

			JOptionPane.showMessageDialog(this, "O aluno selecionado não corresponde aos dados carregados.",
					"Dados inconsistentes", JOptionPane.WARNING_MESSAGE);

			return;
		}

		Object valor = modeloTabela.getValueAt(linhaSelecionada, 2);

		if (valor == null || valor.toString().trim().isEmpty()) {

			JOptionPane.showMessageDialog(this, "Informe a nota da recuperação para o aluno selecionado.",
					"Nota obrigatória", JOptionPane.WARNING_MESSAGE);

			tabelaNotas.changeSelection(linhaSelecionada, 2, false, false);

			return;
		}

		double notaRecuperacao;

		try {

			notaRecuperacao = Double.parseDouble(valor.toString().trim().replace(",", "."));

			if (notaRecuperacao < 0 || notaRecuperacao > 10) {

				throw new NumberFormatException();
			}

		} catch (NumberFormatException e) {

			JOptionPane.showMessageDialog(this,
					"A nota da recuperação é inválida.\n\n" + "Informe um valor entre 0,0 e 10,0.", "Nota inválida",
					JOptionPane.WARNING_MESSAGE);

			tabelaNotas.changeSelection(linhaSelecionada, 2, false, false);

			return;
		}

		Aluno aluno = alunosSelecionados.get(linhaSelecionada);

		try (Connection conn = ConnectionFactory.getConnection()) {

			NotaDAO notaDAO = new NotaDAO(conn);

			Nota menorNota = notaDAO.buscarMenorNotaPorAlunoDisciplinaTrimestre(aluno.getIdAluno(),
					idDisciplinaSelecionada, idTrimestreSelecionado);

			if (menorNota == null) {

				JOptionPane.showMessageDialog(this,
						"O aluno selecionado ainda não possui " + "notas nesta disciplina e trimestre.\n\n"
								+ "A recuperação não foi lançada.",
						"Recuperação não realizada", JOptionPane.INFORMATION_MESSAGE);

				return;
			}

			if (notaRecuperacao <= menorNota.getNota()) {

				JOptionPane.showMessageDialog(this,
						"A recuperação não foi lançada porque " + "a nota informada (" + formatarNota(notaRecuperacao)
								+ ") não é maior que a menor nota atual (" + formatarNota(menorNota.getNota()) + ").",
						"Recuperação não realizada", JOptionPane.INFORMATION_MESSAGE);

				return;
			}

			double notaAnterior = menorNota.getNota();

			menorNota.setNota(notaRecuperacao);

			menorNota.setDataLancamento(LocalDate.now());

			notaDAO.atualizar(menorNota);

			modeloTabela.setValueAt(formatarNota(notaRecuperacao), linhaSelecionada, 2);

			JOptionPane.showMessageDialog(this,
					"Recuperação lançada com sucesso!\n\n" + "Aluno: " + aluno.getNome() + "\n" + "Nota anterior: "
							+ formatarNota(notaAnterior) + "\n" + "Nota da recuperação: "
							+ formatarNota(notaRecuperacao),
					"Recuperação realizada", JOptionPane.INFORMATION_MESSAGE);

		} catch (SQLException | RuntimeException e) {

			JOptionPane.showMessageDialog(this, "Não foi possível lançar a recuperação.\n\n" + e.getMessage(),
					"Erro na recuperação", JOptionPane.ERROR_MESSAGE);
		}
	}

	private String montarAtividade() {

		String descricao = txtAtividade.getText().trim();

		if (descricao.isEmpty()) {
			return "";
		}

		String tipo = cbTipoNota.getSelectedItem() == null ? "" : cbTipoNota.getSelectedItem().toString().trim();

		if (tipo.isEmpty()) {
			return descricao;
		}

		return tipo + " - " + descricao;
	}

	private void limparNotas() {

		if (tabelaNotas.isEditing()) {

			tabelaNotas.getCellEditor().stopCellEditing();
		}

		for (int i = 0; i < modeloTabela.getRowCount(); i++) {

			modeloTabela.setValueAt("", i, 2);
		}

		tabelaNotas.clearSelection();
	}

	private void atualizarResumo() {

		if (lblResumoAlunos != null) {

			lblResumoAlunos.setText(String.valueOf(modeloTabela.getRowCount()));
		}
	}

	private String formatarNota(double nota) {

		return String.format(Locale.US, "%.1f", nota);
	}

	private JLabel obterLabelResumo(JPanel painel) {

		for (java.awt.Component componente : painel.getComponents()) {

			if (!(componente instanceof JPanel)) {
				continue;
			}

			JPanel card = (JPanel) componente;

			for (java.awt.Component filho : card.getComponents()) {

				if (filho instanceof JLabel) {

					JLabel label = (JLabel) filho;

					if (label.getFont().getSize() == 25) {
						return label;
					}
				}
			}
		}

		return null;
	}

	private void adicionarLabel(JPanel painel, String texto, int x, int y) {

		JLabel label = new JLabel(texto);

		label.setForeground(corLabel);

		label.setFont(new Font("Segoe UI", Font.BOLD, 16));

		label.setBounds(x, y, 180, 25);

		painel.add(label);
	}

	private JTextField criarCampoTexto() {

		JTextField campo = new JTextField();

		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		campo.setForeground(textos);

		campo.setCaretColor(textos);

		campo.setBackground(corCampo);

		campo.setBorder(new LineBorder(corBorda, 1, true));

		return campo;
	}

	private JComboBox<String> criarCombo(String[] itens) {

		JComboBox<String> combo = new JComboBox<>(itens);

		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		combo.setForeground(textos);

		combo.setBackground(corCampo);

		combo.setBorder(new LineBorder(corBorda, 1, true));

		combo.setFocusable(false);

		return combo;
	}

	private void adicionarResumo(JPanel painel, String titulo, String valor, int y) {

		JPanel card = criarPainelArredondado(corCampo, corBorda, 20);

		card.setLayout(null);

		card.setBounds(25, y, 210, 60);

		JLabel lbTitulo = new JLabel(titulo);

		lbTitulo.setForeground(textos);

		lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		lbTitulo.setBounds(15, 8, 150, 20);

		card.add(lbTitulo);

		JLabel lbValor = new JLabel(valor);

		lbValor.setForeground(corLabel);

		lbValor.setFont(new Font("Segoe UI", Font.BOLD, 25));

		lbValor.setBounds(15, 28, 150, 28);

		card.add(lbValor);

		painel.add(card);
	}

	private void estilizarBotao(JButton botao) {

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