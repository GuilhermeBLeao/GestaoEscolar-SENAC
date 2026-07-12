package view.Professor;

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
import java.awt.Toolkit;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import model.AtividadeDia;
import model.Disciplina;
import model.Professor;

public class LancarAtividadeDia extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;

  private JTextField campoProfessor;
  private JTextField campoDisciplina;
  private JTextField campoData;
  private JTextArea areaDescricao;

  private Professor professorLogado;
  private Disciplina disciplinaProfessor;

  public LancarAtividadeDia() {
    setTitle("Atividade do Dia");
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("resources/Images/Icons/Disciplinas Professor.png"));
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

    JLabel lblProfessor = new JLabel("Professor:");
    lblProfessor.setForeground(corLabel);
    lblProfessor.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lblProfessor.setBounds(centroX - 740, 250, 200, 40);
    interno.add(lblProfessor);

    campoProfessor = new JTextField(" Pedro Henrique Carvalho");
    campoProfessor.setEditable(false);
    campoProfessor.setFont(new Font("Segoe UI", Font.PLAIN, 28));
    campoProfessor.setBackground(corCampo);
    campoProfessor.setForeground(textos);
    campoProfessor.setBorder(new LineBorder(corBorda));
    campoProfessor.setBounds(centroX - 740, 300, larguraCampo, alturaCampo);
    interno.add(campoProfessor);

    JLabel lblDisciplina = new JLabel("Disciplina:");
    lblDisciplina.setForeground(corLabel);
    lblDisciplina.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lblDisciplina.setBounds(centroX + 40, 250, 200, 40);
    interno.add(lblDisciplina);

    campoDisciplina = new JTextField(" L.E - Espanhol");
    campoDisciplina.setEditable(false);
    campoDisciplina.setFont(new Font("Segoe UI", Font.PLAIN, 28));
    campoDisciplina.setBackground(corCampo);
    campoDisciplina.setForeground(textos);
    campoDisciplina.setBorder(new LineBorder(corBorda));
    campoDisciplina.setBounds(centroX + 40, 300, larguraCampo, alturaCampo);
    interno.add(campoDisciplina);

    JLabel lblData = new JLabel("Data:");
    lblData.setForeground(corLabel);
    lblData.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lblData.setBounds(centroX - 240, 390, 200, 40);
    interno.add(lblData);

    campoData = new JTextField(LocalDate.now().toString());
    campoData.setFont(new Font("Segoe UI", Font.PLAIN, 28));
    campoData.setBackground(corCampo);
    campoData.setForeground(textos);
    campoData.setBorder(new LineBorder(corBorda));
    campoData.setBounds(centroX - 240, 440, 400, alturaCampo);
    interno.add(campoData);

    JLabel lblDescricao = new JLabel("Descrição:");
    lblDescricao.setForeground(corLabel);
    lblDescricao.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lblDescricao.setBounds(centroX - (larguraDescricao / 2), 530, 250, 40);
    interno.add(lblDescricao);

    areaDescricao = new JTextArea();
    areaDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 24));
    areaDescricao.setBackground(corCampo);
    areaDescricao.setForeground(textos);
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
    btnLimparCampos.setBounds(
        inicio + larguraBotao + espacamento, yBotao, larguraBotao, alturaBotao);
    estilizarBotao(btnLimparCampos);
    btnLimparCampos.addActionListener(e -> limparCampos());
    interno.add(btnLimparCampos);

    JButton btnCancelar = new JButton("Cancelar");
    btnCancelar.setBounds(
        inicio + (larguraBotao + espacamento) * 2, yBotao, larguraBotao, alturaBotao);
    estilizarBotao(btnCancelar);
    btnCancelar.addActionListener(e -> dispose());
    interno.add(btnCancelar);
  }

  private JPanel criarTopo() {
    JPanel topo =
        new JPanel(null) {
          private static final long serialVersionUID = 1L;

          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));
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

    JLabel sub =
        new JLabel("Cadastre um pequeno resumo do que será/foi feito durante a aula de hoje.");

    sub.setForeground(new Color(245, 225, 255));
    sub.setFont(new Font("Segoe UI", Font.PLAIN, 26));
    sub.setBounds(52, 120, 1100, 40);
    topo.add(sub);

    return topo;
  }

  private Object limparCampos() {
    areaDescricao.setText("");
    return null;
  }

  private void salvarAtividade() {
    try {
      AtividadeDia atividade = new AtividadeDia();
      if (professorLogado != null) {
        atividade.setProfessorId(professorLogado.getIdProfessor());
      }
      if (disciplinaProfessor != null) {
        atividade.setDisciplinaId(disciplinaProfessor.getIdDisciplina());
      }
      atividade.setData(LocalDate.parse(campoData.getText()));
      atividade.setDescricao(areaDescricao.getText());
      JOptionPane.showMessageDialog(this, "Atividade registrada com sucesso!");
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
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
