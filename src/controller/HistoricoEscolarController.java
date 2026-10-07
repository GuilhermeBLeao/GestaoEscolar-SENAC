package controller;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.FuncionarioDAO;
import dao.NotaDAO;
import dao.PresencaDAO;
import dao.TurmaAlunoDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Disciplina;
import model.Funcionario;
import model.HistoricoEscolar;
import model.HistoricoEscolarItem;
import model.Nota;
import model.Presenca;
import model.Turma;
import model.TurmaAluno;
import model.Usuario;
import util.SessaoUsuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistoricoEscolarController {
	private static final double MEDIA_MINIMA_APROVACAO = 6.0;
	private static final double FREQUENCIA_MINIMA_APROVACAO = 75.0;

	public HistoricoEscolar montarHistorico(int alunoId, int anoLetivo) {
		if (alunoId <= 0) {
			throw new IllegalArgumentException("ID do aluno é inválido.");
		}

		if (anoLetivo < 2000) {
			throw new IllegalArgumentException("Ano letivo está vazio ou é anterior a 2000.");
		}

		Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();

		if (usuarioLogado == null) {
			throw new IllegalArgumentException("Nenhum usuário logado.");
		}

		if (usuarioLogado.getTipoUsuario() == null
				|| !usuarioLogado.getTipoUsuario().name().equalsIgnoreCase("SECRETARIA")) {
			throw new IllegalArgumentException("Apenas usuários da secretaria podem gerar histórico escolar.");
		}

		if (usuarioLogado.getFuncionarioId() <= 0) {
			throw new IllegalArgumentException("O usuário logado não está vinculado a um funcionário.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			AlunoDAO alunoDAO = new AlunoDAO(conn);
			FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
			TurmaDAO turmaDAO = new TurmaDAO(conn);
			DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
			NotaDAO notaDAO = new NotaDAO(conn);
			PresencaDAO presencaDAO = new PresencaDAO(conn);
			TurmaAlunoDAO turmaAlunoDAO = new TurmaAlunoDAO(conn);

			Aluno aluno = alunoDAO.buscarPorId(alunoId);

			if (aluno == null) {
				throw new IllegalArgumentException("Aluno não encontrado.");
			}

			Funcionario funcionario = funcionarioDAO.buscarPorId(usuarioLogado.getFuncionarioId());

			if (funcionario == null) {
				throw new IllegalArgumentException("Funcionário vinculado ao usuário logado não foi encontrado.");
			}

			List<TurmaAluno> historicoTurmas = turmaAlunoDAO.listarPorAluno(alunoId);
			List<Disciplina> disciplinas = listarDisciplinasDoHistorico(historicoTurmas, aluno, turmaDAO,
					disciplinaDAO);
			String descricaoTurmas = montarDescricaoTurmas(historicoTurmas, aluno, turmaDAO);

			if (disciplinas.isEmpty()) {
				throw new IllegalArgumentException("Não existem disciplinas cadastradas para as turmas do aluno.");
			}

			List<Nota> notasAluno = notaDAO.listarPorAluno(alunoId);
			List<Presenca> presencasAluno = presencaDAO.listarPorAluno(alunoId);

			List<HistoricoEscolarItem> itens = montarItensHistorico(disciplinas, notasAluno, presencasAluno);

			HistoricoEscolar historico = new HistoricoEscolar();

			historico.setAlunoId(aluno.getIdAluno());
			historico.setNomeAluno(aluno.getNome());
			historico.setMatricula(aluno.getMatricula());
			historico.setTurma(descricaoTurmas);
			historico.setAnoLetivo(anoLetivo);
			historico.setDataEmissao(LocalDate.now());
			historico.setItens(itens);
			historico.setNomeFuncionario(funcionario.getNome());
			historico.setCargoFuncionario(funcionario.getCargo());

			validarHistorico(historico);

			return historico;

		} catch (SQLException e) {
			throw new RuntimeException("Erro ao montar histórico escolar.", e);
		}
	}

	private List<Disciplina> listarDisciplinasDoHistorico(List<TurmaAluno> historicoTurmas, Aluno aluno,
			TurmaDAO turmaDAO, DisciplinaDAO disciplinaDAO) throws SQLException {
		Map<Integer, Disciplina> disciplinasPorId = new LinkedHashMap<>();

		if (historicoTurmas.isEmpty()) {
			for (Disciplina disciplina : disciplinaDAO.listarPorTurmaIncluindoInativas(aluno.getIdTurma())) {
				disciplinasPorId.putIfAbsent(disciplina.getIdDisciplina(), disciplina);
			}
			return new ArrayList<>(disciplinasPorId.values());
		}

		for (TurmaAluno turmaAluno : historicoTurmas) {
			Turma turma = turmaDAO.buscarPorId(turmaAluno.getTurmaId());
			if (turma == null) {
				throw new IllegalArgumentException("Turma do histórico do aluno não encontrada.");
			}

			for (Disciplina disciplina : disciplinaDAO.listarPorTurmaIncluindoInativas(turmaAluno.getTurmaId())) {
				disciplinasPorId.putIfAbsent(disciplina.getIdDisciplina(), disciplina);
			}
		}

		return new ArrayList<>(disciplinasPorId.values());
	}

	private String montarDescricaoTurmas(List<TurmaAluno> historicoTurmas, Aluno aluno, TurmaDAO turmaDAO)
			throws SQLException {
		Map<Integer, String> turmasPorId = new LinkedHashMap<>();

		if (historicoTurmas.isEmpty()) {
			Turma turmaAtual = turmaDAO.buscarPorId(aluno.getIdTurma());
			if (turmaAtual == null) {
				throw new IllegalArgumentException("Turma do aluno não encontrada.");
			}
			return turmaAtual.getDescricaoTurma();
		}

		for (TurmaAluno turmaAluno : historicoTurmas) {
			Turma turma = turmaDAO.buscarPorId(turmaAluno.getTurmaId());
			if (turma == null) {
				throw new IllegalArgumentException("Turma do histórico do aluno não encontrada.");
			}
			turmasPorId.putIfAbsent(turma.getIdTurma(), turma.getDescricaoTurma());
		}

		if (turmasPorId.size() == 1) {
			return turmasPorId.values().iterator().next();
		}

		return "Todas as turmas";
	}

	private List<HistoricoEscolarItem> montarItensHistorico(List<Disciplina> disciplinas, List<Nota> notasAluno,
			List<Presenca> presencasAluno) {
		List<HistoricoEscolarItem> itens = new ArrayList<>();

		for (Disciplina disciplina : disciplinas) {
			int disciplinaId = disciplina.getIdDisciplina();

			double media = calcularMedia(notasAluno, disciplinaId);
			int totalAulas = contarTotalAulas(presencasAluno, disciplinaId);
			int faltas = contarFaltasValidas(presencasAluno, disciplinaId);
			double frequencia = calcularFrequencia(totalAulas, faltas);
			String situacao = definirSituacao(media, totalAulas, frequencia);

			HistoricoEscolarItem item = new HistoricoEscolarItem();

			item.setDisciplina(disciplina.getDescricao());
			item.setMediaFinal(media);
			item.setTotalAulas(totalAulas);
			item.setFaltas(faltas);
			item.setFrequencia(frequencia);
			item.setSituacao(situacao);

			itens.add(item);
		}

		return itens;
	}

	private double calcularMedia(List<Nota> notasAluno, int disciplinaId) {
		double soma = 0.0;
		int quantidade = 0;

		for (Nota nota : notasAluno) {
			if (nota.getIdDisciplina() == disciplinaId) {
				soma += nota.getNota();
				quantidade++;
			}
		}

		if (quantidade == 0) {
			return 0.0;
		}

		return soma / quantidade;
	}

	private int contarTotalAulas(List<Presenca> presencasAluno, int disciplinaId) {
		int total = 0;

		for (Presenca presenca : presencasAluno) {
			if (presenca.getDisciplinaId() == disciplinaId) {
				total++;
			}
		}

		return total;
	}

	private int contarFaltasValidas(List<Presenca> presencasAluno, int disciplinaId) {
		int faltas = 0;

		for (Presenca presenca : presencasAluno) {
			if (presenca.getDisciplinaId() == disciplinaId && !presenca.isPresente() && !presenca.isFaltaAbonada()) {
				faltas++;
			}
		}

		return faltas;
	}

	private double calcularFrequencia(int totalAulas, int faltas) {
		if (totalAulas == 0) {
			return 0.0;
		}

		return ((double) (totalAulas - faltas) / totalAulas) * 100.0;
	}

	private String definirSituacao(double media, int totalAulas, double frequencia) {
		if (totalAulas == 0) {
			return "Em andamento";
		}

		if (media >= MEDIA_MINIMA_APROVACAO && frequencia >= FREQUENCIA_MINIMA_APROVACAO) {
			return "Aprovado";
		}

		return "Reprovado";
	}

	public String gerarTextoHistorico(HistoricoEscolar historico) {
		validarHistorico(historico);

		DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		StringBuilder texto = new StringBuilder();

		texto.append("HISTÓRICO ESCOLAR\n\n");

		texto.append("Aluno: ").append(historico.getNomeAluno()).append("\n");
		texto.append("Matrícula: ").append(historico.getMatricula()).append("\n");
		texto.append("Turma: ").append(historico.getTurma()).append("\n");
		texto.append("Ano letivo: ").append(historico.getAnoLetivo()).append("\n");
		texto.append("Data de emissão: ").append(historico.getDataEmissao().format(formato)).append("\n\n");

		texto.append("DISCIPLINAS\n\n");

		texto.append(String.format("%-25s %-12s %-12s %-10s %-12s %-15s%n", "Disciplina", "Média", "Aulas", "Faltas",
				"Frequência", "Situação"));

		texto.append("--------------------------------------------------------------------------------------\n");

		for (HistoricoEscolarItem item : historico.getItens()) {
			texto.append(String.format("%-25s %-12.2f %-12d %-10d %-12.2f %-15s%n", item.getDisciplina(),
					item.getMediaFinal(), item.getTotalAulas(), item.getFaltas(), item.getFrequencia(),
					item.getSituacao()));
		}

		texto.append("\n\n");
		texto.append("__________________________________\n");
		texto.append(historico.getNomeFuncionario()).append("\n");
		texto.append(historico.getCargoFuncionario()).append("\n");

		return texto.toString();
	}

	private void validarHistorico(HistoricoEscolar historico) {
		if (historico == null) {
			throw new IllegalArgumentException("Histórico escolar não pode ser nulo.");
		}

		if (historico.getAlunoId() <= 0) {
			throw new IllegalArgumentException("ID do aluno é inválido.");
		}

		if (historico.getNomeAluno() == null || historico.getNomeAluno().trim().isEmpty()) {
			throw new IllegalArgumentException("Nome do aluno é obrigatório.");
		}

		if (historico.getMatricula() == null || historico.getMatricula().trim().isEmpty()) {
			throw new IllegalArgumentException("Matrícula é obrigatória.");
		}

		if (historico.getTurma() == null || historico.getTurma().trim().isEmpty()) {
			throw new IllegalArgumentException("Turma é obrigatória.");
		}

		if (historico.getAnoLetivo() < 2000) {
			throw new IllegalArgumentException("Ano letivo está vazio ou é anterior a 2000.");
		}

		if (historico.getDataEmissao() == null) {
			throw new IllegalArgumentException("Data de emissão é obrigatória.");
		}

		if (historico.getItens() == null || historico.getItens().isEmpty()) {
			throw new IllegalArgumentException("Histórico precisa conter ao menos uma disciplina.");
		}

		for (HistoricoEscolarItem item : historico.getItens()) {
			validarItemHistorico(item);
		}

		if (historico.getNomeFuncionario() == null || historico.getNomeFuncionario().trim().isEmpty()) {
			throw new IllegalArgumentException("Nome do funcionário é obrigatório.");
		}

		if (historico.getCargoFuncionario() == null || historico.getCargoFuncionario().trim().isEmpty()) {
			throw new IllegalArgumentException("Cargo do funcionário é obrigatório.");
		}
	}

	private void validarItemHistorico(HistoricoEscolarItem item) {
		if (item == null) {
			throw new IllegalArgumentException("Item do histórico não pode ser nulo.");
		}

		if (item.getDisciplina() == null || item.getDisciplina().trim().isEmpty()) {
			throw new IllegalArgumentException("Disciplina é obrigatória.");
		}

		if (item.getMediaFinal() < 0 || item.getMediaFinal() > 10) {
			throw new IllegalArgumentException("Média final inválida.");
		}

		if (item.getTotalAulas() < 0) {
			throw new IllegalArgumentException("Total de aulas inválido.");
		}

		if (item.getFaltas() < 0) {
			throw new IllegalArgumentException("Número de faltas inválido.");
		}

		if (item.getFaltas() > item.getTotalAulas()) {
			throw new IllegalArgumentException("Número de faltas não pode ser maior que o total de aulas.");
		}

		if (item.getFrequencia() < 0 || item.getFrequencia() > 100) {
			throw new IllegalArgumentException("Frequência inválida.");
		}

		if (item.getSituacao() == null || item.getSituacao().trim().isEmpty()) {
			throw new IllegalArgumentException("Situação é obrigatória.");
		}
	}
}
