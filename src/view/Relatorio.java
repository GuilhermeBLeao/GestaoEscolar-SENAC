package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import database.ConnectionFactory;
import model.Usuario;
import util.SessaoUsuario;

public class Relatorio extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color COR_EXTERNA = new Color(27, 0, 69);

	private static final Color COR_INTERNA = new Color(38, 2, 92);

	private static final Color COR_BORDA = new Color(120, 70, 220);

	private static final Color COR_LABEL = new Color(255, 120, 220);

	private static final Color COR_CAMPO = new Color(25, 6, 75);

	private static final Color COR_TEXTO = Color.WHITE;

	private static final Color PDF_AZUL = new Color(27, 0, 69);

	private static final Color PDF_CINZA = new Color(238, 238, 238);

	private static final Color PDF_LINHA = new Color(210, 210, 210);

	private JTable tabela;

	private DefaultTableModel modeloTabela;

	private TableRowSorter<DefaultTableModel> sorter;

	private JComboBox<String> cbTipo;

	private JComboBox<String> cbStatus;

	private JComboBox<String> cbAno;

	private JComboBox<String> cbTurmaSetor;

	private JLabel lblTotalAlunos;

	private JLabel lblTotalProfessores;

	private JLabel lblTotalFuncionarios;

	private JLabel lblAprovados;

	private JLabel lblReprovados;

	public Relatorio() {

		setTitle("Painel Administrativo");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setMinimumSize(new Dimension(1280, 720));

		setResizable(false);

		Dimension tamanhoTela = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		setBounds(0, 0, tamanhoTela.width, tamanhoTela.height);

		setExtendedState(JFrame.MAXIMIZED_BOTH);

		JPanel painelExterno = new JPanel(new BorderLayout());

		painelExterno.setBackground(COR_EXTERNA);

		painelExterno.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

		JPanel painelInterno = new JPanel(new BorderLayout());

		painelInterno.setBackground(COR_INTERNA);

		painelInterno.setBorder(BorderFactory.createLineBorder(COR_BORDA, 1));

		painelExterno.add(painelInterno, BorderLayout.CENTER);

		criarConteudo(painelInterno);

		setContentPane(painelExterno);

		carregarDadosDoBanco();
	}

	private void criarConteudo(JPanel painel) {

		JPanel painelTopo = new JPanel(new BorderLayout());

		painelTopo.setOpaque(false);

		painelTopo.setBorder(BorderFactory.createEmptyBorder(22, 28, 15, 28));

		JPanel painelTitulos = new JPanel();

		painelTitulos.setOpaque(false);

		painelTitulos.setLayout(new BoxLayout(painelTitulos, BoxLayout.Y_AXIS));

		JLabel lblTitulo = new JLabel("Painel Administrativo");

		lblTitulo.setForeground(COR_TEXTO);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 27));

		JLabel lblSubtitulo = new JLabel("Visualize informações gerais da escola e gere relatórios completos.");

		lblSubtitulo.setForeground(new Color(215, 195, 235));

		lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		painelTitulos.add(lblTitulo);

		painelTitulos.add(Box.createVerticalStrut(4));

		painelTitulos.add(lblSubtitulo);

		JButton btnVoltar = criarBotao("Voltar");

		btnVoltar.addActionListener(e -> dispose());

		painelTopo.add(painelTitulos, BorderLayout.WEST);

		painelTopo.add(btnVoltar, BorderLayout.EAST);

		painel.add(painelTopo, BorderLayout.NORTH);

		JPanel painelCentral = new JPanel(new BorderLayout(0, 15));

		painelCentral.setOpaque(false);

		painelCentral.setBorder(BorderFactory.createEmptyBorder(0, 28, 25, 28));

		painelCentral.add(criarCards(), BorderLayout.NORTH);

		painelCentral.add(criarAreaRelatorio(), BorderLayout.CENTER);

		painel.add(painelCentral, BorderLayout.CENTER);
	}

	private JPanel criarCards() {

		JPanel painel = new JPanel(new GridLayout(1, 5, 12, 0));

		painel.setOpaque(false);

		lblTotalAlunos = criarLabelValorCard();

		lblTotalProfessores = criarLabelValorCard();

		lblTotalFuncionarios = criarLabelValorCard();

		lblAprovados = criarLabelValorCard();

		lblReprovados = criarLabelValorCard();

		painel.add(criarCard("Total de Alunos", lblTotalAlunos));

		painel.add(criarCard("Professores", lblTotalProfessores));

		painel.add(criarCard("Funcionários", lblTotalFuncionarios));

		painel.add(criarCard("Aprovação", lblAprovados));

		painel.add(criarCard("Reprovação", lblReprovados));

		return painel;
	}

	private JLabel criarLabelValorCard() {

		JLabel label = new JLabel("0", SwingConstants.CENTER);

		label.setForeground(Color.WHITE);

		label.setFont(new Font("Segoe UI", Font.BOLD, 25));

		return label;
	}

	private JPanel criarCard(String titulo, JLabel valor) {

		JPanel painel = new JPanel(new BorderLayout());

		painel.setBackground(COR_CAMPO);

		painel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COR_BORDA),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);

		lblTitulo.setForeground(COR_LABEL);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));

		painel.add(lblTitulo, BorderLayout.NORTH);

		painel.add(valor, BorderLayout.CENTER);

		return painel;
	}

	private JPanel criarAreaRelatorio() {

		JPanel painel = new JPanel(new BorderLayout(0, 12));

		painel.setOpaque(false);

		painel.add(criarFiltros(), BorderLayout.NORTH);

		painel.add(criarTabela(), BorderLayout.CENTER);

		return painel;
	}

	private JPanel criarFiltros() {

		JPanel painel = new JPanel(new GridBagLayout());

		painel.setBackground(COR_CAMPO);

		painel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COR_BORDA),
				BorderFactory.createEmptyBorder(12, 15, 12, 15)));

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(4, 6, 4, 6);

		gbc.fill = GridBagConstraints.HORIZONTAL;

		cbTipo = criarCombo(new String[] { "Todos", "Aluno", "Professor", "Funcionário", "Secretária", "Responsável" });

		cbStatus = criarCombo(new String[] { "Todos", "Ativo", "Inativo", "Transferido", "Aprovado", "Reprovado" });

		cbAno = criarCombo(new String[] { "Todos" });

		cbTurmaSetor = criarCombo(new String[] { "Todos" });

		adicionarFiltro(painel, gbc, 0, "Tipo", cbTipo);

		adicionarFiltro(painel, gbc, 2, "Status", cbStatus);

		adicionarFiltro(painel, gbc, 4, "Ano", cbAno);

		adicionarFiltro(painel, gbc, 6, "Turma / Setor", cbTurmaSetor);

		JPanel painelBotoes = new JPanel(new GridLayout(1, 3, 8, 0));

		painelBotoes.setOpaque(false);

		JButton btnGerar = criarBotao("Gerar");

		JButton btnLimpar = criarBotao("Limpar");

		JButton btnExportar = criarBotao("Exportar PDF");

		btnGerar.addActionListener(e -> aplicarFiltros());

		btnLimpar.addActionListener(e -> limparFiltros());

		btnExportar.addActionListener(e -> exportarPDFPremium());

		painelBotoes.add(btnGerar);

		painelBotoes.add(btnLimpar);

		painelBotoes.add(btnExportar);

		gbc.gridx = 8;

		gbc.gridy = 0;

		gbc.gridheight = 2;

		gbc.weightx = 1;

		gbc.fill = GridBagConstraints.NONE;

		gbc.anchor = GridBagConstraints.EAST;

		painel.add(painelBotoes, gbc);

		return painel;
	}

	private void adicionarFiltro(JPanel painel, GridBagConstraints gbc, int coluna, String titulo,
			JComboBox<String> combo) {

		JLabel label = new JLabel(titulo);

		label.setForeground(COR_LABEL);

		label.setFont(new Font("Segoe UI", Font.BOLD, 12));

		gbc.gridx = coluna;

		gbc.gridy = 0;

		gbc.gridheight = 1;

		gbc.weightx = 0;

		gbc.fill = GridBagConstraints.HORIZONTAL;

		painel.add(label, gbc);

		gbc.gridx = coluna;

		gbc.gridy = 1;

		gbc.weightx = 0.15;

		painel.add(combo, gbc);
	}

	private JPanel criarTabela() {

		String[] colunas = { "ID", "Nome", "Tipo", "Turma / Setor", "Status", "Média", "Frequência", "Ano Letivo" };

		modeloTabela = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {

				return false;
			}
		};

		tabela = new JTable(modeloTabela);

		tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		tabela.setRowHeight(28);

		tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		tabela.setForeground(Color.WHITE);

		tabela.setBackground(COR_CAMPO);

		tabela.setGridColor(new Color(80, 50, 130));

		tabela.setSelectionBackground(new Color(100, 50, 160));

		tabela.setSelectionForeground(Color.WHITE);

		tabela.getTableHeader().setBackground(COR_EXTERNA);

		tabela.getTableHeader().setForeground(COR_LABEL);

		tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

		tabela.getTableHeader().setReorderingAllowed(false);

		tabela.getColumnModel().getColumn(0).setPreferredWidth(60);

		tabela.getColumnModel().getColumn(1).setPreferredWidth(230);

		tabela.getColumnModel().getColumn(2).setPreferredWidth(110);

		tabela.getColumnModel().getColumn(3).setPreferredWidth(180);

		tabela.getColumnModel().getColumn(4).setPreferredWidth(110);

		tabela.getColumnModel().getColumn(5).setPreferredWidth(90);

		tabela.getColumnModel().getColumn(6).setPreferredWidth(110);

		tabela.getColumnModel().getColumn(7).setPreferredWidth(100);

		sorter = new TableRowSorter<>(modeloTabela);

		tabela.setRowSorter(sorter);

		JScrollPane scroll = new JScrollPane(tabela);

		scroll.setBorder(BorderFactory.createLineBorder(COR_BORDA));

		scroll.getViewport().setBackground(COR_CAMPO);

		JPanel painel = new JPanel(new BorderLayout());

		painel.setOpaque(false);

		painel.add(scroll, BorderLayout.CENTER);

		return painel;
	}

	private JComboBox<String> criarCombo(String[] valores) {

		JComboBox<String> combo = new JComboBox<>(valores);

		combo.setPreferredSize(new Dimension(145, 34));

		combo.setBackground(COR_CAMPO);

		combo.setForeground(Color.WHITE);

		combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		return combo;
	}

	private JButton criarBotao(String texto) {

		JButton botao = new JButton(texto);

		botao.setFocusPainted(false);

		botao.setForeground(Color.WHITE);

		botao.setBackground(new Color(92, 42, 150));

		botao.setFont(new Font("Segoe UI", Font.BOLD, 12));

		botao.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COR_BORDA),
				BorderFactory.createEmptyBorder(8, 14, 8, 14)));

		botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		return botao;
	}

	private void carregarDadosDoBanco() {

		modeloTabela.setRowCount(0);

		try (Connection conn = ConnectionFactory.getConnection()) {

			carregarAlunos(conn);

			carregarProfessores(conn);

			carregarFuncionarios(conn);

			carregarResponsaveis(conn);

			carregarFiltrosDinamicos(conn);

		} catch (SQLException e) {

			JOptionPane.showMessageDialog(this,
					"Não foi possível carregar os dados " + "do relatório.\n\n" + e.getMessage(),
					"Erro ao carregar relatório", JOptionPane.ERROR_MESSAGE);
		}

		if (sorter != null) {
			sorter.setRowFilter(null);
		}

		atualizarCards();
	}

	private void carregarAlunos(Connection conn) throws SQLException {

		String sql = """
				SELECT
				    a.id_aluno,
				    a.nome,
				    a.situacao,
				    a.turma_id,
				    t.descricao_turma,
				    a.data_cadastro
				FROM aluno a
				LEFT JOIN turma t
				    ON t.id_turma = a.turma_id
				ORDER BY a.nome
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				int idAluno = rs.getInt("id_aluno");

				String nome = rs.getString("nome");

				String situacao = rs.getString("situacao");

				String turma = rs.getString("descricao_turma");

				if (turma == null || turma.isBlank()) {

					turma = "-";
				}

				String media = obterMediaAluno(conn, idAluno);

				String frequencia = obterFrequenciaAluno(conn, idAluno);

				String ano = obterAnoData(rs.getString("data_cadastro"));

				modeloTabela.addRow(new Object[] { idAluno, nome, "Aluno", turma, normalizarStatusAluno(situacao),
						media, frequencia, ano });
			}
		}
	}

	private void carregarProfessores(Connection conn) throws SQLException {

		String sql = """
				SELECT
				    id_professor,
				    nome,
				    ativo
				FROM professor
				ORDER BY nome
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				modeloTabela.addRow(new Object[] { rs.getInt("id_professor"), rs.getString("nome"), "Professor", "-",
						rs.getBoolean("ativo") ? "Ativo" : "Inativo", "-", "-", "-" });
			}
		}
	}

	private void carregarFuncionarios(Connection conn) throws SQLException {

		String sql = """
				SELECT
				    id_funcionario,
				    nome,
				    cargo,
				    setor,
				    ativo,
				    data_contratacao
				FROM funcionario
				ORDER BY nome
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				String cargo = rs.getString("cargo");

				String setor = rs.getString("setor");

				String local = "-";

				if (setor != null && !setor.isBlank()) {

					local = setor;

				} else if (cargo != null && !cargo.isBlank()) {

					local = cargo;
				}

				String tipo = ehSecretaria(cargo, setor) ? "Secretária" : "Funcionário";

				String ano = obterAnoData(rs.getString("data_contratacao"));

				modeloTabela.addRow(new Object[] { rs.getInt("id_funcionario"), rs.getString("nome"), tipo, local,
						rs.getBoolean("ativo") ? "Ativo" : "Inativo", "-", "-", ano });
			}
		}
	}

	private void carregarResponsaveis(Connection conn) throws SQLException {

		String sql = """
				SELECT
				    id_pais,
				    COALESCE(
				        NULLIF(TRIM(nome_mae), ''),
				        NULLIF(TRIM(nome_pai), '')
				    ) AS nome_responsavel,
				    ativo
				FROM pais_aluno
				ORDER BY nome_responsavel
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				String nome = rs.getString("nome_responsavel");

				if (nome == null || nome.isBlank()) {

					nome = "Responsável não identificado";
				}

				modeloTabela.addRow(new Object[] { rs.getInt("id_pais"), nome, "Responsável", "-",
						rs.getBoolean("ativo") ? "Ativo" : "Inativo", "-", "-", "-" });
			}
		}
	}

	private String obterMediaAluno(Connection conn, int idAluno) throws SQLException {

		String sql = """
				SELECT AVG(nota) AS media
				FROM nota
				WHERE aluno_id = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, idAluno);

			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {

					double media = rs.getDouble("media");

					if (!rs.wasNull()) {

						return String.format("%.2f", media);
					}
				}
			}
		}

		return "-";
	}

	private String obterFrequenciaAluno(Connection conn, int idAluno) throws SQLException {

		String sql = """
				SELECT
				    COUNT(*) AS total,
				    SUM(
				        CASE
				            WHEN presente = true THEN 1
				            ELSE 0
				        END
				    ) AS presentes
				FROM presenca
				WHERE aluno_id = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, idAluno);

			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {

					int total = rs.getInt("total");

					if (total == 0) {
						return "100,00%";
					}

					int presentes = rs.getInt("presentes");

					double percentual = presentes * 100.0 / total;

					return String.format("%.2f%%", percentual);
				}
			}
		}

		return "100,00%";
	}

	private String obterAnoData(String valor) {

		if (valor == null || valor.isBlank()) {

			return "-";
		}

		String tratado = valor.trim();

		if (tratado.length() >= 4) {

			String ano = tratado.substring(0, 4);

			if (ano.matches("\\d{4}")) {
				return ano;
			}
		}

		return "-";
	}

	private String normalizarStatusAluno(String situacao) {

		if (situacao == null || situacao.isBlank()) {

			return "Inativo";
		}

		String valor = situacao.trim().toUpperCase();

		if ("ATIVO".equals(valor)) {
			return "Ativo";
		}

		if ("TRANSFERIDO".equals(valor)) {
			return "Transferido";
		}

		if ("INATIVO".equals(valor)) {
			return "Inativo";
		}

		return situacao;
	}

	private boolean ehSecretaria(String cargo, String setor) {

		StringBuilder texto = new StringBuilder();

		if (cargo != null) {
			texto.append(" ").append(cargo);
		}

		if (setor != null) {
			texto.append(" ").append(setor);
		}

		return texto.toString().toLowerCase().contains("secret");
	}

	private void carregarFiltrosDinamicos(Connection conn) throws SQLException {

		carregarAnos(conn);

		carregarTurmasESetores(conn);
	}

	private void carregarAnos(Connection conn) throws SQLException {

		cbAno.removeAllItems();

		cbAno.addItem("Todos");

		List<String> anos = new ArrayList<>();

		adicionarAnos(conn, """
				SELECT DISTINCT
				    strftime(
				        '%Y',
				        data_cadastro
				    ) AS ano
				FROM aluno
				WHERE data_cadastro IS NOT NULL
				""", anos);

		adicionarAnos(conn, """
				SELECT DISTINCT
				    strftime(
				        '%Y',
				        data_contratacao
				    ) AS ano
				FROM funcionario
				WHERE data_contratacao IS NOT NULL
				""", anos);

		anos.sort((a, b) -> b.compareTo(a));

		for (String ano : anos) {
			cbAno.addItem(ano);
		}
	}

	private void adicionarAnos(Connection conn, String sql, List<String> anos) throws SQLException {

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				String ano = rs.getString("ano");

				if (ano != null && !ano.isBlank() && !anos.contains(ano)) {

					anos.add(ano);
				}
			}
		}
	}

	private void carregarTurmasESetores(Connection conn) throws SQLException {

		cbTurmaSetor.removeAllItems();

		cbTurmaSetor.addItem("Todos");

		List<String> valores = new ArrayList<>();

		String sqlTurmas = """
				SELECT DISTINCT
				    descricao_turma
				FROM turma
				WHERE descricao_turma IS NOT NULL
				ORDER BY descricao_turma
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sqlTurmas); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				String valor = rs.getString("descricao_turma");

				if (valor != null && !valor.isBlank() && !valores.contains(valor)) {

					valores.add(valor);
				}
			}
		}

		String sqlSetores = """
				SELECT DISTINCT
				    setor
				FROM funcionario
				WHERE setor IS NOT NULL
				AND TRIM(setor) <> ''
				ORDER BY setor
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sqlSetores); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				String valor = rs.getString("setor");

				if (valor != null && !valor.isBlank() && !valores.contains(valor)) {

					valores.add(valor);
				}
			}
		}

		valores.sort(String.CASE_INSENSITIVE_ORDER);

		for (String valor : valores) {
			cbTurmaSetor.addItem(valor);
		}
	}

	private void aplicarFiltros() {

		aplicarFiltroVisual();

		atualizarCards();
	}

	private void aplicarFiltroVisual() {

		if (sorter == null) {
			return;
		}

		String tipo = String.valueOf(cbTipo.getSelectedItem());

		String status = String.valueOf(cbStatus.getSelectedItem());

		String ano = String.valueOf(cbAno.getSelectedItem());

		String turmaSetor = String.valueOf(cbTurmaSetor.getSelectedItem());

		List<RowFilter<Object, Object>> filtros = new ArrayList<>();

		if (!"Todos".equals(tipo)) {

			filtros.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(tipo) + "$", 2));
		}

		if (!"Todos".equals(status)) {

			if ("Aprovado".equals(status) || "Reprovado".equals(status)) {

				filtros.add(new RowFilter<Object, Object>() {

					@Override
					public boolean include(Entry<? extends Object, ? extends Object> entry) {

						String tipoLinha = String.valueOf(entry.getValue(2));

						if (!"Aluno".equals(tipoLinha)) {

							return false;
						}

						String mediaTexto = String.valueOf(entry.getValue(5));

						if ("-".equals(mediaTexto)) {

							return false;
						}

						try {

							double media = Double.parseDouble(mediaTexto.replace(',', '.'));

							if ("Aprovado".equals(status)) {

								return media >= 6.0;
							}

							return media < 6.0;

						} catch (NumberFormatException e) {

							return false;
						}
					}
				});
			} else {

				filtros.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(status) + "$", 4));
			}
		}

		if (!"Todos".equals(ano)) {

			filtros.add(RowFilter.regexFilter("^" + Pattern.quote(ano) + "$", 7));
		}

		if (!"Todos".equals(turmaSetor)) {

			filtros.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(turmaSetor) + "$", 3));
		}

		if (filtros.isEmpty()) {

			sorter.setRowFilter(null);

		} else {

			sorter.setRowFilter(RowFilter.andFilter(filtros));
		}
	}

	private void limparFiltros() {

		cbTipo.setSelectedItem("Todos");

		cbStatus.setSelectedItem("Todos");

		cbAno.setSelectedItem("Todos");

		cbTurmaSetor.setSelectedItem("Todos");

		carregarDadosDoBanco();
	}

	private void atualizarCards() {

		int alunos = 0;

		int professores = 0;

		int funcionarios = 0;

		int aprovados = 0;

		int reprovados = 0;

		for (int linha = 0; linha < tabela.getRowCount(); linha++) {

			String tipo = String.valueOf(tabela.getValueAt(linha, 2));

			if ("Aluno".equals(tipo)) {

				alunos++;

				String mediaTexto = String.valueOf(tabela.getValueAt(linha, 5));

				if (!"-".equals(mediaTexto)) {

					try {

						double media = Double.parseDouble(mediaTexto.replace(',', '.'));

						if (media >= 6.0) {

							aprovados++;

						} else {

							reprovados++;
						}

					} catch (NumberFormatException ignored) {
					}
				}

			} else if ("Professor".equals(tipo)) {

				professores++;

			} else if ("Funcionário".equals(tipo) || "Secretária".equals(tipo)) {

				funcionarios++;
			}
		}

		lblTotalAlunos.setText(String.valueOf(alunos));

		lblTotalProfessores.setText(String.valueOf(professores));

		lblTotalFuncionarios.setText(String.valueOf(funcionarios));

		lblAprovados.setText(String.valueOf(aprovados));

		lblReprovados.setText(String.valueOf(reprovados));
	}

	private void exportarPDFPremium() {

		if (tabela.getRowCount() == 0) {

			JOptionPane.showMessageDialog(this, "Não existem dados para exportar.", "Exportação",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		File pasta = new File(System.getProperty("user.dir"), "Relatórios");

		if (!pasta.exists() && !pasta.mkdirs()) {

			JOptionPane.showMessageDialog(this, "Não foi possível criar a pasta de relatórios.", "Erro",
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		String dataArquivo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));

		File arquivo = new File(pasta, "Relatorio_Administrativo_" + dataArquivo + ".pdf");

		Document documento = new Document(PageSize.A4.rotate(), 25, 25, 35, 35);

		try {

			PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(arquivo));

			writer.setPageEvent(new RodapePDF());

			documento.open();

			adicionarCabecalhoPDF(documento);

			adicionarResumoPDF(documento);

			adicionarFiltrosPDF(documento);

			adicionarTabelaPDF(documento);

			documento.close();

			JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso.\n\n" + arquivo.getAbsolutePath(),
					"Exportação concluída", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {

			if (documento.isOpen()) {
				documento.close();
			}

			JOptionPane.showMessageDialog(this, "Não foi possível gerar o PDF.\n\n" + e.getMessage(),
					"Erro ao exportar PDF", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void adicionarCabecalhoPDF(Document documento) throws Exception {

		try {
		    // Carrega a URL da imagem pelo Classpath (a partir da pasta de recursos)
		    java.net.URL logoUrl = getClass().getResource("/Images/Solo-Firme.png");

		    if (logoUrl != null) {
		        // O iText aceita a URL do recurso diretamente
		        Image logo = Image.getInstance(logoUrl);
		        logo.scaleToFit(80, 80);
		        logo.setAlignment(Image.ALIGN_CENTER);
		        documento.add(logo);
		    } else {
		        System.err.println("Erro: Imagem '/Images/Solo-Firme.png' não encontrada no classpath!");
		    }
		} catch (Exception e) {
		    // Imprime o erro no console para facilitar a depuração
		    e.printStackTrace();
		}

		Paragraph escola = new Paragraph("E.E.B. Solo Firme",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PDF_AZUL));

		escola.setAlignment(Element.ALIGN_CENTER);

		documento.add(escola);

		Paragraph titulo = new Paragraph("RELATÓRIO ADMINISTRATIVO",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, PDF_AZUL));

		titulo.setAlignment(Element.ALIGN_CENTER);

		titulo.setSpacingBefore(8);

		titulo.setSpacingAfter(5);

		documento.add(titulo);

		String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

		Paragraph emissao = new Paragraph("Emitido em: " + data,
				FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));

		emissao.setAlignment(Element.ALIGN_CENTER);

		documento.add(emissao);

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		String nomeUsuario = "Usuário não identificado";

		if (usuario != null) {
			nomeUsuario = obterNomeUsuario(usuario);
		}

		Paragraph responsavel = new Paragraph("Usuário responsável: " + nomeUsuario,
				FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));

		responsavel.setAlignment(Element.ALIGN_CENTER);

		responsavel.setSpacingAfter(12);

		documento.add(responsavel);
	}

	private String obterNomeUsuario(Usuario usuario) {

		try (Connection conn = ConnectionFactory.getConnection()) {

			switch (usuario.getTipoUsuario()) {

			case ALUNO:

				return buscarNome(conn, """
						SELECT nome
						FROM aluno
						WHERE id_aluno = ?
						""", usuario.getAlunoId());

			case PROFESSOR:

				return buscarNome(conn, """
						SELECT nome
						FROM professor
						WHERE id_professor = ?
						""", usuario.getProfessorId());

			case RESPONSAVEL:

				return buscarNome(conn, """
						SELECT
						    COALESCE(
						        NULLIF(TRIM(nome_mae), ''),
						        NULLIF(TRIM(nome_pai), '')
						    ) AS nome
						FROM pais_aluno
						WHERE id_pais = ?
						""", usuario.getPaiId());

			default:

				return buscarNome(conn, """
						SELECT nome
						FROM funcionario
						WHERE id_funcionario = ?
						""", usuario.getFuncionarioId());
			}

		} catch (Exception e) {

			return "Usuário não identificado";
		}
	}

	private String buscarNome(Connection conn, String sql, int id) throws SQLException {

		if (id <= 0) {
			return "Usuário não identificado";
		}

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);

			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {

					String nome = rs.getString("nome");

					if (nome != null && !nome.isBlank()) {

						return nome;
					}
				}
			}
		}

		return "Usuário não identificado";
	}

	private void adicionarResumoPDF(Document documento) throws Exception {

		Paragraph titulo = new Paragraph("Resumo", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PDF_AZUL));

		titulo.setSpacingAfter(5);

		documento.add(titulo);

		PdfPTable resumo = new PdfPTable(5);

		resumo.setWidthPercentage(100);

		resumo.setSpacingAfter(12);

		adicionarCelulaResumo(resumo, "Alunos", lblTotalAlunos.getText());

		adicionarCelulaResumo(resumo, "Professores", lblTotalProfessores.getText());

		adicionarCelulaResumo(resumo, "Funcionários", lblTotalFuncionarios.getText());

		adicionarCelulaResumo(resumo, "Aprovados", lblAprovados.getText());

		adicionarCelulaResumo(resumo, "Reprovados", lblReprovados.getText());

		documento.add(resumo);
	}

	private void adicionarCelulaResumo(PdfPTable tabelaPDF, String titulo, String valor) {

		PdfPCell celula = new PdfPCell(
				new Phrase(titulo + "\n" + valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_AZUL)));

		celula.setBackgroundColor(PDF_CINZA);

		celula.setHorizontalAlignment(Element.ALIGN_CENTER);

		celula.setPadding(7);

		tabelaPDF.addCell(celula);
	}

	private void adicionarFiltrosPDF(Document documento) throws Exception {

		String tipo = String.valueOf(cbTipo.getSelectedItem());

		String status = String.valueOf(cbStatus.getSelectedItem());

		String ano = String.valueOf(cbAno.getSelectedItem());

		String turmaSetor = String.valueOf(cbTurmaSetor.getSelectedItem());

		Paragraph filtros = new Paragraph(
				"Filtros utilizados: " + "Tipo = " + tipo + " | Status = " + status + " | Ano = " + ano
						+ " | Turma / Setor = " + turmaSetor,
				FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY));

		filtros.setSpacingAfter(10);

		documento.add(filtros);
	}

	private void adicionarTabelaPDF(Document documento) throws Exception {

		PdfPTable tabelaPDF = new PdfPTable(8);

		tabelaPDF.setWidthPercentage(100);

		tabelaPDF.setWidths(new float[] { 0.6f, 2.7f, 1.1f, 2.0f, 1.2f, 1.0f, 1.2f, 1.0f });

		String[] colunas = { "ID", "Nome", "Tipo", "Turma / Setor", "Status", "Média", "Frequência", "Ano Letivo" };

		for (String coluna : colunas) {

			PdfPCell celula = new PdfPCell(
					new Phrase(coluna, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));

			celula.setBackgroundColor(PDF_AZUL);

			celula.setHorizontalAlignment(Element.ALIGN_CENTER);

			celula.setPadding(5);

			tabelaPDF.addCell(celula);
		}

		for (int linha = 0; linha < tabela.getRowCount(); linha++) {

			for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {

				Object valor = tabela.getValueAt(linha, coluna);

				PdfPCell celula = new PdfPCell(new Phrase(valor == null ? "-" : valor.toString(),
						FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK)));

				celula.setBorderColor(PDF_LINHA);

				celula.setPadding(4);

				if (linha % 2 == 0) {

					celula.setBackgroundColor(Color.WHITE);

				} else {

					celula.setBackgroundColor(new Color(248, 248, 248));
				}

				tabelaPDF.addCell(celula);
			}
		}

		documento.add(tabelaPDF);

		Paragraph total = new Paragraph("Total de registros exibidos: " + tabela.getRowCount(),
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_AZUL));

		total.setAlignment(Element.ALIGN_RIGHT);

		total.setSpacingBefore(8);

		documento.add(total);
	}

	private static class RodapePDF extends PdfPageEventHelper {

		@Override
		public void onEndPage(PdfWriter writer, Document document) {

			PdfContentByte canvas = writer.getDirectContent();

			String texto = "E.E.B. Solo Firme - Relatório Administrativo";

			canvas.saveState();

			canvas.setColorStroke(PDF_LINHA);

			canvas.moveTo(document.left(), document.bottom() - 10);

			canvas.lineTo(document.right(), document.bottom() - 10);

			canvas.stroke();

			canvas.beginText();

			canvas.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA).getBaseFont(), 7);

			canvas.setColorFill(Color.DARK_GRAY);

			canvas.showTextAligned(Element.ALIGN_LEFT, texto, document.left(), document.bottom() - 22, 0);

			canvas.showTextAligned(Element.ALIGN_RIGHT, "Página " + writer.getPageNumber(), document.right(),
					document.bottom() - 22, 0);

			canvas.endText();

			canvas.restoreState();
		}
	}
}