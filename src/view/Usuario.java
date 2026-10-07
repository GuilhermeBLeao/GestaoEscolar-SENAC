package view;

import java.awt.Color;

import java.awt.Component;

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

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.List;

import javax.swing.BorderFactory;

import javax.swing.DefaultCellEditor;

import javax.swing.JButton;

import javax.swing.JCheckBox;

import javax.swing.JComboBox;

import javax.swing.JDialog;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTable;

import javax.swing.JTextField;

import javax.swing.RowFilter;

import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;

import javax.swing.border.LineBorder;

import javax.swing.table.DefaultTableCellRenderer;

import javax.swing.table.DefaultTableModel;

import javax.swing.table.JTableHeader;

import javax.swing.table.TableCellRenderer;

import javax.swing.table.TableRowSorter;

import controller.UsuarioController;

import dao.AlunoDAO;

import dao.FuncionarioDAO;

import dao.PaisAlunoDAO;

import dao.ProfessorDAO;

import database.ConnectionFactory;

import model.Aluno;

import model.Funcionario;

import model.PaisAluno;

import model.Professor;

import variaveisEnum.TipoUsuario;

public class Usuario extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);

	private final Color corInterna = new Color(38, 2, 92);

	private final Color corBorda = new Color(120, 70, 220);

	private final Color corLabel = new Color(255, 120, 220);

	private final Color corCampo = new Color(25, 6, 75);

	private final Color textos = Color.WHITE;

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private JTable tabela;

	private DefaultTableModel modelo;

	private TableRowSorter<DefaultTableModel> sorter;

	private JTextField txtBusca;

	private JComboBox<String> cbFiltroTipo;

	private JComboBox<String> cbFiltroStatus;

	private final UsuarioController usuarioController = new UsuarioController();

	public Usuario() {

		setTitle("Gerenciar Usuários");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		int margem = 30;

		int larguraInterno = areaUtil.width - (margem * 2) + 20;

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

		interno.setBounds(20, 20, larguraInterno, alturaInterno);

		interno.setBackground(corInterna);

		externo.add(interno);

		JLabel titulo = new JLabel("Gerenciar Usuários");

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 42));

		titulo.setHorizontalAlignment(SwingConstants.CENTER);

		titulo.setBounds(250, 35, larguraInterno - 500, 55);

		interno.add(titulo);

		JLabel subtitulo = new JLabel("Visualize, edite, inative ou reative os usuários cadastrados no sistema.");

		subtitulo.setForeground(textos);

		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));

		subtitulo.setHorizontalAlignment(SwingConstants.CENTER);

		subtitulo.setBounds(250, 100, larguraInterno - 500, 30);

		interno.add(subtitulo);

		/*
		 * 
		 * ========================================================= PAINEL DE FILTROS
		 * 
		 * =========================================================
		 * 
		 */

		JPanel filtros = criarPainelArredondado(corCampo, corBorda, 24);

		filtros.setLayout(null);

		filtros.setBounds(40, 155, larguraInterno - 80, 125);

		interno.add(filtros);

		JLabel lbBusca = new JLabel("Buscar usuário");

		lbBusca.setForeground(corLabel);

		lbBusca.setFont(new Font("Segoe UI", Font.BOLD, 16));

		lbBusca.setBounds(30, 15, 200, 25);

		filtros.add(lbBusca);

		txtBusca = criarCampoTexto();

		txtBusca.setBounds(30, 47, 390, 38);

		filtros.add(txtBusca);

		JLabel lbTipo = new JLabel("Tipo de usuário");

		lbTipo.setForeground(corLabel);

		lbTipo.setFont(new Font("Segoe UI", Font.BOLD, 16));

		lbTipo.setBounds(450, 15, 200, 25);

		filtros.add(lbTipo);

		cbFiltroTipo = criarCombo(new String[] { "Todos", "Aluno", "Professor", "Responsável", "Secretaria", "Direção",

				"Pedagógico", "Administrador" });

		cbFiltroTipo.setBounds(450, 47, 210, 38);

		filtros.add(cbFiltroTipo);

		JLabel lbStatus = new JLabel("Status");

		lbStatus.setForeground(corLabel);

		lbStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));

		lbStatus.setBounds(690, 15, 150, 25);

		filtros.add(lbStatus);

		cbFiltroStatus = criarCombo(new String[] { "Todos", "Ativo", "Inativo" });

		cbFiltroStatus.setBounds(690, 47, 150, 38);

		filtros.add(cbFiltroStatus);

		JButton btnBuscar = new JButton("Buscar");

		btnBuscar.setBounds(870, 47, 130, 38);

		estilizarBotao(btnBuscar);

		filtros.add(btnBuscar);

		JButton btnLimpar = new JButton("Limpar");

		btnLimpar.setBounds(1015, 47, 130, 38);

		estilizarBotao(btnLimpar);

		filtros.add(btnLimpar);

		JButton btnCancelar = new JButton("Cancelar");

		btnCancelar.setBounds(1160, 47, 140, 38);

		estilizarBotaoSecundario(btnCancelar);

		filtros.add(btnCancelar);

		btnBuscar.addActionListener(e -> filtrarUsuarios());

		txtBusca.addActionListener(e -> filtrarUsuarios());

		cbFiltroTipo.addActionListener(e -> filtrarUsuarios());

		cbFiltroStatus.addActionListener(e -> filtrarUsuarios());

		btnLimpar.addActionListener(e -> limparFiltros());

		btnCancelar.addActionListener(e -> dispose());

		/*
		 * 
		 * ========================================================= PAINEL DA TABELA
		 * 
		 * =========================================================
		 * 
		 */

		JPanel painelTabela = criarPainelArredondado(corCampo, corBorda, 24);

		painelTabela.setLayout(null);

		painelTabela.setBounds(40, 305, larguraInterno - 80, alturaInterno - 345);

		interno.add(painelTabela);

		JLabel tituloTabela = new JLabel("Usuários cadastrados");

		tituloTabela.setForeground(textos);

		tituloTabela.setFont(new Font("Segoe UI", Font.BOLD, 28));

		tituloTabela.setBounds(30, 18, 400, 35);

		painelTabela.add(tituloTabela);

		criarTabela();

		JScrollPane scroll = new JScrollPane(tabela);

		scroll.setBounds(30, 70, painelTabela.getWidth() - 60, painelTabela.getHeight() - 100);

		scroll.setBorder(new LineBorder(corBorda, 1, true));

		scroll.getViewport().setBackground(corCampo);

		scroll.getVerticalScrollBar().setUnitIncrement(26);

		painelTabela.add(scroll);

		setVisible(true);

	}

	/*
	 * 
	 * ============================================================= TABELA
	 * 
	 * =============================================================
	 * 
	 */

	private void criarTabela() {

		String[] colunas = { "ID", "Nome", "CPF", "Tipo", "Status", "Criação", "Último login", "Editar", "Ação" };

		modelo = new DefaultTableModel(colunas, 0) {

			private static final long serialVersionUID = 1L;

			@Override

			public boolean isCellEditable(int row, int column) {

				return column == 7 || column == 8;

			}

		};

		tabela = new JTable(modelo);

		tabela.setRowHeight(46);

		tabela.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		tabela.setForeground(textos);

		tabela.setBackground(corInterna);

		tabela.setGridColor(new Color(90, 50, 170));

		tabela.setSelectionBackground(new Color(80, 40, 160));

		tabela.setSelectionForeground(Color.WHITE);

		tabela.setShowGrid(true);

		tabela.setFillsViewportHeight(true);

		tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = tabela.getTableHeader();

		header.setFont(new Font("Segoe UI", Font.BOLD, 15));

		header.setBackground(corInterna);

		header.setForeground(textos);

		header.setReorderingAllowed(false);

		header.setResizingAllowed(false);

		DefaultTableCellRenderer centro = new DefaultTableCellRenderer();

		centro.setHorizontalAlignment(SwingConstants.CENTER);

		centro.setVerticalAlignment(SwingConstants.CENTER);

		centro.setBackground(corCampo);

		centro.setForeground(textos);

		centro.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		for (int i = 0; i < 7; i++) {

			tabela.getColumnModel().getColumn(i).setCellRenderer(centro);

		}

		tabela.getColumnModel().getColumn(0).setPreferredWidth(60);

		tabela.getColumnModel().getColumn(1).setPreferredWidth(280);

		tabela.getColumnModel().getColumn(2).setPreferredWidth(150);

		tabela.getColumnModel().getColumn(3).setPreferredWidth(150);

		tabela.getColumnModel().getColumn(4).setPreferredWidth(100);

		tabela.getColumnModel().getColumn(5).setPreferredWidth(110);

		tabela.getColumnModel().getColumn(6).setPreferredWidth(150);

		tabela.getColumnModel().getColumn(7).setPreferredWidth(100);

		tabela.getColumnModel().getColumn(8).setPreferredWidth(110);

		tabela.getColumnModel().getColumn(7).setCellRenderer(new BotaoRenderer(new Color(85, 35, 170)));

		tabela.getColumnModel().getColumn(8).setCellRenderer(new BotaoRenderer(new Color(150, 35, 75)));

		tabela.getColumnModel().getColumn(7).setCellEditor(new BotaoEditor("Editar"));

		tabela.getColumnModel().getColumn(8).setCellEditor(new BotaoEditor("Ação"));

		/*
		 * 
		 * O sorter fica criado uma única vez. Isso corrige o problema anterior em que o
		 * 
		 * filtro era recriado a cada busca.
		 * 
		 */

		sorter = new TableRowSorter<>(modelo);

		tabela.setRowSorter(sorter);

		carregarUsuarios();

	}

	/*
	 * 
	 * ============================================================= CARREGAR
	 * 
	 * USUÁRIOS =============================================================
	 * 
	 */

	private void carregarUsuarios() {

		modelo.setRowCount(0);

		List<model.Usuario> usuarios;

		try {

			usuarios = usuarioController.listarTodosUsuarios();

		} catch (RuntimeException ex) {

			JOptionPane.showMessageDialog(this, obterMensagemErro(ex), "Erro ao carregar usuários",

					JOptionPane.ERROR_MESSAGE);

			return;

		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			for (model.Usuario usuario : usuarios) {

				String nome = obterNomeUsuario(conn, usuario);

				modelo.addRow(new Object[] { usuario.getIdUsuario(), nome, formatarCpf(usuario.getCpf()),

						formatarTipo(usuario.getTipoUsuario()), usuario.isAtivo() ? "Ativo" : "Inativo",

						formatarData(usuario.getDataCriacao()), formatarDataHora(usuario.getUltimoLogin()), "Editar",

						usuario.isAtivo() ? "Inativar" : "Reativar" });

			}

		} catch (SQLException ex) {

			JOptionPane.showMessageDialog(this,

					"Não foi possível carregar os nomes dos usuários.\n\n" + ex.getMessage(), "Erro",

					JOptionPane.ERROR_MESSAGE);

		}

	}

	/*
	 * 
	 * ============================================================= OBTER NOME REAL
	 * 
	 * DO USUÁRIO =============================================================
	 * 
	 */

	private String obterNomeUsuario(model.Usuario usuario) {

		try (Connection conn = ConnectionFactory.getConnection()) {

			return obterNomeUsuario(conn, usuario);

		} catch (SQLException ex) {

			return "Nome não identificado";
		}
	}

	private String obterNomeUsuario(Connection conn, model.Usuario usuario) {

		try {

			/*
			 * 
			 * ALUNO
			 * 
			 */

			if (usuario.getAlunoId() > 0) {

				Aluno aluno = new AlunoDAO(conn).buscarPorId(usuario.getAlunoId());

				if (aluno != null && aluno.getNome() != null && !aluno.getNome().isBlank()) {

					return aluno.getNome();

				}

			}

			/*
			 * 
			 * PROFESSOR
			 * 
			 */

			if (usuario.getProfessorId() > 0) {

				Professor professor = new ProfessorDAO(conn).buscarPorId(usuario.getProfessorId());

				if (professor != null && professor.getNome() != null && !professor.getNome().isBlank()) {

					return professor.getNome();

				}

			}

			/*
			 * 
			 * FUNCIONÁRIO
			 * 
			 */

			if (usuario.getFuncionarioId() > 0) {

				Funcionario funcionario = new FuncionarioDAO(conn).buscarPorId(usuario.getFuncionarioId());

				if (funcionario != null && funcionario.getNome() != null && !funcionario.getNome().isBlank()) {

					return funcionario.getNome();

				}

			}

			/*
			 * 
			 * RESPONSÁVEL
			 * 
			 */

			if (usuario.getPaiId() > 0) {

				PaisAluno pais = new PaisAlunoDAO(conn).buscarPorId(usuario.getPaiId());

				if (pais != null) {

					String cpfUsuario = normalizarCpf(usuario.getCpf());

					String cpfMae = normalizarCpf(pais.getCpfMae());

					String cpfPai = normalizarCpf(pais.getCpfPai());

					/*
					 * 
					 * Se o CPF do usuário corresponde à mãe, mostramos o nome da mãe.
					 * 
					 */

					if (!cpfUsuario.isEmpty() && cpfUsuario.equals(cpfMae) && pais.getNomeMae() != null

							&& !pais.getNomeMae().isBlank()) {

						return pais.getNomeMae();

					}

					/*
					 * 
					 * Se o CPF do usuário corresponde ao pai, mostramos o nome do pai.
					 * 
					 */

					if (!cpfUsuario.isEmpty() && cpfUsuario.equals(cpfPai) && pais.getNomePai() != null

							&& !pais.getNomePai().isBlank()) {

						return pais.getNomePai();

					}

					/*
					 * 
					 * Fallback para registros antigos.
					 * 
					 */

					if (pais.getNomeMae() != null && !pais.getNomeMae().isBlank()) {

						return pais.getNomeMae();

					}

					if (pais.getNomePai() != null && !pais.getNomePai().isBlank()) {

						return pais.getNomePai();

					}

				}

			}

		} catch (SQLException | RuntimeException ex) {

			/*
			 * 
			 * Não interrompe o carregamento da tela por causa de um registro inconsistente.
			 * 
			 */

		}

		return "Nome não identificado";

	}

	/*
	 * 
	 * ============================================================= FILTROS
	 * 
	 * =============================================================
	 * 
	 */

	private void filtrarUsuarios() {

		if (sorter == null) {

			return;

		}

		String busca = txtBusca.getText().trim().toLowerCase();

		String tipoSelecionado = cbFiltroTipo.getSelectedItem() == null ? "Todos"

				: cbFiltroTipo.getSelectedItem().toString();

		String statusSelecionado = cbFiltroStatus.getSelectedItem() == null ? "Todos"

				: cbFiltroStatus.getSelectedItem().toString();

		sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {

			@Override

			public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {

				String id = entry.getStringValue(0).toLowerCase();

				String nome = entry.getStringValue(1).toLowerCase();

				String cpf = entry.getStringValue(2).toLowerCase();

				String tipo = entry.getStringValue(3).toLowerCase();

				String status = entry.getStringValue(4).toLowerCase();

				boolean bateBusca = busca.isEmpty() || id.contains(busca) || nome.contains(busca) || cpf.contains(busca)

						|| tipo.contains(busca) || status.contains(busca);

				boolean bateTipo = "Todos".equals(tipoSelecionado) || tipo.equals(tipoSelecionado.toLowerCase());

				boolean bateStatus = "Todos".equals(statusSelecionado)

						|| status.equals(statusSelecionado.toLowerCase());

				return bateBusca && bateTipo && bateStatus;

			}

		});

	}

	private void limparFiltros() {

		txtBusca.setText("");

		cbFiltroTipo.setSelectedItem("Todos");

		cbFiltroStatus.setSelectedItem("Todos");

		filtrarUsuarios();

	}

	/*
	 * 
	 * ============================================================= EDIÇÃO
	 * 
	 * =============================================================
	 * 
	 */

	private void abrirTelaEdicao(int linhaModelo) {

		int idUsuario = Integer.parseInt(modelo.getValueAt(linhaModelo, 0).toString());

		final model.Usuario usuario;

		try {

			usuario = usuarioController.buscarUsuarioPorId(idUsuario);

		} catch (RuntimeException ex) {

			JOptionPane.showMessageDialog(this, obterMensagemErro(ex), "Erro", JOptionPane.ERROR_MESSAGE);

			return;

		}

		if (usuario == null) {

			JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);

			return;

		}

		JDialog dialog = new JDialog(this, "Editar Usuário", true);

		dialog.setSize(720, 500);

		dialog.setLocationRelativeTo(this);

		dialog.setLayout(null);

		dialog.getContentPane().setBackground(corInterna);

		dialog.setResizable(false);

		/*
		 * 
		 * TÍTULO
		 * 
		 */

		JLabel titulo = new JLabel("Editar usuário");

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));

		titulo.setBounds(35, 25, 500, 40);

		dialog.add(titulo);

		JPanel linha = new JPanel();

		linha.setBackground(corLabel);

		linha.setBounds(35, 75, 300, 3);

		dialog.add(linha);

		/*
		 * 
		 * NOME
		 * 
		 */

		JLabel lblNome = new JLabel("Nome do usuário");

		lblNome.setForeground(corLabel);

		lblNome.setFont(new Font("Segoe UI", Font.BOLD, 15));

		lblNome.setBounds(35, 105, 280, 25);

		dialog.add(lblNome);

		JTextField txtNome = criarCampoTexto();

		txtNome.setBounds(35, 135, 600, 38);

		txtNome.setText(obterNomeUsuario(usuario));

		txtNome.setEditable(false);

		dialog.add(txtNome);

		/*
		 * 
		 * CPF
		 * 
		 */

		JLabel lblCpf = new JLabel("CPF");

		lblCpf.setForeground(corLabel);

		lblCpf.setFont(new Font("Segoe UI", Font.BOLD, 15));

		lblCpf.setBounds(35, 195, 280, 25);

		dialog.add(lblCpf);

		JTextField txtCpf = criarCampoTexto();

		txtCpf.setBounds(35, 225, 280, 38);

		txtCpf.setText(formatarCpf(usuario.getCpf()));

		txtCpf.setEditable(false);

		dialog.add(txtCpf);

		/*
		 * 
		 * TIPO
		 * 
		 */

		JLabel lblTipo = new JLabel("Tipo de usuário");

		lblTipo.setForeground(corLabel);

		lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 15));

		lblTipo.setBounds(335, 195, 300, 25);

		dialog.add(lblTipo);

		JComboBox<String> cbTipo = criarCombo(obterTiposEditaveis());

		cbTipo.setBounds(335, 225, 300, 38);

		cbTipo.setSelectedItem(formatarTipo(usuario.getTipoUsuario()));

		dialog.add(cbTipo);

		/*
		 * 
		 * NOVA SENHA
		 * 
		 */

		JLabel lblSenha = new JLabel("Nova senha");

		lblSenha.setForeground(corLabel);

		lblSenha.setFont(new Font("Segoe UI", Font.BOLD, 15));

		lblSenha.setBounds(35, 285, 280, 25);

		dialog.add(lblSenha);

		javax.swing.JPasswordField txtSenha = criarCampoSenha();

		txtSenha.setBounds(35, 315, 280, 38);

		dialog.add(txtSenha);

		/*
		 * 
		 * CONFIRMAR SENHA
		 * 
		 */

		JLabel lblConfirmarSenha = new JLabel("Confirmar nova senha");

		lblConfirmarSenha.setForeground(corLabel);

		lblConfirmarSenha.setFont(new Font("Segoe UI", Font.BOLD, 15));

		lblConfirmarSenha.setBounds(335, 285, 300, 25);

		dialog.add(lblConfirmarSenha);

		javax.swing.JPasswordField txtConfirmarSenha = criarCampoSenha();

		txtConfirmarSenha.setBounds(335, 315, 300, 38);

		dialog.add(txtConfirmarSenha);

		/*
		 * 
		 * INFORMAÇÃO
		 * 
		 */

		JLabel lblInfo = new JLabel("<html>" + "O nome, CPF, data de criação, último login "

				+ "e vínculo são controlados pelo sistema." + "</html>");

		lblInfo.setForeground(Color.LIGHT_GRAY);

		lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		lblInfo.setBounds(35, 365, 600, 35);

		dialog.add(lblInfo);

		/*
		 * 
		 * SALVAR
		 * 
		 */

		JButton btnSalvar = new JButton("Salvar alterações");

		btnSalvar.setBounds(35, 410, 190, 40);

		estilizarBotao(btnSalvar);

		dialog.add(btnSalvar);

		/*
		 * 
		 * CANCELAR
		 * 
		 */

		JButton btnCancelar = new JButton("Cancelar");

		btnCancelar.setBounds(235, 410, 130, 40);

		estilizarBotaoSecundario(btnCancelar);

		dialog.add(btnCancelar);

		/*
		 * 
		 * AÇÃO SALVAR
		 * 
		 */

		btnSalvar.addActionListener(e -> {

			String novaSenha = new String(txtSenha.getPassword());

			String confirmarSenha = new String(txtConfirmarSenha.getPassword());

			if (!novaSenha.isBlank() && !novaSenha.equals(confirmarSenha)) {

				JOptionPane.showMessageDialog(dialog, "As senhas não conferem.", "Validação",

						JOptionPane.WARNING_MESSAGE);

				return;

			}

			try {

				String tipoSelecionado = cbTipo.getSelectedItem().toString();

				TipoUsuario novoTipo = obterTipo(tipoSelecionado);

				model.Usuario usuarioAtualizado = copiarUsuario(usuario);

				/*
				 * 
				 * O vínculo real é copiado do usuário original e NÃO é removido.
				 * 
				 */

				usuarioAtualizado.setTipoUsuario(novoTipo);

				if (!novaSenha.isBlank()) {

					usuarioAtualizado.setSenha(novaSenha);

				}

				usuarioController.atualizarUsuario(usuarioAtualizado);

				carregarUsuarios();

				filtrarUsuarios();

				JOptionPane.showMessageDialog(dialog, "Usuário atualizado com sucesso!", "Sucesso",

						JOptionPane.INFORMATION_MESSAGE);

				dialog.dispose();

			} catch (IllegalArgumentException ex) {

				JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Não foi possível atualizar",

						JOptionPane.WARNING_MESSAGE);

			} catch (RuntimeException ex) {

				JOptionPane.showMessageDialog(dialog, obterMensagemErro(ex), "Erro", JOptionPane.ERROR_MESSAGE);

			}

		});

		btnCancelar.addActionListener(e -> dialog.dispose());

		dialog.setVisible(true);

	}

	/*
	 * 
	 * ============================================================= COPIAR USUÁRIO
	 * 
	 * =============================================================
	 * 
	 */

	private model.Usuario copiarUsuario(model.Usuario original) {

		model.Usuario copia = new model.Usuario();

		copia.setIdUsuario(original.getIdUsuario());

		copia.setCpf(original.getCpf());

		copia.setAtivo(original.isAtivo());

		/*
		 * 
		 * PRESERVAÇÃO DO VÍNCULO DO ALUNO
		 * 
		 */

		if (original.getAlunoId() > 0) {

			copia.setAlunoId(original.getAlunoId());

		}

		/*
		 * 
		 * PRESERVAÇÃO DO VÍNCULO DO FUNCIONÁRIO
		 * 
		 */

		if (original.getFuncionarioId() > 0) {

			copia.setFuncionarioId(original.getFuncionarioId());

		}

		/*
		 * 
		 * PRESERVAÇÃO DO VÍNCULO DO RESPONSÁVEL
		 * 
		 */

		if (original.getPaiId() > 0) {

			copia.setPaiId(original.getPaiId());

		}

		/*
		 * 
		 * PRESERVAÇÃO DO VÍNCULO DO PROFESSOR
		 * 
		 */

		if (original.getProfessorId() > 0) {

			copia.setProfessorId(original.getProfessorId());

		}

		copia.setTipoUsuario(original.getTipoUsuario());

		copia.carregarDataCriacaoDoBanco(original.getDataCriacao());

		if (original.getUltimoLogin() != null) {

			copia.setUltimoLogin(original.getUltimoLogin());

		}

		return copia;

	}

	/*
	 * 
	 * ============================================================= INATIVAR /
	 * 
	 * REATIVAR =============================================================
	 * 
	 */

	private void inativarOuReativar(int linhaModelo) {

		int idUsuario = Integer.parseInt(modelo.getValueAt(linhaModelo, 0).toString());

		String status = modelo.getValueAt(linhaModelo, 4).toString();

		try {

			if ("Ativo".equals(status)) {

				int opcao = JOptionPane.showConfirmDialog(this,

						"Essa operação irá inativar o usuário.\n" + "O cadastro não será apagado do banco de dados.\n\n"

								+ "O vínculo do usuário também permanecerá preservado.\n\n" + "Deseja continuar?",

						"Inativar usuário", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if (opcao != JOptionPane.YES_OPTION) {

					return;

				}

				usuarioController.excluirUsuario(idUsuario);

				JOptionPane.showMessageDialog(this, "Usuário inativado com sucesso!", "Sucesso",

						JOptionPane.INFORMATION_MESSAGE);

			} else {

				int opcao = JOptionPane.showConfirmDialog(this, "Deseja reativar este usuário?", "Reativar usuário",

						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (opcao != JOptionPane.YES_OPTION) {

					return;

				}

				usuarioController.reativarUsuario(idUsuario);

				JOptionPane.showMessageDialog(this, "Usuário reativado com sucesso!", "Sucesso",

						JOptionPane.INFORMATION_MESSAGE);

			}

			carregarUsuarios();

			filtrarUsuarios();

		} catch (IllegalArgumentException ex) {

			JOptionPane.showMessageDialog(this, ex.getMessage(), "Operação não realizada", JOptionPane.WARNING_MESSAGE);

		} catch (RuntimeException ex) {

			JOptionPane.showMessageDialog(this, obterMensagemErro(ex), "Erro", JOptionPane.ERROR_MESSAGE);

		}

	}

	/*
	 * 
	 * ============================================================= TIPOS DE
	 * 
	 * USUÁRIO =============================================================
	 * 
	 */

	private String[] obterTiposEditaveis() {

		return new String[] { "Aluno", "Professor", "Responsável", "Secretaria", "Direção", "Pedagógico",

				"Administrador" };

	}

	private TipoUsuario obterTipo(String descricao) {

		switch (descricao) {

		case "Aluno":

			return TipoUsuario.ALUNO;

		case "Professor":

			return TipoUsuario.PROFESSOR;

		case "Responsável":

			return TipoUsuario.RESPONSAVEL;

		case "Secretaria":

			return TipoUsuario.SECRETARIA;

		case "Direção":

			return TipoUsuario.DIRECAO;

		case "Pedagógico":

			return TipoUsuario.PEDAGOGICO;

		case "Administrador":

			return TipoUsuario.ADMINISTRADOR;

		default:

			throw new IllegalArgumentException("Tipo de usuário inválido.");

		}

	}

	private String formatarTipo(TipoUsuario tipo) {

		if (tipo == null) {

			return "Não informado";

		}

		switch (tipo) {

		case ALUNO:

			return "Aluno";

		case PROFESSOR:

			return "Professor";

		case RESPONSAVEL:

			return "Responsável";

		case SECRETARIA:

			return "Secretaria";

		case DIRECAO:

			return "Direção";

		case PEDAGOGICO:

			return "Pedagógico";

		case ADMINISTRADOR:

			return "Administrador";

		default:

			return tipo.name();

		}

	}

	/*
	 * 
	 * ============================================================= FORMATAÇÕES
	 * 
	 * =============================================================
	 * 
	 */

	private String formatarCpf(String cpf) {



		if (cpf == null || cpf.isBlank()) {



			return "Não informado";

		}



		String numeros = cpf.replaceAll("\\D", "");



		if (numeros.length() != 11) {



			return cpf;

		}



		return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "." + numeros.substring(6, 9) + "-"

				+ numeros.substring(9);

	}

	private String normalizarCpf(String cpf) {



		if (cpf == null) {



			return "";

		}



		return cpf.replaceAll("\\D", "");

	}

	private String formatarData(LocalDate data) {

		if (data == null) {

			return "Não informado";

		}

		return data.format(FORMATO_DATA);

	}

	private String formatarDataHora(LocalDateTime data) {

		if (data == null) {

			return "Nunca";

		}

		return data.format(FORMATO_DATA_HORA);

	}

	/*
	 * 
	 * ============================================================= MENSAGEM DE
	 * 
	 * ERRO =============================================================
	 * 
	 */

	private String obterMensagemErro(RuntimeException ex) {

		if (ex.getCause() != null && ex.getCause().getMessage() != null) {

			return ex.getCause().getMessage();

		}

		if (ex.getMessage() != null && !ex.getMessage().isBlank()) {

			return ex.getMessage();

		}

		return "Ocorreu um erro durante a operação.";

	}

	/*
	 * 
	 * ============================================================= CAMPOS
	 * 
	 * =============================================================
	 * 
	 */

	private JTextField criarCampoTexto() {

		JTextField campo = new JTextField();

		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		campo.setForeground(Color.WHITE);

		campo.setBackground(new Color(31, 10, 90));

		campo.setCaretColor(Color.WHITE);

		campo.setBorder(

				BorderFactory.createCompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));

		return campo;

	}

	private javax.swing.JPasswordField criarCampoSenha() {

		javax.swing.JPasswordField campo = new javax.swing.JPasswordField();

		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		campo.setForeground(Color.WHITE);

		campo.setBackground(new Color(31, 10, 90));

		campo.setCaretColor(Color.WHITE);

		campo.setBorder(

				BorderFactory.createCompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));

		return campo;

	}

	private JComboBox<String> criarCombo(String[] itens) {

		JComboBox<String> combo = new JComboBox<>(itens);

		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		combo.setForeground(Color.WHITE);

		combo.setBackground(new Color(31, 10, 90));

		combo.setBorder(new LineBorder(corBorda, 1, true));

		combo.setFocusable(false);

		return combo;

	}

	/*
	 * 
	 * ============================================================= BOTÕES
	 * 
	 * =============================================================
	 * 
	 */

	private void estilizarBotao(JButton botao) {

		botao.setFont(new Font("Segoe UI", Font.BOLD, 15));

		botao.setForeground(textos);

		botao.setBackground(corCampo);

		botao.setBorder(new LineBorder(corBorda, 1, true));

		botao.setFocusPainted(false);

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

	}

	private void estilizarBotaoSecundario(JButton botao) {

		botao.setFont(new Font("Segoe UI", Font.BOLD, 15));

		botao.setForeground(Color.WHITE);

		botao.setBackground(new Color(45, 15, 120));

		botao.setBorder(new LineBorder(corBorda, 1, true));

		botao.setFocusPainted(false);

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

	}

	/*
	 * 
	 * ============================================================= PAINEL
	 * 
	 * ARREDONDADO =============================================================
	 * 
	 */

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

	/*
	 * 
	 * ============================================================= RENDERIZADOR
	 * 
	 * DOS BOTÕES =============================================================
	 * 
	 */

	private class BotaoRenderer extends JButton implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public BotaoRenderer(Color cor) {

			setOpaque(true);

			setForeground(Color.WHITE);

			setBackground(cor);

			setFont(new Font("Segoe UI", Font.BOLD, 13));

			setBorder(new LineBorder(corLabel, 1, true));

			setFocusPainted(false);

		}

		@Override

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,

				int row, int column) {

			setText(value == null ? "" : value.toString());

			/*
			 * 
			 * A coluna de ação muda de cor dependendo do texto apresentado.
			 * 
			 */

			if (value != null && "Reativar".equals(value.toString())) {

				setBackground(new Color(25, 115, 105));

			} else if (value != null && "Inativar".equals(value.toString())) {

				setBackground(new Color(150, 35, 75));

			}

			return this;

		}

	}

	/*
	 * 
	 * ============================================================= EDITOR DOS
	 * 
	 * BOTÕES =============================================================
	 * 
	 */

	private class BotaoEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		private final JButton botao;

		private final String acao;

		private int linhaView;

		public BotaoEditor(String acao) {

			super(new JCheckBox());

			this.acao = acao;

			botao = new JButton(acao);

			botao.setForeground(Color.WHITE);

			botao.setFont(new Font("Segoe UI", Font.BOLD, 13));

			botao.setFocusPainted(false);

			botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

			if ("Editar".equals(acao)) {

				botao.setBackground(new Color(85, 35, 170));

			} else {

				botao.setBackground(new Color(150, 35, 75));

			}

			botao.addActionListener(e -> {

				fireEditingStopped();

				int linhaModelo = tabela.convertRowIndexToModel(linhaView);

				if ("Editar".equals(acao)) {

					abrirTelaEdicao(linhaModelo);

				} else {

					inativarOuReativar(linhaModelo);

				}

			});

		}

		@Override

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,

				int column) {

			linhaView = row;

			String texto = value == null ? acao : value.toString();

			botao.setText(texto);

			if ("Reativar".equals(texto)) {

				botao.setBackground(new Color(25, 115, 105));

			} else if ("Inativar".equals(texto)) {

				botao.setBackground(new Color(150, 35, 75));

			} else if ("Editar".equals(texto)) {

				botao.setBackground(new Color(85, 35, 170));

			}

			return botao;

		}

		@Override

		public Object getCellEditorValue() {

			return acao;

		}

	}

}