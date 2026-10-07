package controller;

import dao.DisciplinaDAO;
import database.ConnectionFactory;
import model.Disciplina;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DisciplinaController {
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

	private void validarNaoNula(Disciplina disciplina) {
		if (disciplina == null)
			throw new IllegalArgumentException("Disciplina não pode ser nula.");
	}

	private void normalizar(Disciplina disciplina) {
		disciplina.setDescricao(tratarTexto(disciplina.getDescricao()));
	}

	private void validarCamposBase(Disciplina disciplina) {
		if (disciplina.getDescricao() == null || disciplina.getDescricao().isBlank())
			throw new IllegalArgumentException("Descrição da disciplina é obrigatória.");
		if (disciplina.getDescricao().length() < 3 || disciplina.getDescricao().length() > 100)
			throw new IllegalArgumentException("Descrição da disciplina deve ter entre 3 e 100 caracteres.");
		if (disciplina.getCargaHoraria() <= 0)
			throw new IllegalArgumentException("Carga horária da disciplina deve ser maior que zero.");
		if (disciplina.getCodigo() <= 0)
			throw new IllegalArgumentException("Código da disciplina é obrigatório.");
	}

	private void validarCodigoImutavel(Disciplina atualizada, Disciplina banco) {
		if (atualizada.getCodigo() != banco.getCodigo())
			throw new IllegalArgumentException("Código da disciplina não pode ser alterado após o cadastro.");
	}

	private Disciplina mesclarDadosPermitidos(Disciplina banco, Disciplina atualizada) {
		banco.setDescricao(atualizada.getDescricao());
		banco.setCargaHoraria(atualizada.getCargaHoraria());
		return banco;
	}

	private String tratarTexto(String valor) {
		return valor == null ? null : valor.trim();
	}

	private void informarSeInativo(Disciplina disciplina) {
		if (disciplina != null && !disciplina.isAtivo())
			System.out.println("ATENÇÃO: disciplina encontrada, porém está inativa.");
	}

	public void salvarDisciplina(Disciplina disciplina) {
		validarNaoNula(disciplina);
		normalizar(disciplina);
		validarCamposBase(disciplina);
		executarEmTransacao(conn -> {
			salvarDisciplina(conn, disciplina);
			return null;
		}, "Erro ao salvar disciplina.");
	}

	void salvarDisciplina(Connection conn, Disciplina disciplina) throws SQLException {
		DisciplinaDAO dao = new DisciplinaDAO(conn);
		if (dao.existeCodigo(disciplina.getCodigo()))
			throw new IllegalArgumentException("Já existe disciplina cadastrada com este código.");
		dao.inserir(disciplina);
	}

	public void atualizarDisciplina(Disciplina disciplinaAtualizada) {
		validarNaoNula(disciplinaAtualizada);
		if (disciplinaAtualizada.getIdDisciplina() <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");
		normalizar(disciplinaAtualizada);
		validarCamposBase(disciplinaAtualizada);
		executarEmTransacao(conn -> {
			DisciplinaDAO dao = new DisciplinaDAO(conn);
			Disciplina banco = dao.buscarPorId(disciplinaAtualizada.getIdDisciplina());
			if (banco == null)
				throw new IllegalArgumentException("Disciplina não encontrada.");
			if (!banco.isAtivo())
				throw new IllegalArgumentException("Não é possível atualizar disciplina inativa.");
			validarCodigoImutavel(disciplinaAtualizada, banco);
			dao.atualizar(mesclarDadosPermitidos(banco, disciplinaAtualizada));
			return null;
		}, "Erro ao atualizar disciplina.");
	}

	public boolean excluirDisciplina(int idDisciplina) {
		if (idDisciplina <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");
		return executarEmTransacao(conn -> {
			DisciplinaDAO dao = new DisciplinaDAO(conn);
			if (dao.buscarPorId(idDisciplina) == null)
				throw new IllegalArgumentException("Disciplina não encontrada.");
			return dao.inativar(idDisciplina);
		}, "Erro ao excluir disciplina.");
	}

	public boolean reativarDisciplina(int idDisciplina) {
		if (idDisciplina <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");
		return executarEmTransacao(conn -> new DisciplinaDAO(conn).reativar(idDisciplina),
				"Erro ao reativar disciplina.");
	}

	public Disciplina buscarDisciplinaPorId(int idDisciplina) {
		if (idDisciplina <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Disciplina disciplina = new DisciplinaDAO(conn).buscarPorId(idDisciplina);
			if (disciplina == null)
				throw new IllegalArgumentException("Disciplina não encontrada.");
			informarSeInativo(disciplina);
			return disciplina;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar disciplina por ID.", e);
		}
	}

	public Disciplina buscarDisciplinaPorDescricao(String descricao) {
		String texto = tratarTexto(descricao);
		if (texto == null || texto.isBlank())
			throw new IllegalArgumentException("Descrição da disciplina é obrigatória.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Disciplina disciplina = new DisciplinaDAO(conn).buscarPorDescricao(texto);
			if (disciplina == null)
				throw new IllegalArgumentException("Disciplina não encontrada.");
			informarSeInativo(disciplina);
			return disciplina;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar disciplina por descrição.", e);
		}
	}

	public List<Disciplina> listarDisciplinas() {
		return listarTodasDisciplinas();
	}

	public List<Disciplina> listarTodasDisciplinas() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new DisciplinaDAO(conn).listarTodas();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar disciplinas.", e);
		}
	}

	public List<Disciplina> listarDisciplinasAtivas() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new DisciplinaDAO(conn).listarAtivas();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar disciplinas ativas.", e);
		}
	}

	public List<Disciplina> listarDisciplinasInativas() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new DisciplinaDAO(conn).listarInativas();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar disciplinas inativas.", e);
		}
	}

	public List<Disciplina> listarDisciplinasPorTurma(int idTurma) {
		if (idTurma <= 0)
			throw new IllegalArgumentException("ID da turma inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new DisciplinaDAO(conn).listarPorTurma(idTurma);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar disciplinas por turma.", e);
		}
	}
}
