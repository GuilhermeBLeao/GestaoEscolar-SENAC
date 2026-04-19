//Arthur

package controller;

import dao.FuncionarioDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Funcionario;
import util.ValidaCEP;
import util.ValidaCPF;
import util.ValidaEmail;
import util.ValidaTelefone;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class FuncionarioController {

    public void salvarFuncionario(Funcionario funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não pode ser nulo.");
        }

        normalizarFuncionario(funcionario);
        validarFuncionario(funcionario);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

                if (funcionarioDAO.existeCpf(funcionario.getCpf())) {
                    throw new IllegalArgumentException("Já existe funcionário cadastrado com este CPF.");
                }

                funcionarioDAO.inserir(funcionario);
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar funcionário.", e);
        }
    }

    public void atualizarFuncionario(Funcionario funcionarioAtualizado) {
        if (funcionarioAtualizado == null) {
            throw new IllegalArgumentException("Funcionário não pode ser nulo.");
        }

        if (funcionarioAtualizado.getIdFuncionario() <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        normalizarFuncionario(funcionarioAtualizado);
        validarFuncionario(funcionarioAtualizado);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

                Funcionario funcionarioBanco = funcionarioDAO.buscarPorId(funcionarioAtualizado.getIdFuncionario());
                if (funcionarioBanco == null) {
                    throw new IllegalArgumentException("Funcionário não encontrado.");
                }

                Funcionario funcionarioComMesmoCpf = funcionarioDAO.buscarPorCpf(funcionarioAtualizado.getCpf());
                if (funcionarioComMesmoCpf != null
                        && funcionarioComMesmoCpf.getIdFuncionario() != funcionarioAtualizado.getIdFuncionario()) {
                    throw new IllegalArgumentException("Já existe outro funcionário cadastrado com este CPF.");
                }

                Funcionario funcionarioParaSalvar = mesclarDadosPermitidos(funcionarioBanco, funcionarioAtualizado);

                funcionarioDAO.atualizar(funcionarioParaSalvar);
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar funcionário.", e);
        }
    }

    public boolean excluirFuncionario(int idFuncionario) {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

                Funcionario funcionario = funcionarioDAO.buscarPorId(idFuncionario);
                if (funcionario == null) {
                    throw new IllegalArgumentException("Funcionário não encontrado.");
                }

                boolean excluiu = funcionarioDAO.excluir(idFuncionario);
                conn.commit();
                return excluiu;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir funcionário.", e);
        }
    }

    public Funcionario buscarFuncionarioPorId(int idFuncionario) {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
            return funcionarioDAO.buscarPorId(idFuncionario);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar funcionário por ID.", e);
        }
    }

    public Funcionario buscarFuncionarioPorCpf(String cpf) {
        String cpfTratado = tratarTexto(cpf);

        if (cpfTratado == null || cpfTratado.isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório para busca.");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
            return funcionarioDAO.buscarPorCpf(cpfTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar funcionário por CPF.", e);
        }
    }

    public List<Funcionario> buscarFuncionarioPorNome(String nome) {
        String nomeTratado = tratarTexto(nome);

        if (nomeTratado == null || nomeTratado.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório para busca.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
            return funcionarioDAO.buscarPorNome(nomeTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar funcionário por nome.", e);
        }
    }

    public List<Funcionario> listarFuncionarios() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
            return funcionarioDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar funcionários.", e);
        }
    }

    private Funcionario mesclarDadosPermitidos(Funcionario funcionarioBanco, Funcionario funcionarioAtualizado) {
        funcionarioBanco.setNome(funcionarioAtualizado.getNome());
        funcionarioBanco.setCpf(funcionarioAtualizado.getCpf());
        funcionarioBanco.setCargo(funcionarioAtualizado.getCargo());
        funcionarioBanco.setTelefone(funcionarioAtualizado.getTelefone());
        funcionarioBanco.setAtivo(funcionarioAtualizado.isAtivo());
        funcionarioBanco.setRg(funcionarioAtualizado.getRg());
        funcionarioBanco.setSexo(funcionarioAtualizado.getSexo());
        funcionarioBanco.setSetor(funcionarioAtualizado.getSetor());
        funcionarioBanco.setEmail(funcionarioAtualizado.getEmail());
        funcionarioBanco.setDataNascimento(funcionarioAtualizado.getDataNascimento());
        funcionarioBanco.setDataContratacao(funcionarioAtualizado.getDataContratacao());

        atualizarEnderecoExistente(funcionarioBanco, funcionarioAtualizado);

        return funcionarioBanco;
    }

    private void atualizarEnderecoExistente(Funcionario funcionarioBanco, Funcionario funcionarioAtualizado) {
        if (funcionarioAtualizado.getEndereco() == null) {
            throw new IllegalArgumentException("Endereço do funcionário é obrigatório.");
        }

        Endereco enderecoBanco = funcionarioBanco.getEndereco();
        Endereco enderecoAtualizado = funcionarioAtualizado.getEndereco();

        if (enderecoBanco == null) {
            throw new IllegalArgumentException("Funcionário atual não possui endereço cadastrado.");
        }

        enderecoBanco.setRua(tratarTexto(enderecoAtualizado.getRua()));
        enderecoBanco.setNumero(tratarTexto(enderecoAtualizado.getNumero()));
        enderecoBanco.setComplemento(tratarTexto(enderecoAtualizado.getComplemento()));
        enderecoBanco.setBairro(tratarTexto(enderecoAtualizado.getBairro()));
        enderecoBanco.setCidade(tratarTexto(enderecoAtualizado.getCidade()));
        enderecoBanco.setEstado(tratarTexto(enderecoAtualizado.getEstado()));
        enderecoBanco.setCep(tratarTexto(enderecoAtualizado.getCep()));
    }

    private void normalizarFuncionario(Funcionario funcionario) {
        funcionario.setNome(tratarTexto(funcionario.getNome()));
        funcionario.setCpf(tratarTexto(funcionario.getCpf()));
        funcionario.setCargo(tratarTexto(funcionario.getCargo()));
        funcionario.setTelefone(tratarTexto(funcionario.getTelefone()));
        funcionario.setRg(tratarTexto(funcionario.getRg()));
        funcionario.setSetor(tratarTexto(funcionario.getSetor()));
        funcionario.setEmail(tratarTexto(funcionario.getEmail()));

        if (funcionario.getEndereco() != null) {
            funcionario.getEndereco().setRua(tratarTexto(funcionario.getEndereco().getRua()));
            funcionario.getEndereco().setNumero(tratarTexto(funcionario.getEndereco().getNumero()));
            funcionario.getEndereco().setComplemento(tratarTexto(funcionario.getEndereco().getComplemento()));
            funcionario.getEndereco().setBairro(tratarTexto(funcionario.getEndereco().getBairro()));
            funcionario.getEndereco().setCidade(tratarTexto(funcionario.getEndereco().getCidade()));
            funcionario.getEndereco().setEstado(tratarTexto(funcionario.getEndereco().getEstado()));
            funcionario.getEndereco().setCep(tratarTexto(funcionario.getEndereco().getCep()));
        }
    }

    private void validarFuncionario(Funcionario funcionario) {
        if (funcionario.getNome() == null || funcionario.getNome().isEmpty()) {
            throw new IllegalArgumentException("Nome do funcionário é obrigatório.");
        }

        if (funcionario.getCpf() == null || funcionario.getCpf().isEmpty()) {
            throw new IllegalArgumentException("CPF do funcionário é obrigatório.");
        }

        if (!ValidaCPF.isValido(funcionario.getCpf())) {
            throw new IllegalArgumentException("CPF do funcionário inválido.");
        }

        if (funcionario.getCargo() == null || funcionario.getCargo().isEmpty()) {
            throw new IllegalArgumentException("Cargo do funcionário é obrigatório.");
        }

        if (funcionario.getTelefone() == null || funcionario.getTelefone().isEmpty()) {
            throw new IllegalArgumentException("Telefone do funcionário é obrigatório.");
        }

        if (!ValidaTelefone.isValido(funcionario.getTelefone())) {
            throw new IllegalArgumentException("Telefone do funcionário inválido.");
        }

        if (funcionario.getEmail() == null || funcionario.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email do funcionário é obrigatório.");
        }

        if (!ValidaEmail.isValido(funcionario.getEmail())) {
            throw new IllegalArgumentException("Email do funcionário inválido.");
        }

        if (funcionario.getSexo() == null) {
            throw new IllegalArgumentException("Sexo do funcionário é obrigatório.");
        }

        if (funcionario.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento do funcionário é obrigatória.");
        }

        if (funcionario.getDataContratacao() == null) {
            throw new IllegalArgumentException("Data de contratação do funcionário é obrigatória.");
        }

        if (funcionario.getEndereco() == null) {
            throw new IllegalArgumentException("Endereço do funcionário é obrigatório.");
        }

        validarEndereco(funcionario.getEndereco());
    }

    private void validarEndereco(Endereco endereco) {
        if (endereco.getRua() == null || endereco.getRua().isEmpty()) {
            throw new IllegalArgumentException("Rua é obrigatória.");
        }

        if (endereco.getBairro() == null || endereco.getBairro().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório.");
        }

        if (endereco.getCidade() == null || endereco.getCidade().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória.");
        }

        if (endereco.getEstado() == null || endereco.getEstado().isEmpty()) {
            throw new IllegalArgumentException("Estado é obrigatório.");
        }

        if (endereco.getCep() == null || endereco.getCep().isEmpty()) {
            throw new IllegalArgumentException("CEP é obrigatório.");
        }

        if (!ValidaCEP.isValido(endereco.getCep())) {
            throw new IllegalArgumentException("CEP inválido.");
        }
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}