package controller;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.NotaDAO;
import database.ConnectionFactory;
import model.Nota;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NotaController {
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
				} catch (SQLException ex) {
					e.addSuppressed(ex);
				}
				throw e;

			} catch (SQLException | RuntimeException e) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					e.addSuppressed(ex);
				}
				throw new RuntimeException(mensagemOperacao, e);
			}

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
		}
	}

	private void validarNotaNaoNula(Nota nota) {
		if (nota == null)
			throw new IllegalArgumentException("Nota não pode ser nula.");
	}

	private void normalizarNota(Nota nota) {
		nota.setAtividade(tratarTexto(nota.getAtividade()));
	}

	private void validarParaCadastro(Nota nota) {
		validarCamposBase(nota);
	}

	private void validarParaAtualizacao(Nota notaAtualizada, Nota notaBanco) {
		validarCamposBase(notaAtualizada);
		validarChaveImutavel(notaAtualizada, notaBanco);
	}

	private void validarCamposBase(Nota nota) {
		if (nota.getIdDisciplina() <= 0)
			throw new IllegalArgumentException("ID da disciplina é obrigatório.");

		if (nota.getIdAluno() <= 0)
			throw new IllegalArgumentException("ID do aluno é obrigatório.");

		if (nota.getTrimestreId() <= 0)
			throw new IllegalArgumentException("ID do trimestre é obrigatório.");

		if (nota.getAtividade() == null || nota.getAtividade().isBlank())
			throw new IllegalArgumentException("Atividade é obrigatória.");

		if (nota.getAtividade().length() < 2 || nota.getAtividade().length() > 100)
			throw new IllegalArgumentException("Atividade deve ter entre 2 e 100 caracteres.");

		if (nota.getNota() < 0 || nota.getNota() > 10)
			throw new IllegalArgumentException("Nota deve estar entre 0 e 10.");

		if (nota.getDataLancamento() == null)
			throw new IllegalArgumentException("Data de lançamento da nota é obrigatória.");

		if (nota.getDataLancamento().isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data de lançamento da nota não pode ser futura.");
	}

	private void validarChaveImutavel(Nota notaAtualizada, Nota notaBanco) {
		if (notaAtualizada.getIdAluno() != notaBanco.getIdAluno()
				|| notaAtualizada.getIdDisciplina() != notaBanco.getIdDisciplina()
				|| !notaAtualizada.getAtividade().equals(notaBanco.getAtividade())) {
			throw new IllegalArgumentException(
					"Aluno, disciplina e atividade não podem ser alterados após o cadastro da nota.");
		}
	}

	private Nota mesclarDadosPermitidos(Nota notaBanco, Nota notaAtualizada) {
		notaBanco.setNota(notaAtualizada.getNota());
		return notaBanco;
	}

	private void validarExistenciasRelacionamentos(AlunoDAO alunoDAO, DisciplinaDAO disciplinaDAO, Nota nota)
			throws SQLException {
		if (alunoDAO.buscarPorId(nota.getIdAluno()) == null)
			throw new IllegalArgumentException("Aluno informado não existe.");

		if (disciplinaDAO.buscarPorId(nota.getIdDisciplina()) == null)
			throw new IllegalArgumentException("Disciplina informada não existe.");
	}

	private String tratarTexto(String valor) {
		return valor == null ? null : valor.trim();
	}

	public void salvarNota(Nota nota) {
		validarNotaNaoNula(nota);
		normalizarNota(nota);
		validarParaCadastro(nota);

		executarEmTransacao(conn -> {
			validarExistenciasRelacionamentos(new AlunoDAO(conn), new DisciplinaDAO(conn), nota);
			new NotaDAO(conn).inserir(nota);
			return null;
		}, "Erro ao salvar nota.");
	}

	public void atualizarNota(Nota notaAtualizada) {
		validarNotaNaoNula(notaAtualizada);

		if (notaAtualizada.getNotasId() <= 0)
			throw new IllegalArgumentException("ID da nota inválido.");

		normalizarNota(notaAtualizada);

		executarEmTransacao(conn -> {
			NotaDAO notaDAO = new NotaDAO(conn);

			Nota notaBanco = notaDAO.buscarPorId(notaAtualizada.getNotasId());
			if (notaBanco == null)
				throw new IllegalArgumentException("Nota não encontrada.");

			validarParaAtualizacao(notaAtualizada, notaBanco);
			validarExistenciasRelacionamentos(new AlunoDAO(conn), new DisciplinaDAO(conn), notaAtualizada);

			notaDAO.atualizar(mesclarDadosPermitidos(notaBanco, notaAtualizada));
			return null;
		}, "Erro ao atualizar nota.");
	}

	public boolean excluirNota(int idNota) {
		if (idNota <= 0)
			throw new IllegalArgumentException("ID da nota inválido.");

		return executarEmTransacao(conn -> {
			NotaDAO notaDAO = new NotaDAO(conn);

			if (notaDAO.buscarPorId(idNota) == null)
				throw new IllegalArgumentException("Nota não encontrada.");

			return notaDAO.excluir(idNota);
		}, "Erro ao excluir nota.");
	}

	public Nota buscarNotaPorId(int idNota) {
		if (idNota <= 0)
			throw new IllegalArgumentException("ID da nota inválido.");

		try (Connection conn = ConnectionFactory.getConnection()) {
			return new NotaDAO(conn).buscarPorId(idNota);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar nota por ID.", e);
		}
	}

	public List<Nota> listarNotas() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new NotaDAO(conn).listar();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar notas.", e);
		}
	}

	public List<Nota> buscarNotasPorAluno(int idAluno) {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");

		try (Connection conn = ConnectionFactory.getConnection()) {
			return new NotaDAO(conn).listarPorAluno(idAluno);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar notas por aluno.", e);
		}
	}

	public List<Nota> buscarNotasPorDisciplina(int idDisciplina) {
		if (idDisciplina <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");

		try (Connection conn = ConnectionFactory.getConnection()) {
			return new NotaDAO(conn).listarPorDisciplina(idDisciplina);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar notas por disciplina.", e);
		}
	}

	public List<Nota> buscarNotasPorAlunoETrimestre(int idAluno, int idTrimestre) {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		if (idTrimestre <= 0)
			throw new IllegalArgumentException("ID do trimestre inválido.");

		try (Connection conn = ConnectionFactory.getConnection()) {
			return new NotaDAO(conn).listarPorAlunoETrimestre(idAluno, idTrimestre);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar notas por trimestre.", e);
		}
	}
}