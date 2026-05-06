//Guilherme

package model;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

public class Boletim {
	private int idAluno, idTurma;
	private String nomeAluno, matricula;
	private LocalDate datageracao;
	private List<BoletimItem> itens = new ArrayList<>();
	
	public int getIdAluno() {return idAluno;}
	public void setIdAluno(int idAluno) {this.idAluno = idAluno;}
	public int getIdTurma() {return idTurma;}
	public void setIdTurma(int idTurma) {this.idTurma = idTurma;}
	public String getNomeAluno() {return nomeAluno;}
	public void setNomeAluno(String nomeAluno) {this.nomeAluno = nomeAluno;}
	public String getMatricula() {return matricula;}
	public void setMatricula(String matricula) {this.matricula = matricula;}
	public LocalDate getDatageracao() {return datageracao;}
	public void setDatageracao(LocalDate datageracao) {this.datageracao = datageracao;}
	public List<BoletimItem> getItens() {return itens;}
	public void setItens(List<BoletimItem> itens) {this.itens = itens;}
	public void adicionarItem(BoletimItem item) {this.itens.add(item);}
}