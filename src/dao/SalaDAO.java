//Guilherme

package dao;

import model.Sala;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaDAO {

    private final Connection conn;

    public SalaDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public void inserir(Sala sala) throws SQLException {
        validarSalaNaoNula(sala);

        final String sql = "INSERT INTO sala (capacidade) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, sala.getCapacidade());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir sala. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    sala.setIdSala(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir sala. ID não retornado.");
                }
            }
        }
    }

    public void atualizar(Sala sala) throws SQLException {
        validarSalaNaoNula(sala);

        if (sala.getIdSala() <= 0) {
            throw new IllegalArgumentException("ID da sala inválido.");
        }

        final String sql = "UPDATE sala SET capacidade = ? WHERE id_sala = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sala.getCapacidade());
            stmt.setInt(2, sala.getIdSala());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar sala. Nenhuma linha afetada.");
            }
        }
    }

    public Sala buscarPorId(int idSala) throws SQLException {
        final String sql = "SELECT id_sala, capacidade FROM sala WHERE id_sala = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSala);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearSala(rs);
                }
                return null;
            }
        }
    }

    public List<Sala> listar() throws SQLException {
        final String sql = "SELECT id_sala, capacidade FROM sala ORDER BY id_sala";

        List<Sala> salas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                salas.add(mapearSala(rs));
            }
        }

        return salas;
    }

    public boolean excluir(int idSala) throws SQLException {
        final String sql = "DELETE FROM sala WHERE id_sala = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSala);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir sala. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private Sala mapearSala(ResultSet rs) throws SQLException {
        Sala sala = new Sala();
        sala.setIdSala(rs.getInt("id_sala"));
        sala.setCapacidade(rs.getInt("capacidade"));
        return sala;
    }

    private void validarSalaNaoNula(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("Sala não pode ser nula.");
        }
    }
}