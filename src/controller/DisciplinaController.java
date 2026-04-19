//Luiz

package controller;

import dao.DisciplinaDAO;
import model.Disciplina;
import java.util.List;

public class DisciplinaController{
	private final DisciplinaDAO disciplinaDAO;
	
	public DisciplinaController(){
		this.disciplinaDAO = new DisciplinaDAO();
	}
	
	public void criar(Disciplina d) {
		disciplinaDAO.salvar(d);
	}
	
	public void atualizar(Disciplina d) {
		disciplinaDAO.atualizar(d);
	}
	
	public List<Disciplina> listar(){
		return disciplinaDAO.listarTodas();
	}
	
	public Disciplina buscar(int id) {
		return disciplinaDAO.buscarPorId(id);
	}
	
	public void deletar(int id) {
		disciplinaDAO.deletar(id);
	}
}