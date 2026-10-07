package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class GeradorMatricula {

	private static final int DIGITOS_ALEATORIOS = 5;
	private static final int DIGITOS_SEQUENCIA = 5;

	private GeradorMatricula() {}

	public static String gerarProvisoria(Connection conn) throws SQLException {
		validarConexao(conn);
		garantirRegistroInicial(conn);
		int proximoValor = buscarProximoValor(conn);
		return montarMatricula(proximoValor);
	}

	public static void confirmar(Connection conn, String matricula) throws SQLException {
		validarConexao(conn);
		if (matricula == null || !matricula.matches("\\d{10}")) {
			throw new IllegalArgumentException("Matrícula deve conter exatamente 10 dígitos numéricos.");
		}
		garantirRegistroInicial(conn);

		if (matriculaJaExiste(conn, matricula)) {
			throw new IllegalArgumentException("A matrícula informada já está cadastrada.");
		}

		int valorSequencial = extrairSequencia(matricula);
		int proximoValor = buscarProximoValor(conn);

		if (valorSequencial != proximoValor) {
			throw new IllegalStateException("A matrícula provisória não está mais disponível. "
					+ "Gere uma nova matrícula antes de realizar o cadastro.");
		}

		String updateSql = """
					UPDATE controle_matricula
					   SET ultimo_valor = ?
					 WHERE id = 1
					   AND ultimo_valor = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
			stmt.setInt(1, valorSequencial);
			stmt.setInt(2, valorSequencial - 1);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new IllegalStateException("Não foi possível confirmar a matrícula. "
						+ "O contador de matrículas foi alterado por outra operação.");
			}
		}
	}

	public static String gerar(Connection conn) throws SQLException {
		validarConexao(conn);
		garantirRegistroInicial(conn);

		int proximoValor = buscarProximoValor(conn);
		String matricula = montarMatricula(proximoValor);

		confirmar(conn, matricula);
		return matricula;
	}

	private static int buscarProximoValor(Connection conn) throws SQLException {
		String sql = """
					SELECT ultimo_valor
					  FROM controle_matricula
					 WHERE id = 1
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			if (!rs.next()) {
				throw new SQLException("Registro de controle de matrícula não encontrado.");
			}

			int ultimoValor = rs.getInt("ultimo_valor");

			if (ultimoValor >= 99999) {
				throw new SQLException("Limite da sequência de matrículas atingido.");
			}
			return ultimoValor + 1;
		}
	}

	private static String montarMatricula(int valorSequencial) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < DIGITOS_ALEATORIOS; i++) {
			sb.append(ThreadLocalRandom.current().nextInt(10));
		}
		sb.append(String.format("%0" + DIGITOS_SEQUENCIA + "d", valorSequencial));
		return sb.toString();
	}

	private static int extrairSequencia(String matricula) {
		String sequencia = matricula.substring(DIGITOS_ALEATORIOS);
		return Integer.parseInt(sequencia);
	}

	private static boolean matriculaJaExiste(Connection conn, String matricula) throws SQLException {
		String sql = """
					SELECT 1
					  FROM aluno
					 WHERE matricula = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, matricula);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static void garantirRegistroInicial(Connection conn) throws SQLException {
		String insertSql = """
					INSERT OR IGNORE INTO controle_matricula
					    (id,
					    ultimo_valor)
					VALUES (1, 0)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
			stmt.executeUpdate();
		}
	}

	private static void validarConexao(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
    }
}