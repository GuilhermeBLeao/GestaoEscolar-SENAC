//Igor

package dao;

import model.PaisAluno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaisAlunoDAO {

    private final Connection conn;

    public PaisAlunoDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public boolean existeCpfMae(String cpfMae) throws SQLException {
        final String sql = "SELECT 1 FROM pais_aluno WHERE cpf_mae = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfMae);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeCpfPai(String cpfPai) throws SQLException {
        final String sql = "SELECT 1 FROM pais_aluno WHERE cpf_pai = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfPai);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void inserir(PaisAluno paisAluno) throws SQLException {
        validarPaisAlunoNaoNulo(paisAluno);

        final String sql = """
            INSERT INTO pais_aluno (
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherPaisAluno(stmt, paisAluno);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir pais/responsáveis. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    paisAluno.setIdPais(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir pais/responsáveis. ID não retornado.");
                }
            }
        }
    }

    public void atualizar(PaisAluno paisAluno) throws SQLException {
        validarPaisAlunoNaoNulo(paisAluno);

        if (paisAluno.getIdPais() <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        final String sql = """
            UPDATE pais_aluno
               SET nome_mae = ?,
                   nome_pai = ?,
                   email_mae = ?,
                   email_pai = ?,
                   telefone_mae = ?,
                   telefone_pai = ?
             WHERE id_pais = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paisAluno.getNomeMae());
            stmt.setString(2, paisAluno.getNomePai());
            stmt.setString(3, paisAluno.getEmailMae());
            stmt.setString(4, paisAluno.getEmailPai());
            stmt.setString(5, paisAluno.getTelefoneMae());
            stmt.setString(6, paisAluno.getTelefonePai());
            stmt.setInt(7, paisAluno.getIdPais());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar pais/responsáveis. Nenhuma linha afetada.");
            }
        }
    }

    public PaisAluno buscarPorCpfMae(String cpfMae) throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            WHERE cpf_mae = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfMae);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPaisAluno(rs);
                }
                return null;
            }
        }
    }

    public PaisAluno buscarPorCpfPai(String cpfPai) throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            WHERE cpf_pai = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfPai);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPaisAluno(rs);
                }
                return null;
            }
        }
    }

    public PaisAluno buscarPorId(int idPais) throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            WHERE id_pais = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPaisAluno(rs);
                }
                return null;
            }
        }
    }

    public List<PaisAluno> listar() throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            ORDER BY id_pais
            """;

        List<PaisAluno> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPaisAluno(rs));
            }
        }

        return lista;
    }

    public boolean excluir(int idPais) throws SQLException {
        final String sql = "DELETE FROM pais_aluno WHERE id_pais = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir pais/responsáveis. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private void preencherPaisAluno(PreparedStatement stmt, PaisAluno paisAluno) throws SQLException {
        stmt.setString(1, paisAluno.getNomeMae());
        stmt.setString(2, paisAluno.getNomePai());
        stmt.setString(3, paisAluno.getEmailMae());
        stmt.setString(4, paisAluno.getEmailPai());
        stmt.setString(5, paisAluno.getTelefoneMae());
        stmt.setString(6, paisAluno.getTelefonePai());
        stmt.setString(7, paisAluno.getCpfMae());
        stmt.setString(8, paisAluno.getCpfPai());
    }

    private PaisAluno mapearPaisAluno(ResultSet rs) throws SQLException {
        PaisAluno paisAluno = new PaisAluno();

        paisAluno.setIdPais(rs.getInt("id_pais"));
        paisAluno.setNomeMae(rs.getString("nome_mae"));
        paisAluno.setNomePai(rs.getString("nome_pai"));
        paisAluno.setEmailMae(rs.getString("email_mae"));
        paisAluno.setEmailPai(rs.getString("email_pai"));
        paisAluno.setTelefoneMae(rs.getString("telefone_mae"));
        paisAluno.setTelefonePai(rs.getString("telefone_pai"));
        paisAluno.setCpfMae(rs.getString("cpf_mae"));
        paisAluno.setCpfPai(rs.getString("cpf_pai"));

        return paisAluno;
    }

    private void validarPaisAlunoNaoNulo(PaisAluno paisAluno) {
        if (paisAluno == null) {
            throw new IllegalArgumentException("Pais/Responsáveis não podem ser nulos.");
        }
    }
}