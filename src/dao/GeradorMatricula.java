// Guilherme

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class GeradorMatricula {

  private static final int DIGITOS_SEQUENCIA = 6, MAX_TENTATIVAS = 10;

  public static String gerar(Connection conn) throws SQLException {
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }

    garantirRegistroInicial(conn);

    for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
      int proximoValor = buscarEIncrementarSequencia(conn);
      String matricula = montarMatricula(proximoValor);

      if (!matriculaJaExiste(conn, matricula)) {
        return matricula;
      }
    }

    throw new SQLException(
        "Nao foi possivel gerar uma matricula unica apos " + MAX_TENTATIVAS + " tentativas.");
  }

  private static int buscarEIncrementarSequencia(Connection conn) throws SQLException {
    int anoAtual = LocalDate.now().getYear();
    String updateSql =
        """
        UPDATE controle_matricula
        SET ultimo_numero = ultimo_numero + 1
        WHERE ano = ?
        """;

    String selectSql =
        """
        SELECT ultimo_numero
        FROM controle_matricula
        WHERE ano = ?
        """;

    try (PreparedStatement stmtUpdate = conn.prepareStatement(updateSql)) {
      stmtUpdate.setInt(1, anoAtual);
      int linhasAfetadas = stmtUpdate.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Nao foi possivel atualizar a sequencia da matricula.");
      }
    }

    try (PreparedStatement stmtSelect = conn.prepareStatement(selectSql)) {
      stmtSelect.setInt(1, anoAtual);

      try (ResultSet rs = stmtSelect.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Registro de controle de matricula nao encontrado.");
        }

        return rs.getInt("ultimo_numero");
      }
    }
  }

  private static String montarMatricula(int valorSequencial) {
    return LocalDate.now().getYear()
        + String.format("%0" + DIGITOS_SEQUENCIA + "d", valorSequencial);
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
    int anoAtual = LocalDate.now().getYear();
    String insertSql =
        """
        INSERT OR IGNORE INTO controle_matricula (ano, ultimo_numero)
        VALUES (?, 0)
        """;

    try (PreparedStatement stmtInsert = conn.prepareStatement(insertSql)) {
      stmtInsert.setInt(1, anoAtual);
      stmtInsert.executeUpdate();
    }
  }
}
