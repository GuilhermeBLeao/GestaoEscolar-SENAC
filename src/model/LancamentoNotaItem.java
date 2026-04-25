/*Guilherme e Igor
 Esta classe representa uma nota dentro da turma */

package model;

public class LancamentoNotaItem {
	private int alunoId;
	private String nomeAluno;
	private double nota;

	public LancamentoNotaItem(int alunoId, String nomeAluno) {
		this.alunoId = alunoId;
		this.nomeAluno = nomeAluno;
	}

	public int getAlunoId() {return alunoId;}
	public void setAlunoId(int alunoId) {	this.alunoId = alunoId;}
	public String getNomeAluno() {	return nomeAluno;}
	public void setNomeAluno(String nomeAluno) {this.nomeAluno = nomeAluno;}
	public double getNota() {return nota;}
	public void setNota(double nota) {
		if(nota < 0 || nota > 10)
			throw new IllegalArgumentException("Nota deve estrar entre 0 e 10.");
		this.nota = nota;
		}
}