package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import view.Aluno.AdvertenciaAluno;
import view.Aluno.DisciplinasAluno;
import view.Aluno.Frequencia;
import view.Aluno.MeusDadosAluno;
import view.Aluno.NotasAluno;
import view.Aluno.SuporteAluno;
import view.Aluno.TelaInicialAluno;
import view.Funcionario.AtendimentoSuporte;
import view.Funcionario.MeusDadosFuncionario;
import view.Funcionario.Relatorio;
import view.Funcionario.TelaInicialFuncionario;
import view.Professor.ChamadaAluno;
import view.Professor.DisciplinasProfessor;
import view.Professor.EmitirAvisos;
import view.Professor.LancamentoNota;
import view.Professor.LancarAtividadeDia;
import view.Professor.MeusDadosProfessor;
import view.Professor.MinhasTurmasProfessor;
import view.Professor.RegistrarAdvertenciaProfessor;
import view.Professor.SelecaoPerfil;
import view.Professor.TelaInicialProfessor;
import view.Responsavel.AdvertenciasResponsavel;
import view.Responsavel.AvisosResponsavel;
import view.Responsavel.BoletimResponsavel;
import view.Responsavel.FrequenciaResponsavel;
import view.Responsavel.MeusDadosResponsavel;
import view.Responsavel.SuporteResponsavel;
import view.Responsavel.TelaInicialPais;
import view.Secretaria.AdvertenciaSecretaria;
import view.Secretaria.AlunosSecretaria;
import view.Secretaria.AvisosSecretaria;
import view.Secretaria.Boletim;
import view.Secretaria.CadastroAluno;
import view.Secretaria.CadastroDisciplina;
import view.Secretaria.CadastroFuncionario;
import view.Secretaria.CadastroProfessor;
import view.Secretaria.DocumentosSecretaria;

public class MenuTelas extends JFrame {

  private static final long serialVersionUID = 1L;

  private final Color corExterna = new Color(27, 0, 69);
  private final Color corBorda = new Color(120, 70, 220);
  private final Color corLabel = new Color(255, 120, 220);
  private final Color corCampo = new Color(25, 6, 75);
  private final Color textos = Color.WHITE;
  private final Color corBotao = new Color(100, 50, 150);

  public MenuTelas() {
    setTitle("Menu de Telas");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setResizable(false);

    JPanel externo = new JPanel(new BorderLayout(15, 15));
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(20, 20, 20, 20));
    setContentPane(externo);

    JLabel titulo =
        new JLabel("Sistema de Gerenciamento Escolar - Menu de Telas", SwingConstants.CENTER);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
    titulo.setForeground(corLabel);
    externo.add(titulo, BorderLayout.NORTH);

    JPanel painelPrincipal = new JPanel(new GridLayout(2, 1, 15, 15));
    painelPrincipal.setBackground(corExterna);

    JPanel linhaSuperior = new JPanel(new GridLayout(1, 3, 15, 15));
    linhaSuperior.setBackground(corExterna);

    JPanel linhaInferior = new JPanel(new GridLayout(1, 2, 15, 15));
    linhaInferior.setBackground(corExterna);

    adicionarSecaoAluno(linhaSuperior);
    adicionarSecaoProfessor(linhaSuperior);
    adicionarSecaoResponsavel(linhaSuperior);

    adicionarSecaoFuncionario(linhaInferior);
    adicionarSecaoSecretaria(linhaInferior);

    painelPrincipal.add(linhaSuperior);
    painelPrincipal.add(linhaInferior);

    externo.add(painelPrincipal, BorderLayout.CENTER);
  }

  private void adicionarSecaoAluno(JPanel painel) {
    JPanel secao = criarSecao("ALUNO", 7);

    adicionarBotao(secao, "Advertência", AdvertenciaAluno.class);
    adicionarBotao(secao, "Disciplinas", DisciplinasAluno.class);
    adicionarBotao(secao, "Frequência", Frequencia.class);
    adicionarBotao(secao, "Meus Dados", MeusDadosAluno.class);
    adicionarBotao(secao, "Notas", NotasAluno.class);
    adicionarBotao(secao, "Suporte", SuporteAluno.class);
    adicionarBotao(secao, "Tela Inicial", TelaInicialAluno.class);

    painel.add(secao);
  }

  private void adicionarSecaoFuncionario(JPanel painel) {
    JPanel secao = criarSecao("FUNCIONÁRIO", 5);

    adicionarBotao(secao, "Atendimento Suporte", AtendimentoSuporte.class);
    adicionarBotao(secao, "Meus Dados", MeusDadosFuncionario.class);
    adicionarBotao(secao, "Relatório", Relatorio.class);
    adicionarBotao(secao, "Tela Inicial", TelaInicialFuncionario.class);

    painel.add(secao);
  }

  private void adicionarSecaoProfessor(JPanel painel) {
    JPanel secao = criarSecao("PROFESSOR", 10);

    adicionarBotao(secao, "Chamada de Alunos", ChamadaAluno.class);
    adicionarBotao(secao, "Disciplinas", DisciplinasProfessor.class);
    adicionarBotao(secao, "Emitir Avisos", EmitirAvisos.class);
    adicionarBotao(secao, "Lançamento de Notas", LancamentoNota.class);
    adicionarBotao(secao, "Lançar Atividade do Dia", LancarAtividadeDia.class);
    adicionarBotao(secao, "Meus Dados", MeusDadosProfessor.class);
    adicionarBotao(secao, "Minhas Turmas", MinhasTurmasProfessor.class);
    adicionarBotao(secao, "Registrar Advertência", RegistrarAdvertenciaProfessor.class);
    adicionarBotao(secao, "Seleção de Perfil", SelecaoPerfil.class);
    adicionarBotao(secao, "Tela Inicial", TelaInicialProfessor.class);

    painel.add(secao);
  }

  private void adicionarSecaoResponsavel(JPanel painel) {
    JPanel secao = criarSecao("RESPONSÁVEL", 7);

    adicionarBotao(secao, "Advertências", AdvertenciasResponsavel.class);
    adicionarBotao(secao, "Avisos", AvisosResponsavel.class);
    adicionarBotao(secao, "Boletim", BoletimResponsavel.class);
    adicionarBotao(secao, "Frequência", FrequenciaResponsavel.class);
    adicionarBotao(secao, "Meus Dados", MeusDadosResponsavel.class);
    adicionarBotao(secao, "Suporte", SuporteResponsavel.class);
    adicionarBotao(secao, "Tela Inicial", TelaInicialPais.class);

    painel.add(secao);
  }

  private void adicionarSecaoSecretaria(JPanel painel) {
    JPanel secao = criarSecao("SECRETARIA", 10);

    adicionarBotao(secao, "Advertências", AdvertenciaSecretaria.class);
    adicionarBotao(secao, "Alunos", AlunosSecretaria.class);
    adicionarBotao(secao, "Avisos", AvisosSecretaria.class);
    adicionarBotao(secao, "Boletim", Boletim.class);
    adicionarBotao(secao, "Cadastro de Aluno", CadastroAluno.class);
    adicionarBotao(secao, "Cadastro de Disciplina", CadastroDisciplina.class);
    adicionarBotao(secao, "Cadastro de Funcionário", CadastroFuncionario.class);
    adicionarBotao(secao, "Cadastro de Professor", CadastroProfessor.class);
    adicionarBotao(secao, "Documentos", DocumentosSecretaria.class);
   // adicionarBotao(secao, "Usuários", Usuario.class);

    painel.add(secao);
  }

  private JPanel criarSecao(String titulo, int quantidadeBotoes) {
    JPanel secao = new JPanel();
    secao.setLayout(new GridLayout(quantidadeBotoes, 1, 8, 8));
    secao.setBackground(corCampo);

    TitledBorder bordaTitulo =
        new TitledBorder(
            new LineBorder(corBorda, 2),
            titulo,
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 15),
            corLabel);

    secao.setBorder(
        BorderFactory.createCompoundBorder(bordaTitulo, new EmptyBorder(18, 15, 15, 15)));

    return secao;
  }

  private void adicionarBotao(JPanel secao, String nome, Class<?> classe) {
    JButton botao = new JButton(nome);
    botao.setFont(new Font("Segoe UI", Font.BOLD, 13));
    botao.setForeground(textos);
    botao.setBackground(corBotao);
    botao.setBorder(new LineBorder(corBorda, 1));
    botao.setFocusPainted(false);
    botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

    botao.addActionListener((ActionEvent e) -> abrirTela(classe));

    secao.add(botao);
  }

  private void abrirTela(Class<?> classe) {
    try {
      Object tela = classe.getDeclaredConstructor().newInstance();

      if (tela instanceof JFrame) {
        ((JFrame) tela).setVisible(true);
      }

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
          this, "Erro ao abrir tela: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
    }
  }
}
