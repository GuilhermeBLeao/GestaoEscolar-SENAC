/*Arthur
Guilherme adicionou a exclusão lógica*/

package controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        } catch (SQLException ex) {
          e.addSuppressed(ex);
        }
        throw e;
      } catch (SQLException | RuntimeException e) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          e.addSuppressed(ex);
        }
        throw new RuntimeException(mensagemOperacao, e);
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
    }
  }

  private void validarNaoNulo(Funcionario funcionario) {
    if (funcionario == null) {
      throw new IllegalArgumentException("Funcionário não pode ser nulo.");
    }
  }

  private String tratarTexto(String valor) {
    return valor == null ? null : valor.trim();
  }

  private String normalizarCpf(String valor) {
    String texto = tratarTexto(valor);
    return texto == null ? null : texto.replaceAll("\\D", "");
  }

  private String normalizarTelefone(String valor) {
    String texto = tratarTexto(valor);
    return texto == null ? null : texto.replaceAll("\\D", "");
  }

  private String normalizarCep(String valor) {
    String texto = tratarTexto(valor);
    return texto == null ? null : texto.replaceAll("\\D", "");
  }

  private String normalizarEmail(String valor) {
    String texto = tratarTexto(valor);
    return texto == null ? null : texto.toLowerCase();
  }

  private void normalizarDadosEditaveis(Funcionario funcionario) {
    funcionario.setNome(tratarTexto(funcionario.getNome()));
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

  private void validarEndereco(Endereco endereco) {
    if (endereco == null) {
      throw new IllegalArgumentException("Endereço é obrigatório.");
    }

    ValidaCidade.validar(endereco.getCidade());

    if (endereco.getEstado() == null) {
      throw new IllegalArgumentException("Estado é obrigatório.");
    }

    if (!ValidaCEP.isValido(endereco.getCep())) {
      throw new IllegalArgumentException("CEP inválido.");
    }
  }

  private void validarCamposBase(Funcionario funcionario) {
    ValidaNome.validar(funcionario.getNome());

    String cpfNormalizado = normalizarCpf(funcionario.getCpf());
    if (cpfNormalizado == null || !ValidaCPF.isValido(cpfNormalizado)) {
      throw new IllegalArgumentException("CPF do funcionário inválido.");
    }

    if (funcionario.getCargo() == null || funcionario.getCargo().isBlank()) {
      throw new IllegalArgumentException("Cargo do funcionário é obrigatório.");
    }

    if (!ValidaTelefone.isValido(funcionario.getTelefone())) {
      throw new IllegalArgumentException("Telefone do funcionário inválido.");
    }

    if (!ValidaEmail.isValido(funcionario.getEmail())) {
      throw new IllegalArgumentException("Email do funcionário inválido.");
    }

    if (funcionario.getSexo() == null) {
      throw new IllegalArgumentException("Sexo do funcionário é obrigatório.");
    }

    if (funcionario.getPerfil() == null) {
      throw new IllegalArgumentException("Perfil do funcionário é obrigatório.");
    }

    if (funcionario.getDataNascimento() == null
        || funcionario.getDataNascimento().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Data de nascimento do funcionário inválida.");
    }

    if (funcionario.getDataContratacao() == null
        || funcionario.getDataContratacao().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Data de contratação do funcionário inválida.");
    }

    if (funcionario.getDataContratacao().isBefore(funcionario.getDataNascimento())) {
      throw new IllegalArgumentException(
          "Data de contratação não pode ser anterior à data de nascimento.");
    }

    validarEndereco(funcionario.getEndereco());
  }

  private void validarCpfImutavel(Funcionario funcionarioAtualizado, Funcionario funcionarioBanco) {
    String cpfAtualizado = normalizarCpf(funcionarioAtualizado.getCpf());
    String cpfBanco = normalizarCpf(funcionarioBanco.getCpf());

    if (!cpfBanco.equals(cpfAtualizado)) {
      throw new IllegalArgumentException(
          "CPF do funcionário não pode ser alterado após o cadastro.");
    }
  }

  private Funcionario mesclar(Funcionario funcionarioBanco, Funcionario funcionarioAtualizado) {
    funcionarioBanco.setNome(funcionarioAtualizado.getNome());
    funcionarioBanco.setCargo(funcionarioAtualizado.getCargo());
    funcionarioBanco.setTelefone(funcionarioAtualizado.getTelefone());
    funcionarioBanco.setRg(funcionarioAtualizado.getRg());
    funcionarioBanco.setSexo(funcionarioAtualizado.getSexo());
    funcionarioBanco.setSetor(funcionarioAtualizado.getSetor());
    funcionarioBanco.setEmail(funcionarioAtualizado.getEmail());
    funcionarioBanco.setDataNascimento(funcionarioAtualizado.getDataNascimento());
    funcionarioBanco.setDataContratacao(funcionarioAtualizado.getDataContratacao());
    funcionarioBanco.setEndereco(funcionarioAtualizado.getEndereco());
    funcionarioBanco.setPerfil(funcionarioAtualizado.getPerfil());
    return funcionarioBanco;
  }

  private void informarSeInativo(Funcionario funcionario) {
    if (funcionario != null && !funcionario.isAtivo()) {
      System.out.println("ATENÇÃO: funcionário encontrado, porém está inativo.");
    }
  }

  public void salvarFuncionario(Funcionario funcionario) {
    validarNaoNulo(funcionario);
    normalizarDadosEditaveis(funcionario);
    validarCamposBase(funcionario);

    executarEmTransacao(
        conn -> {
          FuncionarioDAO dao = new FuncionarioDAO(conn);

          if (dao.existeCpf(funcionario.getCpf())) {
            throw new IllegalArgumentException("Já existe funcionário cadastrado com este CPF.");
          }

          dao.inserir(funcionario);
          return null;
        },
        "Erro ao salvar funcionário.");
  }

  public void atualizarFuncionario(Funcionario funcionarioAtualizado) {
    validarNaoNulo(funcionarioAtualizado);

    if (funcionarioAtualizado.getIdFuncionario() <= 0) {
      throw new IllegalArgumentException("ID do funcionário inválido.");
    }

    normalizarDadosEditaveis(funcionarioAtualizado);
    validarCamposBase(funcionarioAtualizado);

    executarEmTransacao(
        conn -> {
          FuncionarioDAO dao = new FuncionarioDAO(conn);
          Funcionario funcionarioBanco = dao.buscarPorId(funcionarioAtualizado.getIdFuncionario());

          if (funcionarioBanco == null) {
            throw new IllegalArgumentException("Funcionário não encontrado.");
          }

          if (!funcionarioBanco.isAtivo()) {
            throw new IllegalArgumentException("Não é possível atualizar funcionário inativo.");
          }

          validarCpfImutavel(funcionarioAtualizado, funcionarioBanco);
          dao.atualizar(mesclar(funcionarioBanco, funcionarioAtualizado));
          return null;
        },
        "Erro ao atualizar funcionário.");
  }

  public boolean excluirFuncionario(int idFuncionario) {
    if (idFuncionario <= 0) {
      throw new IllegalArgumentException("ID do funcionário inválido.");
    }

    return executarEmTransacao(
        conn -> {
          FuncionarioDAO dao = new FuncionarioDAO(conn);

          if (dao.buscarPorId(idFuncionario) == null) {
            throw new IllegalArgumentException("Funcionário não encontrado.");
          }

          return dao.inativar(idFuncionario);
        },
        "Erro ao excluir funcionário.");
  }

  public boolean reativarFuncionario(int idFuncionario) {
    if (idFuncionario <= 0) {
      throw new IllegalArgumentException("ID do funcionário inválido.");
    }

    return executarEmTransacao(
        conn -> new FuncionarioDAO(conn).reativar(idFuncionario), "Erro ao reativar funcionário.");
  }

  public Funcionario buscarFuncionarioPorId(int idFuncionario) {
    if (idFuncionario <= 0) {
      throw new IllegalArgumentException("ID do funcionário inválido.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      Funcionario funcionario = new FuncionarioDAO(conn).buscarPorId(idFuncionario);

      if (funcionario == null) {
        throw new IllegalArgumentException("Funcionário não encontrado.");
      }

      informarSeInativo(funcionario);
      return funcionario;
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar funcionário por ID.", e);
    }
  }

  public Funcionario buscarFuncionarioPorCpf(String cpf) {
    String cpfNormalizado = normalizarCpf(cpf);

    if (cpfNormalizado == null || !ValidaCPF.isValido(cpfNormalizado)) {
      throw new IllegalArgumentException("CPF do funcionário inválido.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      Funcionario funcionario = new FuncionarioDAO(conn).buscarPorCpf(cpfNormalizado);

      if (funcionario == null) {
        throw new IllegalArgumentException("Funcionário não encontrado.");
      }

      informarSeInativo(funcionario);
      return funcionario;
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar funcionário por CPF.", e);
    }
  }

  public Funcionario buscarFuncionarioPorNome(String nome) {
    String nomeTratado = tratarTexto(nome);

    if (nomeTratado == null || nomeTratado.isBlank()) {
      throw new IllegalArgumentException("Nome do funcionário é obrigatório.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      Funcionario funcionario = new FuncionarioDAO(conn).buscarPorNome(nomeTratado);

      if (funcionario == null) {
        throw new IllegalArgumentException("Funcionário não encontrado.");
      }

      informarSeInativo(funcionario);
      return funcionario;
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar funcionário por nome.", e);
    }
  }

  public List<Funcionario> listarFuncionarios() {
    return listarTodosFuncionarios();
  }

  public List<Funcionario> listarTodosFuncionarios() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      return new FuncionarioDAO(conn).listarTodos();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar funcionários.", e);
    }
  }

  public List<Funcionario> listarFuncionariosAtivos() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      return new FuncionarioDAO(conn).listarAtivos();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar funcionários ativos.", e);
    }
  }

  public List<Funcionario> listarFuncionariosInativos() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      return new FuncionarioDAO(conn).listarInativos();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar funcionários inativos.", e);
    }
  }
}
