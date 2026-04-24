//Igor

package dao;

import model.Endereco;
import model.Professor;
import variaveisEnum.Estado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDAO {

    private final Connection conn;

    public ProfessorDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public boolean existeCpf(String cpf) throws SQLException {
        final String sql = "SELECT 1 FROM professor WHERE cpf = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeRg(String rg) throws SQLException {
        final String sql = "SELECT 1 FROM professor WHERE rg = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rg);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void inserir(Professor professor) throws SQLException {
        validarProfessorNaoNulo(professor);
        validarEnderecoNaoNulo(professor.getEndereco());

        final String sql = """
            INSERT INTO professor (
                nome,
                cpf,
                formacao,
                telefone,
                rg,
                data_nascimento
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, professor.getNome());
            stmt.setString(2, professor.getCpf());
            stmt.setString(3, professor.getFormacao());
            stmt.setString(4, professor.getTelefone());
            stmt.setString(5, professor.getRg());
            stmt.setDate(6, Date.valueOf(professor.getDataNascimento()));

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir professor. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    professor.setIdProfessor(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir professor. ID não retornado.");
                }
            }
        }

        inserirEndereco(professor.getEndereco(), professor.getIdProfessor());
    }

    public void atualizar(Professor professor) throws SQLException {
        validarProfessorNaoNulo(professor);
        validarEnderecoNaoNulo(professor.getEndereco());

        if (professor.getIdProfessor() <= 0) {
            throw new IllegalArgumentException("ID do professor inválido.");
        }

        final String sql = """
            UPDATE professor
               SET nome = ?,
                   formacao = ?,
                   telefone = ?,
                   rg = ?,
                   data_nascimento = ?
             WHERE id_professor = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professor.getNome());
            stmt.setString(2, professor.getFormacao());
            stmt.setString(3, professor.getTelefone());
            stmt.setString(4, professor.getRg());
            stmt.setDate(5, Date.valueOf(professor.getDataNascimento()));
            stmt.setInt(6, professor.getIdProfessor());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar professor. Nenhuma linha afetada.");
            }
        }

        if (existeEnderecoDoProfessor(professor.getIdProfessor())) {
            atualizarEndereco(professor.getEndereco(), professor.getIdProfessor());
        } else {
            inserirEndereco(professor.getEndereco(), professor.getIdProfessor());
        }
    }

    public Professor buscarPorId(int idProfessor) throws SQLException {
        final String sql = """
            SELECT
                p.id_professor,
                p.nome,
                p.cpf,
                p.formacao,
                p.telefone,
                p.rg,
                p.data_nascimento,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM professor p
            LEFT JOIN endereco e ON e.professor_id = p.id_professor
            WHERE p.id_professor = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProfessor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProfessor(rs);
                }
                return null;
            }
        }
    }

    public Professor buscarPorCpf(String cpf) throws SQLException {
        final String sql = """
            SELECT
                p.id_professor,
                p.nome,
                p.cpf,
                p.formacao,
                p.telefone,
                p.rg,
                p.data_nascimento,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM professor p
            LEFT JOIN endereco e ON e.professor_id = p.id_professor
            WHERE p.cpf = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProfessor(rs);
                }
                return null;
            }
        }
    }

    public Professor buscarPorRg(String rg) throws SQLException {
        final String sql = """
            SELECT
                p.id_professor,
                p.nome,
                p.cpf,
                p.formacao,
                p.telefone,
                p.rg,
                p.data_nascimento,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM professor p
            LEFT JOIN endereco e ON e.professor_id = p.id_professor
            WHERE p.rg = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rg);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProfessor(rs);
                }
                return null;
            }
        }
    }

    public List<Professor> buscarPorNome(String nome) throws SQLException {
        final String sql = """
            SELECT
                p.id_professor,
                p.nome,
                p.cpf,
                p.formacao,
                p.telefone,
                p.rg,
                p.data_nascimento,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM professor p
            LEFT JOIN endereco e ON e.professor_id = p.id_professor
            WHERE LOWER(p.nome) LIKE LOWER(?)
            ORDER BY p.nome
            """;

        List<Professor> professores = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    professores.add(mapearProfessor(rs));
                }
            }
        }

        return professores;
    }

    public List<Professor> listar() throws SQLException {
        final String sql = """
            SELECT
                p.id_professor,
                p.nome,
                p.cpf,
                p.formacao,
                p.telefone,
                p.rg,
                p.data_nascimento,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM professor p
            LEFT JOIN endereco e ON e.professor_id = p.id_professor
            ORDER BY p.nome
            """;

        List<Professor> professores = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                professores.add(mapearProfessor(rs));
            }
        }

        return professores;
    }

    public boolean excluir(int idProfessor) throws SQLException {
        excluirEnderecoPorProfessorId(idProfessor);

        final String sql = "DELETE FROM professor WHERE id_professor = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProfessor);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir professor. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private void inserirEndereco(Endereco endereco, int professorId) throws SQLException {
        final String sql = """
            INSERT INTO endereco (
                rua,
                numero,
                complemento,
                bairro,
                cidade,
                estado,
                cep,
                professor_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherEndereco(stmt, endereco);
            stmt.setInt(8, professorId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir endereço do professor. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    endereco.setIdEndereco(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir endereço do professor. ID não retornado.");
                }
            }
        }
    }

    private void atualizarEndereco(Endereco endereco, int professorId) throws SQLException {
        final String sql = """
            UPDATE endereco
               SET rua = ?,
                   numero = ?,
                   complemento = ?,
                   bairro = ?,
                   cidade = ?,
                   estado = ?,
                   cep = ?
             WHERE professor_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherEndereco(stmt, endereco);
            stmt.setInt(8, professorId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar endereço do professor. Nenhuma linha afetada.");
            }
        }
    }

    private boolean existeEnderecoDoProfessor(int professorId) throws SQLException {
        final String sql = "SELECT 1 FROM endereco WHERE professor_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, professorId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void excluirEnderecoPorProfessorId(int professorId) throws SQLException {
        final String sql = "DELETE FROM endereco WHERE professor_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, professorId);
            stmt.executeUpdate();
        }
    }

    private void preencherEndereco(PreparedStatement stmt, Endereco endereco) throws SQLException {
        stmt.setString(1, endereco.getRua());
        stmt.setString(2, endereco.getNumero());
        stmt.setString(3, endereco.getComplemento());
        stmt.setString(4, endereco.getBairro());
        stmt.setString(5, endereco.getCidade());
        stmt.setString(6, endereco.getEstado().name());
        stmt.setString(7, endereco.getCep());
    }

    private Professor mapearProfessor(ResultSet rs) throws SQLException {
        Professor professor = new Professor();

        professor.setIdProfessor(rs.getInt("id_professor"));
        professor.setNome(rs.getString("nome"));
        professor.setCpf(rs.getString("cpf"));
        professor.setFormacao(rs.getString("formacao"));
        professor.setTelefone(rs.getString("telefone"));
        professor.setRg(rs.getString("rg"));
        professor.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());

        if (rs.getObject("id_endereco") != null) {
            professor.setEndereco(mapearEndereco(rs));
        }

        return professor;
    }

    private Endereco mapearEndereco(ResultSet rs) throws SQLException {
        Endereco endereco = new Endereco();

        endereco.setIdEndereco(rs.getInt("id_endereco"));
        endereco.setRua(rs.getString("rua"));
        endereco.setNumero(rs.getString("numero"));
        endereco.setComplemento(rs.getString("complemento"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setCidade(rs.getString("cidade"));
        endereco.setEstado(Estado.valueOf(rs.getString("estado")));
        endereco.setCep(rs.getString("cep"));

        return endereco;
    }

    private void validarProfessorNaoNulo(Professor professor) {
        if (professor == null) {
            throw new IllegalArgumentException("Professor não pode ser nulo.");
        }
    }

    private void validarEnderecoNaoNulo(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço do professor não pode ser nulo.");
        }
    }
}