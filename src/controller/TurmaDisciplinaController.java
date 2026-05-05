//luiz

package controller;

import dao.TurmaDisciplinaDAO;
import database.ConnectionFactory;
import model.TurmaDisciplina;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TurmaDisciplinaController {

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

    private void validarNaoNulo(TurmaDisciplina td) {
        if (td == null) {
            throw new IllegalArgumentException("TurmaDisciplina não pode ser nula.");
        }
    }

    private void validarCamposBase(TurmaDisciplina td) {
        if (td.getProfessores_id() <= 0) {
            throw new IllegalArgumentException("ID do professor é obrigatório.");
        }

        if (td.getTurma_id() <= 0) {
            throw new IllegalArgumentException("ID da turma é obrigatório.");
        }

        if (td.getDisciplina_id() <= 0) {
            throw new IllegalArgumentException("ID da disciplina é obrigatório.");
        }

        if (td.getHorarioInicio() == null) {
            throw new IllegalArgumentException("Horário de início é obrigatório.");
        }

        if (td.getHorarioTermino() == null) {
            throw new IllegalArgumentException("Horário de término é obrigatório.");
        }

        if (!td.getHorarioTermino().isAfter(td.getHorarioInicio())) {
            throw new IllegalArgumentException("Horário de término deve ser posterior ao horário de início.");
        }
    }

    private TurmaDisciplina mesclarDadosPermitidos(TurmaDisciplina tdBanco, TurmaDisciplina tdAtualizado) {
        tdBanco.setProfessores_id(tdAtualizado.getProfessores_id());
        tdBanco.setTurma_id(tdAtualizado.getTurma_id());
        tdBanco.setDisciplina_id(tdAtualizado.getDisciplina_id());
        tdBanco.setHorarioInicio(tdAtualizado.getHorarioInicio());
        tdBanco.setHorarioTermino(tdAtualizado.getHorarioTermino());
        return tdBanco;
    }

    public void salvarTurmaDisciplina(TurmaDisciplina td) {
        validarNaoNulo(td);
        validarCamposBase(td);

        executarEmTransacao(conn -> {
            TurmaDisciplinaDAO dao = new TurmaDisciplinaDAO(conn);
            dao.inserir(td);
            return null;
        }, "Erro ao salvar turma-disciplina.");
    }

    public void atualizarTurmaDisciplina(TurmaDisciplina tdAtualizado) {
        validarNaoNulo(tdAtualizado);

        if (tdAtualizado.getId_turmad() <= 0) {
            throw new IllegalArgumentException("ID da turma-disciplina inválido.");
        }

        validarCamposBase(tdAtualizado);

        executarEmTransacao(conn -> {
            TurmaDisciplinaDAO dao = new TurmaDisciplinaDAO(conn);

            TurmaDisciplina tdBanco = dao.buscarPorId(tdAtualizado.getId_turmad());
            if (tdBanco == null) {
                throw new IllegalArgumentException("Turma-disciplina não encontrada.");
            }

            TurmaDisciplina tdParaSalvar = mesclarDadosPermitidos(tdBanco, tdAtualizado);
            dao.atualizar(tdParaSalvar);
            return null;
        }, "Erro ao atualizar turma-disciplina.");
    }

    public boolean excluirTurmaDisciplina(int idTurmad) {
        if (idTurmad <= 0) {
            throw new IllegalArgumentException("ID da turma-disciplina inválido.");
        }

        return executarEmTransacao(conn -> {
            TurmaDisciplinaDAO dao = new TurmaDisciplinaDAO(conn);

            TurmaDisciplina tdExistente = dao.buscarPorId(idTurmad);
            if (tdExistente == null) {
                throw new IllegalArgumentException("Turma-disciplina não encontrada.");
            }

            return dao.excluir(idTurmad);
        }, "Erro ao excluir turma-disciplina.");
    }

    public TurmaDisciplina buscarTurmaDisciplinaPorId(int idTurmad) {
        if (idTurmad <= 0) {
            throw new IllegalArgumentException("ID da turma-disciplina inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            TurmaDisciplinaDAO dao = new TurmaDisciplinaDAO(conn);
            return dao.buscarPorId(idTurmad);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar turma-disciplina por ID.", e);
        }
    }

    public List<TurmaDisciplina> listarTurmaDisciplinas() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            TurmaDisciplinaDAO dao = new TurmaDisciplinaDAO(conn);
            return dao.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar turmas-disciplinas.", e);
        }
    }

    public List<TurmaDisciplina> listarPorTurma(int turmaId) {
        if (turmaId <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            TurmaDisciplinaDAO dao = new TurmaDisciplinaDAO(conn);
            return dao.listarPorTurma(turmaId);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar turmas-disciplinas por turma.", e);
        }
    }
}
