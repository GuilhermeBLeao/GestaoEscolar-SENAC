//Guilherme

package controller;

import dao.AdvertenciaDAO;
import dao.AlunoDAO;
import database.ConnectionFactory;
import model.Advertencia;
import model.AdvertenciaItem;
import model.Aluno;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdvertenciaController {

    public Advertencia prepararAdvertencia(int professorId, int turmaId) {
        if (professorId <= 0) {
            throw new IllegalArgumentException("ID do professor é inválido.");
        }
        if (turmaId <= 0) {
            throw new IllegalArgumentException("ID da turma é inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoBanco = new AlunoDAO(conn);
            List<Aluno> alunos = alunoBanco.listarPorTurma(turmaId);

            if (alunos.isEmpty()) {
                throw new IllegalArgumentException("Não existem alunos cadastrados nesta turma.");
            }

            List<AdvertenciaItem> itens = new ArrayList<>();

            for (Aluno aluno : alunos) {
                itens.add(new AdvertenciaItem(aluno.getIdAluno(), aluno.getNome()));
            }

            Advertencia advertencia = new Advertencia();
            advertencia.setProfessorId(professorId);
            advertencia.setTurmaId(turmaId);
            advertencia.setDataAdvertencia(LocalDate.now());
            advertencia.setItens(itens);

            return advertencia;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao preparar a advertência.", e);
        }
    }

    public void salvarAdvertencia(Advertencia advertencia) {
        validarAdvertencia(advertencia);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AdvertenciaDAO advertenciaDAO = new AdvertenciaDAO(conn);

                for (AdvertenciaItem item : advertencia.getItens()) {
                    if (!itemPossuiAdvertencia(item)) {
                        continue;
                    }

                    Advertencia advertenciaAluno = new Advertencia();
                    advertenciaAluno.setAlunoId(item.getIdAluno());
                    advertenciaAluno.setTurmaId(advertencia.getTurmaId());
                    advertenciaAluno.setProfessorId(advertencia.getProfessorId());
                    advertenciaAluno.setMotivo(item.getMotivo());
                    advertenciaAluno.setDescricao(item.getDescricao());
                    advertenciaAluno.setDataAdvertencia(advertencia.getDataAdvertencia());

                    advertenciaDAO.inserir(advertenciaAluno);
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar advertência.", e);
        }
    }

    private void validarAdvertencia(Advertencia advertencia) {
        if (advertencia == null) {
            throw new IllegalArgumentException("Advertência não pode ser nula.");
        }
        if (advertencia.getProfessorId() <= 0) {
            throw new IllegalArgumentException("ID do professor é obrigatório.");
        }
        if (advertencia.getTurmaId() <= 0) {
            throw new IllegalArgumentException("ID da turma é obrigatório.");
        }
        if (advertencia.getDataAdvertencia() == null) {
            throw new IllegalArgumentException("Data da advertência é obrigatória.");
        }
        if (advertencia.getDataAdvertencia().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da advertência não pode ser futura.");
        }
        if (advertencia.getItens() == null || advertencia.getItens().isEmpty()) {
            throw new IllegalArgumentException("A advertência precisa conter ao menos 1 aluno.");
        }
        if (!existeItemComAdvertencia(advertencia.getItens())) {
            throw new IllegalArgumentException("Selecione ao menos 1 aluno e informe motivo e descrição.");
        }
    }

    private boolean existeItemComAdvertencia(List<AdvertenciaItem> itens) {
        for (AdvertenciaItem item : itens) {
            if (itemPossuiAdvertencia(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean itemPossuiAdvertencia(AdvertenciaItem item) {
        return item != null
                && item.getIdAluno() > 0
                && item.getMotivo() != null
                && !item.getMotivo().trim().isEmpty()
                && item.getDescricao() != null
                && !item.getDescricao().trim().isEmpty();
    }
}