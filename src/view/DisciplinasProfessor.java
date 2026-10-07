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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.DisciplinaDAO;
import dao.ProfessorDAO;
import database.ConnectionFactory;
import model.Professor;
import model.Usuario;
import util.SessaoUsuario;

public class DisciplinasProfessor extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTable tabelaDisciplinas;
	private DefaultTableModel modeloTabela;
	private JLabel lblProfessorResumo, lblDisciplinasResumo, lblTurmasResumo, lblCargaResumo, lblAlunosResumo;

	public DisciplinasProfessor() {
		setTitle("Minhas Disciplinas - Professor");
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
		carregarDadosDoBanco();
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

		JPanel painelRolagem = new JPanel(null);
		painelRolagem.setBackground(corInterna);

		int larguraConteudo = 1220;
		int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

		if (xInicial < 0) {
			xInicial = 0;
		}

		JScrollPane scroll = new JScrollPane(painelRolagem);
		scroll.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		scroll.getViewport().setBackground(corInterna);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		interno.add(scroll);

		JPanel cardResumo = criarPainelArredondado();
		cardResumo.setLayout(null);
		cardResumo.setBounds(xInicial, 0, 1220, 150);
		painelRolagem.add(cardResumo);

		lblProfessorResumo = adicionarResumo(cardResumo, "Professor", "Não informado", 30, 30);
		adicionarResumo(cardResumo, "Matrícula", "Não informado", 310, 30);
		adicionarResumo(cardResumo, "Área principal", "Não informado", 550, 30);
		adicionarResumo(cardResumo, "Turno", "Não informado", 780, 30);
		adicionarResumo(cardResumo, "Ano letivo", "2026", 1030, 30);

		JLabel aviso = new JLabel("Disciplinas vinculadas ao professor para lançamento de notas, frequência e acompanhamento das turmas.");
		aviso.setForeground(new Color(120, 255, 170));
		aviso.setFont(new Font("Segoe UI", Font.BOLD, 16));
		aviso.setBounds(30, 98, 1000, 25);
		cardResumo.add(aviso);

		int yCard = 180;

		JPanel cardDisciplinas = criarCardIndicador("Disciplinas", "0", new Color(120, 200, 255));
		lblDisciplinasResumo = (JLabel) cardDisciplinas.getComponent(1);
		cardDisciplinas.setBounds(xInicial, yCard, 285, 130);
		painelRolagem.add(cardDisciplinas);

		JPanel cardTurmas = criarCardIndicador("Turmas Vinculadas", "0", new Color(120, 255, 170));
		lblTurmasResumo = (JLabel) cardTurmas.getComponent(1);
		cardTurmas.setBounds(xInicial + 312, yCard, 285, 130);
		painelRolagem.add(cardTurmas);

		JPanel cardCarga = criarCardIndicador("Carga Horária", "0", new Color(255, 170, 90));
		lblCargaResumo = (JLabel) cardCarga.getComponent(1);
		cardCarga.setBounds(xInicial + 624, yCard, 285, 130);
		painelRolagem.add(cardCarga);

		JPanel cardAlunos = criarCardIndicador("Alunos Atendidos", "0", new Color(255, 100, 180));
		lblAlunosResumo = (JLabel) cardAlunos.getComponent(1);
		cardAlunos.setBounds(xInicial + 936, yCard, 285, 130);
		painelRolagem.add(cardAlunos);

		JPanel cardTabela = criarPainelArredondado();
		cardTabela.setLayout(null);
		cardTabela.setBounds(xInicial, 340, 1220, 370);
		painelRolagem.add(cardTabela);

		JLabel tituloTabela = new JLabel("Disciplinas Cadastradas");
		tituloTabela.setForeground(corLabel);
		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 25));
		tituloTabela.setBounds(30, 22, 450, 35);
		cardTabela.add(tituloTabela);

		JLabel subtituloTabela = new JLabel("Consulte suas disciplinas, turmas vinculadas, carga horária e quantidade de alunos.");
		subtituloTabela.setForeground(textos);
		subtituloTabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		subtituloTabela.setBounds(32, 58, 950, 25);
		cardTabela.add(subtituloTabela);

		criarTabela();

		JScrollPane scrollTabela = new JScrollPane(tabelaDisciplinas);
		scrollTabela.setBounds(30, 100, 1160, 240);
		scrollTabela.setBorder(new LineBorder(corBorda));
		scrollTabela.getViewport().setBackground(corCampo);
		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
		scrollTabela.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scrollTabela.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		cardTabela.add(scrollTabela);
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

		JLabel titulo = new JLabel("Minhas Disciplinas");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 80, 650, 45);
		topo.add(titulo);

		JLabel sub = new JLabel("Visualize todas as disciplinas vinculadas ao seu cadastro de professor.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(42, 120, 900, 25);
		topo.add(sub);
		return topo;
	}

	private void carregarDadosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null || usuario.getProfessorId() <= 0) {
			mostrarErro("Nenhum professor está autenticado.");
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			Professor professor = new ProfessorDAO(conn).buscarPorId(usuario.getProfessorId());
			
			if (professor == null)
				throw new IllegalArgumentException("Professor não encontrado.");
			lblProfessorResumo.setText(professor.getNome());
			List<Object[]> linhas = new DisciplinaDAO(conn).listarDetalhesPorProfessor(professor.getIdProfessor());
			int turmas = 0, carga = 0, alunos = 0;
			java.util.Set<String> turmasUnicas = new java.util.HashSet<>();
			for (Object[] linha : linhas) {
				modeloTabela.addRow(new Object[] { linha[0], linha[1], linha[2], "" + linha[3] + "h", linha[4],
						"Não informado", linha[5] });
				carga += ((Number) linha[3]).intValue();
				alunos += ((Number) linha[4]).intValue();
				for (String turma : String.valueOf(linha[2]).split(","))
					turmasUnicas.add(turma.trim());
			}
			turmas = turmasUnicas.size();
			lblDisciplinasResumo.setText(String.valueOf(linhas.size()));
			lblTurmasResumo.setText(String.valueOf(turmas));
			lblCargaResumo.setText(carga + "h");
			lblAlunosResumo.setText(String.valueOf(alunos));
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar as disciplinas: " + ex.getMessage());
		}
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void criarTabela() {
		String[] colunas = { "Código", "Disciplina", "Turmas", "Curso", "Carga Horária", "Alunos", "Tipo", "Situação" };

		modeloTabela = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabelaDisciplinas = new JTable(modeloTabela);
		tabelaDisciplinas.setRowHeight(50);
		tabelaDisciplinas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tabelaDisciplinas.setForeground(textos);
		tabelaDisciplinas.setBackground(corCampo);
		tabelaDisciplinas.setGridColor(corBorda);
		tabelaDisciplinas.setSelectionBackground(new Color(120, 40, 220));
		tabelaDisciplinas.setSelectionForeground(textos);
		tabelaDisciplinas.setShowGrid(true);
		tabelaDisciplinas.setShowVerticalLines(true);
		tabelaDisciplinas.setShowHorizontalLines(true);
		tabelaDisciplinas.setRowSelectionAllowed(true);
		tabelaDisciplinas.setFillsViewportHeight(true);
		tabelaDisciplinas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaDisciplinas.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 15));
		header.setForeground(textos);
		header.setBackground(corCampo);
		header.setPreferredSize(new Dimension(header.getWidth(), 42));
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);

		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
		centro.setHorizontalAlignment(JLabel.CENTER);
		centro.setVerticalAlignment(JLabel.CENTER);
		centro.setForeground(textos);
		centro.setBackground(corCampo);

		for (int i = 0; i < tabelaDisciplinas.getColumnCount(); i++) {
			tabelaDisciplinas.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		tabelaDisciplinas.getColumnModel().getColumn(0).setPreferredWidth(80);
		tabelaDisciplinas.getColumnModel().getColumn(1).setPreferredWidth(190);
		tabelaDisciplinas.getColumnModel().getColumn(2).setPreferredWidth(190);
		tabelaDisciplinas.getColumnModel().getColumn(3).setPreferredWidth(160);
		tabelaDisciplinas.getColumnModel().getColumn(4).setPreferredWidth(150);
		tabelaDisciplinas.getColumnModel().getColumn(5).setPreferredWidth(90);
		tabelaDisciplinas.getColumnModel().getColumn(6).setPreferredWidth(130);
		tabelaDisciplinas.getColumnModel().getColumn(7).setPreferredWidth(100);
	}

	private JPanel criarCardIndicador(String titulo, String valor, Color corValor) {
		JPanel card = criarPainelArredondado();
		card.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(textos);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblTitulo.setBounds(25, 25, 230, 25);
		card.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(corValor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 40));
		lblValor.setBounds(25, 60, 180, 50);
		card.add(lblValor);
		return card;
	}

	private JLabel adicionarResumo(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblTitulo.setBounds(x, y, 200, 22);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(textos);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblValor.setBounds(x, y + 24, 260, 28);
		painel.add(lblValor);
		return lblValor;
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
		botao.setForeground(textos);
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