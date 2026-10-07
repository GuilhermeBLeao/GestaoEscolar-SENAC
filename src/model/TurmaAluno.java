// Guilherme

package model;

import java.time.LocalDate;

public class TurmaAluno {
	private int idTurmaAluno;
	private int turmaId;
	private int alunoId;
	private LocalDate dataEntrada;
	private LocalDate dataSaida;
	private boolean ativo;

	public int getIdTurmaAluno() {
		return idTurmaAluno;
	}

	public void setIdTurmaAluno(int idTurmaAluno) {
		if (idTurmaAluno <= 0) {
			throw new IllegalArgumentException("ID do histórico de turma do aluno é inválido.");
		}
		this.idTurmaAluno = idTurmaAluno;
	}

	public int getTurmaId() {
		return turmaId;
	}

	public void setTurmaId(int turmaId) {
		if (turmaId <= 0) {
			throw new IllegalArgumentException("ID da turma é inválido.");
		}
		this.turmaId = turmaId;
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

	public LocalDate getDataEntrada() {
		return dataEntrada;
	}

	public void setDataEntrada(LocalDate dataEntrada) {
		if (dataEntrada == null) {
			throw new IllegalArgumentException("Data de entrada na turma é obrigatória.");
		}
		this.dataEntrada = dataEntrada;
	}

	public LocalDate getDataSaida() {
		return dataSaida;
	}

	public void setDataSaida(LocalDate dataSaida) {
		if (dataSaida != null && dataEntrada != null && dataSaida.isBefore(dataEntrada)) {
			throw new IllegalArgumentException("Data de saída não pode ser anterior à data de entrada.");
		}
		this.dataSaida = dataSaida;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}
