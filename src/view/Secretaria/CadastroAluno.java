package view.Secretaria;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

import controller.AlunoController;
import controller.PaisAlunoController;
import controller.TurmaController;
import controller.UsuarioController;
import model.Aluno;
import model.Endereco;
import model.PaisAluno;
import model.Turma;
import model.Usuario;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;
import variaveisEnum.SituacaoAluno;
import variaveisEnum.TipoUsuario;
import view.JBase;

public class CadastroAluno extends JBase {
  private static final long serialVersionUID = 1L;

  private final int larguraInterno, alturaInterno;
  private static final String SENHA_PADRAO_USUARIO = "Senha@123";
  private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private JFormattedTextField txtMatricula;
  private JTextField txtNomeAluno;
  private JComboBox<String> cbAtivo;
  private JFormattedTextField txtCpfAluno;
  private JFormattedTextField txtRgAluno;
  private JFormattedTextField txtTelefoneAluno;
  private JComboBox<String> cbSexo;
  private JTextField txtEmailAluno;
  private JFormattedTextField txtDataNascimento;
  private JFormattedTextField txtDataCadastro;
  private JComboBox<TurmaItem> cbTurma;
  private JComboBox<String> cbSituacao;
  private JTextArea txtObsSaude;

  private JFormattedTextField txtCep;
  private JTextField txtRua;
  private JTextField txtNumero;
  private JTextField txtComplemento;
  private JTextField txtBairro;
  private JTextField txtCidade;
  private JComboBox<Estado> cbEstado;

  private JTextField txtNomeMae;
  private JFormattedTextField txtCpfMae;
  private JTextField txtEmailMae;
  private JFormattedTextField txtTelefoneMae;
  private JTextField txtNomePai;
  private JFormattedTextField txtCpfPai;
  private JTextField txtEmailPai;
  private JFormattedTextField txtTelefonePai;

  public CadastroAluno() {
    setTitle("Matricula Aluno");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Cadastro de Aluno.png"));
    setResizable(false);

    JPanel externo = new JPanel();
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    externo.setLayout(null);
    setContentPane(externo);

    JPanel interno = new JPanel();
    larguraInterno = getLarguraInterna();
    alturaInterno = getAlturaInterna();
    interno.setBounds(20, 20, larguraInterno, alturaInterno);
    interno.setBackground(corInterna);
    interno.setLayout(null);
    externo.add(interno);

    JLabel lblTitulo = new JLabel("MATRÍCULA DO ALUNO");
    lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 50));
    lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
    lblTitulo.setBounds(0, 10, larguraInterno, 50);
    lblTitulo.setForeground(textos);
    interno.add(lblTitulo);

    JLabel lblInstrucao = new JLabel("Preencha as informações do aluno.");
    lblInstrucao.setFont(new Font("Tahoma", Font.BOLD, 25));
    lblInstrucao.setHorizontalAlignment(SwingConstants.CENTER);
    lblInstrucao.setBounds(0, 100, larguraInterno, 25);
    lblInstrucao.setForeground(textos);
    interno.add(lblInstrucao);

    JSeparator linha = new JSeparator();
    linha.setBounds(10, 140, larguraInterno - 40, 1);
    linha.setForeground(corBorda);
    interno.add(linha);

    JTabbedPane abas = new JTabbedPane();
    abas.setTabPlacement(JTabbedPane.TOP);
    abas.setBounds(10, 165, larguraInterno - 40, 650);
    abas.setFont(new Font("Arial", Font.BOLD, 16));
    abas.setBackground(corExterna);
    abas.setForeground(textos);
    abas.setUI(
        new BasicTabbedPaneUI() {
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

    JPanel abaAluno = criarAba();
    JPanel abaEndereco = criarAba();
    JPanel abaPais = criarAba();

    montarAbaAluno(abaAluno);
    montarAbaEndereco(abaEndereco);
    montarAbaPais(abaPais);

    abas.addTab("Aluno", abaAluno);
    abas.addTab("Endereço", abaEndereco);
    abas.addTab("Pais/Responsáveis", abaPais);

    int yBotao = 850;

    JButton btnMatricular = new JButton("Matricular Aluno");
    btnMatricular.setBounds((larguraInterno / 2) + 130, yBotao, 320, 50);
    btnMatricular.addActionListener(e -> matricularAluno());
    estilizarBotao(btnMatricular);
    interno.add(btnMatricular);

    JButton btnCancelar = new JButton("Cancelar");
    btnCancelar.setBounds((larguraInterno / 2) - 170, yBotao, 240, 50);
    btnCancelar.addActionListener(e -> dispose());
    estilizarBotao(btnCancelar);
    interno.add(btnCancelar);

    JButton btnLimpar = new JButton("Limpar Campos");
    btnLimpar.setBounds((larguraInterno / 2) - 550, yBotao, 320, 50);
    btnLimpar.addActionListener(e -> limparCampos());
    estilizarBotao(btnLimpar);
    interno.add(btnLimpar);
  }

  private JPanel criarAba() {
    JPanel painel = new JPanel(null);
    painel.setBackground(corExterna);
    return painel;
  }

  private void label(JPanel painel, String texto, int x, int y) {
    JLabel label = new JLabel(texto);
    label.setForeground(corLabel);
    label.setFont(new Font("Arial", Font.BOLD, 15));
    label.setBounds(x, y, 280, 22);
    painel.add(label);
  }

  private JFormattedTextField campoMascara(
      JPanel painel, String mascara, int x, int y, int largura) {
    try {
      MaskFormatter formatter = new MaskFormatter(mascara);
      formatter.setPlaceholderCharacter(' ');
      JFormattedTextField campo = new JFormattedTextField(formatter);
      campo.setBounds(x, y, largura, 48);
      campo.setFont(new Font("Arial", Font.PLAIN, 16));
      campo.setForeground(textos);
      campo.setBackground(corCampo);
      campo.setCaretColor(Color.WHITE);
      campo.setBorder(
          new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 15, 0, 15)));
      painel.add(campo);
      return campo;

    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  private JTextField campo(JPanel painel, String dica, int x, int y, int largura) {
    JTextField campo = new JTextField();
    campo.setBounds(x, y, largura, 48);
    campo.setFont(new Font("Arial", Font.PLAIN, 16));
    campo.setForeground(Color.WHITE);
    campo.setBackground(corCampo);
    campo.setCaretColor(Color.WHITE);
    campo.setToolTipText(dica);
    campo.setBorder(
        new CompoundBorder(new LineBorder(corBorda, 1, true), new EmptyBorder(0, 15, 0, 15)));
    painel.add(campo);
    return campo;
  }

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

  private JComboBox<TurmaItem> comboTurmas(JPanel painel, int x, int y, int largura) {
    JComboBox<TurmaItem> combo = new JComboBox<>();
    combo.setBounds(x, y, largura, 48);
    combo.setFont(new Font("Arial", Font.PLAIN, 16));
    combo.setForeground(Color.WHITE);
    combo.setBackground(corCampo);
    combo.setBorder(new LineBorder(corBorda, 1, true));
    combo.setFocusable(false);
    combo.addItem(new TurmaItem(0, "Selecione a turma"));

    try {
      List<Turma> turmas = new TurmaController().listarTurmasAtivas();
      for (Turma turma : turmas) {
        combo.addItem(new TurmaItem(turma.getIdTurma(), turma.getDescricaoTurma()));
      }
    } catch (RuntimeException e) {
      JOptionPane.showMessageDialog(
          this,
          "NÃ£o foi possÃ­vel carregar as turmas: " + e.getMessage(),
          "Turmas",
          JOptionPane.WARNING_MESSAGE);
    }

    painel.add(combo);
    return combo;
  }

  private void montarAbaAluno(JPanel abaAluno) {
    int larguraConteudo = 1320;

    int deslocamentoX = (larguraInterno - larguraConteudo) / 2 - 40;

    label(abaAluno, "Matrícula *", 40 + deslocamentoX, 35);
    txtMatricula = campoMascara(abaAluno, "##########", 40 + deslocamentoX, 60, 250);
    txtMatricula.setEnabled(false);
    txtMatricula.setToolTipText("Gerada automaticamente ao salvar no banco");

    label(abaAluno, "Nome completo *", 330 + deslocamentoX, 35);
    txtNomeAluno = campo(abaAluno, "Digite o nome completo", 330 + deslocamentoX, 60, 600);

    label(abaAluno, "Ativo *", 970 + deslocamentoX, 35);
    cbAtivo = combo(abaAluno, new String[] {"Sim", "Não"}, 970 + deslocamentoX, 60, 180);

    label(abaAluno, "CPF *", 40 + deslocamentoX, 140);
    txtCpfAluno = campoMascara(abaAluno, "###.###.###-##", 40 + deslocamentoX, 165, 250);

    label(abaAluno, "RG", 330 + deslocamentoX, 140);
    txtRgAluno = campoMascara(abaAluno, "###########", 330 + deslocamentoX, 165, 250);

    label(abaAluno, "Telefone *", 620 + deslocamentoX, 140);
    txtTelefoneAluno = campoMascara(abaAluno, "(##) #####-####", 620 + deslocamentoX, 165, 280);

    label(abaAluno, "Sexo *", 940 + deslocamentoX, 140);
    cbSexo =
        combo(
            abaAluno,
            new String[] {"Selecione", "Masculino", "Feminino", "Outros"},
            940 + deslocamentoX,
            165,
            250);

    label(abaAluno, "E-mail *", 40 + deslocamentoX, 245);
    txtEmailAluno = campo(abaAluno, "Digite o e-mail", 40 + deslocamentoX, 270, 620);

    label(abaAluno, "Data de nascimento *", 700 + deslocamentoX, 245);
    txtDataNascimento = campoMascara(abaAluno, "##/##/####", 700 + deslocamentoX, 270, 230);

    label(abaAluno, "Data de cadastro *", 970 + deslocamentoX, 245);
    txtDataCadastro = campoMascara(abaAluno, "##/##/####", 970 + deslocamentoX, 270, 230);
    txtDataCadastro.setText(LocalDate.now().format(DATA_BR));
    txtDataCadastro.setEnabled(false);

    label(abaAluno, "Turma *", 40 + deslocamentoX, 350);
    cbTurma = comboTurmas(abaAluno, 40 + deslocamentoX, 375, 420);

    label(abaAluno, "Situação *", 500 + deslocamentoX, 350);
    cbSituacao =
        combo(
            abaAluno,
            new String[] {"Selecione", "ATIVO", "TRANCADO", "TRANSFERIDO", "CONCLUIDO"},
            500 + deslocamentoX,
            375,
            360);

    label(abaAluno, "Observações de saúde", 40 + deslocamentoX, 460);

    txtObsSaude = new JTextArea();
    txtObsSaude.setFont(new Font("Arial", Font.PLAIN, 16));
    txtObsSaude.setForeground(textos);
    txtObsSaude.setBackground(corCampo);
    txtObsSaude.setCaretColor(Color.WHITE);
    txtObsSaude.setLineWrap(true);
    txtObsSaude.setWrapStyleWord(true);
    txtObsSaude.setMargin(new Insets(10, 10, 10, 10));

    JScrollPane scrollObs = new JScrollPane(txtObsSaude);

    scrollObs.setBounds(40 + deslocamentoX, 485, 1320, 110);
    scrollObs.setBorder(new LineBorder(corBorda, 1, true));
    scrollObs.getVerticalScrollBar().setUnitIncrement(26);
    abaAluno.add(scrollObs);

    JPanel painelDocumentos = new JPanel();
    painelDocumentos.setLayout(null);
    painelDocumentos.setBounds(920 + deslocamentoX, 335, 280, 140);
    painelDocumentos.setBackground(corExterna);
    painelDocumentos.setBorder(BorderFactory.createLineBorder(corBorda, 1, true));
    abaAluno.add(painelDocumentos);

    JLabel lblDocs = new JLabel("Documentos");
    lblDocs.setBounds(15, 5, 200, 25);
    lblDocs.setForeground(textos);
    lblDocs.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocumentos.add(lblDocs);

    JCheckBox chckbxRGAluno = new JCheckBox("RG do Aluno");
    chckbxRGAluno.setBounds(15, 35, 220, 30);
    chckbxRGAluno.setBackground(corExterna);
    chckbxRGAluno.setForeground(textos);
    chckbxRGAluno.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocumentos.add(chckbxRGAluno);

    JCheckBox chckbxCPFAluno = new JCheckBox("CPF do Aluno");
    chckbxCPFAluno.setBounds(15, 65, 220, 30);
    chckbxCPFAluno.setBackground(corExterna);
    chckbxCPFAluno.setForeground(textos);
    chckbxCPFAluno.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocumentos.add(chckbxCPFAluno);

    JCheckBox chckbxComprovante = new JCheckBox("Comprovante de Residência");
    chckbxComprovante.setBounds(15, 95, 245, 30);
    chckbxComprovante.setBackground(corExterna);
    chckbxComprovante.setForeground(textos);
    chckbxComprovante.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocumentos.add(chckbxComprovante);
  }

  private void montarAbaEndereco(JPanel abaEndereco) {
    int larguraConteudo = 1320;

    int deslocamentoX = (larguraInterno - larguraConteudo) / 2 - 40;

    int inicioY = 140;

    label(abaEndereco, "CEP *", 40 + deslocamentoX, inicioY);
    txtCep = campoMascara(abaEndereco, "#####-###", 40 + deslocamentoX, inicioY + 25, 220);

    label(abaEndereco, "Rua *", 300 + deslocamentoX, inicioY);
    txtRua = campo(abaEndereco, "Digite o nome da rua", 300 + deslocamentoX, inicioY + 25, 620);

    label(abaEndereco, "Número *", 960 + deslocamentoX, inicioY);
    txtNumero = campo(abaEndereco, "Digite o número", 960 + deslocamentoX, inicioY + 25, 180);

    int segundaLinhaY = inicioY + 120;

    label(abaEndereco, "Complemento", 40 + deslocamentoX, segundaLinhaY);
    txtComplemento =
        campo(abaEndereco, "Digite o complemento", 40 + deslocamentoX, segundaLinhaY + 25, 280);

    label(abaEndereco, "Bairro *", 360 + deslocamentoX, segundaLinhaY);
    txtBairro = campo(abaEndereco, "Digite o bairro", 360 + deslocamentoX, segundaLinhaY + 25, 280);

    label(abaEndereco, "Cidade *", 680 + deslocamentoX, segundaLinhaY);
    txtCidade = campo(abaEndereco, "Digite a cidade", 680 + deslocamentoX, segundaLinhaY + 25, 280);

    label(abaEndereco, "Estado *", 1000 + deslocamentoX, segundaLinhaY);
    cbEstado = comboEstado(abaEndereco, 1000 + deslocamentoX, segundaLinhaY + 25, 180);
  }

  private void montarAbaPais(JPanel abaPais) {
    int larguraConteudo = 1320;

    int deslocamentoX = (larguraInterno - larguraConteudo) / 2 - 40;

    JLabel lblMae = new JLabel("Dados da Mãe");
    lblMae.setForeground(corLabel);
    lblMae.setFont(new Font("Arial", Font.BOLD, 35));
    lblMae.setBounds(400, 55, 500, 50);
    abaPais.add(lblMae);

    label(abaPais, "Nome completo da mãe", 40 + deslocamentoX, 135);
    txtNomeMae = campo(abaPais, "Digite o nome completo da mãe", 40 + deslocamentoX, 160, 250);

    label(abaPais, "CPF da mãe", 40 + deslocamentoX, 240);
    txtCpfMae = campoMascara(abaPais, "###.###.###-##", 40 + deslocamentoX, 265, 250);

    label(abaPais, "E-mail da mãe", 330 + deslocamentoX, 240);
    txtEmailMae = campo(abaPais, "Digite o e-mail", 330 + deslocamentoX, 265, 300);

    label(abaPais, "Telefone da mãe", 330 + deslocamentoX, 135);
    txtTelefoneMae = campoMascara(abaPais, "(##) #####-####", 330 + deslocamentoX, 160, 250);

    JSeparator linha = new JSeparator();
    linha.setBounds(10, 350, larguraInterno - 70, 1);
    linha.setForeground(corBorda);
    abaPais.add(linha);

    JLabel lblPai = new JLabel("Dados do Pai");
    lblPai.setForeground(corLabel);
    lblPai.setFont(new Font("Arial", Font.BOLD, 35));
    lblPai.setBounds(400, 365, 500, 50);
    abaPais.add(lblPai);

    label(abaPais, "Nome completo do pai", 40 + deslocamentoX, 430);
    txtNomePai = campo(abaPais, "Digite o nome completo do pai", 40 + deslocamentoX, 455, 250);

    label(abaPais, "CPF do pai", 40 + deslocamentoX, 520);
    txtCpfPai = campoMascara(abaPais, "###.###.###-##", 40 + deslocamentoX, 545, 250);

    label(abaPais, "E-mail do pai", 330 + deslocamentoX, 520);
    txtEmailPai = campo(abaPais, "Digite o e-mail", 330 + deslocamentoX, 545, 300);

    label(abaPais, "Telefone do pai", 330 + deslocamentoX, 430);
    txtTelefonePai = campoMascara(abaPais, "(##) #####-####", 330 + deslocamentoX, 455, 250);

    JPanel painelDocsMae = new JPanel();
    painelDocsMae.setLayout(null);
    painelDocsMae.setBounds(680 + deslocamentoX, 145, 200, 120);
    painelDocsMae.setBackground(corExterna);
    painelDocsMae.setBorder(BorderFactory.createLineBorder(corBorda, 1, true));
    abaPais.add(painelDocsMae);

    JLabel lblDocsMae = new JLabel("Documentos da Mãe");
    lblDocsMae.setBounds(15, 5, 200, 25);
    lblDocsMae.setForeground(textos);
    lblDocsMae.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocsMae.add(lblDocsMae);

    JCheckBox chckbxCPFMae = new JCheckBox("CPF da Mãe");
    chckbxCPFMae.setBounds(15, 35, 170, 30);
    chckbxCPFMae.setBackground(corExterna);
    chckbxCPFMae.setForeground(textos);
    chckbxCPFMae.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocsMae.add(chckbxCPFMae);

    JCheckBox chckbxRGMae = new JCheckBox("RG da Mãe");
    chckbxRGMae.setBounds(15, 70, 170, 30);
    chckbxRGMae.setBackground(corExterna);
    chckbxRGMae.setForeground(textos);
    chckbxRGMae.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocsMae.add(chckbxRGMae);

    JPanel painelDocsPai = new JPanel();
    painelDocsPai.setLayout(null);
    painelDocsPai.setBounds(680 + deslocamentoX, 440, 210, 120);
    painelDocsPai.setBackground(corExterna);
    painelDocsPai.setBorder(BorderFactory.createLineBorder(corBorda, 1, true));
    abaPais.add(painelDocsPai);

    JLabel lblDocsPai = new JLabel("Documentos do Pai");
    lblDocsPai.setBounds(15, 5, 200, 25);
    lblDocsPai.setForeground(textos);
    lblDocsPai.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocsPai.add(lblDocsPai);

    JCheckBox chckbxCPFPai = new JCheckBox("CPF do Pai");
    chckbxCPFPai.setBounds(15, 35, 170, 30);
    chckbxCPFPai.setBackground(corExterna);
    chckbxCPFPai.setForeground(textos);
    chckbxCPFPai.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocsPai.add(chckbxCPFPai);

    JCheckBox chckbxRGPai = new JCheckBox("RG do Pai");
    chckbxRGPai.setBounds(15, 70, 170, 30);
    chckbxRGPai.setBackground(corExterna);
    chckbxRGPai.setForeground(textos);
    chckbxRGPai.setFont(new Font("Arial", Font.BOLD, 16));
    painelDocsPai.add(chckbxRGPai);
  }

  private void matricularAluno() {
    try {
      PaisAluno responsaveis = montarResponsaveis();
      PaisAlunoController paisController = new PaisAlunoController();
      PaisAluno responsaveisSalvos = buscarOuSalvarResponsaveis(paisController, responsaveis);

      Aluno aluno = montarAluno(responsaveisSalvos.getIdPais());
      AlunoController alunoController = new AlunoController();
      alunoController.matricularAluno(aluno);

      Aluno alunoSalvo = alunoController.buscarAlunoPorCpf(somenteDigitos(txtCpfAluno.getText()));
      criarUsuariosDaMatricula(alunoSalvo, responsaveisSalvos);

      JOptionPane.showMessageDialog(
          this,
          "Aluno matriculado com sucesso.\nMatrícula: "
              + alunoSalvo.getMatricula()
              + "\nSenha inicial dos usuários: "
              + SENHA_PADRAO_USUARIO,
          "Matrícula",
          JOptionPane.INFORMATION_MESSAGE);
      limparCampos();
    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(
          this, e.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
    } catch (RuntimeException e) {
      JOptionPane.showMessageDialog(
          this, mensagemErro(e), "Erro ao matricular", JOptionPane.ERROR_MESSAGE);
    }
  }

  private PaisAluno montarResponsaveis() {
    PaisAluno pais = new PaisAluno();
    pais.setNomeMae(texto(txtNomeMae));
    pais.setCpfMae(somenteDigitos(txtCpfMae.getText()));
    pais.setEmailMae(texto(txtEmailMae));
    pais.setTelefoneMae(somenteDigitos(txtTelefoneMae.getText()));
    pais.setNomePai(texto(txtNomePai));
    pais.setCpfPai(somenteDigitos(txtCpfPai.getText()));
    pais.setEmailPai(texto(txtEmailPai));
    pais.setTelefonePai(somenteDigitos(txtTelefonePai.getText()));
    return pais;
  }

  private PaisAluno buscarOuSalvarResponsaveis(PaisAlunoController controller, PaisAluno pais) {
    PaisAluno existente = null;
    if (!pais.getCpfMae().isBlank()) {
      existente = controller.buscarPaisAlunoPorCpfMae(pais.getCpfMae());
    }
    if (existente == null && !pais.getCpfPai().isBlank()) {
      existente = controller.buscarPaisAlunoPorCpfPai(pais.getCpfPai());
    }
    if (existente != null) {
      return existente;
    }

    controller.salvarPaisAluno(pais);
    PaisAluno salvo = controller.buscarPaisAlunoPorCpfMae(pais.getCpfMae());
    if (salvo == null) {
      salvo = controller.buscarPaisAlunoPorCpfPai(pais.getCpfPai());
    }
    if (salvo == null) {
      throw new IllegalStateException(
          "ResponsÃ¡veis foram salvos, mas nÃ£o puderam ser localizados.");
    }
    return salvo;
  }

  private Aluno montarAluno(int idPais) {
    TurmaItem turma = (TurmaItem) cbTurma.getSelectedItem();
    if (turma == null || turma.id <= 0) {
      throw new IllegalArgumentException("Selecione uma turma.");
    }

    Aluno aluno = new Aluno();
    aluno.setNome(texto(txtNomeAluno));
    aluno.setCpf(somenteDigitos(txtCpfAluno.getText()));
    aluno.setRg(somenteDigitos(txtRgAluno.getText()));
    aluno.setTelefone(somenteDigitos(txtTelefoneAluno.getText()));
    aluno.setEmail(texto(txtEmailAluno));
    aluno.setSexo(sexoSelecionado());
    aluno.setSituacao(situacaoSelecionada());
    aluno.setDataNascimento(data(txtDataNascimento.getText(), "Data de nascimento"));
    aluno.setObsSaude(texto(txtObsSaude));
    aluno.setIdPais(idPais);
    aluno.setIdTurma(turma.id);
    aluno.setAtivo("Sim".equals(cbAtivo.getSelectedItem()));

    Endereco endereco = new Endereco();
    endereco.setCep(somenteDigitos(txtCep.getText()));
    endereco.setRua(texto(txtRua));
    endereco.setNumero(texto(txtNumero));
    endereco.setComplemento(texto(txtComplemento));
    endereco.setBairro(texto(txtBairro));
    endereco.setCidade(texto(txtCidade));
    if (cbEstado.getSelectedItem() == null) {
      throw new IllegalArgumentException("Selecione o estado.");
    }
    endereco.setEstado((Estado) cbEstado.getSelectedItem());
    aluno.setEndereco(endereco);

    return aluno;
  }

  private void criarUsuariosDaMatricula(Aluno aluno, PaisAluno responsaveis) {
    UsuarioController usuarioController = new UsuarioController();
    criarUsuarioSeNaoExistir(
        usuarioController, aluno.getCpf(), TipoUsuario.ALUNO, aluno.getIdAluno(), 0, 0, 0);
    String cpfResponsavel =
        responsaveis.getCpfMae() != null && !responsaveis.getCpfMae().isBlank()
            ? responsaveis.getCpfMae()
            : responsaveis.getCpfPai();
    criarUsuarioSeNaoExistir(
        usuarioController,
        cpfResponsavel,
        TipoUsuario.RESPONSAVEL,
        0,
        0,
        responsaveis.getIdPais(),
        0);
  }

  private void criarUsuarioSeNaoExistir(
      UsuarioController controller,
      String cpf,
      TipoUsuario tipo,
      int alunoId,
      int funcionarioId,
      int paiId,
      int professorId) {
    if (cpf == null || cpf.isBlank() || usuarioJaExiste(controller, cpf)) {
      return;
    }
    Usuario usuario = new Usuario();
    usuario.setCpf(cpf);
    usuario.setTipoUsuario(tipo);
    usuario.setAlunoId(alunoId);
    usuario.setFuncionarioId(funcionarioId);
    usuario.setPaiId(paiId);
    usuario.setProfessorId(professorId);
    controller.cadastrarUsuario(usuario, SENHA_PADRAO_USUARIO);
  }

  private boolean usuarioJaExiste(UsuarioController controller, String cpf) {
    return controller.listarTodosUsuarios().stream()
        .anyMatch(usuario -> cpf.equals(usuario.getCpf()));
  }

  private Sexo sexoSelecionado() {
    String sexo = String.valueOf(cbSexo.getSelectedItem());
    switch (sexo) {
      case "Masculino":
        return Sexo.MASCULINO;
      case "Feminino":
        return Sexo.FEMININO;
      case "Outros":
        return Sexo.OUTRO;
      default:
        throw new IllegalArgumentException("Selecione o sexo.");
    }
  }

  private SituacaoAluno situacaoSelecionada() {
    String situacao = String.valueOf(cbSituacao.getSelectedItem());
    if ("Selecione".equals(situacao)) {
      throw new IllegalArgumentException("Selecione a situaÃ§Ã£o do aluno.");
    }
    return SituacaoAluno.valueOf(situacao);
  }

  private LocalDate data(String valor, String campo) {
    try {
      return LocalDate.parse(valor.trim(), DATA_BR);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(campo + " invÃ¡lida.");
    }
  }

  private String texto(JTextField campo) {
    return campo == null ? "" : campo.getText().trim();
  }

  private String texto(JTextArea campo) {
    return campo == null ? "" : campo.getText().trim();
  }

  private String somenteDigitos(String valor) {
    return valor == null ? "" : valor.replaceAll("\\D", "");
  }

  private String mensagemErro(Throwable e) {
    Throwable atual = e;
    while (atual.getCause() != null) {
      atual = atual.getCause();
    }
    return atual.getMessage() == null ? e.getMessage() : atual.getMessage();
  }

  private void limparCampos() {
    txtMatricula.setText("");
    txtDataCadastro.setText(LocalDate.now().format(DATA_BR));
    limpar(
        txtNomeAluno,
        txtEmailAluno,
        txtRua,
        txtNumero,
        txtComplemento,
        txtBairro,
        txtCidade,
        txtNomeMae,
        txtEmailMae,
        txtNomePai,
        txtEmailPai);
    limpar(
        txtCpfAluno,
        txtRgAluno,
        txtTelefoneAluno,
        txtDataNascimento,
        txtCep,
        txtCpfMae,
        txtTelefoneMae,
        txtCpfPai,
        txtTelefonePai);
    txtObsSaude.setText("");
    cbAtivo.setSelectedIndex(0);
    cbSexo.setSelectedIndex(0);
    cbTurma.setSelectedIndex(0);
    cbSituacao.setSelectedIndex(0);
    cbEstado.setSelectedIndex(-1);
  }

  private void limpar(JTextField... campos) {
    for (JTextField campo : campos) {
      if (campo != null) {
        campo.setText("");
      }
    }
  }

  private static class TurmaItem {
    private final int id;
    private final String descricao;

    private TurmaItem(int id, String descricao) {
      this.id = id;
      this.descricao = descricao;
    }

    @Override
    public String toString() {
      return id <= 0 ? descricao : id + " - " + descricao;
    }
  }
}
