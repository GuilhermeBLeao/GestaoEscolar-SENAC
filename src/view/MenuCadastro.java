package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import view.Secretaria.CadastroAluno;
import view.Secretaria.CadastroDisciplina;
import view.Secretaria.CadastroFuncionario;
import view.Secretaria.CadastroProfessor;

public class MenuCadastro extends JFrame {
  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color textos = Color.WHITE;

  public MenuCadastro() {
    setTitle("Menu de Cadastros");
    setSize(700, 750);
    setResizable(false);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel externo = new JPanel(null);
    externo.setBackground(corExterna);
    setContentPane(externo);

    JPanel interno = new JPanel(null);
    interno.setBounds(10, 10, 665, 690);
    interno.setBackground(corInterna);
    externo.add(interno);

    criarTela(interno);
  }

  private void criarTela(JPanel painel) {

    JPanel topo = criarTopo();
    topo.setBounds(20, 20, 620, 140);
    painel.add(topo);

    JButton btnVoltar = new JButton("← Voltar");
    btnVoltar.setBounds(20, 20, 120, 35);
    estilizarBotao(btnVoltar);
    btnVoltar.addActionListener(e -> dispose());
    topo.add(btnVoltar);

    JPanel card = criarCard("Selecione o Cadastro");
    card.setBounds(80, 200, 500, 400);

    JButton btnAluno = new JButton("Cadastro de Aluno");
    btnAluno.setBounds(75, 70, 350, 50);
    estilizarBotao(btnAluno);

    btnAluno.addActionListener(
        e -> {
          new CadastroAluno().setVisible(true);
        });

    JButton btnDisciplina = new JButton("Cadastro de Disciplina");
    btnDisciplina.setBounds(75, 140, 350, 50);
    estilizarBotao(btnDisciplina);

    btnDisciplina.addActionListener(
        e -> {
          new CadastroDisciplina().setVisible(true);
        });

    JButton btnProfessor = new JButton("Cadastro de Professor");
    btnProfessor.setBounds(75, 210, 350, 50);
    estilizarBotao(btnProfessor);

    btnProfessor.addActionListener(
        e -> {
          new CadastroProfessor().setVisible(true);
        });

    JButton btnFuncionario = new JButton("Cadastro de Funcionário");
    btnFuncionario.setBounds(75, 280, 350, 50);
    estilizarBotao(btnFuncionario);

    btnFuncionario.addActionListener(
        e -> {
          new CadastroFuncionario().setVisible(true);
        });

    card.add(btnAluno);
    card.add(btnDisciplina);
    card.add(btnProfessor);
    card.add(btnFuncionario);

    painel.add(card);
  }

  private JPanel criarTopo() {
    JPanel topo =
        new JPanel(null) {
          private static final long serialVersionUID = 1L;

          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            GradientPaint gp =
                new GradientPaint(
                    0, 0, new Color(70, 20, 160), getWidth(), getHeight(), new Color(190, 35, 170));

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            g2.dispose();
          }
        };

    topo.setOpaque(false);

    JLabel titulo = new JLabel("Menu de Cadastros");
    titulo.setForeground(textos);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
    titulo.setBounds(30, 60, 350, 35);
    topo.add(titulo);

    JLabel subtitulo = new JLabel("Selecione o cadastro desejado");
    subtitulo.setForeground(new Color(240, 240, 255));
    subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    subtitulo.setBounds(30, 100, 300, 20);
    topo.add(subtitulo);

    return topo;
  }

  private JPanel criarCard(String titulo) {
    JPanel painel = criarPainelArredondado();
    painel.setLayout(null);

    JLabel lblTitulo = new JLabel(titulo);
    lblTitulo.setForeground(corLabel);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lblTitulo.setBounds(30, 20, 350, 30);

    painel.add(lblTitulo);

    return painel;
  }

  private void estilizarBotao(JButton botao) {
    botao.setBackground(corCampo);
    botao.setForeground(textos);
    botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
    botao.setFocusPainted(false);
    botao.setBorder(new LineBorder(corBorda, 1, true));
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
      }
    };
  }
}
