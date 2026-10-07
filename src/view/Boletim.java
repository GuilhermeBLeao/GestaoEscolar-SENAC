package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import dao.AlunoDAO;
import dao.BoletimDAO;
import dao.DisciplinaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.BoletimItem;
import model.Disciplina;
import model.Trimestre;
import model.Turma;

public class Boletim extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JComboBox<String> cbAluno;
	private JComboBox<String> cbTurma;
	private JTabbedPane abasNotas;
	private final Map<String, Aluno> alunosPorNome = new LinkedHashMap<>();
	private final Map<String, Turma> turmasPorNome = new LinkedHashMap<>();
	private List<Trimestre> trimestres = new ArrayList<>();
	private List<Disciplina> disciplinasDaTurma = new ArrayList<>();
	private JLabel lblAluno;
	private JLabel lblTurma;
	private JLabel lblCurso;
	private JLabel lblTurno;
	private JLabel lblUnidade;
	private JLabel lblMunicipio;
	private JLabel lblMedia;
	private JLabel lblFaltas;
	private JLabel lblSituacao;
	private JLabel lblFrequencia;
	private String alunoAtual = "";
	private String mediaGeralAtual = "Não informado";
	private String totalFaltasAtual = "Não informado";
	private String situacaoAtual = "Não informado";
	private String frequenciaGeralAtual = "Não informado";
	private String turmaAtual = "Não informado";
	private String turnoAtual = "Não informado";
	private String cursoAtual = "Não informado";
	private String unidadeAtual = "Não informado";
	private String municipioAtual = "Não informado";
	private int anoLetivoAtual;
	private boolean anoLetivoConcluido;
	private boolean boletimCompleto;
	private BaseFont fonteNormalPdf;
	private BaseFont fonteNegritoPdf;
	private String[][] dadosBoletim = new String[0][0];

	public Boletim() {
		setTitle("Boletim do Aluno");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int margem = 30;
		int larguraInterno = areaUtil.width - margem * 2 + 20;
		int alturaInterno = areaUtil.height - margem * 2;
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

		criarConteudo(interno, larguraInterno, alturaInterno);
		carregarOpcoesDoBanco();
	}

	private void criarConteudo(JPanel interno, int larguraInterno, int alturaInterno) {
		JLabel titulo = new JLabel("Boletim do Aluno");
		titulo.setForeground(textos);
		titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 36));
		titulo.setBounds(35, 35, 500, 45);
		interno.add(titulo);

		JLabel subtitulo = new JLabel("Visualize as notas por trimestre e " + "exporte o boletim oficial em PDF.");
		subtitulo.setForeground(new Color(220, 210, 255));
		subtitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 17));
		subtitulo.setBounds(35, 80, 900, 25);
		interno.add(subtitulo);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(larguraInterno - 170, 35, 120, 38);
		estilizarBotaoAcao(btnVoltar);
		btnVoltar.addActionListener(e -> dispose());
		interno.add(btnVoltar);

		JPanel painelAcao = criarPainelArredondado(corCampo, corBorda, 24);
		painelAcao.setLayout(null);
		painelAcao.setBounds(35, 125, larguraInterno - 70, 100);
		interno.add(painelAcao);

		JLabel lbTurma = new JLabel("Selecionar turma");
		lbTurma.setForeground(corLabel);
		lbTurma.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
		lbTurma.setBounds(30, 20, 200, 22);
		painelAcao.add(lbTurma);

		cbTurma = criarCombo(new String[] { "Selecione" });
		cbTurma.setBounds(30, 48, 250, 38);
		painelAcao.add(cbTurma);

		JLabel lbAluno = new JLabel("Selecionar aluno");
		lbAluno.setForeground(corLabel);
		lbAluno.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
		lbAluno.setBounds(300, 20, 200, 22);
		painelAcao.add(lbAluno);

		cbAluno = criarCombo(new String[] { "Selecione" });
		cbAluno.setBounds(300, 48, 450, 38);
		painelAcao.add(cbAluno);

		JButton btnAbrir = new JButton("Abrir boletim");
		btnAbrir.setBounds(775, 48, 150, 38);
		estilizarBotaoAcao(btnAbrir);
		painelAcao.add(btnAbrir);

		JButton btnExportar = new JButton("Exportar PDF");
		btnExportar.setBounds(945, 48, 150, 38);
		estilizarBotaoAcao(btnExportar);
		painelAcao.add(btnExportar);

		JButton btnTurma = new JButton("Gerar turma");
		btnTurma.setBounds(1110, 48, 110, 38);
		estilizarBotaoAcao(btnTurma);
		painelAcao.add(btnTurma);

		JPanel painelDados = criarPainelArredondado(corCampo, corBorda, 24);
		painelDados.setLayout(null);
		painelDados.setBounds(35, 245, larguraInterno - 70, 135);
		interno.add(painelDados);

		lblAluno = criarRotuloInfo("Aluno", "Não informado", 30, 20, 390);
		lblTurma = criarRotuloInfo("Turma", "Não informado", 425, 20, 140);
		lblCurso = criarRotuloInfo("Curso", "Não informado", 565, 20, 210);
		lblTurno = criarRotuloInfo("Turno", "Não informado", 775, 20, 180);
		lblUnidade = criarRotuloInfo("Unidade Escolar", "Solo Firme", 30, 78, 300);
		lblMunicipio = criarRotuloInfo("Município", "Curitibanos", 330, 78, 260);
		lblMedia = criarRotuloInfo("Média Geral", mediaGeralAtual, 590, 78, 150);
		lblFaltas = criarRotuloInfo("Total de Faltas", totalFaltasAtual, 740, 78, 150);
		lblSituacao = criarRotuloInfo("Situação", situacaoAtual, 890, 78, 140);
		lblFrequencia = criarRotuloInfo("Frequência", frequenciaGeralAtual, 1030, 78, 180);

		painelDados.add(lblAluno);
		painelDados.add(lblTurma);
		painelDados.add(lblCurso);
		painelDados.add(lblTurno);
		painelDados.add(lblUnidade);
		painelDados.add(lblMunicipio);
		painelDados.add(lblMedia);
		painelDados.add(lblFaltas);
		painelDados.add(lblSituacao);
		painelDados.add(lblFrequencia);

		JPanel painelNotas = criarPainelArredondado(corCampo, corBorda, 24);
		painelNotas.setLayout(null);
		painelNotas.setBounds(35, 405, larguraInterno - 70, alturaInterno - 435);
		interno.add(painelNotas);

		JLabel lbNotas = new JLabel("Notas por Trimestre");
		lbNotas.setForeground(corLabel);
		lbNotas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 17));
		lbNotas.setBounds(25, 15, 300, 25);
		painelNotas.add(lbNotas);

		abasNotas = criarAbasNotas();
		abasNotas.setBounds(25, 50, 1760, 467);
		painelNotas.add(abasNotas);
		
		btnAbrir.addActionListener(e -> carregarAluno());
		btnExportar.addActionListener(e -> exportarPdf());
		btnTurma.addActionListener(e -> exportarBoletinsDaTurma());
		cbTurma.addActionListener(e -> carregarAlunosDaTurma());
	}

	private JTabbedPane criarAbasNotas() {
		JTabbedPane abas = new JTabbedPane();
		abas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
		abas.setForeground(textos);
		abas.setBackground(corInterna);
		abas.addTab("1º Trimestre", criarScrollTabela(criarTabelaNotas(1)));
		abas.addTab("2º Trimestre", criarScrollTabela(criarTabelaNotas(2)));
		abas.addTab("3º Trimestre", criarScrollTabela(criarTabelaNotas(3)));
		return abas;
	}

	private void carregarOpcoesDoBanco() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			alunosPorNome.clear();
			turmasPorNome.clear();
			List<Turma> turmas = new TurmaDAO(conn).listarAtivas();

			for (Turma turma : turmas) {
				String nome = turma.getDescricaoTurma() + " (" + turma.getTurno().name() + ")";
				turmasPorNome.put(nome, turma);
				cbTurma.addItem(nome);
			}
			List<Trimestre> todosTrimestres = new BoletimDAO(conn).listarTrimestres();
			prepararTrimestresAnoLetivo(todosTrimestres);
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar turmas e " + "trimestres do banco: " + ex.getMessage());
		}
	}

	private void prepararTrimestresAnoLetivo(List<Trimestre> todosTrimestres) {
		trimestres.clear();
		if (todosTrimestres == null || todosTrimestres.isEmpty()) {
			anoLetivoAtual = 0;
			return;
		}
		anoLetivoAtual = todosTrimestres.stream().mapToInt(Trimestre::getAnoLetivo).max().orElse(0);
		for (Trimestre trimestre : todosTrimestres) {
			if (trimestre.getAnoLetivo() == anoLetivoAtual) {
				trimestres.add(trimestre);
			}
		}
		trimestres.sort(Comparator.comparingInt(Trimestre::getNumero));
	}

	private void carregarAlunosDaTurma() {
		cbAluno.removeAllItems();
		cbAluno.addItem("Selecione");
		alunosPorNome.clear();
		String turmaSelecionada = String.valueOf(cbTurma.getSelectedItem());
		Turma turma = turmasPorNome.get(turmaSelecionada);

		if (turma == null) {
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			for (Aluno aluno : new AlunoDAO(conn).listarPorTurma(turma.getIdTurma())) {
				String nome = aluno.getNome() + " - " + aluno.getMatricula();
				alunosPorNome.put(nome, aluno);
				cbAluno.addItem(nome);
			}
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os alunos " + "da turma: " + ex.getMessage());
		}
	}

	private void carregarBoletimDoBanco(Aluno aluno) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			Turma turma = turmaSelecionada();

			if (turma == null) {
				mostrarErro("Não foi possível identificar " + "a turma do aluno.");
				return;
			}
			disciplinasDaTurma = new DisciplinaDAO(conn).listarPorTurma(turma.getIdTurma());
			BoletimDAO boletimDAO = new BoletimDAO(conn);
			Map<Integer, String[]> linhas = new LinkedHashMap<>();

			for (Disciplina disciplina : disciplinasDaTurma) {
				String[] linha = new String[20];
				linha[0] = String.valueOf(disciplina.getIdDisciplina());
				linha[1] = disciplina.getDescricao();
				linhas.put(disciplina.getIdDisciplina(), linha);
			}

			for (int indiceTrimestre = 0; indiceTrimestre < 3; indiceTrimestre++) {
				if (indiceTrimestre >= trimestres.size()) {
					continue;
				}
				Trimestre trimestre = trimestres.get(indiceTrimestre);
				model.Boletim boletim = boletimDAO.buscarPorAluno(aluno.getIdAluno(), trimestre.getIdTrimestre());

				if (boletim == null) {
					continue;
				}
				for (BoletimItem item : boletim.getItens()) {
					String[] linha = linhas.get(item.getIdDisciplina());

					if (linha == null) {
						continue;
					}
					int colunaMedia = 2 + indiceTrimestre * 2;
					linha[colunaMedia] = formatarMedia(item.getMedia());
				}
			}
			for (Disciplina disciplina : disciplinasDaTurma) {
				String[] linha = linhas.get(disciplina.getIdDisciplina());

				if (linha == null) {
					continue;
				}
				int totalFaltas = 0;
				int totalAulasAno = 0;
				int totalPresencasAno = 0;

				for (int indiceTrimestre = 0; indiceTrimestre < 3; indiceTrimestre++) {
					if (indiceTrimestre >= trimestres.size()) {
						continue;
					}
					Trimestre trimestre = trimestres.get(indiceTrimestre);
					int[] frequencia = obterResumoFrequencia(conn, aluno.getIdAluno(), disciplina.getIdDisciplina(),
						trimestre);
					int colunaFaltas = 3 + indiceTrimestre * 2;
					int colunaTotalAulas = 12 + indiceTrimestre;

					if (frequencia[0] <= 0) {
						linha[colunaFaltas] = "";
						linha[colunaTotalAulas] = "";
						linha[16 + indiceTrimestre] = "";
					} else {
						linha[colunaFaltas] = String.valueOf(frequencia[1]);
						linha[colunaTotalAulas] = String.valueOf(frequencia[0]);
						linha[16 + indiceTrimestre] = String.valueOf(frequencia[2]);
						totalFaltas += frequencia[1];
						totalAulasAno += frequencia[0];
						totalPresencasAno += frequencia[2];
					}
				}
				linha[11] = String.valueOf(totalFaltas);
				linha[15] = String.valueOf(totalAulasAno);
				linha[19] = String.valueOf(totalPresencasAno);
			}
			dadosBoletim = linhas.values().toArray(new String[0][]);
			calcularResumoBoletim();
			atualizarInformacoesAluno(aluno);
			atualizarTabelaNotas();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar o boletim " + "do banco: " + ex.getMessage());
		}
	}

	private void calcularResumoBoletim() {
		anoLetivoConcluido = verificarAnoLetivoConcluido();
		boolean todasDisciplinasComNotas = verificarTodasDisciplinasComNotas();
		boletimCompleto = anoLetivoConcluido && todasDisciplinasComNotas;
		double soma = 0;
		int quantidade = 0;

		for (String[] linha : dadosBoletim) {
			for (int coluna : new int[] { 2, 4, 6 }) {
				String valor = valorOuVazio(linha[coluna]);

				if (!valor.isEmpty()) {
					try {
						soma += Double.parseDouble(valor.replace(',', '.'));
						quantidade++;
					} catch (NumberFormatException e) {
						mostrarErro("Ocorreu um erro: " + e.getMessage());
					}
				}
			}
		}

		if (quantidade == 0) {
			mediaGeralAtual = "Não informado";
		} else {
			mediaGeralAtual = formatarMedia(soma / quantidade);
		}
		int totalFaltas = 0;
		int totalAulas = 0;
		int totalPresencas = 0;

		for (String[] linha : dadosBoletim) {
			totalFaltas += valorInteiroOuZero(linha[11]);
			totalAulas += valorInteiroOuZero(linha[15]);
			totalPresencas += valorInteiroOuZero(linha[19]);
		}
		totalFaltasAtual = String.valueOf(totalFaltas);

		if (totalAulas > 0) {
			double percentual = ((double) totalPresencas / totalAulas) * 100.0;
			frequenciaGeralAtual = formatarPercentual(percentual);
		} else {
			frequenciaGeralAtual = "Não informado";
		}
		if (dadosBoletim.length == 0) {
			situacaoAtual = "Não informado";
			return;
		}
		if (!boletimCompleto) {
			if (anoLetivoConcluido && !todasDisciplinasComNotas) {
				situacaoAtual = "Pendente de notas";
			} else {
				situacaoAtual = "Ano letivo não concluído";
			}
			return;
		}
		boolean todasMediasAprovadas = true;

		for (String[] linha : dadosBoletim) {
			for (int coluna : new int[] { 2, 4, 6 }) {
				double media = Double.parseDouble(linha[coluna].replace(',', '.'));

				if (media < 6.0) {
					todasMediasAprovadas = false;
					break;
				}
			}
			if (!todasMediasAprovadas) {
				break;
			}
		}
		situacaoAtual = todasMediasAprovadas ? "Aprovado(a)" : "Reprovado(a)";

		for (String[] linha : dadosBoletim) {
			double somaDisciplina = 0;
			int quantidadeDisciplina = 0;

			for (int coluna : new int[] { 2, 4, 6 }) {
				String valor = valorOuVazio(linha[coluna]);

				if (!valor.isEmpty()) {
					somaDisciplina += Double.parseDouble(valor.replace(',', '.'));
					quantidadeDisciplina++;
				}
			}

			if (quantidadeDisciplina == 3) {
				double mediaAnual = somaDisciplina / 3.0;
				linha[8] = formatarMedia(mediaAnual);
				linha[10] = formatarMedia(mediaAnual);
			}
		}
	}

	private boolean verificarAnoLetivoConcluido() {
		if (trimestres.size() < 3) {
			return false;
		}
		LocalDate hoje = LocalDate.now();

		for (int i = 0; i < 3; i++) {
			Trimestre trimestre = trimestres.get(i);

			if (trimestre.getDataFim() == null) {
				return false;
			}
			if (trimestre.getDataFim().isAfter(hoje)) {
				return false;
			}
		}
		return true;
	}

	private boolean verificarTodasDisciplinasComNotas() {
		if (disciplinasDaTurma.isEmpty()) {
			return false;
		}

		for (String[] linha : dadosBoletim) {
			for (int coluna : new int[] { 2, 4, 6 }) {
				if (valorOuVazio(linha[coluna]).isEmpty()) {
					return false;
				}
			}
		}
		return dadosBoletim.length == disciplinasDaTurma.size();
	}

	private void atualizarInformacoesAluno(Aluno aluno) {
		Turma turma = turmaSelecionada();
		alunoAtual = aluno.getNome();
		turmaAtual = turma == null ? "Não informado" : turma.getDescricaoTurma();
		turnoAtual = turma == null ? "Não informado" : turma.getTurno().name();
		cursoAtual = "Ensino Médio";
		unidadeAtual = "Solo Firme - Curitibanos";
		municipioAtual = "Curitibanos";

		lblAluno.setText(rotuloHtml("Aluno", alunoAtual));
		lblTurma.setText(rotuloHtml("Turma", turmaAtual));
		lblCurso.setText(rotuloHtml("Curso", cursoAtual));
		lblTurno.setText(rotuloHtml("Turno", turnoAtual));
		lblUnidade.setText(rotuloHtml("Unidade Escolar", unidadeAtual));
		lblMunicipio.setText(rotuloHtml("Município", municipioAtual));
		lblMedia.setText(rotuloHtml("Média Geral", mediaGeralAtual));
		lblFaltas.setText(rotuloHtml("Total de Faltas", totalFaltasAtual));
		lblSituacao.setText(rotuloHtml("Situação", situacaoAtual));
		lblFrequencia.setText(rotuloHtml("Frequência", frequenciaGeralAtual));
	}

	private Turma turmaSelecionada() {
		return turmasPorNome.get(String.valueOf(cbTurma.getSelectedItem()));
	}

	private String rotuloHtml(String titulo, String valor) {
		return "<html><b style='color:#ff78dc'>" + titulo + "</b><br>" + valor + "</html>";
	}

	private String formatarMedia(double media) {
		return String.format(java.util.Locale.US, "%.1f", media).replace('.', ',');
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private JTable criarTabelaNotas(int trimestre) {
		String[] colunas = { "Cód.", "Disciplina", "Nota", "Faltas", "Frequência", "Média", "Média Final",
				"Total Faltas", "Situação" };
		Object[][] dados = new Object[dadosBoletim.length][9];

		for (int i = 0; i < dadosBoletim.length; i++) {
			String nota = "";
			String faltas = "";
			int totalAulas = 0;
			int totalPresencas = 0;

			if (trimestre == 1) {
				nota = dadosBoletim[i][2];
				faltas = dadosBoletim[i][3];
				totalAulas = valorInteiroOuZero(dadosBoletim[i][12]);
				totalPresencas = valorInteiroOuZero(dadosBoletim[i][16]);
			} else if (trimestre == 2) {
				nota = dadosBoletim[i][4];
				faltas = dadosBoletim[i][5];
				totalAulas = valorInteiroOuZero(dadosBoletim[i][13]);
				totalPresencas = valorInteiroOuZero(dadosBoletim[i][17]);
			} else {
				nota = dadosBoletim[i][6];
				faltas = dadosBoletim[i][7];
				totalAulas = valorInteiroOuZero(dadosBoletim[i][14]);
				totalPresencas = valorInteiroOuZero(dadosBoletim[i][18]);
			}
			dados[i][0] = dadosBoletim[i][0];
			dados[i][1] = valorOuVazio(dadosBoletim[i][1]);
			dados[i][2] = valorOuVazio(nota);
			dados[i][3] = valorOuVazio(faltas);
			dados[i][4] = calcularFrequencia(totalPresencas, totalAulas);
			dados[i][5] = valorOuVazio(dadosBoletim[i][8]);
			dados[i][6] = valorOuVazio(dadosBoletim[i][10]);
			dados[i][7] = valorOuVazio(dadosBoletim[i][11]);
			dados[i][8] = obterSituacaoDisciplina(dadosBoletim[i]);
		}
		DefaultTableModel modelo = new DefaultTableModel(dados, colunas) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable tabela = new JTable(modelo);
		tabela.setRowHeight(34);
		tabela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
		tabela.setForeground(textos);
		tabela.setBackground(new Color(31, 10, 90));
		tabela.setGridColor(new Color(90, 50, 170));
		tabela.setSelectionBackground(new Color(80, 40, 160));
		tabela.setSelectionForeground(textos);
		tabela.setShowGrid(true);
		tabela.setFillsViewportHeight(true);

		JTableHeader header = tabela.getTableHeader();
		header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
		header.setBackground(new Color(45, 15, 120));
		header.setForeground(textos);
		header.setReorderingAllowed(false);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
		centro.setHorizontalAlignment(SwingConstants.CENTER);
		centro.setBackground(new Color(31, 10, 90));
		centro.setForeground(textos);

		for (int i = 0; i < tabela.getColumnCount(); i++) {
			tabela.getColumnModel().getColumn(i).setCellRenderer(centro);
		}
		tabela.getColumnModel().getColumn(0).setPreferredWidth(60);
		tabela.getColumnModel().getColumn(1).setPreferredWidth(260);
		tabela.getColumnModel().getColumn(4).setPreferredWidth(100);
		tabela.getColumnModel().getColumn(8).setPreferredWidth(130);
		return tabela;
	}

	private String obterSituacaoDisciplina(String[] linha) {
		if (!boletimCompleto) {
			return "Em andamento";
		}
		String media = valorOuVazio(linha[10]);

		if (media.isEmpty()) {
			return "Pendente";
		}
		try {
			double valor = Double.parseDouble(media.replace(',', '.'));
			return valor >= 6.0 ? "Aprovado(a)" : "Reprovado(a)";
		} catch (NumberFormatException e) {
			return "Pendente";
		}
	}

	private JScrollPane criarScrollTabela(JTable tabela) {
		JScrollPane scroll = new JScrollPane(tabela);
		scroll.getViewport().setBackground(corCampo);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		return scroll;
	}

	private void carregarAluno() {
		Aluno aluno = alunosPorNome.get(String.valueOf(cbAluno.getSelectedItem()));

		if (aluno == null) {
			mostrarErro("Selecione um aluno da turma " + "antes de abrir o boletim.");
			return;
		}
		carregarBoletimDoBanco(aluno);
	}

	public void abrirAluno(int idAluno) {
		for (Map.Entry<String, Turma> turmaEntrada : turmasPorNome.entrySet()) {
			cbTurma.setSelectedItem(turmaEntrada.getKey());
			carregarAlunosDaTurma();

			if (alunosPorNome.values().stream().anyMatch(aluno -> aluno.getIdAluno() == idAluno)) {
				break;
			}
		}

		for (Map.Entry<String, Aluno> entrada : alunosPorNome.entrySet()) {
			if (entrada.getValue().getIdAluno() == idAluno) {
				Turma turma = turmaSelecionada();
				if (turma == null || turma.getIdTurma() != entrada.getValue().getIdTurma()) {
					for (Map.Entry<String, Turma> turmaEntrada : turmasPorNome.entrySet()) {
						if (turmaEntrada.getValue().getIdTurma() == entrada.getValue().getIdTurma()) {
							cbTurma.setSelectedItem(turmaEntrada.getKey());
							break;
						}
					}
					carregarAlunosDaTurma();
				}
				cbAluno.setSelectedItem(entrada.getKey());
				carregarAluno();
				return;
			}
		}
		mostrarErro("Aluno não encontrado entre os " + "registros disponíveis.");
	}

	public void abrirEExportarAluno(int idAluno) {
		abrirAluno(idAluno);

		if (alunoAtual != null && !alunoAtual.isBlank() && !"Não informado".equals(alunoAtual)) {
			gerarPdfAtual(true);
		}
	}

	private void atualizarTabelaNotas() {
		if (abasNotas == null) {
			return;
		}

		abasNotas.removeAll();
		abasNotas.addTab("1º Trimestre", criarScrollTabela(criarTabelaNotas(1)));
		abasNotas.addTab("2º Trimestre", criarScrollTabela(criarTabelaNotas(2)));
		abasNotas.addTab("3º Trimestre", criarScrollTabela(criarTabelaNotas(3)));
		abasNotas.revalidate();
		abasNotas.repaint();
	}

	private void exportarPdf() {
		gerarPdfAtual(true);
	}

	private void gerarPdfAtual(boolean mostrarMensagem) {
		if (alunoAtual == null || alunoAtual.isBlank() || "Não informado".equals(alunoAtual)) {
			mostrarErro("Selecione um aluno e abra o " + "boletim antes de exportar.");
			return;
		}

		String nomeArquivo = "boletim(" + limparNomeArquivo(alunoAtual) + ").pdf";
		File pasta = new File("Boletins");

		if (!pasta.exists()) {
			pasta.mkdirs();
		}

		File arquivo = new File(pasta, nomeArquivo);

		try {
			Document document = new Document(PageSize.A4.rotate(), 25, 25, 25, 25);
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivo));
			
			document.open();
			
			fonteNormalPdf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
			fonteNegritoPdf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);
			
			PdfContentByte cb = writer.getDirectContent();
			desenharBoletimPdf(cb, document.getPageSize().getWidth(), document.getPageSize().getHeight());
			document.close();

			if (mostrarMensagem) {
				JOptionPane.showMessageDialog(this,
						"Boletim exportado com sucesso!\n\n" + "Local: " + arquivo.getAbsolutePath(), "PDF Gerado",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Erro ao gerar PDF:\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void exportarBoletinsDaTurma() {
		Turma turma = turmaSelecionada();

		if (turma == null) {
			mostrarErro("Selecione uma turma antes de gerar os boletins.");
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			List<Aluno> alunos = new AlunoDAO(conn).listarPorTurma(turma.getIdTurma());

			if (alunos.isEmpty()) {
				mostrarErro("Não existem alunos ativos " + "nesta turma.");
				return;
			}
			for (Aluno aluno : alunos) {
				carregarBoletimDoBanco(aluno);
				gerarPdfAtual(false);
			}
			JOptionPane.showMessageDialog(this, alunos.size() + " boletim(ns) gerado(s) " + "na pasta Boletins.",
					"PDF Gerados", JOptionPane.INFORMATION_MESSAGE);
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível gerar os " + "boletins da turma: " + ex.getMessage());
		}
	}

	private void desenharBoletimPdf(PdfContentByte cb, float pageW, float pageH) throws Exception {
		Color azul = new Color(63, 10, 117);
		Color grade = new Color(120, 116, 165);
		Color fundoAlternado = new Color(247, 247, 252);

		float margem = 18;
		float x = margem;
		float y = margem;
		float w = pageW - margem * 2;
		float h = pageH - margem * 2;
		float top = y + h;
		float headerH = 122;
		float headerBottom = top - headerH;

		cb.setColorStroke(azul);
		cb.setLineWidth(1.1f);
		cb.rectangle(x, y, w, h);
		cb.stroke();

		linha(cb, x, headerBottom, x + w, headerBottom);

		// Busca a URL do arquivo no classpath (funciona dentro e fora do .jar)
		java.net.URL logoUrl = getClass().getResource("/Images/Solo-Firme.png");

		if (logoUrl != null) {
		    // O OpenPDF / iText aceita diretamente um objeto java.net.URL
		    com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoUrl);
		    logo.scaleToFit(150, 120);
		    logo.setAbsolutePosition(x + 5, top - 110);
		    cb.addImage(logo);
		} else {
		    System.err.println("Erro: Imagem '/Images/Solo-Firme.png' não foi encontrada!");
		}
		escrever(cb, "E.E.B Solo Firme", x + 180, top - 40, 24, true, Element.ALIGN_LEFT);
		escrever(cb, "S.E.D Santa Catarina", x + 190, top - 66, 15, true, Element.ALIGN_LEFT);
		escreverCor(cb, "Solo Firme: A Base para Salto do Seu Filho.", x + 270, top - 95, 12, true,
			Element.ALIGN_CENTER, corLabel);

		float divisorX = x + 400;
		float dadosX = divisorX + 25;
		float valorX = dadosX + 70;
		float dadosY = top - 32;

		linha(cb, divisorX, top - 14, divisorX, headerBottom + 15);

		escrever(cb, "ALUNO:", dadosX, dadosY, 10, true, Element.ALIGN_LEFT);
		escreverLimitado(cb, alunoAtual, valorX, dadosY, 10, false, Element.ALIGN_LEFT, 270);
		escrever(cb, "TURMA:", dadosX, dadosY - 26, 10, true, Element.ALIGN_LEFT);
		escrever(cb, turmaAtual, valorX, dadosY - 26, 10, false, Element.ALIGN_LEFT);
		escrever(cb, "TURNO:", dadosX, dadosY - 52, 10, true, Element.ALIGN_LEFT);
		escrever(cb, turnoAtual, valorX, dadosY - 52, 10, false, Element.ALIGN_LEFT);
		escrever(cb, "MUNICÍPIO:", dadosX, dadosY - 78, 10, true, Element.ALIGN_LEFT);
		escrever(cb, municipioAtual, valorX, dadosY - 78, 10, false, Element.ALIGN_LEFT);

		float dados2X = x + w - 240;

		escrever(cb, "CURSO:", dados2X, dadosY - 26, 10, true, Element.ALIGN_LEFT);
		escrever(cb, cursoAtual, dados2X + 60, dadosY - 26, 10, false, Element.ALIGN_LEFT);
		escrever(cb, "UNIDADE:", dados2X, dadosY - 52, 10, true, Element.ALIGN_LEFT);
		escrever(cb, unidadeAtual, dados2X + 60, dadosY - 52, 10, false, Element.ALIGN_LEFT);

		float tabelaTop = headerBottom - 12;
		float tabelaBottom = desenharTabelaPdf(cb, x + 7, tabelaTop, w - 14, azul, grade, fundoAlternado);
		float footerTop = tabelaBottom - 12;
		float footerH = 48;

		cb.setColorStroke(azul);
		cb.rectangle(x + 7, footerTop - footerH, w - 14, footerH);
		cb.stroke();

		escrever(cb, "SITUAÇÃO:", x + 22, footerTop - 31, 13, true, Element.ALIGN_LEFT);
		escrever(cb, situacaoAtual.toUpperCase(), x + 100, footerTop - 31, 14, true, Element.ALIGN_LEFT);

		float blocoX = x + w - 360;

		linha(cb, blocoX, footerTop - 8, blocoX, footerTop - footerH + 8);
		linha(cb, blocoX + 115, footerTop - 8, blocoX + 115, footerTop - footerH + 8);
		linha(cb, blocoX + 235, footerTop - 8, blocoX + 235, footerTop - footerH + 8);

		escrever(cb, "MÉDIA GERAL:", blocoX + 58, footerTop - 19, 9, true, Element.ALIGN_CENTER);
		escrever(cb, mediaGeralAtual, blocoX + 58, footerTop - 38, 16, true, Element.ALIGN_CENTER);
		escrever(cb, "TOTAL DE FALTAS:", blocoX + 175, footerTop - 19, 9, true, Element.ALIGN_CENTER);
		escrever(cb, totalFaltasAtual, blocoX + 175, footerTop - 38, 16, true, Element.ALIGN_CENTER);
		escrever(cb, "FREQUÊNCIA TOTAL:", blocoX + 295, footerTop - 19, 9, true, Element.ALIGN_CENTER);
		escrever(cb, frequenciaGeralAtual, blocoX + 295, footerTop - 38, 16, true, Element.ALIGN_CENTER);
	}

	private float desenharTabelaPdf(PdfContentByte cb, float x, float topY, float w, Color azul, Color grade,
			Color fundoAlternado) throws Exception {
		Color azulSubcabecalho = new Color(31, 18, 150);
		float headerGrupoH = 29;
		float headerSubH = 23;
		float headerH = headerGrupoH + headerSubH;
		float rowH = 19;
		float codW = 42;
		float notaW = 40f;
		float faltaW = 36;
		float freqW = 47;
		float mediaFinalW = 56;
		float totalFaltasW = 58;
		float freqFinalW = 65;
		float somaColunas = codW + ((notaW + faltaW + freqW) * 3) + mediaFinalW + totalFaltasW + freqFinalW;
		float discW = w - somaColunas;
		float y = topY - headerH;
		float c0 = x;
		float c1 = c0 + codW;
		float c2 = c1 + discW;
		float c3 = c2 + notaW;
		float c4 = c3 + faltaW;
		float c5 = c4 + freqW;
		float c6 = c5 + notaW;
		float c7 = c6 + faltaW;
		float c8 = c7 + freqW;
		float c9 = c8 + notaW;
		float c10 = c9 + faltaW;
		float c11 = c10 + freqW;
		float c12 = c11;
		float c13 = c12 + mediaFinalW;
		float c14 = c13 + totalFaltasW;
		float c15 = c14 + freqFinalW;

		cb.setColorFill(azul);
		cb.rectangle(x, y, w, headerH);
		cb.fill();
		cb.setColorFill(azulSubcabecalho);
		cb.rectangle(c2, y, c11 - c2, headerSubH);
		cb.fill();
		cb.setColorStroke(grade);
		cb.rectangle(x, y, w, headerH);
		cb.stroke();

		float[] colunasPrincipais = { c1, c2, c5, c8, c11, c12, c13, c14 };
		float[] colunasSubcabecalho = { c3, c4, c6, c7, c9, c10 };
		float[] colunasDados = { c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14 };

		for (float coluna : colunasPrincipais) {
			linha(cb, coluna, y, coluna, y + headerH);
		}
		for (float coluna : colunasSubcabecalho) {
			linha(cb, coluna, y, coluna, y + headerSubH);
		}
		linha(cb, c2, y + headerSubH, c11, y + headerSubH);
		cb.setColorFill(textos);

		float grupoY = y + headerSubH + 10;
		float subY = y + 8;

		escrever(cb, "Cód.", c0 + codW / 2, grupoY - 12, 9, true, Element.ALIGN_CENTER);
		escrever(cb, "Disciplina", c1 + 8, grupoY - 12, 11, true, Element.ALIGN_LEFT);
		escrever(cb, "1º Trimestre", (c2 + c5) / 2, grupoY, 10, true, Element.ALIGN_CENTER);
		escrever(cb, "2º Trimestre", (c5 + c8) / 2, grupoY, 10, true, Element.ALIGN_CENTER);
		escrever(cb, "3º Trimestre", (c8 + c11) / 2, grupoY, 10, true, Element.ALIGN_CENTER);
		escrever(cb, "Média", (c12 + c13) / 2, y + headerSubH + 6, 9, true, Element.ALIGN_CENTER);
		escrever(cb, "Final", (c12 + c13) / 2, y + headerSubH + (-6), 9, true, Element.ALIGN_CENTER);
		escrever(cb, "Total", (c13 + c14) / 2, y + headerSubH + 6, 9, true, Element.ALIGN_CENTER);
		escrever(cb, "Faltas", (c13 + c14) / 2, y + headerSubH + (-6), 9, true, Element.ALIGN_CENTER);
		escrever(cb, "Freq.", (c14 + c15) / 2, y + headerSubH + 6, 9, true, Element.ALIGN_CENTER);
		escrever(cb, "Final", (c14 + c15) / 2, y + headerSubH + (-6), 9, true, Element.ALIGN_CENTER);

		escreverSubcabecalho(cb, c2, c3, c4, subY, notaW, faltaW, freqW);
		escreverSubcabecalho(cb, c5, c6, c7, subY, notaW, faltaW, freqW);
		escreverSubcabecalho(cb, c8, c9, c10, subY, notaW, faltaW, freqW);
		cb.setColorFill(Color.BLACK);

		float rowY = y - rowH;
		for (int i = 0; i < dadosBoletim.length; i++) {
			float atualY = rowY - i * rowH;
			if (i % 2 == 1) {
				cb.setColorFill(fundoAlternado);
				cb.rectangle(x, atualY, w, rowH);
				cb.fill();
				cb.setColorFill(Color.BLACK);
			}
			cb.setColorStroke(grade);
			cb.rectangle(x, atualY, w, rowH);
			cb.stroke();

			for (float coluna : colunasDados) {
				linha(cb, coluna, atualY, coluna, atualY + rowH);
			}
			float textY = atualY + 6;

			String faltas1 = valorOuVazio(dadosBoletim[i][3]);
			String faltas2 = valorOuVazio(dadosBoletim[i][5]);
			String faltas3 = valorOuVazio(dadosBoletim[i][7]);
			String faltasFinal = valorOuVazio(dadosBoletim[i][11]);

			int totalAulas1 = valorInteiroOuZero(dadosBoletim[i][12]);
			int totalAulas2 = valorInteiroOuZero(dadosBoletim[i][13]);
			int totalAulas3 = valorInteiroOuZero(dadosBoletim[i][14]);
			int totalAulasFinal = valorInteiroOuZero(dadosBoletim[i][15]);
			int presencas1 = valorInteiroOuZero(dadosBoletim[i][16]);
			int presencas2 = valorInteiroOuZero(dadosBoletim[i][17]);
			int presencas3 = valorInteiroOuZero(dadosBoletim[i][18]);
			int presencasFinal = valorInteiroOuZero(dadosBoletim[i][19]);

			escrever(cb, dadosBoletim[i][0], c0 + codW / 2, textY, 8, true, Element.ALIGN_CENTER);
			escreverLimitado(cb, dadosBoletim[i][1], c1 + 7, textY, 8, false, Element.ALIGN_LEFT, discW - 12);

			if (totalAulas1 > 0) {
				escreverLinhaTrimestre(cb, c2, c3, c4, textY, notaW, faltaW, freqW, dadosBoletim[i][2], faltas1,
						calcularFrequencia(presencas1, totalAulas1));
			} else {
				escreverLinhaTrimestre(cb, c2, c3, c4, textY, notaW, faltaW, freqW, "", "", "");
			}
			if (totalAulas2 > 0) {
				escreverLinhaTrimestre(cb, c5, c6, c7, textY, notaW, faltaW, freqW, dadosBoletim[i][4], faltas2,
						calcularFrequencia(presencas2, totalAulas2));
			} else {
				escreverLinhaTrimestre(cb, c5, c6, c7, textY, notaW, faltaW, freqW, "", "", "");
			}
			if (totalAulas3 > 0) {
				escreverLinhaTrimestre(cb, c8, c9, c10, textY, notaW, faltaW, freqW, dadosBoletim[i][6], faltas3,
					calcularFrequencia(presencas3, totalAulas3));
			} else {
				escreverLinhaTrimestre(cb, c8, c9, c10, textY, notaW, faltaW, freqW, "", "", "");
			}
			escrever(cb, valorOuVazio(dadosBoletim[i][10]), c12 + mediaFinalW / 2, textY, 8, true,
					Element.ALIGN_CENTER);
			escrever(cb, faltasFinal, c13 + totalFaltasW / 2, textY, 8, true, Element.ALIGN_CENTER);

			if (totalAulasFinal > 0) {
				escrever(cb, calcularFrequenciaFinal(presencasFinal, totalAulasFinal), c14 + freqFinalW / 2, textY, 8,
						true, Element.ALIGN_CENTER);
			} else {
				escrever(cb, "", c14 + freqFinalW / 2, textY, 8, true, Element.ALIGN_CENTER);
			}
		}
		return rowY - dadosBoletim.length * rowH;
	}

	private void escreverSubcabecalho(PdfContentByte cb, float notaX, float faltaX, float freqX, float y, float notaW,
			float faltaW, float freqW) {
		escrever(cb, "Nota", notaX + notaW / 2, y, 8, true, Element.ALIGN_CENTER);
		escrever(cb, "Falta", faltaX + faltaW / 2, y, 8, true, Element.ALIGN_CENTER);
		escrever(cb, "Freq.", freqX + freqW / 2, y, 8, true, Element.ALIGN_CENTER);
	}

	private void escreverLinhaTrimestre(PdfContentByte cb, float notaX, float faltaX, float freqX, float y, float notaW,
			float faltaW, float freqW, String nota, String faltas, String frequencia) {
		escrever(cb, valorOuVazio(nota), notaX + notaW / 2, y, 8, false, Element.ALIGN_CENTER);
		escrever(cb, valorOuVazio(faltas), faltaX + faltaW / 2, y, 8, false, Element.ALIGN_CENTER);
		escrever(cb, valorOuVazio(frequencia), freqX + freqW / 2, y, 8, false, Element.ALIGN_CENTER);
	}

	private int[] obterResumoFrequencia(Connection conn, int idAluno, int idDisciplina, Trimestre trimestre)
			throws SQLException {
		if (trimestre == null) {
			return new int[] { 0, 0, 0 };
		}
		if (trimestre.getDataInicio() == null || trimestre.getDataFim() == null) {
			return new int[] { 0, 0, 0 };
		}
		final String sql = """
				SELECT
					COUNT(*) AS total_aulas,
					COALESCE(SUM(CASE WHEN p.presente = 0 THEN 1 ELSE 0 END), 0) AS total_faltas,
					COALESCE(SUM(CASE WHEN p.presente = 1 OR p.falta_abonada = 1 THEN 1 ELSE 0 END), 0) AS total_presencas
				FROM presenca p
				WHERE p.aluno_id = ?
				  AND p.disciplina_id = ?
				  AND p.data_presenca BETWEEN ? AND ?
				""";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idAluno);
			stmt.setInt(2, idDisciplina);
			stmt.setString(3, trimestre.getDataInicio().toString());
			stmt.setString(4, trimestre.getDataFim().toString());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new int[] { rs.getInt("total_aulas"),
							rs.getInt("total_faltas"),
							rs.getInt("total_presencas") };
				}
			}
		}
		return new int[] { 0, 0, 0 };
	}

	private String formatarPercentual(double percentual) {
		return String.format(java.util.Locale.US, "%.1f%%", percentual).replace('.', ',');
	}

	private int valorInteiroOuZero(String valor) {
		String texto = valorOuVazio(valor);

		if (texto.isEmpty()) {
			return 0;
		}
		try {
			return Integer.parseInt(texto);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String calcularFrequencia(int totalPresencas, int totalAulas) {
		if (totalAulas <= 0) {
			return "";
		}
		double frequencia = ((double) totalPresencas / totalAulas) * 100.0;
		return formatarPercentual(frequencia);
	}

	private String calcularFrequenciaFinal(int totalPresencas, int totalAulas) {
		return calcularFrequencia(totalPresencas, totalAulas);
	}

	private void escrever(PdfContentByte cb, String texto, float x, float y, int tamanho, boolean negrito,
			int alinhamento) {
		try {
			cb.beginText();
			cb.setFontAndSize(negrito ? fonteNegritoPdf : fonteNormalPdf, tamanho);
			cb.showTextAligned(alinhamento, texto == null ? "" : texto, x, y, 0);
			cb.endText();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void escreverCor(PdfContentByte cb, String texto, float x, float y, int tamanho, boolean negrito,
			int alinhamento, Color cor) {
		try {
			cb.beginText();
			cb.setColorFill(cor);
			cb.setFontAndSize(negrito ? fonteNegritoPdf : fonteNormalPdf, tamanho);
			cb.showTextAligned(alinhamento, texto == null ? "" : texto, x, y, 0);
			cb.endText();
			cb.setColorFill(Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void escreverLimitado(PdfContentByte cb, String texto, float x, float y, int tamanho, boolean negrito,
			int alinhamento, float larguraMaxima) {
		String textoAjustado = ajustarTextoPdf(texto, negrito ? fonteNegritoPdf : fonteNormalPdf, tamanho,
				larguraMaxima);
		escrever(cb, textoAjustado, x, y, tamanho, negrito, alinhamento);
	}

	private String ajustarTextoPdf(String texto, BaseFont fonte, int tamanho, float larguraMaxima) {
		if (texto == null) {
			return "";
		}
		String limpo = texto.trim();

		if (limpo.isEmpty()) {
			return "";
		}
		if (fonte.getWidthPoint(limpo, tamanho) <= larguraMaxima) {
			return limpo;
		}
		while (limpo.length() > 3 && fonte.getWidthPoint(limpo + "...", tamanho) > larguraMaxima) {
			limpo = limpo.substring(0, limpo.length() - 1);
		}
		return limpo.trim() + "...";
	}

	private void linha(PdfContentByte cb, float x1, float y1, float x2, float y2) {
		cb.moveTo(x1, y1);
		cb.lineTo(x2, y2);
		cb.stroke();
	}

	private JLabel criarRotuloInfo(String titulo, String valor, int x, int y, int largura) {
		JLabel label = new JLabel("<html><b style='color:#ff78dc'>" + titulo + "</b><br>" + valor + "</html>");
		label.setForeground(textos);
		label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
		label.setBounds(x, y, largura, 45);
		return label;
	}

	private JComboBox<String> criarCombo(String[] itens) {
		JComboBox<String> combo = new JComboBox<>(itens);
		combo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
		combo.setForeground(textos);
		combo.setBackground(new Color(38, 15, 110));
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
		return combo;
	}

	private void estilizarBotaoAcao(JButton botao) {
		botao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
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

	private String limparNomeArquivo(String nome) {
		return nome.replaceAll("[\\\\/:*?\"<>|]", "").trim();
	}

	private String valorOuVazio(String valor) {
		return valor == null ? "" : valor.trim();
	}
}