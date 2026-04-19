//Guilherme

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class GeradorMatricula{
	private static final Random GERADOR = new Random();
	
	public static String gerar(Connection conn) throws SQLException{
		int proximoValor = buscarEIncrementarSequencia(conn);
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0;  i < 5;  i++) {
			sb.append(GERADOR.nextInt(10));
		}
		sb.append(String.format("%05d",proximoValor));
		return sb.toString();
	}
	
	private static int buscarEIncrementarSequencia(Connection conn) throws SQLException{
		garantirRegistroInicial(conn);
		
		String selectSQL = "SELECT ultimo_valor FROM controle_matricula WHERE id = 1";
		String updateSQL = "UPDATE controle_matricula SET ultimo_valor = ? WHERE id = 1";
		
		int ultimoValor;
		
		try(PreparedStatement stmtSelect = conn.prepareStatement(selectSQL);
				 ResultSet rs = stmtSelect.executeQuery()) {
		            if (!rs.next()) 
		                throw new SQLException("Registro de controle de matrícula não encontrado.");
		            ultimoValor = rs.getInt("ultimo_valor");
		        }
		        int proximoValor = ultimoValor + 1;

		        try (PreparedStatement stmtUpdate = conn.prepareStatement(updateSQL)) {
		            stmtUpdate.setInt(1, proximoValor);

		            int linhasAfetadas = stmtUpdate.executeUpdate();
		            if (linhasAfetadas == 0)
		                throw new SQLException("Não foi possível atualizar a sequência da matrícula.");
		        }
		        return proximoValor;
		    }
	private static void garantirRegistroInicial(Connection conn) throws SQLException {
        String insertSql = """
            INSERT OR IGNORE INTO controle_matricula (id, ultimo_valor) VALUES (1, 0) """;

        try (PreparedStatement stmtInsert = conn.prepareStatement(insertSql)) {
            stmtInsert.executeUpdate();
        }
    }
}