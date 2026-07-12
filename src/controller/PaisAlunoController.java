/*Igor
Guilherme adicionou a exclusão lógica*/

package controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import dao.PaisAlunoDAO;
import database.ConnectionFactory;
import model.PaisAluno;
import util.ValidaCPF;
import util.ValidaEmail;
import util.ValidaNome;
import util.ValidaTelefone;

public class PaisAlunoController {
  @FunctionalInterface
  private interface AcaoTransacional<T> {
    T executar(Connection conn) throws SQLException;
  }

  private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {
    try (Connection conn = ConnectionFactory.getConnection()) {
      conn.setAutoCommit(false);
      try {
        T r = acao.executar(conn);
        conn.commit();
        return r;
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

  private void validarNaoNulo(PaisAluno p) {
    if (p == null) throw new IllegalArgumentException("Pais/responsáveis não podem ser nulos.");
  }

  private String tratarTexto(String v) {
    return v == null ? null : v.trim();
  }

  private String normalizarCpf(String v) {
    String t = tratarTexto(v);
    return t == null || t.isBlank() ? null : t.replaceAll("\\D", "");
  }

  private String normalizarTelefone(String v) {
    String t = tratarTexto(v);
    return t == null || t.isBlank() ? null : t.replaceAll("\\D", "");
  }

  private String normalizarEmail(String v) {
    String t = tratarTexto(v);
    return t == null || t.isBlank() ? null : t.toLowerCase();
  }

  private void normalizar(PaisAluno p) {
    p.setNomeMae(tratarTexto(p.getNomeMae()));
    p.setEmailMae(normalizarEmail(p.getEmailMae()));
    p.setTelefoneMae(normalizarTelefone(p.getTelefoneMae()));
    p.setCpfMae(normalizarCpf(p.getCpfMae()));
    p.setNomePai(tratarTexto(p.getNomePai()));
    p.setEmailPai(normalizarEmail(p.getEmailPai()));
    p.setTelefonePai(normalizarTelefone(p.getTelefonePai()));
    p.setCpfPai(normalizarCpf(p.getCpfPai()));
  }

  private boolean temAlgum(String nome, String email, String telefone, String cpf) {
    return (nome != null && !nome.isBlank())
        || (email != null && !email.isBlank())
        || (telefone != null && !telefone.isBlank())
        || (cpf != null && !cpf.isBlank());
  }

  private void validarResponsavel(
      String nome, String email, String telefone, String cpf, String tipo) {
    if (nome == null || nome.isBlank())
      throw new IllegalArgumentException("Nome do(a) " + tipo + " é obrigatório quando informado.");
    ValidaNome.validar(nome);
    if (email == null || email.isBlank() || !ValidaEmail.isValido(email))
      throw new IllegalArgumentException("Email do(a) " + tipo + " inválido.");
    if (telefone == null || telefone.isBlank() || !ValidaTelefone.isValido(telefone))
      throw new IllegalArgumentException("Telefone do(a) " + tipo + " inválido.");
    if (cpf == null || cpf.isBlank() || !ValidaCPF.isValido(cpf))
      throw new IllegalArgumentException("CPF do(a) " + tipo + " inválido.");
  }

  private void validarCamposBase(PaisAluno p) {
    boolean mae = temAlgum(p.getNomeMae(), p.getEmailMae(), p.getTelefoneMae(), p.getCpfMae());
    boolean pai = temAlgum(p.getNomePai(), p.getEmailPai(), p.getTelefonePai(), p.getCpfPai());
    if (!mae && !pai)
      throw new IllegalArgumentException("Pelo menos um responsável deve ser informado.");
    if (mae)
      validarResponsavel(p.getNomeMae(), p.getEmailMae(), p.getTelefoneMae(), p.getCpfMae(), "mãe");
    if (pai)
      validarResponsavel(p.getNomePai(), p.getEmailPai(), p.getTelefonePai(), p.getCpfPai(), "pai");
    if (p.getCpfMae() != null && p.getCpfPai() != null && p.getCpfMae().equals(p.getCpfPai()))
      throw new IllegalArgumentException("CPF da mãe e do pai não podem ser iguais.");
  }

  private void validarCpfImutavel(PaisAluno atual, PaisAluno banco) {
    if (banco.getCpfMae() != null && !banco.getCpfMae().equals(atual.getCpfMae()))
      throw new IllegalArgumentException("CPF da mãe não pode ser alterado após o cadastro.");
    if (banco.getCpfPai() != null && !banco.getCpfPai().equals(atual.getCpfPai()))
      throw new IllegalArgumentException("CPF do pai não pode ser alterado após o cadastro.");
  }

  private PaisAluno mesclar(PaisAluno b, PaisAluno a) {
    b.setNomeMae(a.getNomeMae());
    b.setNomePai(a.getNomePai());
    b.setEmailMae(a.getEmailMae());
    b.setEmailPai(a.getEmailPai());
    b.setTelefoneMae(a.getTelefoneMae());
    b.setTelefonePai(a.getTelefonePai());
    return b;
  }

  private void informarSeInativo(PaisAluno p) {
    if (p != null && !p.isAtivo())
      System.out.println("ATENÇÃO: pais/responsáveis encontrados, porém o cadastro está inativo.");
  }

  // Salva pais/responsáveis após validar dados obrigatórios e impedir CPF duplicado.
  public void salvarPaisAluno(PaisAluno paisAluno) {
    validarNaoNulo(paisAluno);
    normalizar(paisAluno);
    validarCamposBase(paisAluno);
    executarEmTransacao(
        conn -> {
          PaisAlunoDAO dao = new PaisAlunoDAO(conn);
          if (paisAluno.getCpfMae() != null && dao.existeCpfMae(paisAluno.getCpfMae()))
            throw new IllegalArgumentException("Já existe cadastro com este CPF de mãe.");
          if (paisAluno.getCpfPai() != null && dao.existeCpfPai(paisAluno.getCpfPai()))
            throw new IllegalArgumentException("Já existe cadastro com este CPF de pai.");
          dao.inserir(paisAluno);
          return null;
        },
        "Erro ao salvar pais/responsáveis.");
  }

  // Atualiza pais/responsáveis ativos, mantendo CPFs imutáveis.
  public void atualizarPaisAluno(PaisAluno paisAlunoAtualizado) {
    validarNaoNulo(paisAlunoAtualizado);
    if (paisAlunoAtualizado.getIdPais() <= 0)
      throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
    normalizar(paisAlunoAtualizado);
    validarCamposBase(paisAlunoAtualizado);
    executarEmTransacao(
        conn -> {
          PaisAlunoDAO dao = new PaisAlunoDAO(conn);
          PaisAluno banco = dao.buscarPorId(paisAlunoAtualizado.getIdPais());
          if (banco == null)
            throw new IllegalArgumentException("Pais/responsáveis não encontrados.");
          if (!banco.isAtivo())
            throw new IllegalArgumentException(
                "Não é possível atualizar pais/responsáveis inativos.");
          validarCpfImutavel(paisAlunoAtualizado, banco);
          dao.atualizar(mesclar(banco, paisAlunoAtualizado));
          return null;
        },
        "Erro ao atualizar pais/responsáveis.");
  }

  // Realiza exclusão lógica de pais/responsáveis, inativando o cadastro no banco.
  public boolean inativarPaisAluno(int idPais) {
    if (idPais <= 0) throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
    return executarEmTransacao(
        conn -> {
          PaisAlunoDAO dao = new PaisAlunoDAO(conn);
          if (dao.buscarPorId(idPais) == null)
            throw new IllegalArgumentException("Pais/responsáveis não encontrados.");
          return dao.inativar(idPais);
        },
        "Erro ao excluir pais/responsáveis.");
  }

  // Reativa pais/responsáveis previamente inativados.
  public boolean reativarPaisAluno(int idPais) {
    if (idPais <= 0) throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
    return executarEmTransacao(
        conn -> new PaisAlunoDAO(conn).reativar(idPais), "Erro ao reativar pais/responsáveis.");
  }

  // Busca pais/responsáveis por ID, retornando também inativos e avisando quando o cadastro estiver
  // inativo.
  public PaisAluno buscarPaisAlunoPorId(int idPais) {
    if (idPais <= 0) throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
    try (Connection conn = ConnectionFactory.getConnection()) {
      PaisAluno p = new PaisAlunoDAO(conn).buscarPorId(idPais);
      if (p == null) throw new IllegalArgumentException("Pais/responsáveis não encontrados.");
      informarSeInativo(p);
      return p;
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar pais/responsáveis por ID.", e);
    }
  }

  // Busca pais/responsáveis por pelo CPF da mãe, retornando também inativos e avisando quando o
  // cadastro estiver inativo.
  public PaisAluno buscarPaisAlunoPorCpfMae(String cpfMae) {
    String cpfTratado = normalizarCpf(cpfMae);

    if (cpfTratado == null || cpfTratado.isBlank()) {
      throw new IllegalArgumentException("CPF da mãe é obrigatório para busca");
    }

    if (!ValidaCPF.isValido(cpfTratado)) {
      throw new IllegalArgumentException("CPF da mãe inválido.");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
      return paisAlunoDAO.buscarPorCpfMae(cpfTratado);
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar pais/responsáveis por CPF da mãe", e);
    }
  }

  // Busca pais/responsáveis pelo CPF do pai, retornando também inativos e avisando quando o
  // cadastro estiver inativo.
  public PaisAluno buscarPaisAlunoPorCpfPai(String cpfPai) {
    String cpfTratado = normalizarCpf(cpfPai);

    if (cpfTratado == null || cpfTratado.isBlank()) {
      throw new IllegalArgumentException("CPF do pai é obrigatório para busca");
    }

    if (!ValidaCPF.isValido(cpfTratado)) {
      throw new IllegalArgumentException("CPF do pai inválido");
    }

    try (Connection conn = ConnectionFactory.getConnection()) {
      PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
      return paisAlunoDAO.buscarPorCpfPai(cpfTratado);
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao buscar pais/responsáveis por CPF do pai", e);
    }
  }

  // Lista todos os cadastros de pais/responsáveis, ativos e inativos.
  public List<PaisAluno> listarPaisAlunos() {
    return listarTodosPaisAlunos();
  }

  // Lista todos os cadastros de pais/responsáveis, ativos e inativos.
  public List<PaisAluno> listarTodosPaisAlunos() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      return new PaisAlunoDAO(conn).listarTodos();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar pais/responsáveis.", e);
    }
  }

  // Lista somente pais/responsáveis ativos.
  public List<PaisAluno> listarPaisAlunosAtivos() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      return new PaisAlunoDAO(conn).listarAtivos();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar pais/responsáveis ativos.", e);
    }
  }

  // Lista somente pais/responsáveis inativos.
  public List<PaisAluno> listarPaisAlunosInativos() {
    try (Connection conn = ConnectionFactory.getConnection()) {
      return new PaisAlunoDAO(conn).listarInativos();
    } catch (SQLException e) {
      throw new RuntimeException("Erro ao listar pais/responsáveis inativos.", e);
    }
  }
}
