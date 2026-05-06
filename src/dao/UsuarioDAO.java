//Guilherme

package dao;

import model.Usuario;
import util.ValidaCPF;
import variaveisEnum.TipoUsuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private final Connection conn;

    public UsuarioDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    private boolean existeVinculo(String coluna, int idVinculo, int idUsuarioIgnorado) throws SQLException {
        if (idVinculo <= 0) {
            return false;
        }

        final String sql = """
            SELECT 1
            FROM usuario
            WHERE %s = ?
              AND id_usuario <> ?
            """.formatted(coluna);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVinculo);
            stmt.setInt(2, idUsuarioIgnorado);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeCpf(String cpf) throws SQLException {
        String cpfTratado = cpf.trim().replaceAll("\\D", "");

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        final String sql = "SELECT 1 FROM usuario WHERE cpf = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfTratado);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeAlunoId(int alunoId, int idUsuarioIgnorado) throws SQLException {
        return existeVinculo("aluno_id", alunoId, idUsuarioIgnorado);
    }

    public boolean existeFuncionarioId(int funcionarioId, int idUsuarioIgnorado) throws SQLException {
        return existeVinculo("funcionario_id", funcionarioId, idUsuarioIgnorado);
    }

    public boolean existePaiId(int paiId, int idUsuarioIgnorado) throws SQLException {
        return existeVinculo("pai_id", paiId, idUsuarioIgnorado);
    }

    public boolean existeProfessorId(int professorId, int idUsuarioIgnorado) throws SQLException {
        return existeVinculo("professor_id", professorId, idUsuarioIgnorado);
    }

    public void inserir(Usuario usuario) throws SQLException {
        validarUsuarioNaoNulo(usuario);

        final String sql = """
            INSERT INTO usuario (
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherUsuarioParaInsert(stmt, usuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir usuário. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir usuário. ID não retornado.");
                }
            }
        }
    }

    public void atualizar(Usuario usuario) throws SQLException {
        validarUsuarioNaoNulo(usuario);

        if (usuario.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        final String sql = """
            UPDATE usuario
               SET senha_hash = ?,
                   tipo_usuario = ?,
                   aluno_id = ?,
                   funcionario_id = ?,
                   pai_id = ?,
                   professor_id = ?
             WHERE id_usuario = ?
               AND ativo = true
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getSenhaHash());
            stmt.setString(2, usuario.getTipoUsuario().name());
            setNullableInt(stmt, 3, usuario.getAlunoId());
            setNullableInt(stmt, 4, usuario.getFuncionarioId());
            setNullableInt(stmt, 5, usuario.getPaiId());
            setNullableInt(stmt, 6, usuario.getProfessorId());
            stmt.setInt(7, usuario.getIdUsuario());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaAtualizacao(usuario.getIdUsuario());
            }
        }
    }

    public void atualizarUltimoLogin(int idUsuario, LocalDateTime ultimoLogin) throws SQLException {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        if (ultimoLogin == null) {
            throw new IllegalArgumentException("Último login não pode ser nulo.");
        }

        final String sql = """
            UPDATE usuario
               SET ultimo_login = ?
             WHERE id_usuario = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(ultimoLogin));
            stmt.setInt(2, idUsuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar último login. Nenhuma linha afetada.");
            }
        }
    }

    public void atualizarSenhaHash(int idUsuario, String senhaHash) throws SQLException {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha é obrigatório.");
        }

        final String sql = """
            UPDATE usuario
               SET senha_hash = ?
             WHERE id_usuario = ?
               AND ativo = true
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senhaHash);
            stmt.setInt(2, idUsuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaAtualizacao(idUsuario);
            }
        }
    }

    public Usuario autenticar(String cpf, String senha) throws SQLException {
        Usuario usuario = buscarPorCpf(cpf);

        if (usuario == null) {
            return null;
        }

        if (!usuario.validarSenha(senha)) {
            return null;
        }

        return usuario;
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        final String sql = """
            SELECT
                id_usuario,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            FROM usuario
            WHERE id_usuario = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearUsuario(rs) : null;
            }
        }
    }

    public Usuario buscarPorCpf(String cpf) throws SQLException {
        String cpfTratado = cpf.trim().replaceAll("\\D", "");

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        final String sql = """
            SELECT
                id_usuario,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            FROM usuario
            WHERE cpf = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfTratado);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearUsuario(rs) : null;
            }
        }
    }

    private Usuario buscarPorVinculo(String coluna, int idVinculo) throws SQLException {
        if (idVinculo <= 0) {
            return null;
        }

        final String sql = """
            SELECT
                id_usuario,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            FROM usuario
            WHERE %s = ?
            """.formatted(coluna);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVinculo);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearUsuario(rs) : null;
            }
        }
    }

    public Usuario buscarPorAlunoId(int alunoId) throws SQLException {
        return buscarPorVinculo("aluno_id", alunoId);
    }

    public Usuario buscarPorFuncionarioId(int funcionarioId) throws SQLException {
        return buscarPorVinculo("funcionario_id", funcionarioId);
    }

    public Usuario buscarPorPaiId(int paiId) throws SQLException {
        return buscarPorVinculo("pai_id", paiId);
    }

    public Usuario buscarPorProfessorId(int professorId) throws SQLException {
        return buscarPorVinculo("professor_id", professorId);
    }

    public boolean inativar(int idUsuario) throws SQLException {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        final String sql = """
            UPDATE usuario
               SET ativo = false
             WHERE id_usuario = ?
               AND ativo = true
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaInativacao(idUsuario);
            }

            return true;
        }
    }

    public boolean reativar(int idUsuario) throws SQLException {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        final String sql = """
            UPDATE usuario
               SET ativo = true
             WHERE id_usuario = ?
               AND ativo = false
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaReativacao(idUsuario);
            }

            return true;
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        final String sql = """
            SELECT
                id_usuario,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            FROM usuario
            ORDER BY id_usuario
            """;

        List<Usuario> usuarios = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    public List<Usuario> listarAtivos() throws SQLException {
        final String sql = """
            SELECT
                id_usuario,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            FROM usuario
            WHERE ativo = true
            ORDER BY id_usuario
            """;

        List<Usuario> usuarios = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    public List<Usuario> listarInativos() throws SQLException {
        final String sql = """
            SELECT
                id_usuario,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario,
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id
            FROM usuario
            WHERE ativo = false
            ORDER BY id_usuario
            """;

        List<Usuario> usuarios = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    private void preencherUsuarioParaInsert(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getCpf());
        stmt.setString(2, usuario.getSenhaHash());
        stmt.setBoolean(3, usuario.isAtivo());
        stmt.setDate(4, Date.valueOf(usuario.getDataCriacao()));

        if (usuario.getUltimoLogin() == null) {
            stmt.setNull(5, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(5, Timestamp.valueOf(usuario.getUltimoLogin()));
        }

        stmt.setString(6, usuario.getTipoUsuario().name());
        setNullableInt(stmt, 7, usuario.getAlunoId());
        setNullableInt(stmt, 8, usuario.getFuncionarioId());
        setNullableInt(stmt, 9, usuario.getPaiId());
        setNullableInt(stmt, 10, usuario.getProfessorId());
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setAtivo(rs.getBoolean("ativo"));

        Date dataCriacao = rs.getDate("data_criacao");
        if (dataCriacao != null) {
            usuario.carregarDataCriacaoDoBanco(dataCriacao.toLocalDate());
        }

        Timestamp ultimoLogin = rs.getTimestamp("ultimo_login");
        if (ultimoLogin != null) {
            usuario.setUltimoLogin(ultimoLogin.toLocalDateTime());
        }

        usuario.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipo_usuario")));

        int alunoId = getNullableInt(rs, "aluno_id");
        if (alunoId > 0) {
            usuario.setAlunoId(alunoId);
        }

        int funcionarioId = getNullableInt(rs, "funcionario_id");
        if (funcionarioId > 0) {
            usuario.setFuncionarioId(funcionarioId);
        }

        int paiId = getNullableInt(rs, "pai_id");
        if (paiId > 0) {
            usuario.setPaiId(paiId);
        }

        int professorId = getNullableInt(rs, "professor_id");
        if (professorId > 0) {
            usuario.setProfessorId(professorId);
        }

        return usuario;
    }

    private void setNullableInt(PreparedStatement stmt, int indice, int valor) throws SQLException {
        if (valor <= 0) {
            stmt.setNull(indice, Types.INTEGER);
        } else {
            stmt.setInt(indice, valor);
        }
    }

    private int getNullableInt(ResultSet rs, String coluna) throws SQLException {
        int valor = rs.getInt(coluna);
        return rs.wasNull() ? 0 : valor;
    }

    private void validarUsuarioNaoNulo(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }
    }

    private void verificarFalhaAtualizacao(int idUsuario) throws SQLException {
        final String sql = "SELECT ativo FROM usuario WHERE id_usuario = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Usuário não encontrado.");
                }

                if (!rs.getBoolean("ativo")) {
                    throw new SQLException("Usuário está inativo e não pode ser atualizado.");
                }
            }
        }
    }

    private void verificarFalhaInativacao(int idUsuario) throws SQLException {
        final String sql = "SELECT ativo FROM usuario WHERE id_usuario = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Usuário não encontrado.");
                }

                if (!rs.getBoolean("ativo")) {
                    throw new SQLException("Usuário já está inativo.");
                }
            }
        }
    }

    private void verificarFalhaReativacao(int idUsuario) throws SQLException {
        final String sql = "SELECT ativo FROM usuario WHERE id_usuario = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Usuário não encontrado.");
                }

                if (rs.getBoolean("ativo")) {
                    throw new SQLException("Usuário já está ativo.");
                }
            }
        }
    }
}