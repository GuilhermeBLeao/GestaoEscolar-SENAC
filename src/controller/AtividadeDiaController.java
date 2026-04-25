//Arthur, José e Guilherme

package controller;

import dao.AtividadeDiaDAO;
import database.ConnectionFactory;
import model.AtividadeDia;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class AtividadeDiaController {
	public AtividadeDia prepararAtividadeDia(int professorId, int turmaId, int disciplinaId) {
		if(professorId <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}
		if(turmaId <= 0) {
			throw new IllegalArgumentException("ID da turma é inválido.");
		}
		if(disciplinaId <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		
		AtividadeDia atividade = new AtividadeDia();
		
		atividade.setProfessorId(professorId);
		atividade.setTurmaId(turmaId);
		atividade.setDisciplinaId(disciplinaId);
		atividade.setData(LocalDate.now());
		
		return atividade;
	}
	
	public void salvarAtividadeDia(AtividadeDia atividade) {
		validarAtividadeDia(atividade);
		
		try(Connection conn = ConnectionFactory.getConnection()){
			AtividadeDiaDAO atividadeDAO = new AtividadeDiaDAO(conn);
			
			AtividadeDia existente = atividadeDAO.buscarPorTurmaDisciplinaData(
					atividade.getTurmaId(),
					atividade.getDisciplinaId(),
					atividade.getData()
					);
			
			if(existente != null) {
				throw new IllegalArgumentException("Já existe atividade registrada para esta data, turma e disciplina.");
			}
			
			atividadeDAO.inserir(atividade);
		}catch(SQLException e) {
			throw new RuntimeException("Erro ao salvar atividade do dia.",e);
		}
	}

	private void validarAtividadeDia(AtividadeDia atividade) {
		if(atividade == null) {
			throw new IllegalArgumentException("Atividade do dia não pode ser nula.");
		}
		if(atividade.getProfessorId() <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}
		if(atividade.getTurmaId() <= 0) {
			throw new IllegalArgumentException("ID da turma é inválido.");
		}
		if(atividade.getDisciplinaId() <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		if(atividade.getData() == null) {
			throw new IllegalArgumentException("Campo data é obrigatório.");
		}
		if(atividade.getData().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Data não pode ser futura.");
		}
		if(atividade.getDescricao() == null || atividade.getDescricao().trim().isEmpty()) {
			throw new IllegalArgumentException("Campo descrição é obrigatório.");
		}
	}
}