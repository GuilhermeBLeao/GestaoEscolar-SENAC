package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import controller.PresencaController;
import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Disciplina;
import model.Presenca;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class Frequencia extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private JTable tabelaFaltas;
	private DefaultTableModel modeloTabela;
	private JComboBox<String> comboDisciplina;
	private JComboBox<String> comboSituacao;
	private JTextField campoDataInicial;
	private JTextField campoDataFinal;
	private JLabel valorAluno;
	private JLabel valorMatricula;
	private JLabel valorTurma;
	private JLabel valorCurso;
	private JLabel valorTurno;
	private JLabel valorTotalFaltas;
	private JLabel valorFaltasJustificadas;
	private JLabel valorFaltasNaoJustificadas;
	private JLabel valorPresencas;
	private final List<Presenca> presencas = new ArrayList<>();
	private final Map<String, Integer> disciplinasPorDescricao = new HashMap<>();
	private final Map<Integer, String> professoresPorDisciplina = new HashMap<>();

	public Frequencia() {
		setTitle("Minhas Faltas - Aluno");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int margem = 30;
		int larguraInterno = areaUtil.width - (margem * 2);
		int alturaInterno = areaUtil.height - (margem * 2);

		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1280, 720));
		setResizable(false);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);

		JPanel interno = new JPanel(null);
		interno.setBounds(20, 20, larguraInterno, alturaInterno);
		interno.setBackground(corInterna);
		externo.add(interno);

		criarConteudo(interno, larguraInterno, alturaInterno);
	}

	private void criarConteudo(JPanel interno, int larguraInterno, int alturaInterno) {
		JPanel topo = criarTopo();
		topo.setBounds(30, 25, larguraInterno - 60, 160);
		interno.add(topo);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(35, 35, 150, 40);
		estilizarBotao(btnVoltar);
		btnVoltar.addActionListener(e -> dispose());
		topo.add(btnVoltar);

		JPanel painelConteudo = new JPanel(null);
		painelConteudo.setBackground(corInterna);

		int larguraConteudo = 1220;
		int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

		if (xInicial < 0) {
			xInicial = 0;
		}

		painelConteudo.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
		interno.add(painelConteudo);

		JPanel cardResumo = criarPainelArredondado();
		cardResumo.setLayout(null);
		cardResumo.setBounds(xInicial, 0, 1220, 170);
		painelConteudo.add(cardResumo);

		criarResumo(cardResumo);

		JPanel cardFiltros = criarPainelArredondado();
		cardFiltros.setLayout(null);
		cardFiltros.setBounds(xInicial, 180, 1220, 115);
		painelConteudo.add(cardFiltros);

		criarFiltros(cardFiltros);

		JPanel cardTabela = criarPainelArredondado();
		cardTabela.setLayout(null);
		cardTabela.setBounds(xInicial, 305, 1220, 550);
		painelConteudo.add(cardTabela);

		JLabel tituloTabela = new JLabel("Registro de Frequência");
		tituloTabela.setForeground(corLabel);
		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 25));
		tituloTabela.setBounds(30, 20, 400, 35);
		cardTabela.add(tituloTabela);

		JLabel subtituloTabela = new JLabel("Consulte suas presenças, faltas e justificativas registradas pelos professores.");
		subtituloTabela.setForeground(textos);
		subtituloTabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		subtituloTabela.setBounds(32, 56, 950, 25);
		cardTabela.add(subtituloTabela);

		criarTabela();

		JScrollPane scrollTabela = new JScrollPane(tabelaFaltas);
		scrollTabela.setBounds(30, 95, 1160, 422);
		scrollTabela.setBorder(new LineBorder(corBorda));
		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
		scrollTabela.getViewport().setBackground(corCampo);
		cardTabela.add(scrollTabela);

		carregarDadosAluno();
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(),
						new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Minhas Faltas");
		titulo.setForeground(Color.WHITE);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 80, 550, 45);
		topo.add(titulo);

		JLabel sub = new JLabel("Consulte suas presenças, faltas e faltas justificadas registradas pelos professores.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(42, 120, 950, 25);
		topo.add(sub);
		return topo;
	}

	private void criarResumo(JPanel painel) {
		valorAluno = adicionarResumo(painel, "Aluno", "Carregando...", 30, 20);
		valorMatricula = adicionarResumo(painel, "Matrícula", "Carregando...", 300, 20);
		valorTurma = adicionarResumo(painel, "Turma", "Carregando...", 520, 20);
		valorCurso = adicionarResumo(painel, "Curso", "Não informado", 680, 20);
		valorTurno = adicionarResumo(painel, "Turno", "Carregando...", 930, 20);
		valorTotalFaltas = adicionarIndicador(painel, "Total de faltas", "0", 30, 95, new Color(255, 170, 90));
		valorFaltasJustificadas = adicionarIndicador(painel, "Faltas justificadas", "0", 280, 95,
				new Color(120, 200, 255));
		valorFaltasNaoJustificadas = adicionarIndicador(painel, "Faltas não justificadas", "0", 570, 95,
				new Color(255, 100, 120));
		valorPresencas = adicionarIndicador(painel, "Presenças registradas", "0", 900, 95, new Color(120, 255, 170));
	}

	private JLabel adicionarResumo(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblTitulo.setBounds(x, y, 200, 22);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(Color.WHITE);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblValor.setBounds(x, y + 24, 260, 28);
		painel.add(lblValor);
		return lblValor;
	}

	private JLabel adicionarIndicador(JPanel painel, String titulo, String valor, int x, int y, Color corValor) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(textos);
		lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblTitulo.setBounds(x, y, 230, 22);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(corValor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblValor.setBounds(x, y + 25, 160, 35);
		painel.add(lblValor);
		return lblValor;
	}

	private void criarFiltros(JPanel painel) {
		JLabel titulo = new JLabel("Filtros");
		titulo.setForeground(corLabel);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
		titulo.setBounds(30, 12, 100, 25);
		painel.add(titulo);

		JLabel lblDisciplina = criarLabelFiltro("Disciplina", 30, 48);
		painel.add(lblDisciplina);

		comboDisciplina = new JComboBox<>();
		comboDisciplina.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboDisciplina.setBackground(corCampo);
		comboDisciplina.setForeground(Color.WHITE);
		comboDisciplina.setBorder(new LineBorder(corBorda));
		comboDisciplina.setBounds(30, 72, 250, 32);
		painel.add(comboDisciplina);

		JLabel lblSituacao = criarLabelFiltro("Situação", 300, 48);
		painel.add(lblSituacao);

		comboSituacao = new JComboBox<>(new String[] { "Todas", "Presente", "Falta", "Falta justificada",
				"Falta abonada", "Falta não justificada" });
		comboSituacao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboSituacao.setBackground(corCampo);
		comboSituacao.setForeground(Color.WHITE);
		comboSituacao.setBorder(new LineBorder(corBorda));
		comboSituacao.setBounds(300, 72, 220, 32);
		painel.add(comboSituacao);

		JLabel lblDataInicial = criarLabelFiltro("Data inicial", 540, 48);
		painel.add(lblDataInicial);
		campoDataInicial = criarCampoData(540, 72);
		painel.add(campoDataInicial);

		JLabel lblDataFinal = criarLabelFiltro("Data final", 700, 48);
		painel.add(lblDataFinal);
		campoDataFinal = criarCampoData(700, 72);
		painel.add(campoDataFinal);

		JButton btnAplicar = new JButton("Aplicar filtros");
		btnAplicar.setBounds(865, 70, 150, 36);
		estilizarBotao(btnAplicar);
		btnAplicar.addActionListener(e -> aplicarFiltros());
		painel.add(btnAplicar);

		JButton btnLimpar = new JButton("Limpar filtros");
		btnLimpar.setBounds(1030, 70, 160, 36);
		estilizarBotao(btnLimpar);
		btnLimpar.addActionListener(e -> limparFiltros());
		painel.add(btnLimpar);
	}

	private JLabel criarLabelFiltro(String texto, int x, int y) {
		JLabel label = new JLabel(texto);
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Segoe UI", Font.BOLD, 13));
		label.setBounds(x, y, 150, 20);
		return label;
	}

	private JTextField criarCampoData(int x, int y) {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		campo.setForeground(Color.WHITE);
		campo.setBackground(corCampo);
		campo.setCaretColor(Color.WHITE);
		campo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(corBorda),
				BorderFactory.createEmptyBorder(0, 8, 0, 8)));
		campo.setToolTipText("Formato: dd/MM/yyyy");
		campo.setBounds(x, y, 145, 32);
		return campo;
	}

	private void criarTabela() {
		String[] colunas = { "Data", "Disciplina", "Professor", "Aula", "Registro", "Justificativa", "Situação" };
		modeloTabela = new DefaultTableModel(colunas, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tabelaFaltas = new JTable(modeloTabela);
		tabelaFaltas.setRowHeight(42);
		tabelaFaltas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tabelaFaltas.setForeground(Color.WHITE);
		tabelaFaltas.setBackground(new Color(25, 8, 80));
		tabelaFaltas.setGridColor(corBorda);
		tabelaFaltas.setSelectionBackground(new Color(120, 40, 220));
		tabelaFaltas.setSelectionForeground(Color.WHITE);
		tabelaFaltas.setShowGrid(true);
		tabelaFaltas.setShowVerticalLines(true);
		tabelaFaltas.setShowHorizontalLines(true);
		tabelaFaltas.setRowSelectionAllowed(true);
		tabelaFaltas.setFillsViewportHeight(true);

		JTableHeader header = tabelaFaltas.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 15));
		header.setForeground(Color.WHITE);
		header.setBackground(new Color(80, 25, 150));
		header.setPreferredSize(new Dimension(header.getWidth(), 42));
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
		centro.setHorizontalAlignment(JLabel.CENTER);
		centro.setVerticalAlignment(JLabel.CENTER);
		centro.setForeground(Color.WHITE);
		centro.setBackground(new Color(25, 8, 80));

		for (int i = 0; i < tabelaFaltas.getColumnCount(); i++) {
			tabelaFaltas.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		tabelaFaltas.getColumnModel().getColumn(0).setPreferredWidth(100);
		tabelaFaltas.getColumnModel().getColumn(1).setPreferredWidth(170);
		tabelaFaltas.getColumnModel().getColumn(2).setPreferredWidth(210);
		tabelaFaltas.getColumnModel().getColumn(3).setPreferredWidth(120);
		tabelaFaltas.getColumnModel().getColumn(4).setPreferredWidth(100);
		tabelaFaltas.getColumnModel().getColumn(5).setPreferredWidth(260);
		tabelaFaltas.getColumnModel().getColumn(6).setPreferredWidth(150);
	}

	private void carregarDadosAluno() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getAlunoId() <= 0) {
			JOptionPane.showMessageDialog(this, "Não foi possível identificar o aluno logado.", "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

			Aluno aluno = alunoDAO.buscarPorId(usuario.getAlunoId());

			if (aluno == null) {
				throw new IllegalArgumentException("Aluno vinculado ao usuário não foi encontrado.");
			}

			preencherDadosAluno(aluno, turmaDAO);
			carregarDisciplinas(aluno.getIdAluno(), disciplinaDAO);

			PresencaController presencaController = new PresencaController();

			presencas.clear();
			presencas.addAll(presencaController.listarPresencasPorAluno(aluno.getIdAluno()));

			atualizarResumo(presencas);
			preencherTabela(presencas);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Não foi possível carregar a frequência: " + obterMensagemErro(ex),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void preencherDadosAluno(Aluno aluno, TurmaDAO turmaDAO) throws SQLException {
		valorAluno.setText(aluno.getNome() == null || aluno.getNome().isBlank() ? "Não informado" : aluno.getNome());
		valorMatricula.setText(aluno.getMatricula() == null || aluno.getMatricula().isBlank() ? "Não informado"
				: aluno.getMatricula());

		if (aluno.getIdTurma() > 0) {
			Turma turma = turmaDAO.buscarPorId(aluno.getIdTurma());
			if (turma != null) {
				valorTurma.setText(turma.getDescricaoTurma() == null ? "Não informado" : turma.getDescricaoTurma());
				valorTurno.setText(turma.getTurno() == null ? "Não informado" : formatarTurno(turma.getTurno().name()));
			} else {
				valorTurma.setText("Não informado");
				valorTurno.setText("Não informado");
			}
		} else {
			valorTurma.setText("Não informado");
			valorTurno.setText("Não informado");
		}
		valorCurso.setText("Não informado");
	}

	private void carregarDisciplinas(int alunoId, DisciplinaDAO disciplinaDAO) throws SQLException {
		comboDisciplina.removeAllItems();
		disciplinasPorDescricao.clear();
		professoresPorDisciplina.clear();
		comboDisciplina.addItem("Todas");
		List<Object[]> detalhes = disciplinaDAO.listarDetalhesPorAluno(alunoId);

		for (Object[] detalhe : detalhes) {
			if (detalhe == null || detalhe.length < 3) {
				continue;
			}

			String descricao = detalhe[1] == null ? "Não informado" : detalhe[1].toString();

			int codigo = detalhe[0] instanceof Number ? ((Number) detalhe[0]).intValue() : 0;

			@SuppressWarnings("unused")
			String professores = detalhe[2] == null ? "Não informado" : detalhe[2].toString();
			comboDisciplina.addItem(descricao);
			disciplinasPorDescricao.put(descricao, codigo);
		}

		if (detalhes.isEmpty()) {
			return;
		}
		for (Object[] detalhe : detalhes) {
			if (detalhe == null || detalhe.length < 2) {
				continue;
			}
			String descricao = detalhe[1] == null ? "Não informado" : detalhe[1].toString();
			Disciplina disciplina = disciplinaDAO.buscarPorDescricao(descricao);

			if (disciplina != null) {
				String professores = detalhe.length > 2 && detalhe[2] != null ? detalhe[2].toString() : "Não informado";
				disciplinasPorDescricao.put(descricao, disciplina.getIdDisciplina());
				professoresPorDisciplina.put(disciplina.getIdDisciplina(), professores);
			}
		}
	}

	private void preencherTabela(List<Presenca> registros) {
		modeloTabela.setRowCount(0);

		for (Presenca presenca : registros) {
			adicionarLinhaTabela(presenca);
		}
	}

	private void adicionarLinhaTabela(Presenca presenca) {
		String registro = presenca.isPresente() ? "Presente" : "Falta";
		String situacao;

		if (presenca.isFaltaAbonada()) {
			situacao = "Falta abonada";
		} else if (presenca.isFaltaJustificada()) {
			situacao = "Falta justificada";
		} else if (!presenca.isPresente()) {
			situacao = "Falta não justificada";
		} else {
			situacao = "Presente";
		}

		String justificativa = presenca.getMotivoAbonada();

		if (justificativa == null || justificativa.isBlank()) {
			if (presenca.isFaltaJustificada()) {
				justificativa = "Falta justificada";
			} else if (presenca.isFaltaAbonada()) {
				justificativa = "Falta abonada";
			} else {
				justificativa = "-";
			}
		}

		String professor = professoresPorDisciplina.get(presenca.getDisciplinaId());

		if (professor == null || professor.isBlank()) {
			professor = "Não informado";
		}

		String disciplina = buscarDescricaoDisciplina(presenca.getDisciplinaId());
		modeloTabela.addRow(new Object[] { presenca.getData().format(FORMATO_DATA), disciplina, professor,
				"Não informado", registro, justificativa, situacao });
	}

	private String buscarDescricaoDisciplina(int disciplinaId) {
		for (Map.Entry<String, Integer> entrada : disciplinasPorDescricao.entrySet()) {
			if (entrada.getValue() != null && entrada.getValue() == disciplinaId) {
				return entrada.getKey();
			}
		}
		return "Não informado";
	}

	private void atualizarResumo(List<Presenca> registros) {
		int totalFaltas = 0;
		int faltasJustificadas = 0;
		int faltasNaoJustificadas = 0;
		int presencasRegistradas = 0;

		for (Presenca presenca : registros) {
			if (presenca.isPresente()) {
				presencasRegistradas++;
				continue;
			}
			totalFaltas++;

			if (presenca.isFaltaJustificada() || presenca.isFaltaAbonada()) {
				faltasJustificadas++;
			} else {
				faltasNaoJustificadas++;
			}
		}
		valorTotalFaltas.setText(String.valueOf(totalFaltas));
		valorFaltasJustificadas.setText(String.valueOf(faltasJustificadas));
		valorFaltasNaoJustificadas.setText(String.valueOf(faltasNaoJustificadas));
		valorPresencas.setText(String.valueOf(presencasRegistradas));
	}

	private void aplicarFiltros() {
		try {
			Integer disciplinaSelecionada = obterDisciplinaSelecionada();
			String situacaoSelecionada = comboSituacao.getSelectedItem() == null ? "Todas"
					: comboSituacao.getSelectedItem().toString();
			LocalDate dataInicial = obterData(campoDataInicial.getText());
			LocalDate dataFinal = obterData(campoDataFinal.getText());

			if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
				JOptionPane.showMessageDialog(this, "A data inicial não pode ser posterior à data final.",
						"Filtro inválido", JOptionPane.WARNING_MESSAGE);
				return;
			}
			List<Presenca> filtradas = new ArrayList<>();

			for (Presenca presenca : presencas) {
				if (disciplinaSelecionada != null && presenca.getDisciplinaId() != disciplinaSelecionada) {
					continue;
				}
				if (!correspondeSituacao(presenca, situacaoSelecionada)) {
					continue;
				}
				if (dataInicial != null && presenca.getData().isBefore(dataInicial)) {
					continue;
				}
				if (dataFinal != null && presenca.getData().isAfter(dataFinal)) {
					continue;
				}
				filtradas.add(presenca);
			}
			preencherTabela(filtradas);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Filtro inválido", JOptionPane.WARNING_MESSAGE);
		}
	}

	private Integer obterDisciplinaSelecionada() {
		Object selecionada = comboDisciplina.getSelectedItem();

		if (selecionada == null || "Todas".equals(selecionada.toString())) {
			return null;
		}

		String descricao = selecionada.toString();

		Integer idDisciplina = disciplinasPorDescricao.get(descricao);

		if (idDisciplina == null || idDisciplina <= 0) {
			throw new IllegalArgumentException("Não foi possível identificar a disciplina selecionada.");
		}
		return idDisciplina;
	}

	private boolean correspondeSituacao(Presenca presenca, String situacao) {
		switch (situacao) {
		case "Presente":
			return presenca.isPresente();
		case "Falta":
			return !presenca.isPresente();
		case "Falta justificada":
			return !presenca.isPresente() && presenca.isFaltaJustificada();
		case "Falta abonada":
			return !presenca.isPresente() && presenca.isFaltaAbonada();
		case "Falta não justificada":
			return !presenca.isPresente() && !presenca.isFaltaJustificada() && !presenca.isFaltaAbonada();
		case "Todas":
		default:
			return true;
		}
	}

	private LocalDate obterData(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(texto.trim(), FORMATO_DATA);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException("Data inválida. Utilize o formato dd/MM/yyyy.");
		}
	}

	private void limparFiltros() {
		comboDisciplina.setSelectedIndex(0);
		comboSituacao.setSelectedIndex(0);
		campoDataInicial.setText("");
		campoDataFinal.setText("");

		preencherTabela(presencas);
	}

	private String formatarTurno(String turno) {
		if (turno == null || turno.isBlank()) {
			return "Não informado";
		}

		switch (turno.toUpperCase()) {
		case "MATUTINO":
			return "Matutino";
		case "VESPERTINO":
			return "Vespertino";
		case "NOTURNO":
			return "Noturno";
		case "INTEGRAL":
			return "Integral";
		default:
			return turno;
		}
	}

	private String obterMensagemErro(Exception ex) {
		if (ex == null) {
			return "Erro desconhecido.";
		}
		if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
			return ex.getMessage();
		}
		if (ex.getCause() != null && ex.getCause().getMessage() != null && !ex.getCause().getMessage().isBlank()) {
			return ex.getCause().getMessage();
		}
		return "Erro desconhecido.";
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
		botao.setForeground(Color.WHITE);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private JPanel criarPainelArredondado() {
		return new JPanel() {
			private static final long serialVersionUID = 1L;
			{
				setOpaque(false);
			}
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(corCampo);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
				g2.setColor(corBorda);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
				g2.dispose();
				super.paintComponent(g);
			}
		};
	}
}