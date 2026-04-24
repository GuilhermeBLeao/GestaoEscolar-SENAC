//Igor

package controller;

import dao.ProfessorDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Professor;
import util.ValidaCEP;
import util.ValidaCPF;
import util.ValidaCidade;
import util.ValidaNome;
import util.ValidaTelefone;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProfessorController {

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

    private void validarProfessorNaoNulo(Professor professor) {
        if (professor == null) {
            throw new IllegalArgumentException("Professor não pode ser nulo.");
        }
    }

    private void normalizarProfessor(Professor professor) {
        professor.setNome(tratarTexto(professor.getNome()));
        professor.setCpf(normalizarCpf(professor.getCpf()));
        professor.setFormacao(tratarTexto(professor.getFormacao()));
        professor.setTelefone(normalizarTelefone(professor.getTelefone()));
        professor.setRg(tratarTexto(professor.getRg()));

        Endereco endereco = professor.getEndereco();
        if (endereco != null) {
            endereco.setRua(tratarTexto(endereco.getRua()));
            endereco.setNumero(tratarTexto(endereco.getNumero()));
            endereco.setComplemento(tratarTexto(endereco.getComplemento()));
            endereco.setBairro(tratarTexto(endereco.getBairro()));
            endereco.setCidade(tratarTexto(endereco.getCidade()));
            endereco.setCep(normalizarCep(endereco.getCep()));
        }
    }

    private void validarParaCadastro(Professor professor) {
        validarCamposBase(professor);
    }

    private void validarParaAtualizacao(Professor professorAtualizado, Professor professorBanco) {
        validarCamposBase(professorAtualizado);
        validarCpfImutavel(professorAtualizado, professorBanco);
    }

    private void validarCamposBase(Professor professor) {
        ValidaNome.validar(professor.getNome());

        if (!ValidaCPF.isValido(professor.getCpf())) {
            throw new IllegalArgumentException("CPF do professor inválido.");
        }

        if (professor.getFormacao() == null || professor.getFormacao().isBlank()) {
            throw new IllegalArgumentException("Formação do professor é obrigatória.");
        }

        if (!ValidaTelefone.isValido(professor.getTelefone())) {
            throw new IllegalArgumentException("Telefone do professor inválido.");
        }

        if (professor.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento do professor é obrigatória.");
        }

        if (professor.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento do professor não pode ser futura.");
        }

        validarEndereco(professor.getEndereco());
    }

    private void validarCpfImutavel(Professor professorAtualizado, Professor professorBanco) {
        if (!professorBanco.getCpf().equals(professorAtualizado.getCpf())) {
            throw new IllegalArgumentException("CPF do professor não pode ser alterado após o cadastro.");
        }
    }

    private void validarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço é obrigatório.");
        }

        ValidaCidade.validar(endereco.getCidade());

        if (!ValidaCEP.isValido(endereco.getCep())) {
            throw new IllegalArgumentException("CEP inválido.");
        }
    }

    private Professor mesclarDadosPermitidos(Professor professorBanco, Professor professorAtualizado) {
        professorBanco.setNome(professorAtualizado.getNome());
        professorBanco.setFormacao(professorAtualizado.getFormacao());
        professorBanco.setTelefone(professorAtualizado.getTelefone());
        professorBanco.setRg(professorAtualizado.getRg());
        professorBanco.setDataNascimento(professorAtualizado.getDataNascimento());

        atualizarOuCriarEndereco(professorBanco, professorAtualizado);

        return professorBanco;
    }

    private void atualizarOuCriarEndereco(Professor professorBanco, Professor professorAtualizado) {
        Endereco enderecoAtualizado = professorAtualizado.getEndereco();
        if (enderecoAtualizado == null) {
            throw new IllegalArgumentException("Endereço atualizado é obrigatório.");
        }

        Endereco enderecoBanco = professorBanco.getEndereco();

        if (enderecoBanco == null) {
            enderecoBanco = new Endereco();
            professorBanco.setEndereco(enderecoBanco);
        }

        enderecoBanco.setRua(tratarTexto(enderecoAtualizado.getRua()));
        enderecoBanco.setNumero(tratarTexto(enderecoAtualizado.getNumero()));
        enderecoBanco.setComplemento(tratarTexto(enderecoAtualizado.getComplemento()));
        enderecoBanco.setBairro(tratarTexto(enderecoAtualizado.getBairro()));
        enderecoBanco.setCidade(tratarTexto(enderecoAtualizado.getCidade()));
        enderecoBanco.setEstado(enderecoAtualizado.getEstado());
        enderecoBanco.setCep(normalizarCep(enderecoAtualizado.getCep()));
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarCpf(String cpf) {
        String valor = tratarTexto(cpf);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarTelefone(String telefone) {
        String valor = tratarTexto(telefone);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarCep(String cep) {
        String valor = tratarTexto(cep);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public void salvarProfessor(Professor professor) {
        validarProfessorNaoNulo(professor);
        normalizarProfessor(professor);
        validarParaCadastro(professor);

        executarEmTransacao(conn -> {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);

            if (professorDAO.existeCpf(professor.getCpf())) {
                throw new IllegalArgumentException("Já existe professor cadastrado com este CPF.");
            }

            if (professor.getRg() != null && !professor.getRg().isBlank()
                    && professorDAO.existeRg(professor.getRg())) {
                throw new IllegalArgumentException("Já existe professor cadastrado com este RG.");
            }

            professorDAO.inserir(professor);
            return null;
        }, "Erro ao salvar professor.");
    }

    public void atualizarProfessor(Professor professorAtualizado) {
        validarProfessorNaoNulo(professorAtualizado);

        if (professorAtualizado.getIdProfessor() <= 0) {
            throw new IllegalArgumentException("ID do professor inválido.");
        }

        normalizarProfessor(professorAtualizado);

        executarEmTransacao(conn -> {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);

            Professor professorBanco = professorDAO.buscarPorId(professorAtualizado.getIdProfessor());
            if (professorBanco == null) {
                throw new IllegalArgumentException("Professor não encontrado.");
            }

            validarParaAtualizacao(professorAtualizado, professorBanco);

            if (professorAtualizado.getRg() != null && !professorAtualizado.getRg().isBlank()) {
                Professor professorComMesmoRg = professorDAO.buscarPorRg(professorAtualizado.getRg());
                if (professorComMesmoRg != null
                        && professorComMesmoRg.getIdProfessor() != professorAtualizado.getIdProfessor()) {
                    throw new IllegalArgumentException("Já existe outro professor cadastrado com este RG.");
                }
            }

            Professor professorParaSalvar = mesclarDadosPermitidos(professorBanco, professorAtualizado);
            professorDAO.atualizar(professorParaSalvar);
            return null;
        }, "Erro ao atualizar professor.");
    }

    public boolean excluirProfessor(int idProfessor) {
        if (idProfessor <= 0) {
            throw new IllegalArgumentException("ID do professor inválido.");
        }

        return executarEmTransacao(conn -> {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);

            Professor professorExistente = professorDAO.buscarPorId(idProfessor);
            if (professorExistente == null) {
                throw new IllegalArgumentException("Professor não encontrado.");
            }

            return professorDAO.excluir(idProfessor);
        }, "Erro ao excluir professor.");
    }

    public Professor buscarProfessorPorId(int idProfessor) {
        if (idProfessor <= 0) {
            throw new IllegalArgumentException("ID do professor inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);
            return professorDAO.buscarPorId(idProfessor);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar professor por ID.", e);
        }
    }

    public Professor buscarProfessorPorCpf(String cpf) {
        String cpfTratado = normalizarCpf(cpf);

        if (cpfTratado == null || cpfTratado.isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório para busca.");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);
            return professorDAO.buscarPorCpf(cpfTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar professor por CPF.", e);
        }
    }

    public List<Professor> buscarProfessorPorNome(String nome) {
        String nomeTratado = tratarTexto(nome);

        if (nomeTratado == null || nomeTratado.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório para busca.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);
            return professorDAO.buscarPorNome(nomeTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar professor por nome.", e);
        }
    }

    public List<Professor> listarProfessores() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ProfessorDAO professorDAO = new ProfessorDAO(conn);
            return professorDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar professores.", e);
        }
    }
}