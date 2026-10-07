package dao;

import model.ProfessorDisciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDisciplinaDAO {
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final Connection conn;

	public ProfessorDisciplinaDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	private void validarProfessorDisciplinaNaoNula(ProfessorDisciplina profDisciplina) {
		if (profDisciplina == null) {
			throw new IllegalArgumentException("Relação Professor-Disciplina não pode ser nula.");
		}
	}

	public boolean existeVinculacao(int idProfessor, int idDisciplina) throws SQLException {
		if (idProfessor <= 0 || idDisciplina <= 0) {
			throw new IllegalArgumentException("IDs inválidos.");
		}
		final String sql = """
					SELECT 1
					FROM professor_disciplina
					WHERE id_professor = ?
					  AND id_disciplina = ?
					  AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);
			stmt.setInt(2, idDisciplina);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	public void inserir(ProfessorDisciplina profDisciplina) throws SQLException {
		validarProfessorDisciplinaNaoNula(profDisciplina);

		final String sql = """
					INSERT INTO professor_disciplina
						(id_professor,
						id_disciplina,
						data_vinculacao,
						ativo)
					VALUES (?, ?, ?, true)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, profDisciplina.getIdProfessor());
			stmt.setInt(2, profDisciplina.getIdDisciplina());
			stmt.setString(3, profDisciplina.getDataVinculacao().toString());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao vincular professor à disciplina.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					profDisciplina.setIdProfessorDisciplina(rs.getInt(1));
				} else {
					throw new SQLException("ID não retornado.");
				}
			}
		}
	}

	public ProfessorDisciplina buscarPorId(int idProfessorDisciplina) throws SQLException {
		if (idProfessorDisciplina <= 0) {
			throw new IllegalArgumentException("ID inválido.");
		}

		final String sql = """
					SELECT
						id_professor_disciplina,
						id_professor,
						id_disciplina,
						data_vinculacao,
						data_desvinculacao,
						ativo
					FROM professor_disciplina
					WHERE id_professor_disciplina = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessorDisciplina);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProfessorDisciplina(rs);
				}
				return null;
			}
		}
	}

	public List<ProfessorDisciplina> buscarPorProfessor(int idProfessor) throws SQLException {
		if (idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor inválido.");
		}

		final String sql = """
					SELECT
						id_professor_disciplina,
						id_professor,
						id_disciplina,
						data_vinculacao,
						data_desvinculacao,
						ativo
					FROM professor_disciplina
					WHERE id_professor = ?
					  AND ativo = true
				""";

		List<ProfessorDisciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProfessorDisciplina(rs));
				}
			}
		}
		return lista;
	}

	public List<ProfessorDisciplina> buscarPorDisciplina(int idDisciplina) throws SQLException {
		if (idDisciplina <= 0) {
			throw new IllegalArgumentException("ID da disciplina inválido.");
		}

		final String sql = """
					SELECT
						id_professor_disciplina,
						id_professor,
						id_disciplina,
						data_vinculacao,
						data_desvinculacao,
						ativo
					FROM professor_disciplina
					WHERE id_disciplina = ?
					  AND ativo = true
				""";

		List<ProfessorDisciplina> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idDisciplina);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapearProfessorDisciplina(rs));
				}
			}
		}
		return lista;
	}

	public boolean inativar(int idProfessorDisciplina) throws SQLException {
		if (idProfessorDisciplina <= 0) {
			throw new IllegalArgumentException("ID inválido.");
		}

		final String sql = """
					UPDATE professor_disciplina
					   SET ativo = false
					 WHERE id_professor_disciplina = ?
					   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessorDisciplina);
			int linhasAfetadas = stmt.executeUpdate();
			return linhasAfetadas > 0;
		}
	}

	private ProfessorDisciplina mapearProfessorDisciplina(ResultSet rs) throws SQLException {
		ProfessorDisciplina profDisciplina = new ProfessorDisciplina();
		profDisciplina.setIdProfessorDisciplina(rs.getInt("id_professor_disciplina"));
		profDisciplina.setIdProfessor(rs.getInt("id_professor"));
		profDisciplina.setIdDisciplina(rs.getInt("id_disciplina"));

		String dataVinculacao = rs.getString("data_vinculacao");

		if (dataVinculacao != null && !dataVinculacao.isBlank()) {
			try {
				profDisciplina.setDataVinculacao(parseData(dataVinculacao));
			} catch (DateTimeParseException e) {
				throw new SQLException("Data de vinculação inválida. " + "Valor recebido: " + dataVinculacao, e);
			}
		}
		String dataDesvinculacao = rs.getString("data_desvinculacao");
		if (dataDesvinculacao != null && !dataDesvinculacao.isBlank()) {
			try {
				profDisciplina.setDataDesvinculacao(parseData(dataDesvinculacao));
			} catch (DateTimeParseException e) {
				throw new SQLException("Data de desvinculação inválida. " + "Valor recebido: " + dataDesvinculacao, e);
			}
		}
		profDisciplina.setAtivo(rs.getBoolean("ativo"));
		return profDisciplina;
	}

	private LocalDate parseData(String valor) {
		String tratado = valor.trim();

		if (tratado.contains("T")) {
			tratado = tratado.substring(0, tratado.indexOf('T'));
		} else if (tratado.contains(" ")) {
			tratado = tratado.substring(0, tratado.indexOf(' '));
		}
		return LocalDate.parse(tratado, FORMATO_DATA);
	}
}