// Igor

package controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.PresencaDAO;
import database.ConnectionFactory;
import model.Presenca;

public class PresencaController {

  @FunctionalInterface
  private interface AcaoTransacional<T> {
    T executar(Connection conn) throws SQLException;
  }

  private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {
    try (Connection conn = ConnectionFactory.getConnection()) {
      conn.setAutoCommit(false);

      try {
        T resultado = acao.executar(conn);
        conn.commit();
        return resultado;

      } catch (IllegalArgumentException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          e.addSuppressed(rollbackEx);
        }
        throw e;

      } catch (SQLException | RuntimeException e) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          e.addSuppressed(rollbackEx);
        }
        throw new RuntimeException(mensagemOperacao, e);
      }

    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
    }
  }

  private void validarPresencaNaoNula(Presenca presenca) {
    if (presenca == null) {
      throw new IllegalArgumentException("Presença não pode ser nula.");
    }
  }

  private void normalizarPresenca(Presenca presenca) {
    presenca.setMotivoAbonada(tratarTexto(presenca.getMotivoAbonada()));
  }

  private void validarParaCadastro(Presenca presenca) {
    validarCamposBase(presenca);
  }

  private void validarParaAtualizacao(Presenca presencaAtualizada, Presenca presencaBanco) {
    validarCamposBase(presencaAtualizada);
    validarChaveImutavel(presencaAtualizada, presencaBanco);
  }

  private void validarCamposBase(Presenca presenca) {
    if (presenca.getAlunoId() <= 0) {
      throw new IllegalArgumentException("ID do aluno é obrigatório.");
    }

    if (presenca.getDisciplinaId() <= 0) {
      throw new IllegalArgumentException("ID da disciplina é obrigatório.");
    }

    if (presenca.getData() == null) {
      throw new IllegalArgumentException("Data da presença é obrigatória.");
    }

    if (presenca.getData().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Data da presença não pode ser futura.");
    }

    if (presenca.isPresente()) {
      if (presenca.isFaltaAbonada() || presenca.isFaltaJustificada()) {
        throw new IllegalArgumentException(
            "Presença marcada como presente não pode ter falta associada.");
      }

      if (presenca.getMotivoAbonada() != null) {
        throw new IllegalArgumentException(
            "Motivo de abono não deve ser informado para aluno presente.");
      }

      return;
    }

    if (presenca.isFaltaAbonada() && presenca.isFaltaJustificada()) {
      throw new IllegalArgumentException(
          "A falta não pode ser abonada e justificada ao mesmo tempo.");
    }

    if (presenca.isFaltaAbonada()
        && (presenca.getMotivoAbonada() == null || presenca.getMotivoAbonada().isBlank())) {
      throw new IllegalArgumentException("Motivo da falta abonada é obrigatório.");
    }

    if (!presenca.isFaltaAbonada() && presenca.getMotivoAbonada() != null) {
      throw new IllegalArgumentException(
          "Motivo de abono só deve ser informado para falta abonada.");
    }
  }

  private void validarChaveImutavel(Presenca presencaAtualizada, Presenca presencaBanco) {
    if (presencaAtualizada.getAlunoId() != presencaBanco.getAlunoId()
        || presencaAtualizada.getDisciplinaId() != presencaBanco.getDisciplinaId()
        || !presencaAtualizada.getData().equals(presencaBanco.getData())) {
      throw new IllegalArgumentException(
          "Aluno, disciplina e data não podem ser alterados após o cadastro.");
    }
  }

  private Presenca mesclarDadosPermitidos(Presenca presencaBanco, Presenca presencaAtualizada) {
    presencaBanco.setPresente(presencaAtualizada.isPresente());
    presencaBanco.setFaltaAbonada(presencaAtualizada.isFaltaAbonada());
    presencaBanco.setFaltaJustificada(presencaAtualizada.isFaltaJustificada());
    presencaBanco.setMotivoAbonada(presencaAtualizada.getMotivoAbonada());
    return presencaBanco;
  }

  private void validarExistenciasRelacionamentos(
      AlunoDAO alunoDAO, DisciplinaDAO disciplinaDAO, Presenca presenca) throws SQLException {
    if (alunoDAO.buscarPorId(presenca.getAlunoId()) == null) {
      throw new IllegalArgumentException("Aluno informado não existe.");
    }

    if (disciplinaDAO.buscarPorId(presenca.getDisciplinaId()) == null) {
      throw new IllegalArgumentException("Disciplina informada não existe.");
    }
  }

  private String tratarTexto(String valor) {
    if (valor == null) {
      return null;
    }

    String valorTratado = valor.trim();
    return valorTratado.isEmpty() ? null : valorTratado;
  }

  public void salvarPresenca(Presenca presenca) {
    validarPresencaNaoNula(presenca);
    normalizarPresenca(presenca);
    validarParaCadastro(presenca);

    executarEmTransacao(
        conn -> {
          PresencaDAO presencaDAO = new PresencaDAO(conn);
          validarExistenciasRelacionamentos(new AlunoDAO(conn), new DisciplinaDAO(conn), presenca);

          Presenca presencaExistente =
              presencaDAO.buscarPorAlunoDisciplinaData(
                  presenca.getAlunoId(), presenca.getDisciplinaId(), presenca.getData());

          if (presencaExistente != null) {
            throw new IllegalArgumentException(
                "Já existe presença cadastrada para este aluno, disciplina e data.");
          }

          presencaDAO.inserir(presenca);
          return null;
        },
        "Erro ao salvar presença.");
  }

  public void atualizarPresenca(Presenca presencaAtualizada) {
    validarPresencaNaoNula(presencaAtualizada);

    if (presencaAtualizada.getIdPresenca() <= 0) {
      throw new IllegalArgumentException("ID da presença inválido.");
    }

    normalizarPresenca(presencaAtualizada);

    executarEmTransacao(
        conn -> {
          PresencaDAO presencaDAO = new PresencaDAO(conn);

          Presenca presencaBanco = presencaDAO.buscarPorId(presencaAtualizada.getIdPresenca());
          if (presencaBanco == null) {
            throw new IllegalArgumentException("Presença não encontrada.");
          }

          validarParaAtualizacao(presencaAtualizada, presencaBanco);
          validarExistenciasRelacionamentos(
              new AlunoDAO(conn), new DisciplinaDAO(conn), presencaAtualizada);

          Presenca presencaParaSalvar = mesclarDadosPermitidos(presencaBanco, presencaAtualizada);
          presencaDAO.atualizar(presencaParaSalvar);
          return null;
        },
        "Erro ao atualizar presença.");
  }

  public boolean excluirPresenca(int idPresenca) {
    if (idPresenca <= 0) {
      throw new IllegalArgumentException("ID da presença inválido.");
    }

    return executarEmTransacao(
        conn -> {
          PresencaDAO presencaDAO = new PresencaDAO(conn);

          Presenca presencaExistente = presencaDAO.buscarPorId(idPresenca);
          if (presencaExistente == null) {
            throw new IllegalArgumentException("Presença não encontrada.");
          }

          return presencaDAO.excluir(idPresenca);
        },
        "Erro ao excluir presença.");
  }

  public Presenca buscarPresencaPorId(int idPresenca) {
    if (idPresenca <= 0) {
      throw new IllegalArgumentException("ID da presença inválido.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      PresencaDAO presencaDAO = new PresencaDAO(conn);
      return presencaDAO.buscarPorId(idPresenca);
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar presença por ID.", e);
    }
  }

  public List<Presenca> listarPresencas() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      PresencaDAO presencaDAO = new PresencaDAO(conn);
      return presencaDAO.listar();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar presenças.", e);
    }
  }

  public List<Presenca> listarPresencasPorAluno(int alunoId) {
    if (alunoId <= 0) {
      throw new IllegalArgumentException("ID do aluno inválido.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      PresencaDAO presencaDAO = new PresencaDAO(conn);
      return presencaDAO.listarPorAluno(alunoId);
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar presenças por aluno.", e);
    }
  }

  public List<Presenca> listarPresencasPorDisciplina(int disciplinaId) {
    if (disciplinaId <= 0) {
      throw new IllegalArgumentException("ID da disciplina inválido.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      PresencaDAO presencaDAO = new PresencaDAO(conn);
      return presencaDAO.listarPorDisciplina(disciplinaId);
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar presenças por disciplina.", e);
    }
  }
}
