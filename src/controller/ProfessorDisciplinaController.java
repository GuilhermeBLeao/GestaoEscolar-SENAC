package controller;

import dao.DisciplinaDAO;
import dao.ProfessorDAO;
import dao.ProfessorDisciplinaDAO;
import database.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Disciplina;
import model.Professor;
import model.ProfessorDisciplina;

public class ProfessorDisciplinaController {
	@FunctionalInterface
	private interface AcaoTransacional<T> {
		T executar(Connection conn) throws SQLException;
	}

	private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			conn.setAutoCommit(false);
			try {
				T resultado = acao.executar(conn);
				conn.commit();
				return resultado;
			} catch (IllegalArgumentException e) {
				try {
					conn.rollback();
				} catch (SQLException rollbackEx) {
					e.addSuppressed(rollbackEx);
				}
				throw e;
			} catch (SQLException | RuntimeException e) {
				try {
					conn.rollback();
				} catch (SQLException rollbackEx) {
					e.addSuppressed(rollbackEx);
				}
				throw new RuntimeException(mensagemOperacao, e);
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
		}
	}

	private void validarNaoNula(ProfessorDisciplina profDisciplina) {
		if (profDisciplina == null)
			throw new IllegalArgumentException("Relação Professor-Disciplina não pode ser nula.");
	}

	private void validarCamposBase(ProfessorDisciplina profDisciplina, Professor professor, Disciplina disciplina) {
		if (profDisciplina.getIdProfessor() <= 0)
			throw new IllegalArgumentException("ID do professor é inválido.");
		if (profDisciplina.getIdDisciplina() <= 0)
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		if (profDisciplina.getDataVinculacao() == null)
			throw new IllegalArgumentException("Data de vinculação é obrigatória.");
		if (professor == null || !professor.isAtivo())
			throw new IllegalArgumentException("Professor não encontrado ou inativo.");
		if (disciplina == null || !disciplina.isAtivo())
			throw new IllegalArgumentException("Disciplina não encontrada ou inativa.");
	}

	private void validarDataVinculacao(LocalDate dataVinculacao) {
		if (dataVinculacao.isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data de vinculação não pode ser futura.");
	}

	private void validarVinculacaoDuplicada(int idProfessor, int idDisciplina, Connection conn) throws SQLException {
		ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
		if (dao.existeVinculacao(idProfessor, idDisciplina))
			throw new IllegalArgumentException("Este professor já está vinculado a esta disciplina.");
	}

	public void cadastrarDisciplina(Disciplina disciplina) {
		if (disciplina == null)
			throw new IllegalArgumentException("Disciplina não pode ser nula.");
		executarEmTransacao(conn -> {
			DisciplinaController disciplinaCtrl = new DisciplinaController();
			disciplinaCtrl.salvarDisciplina(conn, disciplina);
			return null;
		}, "Erro ao cadastrar disciplina.");
	}

	public void vincularProfessorDisciplina(ProfessorDisciplina profDisciplina) {
		validarNaoNula(profDisciplina);
		executarEmTransacao(conn -> {
			ProfessorDAO profDAO = new ProfessorDAO(conn);
			Professor professor = profDAO.buscarPorId(profDisciplina.getIdProfessor());
			DisciplinaDAO discDAO = new DisciplinaDAO(conn);
			Disciplina disciplina = discDAO.buscarPorId(profDisciplina.getIdDisciplina());
			validarCamposBase(profDisciplina, professor, disciplina);
			validarDataVinculacao(profDisciplina.getDataVinculacao());
			validarVinculacaoDuplicada(profDisciplina.getIdProfessor(), profDisciplina.getIdDisciplina(), conn);
			ProfessorDisciplinaDAO profDiscDAO = new ProfessorDisciplinaDAO(conn);
			profDiscDAO.inserir(profDisciplina);
			return null;
		}, "Erro ao vincular professor à disciplina.");
	}

	public List<ProfessorDisciplina> obterDisciplinasProfessor(int idProfessor) {
		if (idProfessor <= 0)
			throw new IllegalArgumentException("ID do professor inválido.");
		return executarEmTransacao(conn -> {
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			return dao.buscarPorProfessor(idProfessor);
		}, "Erro ao obter disciplinas do professor.");
	}

	public List<ProfessorDisciplina> obterProfessoresDisciplina(int idDisciplina) {
		if (idDisciplina <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");
		return executarEmTransacao(conn -> {
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			return dao.buscarPorDisciplina(idDisciplina);
		}, "Erro ao obter professores da disciplina.");
	}

	public boolean desvincularProfessorDisciplina(int idProfessorDisciplina) {
		if (idProfessorDisciplina <= 0)
			throw new IllegalArgumentException("ID da relação Professor-Disciplina inválido.");
		return executarEmTransacao(conn -> {
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			ProfessorDisciplina profDisc = dao.buscarPorId(idProfessorDisciplina);
			if (profDisc == null)
				throw new IllegalArgumentException("Vinculação Professor-Disciplina não encontrada.");
			return dao.inativar(idProfessorDisciplina);
		}, "Erro ao desvincular professor da disciplina.");
	}

	public ProfessorDisciplina obterVinculacao(int idProfessorDisciplina) {
		if (idProfessorDisciplina <= 0)
			throw new IllegalArgumentException("ID da relação Professor-Disciplina inválido.");
		return executarEmTransacao(conn -> {
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			return dao.buscarPorId(idProfessorDisciplina);
		}, "Erro ao obter vinculação Professor-Disciplina.");
	}
}