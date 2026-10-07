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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import util.SessaoUsuario;

public class AdvertenciasResponsavel extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;

	private JTable tabelaAdvertencias;
	private DefaultTableModel modeloTabela;
	private JComboBox<Turma> comboTurma;
	private JComboBox<Aluno> comboAluno;
	private JTextField campoNome;
	private JTextField campoMatricula;
	private JTextField campoTurma;
	private JTextField campoSituacao;
	private JTextField campoData;
	private JTextField campoProfessor;
	private JTextField campoGravidade;
	private JTextArea areaMotivo;
	private JTextArea areaHistorico;
	private JCheckBox chkCiente;
	private JLabel lblStatus;
	private JLabel lblPendentes;
	private JLabel lblCientes;
	private JLabel lblTotal;
	private JLabel lblAnoAtual;
	private final List<Aluno> alunos = new ArrayList<>();
	private final List<Advertencia> advertencias = new ArrayList<>();
	private Advertencia advertenciaSelecionada;
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final int LARGURA_CARD = 1220;

	public AdvertenciasResponsavel() {
		setTitle("Advertências");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

		criarTela(interno, largura, altura);
		carregarDadosDoBanco();
	}

	private void criarTela(JPanel painel, int largura, int altura) {
		JPanel topo = criarTopo();
		topo.setBounds(30, 25, largura - 60, 160);
		painel.add(topo);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(35, 35, 150, 40);
		estilizarBotao(btnVoltar);
		btnVoltar.addActionListener(e -> dispose());
		topo.add(btnVoltar);

		int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 120);

		JPanel conteudo = new JPanel(null);
		conteudo.setBackground(corInterna);
		conteudo.setPreferredSize(new Dimension(larguraConteudo, 2040));

		JScrollPane scroll = new JScrollPane(conteudo);
		scroll.setBounds(30, 210, largura - 60, altura - 250);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		painel.add(scroll);

		criarResumo(conteudo, larguraConteudo);
		criarSelecao(conteudo, larguraConteudo);
		criarDadosAluno(conteudo, larguraConteudo);
		criarTabelaAdvertencias(conteudo, larguraConteudo);
		criarDetalhesAdvertencia(conteudo, larguraConteudo);
		criarConfirmacaoCiente(conteudo, larguraConteudo);
		criarHistoricoCiencia(conteudo, larguraConteudo);
	}

	private int centerX(int larguraConteudo) {
		return (larguraConteudo - LARGURA_CARD) / 2;
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
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
		titulo.setBounds(40, 80, 400, 45);
		topo.add(titulo);

		JLabel subtitulo = new JLabel("Visualização e confirmação de ciência");
		subtitulo.setForeground(new Color(240, 240, 255));
		subtitulo.setBounds(45, 120, 500, 25);
		topo.add(subtitulo);

		return topo;
	}

	private void criarResumo(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Resumo");
		card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

		lblPendentes = adicionarIndicador(card, "Pendentes", "0", 60, 50, Color.RED);
		lblCientes = adicionarIndicador(card, "Cientes", "0", 350, 50, Color.GREEN);
		lblTotal = adicionarIndicador(card, "Total", "0", 650, 50, Color.CYAN);
		lblAnoAtual = adicionarIndicador(card, "Ano Atual", "0", 950, 50, Color.ORANGE);

		painel.add(card);
	}

	private void carregarDadosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null || usuario.getPaiId() <= 0) {
			mostrarErro("Nenhum responsável está autenticado para consultar as advertências.");
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			alunos.clear();
			alunos.addAll(alunoDAO.listarPorPais(usuario.getPaiId()));

			comboTurma.removeAllItems();
			for (Turma turma : turmaDAO.listarAtivas()) {
				if (alunos.stream().anyMatch(aluno -> aluno.getIdTurma() == turma.getIdTurma())) {
					comboTurma.addItem(turma);
				}
			}
			limparSelecoes();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os dados do banco: " + ex.getMessage());
		}
	}

	private void carregarAlunosDaTurma() {
		Turma turma = (Turma) comboTurma.getSelectedItem();
		comboAluno.removeAllItems();
		limparDadosAluno();
		if (turma == null) {
			return;
		}
		for (Aluno aluno : alunos) {
			if (aluno.getIdTurma() == turma.getIdTurma()) {
				comboAluno.addItem(aluno);
			}
		}
	}

	private void carregarAlunoSelecionado() {
		Aluno aluno = (Aluno) comboAluno.getSelectedItem();
		limparDadosAluno();
		if (aluno == null) {
			return;
		}
		campoNome.setText(aluno.getNome());
		campoMatricula.setText(aluno.getMatricula());
		campoTurma.setText(nomeTurma(aluno.getIdTurma()));
		campoSituacao.setText(aluno.getSituacao() == null ? "Não informado" : aluno.getSituacao().name());
		carregarAdvertencias(aluno);
	}

	private void carregarAdvertencias(Aluno aluno) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			advertencias.clear();
			advertencias.addAll(new AdvertenciaDAO(conn).listarPorAluno(aluno.getIdAluno()));
			modeloTabela.setRowCount(0);
			for (Advertencia advertencia : advertencias) {
				modeloTabela.addRow(new Object[] { advertencia.getDataAdvertencia().format(FORMATO_DATA),
						advertencia.getMotivo(), nomeProfessor(conn, advertencia.getProfessorId()), "-",
						advertencia.getSituacao() });
			}
			atualizarResumo();
			if (!advertencias.isEmpty()) {
				tabelaAdvertencias.setRowSelectionInterval(0, 0);
			}
		} catch (SQLException ex) {
			mostrarErro("Não foi possível carregar as advertências: " + ex.getMessage());
		}
	}

	private String nomeProfessor(Connection conn, int idProfessor) throws SQLException {
		Professor professor = new ProfessorDAO(conn).buscarPorId(idProfessor);
		return professor == null ? "Não informado" : professor.getNome();
	}

	private void carregarAdvertenciaSelecionada() {
		int linha = tabelaAdvertencias.getSelectedRow();
		if (linha < 0 || linha >= advertencias.size()) {
			return;
		}
		advertenciaSelecionada = advertencias.get(linha);
		campoData.setText(advertenciaSelecionada.getDataAdvertencia().format(FORMATO_DATA));
		campoGravidade.setText("Não informado");
		areaMotivo.setText(advertenciaSelecionada.getDescricao());
		campoProfessor.setText("Carregando...");
		try (Connection conn = ConnectionFactory.getConnection()) {
			campoProfessor.setText(nomeProfessor(conn, advertenciaSelecionada.getProfessorId()));
		} catch (SQLException ex) {
			campoProfessor.setText("Não informado");
		}
		lblStatus.setText("Status Atual: " + advertenciaSelecionada.getSituacao());
		chkCiente.setSelected("Ciente".equalsIgnoreCase(advertenciaSelecionada.getSituacao()));
		carregarHistorico();
	}

	private void darCiencia() {
		if (advertenciaSelecionada == null || !chkCiente.isSelected()) {
			mostrarErro("Selecione uma advertência e confirme a ciência.");
			return;
		}
		advertenciaSelecionada.setSituacao("Ciente");
		try (Connection conn = ConnectionFactory.getConnection()) {
			new AdvertenciaDAO(conn).atualizar(advertenciaSelecionada);
			lblStatus.setText("Status Atual: Ciente");
			atualizarResumo();
			JOptionPane.showMessageDialog(this, "Ciência registrada com sucesso.");
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível registrar a ciência: " + ex.getMessage());
		}
	}

	private void carregarHistorico() {
		if (advertenciaSelecionada == null) {
			areaHistorico.setText("");
			return;
		}
		areaHistorico.setText(advertenciaSelecionada.getDataAdvertencia().format(FORMATO_DATA) + " - Situação: "
				+ advertenciaSelecionada.getSituacao());
	}

	private void atualizarResumo() {
		long cientes = advertencias.stream().filter(a -> "Ciente".equalsIgnoreCase(a.getSituacao())).count();
		long pendentes = advertencias.size() - cientes;
		long anoAtual = advertencias.stream().filter(a -> a.getDataAdvertencia().getYear() == LocalDate.now().getYear())
				.count();
		lblCientes.setText(String.valueOf(cientes));
		lblPendentes.setText(String.valueOf(pendentes));
		lblTotal.setText(String.valueOf(advertencias.size()));
		lblAnoAtual.setText(String.valueOf(anoAtual));
	}

	private String nomeTurma(int idTurma) {
		for (int i = 0; i < comboTurma.getItemCount(); i++) {
			Turma turma = comboTurma.getItemAt(i);
			if (turma.getIdTurma() == idTurma) {
				return turma.getDescricaoTurma();
			}
		}
		return "Não informado";
	}

	private void limparSelecoes() {
		comboTurma.setSelectedItem(null);
		comboAluno.removeAllItems();
		limparDadosAluno();
	}

	private void limparDadosAluno() {
		campoNome.setText("");
		campoMatricula.setText("");
		campoTurma.setText("");
		campoSituacao.setText("");
		modeloTabela.setRowCount(0);
		advertencias.clear();
		advertenciaSelecionada = null;
		campoData.setText("");
		campoProfessor.setText("");
		campoGravidade.setText("");
		areaMotivo.setText("");
		areaHistorico.setText("");
		lblStatus.setText("Status Atual: -");
		atualizarResumo();
	}

	private void criarSelecao(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Selecionar aluno");
		card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 130);

		JLabel lblTurma = criarLabel("Turma");
		lblTurma.setBounds(30, 50, 180, 22);
		card.add(lblTurma);
		comboTurma = new JComboBox<>();
		comboTurma.setBounds(30, 78, 470, 35);
		estilizarCombo(comboTurma);
		comboTurma.setRenderer(new DefaultListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list,
						value == null ? "" : ((Turma) value).getDescricaoTurma(),
						index, isSelected, cellHasFocus);
			}
		});
		comboTurma.addActionListener(e -> carregarAlunosDaTurma());
		card.add(comboTurma);

		JLabel lblAluno = criarLabel("Aluno");
		lblAluno.setBounds(560, 50, 180, 22);
		card.add(lblAluno);
		comboAluno = new JComboBox<>();
		comboAluno.setBounds(560, 78, 630, 35);
		estilizarCombo(comboAluno);
		comboAluno.setRenderer(new DefaultListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Aluno aluno = (Aluno) value;
				String texto = aluno == null ? "" : aluno.getNome() + " - " + aluno.getMatricula();
				return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
			}
		});
		comboAluno.addActionListener(e -> carregarAlunoSelecionado());
		card.add(comboAluno);

		painel.add(card);
	}

	private void criarDadosAluno(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Dados do Aluno");
		card.setBounds(centerX(larguraConteudo), 340, LARGURA_CARD, 220);

		campoNome = adicionarCampoAluno(card, "Nome", "", 30, 60);
		campoMatricula = adicionarCampoAluno(card, "Matrícula", "", 430, 60);
		campoTurma = adicionarCampoAluno(card, "Turma", "", 830, 60);
		adicionarCampoAluno(card, "Curso", "Não informado", 30, 140);
		campoSituacao = adicionarCampoAluno(card, "Situação", "", 430, 140);

		painel.add(card);
	}

	private void criarTabelaAdvertencias(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Advertências Recebidas");
		card.setBounds(centerX(larguraConteudo), 580, LARGURA_CARD, 400);

		String[] colunas = { "Data", "Tipo", "Professor", "Gravidade", "Status" };
		modeloTabela = new DefaultTableModel(colunas, 0);

		tabelaAdvertencias = new JTable(modeloTabela);
		tabelaAdvertencias.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				carregarAdvertenciaSelecionada();
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

		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		for (int i = 0; i < tabelaAdvertencias.getColumnCount(); i++) {
			tabelaAdvertencias.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JScrollPane scroll = new JScrollPane(tabelaAdvertencias);
		scroll.setBounds(25, 60, 1170, 300);
		scroll.getViewport().setBackground(corInterna);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void criarDetalhesAdvertencia(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Detalhes da Advertência");
		card.setBounds(centerX(larguraConteudo), 1000, LARGURA_CARD, 420);

		JLabel lblData = criarLabel("Data");
		lblData.setBounds(30, 60, 120, 25);
		card.add(lblData);

		campoData = new JTextField();
		campoData.setEditable(false);
		campoData.setBounds(30, 90, 220, 35);
		estilizarCampo(campoData);
		card.add(campoData);

		JLabel lblProfessor = criarLabel("Professor");
		lblProfessor.setBounds(300, 60, 120, 25);
		card.add(lblProfessor);

		campoProfessor = new JTextField();
		campoProfessor.setEditable(false);
		campoProfessor.setBounds(300, 90, 300, 35);
		estilizarCampo(campoProfessor);
		card.add(campoProfessor);

		JLabel lblGravidade = criarLabel("Gravidade");
		lblGravidade.setBounds(650, 60, 120, 25);
		card.add(lblGravidade);

		campoGravidade = new JTextField();
		campoGravidade.setEditable(false);
		campoGravidade.setBounds(650, 90, 220, 35);
		estilizarCampo(campoGravidade);
		card.add(campoGravidade);

		JLabel lblMotivo = criarLabel("Motivo");
		lblMotivo.setBounds(30, 150, 120, 25);
		card.add(lblMotivo);

		areaMotivo = new JTextArea();
		areaMotivo.setEditable(false);
		areaMotivo.setFont(new Font("Segoe UI", Font.BOLD, 20));
		areaMotivo.setLineWrap(true);
		areaMotivo.setWrapStyleWord(true);
		areaMotivo.setBackground(corCampo);
		areaMotivo.setForeground(textos);

		JScrollPane scroll = new JScrollPane(areaMotivo);
		scroll.setBounds(30, 180, 1160, 180);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);

		painel.add(card);
	}

	private void criarConfirmacaoCiente(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Confirmação de Ciência");
		card.setBounds(centerX(larguraConteudo), 1440, LARGURA_CARD, 250);

		chkCiente = new JCheckBox("Declaro que estou ciente desta advertência.");
		chkCiente.setBounds(30, 70, 500, 30);
		chkCiente.setBackground(corCampo);
		chkCiente.setForeground(textos);
		card.add(chkCiente);

		JButton btnCiente = new JButton("Dar Ciência");
		btnCiente.setBounds(30, 130, 220, 40);
		estilizarBotao(btnCiente);
		btnCiente.addActionListener(e -> darCiencia());
		card.add(btnCiente);

		lblStatus = new JLabel("Status Atual: -");
		lblStatus.setForeground(Color.ORANGE);
		lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblStatus.setBounds(320, 135, 300, 30);
		card.add(lblStatus);

		painel.add(card);
	}

	private void criarHistoricoCiencia(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Histórico de Ciência");
		card.setBounds(centerX(larguraConteudo), 1710, LARGURA_CARD, 320);

		JTextArea areaHistorico = new JTextArea();
		areaHistorico.setEditable(false);
		areaHistorico.setText("");
		areaHistorico.setLineWrap(true);
		areaHistorico.setWrapStyleWord(true);
		areaHistorico.setBackground(corCampo);
		areaHistorico.setForeground(textos);
		areaHistorico.setFont(new Font("Segoe UI", Font.BOLD, 22));

		JScrollPane scroll = new JScrollPane(areaHistorico);
		scroll.setBounds(25, 60, 1170, 230);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);

		painel.add(card);
	}

	private JTextField adicionarCampoAluno(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lbl = criarLabel(titulo);
		lbl.setBounds(x, y, 200, 20);
		painel.add(lbl);

		JTextField campo = new JTextField(valor);
		campo.setEditable(false);
		campo.setBounds(x, y + 25, 320, 35);
		estilizarCampo(campo);
		painel.add(campo);
		return campo;
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

	private void estilizarBotao(JButton botao) {
		botao.setBackground(corCampo);
		botao.setForeground(textos);
		botao.setFocusPainted(false);
		botao.setBorder(new LineBorder(corBorda));
	}

	private JLabel adicionarIndicador(JPanel painel, String titulo, String valor, int x, int y, Color cor) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(textos);
		lblTitulo.setBounds(x, y, 180, 20);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(cor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
		lblValor.setBounds(x, y + 20, 200, 40);
		painel.add(lblValor);
		return lblValor;
	}

	private <T> void estilizarCombo(JComboBox<T> combo) {
		combo.setBackground(corCampo);
		combo.setForeground(textos);
		combo.setBorder(new LineBorder(corBorda));
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
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