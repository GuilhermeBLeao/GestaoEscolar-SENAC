package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.MaskFormatter;

import controller.AdvertenciaController;
import dao.AdvertenciaDAO;
import dao.AlunoDAO;
import dao.ProfessorDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Advertencia;
import model.Aluno;
import model.Professor;
import model.Turma;
import model.Usuario;
import util.DadosSistema;
import util.SessaoUsuario;

public class AdvertenciaSecretaria extends JFrame {
	private static final long serialVersionUID = 1L;

	private JTextField txtNomeAluno;
	private JTable tabelaAdvertencias;
	private JComboBox<String> comboTurma;
	private JComboBox<String> comboStatus;
	private DefaultTableModel modeloTabela;
	private JFormattedTextField txtMatricula;
	private JTextField txtNomeAlunoDetalhe;
	private JTextField txtMatriculaDetalhe;
	private JTextField txtTurmaDetalhe;
	private JTextField txtCursoDetalhe;
	private JTextField txtResponsavelDetalhe;
	private JTextField txtTelefoneDetalhe;
	private JFormattedTextField txtData;
	private JTextField txtFuncionario;
	private JComboBox<String> comboTipo;
	private JTextArea areaDescricao;
	private JTextArea areaProvidencias;
	private JCheckBox chkAluno;
	private JCheckBox chkResponsavel;
	private final List<Advertencia> advertencias = new ArrayList<>();
	private final List<Advertencia> advertenciasExibidas = new ArrayList<>();
	private final List<Aluno> alunos = new ArrayList<>();
	private final List<Turma> turmas = new ArrayList<>();
	private int professorResponsavelId;
	private JLabel lblTotal;
	private JLabel lblEsteMes;
	private JLabel lblPendentes;
	private JLabel lblCanceladas;
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final Color textos = Color.WHITE;
	private static final int LARGURA_CARD = 1220;
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);

	public AdvertenciaSecretaria() {
		setTitle("Advertências - Secretaria");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(false);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);

		Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int largura = area.width - 60;
		int altura = area.height - 60;

		JPanel interno = new JPanel(null);
		interno.setBounds(20, 20, largura, altura);
		interno.setBackground(corInterna);
		externo.add(interno);

		criarConteudo(interno, largura, altura);
		carregarDadosDoBanco();
	}

	private void criarConteudo(JPanel interno, int largura, int altura) {
		JPanel topo = criarTopo();
		topo.setBounds(30, 25, largura - 60, 160);
		interno.add(topo);
		int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 80);

		JPanel painelRolagem = new JPanel(null);
		painelRolagem.setBackground(corInterna);
		painelRolagem.setPreferredSize(new Dimension(larguraConteudo, 2310));

		JScrollPane scroll = new JScrollPane(painelRolagem);
		scroll.setBounds(30, 210, largura - 60, altura - 240);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		scroll.setBorder(null);
		interno.add(scroll);

		criarResumo(painelRolagem, larguraConteudo);
		criarPesquisa(painelRolagem, larguraConteudo);
		criarHistorico(painelRolagem, larguraConteudo);
		criarDadosAluno(painelRolagem, larguraConteudo);
		criarCadastroAdvertencia(painelRolagem, larguraConteudo);
		criarDescricao(painelRolagem, larguraConteudo);
		criarProvidencias(painelRolagem, larguraConteudo);
		criarAssinaturas(painelRolagem, larguraConteudo);
		criarAcoes(painelRolagem, larguraConteudo);
	}

	private int centerX(int larguraConteudo) {
		return (larguraConteudo - LARGURA_CARD) / 2;
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(),
						new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				g2.dispose();
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Advertências");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 80, 500, 45);
		topo.add(titulo);

		JLabel subtitulo = new JLabel("Registro disciplinar dos alunos");
		subtitulo.setForeground(new Color(245, 225, 255));
		subtitulo.setBounds(45, 120, 500, 25);
		topo.add(subtitulo);
		return topo;
	}

	private void criarResumo(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Resumo Geral");
		card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);
		lblTotal = adicionarIndicador(card, "Total", "0", 60, 60, Color.CYAN);
		lblEsteMes = adicionarIndicador(card, "Este Mês", "0", 340, 60, Color.GREEN);
		lblPendentes = adicionarIndicador(card, "Pendentes", "0", 650, 60, Color.ORANGE);
		lblCanceladas = adicionarIndicador(card, "Canceladas", "0", 930, 60, Color.RED);
		painel.add(card);
	}

	private void criarPesquisa(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Pesquisa");
		card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 180);

		JLabel lblNome = criarLabelCampo("Aluno");
		lblNome.setBounds(30, 50, 120, 25);
		card.add(lblNome);
		txtNomeAluno = new JTextField();
		txtNomeAluno.setBounds(30, 80, 280, 35);
		estilizarCampo(txtNomeAluno);
		card.add(txtNomeAluno);

		JLabel lblMatricula = criarLabelCampo("Matrícula");
		lblMatricula.setBounds(340, 50, 120, 25);
		card.add(lblMatricula);
		txtMatricula = criarCampoMascara("##########");
		txtMatricula.setBounds(340, 80, 180, 35);
		card.add(txtMatricula);

		JLabel lblTurma = criarLabelCampo("Turma");
		lblTurma.setBounds(550, 50, 120, 25);
		card.add(lblTurma);
		comboTurma = new JComboBox<>(new String[] { "Todas" });
		comboTurma.setBounds(550, 80, 150, 35);
		estilizarCombo(comboTurma);
		card.add(comboTurma);

		JLabel lblStatus = criarLabelCampo("Status");
		lblStatus.setBounds(730, 50, 120, 25);
		card.add(lblStatus);
		comboStatus = new JComboBox<>(new String[] { "Todos", "Ativa", "Assinada", "Cancelada" });
		comboStatus.setBounds(730, 80, 180, 35);
		estilizarCombo(comboStatus);
		card.add(comboStatus);

		JButton btnPesquisar = new JButton("Pesquisar");
		btnPesquisar.setBounds(960, 45, 180, 35);
		estilizarBotao(btnPesquisar);
		btnPesquisar.addActionListener(e -> pesquisar());
		card.add(btnPesquisar);

		JButton btnNova = new JButton("Nova Advertência");
		btnNova.setBounds(960, 90, 180, 35);
		estilizarBotao(btnNova);
		btnNova.addActionListener(e -> limparFormulario());
		card.add(btnNova);
		painel.add(card);
	}

	private void criarHistorico(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Histórico de Advertências");
		card.setBounds(centerX(larguraConteudo), 390, LARGURA_CARD, 350);
		String[] colunas = { "ID", "Matrícula", "Aluno", "Data", "Motivo", "Status" };
		modeloTabela = new DefaultTableModel(colunas, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tabelaAdvertencias = new JTable(modeloTabela);
		tabelaAdvertencias.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				preencherFormularioComSelecionada();
			}
		});
		tabelaAdvertencias.setRowHeight(30);
		tabelaAdvertencias.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		tabelaAdvertencias.setForeground(textos);
		tabelaAdvertencias.setBackground(corInterna);
		tabelaAdvertencias.setGridColor(new Color(90, 50, 170));
		tabelaAdvertencias.setSelectionBackground(new Color(80, 40, 160));
		tabelaAdvertencias.setSelectionForeground(textos);
		tabelaAdvertencias.setShowGrid(true);
		tabelaAdvertencias.setShowHorizontalLines(true);
		tabelaAdvertencias.setShowVerticalLines(true);
		tabelaAdvertencias.setIntercellSpacing(new Dimension(1, 1));
		tabelaAdvertencias.setFillsViewportHeight(false);
		tabelaAdvertencias.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaAdvertencias.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 16));
		header.setBackground(corCampo);
		header.setForeground(textos);
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);
		header.setPreferredSize(new Dimension(header.getWidth(), 34));

		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
		centro.setHorizontalAlignment(SwingConstants.CENTER);
		centro.setVerticalAlignment(SwingConstants.CENTER);
		centro.setBackground(corCampo);
		centro.setForeground(textos);
		centro.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		for (int i = 0; i < tabelaAdvertencias.getColumnCount(); i++) {
			tabelaAdvertencias.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JScrollPane scrollTabela = new JScrollPane(tabelaAdvertencias);
		scrollTabela.setBounds(25, 60, 1170, 250);
		scrollTabela.getViewport().setBackground(corInterna);
		scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scrollTabela);
		painel.add(card);
	}

	private void criarDadosAluno(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Dados do Aluno");
		card.setBounds(centerX(larguraConteudo), 760, LARGURA_CARD, 250);
		txtNomeAlunoDetalhe = adicionarCampoAluno(card, "Nome Completo", "", 30, 60);
		txtMatriculaDetalhe = adicionarCampoAluno(card, "Matrícula", "", 430, 60);
		txtTurmaDetalhe = adicionarCampoAluno(card, "Turma", "", 830, 60);
		txtCursoDetalhe = adicionarCampoAluno(card, "Curso", "", 30, 140);
		txtResponsavelDetalhe = adicionarCampoAluno(card, "Responsável", "", 430, 140);
		txtTelefoneDetalhe = adicionarCampoAluno(card, "Telefone", "", 830, 140);
		painel.add(card);
	}

	private void criarCadastroAdvertencia(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Cadastro da Advertência");
		card.setBounds(centerX(larguraConteudo), 1030, LARGURA_CARD, 280);
		
		JLabel lblData = criarLabelCampo("Data");
		lblData.setBounds(30, 60, 120, 25);
		card.add(lblData);
		txtData = criarCampoMascara("##/##/####");
		txtData.setBounds(30, 90, 220, 35);
		card.add(txtData);

		JLabel lblFuncionario = criarLabelCampo("Funcionário Responsável");
		lblFuncionario.setBounds(300, 60, 220, 25);
		card.add(lblFuncionario);
		txtFuncionario = new JTextField();
		txtFuncionario.setBounds(300, 90, 350, 35);
		estilizarCampo(txtFuncionario);
		card.add(txtFuncionario);

		JLabel lblTipo = criarLabelCampo("Tipo da Advertência");
		lblTipo.setBounds(700, 60, 200, 25);
		card.add(lblTipo);
		comboTipo = new JComboBox<>();
		comboTipo.setEditable(true);
		comboTipo.setBounds(700, 90, 300, 35);
		estilizarCombo(comboTipo);
		card.add(comboTipo);
		painel.add(card);
	}

	private void criarDescricao(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Descrição da Ocorrência");
		card.setBounds(centerX(larguraConteudo), 1330, LARGURA_CARD, 300);
		areaDescricao = new JTextArea();
		areaDescricao.setLineWrap(true);
		areaDescricao.setWrapStyleWord(true);
		areaDescricao.setBackground(corCampo);
		areaDescricao.setForeground(textos);
		areaDescricao.setCaretColor(textos);

		JScrollPane scroll = new JScrollPane(areaDescricao);
		scroll.setBounds(25, 60, 1170, 210);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void criarProvidencias(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Providências");
		card.setBounds(centerX(larguraConteudo), 1650, LARGURA_CARD, 250);
		areaProvidencias = new JTextArea();
		areaProvidencias.setLineWrap(true);
		areaProvidencias.setWrapStyleWord(true);
		areaProvidencias.setBackground(corCampo);
		areaProvidencias.setForeground(textos);
		areaProvidencias.setCaretColor(textos);

		JScrollPane scroll = new JScrollPane(areaProvidencias);
		scroll.setBounds(25, 60, 1170, 160);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void criarAssinaturas(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Assinaturas");
		card.setBounds(centerX(larguraConteudo), 1920, LARGURA_CARD, 180);
		chkAluno = new JCheckBox("Assinatura do Aluno Recebida");
		chkAluno.setBounds(40, 60, 350, 30);
		chkAluno.setBackground(corCampo);
		chkAluno.setForeground(textos);
		card.add(chkAluno);
		chkResponsavel = new JCheckBox("Assinatura do Responsável Recebida");
		chkResponsavel.setBounds(40, 100, 400, 30);
		chkResponsavel.setBackground(corCampo);
		chkResponsavel.setForeground(textos);
		card.add(chkResponsavel);
		painel.add(card);
	}

	private void criarAcoes(JPanel painel, int larguraConteudo) {
		JPanel card = criarCardSecao("Ações");
		card.setBounds(centerX(larguraConteudo), 2120, LARGURA_CARD, 150);
		JButton btnRegistrar = new JButton("Registrar");
		JButton btnSalvar = new JButton("Salvar");
		JButton btnImprimir = new JButton("Imprimir");
		JButton btnCancelar = new JButton("Cancelar");
		JButton[] botoes = { btnRegistrar, btnSalvar, btnImprimir, btnCancelar };
		int x = 30;
		for (JButton b : botoes) {
			b.setBounds(x, 60, 250, 40);
			estilizarBotao(b);
			card.add(b);
			x += 290;
		}

		painel.add(card);
		btnRegistrar.addActionListener(e -> salvarAdvertencia());
		btnSalvar.addActionListener(e -> salvarAdvertencia());
		btnImprimir.addActionListener(e -> imprimirTabela());
		btnCancelar.addActionListener(e -> dispose());
	}

	private void carregarDadosDoBanco() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			AdvertenciaDAO advertenciaDAO = new AdvertenciaDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			ProfessorDAO professorDAO = new ProfessorDAO(conn);
			advertencias.clear();
			advertencias.addAll(advertenciaDAO.listar());
			comboTipo.removeAllItems();

			for (String motivo : new LinkedHashSet<>(advertencias.stream().map(Advertencia::getMotivo).toList())) {
				comboTipo.addItem(motivo);
			}
			turmas.clear();
			turmas.addAll(turmaDAO.listarAtivas());
			alunos.clear();
			
			for (Turma turma : turmas) {
				alunos.addAll(alunoDAO.listarPorTurma(turma.getIdTurma()));
			}
			comboTurma.removeAllItems();
			comboTurma.addItem("Todas");

			for (Turma turma : turmas) {
				comboTurma.addItem(descricaoTurma(turma));
			}
			professorResponsavelId = professorDaSessao();

			if (professorResponsavelId <= 0) {
				for (Professor professor : professorDAO.listarTodos()) {
					if (professor.isAtivo()) {
						professorResponsavelId = professor.getIdProfessor();
						break;
					}
				}
			}
			txtFuncionario.setText(DadosSistema.nomeUsuarioLogado());
			pesquisar();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os dados " + "do banco de dados.", ex);
		}
	}

	private int professorDaSessao() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		return usuario == null ? 0 : usuario.getProfessorId();
	}

	private void pesquisar() {
		String nome = txtNomeAluno.getText().trim().toLowerCase();
		String matricula = txtMatricula.getText().trim();
		String status = String.valueOf(comboStatus.getSelectedItem());
		Turma turmaSelecionada = turmaSelecionada();
		modeloTabela.setRowCount(0);
		advertenciasExibidas.clear();

		for (Advertencia advertencia : advertencias) {
			Aluno aluno = alunoPorId(advertencia.getAlunoId());
			if (aluno == null || (!nome.isEmpty() && !aluno.getNome().toLowerCase().contains(nome))
					|| (!matricula.isEmpty() && !aluno.getMatricula().equals(matricula))
					|| (turmaSelecionada != null && advertencia.getTurmaId() != turmaSelecionada.getIdTurma())
					|| (!"Todos".equals(status) && !status.equals(advertencia.getSituacao()))) {
				continue;
			}
			advertenciasExibidas.add(advertencia);
			modeloTabela.addRow(new Object[] { advertencia.getIdAdvertencia(), aluno.getMatricula(), aluno.getNome(),
					advertencia.getDataAdvertencia().format(FORMATO_DATA), advertencia.getMotivo(),
					advertencia.getSituacao() });
		}
		atualizarResumo();

		if (advertenciasExibidas.size() == 1) {

			tabelaAdvertencias.setRowSelectionInterval(0, 0);
		}
	}

	private Turma turmaSelecionada() {

		Object valor = comboTurma.getSelectedItem();

		if (valor == null || "Todas".equals(valor.toString())) {

			return null;
		}

		for (Turma turma : turmas) {

			if (descricaoTurma(turma).equals(valor.toString())) {

				return turma;
			}
		}

		return null;
	}

	private String descricaoTurma(Turma turma) {

		return turma.getIdTurma() + " - " + turma.getDescricaoTurma();
	}

	private Aluno alunoPorId(int idAluno) {

		for (Aluno aluno : alunos) {

			if (aluno.getIdAluno() == idAluno) {

				return aluno;
			}
		}

		return null;
	}

	private void atualizarResumo() {

		LocalDate hoje = LocalDate.now();

		long esteMes = advertencias.stream().filter(a -> a.getDataAdvertencia().getMonth() == hoje.getMonth()
				&& a.getDataAdvertencia().getYear() == hoje.getYear()).count();

		long pendentes = advertencias.stream().filter(a -> "Ativa".equalsIgnoreCase(a.getSituacao())).count();

		long canceladas = advertencias.stream().filter(a -> "Cancelada".equalsIgnoreCase(a.getSituacao())).count();

		lblTotal.setText(String.valueOf(advertencias.size()));

		lblEsteMes.setText(String.valueOf(esteMes));

		lblPendentes.setText(String.valueOf(pendentes));

		lblCanceladas.setText(String.valueOf(canceladas));
	}

	private void preencherFormularioComSelecionada() {

		int linha = tabelaAdvertencias.getSelectedRow();

		if (linha < 0 || linha >= advertenciasExibidas.size()) {

			return;
		}

		Advertencia advertencia = advertenciasExibidas.get(linha);

		Aluno aluno = alunoPorId(advertencia.getAlunoId());

		if (aluno != null) {

			preencherDadosAluno(aluno);
		}

		txtData.setText(advertencia.getDataAdvertencia().format(FORMATO_DATA));

		comboTipo.setSelectedItem(advertencia.getMotivo());

		areaDescricao.setText(advertencia.getDescricao());

		areaProvidencias.setText(advertencia.getOrientacao());
	}

	private void preencherDadosAluno(Aluno aluno) {

		txtNomeAlunoDetalhe.setText(aluno.getNome());

		txtMatriculaDetalhe.setText(aluno.getMatricula());

		Turma turma = turmaPorId(aluno.getIdTurma());

		txtTurmaDetalhe.setText(turma == null ? "" : turma.getDescricaoTurma());

		txtCursoDetalhe.setText("Não informado");

		txtResponsavelDetalhe.setText("Não informado");

		txtTelefoneDetalhe.setText(aluno.getTelefone());
	}

	private Turma turmaPorId(int idTurma) {

		for (Turma turma : turmas) {

			if (turma.getIdTurma() == idTurma) {

				return turma;
			}
		}

		return null;
	}

	private void salvarAdvertencia() {

		try {

			int linha = tabelaAdvertencias.getSelectedRow();

			Aluno aluno = linha >= 0 && linha < advertenciasExibidas.size()
					? alunoPorId(advertenciasExibidas.get(linha).getAlunoId())
					: alunoPorMatricula(txtMatricula.getText().trim());

			if (aluno == null) {

				throw new IllegalArgumentException(
						"Selecione um aluno na tabela " + "ou informe uma matrícula válida.");
			}

			if (professorResponsavelId <= 0) {

				throw new IllegalArgumentException(
						"Não há professor responsável ativo " + "cadastrado para registrar " + "a advertência.");
			}

			LocalDate data = LocalDate.parse(txtData.getText().trim(), FORMATO_DATA);

			Turma turma = turmaPorId(aluno.getIdTurma());

			if (turma == null) {

				throw new IllegalArgumentException("A turma do aluno não está disponível " + "no banco de dados.");
			}

			Advertencia advertencia = new Advertencia();

			advertencia.setAlunoId(aluno.getIdAluno());

			advertencia.setTurmaId(turma.getIdTurma());

			advertencia.setProfessorId(professorResponsavelId);

			advertencia.setMotivo(String.valueOf(comboTipo.getSelectedItem()));

			advertencia.setDescricao(areaDescricao.getText().trim());

			advertencia.setOrientacao(
					areaProvidencias.getText().trim().isEmpty() ? "Não informada" : areaProvidencias.getText().trim());

			advertencia.setDataAdvertencia(data);

			new AdvertenciaController().salvarAdvertencia(advertencia);

			JOptionPane.showMessageDialog(this, "Advertência salva com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);

			carregarDadosDoBanco();

			limparFormulario();

		} catch (DateTimeParseException ex) {

			mostrarErro("Informe uma data válida no formato dd/MM/aaaa.", ex);

		} catch (RuntimeException ex) {

			mostrarErro("Não foi possível salvar a advertência.", ex);
		}
	}

	private Aluno alunoPorMatricula(String matricula) {

		for (Aluno aluno : alunos) {

			if (aluno.getMatricula().equals(matricula)) {

				return aluno;
			}
		}

		return null;
	}

	private void limparFormulario() {

		tabelaAdvertencias.clearSelection();

		txtNomeAlunoDetalhe.setText("");
		txtMatriculaDetalhe.setText("");
		txtTurmaDetalhe.setText("");
		txtCursoDetalhe.setText("");
		txtResponsavelDetalhe.setText("");
		txtTelefoneDetalhe.setText("");

		txtData.setText(LocalDate.now().format(FORMATO_DATA));

		comboTipo.setSelectedIndex(0);

		areaDescricao.setText("");
		areaProvidencias.setText("");

		chkAluno.setSelected(false);
		chkResponsavel.setSelected(false);
	}

	private void imprimirTabela() {

		try {

			if (!tabelaAdvertencias.print()) {

				JOptionPane.showMessageDialog(this, "A impressão foi cancelada.", "Impressão",
						JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (PrinterException ex) {

			mostrarErro("Não foi possível imprimir a tabela.", ex);
		}
	}

	private void mostrarErro(String mensagem, Exception ex) {

		JOptionPane.showMessageDialog(this, mensagem + "\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private JTextField adicionarCampoAluno(JPanel painel, String titulo, String valor, int x, int y) {

		JLabel lbl = new JLabel(titulo);

		lbl.setForeground(corLabel);

		lbl.setBounds(x, y, 250, 20);

		painel.add(lbl);

		JTextField campo = new JTextField(valor);

		campo.setEditable(true);

		campo.setBounds(x, y + 25, 320, 35);

		estilizarCampo(campo);

		campo.setEditable(false);

		painel.add(campo);

		return campo;
	}

	private JPanel criarCardSecao(String titulo) {

		JPanel painel = criarPainelArredondado();

		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(corLabel);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

		lblTitulo.setBounds(30, 15, 500, 30);

		painel.add(lblTitulo);

		return painel;
	}

	private JLabel criarLabelCampo(String texto) {

		JLabel lbl = new JLabel(texto);

		lbl.setForeground(corLabel);

		lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

		return lbl;
	}

	private void estilizarCampo(JTextField campo) {

		campo.setBackground(corCampo);
		campo.setForeground(textos);
		campo.setCaretColor(textos);

		campo.setBorder(new LineBorder(corBorda));
	}

	private void estilizarCombo(JComboBox<String> combo) {

		combo.setBackground(corCampo);
		combo.setForeground(textos);

		combo.setBorder(new LineBorder(corBorda));
	}

	private void estilizarBotao(JButton botao) {

		botao.setBackground(corCampo);
		botao.setForeground(textos);
		botao.setFocusPainted(false);

		botao.setBorder(new LineBorder(corBorda));
	}

	/**
	 * Cria um campo com máscara e corrige o posicionamento automático do cursor.
	 *
	 * Quando o campo está vazio, o cursor é colocado explicitamente na primeira
	 * posição da máscara.
	 *
	 * Quando o campo já possui conteúdo, todo o conteúdo é selecionado para
	 * facilitar a substituição.
	 */
	private JFormattedTextField criarCampoMascara(String mascara) {

		try {

			MaskFormatter formatter = new MaskFormatter(mascara);

			formatter.setPlaceholderCharacter(' ');

			JFormattedTextField campo = new JFormattedTextField(formatter);

			campo.setBackground(corCampo);
			campo.setForeground(textos);
			campo.setCaretColor(textos);

			campo.setBorder(new LineBorder(corBorda));

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

					} else {

						SwingUtilities.invokeLater(campo::selectAll);
					}
				}
			});

			return campo;

		} catch (ParseException e) {

			throw new IllegalArgumentException("Máscara inválida: " + mascara, e);
		}
	}

	private JLabel adicionarIndicador(JPanel painel, String titulo, String valor, int x, int y, Color cor) {

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(textos);

		lblTitulo.setBounds(x, y, 200, 20);

		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);

		lblValor.setForeground(cor);

		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));

		lblValor.setBounds(x, y + 20, 150, 40);

		painel.add(lblValor);

		return lblValor;
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
			}
		};
	}
}