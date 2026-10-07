package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import controller.AlunoController;
import dao.AlunoDAO;
import dao.PaisAlunoDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.PaisAluno;
import model.Turma;

public class AlunosSecretaria extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	private JTable tabela;
	private DefaultTableModel modeloTabela;
	private JTextField campoNomePesquisa;
	private JTextField campoMatriculaPesquisa;
	private JLabel lblTotal;
	private JLabel lblAtivos;
	private JLabel lblInativos;
	private JLabel lblTransferidos;
	private JTextField campoNome;
	private JTextField campoCpf;
	private JTextField campoRg;
	private JTextField campoNascimento;
	private JTextField campoTelefone;
	private JTextField campoEmail;
	private JTextField campoResponsavel;
	private JTextField campoCpfResponsavel;
	private JTextField campoTelefoneResponsavel;
	private JTextField campoParentesco;
	private final List<Aluno> alunos = new ArrayList<>();
	private final List<Aluno> alunosExibidos = new ArrayList<>();
	private final Map<Integer, Turma> turmasPorId = new HashMap<>();
	private Aluno alunoSelecionado;
	private JButton btnSalvar;
	private JButton btnInativar;

	public AlunosSecretaria() {
		setTitle("Alunos - Secretaria");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(false);

		Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int largura = area.width - 60;
		int altura = area.height - 60;

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		setContentPane(externo);

		JPanel interno = new JPanel(null);
		interno.setBounds(20, 20, largura, altura);
		interno.setBackground(corInterna);
		externo.add(interno);

		criarResumo(interno);
		criarPesquisa(interno);
		criarTabela(interno);
		criarCadastroAluno(interno);
		criarResponsavel(interno);

		JPanel topo = criarTopo();
		topo.setBounds(30, 25, largura - 60, 160);
		interno.add(topo);

		JButton btnVoltar = new JButton("← Voltar");
		btnVoltar.setBounds(35, 12, 150, 40);
		estilizarBotao(btnVoltar);
		btnVoltar.addActionListener(e -> dispose());
		topo.add(btnVoltar);
		carregarAlunosDoBanco();
	}

	private int centro(int larguraCard, int larguraTela) {
		return Math.max(20, (larguraTela - larguraCard) / 2);
	}

	private void criarResumo(JPanel p) {
		JPanel card = card();
		card.setBounds(centro(1280, p.getWidth()), 210, 1280, 100);

		lblTotal = indicador("Total", "0", 30);
		lblAtivos = indicador("Ativos", "0", 330);
		lblInativos = indicador("Inativos", "0", 630);
		lblTransferidos = indicador("Transferidos", "0", 930);
		card.add(lblTotal.getParent());
		card.add(lblAtivos.getParent());
		card.add(lblInativos.getParent());
		card.add(lblTransferidos.getParent());

		p.add(card);
	}

	private JLabel indicador(String titulo, String valor, int x) {
		JPanel p = new JPanel(null);
		p.setOpaque(false);
		p.setBounds(x, 10, 220, 80);

		JLabel t = new JLabel(titulo);
		t.setForeground(corLabel);
		t.setBounds(0, 0, 200, 20);

		JLabel v = new JLabel(valor);
		v.setForeground(textos);
		v.setFont(new Font("Segoe UI", Font.BOLD, 28));
		v.setBounds(0, 25, 200, 35);

		p.add(t);
		p.add(v);
		return v;
	}

	private void criarPesquisa(JPanel p) {
		JPanel card = card();
		card.setBounds(centro(1280, p.getWidth()), 330, 1280, 110);

		JLabel l1 = label("Nome");
		l1.setBounds(20, 15, 100, 20);

		JTextField nome = campo();
		campoNomePesquisa = nome;
		nome.setBounds(20, 40, 280, 35);

		JLabel l2 = label("Matrícula");
		l2.setBounds(320, 15, 100, 20);

		JTextField matricula = campo();
		campoMatriculaPesquisa = matricula;
		matricula.setBounds(320, 40, 150, 35);

		JButton pesquisar = botao("Pesquisar");
		pesquisar.setBounds(500, 40, 150, 35);
		pesquisar.addActionListener(e -> atualizarTabela());

		JButton limpar = botao("Limpar");
		limpar.setBounds(670, 40, 150, 35);
		limpar.addActionListener(e -> {
			campoNomePesquisa.setText("");
			campoMatriculaPesquisa.setText("");
			atualizarTabela();
		});

		card.add(l1);
		card.add(nome);
		card.add(l2);
		card.add(matricula);
		card.add(pesquisar);
		card.add(limpar);
		p.add(card);
	}

	private void criarTabela(JPanel p) {
		JPanel card = card();
		card.setBounds(centro(1280, p.getWidth()), 460, 1280, 220);

		String[] cols = { "Matrícula", "Nome", "Turma", "Curso", "Turno", "Situação" };

		modeloTabela = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tabela = new JTable(modeloTabela);
		tabela.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				carregarAlunoSelecionado();
			}
		});
		tabela.setRowHeight(30);
		tabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		tabela.setForeground(textos);
		tabela.setBackground(corInterna);
		tabela.setGridColor(new Color(90, 50, 170));
		tabela.setSelectionBackground(new Color(80, 40, 160));
		tabela.setSelectionForeground(textos);
		tabela.setShowGrid(true);
		tabela.setShowHorizontalLines(true);
		tabela.setShowVerticalLines(true);
		tabela.setIntercellSpacing(new Dimension(1, 1));
		tabela.setFillsViewportHeight(false);
		tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabela.getTableHeader();
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

		for (int i = 0; i < tabela.getColumnCount(); i++) {
			tabela.getColumnModel().getColumn(i).setCellRenderer(centro);
		}

		JScrollPane scrollTabela = new JScrollPane(tabela);
		scrollTabela.setBounds(25, 40, 1230, 160);
		scrollTabela.getViewport().setBackground(corInterna);
		scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
		scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
		card.add(scrollTabela);
		p.add(card);
	}

	private void carregarAlunosDoBanco() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			alunos.clear();
			alunos.addAll(new AlunoDAO(conn).listar());
			turmasPorId.clear();
			for (Turma turma : new TurmaDAO(conn).listarTodos()) {
				turmasPorId.put(turma.getIdTurma(), turma);
			}
			atualizarTabela();
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar os alunos do banco: " + ex.getMessage());
		}
	}

	private void atualizarTabela() {
		String nomeFiltro = campoNomePesquisa.getText().trim().toLowerCase();
		String matriculaFiltro = campoMatriculaPesquisa.getText().trim();
		alunosExibidos.clear();
		modeloTabela.setRowCount(0);

		for (Aluno aluno : alunos) {
			if ((!nomeFiltro.isEmpty() && !aluno.getNome().toLowerCase().contains(nomeFiltro))
					|| (!matriculaFiltro.isEmpty() && !aluno.getMatricula().contains(matriculaFiltro))) {
				continue;
			}
			alunosExibidos.add(aluno);
			Turma turma = turmasPorId.get(aluno.getIdTurma());
			modeloTabela.addRow(new Object[] { aluno.getMatricula(), aluno.getNome(),
					turma == null ? "Não informado" : turma.getDescricaoTurma(), "Não informado",
					turma == null || turma.getTurno() == null ? "Não informado" : turma.getTurno().name(),
					aluno.getSituacao().name() });
		}
		atualizarResumo();
		limparDetalhes();
	}

	private void carregarAlunoSelecionado() {
		int linha = tabela.getSelectedRow();
		if (linha < 0 || linha >= alunosExibidos.size()) {
			return;
		}
		Aluno aluno = alunosExibidos.get(linha);
		alunoSelecionado = aluno;
		campoNome.setText(valor(aluno.getNome()));
		campoCpf.setText(valor(aluno.getCpf()));
		campoRg.setText(valor(aluno.getRg()));
		campoNascimento.setText(aluno.getDataNascimento() == null ? "Não informado"
				: aluno.getDataNascimento().toString());
		campoTelefone.setText(valor(aluno.getTelefone()));
		campoEmail.setText(valor(aluno.getEmail()));

		try (Connection conn = ConnectionFactory.getConnection()) {
			PaisAluno responsavel = new PaisAlunoDAO(conn).buscarPorId(aluno.getIdPais());
			if (responsavel == null) {
				limparResponsavel();
			} else {
				boolean usaMae = responsavel.getNomeMae() != null && !responsavel.getNomeMae().isBlank();
				campoResponsavel.setText(valor(usaMae ? responsavel.getNomeMae() : responsavel.getNomePai()));
				campoCpfResponsavel.setText(valor(usaMae ? responsavel.getCpfMae() : responsavel.getCpfPai()));
				campoTelefoneResponsavel
						.setText(valor(usaMae ? responsavel.getTelefoneMae() : responsavel.getTelefonePai()));
				campoParentesco.setText(usaMae ? "Mãe" : "Pai");
			}
		} catch (SQLException ex) {
			mostrarErro("Não foi possível carregar o responsável: " + ex.getMessage());
		}
		habilitarEdicao(true);
	}

	private void atualizarResumo() {
		long ativos = alunos.stream().filter(a -> a.getSituacao().isAtivo()).count();
		long inativos = alunos.stream().filter(a -> a.getSituacao() == variaveisEnum.SituacaoAluno.INATIVO).count();
		long transferidos = alunos.stream().filter(a -> a.getSituacao() == variaveisEnum.SituacaoAluno.TRANSFERIDO)
				.count();
		lblTotal.setText(String.valueOf(alunos.size()));
		lblAtivos.setText(String.valueOf(ativos));
		lblInativos.setText(String.valueOf(inativos));
		lblTransferidos.setText(String.valueOf(transferidos));
	}

	private void limparDetalhes() {
		alunoSelecionado = null;
		if (campoNome == null) {
			return;
		}
		campoNome.setText("");
		campoCpf.setText("");
		campoRg.setText("");
		campoNascimento.setText("");
		campoTelefone.setText("");
		campoEmail.setText("");
		limparResponsavel();
		habilitarEdicao(false);
	}

	private void limparResponsavel() {
		campoResponsavel.setText("");
		campoCpfResponsavel.setText("");
		campoTelefoneResponsavel.setText("");
		campoParentesco.setText("");
	}

	private String valor(String texto) {
		return texto == null || texto.isBlank() ? "Não informado" : texto;
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void criarCadastroAluno(JPanel p) {
		JPanel card = card();
		card.setBounds(centro(1280, p.getWidth()), 700, 620, 220);

		campoNome = adicionarCampo(card, "Nome Completo", 20, 20, 250);
		campoCpf = adicionarCampo(card, "CPF", 320, 20, 250);
		campoRg = adicionarCampo(card, "RG", 20, 80, 250);
		campoNascimento = adicionarCampo(card, "Nascimento", 320, 80, 250);
		campoTelefone = adicionarCampo(card, "Telefone", 20, 140, 250);
		campoEmail = adicionarCampo(card, "E-mail", 320, 140, 250);
		p.add(card);
	}

	private void criarResponsavel(JPanel p) {
		JPanel card = card();
		card.setBounds(centro(1280, p.getWidth()) + 660, 700, 620, 220);

		campoResponsavel = adicionarCampo(card, "Responsável", 20, 20, 250);
		campoCpfResponsavel = adicionarCampo(card, "CPF", 320, 20, 250);
		campoTelefoneResponsavel = adicionarCampo(card, "Telefone", 20, 80, 250);
		campoParentesco = adicionarCampo(card, "Parentesco", 320, 80, 250);

		btnSalvar = botao("Salvar Alterações");
		btnSalvar.setBounds(20, 150, 220, 40);
		btnSalvar.addActionListener(e -> salvarAlteracoes());

		btnInativar = botao("Inativar Aluno");
		btnInativar.setBounds(260, 150, 220, 40);
		btnInativar.addActionListener(e -> inativarAluno());

		card.add(btnSalvar);
		card.add(btnInativar);
		habilitarEdicao(false);
		p.add(card);
	}

	private JTextField adicionarCampo(JPanel p, String texto, int x, int y, int largura) {
		JLabel l = label(texto);
		l.setBounds(x, y, 200, 20);

		JTextField c = campo();
		c.setBounds(x, y + 25, largura, 32);

		p.add(l);
		p.add(c);
		return c;
	}

	private void habilitarEdicao(boolean habilitado) {
		if (campoNome == null)
			return;
		campoNome.setEditable(habilitado);
		campoRg.setEditable(habilitado);
		campoNascimento.setEditable(habilitado);
		campoTelefone.setEditable(habilitado);
		campoEmail.setEditable(habilitado);
		campoCpf.setEditable(false);
		campoResponsavel.setEditable(false);
		campoCpfResponsavel.setEditable(false);
		campoTelefoneResponsavel.setEditable(false);
		campoParentesco.setEditable(false);
		if (btnSalvar != null)
			btnSalvar.setEnabled(habilitado);
		if (btnInativar != null)
			btnInativar.setEnabled(habilitado && alunoSelecionado != null
					&& alunoSelecionado.getSituacao().isAtivo());
	}

	private void salvarAlteracoes() {
		if (alunoSelecionado == null) {
			mostrarErro("Selecione um aluno antes de salvar.");
			return;
		}
		try {
			alunoSelecionado.setNome(campoNome.getText().trim());
			alunoSelecionado.setRg(campoRg.getText().trim());
			alunoSelecionado.setDataNascimento(LocalDate.parse(campoNascimento.getText().trim()));
			alunoSelecionado.setTelefone(campoTelefone.getText().trim());
			alunoSelecionado.setEmail(campoEmail.getText().trim());
			new AlunoController().atualizarAluno(alunoSelecionado);
			JOptionPane.showMessageDialog(this, "Alterações salvas com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			carregarAlunosDoBanco();
		} catch (RuntimeException ex) {
			mostrarErro("Não foi possível salvar as alterações: " + ex.getMessage());
		}
	}

	private void inativarAluno() {
		if (alunoSelecionado == null) {
			mostrarErro("Selecione um aluno antes de inativar.");
			return;
		}
		if (JOptionPane.showConfirmDialog(this, "Deseja inativar o aluno selecionado?", "Confirmar inativação",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		try {
			new AlunoController().excluirAluno(alunoSelecionado.getIdAluno());
			JOptionPane.showMessageDialog(this, "Aluno inativado com sucesso.", "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
			carregarAlunosDoBanco();
		} catch (RuntimeException ex) {
			mostrarErro("Não foi possível inativar o aluno: " + ex.getMessage());
		}
	}

	private JPanel card() {
		JPanel p = new JPanel(null);
		p.setBackground(corCampo);
		p.setBorder(new LineBorder(corBorda));
		return p;
	}

	private JLabel label(String t) {
		JLabel l = new JLabel(t);
		l.setForeground(corLabel);
		return l;
	}

	private JTextField campo() {
		JTextField c = new JTextField();
		c.setBackground(corCampo);
		c.setForeground(textos);
		c.setBorder(new LineBorder(corBorda));
		return c;
	}

	private JButton botao(String t) {
		JButton b = new JButton(t);
		b.setBackground(corCampo);
		b.setForeground(textos);
		b.setBorder(new LineBorder(corBorda));
		return b;
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				GradientPaint gp = new GradientPaint(0, 0, new Color(70, 20, 160), getWidth(), getHeight(),
						new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight() - 20, 30, 30);
				g2.dispose();
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Alunos - Secretaria");
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
		titulo.setBounds(40, 50, 500, 45);
		topo.add(titulo);

		JLabel subtitulo = new JLabel("Verifique todos os registros de alunos");
		subtitulo.setForeground(new Color(245, 225, 255));
		subtitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
		subtitulo.setBounds(45, 100, 500, 25);
		topo.add(subtitulo);

		return topo;
	}

	private void estilizarBotao(JButton botao) {
		botao.setBackground(corCampo);
		botao.setForeground(textos);
		botao.setFocusPainted(false);
		botao.setBorder(new LineBorder(corBorda));
	}
}
