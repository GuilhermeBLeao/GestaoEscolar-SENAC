//Luiz

package dao;

import java.util.ArrayList;
import java.util.List;
import model.Disciplina;

public class DisciplinaDAO {
	private List<Disciplina> lista = new ArrayList<>();
	private int contadorId = 1;
	
	public void salvar(Disciplina d) {
		d.setIdDisciplina(contadorId++);
		lista.add(d);
	}
	
	public List <Disciplina> listarTodas(){
		return lista;
	}
	
	public Disciplina buscarPorId(int id) {
		for(Disciplina d: lista) {
			if(d.getIdDisciplina() == id) {
				return d;
			}
		}
		return null;
	}
	
	public void atualizar(Disciplina disciplinaAtualizada) {
		for(int i = 0; i < lista.size(); i++) {
			Disciplina d = lista.get(i);
			
			if(d.getIdDisciplina() == disciplinaAtualizada.getIdDisciplina()) {
				lista.set(i,  disciplinaAtualizada);
				return;
			}
		}
	}
	
	public void deletar(int id) {
		for(int i  = 0; i < lista.size(); i++) {
			if(lista.get(i).getIdDisciplina() == id) {
				lista.remove(i);
				return;
			}
		}
	}
}