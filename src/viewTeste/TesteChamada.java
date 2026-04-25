//Guilherme

package viewTeste;

import controller.ChamadaController;
import model.Chamada;

public class TesteChamada {
	public static void main(String[] args) {
		ChamadaController chamadaController = new ChamadaController();
		
		int idProfessor = 1, idTurma = 1, idDisciplina = 1;
		
		Chamada chamada = chamadaController.prepararChamada(idProfessor, idTurma, idDisciplina);
		
		chamada.getItens().get(0).setPresente(true);
		chamada.getItens().get(1).setPresente(false);
		
		chamadaController.salvarChamada(chamada);
		
		System.out.println("Chamada salva com sucesso!");
	}
}
