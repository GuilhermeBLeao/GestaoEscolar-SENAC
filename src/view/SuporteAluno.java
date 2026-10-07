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

import dao.AlunoDAO;
import dao.ChamadoDAO;
import dao.ConfiguracaoSistemaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Chamado;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class SuporteAluno extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	private JTable tabelaChamados;
	private DefaultTableModel modeloTabela;

	private JLabel lblChamadosAbertos;
	private JLabel lblChamadosRespondidos;
	private JLabel lblChamadosResolvidos;

	private JLabel lblAluno;
	private JLabel lblMatricula;
	private JLabel lblTurma;
	private JLabel lblCurso;
	private JLabel lblTurno;

	private JLabel lblHorarioSecretaria;
	private JLabel lblEmailSecretaria;
	private JLabel lblTelefoneSecretaria;

	private JComboBox<String> comboCategoria;
	private JComboBox<String> comboPrioridade;
	private JTextField campoAssunto;
	private JTextArea areaDescricao;

	public SuporteAluno() {

		setTitle("Suporte - Aluno");

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

		carregarDadosDaTela();
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

		painelRolagem.setPreferredSize(new Dimension(larguraInterno - 80, 960));

		JScrollPane scroll = new JScrollPane(painelRolagem);

		scroll.setBounds(30, 210, larguraInterno - 60, alturaInterno - 240);

		scroll.setBorder(null);

		scroll.getVerticalScrollBar().setUnitIncrement(26);

		scroll.getViewport().setBackground(corInterna);

		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		interno.add(scroll);

		JPanel cardAluno = criarPainelArredondado();

		cardAluno.setLayout(null);

		cardAluno.setBounds(xInicial + 5, 0, 1220, 135);

		painelRolagem.add(cardAluno);

		criarResumoAluno(cardAluno);

		JLabel aviso = new JLabel(
				"Use esta área para pedir ajuda sobre acesso, notas, faltas, boletim, cadastro ou dúvidas gerais.");

		aviso.setForeground(new Color(120, 255, 170));

		aviso.setFont(new Font("Segoe UI", Font.BOLD, 16));

		aviso.setBounds(35, 95, 900, 25);

		cardAluno.add(aviso);

		JPanel cardCanais = criarCardSecao("Canais de Atendimento");

		cardCanais.setBounds(xInicial + 5, 165, 390, 250);

		painelRolagem.add(cardCanais);

		criarCanaisAtendimento(cardCanais);

		JPanel cardOrientacoes = criarCardSecao("Orientações");

		cardOrientacoes.setBounds(xInicial + 420, 165, 390, 250);

		painelRolagem.add(cardOrientacoes);

		adicionarInfo(cardOrientacoes, "Acesso ao sistema:", "Informe erro, tela e horário", 25, 65);

		adicionarInfo(cardOrientacoes, "Notas ou faltas:", "Informe disciplina e professor", 25, 115);

		adicionarInfo(cardOrientacoes, "Cadastro:", "Informe o dado incorreto", 25, 165);

		JPanel cardStatus = criarCardSecao("Resumo de Chamados");

		cardStatus.setBounds(xInicial + 835, 165, 390, 250);

		painelRolagem.add(cardStatus);

		criarResumoChamados(cardStatus);

		JPanel cardFormulario = criarCardSecao("Abrir Solicitação de Suporte");

		cardFormulario.setBounds(xInicial + 5, 445, 600, 430);

		painelRolagem.add(cardFormulario);

		criarFormulario(cardFormulario);

		JPanel cardHistorico = criarCardSecao("Histórico de Solicitações");

		cardHistorico.setBounds(xInicial + 635, 445, 590, 430);

		painelRolagem.add(cardHistorico);

		criarTabela();

		JScrollPane scrollTabela = new JScrollPane(tabelaChamados);

		scrollTabela.setBounds(30, 70, 540, 330);

		scrollTabela.setBorder(new LineBorder(corBorda));

		scrollTabela.getViewport().setBackground(corCampo);

		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);

		cardHistorico.add(scrollTabela);
	}

	private void criarResumoAluno(JPanel cardAluno) {

		lblAluno = adicionarResumoComLabel(cardAluno, "Aluno", 30, 30);

		lblMatricula = adicionarResumoComLabel(cardAluno, "Matrícula", 300, 30);

		lblTurma = adicionarResumoComLabel(cardAluno, "Turma", 520, 30);

		lblCurso = adicionarResumoComLabel(cardAluno, "Curso", 680, 30);

		lblTurno = adicionarResumoComLabel(cardAluno, "Turno", 930, 30);
	}

	private JLabel adicionarResumoComLabel(JPanel painel, String titulo, int x, int y) {

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(corLabel);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

		lblTitulo.setBounds(x + 5, y, 200, 22);

		painel.add(lblTitulo);

		JLabel lblValor = new JLabel("-");

		lblValor.setForeground(textos);

		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));

		lblValor.setBounds(x, y + 24, 260, 28);

		painel.add(lblValor);

		return lblValor;
	}

	private void criarCanaisAtendimento(JPanel painel) {

		lblHorarioSecretaria = adicionarInfoComLabel(painel, "Secretaria:", 25, 65);

		lblEmailSecretaria = adicionarInfoComLabel(painel, "E-mail:", 25, 115);

		lblTelefoneSecretaria = adicionarInfoComLabel(painel, "Telefone:", 25, 165);
	}

	private JLabel adicionarInfoComLabel(JPanel painel, String titulo, int x, int y) {

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(corLabel);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

		lblTitulo.setBounds(x + 5, y, 210, 22);

		painel.add(lblTitulo);

		JLabel lblValor = new JLabel("-");

		lblValor.setForeground(textos);

		lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		lblValor.setBounds(x + 5, y + 22, 330, 24);

		painel.add(lblValor);

		return lblValor;
	}

	private void criarResumoChamados(JPanel painel) {

		lblChamadosAbertos = adicionarIndicador(painel, "Abertos", 35, 75, new Color(255, 170, 90));

		lblChamadosRespondidos = adicionarIndicador(painel, "Respondidos", 150, 75, new Color(120, 200, 255));

		lblChamadosResolvidos = adicionarIndicador(painel, "Resolvidos", 290, 75, new Color(120, 255, 170));

		JLabel statusTexto = new JLabel("<html>Tempo médio de resposta:<br>" + "<b>consulte a secretaria</b></html>");

		statusTexto.setForeground(textos);

		statusTexto.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		statusTexto.setBounds(40, 155, 300, 60);

		painel.add(statusTexto);
	}

	private JLabel adicionarIndicador(JPanel painel, String titulo, int x, int y, Color corValor) {

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(textos);

		lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		lblTitulo.setBounds(x + 5, y, 120, 22);

		painel.add(lblTitulo);

		JLabel lblValor = new JLabel("0");

		lblValor.setForeground(corValor);

		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 30));

		lblValor.setBounds(x + 5, y + 28, 80, 35);

		painel.add(lblValor);

		return lblValor;
	}

	private void criarFormulario(JPanel cardFormulario) {

		JLabel lblCategoria = criarLabelCampo("Categoria:");

		lblCategoria.setBounds(30, 70, 200, 22);

		cardFormulario.add(lblCategoria);

		comboCategoria = new JComboBox<>(new String[] { "Selecione", "Acesso ao sistema", "Notas", "Faltas", "Boletim",
				"Dados cadastrais", "Calendário", "Outro" });

		comboCategoria.setBounds(30, 95, 250, 38);

		estilizarCombo(comboCategoria);

		cardFormulario.add(comboCategoria);

		JLabel lblPrioridade = criarLabelCampo("Prioridade:");

		lblPrioridade.setBounds(315, 70, 200, 22);

		cardFormulario.add(lblPrioridade);

		comboPrioridade = new JComboBox<>(new String[] { "Baixa", "Média", "Alta" });

		comboPrioridade.setBounds(315, 95, 250, 38);

		estilizarCombo(comboPrioridade);

		cardFormulario.add(comboPrioridade);

		JLabel lblAssunto = criarLabelCampo("Assunto:");

		lblAssunto.setBounds(30, 150, 200, 22);

		cardFormulario.add(lblAssunto);

		campoAssunto = new JTextField();

		campoAssunto.setBounds(30, 175, 535, 38);

		estilizarCampo(campoAssunto);

		cardFormulario.add(campoAssunto);

		JLabel lblDescricao = criarLabelCampo("Descrição:");

		lblDescricao.setBounds(30, 230, 200, 22);

		cardFormulario.add(lblDescricao);

		areaDescricao = new JTextArea();

		areaDescricao.setLineWrap(true);

		areaDescricao.setWrapStyleWord(true);

		areaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		areaDescricao.setForeground(textos);

		areaDescricao.setBackground(corCampo);

		areaDescricao.setCaretColor(textos);

		areaDescricao.setBorder(new EmptyBorder(10, 10, 10, 10));

		JScrollPane scrollDescricao = new JScrollPane(areaDescricao);

		scrollDescricao.setBounds(30, 255, 535, 95);

		scrollDescricao.setBorder(new LineBorder(corBorda));

		scrollDescricao.getViewport().setBackground(corCampo);

		scrollDescricao.getVerticalScrollBar().setUnitIncrement(26);

		cardFormulario.add(scrollDescricao);

		JButton btnEnviar = new JButton("Enviar solicitação");

		btnEnviar.setBounds(30, 370, 220, 42);

		estilizarBotao(btnEnviar);

		btnEnviar.addActionListener(e -> enviarChamado());

		cardFormulario.add(btnEnviar);

		JButton btnLimpar = new JButton("Limpar campos");

		btnLimpar.setBounds(265, 370, 180, 42);

		estilizarBotao(btnLimpar);

		btnLimpar.addActionListener(e -> limparFormulario());

		cardFormulario.add(btnLimpar);
	}

	private void carregarDadosDaTela() {

		carregarResumoAluno();

		carregarConfiguracoes();

		carregarChamadosDoBanco();

		carregarResumoChamados();
	}

	private void carregarResumoAluno() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getAlunoId() <= 0) {

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			Aluno aluno = new AlunoDAO(conn).buscarPorId(usuario.getAlunoId());

			if (aluno == null) {
				return;
			}

			Turma turma = null;

			if (aluno.getIdTurma() > 0) {

				turma = new TurmaDAO(conn).buscarPorId(aluno.getIdTurma());
			}

			lblAluno.setText(valorOuTraco(aluno.getNome()));

			lblMatricula.setText(valorOuTraco(aluno.getMatricula()));

			lblTurma.setText(turma == null ? "-" : valorOuTraco(turma.getDescricaoTurma()));

			/*
			 * A estrutura de Aluno/Turma fornecida para este projeto não possui um campo de
			 * curso.
			 *
			 * Para não inventar informação, o sistema exibe "-".
			 */
			lblCurso.setText("-");

			lblTurno.setText(turma == null || turma.getTurno() == null ? "-" : turma.getTurno().name());

		} catch (SQLException | RuntimeException ex) {

			lblAluno.setText("-");
			lblMatricula.setText("-");
			lblTurma.setText("-");
			lblCurso.setText("-");
			lblTurno.setText("-");
		}
	}

	private void carregarConfiguracoes() {

		lblHorarioSecretaria.setText(valorConfiguracao("escola.horario"));

		lblEmailSecretaria.setText(valorConfiguracao("escola.email"));

		lblTelefoneSecretaria.setText(valorConfiguracao("escola.telefone"));
	}

	private String valorConfiguracao(String chave) {

		String valor = carregarConfiguracao(chave);

		return valor == null || valor.isBlank() ? "-" : valor;
	}

	private String carregarConfiguracao(String chave) {

		try (Connection conn = ConnectionFactory.getConnection()) {

			String valor = new ConfiguracaoSistemaDAO(conn).buscar(chave);

			return valor == null ? "" : valor;

		} catch (SQLException | RuntimeException ex) {

			return "";
		}
	}

	private void carregarChamadosDoBanco() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getAlunoId() <= 0 || modeloTabela == null) {

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			modeloTabela.setRowCount(0);

			ChamadoDAO chamadoDAO = new ChamadoDAO(conn);

			for (Chamado chamado : chamadoDAO.listarPorAluno(usuario.getAlunoId())) {

				String data = chamado.getDataCriacao() == null ? ""
						: chamado.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

				modeloTabela.addRow(
						new Object[] { data, chamado.getCategoria(), chamado.getAssunto(), chamado.getStatus() });
			}

		} catch (SQLException | RuntimeException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível carregar os chamados:\n" + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarResumoChamados() {

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getAlunoId() <= 0) {

			lblChamadosAbertos.setText("0");
			lblChamadosRespondidos.setText("0");
			lblChamadosResolvidos.setText("0");

			return;
		}

		String sql = """
				SELECT
				    SUM(
				        CASE
				            WHEN LOWER(TRIM(status)) = 'aberto'
				            THEN 1
				            ELSE 0
				        END
				    ) AS abertos,

				    SUM(
				        CASE
				            WHEN resposta IS NOT NULL
				            AND TRIM(resposta) <> ''
				            THEN 1
				            ELSE 0
				        END
				    ) AS respondidos,

				    SUM(
				        CASE
				            WHEN LOWER(TRIM(status)) LIKE '%resolvido%'
				            OR LOWER(TRIM(status)) LIKE '%fechado%'
				            THEN 1
				            ELSE 0
				        END
				    ) AS resolvidos

				FROM chamado
				WHERE aluno_id = ?
				""";

		try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, usuario.getAlunoId());

			try (ResultSet rs = stmt.executeQuery()) {

				if (rs.next()) {

					lblChamadosAbertos.setText(String.valueOf(obterInteiroSeguro(rs, "abertos")));

					lblChamadosRespondidos.setText(String.valueOf(obterInteiroSeguro(rs, "respondidos")));

					lblChamadosResolvidos.setText(String.valueOf(obterInteiroSeguro(rs, "resolvidos")));
				}
			}

		} catch (SQLException ex) {

			lblChamadosAbertos.setText("0");
			lblChamadosRespondidos.setText("0");
			lblChamadosResolvidos.setText("0");
		}
	}

	private int obterInteiroSeguro(ResultSet rs, String coluna) throws SQLException {

		int valor = rs.getInt(coluna);

		return rs.wasNull() ? 0 : valor;
	}

	private void enviarChamado() {

		String categoria = String.valueOf(comboCategoria.getSelectedItem());

		String prioridade = String.valueOf(comboPrioridade.getSelectedItem());

		String assunto = campoAssunto.getText();

		String descricao = areaDescricao.getText();

		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null || usuario.getAlunoId() <= 0) {

			JOptionPane.showMessageDialog(this, "Não foi possível identificar o aluno logado.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		if ("Selecione".equals(categoria)) {

			JOptionPane.showMessageDialog(this, "Selecione uma categoria.", "Atenção", JOptionPane.WARNING_MESSAGE);

			comboCategoria.requestFocus();

			return;
		}

		if (assunto == null || assunto.isBlank()) {

			JOptionPane.showMessageDialog(this, "Informe o assunto da solicitação.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			campoAssunto.requestFocus();

			return;
		}

		if (descricao == null || descricao.isBlank()) {

			JOptionPane.showMessageDialog(this, "Informe a descrição da solicitação.", "Atenção",
					JOptionPane.WARNING_MESSAGE);

			areaDescricao.requestFocus();

			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			Chamado chamado = new Chamado();

			chamado.setAlunoId(usuario.getAlunoId());

			chamado.setCategoria(categoria);

			chamado.setPrioridade(prioridade);

			chamado.setAssunto(assunto.trim());

			chamado.setDescricao(descricao.trim());

			new ChamadoDAO(conn).inserir(chamado);

			limparFormulario();

			carregarChamadosDoBanco();

			carregarResumoChamados();

			JOptionPane.showMessageDialog(this, "Solicitação enviada com sucesso.", "Suporte",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (SQLException | RuntimeException ex) {

			JOptionPane.showMessageDialog(this, "Não foi possível enviar a solicitação:\n" + ex.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void limparFormulario() {

		comboCategoria.setSelectedIndex(0);

		comboPrioridade.setSelectedIndex(0);

		campoAssunto.setText("");

		areaDescricao.setText("");
	}

	private String valorOuTraco(String valor) {

		return valor == null || valor.isBlank() ? "-" : valor;
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

		JLabel titulo = new JLabel("Suporte ao Aluno");

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));

		titulo.setBounds(40, 80, 600, 45);

		topo.add(titulo);

		JLabel sub = new JLabel("Abra solicitações, acompanhe respostas e tire dúvidas com a secretaria.");

		sub.setForeground(new Color(245, 225, 255));

		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));

		sub.setBounds(47, 120, 900, 25);

		topo.add(sub);

		return topo;
	}

	private void criarTabela() {

		String[] colunas = { "Data", "Categoria", "Assunto", "Status" };

		modeloTabela = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {

				return false;
			}
		};

		tabelaChamados = new JTable(modeloTabela);

		tabelaChamados.setRowHeight(38);

		tabelaChamados.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		tabelaChamados.setForeground(textos);

		tabelaChamados.setBackground(corCampo);

		tabelaChamados.setGridColor(corBorda);

		tabelaChamados.setSelectionBackground(new Color(120, 40, 220));

		tabelaChamados.setSelectionForeground(textos);

		tabelaChamados.setShowGrid(true);

		tabelaChamados.setFillsViewportHeight(true);

		JTableHeader header = tabelaChamados.getTableHeader();

		header.setFont(new Font("Segoe UI", Font.BOLD, 14));

		header.setForeground(textos);

		header.setBackground(new Color(80, 25, 150));

		header.setPreferredSize(new Dimension(header.getWidth(), 38));

		header.setReorderingAllowed(false);

		header.setResizingAllowed(false);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();

		centro.setHorizontalAlignment(JLabel.CENTER);

		centro.setVerticalAlignment(JLabel.CENTER);

		centro.setForeground(textos);

		centro.setBackground(corCampo);

		for (int i = 0; i < tabelaChamados.getColumnCount(); i++) {

			tabelaChamados.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		tabelaChamados.getColumnModel().getColumn(0).setPreferredWidth(90);

		tabelaChamados.getColumnModel().getColumn(1).setPreferredWidth(100);

		tabelaChamados.getColumnModel().getColumn(2).setPreferredWidth(210);

		tabelaChamados.getColumnModel().getColumn(3).setPreferredWidth(100);
	}

	private JPanel criarCardSecao(String titulo) {

		JPanel painel = criarPainelArredondado();

		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(corLabel);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

		lblTitulo.setBounds(30, 18, 500, 30);

		painel.add(lblTitulo);

		return painel;
	}

	private void adicionarInfo(JPanel painel, String titulo, String valor, int x, int y) {

		JLabel lblTitulo = new JLabel(titulo);

		lblTitulo.setForeground(corLabel);

		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

		lblTitulo.setBounds(x + 5, y, 210, 22);

		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);

		lblValor.setForeground(textos);

		lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		lblValor.setBounds(x + 5, y + 22, 330, 24);

		painel.add(lblValor);
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