// Guilherme

package model;

import util.ValidaCEP;
import util.ValidaCidade;
import variaveisEnum.Estado;

public class Endereco {
  private int idEndereco;
  private String rua, numero, complemento, bairro, cidade, cep;
  private Estado estado;

  public int getIdEndereco() {
    return idEndereco;
  }

  public void setIdEndereco(int idEndereco) {
    if (idEndereco <= 0) {
      throw new IllegalArgumentException("ID do endereço é inválido.");
    }
    this.idEndereco = idEndereco;
  }

  public String getRua() {
    return rua;
  }

  public void setRua(String rua) {
    if (rua == null || rua.trim().isEmpty())
      throw new IllegalArgumentException("Campo rua é obrigatório.");
    this.rua = rua;
  }

  public String getNumero() {
    return numero;
  }

  public void setNumero(String numero) {
    this.numero = numero;
  }

  public String getComplemento() {
    return complemento;
  }

  public void setComplemento(String complemento) {
    this.complemento = complemento;
  }

  public String getBairro() {
    return bairro;
  }

  public void setBairro(String bairro) {
    if (bairro == null || bairro.trim().isEmpty())
      throw new IllegalArgumentException("Campo bairro é obrigatório.");
    this.bairro = bairro;
  }

  public String getCidade() {
    return cidade;
  }

  public void setCidade(String cidade) {
    if (!ValidaCidade.isValido(cidade))
      throw new IllegalArgumentException("Campo cidade é obrigatório.");
    this.cidade = cidade;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado estado) {
    if (estado == null) throw new IllegalArgumentException("Campo estado é obrigatório.");
    this.estado = estado;
  }

  public String getCep() {
    return cep;
  }

  public void setCep(String cep) {
    if (!ValidaCEP.isValido(cep)) throw new IllegalArgumentException("Campo cep é obrigatório.");
    this.cep = cep;
  }
}
