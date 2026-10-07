package dao;

import model.Trimestre;

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

public class TrimestreDAO {
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final Connection conn;

	public TrimestreDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public void inserir(Trimestre trimestre) throws SQLException {
		validarTrimestreNaoNulo(trimestre);

		final String sql = """
					INSERT INTO trimestre
					    (numero,
					    ano_letivo,
					    data_inicio,
					    data_fim)
					VALUES (?, ?, ?, ?)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, trimestre.getNumero());
			stmt.setInt(2, trimestre.getAnoLetivo());
			stmt.setString(3, trimestre.getDataInicio().toString());
			stmt.setString(4, trimestre.getDataFim().toString());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir trimestre. " + "Nenhuma linha afetada.");
			}
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					trimestre.setIdTrimestre(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir trimestre. " + "ID nao retornado.");
				}
			}
		}
	}

	public void atualizar(Trimestre trimestre) throws SQLException {
		validarTrimestreNaoNulo(trimestre);

		if (trimestre.getIdTrimestre() <= 0) {
			throw new IllegalArgumentException("ID do trimestre invalido.");
		}

		final String sql = """
					UPDATE trimestre
					   SET numero = ?,
					       ano_letivo = ?,
					       data_inicio = ?,
					       data_fim = ?
					 WHERE id_trimestre = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, trimestre.getNumero());
			stmt.setInt(2, trimestre.getAnoLetivo());
			stmt.setString(3, trimestre.getDataInicio().toString());
			stmt.setString(4, trimestre.getDataFim().toString());
			stmt.setInt(5, trimestre.getIdTrimestre());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao atualizar trimestre. " + "Nenhuma linha afetada.");
			}
		}
	}

	public Trimestre buscarPorId(int idTrimestre) throws SQLException {
		final String sql = """
					SELECT *
					FROM trimestre
					WHERE id_trimestre = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTrimestre);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearTrimestre(rs);
				}
				return null;
			}
		}
	}

	public Trimestre buscarPorNumeroEAno(int numero, int anoLetivo) throws SQLException {
		final String sql = """
					SELECT *
					FROM trimestre
					WHERE numero = ?
					  AND ano_letivo = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, numero);
			stmt.setInt(2, anoLetivo);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearTrimestre(rs);
				}
				return null;
			}
		}
	}

	public List<Trimestre> listar() throws SQLException {
		final String sql = """
					SELECT *
					FROM trimestre
					ORDER BY ano_letivo DESC, numero
				""";

		List<Trimestre> trimestres = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				trimestres.add(mapearTrimestre(rs));
			}
		}
		return trimestres;
	}

	public List<Trimestre> listarPorAnoLetivo(int anoLetivo) throws SQLException {
		final String sql = """
					SELECT *
					FROM trimestre
					WHERE ano_letivo = ?
					ORDER BY numero
				""";

		List<Trimestre> trimestres = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, anoLetivo);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					trimestres.add(mapearTrimestre(rs));
				}
			}
		}
		return trimestres;
	}

	public boolean excluir(int idTrimestre) throws SQLException {
		final String sql = """
					DELETE FROM trimestre
					WHERE id_trimestre = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTrimestre);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao excluir trimestre. " + "Nenhuma linha afetada.");
			}
			return true;
		}
	}

	private Trimestre mapearTrimestre(ResultSet rs) throws SQLException {
		Trimestre trimestre = new Trimestre();
		trimestre.setIdTrimestre(rs.getInt("id_trimestre"));
		trimestre.setNumero(rs.getInt("numero"));
		trimestre.setAnoLetivo(rs.getInt("ano_letivo"));
		
		String dataInicio = rs.getString("data_inicio");

		if (dataInicio != null && !dataInicio.isBlank()) {
			try {
				trimestre.setDataInicio(parseData(dataInicio));
			} catch (DateTimeParseException e) {
				throw new SQLException("Data de início do trimestre inválida. " + "Valor recebido: " + dataInicio, e);
			}
		}

		String dataFim = rs.getString("data_fim");

		if (dataFim != null && !dataFim.isBlank()) {
			try {
				trimestre.setDataFim(parseData(dataFim));
			} catch (DateTimeParseException e) {
				throw new SQLException("Data de fim do trimestre inválida. " + "Valor recebido: " + dataFim, e);
			}
		}
		return trimestre;
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

	private void validarTrimestreNaoNulo(Trimestre trimestre) {
		if (trimestre == null) {
			throw new IllegalArgumentException("Trimestre nao pode ser nulo.");
		}
	}

	public List<Trimestre> listarTodos() throws SQLException {
		final String sql = """
				SELECT id_trimestre, numero, ano_letivo, data_inicio, data_fim
				FROM trimestre
				ORDER BY ano_letivo DESC, numero ASC
			""";

		List<Trimestre> trimestres = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				trimestres.add(mapearTrimestre(rs));
			}
		}
		return trimestres;
	}
}