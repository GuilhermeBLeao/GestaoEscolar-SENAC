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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.AtividadeDiaDAO;
import dao.DisciplinaDAO;
import dao.ProfessorDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.AtividadeDia;
import model.Disciplina;
import model.Professor;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

public class LancarAtividadeDia extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private JTextField campoProfessor;
	private JComboBox<String> cbTurma;
	private JComboBox<String> cbDisciplina;
	private JTextField campoData;
	private JTextArea areaDescricao;
	private final List<Turma> turmasDisponiveis = new ArrayList<>();
	private final List<Disciplina> disciplinasDisponiveis = new ArrayList<>();
	private int idProfessorLogado;
	private int idTurmaSelecionada;
	private int idDisciplinaSelecionada;

	public LancarAtividadeDia() {
		setTitle("Atividade do Dia");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);

		JPanel externo = new JPanel(null);
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);

		JPanel interno = new JPanel(null);
		interno.setBounds(30, 30, areaUtil.width - 60, areaUtil.height - 80);
		interno.setBackground(corInterna);
		externo.add(interno);
		int larguraInterno = areaUtil.width - 60;

		JPanel topo = criarTopo();
		topo.setBounds((larguraInterno - 1200) / 2, 15, 1200, 220);
		interno.add(topo);
		int larguraCampo = 700;
		int alturaCampo = 70;
		int larguraDescricao = 1480;
		int alturaDescricao = 280;
		int centroX = larguraInterno / 2;

		JLabel lblProfessor = criarLabel("Professor:");
		lblProfessor.setBounds(centroX - 740, 250, 200, 40);
		interno.add(lblProfessor);
		campoProfessor = new JTextField();
		campoProfessor.setEditable(false);
		campoProfessor.setFont(new Font("Segoe UI", Font.PLAIN, 28));
		campoProfessor.setBackground(corCampo);
		campoProfessor.setForeground(textos);
		campoProfessor.setBorder(new LineBorder(corBorda));
		campoProfessor.setBounds(centroX - 740, 300, larguraCampo, alturaCampo);
		interno.add(campoProfessor);
		
		JLabel lblTurma = criarLabel("Turma:");
		lblTurma.setBounds(centroX + 40, 250, 200, 40);
		interno.add(lblTurma);
		cbTurma = criarCombo();
		cbTurma.setBounds(centroX + 40, 300, larguraCampo, alturaCampo);
		interno.add(cbTurma);

		JLabel lblDisciplina = criarLabel("Disciplina:");
		lblDisciplina.setBounds(centroX - 740, 390, 200, 40);
		interno.add(lblDisciplina);
		cbDisciplina = criarCombo();
		cbDisciplina.setBounds(centroX - 740, 440, larguraCampo, alturaCampo);
		interno.add(cbDisciplina);

		JLabel lblData = criarLabel("Data:");
		lblData.setBounds(centroX + 40, 390, 200, 40);
		interno.add(lblData);
		campoData = new JTextField(LocalDate.now().format(formatoData));
		campoData.setFont(new Font("Segoe UI", Font.PLAIN, 28));
		campoData.setBackground(corCampo);
		campoData.setForeground(textos);
		campoData.setCaretColor(textos);
		campoData.setBorder(new LineBorder(corBorda));
		campoData.setHorizontalAlignment(SwingConstants.CENTER);
		campoData.setBounds(centroX + 40, 440, 400, alturaCampo);
		interno.add(campoData);

		JLabel lblDescricao = criarLabel("Descrição:");
		lblDescricao.setBounds(centroX - (larguraDescricao / 2), 530, 250, 40);
		interno.add(lblDescricao);
		areaDescricao = new JTextArea();
		areaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 24));
		areaDescricao.setBackground(corCampo);
		areaDescricao.setForeground(textos);
		areaDescricao.setCaretColor(textos);
		areaDescricao.setLineWrap(true);
		areaDescricao.setWrapStyleWord(true);
		areaDescricao.setBorder(new LineBorder(corBorda));
		JScrollPane scroll = new JScrollPane(areaDescricao);
		scroll.setBounds(centroX - (larguraDescricao / 2), 580, larguraDescricao, alturaDescricao);
		scroll.getVerticalScrollBar().setUnitIncrement(26);
		interno.add(scroll);
		
		int larguraBotao = 260;
		int alturaBotao = 50;
		int espacamento = 40;
		int yBotao = 890;
		int total = larguraBotao * 3 + espacamento * 2;
		int inicio = centroX - (total / 2);

		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.setBounds(inicio, yBotao, larguraBotao, alturaBotao);
		estilizarBotao(btnSalvar);
		btnSalvar.addActionListener(e -> salvarAtividade());
		interno.add(btnSalvar);

		JButton btnLimparCampos = new JButton("Limpar Campos");
		btnLimparCampos.setBounds(inicio + larguraBotao + espacamento, yBotao, larguraBotao, alturaBotao);
		estilizarBotao(btnLimparCampos);
		btnLimparCampos.addActionListener(e -> limparCampos());
		interno.add(btnLimparCampos);
		
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.setBounds(inicio + (larguraBotao + espacamento) * 2, yBotao, larguraBotao, alturaBotao);
		estilizarBotao(btnCancelar);
		btnCancelar.addActionListener(e -> dispose());
		interno.add(btnCancelar);
		cbTurma.addActionListener(e -> atualizarDisciplinas());
		cbDisciplina.addActionListener(e -> atualizarDisciplinaSelecionada());
		campoData.addActionListener(e -> carregarAtividadeExistente());
		carregarProfessorLogado();
		setVisible(true);
	}

	private JPanel criarTopo() {
		JPanel topo = new JPanel(null) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, new Color(70, 20, 160),
								getWidth(), getHeight(), new Color(190, 35, 170));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, getWidth(), getHeight() - 20, 30, 30);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		topo.setOpaque(false);

		JLabel titulo = new JLabel("Lançar atividade do dia");
		titulo.setForeground(Color.WHITE);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 54));
		titulo.setBounds(50, 35, 800, 70);
		topo.add(titulo);

		JLabel sub = new JLabel("Cadastre um pequeno resumo do que será/foi feito durante a aula.");
		sub.setForeground(new Color(245, 225, 255));
		sub.setFont(new Font("Segoe UI", Font.PLAIN, 26));
		sub.setBounds(52, 120, 1100, 40);
		topo.add(sub);
		return topo;
	}

	private void carregarProfessorLogado() {
		Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();

		if (usuarioLogado == null) {
			JOptionPane.showMessageDialog(this, "Nenhum usuário está logado.", 
				"Sessão inválida", JOptionPane.WARNING_MESSAGE);
			return;
		}
		idProfessorLogado = usuarioLogado.getProfessorId();

		if (idProfessorLogado <= 0) {
			JOptionPane.showMessageDialog(this, "O usuário logado não possui um professor associado.",
					"Professor inválido", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			ProfessorDAO professorDAO = new ProfessorDAO(conn);
			Professor professor = professorDAO.buscarPorId(idProfessorLogado);
			
			if (professor == null) {
				JOptionPane.showMessageDialog(this, "Não foi possível localizar o professor logado.",
						"Professor não encontrado", JOptionPane.WARNING_MESSAGE);
				return;
			}
			campoProfessor.setText(professor.getNome());
			carregarTurmas();
		} catch (SQLException | RuntimeException e) {
			JOptionPane.showMessageDialog(this, 
					"Não foi possível carregar os dados do professor.\n\n" + e.getMessage(),
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarTurmas() {
		cbTurma.removeAllItems();
		turmasDisponiveis.clear();
		idTurmaSelecionada = 0;
		idDisciplinaSelecionada = 0;

		if (idProfessorLogado <= 0) {
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			List<Turma> turmas = turmaDAO.listarPorProfessor(idProfessorLogado);
			turmasDisponiveis.addAll(turmas);
			for (Turma turma : turmasDisponiveis) {
				cbTurma.addItem(turma.getDescricaoTurma());
			}
		} catch (SQLException | RuntimeException e) {
			JOptionPane.showMessageDialog(this, "Não foi possível carregar as turmas.\n\n"
							+ e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (cbTurma.getItemCount() > 0) {
			cbTurma.setSelectedIndex(0);
			atualizarDisciplinas();
		} else {
			JOptionPane.showMessageDialog(this, "O professor logado não possui turmas vinculadas.",
					"Nenhuma turma", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void atualizarDisciplinas() {
		cbDisciplina.removeAllItems();
		disciplinasDisponiveis.clear();
		idTurmaSelecionada = 0;
		idDisciplinaSelecionada = 0;
		int indiceTurma = cbTurma.getSelectedIndex();
		
		if (indiceTurma < 0 || indiceTurma >= turmasDisponiveis.size()) {
			return;
		}
		Turma turma = turmasDisponiveis.get(indiceTurma);
		idTurmaSelecionada = turma.getIdTurma();
		try (Connection conn = ConnectionFactory.getConnection()) {
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
			List<Disciplina> disciplinas = disciplinaDAO
					.listarPorProfessorETurma(idProfessorLogado, idTurmaSelecionada);
			disciplinasDisponiveis.addAll(disciplinas);

			for (Disciplina disciplina : disciplinasDisponiveis) {
				cbDisciplina.addItem(disciplina.getDescricao());
			}
		} catch (SQLException | RuntimeException e) {
			JOptionPane.showMessageDialog(this, "Não foi possível carregar as disciplinas.\n\n"
							+ e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (cbDisciplina.getItemCount() > 0) {
			cbDisciplina.setSelectedIndex(0);
			atualizarDisciplinaSelecionada();
		} else {
			JOptionPane.showMessageDialog(this, "O professor não possui disciplinas vinculadas "
						+ "à turma selecionada.", "Nenhuma disciplina", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void atualizarDisciplinaSelecionada() {
		idDisciplinaSelecionada = 0;
		int indiceDisciplina = cbDisciplina.getSelectedIndex();
		
		if (indiceDisciplina < 0 || indiceDisciplina >= disciplinasDisponiveis.size()) {
			return;
		}

		idDisciplinaSelecionada = disciplinasDisponiveis.get(indiceDisciplina).getIdDisciplina();
		carregarAtividadeExistente();
	}

	private void carregarAtividadeExistente() {
		if (idTurmaSelecionada <= 0 || idDisciplinaSelecionada <= 0) {
			return;
		}
		LocalDate data;
		try {
			data = obterDataInformada();
		} catch (IllegalArgumentException e) {
			return;
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			AtividadeDiaDAO atividadeDiaDAO = new AtividadeDiaDAO(conn);
			AtividadeDia atividade = atividadeDiaDAO.buscarPorTurmaDisciplinaData(idTurmaSelecionada,
							idDisciplinaSelecionada, data);

			if (atividade != null) {
				areaDescricao.setText(atividade.getDescricao());
			} else {
				areaDescricao.setText("");
			}
		} catch (SQLException | RuntimeException e) {
			JOptionPane.showMessageDialog(this, "Não foi possível verificar a atividade "
							+ "já registrada para esta aula.\n\n" + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void salvarAtividade() {
		if (idProfessorLogado <= 0) {
			JOptionPane.showMessageDialog(this, "Não foi possível identificar o professor logado.",
					"Professor inválido", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (idTurmaSelecionada <= 0) {
			JOptionPane.showMessageDialog(this, "Selecione uma turma.", "Turma obrigatória",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (idDisciplinaSelecionada <= 0) {
			JOptionPane.showMessageDialog(this, "Selecione uma disciplina.", "Disciplina obrigatória",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		LocalDate data;
		try {
			data = obterDataInformada();
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Data inválida", 
				JOptionPane.WARNING_MESSAGE);
			campoData.requestFocus();
			return;
		}

		String descricao = areaDescricao.getText().trim();
		
		if (descricao.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Informe a descrição da atividade realizada na aula.",
					"Descrição obrigatória", JOptionPane.WARNING_MESSAGE);
			areaDescricao.requestFocus();
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AtividadeDiaDAO atividadeDiaDAO = new AtividadeDiaDAO(conn);
			AtividadeDia atividadeExistente = atividadeDiaDAO
				.buscarPorTurmaDisciplinaData(idTurmaSelecionada, idDisciplinaSelecionada, data);

			if (atividadeExistente != null) {
				JOptionPane.showMessageDialog(this, "Já existe uma atividade registrada "
								+ "para esta turma, disciplina e data.\n\n"
								+ "A atividade existente foi carregada. "
								+ "O DAO atual precisa disponibilizar "
								+ "um método de atualização para permitir "
								+ "alterá-la.",
						"Atividade já registrada", JOptionPane.INFORMATION_MESSAGE);
				areaDescricao.requestFocus();
				return;
			}

			AtividadeDia atividade = new AtividadeDia();
			atividade.setProfessorId(idProfessorLogado);
			atividade.setTurmaId(idTurmaSelecionada);
			atividade.setDisciplinaId(idDisciplinaSelecionada);
			atividade.setData(data);
			atividade.setDescricao(descricao);
			atividadeDiaDAO.inserir(atividade);
			JOptionPane.showMessageDialog(this, "Atividade do dia registrada com sucesso!",
					"Sucesso", JOptionPane.INFORMATION_MESSAGE);
			areaDescricao.setText("");
		} catch (SQLException | RuntimeException e) {
			JOptionPane.showMessageDialog(this, "Não foi possível salvar a atividade.\n\n"
							+ e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private LocalDate obterDataInformada() {
		String texto = campoData.getText().trim();

		if (texto.isEmpty()) {
			throw new IllegalArgumentException("Informe a data da aula.");
		}
		try {
			return LocalDate.parse(texto, formatoData);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Data inválida.\n\n" 
				+ "Informe a data no formato DD/MM/AAAA.");
		}
	}

	private void limparCampos() {
		areaDescricao.setText("");
		campoData.setText(LocalDate.now().format(formatoData));

		if (cbTurma.getItemCount() > 0) {
			cbTurma.setSelectedIndex(0);
		}
		areaDescricao.requestFocus();
	}

	private JLabel criarLabel(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 24));
		return label;
	}

	private JComboBox<String> criarCombo() {
		JComboBox<String> combo = new JComboBox<>();
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 24));
		combo.setBackground(corCampo);
		combo.setForeground(textos);
		combo.setBorder(new LineBorder(corBorda));
		combo.setFocusable(false);
		return combo;
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 22));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
}