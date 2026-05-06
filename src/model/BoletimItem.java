//Guilherme

package model;

public class BoletimItem {
	private int idDisciplina;
	private String nomeDisciplina, situacao;
	private double media;
	
	public int getIdDisciplina() {return idDisciplina;}
	public void setIdDisciplina(int idDisciplina) {this.idDisciplina = idDisciplina;}
	public String getNomeDisciplina() {return nomeDisciplina;}
	public void setNomeDisciplina(String nomeDisciplina) {this.nomeDisciplina = nomeDisciplina;}
	public String getSituacao() {return situacao;}
	public void setSituacao(String situacao) {this.situacao = situacao;}
	public double getMedia() {return media;}
	public void setMedia(double media) {
		this.media = media;
		if(media >= 6.0) {
			this.situacao = "APROVADO";
		}else {
			this.situacao = "REPROVADO";
		}
	}
}