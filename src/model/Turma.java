//Luiz e Guilherme

package model;

import variaveisEnum.Turno;

public class Turma{
	private int idTurma, salaId;
	private String descricaoTurma;
	private Turno turno;
	
	public int getIdTurma() {return idTurma;}
	public void setIdTurma(int idTurma) {this.idTurma = idTurma;}
	public int getSalaId() {return salaId;}
	public void setSalaId(int salaId) {
		if(salaId <=0)
			throw new IllegalArgumentException("ID da sala não pode ser menor ou igual a 0.");
		this.salaId = salaId;
		}
	public String getDescricaoTurma() {return descricaoTurma;}
	public void setDescricaoTurma(String descricaoTurma) {
		if(descricaoTurma == null || descricaoTurma.trim().isEmpty())
		throw new IllegalArgumentException("Campo descrição é obrigatório.");
		this.descricaoTurma = descricaoTurma;
		}
	public Turno getTurno() {
		return turno;
	}
	public void setTurno(Turno turno) {
		if (turno == null)
	        throw new IllegalArgumentException("Campo turno é obrigatório.");
		this.turno = turno;
	}
}