package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import database.ConnectionFactory;
import model.Usuario;

public final class DadosSistema {
  private DadosSistema() {}

  public static Usuario usuarioLogado() {
    return SessaoUsuario.getUsuarioLogado();
  }

  public static String nomeUsuarioLogado() {
    Usuario usuario = usuarioLogado();
    if (usuario == null) {
      return "Usuario";
    }
    try {
      switch (usuario.getTipoUsuario()) {
        case ALUNO:
          return nomeAluno(usuario.getAlunoId());
        case PROFESSOR:
          return nomeProfessor(usuario.getProfessorId());
        case RESPONSAVEL:
          return nomeResponsavel(usuario.getPaiId());
        case ADMINISTRADOR:
        case SECRETARIA:
        case DIRECAO:
        case PEDAGOGICO:
          return nomeFuncionario(usuario.getFuncionarioId());
        default:
          return "Usuario";
      }
    } catch (Exception e) {
      return "Usuario";
    }
  }

  public static int alunoIdAtual() {
    Usuario usuario = usuarioLogado();
    if (usuario == null) {
      return 1;
    }
    if (usuario.getAlunoId() > 0) {
      return usuario.getAlunoId();
    }
    if (usuario.getPaiId() > 0) {
      return primeiroInt(
          "SELECT id_aluno FROM aluno WHERE pais_id = ? ORDER BY nome LIMIT 1",
          usuario.getPaiId(),
          1);
    }
    return 1;
  }

  public static int professorIdAtual() {
    Usuario usuario = usuarioLogado();
    return usuario != null && usuario.getProfessorId() > 0 ? usuario.getProfessorId() : 1;
  }

  public static int funcionarioIdAtual() {
    Usuario usuario = usuarioLogado();
    return usuario != null && usuario.getFuncionarioId() > 0 ? usuario.getFuncionarioId() : 1;
  }

  public static String nomeAlunoAtual() {
    return nomeAluno(alunoIdAtual());
  }

  public static String turmaAlunoAtual() {
    return primeiroTexto(
        "SELECT t.descricao_turma FROM aluno a INNER JOIN turma t ON t.id_turma = a.turma_id WHERE"
            + " a.id_aluno = ?",
        alunoIdAtual(),
        "Turma");
  }

  public static int totalDisciplinasAlunoAtual() {
    return primeiroInt(
        "SELECT COUNT(*) FROM turma_disciplina td INNER JOIN aluno a ON a.turma_id = td.turma_id"
            + " WHERE a.id_aluno = ?",
        alunoIdAtual(),
        0);
  }

  public static String mediaAlunoAtual() {
    double media =
        primeiroDouble("SELECT AVG(nota) FROM nota WHERE aluno_id = ?", alunoIdAtual(), 0);
    return String.format(java.util.Locale.US, "%.2f", media).replace('.', ',');
  }

  public static String frequenciaAlunoAtual() {
    double total =
        primeiroDouble("SELECT COUNT(*) FROM presenca WHERE aluno_id = ?", alunoIdAtual(), 0);
    if (total == 0) {
      return "100%";
    }
    double presentes =
        primeiroDouble(
            "SELECT COUNT(*) FROM presenca WHERE aluno_id = ? AND presente = true",
            alunoIdAtual(),
            0);
    return Math.round((presentes / total) * 100) + "%";
  }

  public static List<Object[]> disciplinasAlunoAtual() {
    String sql =
        "SELECT d.codigo, d.descricao, COALESCE(p.nome, 'Professor nao vinculado') AS professor,"
            + " d.carga_horaria, t.descricao_turma, d.ativo FROM aluno a INNER JOIN"
            + " turma_disciplina td ON td.turma_id = a.turma_id INNER JOIN disciplina d ON"
            + " d.id_disciplina = td.disciplina_id LEFT JOIN professor_disciplina pd ON"
            + " pd.id_disciplina = d.id_disciplina AND pd.ativo = true LEFT JOIN professor p ON"
            + " p.id_professor = pd.id_professor LEFT JOIN turma t ON t.id_turma = a.turma_id WHERE"
            + " a.id_aluno = ? ORDER BY d.descricao";
    return consultarLinhas(
        sql,
        alunoIdAtual(),
        rs ->
            new Object[] {
              rs.getString("codigo"),
              rs.getString("descricao"),
              rs.getString("professor"),
              rs.getInt("carga_horaria") + "h",
              "Conforme horario",
              rs.getString("descricao_turma"),
              rs.getBoolean("ativo") ? "Ativa" : "Inativa"
            });
  }

  public static List<Object[]> frequenciaAlunoAtualLinhas() {
    String sql =
        "SELECT pr.data_presenca, d.descricao, COALESCE(p.nome, '-') AS professor, pr.presente,"
            + " pr.falta_justificada, pr.falta_abonada, pr.motivo_abonada FROM presenca pr INNER"
            + " JOIN disciplina d ON d.id_disciplina = pr.disciplina_id LEFT JOIN"
            + " professor_disciplina pd ON pd.id_disciplina = d.id_disciplina AND pd.ativo = true"
            + " LEFT JOIN professor p ON p.id_professor = pd.id_professor WHERE pr.aluno_id = ?"
            + " ORDER BY pr.data_presenca DESC";
    return consultarLinhas(
        sql,
        alunoIdAtual(),
        rs -> {
          boolean presente = rs.getBoolean("presente");
          boolean justificada = rs.getBoolean("falta_justificada");
          boolean abonada = rs.getBoolean("falta_abonada");
          String registro = presente ? "Presente" : "Falta";
          String situacao =
              presente
                  ? "Presente"
                  : abonada ? "Falta abonada" : justificada ? "Falta justificada" : "Falta";
          String justificativa = rs.getString("motivo_abonada");
          if (justificativa == null || justificativa.isBlank()) {
            justificativa = justificada || abonada ? "Justificada" : "-";
          }
          return new Object[] {
            rs.getString("data_presenca"),
            rs.getString("descricao"),
            rs.getString("professor"),
            "Aula",
            registro,
            justificativa,
            situacao
          };
        });
  }

  public static List<Object[]> frequenciaResponsavelAlunoLinhas() {
    List<Object[]> linhas = new ArrayList<>();
    for (Object[] linha : frequenciaAlunoAtualLinhas()) {
      linhas.add(new Object[] {linha[0], linha[1], linha[2], linha[4], linha[5]});
    }
    return linhas;
  }

  public static int totalRegistrosFrequenciaAlunoAtual() {
    return primeiroInt("SELECT COUNT(*) FROM presenca WHERE aluno_id = ?", alunoIdAtual(), 0);
  }

  public static int totalPresencasAlunoAtual() {
    return primeiroInt(
        "SELECT COUNT(*) FROM presenca WHERE aluno_id = ? AND presente = true", alunoIdAtual(), 0);
  }

  public static int totalFaltasAlunoAtual() {
    return primeiroInt(
        "SELECT COUNT(*) FROM presenca WHERE aluno_id = ? AND presente = false", alunoIdAtual(), 0);
  }

  public static int totalFaltasJustificadasAlunoAtual() {
    return primeiroInt(
        "SELECT COUNT(*) FROM presenca WHERE aluno_id = ? AND presente = false AND"
            + " (falta_justificada = true OR falta_abonada = true)",
        alunoIdAtual(),
        0);
  }

  public static List<Object[]> advertenciasAlunoAtualLinhas() {
    String sql =
        "SELECT ad.data_advertencia, ad.motivo, COALESCE(p.nome, '-') AS professor "
            + "FROM advertencia ad LEFT JOIN professor p ON p.id_professor = ad.professor_id "
            + "WHERE ad.aluno_id = ? ORDER BY ad.data_advertencia DESC";
    return consultarLinhas(
        sql,
        alunoIdAtual(),
        rs ->
            new Object[] {
              rs.getString("data_advertencia"),
              rs.getString("motivo"),
              rs.getString("professor"),
              "Ativa"
            });
  }

  public static List<Object[]> advertenciasResponsavelAlunoLinhas() {
    List<Object[]> linhas = new ArrayList<>();
    for (Object[] linha : advertenciasAlunoAtualLinhas()) {
      linhas.add(new Object[] {linha[0], linha[1], linha[2], "-", linha[3]});
    }
    return linhas;
  }

  public static int totalAdvertenciasAlunoAtual() {
    return primeiroInt("SELECT COUNT(*) FROM advertencia WHERE aluno_id = ?", alunoIdAtual(), 0);
  }

  public static Object[] dadosAlunoAtualLinha() {
    String sql =
        "SELECT a.nome, a.cpf, a.rg, a.data_nascimento, a.sexo, a.telefone, a.email, a.situacao, "
            + "a.matricula, a.data_cadastro, a.obs_saude, t.descricao_turma, t.turno, "
            + "e.rua, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep, "
            + "COALESCE(pa.nome_mae, pa.nome_pai) AS responsavel_nome, "
            + "COALESCE(pa.telefone_mae, pa.telefone_pai) AS responsavel_telefone, "
            + "COALESCE(pa.email_mae, pa.email_pai) AS responsavel_email "
            + "FROM aluno a "
            + "LEFT JOIN turma t ON t.id_turma = a.turma_id "
            + "LEFT JOIN endereco e ON e.aluno_id = a.id_aluno "
            + "LEFT JOIN pais_aluno pa ON pa.id_pais = a.pais_id "
            + "WHERE a.id_aluno = ?";
    List<Object[]> linhas =
        consultarLinhas(
            sql,
            alunoIdAtual(),
            rs ->
                new Object[] {
                  nvl(rs.getString("nome"), "Aluno"),
                  nvl(rs.getString("descricao_turma"), "Turma"),
                  nvl(rs.getString("situacao"), "Ativo"),
                  nvl(rs.getString("cpf"), "-"),
                  nvl(rs.getString("rg"), "-"),
                  nvl(rs.getString("data_nascimento"), "-"),
                  nvl(rs.getString("sexo"), "-"),
                  nvl(rs.getString("telefone"), "-"),
                  nvl(rs.getString("email"), "-"),
                  nvl(rs.getString("matricula"), "-"),
                  nvl(rs.getString("turno"), "-"),
                  nvl(rs.getString("rua"), "-"),
                  nvl(rs.getString("numero"), "-"),
                  nvl(rs.getString("bairro"), "-"),
                  cidadeUf(rs.getString("cidade"), rs.getString("estado")),
                  nvl(rs.getString("responsavel_nome"), "-"),
                  nvl(rs.getString("responsavel_telefone"), "-"),
                  nvl(rs.getString("responsavel_email"), "-"),
                  nvl(rs.getString("cep"), "-"),
                  nvl(rs.getString("complemento"), "-"),
                  nvl(rs.getString("data_cadastro"), "-"),
                  nvl(rs.getString("obs_saude"), "Sem observacoes cadastradas")
                });
    return linhas.isEmpty()
        ? new Object[] {
          "Aluno",
          "Turma",
          "Ativo",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "-",
          "Sem observacoes cadastradas"
        }
        : linhas.get(0);
  }

  public static Object[] dadosProfessorAtualLinha() {
    String sql =
        "SELECT p.nome, p.cpf, p.rg, p.data_nascimento, p.sexo, p.telefone, p.formacao, p.ativo, "
            + "e.rua, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep "
            + "FROM professor p LEFT JOIN endereco_professor e ON e.professor_id = p.id_professor "
            + "WHERE p.id_professor = ?";
    List<Object[]> linhas =
        consultarLinhas(
            sql,
            professorIdAtual(),
            rs ->
                new Object[] {
                  nvl(rs.getString("nome"), "Professor"),
                  nvl(rs.getString("cpf"), "-"),
                  nvl(rs.getString("rg"), "-"),
                  nvl(rs.getString("data_nascimento"), "-"),
                  nvl(rs.getString("sexo"), "-"),
                  nvl(rs.getString("telefone"), "-"),
                  nvl(rs.getString("formacao"), "-"),
                  rs.getBoolean("ativo") ? "Ativo" : "Inativo",
                  nvl(rs.getString("rua"), "-"),
                  nvl(rs.getString("numero"), "-"),
                  nvl(rs.getString("bairro"), "-"),
                  cidadeUf(rs.getString("cidade"), rs.getString("estado")),
                  nvl(rs.getString("cep"), "-"),
                  nvl(rs.getString("complemento"), "-")
                });
    return linhas.isEmpty()
        ? new Object[] {
          "Professor", "-", "-", "-", "-", "-", "-", "Ativo", "-", "-", "-", "-", "-", "-"
        }
        : linhas.get(0);
  }

  public static Object[] dadosResponsavelAtualLinha() {
    Usuario usuario = usuarioLogado();
    int paiId =
        usuario != null && usuario.getPaiId() > 0
            ? usuario.getPaiId()
            : primeiroInt("SELECT pais_id FROM aluno WHERE id_aluno = ?", alunoIdAtual(), 0);
    String sql =
        "SELECT pa.nome_mae, pa.nome_pai, pa.email_mae, pa.email_pai, pa.telefone_mae,"
            + " pa.telefone_pai, pa.cpf_mae, pa.cpf_pai, pa.ativo FROM pais_aluno pa WHERE"
            + " pa.id_pais = ?";
    List<Object[]> linhas =
        consultarLinhas(
            sql,
            paiId,
            rs ->
                new Object[] {
                  primeiroNaoVazio(
                      rs.getString("nome_mae"), rs.getString("nome_pai"), "Responsavel"),
                  primeiroNaoVazio(rs.getString("cpf_mae"), rs.getString("cpf_pai"), "-"),
                  primeiroNaoVazio(rs.getString("telefone_mae"), rs.getString("telefone_pai"), "-"),
                  primeiroNaoVazio(rs.getString("email_mae"), rs.getString("email_pai"), "-"),
                  rs.getBoolean("ativo") ? "Ativo" : "Inativo"
                });
    return linhas.isEmpty() ? new Object[] {"Responsavel", "-", "-", "-", "Ativo"} : linhas.get(0);
  }

  public static List<Object[]> notasAlunoAtualLinhas() {
    String sql =
        "SELECT d.descricao, t.numero, n.nota "
            + "FROM nota n "
            + "INNER JOIN disciplina d ON d.id_disciplina = n.disciplina_id "
            + "INNER JOIN trimestre t ON t.id_trimestre = n.trimestre_id "
            + "WHERE n.aluno_id = ? ORDER BY d.descricao, t.numero, n.data_lancamento, n.notas_id";
    List<Object[]> notas =
        consultarLinhas(
            sql,
            alunoIdAtual(),
            rs ->
                new Object[] {
                  rs.getString("descricao"), rs.getInt("numero"), formatarNota(rs.getDouble("nota"))
                });
    Map<String, Object[]> porDisciplina = new LinkedHashMap<>();
    Map<String, int[]> contadores = new LinkedHashMap<>();
    for (Object[] nota : notas) {
      String disciplina = String.valueOf(nota[0]);
      int trimestre = ((Number) nota[1]).intValue();
      Object[] linha =
          porDisciplina.computeIfAbsent(
              disciplina,
              d -> {
                Object[] valores = new Object[13];
                Arrays.fill(valores, "-");
                valores[0] = d;
                return valores;
              });
      int[] contador = contadores.computeIfAbsent(disciplina, d -> new int[4]);
      int indiceTrimestre = Math.max(1, Math.min(3, trimestre));
      int posicaoNoTrimestre = contador[indiceTrimestre]++;
      if (posicaoNoTrimestre < 4) {
        linha[1 + ((indiceTrimestre - 1) * 4) + posicaoNoTrimestre] = nota[2];
      }
    }
    return new ArrayList<>(porDisciplina.values());
  }

  public static List<Object[]> alunosSecretariaLinhas() {
    String sql =
        "SELECT a.matricula, a.nome, t.descricao_turma, t.turno, a.situacao "
            + "FROM aluno a LEFT JOIN turma t ON t.id_turma = a.turma_id ORDER BY a.nome";
    return consultarLinhas(
        sql,
        0,
        rs ->
            new Object[] {
              rs.getString("matricula"),
              rs.getString("nome"),
              rs.getString("descricao_turma"),
              "Ensino Medio",
              rs.getString("turno"),
              rs.getString("situacao")
            });
  }

  public static List<Object[]> usuariosLinhas() {
    String sql =
        "SELECT u.id_usuario, u.tipo_usuario, u.ativo, u.cpf, a.nome AS aluno_nome, p.nome AS"
            + " professor_nome, f.nome AS funcionario_nome, pa.nome_mae AS responsavel_nome,"
            + " a.email AS aluno_email, f.email AS funcionario_email, pa.email_mae AS"
            + " responsavel_email FROM usuario u LEFT JOIN aluno a ON a.id_aluno = u.aluno_id LEFT"
            + " JOIN professor p ON p.id_professor = u.professor_id LEFT JOIN funcionario f ON"
            + " f.id_funcionario = u.funcionario_id LEFT JOIN pais_aluno pa ON pa.id_pais ="
            + " u.pai_id ORDER BY u.id_usuario";
    return consultarLinhas(
        sql,
        0,
        rs -> {
          String nome =
              primeiroNaoVazio(
                  rs.getString("aluno_nome"),
                  rs.getString("professor_nome"),
                  rs.getString("funcionario_nome"),
                  rs.getString("responsavel_nome"),
                  rs.getString("cpf"));
          String email =
              primeiroNaoVazio(
                  rs.getString("aluno_email"),
                  rs.getString("funcionario_email"),
                  rs.getString("responsavel_email"),
                  rs.getString("cpf") + "@teste.com");
          return new Object[] {
            rs.getInt("id_usuario"),
            nome,
            rs.getString("tipo_usuario"),
            email,
            rs.getBoolean("ativo") ? "Ativo" : "Inativo",
            "Editar",
            "Salvar",
            "Excluir"
          };
        });
  }

  public static List<Object[]> chamadaAlunosLinhas() {
    String sql =
        "SELECT matricula, nome FROM aluno WHERE turma_id = (SELECT turma_id FROM aluno WHERE"
            + " id_aluno = ?) ORDER BY nome";
    return consultarLinhas(
        sql,
        alunoIdAtual(),
        rs ->
            new Object[] {
              rs.getString("matricula"), rs.getString("nome"), false, false, false, false, ""
            });
  }

  public static List<Object[]> lancamentoNotasAlunosLinhas() {
    String sql =
        "SELECT matricula, nome FROM aluno WHERE turma_id = (SELECT turma_id FROM aluno WHERE"
            + " id_aluno = ?) ORDER BY nome";
    return consultarLinhas(
        sql,
        alunoIdAtual(),
        rs -> new Object[] {rs.getString("matricula"), rs.getString("nome"), ""});
  }

  public static List<Object[]> turmasProfessorAtualLinhas() {
    String sql =
        "SELECT t.descricao_turma, d.descricao, t.turno, s.id_sala, t.ativo, COUNT(a.id_aluno) AS"
            + " total_alunos FROM professor_disciplina pd INNER JOIN disciplina d ON"
            + " d.id_disciplina = pd.id_disciplina INNER JOIN turma_disciplina td ON"
            + " td.disciplina_id = d.id_disciplina INNER JOIN turma t ON t.id_turma = td.turma_id"
            + " LEFT JOIN sala s ON s.id_sala = t.sala_id LEFT JOIN aluno a ON a.turma_id ="
            + " t.id_turma WHERE pd.id_professor = ? AND pd.ativo = true GROUP BY t.id_turma,"
            + " d.id_disciplina ORDER BY t.descricao_turma, d.descricao";
    return consultarLinhas(
        sql,
        professorIdAtual(),
        rs ->
            new Object[] {
              rs.getString("descricao_turma"),
              "Ensino Medio",
              rs.getString("descricao"),
              rs.getString("turno"),
              rs.getInt("total_alunos"),
              "Conforme horario",
              "Sala " + rs.getInt("id_sala"),
              rs.getBoolean("ativo") ? "Ativa" : "Inativa"
            });
  }

  public static List<Object[]> disciplinasProfessorAtualLinhas() {
    String sql =
        "SELECT d.codigo, d.descricao, d.carga_horaria, d.ativo, GROUP_CONCAT(DISTINCT"
            + " t.descricao_turma) AS turmas, COUNT(DISTINCT a.id_aluno) AS total_alunos FROM"
            + " professor_disciplina pd INNER JOIN disciplina d ON d.id_disciplina ="
            + " pd.id_disciplina LEFT JOIN turma_disciplina td ON td.disciplina_id ="
            + " d.id_disciplina LEFT JOIN turma t ON t.id_turma = td.turma_id LEFT JOIN aluno a ON"
            + " a.turma_id = t.id_turma WHERE pd.id_professor = ? AND pd.ativo = true GROUP BY"
            + " d.id_disciplina ORDER BY d.descricao";
    return consultarLinhas(
        sql,
        professorIdAtual(),
        rs ->
            new Object[] {
              rs.getString("codigo"),
              rs.getString("descricao"),
              nvl(rs.getString("turmas"), "-"),
              "Ensino Medio",
              rs.getInt("carga_horaria") + "h",
              rs.getInt("total_alunos"),
              "Obrigatoria",
              rs.getBoolean("ativo") ? "Ativa" : "Inativa"
            });
  }

  public static int totalDisciplinasProfessorAtual() {
    return primeiroInt(
        "SELECT COUNT(*) FROM professor_disciplina WHERE id_professor = ? AND ativo = true",
        professorIdAtual(),
        0);
  }

  public static int totalTurmasProfessorAtual() {
    return primeiroInt(
        "SELECT COUNT(DISTINCT td.turma_id) FROM professor_disciplina pd "
            + "INNER JOIN turma_disciplina td ON td.disciplina_id = pd.id_disciplina "
            + "WHERE pd.id_professor = ? AND pd.ativo = true",
        professorIdAtual(),
        0);
  }

  public static int totalAlunosProfessorAtual() {
    return primeiroInt(
        "SELECT COUNT(DISTINCT a.id_aluno) FROM professor_disciplina pd "
            + "INNER JOIN turma_disciplina td ON td.disciplina_id = pd.id_disciplina "
            + "INNER JOIN aluno a ON a.turma_id = td.turma_id "
            + "WHERE pd.id_professor = ? AND pd.ativo = true",
        professorIdAtual(),
        0);
  }

  public static String primeiraDisciplinaProfessorAtual() {
    return primeiroTexto(
        "SELECT d.descricao FROM professor_disciplina pd "
            + "INNER JOIN disciplina d ON d.id_disciplina = pd.id_disciplina "
            + "WHERE pd.id_professor = ? AND pd.ativo = true ORDER BY d.descricao LIMIT 1",
        professorIdAtual(),
        "Disciplina");
  }

  public static void preencher(DefaultTableModel modelo, List<Object[]> linhas) {
    for (Object[] linha : linhas) {
      modelo.addRow(linha);
    }
  }

  public static void salvarNota(String matricula, String atividade, double nota)
      throws SQLException {
    int alunoId =
        primeiroInt("SELECT id_aluno FROM aluno WHERE matricula = ?", somenteDigitos(matricula), 0);
    int disciplinaId =
        primeiroInt(
            "SELECT id_disciplina FROM professor_disciplina WHERE id_professor = ? AND ativo = true"
                + " LIMIT 1",
            professorIdAtual(),
            1);
    int trimestreId =
        primeiroInt("SELECT id_trimestre FROM trimestre ORDER BY id_trimestre LIMIT 1", 0, 1);
    if (alunoId <= 0) {
      throw new SQLException("Aluno nao encontrado para a matricula " + matricula + ".");
    }

    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement existente =
            conn.prepareStatement(
                "SELECT notas_id FROM nota WHERE aluno_id = ? AND disciplina_id = ? AND"
                    + " trimestre_id = ? AND atividade = ?")) {
      existente.setInt(1, alunoId);
      existente.setInt(2, disciplinaId);
      existente.setInt(3, trimestreId);
      existente.setString(4, atividade);
      try (ResultSet rs = existente.executeQuery()) {
        if (rs.next()) {
          try (PreparedStatement update =
              conn.prepareStatement(
                  "UPDATE nota SET nota = ?, data_lancamento = ? WHERE notas_id = ?")) {
            update.setDouble(1, nota);
            update.setString(2, LocalDate.now().toString());
            update.setInt(3, rs.getInt("notas_id"));
            update.executeUpdate();
          }
          return;
        }
      }

      try (PreparedStatement insert =
          conn.prepareStatement(
              "INSERT INTO nota (disciplina_id, aluno_id, atividade, nota, data_lancamento,"
                  + " trimestre_id) VALUES (?, ?, ?, ?, ?, ?)")) {
        insert.setInt(1, disciplinaId);
        insert.setInt(2, alunoId);
        insert.setString(3, atividade);
        insert.setDouble(4, nota);
        insert.setString(5, LocalDate.now().toString());
        insert.setInt(6, trimestreId);
        insert.executeUpdate();
      }
    }
  }

  public static void salvarPresenca(
      String matricula,
      boolean presente,
      boolean faltaJustificada,
      boolean faltaAbonada,
      String motivo)
      throws SQLException {
    int alunoId =
        primeiroInt("SELECT id_aluno FROM aluno WHERE matricula = ?", somenteDigitos(matricula), 0);
    int disciplinaId =
        primeiroInt(
            "SELECT id_disciplina FROM professor_disciplina WHERE id_professor = ? AND ativo = true"
                + " LIMIT 1",
            professorIdAtual(),
            1);
    if (alunoId <= 0) {
      throw new SQLException("Aluno nao encontrado para a matricula " + matricula + ".");
    }
    String data = LocalDate.now().toString();
    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement existente =
            conn.prepareStatement(
                "SELECT id_presenca FROM presenca WHERE aluno_id = ? AND disciplina_id = ? AND"
                    + " data_presenca = ?")) {
      existente.setInt(1, alunoId);
      existente.setInt(2, disciplinaId);
      existente.setString(3, data);
      try (ResultSet rs = existente.executeQuery()) {
        if (rs.next()) {
          try (PreparedStatement update =
              conn.prepareStatement(
                  "UPDATE presenca SET presente = ?, falta_abonada = ?, falta_justificada = ?,"
                      + " motivo_abonada = ? WHERE id_presenca = ?")) {
            update.setBoolean(1, presente);
            update.setBoolean(2, faltaAbonada);
            update.setBoolean(3, faltaJustificada);
            update.setString(4, motivo);
            update.setInt(5, rs.getInt("id_presenca"));
            update.executeUpdate();
          }
          return;
        }
      }

      try (PreparedStatement insert =
          conn.prepareStatement(
              "INSERT INTO presenca (aluno_id, disciplina_id, data_presenca, presente,"
                  + " falta_abonada, falta_justificada, motivo_abonada) VALUES (?, ?, ?, ?, ?, ?,"
                  + " ?)")) {
        insert.setInt(1, alunoId);
        insert.setInt(2, disciplinaId);
        insert.setString(3, data);
        insert.setBoolean(4, presente);
        insert.setBoolean(5, faltaAbonada);
        insert.setBoolean(6, faltaJustificada);
        insert.setString(7, motivo);
        insert.executeUpdate();
      }
    }
  }

  private static String nomeAluno(int idAluno) {
    return primeiroTexto("SELECT nome FROM aluno WHERE id_aluno = ?", idAluno, "Aluno");
  }

  private static String nomeProfessor(int idProfessor) {
    return primeiroTexto(
        "SELECT nome FROM professor WHERE id_professor = ?", idProfessor, "Professor");
  }

  private static String nomeFuncionario(int idFuncionario) {
    return primeiroTexto(
        "SELECT nome FROM funcionario WHERE id_funcionario = ?", idFuncionario, "Funcionario");
  }

  private static String nomeResponsavel(int idPai) {
    return primeiroTexto(
        "SELECT COALESCE(nome_mae, nome_pai) FROM pais_aluno WHERE id_pais = ?",
        idPai,
        "Responsavel");
  }

  private static String nvl(String valor, String padrao) {
    return valor == null || valor.isBlank() ? padrao : valor;
  }

  private static String cidadeUf(String cidade, String estado) {
    if ((cidade == null || cidade.isBlank()) && (estado == null || estado.isBlank())) {
      return "-";
    }
    if (estado == null || estado.isBlank()) {
      return cidade;
    }
    if (cidade == null || cidade.isBlank()) {
      return estado;
    }
    return cidade + " - " + estado;
  }

  private static String formatarNota(double nota) {
    return String.format(java.util.Locale.US, "%.1f", nota).replace('.', ',');
  }

  private static String primeiroTexto(String sql, int parametro, String padrao) {
    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, parametro);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getString(1) : padrao;
      }
    } catch (SQLException e) {
      return padrao;
    }
  }

  private static int primeiroInt(String sql, int parametro, int padrao) {
    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      if (parametro > 0) {
        stmt.setInt(1, parametro);
      }
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getInt(1) : padrao;
      }
    } catch (SQLException e) {
      return padrao;
    }
  }

  private static double primeiroDouble(String sql, int parametro, double padrao) {
    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, parametro);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getDouble(1) : padrao;
      }
    } catch (SQLException e) {
      return padrao;
    }
  }

  private static int primeiroInt(String sql, String parametro, int padrao) {
    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, parametro);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getInt(1) : padrao;
      }
    } catch (SQLException e) {
      return padrao;
    }
  }

  private static String somenteDigitos(String valor) {
    return valor == null ? "" : valor.replaceAll("\\D", "");
  }

  private static List<Object[]> consultarLinhas(String sql, int parametro, MapeadorLinha mapeador) {
    List<Object[]> linhas = new ArrayList<>();
    try (Connection conn = ConnectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      if (parametro > 0) {
        stmt.setInt(1, parametro);
      }
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          linhas.add(mapeador.mapear(rs));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return linhas;
  }

  private static String primeiroNaoVazio(String... valores) {
    for (String valor : valores) {
      if (valor != null && !valor.isBlank()) {
        return valor;
      }
    }
    return "";
  }

  @FunctionalInterface
  private interface MapeadorLinha {
    Object[] mapear(ResultSet rs) throws SQLException;
  }
}
