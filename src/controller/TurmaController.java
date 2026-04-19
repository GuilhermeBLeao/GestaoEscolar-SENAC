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

        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        Turma turma = turmaDAO.buscarPorId(idTurma);

        if (turma == null) {
            throw new IllegalArgumentException("Turma não encontrada.");
        }

        turma.setSalaId(salaId);
        turma.setDescricaoTurma(descricaoTurma);
        turma.setTurno(turno);

        turmaDAO.atualizar(turma);
    }

    public void excluirTurma(int idTurma) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        turmaDAO.excluir(idTurma);
    }

    public List<Turma> listarTurmas() {
        return turmaDAO.listar();
    }

    public Turma buscarTurma(int idTurma) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        return turmaDAO.buscarPorId(idTurma);
    }
}