package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.AvisoDAO;
import dao.ChamadoDAO;
import database.ConnectionFactory;
import model.Aviso;
import model.Usuario;
import util.DadosSistema;
import util.SessaoUsuario;

public class TelaInicialFuncionario extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Color corExterna = new Color(27, 0, 69);
	private final Color corInterna = new Color(38, 2, 92);
	private final Color corBorda = new Color(120, 70, 220);
	private final Color corLabel = new Color(255, 120, 220);
	private final Color corCampo = new Color(25, 6, 75);
	private static final Color ROXO = new Color(91, 45, 104);
	private final Color textos = Color.WHITE;

	private final int larguraInterno;
	private final int alturaInterno;
	private final int margem;

	private Calendar mesAtual = Calendar.getInstance();

	private JPanel gradeDias;
	private JLabel lblMesAno;

	private int atendimentosHoje;
	private int chamadosAbertos;

	private List<Aviso> avisosDoFuncionario = new ArrayList<>();

	public TelaInicialFuncionario() {
		setTitle("Funcionário");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Rectangle areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		margem = 30;
		larguraInterno = areaUtil.width - (margem * 2) + 20;
		alturaInterno = areaUtil.height - (margem * 2);

		setMaximizedBounds(areaUtil);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1200, 720));
		setResizable(false);
		carregarDadosDoBanco();

		JPanel externo = new JPanel();
		externo.setBackground(corExterna);
		externo.setBorder(new EmptyBorder(5, 5, 5, 5));
		externo.setLayout(null);
		setContentPane(externo);

		JPanel interno = new JPanel();
		interno.setBounds(25, 20, larguraInterno, alturaInterno);
		interno.setBackground(corInterna);
		interno.setLayout(null);
		externo.add(interno);

		JLabel lblLogoSoloFirme = new JLabel();

		java.net.URL logoUrl = getClass().getResource("/Images/Solo-Firme.png");

		if (logoUrl != null) {
		    ImageIcon iconeSoloFirme = new ImageIcon(logoUrl);
		    Image imgPjp = iconeSoloFirme.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
		    lblLogoSoloFirme.setIcon(new ImageIcon(imgPjp));
		} else {
		    System.err.println("Erro: Imagem não encontrada!");
		}

		lblLogoSoloFirme.setBounds(11, 6, 220, 220);
		interno.add(lblLogoSoloFirme);

		JSeparator separador = new JSeparator();
		separador.setOrientation(SwingConstants.VERTICAL);
		separador.setBounds(235, 0, 2, alturaInterno);
		separador.setForeground(corBorda);
		interno.add(separador);

		String[] botoes = { "Alunos", "Avisos", "Boletim", "Cadastros", "Meus Dados", "Relatórios", "Usuários", "Alterar Dados Usuário", "Vínculo Prof X Disciplina"};

		int posicaoY = 200;
		int alturaBotao = 42;
		int espacamento = 10;

		for (String texto : botoes) {
			JButton botao = criarBotaoMenu(texto);
			botao.setBounds(17, posicaoY, 200, alturaBotao);
			botao.addActionListener(e -> {
				switch (texto) {
				case "Alunos":
					new AlunosSecretaria().setVisible(true);
					break;
				case "Avisos":
					new AvisosSecretaria().setVisible(true);
					break;
				case "Boletim":
					new Boletim().setVisible(true);
					break;
				case "Cadastros":
					new MenuCadastro().setVisible(true);
					break;
				case "Meus Dados":
					new MeusDadosFuncionario().setVisible(true);
					break;
				case "Relatórios":
					new Relatorio().setVisible(true);
					break;
				case "Usuários":
					new view.Usuario().setVisible(true);
					break;
				case "Alterar Dados Usuário":
					new AlterarCpfRg().setVisible(true);
					break;
				case "Vínculo Prof X Disciplina":
					new VinculoProfessorDisciplina().setVisible(true);
				default:
					break;
				}
			});

			interno.add(botao);
			posicaoY += alturaBotao + espacamento;
		}

		JButton btnSair = new JButton("Sair");
		btnSair.setBounds(17, alturaInterno - 90, 200, 60);
		estilizarBotao(btnSair);
		btnSair.addActionListener(e -> {
			SessaoUsuario.encerrarSessao();
			dispose();
			System.exit(0);
		});
		interno.add(btnSair);

		String nomeFuncionario = obterNomeFuncionario();
		JLabel usuario = new JLabel("Olá, " + nomeFuncionario);
		usuario.setForeground(textos);
		usuario.setFont(new Font("Segoe UI", Font.BOLD, 20));
		usuario.setBounds(larguraInterno - 320, 30, 280, 30);
		interno.add(usuario);

		JLabel bemVindo = new JLabel("Painel administrativo");
		bemVindo.setForeground(textos);
		bemVindo.setFont(new Font("Segoe UI", Font.BOLD, 28));
		bemVindo.setBounds(335, 90, 500, 40);
		interno.add(bemVindo);

		JLabel nome = new JLabel(nomeFuncionario);
		nome.setForeground(corLabel);
		nome.setFont(new Font("Segoe UI", Font.BOLD, 48));
		nome.setBounds(335, 135, 720, 65);
		interno.add(nome);

		JLabel descricao = new JLabel("Gerencie atendimentos, comunicados e rotinas internas da instituição.");
		descricao.setForeground(textos);
		descricao.setFont(new Font("Segoe UI", Font.PLAIN, 21));
		descricao.setBounds(345, 215, 950, 30);
		interno.add(descricao);

		JPanel cardAtendimentos = criarCard("Atendimentos Hoje", String.valueOf(atendimentosHoje), "Chamados registrados hoje");
		cardAtendimentos.setBounds(350, 290, 370, 140);
		interno.add(cardAtendimentos);

		JPanel cardChamados = criarCard("Chamados Abertos", String.valueOf(chamadosAbertos), "Solicitações pendentes");
		cardChamados.setBounds(740, 290, 370, 140);
		interno.add(cardChamados);

		JPanel cardAvisos = criarCard("Avisos Ativos", String.valueOf(avisosDoFuncionario.size()), "Comunicados disponíveis");
		cardAvisos.setBounds(1130, 290, 370, 140);
		interno.add(cardAvisos);

		JPanel chamadosRecentes = criarPainelTitulo("Chamados Recentes");
		chamadosRecentes.setBounds(350, 460, 760, 360);
		interno.add(chamadosRecentes);
		preencherChamadosRecentes(chamadosRecentes);

		JPanel alertas = criarPainelTitulo("Alertas Internos");
		alertas.setBounds(1130, 460, 370, 360);
		interno.add(alertas);
		preencherAlertasInternos(alertas);

		JPanel calendario = criarCalendarioPremiumPersonalizado();
		calendario.setBounds(larguraInterno - 360, 290, 350, 530);
		interno.add(calendario);

		JPanel rodape = new JPanel();
		rodape.setLayout(null);
		rodape.setBackground(corCampo);
		rodape.setBorder(new LineBorder(corBorda, 1, true));
		rodape.setBounds(335, alturaInterno - 140, larguraInterno - 350, 120);
		interno.add(rodape);

		JLabel fique = new JLabel("Central de trabalho");
		fique.setForeground(textos);
		fique.setFont(new Font("Segoe UI", Font.BOLD, 28));
		fique.setBounds(45, 20, 500, 30);
		rodape.add(fique);

		JLabel texto = new JLabel("Consulte as informações administrativas e acompanhe as solicitações registradas no sistema.");
		texto.setForeground(textos);
		texto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		texto.setBounds(45, 60, 950, 30);
		rodape.add(texto);
		setVisible(true);
	}

	private String obterNomeFuncionario() {
		Usuario usuario = SessaoUsuario.getUsuarioLogado();

		if (usuario == null) {
			throw new IllegalStateException("Nenhum usuário autenticado na sessão atual.");
		}

		String nome = DadosSistema.nomeUsuarioLogado();

		if (nome == null || nome.isBlank()) {
			return "Funcionário";
		}
		return nome.trim();
	}

	private void carregarDadosDoBanco() {
		Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();

		if (usuarioLogado == null) {
			throw new IllegalStateException("Nenhum usuário autenticado na sessão atual.");
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			ChamadoDAO chamadoDAO = new ChamadoDAO(conn);

			atendimentosHoje = chamadoDAO.contarAtendimentosHoje();
			chamadosAbertos = chamadoDAO.contarAbertos();

			AvisoDAO avisoDAO = new AvisoDAO(conn);
			avisosDoFuncionario = avisoDAO.listar().stream().filter(this::avisoDisponivelParaFuncionario).toList();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar os dados do painel: " + e.getMessage(), "Erro de banco de dados", JOptionPane.ERROR_MESSAGE);
			throw new IllegalStateException("Erro ao carregar os dados do painel.", e);
		}
	}

	private boolean avisoDisponivelParaFuncionario(Aviso aviso) {
		if (aviso == null || aviso.getPublico() == null) {
			return false;
		}

		String publico = aviso.getPublico().trim().toLowerCase(Locale.ROOT);
		return publico.contains("funcionario") || publico.contains("funcionário") || publico.contains("todos");
	}

	private void preencherChamadosRecentes(JPanel painel) {
		List<ChamadoResumo> chamados = listarChamadosRecentes();

		if (chamados.isEmpty()) {
			JLabel semChamados = new JLabel("Nenhum chamado registrado.");
			semChamados.setForeground(textos);
			semChamados.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			semChamados.setBounds(30, 80, 650, 25);
			painel.add(semChamados);
			return;
		}

		int y = 80;

		for (ChamadoResumo chamado : chamados) {
			adicionarChamado(painel, chamado, y);
			y += 65;

			if (y > 320) {
				break;
			}
		}
	}

	private List<ChamadoResumo> listarChamadosRecentes() {
		List<ChamadoResumo> chamados = new ArrayList<>();

		String sql = """
					SELECT
					    id_chamado,
					    assunto,
					    status,
					    data_criacao
					FROM chamado
					ORDER BY datetime(data_criacao) DESC
					LIMIT 5
				""";

		try (Connection conn = ConnectionFactory.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				int idChamado = rs.getInt("id_chamado");
				String assunto = rs.getString("assunto");
				String status = rs.getString("status");
				String dataCriacao = rs.getString("data_criacao");
				chamados.add(new ChamadoResumo(idChamado, assunto, status, dataCriacao));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Erro ao consultar os chamados recentes.", e);
		}
		return chamados;
	}

	private void adicionarChamado(JPanel painel, ChamadoResumo chamado, int y) {
		JLabel titulo = new JLabel("#" + chamado.idChamado + " - " + limitarTexto(chamado.assunto, 50));
		titulo.setForeground(textos);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
		titulo.setBounds(30, y, 650, 25);
		painel.add(titulo);

		JLabel detalhes = new JLabel("Status: " + valorOuNaoInformado(chamado.status) + "    |    Criado em: "
				+ valorOuNaoInformado(chamado.dataCriacao));
		detalhes.setForeground(corLabel);
		detalhes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		detalhes.setBounds(30, y + 28, 650, 22);
		painel.add(detalhes);
	}

	private String limitarTexto(String texto, int limite) {
		if (texto == null || texto.isBlank()) {
			return "Sem assunto";
		}

		if (texto.length() <= limite) {
			return texto;
		}
		return texto.substring(0, limite - 3) + "...";
	}

	private String valorOuNaoInformado(String valor) {
		if (valor == null || valor.isBlank()) {
			return "Não informado";
		}
		return valor;
	}

	private void preencherAlertasInternos(JPanel alertas) {
		if (avisosDoFuncionario.isEmpty() && chamadosAbertos == 0) {
			JLabel semAlertas = new JLabel("Nenhum alerta no momento.");
			semAlertas.setForeground(textos);
			semAlertas.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			semAlertas.setBounds(30, 80, 320, 25);
			alertas.add(semAlertas);
			return;
		}

		int y = 80;

		if (chamadosAbertos > 0) {
			adicionarAviso(alertas, "Chamados abertos", chamadosAbertos + " solicitações aguardando atendimento.", y);
			y += 90;
		}
		int maxAvisos = chamadosAbertos > 0 ? 2 : 3;

		for (int i = 0; i < Math.min(maxAvisos, avisosDoFuncionario.size()); i++) {
			Aviso aviso = avisosDoFuncionario.get(i);
			adicionarAviso(alertas, aviso.getTitulo(), aviso.getDescricao(), y);
			y += 90;
		}
	}

	private void estilizarBotao(JButton botao) {
		botao.setFont(new Font("Segoe UI", Font.BOLD, 17));
		botao.setForeground(textos);
		botao.setBackground(corCampo);
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private JPanel criarCalendarioPremiumPersonalizado() {
		JPanel calendario = criarPainelArredondado(corCampo, corBorda, 28);
		calendario.setLayout(null);

		JLabel titulo = new JLabel("Calendário");
		titulo.setForeground(Color.WHITE);
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
		titulo.setBounds(30, 18, 250, 35);
		calendario.add(titulo);

		JLabel subtitulo = new JLabel("Avisos institucionais");
		subtitulo.setForeground(textos);
		subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		subtitulo.setBounds(32, 55, 220, 22);
		calendario.add(subtitulo);

		JPanel linha = new JPanel();
		linha.setBackground(corLabel);
		linha.setBounds(30, 88, 280, 3);
		calendario.add(linha);

		JButton btnAnterior = criarBotaoCalendario("‹");
		btnAnterior.setBounds(30, 105, 42, 34);
		btnAnterior.setBackground(corCampo);
		btnAnterior.setBorder(new LineBorder(corBorda, 1, true));
		calendario.add(btnAnterior);

		lblMesAno = new JLabel("", SwingConstants.CENTER);
		lblMesAno.setForeground(Color.WHITE);
		lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 17));
		lblMesAno.setBounds(80, 105, 200, 34);
		calendario.add(lblMesAno);

		JButton btnProximo = criarBotaoCalendario("›");
		btnProximo.setBounds(295, 105, 42, 34);
		btnProximo.setBackground(corCampo);
		btnProximo.setBorder(new LineBorder(corBorda, 1, true));
		calendario.add(btnProximo);

		JPanel semana = new JPanel(new GridLayout(1, 7, 6, 0));
		semana.setOpaque(false);
		semana.setBounds(30, 155, 295, 25);
		calendario.add(semana);

		String[] diasSemana = { "D", "S", "T", "Q", "Q", "S", "S" };

		for (String dia : diasSemana) {
			JLabel lbl = new JLabel(dia, SwingConstants.CENTER);
			lbl.setForeground(textos);
			lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
			semana.add(lbl);
		}
		gradeDias = new JPanel(new GridLayout(6, 7, 6, 6));
		gradeDias.setOpaque(false);
		gradeDias.setBounds(30, 185, 295, 210);
		calendario.add(gradeDias);

		JLabel eventosTitulo = new JLabel("Avisos");
		eventosTitulo.setForeground(Color.WHITE);
		eventosTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
		eventosTitulo.setBounds(30, 375, 250, 25);
		calendario.add(eventosTitulo);
		preencherCompromissos(calendario);

		btnAnterior.addActionListener(e -> {
			mesAtual.add(Calendar.MONTH, -1);
			atualizarCalendario();
		});
		btnProximo.addActionListener(e -> {
			mesAtual.add(Calendar.MONTH, 1);
			atualizarCalendario();
		});
		atualizarCalendario();
		return calendario;
	}

	private void preencherCompromissos(JPanel calendario) {
		if (avisosDoFuncionario.isEmpty()) {
			JLabel semCompromissos = new JLabel("Nenhum aviso cadastrado.");
			semCompromissos.setForeground(textos);
			semCompromissos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
			semCompromissos.setBounds(30, 410, 295, 22);
			calendario.add(semCompromissos);
			return;
		}
		int y = 410;

		for (int i = 0; i < Math.min(3, avisosDoFuncionario.size()); i++) {
			Aviso aviso = avisosDoFuncionario.get(i);
			String dataFormatada;

			if (aviso.getDataAviso() == null) {
				dataFormatada = "--/--";
			} else {
				dataFormatada = String.format("%02d/%02d", aviso.getDataAviso().getDayOfMonth(), aviso.getDataAviso().getMonthValue());
			}
			adicionarEventoCalendario(calendario, dataFormatada, aviso.getTitulo(), y);
			y += 35;
		}
	}

	private void atualizarCalendario() {
		gradeDias.removeAll();
		String[] meses = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro",
				"Outubro", "Novembro", "Dezembro" };
		int mes = mesAtual.get(Calendar.MONTH);
		int ano = mesAtual.get(Calendar.YEAR);
		lblMesAno.setText(meses[mes] + " " + ano);

		Calendar calendario = Calendar.getInstance();
		calendario.set(Calendar.YEAR, ano);
		calendario.set(Calendar.MONTH, mes);
		calendario.set(Calendar.DAY_OF_MONTH, 1);
		int primeiroDiaSemana = calendario.get(Calendar.DAY_OF_WEEK);
		int totalDias = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);

		Calendar hoje = Calendar.getInstance();
		int espacosAntes = primeiroDiaSemana - 1;

		for (int i = 0; i < espacosAntes; i++) {
			gradeDias.add(criarCelulaVazia());
		}
		for (int dia = 1; dia <= totalDias; dia++) {
			boolean ehHoje = dia == hoje.get(Calendar.DAY_OF_MONTH) && mes == hoje.get(Calendar.MONTH) && ano == hoje.get(Calendar.YEAR);
			boolean temEvento = existeAvisoNoDia(dia, mes, ano);
			gradeDias.add(criarCelulaDia(dia, ehHoje, temEvento));
		}
		int totalComponentes = espacosAntes + totalDias;
		while (totalComponentes < 42) {
			gradeDias.add(criarCelulaVazia());
			totalComponentes++;
		}
		gradeDias.revalidate();
		gradeDias.repaint();
	}

	private boolean existeAvisoNoDia(int dia, int mesCalendar, int ano) {
		int mesLocalDate = mesCalendar + 1;
		for (Aviso aviso : avisosDoFuncionario) {
			LocalDate data = aviso.getDataAviso();
			if (data != null && data.getDayOfMonth() == dia && data.getMonthValue() == mesLocalDate && data.getYear() == ano) {
				return true;
			}
		}
		return false;
	}

	private JPanel criarCelulaDia(int dia, boolean ehHoje, boolean temEvento) {
		Color fundoNormal = new Color(31, 10, 90);
		Color fundoHover = new Color(55, 20, 135);
		Color fundoHoje = new Color(255, 120, 220);
		JPanel celula = criarPainelArredondado(ehHoje ? fundoHoje : fundoNormal, temEvento ? corLabel : new Color(60, 140, 150), 16);
		celula.setLayout(null);
		celula.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JLabel numero = new JLabel(String.valueOf(dia), SwingConstants.CENTER);
		numero.setForeground(Color.WHITE);
		numero.setFont(new Font("Segoe UI", Font.BOLD, 13));
		numero.setBounds(5, 4, 36, 20);
		celula.add(numero);

		if (temEvento) {
			JPanel ponto = new JPanel();
			ponto.setBackground(ehHoje ? Color.WHITE : corLabel);
			ponto.setBounds(20, 27, 6, 6);
			celula.add(ponto);
		}
		celula.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!ehHoje) {
					celula.setBackground(fundoHover);
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (!ehHoje) {
					celula.setBackground(fundoNormal);
				}
			}
		});
		return celula;
	}

	private JPanel criarCelulaVazia() {
		JPanel vazio = new JPanel();
		vazio.setOpaque(false);
		return vazio;
	}

	private JButton criarBotaoCalendario(String texto) {
		JButton botao = new JButton(texto);
		botao.setForeground(Color.WHITE);
		botao.setBackground(corCampo);
		botao.setFont(new Font("Segoe UI", Font.BOLD, 24));
		botao.setBorder(new LineBorder(corBorda, 1, true));
		botao.setFocusPainted(false);
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return botao;
	}

	private void adicionarEventoCalendario(JPanel painel, String data, String titulo, int y) {
		JPanel evento = criarPainelArredondado(corCampo, corBorda, 18);
		evento.setLayout(null);
		evento.setBounds(30, y, 295, 32);

		JLabel lbData = new JLabel(data);
		lbData.setForeground(corLabel);
		lbData.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lbData.setBounds(17, 5, 55, 22);
		evento.add(lbData);

		JLabel lbTitulo = new JLabel(limitarTexto(titulo, 30));
		lbTitulo.setForeground(Color.WHITE);
		lbTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lbTitulo.setBounds(80, 5, 210, 22);
		evento.add(lbTitulo);
		painel.add(evento);
	}

	private JPanel criarPainelArredondado(Color fundo, Color borda, int raio) {
		return new JPanel() {
			private static final long serialVersionUID = 1L;
			{
				setOpaque(false);
				setBackground(fundo);
			}
			
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getBackground());
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
				g2.setColor(borda);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
				g2.dispose();
				super.paintComponent(g);
			}
		};
	}

	private JPanel criarCard(String titulo, String valor, String descricao) {
		JPanel card = criarPainelArredondado(corCampo, corBorda, 24);
		card.setLayout(null);

		JLabel lbTitulo = new JLabel(titulo, SwingConstants.CENTER);
		lbTitulo.setForeground(textos);
		lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lbTitulo.setBounds(15, 15, 350, 25);
		card.add(lbTitulo);

		JLabel lbValor = new JLabel(valor, SwingConstants.CENTER);
		lbValor.setForeground(textos);
		lbValor.setFont(new Font("Segoe UI", Font.BOLD, 42));
		lbValor.setBounds(15, 45, 350, 45);
		card.add(lbValor);

		JLabel lbDescricao = new JLabel(descricao, SwingConstants.CENTER);
		lbDescricao.setForeground(corLabel);
		lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lbDescricao.setBounds(15, 100, 350, 20);
		card.add(lbDescricao);
		return card;
	}

	private JPanel criarPainelTitulo(String titulo) {
		JPanel painel = criarPainelArredondado(corCampo, corBorda, 24);
		painel.setLayout(null);

		JLabel label = new JLabel(titulo);
		label.setForeground(textos);
		label.setFont(new Font("Segoe UI", Font.BOLD, 28));
		label.setBounds(35, 20, 500, 30);
		painel.add(label);
		return painel;
	}

	private void adicionarAviso(JPanel painel, String titulo, String descricao, int y) {
		JLabel lbTitulo = new JLabel(limitarTexto(titulo, 28));
		lbTitulo.setForeground(textos);
		lbTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
		lbTitulo.setBounds(30, y, 350, 30);
		painel.add(lbTitulo);

		JLabel lbDescricao = new JLabel(limitarTexto(descricao, 42));
		lbDescricao.setForeground(textos);
		lbDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lbDescricao.setBounds(30, y + 35, 350, 20);
		painel.add(lbDescricao);
	}

	private JButton criarBotaoMenu(String texto) {
		JButton botao = new JButton(texto);
		botao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		botao.setForeground(textos);
		botao.setBackground(ROXO);
		botao.setFocusPainted(false);
		botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return botao;
	}

	private static class ChamadoResumo {
		private final int idChamado;
		private final String assunto;
		private final String status;
		private final String dataCriacao;

		private ChamadoResumo(int idChamado, String assunto, String status, String dataCriacao) {
			this.idChamado = idChamado;
			this.assunto = assunto;
			this.status = status;
			this.dataCriacao = dataCriacao;
		}
	}
}