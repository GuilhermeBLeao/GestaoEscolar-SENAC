//Arthur

package dao;

import model.Endereco;
import model.Funcionario;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import variaveisEnum.Perfil;

public class FuncionarioDAO {

    private final Connection conn;

    public FuncionarioDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public boolean existeCpf(String cpf) throws SQLException {
        final String sql = "SELECT 1 FROM funcionario WHERE cpf = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void inserir(Funcionario funcionario) throws SQLException {
        validarFuncionarioNaoNulo(funcionario);
        validarEnderecoNaoNulo(funcionario.getEndereco());

        final String sql = """
            INSERT INTO funcionario (
                nome,
                cpf,
                cargo,
                telefone,
                rg,
                sexo,
                setor,
                email,
                ativo,
                data_nascimento,
                data_contratacao,
                perfil
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherFuncionarioParaInsert(stmt, funcionario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir funcionário. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    funcionario.setIdFuncionario(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir funcionário. ID não retornado.");
                }
            }
        }

        inserirEndereco(funcionario.getEndereco(), funcionario.getIdFuncionario());
    }

    public void atualizar(Funcionario funcionario) throws SQLException {
        validarFuncionarioNaoNulo(funcionario);
        validarEnderecoNaoNulo(funcionario.getEndereco());

        if (funcionario.getIdFuncionario() <= 0) {
            throw new IllegalArgumentException("ID do funcionário inválido.");
        }

        final String sql = """
            UPDATE funcionario
               SET nome = ?,
                   cargo = ?,
                   telefone = ?,
                   rg = ?,
                   sexo = ?,
                   setor = ?,
                   email = ?,
                   ativo = ?,
                   data_nascimento = ?,
                   data_contratacao = ?,
                   perfil = ?
             WHERE id_funcionario = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherFuncionarioParaUpdate(stmt, funcionario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar funcionário. Nenhuma linha afetada.");
            }
        }

        if (existeEnderecoDoFuncionario(funcionario.getIdFuncionario())) {
            atualizarEndereco(funcionario.getEndereco(), funcionario.getIdFuncionario());
        } else {
            inserirEndereco(funcionario.getEndereco(), funcionario.getIdFuncionario());
        }
    }

    public Funcionario buscarPorId(int idFuncionario) throws SQLException {
        final String sql = """
            SELECT
                f.id_funcionario,
                f.nome,
                f.cpf,
                f.cargo,
                f.telefone,
                f.rg,
                f.sexo,
                f.setor,
                f.email,
                f.ativo,
                f.data_nascimento,
                f.data_contratacao,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep,
                f.perfil
            FROM funcionario f
            LEFT JOIN endereco e ON e.funcionario_id = f.id_funcionario
            WHERE f.id_funcionario = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFuncionario(rs);
                }
                return null;
            }
        }
    }

    public Funcionario buscarPorCpf(String cpf) throws SQLException {
        final String sql = """
            SELECT
                f.id_funcionario,
                f.nome,
                f.cpf,
                f.cargo,
                f.telefone,
                f.rg,
                f.sexo,
                f.setor,
                f.email,
                f.ativo,
                f.data_nascimento,
                f.data_contratacao,
                f.perfil,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM funcionario f
            LEFT JOIN endereco e ON e.funcionario_id = f.id_funcionario
            WHERE f.cpf = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFuncionario(rs);
                }
                return null;
            }
        }
    }

    public List<Funcionario> buscarPorNome(String nome) throws SQLException {
        final String sql = """
            SELECT
                f.id_funcionario,
                f.nome,
                f.cpf,
                f.cargo,
                f.telefone,
                f.rg,
                f.sexo,
                f.setor,
                f.email,
                f.ativo,
                f.data_nascimento,
                f.data_contratacao,
                f.perfil,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM funcionario f
            LEFT JOIN endereco e ON e.funcionario_id = f.id_funcionario
            WHERE LOWER(f.nome) LIKE LOWER(?)
            ORDER BY f.nome
            """;

        List<Funcionario> funcionarios = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    funcionarios.add(mapearFuncionario(rs));
                }
            }
        }

        return funcionarios;
    }

    public List<Funcionario> listar() throws SQLException {
        final String sql = """
            SELECT
                f.id_funcionario,
                f.nome,
                f.cpf,
                f.cargo,
                f.telefone,
                f.rg,
                f.sexo,
                f.setor,
                f.email,
                f.ativo,
                f.data_nascimento,
                f.data_contratacao,
                f.perfil,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM funcionario f
            LEFT JOIN endereco e ON e.funcionario_id = f.id_funcionario
            ORDER BY f.nome
            """;

        List<Funcionario> funcionarios = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                funcionarios.add(mapearFuncionario(rs));
            }
        }

        return funcionarios;
    }

    public boolean excluir(int idFuncionario) throws SQLException {
        excluirEnderecoPorFuncionarioId(idFuncionario);

        final String sql = "DELETE FROM funcionario WHERE id_funcionario = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFuncionario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir funcionário. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private void preencherFuncionarioParaInsert(PreparedStatement stmt, Funcionario funcionario) throws SQLException {
        stmt.setString(1, funcionario.getNome());
        stmt.setString(2, funcionario.getCpf());
        stmt.setString(3, funcionario.getCargo());
        stmt.setString(4, funcionario.getTelefone());
        stmt.setString(5, funcionario.getRg());
        stmt.setString(6, funcionario.getSexo().name());
        stmt.setString(7, funcionario.getSetor());
        stmt.setString(8, funcionario.getEmail());
        stmt.setBoolean(9, funcionario.isAtivo());
        stmt.setDate(10, Date.valueOf(funcionario.getDataNascimento()));
        stmt.setDate(11, Date.valueOf(funcionario.getDataContratacao()));
        stmt.setString(12, funcionario.getPerfil().name());
    }

    private void preencherFuncionarioParaUpdate(PreparedStatement stmt, Funcionario funcionario) throws SQLException {
        stmt.setString(1, funcionario.getNome());
        stmt.setString(2, funcionario.getCargo());
        stmt.setString(3, funcionario.getTelefone());
        stmt.setString(4, funcionario.getRg());
        stmt.setString(5, funcionario.getSexo().name());
        stmt.setString(6, funcionario.getSetor());
        stmt.setString(7, funcionario.getEmail());
        stmt.setBoolean(8, funcionario.isAtivo());
        stmt.setDate(9, Date.valueOf(funcionario.getDataNascimento()));
        stmt.setDate(10, Date.valueOf(funcionario.getDataContratacao()));
        stmt.setString(11, funcionario.getPerfil().name());
        stmt.setInt(12, funcionario.getIdFuncionario());

    }

    private void inserirEndereco(Endereco endereco, int funcionarioId) throws SQLException {
        final String sql = """
            INSERT INTO endereco (
                rua,
                numero,
                complemento,
                bairro,
                cidade,
                estado,
                cep,
                funcionario_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherEndereco(stmt, endereco);
            stmt.setInt(8, funcionarioId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir endereço do funcionário. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    endereco.setIdEndereco(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir endereço do funcionário. ID não retornado.");
                }
            }
        }
    }

    private void atualizarEndereco(Endereco endereco, int funcionarioId) throws SQLException {
        final String sql = """
            UPDATE endereco
               SET rua = ?,
                   numero = ?,
                   complemento = ?,
                   bairro = ?,
                   cidade = ?,
                   estado = ?,
                   cep = ?
             WHERE funcionario_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherEndereco(stmt, endereco);
            stmt.setInt(8, funcionarioId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar endereço do funcionário. Nenhuma linha afetada.");
            }
        }
    }

    private boolean existeEnderecoDoFuncionario(int funcionarioId) throws SQLException {
        final String sql = "SELECT 1 FROM endereco WHERE funcionario_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, funcionarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void excluirEnderecoPorFuncionarioId(int funcionarioId) throws SQLException {
        final String sql = "DELETE FROM endereco WHERE funcionario_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, funcionarioId);
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

    private Funcionario mapearFuncionario(ResultSet rs) throws SQLException {
        Funcionario funcionario = new Funcionario();

        funcionario.setIdFuncionario(rs.getInt("id_funcionario"));
        funcionario.setNome(rs.getString("nome"));
        funcionario.setCpf(rs.getString("cpf"));
        funcionario.setCargo(rs.getString("cargo"));
        funcionario.setTelefone(rs.getString("telefone"));
        funcionario.setRg(rs.getString("rg"));
        funcionario.setSexo(Sexo.valueOf(rs.getString("sexo")));
        funcionario.setSetor(rs.getString("setor"));
        funcionario.setEmail(rs.getString("email"));
        funcionario.setAtivo(rs.getBoolean("ativo"));
        funcionario.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
        funcionario.setDataContratacao(rs.getDate("data_contratacao").toLocalDate());

        if (rs.getObject("id_endereco") != null) {
            funcionario.setEndereco(mapearEndereco(rs));
        }

        String perfilStr = rs.getString("perfil");
    if (perfilStr != null) {
        funcionario.setPerfil(Perfil.valueOf(perfilStr));
    }

        return funcionario;
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

    private void validarFuncionarioNaoNulo(Funcionario funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não pode ser nulo.");
        }
    }

    private void validarEnderecoNaoNulo(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço do funcionário não pode ser nulo.");
        }
    }
}