/*Luiz - Igor
Guilherme editou adicionando exclusão lógica (inativar ao invés de excluir)*/

package dao;

import model.Disciplina;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

	private final Connection conn;

	// Construtor: recebe a conexão com o banco
	public DisciplinaDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	// Verifica se já existe uma disciplina com o código informado
	public boolean existeCodigo(int codigo) throws SQLException {
		if (codigo <= 0) {
			throw new IllegalArgumentException("Código inválido.");
		}

		final String sql = "SELECT 1 FROM disciplina WHERE codigo = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, codigo);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next(); // true se encontrou
			}
		}
	}

	// Insere uma nova disciplina no banco (sempre como ativa)
	public void inserir(Disciplina disciplina) throws SQLException {
		validarDisciplinaNaoNula(disciplina);

		final String sql = """
				INSERT INTO disciplina (
				    descricao,
				    carga_horaria,
				    codigo,
				    ativo
				) VALUES (?, ?, ?, true)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, disciplina.getDescricao());
			stmt.setInt(2, disciplina.getCargaHoraria());
			stmt.setInt(3, disciplina.getCodigo());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir disciplina.");
			}

			// Recupera o ID gerado
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					disciplina.setIdDisciplina(rs.getInt(1));
				} else {
					throw new SQLException("ID não retornado.");
				}
			}
		}
	}

	// Atualiza uma disciplina (apenas se estiver ativa)
	public void atualizar(Disciplina disciplina) throws SQLException {
		validarDisciplinaNaoNula(disciplina);

		if (disciplina.getIdDisciplina() <= 0) {
			throw new IllegalArgumentException("ID inválido.");
		}

		final String sql = """
				UPDATE disciplina
				   SET descricao = ?,
				       carga_horaria = ?
				 WHERE id_disciplina = ?
				   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, disciplina.getDescricao());
			stmt.setInt(2, disciplina.getCargaHoraria());
			stmt.setInt(3, disciplina.getIdDisciplina());

			int linhasAfetadas = stmt.executeUpdate();

			// Se não atualizou, verifica o motivo
			if (linhasAfetadas == 0) {
				verificarFalhaAtualizacao(disciplina.getIdDisciplina());
			}
		}
	}

	// Busca disciplina pelo ID (retorna ativa ou inativa)
	public Disciplina buscarPorId(int idDisciplina) throws SQLException {
		if (idDisciplina <= 0) {
			throw new IllegalArgumentException("ID inválido.");
		}

		final String sql = """
				SELECT *
				FROM disciplina
				WHERE id_disciplina = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idDisciplina);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? mapearDisciplina(rs) : null;
			}
		}
	}

	// Busca disciplina pelo nome/descrição
	public Disciplina buscarPorDescricao(String descricao) throws SQLException {
		if (descricao == null || descricao.trim().isEmpty()) {
			throw new IllegalArgumentException("Descrição obrigatória.");
		}

		final String sql = """
				SELECT *
				FROM disciplina
				WHERE descricao = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, descricao.trim());

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? mapearDisciplina(rs) : null;
			}
		}
	}

	// Lista TODAS as disciplinas (ativas e inativas)
	public List<Disciplina> listarTodas() throws SQLException {
		final String sql = "SELECT * FROM disciplina ORDER BY descricao";

		List<Disciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				lista.add(mapearDisciplina(rs));
			}
		}
		return lista;
	}

	// Lista apenas disciplinas ativas
	public List<Disciplina> listarAtivas() throws SQLException {
		final String sql = "SELECT * FROM disciplina WHERE ativo = true ORDER BY descricao";

		List<Disciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				lista.add(mapearDisciplina(rs));
			}
		}
		return lista;
	}

	// Lista apenas disciplinas inativas
	public List<Disciplina> listarInativas() throws SQLException {
		final String sql = "SELECT * FROM disciplina WHERE ativo = false ORDER BY descricao";

		List<Disciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				lista.add(mapearDisciplina(rs));
			}
		}
		return lista;
	}

	// Lista disciplinas vinculadas a uma turma específica
	public List<Disciplina> listarPorTurma(int idTurma) throws SQLException {
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
				SELECT d.*
				FROM disciplina d
				INNER JOIN turma_disciplina td
				    ON td.disciplina_id = d.id_disciplina
				WHERE td.turma_id = ?
				  AND d.ativo = true
				ORDER BY d.descricao
				""";

		List<Disciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearDisciplina(rs));
				}
			}
		}
		return lista;
	}

	public List<Disciplina> listarPorTurmaIncluindoInativas(int idTurma) throws SQLException {
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
				SELECT d.*
				FROM disciplina d
				INNER JOIN turma_disciplina td
				    ON td.disciplina_id = d.id_disciplina
				WHERE td.turma_id = ?
				ORDER BY d.descricao
				""";

		List<Disciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearDisciplina(rs));
				}
			}
		}
		return lista;
	}

	// Verifica se uma disciplina pertence a uma turma específica
	public boolean disciplinaPertenceTurma(int idDisciplina, int idTurma) throws SQLException {
		if (idDisciplina <= 0) {
			throw new IllegalArgumentException("ID da disciplina inválido.");
		}
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
				SELECT 1
				FROM turma_disciplina
				WHERE disciplina_id = ?
				  AND turma_id = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idDisciplina);
			stmt.setInt(2, idTurma);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	// Inativa uma disciplina (exclusão lógica)
	public boolean inativar(int idDisciplina) throws SQLException {
		if (idDisciplina <= 0) {
			throw new IllegalArgumentException("ID inválido.");
		}

		final String sql = """
				UPDATE disciplina
				   SET ativo = false
				 WHERE id_disciplina = ?
				   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idDisciplina);

			int linhas = stmt.executeUpdate();

			if (linhas == 0) {
				verificarFalhaInativacao(idDisciplina);
			}
			return true;
		}
	}

	// Reativa uma disciplina
	public boolean reativar(int idDisciplina) throws SQLException {
		if (idDisciplina <= 0) {
			throw new IllegalArgumentException("ID inválido.");
		}

		final String sql = """
				UPDATE disciplina
				   SET ativo = true
				 WHERE id_disciplina = ?
				   AND ativo = false
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idDisciplina);

			int linhas = stmt.executeUpdate();

			if (linhas == 0) {
				verificarFalhaReativacao(idDisciplina);
			}
			return true;
		}
	}

	// Verifica por que falhou a atualização
	private void verificarFalhaAtualizacao(int id) throws SQLException {
		final String sql = "SELECT ativo FROM disciplina WHERE id_disciplina = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next())
					throw new SQLException("Disciplina não encontrada.");
				if (!rs.getBoolean("ativo"))
					throw new SQLException("Disciplina inativa.");
			}
		}
	}

	// Verifica falha ao inativar
	private void verificarFalhaInativacao(int id) throws SQLException {
		final String sql = "SELECT ativo FROM disciplina WHERE id_disciplina = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next())
					throw new SQLException("Disciplina não encontrada.");
				if (!rs.getBoolean("ativo"))
					throw new SQLException("Já está inativa.");
			}
		}
	}

	// Verifica falha ao reativar
	private void verificarFalhaReativacao(int id) throws SQLException {
		final String sql = "SELECT ativo FROM disciplina WHERE id_disciplina = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next())
					throw new SQLException("Disciplina não encontrada.");
				if (rs.getBoolean("ativo"))
					throw new SQLException("Já está ativa.");
			}
		}
	}

	// Converte ResultSet em objeto Disciplina
	private Disciplina mapearDisciplina(ResultSet rs) throws SQLException {
		Disciplina d = new Disciplina();
		d.setIdDisciplina(rs.getInt("id_disciplina"));
		d.setDescricao(rs.getString("descricao"));
		d.setCargaHoraria(rs.getInt("carga_horaria"));
		d.setCodigo(rs.getInt("codigo"));
		d.setAtivo(rs.getBoolean("ativo"));
		return d;
	}

	// Valida se objeto não é nulo
	private void validarDisciplinaNaoNula(Disciplina d) {
		if (d == null) {
			throw new IllegalArgumentException("Disciplina não pode ser nula.");
		}
	}
}
