//Guilherme e Igor

package controller;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.NotaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Nota;
import model.LancamentoNotaItem;
import model.LancamentoNota;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LancamentoNotaController {

    public LancamentoNota preparaLancamentoNota(int turmaId, int disciplinaId, String atividade) {
        if (turmaId <= 0) {
            throw new IllegalArgumentException("ID da turma é inválido.");
        }

        if (disciplinaId <= 0) {
            throw new IllegalArgumentException("ID da disciplina é inválido.");
        }

        if (atividade == null || atividade.trim().isEmpty()) {
            throw new IllegalArgumentException("Atividade é obrigatória.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            validarExistenciasRelacionamentos(
                    new TurmaDAO(conn),
                    new DisciplinaDAO(conn),
                    turmaId,
                    disciplinaId
            );
            AlunoDAO alunoBanco = new AlunoDAO(conn);
            List<Aluno> alunos = alunoBanco.listarPorTurma(turmaId);

            if (alunos.isEmpty()) {
                throw new IllegalArgumentException("Não existem alunos cadastrados.");
            }

            List<LancamentoNotaItem> itens = new ArrayList<>();

            for (Aluno aluno : alunos) {
                itens.add(new LancamentoNotaItem(aluno.getIdAluno(), aluno.getNome()));
            }

            LancamentoNota lancamento = new LancamentoNota();

            lancamento.setTurmaId(turmaId);
            lancamento.setDisciplinaId(disciplinaId);
            lancamento.setDataLancamento(LocalDate.now());
            lancamento.setAtividade(atividade.trim());
            lancamento.setItens(itens);

            return lancamento;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao preparar o lançamento de notas.", e);
        }
    }

    public void salvarLancamento(LancamentoNota lancamento) {
        validarLancamento(lancamento);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                NotaDAO notaDAO = new NotaDAO(conn);
                AlunoDAO alunoDAO = new AlunoDAO(conn);

                validarExistenciasRelacionamentos(
                        new TurmaDAO(conn),
                        new DisciplinaDAO(conn),
                        lancamento.getTurmaId(),
                        lancamento.getDisciplinaId()
                );

                for (LancamentoNotaItem item : lancamento.getItens()) {
                    validarExistenciaAluno(alunoDAO, item.getAlunoId());

                    Nota existente = notaDAO.buscarPorAlunoDisciplinaAtividade(
                            item.getAlunoId(),
                            lancamento.getDisciplinaId(),
                            lancamento.getAtividade()
                    );

                    if (existente != null) {
                        throw new IllegalArgumentException(
                                "Já existe nota para esta disciplina, atividade e um dos alunos selecionados."
                        );
                    }

                    Nota nota = new Nota();

                    nota.setIdAluno(item.getAlunoId());
                    nota.setIdDisciplina(lancamento.getDisciplinaId());
                    nota.setDataLancamento(lancamento.getDataLancamento());
                    nota.setAtividade(lancamento.getAtividade().trim());
                    nota.setNota(item.getNota());

                    notaDAO.inserir(nota);
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar lançamento de nota.", e);
        }
    }

    private void validarExistenciasRelacionamentos(
            TurmaDAO turmaDAO,
            DisciplinaDAO disciplinaDAO,
            int turmaId,
            int disciplinaId
    ) throws SQLException {
        if (turmaDAO.buscarPorId(turmaId) == null) {
            throw new IllegalArgumentException("Turma informada não existe.");
        }

        if (disciplinaDAO.buscarPorId(disciplinaId) == null) {
            throw new IllegalArgumentException("Disciplina informada não existe.");
        }
    }

    private void validarExistenciaAluno(AlunoDAO alunoDAO, int alunoId) throws SQLException {
        if (alunoDAO.buscarPorId(alunoId) == null) {
            throw new IllegalArgumentException("Aluno informado no lançamento de nota não existe.");
        }
    }

    private void validarLancamento(LancamentoNota lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("Lançamento de nota não pode ser nula.");
        }

        if (lancamento.getDisciplinaId() <= 0) {
            throw new IllegalArgumentException("ID da disciplina é obrigatório.");
        }

        if (lancamento.getTurmaId() <= 0) {
            throw new IllegalArgumentException("ID da turma é obrigatório.");
        }

        if (lancamento.getDataLancamento() == null) {
            throw new IllegalArgumentException("Data de lançamento da nota é obrigatória.");
        }

        if (lancamento.getDataLancamento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de lançamento da nota não pode ser futura.");
        }

        if (lancamento.getAtividade() == null || lancamento.getAtividade().trim().isEmpty()) {
            throw new IllegalArgumentException("Atividade é obrigatória.");
        }

        if (lancamento.getItens() == null || lancamento.getItens().isEmpty()) {
            throw new IllegalArgumentException("O lançamento de nota precisa conter ao menos 1 aluno.");
        }

        for (LancamentoNotaItem item : lancamento.getItens()) {
            if (item == null) {
                throw new IllegalArgumentException("Item de lançamento de nota inválido.");
            }

            if (item.getAlunoId() <= 0) {
                throw new IllegalArgumentException("ID do aluno é inválido.");
            }

            if (item.getNota() < 0 || item.getNota() > 10) {
                throw new IllegalArgumentException("A nota deve estar entre 0 e 10.");
            }
        }
    }
}