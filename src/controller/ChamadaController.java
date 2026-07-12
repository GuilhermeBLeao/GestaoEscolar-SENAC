// Luiz, Igor e Guilherme

package controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.PresencaDAO;
import dao.ProfessorDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Chamada;
import model.ChamadaItem;
import model.Presenca;

public class ChamadaController {

  public Chamada prepararChamada(int professorId, int turmaId, int disciplinaId) {
    if (professorId <= 0) throw new IllegalArgumentException("ID do professor é inválido.");
    if (turmaId <= 0) throw new IllegalArgumentException("ID da turma é inválido.");
    if (disciplinaId <= 0) throw new IllegalArgumentException("ID da disciplina é inválido.");

    try (Connection conn = ConnectionFactory.getConnection()) {
      DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
      validarExistenciasRelacionamentos(
          new ProfessorDAO(conn),
          new TurmaDAO(conn),
          disciplinaDAO,
          professorId,
          turmaId,
          disciplinaId);
      validarDisciplinaPertenceTurma(disciplinaDAO, disciplinaId, turmaId);

      List<Aluno> alunos = new AlunoDAO(conn).listarPorTurma(turmaId);

      if (alunos.isEmpty())
        throw new IllegalArgumentException("Não existem alunos cadastrados nesta turma.");

      List<ChamadaItem> itens = new ArrayList<>();
      for (Aluno aluno : alunos) {
        itens.add(new ChamadaItem(aluno.getIdAluno(), aluno.getNome()));
      }

      Chamada chamada = new Chamada();
      chamada.setProfessorId(professorId);
      chamada.setTurmaId(turmaId);
      chamada.setDisciplinaId(disciplinaId);
      chamada.setData(LocalDate.now());
      chamada.setItens(itens);

      return chamada;

    } catch (SQLException e) {
      throw new RuntimeException("Erro ao preparar a chamada.", e);
    }
  }

  // Responsável por persistir a chamada no banco.
  public void salvarChamada(Chamada chamada) {
    validarChamada(chamada);

    try (Connection conn = ConnectionFactory.getConnection()) {
      conn.setAutoCommit(false);

      try {
        PresencaDAO presencaDAO = new PresencaDAO(conn);

        validarExistenciasRelacionamentos(
            new ProfessorDAO(conn),
            new TurmaDAO(conn),
            new DisciplinaDAO(conn),
            chamada.getProfessorId(),
            chamada.getTurmaId(),
            chamada.getDisciplinaId());

        DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
        validarDisciplinaPertenceTurma(
            disciplinaDAO, chamada.getDisciplinaId(), chamada.getTurmaId());

        for (ChamadaItem item : chamada.getItens()) {
          AlunoDAO alunoDAO = new AlunoDAO(conn);
          if (!alunoDAO.alunoPertenceTurma(item.getAlunoId(), chamada.getTurmaId())) {
            throw new IllegalArgumentException(
                "Aluno ID "
                    + item.getAlunoId()
                    + " não pertence à turma informada ou não está ativo.");
          }

          Presenca existente =
              presencaDAO.buscarPorAlunoDisciplinaData(
                  item.getAlunoId(), chamada.getDisciplinaId(), chamada.getData());

          if (existente != null) {
            throw new IllegalArgumentException(
                "Já existe chamada para o aluno ID: " + item.getAlunoId());
          }

          Presenca presenca = new Presenca();
          presenca.setAlunoId(item.getAlunoId());
          presenca.setDisciplinaId(chamada.getDisciplinaId());
          presenca.setData(chamada.getData());
          presenca.setPresente(item.isPresente());
          presencaDAO.inserir(presenca);
        }

        conn.commit();

      } catch (Exception e) {
        conn.rollback();
        throw e;
      }

    } catch (SQLException e) {
      throw new RuntimeException("Erro ao salvar chamada.", e);
    }
  }

  private void validarExistenciasRelacionamentos(
      ProfessorDAO professorDAO,
      TurmaDAO turmaDAO,
      DisciplinaDAO disciplinaDAO,
      int professorId,
      int turmaId,
      int disciplinaId)
      throws SQLException {
    if (professorDAO.buscarPorId(professorId) == null)
      throw new IllegalArgumentException("Professor informado não existe.");
    if (turmaDAO.buscarPorId(turmaId) == null)
      throw new IllegalArgumentException("Turma informada não existe.");
    if (disciplinaDAO.buscarPorId(disciplinaId) == null)
      throw new IllegalArgumentException("Disciplina informada não existe.");
  }

  private void validarDisciplinaPertenceTurma(
      DisciplinaDAO disciplinaDAO, int disciplinaId, int turmaId) throws SQLException {
    if (!disciplinaDAO.disciplinaPertenceTurma(disciplinaId, turmaId)) {
      throw new IllegalArgumentException("Disciplina não pertence à turma informada.");
    }
  }

  private void validarChamada(Chamada chamada) {
    if (chamada == null) throw new IllegalArgumentException("Chamada não pode ser nula.");
    if (chamada.getProfessorId() <= 0)
      throw new IllegalArgumentException("ID do professor é obrigatório.");
    if (chamada.getTurmaId() <= 0) throw new IllegalArgumentException("ID da turma é obrigatório.");
    if (chamada.getDisciplinaId() <= 0)
      throw new IllegalArgumentException("ID da disciplina é obrigatório.");
    if (chamada.getData() == null)
      throw new IllegalArgumentException("Data da chamada é obrigatória.");
    if (chamada.getData().isAfter(LocalDate.now()))
      throw new IllegalArgumentException("Data da chamada não pode ser futura.");
    if (chamada.getItens() == null || chamada.getItens().isEmpty())
      throw new IllegalArgumentException("A chamada precisa conter ao menos 1 aluno.");
  }
}
