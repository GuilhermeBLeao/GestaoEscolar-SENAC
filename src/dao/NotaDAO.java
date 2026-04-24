//Igor

package dao;

import model.Nota;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO {

    private final Connection conn;

    public NotaDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public void inserir(Nota nota) throws SQLException {
        validarNotaNaoNula(nota);

        final String sql = """
            INSERT INTO nota (
                id_disciplina,
                id_aluno,
                atividade,
                nota
            ) VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, nota.getIdDisciplina());
            stmt.setInt(2, nota.getIdAluno());
            stmt.setString(3, nota.getAtividade());
            stmt.setDouble(4, nota.getNota());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir nota. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    nota.setNotasId(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir nota. ID não retornado.");
                }
            }
        }
    }

    public void atualizar(Nota nota) throws SQLException {
        validarNotaNaoNula(nota);

        if (nota.getNotasId() <= 0) {
            throw new IllegalArgumentException("ID da nota inválido.");
        }

        final String sql = """
            UPDATE nota
               SET nota = ?
             WHERE notas_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, nota.getNota());
            stmt.setInt(2, nota.getNotasId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar nota. Nenhuma linha afetada.");
            }
        }
    }

    public Nota buscarPorId(int idNota) throws SQLException {
        final String sql = """
            SELECT notas_id, id_disciplina, id_aluno, atividade, nota
            FROM nota
            WHERE notas_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idNota);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearNota(rs);
                }
                return null;
            }
        }
    }

    public boolean excluir(int idNota) throws SQLException {
        final String sql = "DELETE FROM nota WHERE notas_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idNota);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir nota. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    public List<Nota> listar() throws SQLException {
        final String sql = """
            SELECT notas_id, id_disciplina, id_aluno, atividade, nota
            FROM nota
            ORDER BY id_aluno, id_disciplina, atividade
            """;

        List<Nota> notas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                notas.add(mapearNota(rs));
            }
        }

        return notas;
    }

    public List<Nota> listarPorAluno(int idAluno) throws SQLException {
        final String sql = """
            SELECT notas_id, id_disciplina, id_aluno, atividade, nota
            FROM nota
            WHERE id_aluno = ?
            ORDER BY id_disciplina, atividade
            """;

        List<Nota> notas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notas.add(mapearNota(rs));
                }
            }
        }

        return notas;
    }

    public List<Nota> listarPorDisciplina(int idDisciplina) throws SQLException {
        final String sql = """
            SELECT notas_id, id_disciplina, id_aluno, atividade, nota
            FROM nota
            WHERE id_disciplina = ?
            ORDER BY id_aluno, atividade
            """;

        List<Nota> notas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDisciplina);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notas.add(mapearNota(rs));
                }
            }
        }

        return notas;
    }

    private Nota mapearNota(ResultSet rs) throws SQLException {
        Nota nota = new Nota();
        nota.setNotasId(rs.getInt("notas_id"));
        nota.setIdDisciplina(rs.getInt("id_disciplina"));
        nota.setIdAluno(rs.getInt("id_aluno"));
        nota.setAtividade(rs.getString("atividade"));
        nota.setNota(rs.getDouble("nota"));
        return nota;
    }

    private void validarNotaNaoNula(Nota nota) {
        if (nota == null) {
            throw new IllegalArgumentException("Nota não pode ser nula.");
        }
    }
}