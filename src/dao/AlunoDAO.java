//Guilherme e Igor

package dao;

import model.Aluno;
import model.Endereco;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;
import variaveisEnum.SituacaoAluno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {

    private final Connection conn;

    public AlunoDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    public boolean existeCpf(String cpf) throws SQLException {
        final String sql = "SELECT 1 FROM aluno WHERE cpf = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void inserir(Aluno aluno) throws SQLException {
        validarAlunoNaoNulo(aluno);
        validarEnderecoNaoNulo(aluno.getEndereco());

        final String sql = """
            INSERT INTO aluno (
                nome,
                email,
                situacao,
                sexo,
                telefone,
                cpf,
                data_nascimento,
                data_cadastro,
                matricula,
                rg,
                obs_saude,
                pais_id,
                turma_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherAlunoParaInsert(stmt, aluno);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir aluno. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    aluno.setIdAluno(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir aluno. ID não retornado.");
                }
            }
        }

        inserirEndereco(aluno.getEndereco(), aluno.getIdAluno());
    }

    public void atualizar(Aluno aluno) throws SQLException {
        validarAlunoNaoNulo(aluno);
        validarEnderecoNaoNulo(aluno.getEndereco());

        if (aluno.getIdAluno() <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        final String sql = """
            UPDATE aluno
               SET nome = ?,
                   email = ?,
                   situacao = ?,
                   sexo = ?,
                   telefone = ?,
                   rg = ?,
                   obs_saude = ?,
                   data_nascimento = ?,
                   pais_id = ?,
                   turma_id = ?
             WHERE id_aluno = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherAlunoParaUpdate(stmt, aluno);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar aluno. Nenhuma linha afetada.");
            }
        }

        if (existeEnderecoDoAluno(aluno.getIdAluno())) {
            atualizarEndereco(aluno.getEndereco(), aluno.getIdAluno());
        } else {
            inserirEndereco(aluno.getEndereco(), aluno.getIdAluno());
        }
    }

    public Aluno buscarPorId(int idAluno) throws SQLException {
        final String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.rg,
                a.obs_saude,
                a.pais_id,
                a.turma_id,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            LEFT JOIN endereco e ON e.aluno_id = a.id_aluno
            WHERE a.id_aluno = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearAluno(rs);
                }
                return null;
            }
        }
    }

    public Aluno buscarPorCpf(String cpf) throws SQLException {
        final String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.rg,
                a.obs_saude,
                a.pais_id,
                a.turma_id,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            LEFT JOIN endereco e ON e.aluno_id = a.id_aluno
            WHERE a.cpf = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearAluno(rs);
                }
                return null;
            }
        }
    }

    public Aluno buscarPorMatricula(String matricula) throws SQLException {
        final String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.rg,
                a.obs_saude,
                a.pais_id,
                a.turma_id,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            LEFT JOIN endereco e ON e.aluno_id = a.id_aluno
            WHERE a.matricula = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearAluno(rs);
                }
                return null;
            }
        }
    }
    
    public List<Aluno> listarPorTurma(int idTurma) throws SQLException{
    		if(idTurma <= 0)
    			throw new IllegalArgumentException("ID da turma inválido");
    		final String sql = """
    				SELECT
    					a.id_aluno,
    					a.nome,
    					a.email,
    					a.situacao,
    					a.sexo,
    					a.telefone,
    					a.cpf,
    					a.data_nascimento,
    					a.data_cadastro,
    					a.matricula,
    					a.rg,
    					a.obs_saude,
    					a.pais_id,
    					a.turma_id,
    					e.id_endereco,
    				    e.rua,
    				    e.numero,
    				    e.complemento,
    				    e.bairro,
    				    e.cidade,
    				    e.estado,
    				    e.cep
    				    FROM aluno a
    				    LEFT JOIN endereco e
    				    ON e.aluno_id = a.id_aluno
    				    WHERE a.turma_id = ?
    				    ORDER BY a.nome
    				""";
    		List<Aluno> alunos = new ArrayList<>();
    		
    		try(PreparedStatement stmt = conn.prepareStatement(sql)){
    			stmt.setInt(1, idTurma);
    			
    			try(ResultSet rs = stmt.executeQuery()){
    				while(rs.next()) {
    					alunos.add(mapearAluno(rs));
    				}
    			}
    		}
    		return alunos;
    }

    public List<Aluno> listar() throws SQLException {
        final String sql = """
            SELECT
                a.id_aluno,
                a.nome,
                a.email,
                a.situacao,
                a.sexo,
                a.telefone,
                a.cpf,
                a.data_nascimento,
                a.data_cadastro,
                a.matricula,
                a.rg,
                a.obs_saude,
                a.pais_id,
                a.turma_id,
                e.id_endereco,
                e.rua,
                e.numero,
                e.complemento,
                e.bairro,
                e.cidade,
                e.estado,
                e.cep
            FROM aluno a
            LEFT JOIN endereco e ON e.aluno_id = a.id_aluno
            ORDER BY a.nome
            """;

        List<Aluno> alunos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alunos.add(mapearAluno(rs));
            }
        }

        return alunos;
    }

    public boolean excluir(int idAluno) throws SQLException {
        excluirEnderecoPorAlunoId(idAluno);

        final String sql = "DELETE FROM aluno WHERE id_aluno = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao excluir aluno. Nenhuma linha afetada.");
            }

            return true;
        }
    }

    private void preencherAlunoParaInsert(PreparedStatement stmt, Aluno aluno) throws SQLException {
        stmt.setString(1, aluno.getNome());
        stmt.setString(2, aluno.getEmail());
        stmt.setString(3, aluno.getSituacao().name());
        stmt.setString(4, aluno.getSexo().name());
        stmt.setString(5, aluno.getTelefone());
        stmt.setString(6, aluno.getCpf());
        stmt.setDate(7, Date.valueOf(aluno.getDataNascimento()));
        stmt.setDate(8, Date.valueOf(aluno.getDataCadastro()));
        stmt.setString(9, aluno.getMatricula());
        stmt.setString(10, aluno.getRg());
        stmt.setString(11, aluno.getObsSaude());
        stmt.setInt(12, aluno.getIdPais());
        stmt.setInt(13, aluno.getIdTurma());
    }

    private void preencherAlunoParaUpdate(PreparedStatement stmt, Aluno aluno) throws SQLException {
        stmt.setString(1, aluno.getNome());
        stmt.setString(2, aluno.getEmail());
        stmt.setString(3, aluno.getSituacao().name());
        stmt.setString(4, aluno.getSexo().name());
        stmt.setString(5, aluno.getTelefone());
        stmt.setString(6, aluno.getRg());
        stmt.setString(7, aluno.getObsSaude());
        stmt.setDate(8, Date.valueOf(aluno.getDataNascimento()));
        stmt.setInt(9, aluno.getIdPais());
        stmt.setInt(10, aluno.getIdTurma());
        stmt.setInt(11, aluno.getIdAluno());
    }

    private void inserirEndereco(Endereco endereco, int alunoId) throws SQLException {
        final String sql = """
            INSERT INTO endereco (
                rua,
                numero,
                complemento,
                bairro,
                cidade,
                estado,
                cep,
                aluno_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherEndereco(stmt, endereco);
            stmt.setInt(8, alunoId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir endereço do aluno. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    endereco.setIdEndereco(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir endereço do aluno. ID não retornado.");
                }
            }
        }
    }

    private void atualizarEndereco(Endereco endereco, int alunoId) throws SQLException {
        final String sql = """
            UPDATE endereco
               SET rua = ?,
                   numero = ?,
                   complemento = ?,
                   bairro = ?,
                   cidade = ?,
                   estado = ?,
                   cep = ?
             WHERE aluno_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherEndereco(stmt, endereco);
            stmt.setInt(8, alunoId);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar endereço do aluno. Nenhuma linha afetada.");
            }
        }
    }

    private boolean existeEnderecoDoAluno(int alunoId) throws SQLException {
        final String sql = "SELECT 1 FROM endereco WHERE aluno_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alunoId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void excluirEnderecoPorAlunoId(int alunoId) throws SQLException {
        final String sql = "DELETE FROM endereco WHERE aluno_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alunoId);
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

    private Aluno mapearAluno(ResultSet rs) throws SQLException {
        Aluno aluno = new Aluno();

        aluno.setIdAluno(rs.getInt("id_aluno"));
        aluno.setNome(rs.getString("nome"));
        aluno.setEmail(rs.getString("email"));
        aluno.setSituacao(SituacaoAluno.valueOf(rs.getString("situacao")));
        aluno.setSexo(Sexo.valueOf(rs.getString("sexo")));
        aluno.setTelefone(rs.getString("telefone"));
        aluno.setCpf(rs.getString("cpf"));
        Date dataNascimento = rs.getDate("data_nascimento");
        if (dataNascimento != null) {
        	aluno.setDataNascimento(dataNascimento.toLocalDate());
        }
        Date dataCadastro = rs.getDate("data_cadastro");
        if(dataCadastro != null) {
        	aluno.setDataCadastro(dataCadastro.toLocalDate());
        }
        aluno.setMatricula(rs.getString("matricula"));
        aluno.setRg(rs.getString("rg"));
        aluno.setObsSaude(rs.getString("obs_saude"));
        aluno.setIdPais(rs.getInt("pais_id"));
        aluno.setIdTurma(rs.getInt("turma_id"));

        if (rs.getObject("id_endereco") != null) {
            aluno.setEndereco(mapearEndereco(rs));
        }

        return aluno;
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

    private void validarAlunoNaoNulo(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }
    }

    private void validarEnderecoNaoNulo(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço do aluno não pode ser nulo.");
        }
    }
}