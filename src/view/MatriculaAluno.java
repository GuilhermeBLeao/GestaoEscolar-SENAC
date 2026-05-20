// Guilherme

package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.text.ParseException;

import variaveisEnum.Estado;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.MaskFormatter;
import javax.swing.JCheckBox;

public class MatriculaAluno extends JFrame {
	private static final long serialVersionUID = 1L;

	// Definição das cores da interface
	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	// Variáveis responsáveis pelo tamanho interno da tela
	private final int larguraInterno, alturaInterno, margem;

	// Método principal responsável por iniciar a aplicação
	public static void main(String[] args) {

		EventQueue.invokeLater(() -> {

			try {

				MatriculaAluno frame = new MatriculaAluno();
				frame.setVisible(true);

			} catch (Exception e) {

				e.printStackTrace();
			}
		});
	}

	// Construtor responsável por criar toda a interface principal
	public MatriculaAluno() {
		// Configurações do JFrame
		setTitle("Matricula Aluno");
		setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Matricular.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(false);
		setMinimumSize(new Dimension(1200, 720));

		// Impede redimensionamento da tela
		setResizable(false);

		// Painel externo
		JPanel externo = new JPanel();
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);
		externo.setLayout(null);

		// Painel interno
		JPanel interno = new JPanel();
		margem = 30;
		larguraInterno = areaUtil.width - (margem * 2) + 20;
		alturaInterno = areaUtil.height - (margem * 2);
		interno.setBounds(20, 20, larguraInterno, alturaInterno);
		interno.setBackground(corInterna);
		interno.setLayout(null);
		externo.add(interno);

		// Título principal
		JLabel lblTitulo = new JLabel("MATRÍCULA DO ALUNO");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 50));
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 10, larguraInterno, 50);
		lblTitulo.setForeground(textos);
		interno.add(lblTitulo);

		// Texto secundário
		JLabel lblInstrucao = new JLabel("Preencha as informações do aluno.");
		lblInstrucao.setFont(new Font("Tahoma", Font.BOLD, 25));
		lblInstrucao.setHorizontalAlignment(SwingConstants.CENTER);
		lblInstrucao.setBounds(0, 100, larguraInterno, 25);
		lblInstrucao.setForeground(textos);
		interno.add(lblInstrucao);

		// Linha separadora
		JSeparator linha = new JSeparator();
		linha.setBounds(10, 140, larguraInterno - 40, 1);
		linha.setForeground(corBorda);
		interno.add(linha);



		// Criação do JTabbedPane
		JTabbedPane abas = new JTabbedPane();
		abas.setTabPlacement(JTabbedPane.TOP);
		abas.setBounds(10, 165, larguraInterno - 40, 650);
		abas.setFont(new Font("Arial", Font.BOLD, 16));
		abas.setBackground(corExterna);
		abas.setForeground(Color.WHITE);
		
		abas.setUI(new BasicTabbedPaneUI() {

            @Override
            protected LayoutManager createLayoutManager() {
                return new TabbedPaneLayout() {

                    @Override
                    protected void calculateTabRects(int tabPlacement, int tabCount) {
                        super.calculateTabRects(tabPlacement, tabCount);

                        int totalWidth = 0;
                        
                        for (int i = 0; i < rects.length; i++) {
                            totalWidth += rects[i].width;
                        }

                        int margem = (abas.getWidth() - totalWidth) / 2;

                        for (int i = 0; i < rects.length; i++) {
                            rects[i].x += margem;
                        }
                    }
                };
            }
        });
		
		interno.add(abas);
		

		// Criação das abas
		JPanel abaAluno = criarAba();
		JPanel abaEndereco = criarAba();
		JPanel abaPais = criarAba();

		// Montagem das abas
		montarAbaAluno(abaAluno);
		montarAbaEndereco(abaEndereco);
		montarAbaPais(abaPais);

		// Adiciona abas no JTabbedPane
		abas.addTab("Aluno", abaAluno);
		abas.addTab("Endereço", abaEndereco);
		abas.addTab("Pais/Responsáveis", abaPais);

		// Botão matricular
		JButton btnMatricular = new JButton("Matricular Aluno");
		btnMatricular.setBounds((larguraInterno / 2) + 130, 850, 320, 50);
		botaoPrincipal(btnMatricular);
		interno.add(btnMatricular);

		// Botão cancelar
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.setBounds((larguraInterno / 2) - 170, 850, 240, 50);
		botaoSecundario(btnCancelar);
		interno.add(btnCancelar);

		// Botão limpar
		JButton btnLimpar = new JButton("Limpar Campos");
		btnLimpar.setBounds((larguraInterno / 2) - 550, 850, 320, 50);
		botaoTerciario(btnLimpar);
		interno.add(btnLimpar);
	}

	// Método responsável por criar uma aba padrão
	private JPanel criarAba() {
		JPanel painel = new JPanel(null);
		painel.setBackground(corExterna);
		return painel;
	}

	// Método responsável por criar labels padronizadas
	private void label(JPanel painel, String texto, int x, int y) {
		JLabel label = new JLabel(texto);
		label.setForeground(corLabel);
		label.setFont(new Font("Arial", Font.BOLD, 15));
		label.setBounds(x, y, 280, 22);
		painel.add(label);
	}

	// Método responsável por criar campos com máscara
	private JFormattedTextField campoMascara(JPanel painel, String mascara, int x, int y, int largura) {
		try {
			MaskFormatter formatter = new MaskFormatter(mascara);
			formatter.setPlaceholderCharacter(' ');
			JFormattedTextField campo = new JFormattedTextField(formatter);
			campo.setBounds(x, y, largura, 48);
			campo.setFont(new Font("Arial", Font.PLAIN, 16));
			campo.setForeground(textos);
			campo.setBackground(corCampo);
			campo.setCaretColor(Color.WHITE);
			campo.setBorder(new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 15, 0, 15)));
			painel.add(campo);
			return campo;

		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Método responsável por criar campos de texto padronizados
	private JTextField campo(JPanel painel, String dica, int x, int y, int largura) {
		JTextField campo = new JTextField();
		campo.setBounds(x, y, largura, 48);
		campo.setFont(new Font("Arial", Font.PLAIN, 16));
		campo.setForeground(Color.WHITE);
		campo.setBackground(corCampo);
		campo.setCaretColor(Color.WHITE);
		campo.setToolTipText(dica);
		campo.setBorder(new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 15, 0, 15)));
		painel.add(campo);
		return campo;
	}

	// Método responsável por criar JComboBox padronizados
	private JComboBox<String> combo(JPanel painel, String[] itens, int x, int y, int largura) {
		JComboBox<String> combo = new JComboBox<>(itens);
		combo.setBounds(x, y, largura, 48);
		combo.setFont(new Font("Arial", Font.PLAIN, 16));
		combo.setForeground(Color.WHITE);
		combo.setBackground(corCampo);
		combo.setBorder(new LineBorder(corBorda, 1, true));
		combo.setFocusable(false);
		painel.add(combo);
		return combo;
	}

	private JComboBox<Estado> comboEstado(JPanel painel, int x, int y, int largura) {
		JComboBox<Estado> cbEstado = new JComboBox<>(Estado.values());
		cbEstado.setBounds(x, y, largura, 48);
		cbEstado.setFont(new Font("Arial", Font.PLAIN, 16));
		cbEstado.setForeground(Color.WHITE);
		cbEstado.setBackground(corCampo);
		cbEstado.setBorder(new LineBorder(corBorda, 1, true));
		cbEstado.setFocusable(false);
		cbEstado.setSelectedIndex(-1);
		painel.add(cbEstado);
		return cbEstado;
	}

	// Método responsável por estilizar o botão principal
	private void botaoPrincipal(JButton botao) {
		botao.setFont(new Font("Arial", Font.BOLD, 16));
		botao.setForeground(textos);
		botao.setBackground(corExterna);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	// Método responsável por estilizar o botão secundário
	private void botaoSecundario(JButton botao) {
		botao.setFont(new Font("Arial", Font.BOLD, 16));
		botao.setForeground(textos);
		botao.setBackground(corExterna);
		botao.setFocusPainted(false);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	// Método responsável por estilizar o botão limpar campos
	private void botaoTerciario(JButton botao) {
		botao.setFont(new Font("Arial", Font.BOLD, 16));
		botao.setForeground(textos);
		botao.setBackground(corExterna);
		botao.setFocusPainted(false);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	// Método responsável por montar todos os componentes da aba aluno
	private void montarAbaAluno(JPanel abaAluno) {
		// Largura total dos componentes
		int larguraConteudo = 1320;

		// Centralização horizontal
		int deslocamentoX = (larguraInterno - larguraConteudo) / 2 - 40;

		// Matrícula
		label(abaAluno, "Matrícula *", 40 + deslocamentoX, 35);
		campoMascara(abaAluno, "##########", 40 + deslocamentoX, 60, 250);

		// Nome
		label(abaAluno, "Nome completo *", 330 + deslocamentoX, 35);
		campo(abaAluno, "Digite o nome completo", 330 + deslocamentoX, 60, 600);

		// Status
		label(abaAluno, "Ativo *", 970 + deslocamentoX, 35);
		combo(abaAluno, new String[] { "Sim", "Não" }, 970 + deslocamentoX, 60, 180);

		// CPF
		label(abaAluno, "CPF *", 40 + deslocamentoX, 140);
		campoMascara(abaAluno, "###.###.###-##", 40 + deslocamentoX, 165, 250);

		// RG
		label(abaAluno, "RG", 330 + deslocamentoX, 140);
		campoMascara(abaAluno, "###########", 330 + deslocamentoX, 165, 250);

		// Telefone
		label(abaAluno, "Telefone *", 620 + deslocamentoX, 140);
		campoMascara(abaAluno, "(##) #####-####", 620 + deslocamentoX, 165, 280);

		// Sexo
		label(abaAluno, "Sexo *", 940 + deslocamentoX, 140);
		combo(abaAluno, new String[] { "Selecione", "Masculino", "Feminino", "Outros" }, 940 + deslocamentoX, 165, 250);

		// E-mail
		label(abaAluno, "E-mail *", 40 + deslocamentoX, 245);
		campo(abaAluno, "Digite o e-mail", 40 + deslocamentoX, 270, 620);

		// Data de nascimento
		label(abaAluno, "Data de nascimento *", 700 + deslocamentoX, 245);
		campoMascara(abaAluno, "##/##/####", 700 + deslocamentoX, 270, 230);

		// Data de cadastro
		label(abaAluno, "Data de cadastro *", 970 + deslocamentoX, 245);
		campoMascara(abaAluno, "##/##/####", 970 + deslocamentoX, 270, 230);

		// Turma
		label(abaAluno, "Turma *", 40 + deslocamentoX, 350);
		combo(abaAluno, new String[] { "Selecione a turma" }, 40 + deslocamentoX, 375, 420);

		// Situação
		label(abaAluno, "Situação *", 500 + deslocamentoX, 350);
		combo(abaAluno, new String[] { "Selecione", "CURSANDO", "TRANCADO", "TRANSFERIDO", "CONCLUIDO" },
				500 + deslocamentoX, 375, 360);

		// Observações saúde
		label(abaAluno, "Observações de saúde", 40 + deslocamentoX, 460);

		// Área de texto
		JTextArea obs = new JTextArea();
		obs.setFont(new Font("Arial", Font.PLAIN, 16));
		obs.setForeground(textos);
		obs.setBackground(corCampo);
		obs.setCaretColor(Color.WHITE);
		obs.setLineWrap(true);
		obs.setWrapStyleWord(true);
		obs.setMargin(new Insets(10, 10, 10, 10));

		// Scroll da área de texto
		JScrollPane scrollObs = new JScrollPane(obs);

		scrollObs.setBounds(40 + deslocamentoX, 485, 1320, 110);
		scrollObs.setBorder(new LineBorder(corBorda, 1, true));

		abaAluno.add(scrollObs);

		// =========================
		// DOCUMENTOS DO ALUNO
		// =========================

		JPanel painelDocumentos = new JPanel();
		painelDocumentos.setLayout(null);
		painelDocumentos.setBounds(920 + deslocamentoX, 335, 280, 140);

		painelDocumentos.setBackground(corExterna);

		painelDocumentos.setBorder(
		    BorderFactory.createLineBorder(corBorda, 1, true)
		);

		abaAluno.add(painelDocumentos);

		// Título opcional
		JLabel lblDocs = new JLabel("Documentos");
		lblDocs.setBounds(15, 5, 200, 25);
		lblDocs.setForeground(textos);
		lblDocs.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocumentos.add(lblDocs);

		// RG
		JCheckBox chckbxRGAluno = new JCheckBox("RG do Aluno");
		chckbxRGAluno.setBounds(15, 35, 220, 30);
		chckbxRGAluno.setBackground(corExterna);
		chckbxRGAluno.setForeground(textos);
		chckbxRGAluno.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocumentos.add(chckbxRGAluno);

		// CPF
		JCheckBox chckbxCPFAluno = new JCheckBox("CPF do Aluno");
		chckbxCPFAluno.setBounds(15, 65, 220, 30);
		chckbxCPFAluno.setBackground(corExterna);
		chckbxCPFAluno.setForeground(textos);
		chckbxCPFAluno.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocumentos.add(chckbxCPFAluno);

		// Comprovante
		JCheckBox chckbxComprovante = new JCheckBox("Comprovante de Residência");
		chckbxComprovante.setBounds(15, 95, 245, 30);
		chckbxComprovante.setBackground(corExterna);
		chckbxComprovante.setForeground(textos);
		chckbxComprovante.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocumentos.add(chckbxComprovante);
	}

	// Método responsável por montar os componentes da aba endereço
	private void montarAbaEndereco(JPanel abaEndereco) {

		// Largura total dos componentes
		int larguraConteudo = 1320;

		// Centralização horizontal
		int deslocamentoX = (larguraInterno - larguraConteudo) / 2 - 40;

		// Centralização vertical
		int inicioY = 140;

		// =========================
		// PRIMEIRA LINHA
		// =========================

		// CEP
		label(abaEndereco, "CEP *", 40 + deslocamentoX, inicioY);
		campoMascara(abaEndereco, "#####-###", 40 + deslocamentoX, inicioY + 25, 220);

		// Rua
		label(abaEndereco, "Rua *", 300 + deslocamentoX, inicioY);
		campo(abaEndereco, "Digite o nome da rua", 300 + deslocamentoX, inicioY + 25, 620);

		// Número
		label(abaEndereco, "Número *", 960 + deslocamentoX, inicioY);
		campo(abaEndereco, "Digite o número", 960 + deslocamentoX, inicioY + 25, 180);

		// =========================
		// SEGUNDA LINHA
		// =========================

		int segundaLinhaY = inicioY + 120;

		// Complemento
		label(abaEndereco, "Complemento", 40 + deslocamentoX, segundaLinhaY);
		campo(abaEndereco, "Digite o complemento", 40 + deslocamentoX, segundaLinhaY + 25, 280);

		// Bairro
		label(abaEndereco, "Bairro *", 360 + deslocamentoX, segundaLinhaY);
		campo(abaEndereco, "Digite o bairro", 360 + deslocamentoX, segundaLinhaY + 25, 280);

		// Cidade
		label(abaEndereco, "Cidade *", 680 + deslocamentoX, segundaLinhaY);
		campo(abaEndereco, "Digite a cidade", 680 + deslocamentoX, segundaLinhaY + 25, 280);

		// Estado
		label(abaEndereco, "Estado *", 1000 + deslocamentoX, segundaLinhaY);
		comboEstado(abaEndereco, 1000 + deslocamentoX, segundaLinhaY + 25, 180);
	}

	// Método responsável por montar os componentes da aba pais/responsáveis
	private void montarAbaPais(JPanel abaPais) {
		// Largura total dos componentes
		int larguraConteudo = 1320;

		// Centralização horizontal
		int deslocamentoX = (larguraInterno - larguraConteudo) / 2 - 40;

		JLabel lblMae = new JLabel("Dados da Mãe");
		lblMae.setForeground(corLabel);
		lblMae.setFont(new Font("Arial", Font.BOLD, 35));
		lblMae.setBounds(400, 55, 500, 50);
		abaPais.add(lblMae);

		// Nome da mãe
		label(abaPais, "Nome completo da mãe", 40 + deslocamentoX, 135);
		campo(abaPais, "Digite o nome completo da mãe", 40 + deslocamentoX, 160, 250);

		// CPF da mãe
		label(abaPais, "CPF da mãe", 40 + deslocamentoX, 240);
		campoMascara(abaPais, "###.###.###-##", 40 + deslocamentoX, 265, 250);

		// E-mail da mãe
		label(abaPais, "E-mail da mãe", 330 + deslocamentoX, 240);
		campo(abaPais, "Digite o e-mail", 330 + deslocamentoX, 265, 300);

		// Telefone da mãe
		label(abaPais, "Telefone da mãe", 330 + deslocamentoX, 135);
		campoMascara(abaPais, "(##) #####-####", 330 + deslocamentoX, 160, 250);

		// Linha separadora
		JSeparator linha = new JSeparator();
		linha.setBounds(10, 350, larguraInterno - 70, 1);
		linha.setForeground(corBorda);
		abaPais.add(linha);

		// Label Pai
		JLabel lblPai = new JLabel("Dados do Pai");
		lblPai.setForeground(corLabel);
		lblPai.setFont(new Font("Arial", Font.BOLD, 35));
		lblPai.setBounds(400, 365, 500, 50);
		abaPais.add(lblPai);

		// Nome do pai
		label(abaPais, "Nome completo do pai", 40 + deslocamentoX, 430);
		campo(abaPais, "Digite o nome completo do pai", 40 + deslocamentoX, 455, 250);

		// CPF do pai
		label(abaPais, "CPF do pai", 40 + deslocamentoX, 520);
		campoMascara(abaPais, "###.###.###-##", 40 + deslocamentoX, 545, 250);

		// E-mail do pai
		label(abaPais, "E-mail do pai", 330 + deslocamentoX, 520);
		campo(abaPais, "Digite o e-mail", 330 + deslocamentoX, 545, 300);

		// Telefone do pai
		label(abaPais, "Telefone do pai", 330 + deslocamentoX, 430);
		campoMascara(abaPais, "(##) #####-####", 330 + deslocamentoX, 455, 250);

		// =========================
		// DOCUMENTOS DA MÃE
		// =========================

		JPanel painelDocsMae = new JPanel();
		painelDocsMae.setLayout(null);
		painelDocsMae.setBounds(680 + deslocamentoX, 145, 200, 120);

		painelDocsMae.setBackground(corExterna);

		painelDocsMae.setBorder(
		    BorderFactory.createLineBorder(corBorda, 1, true)
		);

		abaPais.add(painelDocsMae);

		// Título
		JLabel lblDocsMae = new JLabel("Documentos da Mãe");
		lblDocsMae.setBounds(15, 5, 200, 25);
		lblDocsMae.setForeground(textos);
		lblDocsMae.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocsMae.add(lblDocsMae);

		// CPF da mãe
		JCheckBox chckbxCPFMae = new JCheckBox("CPF da Mãe");
		chckbxCPFMae.setBounds(15, 35, 170, 30);
		chckbxCPFMae.setBackground(corExterna);
		chckbxCPFMae.setForeground(textos);
		chckbxCPFMae.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocsMae.add(chckbxCPFMae);

		// RG da mãe
		JCheckBox chckbxRGMae = new JCheckBox("RG da Mãe");
		chckbxRGMae.setBounds(15, 70, 170, 30);
		chckbxRGMae.setBackground(corExterna);
		chckbxRGMae.setForeground(textos);
		chckbxRGMae.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocsMae.add(chckbxRGMae);


		// =========================
		// DOCUMENTOS DO PAI
		// =========================

		JPanel painelDocsPai = new JPanel();
		painelDocsPai.setLayout(null);
		painelDocsPai.setBounds(680 + deslocamentoX, 440, 210, 120);

		painelDocsPai.setBackground(corExterna);

		painelDocsPai.setBorder(
		    BorderFactory.createLineBorder(corBorda, 1, true)
		);

		abaPais.add(painelDocsPai);

		// Título
		JLabel lblDocsPai = new JLabel("Documentos do Pai");
		lblDocsPai.setBounds(15, 5, 200, 25);
		lblDocsPai.setForeground(textos);
		lblDocsPai.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocsPai.add(lblDocsPai);

		// CPF do pai
		JCheckBox chckbxCPFPai = new JCheckBox("CPF do Pai");
		chckbxCPFPai.setBounds(15, 35, 170, 30);
		chckbxCPFPai.setBackground(corExterna);
		chckbxCPFPai.setForeground(textos);
		chckbxCPFPai.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocsPai.add(chckbxCPFPai);

		// RG do pai
		JCheckBox chckbxRGPai = new JCheckBox("RG do Pai");
		chckbxRGPai.setBounds(15, 70, 170, 30);
		chckbxRGPai.setBackground(corExterna);
		chckbxRGPai.setForeground(textos);
		chckbxRGPai.setFont(new Font("Arial", Font.BOLD, 16));

		painelDocsPai.add(chckbxRGPai);
	}
}
