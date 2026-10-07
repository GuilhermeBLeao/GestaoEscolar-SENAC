package controller;

import dao.OcorrenciaDAO;
import database.ConnectionFactory;
import model.Ocorrencia;
import variaveisEnum.TipoOcorrencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OcorrenciaController {
    public Ocorrencia prepararOcorrencia(int funcionarioId, int alunoId) {
        if (funcionarioId <= 0) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório.");
        }

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setFuncionarioId(funcionarioId);

        if (alunoId > 0) {
            ocorrencia.setAlunoId(alunoId);
        }

        ocorrencia.setDataOcorrencia(LocalDate.now());
        ocorrencia.setTipoOcorrencia(TipoOcorrencia.AVISO);
        ocorrencia.setAtendenteNome("Secretaria");

        return ocorrencia;
    }

    public void salvarOcorrencia(Ocorrencia ocorrencia) {
        validarOcorrencia(ocorrencia);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO(conn);
                ocorrenciaDAO.inserir(ocorrencia);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar ocorrência.", e);
        }
    }

    public void atualizarOcorrencia(Ocorrencia ocorrencia) {
        validarOcorrencia(ocorrencia);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO(conn);
                ocorrenciaDAO.atualizar(ocorrencia);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar ocorrência.", e);
        }
    }

    public Ocorrencia buscarOcorrencia(int idOcorrencia) {
        if (idOcorrencia <= 0) {
            throw new IllegalArgumentException("ID da ocorrência é obrigatório.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO(conn);
            return ocorrenciaDAO.buscarPorId(idOcorrencia);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ocorrência.", e);
        }
    }

    public List<Ocorrencia> listarOcorrencias() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO(conn);
            return ocorrenciaDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ocorrências.", e);
        }
    }

    public List<Ocorrencia> listarOcorrenciasPorFuncionario(int funcionarioId) {
        if (funcionarioId <= 0) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO(conn);
            return ocorrenciaDAO.listarPorFuncionario(funcionarioId);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ocorrências do funcionário.", e);
        }
    }

    private void validarOcorrencia(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            throw new IllegalArgumentException("Ocorrência não pode ser nula.");
        }

        if (ocorrencia.getFuncionarioId() <= 0) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório.");
        }

        if (ocorrencia.getDataOcorrencia() == null) {
            throw new IllegalArgumentException("Data da ocorrência é obrigatória.");
        }

        if (ocorrencia.getDataOcorrencia().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da ocorrência não pode ser futura.");
        }

        if (ocorrencia.getTipoOcorrencia() == null) {
            throw new IllegalArgumentException("Tipo de ocorrência é obrigatório.");
        }

        if (ocorrencia.getDescricao() == null || ocorrencia.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da ocorrência é obrigatória.");
        }

        if (ocorrencia.getAtendenteNome() == null || ocorrencia.getAtendenteNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do atendente é obrigatório.");
        }
    }
}