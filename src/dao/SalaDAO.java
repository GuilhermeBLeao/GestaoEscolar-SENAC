//Guilherme

package dao;

import model.Sala;
import database.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaDAO{
	public void inserir(Sala sala) {
		String sql = "INSERT INTO sala (capacidade) VALUES (?)";
		
		try(
			Connection conn = ConnectionFactory.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)		
				){
			stmt.setInt(1, sala.getCapacidade());
			
			stmt.executeUpdate();
			
			try(ResultSet rs = stmt.getGeneratedKeys()){
				if(rs.next())
					sala.setIdSala(rs.getInt(1));
				}
		}catch(SQLException e) {
			throw new RuntimeException("Erro ao inserir sala.",e);
		}
	}
	
	public void atualizar(Sala sala) {
		String sql = "UPDATE sala SET capacidade = ? WHERE id_sala = ?";
		try(Connection conn = ConnectionFactory.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql)
		){
			stmt.setInt(1,sala.getCapacidade());
			stmt.setInt(2, sala.getIdSala());
			int linhasAfetadas = stmt.executeUpdate();
			if(linhasAfetadas == 0)
				throw new RuntimeException("Nenhuma sala foi atualizada.");
		}catch(SQLException e) {
			throw new RuntimeException("Erro ao atualizar a sala.",e);
		}
	}
	
    public void excluir(int idSala) {
        String sql = "DELETE FROM sala WHERE id_sala = ?";
        try (
            Connection conn = ConnectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idSala);
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0)
                throw new RuntimeException("Nenhuma sala foi excluída.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir sala.", e);
        }
    }

    public List<Sala> listar() {
        List<Sala> lista = new ArrayList<>();
        String sql = "SELECT * FROM sala ORDER BY id_sala";
        try (
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                Sala s = new Sala();
                s.setIdSala(rs.getInt("id_sala"));
                s.setCapacidade(rs.getInt("capacidade"));
                lista.add(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar salas.", e);
        }
        return lista;
    }

    public Sala buscarPorId(int idSala) {
        String sql = "SELECT * FROM sala WHERE id_sala = ?";
        try (
            Connection conn = ConnectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idSala);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sala s = new Sala();
                    s.setIdSala(rs.getInt("id_sala"));
                    s.setCapacidade(rs.getInt("capacidade"));
                    return s;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sala por ID.", e);
        }
        return null;
    }
}