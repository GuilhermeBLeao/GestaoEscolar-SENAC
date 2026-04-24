//Luiz - Igor editou

package dao;

import model.Disciplina;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    private final Connection conn;

    public DisciplinaDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public boolean existeCodigo(int codigo) throws SQLException {
        final String sql = "SELECT 1 FROM disciplina WHERE codigo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void inserir(Disciplina disciplina) throws SQLException {
        validarDisciplinaNaoNula(disciplina);

        final String sql = """
            INSERT INTO disciplina (
                descricao,
                carga_horaria,
                codigo,
                ativo
            ) VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, disciplina.getDescricao());
            stmt.setInt(2, disciplina.getCargaHoraria());
            stmt.setInt(3, disciplina.getCodigo());
            stmt.setBoolean(4, disciplina.isAtivo());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir disciplina. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    disciplina.setIdDisciplina(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir disciplina. ID não retornado.");
                }
            }
        }
    }

    public void atualizar(Disciplina disciplina) throws SQLException {
        validarDisciplinaNaoNula(disciplina);

        if (disciplina.getIdDisciplina() <= 0) {
            throw new IllegalArgumentException("ID da disciplina inválido.");
        }

        final String sql = """
            UPDATE disciplina
               SET descricao = ?,
                   carga_horaria = ?,
                   ativo = ?
             WHERE id_disciplina = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, disciplina.getDescricao());
            stmt.setInt(2, disciplina.getCargaHoraria());
            stmt.setBoolean(3, disciplina.isAtivo());
            stmt.setInt(4, disciplina.getIdDisciplina());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar disciplina. Nenhuma linha afetada.");
            }
        }
    }

    public Disciplina buscarPorId(int idDisciplina) throws SQLException {
        final String sql = """
            SELECT id_disciplina, descricao, carga_horaria, codigo, ativo
            FROM disciplina
            WHERE id_disciplina = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDisciplina);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearDisciplina(rs);
                }
                return null;
            }
        }
    }

    public List<Disciplina> listar() throws SQLException {
        final String sql = """
            SELECT id_disciplina, descricao, carga_horaria, codigo, ativo
            FROM disciplina
            ORDER BY descricao
            """;

        List<Disciplina> disciplinas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                disciplinas.add(mapearDisciplina(rs));
            }
        }

        return disciplinas;
    }

    public boolean excluir(int idDisciplina) throws SQLException {
        final String sql = "DELETE FROM disciplina WHERE id_disciplina = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDisciplina);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir disciplina. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private Disciplina mapearDisciplina(ResultSet rs) throws SQLException {
        Disciplina disciplina = new Disciplina();
        disciplina.setIdDisciplina(rs.getInt("id_disciplina"));
        disciplina.setDescricao(rs.getString("descricao"));
        disciplina.setCargaHoraria(rs.getInt("carga_horaria"));
        disciplina.setCodigo(rs.getInt("codigo"));
        disciplina.setAtivo(rs.getBoolean("ativo"));
        return disciplina;
    }

    private void validarDisciplinaNaoNula(Disciplina disciplina) {
        if (disciplina == null) {
            throw new IllegalArgumentException("Disciplina não pode ser nula.");
        }
    }
}