//Luiz, Igor e Guilherme

package controller;

import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.PresencaDAO;
import dao.ProfessorDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Chamada;
import model.ChamadaItem;
import model.Presenca;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChamadaController{
	public Chamada prepararChamada(int professorId, int turmaId, int disciplinaId) {
		if(professorId <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}
		if(turmaId <= 0) {
			throw new IllegalArgumentException("ID da turma é inválido.");
		}
		if(disciplinaId <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido");
		}
		
		try(Connection conn = ConnectionFactory.getConnection()){
            validarExistenciasRelacionamentos(
                    new ProfessorDAO(conn),
                    new TurmaDAO(conn),
                    new DisciplinaDAO(conn),
                    professorId,
                    turmaId,
                    disciplinaId
            );
			//Cria o DAO
			AlunoDAO alunoBanco = new AlunoDAO(conn);
			//Busca todos os alunos daquela turma
			List<Aluno> alunos = alunoBanco.listarPorTurma(turmaId);
			
			//Evita criar chamada sem alunos
			if(alunos.isEmpty()) {
				throw new IllegalArgumentException("Não existem alunos cadastrados nesta turma.");
			}
			
			//Cria os itens da chamada
			List<ChamadaItem> itens = new ArrayList<>();
			
			//Para cada aluno
			for(Aluno aluno : alunos) {
				//Cria um item da chamada (presença individual)
				itens.add(new ChamadaItem(aluno.getIdAluno(), aluno.getNome()));
			}
			
			//Monta o objeto da chamada
			Chamada chamada = new Chamada();
			//Define professor, turma, data e lista de alunos.
			chamada.setProfessorId(professorId);
			chamada.setTurmaId(turmaId);
			chamada.setData(LocalDate.now());
			chamada.setDisciplinaId(disciplinaId);
			chamada.setItens(itens);
			
			//Retorna a chamada pronta(mas ainda não salva)
			return chamada;			
		}catch(SQLException e) {
			throw new RuntimeException("Erro ao preparar a chamada.", e);
		}
	}
	
	//Responsável por persistir a chamada no banco
	public void salvarChamada(Chamada chamada) {
        //Validação
		validarChamada(chamada);

        try (Connection conn = ConnectionFactory.getConnection()) {
            //Inicia a transação manual
        	conn.setAutoCommit(false);

            try {
                PresencaDAO presencaDAO = new PresencaDAO(conn);
                validarExistenciasRelacionamentos(
                        new ProfessorDAO(conn),
                        new TurmaDAO(conn),
                        new DisciplinaDAO(conn),
                        chamada.getProfessorId(),
                        chamada.getTurmaId(),
                        chamada.getDisciplinaId()
                );
                
                //Loop dos alunos                
                for (ChamadaItem item : chamada.getItens()) {
                	Presenca existente = presencaDAO.buscarPorAlunoDisciplinaData(item.getAlunoId(), chamada.getDisciplinaId(), chamada.getData());

                	if (existente != null) {
                	    throw new IllegalArgumentException("Já existe chamada para o aluno ID: " + item.getAlunoId());
                	}
                	}

                //Confirma tudo e realiza o commit
                conn.commit();

            } catch (Exception e) {
            	//Em caso de erro, desfaz tudo
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar chamada.", e);
        }
    }

    private void validarExistenciasRelacionamentos(
            ProfessorDAO professorDAO,
            TurmaDAO turmaDAO,
            DisciplinaDAO disciplinaDAO,
            int professorId,
            int turmaId,
            int disciplinaId
    ) throws SQLException {
        if (professorDAO.buscarPorId(professorId) == null) {
            throw new IllegalArgumentException("Professor informado não existe.");
        }

        if (turmaDAO.buscarPorId(turmaId) == null) {
            throw new IllegalArgumentException("Turma informada não existe.");
        }

        if (disciplinaDAO.buscarPorId(disciplinaId) == null) {
            throw new IllegalArgumentException("Disciplina informada não existe.");
        }
    }

	private void validarChamada(Chamada chamada) {
		//Validações
		if(chamada == null) {
			throw new IllegalArgumentException("Chamada não pode ser nula.");
		}
		if(chamada.getProfessorId() <= 0) {
			throw new IllegalArgumentException("ID do professor é obrigatório.");
		}
		if(chamada.getTurmaId() <= 0) {
			throw new IllegalArgumentException("ID da turma é obrigatório.");
		}
		if(chamada.getDisciplinaId() <= 0) {
			throw new IllegalArgumentException("ID da disciplina é obrigatório.");
		}
		if(chamada.getData() == null) {
			throw new IllegalArgumentException("Data da chamada é obrigatória.");
		}
		if(chamada.getData().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Data da chamada não pode ser futura.");
		}
		if(chamada.getItens() == null || chamada.getItens().isEmpty()) {
			throw new IllegalArgumentException("A chamada precisa conter ao menos 1 aluno.");
		}
	}
}
