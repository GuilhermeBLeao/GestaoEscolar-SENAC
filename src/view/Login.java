package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import controller.UsuarioController;
import model.Usuario;
import util.SessaoUsuario;
import variaveisEnum.TipoUsuario;
import view.Aluno.TelaInicialAluno;
import view.Funcionario.TelaInicialFuncionario;
import view.Professor.SelecaoPerfil;
import view.Professor.TelaInicialProfessor;
import view.Responsavel.TelaInicialPais;

public class Login extends JFrame {
  private static final long serialVersionUID = 1L;

  // Declaração das cores a serem utilizadas
  private final Color corExterna = new Color(27, 0, 69);
  private final Color corInterna = new Color(38, 2, 92);
  private final Color campo = new Color(25, 6, 75);
  private final Color borda = new Color(145, 85, 220);
  private final Color textoSecundario = new Color(190, 160, 230);
  private final Color textos = Color.WHITE;

  // Declaração do campo de senha
  private JPasswordField pfSenha;
  private char echoChar;
  private boolean passwordVisible = false;

  public static void main(String[] args) {
	  SwingUtilities.invokeLater (() -> new Login().setVisible(true));
  } 

  // Cria o JFrame
  public Login() {
    setResizable(false);
    setTitle("E.E.B. Solo Firme");
    setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/Solo Firme.png"));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 700, 750);
    setLocationRelativeTo(null);

    // Cria e configura o painel "externo"
    JPanel externo = new JPanel();
    externo.setBackground(corExterna);
    externo.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(externo);
    externo.setLayout(null);

    // Cria e configura o painel "interno"
    JPanel interno = new JPanel();
    interno.setBounds(42, 30, 600, 650);
    externo.add(interno);
    interno.setBackground(corInterna);
    interno.setLayout(null);

    // Cria a label para aplicar a logo
    JLabel lblLogo = new JLabel();
    ImageIcon icone = new ImageIcon("resources/Images/Solo Firme.png");
    Image img = icone.getImage().getScaledInstance(360, 260, Image.SCALE_SMOOTH);
    lblLogo.setIcon(new ImageIcon(img));
    lblLogo.setBounds(113, 12, 360, 180);
    interno.add(lblLogo);

    // Cria e configura o campo para Login
    JTextField txtCpf = new JTextField();
    txtCpf.setFont(new Font("Dialog", Font.PLAIN, 20));
    txtCpf.setBounds(30, 320, 520, 40);
    txtCpf.setBackground(campo);
    txtCpf.setBorder(new CompoundBorder(new LineBorder(borda, 1, true), null));
    txtCpf.setForeground(textos);

    // Cria a máscara para CPF
    txtCpf.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
            String texto = txtCpf.getText();

            // Configura para apenas números
            texto = texto.replaceAll("[^0-9]", "");

            if (texto.length() > 11) {
              texto = texto.substring(0, 11);
            }

            if (texto.length() > 9) {
              texto = texto.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d+)", "$1.$2.$3-$4");
            } else if (texto.length() > 6) {
              texto = texto.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1.$2.$3");
            } else if (texto.length() > 3) {
              texto = texto.replaceFirst("(\\d{3})(\\d+)", "$1.$2");
            }
            txtCpf.setText(texto);
          }
        });
    interno.add(txtCpf);

    // Cria e configura a label informativa para Email
    JLabel lblEmail = new JLabel("CPF");
    lblEmail.setFont(new Font("Times New Roman", Font.PLAIN, 22));
    lblEmail.setHorizontalAlignment(SwingConstants.CENTER);
    lblEmail.setForeground(textos);
    lblEmail.setBounds(12, 284, 80, 40);
    interno.add(lblEmail);

    // Cria e configura a label informativa para Senha
    JLabel lblSenha = new JLabel("Senha");
    lblSenha.setFont(new Font("Times New Roman", Font.PLAIN, 22));
    lblSenha.setHorizontalAlignment(SwingConstants.LEFT);
    lblSenha.setForeground(textos);
    lblSenha.setBounds(30, 371, 100, 40);
    interno.add(lblSenha);

    //// Cria e configura a label informativa para "Acesso"
    JLabel lblAcesso = new JLabel("Acesse a sua conta");
    lblAcesso.setFont(new Font("Mongolian Baiti", Font.BOLD, 28));
    lblAcesso.setHorizontalAlignment(SwingConstants.CENTER);
    lblAcesso.setForeground(textos);
    lblAcesso.setBounds(90, 150, 416, 100);
    interno.add(lblAcesso);

    //// Cria e configura a label informativa para "texto secundário"
    JLabel lblCredenciais = new JLabel("Informe as suas credenciais para acessar o sistema");
    lblCredenciais.setFont(new Font("Montserrat", Font.BOLD, 16));
    lblCredenciais.setHorizontalAlignment(SwingConstants.CENTER);
    lblCredenciais.setForeground(textos);
    lblCredenciais.setBounds(80, 200, 416, 100);
    interno.add(lblCredenciais);

    // Cria e configura o campo da senha
    pfSenha = new JPasswordField();
    pfSenha.setFont(new Font("Dialog", Font.PLAIN, 20));
    pfSenha.setBackground(campo);
    pfSenha.setBorder(new CompoundBorder(new LineBorder(borda, 1, true), null));
    pfSenha.setForeground(textos);
    pfSenha.setBounds(30, 410, 495, 40);
    interno.add(pfSenha);
    echoChar = pfSenha.getEchoChar();

    // Cria e configura o label "olho" - Mostrar e ocultar a senha
    JLabel lblEye = new JLabel("👁");
    lblEye.setFont(new Font("Dialog", Font.BOLD, 22));
    lblEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
    lblEye.setAutoscrolls(true);
    lblEye.setOpaque(true);
    lblEye.setBackground(campo);
    lblEye.setForeground(textos);
    lblEye.setBorder(new CompoundBorder(new LineBorder(borda, 1, true), null));
    lblEye.setSize(25, 40);
    lblEye.setLocation(525, 410);

    // Configura o "mostrar e ocultar a senha"
    lblEye.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (!passwordVisible) {
              pfSenha.setEchoChar((char) 0); // mostra senha
            } else {
              pfSenha.setEchoChar(echoChar); // oculta senha
            }
            passwordVisible = !passwordVisible;
          }
        });
    interno.add(lblEye);

    // Cria e configura o botão
    JButton btnEntrar = new JButton("ENTRAR");
    btnEntrar.setFont(new Font("Montserrat", Font.BOLD, 18));
    btnEntrar.setBackground(corExterna);
    btnEntrar.setForeground(textoSecundario);
    btnEntrar.setBounds(45, 480, 480, 45);
    btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnEntrar.setFocusPainted(false);
    btnEntrar.setBorder(new LineBorder(borda, 1, true));
    btnEntrar.addActionListener(
        e -> autenticar(txtCpf.getText(), new String(pfSenha.getPassword())));
    interno.add(btnEntrar);
  }

  private void autenticar(String cpf, String senha) {
    try {
      UsuarioController controller = new UsuarioController();
      Usuario usuario = controller.autenticarLogin(cpf, senha);

      if (usuario == null || !usuario.isAtivo()) {
        JOptionPane.showMessageDialog(
            this, "CPF ou senha invalidos.", "Login", JOptionPane.WARNING_MESSAGE);
        return;
      }

      if (SessaoUsuario.existeUsuarioLogado()) {
        SessaoUsuario.encerrarSessao();
      }

      TipoUsuario perfil = escolherPerfil(usuario);
      if (perfil == null) {
        return;
      }

      usuario.setTipoUsuario(perfil);
      SessaoUsuario.setUsuarioLogado(usuario);
      controller.atualizarUltimoLogin(usuario.getIdUsuario(), LocalDateTime.now());

      abrirTelaInicial(perfil);
      dispose();
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(
          this, ex.getMessage(), "Dados invalidos", JOptionPane.WARNING_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
          this, "Erro ao autenticar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
    }
  }

  private TipoUsuario escolherPerfil(Usuario usuario) {
    boolean temProfessor = usuario.getProfessorId() > 0;
    boolean temResponsavel = usuario.getPaiId() > 0;

    if (temProfessor && temResponsavel) {
      SelecaoPerfil selecao = new SelecaoPerfil(this, "CPF " + usuario.getCpf(), true, true);
      selecao.setVisible(true);

      String perfilSelecionado = selecao.getPerfilSelecionado();
      if ("PROFESSOR".equals(perfilSelecionado)) {
        return TipoUsuario.PROFESSOR;
      }
      if ("RESPONSAVEL".equals(perfilSelecionado)) {
        return TipoUsuario.RESPONSAVEL;
      }
      return null;
    }

    return usuario.getTipoUsuario();
  }

  private void abrirTelaInicial(TipoUsuario perfil) {
    JFrame tela;
    switch (perfil) {
      case ALUNO:
        tela = new TelaInicialAluno();
        break;
      case PROFESSOR:
        tela = new TelaInicialProfessor();
        break;
      case RESPONSAVEL:
        tela = new TelaInicialPais();
        break;
      case SECRETARIA:
      case ADMINISTRADOR:
      case DIRECAO:
      case PEDAGOGICO:
        tela = new TelaInicialFuncionario();
        break;
      default:
        tela = new MenuTelas();
        break;
    }
    tela.setVisible(true);
  }
}
