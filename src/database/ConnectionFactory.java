// Márcio e Guilherme

package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
  private static final String URL = "jdbc:sqlite:./database/banco.db";

  public static Connection getConnection() throws SQLException {
    File diretorio = new File("./database");
    if (!diretorio.exists()) {
      diretorio.mkdirs();
    }

    Connection conn = DriverManager.getConnection(URL);
    configurarSqlite(conn);
    DatabaseInitializer.initialize(conn);
    return conn;
  }

  private static void configurarSqlite(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("PRAGMA journal_mode = TRUNCATE");
      stmt.execute("PRAGMA busy_timeout = 5000");
      stmt.execute("PRAGMA foreign_keys = ON");
    }
  }
}
