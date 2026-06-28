/* Igor
Guilherme adicionou trimestre*/

package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Nota;
import util.SqlDates;

public class NotaDAO {

  private final Connection conn;

  public NotaDAO(Connection conn) {
    if (conn == null) throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    this.conn = conn;
  }

  public void inserir(Nota nota) throws SQLException {
    validarNotaNaoNula(nota);

    final String sql =
        """
        INSERT INTO nota (
        disciplina_id,
        aluno_id,
        atividade,
        nota,
        data_lancamento,
        trimestre_id
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, nota.getIdDisciplina());
      stmt.setInt(2, nota.getIdAluno());
      stmt.setString(3, nota.getAtividade());
      stmt.setDouble(4, nota.getNota());
      stmt.setDate(5, Date.valueOf(nota.getDataLancamento()));
      stmt.setInt(6, nota.getTrimestreId());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0)
        throw new SQLException("Falha ao inserir nota. Nenhuma linha afetada.");

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          nota.setNotasId(rs.getInt(1));
        } else {
          throw new SQLException("Falha ao inserir nota. ID não retornado.");
        }
      }
    }
  }

  public void atualizar(Nota nota) throws SQLException {
    validarNotaNaoNula(nota);

    if (nota.getNotasId() <= 0) throw new IllegalArgumentException("ID da nota inválido.");

    final String sql =
        """
        UPDATE nota
        SET nota = ?,
        data_lancamento = ?
        WHERE notas_id = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDouble(1, nota.getNota());
      stmt.setDate(2, Date.valueOf(nota.getDataLancamento()));
      stmt.setInt(3, nota.getNotasId());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0)
        throw new SQLException("Falha ao atualizar nota. Nenhuma linha afetada.");
    }
  }

  public Nota buscarPorId(int idNota) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM nota
        WHERE notas_id = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idNota);

      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? mapearNota(rs) : null;
      }
    }
  }

  public boolean excluir(int idNota) throws SQLException {
    final String sql =
        """
        DELETE FROM nota
        WHERE notas_id = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idNota);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0)
        throw new SQLException("Falha ao excluir nota. Nenhuma linha afetada.");

      return true;
    }
  }

  public List<Nota> listar() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM nota
        ORDER BY aluno_id, disciplina_id, trimestre_id, atividade
        """;

    List<Nota> notas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        notas.add(mapearNota(rs));
      }
    }

    return notas;
  }

  public List<Nota> listarPorAluno(int idAluno) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM nota
        WHERE aluno_id = ?
        ORDER BY disciplina_id, trimestre_id, atividade
        """;

    List<Nota> notas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAluno);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          notas.add(mapearNota(rs));
        }
      }
    }

    return notas;
  }

  public List<Nota> listarPorDisciplina(int idDisciplina) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM nota
        WHERE disciplina_id = ?
        ORDER BY aluno_id, trimestre_id, atividade
        """;

    List<Nota> notas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idDisciplina);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          notas.add(mapearNota(rs));
        }
      }
    }

    return notas;
  }

  public List<Nota> listarPorAlunoETrimestre(int idAluno, int idTrimestre) throws SQLException {
    final String sql =
        """
        SELECT *
        FROM nota
        WHERE aluno_id = ?
        AND trimestre_id = ?
        ORDER BY disciplina_id, atividade
        """;

    List<Nota> notas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAluno);
      stmt.setInt(2, idTrimestre);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          notas.add(mapearNota(rs));
        }
      }
    }

    return notas;
  }

  public Nota buscarPorAlunoDisciplinaAtividade(
      int idAluno, int idDisciplina, int idTrimestre, String atividade) throws SQLException {

    final String sql =
        """
        SELECT *
        FROM nota
        WHERE aluno_id = ?
        AND disciplina_id = ?
        AND trimestre_id = ?
        AND atividade = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAluno);
      stmt.setInt(2, idDisciplina);
      stmt.setInt(3, idTrimestre);
      stmt.setString(4, atividade);

      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? mapearNota(rs) : null;
      }
    }
  }

  private Nota mapearNota(ResultSet rs) throws SQLException {
    Nota nota = new Nota();

    nota.setNotasId(rs.getInt("notas_id"));
    nota.setIdDisciplina(rs.getInt("disciplina_id"));
    nota.setIdAluno(rs.getInt("aluno_id"));
    nota.setAtividade(rs.getString("atividade"));
    nota.setNota(rs.getDouble("nota"));
    nota.setTrimestreId(rs.getInt("trimestre_id"));

    java.time.LocalDate dataLancamento = SqlDates.getLocalDate(rs, "data_lancamento");
    if (dataLancamento != null) {
      nota.setDataLancamento(dataLancamento);
    }

    return nota;
  }

  private void validarNotaNaoNula(Nota nota) {
    if (nota == null) throw new IllegalArgumentException("Nota não pode ser nula.");
  }
}
