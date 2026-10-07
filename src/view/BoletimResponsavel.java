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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.AlunoDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Usuario;
import util.SessaoUsuario;

public class BoletimResponsavel extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private JComboBox<String> comboAluno;
	private final Map<String, Aluno> alunosDisponiveis = new LinkedHashMap<>();
	private JLabel lblCpf;
	private JLabel lblAlunoVinculado;
	private JLabel lblInfoAluno;
	private JLabel lblInfoTurma;
	private JLabel lblInfoSituacao;

	public BoletimResponsavel() {
		setTitle("Boletim do Aluno - Responsável");
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
		carregarAlunosDisponiveis();
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

		int larguraConteudo = 1220;
		int xInicial = ((larguraInterno - 60) - larguraConteudo) / 2;

		if (xInicial < 0) {
			xInicial = 0;
		}

		JPanel cardResponsavel = criarPainelArredondado();
		cardResponsavel.setLayout(null);
		cardResponsavel.setBounds(30 + xInicial, 215, 1220, 150);
		interno.add(cardResponsavel);

		adicionarResumo(cardResponsavel, "Responsável", "Não informado", 30, 35);
		lblCpf = adicionarResumo(cardResponsavel, "CPF", "Não informado", 330, 35);
		lblAlunoVinculado = adicionarResumo(cardResponsavel, "Aluno vinculado", "Não informado", 560, 35);
		adicionarResumo(cardResponsavel, "Ano letivo", "Não informado", 940, 35);

		JLabel aviso = new JLabel("Selecione o aluno e exporte o boletim em PDF automaticamente.");
		aviso.setForeground(new Color(120, 255, 170));
		aviso.setFont(new Font("Segoe UI", Font.BOLD, 16));
		aviso.setBounds(30, 105, 850, 25);
		cardResponsavel.add(aviso);

		JPanel cardExportar = criarCardSecao("Exportar Boletim");
		cardExportar.setBounds(30 + xInicial, 395, 600, 310);
		interno.add(cardExportar);

		JLabel lblAluno = criarLabelCampo("Aluno:");
		lblAluno.setBounds(30, 75, 200, 25);
		cardExportar.add(lblAluno);

		comboAluno = new JComboBox<>(new String[] { "Selecione" });
		comboAluno.setBounds(30, 105, 520, 42);
		estilizarCombo(comboAluno);
		cardExportar.add(comboAluno);

		JLabel lblTrimestre = criarLabelCampo("Período:");
		lblTrimestre.setBounds(30, 165, 500, 25);
		cardExportar.add(lblTrimestre);

		JLabel periodo = new JLabel("O PDF inclui os três trimestres.");
		periodo.setForeground(textos);
		periodo.setBounds(30, 195, 280, 42);
		cardExportar.add(periodo);

		JButton btnExportar = new JButton("Exportar boletim em PDF");
		btnExportar.setBounds(310, 195, 240, 42);
		estilizarBotao(btnExportar);
		btnExportar.addActionListener(e -> exportarBoletim());
		cardExportar.add(btnExportar);

		JPanel cardInfo = criarCardSecao("Informações do Boletim");
		cardInfo.setBounds(660 + xInicial, 395, 590, 310);
		interno.add(cardInfo);

		lblInfoAluno = adicionarInfo(cardInfo, "Aluno:", "Não informado", 30, 75);
		lblInfoTurma = adicionarInfo(cardInfo, "Turma:", "Não informado", 30, 125);
		adicionarInfo(cardInfo, "Curso:", "Não informado", 30, 175);
		lblInfoSituacao = adicionarInfo(cardInfo, "Situação:", "Não informado", 30, 225);

		adicionarInfo(cardInfo, "Unidade:", "Não informado", 310, 75);
		adicionarInfo(cardInfo, "Turno:", "Não informado", 310, 125);
		adicionarInfo(cardInfo, "Ano:", "Não informado", 310, 175);
		adicionarInfo(cardInfo, "Pendências:", "Não informado", 310, 225);

		comboAluno.addActionListener(e -> atualizarAlunoSelecionado());
	}

	private void exportarBoletim() {
		Aluno aluno = alunosDisponiveis.get(String.valueOf(comboAluno.getSelectedItem()));
		if (aluno == null) {
			JOptionPane.showMessageDialog(this, "Selecione um aluno válido.", "Atenção", JOptionPane.WARNING_MESSAGE);
			return;
		}
		Boletim boletim = new Boletim();
		boletim.abrirEExportarAluno(aluno.getIdAluno());
	}

	private void carregarAlunosDisponiveis() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();
		if (usuario == null) {
			mostrarErro("Nenhum usuário está autenticado.");
			return;
		}
		lblCpf.setText(usuario.getCpf() == null ? "Não informado" : usuario.getCpf());
		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			List<Aluno> alunos;
			if (usuario.getAlunoId() > 0) {
				Aluno aluno = alunoDAO.buscarPorId(usuario.getAlunoId());
				alunos = aluno == null ? List.of() : List.of(aluno);
			} else if (usuario.getPaiId() > 0) {
				alunos = alunoDAO.listarPorPais(usuario.getPaiId());
			} else {
				alunos = List.of();
			}
			for (Aluno aluno : alunos) {
				String chave = aluno.getNome() + " - " + aluno.getMatricula();
				alunosDisponiveis.put(chave, aluno);
				comboAluno.addItem(chave);
			}
			if (!alunosDisponiveis.isEmpty()) {
				comboAluno.setSelectedIndex(1);
				atualizarAlunoSelecionado();
			}
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os alunos do banco: " + ex.getMessage());
		}
	}

	private void atualizarAlunoSelecionado() {
		Aluno aluno = alunosDisponiveis.get(String.valueOf(comboAluno.getSelectedItem()));
		if (aluno == null) {
			return;
		}
		lblAlunoVinculado.setText(aluno.getNome());
		lblInfoAluno.setText(aluno.getNome());
		lblInfoTurma.setText(String.valueOf(aluno.getIdTurma()));
		lblInfoSituacao.setText(aluno.getSituacao() == null ? "Não informado" : aluno.getSituacao().name());
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
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

		JLabel titulo = new JLabel("Boletim do Aluno");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
		titulo.setBounds(40, 80, 600, 45);
		topo.add(titulo);

		JLabel sub = new JLabel("Área do responsável para consultar e exportar o boletim escolar do aluno.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		sub.setBounds(42, 120, 950, 25);
		topo.add(sub);
		return topo;
	}

	private JPanel criarCardSecao(String titulo) {
		JPanel painel = criarPainelArredondado();
		painel.setLayout(null);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitulo.setBounds(25, 18, 500, 30);
		painel.add(lblTitulo);
		return painel;
	}

	private JLabel adicionarResumo(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblTitulo.setBounds(x, y, 220, 22);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(textos);
		lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblValor.setBounds(x, y + 24, 360, 28);
		painel.add(lblValor);
		return lblValor;
	}

	private JLabel adicionarInfo(JPanel painel, String titulo, String valor, int x, int y) {
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(corLabel);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblTitulo.setBounds(x, y, 200, 22);
		painel.add(lblTitulo);

		JLabel lblValor = new JLabel(valor);
		lblValor.setForeground(textos);
		lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lblValor.setBounds(x, y + 22, 260, 24);
		painel.add(lblValor);
		return lblValor;
	}

	private JLabel criarLabelCampo(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 14));
		return label;
	}

	private void estilizarCombo(JComboBox<String> combo) {
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		combo.setForeground(textos);
		combo.setBackground(new Color(25, 8, 80));
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