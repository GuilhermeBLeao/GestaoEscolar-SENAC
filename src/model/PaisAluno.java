//Luiz

package model;

public class PaisAluno{
	private int idPais;
	private boolean ativo;
	private String nomeMae, nomePai, emailMae, emailPai, telefoneMae, telefonePai, cpfMae, cpfPai;
	
	public int getIdPais() {return idPais;}
	public void setIdPais(int idPais) {
		if(idPais <= 0) {
			throw new IllegalArgumentException("ID dos pais/responsáveis é inválido.");
		}
		this.idPais = idPais;
		}
	public String getNomeMae() {return nomeMae;}
	public void setNomeMae(String nomeMae) {this.nomeMae = nomeMae;}
	public String getNomePai() {return nomePai;}
	public void setNomePai(String nomePai) {this.nomePai = nomePai;}
	public String getEmailMae() {return emailMae;}
	public void setEmailMae(String emailMae) {this.emailMae = emailMae;}
	public String getEmailPai() {return emailPai;}
	public void setEmailPai(String emailPai) {this.emailPai = emailPai;}
	public String getTelefoneMae() {return telefoneMae;}
	public void setTelefoneMae(String telefoneMae) {this.telefoneMae = telefoneMae;}
	public String getTelefonePai() {return telefonePai;}
	public void setTelefonePai(String telefonePai) {this.telefonePai = telefonePai;}
	public String getCpfMae() {return cpfMae;}
	public void setCpfMae(String cpfMae) {this.cpfMae = cpfMae;}
	public String getCpfPai() {return cpfPai;}
	public void setCpfPai(String cpfPai) {this.cpfPai = cpfPai;}
	public boolean isAtivo() {return ativo;}
	public void setAtivo(boolean ativo) {this.ativo = ativo;}
}