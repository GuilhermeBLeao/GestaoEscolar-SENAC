// igor
// Classe DAO que gerencia as operações de banco de dados para a relação Professor-Disciplina

package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.ProfessorDisciplina;
import util.SqlDates;

public class ProfessorDisciplinaDAO {

  // Atributo que armazena a conexão com o banco de dados
  private final Connection conn;

  // Construtor que recebe a conexão com o banco de dados e valida se ela não é nula
  public ProfessorDisciplinaDAO(Connection conn) {
    // Valida se a conexão é nula lançando exceção caso verdadeiro
    if (conn == null) {
      throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    }
    // Atribui a conexão recebida ao atributo da classe
    this.conn = conn;
  }

  // Método que valida se o objeto ProfessorDisciplina não é nulo
  private void validarProfessorDisciplinaNaoNula(ProfessorDisciplina profDisciplina) {
    // Lança exceção se o objeto for nulo
    if (profDisciplina == null) {
      throw new IllegalArgumentException("Relação Professor-Disciplina não pode ser nula.");
    }
  }

  // Método que verifica se um professor já possui uma disciplina associada e ativa
  public boolean existeVinculacao(int idProfessor, int idDisciplina) throws SQLException {
    // Valida se os IDs são maiores que zero
    if (idProfessor <= 0 || idDisciplina <= 0) {
      throw new IllegalArgumentException("IDs inválidos.");
    }

    // Define a consulta SQL para verificar a existência da vinculação ativa
    final String sql =
        "SELECT 1 FROM professor_disciplina WHERE id_professor = ? AND id_disciplina = ? AND ativo"
            + " = true";

    // Cria um statement preparado com a consulta SQL
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      // Define o primeiro parâmetro como o ID do professor
      stmt.setInt(1, idProfessor);
      // Define o segundo parâmetro como o ID da disciplina
      stmt.setInt(2, idDisciplina);

      // Executa a consulta e armazena o resultado
      try (ResultSet rs = stmt.executeQuery()) {
        // Retorna true se encontrou registro, false caso contrário
        return rs.next();
      }
    }
  }

  // Método que insere uma nova vinculação Professor-Disciplina no banco de dados
  public void inserir(ProfessorDisciplina profDisciplina) throws SQLException {
    // Valida se o objeto não é nulo
    validarProfessorDisciplinaNaoNula(profDisciplina);

    // Define a consulta SQL de inserção com os campos necessários
    final String sql =
        """
        INSERT INTO professor_disciplina (
        id_professor,
        id_disciplina,
        data_vinculacao,
        ativo
        ) VALUES (?, ?, ?, true)
        """;

    // Cria um statement preparado que retorna as chaves geradas
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      // Define o primeiro parâmetro como o ID do professor
      stmt.setInt(1, profDisciplina.getIdProfessor());
      // Define o segundo parâmetro como o ID da disciplina
      stmt.setInt(2, profDisciplina.getIdDisciplina());
      // Define o terceiro parâmetro como a data de vinculação
      stmt.setDate(3, Date.valueOf(profDisciplina.getDataVinculacao()));

      // Executa a inserção e armazena o número de linhas afetadas
      int linhasAfetadas = stmt.executeUpdate();

      // Valida se pelo menos uma linha foi inserida
      if (linhasAfetadas == 0) {
        throw new SQLException("Falha ao vincular professor à disciplina.");
      }

      // Recupera as chaves geradas (ID do registro inserido)
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        // Valida se a chave foi gerada
        if (rs.next()) {
          // Define o ID da relação Professor-Disciplina no objeto
          profDisciplina.setIdProfessorDisciplina(rs.getInt(1));
        } else {
          throw new SQLException("ID não retornado.");
        }
      }
    }
  }

  // Método que busca uma vinculação Professor-Disciplina pelo seu ID
  public ProfessorDisciplina buscarPorId(int idProfessorDisciplina) throws SQLException {
    // Valida se o ID é maior que zero
    if (idProfessorDisciplina <= 0) {
      throw new IllegalArgumentException("ID inválido.");
    }

    // Define a consulta SQL de seleção dos campos
    final String sql =
        """
        SELECT id_professor_disciplina, id_professor, id_disciplina, data_vinculacao, data_desvinculacao, ativo
        FROM professor_disciplina
        WHERE id_professor_disciplina = ?
        """;

    // Cria um statement preparado com a consulta
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      // Define o parâmetro como o ID da relação
      stmt.setInt(1, idProfessorDisciplina);

      // Executa a consulta e armazena o resultado
      try (ResultSet rs = stmt.executeQuery()) {
        // Valida se encontrou um registro
        if (rs.next()) {
          // Retorna o objeto mapeado a partir do resultado
          return mapearProfessorDisciplina(rs);
        }
        // Retorna nulo se não encontrou nada
        return null;
      }
    }
  }

  // Método que busca todas as disciplinas associadas a um professor ativo
  public List<ProfessorDisciplina> buscarPorProfessor(int idProfessor) throws SQLException {
    // Valida se o ID do professor é maior que zero
    if (idProfessor <= 0) {
      throw new IllegalArgumentException("ID do professor inválido.");
    }

    // Define a consulta SQL que filtra por ID do professor
    final String sql =
        """
        SELECT id_professor_disciplina, id_professor, id_disciplina, data_vinculacao, data_desvinculacao, ativo
        FROM professor_disciplina
        WHERE id_professor = ? AND ativo = true
        """;

    // Cria uma lista para armazenar os resultados
    List<ProfessorDisciplina> lista = new ArrayList<>();

    // Cria um statement preparado com a consulta
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      // Define o parâmetro como o ID do professor
      stmt.setInt(1, idProfessor);

      // Executa a consulta e armazena o resultado
      try (ResultSet rs = stmt.executeQuery()) {
        // Itera enquanto existirem registros
        while (rs.next()) {
          // Mapeia o registro e adiciona à lista
          lista.add(mapearProfessorDisciplina(rs));
        }
      }
    }

    // Retorna a lista de vinculações encontradas
    return lista;
  }

  // Método que busca todos os professores associados a uma disciplina ativa
  public List<ProfessorDisciplina> buscarPorDisciplina(int idDisciplina) throws SQLException {
    // Valida se o ID da disciplina é maior que zero
    if (idDisciplina <= 0) {
      throw new IllegalArgumentException("ID da disciplina inválido.");
    }

    // Define a consulta SQL que filtra por ID da disciplina
    final String sql =
        """
        SELECT id_professor_disciplina, id_professor, id_disciplina, data_vinculacao, data_desvinculacao, ativo
        FROM professor_disciplina
        WHERE id_disciplina = ? AND ativo = true
        """;

    // Cria uma lista para armazenar os resultados
    List<ProfessorDisciplina> lista = new ArrayList<>();

    // Cria um statement preparado com a consulta
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      // Define o parâmetro como o ID da disciplina
      stmt.setInt(1, idDisciplina);

      // Executa a consulta e armazena o resultado
      try (ResultSet rs = stmt.executeQuery()) {
        // Itera enquanto existirem registros
        while (rs.next()) {
          // Mapeia o registro e adiciona à lista
          lista.add(mapearProfessorDisciplina(rs));
        }
      }
    }

    // Retorna a lista de vinculações encontradas
    return lista;
  }

  // Método que realiza a exclusão lógica (inativa) de uma vinculação Professor-Disciplina
  public boolean inativar(int idProfessorDisciplina) throws SQLException {
    // Valida se o ID é maior que zero
    if (idProfessorDisciplina <= 0) {
      throw new IllegalArgumentException("ID inválido.");
    }

    // Define a consulta SQL que atualiza o status para inativo
    final String sql =
        "UPDATE professor_disciplina SET ativo = false WHERE id_professor_disciplina = ? AND ativo"
            + " = true";

    // Cria um statement preparado com a consulta
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      // Define o parâmetro como o ID da relação
      stmt.setInt(1, idProfessorDisciplina);

      // Executa a atualização e armazena o número de linhas afetadas
      int linhasAfetadas = stmt.executeUpdate();

      // Retorna true se uma linha foi afetada, false caso contrário
      return linhasAfetadas > 0;
    }
  }

  // Método que mapeia uma linha do ResultSet para um objeto ProfessorDisciplina
  private ProfessorDisciplina mapearProfessorDisciplina(ResultSet rs) throws SQLException {
    // Cria um novo objeto ProfessorDisciplina
    ProfessorDisciplina profDisciplina = new ProfessorDisciplina();

    // Define o ID da relação Professor-Disciplina
    profDisciplina.setIdProfessorDisciplina(rs.getInt("id_professor_disciplina"));
    // Define o ID do professor
    profDisciplina.setIdProfessor(rs.getInt("id_professor"));
    // Define o ID da disciplina
    profDisciplina.setIdDisciplina(rs.getInt("id_disciplina"));
    // Define a data de vinculação a partir da coluna do banco de dados
    profDisciplina.setDataVinculacao(SqlDates.getLocalDate(rs, "data_vinculacao"));
    // Verifica se existe data de desvinculação antes de atribuir
    if (SqlDates.getLocalDate(rs, "data_desvinculacao") != null) {
      profDisciplina.setDataDesvinculacao(SqlDates.getLocalDate(rs, "data_desvinculacao"));
    }
    // Define o status ativo/inativo
    profDisciplina.setAtivo(rs.getBoolean("ativo"));

    // Retorna o objeto mapeado
    return profDisciplina;
  }
}
