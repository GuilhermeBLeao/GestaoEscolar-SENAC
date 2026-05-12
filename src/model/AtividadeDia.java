//Arthur, José e Guilherme

package model;

import java.time.LocalDate;

public class AtividadeDia {
	private int idAtividade, professorId, turmaId, disciplinaId;
	private LocalDate data;
	private String descricao;

	public int getIdAtividade() {
		return idAtividade;
	}

	public void setIdAtividade(int idAtividade) {
		if (idAtividade <= 0) {
			throw new IllegalArgumentException("ID da atividade é inválido.");
		}
		this.idAtividade = idAtividade;
	}

	public int getProfessorId() {
		return professorId;
	}

	public void setProfessorId(int professorId) {
		if (professorId <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}
		this.professorId = professorId;
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

	public int getDisciplinaId() {
		return disciplinaId;
	}

	public void setDisciplinaId(int disciplinaId) {
		if (disciplinaId <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		this.disciplinaId = disciplinaId;
	}

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		if (data == null) {
			throw new IllegalArgumentException("Campo data é obrigatório.");
		}
		this.data = data;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		if (descricao == null || descricao.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo descrição é obrigatório.");
		}
		this.descricao = descricao;
	}
}