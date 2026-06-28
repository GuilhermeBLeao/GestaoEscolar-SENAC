// Guilherme

// Define o pacote desta classe
package dao;

// Importa classe de outro pacote
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Sala;

public class SalaDAO {

  private final Connection conn;

  public SalaDAO(Connection conn) {
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }
    this.conn = conn;
  }

  // Insere a sala no banco
  public void inserir(Sala sala) throws SQLException {
    validarSalaNaoNula(sala);

    final String sql = "INSERT INTO sala (capacidade, ativo) VALUES (?, true)";

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, sala.getCapacidade());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao inserir sala. Nenhuma linha afetada.");
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          sala.setIdSala(rs.getInt(1));
        } else {
          throw new SQLException("Falha ao inserir sala. ID não retornado.");
        }
      }
    }
  }

  // Atualiza o cadastro da sala no banco
  public void atualizar(Sala sala) throws SQLException {
    validarSalaNaoNula(sala);

    if (sala.getIdSala() <= 0) {
      throw new IllegalArgumentException("ID da sala inválido.");
    }

    final String sql = "UPDATE sala SET capacidade = ? WHERE id_sala = ? AND ativo = true";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, sala.getCapacidade());
      stmt.setInt(2, sala.getIdSala());

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        verificarFalhaAtualizacao(sala.getIdSala());
      }
    }
  }

  // Busca sala pelo ID - Todas incluindo ativas e inativas
  public Sala buscarPorId(int idSala) throws SQLException {
    final String sql = "SELECT * FROM sala WHERE id_sala = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idSala);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapearSala(rs);
        }
        return null;
      }
    }
  }

  // Gera lista de todas as salas - Incluindo ativas e inativas
  public List<Sala> listarTodas() throws SQLException {
    final String sql = "SELECT * FROM sala ORDER BY id_sala";

    List<Sala> salas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        salas.add(mapearSala(rs));
      }
    }
    return salas;
  }

  // Gera lista das salas ativas
  public List<Sala> listarAtivas() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM sala
        WHERE ativo = true
        ORDER BY id_sala
        """;

    List<Sala> salas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        salas.add(mapearSala(rs));
      }
    }
    return salas;
  }

  // Gera lista das salas inativas
  public List<Sala> listarInativas() throws SQLException {
    final String sql =
        """
        SELECT *
        FROM sala
        WHERE ativo = false
        ORDER BY id_sala
        """;

    List<Sala> salas = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        salas.add(mapearSala(rs));
      }
    }
    return salas;
  }

  // Exclusão Lógica - Inativar
  public boolean inativar(int idSala) throws SQLException {
    final String sql =
        """
        UPDATE sala
        SET ativo = false
        WHERE id_sala = ?
        AND ativo = true
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idSala);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        verificarFalhaInativacao(idSala);
      }

      return true;
    }
  }

  // Exclusão Lógica - Reativar
  public boolean reativar(int idSala) throws SQLException {
    final String sql =
        """
        UPDATE sala
        SET ativo = true
        WHERE id_sala = ?
        AND ativo = false
        """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idSala);

      int linhasAfetadas = stmt.executeUpdate();
      if (linhasAfetadas == 0) {
        verificarFalhaReativacao(idSala);
      }
      return true;
    }
  }

  // Mapeamento da sala
  private Sala mapearSala(ResultSet rs) throws SQLException {
    Sala sala = new Sala();
    sala.setIdSala(rs.getInt("id_sala"));
    sala.setCapacidade(rs.getInt("capacidade"));
    sala.setAtivo(rs.getBoolean("ativo"));
    return sala;
  }

  // Valida Nulo
  private void validarSalaNaoNula(Sala sala) {
    if (sala == null) {
      throw new IllegalArgumentException("Sala não pode ser nula.");
    }
  }

  // Verifica o motivo da falha ao inativar - ID inválido ou cadastro já inativo
  private void verificarFalhaInativacao(int idSala) throws SQLException {
    final String sql = "SELECT ativo FROM sala WHERE id_sala = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idSala);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Sala não encontrada.");
        }
        if (!rs.getBoolean("ativo")) {
          throw new SQLException("Sala já está inativa.");
        }
      }
    }
  }

  // Verifica o motivo da falha ao reativar - ID inválido ou cadastro já ativo
  private void verificarFalhaReativacao(int idSala) throws SQLException {
    final String sql = "SELECT ativo FROM sala WHERE id_sala = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idSala);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Sala não encontrada.");
        }
        if (rs.getBoolean("ativo")) {
          throw new SQLException("Sala já está ativa.");
        }
      }
    }
  }

  // Verifica o motivo da falha ao atualizar - ID inválido ou cadastro já inativo
  private void verificarFalhaAtualizacao(int idSala) throws SQLException {
    final String sql = "SELECT ativo FROM sala WHERE id_sala = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idSala);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Sala não encontrada.");
        }

        if (!rs.getBoolean("ativo")) {
          throw new SQLException("Sala está inativa e não pode ser atualizada.");
        }
      }
    }
  }
}
