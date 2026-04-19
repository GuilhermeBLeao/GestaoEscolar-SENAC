//Guilherme

package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

import variaveisEnum.TipoUsuario;

public class Usuario {
    private int idUsuario, alunoId, funcionarioId, paiId, professorId;
    private String cpf, senhaHash;
    private boolean ativo;
    private LocalDate dataCriacao, ultimoLogin;
    private TipoUsuario tipoUsuario;

    public int getIdUsuario() {return idUsuario;}
    public void setIdUsuario(int idUsuario) {this.idUsuario = idUsuario;}
    public int getAlunoId() {return alunoId;}
    public void setAlunoId(int alunoId) {this.alunoId = alunoId;}
    public int getFuncionarioId() {return funcionarioId;}
    public void setFuncionarioId(int funcionarioId) {this.funcionarioId = funcionarioId;}
    public int getPaiId() {return paiId;}
    public void setPaiId(int paiId) {this.paiId = paiId;}
    public int getProfessorId() {return professorId;}
    public void setProfessorId(int professorId) {this.professorId = professorId;}
    public String getCpf() {return cpf;}
    public void setCpf(String cpf) {
    	if (cpf == null || cpf.trim().isEmpty())
            throw new IllegalArgumentException("Campo CPF é obrigatório.");
        this.cpf = cpf;
    }
    public String getSenhaHash() {return senhaHash;}
    public void setSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) 
            throw new IllegalArgumentException("Campo senha é obrigatório.");
        this.senhaHash = gerarHash(senha);
    }
    public boolean validarSenha(String senhaInformada) {
        if (senhaInformada == null) 
        	return false;
        return gerarHash(senhaInformada).equals(this.senhaHash);
    }
   
    public TipoUsuario getTipoUsuario() {
		return tipoUsuario;
	}
	public void setTipoUsuario(TipoUsuario tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}
	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}
	public boolean isAtivo() {return ativo;}
    public void setAtivo(boolean ativo) {this.ativo = ativo;}
    public LocalDate getDataCriacao() {return dataCriacao;}
    public void setDataCriacao(LocalDate dataCriacao) {
        if (dataCriacao == null)
            throw new IllegalArgumentException("Campo data de criação é obrigatório.");
        this.dataCriacao = dataCriacao;
    }
    public LocalDate getUltimoLogin() {return ultimoLogin;}
    public void setUltimoLogin(LocalDate ultimoLogin) {this.ultimoLogin = ultimoLogin;}
    private String gerarHash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha.", e);
        }
    }
}