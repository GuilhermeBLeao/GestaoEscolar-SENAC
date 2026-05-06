//Márcio - Editado por Guilherme

package model;

import java.time.LocalDate;

import util.ValidaCPF;
import variaveisEnum.Sexo;
import variaveisEnum.SituacaoAluno;

public class Aluno {
    private int idAluno, idPais, idTurma;
    private String nome, email, telefone, cpf, rg, obsSaude, matricula;
    private LocalDate dataNascimento, dataCadastro;
    private Endereco endereco;
    private boolean ativo;
    private Sexo sexo;
    private SituacaoAluno situacao;

    public Aluno() {}

    public Aluno(String matricula) {
        setMatricula(matricula);
    }

    public int getIdAluno() { return idAluno; }
    public void setIdAluno(int idAluno) {
    	if(idAluno <= 0) {
    		throw new IllegalArgumentException("ID do aluno é inválido.");
    	}
    	this.idAluno = idAluno; 
    	}
    public int getIdPais() { return idPais; }
    public void setIdPais(int idPais) { 
    	if(idPais <= 0) {
    		throw new IllegalArgumentException("ID dos pais/responsáveis é inválido.");
    	}
    	this.idPais = idPais; 
    	}
    public int getIdTurma() { return idTurma; }
    public void setIdTurma(int idTurma) { 
    	if(idTurma <= 0) {
    		throw new IllegalArgumentException("ID da turma é inválido.");
    	}
    	this.idTurma = idTurma;
    	}
    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Campo nome é obrigatório.");
        this.nome = nome;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Campo email é obrigatório.");
        this.email = email;
    }
    public SituacaoAluno getSituacao() { return situacao; }
    public void setSituacao(SituacaoAluno situacao) {
        if (situacao == null)
            throw new IllegalArgumentException("Campo situação é obrigatório.");
        this.situacao = situacao;
    }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) {
        if (sexo == null)
            throw new IllegalArgumentException("Campo sexo é obrigatório.");
        this.sexo = sexo;
    }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty())
            throw new IllegalArgumentException("Campo telefone é obrigatório.");
        this.telefone = telefone;
    }
    public String getCpf() { return cpf; }
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
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null)
            throw new IllegalArgumentException("Campo data de nascimento é obrigatório.");
        this.dataNascimento = dataNascimento;
    }
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) {
        if (dataCadastro == null)
            throw new IllegalArgumentException("Campo data de cadastro é obrigatório.");
        this.dataCadastro = dataCadastro;
    }
    public String getMatricula() { return matricula; }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty())
            throw new IllegalArgumentException("Campo matrícula é obrigatório.");
        String matriculaTratada = matricula.trim();

        if (!matriculaTratada.matches("\\d{10}"))
            throw new IllegalArgumentException("Matrícula deve conter exatamente 10 números.");

        if (this.matricula != null && !this.matricula.trim().isEmpty())
            throw new IllegalStateException("Matrícula já foi definida.");

        this.matricula = matriculaTratada;
    }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getObsSaude() { return obsSaude; }
    public void setObsSaude(String obsSaude) { this.obsSaude = obsSaude; }
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) {
        if (endereco == null)
            throw new IllegalArgumentException("Campo endereço é obrigatório.");
        this.endereco = endereco;
    }
	public boolean isAtivo() {return ativo;}
	public void setAtivo(boolean ativo) {this.ativo = ativo;}
}