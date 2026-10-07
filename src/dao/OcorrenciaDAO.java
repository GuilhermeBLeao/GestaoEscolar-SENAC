package dao;

import model.Ocorrencia;
import variaveisEnum.TipoOcorrencia;

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

public class OcorrenciaDAO {
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final Connection conn;

	public OcorrenciaDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public void inserir(Ocorrencia ocorrencia) throws SQLException {
		validarOcorrenciaNaoNula(ocorrencia);

		final String sql = """
					INSERT INTO ocorrencia
					    (funcionario_id,
					    aluno_id,
					    tipo_ocorrencia,
					    descricao,
					    atendente_nome,
					    data_ocorrencia)
					VALUES (?, ?, ?, ?, ?, ?)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, ocorrencia.getFuncionarioId());

			if (ocorrencia.getAlunoId() > 0) {
				stmt.setInt(2, ocorrencia.getAlunoId());
			} else {
				stmt.setNull(2, java.sql.Types.INTEGER);
			}
			stmt.setString(3, ocorrencia.getTipoOcorrencia().name());
			stmt.setString(4, ocorrencia.getDescricao());
			stmt.setString(5, ocorrencia.getAtendenteNome());
			stmt.setString(6, ocorrencia.getDataOcorrencia().toString());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir ocorrência. Nenhuma linha afetada.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					ocorrencia.setIdOcorrencia(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir ocorrência. ID não retornado.");
				}
			}
		}
	}

	public void atualizar(Ocorrencia ocorrencia) throws SQLException {
		validarOcorrenciaNaoNula(ocorrencia);

		if (ocorrencia.getIdOcorrencia() <= 0) {
			throw new IllegalArgumentException("ID da ocorrência inválido.");
		}

		final String sql = """
					UPDATE ocorrencia
					   SET funcionario_id = ?,
					       aluno_id = ?,
					       tipo_ocorrencia = ?,
					       descricao = ?,
					       atendente_nome = ?,
					       data_ocorrencia = ?
					 WHERE id_ocorrencia = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, ocorrencia.getFuncionarioId());
			if (ocorrencia.getAlunoId() > 0) {
				stmt.setInt(2, ocorrencia.getAlunoId());
			} else {
				stmt.setNull(2, java.sql.Types.INTEGER);
			}
			stmt.setString(3, ocorrencia.getTipoOcorrencia().name());
			stmt.setString(4, ocorrencia.getDescricao());
			stmt.setString(5, ocorrencia.getAtendenteNome());
			stmt.setString(6, ocorrencia.getDataOcorrencia().toString());
			stmt.setInt(7, ocorrencia.getIdOcorrencia());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao atualizar ocorrência. Nenhuma linha afetada.");
			}
		}
	}

	public Ocorrencia buscarPorId(int idOcorrencia) throws SQLException {
		final String sql = """
					SELECT *
					FROM ocorrencia
					WHERE id_ocorrencia = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idOcorrencia);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearOcorrencia(rs);
				}
			}
		}
		return null;
	}

	public List<Ocorrencia> listar() throws SQLException {
		final String sql = """
					SELECT *
					FROM ocorrencia
					ORDER BY data_ocorrencia DESC, id_ocorrencia DESC
				""";

		List<Ocorrencia> ocorrencias = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				ocorrencias.add(mapearOcorrencia(rs));
			}
		}
		return ocorrencias;
	}

	public List<Ocorrencia> listarPorFuncionario(int funcionarioId) throws SQLException {
		final String sql = """
					SELECT *
					FROM ocorrencia
					WHERE funcionario_id = ?
					ORDER BY data_ocorrencia DESC, id_ocorrencia DESC
				""";

		List<Ocorrencia> ocorrencias = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, funcionarioId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ocorrencias.add(mapearOcorrencia(rs));
				}
			}
		}
		return ocorrencias;
	}

	private Ocorrencia mapearOcorrencia(ResultSet rs) throws SQLException {
		Ocorrencia ocorrencia = new Ocorrencia();
		ocorrencia.setIdOcorrencia(rs.getInt("id_ocorrencia"));
		ocorrencia.setFuncionarioId(rs.getInt("funcionario_id"));

		int alunoId = rs.getInt("aluno_id");

		if (!rs.wasNull()) {
			ocorrencia.setAlunoId(alunoId);
		}
		String tipo = rs.getString("tipo_ocorrencia");
		if (tipo != null && !tipo.isBlank()) {
			ocorrencia.setTipoOcorrencia(TipoOcorrencia.valueOf(tipo));
		}
		ocorrencia.setDescricao(rs.getString("descricao"));
		ocorrencia.setAtendenteNome(rs.getString("atendente_nome"));

		String dataOcorrencia = rs.getString("data_ocorrencia");

		if (dataOcorrencia != null && !dataOcorrencia.isBlank()) {
			try {
				ocorrencia.setDataOcorrencia(parseData(dataOcorrencia));
			} catch (DateTimeParseException e) {
				throw new SQLException("Data da ocorrência inválida. Valor recebido: " + dataOcorrencia, e);
			}
		}
		return ocorrencia;
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

	private void validarOcorrenciaNaoNula(Ocorrencia ocorrencia) {
		if (ocorrencia == null) {
			throw new IllegalArgumentException("Ocorrência não pode ser nula.");
		}
	}
}