// Guilherme

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Boletim;
import model.BoletimItem;
import model.Trimestre;
import util.SqlDates;

public class BoletimDAO {
  private final Connection conn;

  public BoletimDAO(Connection conn) {
    if (conn == null) throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
    this.conn = conn;
  }

  public Boletim buscarPorAluno(int idAluno, int idTrimestre) throws SQLException {
    if (idAluno <= 0) throw new IllegalArgumentException("ID do aluno inválido.");
    if (idTrimestre <= 0) throw new IllegalArgumentException("ID do trimestre inválido.");

    final String sql =
        """
        SELECT
        a.id_aluno,
        a.nome          AS nome_aluno,
        a.matricula,
        a.turma_id,
        d.id_disciplina,
        d.descricao     AS nome_disciplina,
        AVG(n.nota)     AS media,
        t.id_trimestre  AS trimestre_id,
        t.numero        AS trimestre_numero,
        t.data_inicio   AS trimestre_inicio,
        t.data_fim      AS trimestre_fim,
        t.ano_letivo    AS ano_letivo
        FROM aluno a
        INNER JOIN nota n       ON n.aluno_id      = a.id_aluno
        INNER JOIN disciplina d ON d.id_disciplina = n.disciplina_id
        INNER JOIN trimestre t  ON t.id_trimestre  = n.trimestre_id
        WHERE a.id_aluno     = ?
        AND n.trimestre_id = ?
        GROUP BY
        a.id_aluno, a.nome, a.matricula, a.turma_id,
        d.id_disciplina, d.descricao,
        t.id_trimestre, t.numero, t.data_inicio, t.data_fim, t.ano_letivo
        ORDER BY d.descricao
        """;
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idAluno);
      stmt.setInt(2, idTrimestre);

      try (ResultSet rs = stmt.executeQuery()) {
        Boletim boletim = null;

        while (rs.next()) {
          if (boletim == null) boletim = mapearCabecalho(rs);
          boletim.adicionarItem(mapearItem(rs));
        }
        return boletim;
      }
    }
  }

  public List<Boletim> buscarPorTurma(int idTurma, int idTrimestre) throws SQLException {
    if (idTurma <= 0) throw new IllegalArgumentException("ID da turma inválido.");
    if (idTrimestre <= 0) throw new IllegalArgumentException("ID do trimestre inválido.");

    final String sql =
        """
        SELECT
        a.id_aluno,
        a.nome          AS nome_aluno,
        a.matricula,
        a.turma_id,
        d.id_disciplina,
        d.descricao     AS nome_disciplina,
        AVG(n.nota)     AS media,
        t.id_trimestre  AS trimestre_id,
        t.numero        AS trimestre_numero,
        t.data_inicio   AS trimestre_inicio,
        t.data_fim      AS trimestre_fim,
        t.ano_letivo    AS ano_letivo
        FROM aluno a
        INNER JOIN nota n       ON n.aluno_id      = a.id_aluno
        INNER JOIN disciplina d ON d.id_disciplina = n.disciplina_id
        INNER JOIN trimestre t  ON t.id_trimestre  = n.trimestre_id
        WHERE a.turma_id     = ?
        AND n.trimestre_id = ?
        GROUP BY
        a.id_aluno, a.nome, a.matricula, a.turma_id,
        d.id_disciplina, d.descricao,
        t.id_trimestre, t.numero, t.data_inicio, t.data_fim, t.ano_letivo
        ORDER BY a.nome, d.descricao
        """;

    Map<Integer, Boletim> boletins = new LinkedHashMap<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idTurma);
      stmt.setInt(2, idTrimestre);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int idAluno = rs.getInt("id_aluno");

          Boletim boletim = boletins.get(idAluno);
          if (boletim == null) {
            boletim = mapearCabecalho(rs);
            boletins.put(idAluno, boletim);
          }
          boletim.adicionarItem(mapearItem(rs));
        }
      }
    }
    return new ArrayList<>(boletins.values());
  }

  private Boletim mapearCabecalho(ResultSet rs) throws SQLException {
    Trimestre trimestre = new Trimestre();
    trimestre.setIdTrimestre(rs.getInt("trimestre_id"));
    trimestre.setNumero(rs.getInt("trimestre_numero"));
    trimestre.setDataInicio(SqlDates.getLocalDate(rs, "trimestre_inicio"));
    trimestre.setDataFim(SqlDates.getLocalDate(rs, "trimestre_fim"));
    trimestre.setAnoLetivo(rs.getInt("ano_letivo"));

    Boletim boletim = new Boletim();
    boletim.setIdAluno(rs.getInt("id_aluno"));
    boletim.setNomeAluno(rs.getString("nome_aluno"));
    boletim.setMatricula(rs.getString("matricula"));
    boletim.setIdTurma(rs.getInt("turma_id"));
    boletim.setDatageracao(LocalDate.now());
    boletim.setTrimestre(trimestre);

    return boletim;
  }

  private BoletimItem mapearItem(ResultSet rs) throws SQLException {
    BoletimItem item = new BoletimItem();

    item.setIdDisciplina(rs.getInt("id_disciplina"));
    item.setNomeDisciplina(rs.getString("nome_disciplina"));
    item.setMedia(rs.getDouble("media"));

    return item;
  }
}
