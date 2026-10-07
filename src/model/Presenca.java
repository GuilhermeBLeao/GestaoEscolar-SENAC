//Guilherme

package model;

import java.time.LocalDate;

public class Presenca {
	private LocalDate data;
	private String motivoAbonada;
	private int idPresenca, disciplinaId, alunoId;
	private boolean presente, faltaAbonada, faltaJustificada;

	public int getIdPresenca() {
		return idPresenca;
	}

	public void setIdPresenca(int idPresenca) {
		if (idPresenca <= 0) {
			throw new IllegalArgumentException("ID da presença é inválido.");
		}
		this.idPresenca = idPresenca;
	}

	public int getDisciplinaId() {
		return disciplinaId;
	}

	public void setDisciplinaId(int disciplinaId) {
		if (disciplinaId <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		this.disciplinaId = disciplinaId;
	}

	public int getAlunoId() {
		return alunoId;
	}

	public void setAlunoId(int alunoId) {
		if (alunoId <= 0) {
			throw new IllegalArgumentException("ID do aluno é inválido.");
		}
		this.alunoId = alunoId;
	}

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		if (data == null)
			throw new IllegalArgumentException("Campo data é obrigatório");
		else if (data.isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data não pode ser futura.");
		this.data = data;
	}

	public boolean isPresente() {
		return presente;
	}

	public void setPresente(boolean presente) {
		this.presente = presente;
	}

	public boolean isFaltaAbonada() {
		return faltaAbonada;
	}

	public void setFaltaAbonada(boolean faltaAbonada) {
		this.faltaAbonada = faltaAbonada;
	}

	public boolean isFaltaJustificada() {
		return faltaJustificada;
	}

	public void setFaltaJustificada(boolean faltaJustificada) {
		this.faltaJustificada = faltaJustificada;
	}

	public String getMotivoAbonada() {
		return motivoAbonada;
	}

	public void setMotivoAbonada(String motivoAbonada) {
		this.motivoAbonada = motivoAbonada;
	}
}