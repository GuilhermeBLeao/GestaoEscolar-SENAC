package dto;

public class UsuarioConsultaDTO {

	private int idUsuario;
	private String nome;
	private String cpf;
	private String tipoUsuario;

	public int getIdUsuario() {return idUsuario;}
	public void setIdUsuario(int idUsuario) {this.idUsuario = idUsuario;}
	public String getNome() {return nome;}
	public void setNome(String nome) {this.nome = nome;}
	public String getCpf() {return cpf;}
	public void setCpf(String cpf) {this.cpf = cpf;}
	public String getTipoUsuario() {return tipoUsuario;}
	public void setTipoUsuario(String tipoUsuario) {this.tipoUsuario = tipoUsuario;}
}