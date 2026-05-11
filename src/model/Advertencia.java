//Guilherme

package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Advertencia {
	private int alunoId, turmaId, professorId, idAdvertencia;
	private String motivo, descricao;
	private LocalDate dataAdvertencia;
	private List<AdvertenciaItem> itens = new ArrayList<>();

	public int getIdAdvertencia() {
		return idAdvertencia;
	}

	public void setIdAdvertencia(int idAdvertencia) {
		if (idAdvertencia <= 0) {
			throw new IllegalArgumentException("ID da advertência é inválido.");
		}
		this.idAdvertencia = idAdvertencia;
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

	public int getTurmaId() {
		return turmaId;
	}

	public void setTurmaId(int turmaId) {
		if (turmaId <= 0) {
			throw new IllegalArgumentException("ID da turma é inválido.");
		}
		this.turmaId = turmaId;
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

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		if (motivo == null || motivo.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo motivo é obrigatório.");
		}
		this.motivo = motivo;
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

	public LocalDate getDataAdvertencia() {
		return dataAdvertencia;
	}

	public void setDataAdvertencia(LocalDate dataAdvertencia) {
		if (dataAdvertencia == null) {
			throw new IllegalArgumentException("Campo data da advertência é obrigatório.");
		}
		this.dataAdvertencia = dataAdvertencia;
	}

	public List<AdvertenciaItem> getItens() {
		return itens;
	}

	public void setItens(List<AdvertenciaItem> itens) {
		this.itens = itens == null ? new ArrayList<>() : itens;
	}
}