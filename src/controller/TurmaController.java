//Igor

package controller;

import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Turma;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TurmaController {

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

    private void validarTurmaNaoNula(Turma turma) {
        if (turma == null) {
            throw new IllegalArgumentException("Turma não pode ser nula.");
        }
    }

    private void normalizarTurma(Turma turma) {
        if (turma.getDescricaoTurma() != null) {
            turma.setDescricaoTurma(tratarTexto(turma.getDescricaoTurma()));
        }
    }

    private void validarParaCadastro(Turma turma) {
        validarCamposBase(turma);
    }

    private void validarParaAtualizacao(Turma turmaAtualizada, Turma turmaBanco) {
        validarCamposBase(turmaAtualizada);
    }

    private void validarCamposBase(Turma turma) {
        if (turma.getSalaId() <= 0) {
            throw new IllegalArgumentException("ID da sala é obrigatório.");
        }

        if (turma.getDescricaoTurma() == null || turma.getDescricaoTurma().isBlank()) {
            throw new IllegalArgumentException("Descrição da turma é obrigatória.");
        }

        if (turma.getTurno() == null) {
            throw new IllegalArgumentException("Turno da turma é obrigatório.");
        }
    }

    private Turma mesclarDadosPermitidos(Turma turmaBanco, Turma turmaAtualizada) {
        turmaBanco.setSalaId(turmaAtualizada.getSalaId());
        turmaBanco.setDescricaoTurma(turmaAtualizada.getDescricaoTurma());
        turmaBanco.setTurno(turmaAtualizada.getTurno());
        return turmaBanco;
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    public void salvarTurma(Turma turma) {
        validarTurmaNaoNula(turma);
        normalizarTurma(turma);
        validarParaCadastro(turma);

        executarEmTransacao(conn -> {
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            turmaDAO.inserir(turma);
            return null;
        }, "Erro ao salvar turma.");
    }

    public void atualizarTurma(Turma turmaAtualizada) {
        validarTurmaNaoNula(turmaAtualizada);

        if (turmaAtualizada.getIdTurma() <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        normalizarTurma(turmaAtualizada);

        executarEmTransacao(conn -> {
            TurmaDAO turmaDAO = new TurmaDAO(conn);

            Turma turmaBanco = turmaDAO.buscarPorId(turmaAtualizada.getIdTurma());
            if (turmaBanco == null) {
                throw new IllegalArgumentException("Turma não encontrada.");
            }

            validarParaAtualizacao(turmaAtualizada, turmaBanco);

            Turma turmaParaSalvar = mesclarDadosPermitidos(turmaBanco, turmaAtualizada);
            turmaDAO.atualizar(turmaParaSalvar);
            return null;
        }, "Erro ao atualizar turma.");
    }

    public boolean excluirTurma(int idTurma) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        return executarEmTransacao(conn -> {
            TurmaDAO turmaDAO = new TurmaDAO(conn);

            Turma turmaExistente = turmaDAO.buscarPorId(idTurma);
            if (turmaExistente == null) {
                throw new IllegalArgumentException("Turma não encontrada.");
            }

            return turmaDAO.excluir(idTurma);
        }, "Erro ao excluir turma.");
    }

    public Turma buscarTurmaPorId(int idTurma) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            return turmaDAO.buscarPorId(idTurma);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar turma por ID.", e);
        }
    }

    public List<Turma> listarTurmas() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            return turmaDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar turmas.", e);
        }
    }
}