package dao;

import model.TurmaDisciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TurmaDisciplinaDAO {
    private final Connection conn;

    public TurmaDisciplinaDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public void inserir(TurmaDisciplina turmaDisciplina) throws SQLException {
        validarTurmaDisciplinaNaoNula(turmaDisciplina);

        final String sql = "INSERT INTO turma_disciplina (turma_id, disciplina_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, turmaDisciplina.getTurmaId());
            stmt.setInt(2, turmaDisciplina.getDisciplinaId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir vinculo entre turma e disciplina.");
            }
        }
    }

    public boolean existeVinculo(int idTurma, int idDisciplina) throws SQLException {
        validarIds(idTurma, idDisciplina);

        final String sql = "SELECT 1 FROM turma_disciplina WHERE turma_id = ? AND disciplina_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTurma);
            stmt.setInt(2, idDisciplina);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<TurmaDisciplina> listarPorTurma(int idTurma) throws SQLException {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma invalido.");
        }

        final String sql = "SELECT turma_id, disciplina_id FROM turma_disciplina WHERE turma_id = ? ORDER BY disciplina_id";
        List<TurmaDisciplina> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTurma);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearTurmaDisciplina(rs));
                }
            }
        }
        return lista;
    }

    public List<TurmaDisciplina> listarPorDisciplina(int idDisciplina) throws SQLException {
        if (idDisciplina <= 0) {
            throw new IllegalArgumentException("ID da disciplina invalido.");
        }

        final String sql = "SELECT turma_id, disciplina_id FROM turma_disciplina WHERE disciplina_id = ? ORDER BY turma_id";

        List<TurmaDisciplina> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDisciplina);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearTurmaDisciplina(rs));
                }
            }
        }
        return lista;
    }

    public boolean excluir(int idTurma, int idDisciplina) throws SQLException {
        validarIds(idTurma, idDisciplina);

        final String sql = "DELETE FROM turma_disciplina WHERE turma_id = ? AND disciplina_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTurma);
            stmt.setInt(2, idDisciplina);
            return stmt.executeUpdate() > 0;
        }
    }

    private TurmaDisciplina mapearTurmaDisciplina(ResultSet rs) throws SQLException {
        TurmaDisciplina turmaDisciplina = new TurmaDisciplina();
        turmaDisciplina.setTurmaId(rs.getInt("turma_id"));
        turmaDisciplina.setDisciplinaId(rs.getInt("disciplina_id"));
        return turmaDisciplina;
    }

    private void validarTurmaDisciplinaNaoNula(TurmaDisciplina turmaDisciplina) {
        if (turmaDisciplina == null) {
            throw new IllegalArgumentException("Vinculo entre turma e disciplina nao pode ser nulo.");
        }
    }

    private void validarIds(int idTurma, int idDisciplina) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma invalido.");
        }
        if (idDisciplina <= 0) {
            throw new IllegalArgumentException("ID da disciplina invalido.");
        }
    }
}