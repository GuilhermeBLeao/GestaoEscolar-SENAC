//Guilherme

package controller;

import java.util.List;
import dao.SalaDAO;
import model.Sala;

public class SalaController {
    private final SalaDAO salaDAO;

    public SalaController() {
        this.salaDAO = new SalaDAO();
    }

    public void salvarSala(int capacidade) {
        Sala sala = new Sala();
        sala.setCapacidade(capacidade);

        salaDAO.inserir(sala);
    }

    public void atualizarSala(int id_sala, int capacidade) {
        Sala sala = salaDAO.buscarPorId(id_sala);

        if (sala == null) {
            throw new IllegalArgumentException("Sala não encontrada.");
        }

        sala.setCapacidade(capacidade);

        salaDAO.atualizar(sala);
    }

    public void excluirSala(int id_sala) {
        salaDAO.excluir(id_sala);
    }

    public List<Sala> listarSalas() {
        return salaDAO.listar();
    }

    public Sala buscarSala(int id_sala) {
        return salaDAO.buscarPorId(id_sala);
    }
}