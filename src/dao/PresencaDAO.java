// Igor

package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.Presenca;
import util.SqlDates;

public class PresencaDAO {

  private final Connection conn;

  public PresencaDAO(Connection conn) {
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }
    this.conn = conn;
  }

  public void inserir(Presenca presenca) throws SQLException {
    validarPresencaNaoNula(presenca);

    final String sql =
        """
        INSERT INTO presenca (
        aluno_id,
        disciplina_id,
        data_presenca,
        presente,
        falta_abonada,
        falta_justificada,
        motivo_abonada
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, presenca.getAlunoId());
      stmt.setInt(2, presenca.getDisciplinaId());
      stmt.setDate(3, Date.valueOf(presenca.getData()));
      stmt.setBoolean(4, presenca.isPresente());
      stmt.setBoolean(5, presenca.isFaltaAbonada());
      stmt.setBoolean(6, presenca.isFaltaJustificada());
      stmt.setString(7, presenca.getMotivoAbonada());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao inserir presença. Nenhuma linha afetada.");
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          presenca.setIdPresenca(rs.getInt(1));
        } else {
          throw new SQLException("Falha ao inserir presença. ID não retornado.");
        }
      }
    }
  }

  public void atualizar(Presenca presenca) throws SQLException {
    validarPresencaNaoNula(presenca);

    if (presenca.getIdPresenca() <= 0) {
      throw new IllegalArgumentException("ID da presença inválido.");
    }

    final String sql =
        """
        UPDATE presenca
        SET presente = ?,
        falta_abonada = ?,
        falta_justificada = ?,
        motivo_abonada = ?
        WHERE id_presenca = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setBoolean(1, presenca.isPresente());
      stmt.setBoolean(2, presenca.isFaltaAbonada());
      stmt.setBoolean(3, presenca.isFaltaJustificada());
      stmt.setString(4, presenca.getMotivoAbonada());
      stmt.setInt(5, presenca.getIdPresenca());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao atualizar presença. Nenhuma linha afetada.");
      }
    }
  }

  public Presenca buscarPorId(int idPresenca) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM presenca
        WHERE id_presenca = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idPresenca);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapearPresenca(rs);
        }
        return null;
      }
    }
  }

  public Presenca buscarPorAlunoDisciplinaData(int alunoId, int disciplinaId, LocalDate data)
      throws SQLException {

    final String sql =
        """
        SELECT *
        FROM presenca
        WHERE aluno_id = ?
        AND disciplina_id = ?
        AND data_presenca = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, alunoId);
      stmt.setInt(2, disciplinaId);
      stmt.setString(3, data.toString());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapearPresenca(rs);
        }
        return null;
      }
    }
  }

  public boolean excluir(int idPresenca) throws SQLException {
    final String sql = "DELETE FROM presenca WHERE id_presenca = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idPresenca);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao excluir presença. Nenhuma linha afetada.");
      }

      return true;
    }
  }

  public List<Presenca> listar() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM presenca
        ORDER BY data_presenca DESC, id_presenca DESC
        """;

    List<Presenca> presencas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        presencas.add(mapearPresenca(rs));
      }
    }

    return presencas;
  }

  public List<Presenca> listarPorAluno(int alunoId) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM presenca
        WHERE aluno_id = ?
        ORDER BY data_presenca DESC, id_presenca DESC
        """;

    List<Presenca> presencas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, alunoId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          presencas.add(mapearPresenca(rs));
        }
      }
    }

    return presencas;
  }

  public List<Presenca> listarPorDisciplina(int disciplinaId) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM presenca
        WHERE disciplina_id = ?
        ORDER BY data_presenca DESC, id_presenca DESC
        """;

    List<Presenca> presencas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, disciplinaId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          presencas.add(mapearPresenca(rs));
        }
      }
    }

    return presencas;
  }

  private Presenca mapearPresenca(ResultSet rs) throws SQLException {
    Presenca presenca = new Presenca();
    presenca.setIdPresenca(rs.getInt("id_presenca"));
    presenca.setAlunoId(rs.getInt("aluno_id"));
    presenca.setDisciplinaId(rs.getInt("disciplina_id"));
    presenca.setData(SqlDates.getLocalDate(rs, "data_presenca"));
    presenca.setPresente(rs.getBoolean("presente"));
    presenca.setFaltaAbonada(rs.getBoolean("falta_abonada"));
    presenca.setFaltaJustificada(rs.getBoolean("falta_justificada"));
    presenca.setMotivoAbonada(rs.getString("motivo_abonada"));
    return presenca;
  }

  private void validarPresencaNaoNula(Presenca presenca) {
    if (presenca == null) {
      throw new IllegalArgumentException("Presença não pode ser nula.");
    }
  }
}
