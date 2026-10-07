package dao;

import model.Aviso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AvisoDAO {
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final Connection conn;

	public AvisoDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public List<Aviso> listar() throws SQLException {
		final String sql = "SELECT * FROM aviso ORDER BY data_aviso DESC, id_aviso DESC";
		List<Aviso> avisos = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				avisos.add(mapear(rs));
			}
		}
		return avisos;
	}

	public List<Aviso> listarParaResponsavel(int idPais, int idUsuario) throws SQLException {
		if (idPais <= 0 || idUsuario <= 0) {
			throw new IllegalArgumentException("Responsável ou usuário inválido.");
		}

		final String sql = """
					SELECT a.*, EXISTS
					    (SELECT 1 FROM aviso_leitura al
					    WHERE al.aviso_id = a.id_aviso AND al.usuario_id = ?)
					AS lido_pelo_usuario
					FROM aviso a
					WHERE instr(lower(a.publico), 'respons') > 0
					  AND (a.turma_id IS NULL OR EXISTS (
					      SELECT 1 FROM aluno aluno
					      WHERE aluno.pais_id = ? AND aluno.turma_id = a.turma_id))
					ORDER BY a.data_aviso DESC, a.id_aviso DESC
				""";

		List<Aviso> avisos = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idUsuario);
			stmt.setInt(2, idPais);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Aviso aviso = mapear(rs);
					aviso.setLido(rs.getBoolean("lido_pelo_usuario"));
					avisos.add(aviso);
				}
			}
		}
		return avisos;
	}

	public void marcarComoLido(int idAviso, int idUsuario) throws SQLException {
		final String sql = "INSERT OR IGNORE INTO aviso_leitura (aviso_id, usuario_id) VALUES (?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idAviso);
			stmt.setInt(2, idUsuario);
			stmt.executeUpdate();
		}
	}

	public void inserir(Aviso aviso) throws SQLException {
		if (aviso == null) {
			throw new IllegalArgumentException("Aviso não pode ser nulo.");
		}

		final String sql = "INSERT INTO aviso (titulo, descricao, categoria, prioridade, publico, turma_id, data_aviso) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, aviso.getTitulo());
			stmt.setString(2, aviso.getDescricao());
			stmt.setString(3, aviso.getCategoria());
			stmt.setString(4, aviso.getPrioridade());
			stmt.setString(5, aviso.getPublico());

			if (aviso.getTurmaId() == null) {
				stmt.setNull(6, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(6, aviso.getTurmaId());
			}
			stmt.setString(7, aviso.getDataAviso().toString());

			if (stmt.executeUpdate() == 0) {
				throw new SQLException("Nenhum aviso foi inserido.");
			}
		}
	}

	private Aviso mapear(ResultSet rs) throws SQLException {
		Aviso aviso = new Aviso();
		aviso.setIdAviso(rs.getInt("id_aviso"));
		aviso.setTitulo(rs.getString("titulo"));
		aviso.setDescricao(rs.getString("descricao"));
		aviso.setCategoria(rs.getString("categoria"));
		aviso.setPrioridade(rs.getString("prioridade"));
		aviso.setPublico(rs.getString("publico"));
		int turmaId = rs.getInt("turma_id");
		aviso.setTurmaId(rs.wasNull() ? null : turmaId);
		aviso.setLido(rs.getBoolean("lido"));
		String dataAviso = rs.getString("data_aviso");
		if (dataAviso != null && !dataAviso.isBlank()) {
			try {
				aviso.setDataAviso(parseData(dataAviso));
			} catch (DateTimeParseException e) {
				throw new SQLException("Data do aviso inválida. Valor recebido: " + dataAviso, e);
			}
		}
		return aviso;
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