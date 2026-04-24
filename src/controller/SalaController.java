//Guilherme

package controller;

import dao.SalaDAO;
import database.ConnectionFactory;
import model.Sala;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SalaController {

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

    private void validarSalaNaoNula(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("Sala não pode ser nula.");
        }
    }

    private void validarParaCadastro(Sala sala) {
        validarCamposBase(sala);
    }

    private void validarParaAtualizacao(Sala salaAtualizada, Sala salaBanco) {
        validarCamposBase(salaAtualizada);
    }

    private void validarCamposBase(Sala sala) {
        if (sala.getCapacidade() <= 0) {
            throw new IllegalArgumentException("Capacidade da sala deve ser maior que zero.");
        }
    }

    private Sala mesclarDadosPermitidos(Sala salaBanco, Sala salaAtualizada) {
        salaBanco.setCapacidade(salaAtualizada.getCapacidade());
        return salaBanco;
    }

    public void salvarSala(Sala sala) {
        validarSalaNaoNula(sala);
        validarParaCadastro(sala);

        executarEmTransacao(conn -> {
            SalaDAO salaDAO = new SalaDAO(conn);
            salaDAO.inserir(sala);
            return null;
        }, "Erro ao salvar sala.");
    }

    public void atualizarSala(Sala salaAtualizada) {
        validarSalaNaoNula(salaAtualizada);

        if (salaAtualizada.getIdSala() <= 0) {
            throw new IllegalArgumentException("ID da sala inválido.");
        }

        executarEmTransacao(conn -> {
            SalaDAO salaDAO = new SalaDAO(conn);

            Sala salaBanco = salaDAO.buscarPorId(salaAtualizada.getIdSala());
            if (salaBanco == null) {
                throw new IllegalArgumentException("Sala não encontrada.");
            }

            validarParaAtualizacao(salaAtualizada, salaBanco);

            Sala salaParaSalvar = mesclarDadosPermitidos(salaBanco, salaAtualizada);
            salaDAO.atualizar(salaParaSalvar);
            return null;
        }, "Erro ao atualizar sala.");
    }

    public boolean excluirSala(int idSala) {
        if (idSala <= 0) {
            throw new IllegalArgumentException("ID da sala inválido.");
        }

        return executarEmTransacao(conn -> {
            SalaDAO salaDAO = new SalaDAO(conn);

            Sala salaExistente = salaDAO.buscarPorId(idSala);
            if (salaExistente == null) {
                throw new IllegalArgumentException("Sala não encontrada.");
            }

            return salaDAO.excluir(idSala);
        }, "Erro ao excluir sala.");
    }

    public Sala buscarSalaPorId(int idSala) {
        if (idSala <= 0) {
            throw new IllegalArgumentException("ID da sala inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            SalaDAO salaDAO = new SalaDAO(conn);
            return salaDAO.buscarPorId(idSala);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sala por ID.", e);
        }
    }

    public List<Sala> listarSalas() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            SalaDAO salaDAO = new SalaDAO(conn);
            return salaDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar salas.", e);
        }
    }
}