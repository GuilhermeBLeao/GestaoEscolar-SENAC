package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.AdvertenciaDAO;
import dao.ProfessorDAO;
import database.ConnectionFactory;
import model.Advertencia;
import model.Professor;
import model.Usuario;
import util.SessaoUsuario;

public class AdvertenciaAluno extends JFrame {
	private static final long serialVersionUID = 1L;
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTable tabela;
	private DefaultTableModel modelo;
	private JComboBox<String> cbFiltro;
	private JLabel lblTituloDetalhe;
	private JTextArea txtDetalhes;
	private final List<AdvertenciaExibicao> advertencias = new ArrayList<>();

	public AdvertenciaAluno() {
		setTitle("Advertências do Aluno");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(false);
		carregarDadosDoBanco();
		montarTela();
		setVisible(true);
	}

	private void montarTela() {
		JPanel fundo = new JPanel(new BorderLayout()) {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				GradientPaint gp = new GradientPaint(0, 0, corExterna, getWidth(), getHeight(), corInterna);
				g2.setPaint(gp);
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		fundo.setBorder(new EmptyBorder(25, 35, 25, 35));
		setContentPane(fundo);
		fundo.add(criarTopo(), BorderLayout.NORTH);

		JPanel conteudoComEspaco = new JPanel(new BorderLayout());
		conteudoComEspaco.setOpaque(false);
		conteudoComEspaco.setBorder(new EmptyBorder(15, 0, 0, 0));
		conteudoComEspaco.add(criarConteudo(), BorderLayout.CENTER);

		fundo.add(conteudoComEspaco, BorderLayout.CENTER);
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				g2.dispose();
			}
		};

		topo.setOpaque(false);
		topo.setPreferredSize(new Dimension(0, 150));

		JLabel titulo = new JLabel("Visualizar Advertência");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 50, 700, 45);
		topo.add(titulo);

		JLabel sub = new JLabel("Visualize as suas advertências");
		sub.setForeground(corLabel);
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(42, 100, 900, 25);
		topo.add(sub);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(40, 10, 120, 40);
		btnVoltar.setBackground(corCampo);
		btnVoltar.setForeground(textos);
		btnVoltar.setBorder(new LineBorder(corBorda));
		btnVoltar.setFocusPainted(false);
		btnVoltar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnVoltar.addActionListener(e -> dispose());
		topo.add(btnVoltar);

		return topo;
	}

	private JPanel criarConteudo() {
		JPanel conteudo = new JPanel(new GridLayout(1, 2, 25, 0));
		conteudo.setOpaque(false);
		conteudo.add(criarPainelTabela());
		conteudo.add(criarPainelDetalhes());
		return conteudo;
	}

	private JPanel criarPainelTabela() {
		JPanel painel = criarCard();
		painel.setLayout(new BorderLayout(0, 15));

		JPanel topoTabela = new JPanel(new BorderLayout());
		topoTabela.setOpaque(false);

		JLabel lbl = new JLabel("Registros encontrados");
		lbl.setForeground(textos);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));

		cbFiltro = new JComboBox<>(new String[] { "Todas", "Ativa" });
		cbFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		cbFiltro.setBackground(corCampo);
		cbFiltro.setForeground(textos);
		cbFiltro.addActionListener(e -> atualizarTabela());

		topoTabela.add(lbl, BorderLayout.WEST);
		topoTabela.add(cbFiltro, BorderLayout.EAST);

		modelo = new DefaultTableModel(new Object[] { "Data", "Tipo", "Responsável", "Situação" }, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabela = new JTable(modelo);
		tabela.setRowHeight(42);
		tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tabela.setForeground(textos);
		tabela.setBackground(corCampo);
		tabela.setSelectionBackground(new Color(90, 45, 170));
		tabela.setSelectionForeground(textos);
		tabela.setGridColor(new Color(85, 45, 145));
		tabela.setShowVerticalLines(false);

		JTableHeader header = tabela.getTableHeader();
		header.setBackground(new Color(55, 15, 120));
		header.setForeground(textos);
		header.setFont(new Font("Segoe UI", Font.BOLD, 14));
		header.setPreferredSize(new Dimension(0, 40));

		tabela.setDefaultRenderer(Object.class, new RenderizadorTabela());

		tabela.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				mostrarDetalhes();
			}
		});

		JScrollPane scroll = new JScrollPane(tabela);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		scroll.getViewport().setBackground(corCampo);
		painel.add(topoTabela, BorderLayout.NORTH);
		painel.add(scroll, BorderLayout.CENTER);
		atualizarTabela();
		return painel;
	}

	private JPanel criarPainelDetalhes() {
		JPanel painel = criarCard();
		painel.setLayout(new BorderLayout(0, 15));

		lblTituloDetalhe = new JLabel("Selecione uma advertência");
		lblTituloDetalhe.setForeground(textos);
		lblTituloDetalhe.setFont(new Font("Segoe UI", Font.BOLD, 24));

		txtDetalhes = new JTextArea();
		txtDetalhes.setEditable(false);
		txtDetalhes.setLineWrap(true);
		txtDetalhes.setWrapStyleWord(true);
		txtDetalhes.setBackground(corCampo);
		txtDetalhes.setForeground(textos);
		txtDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		txtDetalhes.setBorder(new EmptyBorder(18, 18, 18, 18));
		txtDetalhes.setText("Clique em uma advertência da tabela para visualizar os detalhes completos.");

		JScrollPane scroll = new JScrollPane(txtDetalhes);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getVerticalScrollBar().setUnitIncrement(26);

		painel.add(lblTituloDetalhe, BorderLayout.NORTH);
		painel.add(scroll, BorderLayout.CENTER);
		return painel;
	}

	private JPanel criarCard() {
		JPanel card = new JPanel();
		card.setBackground(corCampo);
		card.setBorder(new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(22, 22, 22, 22)));
		return card;
	}

	private void atualizarTabela() {
		modelo.setRowCount(0);

		String filtro = cbFiltro.getSelectedItem().toString();

		for (AdvertenciaExibicao a : advertencias) {
			if (filtro.equals("Todas") || a.situacao.equals(filtro)) {
				modelo.addRow(new Object[] { a.data, a.tipo, a.responsavel, a.situacao });
			}
		}
	}

	private void mostrarDetalhes() {
		int linha = tabela.getSelectedRow();

		if (linha < 0)
			return;

		String data = tabela.getValueAt(linha, 0).toString();
		String tipo = tabela.getValueAt(linha, 1).toString();

		for (AdvertenciaExibicao a : advertencias) {
			if (a.data.equals(data) && a.tipo.equals(tipo)) {
				lblTituloDetalhe.setText(a.tipo + " - " + a.situacao);

				txtDetalhes.setText("Data: " + a.data + "\n\n" + "Tipo: " + a.tipo + "\n\n"
						+ "Responsável pelo registro: " + a.responsavel + "\n\n" + "Situação: " + a.situacao + "\n\n"
						+ "Descrição:\n" + a.descricao + "\n\n" + "Orientação da escola:\n" + a.orientacao);
				break;
			}
		}
	}

	private void carregarDadosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null || usuario.getAlunoId() <= 0) {
			throw new IllegalStateException("Nenhum aluno está autenticado para consultar as advertências.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AdvertenciaDAO advertenciaDAO = new AdvertenciaDAO(conn);
			ProfessorDAO professorDAO = new ProfessorDAO(conn);
			DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

			for (Advertencia advertencia : advertenciaDAO.listarPorAluno(usuario.getAlunoId())) {
				Professor professor = professorDAO.buscarPorId(advertencia.getProfessorId());
				String nomeProfessor = professor == null ? "Não informado" : professor.getNome();

				advertencias.add(new AdvertenciaExibicao(
						advertencia.getDataAdvertencia().format(formatoData), advertencia.getMotivo(), nomeProfessor,
						advertencia.getSituacao(), advertencia.getDescricao(), advertencia.getOrientacao()));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Não foi possível carregar as advertências do banco de dados.", e);
		}
	}

	private class RenderizadorTabela extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			setHorizontalAlignment(SwingConstants.CENTER);
			setBorder(new EmptyBorder(5, 8, 5, 8));

			if (!isSelected) {
				c.setBackground(corCampo);
				c.setForeground(textos);
			}

			if (column == 3 && value != null) {
				String situacao = value.toString();

				if (situacao.equals("Pendente")) {
					c.setForeground(new Color(255, 180, 90));
				} else if (situacao.equals("Ciente")) {
					c.setForeground(new Color(120, 200, 255));
				} else if (situacao.equals("Resolvida")) {
					c.setForeground(new Color(120, 255, 170));
				}
			}

			return c;
		}
	}

	private static class AdvertenciaExibicao {
		String data;
		String tipo;
		String responsavel;
		String situacao;
		String descricao;
		String orientacao;

		AdvertenciaExibicao(String data, String tipo, String responsavel, String situacao, String descricao,
				String orientacao) {
			this.data = data;
			this.tipo = tipo;
			this.responsavel = responsavel;
			this.situacao = situacao;
			this.descricao = descricao;
			this.orientacao = orientacao;
		}
	}
}