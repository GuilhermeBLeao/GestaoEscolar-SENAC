//Márcio e Guilherme

package model;

import java.time.LocalDate;

import variaveisEnum.SexoEnum;
import variaveisEnum.SituacaoAluno;

public class Aluno{
	private int idAluno, idPais, idTurma;
	private String nome, email, telefone, cpf, rg, obsSaude, matricula;
	private LocalDate dataNascimento, dataCadastro;
	private Endereco endereco;
	private SexoEnum sexo;
	private SituacaoAluno situacao;
	
	public Aluno() {}
	public Aluno(String matricula) {this.matricula = matricula;}
	public int getIdAluno() {	return idAluno;	}
	public void setIdAluno(int idAluno) {this.idAluno = idAluno;}
	public int getIdPais() {return idPais;}
	public void setIdPais(int idPais) {this.idPais = idPais;}
	public int getIdTurma() {return idTurma;}
	public void setIdTurma(int idTurma) {this.idTurma = idTurma;}
	public String getNome() {return nome;}
	public void setNome(String nome) {
		if(nome == null || nome.trim().isEmpty())
			throw new IllegalArgumentException("Campo nome é obrigatório.");
		this.nome = nome;
		}
	public String getEmail() {return email;}
	public void setEmail(String email) {
		if(email == null || email.trim().isEmpty())
			throw new IllegalArgumentException("Campo email é obrigatório.");
		this.email = email;
		}
	public SituacaoAluno getSituacao() {return situacao;}
	public void setSituacao(SituacaoAluno situacao) {
		if(situacao == null)
			throw new IllegalArgumentException("Campo situação é obritório.");
		this.situacao = situacao;
	}
	public SexoEnum getSexo() {return sexo;}
	public void setSexo(SexoEnum sexo) {
	    if (sexo == null)
	        throw new IllegalArgumentException("Campo sexo é obrigatório.");
	    this.sexo = sexo;
	}
	public String getTelefone() {return telefone;}
	public void setTelefone(String telefone) {
		if(telefone == null || telefone.trim().isEmpty())
			throw new IllegalArgumentException("Campo telefone é obrigatório.");
		this.telefone = telefone;
		}
	public String getCpf() {return cpf;}
	public void setCpf(String cpf) {
		if(cpf == null || cpf.trim().isEmpty())
			throw new IllegalArgumentException("Campo cpf é obrigatório.");
		this.cpf = cpf;
		}
	public LocalDate getDataNascimento() {return dataNascimento;}
	public void setDataNascimento(LocalDate dataNascimento) {
		if(dataNascimento == null)
			throw new IllegalArgumentException("Campo data de nascimento é obrigatório.");
		this.dataNascimento = dataNascimento;
		}
	public LocalDate getDataCadastro() {return dataCadastro;}
	public void setDataCadastro(LocalDate dataCadastro) {
		if(dataCadastro == null)
			throw new IllegalArgumentException("Campo data de cadastro é obrigatório.");
		this.dataCadastro = dataCadastro;
		}
	public String getMatricula() {return matricula;}
	public String getRg() {return rg;}
	public void setRg(String rg) {this.rg = rg;}
	public String getObsSaude() {return obsSaude;}
	public void setObsSaude(String obsSaude) {this.obsSaude = obsSaude;}
	public void setMatricula(String matricula) {
	    if (matricula == null || matricula.trim().isEmpty())
	        throw new IllegalArgumentException("Campo matrícula é obrigatório.");
	    if (this.matricula != null && !this.matricula.trim().isEmpty())
	        throw new IllegalStateException("Matrícula já foi definida.");
	    this.matricula = matricula;
	}
	public Endereco getEndereco() {return endereco;}
	public void setEndereco(Endereco endereco) {
		if(endereco == null)
			throw new IllegalArgumentException("Campo endereço é obrigatório.");
		this.endereco = endereco;
	}
}