//Igor

package dao;

import model.Turma;
import variaveisEnum.Turno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    private final Connection conn;

    public TurmaDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public void inserir(Turma turma) throws SQLException {
        validarTurmaNaoNula(turma);

        final String sql = """
            INSERT INTO turma (
                sala_id,
                descricao_turma,
                turno
            ) VALUES (?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, turma.getSalaId());
            stmt.setString(2, turma.getDescricaoTurma());
            stmt.setString(3, turma.getTurno().name());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir turma. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    turma.setIdTurma(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir turma. ID não retornado.");
                }
            }
        }
    }

    public void atualizar(Turma turma) throws SQLException {
        validarTurmaNaoNula(turma);

        if (turma.getIdTurma() <= 0) {
            throw new IllegalArgumentException("ID da turma inválido.");
        }

        final String sql = """
            UPDATE turma
               SET sala_id = ?,
                   descricao_turma = ?,
                   turno = ?
             WHERE id_turma = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, turma.getSalaId());
            stmt.setString(2, turma.getDescricaoTurma());
            stmt.setString(3, turma.getTurno().name());
            stmt.setInt(4, turma.getIdTurma());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar turma. Nenhuma linha afetada.");
            }
        }
    }

    public Turma buscarPorId(int idTurma) throws SQLException {
        final String sql = """
            SELECT id_turma, sala_id, descricao_turma, turno
            FROM turma
            WHERE id_turma = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTurma);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearTurma(rs);
                }
                return null;
            }
        }
    }

    public List<Turma> listar() throws SQLException {
        final String sql = """
            SELECT id_turma, sala_id, descricao_turma, turno
            FROM turma
            ORDER BY descricao_turma
            """;

        List<Turma> turmas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                turmas.add(mapearTurma(rs));
            }
        }

        return turmas;
    }

    public boolean excluir(int idTurma) throws SQLException {
        final String sql = "DELETE FROM turma WHERE id_turma = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTurma);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir turma. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private Turma mapearTurma(ResultSet rs) throws SQLException {
        Turma turma = new Turma();
        turma.setIdTurma(rs.getInt("id_turma"));
        turma.setSalaId(rs.getInt("sala_id"));
        turma.setDescricaoTurma(rs.getString("descricao_turma"));
        turma.setTurno(Turno.valueOf(rs.getString("turno")));
        return turma;
    }

    private void validarTurmaNaoNula(Turma turma) {
        if (turma == null) {
            throw new IllegalArgumentException("Turma não pode ser nula.");
        }
    }
}