/*Guilherme e Igor
 Esta classe representa o lançamento de notas como um todo */

package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LancamentoNota {
	private int turmaId, disciplinaId;
	private String atividade;
	private LocalDate dataLancamento;
	private List<LancamentoNotaItem> itens = new ArrayList<>();
	
	public int getTurmaId() {return turmaId;}
	public void setTurmaId(int turmaId) {this.turmaId = turmaId;	}
	public LocalDate getDataLancamento() {return dataLancamento;}
	public void setDataLancamento(LocalDate dataLancamento) {
	    if (dataLancamento == null) {
	        throw new IllegalArgumentException("Campo data de lançamento é obrigatória.");
	    }
	    this.dataLancamento = dataLancamento;
	}
	public int getDisciplinaId() {return disciplinaId;}
	public void setDisciplinaId(int disciplinaId) {this.disciplinaId = disciplinaId;}
	public String getAtividade() {	return atividade;}
	public void setAtividade(String atividade) {
		if(atividade == null || atividade.trim().isEmpty())
			throw new IllegalArgumentException("Atividade é obrigatória.");
		this.atividade = atividade;
		}
	public List<LancamentoNotaItem> getItens() {return itens;}
	public void setItens(List<LancamentoNotaItem> itens) {this.itens = itens == null ? new ArrayList<>() : itens;}
}