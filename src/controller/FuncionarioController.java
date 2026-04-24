//Arthur

package controller;

import dao.FuncionarioDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Funcionario;
import util.ValidaCEP;
import util.ValidaCPF;
import util.ValidaCidade;
import util.ValidaEmail;
import util.ValidaNome;
import util.ValidaTelefone;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FuncionarioController {

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

    private void validarFuncionarioNaoNulo(Funcionario funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não pode ser nulo.");
        }
    }

    private void normalizarFuncionario(Funcionario funcionario) {
        funcionario.setNome(tratarTexto(funcionario.getNome()));
        funcionario.setCpf(normalizarCpf(funcionario.getCpf()));
        funcionario.setCargo(tratarTexto(funcionario.getCargo()));
        funcionario.setTelefone(normalizarTelefone(funcionario.getTelefone()));
        funcionario.setRg(tratarTexto(funcionario.getRg()));
        funcionario.setSetor(tratarTexto(funcionario.getSetor()));
        funcionario.setEmail(normalizarEmail(funcionario.getEmail()));

        Endereco endereco = funcionario.getEndereco();
        if (endereco != null) {
            endereco.setRua(tratarTexto(endereco.getRua()));
            endereco.setNumero(tratarTexto(endereco.getNumero()));
            endereco.setComplemento(tratarTexto(endereco.getComplemento()));
            endereco.setBairro(tratarTexto(endereco.getBairro()));
            endereco.setCidade(tratarTexto(endereco.getCidade()));
            endereco.setCep(normalizarCep(endereco.getCep()));
        }
    }

    private void validarParaCadastro(Funcionario funcionario) {
        validarCamposBase(funcionario);
    }

    private void validarParaAtualizacao(Funcionario funcionarioAtualizado, Funcionario funcionarioBanco) {
        validarCamposBase(funcionarioAtualizado);
        validarCpfImutavel(funcionarioAtualizado, funcionarioBanco);
    }

    private void validarCamposBase(Funcionario funcionario) {
        ValidaNome.validar(funcionario.getNome());

        if (!ValidaCPF.isValido(funcionario.getCpf())) {
            throw new IllegalArgumentException("CPF do funcionário inválido.");
        }

        if (!ValidaTelefone.isValido(funcionario.getTelefone())) {
            throw new IllegalArgumentException("Telefone do funcionário inválido.");
        }

        if (!ValidaEmail.isValido(funcionario.getEmail())) {
            throw new IllegalArgumentException("Email do funcionário inválido.");
        }

        if (funcionario.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento do funcionário é obrigatória.");
        }

        if (funcionario.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento do funcionário não pode ser futura.");
        }

        if (funcionario.getDataContratacao() == null) {
            throw new IllegalArgumentException("Data de contratação do funcionário é obrigatória.");
        }

        if (funcionario.getDataContratacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de contratação do funcionário não pode ser futura.");
        }

        if (funcionario.getDataContratacao().isBefore(funcionario.getDataNascimento())) {
            throw new IllegalArgumentException("Data de contratação não pode ser anterior à data de nascimento.");
        }

        validarEndereco(funcionario.getEndereco());
    }

    private void validarCpfImutavel(Funcionario funcionarioAtualizado, Funcionario funcionarioBanco) {
        if (!funcionarioBanco.getCpf().equals(funcionarioAtualizado.getCpf())) {
            throw new IllegalArgumentException("CPF do funcionário não pode ser alterado após o cadastro.");
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

    private Funcionario mesclarDadosPermitidos(Funcionario funcionarioBanco, Funcionario funcionarioAtualizado) {
        funcionarioBanco.setNome(funcionarioAtualizado.getNome());
        funcionarioBanco.setCargo(funcionarioAtualizado.getCargo());
        funcionarioBanco.setTelefone(funcionarioAtualizado.getTelefone());
        funcionarioBanco.setAtivo(funcionarioAtualizado.isAtivo());
        funcionarioBanco.setRg(funcionarioAtualizado.getRg());
        funcionarioBanco.setSexo(funcionarioAtualizado.getSexo());
        funcionarioBanco.setSetor(funcionarioAtualizado.getSetor());
        funcionarioBanco.setEmail(funcionarioAtualizado.getEmail());
        funcionarioBanco.setDataNascimento(funcionarioAtualizado.getDataNascimento());
        funcionarioBanco.setDataContratacao(funcionarioAtualizado.getDataContratacao());

        atualizarOuCriarEndereco(funcionarioBanco, funcionarioAtualizado);

        return funcionarioBanco;
    }

    private void atualizarOuCriarEndereco(Funcionario funcionarioBanco, Funcionario funcionarioAtualizado) {
        Endereco enderecoAtualizado = funcionarioAtualizado.getEndereco();
        if (enderecoAtualizado == null) {
            throw new IllegalArgumentException("Endereço atualizado é obrigatório.");
        }

        Endereco enderecoBanco = funcionarioBanco.getEndereco();

        if (enderecoBanco == null) {
            enderecoBanco = new Endereco();
            funcionarioBanco.setEndereco(enderecoBanco);
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

    private String normalizarEmail(String email) {
        String valor = tratarTexto(email);
        return valor == null ? null : valor.toLowerCase();
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

    public void salvarFuncionario(Funcionario funcionario) {
        validarFuncionarioNaoNulo(funcionario);
        normalizarFuncionario(funcionario);
        validarParaCadastro(funcionario);

        executarEmTransacao(conn -> {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

            if (funcionarioDAO.existeCpf(funcionario.getCpf())) {
                throw new IllegalArgumentException("Já existe funcionário cadastrado com este CPF.");
            }

            funcionarioDAO.inserir(funcionario);
            return null;
        }, "Erro ao salvar funcionário.");
    }

    public void atualizarFuncionario(Funcionario funcionarioAtualizado) {
        validarFuncionarioNaoNulo(funcionarioAtualizado);

        if (funcionarioAtualizado.getIdFuncionario() <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        normalizarFuncionario(funcionarioAtualizado);

        executarEmTransacao(conn -> {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

            Funcionario funcionarioBanco = funcionarioDAO.buscarPorId(funcionarioAtualizado.getIdFuncionario());
            if (funcionarioBanco == null) {
                throw new IllegalArgumentException("Funcionário não encontrado.");
            }

            validarParaAtualizacao(funcionarioAtualizado, funcionarioBanco);

            Funcionario funcionarioParaSalvar = mesclarDadosPermitidos(funcionarioBanco, funcionarioAtualizado);
            funcionarioDAO.atualizar(funcionarioParaSalvar);
            return null;
        }, "Erro ao atualizar funcionário.");
    }

    public boolean excluirFuncionario(int idFuncionario) {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        return executarEmTransacao(conn -> {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

            Funcionario funcionarioExistente = funcionarioDAO.buscarPorId(idFuncionario);
            if (funcionarioExistente == null) {
                throw new IllegalArgumentException("Funcionário não encontrado.");
            }

            return funcionarioDAO.excluir(idFuncionario);
        }, "Erro ao excluir funcionário.");
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
        String cpfTratado = normalizarCpf(cpf);

        if (cpfTratado == null || cpfTratado.isBlank()) {
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

        if (nomeTratado == null || nomeTratado.isBlank()) {
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
}