//Guilherme

package model;

import java.time.LocalDate;

import org.mindrot.jbcrypt.BCrypt;

import variaveisEnum.TipoUsuario;
import util.ValidaCPF;

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
    public void setProfessorId(int professorId) { this.professorId = professorId;}
    public String getCpf() {return cpf;}
    public void setCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) 
            throw new IllegalArgumentException("Campo CPF é obrigatório.");
        String cpfTratado = cpf.trim();
        if (!ValidaCPF.isValido(cpfTratado)) 
            throw new IllegalArgumentException("CPF inválido.");
        this.cpf = cpfTratado.replaceAll("\\D", "");
    }
    public String getSenhaHash() {return senhaHash;}
    public void setSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo senha é obrigatório.");
        }
        this.senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
    }
    public boolean validarSenha(String senhaInformada) {
        if (senhaInformada == null || this.senhaHash == null || this.senhaHash.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(senhaInformada, this.senhaHash);
    }
    public TipoUsuario getTipoUsuario() {return tipoUsuario;}
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        if (tipoUsuario == null) {
            throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
        }
        this.tipoUsuario = tipoUsuario;
        }
    public void setSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Hash da senha é obrigatório.");
        }
        this.senhaHash = senhaHash;
    }
    public boolean isAtivo() { return ativo;}
    public void setAtivo(boolean ativo) {this.ativo = ativo;}
    public LocalDate getDataCriacao() {return dataCriacao;}
    public void setDataCriacao(LocalDate dataCriacao) {
        if (dataCriacao == null) {
            throw new IllegalArgumentException("Campo data de criação é obrigatório.");
        }
        this.dataCriacao = dataCriacao;
        }
    public LocalDate getUltimoLogin() {return ultimoLogin;}
    public void setUltimoLogin(LocalDate ultimoLogin) {this.ultimoLogin = ultimoLogin;}
}