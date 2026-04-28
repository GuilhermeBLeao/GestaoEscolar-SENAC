/*Guilherme
 Esta classe representa a chamada como um todo */

package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Chamada {
	private int  professorId,turmaId,  disciplinaId;
	private LocalDate data;
	//Lista de presenças individuais
	private List<ChamadaItem> itens = new ArrayList<>();
	
	public int getProfessorId() {return professorId;}
	public void setProfessorId(int professorId) {this.professorId = professorId;	}
	public int getTurmaId() {return turmaId;}
	public void setTurmaId(int turmaId) {this.turmaId = turmaId;	}
	public int getDisciplinaId() {return disciplinaId;}
	public void setDisciplinaId(int disciplinaId) {this.disciplinaId = disciplinaId;}
	public LocalDate getData() {return data;}
	public void setData(LocalDate data) {
		if(data == null)
			throw new IllegalArgumentException("Data da chamada é obrigatória.");
		this.data = data;}
	public List<ChamadaItem> getItens() {return itens;}
	//Evitar NullPointerException
	public void setItens(List<ChamadaItem> itens) {this.itens = itens == null ? new ArrayList<>() : itens;}
}