//José

package model;

import java.time.LocalDate;

public class Nota{
	private int notasId, idDisciplina, idAluno;
	private String atividade;
	private double nota;
	private LocalDate dataLancamento;
	
	public int getNotasId() {return notasId;}
	public void setNotasId(int notasId) {
		if(notasId <= 0) {
			throw new IllegalArgumentException("ID de notas é inválido.");
		}
		this.notasId = notasId;
		}
	public int getIdDisciplina() {return idDisciplina;}
	public void setIdDisciplina(int idDisciplina) {
		if(idDisciplina <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		this.idDisciplina = idDisciplina;
		}
	public int getIdAluno() {return idAluno;}
	public void setIdAluno(int idAluno) {
		if(idAluno <= 0) {
			throw new IllegalArgumentException("ID do aluno é inválido.");
		}
		this.idAluno = idAluno;
		}
	public String getAtividade() {return atividade;}
	public void setAtividade(String atividade) {
		if(atividade == null || atividade.trim().isEmpty())
			throw new IllegalArgumentException("Campo atividade é obrigatório.");
		this.atividade = atividade;
		}
	public double getNota() {return nota;}
	public void setNota(double nota) {
		if(nota < 0)
			throw new IllegalArgumentException("Campo nota é obrigatório.");
		else if(nota > 10)
			throw new IllegalArgumentException("Nota inválida, digite uma nota de 0,0 a 10,0");
		this.nota = nota;
		}
	public LocalDate getDataLancamento() {return dataLancamento;}
	public void setDataLancamento(LocalDate dataLancamento) {
		if(dataLancamento == null) {
			throw new IllegalArgumentException("Campo data de lançamento é obrigatório.");
		}
		else if(dataLancamento.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Data de lançamento não pode ser futura.");
		}
		this.dataLancamento = dataLancamento;}
}