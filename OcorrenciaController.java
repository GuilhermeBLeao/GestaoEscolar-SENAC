//igor

package controller;

import dao.OcorrenciaDAO;
import database.ConnectionFactory;
import model.Ocorrencia;
import variaveisEnum.TipoOcorrencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// Controlador responsável por criar, validar e persistir ocorrências escolares.
public class OcorrenciaController {

    // Prepara uma nova ocorrência preenchendo valores padrão.
    public Ocorrencia prepararOcorrencia(int funcionarioId, int alunoId) {
        if (funcionarioId <= 0) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório.");
        }

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setFuncionarioId(funcionarioId);

        // Se houver um aluno específico associado, armazena o ID dele.
        if (alunoId > 0) {
            ocorrencia.setAlunoId(alunoId);
        }

        // Define valores iniciais padrão para a ocorrência.
        ocorrencia.setDataOcorrencia(LocalDate.now());
        ocorrencia.setTipoOcorrencia(TipoOcorrencia.AVISO);
        ocorrencia.setAtendenteNome("Secretaria");

        return ocorrencia;
    }

    // Salva a ocorrência no banco, usando transação para garantir consistência.
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

    // Atualiza uma ocorrência existente no banco usando transação.
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

    // Busca uma ocorrência pelo seu ID.
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

    // Lista todas as ocorrências do sistema.
    public List<Ocorrencia> listarOcorrencias() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO(conn);
            return ocorrenciaDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ocorrências.", e);
        }
    }

    // Lista ocorrências registradas por um funcionário específico.
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

    // Valida os campos obrigatórios antes de salvar ou atualizar.
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
