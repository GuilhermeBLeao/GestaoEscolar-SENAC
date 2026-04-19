//Luiz

package model;

public class PaisAluno{
	private int idPais;
	private String nomeMae, nomePai, emailMae, emailPai, telefoneMae, telefonePai, cpfMae, cpfPai;
	
	public int getIdPais() {return idPais;}
	public void setIdPais(int idPais) {this.idPais = idPais;}
	public String getNomeMae() {return nomeMae;}
	public void setNomeMae(String nomeMae) {
		if(nomeMae == null || nomeMae.trim().isEmpty())
			throw new IllegalArgumentException("Campo nome da mãe é obrigatório.");
		this.nomeMae = nomeMae;
		}
	public String getNomePai() {return nomePai;}
	public void setNomePai(String nomePai) {this.nomePai = nomePai;}
	public String getEmailMae() {return emailMae;}
	public void setEmailMae(String emailMae) {
		if(emailMae == null || emailMae.trim().isEmpty())
			throw new IllegalArgumentException("Campo email da mãe é obrigatório.");
		this.emailMae = emailMae;
		}
	public String getEmailPai() {return emailPai;}
	public void setEmailPai(String emailPai) {this.emailPai = emailPai;}
	public String getTelefoneMae() {return telefoneMae;}
	public void setTelefoneMae(String telefoneMae) {
		if(telefoneMae == null || telefoneMae.trim().isEmpty())
			throw new IllegalArgumentException("Campo telefone da mãe é obrigatório.");
		this.telefoneMae = telefoneMae;}
	public String getTelefonePai() {return telefonePai;}
	public void setTelefonePai(String telefonePai) {this.telefonePai = telefonePai;}
	public String getCpfMae() {return cpfMae;}
	public void setCpfMae(String cpfMae) {
		if(cpfMae == null || cpfMae.trim().isEmpty())
			throw new IllegalArgumentException("Campo cpf da mãe é obrigatório.");
		this.cpfMae = cpfMae;}
	public String getCpfPai() {return cpfPai;}
	public void setCpfPai(String cpfPai) {this.cpfPai = cpfPai;}
}