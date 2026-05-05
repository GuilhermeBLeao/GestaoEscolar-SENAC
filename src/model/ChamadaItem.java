/*Guilherme
Esta classe representa um aluno dentro da chamada*/
package model;

public class ChamadaItem {
	private int alunoId;
	private String nomeAluno;
	private boolean presente;
	
	public ChamadaItem(int alunoId, String nomeAluno) {
		this.alunoId = alunoId;
		this.nomeAluno = nomeAluno;
		//Por padrão todo aluno começa com presença
		this.presente = true;
	}
	
	public int getAlunoId() {return alunoId;}
	public void setAlunoId(int alunoId) {this.alunoId = alunoId;}
	public String getNomeAluno() {	return nomeAluno;}
	public void setNomeAluno(String nomeAluno) {this.nomeAluno = nomeAluno;}
	public boolean isPresente() {return presente;}
	public void setPresente(boolean presente) {this.presente = presente;}
}
