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
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

import dao.ChamadoDAO;
import database.ConnectionFactory;
import model.Chamado;

public class AtendimentoSuporte extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JTable tabelaChamados;
	private DefaultTableModel modeloTabela;
	private JTextField campoAluno;
	private JFormattedTextField campoMatricula;
	private JComboBox<String> comboCategoria;
	private JComboBox<String> comboStatus;
	private JComboBox<String> comboNovoStatus;
	private JTextField campoDadosAluno;
	private JTextField campoDadosMatricula;
	private JTextField campoDadosCategoria;
	private JTextField campoDadosPrioridade;
	private JTextField campoDadosAssunto;
	private JTextArea areaDescricao;
	private JTextArea areaResposta;
	private JLabel lblAbertos;
	private JLabel lblEmAtendimento;
	private JLabel lblRespondidos;
	private JLabel lblResolvidos;
	private final List<Chamado> chamados = new ArrayList<>();
	private final List<Chamado> chamadosExibidos = new ArrayList<>();
	private Chamado chamadoSelecionado;
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public AtendimentoSuporte() {
		setTitle("Atendimento de Chamados");
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
		carregarChamadosDoBanco();
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
		painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 1370));

		JScrollPane scrollPrincipal = new JScrollPane(painelRolagem);
		scrollPrincipal.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);
		scrollPrincipal.setBorder(null);
		scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPrincipal.getVerticalScrollBar().setUnitIncrement(26);
		scrollPrincipal.getViewport().setBackground(corInterna);
		interno.add(scrollPrincipal);
		int larguraConteudo = 1220;
		int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

		if (xInicial < 0) {
			xInicial = 0;
		}

		JPanel cardResumo = criarCardSecao("Resumo dos Chamados");
		cardResumo.setBounds(xInicial + 5, 0, 1220, 180);
		painelRolagem.add(cardResumo);
		lblAbertos = adicionarIndicador(cardResumo, "Abertos", "0", 70, 70, new Color(255, 180, 90));
		lblEmAtendimento = adicionarIndicador(cardResumo, "Em Atendimento", "0", 330, 70, new Color(120, 200, 255));
		lblRespondidos = adicionarIndicador(cardResumo, "Respondidos", "0", 650, 70, new Color(255, 220, 120));
		lblResolvidos = adicionarIndicador(cardResumo, "Resolvidos", "0", 950, 70, new Color(120, 255, 170));

		JPanel cardFiltro = criarCardSecao("Filtros de Pesquisa");
		cardFiltro.setBounds(xInicial + 5, 200, 1220, 170);
		painelRolagem.add(cardFiltro);

		JLabel lblAluno = criarLabelCampo("Aluno:");
		lblAluno.setBounds(30, 70, 150, 22);
		cardFiltro.add(lblAluno);
		campoAluno = new JTextField();
		campoAluno.setBounds(30, 95, 260, 38);
		estilizarCampo(campoAluno);
		cardFiltro.add(campoAluno);

		JLabel lblMatricula = criarLabelCampo("Matrícula:");
		lblMatricula.setBounds(315, 70, 150, 22);
		cardFiltro.add(lblMatricula);
		campoMatricula = criarCampoMascara("##########");
		campoMatricula.setBounds(315, 95, 180, 38);
		cardFiltro.add(campoMatricula);

		JLabel lblCategoria = criarLabelCampo("Categoria:");
		lblCategoria.setBounds(520, 70, 150, 22);
		cardFiltro.add(lblCategoria);
		comboCategoria = new JComboBox<>(new String[] { "Todas", "Acesso ao Sistema", "Notas", "Faltas", "Boletim", "Cadastro", "Outro" });
		comboCategoria.setBounds(520, 95, 220, 38);
		estilizarCombo(comboCategoria);
		cardFiltro.add(comboCategoria);

		JLabel lblStatus = criarLabelCampo("Status:");
		lblStatus.setBounds(765, 70, 150, 22);
		cardFiltro.add(lblStatus);
		comboStatus = new JComboBox<>(new String[] { "Todos", "Aberto", "Em Atendimento", "Respondido", "Resolvido" });
		comboStatus.setBounds(765, 95, 180, 38);
		estilizarCombo(comboStatus);
		cardFiltro.add(comboStatus);

		JButton btnPesquisar = new JButton("Pesquisar");
		btnPesquisar.setBounds(970, 95, 110, 38);
		estilizarBotao(btnPesquisar);
		btnPesquisar.addActionListener(e -> atualizarTabela());
		cardFiltro.add(btnPesquisar);

		JButton btnLimpar = new JButton("Limpar");
		btnLimpar.setBounds(1095, 95, 95, 38);
		estilizarBotao(btnLimpar);
		btnLimpar.addActionListener(e -> limparFiltros());
		cardFiltro.add(btnLimpar);

		JPanel cardTabela = criarCardSecao("Chamados dos Alunos");
		cardTabela.setBounds(xInicial + 5, 390, 1220, 350);
		painelRolagem.add(cardTabela);
		criarTabela();

		JScrollPane scrollTabela = new JScrollPane(tabelaChamados);
		scrollTabela.setBounds(30, 70, 1160, 250);
		scrollTabela.setBorder(new LineBorder(corBorda));
		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
		scrollTabela.getViewport().setBackground(corCampo);
		cardTabela.add(scrollTabela);

		JPanel cardDados = criarCardSecao("Dados do Chamado");
		cardDados.setBounds(xInicial + 5, 775, 580, 520);
		painelRolagem.add(cardDados);
		campoDadosAluno = adicionarInfo(cardDados, "Aluno:", "", 30, 70);
		campoDadosMatricula = adicionarInfo(cardDados, "Matrícula:", "", 30, 130);
		campoDadosCategoria = adicionarInfo(cardDados, "Categoria:", "", 30, 190);
		campoDadosPrioridade = adicionarInfo(cardDados, "Prioridade:", "", 30, 250);
		campoDadosAssunto = adicionarInfo(cardDados, "Assunto:", "", 30, 310);

		JLabel lblDescricao = criarLabelCampo("Descrição:");
		lblDescricao.setBounds(30, 370, 200, 25);
		cardDados.add(lblDescricao);
		areaDescricao = new JTextArea();
		areaDescricao.setEditable(false);
		areaDescricao.setLineWrap(true);
		areaDescricao.setWrapStyleWord(true);
		areaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		areaDescricao.setForeground(textos);
		areaDescricao.setBackground(corCampo);
		areaDescricao.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollDescricao = new JScrollPane(areaDescricao);
		scrollDescricao.setBounds(30, 400, 520, 90);
		scrollDescricao.setBorder(new LineBorder(corBorda));
		scrollDescricao.getVerticalScrollBar().setUnitIncrement(26);
		cardDados.add(scrollDescricao);

		JPanel cardAtendimento = criarCardSecao("Atendimento");
		cardAtendimento.setBounds(xInicial + 645, 775, 580, 520);
		painelRolagem.add(cardAtendimento);

		JLabel lblResposta = criarLabelCampo("Resposta ao aluno:");
		lblResposta.setBounds(30, 70, 250, 25);
		cardAtendimento.add(lblResposta);
		areaResposta = new JTextArea();
		areaResposta.setLineWrap(true);
		areaResposta.setWrapStyleWord(true);
		areaResposta.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		areaResposta.setForeground(textos);
		areaResposta.setBackground(corCampo);
		areaResposta.setCaretColor(textos);
		areaResposta.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollResposta = new JScrollPane(areaResposta);
		scrollResposta.setBounds(30, 100, 520, 220);
		scrollResposta.getVerticalScrollBar().setUnitIncrement(26);
		scrollResposta.setBorder(new LineBorder(corBorda));
		cardAtendimento.add(scrollResposta);

		JLabel lblNovoStatus = criarLabelCampo("Novo Status:");
		lblNovoStatus.setBounds(30, 345, 200, 22);
		cardAtendimento.add(lblNovoStatus);
		comboNovoStatus = new JComboBox<>(new String[] { "Aberto", "Em Atendimento", "Respondido", "Resolvido" });
		comboNovoStatus.setBounds(30, 370, 250, 38);
		estilizarCombo(comboNovoStatus);
		cardAtendimento.add(comboNovoStatus);

		JButton btnResponder = new JButton("Responder");
		btnResponder.setBounds(30, 450, 150, 42);
		estilizarBotao(btnResponder);
		btnResponder.addActionListener(e -> salvarAtendimento());
		cardAtendimento.add(btnResponder);

		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.setBounds(200, 450, 150, 42);
		estilizarBotao(btnSalvar);
		btnSalvar.addActionListener(e -> salvarAtendimento());
		cardAtendimento.add(btnSalvar);

		JButton btnResolver = new JButton("Resolver Chamado");
		btnResolver.setBounds(370, 450, 180, 42);
		estilizarBotao(btnResolver);
		btnResolver.addActionListener(e -> {
			comboNovoStatus.setSelectedItem("Resolvido");
			salvarAtendimento();
		});
		cardAtendimento.add(btnResolver);
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Atendimento de Chamados");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 80, 700, 45);
		topo.add(titulo);

		JLabel sub = new JLabel("Gerencie solicitações, responda alunos e acompanhe atendimentos.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(47, 120, 900, 25);
		topo.add(sub);
		return topo;
	}

	private void criarTabela() {
		String[] colunas = { "ID", "Data", "Aluno", "Matrícula", "Categoria", "Assunto", "Prioridade", "Status" };
		modeloTabela = new DefaultTableModel(colunas, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tabelaChamados = new JTable(modeloTabela);
		tabelaChamados.getSelectionModel().addListSelectionListener(e -> {

			if (!e.getValueIsAdjusting()) {
				carregarChamadoSelecionado();
			}
		});
		tabelaChamados.setRowHeight(42);
		tabelaChamados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		tabelaChamados.setForeground(textos);
		tabelaChamados.setBackground(corCampo);
		tabelaChamados.setGridColor(corBorda);

		JTableHeader header = tabelaChamados.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 13));
		header.setForeground(textos);
		header.setBackground(new Color(80, 25, 150));
		header.setPreferredSize(new Dimension(header.getWidth(), 50));
		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
		centro.setHorizontalAlignment(JLabel.CENTER);
		centro.setForeground(textos);
		centro.setBackground(corCampo);

		for (int i = 0; i < tabelaChamados.getColumnCount(); i++) {
			tabelaChamados.getColumnModel().getColumn(i).setCellRenderer(centro);
		}
	}

	private void carregarChamadosDoBanco() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			chamados.clear();
			chamados.addAll(new ChamadoDAO(conn).listar());
			atualizarTabela();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os chamados do banco: " + ex.getMessage());
		}
	}

	private void atualizarTabela() {
		String alunoFiltro = campoAluno.getText().trim().toLowerCase();
		String matriculaFiltro = campoMatricula.getText().trim();
		String categoriaFiltro = String.valueOf(comboCategoria.getSelectedItem());
		String statusFiltro = String.valueOf(comboStatus.getSelectedItem());
		chamadosExibidos.clear();
		modeloTabela.setRowCount(0);

		for (Chamado chamado : chamados) {
			if ((!alunoFiltro.isEmpty() && !chamado.getAlunoNome().toLowerCase().contains(alunoFiltro))
					|| (!matriculaFiltro.isEmpty() && !chamado.getMatricula().contains(matriculaFiltro))
					|| (!"Todas".equals(categoriaFiltro) && !categoriaFiltro.equals(chamado.getCategoria()))
					|| (!"Todos".equals(statusFiltro) && !statusFiltro.equals(chamado.getStatus()))) {
				continue;
			}
			chamadosExibidos.add(chamado);
			modeloTabela.addRow(new Object[] { chamado.getIdChamado(), data(chamado), chamado.getAlunoNome(), chamado.getMatricula(), chamado.getCategoria(), chamado.getAssunto(), chamado.getPrioridade(),
					chamado.getStatus() });
		}
		atualizarResumo();
		limparDetalhes();
	}

	private void carregarChamadoSelecionado() {
		int linha = tabelaChamados.getSelectedRow();

		if (linha < 0 || linha >= chamadosExibidos.size()) {
			return;
		}
		chamadoSelecionado = chamadosExibidos.get(linha);
		campoDadosAluno.setText(chamadoSelecionado.getAlunoNome());
		campoDadosMatricula.setText(chamadoSelecionado.getMatricula());
		campoDadosCategoria.setText(chamadoSelecionado.getCategoria());
		campoDadosPrioridade.setText(chamadoSelecionado.getPrioridade());
		campoDadosAssunto.setText(chamadoSelecionado.getAssunto());
		areaDescricao.setText(chamadoSelecionado.getDescricao());
		areaResposta.setText(chamadoSelecionado.getResposta() == null ? "" : chamadoSelecionado.getResposta());
		comboNovoStatus.setSelectedItem(chamadoSelecionado.getStatus());
	}

	private void salvarAtendimento() {
		if (chamadoSelecionado == null) {
			mostrarErro("Selecione um chamado para atender.");
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			new ChamadoDAO(conn).atualizarAtendimento(chamadoSelecionado.getIdChamado(), areaResposta.getText().trim(),
					String.valueOf(comboNovoStatus.getSelectedItem()));
			JOptionPane.showMessageDialog(this, "Atendimento salvo com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			carregarChamadosDoBanco();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível salvar o atendimento: " + ex.getMessage());
		}
	}

	private void limparFiltros() {
		campoAluno.setText("");
		campoMatricula.setValue(null);
		comboCategoria.setSelectedIndex(0);
		comboStatus.setSelectedIndex(0);
		atualizarTabela();
	}

	private void atualizarResumo() {
		lblAbertos.setText(String.valueOf(contarStatus("Aberto")));
		lblEmAtendimento.setText(String.valueOf(contarStatus("Em Atendimento")));
		lblRespondidos.setText(String.valueOf(contarStatus("Respondido")));
		lblResolvidos.setText(String.valueOf(contarStatus("Resolvido")));
	}

	private long contarStatus(String status) {
		return chamados.stream().filter(chamado -> status.equalsIgnoreCase(chamado.getStatus())).count();
	}

	private String data(Chamado chamado) {
		return chamado.getDataCriacao() == null ? "Não informado" : chamado.getDataCriacao().format(FORMATO_DATA);
	}

	private void limparDetalhes() {
		chamadoSelecionado = null;
		campoDadosAluno.setText("");
		campoDadosMatricula.setText("");
		campoDadosCategoria.setText("");
		campoDadosPrioridade.setText("");
		campoDadosAssunto.setText("");
		areaDescricao.setText("");
		areaResposta.setText("");
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private JPanel criarCardSecao(String titulo) {
		JPanel painel = criarPainelArredondado();
		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitulo.setBounds(30, 18, 600, 30);
		painel.add(lblTitulo);
		return painel;
	}

	private JTextField adicionarInfo(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblTitulo.setBounds(x, y, 200, 22);
		painel.add(lblTitulo);

		JTextField lblValor = new JTextField(valor);
		lblValor.setEditable(false);
		estilizarCampo(lblValor);
		lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lblValor.setBounds(x, y + 22, 500, 28);
		painel.add(lblValor);
		return lblValor;
	}

	private JLabel adicionarIndicador(JPanel painel, String titulo, String valor, int x, int y, Color corValor) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(textos);
		lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		lblTitulo.setBounds(x, y, 180, 22);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(corValor);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 34));
		lblValor.setBounds(x, y + 28, 120, 40);
		painel.add(lblValor);
		return lblValor;
	}

	private JLabel criarLabelCampo(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		return label;
	}

	private void estilizarCampo(JTextField campo) {
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		campo.setForeground(textos);
		campo.setBackground(corCampo);
		campo.setCaretColor(textos);
		campo.setBorder(new LineBorder(corBorda));
	}

	private void estilizarCombo(JComboBox<String> combo) {
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		combo.setForeground(textos);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda));
		combo.setFocusable(false);
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private JFormattedTextField criarCampoMascara(String mascara) {
		try {
			MaskFormatter formatter = new MaskFormatter(mascara);
			formatter.setPlaceholderCharacter(' ');
			JFormattedTextField campo = new JFormattedTextField(formatter);
			campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			campo.setForeground(textos);
			campo.setBackground(corCampo);
			campo.setCaretColor(textos);
			campo.setBorder(new LineBorder(corBorda));
			campo.addFocusListener(new java.awt.event.FocusAdapter() {
				@Override
				public void focusGained(java.awt.event.FocusEvent e) {
					javax.swing.SwingUtilities.invokeLater(() -> {
						campo.setCaretPosition(0);
					});
				}
			});
			campo.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (campo.getText().trim().isEmpty()) {
						javax.swing.SwingUtilities.invokeLater(() -> campo.setCaretPosition(0));
					}
				}
			});
			return campo;
		} catch (ParseException e) {
			throw new IllegalArgumentException("Máscara inválida: " + mascara, e);
		}
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