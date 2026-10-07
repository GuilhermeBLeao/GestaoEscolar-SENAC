package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

import controller.AlunoController;
import controller.PaisAlunoController;
import dao.PaisAlunoDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Endereco;
import model.PaisAluno;
import model.Turma;
import dao.GeradorMatricula;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;
import variaveisEnum.SituacaoAluno;

public class CadastroAluno extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;
	private final List<JTextField> camposTexto = new ArrayList<>();
	private final List<JFormattedTextField> camposMascara = new ArrayList<>();
	private final List<JComboBox<String>> combosTexto = new ArrayList<>();
	private final List<Turma> turmas = new ArrayList<>();
	private JComboBox<Estado> comboEstadoCadastro;
	private JTextArea campoObservacoes;
	private JTextField campoMatricula;
	private JTabbedPane abas;
	public CadastroAluno() {
		setTitle("Matrícula Aluno");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int largura = Math.min(1280, areaUtil.width - 40);
		int altura = Math.min(800, areaUtil.height - 40);
		largura = Math.max(900, largura);
		altura = Math.max(650, altura);
		setSize(largura, altura);
		setMinimumSize(new Dimension(900, 650));
		setLocationRelativeTo(null);
		setResizable(true);

		JPanel externo = new JPanel(new BorderLayout(0, 10));
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(15, 15, 15, 15));
		setContentPane(externo);

		JPanel cabecalho = new JPanel();
		cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
		cabecalho.setBackground(corExterna);

		JLabel lblTitulo = new JLabel("MATRÍCULA DO ALUNO");
		lblTitulo.setForeground(textos);
		lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel lblInstrucao = new JLabel("Preencha as informações do aluno.");
		lblInstrucao.setForeground(textos);
		lblInstrucao.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		lblInstrucao.setAlignmentX(Component.CENTER_ALIGNMENT);

		cabecalho.add(lblTitulo);
		cabecalho.add(Box.createVerticalStrut(5));
		cabecalho.add(lblInstrucao);
		externo.add(cabecalho, BorderLayout.NORTH);

		abas = new JTabbedPane(JTabbedPane.TOP);
		abas.setFont(new Font("Segoe UI", Font.BOLD, 15));
		abas.setBackground(corExterna);
		abas.setForeground(textos);

		JPanel abaAluno = montarAbaAluno();
		JPanel abaEndereco = montarAbaEndereco();
		JPanel abaPais = montarAbaPais();

		abas.addTab("Aluno", criarScrollAba(abaAluno));
		abas.addTab("Endereço", criarScrollAba(abaEndereco));
		abas.addTab("Pais / Responsáveis", criarScrollAba(abaPais));
		externo.add(abas, BorderLayout.CENTER);

		JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
		painelBotoes.setBackground(corExterna);

		JButton btnLimpar = new JButton("Limpar Campos");
		estilizarBotao(btnLimpar);
		btnLimpar.setPreferredSize(new Dimension(200, 45));
		btnLimpar.addActionListener(e -> limparCampos());
		
		JButton btnCancelar = new JButton("Cancelar");
		estilizarBotao(btnCancelar);
		btnCancelar.setPreferredSize(new Dimension(200, 45));
		btnCancelar.addActionListener(e -> dispose());
		
		JButton btnMatricular = new JButton("Matricular Aluno");
		estilizarBotao(btnMatricular);
		btnMatricular.setPreferredSize(new Dimension(240, 45));
		btnMatricular.addActionListener(e -> matricularAluno());
		
		painelBotoes.add(btnLimpar);
		painelBotoes.add(btnCancelar);
		painelBotoes.add(btnMatricular);
		externo.add(painelBotoes, BorderLayout.SOUTH);

		carregarTurmas();
		gerarMatriculaProvisoria();
		setVisible(true);
	}

	private JScrollPane criarScrollAba(JPanel painel) {
		JScrollPane scroll = new JScrollPane(painel);
		scroll.setBorder(new LineBorder(corBorda, 1, true));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		scroll.getViewport().setBackground(corInterna);
		return scroll;
	}

	private JPanel criarAba() {
		PainelRolavel painel = new PainelRolavel();
		painel.setBackground(corInterna);
		painel.setBorder(new EmptyBorder(20, 20, 30, 20));
		return painel;
	}

	private JPanel montarAbaAluno() {
		JPanel painel = criarAba();

		campoMatricula = criarCampoTexto();
		campoMatricula.setEditable(false);
		campoMatricula.setEnabled(false);

		adicionarCampo(painel, "Matrícula", campoMatricula, 0, 0, 1, 1.0);

		JTextField campoNome = criarCampoTexto();
		camposTexto.add(campoNome);
		adicionarCampo(painel, "Nome completo *", campoNome, 1, 0, 2, 2.0);

		JComboBox<String> comboAtivo = criarCombo(new String[] { "Sim", "Não" });
		combosTexto.add(comboAtivo);
		adicionarCampo(painel, "Ativo", comboAtivo, 3, 0, 1, 1.0);

		JFormattedTextField campoCpf = criarCampoMascara("###.###.###-##");
		camposMascara.add(campoCpf);
		adicionarCampo(painel, "CPF *", campoCpf, 0, 1, 1, 1.0);

		JFormattedTextField campoRg = criarCampoMascara("###########");
		camposMascara.add(campoRg);
		adicionarCampo(painel, "RG", campoRg, 1, 1, 1, 1.0);

		JFormattedTextField campoTelefone = criarCampoMascara("(##) #####-####");
		camposMascara.add(campoTelefone);
		adicionarCampo(painel, "Telefone *", campoTelefone, 2, 1, 1, 1.0);

		JComboBox<String> comboSexo = criarCombo(new String[] { "Selecione", "Masculino", "Feminino", "Outros" });
		combosTexto.add(comboSexo);
		adicionarCampo(painel, "Sexo *", comboSexo, 3, 1, 1, 1.0);

		JTextField campoEmail = criarCampoTexto();
		camposTexto.add(campoEmail);
		adicionarCampo(painel, "E-mail *", campoEmail, 0, 2, 2, 2.0);

		JFormattedTextField campoDataNascimento = criarCampoMascara("##/##/####");
		camposMascara.add(campoDataNascimento);
		adicionarCampo(painel, "Data de nascimento *", campoDataNascimento, 2, 2, 1, 1.0);

		JFormattedTextField campoDataCadastro = criarCampoMascara("##/##/####");
		campoDataCadastro.setEnabled(false);
		camposMascara.add(campoDataCadastro);
		adicionarCampo(painel, "Data de cadastro", campoDataCadastro, 3, 2, 1, 1.0);

		JComboBox<String> comboTurma = criarCombo(new String[] { "Selecione a turma" });
		combosTexto.add(comboTurma);
		adicionarCampo(painel, "Turma *", comboTurma, 0, 3, 2, 2.0);

		JComboBox<String> comboSituacao = criarCombo(new String[] { "Selecione", "CURSANDO", "TRANCADO", "TRANSFERIDO", "CONCLUIDO" });
		combosTexto.add(comboSituacao);
		adicionarCampo(painel, "Situação", comboSituacao, 2, 3, 2, 2.0);

		JLabel labelObservacoes = criarLabelCampo("Observações de saúde");
		painel.add(labelObservacoes, criarConstraints(0, 4, 4, 4.0));

		campoObservacoes = new JTextArea(5, 20);
		campoObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		campoObservacoes.setForeground(textos);
		campoObservacoes.setBackground(corCampo);
		campoObservacoes.setCaretColor(textos);
		campoObservacoes.setLineWrap(true);
		campoObservacoes.setWrapStyleWord(true);
		campoObservacoes.setMargin(new Insets(10, 10, 10, 10));

		JScrollPane scrollObservacoes = new JScrollPane(campoObservacoes);
		scrollObservacoes.setPreferredSize(new Dimension(200, 130));
		scrollObservacoes.setBorder(new LineBorder(corBorda, 1, true));
		painel.add(scrollObservacoes, criarConstraints(0, 5, 4, 4.0));
		GridBagConstraints espaco = criarConstraints(0, 6, 4, 1.0);
		espaco.weighty = 1;
		painel.add(Box.createVerticalGlue(), espaco);
		return painel;
	}

	private JPanel montarAbaEndereco() {
		JPanel painel = criarAba();

		JFormattedTextField campoCep = criarCampoMascara("#####-###");
		camposMascara.add(campoCep);
		adicionarCampo(painel, "CEP *", campoCep, 0, 0, 1, 1.0);

		JTextField campoRua = criarCampoTexto();
		camposTexto.add(campoRua);
		adicionarCampo(painel, "Rua *", campoRua, 1, 0, 2, 2.0);

		JTextField campoNumero = criarCampoTexto();
		camposTexto.add(campoNumero);
		adicionarCampo(painel, "Número *", campoNumero, 3, 0, 1, 1.0);

		JTextField campoComplemento = criarCampoTexto();
		camposTexto.add(campoComplemento);
		adicionarCampo(painel, "Complemento", campoComplemento, 0, 1, 1, 1.0);

		JTextField campoBairro = criarCampoTexto();
		camposTexto.add(campoBairro);
		adicionarCampo(painel, "Bairro *", campoBairro, 1, 1, 1, 1.0);

		JTextField campoCidade = criarCampoTexto();
		camposTexto.add(campoCidade);
		adicionarCampo(painel, "Cidade *", campoCidade, 2, 1, 1, 1.0);

		comboEstadoCadastro = criarComboEstado();
		adicionarCampo(painel, "Estado *", comboEstadoCadastro, 3, 1, 1, 1.0);
		GridBagConstraints espaco = criarConstraints(0, 2, 4, 1.0);
		espaco.weighty = 1;
		painel.add(Box.createVerticalGlue(), espaco);
		return painel;
	}

	private JPanel montarAbaPais() {
		JPanel painel = criarAba();
		JLabel tituloMae = criarLabelCampo("Dados da Mãe");
		tituloMae.setFont(new Font("Segoe UI", Font.BOLD, 22));
		painel.add(tituloMae, criarConstraints(0, 0, 4, 1.0));

		JTextField campoNomeMae = criarCampoTexto();
		camposTexto.add(campoNomeMae);

		JFormattedTextField campoCpfMae = criarCampoMascara("###.###.###-##");
		camposMascara.add(campoCpfMae);
		adicionarCampo(painel, "Nome completo da mãe", campoNomeMae, 0, 1, 2, 2.0);
		adicionarCampo(painel, "CPF da mãe", campoCpfMae, 2, 1, 2, 2.0);

		JTextField campoEmailMae = criarCampoTexto();
		camposTexto.add(campoEmailMae);

		JFormattedTextField campoTelefoneMae = criarCampoMascara("(##) #####-####");
		camposMascara.add(campoTelefoneMae);
		adicionarCampo(painel, "E-mail da mãe", campoEmailMae, 0, 2, 2, 2.0);
		adicionarCampo(painel, "Telefone da mãe", campoTelefoneMae, 2, 2, 2, 2.0);

		JLabel tituloPai = criarLabelCampo("Dados do Pai");
		tituloPai.setFont(new Font("Segoe UI", Font.BOLD, 22));
		painel.add(tituloPai, criarConstraints(0, 3, 4, 1.0));

		JTextField campoNomePai = criarCampoTexto();
		camposTexto.add(campoNomePai);

		JFormattedTextField campoCpfPai = criarCampoMascara("###.###.###-##");
		camposMascara.add(campoCpfPai);
		adicionarCampo(painel, "Nome completo do pai", campoNomePai, 0, 4, 2, 2.0);
		adicionarCampo(painel, "CPF do pai", campoCpfPai, 2, 4, 2, 2.0);

		JTextField campoEmailPai = criarCampoTexto();
		camposTexto.add(campoEmailPai);

		JFormattedTextField campoTelefonePai = criarCampoMascara("(##) #####-####");
		camposMascara.add(campoTelefonePai);
		adicionarCampo(painel, "E-mail do pai", campoEmailPai, 0, 5, 2, 2.0);
		adicionarCampo(painel, "Telefone do pai", campoTelefonePai, 2, 5, 2, 2.0);
		GridBagConstraints espaco = criarConstraints(0, 6, 4, 1.0);
		espaco.weighty = 1;
		painel.add(Box.createVerticalGlue(), espaco);
		return painel;
	}

	private GridBagConstraints criarConstraints(int gridx, int gridy, int gridwidth, double weightx) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = 1;
		gbc.weightx = weightx;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(8, 8, 8, 8);
		return gbc;
	}

	private JLabel criarLabelCampo(String texto) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Segoe UI", Font.BOLD, 15));
		return label;
	}

	private JPanel criarBlocoCampo(String texto, JComponent componente) {
		JPanel bloco = new JPanel(new BorderLayout(0, 5));
		bloco.setOpaque(false);

		JLabel label = criarLabelCampo(texto);
		bloco.add(label, BorderLayout.NORTH);
		bloco.add(componente, BorderLayout.CENTER);
		componente.setPreferredSize(new Dimension(200, 40));
		return bloco;
	}

	private void adicionarCampo(JPanel painel, String texto, JComponent componente, int gridx, int gridy, int gridwidth,
		double weightx) {
		JPanel bloco = criarBlocoCampo(texto, componente);
		painel.add(bloco, criarConstraints(gridx, gridy, gridwidth, weightx));
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
			campo.setBorder(new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12)));
			campo.addFocusListener(new java.awt.event.FocusAdapter() {
				@Override
				public void focusGained(java.awt.event.FocusEvent e) {
					javax.swing.SwingUtilities.invokeLater(() -> campo.setCaretPosition(0));
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

	private JTextField criarCampoTexto() {
		JTextField campo = new JTextField();
		campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		campo.setForeground(textos);
		campo.setBackground(corCampo);
		campo.setCaretColor(textos);
		Border borda = new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 12, 0, 12));
		campo.setBorder(borda);
		return campo;
	}

	private JComboBox<String> criarCombo(String[] itens) {
		JComboBox<String> combo = new JComboBox<>(itens);
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		combo.setForeground(textos);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
		return combo;
	}

	private JComboBox<Estado> criarComboEstado() {
		JComboBox<Estado> combo = new JComboBox<>(Estado.values());
		combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		combo.setForeground(textos);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
		combo.setSelectedIndex(-1);
		return combo;
	}

	private void carregarTurmas() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			turmas.clear();
			turmas.addAll(new TurmaDAO(conn).listarAtivas());

			JComboBox<String> comboTurma = combosTexto.get(2);
			comboTurma.removeAllItems();
			comboTurma.addItem("Selecione a turma");

			for (Turma turma : turmas) {
				comboTurma.addItem(turma.getIdTurma() + " - " + turma.getDescricaoTurma());
			}
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível carregar as turmas:\n\n" + ex.getMessage());
		}
	}

	private void gerarMatriculaProvisoria() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			String matricula = GeradorMatricula.gerarProvisoria(conn);
			campoMatricula.setText(matricula);
			LocalDate dataAtual = LocalDate.now();
			camposMascara.get(4).setText(dataAtual.format(FORMATO_DATA));
		} catch (SQLException | RuntimeException ex) {
			mostrarErro("Não foi possível gerar a matrícula provisória.\n\n" + ex.getMessage());
		}
	}

	private void matricularAluno() {
		try {
			Turma turma = turmaSelecionada();
			Estado estado = comboEstadoCadastro.getSelectedItem() instanceof Estado
					? (Estado) comboEstadoCadastro.getSelectedItem()
					: null;

			if (turma == null) {
				throw new IllegalArgumentException("Selecione a turma.");
			}
			if (estado == null) {
				throw new IllegalArgumentException("Selecione o estado do endereço.");
			}
			PaisAluno pais = criarPaisAluno();
			int idPais = obterOuCriarPais(pais);
			Aluno aluno = new Aluno();
			aluno.setMatricula(campoMatricula.getText().trim());
			aluno.setNome(camposTexto.get(0).getText());
			aluno.setEmail(camposTexto.get(1).getText());
			aluno.setTelefone(limparNumeros(camposMascara.get(2).getText()));
			aluno.setCpf(limparNumeros(camposMascara.get(0).getText()));
			aluno.setRg(limparNumeros(camposMascara.get(1).getText()));
			aluno.setSexo(sexoSelecionado());
			aluno.setSituacao(SituacaoAluno.ATIVO);
			aluno.setIdPais(idPais);
			aluno.setIdTurma(turma.getIdTurma());
			aluno.setDataNascimento(parseData(camposMascara.get(3).getText()));
			aluno.setObsSaude(campoObservacoes.getText());
			aluno.setEndereco(criarEndereco(estado));
			new AlunoController().matricularAluno(aluno);

			JOptionPane.showMessageDialog(this, "Aluno matriculado com sucesso.\n\n"
				+ "Matrícula gerada: " + aluno.getMatricula(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
			limparCampos();
		} catch (DateTimeParseException ex) {
			mostrarErro("Informe a data de nascimento " + "no formato dd/MM/aaaa.");
		} catch (RuntimeException ex) {
			mostrarErro(ex.getMessage());
		}
	}

	private PaisAluno criarPaisAluno() {
		PaisAluno pais = new PaisAluno();
		pais.setNomeMae(camposTexto.get(7).getText());
		pais.setCpfMae(limparNumeros(camposMascara.get(6).getText()));
		pais.setEmailMae(camposTexto.get(8).getText());
		pais.setTelefoneMae(limparNumeros(camposMascara.get(7).getText()));
		pais.setNomePai(camposTexto.get(9).getText());
		pais.setCpfPai(limparNumeros(camposMascara.get(8).getText()));
		pais.setEmailPai(camposTexto.get(10).getText());
		pais.setTelefonePai(limparNumeros(camposMascara.get(9).getText()));
		return pais;
	}

	private int obterOuCriarPais(PaisAluno pais) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PaisAlunoDAO dao = new PaisAlunoDAO(conn);
			PaisAluno existente = null;

			if (pais.getCpfMae() != null && !pais.getCpfMae().isBlank()) {
				existente = dao.buscarPorCpfMae(pais.getCpfMae());
			}
			if (existente == null && pais.getCpfPai() != null && !pais.getCpfPai().isBlank()) {
				existente = dao.buscarPorCpfPai(pais.getCpfPai());
			}
			if (existente != null) {
				return existente.getIdPais();
			}
		} catch (SQLException ex) {
			throw new RuntimeException("Não foi possível consultar " + "os responsáveis.", ex);
		}
		new PaisAlunoController().salvarPaisAluno(pais);
		return pais.getIdPais();
	}

	private Endereco criarEndereco(Estado estado) {
		Endereco endereco = new Endereco();
		endereco.setCep(limparNumeros(camposMascara.get(5).getText()));
		endereco.setRua(camposTexto.get(2).getText());
		endereco.setNumero(camposTexto.get(3).getText());
		endereco.setComplemento(camposTexto.get(4).getText());
		endereco.setBairro(camposTexto.get(5).getText());
		endereco.setCidade(camposTexto.get(6).getText());
		endereco.setEstado(estado);
		return endereco;
	}

	private Turma turmaSelecionada() {
		Object valor = combosTexto.get(2).getSelectedItem();

		if (valor == null) {
			return null;
		}
		String selecionada = valor.toString();

		for (Turma turma : turmas) {
			String item = turma.getIdTurma() + " - " + turma.getDescricaoTurma();

			if (item.equals(selecionada)) {
				return turma;
			}
		}
		return null;
	}

	private Sexo sexoSelecionado() {
		Object valor = combosTexto.get(1).getSelectedItem();

		if (valor == null) {
			throw new IllegalArgumentException("Selecione o sexo do aluno.");
		}
		String sexo = valor.toString();
		if ("Masculino".equals(sexo)) {
			return Sexo.MASCULINO;
		}
		if ("Feminino".equals(sexo)) {
			return Sexo.FEMININO;
		}
		if ("Outros".equals(sexo)) {
			return Sexo.OUTRO;
		}
		throw new IllegalArgumentException("Selecione o sexo do aluno.");
	}

	private LocalDate parseData(String texto) {
		return LocalDate.parse(texto.trim(), FORMATO_DATA);
	}

	private String limparNumeros(String texto) {
		if (texto == null) {
			return "";
		}
		return texto.replaceAll("\\D", "");
	}
	
	private void limparCampos() {
		for (JTextField campo : camposTexto) {
			campo.setText("");
		}
		for (JFormattedTextField campo : camposMascara) {
			campo.setValue(null);
		}
		for (JComboBox<String> combo : combosTexto) {
			if (combo.getItemCount() > 0) {
				combo.setSelectedIndex(0);
			} else {
				combo.setSelectedIndex(-1);
			}
		}
		if (comboEstadoCadastro != null) {
			comboEstadoCadastro.setSelectedIndex(-1);
		}
		if (campoObservacoes != null) {
			campoObservacoes.setText("");
		}
		gerarMatriculaProvisoria();
	}

	private void mostrarErro(String mensagem) {
		JOptionPane.showMessageDialog(this, mensagem == null || mensagem.isBlank() ? "Dados inválidos." : mensagem,
				"Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 15));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private static class PainelRolavel extends JPanel implements javax.swing.Scrollable {
		private static final long serialVersionUID = 1L;

		public PainelRolavel() {
			super(new GridBagLayout());
			setOpaque(true);
		}
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(900, 500);
		}
		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 20;
		}
		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			if (orientation == javax.swing.SwingConstants.VERTICAL) {
				return Math.max(50, visibleRect.height - 20);
			}
			return Math.max(50, visibleRect.width - 20);
		}
		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}
}