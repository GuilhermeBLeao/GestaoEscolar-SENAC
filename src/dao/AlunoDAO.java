//Márcio e Guilherme

package dao;

import model.Aluno;
import model.Endereco;
import variaveisEnum.SexoEnum;
import variaveisEnum.SituacaoAluno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {

    private final Connection conn;

    public AlunoDAO(Connection conn) {
        if (conn == null) 
            throw new IllegalArgumentException("A conexão não pode ser nula.");
        this.conn = conn;
    }

    public void inserir(Aluno aluno) throws SQLException {
        validarAluno(aluno);

        int idEndereco = inserirEndereco(aluno.getEndereco());

        if (aluno.getMatricula() == null || aluno.getMatricula().trim().isEmpty()) {
            aluno.setMatricula(GeradorMatricula.gerar(conn));
        }

        String sql = """
            INSERT INTO aluno (
                nome,
                email,
                situacao,
                sexo,
                telefone,
                cpf,
                rg,
                obs_saude,
                data_nascimento,
                data_cadastro,
                matricula,
                id_pais,
                id_turma,
                id_endereco)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getSituacao().name());
            stmt.setString(4, aluno.getSexo().name());
            stmt.setString(5, aluno.getTelefone());
            stmt.setString(6, aluno.getCpf());
            stmt.setString(7, aluno.getRg());
            stmt.setString(8, aluno.getObsSaude());
            stmt.setDate(9, Date.valueOf(aluno.getDataNascimento()));
            stmt.setDate(10, Date.valueOf(aluno.getDataCadastro()));
            stmt.setString(11, aluno.getMatricula());
            stmt.setInt(12, aluno.getIdPais());
            stmt.setInt(13, aluno.getIdTurma());
            stmt.setInt(14, idEndereco);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir aluno.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    aluno.setIdAluno(rs.getInt(1));
                }
            }
        }

        aluno.getEndereco().setIdEndereco(idEndereco);
    }

    public void atualizar(Aluno aluno) throws SQLException {
        validarAluno(aluno);

        if (aluno.getIdAluno() <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido para atualização.");
        }

        if (aluno.getEndereco() == null || aluno.getEndereco().getIdEndereco() <= 0) {
            throw new IllegalArgumentException("Endereço do aluno inválido para atualização.");
        }

        atualizarEndereco(aluno.getEndereco());

        String sql = """
            UPDATE aluno
               SET nome = ?,
                   email = ?,
                   situacao = ?,
                   sexo = ?,
                   telefone = ?,
                   cpf = ?,
                   rg = ?,
                   obs_saude = ?,
                   data_nascimento = ?,
                   id_pais = ?,
                   id_turma = ?
             WHERE id_aluno = ?""";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getEmail());
            stmt.setString(3, aluno.getSituacao().name());
            stmt.setString(4, aluno.getSexo().name());
            stmt.setString(5, aluno.getTelefone());
            stmt.setString(6, aluno.getCpf());
            stmt.setString(7, aluno.getRg());
            stmt.setString(8, aluno.getObsSaude());
            stmt.setDate(9, Date.valueOf(aluno.getDataNascimento()));
            stmt.setInt(10, aluno.getIdPais());
            stmt.setInt(11, aluno.getIdTurma());
            stmt.setInt(12, aluno.getIdAluno());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Nenhum aluno foi atualizado.");
            }
        }
    }

    public boolean excluir(int idAluno) throws SQLException {
        if (idAluno <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        Integer idEndereco = buscarIdEnderecoPorAluno(idAluno);

        String sqlAluno = "DELETE FROM aluno WHERE id_aluno = ?";

        int linhasAfetadas;
        try (PreparedStatement stmt = conn.prepareStatement(sqlAluno)) {
            stmt.setInt(1, idAluno);
            linhasAfetadas = stmt.executeUpdate();
        }

        if (linhasAfetadas > 0 && idEndereco != null) {
            excluirEndereco(idEndereco);
        }

        return linhasAfetadas > 0;
    }

    public Aluno buscarPorId(int idAluno) throws SQLException {
        if (idAluno <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.rg,
                a.obs_saude,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.id_pais,
                a.id_turma,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            INNER JOIN endereco e
            ON e.id_endereco = a.id_endereco
            WHERE a.id_aluno = ? """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarAluno(rs);
                }
            }
        }

        return null;
    }

    public Aluno buscarPorCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.rg,
                a.obs_saude,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.id_pais,
                a.id_turma,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            INNER JOIN endereco e 
            ON e.id_endereco = a.id_endereco
            WHERE a.cpf = ? """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarAluno(rs);
                }
            }
        }

        return null;
    }

    public Aluno buscarPorMatricula(String matricula) throws SQLException {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("Matrícula inválida.");
        }

        String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.rg,
                a.obs_saude,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.id_pais,
                a.id_turma,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            INNER JOIN endereco e
            ON e.id_endereco = a.id_endereco
            WHERE a.matricula = ? """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarAluno(rs);
                }
            }
        }

        return null;
    }

    public List<Aluno> listarTodos() throws SQLException {
        List<Aluno> alunos = new ArrayList<>();

        String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.rg,
                a.obs_saude,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.id_pais,
                a.id_turma,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            INNER JOIN endereco e
            ON e.id_endereco = a.id_endereco
            ORDER BY a.nome """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alunos.add(montarAluno(rs));
            }
        }

        return alunos;
    }

    public boolean existeCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        String sql = "SELECT 1 FROM aluno WHERE cpf = ?"; //Confere se existe o cadastro deste CPF

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeMatricula(String matricula) throws SQLException {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("Matrícula inválida.");
        }

        String sql = "SELECT 1 FROM aluno WHERE matricula = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void validarAluno(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }
        if (aluno.getEndereco() == null) {
            throw new IllegalArgumentException("Endereço do aluno é obrigatório.");
        }
    }

    private int inserirEndereco(Endereco endereco) throws SQLException {
        String sql = """
            INSERT INTO endereco (
                rua,
                numero,
                complemento,
                bairro,
                cidade,
                estado,
                cep)
                VALUES (?, ?, ?, ?, ?, ?, ?) """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, endereco.getRua());
            stmt.setString(2, endereco.getNumero());
            stmt.setString(3, endereco.getComplemento());
            stmt.setString(4, endereco.getBairro());
            stmt.setString(5, endereco.getCidade());
            stmt.setString(6, endereco.getEstado());
            stmt.setString(7, endereco.getCep());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir endereço.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID do endereço inserido.");
    }

    private void atualizarEndereco(Endereco endereco) throws SQLException {
        String sql = """
            UPDATE endereco
               SET rua = ?,
                   numero = ?,
                   complemento = ?,
                   bairro = ?,
                   cidade = ?,
                   estado = ?,
                   cep = ?
             WHERE id_endereco = ? """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, endereco.getRua());
            stmt.setString(2, endereco.getNumero());
            stmt.setString(3, endereco.getComplemento());
            stmt.setString(4, endereco.getBairro());
            stmt.setString(5, endereco.getCidade());
            stmt.setString(6, endereco.getEstado());
            stmt.setString(7, endereco.getCep());
            stmt.setInt(8, endereco.getIdEndereco());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Nenhum endereço foi atualizado.");
            }
        }
    }

    private void excluirEndereco(int idEndereco) throws SQLException {
        String sql = "DELETE FROM endereco WHERE id_endereco = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEndereco);
            stmt.executeUpdate();
        }
    }

    private Integer buscarIdEnderecoPorAluno(int idAluno) throws SQLException {
        String sql = "SELECT id_endereco FROM aluno WHERE id_aluno = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_endereco");
                }
            }
        }

        return null;
    }

    private Aluno montarAluno(ResultSet rs) throws SQLException {
        Aluno aluno = new Aluno(rs.getString("matricula"));
        aluno.setIdAluno(rs.getInt("id_aluno"));
        aluno.setNome(rs.getString("nome"));
        aluno.setEmail(rs.getString("email"));
        aluno.setSituacao(SituacaoAluno.valueOf(rs.getString("situacao")));
        aluno.setSexo(SexoEnum.valueOf(rs.getString("sexo")));
        aluno.setTelefone(rs.getString("telefone"));
        aluno.setCpf(rs.getString("cpf"));
        aluno.setRg(rs.getString("rg"));
        aluno.setObsSaude(rs.getString("obs_saude"));

        Date dataNascimento = rs.getDate("data_nascimento");
        if (dataNascimento != null) {
            aluno.setDataNascimento(dataNascimento.toLocalDate());
        }

        Date dataCadastro = rs.getDate("data_cadastro");
        if (dataCadastro != null) {
            aluno.setDataCadastro(dataCadastro.toLocalDate());
        }

        aluno.setIdPais(rs.getInt("id_pais"));
        aluno.setIdTurma(rs.getInt("id_turma"));

        Endereco endereco = new Endereco();
        endereco.setIdEndereco(rs.getInt("id_endereco"));
        endereco.setRua(rs.getString("rua"));
        endereco.setNumero(rs.getString("numero"));
        endereco.setComplemento(rs.getString("complemento"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setCidade(rs.getString("cidade"));
        endereco.setEstado(rs.getString("estado"));
        endereco.setCep(rs.getString("cep"));

        aluno.setEndereco(endereco);

        return aluno;
    }
}