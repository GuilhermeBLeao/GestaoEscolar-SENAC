/*Igor
Guilherme - adicionou exclusão lógica (Inativar ao invés de excluir)*/

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Turma;
import variaveisEnum.Turno;

public class TurmaDAO {

  private final Connection conn;

  public TurmaDAO(Connection conn) {
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }
    this.conn = conn;
  }

  // Insere uma nova turma (sempre ativa)
  public void inserir(Turma turma) throws SQLException {
    validarTurmaNaoNula(turma);

    final String sql =
        """
        INSERT INTO turma (
        sala_id,
        descricao_turma,
        turno,
        ativo
        ) VALUES (?, ?, ?, true)
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, turma.getSalaId());
      stmt.setString(2, turma.getDescricaoTurma());
      stmt.setString(3, turma.getTurno().name());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao inserir turma. Nenhuma linha afetada.");
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          turma.setIdTurma(rs.getInt(1));
        } else {
          throw new SQLException("Falha ao inserir turma. ID não retornado.");
        }
      }
    }
  }

  // Atualiza turma (apenas se estiver ativa)
  public void atualizar(Turma turma) throws SQLException {
    validarTurmaNaoNula(turma);

    if (turma.getIdTurma() <= 0) {
      throw new IllegalArgumentException("ID da turma inválido.");
    }

    final String sql =
        """
        UPDATE turma
        SET sala_id = ?,
        descricao_turma = ?,
        turno = ?
        WHERE id_turma = ?
        AND ativo = true
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, turma.getSalaId());
      stmt.setString(2, turma.getDescricaoTurma());
      stmt.setString(3, turma.getTurno().name());
      stmt.setInt(4, turma.getIdTurma());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        verificarFalhaAtualizacao(turma.getIdTurma());
      }
    }
  }

  // Busca turma por ID (ativa ou inativa)
  public Turma buscarPorId(int idTurma) throws SQLException {
    if (idTurma <= 0) {
      throw new IllegalArgumentException("ID da turma inválido.");
    }

    final String sql =
        """
        SELECT *
        FROM turma
        WHERE id_turma = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);

      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? mapearTurma(rs) : null;
      }
    }
  }

  // Busca turma por descrição
  public Turma buscarPorDescricao(String descricao) throws SQLException {
    if (descricao == null || descricao.trim().isEmpty()) {
      throw new IllegalArgumentException("Descrição inválida.");
    }

    final String sql =
        """
        SELECT *
        FROM turma
        WHERE descricao_turma = ?
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, descricao.trim());

      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? mapearTurma(rs) : null;
      }
    }
  }

  // Lista todas as turmas (ativas e inativas)
  public List<Turma> listarTodos() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM turma
        ORDER BY descricao_turma
        """;

    List<Turma> lista = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        lista.add(mapearTurma(rs));
      }
    }
    return lista;
  }

  // Lista apenas turmas ativas
  public List<Turma> listarAtivas() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM turma
        WHERE ativo = true
        ORDER BY descricao_turma
        """;

    List<Turma> lista = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        lista.add(mapearTurma(rs));
      }
    }
    return lista;
  }

  // Lista apenas turmas inativas
  public List<Turma> listarInativas() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM turma
        WHERE ativo = false
        ORDER BY descricao_turma
        """;

    List<Turma> lista = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        lista.add(mapearTurma(rs));
      }
    }
    return lista;
  }

  // Inativa turma (exclusão lógica)
  public boolean inativar(int idTurma) throws SQLException {
    if (idTurma <= 0) {
      throw new IllegalArgumentException("ID da turma inválido.");
    }

    final String sql =
        """
        UPDATE turma
        SET ativo = false
        WHERE id_turma = ?
        AND ativo = true
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        verificarFalhaInativacao(idTurma);
      }

      return true;
    }
  }

  // Reativa turma
  public boolean reativar(int idTurma) throws SQLException {
    if (idTurma <= 0) {
      throw new IllegalArgumentException("ID da turma inválido.");
    }

    final String sql =
        """
        UPDATE turma
        SET ativo = true
        WHERE id_turma = ?
        AND ativo = false
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        verificarFalhaReativacao(idTurma);
      }

      return true;
    }
  }

  // Mapeia ResultSet → Turma
  private Turma mapearTurma(ResultSet rs) throws SQLException {
    Turma turma = new Turma();
    turma.setIdTurma(rs.getInt("id_turma"));
    turma.setSalaId(rs.getInt("sala_id"));
    turma.setDescricaoTurma(rs.getString("descricao_turma"));
    turma.setTurno(Turno.valueOf(rs.getString("turno"))); // deve bater com enum
    turma.setAtivo(rs.getBoolean("ativo"));
    return turma;
  }

  // Validação de objeto nulo
  private void validarTurmaNaoNula(Turma turma) {
    if (turma == null) {
      throw new IllegalArgumentException("Turma não pode ser nula.");
    }
  }

  // Verifica falha na atualização
  private void verificarFalhaAtualizacao(int idTurma) throws SQLException {
    final String sql = "SELECT ativo FROM turma WHERE id_turma = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Turma não encontrada.");
        }

        if (!rs.getBoolean("ativo")) {
          throw new SQLException("Turma está inativa e não pode ser atualizada.");
        }
      }
    }
  }

  // Verifica falha ao inativar
  private void verificarFalhaInativacao(int idTurma) throws SQLException {
    final String sql = "SELECT ativo FROM turma WHERE id_turma = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Turma não encontrada.");
        }

        if (!rs.getBoolean("ativo")) {
          throw new SQLException("Turma já está inativa.");
        }
      }
    }
  }

  // Verifica falha ao reativar
  private void verificarFalhaReativacao(int idTurma) throws SQLException {
    final String sql = "SELECT ativo FROM turma WHERE id_turma = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Turma não encontrada.");
        }

        if (rs.getBoolean("ativo")) {
          throw new SQLException("Turma já está ativa.");
        }
      }
    }
  }
}
