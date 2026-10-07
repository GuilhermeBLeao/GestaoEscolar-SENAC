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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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

import dao.AvisoDAO;
import database.ConnectionFactory;
import model.Aviso;
import model.Usuario;
import util.SessaoUsuario;

public class AvisosResponsavel extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;

	private JTable tabelaAvisos;
	private DefaultTableModel modeloTabela;
	private JComboBox<String> cbCategoria;
	private JComboBox<String> cbStatus;
	private JLabel lblNaoLidos;
	private JLabel lblLidos;
	private JLabel lblImportantes;
	private JLabel lblTotal;
	private JTextField txtTitulo;
	private JTextField txtData;
	private JTextArea areaDetalhes;
	private JTextArea areaImportantes;
	private JTextArea areaComunicados;
	private final List<Aviso> avisos = new ArrayList<>();
	private final List<Aviso> avisosExibidos = new ArrayList<>();
	private Aviso avisoSelecionado;
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final int LARGURA_CARD = 1220;

	public AvisosResponsavel() {
		setTitle("Avisos");
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
		interno.setBackground(corInterna);
		interno.setBounds(20, 20, largura, altura);
		externo.add(interno);

		criarTela(interno, largura, altura);
		carregarAvisosDoBanco();
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

		int larguraConteudo = Math.max(LARGURA_CARD + 60, largura - 100);

		JPanel conteudo = new JPanel(null);
		conteudo.setBackground(corInterna);
		conteudo.setPreferredSize(new Dimension(larguraConteudo, 1870));

		JScrollPane scroll = new JScrollPane(conteudo);
		scroll.setBounds(30, 210, largura - 60, altura - 250);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		painel.add(scroll);

		criarResumo(conteudo, larguraConteudo);
		criarFiltros(conteudo, larguraConteudo);
		criarTabelaAvisos(conteudo, larguraConteudo);
		criarDetalhesAviso(conteudo, larguraConteudo);
		criarAvisosImportantes(conteudo, larguraConteudo);
		criarComunicadosGerais(conteudo, larguraConteudo);
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

		JLabel titulo = new JLabel("Avisos");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 80, 400, 45);
		topo.add(titulo);

		JLabel subtitulo = new JLabel("Avisos enviados pela escola");
		subtitulo.setForeground(new Color(240, 240, 255));
		subtitulo.setBounds(45, 120, 400, 25);
		topo.add(subtitulo);
		return topo;
	}

	private void criarResumo(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Resumo");
		card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

		lblNaoLidos = adicionarIndicador(card, "Não Lidos", "0", 60, 50, Color.ORANGE);
		lblLidos = adicionarIndicador(card, "Lidos", "0", 350, 50, Color.GREEN);
		lblImportantes = adicionarIndicador(card, "Importantes", "0", 650, 50, Color.RED);
		lblTotal = adicionarIndicador(card, "Total", "0", 950, 50, Color.CYAN);
		painel.add(card);
	}

	private void criarFiltros(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Filtros");
		card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 180);

		JLabel lblCategoria = criarLabel("Categoria");
		lblCategoria.setBounds(30, 50, 120, 25);
		card.add(lblCategoria);

		cbCategoria = new JComboBox<>(new String[] { "Todas" });
		cbCategoria.setBounds(30, 80, 250, 35);
		estilizarCombo(cbCategoria);
		card.add(cbCategoria);

		JLabel lblStatus = criarLabel("Status");
		lblStatus.setBounds(330, 50, 120, 25);
		card.add(lblStatus);

		cbStatus = new JComboBox<>(new String[] { "Todos", "Lido", "Não Lido" });
		cbStatus.setBounds(330, 80, 220, 35);
		estilizarCombo(cbStatus);
		card.add(cbStatus);

		JButton btnPesquisar = new JButton("Pesquisar");
		btnPesquisar.setBounds(650, 78, 180, 40);
		estilizarBotao(btnPesquisar);
		btnPesquisar.addActionListener(e -> atualizarTabela());
		card.add(btnPesquisar);
		painel.add(card);
	}

	private void criarTabelaAvisos(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Lista de Avisos");
		card.setBounds(centerX(larguraConteudo), 390, LARGURA_CARD, 400);

		String[] colunas = { "Data", "Categoria", "Título", "Prioridade", "Status" };

		modeloTabela = new DefaultTableModel(colunas, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tabelaAvisos = new JTable(modeloTabela);
		tabelaAvisos.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				carregarAvisoSelecionado();
			}
		});
		tabelaAvisos.setRowHeight(30);
		tabelaAvisos.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		tabelaAvisos.setForeground(textos);
		tabelaAvisos.setBackground(corInterna);
		tabelaAvisos.setGridColor(new Color(90, 50, 170));
		tabelaAvisos.setSelectionBackground(new Color(80, 40, 160));
		tabelaAvisos.setSelectionForeground(textos);
		tabelaAvisos.setShowGrid(true);
		tabelaAvisos.setShowHorizontalLines(true);
		tabelaAvisos.setShowVerticalLines(true);
		tabelaAvisos.setIntercellSpacing(new Dimension(1, 1));
		tabelaAvisos.setFillsViewportHeight(false);
		tabelaAvisos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaAvisos.getTableHeader();
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

		for (int i = 0; i < tabelaAvisos.getColumnCount(); i++) {
			tabelaAvisos.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JScrollPane scroll = new JScrollPane(tabelaAvisos);
		scroll.setBounds(25, 60, 1170, 300);
		scroll.getViewport().setBackground(corInterna);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void criarDetalhesAviso(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Detalhes do Aviso");
		card.setBounds(centerX(larguraConteudo), 810, LARGURA_CARD, 380);

		JLabel lblTitulo = criarLabel("Título");
		lblTitulo.setBounds(30, 60, 120, 25);
		card.add(lblTitulo);

		txtTitulo = new JTextField();
		txtTitulo.setEditable(false);
		txtTitulo.setBounds(30, 90, 600, 35);
		estilizarCampo(txtTitulo);
		card.add(txtTitulo);

		JLabel lblData = criarLabel("Data");
		lblData.setBounds(700, 60, 120, 25);
		card.add(lblData);

		txtData = new JTextField();
		txtData.setEditable(false);
		txtData.setBounds(700, 90, 220, 35);
		estilizarCampo(txtData);
		card.add(txtData);

		areaDetalhes = criarAreaSomenteLeitura();

		JScrollPane scroll = new JScrollPane(areaDetalhes);
		scroll.setBounds(30, 150, 1160, 190);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void criarAvisosImportantes(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Avisos Importantes");
		card.setBounds(centerX(larguraConteudo), 1210, LARGURA_CARD, 250);

		areaImportantes = criarAreaSomenteLeitura();

		JScrollPane scrollImportantes = new JScrollPane(areaImportantes);
		scrollImportantes.setBounds(25, 60, 1170, 160);
		scrollImportantes.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scrollImportantes);
		painel.add(card);
	}

	private void criarComunicadosGerais(JPanel painel, int larguraConteudo) {
		JPanel card = criarCard("Comunicados Gerais");
		card.setBounds(centerX(larguraConteudo), 1480, LARGURA_CARD, 350);

		areaComunicados = criarAreaSomenteLeitura();

		JScrollPane scroll = new JScrollPane(areaComunicados);
		scroll.setBounds(25, 60, 1170, 250);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scroll);
		painel.add(card);
	}

	private void carregarAvisosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null || usuario.getPaiId() <= 0 || usuario.getIdUsuario() <= 0) {
			mostrarErro("Nenhum responsável autenticado para consultar os avisos.");
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			avisos.clear();
			avisos.addAll(new AvisoDAO(conn).listarParaResponsavel(usuario.getPaiId(), usuario.getIdUsuario()));
			cbCategoria.removeAllItems();
			cbCategoria.addItem("Todas");
			for (String categoria : new LinkedHashSet<>(avisos.stream().map(Aviso::getCategoria).toList())) {
				cbCategoria.addItem(categoria);
			}
			atualizarTabela();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os avisos do banco: " + ex.getMessage());
		}
	}

	private void atualizarTabela() {
		String categoria = String.valueOf(cbCategoria.getSelectedItem());
		String status = String.valueOf(cbStatus.getSelectedItem());
		avisosExibidos.clear();
		modeloTabela.setRowCount(0);
		for (Aviso aviso : avisos) {
			String statusAviso = aviso.isLido() ? "Lido" : "Não Lido";
			if ((!"Todas".equals(categoria) && !categoria.equals(aviso.getCategoria()))
					|| (!"Todos".equals(status) && !status.equals(statusAviso))) {
				continue;
			}
			avisosExibidos.add(aviso);
			modeloTabela
					.addRow(new Object[] { data(aviso), aviso.getCategoria(), aviso.getTitulo(), aviso.getPrioridade(),
							statusAviso });
		}
		atualizarResumo();
		carregarPainelComunicados();
	}

	private void carregarAvisoSelecionado() {
		int linha = tabelaAvisos.getSelectedRow();
		if (linha < 0 || linha >= avisosExibidos.size()) {
			return;
		}
		avisoSelecionado = avisosExibidos.get(linha);
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario != null && usuario.getIdUsuario() > 0 && !avisoSelecionado.isLido()) {
			try (Connection conn = ConnectionFactory.getConnection()) {
				new AvisoDAO(conn).marcarComoLido(avisoSelecionado.getIdAviso(), usuario.getIdUsuario());
				avisoSelecionado.setLido(true);
			} catch (SQLException ex) {
				mostrarErro("Não foi possível registrar a leitura do aviso: " + ex.getMessage());
			}
		}
		txtTitulo.setText(avisoSelecionado.getTitulo());
		txtData.setText(data(avisoSelecionado));
		areaDetalhes.setText(avisoSelecionado.getDescricao());
		atualizarTabela();
	}

	private void atualizarResumo() {
		long naoLidos = avisos.stream().filter(aviso -> !aviso.isLido()).count();
		long importantes = avisos.stream().filter(aviso -> "Alta".equalsIgnoreCase(aviso.getPrioridade())).count();
		lblNaoLidos.setText(String.valueOf(naoLidos));
		lblLidos.setText(String.valueOf(avisos.size() - naoLidos));
		lblImportantes.setText(String.valueOf(importantes));
		lblTotal.setText(String.valueOf(avisos.size()));
	}

	private void carregarPainelComunicados() {
		StringBuilder importantes = new StringBuilder();
		StringBuilder comunicados = new StringBuilder();
		for (Aviso aviso : avisosExibidos) {
			String linha = data(aviso) + " - " + aviso.getTitulo() + "\n" + aviso.getDescricao() + "\n\n";
			comunicados.append(linha);
			if ("Alta".equalsIgnoreCase(aviso.getPrioridade())) {
				importantes.append(linha);
			}
		}
		areaImportantes.setText(importantes.toString());
		areaComunicados.setText(comunicados.toString());
	}

	private String data(Aviso aviso) {
		return aviso.getDataAviso() == null ? "Não informado" : aviso.getDataAviso().format(FORMATO_DATA);
	}

	private JTextArea criarAreaSomenteLeitura() {
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBackground(corCampo);
		area.setForeground(textos);
		return area;
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
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
		lblTitulo.setBounds(x, y, 180, 20);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(cor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
		lblValor.setBounds(x, y + 20, 200, 40);
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
