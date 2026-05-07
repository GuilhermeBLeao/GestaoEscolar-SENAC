//Guilherme

package controller;

import dao.BoletimDAO;
import database.ConnectionFactory;
import model.Boletim;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BoletimController {
	public Boletim gerarBoletim(int idAluno) {
		if(idAluno <= 0) {
			throw new IllegalArgumentException("ID do aluno inválido.");
		}
		
		try(Connection conn = ConnectionFactory.getConnection()){
			BoletimDAO dao = new BoletimDAO(conn);
			
			Boletim boletim = dao.buscarPorAluno(idAluno);
			
			if(boletim == null) {
				throw new IllegalArgumentException("Nenhuma nota encontrada para este aluno.");
			}
			return boletim;
		}catch(SQLException e) {
			throw new RuntimeException("Erro ao gerar boletim do aluno.", e);
		}
	}
	
	public List<Boletim> gerarBoletinsPorTurma(int idTurma){
		 if (idTurma <= 0) {
	            throw new IllegalArgumentException("ID da turma inválido.");
	        }

	        try (Connection conn = ConnectionFactory.getConnection()) {
	            BoletimDAO dao = new BoletimDAO(conn);

	            List<Boletim> boletins = dao.buscarPorTurma(idTurma);

	            if (boletins == null || boletins.isEmpty()) {
	                throw new IllegalArgumentException("Nenhuma nota encontrada para esta turma.");
	            }
	            return boletins;
	        } catch (SQLException e) {
	            throw new RuntimeException("Erro ao gerar boletins da turma.", e);
	        }
	    }
}