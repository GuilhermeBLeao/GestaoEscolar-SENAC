package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracaoSistemaDAO {
	private final Connection conn;

	public ConfiguracaoSistemaDAO(Connection conn) {
		this.conn = conn;
	}

	public String buscar(String chave) throws SQLException {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT valor FROM configuracao_sistema WHERE chave = ?")) {
			stmt.setString(1, chave);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? rs.getString(1) : "";
			}
		}
	}
}
