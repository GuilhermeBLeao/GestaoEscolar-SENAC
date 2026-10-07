package dao;

import model.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalaDAO {
	private final Connection conn;

	public SalaDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public void inserir(Sala sala) throws SQLException {
		validarSalaNaoNula(sala);

		final String sql = "INSERT INTO sala (capacidade, ativo) VALUES (?, true)";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, sala.getCapacidade());

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir sala. Nenhuma linha afetada.");
			}
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					sala.setIdSala(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir sala. ID não retornado.");
				}
			}
		}
	}

	public void atualizar(Sala sala) throws SQLException {
		validarSalaNaoNula(sala);

		if (sala.getIdSala() <= 0) {
			throw new IllegalArgumentException("ID da sala inválido.");
		}

		final String sql = "UPDATE sala SET capacidade = ? WHERE id_sala = ? AND ativo = true";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, sala.getCapacidade());
			stmt.setInt(2, sala.getIdSala());
			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaAtualizacao(sala.getIdSala());
			}
		}
	}

	public Sala buscarPorId(int idSala) throws SQLException {
		final String sql = "SELECT * FROM sala WHERE id_sala = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idSala);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearSala(rs);
				}
				return null;
			}
		}
	}

	public List<Sala> listarTodas() throws SQLException {
		final String sql = "SELECT * FROM sala ORDER BY id_sala";

		List<Sala> salas = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				salas.add(mapearSala(rs));
			}
		}
		return salas;
	}

	public List<Sala> listarAtivas() throws SQLException {
		final String sql = """
				    SELECT *
				    FROM sala
				    WHERE ativo = true
				    ORDER BY id_sala
				""";

		List<Sala> salas = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				salas.add(mapearSala(rs));
			}
		}
		return salas;
	}

	public List<Sala> listarInativas() throws SQLException {
		final String sql = """
				    SELECT *
				    FROM sala
				    WHERE ativo = false
				    ORDER BY id_sala
				""";

		List<Sala> salas = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				salas.add(mapearSala(rs));
			}
		}
		return salas;
	}

	public boolean inativar(int idSala) throws SQLException {
		final String sql = """
					UPDATE sala
					SET ativo = false
					WHERE id_sala = ?
					AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idSala);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaInativacao(idSala);
			}
			return true;
		}
	}

	public boolean reativar(int idSala) throws SQLException {
		final String sql = """
					UPDATE sala
					SET ativo = true
					WHERE id_sala = ?
					AND ativo = false
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idSala);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaReativacao(idSala);
			}
			return true;
		}
	}

	private Sala mapearSala(ResultSet rs) throws SQLException {
		Sala sala = new Sala();
		sala.setIdSala(rs.getInt("id_sala"));
		sala.setCapacidade(rs.getInt("capacidade"));
		sala.setAtivo(rs.getBoolean("ativo"));
		return sala;
	}

	private void validarSalaNaoNula(Sala sala) {
		if (sala == null) {
			throw new IllegalArgumentException("Sala não pode ser nula.");
		}
	}

	private void verificarFalhaInativacao(int idSala) throws SQLException {
		final String sql = "SELECT ativo FROM sala WHERE id_sala = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idSala);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Sala não encontrada.");
				}
				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Sala já está inativa.");
				}
			}
		}
	}

	private void verificarFalhaReativacao(int idSala) throws SQLException {
		final String sql = "SELECT ativo FROM sala WHERE id_sala = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idSala);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Sala não encontrada.");
				}
				if (rs.getBoolean("ativo")) {
					throw new SQLException("Sala já está ativa.");
				}
			}
		}
	}

	private void verificarFalhaAtualizacao(int idSala) throws SQLException {
		final String sql = "SELECT ativo FROM sala WHERE id_sala = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idSala);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Sala não encontrada.");
				}
				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Sala está inativa e não pode ser atualizada.");
				}
			}
		}
	}
}