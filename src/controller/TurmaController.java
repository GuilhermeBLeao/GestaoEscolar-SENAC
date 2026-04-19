//Igor

package controller;

import dao.TurmaDAO;
import model.Turma;
import variaveisEnum.Turno;

import java.util.List;

public class TurmaController {

    private final TurmaDAO turmaDAO;

    public TurmaController() {
        this.turmaDAO = new TurmaDAO();
    }

    public void salvarTurma(int salaId, String descricaoTurma, Turno turno) {
        Turma turma = new Turma();
        turma.setSalaId(salaId);
        turma.setDescricaoTurma(descricaoTurma);
        turma.setTurno(turno);

        turmaDAO.inserir(turma);
    }

    public void atualizarTurma(int idTurma, int salaId, String descricaoTurma, Turno turno) {
        Turma turma = new Turma();
        turma.setIdTurma(idTurma);
        turma.setSalaId(salaId);
        turma.setDescricaoTurma(descricaoTurma);
        turma.setTurno(turno);

        turmaDAO.atualizar(turma);
    }

    public void excluirTurma(int idTurma) {
        turmaDAO.excluir(idTurma);
    }

    public List<Turma> listarTurmas() {
        return turmaDAO.listar();
    }

    public Turma buscarTurma(int idTurma) {
        return turmaDAO.buscarPorId(idTurma);
    }
}