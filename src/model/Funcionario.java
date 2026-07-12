// Arthur e Guilherme

package model;

import java.time.LocalDate;

import util.ValidaCPF;
import variaveisEnum.Perfil;
import variaveisEnum.Permissao;
import variaveisEnum.Sexo;

public class Funcionario {
  private int idFuncionario;
  private String nome, cpf, cargo, telefone, rg, setor, email;
  private boolean ativo;
  private LocalDate dataNascimento, dataContratacao;
  private Endereco endereco;
  private Sexo sexo;
  private Perfil perfil;

  public int getIdFuncionario() {
    return idFuncionario;
  }

  public void setIdFuncionario(int idFuncionario) {
    if (idFuncionario <= 0) {
      throw new IllegalArgumentException("ID do funcionário é inválido.");
    }
    this.idFuncionario = idFuncionario;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    if (nome == null || nome.trim().isEmpty())
      throw new IllegalArgumentException("Campo nome é obrigatório.");
    this.nome = nome;
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

  public String getCargo() {
    return cargo;
  }

  public void setCargo(String cargo) {
    if (cargo == null || cargo.trim().isEmpty())
      throw new IllegalArgumentException("Campo cargo é obrigatório.");
    this.cargo = cargo;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    if (telefone == null || telefone.trim().isEmpty())
      throw new IllegalArgumentException("Campo telefone é obrigatório.");
    this.telefone = telefone;
  }

  public String getRg() {
    return rg;
  }

  public void setRg(String rg) {
    this.rg = rg;
  }

  public Sexo getSexo() {
    return sexo;
  }

  public void setSexo(Sexo sexo) {
    if (sexo == null) throw new IllegalArgumentException("Campo sexo é obrigatório.");
    this.sexo = sexo;
  }

  public String getSetor() {
    return setor;
  }

  public void setSetor(String setor) {
    if (setor == null || setor.trim().isEmpty()) {
      throw new IllegalArgumentException("Campo setir é obrigatório.");
    }
    this.setor = setor;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }

  public LocalDate getDataNascimento() {
    return dataNascimento;
  }

  public void setDataNascimento(LocalDate dataNascimento) {
    if (dataNascimento == null)
      throw new IllegalArgumentException("Campo data de nascimento é obrigatório.");
    this.dataNascimento = dataNascimento;
  }

  public LocalDate getDataContratacao() {
    return dataContratacao;
  }

  public void setDataContratacao(LocalDate dataContratacao) {
    if (dataContratacao == null)
      throw new IllegalArgumentException("Campo data de contratação é obrigatório.");
    this.dataContratacao = dataContratacao;
  }

  public Endereco getEndereco() {
    return endereco;
  }

  public void setEndereco(Endereco endereco) {
    if (endereco == null) throw new IllegalArgumentException("Campo endereço é obrigatório.");
    this.endereco = endereco;
  }

  public Perfil getPerfil() {
    return perfil;
  }

  public void setPerfil(Perfil perfil) {
    if (perfil == null) throw new IllegalArgumentException("Perfil é obrigatório.");
    this.perfil = perfil;
  }

  public boolean temPermissao(Permissao permissao) {
    if (perfil == null) return false;
    return perfil.getPermissoes().contains(permissao);
  }
}
