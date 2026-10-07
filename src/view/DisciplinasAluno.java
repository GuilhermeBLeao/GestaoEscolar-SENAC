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

import javax.swing.BorderFactory;
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

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class DisciplinasAluno extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTable tabelaDisciplinas;
	private DefaultTableModel modeloTabela;
	private JLabel lblAlunoResumo, lblMatriculaResumo, lblTurmaResumo, lblTurnoResumo;

	public DisciplinasAluno() {
		setTitle("Minhas Disciplinas - Aluno");
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
		painelRolagem.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
		interno.add(painelRolagem);

		JPanel cardResumo = criarPainelArredondado();
		cardResumo.setLayout(null);
		cardResumo.setBounds(xInicial, 0, 1220, 145);
		painelRolagem.add(cardResumo);

		lblAlunoResumo = adicionarResumo(cardResumo, "Aluno", "Não informado", 30, 32);
		lblMatriculaResumo = adicionarResumo(cardResumo, "Matrícula", "Não informado", 290, 32);
		lblTurmaResumo = adicionarResumo(cardResumo, "Turma", "Não informado", 510, 32);
		adicionarResumo(cardResumo, "Curso", "Ensino Médio", 680, 32);
		lblTurnoResumo = adicionarResumo(cardResumo, "Turno", "Não informado", 910, 32);

		JLabel status = new JLabel("Disciplinas ativas no ano letivo de 2026");
		status.setForeground(new Color(120, 255, 170));
		status.setFont(new Font("Segoe UI", Font.BOLD, 17));
		status.setBounds(30, 100, 500, 25);
		cardResumo.add(status);

		JPanel cardTabela = criarPainelArredondado();
		cardTabela.setLayout(null);
		cardTabela.setBounds(xInicial, 175, 1220, 550);
		painelRolagem.add(cardTabela);

		JLabel tituloTabela = new JLabel("Disciplinas Cadastradas");
		tituloTabela.setForeground(corLabel);
		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 25));
		tituloTabela.setBounds(30, 22, 400, 35);
		cardTabela.add(tituloTabela);

		JLabel subtituloTabela = new JLabel("Veja abaixo todas as matérias vinculadas à sua turma.");
		subtituloTabela.setForeground(corLabel);
		subtituloTabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		subtituloTabela.setBounds(32, 58, 600, 25);
		cardTabela.add(subtituloTabela);

		criarTabela();

		JScrollPane scrollTabela = new JScrollPane(tabelaDisciplinas);
		scrollTabela.setBounds(30, 100, 1160, 422);
		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
		scrollTabela.setBorder(new LineBorder(corBorda));
		scrollTabela.getViewport().setBackground(corCampo);
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
		titulo.setBounds(40, 80, 550, 45);
		topo.add(titulo);

		JLabel sub = new JLabel("Consulte as disciplinas cadastradas para sua turma e seus respectivos professores.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(42, 120, 900, 25);
		topo.add(sub);

		return topo;
	}

	private void carregarDadosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null || usuario.getAlunoId() <= 0) {
			mostrarErro("Nenhum aluno está autenticado para consultar as disciplinas.");
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			Aluno aluno = new AlunoDAO(conn).buscarPorId(usuario.getAlunoId());
			if (aluno == null)
				throw new IllegalArgumentException("Aluno não encontrado.");
			Turma turma = new dao.TurmaDAO(conn).buscarPorId(aluno.getIdTurma());
			lblAlunoResumo.setText(aluno.getNome());
			lblMatriculaResumo.setText(aluno.getMatricula());
			lblTurmaResumo.setText(turma == null ? "Não informado" : turma.getDescricaoTurma());
			lblTurnoResumo.setText(turma == null ? "Não informado" : turma.getTurno().name());
			for (Object[] linha : new DisciplinaDAO(conn).listarDetalhesPorAluno(aluno.getIdAluno())) {
				modeloTabela.addRow(linha);
			}
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar as disciplinas do banco: " + ex.getMessage());
		}
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void criarTabela() {
		String[] colunas = { "Código", "Disciplina", "Professor", "Carga Horária", "Dias de Aula", "Sala", "Situação" };

		modeloTabela = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabelaDisciplinas = new JTable(modeloTabela);
		tabelaDisciplinas.setRowHeight(42);
		tabelaDisciplinas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tabelaDisciplinas.setForeground(textos);
		tabelaDisciplinas.setBackground(new Color(25, 8, 80));
		tabelaDisciplinas.setGridColor(corBorda);
		tabelaDisciplinas.setSelectionBackground(new Color(120, 40, 220));
		tabelaDisciplinas.setSelectionForeground(textos);
		tabelaDisciplinas.setShowGrid(true);
		tabelaDisciplinas.setFillsViewportHeight(true);

		JTableHeader header = tabelaDisciplinas.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 15));
		header.setForeground(textos);
		header.setBackground(new Color(80, 25, 150));
		header.setPreferredSize(new Dimension(header.getWidth(), 42));
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
		centro.setHorizontalAlignment(JLabel.CENTER);
		centro.setForeground(textos);
		centro.setBackground(new Color(25, 8, 80));

		DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);

				lbl.setHorizontalAlignment(JLabel.LEFT);

				lbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

				return lbl;
			}
		};

		for (int i = 0; i < tabelaDisciplinas.getColumnCount(); i++) {
			tabelaDisciplinas.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		tabelaDisciplinas.getColumnModel().getColumn(1).setCellRenderer(esquerda);
		tabelaDisciplinas.getColumnModel().getColumn(2).setCellRenderer(esquerda);
		tabelaDisciplinas.getColumnModel().getColumn(0).setPreferredWidth(80);
		tabelaDisciplinas.getColumnModel().getColumn(1).setPreferredWidth(200);
		tabelaDisciplinas.getColumnModel().getColumn(2).setPreferredWidth(240);
		tabelaDisciplinas.getColumnModel().getColumn(3).setPreferredWidth(130);
		tabelaDisciplinas.getColumnModel().getColumn(4).setPreferredWidth(190);
		tabelaDisciplinas.getColumnModel().getColumn(5).setPreferredWidth(120);
		tabelaDisciplinas.getColumnModel().getColumn(6).setPreferredWidth(100);
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