//luiz

package dao; 

import model.TurmaDisciplina; 

import java.sql.*; 
import java.util.ArrayList;
import java.util.List; 

public class TurmaDisciplinaDAO { 

    private final Connection conn; 

    public TurmaDisciplinaDAO(Connection conn) { 
        if (conn == null) { // Verifica se a conexão recebida é nula
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados."); // Lança exceção se a conexão for nula
        }
        this.conn = conn; 
    }

    public void inserir(TurmaDisciplina td) throws SQLException { 
        validarNaoNulo(td); 

        final String sql = """ 
            INSERT INTO turma_disciplina (
                professores_id,   
                turma_id,         
                disciplina_id,    
                horario_inicio,   
                horario_termino   
            ) VALUES (?, ?, ?, ?, ?) 
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { 
            stmt.setInt(1, td.getProfessores_id()); 
            stmt.setInt(2, td.getTurma_id()); 
            stmt.setInt(3, td.getDisciplina_id()); 
            stmt.setTimestamp(4, td.getHorarioInicio() != null ? Timestamp.valueOf(td.getHorarioInicio()) : null); 
            stmt.setTimestamp(5, td.getHorarioTermino() != null ? Timestamp.valueOf(td.getHorarioTermino()) : null);
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) { 
                throw new SQLException("Falha ao inserir turma-disciplina. Nenhuma linha afetada."); 
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) { 
                if (rs.next()) { 
                    td.setId_turmad(rs.getInt(1)); 
                } else {
                    throw new SQLException("Falha ao inserir turma-disciplina. ID não retornado."); 
                }
            }
        }
    }

    public void atualizar(TurmaDisciplina td) throws SQLException { //atualiza um registro existente no banco
        validarNaoNulo(td); // Verifica que o objeto não é nulo

        if (td.getId_turmad() <= 0) { 
            throw new IllegalArgumentException("ID da turma-disciplina inválido."); 
        }

        final String sql = """ 
            UPDATE turma_disciplina
               SET professores_id = ?,  
                   turma_id = ?,        
                   disciplina_id = ?,   
                   horario_inicio = ?,  
                   horario_termino = ?  
             WHERE id_turmad = ?        
            """; 

        try (PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setInt(1, td.getProfessores_id()); 
            stmt.setInt(2, td.getTurma_id()); 
            stmt.setInt(3, td.getDisciplina_id()); 
            stmt.setTimestamp(4, td.getHorarioInicio() != null ? Timestamp.valueOf(td.getHorarioInicio()) : null); 
            stmt.setTimestamp(5, td.getHorarioTermino() != null ? Timestamp.valueOf(td.getHorarioTermino()) : null); 
            stmt.setInt(6, td.getId_turmad()); 

            int linhasAfetadas = stmt.executeUpdate(); 
            if (linhasAfetadas == 0) { 
                throw new SQLException("Falha ao atualizar turma-disciplina. Nenhuma linha afetada."); 
            }
        }
    }

    public TurmaDisciplina buscarPorId(int idTurmad) throws SQLException { // busca um único registro pelo ID
        final String sql = """
            SELECT id_turmad, professores_id, turma_id, disciplina_id, horario_inicio, horario_termino
            FROM turma_disciplina
            WHERE id_turmad = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setInt(1, idTurmad); 

            try (ResultSet rs = stmt.executeQuery()) { 
                if (rs.next()) { 
                    return mapear(rs); 
                }
                return null; 
            }
        }
    }

    public List<TurmaDisciplina> listarPorTurma(int turmaId) throws SQLException { // Método que retorna todos os vínculos de uma turma específica
        final String sql = """ 
            SELECT id_turmad, professores_id, turma_id, disciplina_id, horario_inicio, horario_termino
            FROM turma_disciplina
            WHERE turma_id = ?
            """;

        List<TurmaDisciplina> lista = new ArrayList<>(); 

        try (PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setInt(1, turmaId); 

            try (ResultSet rs = stmt.executeQuery()) { 
                while (rs.next()) { 
                    lista.add(mapear(rs)); 
                }
            }
        }

        return lista;
    }

    public List<TurmaDisciplina> listar() throws SQLException { 
        final String sql = """
            SELECT id_turmad, professores_id, turma_id, disciplina_id, horario_inicio, horario_termino
            FROM turma_disciplina
            """;

        List<TurmaDisciplina> lista = new ArrayList<>();  

        try (PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) { 

            while (rs.next()) { 
                lista.add(mapear(rs)); 
            }
        }

        return lista; // Retorna a lista com todos os registros
    }

    public boolean excluir(int idTurmad) throws SQLException { 
        final String sql = "DELETE FROM turma_disciplina WHERE id_turmad = ?"; 

        try (PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setInt(1, idTurmad); 

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) { 
                throw new SQLException("Falha ao excluir turma-disciplina. Nenhuma linha afetada."); 
            }

            return true; 
        }
    }

    private TurmaDisciplina mapear(ResultSet rs) throws SQLException { 
        TurmaDisciplina td = new TurmaDisciplina(); 
        td.setId_turmad(rs.getInt("id_turmad")); 
        td.setProfessores_id(rs.getInt("professores_id")); 
        td.setTurma_id(rs.getInt("turma_id")); 
        td.setDisciplina_id(rs.getInt("disciplina_id")); 

        Timestamp inicio = rs.getTimestamp("horario_inicio");
        if (inicio != null) { 
            td.setHorarioInicio(inicio.toLocalDateTime()); 
        }

        Timestamp termino = rs.getTimestamp("horario_termino"); 
        if (termino != null) { 
            td.setHorarioTermino(termino.toLocalDateTime()); 
        }

        return td; 
    }

    private void validarNaoNulo(TurmaDisciplina td) { 
        if (td == null) { 
            throw new IllegalArgumentException("TurmaDisciplina não pode ser nula."); 
        }
    }
}
