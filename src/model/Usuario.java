// Guilherme

package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;

import util.ValidaCPF;
import util.ValidaSenha;
import variaveisEnum.TipoUsuario;

public class Usuario {

  private int idUsuario, alunoId, funcionarioId, paiId, professorId;
  private String cpf, senhaHash;
  private boolean ativo = true;
  private LocalDate dataCriacao;
  private LocalDateTime ultimoLogin;
  private TipoUsuario tipoUsuario;

  public Usuario() {
    this.ativo = true;
    this.dataCriacao = LocalDate.now();
  }

  public int getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(int idUsuario) {
    if (idUsuario <= 0) {
      throw new IllegalArgumentException("ID do usuário é inválido.");
    }
    this.idUsuario = idUsuario;
  }

  public int getAlunoId() {
    return alunoId;
  }

  public void setAlunoId(int alunoId) {
    if (alunoId < 0) {
      throw new IllegalArgumentException("ID do aluno é inválido.");
    }
    this.alunoId = alunoId;
  }

  public int getFuncionarioId() {
    return funcionarioId;
  }

  public void setFuncionarioId(int funcionarioId) {
    if (funcionarioId < 0) {
      throw new IllegalArgumentException("ID do funcionário é inválido.");
    }
    this.funcionarioId = funcionarioId;
  }

  public int getPaiId() {
    return paiId;
  }

  public void setPaiId(int paiId) {
    if (paiId < 0) {
      throw new IllegalArgumentException("ID dos pais/responsáveis é inválido.");
    }
    this.paiId = paiId;
  }

  public int getProfessorId() {
    return professorId;
  }

  public void setProfessorId(int professorId) {
    if (professorId < 0) {
      throw new IllegalArgumentException("ID do professor é inválido.");
    }
    this.professorId = professorId;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    if (this.cpf != null && !this.cpf.isBlank()) {
      throw new IllegalArgumentException("CPF não pode ser alterado após ser definido.");
    }

    String cpfTratado = cpf.trim().replaceAll("\\D", "");

    if (!ValidaCPF.isValido(cpfTratado)) {
      throw new IllegalArgumentException("CPF inválido.");
    }
    this.cpf = cpfTratado;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public void setSenha(String senha) {
    ValidaSenha.validar(senha);
    this.senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
  }

  public void setSenhaHash(String senhaHash) {
    if (senhaHash == null || senhaHash.isBlank()) {
      throw new IllegalArgumentException("Hash da senha é obrigatório.");
    }
    if (!senhaHash.matches("^\\$2[aby]\\$\\d{2}\\$.*$")) {
      throw new IllegalArgumentException("Hash BCrypt inválido.");
    }
    this.senhaHash = senhaHash;
  }

  public boolean validarSenha(String senhaInformada) {
    if (senhaInformada == null || senhaHash == null || senhaHash.isBlank()) {
      return false;
    }
    return BCrypt.checkpw(senhaInformada, senhaHash);
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }

  public LocalDate getDataCriacao() {
    return dataCriacao;
  }

  public void carregarDataCriacaoDoBanco(LocalDate dataCriacao) {
    if (dataCriacao == null) {
      throw new IllegalArgumentException("Campo data de criação é obrigatório.");
    }
    if (dataCriacao.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Data de criação não pode ser futura.");
    }
    this.dataCriacao = dataCriacao;
  }

  public LocalDateTime getUltimoLogin() {
    return ultimoLogin;
  }

  public void setUltimoLogin(LocalDateTime ultimoLogin) {
    if (ultimoLogin != null && ultimoLogin.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("Último login não pode ser futuro.");
    }
    this.ultimoLogin = ultimoLogin;
  }

  public TipoUsuario getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(TipoUsuario tipoUsuario) {
    if (tipoUsuario == null) {
      throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
    }
    this.tipoUsuario = tipoUsuario;
  }
}
