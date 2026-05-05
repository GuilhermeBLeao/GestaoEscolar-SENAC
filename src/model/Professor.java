//Guilherme

package model;

import java.time.LocalDate;

import util.ValidaCPF;
import variaveisEnum.Sexo;

public class Professor{
	private int idProfessor;
	private String nome, cpf, formacao, telefone, rg;
	private LocalDate dataNascimento;
	private Endereco endereco;
	private Sexo sexo;
	private boolean ativo;
	
	public int getIdProfessor() {return idProfessor;}
	public void setIdProfessor(int idProfessor) {
		if(idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}
		this.idProfessor = idProfessor;
		}
	public String getTelefone() {return telefone;}
	public void setTelefone(String telefone) {this.telefone = telefone;}
	public String getRg() {return rg;}
	public void setRg(String rg) {this.rg = rg;}
	public LocalDate getDataNascimento() {return dataNascimento;}
	public void setDataNascimento(LocalDate dataNascimento) {
		if(dataNascimento == null)
    		throw new IllegalArgumentException("Campo data de nascimento é obrigatório.");
		this.dataNascimento = dataNascimento;
		}
	public String getNome() {return nome;}
	public void setNome(String nome) {
		if(nome == null || nome.trim().isEmpty())
    		throw new IllegalArgumentException("Campo nome é obrigatório.");
		this.nome = nome;
		}
	public String getCpf() {return cpf;}
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
	public String getFormacao() {return formacao;}
	public void setFormacao(String formacao) {
		if(formacao == null || formacao.trim().isEmpty())
    		throw new IllegalArgumentException("Campo formação é obrigatório.");
		this.formacao = formacao;
		}
	public Endereco getEndereco() {return endereco;}
	public void setEndereco(Endereco endereco) {
		if(endereco == null)
			throw new IllegalArgumentException("Campo endereço é obrigatório.");
		this.endereco = endereco;
	}
	public Sexo getSexo() {return sexo;}
	public void setSexo(Sexo sexo) {
		if(sexo == null) {
			throw new IllegalArgumentException("Campo sexo é obrigatório.");
		}
		this.sexo = sexo;
	}
	public boolean isAtivo() {return ativo;}
	public void setAtivo(boolean ativo) {this.ativo = ativo;}
}