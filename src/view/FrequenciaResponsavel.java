package view;

import java.sql.Connection;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.SwingConstants;
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

public class FrequenciaResponsavel extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final int LARGURA_CARD = 1220;
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;
	private JTable tabelaFrequencia;
	private DefaultTableModel modeloTabela;
	private JComboBox<String> comboFilho;
	private JComboBox<String> comboPeriodo;
	private JComboBox<String> comboMes;
	private JLabel valorPresencas;
	private JLabel valorFaltas;
	private JLabel valorJustificadas;
	private JLabel valorFrequencia;
	private JLabel valorNome;
	private JLabel valorMatricula;
	private JLabel valorTurma;
	private JLabel valorCurso;
	private JLabel valorTurno;
	private JLabel valorSituacao;
	private JTextArea areaObservacoes;
	private final List<Aluno> filhos = new ArrayList<>();
	private final List<Presenca> presencas = new ArrayList<>();
	private final Map<String, Integer> idsFilhosPorDescricao = new HashMap<>();
	private final Map<Integer, String> professoresPorDisciplina = new HashMap<>();
	private int alunoSelecionadoId;

	public FrequenciaResponsavel() {
		setTitle("Frequência Escolar");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);

		Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int largura = area.width - 60;
		int altura = area.height - 60;

		JPanel interno = new JPanel(null);
		interno.setBackground(corInterna);
		interno.setBounds(20, 20, largura, altura);
		externo.add(interno);

		criarTela(interno, largura, altura);
		carregarFilhosDoBanco();
	}

	private void criarTela(JPanel painel, int largura, int altura) {
		JPanel topo = criarTopo();
		topo.setBounds(30, 25, largura - 60, 160);
		painel.add(topo);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(35, 30, 150, 40);
		estilizarBotao(btnVoltar);
		btnVoltar.addActionListener(e -> dispose());
		topo.add(btnVoltar);

		int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 100);

		JPanel conteudo = new JPanel(null);
		conteudo.setBackground(corInterna);
		conteudo.setPreferredSize(new Dimension(larguraConteudo, 1670));

		JScrollPane scroll = new JScrollPane(conteudo);
		scroll.setBounds(30, 210, largura - 60, altura - 250);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		painel.add(scroll);

		criarResumo(conteudo, larguraConteudo);
		criarDadosAluno(conteudo, larguraConteudo);
		criarFiltros(conteudo, larguraConteudo);
		criarTabelaFrequencia(conteudo, larguraConteudo);
		criarEstatisticas(conteudo, larguraConteudo);
		criarObservacoes(conteudo, larguraConteudo);
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
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(),
						new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				g2.dispose();
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Frequência Escolar");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 75, 500, 45);
		topo.add(titulo);

		JLabel subtitulo = new JLabel("Visualização da frequência escolar dos alunos vinculados ao responsável.");
		subtitulo.setFont(new Font("Dialog", Font.BOLD, 20));
		subtitulo.setForeground(new Color(240, 240, 255));
		subtitulo.setBounds(45, 110, 900, 50);
		topo.add(subtitulo);
		return topo;
	}

	private void criarResumo(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Resumo");
		card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);
		valorPresencas = adicionarIndicador(card, "Presenças", "0", 60, 50, Color.GREEN);
		valorFaltas = adicionarIndicador(card, "Faltas", "0", 350, 50, Color.RED);
		valorJustificadas = adicionarIndicador(card, "Justificadas", "0", 650, 50, Color.ORANGE);
		valorFrequencia = adicionarIndicador(card, "Frequência", "0%", 950, 50, Color.CYAN);
		painel.add(card);
	}

	private void criarDadosAluno(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Dados do Aluno");
		card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 260);

		JLabel lblAluno = criarLabel("Aluno");
		lblAluno.setBounds(30, 52, 120, 20);
		card.add(lblAluno);

		comboFilho = new JComboBox<>();
		comboFilho.setBounds(30, 77, 320, 35);
		estilizarCombo(comboFilho);

		comboFilho.addActionListener(e -> {
			if (!comboFilho.isPopupVisible()) {
				selecionarFilho();
			}
		});
		card.add(comboFilho);
		valorNome = adicionarCampoAluno(card, "Nome", "Não informado", 430, 52);
		valorMatricula = adicionarCampoAluno(card, "Matrícula", "Não informado", 830, 52);
		valorTurma = adicionarCampoAluno(card, "Turma", "Não informado", 30, 135);
		valorCurso = adicionarCampoAluno(card, "Curso", "Não informado", 430, 135);
		valorTurno = adicionarCampoAluno(card, "Turno", "Não informado", 830, 135);
		valorSituacao = adicionarCampoAluno(card, "Situação", "Não informado", 30, 218);
		painel.add(card);
	}

	private void criarFiltros(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Filtros");
		card.setBounds(centerX(larguraConteudo), 460, LARGURA_CARD, 180);

		JLabel lblPeriodo = criarLabel("Período");
		lblPeriodo.setBounds(30, 50, 120, 25);
		card.add(lblPeriodo);
		comboPeriodo = new JComboBox<>(new String[] { "Ano Letivo", "1º Trimestre", "2º Trimestre", "3º Trimestre"});
		comboPeriodo.setBounds(30, 80, 220, 35);
		estilizarCombo(comboPeriodo);
		card.add(comboPeriodo);

		JLabel lblMes = criarLabel("Mês");
		lblMes.setBounds(300, 50, 120, 25);
		card.add(lblMes);
		comboMes = new JComboBox<>(new String[] { "Todos", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
				"Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" });
		comboMes.setBounds(300, 80, 220, 35);
		estilizarCombo(comboMes);
		card.add(comboMes);

		JButton btnFiltrar = new JButton("Filtrar");
		btnFiltrar.setBounds(620, 78, 180, 40);
		estilizarBotao(btnFiltrar);
		btnFiltrar.addActionListener(e -> aplicarFiltros());
		card.add(btnFiltrar);

		JButton btnLimpar = new JButton("Limpar filtros");
		btnLimpar.setBounds(820, 78, 180, 40);
		estilizarBotao(btnLimpar);
		btnLimpar.addActionListener(e -> limparFiltros());
		card.add(btnLimpar);
		painel.add(card);
	}

	private void criarTabelaFrequencia(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Registro de Frequência");
		card.setBounds(centerX(larguraConteudo), 650, LARGURA_CARD, 420);
		String[] colunas = { "Data", "Disciplina", "Professor", "Status", "Observação" };
		modeloTabela = new DefaultTableModel(colunas, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tabelaFrequencia = new JTable(modeloTabela);
		tabelaFrequencia.setRowHeight(30);
		tabelaFrequencia.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		tabelaFrequencia.setForeground(textos);
		tabelaFrequencia.setBackground(corInterna);
		tabelaFrequencia.setGridColor(new Color(90, 50, 170));
		tabelaFrequencia.setSelectionBackground(new Color(80, 40, 160));
		tabelaFrequencia.setSelectionForeground(textos);
		tabelaFrequencia.setShowGrid(true);
		tabelaFrequencia.setShowHorizontalLines(true);
		tabelaFrequencia.setShowVerticalLines(true);
		tabelaFrequencia.setIntercellSpacing(new Dimension(1, 1));
		tabelaFrequencia.setFillsViewportHeight(false);
		tabelaFrequencia.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaFrequencia.getTableHeader();
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

		for (int i = 0; i < tabelaFrequencia.getColumnCount(); i++) {
			tabelaFrequencia.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JScrollPane scroll = new JScrollPane(tabelaFrequencia);
		scroll.setBounds(25, 60, 1170, 300);
		scroll.getViewport().setBackground(corInterna);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void criarEstatisticas(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Estatísticas");
		card.setBounds(centerX(larguraConteudo), 1090, LARGURA_CARD, 220);
		adicionarIndicador(card, "Total de Registros", "0", 60, 60, Color.CYAN);

		JLabel valorTotal = adicionarIndicador(card, "Presenças", "0", 350, 60, Color.GREEN);
		JLabel valorFaltasEstatistica = adicionarIndicador(card, "Faltas", "0", 650, 60, Color.RED);
		JLabel valorPercentual = adicionarIndicador(card, "Percentual", "0%", 950, 60, Color.ORANGE);

		valorTotal.setName("estatisticaPresencas");
		valorFaltasEstatistica.setName("estatisticaFaltas");
		valorPercentual.setName("estatisticaPercentual");
		painel.add(card);
	}

	private void criarObservacoes(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Informações sobre a Frequência");
		card.setBounds(centerX(larguraConteudo), 1330, LARGURA_CARD, 320);
		areaObservacoes = new JTextArea();
		areaObservacoes.setText("As informações apresentadas nesta tela " + "correspondem aos registros de frequência "
				+ "lançados pelos professores.");
		areaObservacoes.setEditable(false);
		areaObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		areaObservacoes.setLineWrap(true);
		areaObservacoes.setWrapStyleWord(true);
		areaObservacoes.setBackground(corCampo);
		areaObservacoes.setForeground(textos);

		JScrollPane scroll = new JScrollPane(areaObservacoes);
		scroll.setBounds(25, 60, 1170, 230);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void carregarFilhosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getPaiId() <= 0) {
			JOptionPane.showMessageDialog(this, "Não foi possível identificar o responsável logado.", "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			List<Aluno> alunos = alunoDAO.listarPorPais(usuario.getPaiId());

			if (alunos.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Não existem alunos vinculados a este responsável.",
						"Frequência Escolar", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			filhos.clear();
			filhos.addAll(alunos);
			comboFilho.removeAllItems();
			idsFilhosPorDescricao.clear();

			for (Aluno aluno : filhos) {
				String descricao = montarDescricaoFilho(aluno);
				comboFilho.addItem(descricao);
				idsFilhosPorDescricao.put(descricao, aluno.getIdAluno());
			}
			if (!filhos.isEmpty()) {
				comboFilho.setSelectedIndex(0);
				selecionarFilho();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Não foi possível carregar os alunos: " + obterMensagemErro(ex), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private String montarDescricaoFilho(Aluno aluno) {
		String nome = aluno.getNome() == null ? "Aluno" : aluno.getNome();
		String matricula = aluno.getMatricula() == null || aluno.getMatricula().isBlank() ? "Sem matrícula"
				: aluno.getMatricula();
		return nome + " - " + matricula;
	}

	private void selecionarFilho() {
		Object selecionado = comboFilho.getSelectedItem();

		if (selecionado == null) {
			return;
		}
		Integer idAluno = idsFilhosPorDescricao.get(selecionado.toString());

		if (idAluno == null || idAluno <= 0) {
			return;
		}
		alunoSelecionadoId = idAluno;
		carregarDadosDoFilho(idAluno);
	}

	private void carregarDadosDoFilho(int idAluno) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
			Aluno aluno = alunoDAO.buscarPorId(idAluno);

			if (aluno == null) {
				throw new IllegalArgumentException("Aluno não encontrado.");
			}
			preencherDadosAluno(aluno, turmaDAO);
			carregarProfessores(aluno.getIdAluno(), disciplinaDAO);
			PresencaController presencaController = new PresencaController();
			presencas.clear();
			presencas.addAll(presencaController.listarPresencasPorAluno(aluno.getIdAluno()));
			atualizarResumo(presencas);
			preencherTabela(presencas);
			atualizarEstatisticas(presencas);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Não foi possível carregar a frequência do aluno: " + obterMensagemErro(ex), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void preencherDadosAluno(Aluno aluno, TurmaDAO turmaDAO) throws Exception {
		valorNome.setText(aluno.getNome() == null ? "Não informado" : aluno.getNome());
		valorMatricula.setText(aluno.getMatricula() == null || aluno.getMatricula().isBlank() ? "Não informado"
				: aluno.getMatricula());
		valorSituacao.setText(aluno.getSituacao() == null ? "Não informado" : formatarSituacao(aluno.getSituacao().name()));
		valorCurso.setText("Não informado");

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
	}

	private void carregarProfessores(int idAluno, DisciplinaDAO disciplinaDAO) throws Exception {
		professoresPorDisciplina.clear();
		List<Object[]> detalhes = disciplinaDAO.listarDetalhesPorAluno(idAluno);

		for (Object[] detalhe : detalhes) {
			if (detalhe == null || detalhe.length < 3) {
				continue;
			}
			if (detalhe[1] == null) {
				continue;
			}
			String descricao = detalhe[1].toString();
			String professores = detalhe[2] == null ? "Não informado" : detalhe[2].toString();
			Disciplina disciplina = disciplinaDAO.buscarPorDescricao(descricao);

			if (disciplina != null) {
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
		String status = presenca.isPresente() ? "Presente" : "Falta";
		String observacao = presenca.getMotivoAbonada();

		if (observacao == null || observacao.isBlank()) {
			if (presenca.isFaltaAbonada()) {
				observacao = "Falta abonada";
			} else if (presenca.isFaltaJustificada()) {
				observacao = "Falta justificada";
			} else {
				observacao = "-";
			}
		}

		String professor = professoresPorDisciplina.get(presenca.getDisciplinaId());

		if (professor == null || professor.isBlank()) {
			professor = "Não informado";
		}

		String disciplina = buscarDescricaoDisciplina(presenca.getDisciplinaId());
		modeloTabela.addRow(new Object[] { presenca.getData().format(FORMATO_DATA), disciplina, professor, status, observacao });
	}

	private String buscarDescricaoDisciplina(int disciplinaId) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			Disciplina disciplina = new DisciplinaDAO(conn).buscarPorId(disciplinaId);

			if (disciplina != null) {
				return disciplina.getDescricao();
			}
		} catch (Exception ex) {
			// Mantém a tela funcional mesmo se a disciplina não puder ser carregada.
		}
		return "Não informado";
	}

	private void atualizarResumo(List<Presenca> registros) {
		int totalPresencas = 0;
		int totalFaltas = 0;
		int totalJustificadas = 0;

		for (Presenca presenca : registros) {
			if (presenca.isPresente()) {
				totalPresencas++;
			} else {
				totalFaltas++;
				if (presenca.isFaltaJustificada() || presenca.isFaltaAbonada()) {
					totalJustificadas++;
				}
			}
		}
		int totalRegistros = totalPresencas + totalFaltas;
		double percentual = totalRegistros == 0 ? 0 : (totalPresencas * 100.0) / totalRegistros;
		valorPresencas.setText(String.valueOf(totalPresencas));
		valorFaltas.setText(String.valueOf(totalFaltas));
		valorJustificadas.setText(String.valueOf(totalJustificadas));
		valorFrequencia.setText(String.format("%.1f%%", percentual));
	}

	private void atualizarEstatisticas(List<Presenca> registros) {
		int totalPresencas = 0;
		int totalFaltas = 0;

		for (Presenca presenca : registros) {
			if (presenca.isPresente()) {
				totalPresencas++;
			} else {
				totalFaltas++;
			}
		}
		int total = totalPresencas + totalFaltas;
		double percentual = total == 0 ? 0 : (totalPresencas * 100.0) / total;
		atualizarIndicadorEstatistica("estatisticaTotal", String.valueOf(total));
		atualizarIndicadorEstatistica("estatisticaPresencas", String.valueOf(totalPresencas));
		atualizarIndicadorEstatistica("estatisticaFaltas", String.valueOf(totalFaltas));
		atualizarIndicadorEstatistica("estatisticaPercentual", String.format("%.1f%%", percentual));
	}

	private void atualizarIndicadorEstatistica(String nome, String valor) {
		JLabel indicador = encontrarLabelPorNome(getContentPane(), nome);

		if (indicador != null) {
			indicador.setText(valor);
		}
	}

	private JLabel encontrarLabelPorNome(java.awt.Container container, String nome) {
		for (java.awt.Component componente : container.getComponents()) {
			if (componente instanceof JLabel) {
				JLabel label = (JLabel) componente;
				if (nome.equals(label.getName())) {
					return label;
				}
			}
			if (componente instanceof java.awt.Container) {
				JLabel encontrado = encontrarLabelPorNome((java.awt.Container) componente, nome);
				if (encontrado != null) {
					return encontrado;
				}
			}
		}
		return null;
	}

	private void aplicarFiltros() {
		if (alunoSelecionadoId <= 0) {
			return;
		}
		String periodo = comboPeriodo.getSelectedItem() == null ? "Ano Letivo"
				: comboPeriodo.getSelectedItem().toString();
		String mes = comboMes.getSelectedItem() == null ? "Todos" : comboMes.getSelectedItem().toString();
		List<Presenca> filtradas = new ArrayList<>();

		for (Presenca presenca : presencas) {
			if (!correspondePeriodo(presenca.getData(), periodo)) {
				continue;
			}
			if (!correspondeMes(presenca.getData(), mes)) {
				continue;
			}
			filtradas.add(presenca);
		}
		preencherTabela(filtradas);
		atualizarResumo(filtradas);
		atualizarEstatisticas(filtradas);
	}

	private boolean correspondePeriodo(LocalDate data, String periodo) {
		if (data == null) {
			return false;
		}
		int anoAtual = LocalDate.now().getYear();

		if (data.getYear() != anoAtual) {
			return false;
		}
		switch (periodo) {
			case "1º Trimestre":
				return data.getMonthValue() >= 2 && data.getMonthValue() <= 5;

		case "2º Trimestre":
			return data.getMonthValue() >= 5 && data.getMonthValue() <= 9;

		case "3º Trimestre":
			return data.getMonthValue() >= 9 && data.getMonthValue() <= 12;
		case "Ano Letivo":
		default:
			return true;
		}
	}

	private boolean correspondeMes(LocalDate data, String mes) {
		if (data == null || mes == null || "Todos".equals(mes)) {
			return true;
		}
		Month mesSelecionado = converterMes(mes);
		return mesSelecionado != null && data.getMonth().equals(mesSelecionado);
	}

	private Month converterMes(String mes) {
			switch (mes) {
			case "Janeiro":
				return Month.JANUARY;
			case "Fevereiro":
				return Month.FEBRUARY;
			case "Março":
				return Month.MARCH;
			case "Abril":
				return Month.APRIL;
			case "Maio":
				return Month.MAY;
			case "Junho":
				return Month.JUNE;
			case "Julho":
				return Month.JULY;
			case "Agosto":
				return Month.AUGUST;
			case "Setembro":
				return Month.SEPTEMBER;
			case "Outubro":
				return Month.OCTOBER;
			case "Novembro":
				return Month.NOVEMBER;
			case "Dezembro":
				return Month.DECEMBER;
			default:
				return null;
		}
	}

	private void limparFiltros() {
		comboPeriodo.setSelectedIndex(0);
		comboMes.setSelectedIndex(0);
		preencherTabela(presencas);
		atualizarResumo(presencas);
		atualizarEstatisticas(presencas);
	}

	private JLabel adicionarCampoAluno(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lbl = criarLabel(titulo);
		lbl.setBounds(x, y, 200, 20);
		painel.add(lbl);

		JTextField campo = new JTextField(valor);
		campo.setEditable(false);
		campo.setBounds(x, y + 25, 320, 35);
		estilizarCampo(campo);
		painel.add(campo);
		return lbl;
	}

	private JPanel criarCard(String titulo) {
		JPanel painel = criarPainelArredondado();
		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitulo.setBounds(30, 15, 500, 30);
		painel.add(lblTitulo);
		return painel;
	}

	private JLabel criarLabel(String texto) {
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

	private JLabel adicionarIndicador(JPanel painel, String titulo, String valor, int x, int y, Color cor) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(textos);
		lblTitulo.setBounds(x, y, 200, 20);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(cor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
		lblValor.setBounds(x, y + 20, 200, 40);
		painel.add(lblValor);
		return lblValor;
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

	private String formatarSituacao(String situacao) {
		if (situacao == null || situacao.isBlank()) {
			return "Não informado";
		}
		switch (situacao.toUpperCase()) {
			case "ATIVO":
				return "Ativo";
			case "INATIVO":
				return "Inativo";
			case "TRANSFERIDO":
				return "Transferido";
			default:
				return situacao;
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