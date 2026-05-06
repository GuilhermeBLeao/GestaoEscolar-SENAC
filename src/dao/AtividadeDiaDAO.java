//Arthur, José e Guilherme

package dao;

import model.AtividadeDia;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AtividadeDiaDAO {
	private final Connection conn;
	
	public AtividadeDiaDAO(Connection conn) {
		if(conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}
	
	public void inserir(AtividadeDia atividade) throws SQLException{
		String sql = """
				INSERT INTO atividade_dia
				(professor_id, turma_id, disciplina_id, data, descricao)
				VALUES (?,?,?,?,?)
				""";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setInt(1,  atividade.getProfessorId());
			stmt.setInt(2, atividade.getTurmaId());
			stmt.setInt(3, atividade.getDisciplinaId());
			stmt.setDate(4, Date.valueOf(atividade.getData()));
			stmt.setString(5, atividade.getDescricao());
			
			int linhasAfetadas = stmt.executeUpdate();
			
			if(linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir a atividade do dia.");
			}
		}
	}
	
	public AtividadeDia buscarPorTurmaDisciplinaData(
			int turmaId,
			int disciplinaId,
			LocalDate data
			)throws SQLException{
				String sql = """
						SELECT id_atividade_dia, professor_id, turma_id, disciplina_id, data, descricao
						FROM atividade_dia
						WHERE turma_id = ?
						AND disciplina_id = ?
						AND data = ?
						""";
				try(PreparedStatement stmt = conn.prepareStatement(sql)){
					stmt.setInt(1, turmaId);
					stmt.setInt(2, disciplinaId);
					stmt.setDate(3, Date.valueOf(data));
					
					try(ResultSet rs = stmt.executeQuery()){
						if(rs.next()) {
							return montarAtividadeDia(rs);
						}
					}
			}
				return null;
	}
	
	private AtividadeDia montarAtividadeDia(ResultSet rs) throws SQLException{
		AtividadeDia atividade = new AtividadeDia();
		
		atividade.setIdAtividade(rs.getInt("id_atividade_dia"));
		atividade.setProfessorId(rs.getInt("professor_id"));
		atividade.setTurmaId(rs.getInt("turma_id"));
		atividade.setDisciplinaId(rs.getInt("disciplina_id"));
		atividade.setData(rs.getDate("data").toLocalDate());
		atividade.setDescricao(rs.getString("descricao"));
		
		return atividade;
	}
}
