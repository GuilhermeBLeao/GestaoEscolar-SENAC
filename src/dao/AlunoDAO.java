//Guilherme e Igor

//Define o pacote da classe
package dao;

//Importação das classes de outros pacotes
import model.Aluno;
import model.Endereco;
import util.ValidaCPF;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;
import variaveisEnum.SituacaoAluno;

//Importação de bibliotecas
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO{
    private final Connection conn;

    public AlunoDAO(Connection conn){
        if(conn == null){
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

//Verificação de existência de CPF
    public boolean existeCpf(String cpf) throws SQLException{
    		String cpfTratado = cpf.trim().replaceAll("\\D", "");
    		if(!ValidaCPF.isValido(cpfTratado)) {
    			throw new IllegalArgumentException("CPF inválido.");
    		}
        final String sql = "SELECT 1 FROM aluno WHERE cpf = ?";

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,cpfTratado);

            try(ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }
        }
    }

//Inserção do aluno no banco
    public void inserir(Aluno aluno) throws SQLException{
        validarAlunoNaoNulo(aluno);
        validarEnderecoNaoNulo(aluno.getEndereco());

        final String sql = """
            INSERT INTO aluno
            (nome,
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
            turma_id,
            ativo)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,true)
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            preencherAlunoParaInsert(stmt, aluno);

            int linhasAfetadas = stmt.executeUpdate();
            if(linhasAfetadas == 0){
                throw new SQLException("Falha ao inserir aluno. Nenhuma linha afetada.");
            }

            try(ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()){
                    aluno.setIdAluno(rs.getInt(1));
                }else{
                    throw new SQLException("Falha ao inserir aluno. ID não retornado.");
                }
            }
        }
        inserirEndereco(aluno.getEndereco(), aluno.getIdAluno());
    }

//Atualização do aluno
    public void atualizar(Aluno aluno) throws SQLException{
        validarAlunoNaoNulo(aluno);
        validarEnderecoNaoNulo(aluno.getEndereco());

        if(aluno.getIdAluno()  <= 0){
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
            AND ativo = true
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            preencherAlunoParaUpdate(stmt, aluno);

            int linhasAfetadas = stmt.executeUpdate();
            if(linhasAfetadas == 0){
                verificarFalhaAtualizacao(aluno.getIdAluno());
            }
        }
        if(existeEnderecoDoAluno(aluno.getIdAluno())) {
            atualizarEndereco(aluno.getEndereco(), aluno.getIdAluno());
        }else{
            inserirEndereco(aluno.getEndereco(), aluno.getIdAluno());
        }
    }

//Busca aluno pelo ID - Inclui inativos
    public Aluno buscarPorId(int id_aluno) throws SQLException{
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea ON ea.aluno_id = a.id_aluno
            WHERE a.id_aluno = ?
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id_aluno);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return mapearAluno(rs);
                }
                return null;
            }
        }
    }

//Busca aluno pelo CPF - Inclui inativos
    public Aluno buscarPorCPF(String cpf) throws SQLException{
    		String cpfTratado = cpf.trim().replaceAll("\\D", "");
    		if(!ValidaCPF.isValido(cpfTratado)) {
    			throw new IllegalArgumentException("CPF inválido");
    		}
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea ON ea.aluno_id = a.id_aluno
            WHERE a.cpf = ?
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, cpfTratado);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return mapearAluno(rs);
                }
                return null;
            }
        }
    }

//Busca aluno pela Matricula - Inclui inativos
    public Aluno buscarPorMatricula(String matricula) throws SQLException{
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea ON ea.aluno_id = a.id_aluno
            WHERE a.matricula = ?
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, matricula);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return mapearAluno(rs);
                }
                return null;
            }
        }
    }

//Listar todos os alunos de uma turma
    public List<Aluno> listarPorTurma(int idTurma) throws SQLException{
        if(idTurma <= 0){
            throw new IllegalArgumentException("ID da turma inválido.");
        }
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea
            ON ea.aluno_id = a.id_aluno
            WHERE a.turma_id = ?
            ORDER BY a.nome;
        """;

        List<Aluno> alunos = new ArrayList<>();

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, idTurma);

            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    alunos.add(mapearAluno(rs));
                }
            }
        }
        return alunos;
    }

//Listar todos os alunos - Apenas ativos
    public List<Aluno> listarAtivos() throws SQLException{
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea
            ON ea.aluno_id = a.id_aluno
            WHERE a.ativo = true
            ORDER BY a.nome
        """;
        List<Aluno> alunos = new ArrayList<>();

        try(PreparedStatement stmt = conn.prepareStatement(sql);ResultSet rs = stmt.executeQuery()){
            while(rs.next()){
                alunos.add(mapearAluno(rs));
            }
        }
        return alunos;
    }

//Listar todos os alunos - Apenas inativos
    public List<Aluno> listarInativos() throws SQLException{
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea
            ON ea.aluno_id = a.id_aluno
            WHERE a.ativo = false
            ORDER BY a.nome
        """;
        List<Aluno> alunos = new ArrayList<>();

        try(PreparedStatement stmt = conn.prepareStatement(sql);ResultSet rs = stmt.executeQuery()){
            while(rs.next()){
                alunos.add(mapearAluno(rs));
            }
        }
        return alunos;
    }

//Listar todos os alunos - Todos
    public List<Aluno> listarTodos() throws SQLException{
        final String sql = """
            SELECT *
            FROM aluno a
            LEFT JOIN endereco_aluno ea
            ON ea.aluno_id = a.id_aluno
            ORDER BY a.nome
        """;
        List<Aluno> alunos = new ArrayList<>();

        try(PreparedStatement stmt = conn.prepareStatement(sql);ResultSet rs = stmt.executeQuery()){
            while(rs.next()){
                alunos.add(mapearAluno(rs));
            }
        }
        return alunos;
    }

//Inativa cadastro de determinado aluno - Exclusão Lógica
    public boolean inativar(int idAluno) throws SQLException{
        if(idAluno <= 0){
            throw new IllegalArgumentException("ID do aluno inválido.");
        }
        final String sql = """
            UPDATE aluno
            SET ativo = false
            WHERE id_aluno = ?
            AND ativo = true
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, idAluno);

            int linhasAfetadas = stmt.executeUpdate();

            if(linhasAfetadas == 0){
                verificarFalhaInativacao(idAluno);
            }
            return true;
        }
    }

//Reativa cadastro de determinado aluno - Exclusão Lógica
    public boolean reativar(int idAluno) throws SQLException{
        if(idAluno <= 0){
            throw new IllegalArgumentException("ID do aluno inválido.");
        }
        final String sql = """
            UPDATE aluno
            SET ativo = true
            WHERE id_aluno = ?
            AND ativo = false
        """;

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, idAluno);

            int linhasAfetadas = stmt.executeUpdate();

            if(linhasAfetadas == 0){
                verificarFalhaReativacao(idAluno);
            }
            return true;
        }
    }

//Preenche o aluno para inserir no banco
    private void preencherAlunoParaInsert(PreparedStatement stmt, Aluno aluno) throws SQLException{
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

//Preenche o aluno para atualizar no banco
    private void preencherAlunoParaUpdate(PreparedStatement stmt, Aluno aluno) throws SQLException{
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

//Prepara o endereco do aluno para inserir no banco
    private void inserirEndereco(Endereco endereco, int alunoId) throws SQLException {
        final String sql = """
            INSERT INTO endereco_aluno(
                rua,
                numero,
                complemento,
                bairro,
                cidade,
                estado,
                cep,
                aluno_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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

//Prepara o endereco do aluno para atualizar no banco
    private void atualizarEndereco(Endereco endereco, int alunoId) throws SQLException {
        final String sql = """
            UPDATE endereco_aluno
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

//Verifica se o aluno possui endereço cadastrado
    private boolean existeEnderecoDoAluno(int alunoId) throws SQLException {
        final String sql = "SELECT 1 FROM endereco_aluno WHERE aluno_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alunoId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

//Preenche o endereço do aluno
    private void preencherEndereco(PreparedStatement stmt, Endereco endereco) throws SQLException {
        stmt.setString(1, endereco.getRua());
        stmt.setString(2, endereco.getNumero());
        stmt.setString(3, endereco.getComplemento());
        stmt.setString(4, endereco.getBairro());
        stmt.setString(5, endereco.getCidade());
        stmt.setString(6, endereco.getEstado().name());
        stmt.setString(7, endereco.getCep());
    }

//Mapea o aluno
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

//Mapea o endereço
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

//Verifica se aluno é nulo
    private void validarAlunoNaoNulo(Aluno aluno){
        if(aluno == null){
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }
    }

//Verifica se endereço é nulo
    private void validarEnderecoNaoNulo(Endereco endereco){
        if(endereco == null){
            throw new IllegalArgumentException("Endereço do aluno não pode ser nulo.");
        }
    }

//Verifica se a falha na inativação é por aluno não encontrado ou cadastro inativo
    private void verificarFalhaInativacao(int idAluno) throws SQLException {
        final String sql = "SELECT ativo FROM aluno WHERE id_aluno = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Aluno não encontrado.");
                }

                if (!rs.getBoolean("ativo")) {
                    throw new SQLException("Aluno já está inativo.");
                }
            }
        }
    }
    
  //Verifica se a falha na reativação é por ID inválido ou já está ativo
    private void verificarFalhaReativacao(int idAluno) throws SQLException {
        final String sql = "SELECT ativo FROM aluno WHERE id_aluno = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Aluno não encontrado.");
                }

                if (rs.getBoolean("ativo")) {
                    throw new SQLException("Aluno já está ativo.");
                }
            }
        }
    }

//Verifica se a falha na atualização é por ID inválido ou aluno inativo
    private void verificarFalhaAtualizacao(int idAluno) throws SQLException {
        final String sql = "SELECT ativo FROM aluno WHERE id_aluno = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Aluno não encontrado.");
                }

                if (!rs.getBoolean("ativo")) {
                    throw new SQLException("Aluno está inativo e não pode ser atualizado.");
                }
            }
        }
    }
}