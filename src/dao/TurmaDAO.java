//Igor

package dao;

import database.ConnectionFactory;
import model.Turma;
import variaveisEnum.Turno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    private static final String INSERT_SQL =
            "INSERT INTO turma (sala_id, descricao_turma, turno) VALUES (?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE turma SET sala_id = ?, descricao_turma = ?, turno = ? WHERE id_turma = ?";

    private static final String DELETE_SQL =
            "DELETE FROM turma WHERE id_turma = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT id_turma, sala_id, descricao_turma, turno FROM turma";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id_turma, sala_id, descricao_turma, turno FROM turma WHERE id_turma = ?";

    private Connection getConnection() throws SQLException {
        Connection conn = ConnectionFactory.getConnection();

        if (conn == null) {
            throw new SQLException("Não foi possível estabelecer conexão com o banco de dados.");
        }

        return conn;
    }

    public void inserir(Turma turma) {
        validarTurma(turma);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatementTurma(stmt, turma);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir turma. Nenhuma linha foi afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    turma.setIdTurma(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir turma no banco de dados.", e);
        }
    }

    public void atualizar(Turma turma) {
        validarTurma(turma);

        if (turma.getIdTurma() <= 0) {
            throw new IllegalArgumentException("ID da turma é obrigatório para atualização.");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setInt(1, turma.getSalaId());
            stmt.setString(2, turma.getDescricaoTurma());
            stmt.setString(3, turma.getTurno().name());
            stmt.setInt(4, turma.getIdTurma());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new RuntimeException("Nenhuma turma foi atualizada. Verifique se o ID existe.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar turma no banco de dados.", e);
        }
    }

    public void excluir(int idTurma) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido para exclusão.");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, idTurma);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new RuntimeException("Nenhuma turma foi excluída. Verifique se o ID existe.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir turma do banco de dados.", e);
        }
    }

    public List<Turma> listar() {
        List<Turma> lista = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(montarTurma(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar turmas.", e);
        }

        return lista;
    }

    public Turma buscarPorId(int idTurma) {
        if (idTurma <= 0) {
            throw new IllegalArgumentException("ID da turma inválido para busca.");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, idTurma);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarTurma(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar turma por ID.", e);
        }

        return null;
    }

    private void preencherStatementTurma(PreparedStatement stmt, Turma turma) throws SQLException {
        stmt.setInt(1, turma.getSalaId());
        stmt.setString(2, turma.getDescricaoTurma());
        stmt.setString(3, turma.getTurno().name());
    }

    private Turma montarTurma(ResultSet rs) throws SQLException {
        Turma turma = new Turma();
        turma.setIdTurma(rs.getInt("id_turma"));
        turma.setSalaId(rs.getInt("sala_id"));
        turma.setDescricaoTurma(rs.getString("descricao_turma"));
        turma.setTurno(Turno.valueOf(rs.getString("turno")));
        return turma;
    }

    private void validarTurma(Turma turma) {
        if (turma == null) {
            throw new IllegalArgumentException("A turma não pode ser nula.");
        }

        if (turma.getSalaId() <= 0) {
            throw new IllegalArgumentException("Sala da turma é obrigatória.");
        }

        if (turma.getDescricaoTurma() == null || turma.getDescricaoTurma().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da turma é obrigatória.");
        }

        if (turma.getTurno() == null) {
            throw new IllegalArgumentException("Turno da turma é obrigatório.");
        }
    }
}