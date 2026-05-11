//Luiz e Guilherme

package model;

import variaveisEnum.Turno;

public class Turma {
	private int idTurma, salaId;
	private String descricaoTurma;
	private Turno turno;
	private boolean ativo;

	public int getIdTurma() {
		return idTurma;
	}

	public void setIdTurma(int idTurma) {
		if (idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma é inválido.");
		}
		this.idTurma = idTurma;
	}

	public int getSalaId() {
		return salaId;
	}

	public void setSalaId(int salaId) {
		if (salaId <= 0)
			throw new IllegalArgumentException("ID da sala é inválido.");
		this.salaId = salaId;
	}

	public String getDescricaoTurma() {
		return descricaoTurma;
	}

	public void setDescricaoTurma(String descricaoTurma) {
		if (descricaoTurma == null || descricaoTurma.trim().isEmpty())
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

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}