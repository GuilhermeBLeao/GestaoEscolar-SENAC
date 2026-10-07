package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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

public class AvisosSecretaria extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTable tabelaAvisos;
	private DefaultTableModel modeloTabela;
	private JComboBox<String> cbCategoria;
	private JComboBox<String> cbStatus;
	private JTextArea areaDetalhes;
	private JTextArea areaImportantes;
	private JTextArea areaComunicados;
	private JLabel lblTotal;
	private JLabel lblNaoLidos;
	private JLabel lblImportantes;
	private final List<Aviso> avisos = new ArrayList<>();
	private final List<Aviso> avisosExibidos = new ArrayList<>();
	private Aviso avisoSelecionado;

	public AvisosSecretaria() {
		setTitle("Avisos");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int margem = 30;
		int larguraInterno = areaUtil.width - (margem * 2);
		int alturaInterno = areaUtil.height - (margem * 2);
		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);

		JPanel interno = new JPanel(null);
		interno.setBounds((areaUtil.width - larguraInterno) / 2, (areaUtil.height - alturaInterno) / 2, larguraInterno,
				alturaInterno);
		interno.setBackground(corInterna);
		externo.add(interno);
		criarTela(interno, larguraInterno, alturaInterno);
		carregarAvisosDoBanco();
		setLocationRelativeTo(null);
	}

	private void criarTela(JPanel interno, int larguraInterno, int alturaInterno) {
		JLabel titulo = new JLabel("Avisos");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBounds(0, 35, larguraInterno, 55);
		interno.add(titulo);

		JLabel subtitulo = new JLabel("Visualize e publique avisos para a comunidade escolar.");
		subtitulo.setForeground(textos);
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 19));
		subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
		subtitulo.setBounds(0, 90, larguraInterno, 30);
		interno.add(subtitulo);

		JPanel painelFiltros = criarPainelArredondado(corCampo, corBorda, 24);
		painelFiltros.setLayout(null);
		painelFiltros.setBounds(35, 140, larguraInterno - 70, 105);
		interno.add(painelFiltros);
		adicionarLabel(painelFiltros, "Categoria", 25, 10);
		cbCategoria = criarCombo(new String[] { "Todas", "Acadêmico", "Administrativo", "Evento", "Urgente", "Comunicado", "Outro" });
		cbCategoria.setBounds(25, 42, 180, 36);
		painelFiltros.add(cbCategoria);
		adicionarLabel(painelFiltros, "Status", 225, 10);
		cbStatus = criarCombo(new String[] { "Todos", "Não lidos", "Lidos" });
		cbStatus.setBounds(225, 42, 160, 36);
		painelFiltros.add(cbStatus);

		JButton btnFiltrar = new JButton("Filtrar");
		btnFiltrar.setBounds(405, 42, 120, 36);
		estilizarBotao(btnFiltrar);
		painelFiltros.add(btnFiltrar);

		JButton btnNovoAviso = new JButton("Novo aviso");
		btnNovoAviso.setBounds(painelFiltros.getWidth() - 180, 42, 150, 36);
		estilizarBotao(btnNovoAviso);
		painelFiltros.add(btnNovoAviso);
		
		JButton btnVoltar = new JButton("Voltar");
		btnVoltar.setBounds(80, 50, 120, 36);
		estilizarBotao(btnVoltar);
		interno.add(btnVoltar);

		JPanel painelTabela = criarPainelArredondado(corCampo, corBorda, 24);
		painelTabela.setLayout(null);
		painelTabela.setBounds(35, 260, (larguraInterno * 60) / 100, alturaInterno - 295);
		interno.add(painelTabela);

		JLabel tituloTabela = new JLabel("Avisos cadastrados");
		tituloTabela.setForeground(textos);
		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 26));
		tituloTabela.setBounds(25, 20, 350, 35);
		painelTabela.add(tituloTabela);
		criarTabela();

		JScrollPane scrollTabela = new JScrollPane(tabelaAvisos);
		scrollTabela.setBounds(25, 65, painelTabela.getWidth() - 50, painelTabela.getHeight() - 90);
		scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
		scrollTabela.getViewport().setBackground(corInterna);
		scrollTabela.getVerticalScrollBar().setUnitIncrement(20);
		painelTabela.add(scrollTabela);

		JPanel painelDireito = criarPainelArredondado(corCampo, corBorda, 24);
		painelDireito.setLayout(null);
		painelDireito.setBounds(painelTabela.getX() + painelTabela.getWidth() + 20, 260, larguraInterno - painelTabela.getWidth() - 75, alturaInterno - 295);
		interno.add(painelDireito);

		JLabel tituloDetalhes = new JLabel("Detalhes");
		tituloDetalhes.setForeground(textos);
		tituloDetalhes.setFont(new Font("Segoe UI", Font.BOLD, 24));
		tituloDetalhes.setBounds(25, 20, 250, 35);
		painelDireito.add(tituloDetalhes);
		areaDetalhes = criarAreaTexto();
		areaDetalhes.setEditable(false);

		JScrollPane scrollDetalhes = new JScrollPane(areaDetalhes);
		scrollDetalhes.setBounds(25, 65, painelDireito.getWidth() - 50, 180);
		scrollDetalhes.setBorder(new LineBorder(corBorda, 1, true));
		painelDireito.add(scrollDetalhes);

		JLabel tituloImportantes = new JLabel("Avisos importantes");
		tituloImportantes.setForeground(textos);
		tituloImportantes.setFont(new Font("Segoe UI", Font.BOLD, 21));
		tituloImportantes.setBounds(25, 265, 250, 30);
		painelDireito.add(tituloImportantes);
		areaImportantes = criarAreaTexto();
		areaImportantes.setEditable(false);

		JScrollPane scrollImportantes = new JScrollPane(areaImportantes);
		scrollImportantes.setBounds(25, 300, painelDireito.getWidth() - 50, 100);
		scrollImportantes.setBorder(new LineBorder(corBorda, 1, true));
		painelDireito.add(scrollImportantes);

		JLabel tituloComunicados = new JLabel("Comunicados");
		tituloComunicados.setForeground(textos);
		tituloComunicados.setFont(new Font("Segoe UI", Font.BOLD, 21));
		tituloComunicados.setBounds(25, 420, 250, 30);
		painelDireito.add(tituloComunicados);
		areaComunicados = criarAreaTexto();
		areaComunicados.setEditable(false);

		JScrollPane scrollComunicados = new JScrollPane(areaComunicados);
		scrollComunicados.setBounds(25, 455, painelDireito.getWidth() - 50, 100);
		scrollComunicados.setBorder(new LineBorder(corBorda, 1, true));
		painelDireito.add(scrollComunicados);

		JPanel painelResumo = criarPainelArredondado(corCampo, corBorda, 20);
		painelResumo.setLayout(null);
		painelResumo.setBounds(35, 205, 300, 0);
		lblTotal = criarLabelResumo("Total: 0");
		lblNaoLidos = criarLabelResumo("Não lidos: 0");
		lblImportantes = criarLabelResumo("Importantes: 0");
		cbCategoria.addActionListener(e -> aplicarFiltros());
		cbStatus.addActionListener(e -> aplicarFiltros());
		tabelaAvisos.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				carregarAvisoSelecionado();
			}
		});
		btnFiltrar.addActionListener(e -> aplicarFiltros());
		btnNovoAviso.addActionListener(e -> abrirFormularioNovoAviso());
		btnVoltar.addActionListener(e -> dispose());
	}

	private void criarTabela() {
		String[] colunas = { "Data", "Categoria", "Título", "Prioridade", "Público", "Status" };
		modeloTabela = new DefaultTableModel(new Object[][] {}, colunas) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tabelaAvisos = new JTable(modeloTabela);
		tabelaAvisos.setRowHeight(42);
		tabelaAvisos.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tabelaAvisos.setForeground(textos);
		tabelaAvisos.setBackground(corInterna);
		tabelaAvisos.setGridColor(new Color(90, 50, 170));
		tabelaAvisos.setSelectionBackground(new Color(80, 40, 160));
		tabelaAvisos.setSelectionForeground(textos);
		tabelaAvisos.setShowGrid(true);
		tabelaAvisos.setShowHorizontalLines(true);
		tabelaAvisos.setShowVerticalLines(true);
		tabelaAvisos.setIntercellSpacing(new Dimension(1, 1));
		tabelaAvisos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaAvisos.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 14));
		header.setBackground(corInterna);
		header.setForeground(textos);
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);
		header.setPreferredSize(new Dimension(header.getWidth(), 32));
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		renderer.setVerticalAlignment(SwingConstants.CENTER);
		renderer.setBackground(new Color(31, 10, 90));
		renderer.setForeground(textos);
		renderer.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		for (int i = 0; i < tabelaAvisos.getColumnCount(); i++) {
			tabelaAvisos.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		tabelaAvisos.getColumnModel().getColumn(0).setPreferredWidth(90);
		tabelaAvisos.getColumnModel().getColumn(1).setPreferredWidth(120);
		tabelaAvisos.getColumnModel().getColumn(2).setPreferredWidth(260);
		tabelaAvisos.getColumnModel().getColumn(3).setPreferredWidth(100);
		tabelaAvisos.getColumnModel().getColumn(4).setPreferredWidth(120);
		tabelaAvisos.getColumnModel().getColumn(5).setPreferredWidth(100);
	}

	private void carregarAvisosDoBanco() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getIdUsuario() <= 0) {
			mostrarErro("Nenhum usuário autenticado para consultar os avisos.");
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			AvisoDAO avisoDAO = new AvisoDAO(conn);
			avisos.clear();
			avisos.addAll(avisoDAO.listar());
			aplicarFiltros();
		} catch (SQLException | RuntimeException e) {
			mostrarErro("Não foi possível carregar os avisos.\n\n" + e.getMessage());
		}
	}

	private void aplicarFiltros() {
		String categoriaSelecionada = cbCategoria == null ? "Todas" : String.valueOf(cbCategoria.getSelectedItem());
		String statusSelecionado = cbStatus == null ? "Todos" : String.valueOf(cbStatus.getSelectedItem());
		avisosExibidos.clear();
		modeloTabela.setRowCount(0);

		for (Aviso aviso : avisos) {
			if (!"Todas".equals(categoriaSelecionada) &&
					!categoriaSelecionada.equalsIgnoreCase(valorOuVazio(aviso.getCategoria()))) {
				continue;
			}
			if ("Não lidos".equals(statusSelecionado) && aviso.isLido()) {
				continue;
			}
			if ("Lidos".equals(statusSelecionado) && !aviso.isLido()) {
				continue;
			}
			avisosExibidos.add(aviso);
			modeloTabela.addRow(new Object[] { formatarData(aviso.getDataAviso()), valorOuVazio(aviso.getCategoria()),
					valorOuVazio(aviso.getTitulo()), valorOuVazio(aviso.getPrioridade()),
					valorOuVazio(aviso.getPublico()), aviso.isLido() ? "Lido" : "Não lido" });
		}
		atualizarResumo();
		carregarPainelComunicados();
	}

	private void carregarAvisoSelecionado() {
		int linha = tabelaAvisos.getSelectedRow();

		if (linha < 0 || linha >= avisosExibidos.size()) {
			avisoSelecionado = null;

			if (areaDetalhes != null) {
				areaDetalhes.setText("");
			}
			return;
		}
		avisoSelecionado = avisosExibidos.get(linha);

		StringBuilder texto = new StringBuilder();
		texto.append("Título: ").append(valorOuVazio(avisoSelecionado.getTitulo())).append("\n\n");
		texto.append("Categoria: ").append(valorOuVazio(avisoSelecionado.getCategoria())).append("\n");
		texto.append("Prioridade: ").append(valorOuVazio(avisoSelecionado.getPrioridade())).append("\n");
		texto.append("Público: ").append(valorOuVazio(avisoSelecionado.getPublico())).append("\n");
		texto.append("Data: ").append(formatarData(avisoSelecionado.getDataAviso())).append("\n\n");
		texto.append(valorOuVazio(avisoSelecionado.getDescricao()));
		areaDetalhes.setText(texto.toString());

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getIdUsuario() <= 0) {

			return;
		}
		if (!avisoSelecionado.isLido()) {
			try (Connection conn = ConnectionFactory.getConnection()) {
				new AvisoDAO(conn).marcarComoLido(avisoSelecionado.getIdAviso(), usuario.getIdUsuario());
				avisoSelecionado.setLido(true);

				if (linha >= 0 && linha < modeloTabela.getRowCount()) {
					modeloTabela.setValueAt("Lido", linha, 5);
				}
				atualizarResumo();
			} catch (SQLException | RuntimeException e) {
			}
		}
	}

	private void atualizarResumo() {
		int total = avisos.size();
		int naoLidos = 0;
		int importantes = 0;

		for (Aviso aviso : avisos) {
			if (!aviso.isLido()) {
				naoLidos++;
			}
			if ("Alta".equalsIgnoreCase(valorOuVazio(aviso.getPrioridade()))
					|| "Urgente".equalsIgnoreCase(valorOuVazio(aviso.getPrioridade()))) {
				importantes++;
			}
		}
		if (lblTotal != null) {
			lblTotal.setText("Total: " + total);
		}
		if (lblNaoLidos != null) {
			lblNaoLidos.setText("Não lidos: " + naoLidos);
		}
		if (lblImportantes != null) {
			lblImportantes.setText("Importantes: " + importantes);
		}
	}

	private void carregarPainelComunicados() {
		if (areaImportantes == null || areaComunicados == null) {
			return;
		}
		StringBuilder importantes = new StringBuilder();
		StringBuilder comunicados = new StringBuilder();

		for (Aviso aviso : avisos) {
			String prioridade = valorOuVazio(aviso.getPrioridade());

			if ("Alta".equalsIgnoreCase(prioridade) || "Urgente".equalsIgnoreCase(prioridade)) {
				importantes.append("• ").append(valorOuVazio(aviso.getTitulo())).append("\n");
			}
			String categoria = valorOuVazio(aviso.getCategoria());

			if ("Comunicado".equalsIgnoreCase(categoria) || "Administrativo".equalsIgnoreCase(categoria)) {
				comunicados.append("• ").append(valorOuVazio(aviso.getTitulo())).append("\n");
			}
		}
		if (importantes.length() == 0) {
			importantes.append("Nenhum aviso importante.");
		}
		if (comunicados.length() == 0) {
			comunicados.append("Nenhum comunicado disponível.");
		}
		areaImportantes.setText(importantes.toString());
		areaComunicados.setText(comunicados.toString());
	}

	private void abrirFormularioNovoAviso() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getIdUsuario() <= 0) {
			mostrarErro("É necessário estar autenticado para cadastrar um aviso.");
			return;
		}

		JTextField campoTitulo = criarCampoTexto();
		campoTitulo.setPreferredSize(new Dimension(350, 32));
		JComboBox<String> campoCategoria = criarCombo(
				new String[] { "Acadêmico", "Administrativo", "Evento", "Urgente", "Comunicado", "Outro" });
		JComboBox<String> campoPrioridade = criarCombo(new String[] { "Baixa", "Média", "Alta", "Urgente" });
		JComboBox<String> campoPublico = criarCombo(
				new String[] { "Todos", "Professores", "Funcionários", "Responsáveis" });

		JTextField campoData = criarCampoTexto();
		campoData.setText(FORMATO_DATA.format(LocalDate.now()));
		campoData.setPreferredSize(new Dimension(350, 32));

		JTextArea campoDescricao = criarAreaTexto();
		campoDescricao.setRows(6);
		campoDescricao.setLineWrap(true);
		campoDescricao.setWrapStyleWord(true);

		JScrollPane scrollDescricao = new JScrollPane(campoDescricao);
		scrollDescricao.setPreferredSize(new Dimension(350, 130));

		JPanel painel = new JPanel();
		painel.setLayout(new javax.swing.BoxLayout(painel, javax.swing.BoxLayout.Y_AXIS));
		painel.setBackground(corInterna);

		JLabel labelTitulo = criarLabelFormulario("Título");
		JLabel labelCategoria = criarLabelFormulario("Categoria");
		JLabel labelPrioridade = criarLabelFormulario("Prioridade");
		JLabel labelPublico = criarLabelFormulario("Público");
		JLabel labelData = criarLabelFormulario("Data do aviso");
		JLabel labelDescricao = criarLabelFormulario("Descrição");

		painel.add(labelTitulo);
		painel.add(campoTitulo);
		painel.add(javax.swing.Box.createVerticalStrut(10));
		painel.add(labelCategoria);
		painel.add(campoCategoria);
		painel.add(javax.swing.Box.createVerticalStrut(10));
		painel.add(labelPrioridade);
		painel.add(campoPrioridade);
		painel.add(javax.swing.Box.createVerticalStrut(10));
		painel.add(labelPublico);
		painel.add(campoPublico);
		painel.add(javax.swing.Box.createVerticalStrut(10));
		painel.add(labelData);
		painel.add(campoData);
		painel.add(javax.swing.Box.createVerticalStrut(10));
		painel.add(labelDescricao);
		painel.add(scrollDescricao);

		int resultado = JOptionPane.showConfirmDialog(this, painel, "Novo aviso", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (resultado != JOptionPane.OK_OPTION) {
			return;
		}

		String titulo = campoTitulo.getText().trim();
		String descricao = campoDescricao.getText().trim();
		String categoria = String.valueOf(campoCategoria.getSelectedItem());
		String prioridade = String.valueOf(campoPrioridade.getSelectedItem());
		String publico = String.valueOf(campoPublico.getSelectedItem());
		String dataTexto = campoData.getText().trim();

		if (titulo.isEmpty()) {
			mostrarAviso("Informe o título do aviso.");
			return;
		}
		if (descricao.isEmpty()) {
			mostrarAviso("Informe a descrição do aviso.");
			return;
		}
		LocalDate dataAviso;
		try {
			dataAviso = LocalDate.parse(dataTexto, FORMATO_DATA);
		} catch (DateTimeParseException e) {
			mostrarAviso("A data informada é inválida.\n\n" + "Utilize o formato dd/MM/yyyy.");
			return;
		}
		Aviso aviso = new Aviso();
		aviso.setTitulo(titulo);
		aviso.setDescricao(descricao);
		aviso.setCategoria(categoria);
		aviso.setPrioridade(prioridade);
		aviso.setPublico(publico);
		aviso.setTurmaId(null);
		aviso.setDataAviso(dataAviso);
		aviso.setLido(false);
		try (Connection conn = ConnectionFactory.getConnection()) {
			AvisoDAO avisoDAO = new AvisoDAO(conn);
			avisoDAO.inserir(aviso);
			JOptionPane.showMessageDialog(this, "Aviso cadastrado com sucesso!", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			carregarAvisosDoBanco();
		} catch (SQLException | RuntimeException e) {
			mostrarErro("Não foi possível cadastrar o aviso.\n\n" + e.getMessage());
		}
	}

	private JLabel criarLabelFormulario(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		return label;
	}

	private JLabel criarLabelResumo(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(textos);
		label.setFont(new Font("Segoe UI", Font.BOLD, 15));
		return label;
	}

	private JTextArea criarAreaTexto() {
		JTextArea area = new JTextArea();
		area.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		area.setForeground(textos);
		area.setBackground(corCampo);
		area.setCaretColor(textos);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBorder(new EmptyBorder(10, 10, 10, 10));
		return area;
	}

	private JTextField criarCampoTexto() {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		campo.setForeground(textos);
		campo.setCaretColor(textos);
		campo.setBackground(corCampo);
		campo.setBorder(new LineBorder(corBorda, 1, true));
		return campo;
	}

	private JComboBox<String> criarCombo(String[] itens) {
		JComboBox<String> combo = new JComboBox<>(itens);
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		combo.setForeground(textos);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
		return combo;
	}

	private void adicionarLabel(JPanel painel, String texto, int x, int y) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		label.setBounds(x, y, 180, 25);
		painel.add(label);
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
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

	private String formatarData(LocalDate data) {
		if (data == null) {
			return "";
		}
		return FORMATO_DATA.format(data);
	}

	private String valorOuVazio(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.trim();
	}

	private void mostrarAviso(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Aviso", JOptionPane.WARNING_MESSAGE);
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}
}