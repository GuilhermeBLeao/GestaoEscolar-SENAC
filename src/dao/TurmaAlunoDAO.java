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

import model.TurmaAluno;
import util.SqlDates;

public class TurmaAlunoDAO {
  private final Connection conn;

  public TurmaAlunoDAO(Connection conn) {
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }
    this.conn = conn;
  }

  public TurmaAluno buscarPorAlunoEAnoLetivo(int idAluno, int anoLetivo) throws SQLException {
    if (idAluno <= 0) {
      throw new IllegalArgumentException("ID do aluno é inválido.");
    }
    if (anoLetivo < 2000) {
      throw new IllegalArgumentException("Ano letivo inválido.");
    }

    final String sql =
        """
        SELECT *
        FROM turma_aluno
        WHERE aluno_id = ?
        AND data_entrada <= ?
        AND (data_saida IS NULL OR data_saida >= ?)
        ORDER BY data_entrada DESC
        LIMIT 1
        """;

    String dataFimAno = anoLetivo + "-12-31";
    String dataInicioAno = anoLetivo + "-01-01";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAluno);
      stmt.setString(2, dataFimAno);
      stmt.setString(3, dataInicioAno);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapearTurmaAluno(rs);
        }
        return null;
      }
    }
  }

  public void inserir(TurmaAluno turmaAluno) throws SQLException {
    validarTurmaAluno(turmaAluno);

    final String sql =
        """
        INSERT INTO turma_aluno (
        turma_id,
        aluno_id,
        data_entrada,
        data_saida,
        ativo
        ) VALUES (?, ?, ?, ?, ?)
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, turmaAluno.getTurmaId());
      stmt.setInt(2, turmaAluno.getAlunoId());
      stmt.setDate(3, Date.valueOf(turmaAluno.getDataEntrada()));
      if (turmaAluno.getDataSaida() == null) {
        stmt.setNull(4, java.sql.Types.DATE);
      } else {
        stmt.setDate(4, Date.valueOf(turmaAluno.getDataSaida()));
      }
      stmt.setBoolean(5, turmaAluno.isAtivo());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException(
            "Falha ao inserir histórico de turma do aluno. Nenhuma linha afetada.");
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          turmaAluno.setIdTurmaAluno(rs.getInt(1));
        } else {
          throw new SQLException("Falha ao inserir histórico de turma do aluno. ID não retornado.");
        }
      }
    }
  }

  public boolean encerrarTurmaAtual(int idAluno, java.time.LocalDate dataSaida)
      throws SQLException {
    if (idAluno <= 0) {
      throw new IllegalArgumentException("ID do aluno é inválido.");
    }
    if (dataSaida == null) {
      throw new IllegalArgumentException("Data de saída da turma é obrigatória.");
    }

    final String sql =
        """
        UPDATE turma_aluno
        SET data_saida = ?,
        ativo = false
        WHERE aluno_id = ?
        AND ativo = true
        AND data_saida IS NULL
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(dataSaida));
      stmt.setInt(2, idAluno);
      return stmt.executeUpdate() > 0;
    }
  }

  public List<TurmaAluno> listarPorAluno(int idAluno) throws SQLException {
    if (idAluno <= 0) {
      throw new IllegalArgumentException("ID do aluno é inválido.");
    }

    final String sql =
        """
        SELECT *
        FROM turma_aluno
        WHERE aluno_id = ?
        ORDER BY data_entrada DESC
        """;

    List<TurmaAluno> lista = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAluno);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          lista.add(mapearTurmaAluno(rs));
        }
      }
    }

    return lista;
  }

  private TurmaAluno mapearTurmaAluno(ResultSet rs) throws SQLException {
    TurmaAluno turmaAluno = new TurmaAluno();
    turmaAluno.setIdTurmaAluno(rs.getInt("id_turmaluno"));
    turmaAluno.setTurmaId(rs.getInt("turma_id"));
    turmaAluno.setAlunoId(rs.getInt("aluno_id"));
    turmaAluno.setDataEntrada(SqlDates.getLocalDate(rs, "data_entrada"));
    if (SqlDates.getLocalDate(rs, "data_saida") != null) {
      turmaAluno.setDataSaida(SqlDates.getLocalDate(rs, "data_saida"));
    }
    turmaAluno.setAtivo(rs.getBoolean("ativo"));
    return turmaAluno;
  }

  private void validarTurmaAluno(TurmaAluno turmaAluno) {
    if (turmaAluno == null) {
      throw new IllegalArgumentException("Histórico de turma do aluno não pode ser nulo.");
    }
    if (turmaAluno.getAlunoId() <= 0) {
      throw new IllegalArgumentException("ID do aluno é inválido.");
    }
    if (turmaAluno.getTurmaId() <= 0) {
      throw new IllegalArgumentException("ID da turma é inválido.");
    }
    if (turmaAluno.getDataEntrada() == null) {
      throw new IllegalArgumentException("Data de entrada na turma é obrigatória.");
    }
  }
}
