// Guilherme

package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Advertencia;
import util.SqlDates;

public class AdvertenciaDAO {

  private final Connection conn;

  public AdvertenciaDAO(Connection conn) {
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }
    this.conn = conn;
  }

  public void inserir(Advertencia advertencia) throws SQLException {
    validarAdvertenciaNaoNula(advertencia);

    final String sql =
        """
        INSERT INTO advertencia (
        aluno_id,
        turma_id,
        professor_id,
        motivo,
        descricao,
        data_advertencia
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, advertencia.getAlunoId());
      stmt.setInt(2, advertencia.getTurmaId());
      stmt.setInt(3, advertencia.getProfessorId());
      stmt.setString(4, advertencia.getMotivo());
      stmt.setString(5, advertencia.getDescricao());
      stmt.setDate(6, Date.valueOf(advertencia.getDataAdvertencia()));

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao inserir advertencia. Nenhuma linha afetada.");
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          advertencia.setIdAdvertencia(rs.getInt(1));
        } else {
          throw new SQLException("Falha ao inserir advertencia. ID nao retornado.");
        }
      }
    }
  }

  public void atualizar(Advertencia advertencia) throws SQLException {
    validarAdvertenciaNaoNula(advertencia);

    if (advertencia.getIdAdvertencia() <= 0) {
      throw new IllegalArgumentException("ID da advertencia invalido.");
    }

    final String sql =
        """
        UPDATE advertencia
        SET aluno_id = ?,
        turma_id = ?,
        professor_id = ?,
        motivo = ?,
        descricao = ?,
        data_advertencia = ?
        WHERE id_advertencia = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, advertencia.getAlunoId());
      stmt.setInt(2, advertencia.getTurmaId());
      stmt.setInt(3, advertencia.getProfessorId());
      stmt.setString(4, advertencia.getMotivo());
      stmt.setString(5, advertencia.getDescricao());
      stmt.setDate(6, Date.valueOf(advertencia.getDataAdvertencia()));
      stmt.setInt(7, advertencia.getIdAdvertencia());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao atualizar advertencia. Nenhuma linha afetada.");
      }
    }
  }

  public Advertencia buscarPorId(int idAdvertencia) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM advertencia
        WHERE id_advertencia = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAdvertencia);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapearAdvertencia(rs);
        }
        return null;
      }
    }
  }

  public boolean excluir(int idAdvertencia) throws SQLException {
    final String sql = "DELETE FROM advertencia WHERE id_advertencia = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAdvertencia);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao excluir advertencia. Nenhuma linha afetada.");
      }

      return true;
    }
  }

  public List<Advertencia> listar() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM advertencia
        ORDER BY data_advertencia DESC, id_advertencia DESC
        """;

    List<Advertencia> advertencias = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        advertencias.add(mapearAdvertencia(rs));
      }
    }

    return advertencias;
  }

  public List<Advertencia> listarPorAluno(int alunoId) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM advertencia
        WHERE aluno_id = ?
        ORDER BY data_advertencia DESC, id_advertencia DESC
        """;

    List<Advertencia> advertencias = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, alunoId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          advertencias.add(mapearAdvertencia(rs));
        }
      }
    }

    return advertencias;
  }

  public List<Advertencia> listarPorTurma(int turmaId) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM advertencia
        WHERE turma_id = ?
        ORDER BY data_advertencia DESC, id_advertencia DESC
        """;

    List<Advertencia> advertencias = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, turmaId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          advertencias.add(mapearAdvertencia(rs));
        }
      }
    }

    return advertencias;
  }

  private Advertencia mapearAdvertencia(ResultSet rs) throws SQLException {
    Advertencia advertencia = new Advertencia();

    advertencia.setIdAdvertencia(rs.getInt("id_advertencia"));
    advertencia.setAlunoId(rs.getInt("aluno_id"));
    advertencia.setTurmaId(rs.getInt("turma_id"));
    advertencia.setProfessorId(rs.getInt("professor_id"));
    advertencia.setMotivo(rs.getString("motivo"));
    advertencia.setDescricao(rs.getString("descricao"));
    advertencia.setDataAdvertencia(SqlDates.getLocalDate(rs, "data_advertencia"));

    return advertencia;
  }

  private void validarAdvertenciaNaoNula(Advertencia advertencia) {
    if (advertencia == null) {
      throw new IllegalArgumentException("Advertencia nao pode ser nula.");
    }
  }
}
