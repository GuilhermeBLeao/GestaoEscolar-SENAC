//Guilherme

package view;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPasswordField pfSenha;
	private char echoChar;
	private boolean passwordVisible = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// Create the frame.
	public Login() {
		setResizable(false);
		setTitle("JetCoder's");
		setIconImage(Toolkit.getDefaultToolkit().getImage("resources/Images/JetCoder's fundo preto.jpeg"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 750);
		setLocationRelativeTo(null);

		JPanel externo = new JPanel();
		externo.setBackground(new Color(18, 3, 50));
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(externo);
		externo.setLayout(null);

		JPanel interno = new JPanel();
		interno.setBounds(42, 30, 600, 650);
		externo.add(interno);
		interno.setBackground(new Color(20, 3, 80));
		interno.setLayout(null);

		JLabel lblLogoPJP = new JLabel();
		ImageIcon iconePjp = new ImageIcon("resources/Images/pjp.png");
		Image imgPjp = iconePjp.getImage().getScaledInstance(360, 260, Image.SCALE_SMOOTH);

		lblLogoPJP.setIcon(new ImageIcon(imgPjp));
		lblLogoPJP.setBounds(113, 11, 360, 140);
		interno.add(lblLogoPJP);

		JLabel lblLogoSenac = new JLabel();
		ImageIcon iconeSenac = new ImageIcon("resources/Images/Senac_logo.png");
		Image imgSenac = iconeSenac.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
		lblLogoSenac.setIcon(new ImageIcon(imgSenac));
		lblLogoSenac.setBounds(210, 530, 150, 100);
		interno.add(lblLogoSenac);

		JTextField txtCpf = new JTextField();
		txtCpf.setFont(new Font("Dialog", Font.PLAIN, 20));
		txtCpf.setBounds(30, 320, 520, 40);
		txtCpf.setBorder(null);

		txtCpf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String texto = txtCpf.getText();

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

		JLabel lblEmail = new JLabel("CPF");
		lblEmail.setFont(new Font("Times New Roman", Font.PLAIN, 22));
		lblEmail.setHorizontalAlignment(SwingConstants.CENTER);
		lblEmail.setForeground(new Color(255, 255, 255));
		lblEmail.setBounds(12, 284, 80, 40);
		interno.add(lblEmail);

		JLabel lblSenha = new JLabel("Senha");
		lblSenha.setFont(new Font("Times New Roman", Font.PLAIN, 22));
		lblSenha.setHorizontalAlignment(SwingConstants.LEFT);
		lblSenha.setForeground(new Color(255, 255, 255));
		lblSenha.setBounds(30, 371, 100, 40);
		interno.add(lblSenha);

		JLabel lblAcesso = new JLabel("Acesse a sua conta");
		lblAcesso.setFont(new Font("Mongolian Baiti", Font.BOLD, 28));
		lblAcesso.setHorizontalAlignment(SwingConstants.CENTER);
		lblAcesso.setForeground(new Color(255, 255, 255));
		lblAcesso.setBounds(90, 150, 416, 100);
		interno.add(lblAcesso);

		JLabel lblCredenciais = new JLabel("Informe as suas credenciais para acessar o sistema");
		lblCredenciais.setFont(new Font("Montserrat", Font.BOLD, 16));
		lblCredenciais.setHorizontalAlignment(SwingConstants.CENTER);
		lblCredenciais.setForeground(new Color(255, 255, 255));
		lblCredenciais.setBounds(80, 200, 416, 100);
		interno.add(lblCredenciais);

		pfSenha = new JPasswordField();
		pfSenha.setFont(new Font("Dialog", Font.PLAIN, 20));
		pfSenha.setBorder(null);
		pfSenha.setBounds(30, 410, 495, 40);
		interno.add(pfSenha);
		echoChar = pfSenha.getEchoChar();

		JLabel lblEye = new JLabel("👁");
		lblEye.setFont(new Font("Dialog", Font.BOLD, 22));
		lblEye.setAutoscrolls(true);
		lblEye.setOpaque(true);
		lblEye.setBackground(Color.WHITE);
		lblEye.setForeground(Color.BLACK);
		lblEye.setSize(25, 40);
		lblEye.setLocation(525, 410);
		lblEye.setCursor(new Cursor(Cursor.HAND_CURSOR));

		lblEye.addMouseListener(new MouseAdapter() {
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

		JButton btnEntrar = new JButton("ENTRAR");
		btnEntrar.setFont(new Font("Montserrat", Font.BOLD, 18));
		btnEntrar.setBackground(new Color(0, 128, 255));
		btnEntrar.setForeground(new Color(255, 255, 255));
		btnEntrar.setBounds(45, 480, 480, 45);
		btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnEntrar.setFocusPainted(false);
		btnEntrar.setBorderPainted(false);
		interno.add(btnEntrar);
	}
}
