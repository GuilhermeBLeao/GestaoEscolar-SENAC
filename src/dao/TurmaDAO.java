package dao;

import model.Turma;
import variaveisEnum.Turno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {
	private final Connection conn;

	public TurmaDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public void inserir(Turma turma) throws SQLException {
		validarTurmaNaoNula(turma);

		final String sql = """
					INSERT INTO turma
					    (sala_id,
					    descricao_turma,
					    turno,
					    ativo)
					VALUES (?, ?, ?, true)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, turma.getSalaId());
			stmt.setString(2, turma.getDescricaoTurma());
			stmt.setString(3, turma.getTurno().name());

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir turma. Nenhuma linha afetada.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					turma.setIdTurma(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir turma. ID não retornado.");
				}
			}
		}
	}

	public void atualizar(Turma turma) throws SQLException {
		validarTurmaNaoNula(turma);

		if (turma.getIdTurma() <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
					UPDATE turma
					   SET sala_id = ?,
					       descricao_turma = ?,
					       turno = ?
					 WHERE id_turma = ?
					   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, turma.getSalaId());
			stmt.setString(2, turma.getDescricaoTurma());
			stmt.setString(3, turma.getTurno().name());
			stmt.setInt(4, turma.getIdTurma());

			int linhasAfetadas = stmt.executeUpdate();
			
			if (linhasAfetadas == 0) {
				verificarFalhaAtualizacao(turma.getIdTurma());
			}
		}
	}

	public Turma buscarPorId(int idTurma) throws SQLException {
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
					SELECT *
					FROM turma
					WHERE id_turma = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? mapearTurma(rs) : null;
			}
		}
	}

	public Turma buscarPorDescricao(String descricao) throws SQLException {
		if (descricao == null || descricao.trim().isEmpty()) {
			throw new IllegalArgumentException("Descrição inválida.");
		}

		final String sql = """
					SELECT *
					FROM turma
					WHERE descricao_turma = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, descricao.trim());
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? mapearTurma(rs) : null;
			}
		}
	}

	public List<Turma> listarTodos() throws SQLException {
		final String sql = """
					SELECT *
					FROM turma
					ORDER BY descricao_turma
				""";

		List<Turma> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				lista.add(mapearTurma(rs));
			}
		}
		return lista;
	}

	public List<Turma> listarAtivas() throws SQLException {
		final String sql = """
					SELECT *
					FROM turma
					WHERE ativo = true
					ORDER BY descricao_turma
				""";

		List<Turma> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				lista.add(mapearTurma(rs));
			}
		}
		return lista;
	}

	public List<Turma> listarPorProfessor(int idProfessor) throws SQLException {
		if (idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor inválido.");
		}

		final String sql = """
					SELECT DISTINCT t.*
					FROM turma t
					INNER JOIN turma_disciplina td ON td.turma_id = t.id_turma
					INNER JOIN professor_disciplina pd ON pd.id_disciplina = td.disciplina_id
					WHERE pd.id_professor = ? AND pd.ativo = true AND t.ativo = true
					ORDER BY t.descricao_turma
				""";
		List<Turma> lista = new ArrayList<>();
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					lista.add(mapearTurma(rs));
			}
		}
		return lista;
	}

	public List<Turma> listarInativas() throws SQLException {
		final String sql = """
					SELECT *
					FROM turma
					WHERE ativo = false
					ORDER BY descricao_turma
				""";

		List<Turma> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				lista.add(mapearTurma(rs));
			}
		}
		return lista;
	}

	public boolean inativar(int idTurma) throws SQLException {
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
					UPDATE turma
					   SET ativo = false
					 WHERE id_turma = ?
					   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaInativacao(idTurma);
			}
			return true;
		}
	}

	public boolean reativar(int idTurma) throws SQLException {
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}

		final String sql = """
					UPDATE turma
					   SET ativo = true
					 WHERE id_turma = ?
					   AND ativo = false
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaReativacao(idTurma);
			}
			return true;
		}
	}

	private Turma mapearTurma(ResultSet rs) throws SQLException {
		Turma turma = new Turma();
		turma.setIdTurma(rs.getInt("id_turma"));
		turma.setSalaId(rs.getInt("sala_id"));
		turma.setDescricaoTurma(rs.getString("descricao_turma"));
		turma.setTurno(Turno.valueOf(rs.getString("turno")));
		turma.setAtivo(rs.getBoolean("ativo"));
		return turma;
	}

	private void validarTurmaNaoNula(Turma turma) {
		if (turma == null) {
			throw new IllegalArgumentException("Turma não pode ser nula.");
		}
	}

	private void verificarFalhaAtualizacao(int idTurma) throws SQLException {
		final String sql = "SELECT ativo FROM turma WHERE id_turma = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Turma não encontrada.");
				}
				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Turma está inativa e não pode ser atualizada.");
				}
			}
		}
	}

	private void verificarFalhaInativacao(int idTurma) throws SQLException {
		final String sql = "SELECT ativo FROM turma WHERE id_turma = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Turma não encontrada.");
				}
				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Turma já está inativa.");
				}
			}
		}
	}

	private void verificarFalhaReativacao(int idTurma) throws SQLException {
		final String sql = "SELECT ativo FROM turma WHERE id_turma = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Turma não encontrada.");
				}
				if (rs.getBoolean("ativo")) {
					throw new SQLException("Turma já está ativa.");
				}
			}
		}
	}
}