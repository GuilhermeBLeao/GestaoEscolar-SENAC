package dao;

import model.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {
	private final Connection conn;

	public DisciplinaDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public boolean existeCodigo(int codigo) throws SQLException {
		if (codigo <= 0) {
			throw new IllegalArgumentException("Código inválido.");
		}

		final String sql = "SELECT 1 FROM disciplina WHERE codigo = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, codigo);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	public void inserir(Disciplina disciplina) throws SQLException {
		validarDisciplinaNaoNula(disciplina);

		final String sql = """
					INSERT INTO disciplina
					    (descricao,
					    carga_horaria,
					    codigo,
					    ativo)
					VALUES (?, ?, ?, true)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, disciplina.getDescricao());
			stmt.setInt(2, disciplina.getCargaHoraria());
			stmt.setInt(3, disciplina.getCodigo());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir disciplina.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					disciplina.setIdDisciplina(rs.getInt(1));
				} else {
					throw new SQLException("ID não retornado.");
				}
			}
		}
	}

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

			if (linhasAfetadas == 0) {
				verificarFalhaAtualizacao(disciplina.getIdDisciplina());
			}
		}
	}

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

	public List<Disciplina> listarPorProfessorETurma(int idProfessor, int idTurma) throws SQLException {
		if (idProfessor <= 0 || idTurma <= 0)
			throw new IllegalArgumentException("Professor e turma inválidos.");
		final String sql = """
					SELECT d.* FROM disciplina d
					INNER JOIN turma_disciplina td ON td.disciplina_id = d.id_disciplina
					INNER JOIN professor_disciplina pd ON pd.id_disciplina = d.id_disciplina
					WHERE td.turma_id = ? AND pd.id_professor = ? AND pd.ativo = true AND d.ativo = true
					ORDER BY d.descricao
				""";
		List<Disciplina> lista = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			stmt.setInt(2, idProfessor);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					lista.add(mapearDisciplina(rs));
			}
		}
		return lista;
	}

	public List<Object[]> listarDetalhesPorAluno(int idAluno) throws SQLException {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		final String sql = """
					SELECT d.codigo, d.descricao, d.carga_horaria,
					       COALESCE(GROUP_CONCAT(DISTINCT p.nome), 'Não informado') AS professores,
					       d.ativo
					FROM aluno a
					INNER JOIN turma_disciplina td ON td.turma_id = a.turma_id
					INNER JOIN disciplina d ON d.id_disciplina = td.disciplina_id
					LEFT JOIN professor_disciplina pd ON pd.id_disciplina = d.id_disciplina AND pd.ativo = true
					LEFT JOIN professor p ON p.id_professor = pd.id_professor AND p.ativo = true
					WHERE a.id_aluno = ? AND d.ativo = true
					GROUP BY d.id_disciplina, d.codigo, d.descricao, d.carga_horaria, d.ativo
					ORDER BY d.descricao
				""";
		
		List<Object[]> lista = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idAluno);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(new Object[] { rs.getInt("codigo"), rs.getString("descricao"),
							rs.getString("professores"), rs.getInt("carga_horaria"), "Não informado",
							rs.getBoolean("ativo") ? "Ativa" : "Inativa" });
				}
			}
		}
		return lista;
	}

	public List<Object[]> listarDetalhesPorProfessor(int idProfessor) throws SQLException {
		if (idProfessor <= 0)
			throw new IllegalArgumentException("ID do professor inválido.");
		final String sql = """
					SELECT d.codigo, d.descricao, d.carga_horaria,
					       GROUP_CONCAT(DISTINCT t.descricao_turma) AS turmas,
					       COUNT(DISTINCT a.id_aluno) AS alunos, d.ativo
					FROM professor_disciplina pd
					INNER JOIN disciplina d ON d.id_disciplina = pd.id_disciplina
					LEFT JOIN turma_disciplina td ON td.disciplina_id = d.id_disciplina
					LEFT JOIN turma t ON t.id_turma = td.turma_id AND t.ativo = true
					LEFT JOIN aluno a ON a.turma_id = t.id_turma AND a.situacao = 'ATIVO'
					WHERE pd.id_professor = ? AND pd.ativo = true AND d.ativo = true
					GROUP BY d.id_disciplina, d.codigo, d.descricao, d.carga_horaria, d.ativo
					ORDER BY d.descricao
				""";
		
		List<Object[]> lista = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					lista.add(new Object[] { rs.getInt("codigo"), rs.getString("descricao"),
							rs.getString("turmas") == null ? "Não informado" : rs.getString("turmas"),
							rs.getInt("carga_horaria"), rs.getInt("alunos"),
							rs.getBoolean("ativo") ? "Ativa" : "Inativa" });
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

	private Disciplina mapearDisciplina(ResultSet rs) throws SQLException {
		Disciplina d = new Disciplina();
		d.setIdDisciplina(rs.getInt("id_disciplina"));
		d.setDescricao(rs.getString("descricao"));
		d.setCargaHoraria(rs.getInt("carga_horaria"));
		d.setCodigo(rs.getInt("codigo"));
		d.setAtivo(rs.getBoolean("ativo"));
		return d;
	}

	private void validarDisciplinaNaoNula(Disciplina d) {
		if (d == null) {
			throw new IllegalArgumentException("Disciplina não pode ser nula.");
		}
	}
}