//Guilherme

package dao;

import model.BoletimItem;
import model.Boletim;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class BoletimDAO {
	private final Connection conn;
	
	public BoletimDAO(Connection conn) {
		if(conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}
	
	public Boletim buscarPorAluno(int idAluno) throws SQLException{
		if(idAluno <= 0) {
			throw new IllegalArgumentException("ID do aluno inválido.");
		}
		
		final String sql = """
					SELECT 
						a.id_aluno,
						a.nome AS nome_aluno,
						a.matricula,
						a.turma_id,
						d.id_disciplina,
						d.descricao AS nome_disciplina,
						AVG(n.nota) AS media
					FROM aluno a
					INNER JOIN nota n 
					ON n.aluno_id = a.id_aluno
					INNER JOIN disciplina d
					ON d.id_disciplina = n.disciplina_id
					WHERE a.id_aluno = ?
					GROUP BY
						a.id_aluno,
						a.nome,
						a.matricula,
						a.turma_id,
						d.id_disciplina,
						d.descricao
					ORDER BY d.descricao
				""";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setInt(1, idAluno);
			
			try(ResultSet rs = stmt.executeQuery()){
				Boletim boletim = null;
				
				while(rs.next()) {
					if(boletim == null) {
						boletim = mapearCabecalho(rs);
					}
					
					boletim.adicionarItem(mapearItem(rs));
				}
				return boletim;
			}
		}
	}

	public List<Boletim> buscarPorTurma(int idTurma) throws SQLException{
		if(idTurma <= 0) {
			throw new IllegalArgumentException("ID da turma inválido.");
		}
		
		final String sql = """
					SELECT
						a.id_aluno,
						a.nome AS nome_aluno,
						a.matricula,
						a.turma_id,
						d.id_disciplina,
						d.descricao AS nome_disciplina,
						AVG(n.nota) AS media
					FROM aluno a
					INNER JOIN nota n
					ON n.aluno_id = a.id_aluno
					INNER JOIN disciplina d
					ON d.id_disciplina = n.disciplina_id
					WHERE a.turma_id = ?
					GROUP BY
						a.id_aluno,
						a.nome,
						a.matricula,
						a.turma_id,
						d.id_disciplina,
						d.descricao
					ORDER BY d.descricao
				""";
		
		Map<Integer, Boletim> boletins = new LinkedHashMap<>();
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setInt(1, idTurma);
			
			try(ResultSet rs = stmt.executeQuery()){
				while(rs.next()) {
					int idAluno = rs.getInt("id_aluno");
					
					Boletim boletim = boletins.get(idAluno);
					
					if(boletim == null) {
						boletim = mapearCabecalho(rs);
						boletins.put(idAluno, boletim);
					}
					
					boletim.adicionarItem(mapearItem(rs));
				}
			}
		}
		return new ArrayList<>(boletins.values());
	}
	
	private Boletim mapearCabecalho(ResultSet rs) throws SQLException{
		Boletim boletim = new Boletim();
		
		boletim.setIdAluno(rs.getInt("id_aluno"));
		boletim.setNomeAluno(rs.getString("nome_aluno"));
		boletim.setMatricula(rs.getString("matricula"));
		boletim.setIdTurma(rs.getInt("turma_id"));
		boletim.setDatageracao(LocalDate.now());
		
		return boletim;
	}
	
	private BoletimItem mapearItem(ResultSet rs) throws SQLException {
		BoletimItem item = new BoletimItem();
		
		item.setIdDisciplina(rs.getInt("id_disciplina"));
		item.setNomeDisciplina(rs.getString("nome_disciplina"));
		item.setMedia(rs.getDouble("media"));
		
		return item;
	}
}
