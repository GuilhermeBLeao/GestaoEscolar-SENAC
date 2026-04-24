//Luiz - Igor editou

package controller;

import dao.DisciplinaDAO;
import database.ConnectionFactory;
import model.Disciplina;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DisciplinaController {

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

    private void validarDisciplinaNaoNula(Disciplina disciplina) {
        if (disciplina == null) {
            throw new IllegalArgumentException("Disciplina não pode ser nula.");
        }
    }

    private void normalizarDisciplina(Disciplina disciplina) {
        if (disciplina.getDescricao() != null) {
            disciplina.setDescricao(tratarTexto(disciplina.getDescricao()));
        }
    }

    private void validarParaCadastro(Disciplina disciplina) {
        validarCamposBase(disciplina);
    }

    private void validarParaAtualizacao(Disciplina disciplinaAtualizada, Disciplina disciplinaBanco) {
        validarCamposBase(disciplinaAtualizada);
        validarCodigoImutavel(disciplinaAtualizada, disciplinaBanco);
    }

    private void validarCamposBase(Disciplina disciplina) {
        if (disciplina.getDescricao() == null || disciplina.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição da disciplina é obrigatória.");
        }

        if (disciplina.getDescricao().length() < 3 || disciplina.getDescricao().length() > 100) {
            throw new IllegalArgumentException("Descrição da disciplina deve ter entre 3 e 100 caracteres.");
        }

        if (disciplina.getCargaHoraria() <= 0) {
            throw new IllegalArgumentException("Carga horária da disciplina é obrigatória.");
        }

        if (disciplina.getCodigo() <= 0) {
            throw new IllegalArgumentException("Código da disciplina é obrigatório.");
        }
    }

    private void validarCodigoImutavel(Disciplina disciplinaAtualizada, Disciplina disciplinaBanco) {
        if (disciplinaAtualizada.getCodigo() != disciplinaBanco.getCodigo()) {
            throw new IllegalArgumentException("Código da disciplina não pode ser alterado após o cadastro.");
        }
    }

    private Disciplina mesclarDadosPermitidos(Disciplina disciplinaBanco, Disciplina disciplinaAtualizada) {
        disciplinaBanco.setDescricao(disciplinaAtualizada.getDescricao());
        disciplinaBanco.setCargaHoraria(disciplinaAtualizada.getCargaHoraria());
        disciplinaBanco.setAtivo(disciplinaAtualizada.isAtivo());
        return disciplinaBanco;
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    public void salvarDisciplina(Disciplina disciplina) {
        validarDisciplinaNaoNula(disciplina);
        normalizarDisciplina(disciplina);
        validarParaCadastro(disciplina);

        executarEmTransacao(conn -> {
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

            if (disciplinaDAO.existeCodigo(disciplina.getCodigo())) {
                throw new IllegalArgumentException("Já existe disciplina cadastrada com este código.");
            }

            disciplinaDAO.inserir(disciplina);
            return null;
        }, "Erro ao salvar disciplina.");
    }

    public void atualizarDisciplina(Disciplina disciplinaAtualizada) {
        validarDisciplinaNaoNula(disciplinaAtualizada);

        if (disciplinaAtualizada.getIdDisciplina() <= 0) {
            throw new IllegalArgumentException("ID da disciplina inválido.");
        }

        normalizarDisciplina(disciplinaAtualizada);

        executarEmTransacao(conn -> {
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

            Disciplina disciplinaBanco = disciplinaDAO.buscarPorId(disciplinaAtualizada.getIdDisciplina());
            if (disciplinaBanco == null) {
                throw new IllegalArgumentException("Disciplina não encontrada.");
            }

            validarParaAtualizacao(disciplinaAtualizada, disciplinaBanco);

            Disciplina disciplinaParaSalvar = mesclarDadosPermitidos(disciplinaBanco, disciplinaAtualizada);
            disciplinaDAO.atualizar(disciplinaParaSalvar);
            return null;
        }, "Erro ao atualizar disciplina.");
    }

    public boolean excluirDisciplina(int idDisciplina) {
        if (idDisciplina <= 0) {
            throw new IllegalArgumentException("ID da disciplina inválido.");
        }

        return executarEmTransacao(conn -> {
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);

            Disciplina disciplinaExistente = disciplinaDAO.buscarPorId(idDisciplina);
            if (disciplinaExistente == null) {
                throw new IllegalArgumentException("Disciplina não encontrada.");
            }

            return disciplinaDAO.excluir(idDisciplina);
        }, "Erro ao excluir disciplina.");
    }

    public Disciplina buscarDisciplinaPorId(int idDisciplina) {
        if (idDisciplina <= 0) {
            throw new IllegalArgumentException("ID da disciplina inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
            return disciplinaDAO.buscarPorId(idDisciplina);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar disciplina por ID.", e);
        }
    }

    public List<Disciplina> listarDisciplinas() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
            return disciplinaDAO.listar();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar disciplinas.", e);
        }
    }
}