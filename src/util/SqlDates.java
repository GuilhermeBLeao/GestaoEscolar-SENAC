package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class SqlDates {
  private SqlDates() {}

  public static LocalDate getLocalDate(ResultSet rs, String coluna) throws SQLException {
    String valor = rs.getString(coluna);
    if (valor == null || valor.isBlank()) {
      return null;
    }

    valor = valor.trim();
    if (valor.matches("\\d+")) {
      long epoch = Long.parseLong(valor);
      Instant instant =
          valor.length() >= 13 ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
      return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    return LocalDate.parse(valor.length() >= 10 ? valor.substring(0, 10) : valor);
  }
}
