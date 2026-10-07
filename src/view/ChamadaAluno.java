package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import controller.ChamadaController;
import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Chamada;
import model.ChamadaItem;
import model.Disciplina;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class ChamadaAluno extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	private JTable tabelaChamada;
	private DefaultTableModel modeloTabela;

	private JLabel lblTotalAlunos;
	private JLabel lblPresentes;
	private JLabel lblFaltas;
	private JLabel lblJustificadas;
	private JLabel lblAbonadas;

	private JTextField txtData;

	private JComboBox<String> cbTurma;
	private JComboBox<String> cbDisciplina;

	private final List<Turma> turmas = new ArrayList<>();
	private final List<Disciplina> disciplinas = new ArrayList<>();

	private int professorId;

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public ChamadaAluno() {

		setTitle("Chamada de Alunos");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int margem = 30;

		int larguraInterno = areaUtil.width - 60;

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

		int larguraResumo = 300;

		int margemLateral = 40;

		int larguraConteudo = larguraInterno - larguraResumo - (margemLateral * 3);

		JLabel titulo = new JLabel("Chamada de Alunos");

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));

		titulo.setHorizontalAlignment(SwingConstants.CENTER);

		titulo.setBounds(0, 45, larguraInterno, 55);

		interno.add(titulo);

		JLabel subtitulo = new JLabel("Registre presença, faltas, justificativas e faltas abonadas.");

		subtitulo.setForeground(textos);

		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));

		subtitulo.setHorizontalAlignment(SwingConstants.CENTER);

		subtitulo.setBounds(0, 105, larguraInterno, 30);

		interno.add(subtitulo);

		/*
		 * --------------------------------------------------------- FILTROS
		 * ---------------------------------------------------------
		 */

		JPanel filtros = criarPainelArredondado(corCampo, corBorda, 24);

		filtros.setLayout(null);

		filtros.setBounds(margemLateral, 160, larguraConteudo, 120);

		interno.add(filtros);

		int larguraConteudoFiltros = 1050;

		int inicioX = Math.max(30, (filtros.getWidth() - larguraConteudoFiltros) / 2);

		adicionarLabel(filtros, "Turma", inicioX, 20);

		cbTurma = criarCombo(new String[] { "Selecione a turma" });

		cbTurma.setBounds(inicioX, 55, 200, 38);

		filtros.add(cbTurma);

		cbTurma.addActionListener(e -> carregarDisciplinas());

		adicionarLabel(filtros, "Disciplina", inicioX + 225, 20);

		cbDisciplina = criarCombo(new String[] { "Selecione a disciplina" });

		cbDisciplina.setBounds(inicioX + 225, 55, 300, 38);

		filtros.add(cbDisciplina);

		adicionarLabel(filtros, "Data", inicioX + 550, 20);

		txtData = criarCampoTexto();

		txtData.setText(LocalDate.now().format(FORMATO_DATA));

		txtData.setBounds(inicioX + 550, 55, 150, 38);

		filtros.add(txtData);

		JButton btnCarregar = new JButton("Carregar alunos");

		btnCarregar.setBounds(inicioX + 725, 55, 190, 38);

		estilizarBotao(btnCarregar);

		btnCarregar.addActionListener(e -> carregarAlunos());

		filtros.add(btnCarregar);

		/*
		 * --------------------------------------------------------- PAINEL DA TABELA
		 * ---------------------------------------------------------
		 */

		JPanel tabelaPainel = criarPainelArredondado(corCampo, corBorda, 24);

		tabelaPainel.setLayout(null);

		tabelaPainel.setBounds(margemLateral, 305, larguraConteudo, alturaInterno - 365);

		interno.add(tabelaPainel);

		JLabel tituloTabela = new JLabel("Lista de Presença");

		tituloTabela.setForeground(textos);

		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 26));

		tituloTabela.setBounds(30, 20, 500, 35);

		tabelaPainel.add(tituloTabela);

		criarTabela();

		JScrollPane scroll = new JScrollPane(tabelaChamada);

		int larguraScroll = tabelaPainel.getWidth() - 60;

		int alturaScroll = tabelaPainel.getHeight() - 145;

		scroll.setBounds(30, 70, larguraScroll, alturaScroll);

		scroll.getViewport().setBackground(corInterna);

		scroll.setBorder(new LineBorder(corBorda, 1, true));

		scroll.getVerticalScrollBar().setUnitIncrement(26);

		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tabelaPainel.add(scroll);

		/*
		 * --------------------------------------------------------- BOTÕES
		 * ---------------------------------------------------------
		 */

		int larguraBotao = 180;

		int alturaBotao = 42;

		int espacamento = 20;

		int totalBotoes = 4;

		int larguraTotal = (larguraBotao * totalBotoes) + (espacamento * (totalBotoes - 1));

		int inicioBotoes = (tabelaPainel.getWidth() - larguraTotal) / 2;

		int yBotoes = scroll.getY() + scroll.getHeight() + 30;

		JButton btnTodosPresentes = new JButton("Todos presentes");

		btnTodosPresentes.setBounds(inicioBotoes, yBotoes, larguraBotao, alturaBotao);

		estilizarBotao(btnTodosPresentes);

		tabelaPainel.add(btnTodosPresentes);

		JButton btnSalvar = new JButton("Salvar chamada");

		btnSalvar.setBounds(inicioBotoes + (larguraBotao + espacamento), yBotoes, larguraBotao, alturaBotao);

		estilizarBotao(btnSalvar);

		tabelaPainel.add(btnSalvar);

		JButton btnLimpar = new JButton("Limpar");

		btnLimpar.setBounds(inicioBotoes + ((larguraBotao + espacamento) * 2), yBotoes, larguraBotao, alturaBotao);

		estilizarBotao(btnLimpar);

		tabelaPainel.add(btnLimpar);

		JButton btnCancelar = new JButton("Cancelar");

		btnCancelar.setBounds(inicioBotoes + ((larguraBotao + espacamento) * 3), yBotoes, larguraBotao, alturaBotao);

		estilizarBotao(btnCancelar);

		tabelaPainel.add(btnCancelar);

		/*
		 * --------------------------------------------------------- RESUMO
		 * ---------------------------------------------------------
		 */

		JPanel resumo = criarPainelArredondado(corCampo, corBorda, 28);

		resumo.setLayout(null);

		resumo.setBounds(larguraInterno - larguraResumo - margemLateral, 160, larguraResumo, alturaInterno - 300);

		interno.add(resumo);

		JLabel tituloResumo = new JLabel("Resumo");

		tituloResumo.setForeground(textos);

		tituloResumo.setFont(new Font("Segoe UI", Font.BOLD, 28));

		tituloResumo.setBounds(25, 25, 230, 35);

		resumo.add(tituloResumo);

		JPanel linha = new JPanel();

		linha.setBackground(corLabel);

		linha.setBounds(25, 70, 230, 3);

		resumo.add(linha);

		lblTotalAlunos = adicionarResumo(resumo, "Alunos", "0", 105);

		lblPresentes = adicionarResumo(resumo, "Presentes", "0", 180);

		lblFaltas = adicionarResumo(resumo, "Faltas", "0", 255);

		lblJustificadas = adicionarResumo(resumo, "Faltas justificadas", "0", 330);

		lblAbonadas = adicionarResumo(resumo, "Faltas abonadas", "0", 405);

		JLabel dica = new JLabel(
				"<html>" + "Presente marcado = presença.<br><br>" + "Presente desmarcado = falta.<br><br>"
						+ "Se marcar falta abonada, " + "o motivo será obrigatório." + "</html>");

		dica.setForeground(textos);

		dica.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		dica.setBounds(25, 480, 245, 130);

		resumo.add(dica);

		/*
		 * --------------------------------------------------------- AÇÕES
		 * ---------------------------------------------------------
		 */

		btnTodosPresentes.addActionListener(e -> marcarTodosPresentes());

		btnSalvar.addActionListener(e -> salvarChamada());

		btnLimpar.addActionListener(e -> limparChamada());

		btnCancelar.addActionListener(e -> dispose());

		carregarDadosProfessor();

		atualizarResumo();

		setVisible(true);
	}

	private void carregarDadosProfessor() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getProfessorId() <= 0) {

			mostrarErro("Nenhum professor está autenticado.");

			return;
		}

		professorId = usuario.getProfessorId();

		try (Connection conn = ConnectionFactory.getConnection()) {

			turmas.clear();

			turmas.addAll(new TurmaDAO(conn).listarPorProfessor(professorId));

			cbTurma.removeAllItems();

			cbTurma.addItem("Selecione a turma");

			for (Turma turma : turmas) {

				cbTurma.addItem(turma.getDescricaoTurma());
			}

		} catch (SQLException | RuntimeException ex) {

			mostrarErro("Não foi possível carregar as turmas " + "do professor:\n\n" + ex.getMessage());
		}
	}

	private void carregarDisciplinas() {

		cbDisciplina.removeAllItems();

		cbDisciplina.addItem("Selecione a disciplina");

		disciplinas.clear();

		Turma turma = turmaSelecionada();

		if (turma == null) {
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			disciplinas.addAll(new DisciplinaDAO(conn).listarPorProfessorETurma(professorId, turma.getIdTurma()));

			for (Disciplina disciplina : disciplinas) {

				cbDisciplina.addItem(disciplina.getDescricao());
			}

		} catch (SQLException | RuntimeException ex) {

			mostrarErro("Não foi possível carregar as disciplinas:\n\n" + ex.getMessage());
		}
	}

	private void carregarAlunos() {

		Turma turma = turmaSelecionada();

		Disciplina disciplina = disciplinaSelecionada();

		if (turma == null) {

			mostrarErro("Selecione uma turma.");

			return;
		}

		if (disciplina == null) {

			mostrarErro("Selecione uma disciplina.");

			return;
		}

		LocalDate data;

		try {

			data = obterDataInformada();

		} catch (DateTimeParseException ex) {

			mostrarErro("Informe uma data válida no formato " + "dd/MM/aaaa.");

			txtData.requestFocus();

			return;
		}

		if (data.isAfter(LocalDate.now())) {

			mostrarErro("A data da chamada não pode ser futura.");

			txtData.requestFocus();

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			List<Aluno> alunos = new AlunoDAO(conn).listarPorTurma(turma.getIdTurma());

			modeloTabela.setRowCount(0);

			for (Aluno aluno : alunos) {

				/*
				 * Regra:
				 *
				 * Presente = false Falta just. = false Falta abonada = false Motivo = vazio
				 *
				 * Portanto, todos começam como falta normal até que o professor marque
				 * Presente.
				 */
				modeloTabela.addRow(new Object[] { aluno.getMatricula(), aluno.getNome(), false, false, false, "" });
			}

			atualizarResumo();

			if (alunos.isEmpty()) {

				JOptionPane.showMessageDialog(this, "Não existem alunos cadastrados " + "nesta turma.", "Nenhum aluno",
						JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (SQLException | RuntimeException ex) {

			mostrarErro("Não foi possível carregar os alunos " + "da turma:\n\n" + ex.getMessage());
		}
	}

	private void salvarChamada() {

		Turma turma = turmaSelecionada();

		Disciplina disciplina = disciplinaSelecionada();

		if (turma == null) {

			mostrarErro("Selecione uma turma.");

			return;
		}

		if (disciplina == null) {

			mostrarErro("Selecione uma disciplina.");

			return;
		}

		if (modeloTabela.getRowCount() == 0) {

			mostrarErro("Carregue os alunos antes de salvar " + "a chamada.");

			return;
		}

		LocalDate data;

		try {

			data = obterDataInformada();

		} catch (DateTimeParseException ex) {

			mostrarErro("Informe uma data válida no formato " + "dd/MM/aaaa.");

			txtData.requestFocus();

			return;
		}

		if (data.isAfter(LocalDate.now())) {

			mostrarErro("A data da chamada não pode ser futura.");

			txtData.requestFocus();

			return;
		}

		/*
		 * --------------------------------------------------------- VALIDAR FALTAS
		 * ABONADAS ---------------------------------------------------------
		 */

		for (int i = 0; i < modeloTabela.getRowCount(); i++) {

			boolean faltaAbonada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 4));

			if (!faltaAbonada) {
				continue;
			}

			String motivo = modeloTabela.getValueAt(i, 5).toString().trim();

			if (motivo.isEmpty()) {

				mostrarErro("Informe o motivo da falta abonada " + "para o aluno:\n\n" + modeloTabela.getValueAt(i, 1));

				tabelaChamada.requestFocus();

				tabelaChamada.changeSelection(i, 5, false, false);

				return;
			}
		}

		try {

			Chamada chamada = new Chamada();

			chamada.setProfessorId(professorId);

			chamada.setTurmaId(turma.getIdTurma());

			chamada.setDisciplinaId(disciplina.getIdDisciplina());

			chamada.setData(data);

			List<ChamadaItem> itens = new ArrayList<>();

			try (Connection conn = ConnectionFactory.getConnection()) {

				List<Aluno> alunos = new AlunoDAO(conn).listarPorTurma(turma.getIdTurma());

				if (alunos.size() != modeloTabela.getRowCount()) {

					throw new IllegalStateException("A quantidade de alunos da turma " + "mudou. Carregue os alunos "
							+ "novamente antes de salvar.");
				}

				for (int i = 0; i < modeloTabela.getRowCount(); i++) {

					Aluno aluno = alunos.get(i);

					boolean presente = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 2));

					boolean faltaJustificada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 3));

					boolean faltaAbonada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 4));

					String motivoAbonada = String.valueOf(modeloTabela.getValueAt(i, 5)).trim();

					ChamadaItem item = new ChamadaItem(aluno.getIdAluno(), aluno.getNome());

					item.setPresente(presente);

					item.setFaltaJustificada(faltaJustificada);

					item.setFaltaAbonada(faltaAbonada);

					item.setMotivoAbonada(faltaAbonada ? motivoAbonada : null);

					itens.add(item);
				}
			}

			chamada.setItens(itens);

			new ChamadaController().salvarChamada(chamada);

			JOptionPane.showMessageDialog(this,
					"Chamada lançada com sucesso!\n\n" + "Data: " + data.format(FORMATO_DATA) + "\nTurma: "
							+ turma.getDescricaoTurma() + "\nDisciplina: " + disciplina.getDescricao(),
					"Chamada salva", JOptionPane.INFORMATION_MESSAGE);

			limparTabelaDepoisDoSalvar();

		} catch (SQLException ex) {

			mostrarErro("Não foi possível acessar o banco de dados:\n\n" + ex.getMessage());

		} catch (RuntimeException ex) {

			mostrarErro("Não foi possível salvar a chamada:\n\n" + ex.getMessage());
		}
	}

	private LocalDate obterDataInformada() throws DateTimeParseException {

		String texto = txtData.getText() == null ? "" : txtData.getText().trim();

		if (texto.isEmpty()) {

			throw new DateTimeParseException("Data vazia.", texto, 0);
		}

		return LocalDate.parse(texto, FORMATO_DATA);
	}

	private void marcarTodosPresentes() {

		for (int i = 0; i < modeloTabela.getRowCount(); i++) {

			modeloTabela.setValueAt(true, i, 2);

			modeloTabela.setValueAt(false, i, 3);

			modeloTabela.setValueAt(false, i, 4);

			modeloTabela.setValueAt("", i, 5);
		}

		atualizarResumo();
	}

	private void limparChamada() {

		modeloTabela.setRowCount(0);

		atualizarResumo();
	}

	private void limparTabelaDepoisDoSalvar() {

		modeloTabela.setRowCount(0);

		atualizarResumo();
	}

	private Turma turmaSelecionada() {

		int indice = cbTurma.getSelectedIndex();

		if (indice <= 0 || indice > turmas.size()) {

			return null;
		}

		return turmas.get(indice - 1);
	}

	private Disciplina disciplinaSelecionada() {

		int indice = cbDisciplina.getSelectedIndex();

		if (indice <= 0 || indice > disciplinas.size()) {

			return null;
		}

		return disciplinas.get(indice - 1);
	}

	private void atualizarResumo() {

		if (modeloTabela == null || lblTotalAlunos == null) {

			return;
		}

		int total = modeloTabela.getRowCount();

		int presentes = 0;

		int faltas = 0;

		int justificadas = 0;

		int abonadas = 0;

		for (int i = 0; i < total; i++) {

			boolean presente = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 2));

			boolean justificada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 3));

			boolean abonada = Boolean.TRUE.equals(modeloTabela.getValueAt(i, 4));

			if (presente) {

				presentes++;

			} else {

				faltas++;
			}

			if (justificada) {

				justificadas++;
			}

			if (abonada) {

				abonadas++;
			}
		}

		lblTotalAlunos.setText(String.valueOf(total));

		lblPresentes.setText(String.valueOf(presentes));

		lblFaltas.setText(String.valueOf(faltas));

		lblJustificadas.setText(String.valueOf(justificadas));

		lblAbonadas.setText(String.valueOf(abonadas));
	}

	private void adicionarLabel(JPanel painel, String texto, int x, int y) {

		JLabel label = new JLabel(texto);

		label.setForeground(corLabel);

		label.setFont(new Font("Segoe UI", Font.BOLD, 16));

		label.setBounds(x, y, 200, 25);

		painel.add(label);
	}

	private JTextField criarCampoTexto() {

		JTextField campo = new JTextField();

		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		campo.setForeground(textos);

		campo.setBackground(corCampo);

		campo.setCaretColor(textos);

		campo.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));

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

	private void criarTabela() {

		String[] colunas = { "Matrícula", "Aluno", "Presente", "Falta Justificada", "Falta Abonada",
				"Motivo Falta Abonada" };

		modeloTabela = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {

				return column >= 2;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {

				if (columnIndex >= 2 && columnIndex <= 4) {

					return Boolean.class;
				}

				return String.class;
			}

			@Override
			public void setValueAt(Object valor, int row, int column) {

				super.setValueAt(valor, row, column);

				/*
				 * Presente marcado: limpa as duas classificações de falta e o motivo.
				 */
				if (column == 2 && Boolean.TRUE.equals(valor)) {

					super.setValueAt(false, row, 3);

					super.setValueAt(false, row, 4);

					super.setValueAt("", row, 5);
				}

				/*
				 * Falta justificada: presente deve ficar falso e falta abonada deve ficar
				 * falsa.
				 */
				if (column == 3 && Boolean.TRUE.equals(valor)) {

					super.setValueAt(false, row, 2);

					super.setValueAt(false, row, 4);

					super.setValueAt("", row, 5);
				}

				/*
				 * Falta abonada: presente deve ficar falso e justificada deve ficar falsa.
				 *
				 * O motivo permanece disponível para preenchimento.
				 */
				if (column == 4 && Boolean.TRUE.equals(valor)) {

					super.setValueAt(false, row, 2);

					super.setValueAt(false, row, 3);
				}

				atualizarResumo();
			}
		};

		tabelaChamada = new JTable(modeloTabela);

		tabelaChamada.setRowHeight(50);

		tabelaChamada.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		tabelaChamada.setForeground(textos);

		tabelaChamada.setBackground(corInterna);

		tabelaChamada.setGridColor(new Color(90, 50, 170));

		tabelaChamada.setSelectionBackground(new Color(80, 40, 160));

		tabelaChamada.setSelectionForeground(textos);

		tabelaChamada.setShowGrid(true);

		tabelaChamada.setShowHorizontalLines(true);

		tabelaChamada.setShowVerticalLines(true);

		tabelaChamada.setIntercellSpacing(new Dimension(1, 1));

		tabelaChamada.setFillsViewportHeight(false);

		tabelaChamada.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaChamada.getTableHeader();

		header.setFont(new Font("Segoe UI", Font.BOLD, 15));

		header.setBackground(corCampo);

		header.setForeground(textos);

		header.setReorderingAllowed(false);

		header.setResizingAllowed(false);

		header.setPreferredSize(new Dimension(header.getWidth(), 34));

		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();

		centro.setHorizontalAlignment(SwingConstants.CENTER);

		centro.setVerticalAlignment(SwingConstants.CENTER);

		centro.setBackground(new Color(31, 10, 90));

		centro.setForeground(textos);

		centro.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		tabelaChamada.getColumnModel().getColumn(0).setCellRenderer(centro);

		tabelaChamada.getColumnModel().getColumn(1).setCellRenderer(centro);

		DefaultTableCellRenderer rendererCheckbox = new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				JCheckBox checkbox = new JCheckBox();

				checkbox.setHorizontalAlignment(SwingConstants.CENTER);

				checkbox.setSelected(Boolean.TRUE.equals(value));

				checkbox.setBackground(isSelected ? new Color(80, 40, 160) : new Color(31, 10, 90));

				checkbox.setForeground(textos);

				checkbox.setFocusPainted(false);

				return checkbox;
			}
		};

		for (int i = 2; i <= 4; i++) {

			tabelaChamada.getColumnModel().getColumn(i).setCellRenderer(rendererCheckbox);

			JCheckBox editor = new JCheckBox();

			editor.setHorizontalAlignment(SwingConstants.CENTER);

			editor.setBackground(corInterna);

			editor.setForeground(textos);

			editor.setFocusPainted(false);

			tabelaChamada.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(editor));
		}

		JTextField campoMotivo = new JTextField();

		campoMotivo.setHorizontalAlignment(JTextField.LEFT);

		campoMotivo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		campoMotivo.setForeground(textos);

		campoMotivo.setCaretColor(textos);

		campoMotivo.setBackground(new Color(31, 10, 90));

		campoMotivo.setBorder(new LineBorder(corLabel, 1, true));

		tabelaChamada.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(campoMotivo));

		tabelaChamada.getColumnModel().getColumn(5).setCellRenderer(centro);

		tabelaChamada.getColumnModel().getColumn(0).setPreferredWidth(140);

		tabelaChamada.getColumnModel().getColumn(1).setPreferredWidth(360);

		tabelaChamada.getColumnModel().getColumn(2).setPreferredWidth(130);

		tabelaChamada.getColumnModel().getColumn(3).setPreferredWidth(180);

		tabelaChamada.getColumnModel().getColumn(4).setPreferredWidth(170);

		tabelaChamada.getColumnModel().getColumn(5).setPreferredWidth(350);
	}

	private JLabel adicionarResumo(JPanel painel, String titulo, String valor, int y) {

		JPanel card = criarPainelArredondado(corCampo, corBorda, 20);

		card.setLayout(null);

		card.setBounds(25, y, 245, 55);

		JLabel lbTitulo = new JLabel(titulo);

		lbTitulo.setForeground(textos);

		lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		lbTitulo.setBounds(15, 7, 190, 18);

		card.add(lbTitulo);

		JLabel lbValor = new JLabel(valor);

		lbValor.setForeground(corLabel);

		lbValor.setFont(new Font("Segoe UI", Font.BOLD, 24));

		lbValor.setBounds(15, 25, 150, 25);

		card.add(lbValor);

		painel.add(card);

		return lbValor;
	}

	private void estilizarBotao(JButton botao) {

		botao.setFont(new Font("Segoe UI", Font.BOLD, 15));

		botao.setForeground(textos);

		botao.setBackground(corCampo);

		botao.setBorder(new LineBorder(corBorda, 1, true));

		botao.setFocusPainted(false);

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void mostrarErro(String mensagem) {

		JOptionPane.showMessageDialog(this, mensagem == null ? "Não foi possível realizar a operação." : mensagem,
				"Erro", JOptionPane.ERROR_MESSAGE);
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