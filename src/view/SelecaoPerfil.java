package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class SelecaoPerfil extends JDialog {

	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private final Color textos = Color.WHITE;

	private String perfilSelecionado = null;

	public SelecaoPerfil(JFrame parent, String nomeUsuario, boolean temProfessor, boolean temResponsavel) {

		super(parent, "Seleção de Perfil", Dialog.ModalityType.APPLICATION_MODAL);

		setSize(650, 450);
		setLocationRelativeTo(parent);
		setResizable(false);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel fundo = new JPanel(null);

		fundo.setBackground(corExterna);

		fundo.setBorder(new LineBorder(corBorda, 2));

		setContentPane(fundo);

		JPanel topo = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				GradientPaint gp = new GradientPaint(0, 0, new Color(80, 20, 170), getWidth(), getHeight(),
						new Color(190, 35, 170));

				g2.setPaint(gp);

				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

				g2.dispose();
			}
		};

		topo.setLayout(null);
		topo.setOpaque(false);

		topo.setBounds(30, 30, 560, 120);

		fundo.add(topo);

		JLabel titulo = new JLabel("Selecione seu perfil", SwingConstants.CENTER);

		titulo.setForeground(textos);

		titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));

		titulo.setBounds(20, 22, 520, 40);

		topo.add(titulo);

		JLabel subtitulo = new JLabel("Encontramos mais de um acesso para este CPF.", SwingConstants.CENTER);

		subtitulo.setForeground(new Color(245, 225, 255));

		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		subtitulo.setBounds(20, 68, 520, 28);

		topo.add(subtitulo);

		JLabel nome = new JLabel(nomeUsuario, SwingConstants.CENTER);

		nome.setForeground(corLabel);

		nome.setFont(new Font("Segoe UI", Font.BOLD, 22));

		nome.setBounds(30, 175, 560, 35);

		fundo.add(nome);

		JLabel info = new JLabel("Escolha como deseja entrar no sistema:", SwingConstants.CENTER);

		info.setForeground(textos);

		info.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		info.setBounds(30, 215, 560, 30);

		fundo.add(info);

		int quantidadeBotoes = 0;

		if (temProfessor) {
			quantidadeBotoes++;
		}

		if (temResponsavel) {
			quantidadeBotoes++;
		}

		int larguraBotao = 210;
		int alturaBotao = 70;
		int espacamento = 30;

		int larguraTotal = (quantidadeBotoes * larguraBotao) + ((quantidadeBotoes - 1) * espacamento);

		int xInicial = (620 - larguraTotal) / 2;

		if (temProfessor) {

			JButton btnProfessor = criarBotaoPerfil("Professor", "Acessar área pedagógica");

			btnProfessor.setBounds(xInicial, 270, larguraBotao, alturaBotao);

			estilizarBotao(btnProfessor);

			btnProfessor.addActionListener(e -> {

				perfilSelecionado = "PROFESSOR";

				dispose();
			});

			fundo.add(btnProfessor);

			xInicial += larguraBotao + espacamento;
		}

		if (temResponsavel) {

			JButton btnResponsavel = criarBotaoPerfil("Responsável", "Acompanhar aluno");

			btnResponsavel.setBounds(xInicial, 270, larguraBotao, alturaBotao);

			estilizarBotao(btnResponsavel);

			btnResponsavel.addActionListener(e -> {

				perfilSelecionado = "RESPONSAVEL";

				dispose();
			});

			fundo.add(btnResponsavel);
		}

		JButton btnCancelar = new JButton("Cancelar");

		btnCancelar.setBounds(235, 365, 150, 35);

		estilizarBotao(btnCancelar);

		btnCancelar.addActionListener(e -> {

			perfilSelecionado = null;

			dispose();
		});

		fundo.add(btnCancelar);
	}

	private JButton criarBotaoPerfil(String titulo, String descricao) {

		JButton botao = new JButton("<html><center>" + "<span style='font-size:18px;'>" + titulo + "</span><br>"
				+ "<span style='font-size:11px;'>" + descricao + "</span>" + "</center></html>");

		estilizarBotao(botao);

		return botao;
	}

	public String getPerfilSelecionado() {
		return perfilSelecionado;
	}

	private void estilizarBotao(JButton botao) {

		botao.setFont(new Font("Segoe UI", Font.BOLD, 14));

		botao.setForeground(textos);
		botao.setBackground(corCampo);

		botao.setBorder(new LineBorder(corBorda));

		botao.setFocusPainted(false);

		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
}