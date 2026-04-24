//Guilherme

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class GeradorMatricula {

    private static final int DIGITOS_ALEATORIOS = 5, DIGITOS_SEQUENCIA = 5, MAX_TENTATIVAS = 10;

    public static String gerar(Connection conn) throws SQLException {
        if (conn == null)
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");

        garantirRegistroInicial(conn);

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            int proximoValor = buscarEIncrementarSequencia(conn);
            String matricula = montarMatricula(proximoValor);

            if (!matriculaJaExiste(conn, matricula))
                return matricula;
        }

        throw new SQLException("Não foi possível gerar uma matrícula única após " + MAX_TENTATIVAS + " tentativas.");
    }

    private static int buscarEIncrementarSequencia(Connection conn) throws SQLException {
        String updateSql = """
            UPDATE controle_matricula
               SET ultimo_valor = ultimo_valor + 1
             WHERE id = 1
            """;

        String selectSql = """
            SELECT ultimo_valor
              FROM controle_matricula
             WHERE id = 1
            """;

        try (PreparedStatement stmtUpdate = conn.prepareStatement(updateSql)) {
            int linhasAfetadas = stmtUpdate.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Não foi possível atualizar a sequência da matrícula.");
            }
        }

        try (PreparedStatement stmtSelect = conn.prepareStatement(selectSql);
             ResultSet rs = stmtSelect.executeQuery()) {

            if (!rs.next()) {
                throw new SQLException("Registro de controle de matrícula não encontrado.");
            }

            return rs.getInt("ultimo_valor");
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

    private static boolean matriculaJaExiste(Connection conn, String matricula) throws SQLException {
        String sql = "SELECT 1 FROM aluno WHERE matricula = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void garantirRegistroInicial(Connection conn) throws SQLException {
        String insertSql = """
            INSERT OR IGNORE INTO controle_matricula (id, ultimo_valor)
            VALUES (1, 0)
            """;

        try (PreparedStatement stmtInsert = conn.prepareStatement(insertSql)) {
            stmtInsert.executeUpdate();
        }
    }
}