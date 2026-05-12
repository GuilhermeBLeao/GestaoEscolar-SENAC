// Guilherme

package controller;

import dao.BoletimDAO;
import database.ConnectionFactory;
import model.Boletim;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BoletimController {

	public Boletim gerarBoletim(int idAluno, int idTrimestre) {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		if (idTrimestre <= 0)
			throw new IllegalArgumentException("ID do trimestre inválido.");

		try (Connection conn = ConnectionFactory.getConnection()) {
			Boletim boletim = new BoletimDAO(conn).buscarPorAluno(idAluno, idTrimestre);

			if (boletim == null)
				throw new IllegalArgumentException("Nenhuma nota encontrada para este aluno no trimestre informado.");

			return boletim;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao gerar boletim do aluno.", e);
		}
	}

	public List<Boletim> gerarBoletinsPorTurma(int idTurma, int idTrimestre) {
		if (idTurma <= 0)
			throw new IllegalArgumentException("ID da turma inválido.");
		if (idTrimestre <= 0)
			throw new IllegalArgumentException("ID do trimestre inválido.");

		try (Connection conn = ConnectionFactory.getConnection()) {
			List<Boletim> boletins = new BoletimDAO(conn).buscarPorTurma(idTurma, idTrimestre);

			if (boletins == null || boletins.isEmpty())
				throw new IllegalArgumentException("Nenhuma nota encontrada para esta turma no trimestre informado.");

			return boletins;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao gerar boletins da turma.", e);
		}
	}
}