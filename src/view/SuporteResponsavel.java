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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.AlunoDAO;
import dao.ChamadoDAO;
import dao.ConfiguracaoSistemaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Chamado;
import model.Usuario;
import util.SessaoUsuario;

public class SuporteResponsavel extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color textos = Color.WHITE;

	private static final int LARGURA_CARD = 1220;

	private JTable tabelaChamados;
	private DefaultTableModel modeloTabela;

	private JComboBox<String> comboAluno;
	private JComboBox<String> comboCategoria;
	private JComboBox<String> comboPrioridade;

	private JTextField campoAssunto;
	private JTextArea areaMensagem;

	private JTextField campoProtocolo;
	private JTextField campoStatus;
	private JTextField campoCategoriaDetalhes;
	private JTextField campoAssuntoDetalhes;
	private JTextArea areaDescricaoDetalhes;
	private JTextArea areaResposta;

	private JLabel lblAbertos;
	private JLabel lblRespondidos;
	private JLabel lblEmAnalise;
	private JLabel lblTotal;

	private final List<Integer> idsAlunosCombo = new ArrayList<>();

	private List<Aluno> alunosDoResponsavel = new ArrayList<>();

	public SuporteResponsavel() {

		setTitle("Suporte ao Responsável");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setResizable(false);

		setMinimumSize(new Dimension(1280, 720));

		Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int largura = area.width - 60;

		int altura = area.height - 60;

		setMaximizedBounds(area);

		setExtendedState(JFrame.MAXIMIZED_BOTH);

		JPanel externo = new JPanel(null);

		externo.setBackground(corExterna);

		externo.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(externo);

		JPanel interno = new JPanel(null);

		interno.setBackground(corInterna);

		interno.setBounds(20, 20, largura, altura);

		externo.add(interno);

		criarTela(interno, largura, altura);

		carregarDadosDaTela();
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

		conteudo.setPreferredSize(new Dimension(larguraConteudo, 2120));

		JScrollPane scroll = new JScrollPane(conteudo);

		scroll.setBounds(30, 210, largura - 60, altura - 250);

		scroll.getVerticalScrollBar().setUnitIncrement(26);

		scroll.setBorder(null);

		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		painel.add(scroll);

		criarResumo(conteudo, larguraConteudo);

		criarNovoChamado(conteudo, larguraConteudo);

		criarTabelaChamados(conteudo, larguraConteudo);

		criarDetalhesChamado(conteudo, larguraConteudo);

		criarFAQ(conteudo, larguraConteudo);

		criarContatos(conteudo, larguraConteudo);
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

				super.paintComponent(g);
			}
		};

		topo.setOpaque(false);

		JLabel titulo = new JLabel("Central de Suporte");

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));

		titulo.setBounds(40, 80, 600, 45);

		topo.add(titulo);

		JLabel subtitulo = new JLabel("Atendimento aos responsáveis");

		subtitulo.setForeground(new Color(240, 240, 255));

		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));

		subtitulo.setBounds(45, 120, 500, 25);

		topo.add(subtitulo);

		return topo;
	}

	private void criarResumo(JPanel painel, int larguraConteudo) {

		JPanel card = criarCard("Resumo");

		card.setBounds(centerX(larguraConteudo), 20, LARGURA_CARD, 150);

		lblAbertos = adicionarIndicador(card, "Abertos", 60, 50, new Color(255, 170, 90));

		lblRespondidos = adicionarIndicador(card, "Respondidos", 350, 50, new Color(120, 255, 170));

		lblEmAnalise = adicionarIndicador(card, "Em Análise", 650, 50, new Color(120, 220, 255));

		lblTotal = adicionarIndicador(card, "Total", 950, 50, new Color(255, 120, 220));

		painel.add(card);
	}

	private void criarNovoChamado(JPanel painel, int larguraConteudo) {

		JPanel card = criarCard("Abrir Chamado");

		card.setBounds(centerX(larguraConteudo), 190, LARGURA_CARD, 350);

		JLabel lblAluno = criarLabel("Aluno");

		lblAluno.setBounds(30, 60, 120, 25);

		card.add(lblAluno);

		comboAluno = new JComboBox<>();

		comboAluno.setBounds(30, 90, 450, 35);

		estilizarCombo(comboAluno);

		card.add(comboAluno);

		JLabel lblCategoria = criarLabel("Categoria");

		lblCategoria.setBounds(520, 60, 120, 25);

		card.add(lblCategoria);

		comboCategoria = new JComboBox<>(new String[] { "Financeiro", "Matrícula", "Boletim", "Frequência",
				"Advertência", "Sistema", "Outros" });

		comboCategoria.setBounds(520, 90, 250, 35);

		estilizarCombo(comboCategoria);

		card.add(comboCategoria);

		JLabel lblPrioridade = criarLabel("Prioridade");

		lblPrioridade.setBounds(800, 60, 120, 25);

		card.add(lblPrioridade);

		comboPrioridade = new JComboBox<>(new String[] { "Baixa", "Média", "Alta" });

		comboPrioridade.setBounds(800, 90, 250, 35);

		estilizarCombo(comboPrioridade);

		card.add(comboPrioridade);

		JLabel lblAssunto = criarLabel("Assunto");

		lblAssunto.setBounds(30, 145, 150, 25);

		card.add(lblAssunto);

		campoAssunto = new JTextField();

		campoAssunto.setBounds(30, 175, 740, 35);

		estilizarCampo(campoAssunto);

		card.add(campoAssunto);

		JLabel lblMensagem = criarLabel("Mensagem");

		lblMensagem.setBounds(30, 225, 150, 25);

		card.add(lblMensagem);

		areaMensagem = new JTextArea();

		areaMensagem.setLineWrap(true);

		areaMensagem.setWrapStyleWord(true);

		areaMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		areaMensagem.setBackground(corCampo);

		areaMensagem.setForeground(textos);

		areaMensagem.setCaretColor(textos);

		areaMensagem.setBorder(new EmptyBorder(8, 8, 8, 8));

		JScrollPane scrollMensagem = new JScrollPane(areaMensagem);

		scrollMensagem.setBounds(30, 255, 900, 65);

		scrollMensagem.setBorder(new LineBorder(corBorda));

		scrollMensagem.getViewport().setBackground(corCampo);

		scrollMensagem.getVerticalScrollBar().setUnitIncrement(26);

		card.add(scrollMensagem);

		JButton btnEnviar = new JButton("Enviar Chamado");

		btnEnviar.setBounds(980, 220, 200, 45);

		estilizarBotao(btnEnviar);

		btnEnviar.addActionListener(e -> enviarChamado());

		card.add(btnEnviar);

		JButton btnLimpar = new JButton("Limpar");

		btnLimpar.setBounds(980, 275, 200, 45);

		estilizarBotao(btnLimpar);

		btnLimpar.addActionListener(e -> limparFormulario());

		card.add(btnLimpar);

		painel.add(card);
	}

	private void criarTabelaChamados(JPanel painel, int larguraConteudo) {

		JPanel card = criarCard("Meus Chamados");

		card.setBounds(centerX(larguraConteudo), 560, LARGURA_CARD, 420);

		String[] colunas = { "Protocolo", "Data", "Aluno", "Categoria", "Assunto", "Status" };

		modeloTabela = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {

				return false;
			}
		};

		tabelaChamados = new JTable(modeloTabela);

		tabelaChamados.setRowHeight(30);

		tabelaChamados.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		tabelaChamados.setForeground(textos);

		tabelaChamados.setBackground(corInterna);

		tabelaChamados.setGridColor(new Color(90, 50, 170));

		tabelaChamados.setSelectionBackground(new Color(80, 40, 160));

		tabelaChamados.setSelectionForeground(textos);

		tabelaChamados.setShowGrid(true);

		tabelaChamados.setShowHorizontalLines(true);

		tabelaChamados.setShowVerticalLines(true);

		tabelaChamados.setIntercellSpacing(new Dimension(1, 1));

		tabelaChamados.setFillsViewportHeight(false);

		tabelaChamados.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabelaChamados.getTableHeader();

		header.setFont(new Font("Segoe UI", Font.BOLD, 14));

		header.setBackground(corCampo);

		header.setForeground(textos);

		header.setReorderingAllowed(false);

		header.setResizingAllowed(false);

		header.setPreferredSize(new Dimension(header.getWidth(), 34));

		DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();

		headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();

		centro.setHorizontalAlignment(SwingConstants.CENTER);

		centro.setVerticalAlignment(SwingConstants.CENTER);

		centro.setBackground(corCampo);

		centro.setForeground(textos);

		centro.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		for (int i = 0; i < tabelaChamados.getColumnCount(); i++) {

			tabelaChamados.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JScrollPane scroll = new JScrollPane(tabelaChamados);

		scroll.setBounds(25, 60, 1170, 300);

		scroll.getViewport().setBackground(corInterna);

		scroll.setBorder(new LineBorder(corBorda, 1, true));

		scroll.getVerticalScrollBar().setUnitIncrement(26);

		card.add(scroll);

		tabelaChamados.getSelectionModel().addListSelectionListener(this::selecionarChamado);

		painel.add(card);
	}

	private void criarDetalhesChamado(JPanel painel, int larguraConteudo) {

		JPanel card = criarCard("Detalhes do Chamado");

		card.setBounds(centerX(larguraConteudo), 1010, LARGURA_CARD, 420);

		JLabel lblProtocolo = criarLabel("Protocolo");

		lblProtocolo.setBounds(30, 60, 150, 25);

		card.add(lblProtocolo);

		campoProtocolo = new JTextField();

		campoProtocolo.setEditable(false);

		campoProtocolo.setBounds(30, 90, 250, 35);

		estilizarCampo(campoProtocolo);

		card.add(campoProtocolo);

		JLabel lblStatus = criarLabel("Status");

		lblStatus.setBounds(350, 60, 120, 25);

		card.add(lblStatus);

		campoStatus = new JTextField();

		campoStatus.setEditable(false);

		campoStatus.setBounds(350, 90, 220, 35);

		estilizarCampo(campoStatus);

		card.add(campoStatus);

		JLabel lblCategoria = criarLabel("Categoria");

		lblCategoria.setBounds(640, 60, 150, 25);

		card.add(lblCategoria);

		campoCategoriaDetalhes = new JTextField();

		campoCategoriaDetalhes.setEditable(false);

		campoCategoriaDetalhes.setBounds(640, 90, 250, 35);

		estilizarCampo(campoCategoriaDetalhes);

		card.add(campoCategoriaDetalhes);

		JLabel lblAssunto = criarLabel("Assunto");

		lblAssunto.setBounds(920, 60, 150, 25);

		card.add(lblAssunto);

		campoAssuntoDetalhes = new JTextField();

		campoAssuntoDetalhes.setEditable(false);

		campoAssuntoDetalhes.setBounds(920, 90, 270, 35);

		estilizarCampo(campoAssuntoDetalhes);

		card.add(campoAssuntoDetalhes);

		JLabel lblDescricao = criarLabel("Descrição do Chamado");

		lblDescricao.setBounds(30, 145, 250, 25);

		card.add(lblDescricao);

		areaDescricaoDetalhes = new JTextArea();

		areaDescricaoDetalhes.setEditable(false);

		areaDescricaoDetalhes.setLineWrap(true);

		areaDescricaoDetalhes.setWrapStyleWord(true);

		areaDescricaoDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		areaDescricaoDetalhes.setBackground(corCampo);

		areaDescricaoDetalhes.setForeground(textos);

		areaDescricaoDetalhes.setBorder(new EmptyBorder(8, 8, 8, 8));

		JScrollPane scrollDescricao = new JScrollPane(areaDescricaoDetalhes);

		scrollDescricao.setBounds(30, 175, 560, 90);

		scrollDescricao.setBorder(new LineBorder(corBorda));

		scrollDescricao.getViewport().setBackground(corCampo);

		card.add(scrollDescricao);

		JLabel lblResposta = criarLabel("Resposta da Escola");

		lblResposta.setBounds(620, 145, 250, 25);

		card.add(lblResposta);

		areaResposta = new JTextArea();

		areaResposta.setEditable(false);

		areaResposta.setLineWrap(true);

		areaResposta.setWrapStyleWord(true);

		areaResposta.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		areaResposta.setBackground(corCampo);

		areaResposta.setForeground(textos);

		areaResposta.setBorder(new EmptyBorder(8, 8, 8, 8));

		JScrollPane scrollResposta = new JScrollPane(areaResposta);

		scrollResposta.setBounds(620, 175, 570, 90);

		scrollResposta.setBorder(new LineBorder(corBorda));

		scrollResposta.getViewport().setBackground(corCampo);

		card.add(scrollResposta);

		JLabel orientacao = new JLabel("Selecione um chamado na tabela acima para visualizar seus detalhes.");

		orientacao.setForeground(new Color(190, 180, 220));

		orientacao.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		orientacao.setBounds(30, 290, 700, 25);

		card.add(orientacao);

		painel.add(card);

		limparDetalhes();
	}

	private void criarFAQ(JPanel painel, int larguraConteudo) {

		JPanel card = criarCard("Perguntas Frequentes");

		card.setBounds(centerX(larguraConteudo), 1460, LARGURA_CARD, 320);

		JTextArea areaFAQ = new JTextArea();

		areaFAQ.setEditable(false);

		areaFAQ.setFont(new Font("Segoe UI", Font.PLAIN, 20));

		areaFAQ.setLineWrap(true);

		areaFAQ.setWrapStyleWord(true);

		areaFAQ.setBackground(corCampo);

		areaFAQ.setForeground(textos);

		areaFAQ.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollFAQ = new JScrollPane(areaFAQ);

		scrollFAQ.setBounds(25, 60, 1170, 230);

		scrollFAQ.getVerticalScrollBar().setUnitIncrement(26);

		scrollFAQ.setBorder(new LineBorder(corBorda));

		card.add(scrollFAQ);

		painel.add(card);

		String faq = carregarConfiguracao("suporte.faq");

		if (faq == null || faq.isBlank()) {

			areaFAQ.setText("");
		} else {

			areaFAQ.setText(faq);
		}
	}

	private void criarContatos(JPanel painel, int larguraConteudo) {

		JPanel card = criarCard("Contatos da Escola");

		card.setBounds(centerX(larguraConteudo), 1800, LARGURA_CARD, 300);

		JTextArea areaContato = new JTextArea();

		areaContato.setEditable(false);

		areaContato.setFont(new Font("Segoe UI", Font.PLAIN, 20));

		areaContato.setLineWrap(true);

		areaContato.setWrapStyleWord(true);

		areaContato.setBackground(corCampo);

		areaContato.setForeground(textos);

		areaContato.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollContato = new JScrollPane(areaContato);

		scrollContato.setBounds(25, 60, 1170, 200);

		scrollContato.getVerticalScrollBar().setUnitIncrement(26);

		scrollContato.setBorder(new LineBorder(corBorda));

		card.add(scrollContato);

		painel.add(card);

		String contatos = carregarConfiguracao("escola.contatos");

		if (contatos == null || contatos.isBlank()) {

			areaContato.setText("");
		} else {

			areaContato.setText(contatos);
		}
	}

	private void carregarDadosDaTela() {

		carregarAlunosDoResponsavel();

		carregarChamadosDoBanco();

		carregarResumoChamados();
	}

	private void carregarAlunosDoResponsavel() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getPaiId() <= 0) {

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			alunosDoResponsavel = new AlunoDAO(conn).listarPorPais(usuario.getPaiId());

			comboAluno.removeAllItems();

			idsAlunosCombo.clear();

			for (Aluno aluno : alunosDoResponsavel) {

				comboAluno
						.addItem(valorOuTraco(aluno.getNome()) + " - Matrícula: " + valorOuTraco(aluno.getMatricula()));

				idsAlunosCombo.add(aluno.getIdAluno());
			}

			if (comboAluno.getItemCount() == 0) {

				comboAluno.addItem("Nenhum aluno vinculado");
			}

		} catch (SQLException | RuntimeException ex) {

			comboAluno.removeAllItems();

			comboAluno.addItem("Não foi possível carregar os alunos");

			idsAlunosCombo.clear();

			JOptionPane.showMessageDialog(this,
					"Não foi possível carregar os alunos vinculados ao responsável:\n" + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarChamadosDoBanco() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getPaiId() <= 0 || modeloTabela == null) {

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			List<Aluno> alunos = new AlunoDAO(conn).listarPorPais(usuario.getPaiId());

			modeloTabela.setRowCount(0);

			ChamadoDAO chamadoDAO = new ChamadoDAO(conn);

			for (Aluno aluno : alunos) {

				for (Chamado chamado : chamadoDAO.listarPorAluno(aluno.getIdAluno())) {

					String data = chamado.getDataCriacao() == null ? ""
							: chamado.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

					modeloTabela.addRow(new Object[] { chamado.getIdChamado(), data, valorOuTraco(aluno.getNome()),
							valorOuTraco(chamado.getCategoria()), valorOuTraco(chamado.getAssunto()),
							valorOuTraco(chamado.getStatus()) });
				}
			}

			limparDetalhes();

		} catch (SQLException | RuntimeException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os chamados:\n" + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarResumoChamados() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getPaiId() <= 0) {

			definirResumo(0, 0, 0, 0);

			return;
		}

		String sql = """
				SELECT
				    COUNT(*) AS total,

				    SUM(
				        CASE
				            WHEN LOWER(TRIM(c.status)) = 'aberto'
				            THEN 1
				            ELSE 0
				        END
				    ) AS abertos,

				    SUM(
				        CASE
				            WHEN c.resposta IS NOT NULL
				            AND TRIM(c.resposta) <> ''
				            THEN 1
				            ELSE 0
				        END
				    ) AS respondidos,

				    SUM(
				        CASE
				            WHEN LOWER(TRIM(c.status)) LIKE '%análise%'
				            OR LOWER(TRIM(c.status)) LIKE '%analise%'
				            THEN 1
				            ELSE 0
				        END
				    ) AS em_analise

				FROM chamado c

				INNER JOIN aluno a
				    ON a.id_aluno = c.aluno_id

				WHERE a.pais_id = ?
				""";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, usuario.getPaiId());

			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {

					int total = obterInteiroSeguro(rs, "total");

					int abertos = obterInteiroSeguro(rs, "abertos");

					int respondidos = obterInteiroSeguro(rs, "respondidos");

					int emAnalise = obterInteiroSeguro(rs, "em_analise");

					definirResumo(abertos, respondidos, emAnalise, total);

				} else {

					definirResumo(0, 0, 0, 0);
				}
			}

		} catch (SQLException ex) {

			definirResumo(0, 0, 0, 0);
		}
	}

	private void definirResumo(int abertos, int respondidos, int emAnalise, int total) {

		lblAbertos.setText(String.valueOf(abertos));

		lblRespondidos.setText(String.valueOf(respondidos));

		lblEmAnalise.setText(String.valueOf(emAnalise));

		lblTotal.setText(String.valueOf(total));
	}

	private void enviarChamado() {

		if (idsAlunosCombo.isEmpty()) {

			JOptionPane.showMessageDialog(this, "Não há aluno vinculado a este responsável.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		int indiceAluno = comboAluno.getSelectedIndex();

		if (indiceAluno < 0 || indiceAluno >= idsAlunosCombo.size()) {

			JOptionPane.showMessageDialog(this, "Selecione o aluno para o qual deseja abrir o chamado.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		String categoria = String.valueOf(comboCategoria.getSelectedItem());

		String prioridade = String.valueOf(comboPrioridade.getSelectedItem());

		String assunto = campoAssunto.getText();

		String descricao = areaMensagem.getText();

		if (assunto == null || assunto.isBlank()) {

			JOptionPane.showMessageDialog(this, "Informe o assunto do chamado.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			campoAssunto.requestFocus();

			return;
		}

		if (descricao == null || descricao.isBlank()) {

			JOptionPane.showMessageDialog(this, "Informe a mensagem do chamado.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			areaMensagem.requestFocus();

			return;
		}

		int alunoId = idsAlunosCombo.get(indiceAluno);

		try (Connection conn = ConnectionFactory.getConnection()) {

			Chamado chamado = new Chamado();

			chamado.setAlunoId(alunoId);

			chamado.setCategoria(categoria);

			chamado.setPrioridade(prioridade);

			chamado.setAssunto(assunto.trim());

			chamado.setDescricao(descricao.trim());

			new ChamadoDAO(conn).inserir(chamado);

			limparFormulario();

			carregarChamadosDoBanco();

			carregarResumoChamados();

			JOptionPane.showMessageDialog(this, "Chamado enviado com sucesso.", "Suporte",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (SQLException | RuntimeException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível enviar o chamado:\n" + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void selecionarChamado(ListSelectionEvent evento) {

		if (evento.getValueIsAdjusting()) {
			return;
		}

		int linha = tabelaChamados.getSelectedRow();

		if (linha < 0) {
			limparDetalhes();
			return;
		}

		Object protocolo = modeloTabela.getValueAt(linha, 0);

		if (protocolo == null) {
			limparDetalhes();
			return;
		}

		try {

			int idChamado = Integer.parseInt(protocolo.toString());

			carregarDetalhesChamado(idChamado);

		} catch (NumberFormatException ex) {

			limparDetalhes();
		}
	}

	private void carregarDetalhesChamado(int idChamado) {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getPaiId() <= 0) {

			limparDetalhes();
			return;
		}

		String sql = """
				SELECT
				    c.id_chamado,
				    c.categoria,
				    c.assunto,
				    c.descricao,
				    c.status,
				    c.resposta

				FROM chamado c

				INNER JOIN aluno a
				    ON a.id_aluno = c.aluno_id

				WHERE c.id_chamado = ?
				  AND a.pais_id = ?
				""";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, idChamado);

			stmt.setInt(2, usuario.getPaiId());

			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {

					campoProtocolo.setText(String.valueOf(rs.getInt("id_chamado")));

					campoCategoriaDetalhes.setText(valorOuTraco(rs.getString("categoria")));

					campoAssuntoDetalhes.setText(valorOuTraco(rs.getString("assunto")));

					campoStatus.setText(valorOuTraco(rs.getString("status")));

					areaDescricaoDetalhes.setText(valorOuTraco(rs.getString("descricao")));

					String resposta = rs.getString("resposta");

					if (resposta == null || resposta.isBlank()) {

						areaResposta.setText("");
					} else {

						areaResposta.setText(resposta);
					}

					return;
				}
			}

			limparDetalhes();

		} catch (SQLException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os detalhes do chamado:\n" + ex.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);

			limparDetalhes();
		}
	}

	private void limparDetalhes() {

		if (campoProtocolo == null) {
			return;
		}

		campoProtocolo.setText("");

		campoStatus.setText("");

		campoCategoriaDetalhes.setText("");

		campoAssuntoDetalhes.setText("");

		areaDescricaoDetalhes.setText("");

		areaResposta.setText("");
	}

	private void limparFormulario() {

		if (comboAluno.getItemCount() > 0) {
			comboAluno.setSelectedIndex(0);
		}

		comboCategoria.setSelectedIndex(0);

		comboPrioridade.setSelectedIndex(0);

		campoAssunto.setText("");

		areaMensagem.setText("");
	}

	private int obterInteiroSeguro(ResultSet rs, String coluna) throws SQLException {

		int valor = rs.getInt(coluna);

		return rs.wasNull() ? 0 : valor;
	}

	private String carregarConfiguracao(String chave) {

		try (Connection conn = ConnectionFactory.getConnection()) {

			String valor = new ConfiguracaoSistemaDAO(conn).buscar(chave);

			return valor == null ? "" : valor;

		} catch (SQLException | RuntimeException ex) {

			return "";
		}
	}

	private JPanel criarCard(String titulo) {

		JPanel painel = criarPainelArredondado();

		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(corLabel);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

		lblTitulo.setBounds(30, 15, 600, 30);

		painel.add(lblTitulo);

		return painel;
	}

	private JLabel criarLabel(String texto) {

		JLabel lbl = new JLabel(texto);

		lbl.setForeground(corLabel);

		lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

		return lbl;
	}

	private JLabel adicionarIndicador(JPanel painel, String titulo, int x, int y, Color cor) {

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(textos);

		lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		lblTitulo.setBounds(x, y, 200, 20);

		painel.add(lblTitulo);

		JLabel lblValor = new JLabel("0");

		lblValor.setForeground(cor);

		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));

		lblValor.setBounds(x, y + 20, 200, 40);

		painel.add(lblValor);

		return lblValor;
	}

	private void estilizarCampo(JTextField campo) {

		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		campo.setBackground(corCampo);

		campo.setForeground(textos);

		campo.setCaretColor(textos);

		campo.setBorder(new LineBorder(corBorda));
	}

	private void estilizarCombo(JComboBox<String> combo) {

		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		combo.setBackground(corCampo);

		combo.setForeground(textos);

		combo.setBorder(new LineBorder(corBorda));

		combo.setFocusable(false);
	}

	private void estilizarBotao(JButton botao) {

		botao.setFont(new Font("Segoe UI", Font.BOLD, 14));

		botao.setBackground(corCampo);

		botao.setForeground(textos);

		botao.setFocusPainted(false);

		botao.setBorder(new LineBorder(corBorda));

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private String valorOuTraco(String valor) {

		return valor == null || valor.isBlank() ? "-" : valor;
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