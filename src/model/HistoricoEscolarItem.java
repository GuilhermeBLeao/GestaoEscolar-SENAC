//Guilherme

package model;

public class HistoricoEscolarItem {

	private String disciplina, situacao;
	private double mediaFinal, frequencia;
	private int totalAulas, faltas;

	public String getDisciplina() {
		return disciplina;
	}

	public void setDisciplina(String disciplina) {
		if (disciplina == null || disciplina.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo disciplina é obrigatório.");
		}
		this.disciplina = disciplina.trim();
	}

	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		if (situacao == null || situacao.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo situação é obrigatório.");
		}
		this.situacao = situacao.trim();
	}

	public double getMediaFinal() {
		return mediaFinal;
	}

	public void setMediaFinal(double mediaFinal) {
		if (mediaFinal < 0 || mediaFinal > 10) {
			throw new IllegalArgumentException("Média final inválida.");
		}
		this.mediaFinal = mediaFinal;
	}

	public double getFrequencia() {
		return frequencia;
	}

	public void setFrequencia(double frequencia) {
		if (frequencia < 0 || frequencia > 100) {
			throw new IllegalArgumentException("Frequência inválida.");
		}
		this.frequencia = frequencia;
	}

	public int getTotalAulas() {
		return totalAulas;
	}

	public void setTotalAulas(int totalAulas) {
		if (totalAulas < 0) {
			throw new IllegalArgumentException("Total de aulas inválido.");
		}
		this.totalAulas = totalAulas;
	}

	public int getFaltas() {
		return faltas;
	}

	public void setFaltas(int faltas) {
		if (faltas < 0) {
			throw new IllegalArgumentException("Número de faltas inválido.");
		}
		this.faltas = faltas;
	}
}