//Arthur

package dao;

import model.Funcionario;
import variaveisEnum.SexoEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    private final Connection conn;

    public FuncionarioDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("A conexão não pode ser nula.");
        }
        this.conn = conn;
    }

    public void inserir(Funcionario funcionario) throws SQLException {
        validarFuncionario(funcionario);

        String sql = """
            INSERT INTO funcionarios
                (nome, cpf, cargo, telefone, ativo, rg, data_nascimento, sexo, setor, data_contratacao, email)
            VALUES
                (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getCpf());
            stmt.setString(3, funcionario.getCargo());
            stmt.setString(4, funcionario.getTelefone());
            stmt.setBoolean(5, funcionario.isAtivo());
            stmt.setString(6, funcionario.getRg());
            stmt.setDate(7, Date.valueOf(funcionario.getDataNascimento()));
            stmt.setString(8, funcionario.getSexo().name());
            stmt.setString(9, funcionario.getSetor());
            stmt.setDate(10, Date.valueOf(funcionario.getDataContratacao()));
            stmt.setString(11, funcionario.getEmail());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir funcionário.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    funcionario.setIdFuncionario(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Funcionario funcionario) throws SQLException {
        validarFuncionario(funcionario);

        if (funcionario.getIdFuncionario() <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido para atualização.");
        }

        String sql = """
            UPDATE funcionarios
               SET nome = ?,
                   cpf = ?,
                   cargo = ?,
                   telefone = ?,
                   ativo = ?,
                   rg = ?,
                   data_nascimento = ?,
                   sexo = ?,
                   setor = ?,
                   data_contratacao = ?,
                   email = ?
             WHERE id_funcionarios = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getCpf());
            stmt.setString(3, funcionario.getCargo());
            stmt.setString(4, funcionario.getTelefone());
            stmt.setBoolean(5, funcionario.isAtivo());
            stmt.setString(6, funcionario.getRg());
            stmt.setDate(7, Date.valueOf(funcionario.getDataNascimento()));
            stmt.setString(8, funcionario.getSexo().name());
            stmt.setString(9, funcionario.getSetor());
            stmt.setDate(10, Date.valueOf(funcionario.getDataContratacao()));
            stmt.setString(11, funcionario.getEmail());
            stmt.setInt(12, funcionario.getIdFuncionario());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Nenhum funcionário foi atualizado.");
            }
        }
    }

    public boolean excluir(int idFuncionario) throws SQLException {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        String sql = "DELETE FROM funcionarios WHERE id_funcionarios = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);
            return stmt.executeUpdate() > 0;
        }
    }

    public Funcionario buscarPorId(int idFuncionario) throws SQLException {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        String sql = """
            SELECT id_funcionarios, nome, cpf, cargo, telefone, ativo, rg,
                   data_nascimento, sexo, setor, data_contratacao, email
              FROM funcionarios
             WHERE id_funcionarios = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarFuncionario(rs);
                }
            }
        }

        return null;
    }

    public Funcionario buscarPorCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        String sql = """
            SELECT id_funcionarios, nome, cpf, cargo, telefone, ativo, rg,
                   data_nascimento, sexo, setor, data_contratacao, email
              FROM funcionarios
             WHERE cpf = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarFuncionario(rs);
                }
            }
        }

        return null;
    }

    public List<Funcionario> buscarPorNome(String nome) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome inválido.");
        }

        List<Funcionario> lista = new ArrayList<>();

        String sql = """
            SELECT id_funcionarios, nome, cpf, cargo, telefone, ativo, rg,
                   data_nascimento, sexo, setor, data_contratacao, email
              FROM funcionarios
             WHERE nome LIKE ?
             ORDER BY nome
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome.trim() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarFuncionario(rs));
                }
            }
        }

        return lista;
    }

    public List<Funcionario> listar() throws SQLException {
        List<Funcionario> lista = new ArrayList<>();

        String sql = """
            SELECT id_funcionarios, nome, cpf, cargo, telefone, ativo, rg,
                   data_nascimento, sexo, setor, data_contratacao, email
              FROM funcionarios
             ORDER BY nome
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(montarFuncionario(rs));
            }
        }

        return lista;
    }

    public boolean existeCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        String sql = "SELECT 1 FROM funcionarios WHERE cpf = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void validarFuncionario(Funcionario funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não pode ser nulo.");
        }

        if (funcionario.getEndereco() == null) {
            throw new IllegalArgumentException("Endereço do funcionário é obrigatório.");
        }
    }

    private Funcionario montarFuncionario(ResultSet rs) throws SQLException {
        Funcionario funcionario = new Funcionario();

        funcionario.setIdFuncionario(rs.getInt("id_funcionarios"));
        funcionario.setNome(rs.getString("nome"));
        funcionario.setCpf(rs.getString("cpf"));
        funcionario.setCargo(rs.getString("cargo"));
        funcionario.setTelefone(rs.getString("telefone"));
        funcionario.setAtivo(rs.getBoolean("ativo"));
        funcionario.setRg(rs.getString("rg"));
        funcionario.setSexo(SexoEnum.valueOf(rs.getString("sexo")));
        funcionario.setSetor(rs.getString("setor"));
        funcionario.setEmail(rs.getString("email"));

        Date dataNascimento = rs.getDate("data_nascimento");
        if (dataNascimento != null) {
            funcionario.setDataNascimento(dataNascimento.toLocalDate());
        }

        Date dataContratacao = rs.getDate("data_contratacao");
        if (dataContratacao != null) {
            funcionario.setDataContratacao(dataContratacao.toLocalDate());
        }

        return funcionario;
    }
}