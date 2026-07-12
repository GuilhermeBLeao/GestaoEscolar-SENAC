package view.Secretaria;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.sql.Connection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
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
import javax.swing.text.MaskFormatter;

import dao.AlunoDAO;
import database.ConnectionFactory;
import model.Aluno;
import util.ValidaCPF;
import util.ValidaData;
import util.ValidaEmail;
import util.ValidaNome;
import util.ValidaTelefone;

public class AlunosSecretaria extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTable tabela;

  private JTextField txtNome;
  private JFormattedTextField txtCpf;
  private JTextField txtRg;
  private JFormattedTextField txtNascimento;
  private JFormattedTextField txtTelefone;
  private JTextField txtEmail;

  private JTextField txtResponsavel;
  private JFormattedTextField txtCpfResponsavel;
  private JFormattedTextField txtTelefoneResponsavel;
  private JTextField txtParentesco;
  private JTextField matricula;
  private JTextField nome = campo();

  public AlunosSecretaria() {
    setTitle("Alunos - Secretaria");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Alunos Secretaria.jpeg"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setResizable(false);

    Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    int largura = area.width - 60;

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    setContentPane(externo);

    JPanel interno = new JPanel(null);
    interno.setBounds(20, 20, 1880, 970);
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
  }

  private Object[][] buscarAlunosBanco() {

    try (Connection conn = ConnectionFactory.getConnection()) {

      AlunoDAO alunoDAO = new AlunoDAO(conn);

      List<Aluno> alunos = alunoDAO.listar();

      Object[][] dados = new Object[alunos.size()][6];

      for (int i = 0; i < alunos.size(); i++) {

        Aluno aluno = alunos.get(i);

        dados[i][0] = aluno.getMatricula();
        dados[i][1] = aluno.getNome();
        dados[i][2] = aluno.getIdTurma();
        dados[i][3] = "";
        dados[i][4] = "";
        dados[i][5] = aluno.getSituacao();
      }

      return dados;

    } catch (Exception e) {

      e.printStackTrace();

      JOptionPane.showMessageDialog(
          this,
          "Erro ao carregar alunos do banco:\n" + e.getMessage(),
          "Erro",
          JOptionPane.ERROR_MESSAGE);

      return new Object[0][0];
    }
  }

  private void criarResumo(JPanel p) {
    JPanel card = card();
    card.setBounds(300, 210, 1280, 100);

    card.add(indicador("Total", "523", 30));
    card.add(indicador("Ativos", "497", 330));
    card.add(indicador("Inativos", "19", 630));
    card.add(indicador("Transferidos", "7", 930));

    p.add(card);
  }

  private JPanel indicador(String titulo, String valor, int x) {
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
    return p;
  }

  private void criarPesquisa(JPanel p) {
    JPanel card = card();
    card.setBounds(300, 330, 1280, 110);

    JLabel l1 = label("Nome");
    l1.setBounds(20, 15, 100, 20);

    nome.setBounds(20, 40, 280, 35);

    JLabel l2 = label("Matrícula");
    l2.setBounds(320, 15, 100, 20);

    matricula = campo();
    matricula.setBounds(320, 40, 150, 35);

    JButton pesquisar = botao("Pesquisar");
    pesquisar.setBounds(500, 40, 150, 35);
    pesquisar.addActionListener(e -> pesquisarAlunos());

    JButton limpar = botao("Limpar");
    limpar.setBounds(670, 40, 150, 35);
    limpar.addActionListener(
        e -> {
          nome.setText("");
          matricula.setText("");

          carregarTabela();
        });
    matricula = campo();
    matricula.setBounds(320, 40, 150, 35);

    matricula.addKeyListener(
        new java.awt.event.KeyAdapter() {
          @Override
          public void keyTyped(java.awt.event.KeyEvent e) {

            char c = e.getKeyChar();

            if (!Character.isDigit(c) || matricula.getText().length() >= 10) {
              e.consume();
            }
          }
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
    card.setBounds(300, 460, 1280, 220);

    String[] cols = {"Matrícula", "Nome", "Turma", "Curso", "Turno", "Situação"};

    DefaultTableModel model = new DefaultTableModel(buscarAlunosBanco(), cols);

    tabela = new JTable(model);
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

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);
    centro.setVerticalAlignment(SwingConstants.CENTER);
    centro.setBackground(corCampo);
    centro.setForeground(textos);
    centro.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < tabela.getColumnCount(); i++) {
      tabela.getColumnModel().getColumn(i).setCellRenderer(centro);
    }

    JScrollPane scrollTabela = new JScrollPane(tabela);
    scrollTabela.setBounds(25, 40, 1170, 140);
    scrollTabela.getViewport().setBackground(corInterna);
    scrollTabela.setBorder(new LineBorder(corBorda, 1, true));
    scrollTabela.getVerticalScrollBar().setUnitIncrement(26);
    card.add(scrollTabela);
    p.add(card);
  }

  private void criarCadastroAluno(JPanel p) {
    JPanel card = card();
    card.setBounds(300, 700, 620, 220);

    adicionarCampo(card, "Nome Completo", 20, 20, 250);
    adicionarCampo(card, "CPF", 320, 20, 250);
    adicionarCampo(card, "RG", 20, 80, 250);
    adicionarCampo(card, "Nascimento", 320, 80, 250);
    adicionarCampo(card, "Telefone", 20, 140, 250);
    adicionarCampo(card, "E-mail", 320, 140, 250);
    p.add(card);
  }

  private void criarResponsavel(JPanel p) {
    JPanel card = card();
    card.setBounds(960, 700, 620, 220);

    adicionarCampo(card, "Responsável", 20, 20, 250);
    adicionarCampo(card, "CPF", 320, 20, 250);
    adicionarCampo(card, "Telefone", 20, 80, 250);
    adicionarCampo(card, "Parentesco", 320, 80, 250);

    JButton salvar = botao("Salvar Alterações");

    salvar.addActionListener(
        e -> {
          StringBuilder erros = new StringBuilder();

          try {
            ValidaNome.validar(txtNome.getText());
          } catch (Exception ex) {
            erros.append("• Nome inválido.\n");
          }

          try {
            ValidaCPF.validar(txtCpf.getText());
          } catch (Exception ex) {
            erros.append("• CPF inválido.\n");
          }

          if (!ValidaData.validar(txtNascimento.getText())) {
            erros.append("• Data de nascimento inválida.\n");
          }

          try {
            ValidaTelefone.validar(txtTelefone.getText());
          } catch (Exception ex) {
            erros.append("• Telefone inválido.\n");
          }

          try {
            ValidaEmail.validar(txtEmail.getText());
          } catch (Exception ex) {
            erros.append("• E-mail inválido.\n");
          }

          // Responsável (somente se preenchido)

          if (!txtResponsavel.getText().trim().isEmpty()) {
            try {
              ValidaNome.validar(txtResponsavel.getText());
            } catch (Exception ex) {
              erros.append("• Nome do responsável inválido.\n");
            }
          }

          String cpfResp = txtCpfResponsavel.getText().replaceAll("\\D", "");

          if (!cpfResp.isEmpty()) {
            try {
              ValidaCPF.validar(txtCpfResponsavel.getText());
            } catch (Exception ex) {
              erros.append("• CPF do responsável inválido.\n");
            }
          }

          String telResp = txtTelefoneResponsavel.getText().replaceAll("\\D", "");

          if (!telResp.isEmpty()) {
            try {
              ValidaTelefone.validar(txtTelefoneResponsavel.getText());
            } catch (Exception ex) {
              erros.append("• Telefone do responsável inválido.\n");
            }
          }

          if (erros.length() > 0) {

            JOptionPane.showMessageDialog(
                this,
                "Foram encontrados os seguintes erros:\n\n" + erros.toString(),
                "Erro de validação",
                JOptionPane.ERROR_MESSAGE);

            return;
          }

          JOptionPane.showMessageDialog(
              this,
              "Dados validados. Para salvar a matrícula completa no banco, use a tela de Cadastro"
                  + " de Aluno.",
              "Cadastro completo necessário",
              JOptionPane.INFORMATION_MESSAGE);
          new CadastroAluno().setVisible(true);
        });
    salvar.setBounds(20, 150, 220, 40);

    JButton inativar = botao("Inativar Aluno");
    inativar.setBounds(260, 150, 220, 40);

    card.add(salvar);
    card.add(inativar);
    p.add(card);
  }

  private void adicionarCampo(JPanel p, String texto, int x, int y, int largura) {

    JLabel l = label(texto);
    l.setBounds(x, y, 200, 20);

    JComponent campo;

    switch (texto) {
      case "Nome Completo":
        txtNome = campo();
        campo = txtNome;
        break;
      case "CPF":
        if (txtCpf == null) {
          txtCpf = campoMascarado("###.###.###-##");
          campo = txtCpf;
        } else {
          txtCpfResponsavel = campoMascarado("###.###.###-##");
          campo = txtCpfResponsavel;
        }
        break;
      case "RG":
        txtRg = campo();
        txtRg.addKeyListener(
            new java.awt.event.KeyAdapter() {
              public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || txtRg.getText().length() >= 11) {
                  e.consume();
                }
              }
            });
        campo = txtRg;
        break;
      case "Nascimento":
        txtNascimento = campoMascarado("##/##/####");
        campo = txtNascimento;
        break;
      case "Telefone":
        if (txtTelefone == null) {
          txtTelefone = campoMascarado("(##) #####-####");
          campo = txtTelefone;
        } else {
          txtTelefoneResponsavel = campoMascarado("(##) #####-####");
          campo = txtTelefoneResponsavel;
        }
        break;
      case "E-mail":
        txtEmail = campo();
        campo = txtEmail;
        break;
      case "Responsável":
        txtResponsavel = campo();
        campo = txtResponsavel;
        break;
      case "Parentesco":
        txtParentesco = campo();
        campo = txtParentesco;
        break;
      default:
        campo = campo();
    }

    campo.setBounds(x, y + 25, largura, 32);
    p.add(l);
    p.add(campo);
  }

  private JFormattedTextField campoMascarado(String mascara) {
    try {
      MaskFormatter mf = new MaskFormatter(mascara);
      mf.setPlaceholderCharacter('_');
      JFormattedTextField campo = new JFormattedTextField(mf);
      campo.setBackground(corCampo);
      campo.setForeground(textos);
      campo.setBorder(new LineBorder(corBorda));
      return campo;
    } catch (Exception e) {
      return new JFormattedTextField();
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
    JPanel topo =
        new JPanel(null) {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
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

  private void pesquisarAlunos() {

    try (Connection conn = ConnectionFactory.getConnection()) {

      AlunoDAO dao = new AlunoDAO(conn);
      List<Aluno> alunos = dao.listar();

      String nomeBusca = nome.getText().trim().toLowerCase();
      String matriculaBusca = matricula.getText().trim().toLowerCase();

      javax.swing.table.DefaultTableModel model =
          (javax.swing.table.DefaultTableModel) tabela.getModel();

      model.setRowCount(0);

      for (Aluno aluno : alunos) {

        if (!nomeBusca.isEmpty() && !aluno.getNome().toLowerCase().contains(nomeBusca)) {
          continue;
        }

        if (!matriculaBusca.isEmpty()
            && !aluno.getMatricula().toLowerCase().contains(matriculaBusca)) {
          continue;
        }

        model.addRow(
            new Object[] {
              aluno.getMatricula(), aluno.getNome(), aluno.getIdTurma(), "", "", aluno.getSituacao()
            });
      }

    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Erro ao pesquisar alunos:\n" + e.getMessage());
    }
  }

  private void carregarTabela() {

    String[] colunas = {"Matrícula", "Nome", "Turma", "Curso", "Turno", "Situação"};

    tabela.setModel(new javax.swing.table.DefaultTableModel(buscarAlunosBanco(), colunas));
  }
}
