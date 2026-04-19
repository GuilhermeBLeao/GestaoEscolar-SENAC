//Guilherme

package dao;

import database.ConnectionFactory;
import model.Usuario;
import variaveisEnum.TipoUsuario;
import util.ValidaCPF;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void inserir(Usuario usuario) {
        validarUsuario(usuario);

        String sql = """
            INSERT INTO usuario (
                aluno_id,
                funcionario_id,
                pai_id,
                professor_id,
                cpf,
                senha_hash,
                ativo,
                data_criacao,
                ultimo_login,
                tipo_usuario
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(stmt, usuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0)
                throw new SQLException("Falha ao inserir usuário.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    usuario.setIdUsuario(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário.", e);
        }
    }

    public void atualizar(Usuario usuario) {
        validarUsuario(usuario);
        if (usuario.getIdUsuario() <= 0)
            throw new IllegalArgumentException("ID do usuário inválido para atualização.");

        String sql = """
            UPDATE usuario
               SET aluno_id = ?,
                   funcionario_id = ?,
                   pai_id = ?,
                   professor_id = ?,
                   cpf = ?,
                   senha_hash = ?,
                   ativo = ?,
                   data_criacao = ?,
                   ultimo_login = ?,
                   tipo_usuario = ?
             WHERE id_usuario = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preencherStatement(stmt, usuario);
            stmt.setInt(11, usuario.getIdUsuario());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0)
                throw new RuntimeException("Nenhum usuário foi atualizado.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário.", e);
        }
    }

    public boolean excluir(int idUsuario) {
        if (idUsuario <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir usuário.", e);
        }
    }

    public Usuario buscarPorId(int idUsuario) {
        if (idUsuario <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return montarUsuario(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por ID.", e);
        }
        return null;
    }

    public Usuario buscarPorCpf(String cpf) {
        String cpfTratado = cpf == null ? null : cpf.trim();

        if (cpfTratado == null || cpfTratado.isEmpty())
            throw new IllegalArgumentException("CPF inválido.");
        if (!ValidaCPF.isValido(cpfTratado))
            throw new IllegalArgumentException("CPF inválido.");

        String sql = "SELECT * FROM usuario WHERE cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpfTratado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return montarUsuario(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por CPF.", e);
        }
        return null;
    }

    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT * FROM usuario ORDER BY id_usuario";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next())
                lista.add(montarUsuario(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários.", e);
        }

        return lista;
    }

    public boolean existeCpf(String cpf) {
        String cpfTratado = cpf == null ? null : cpf.trim();

        if (cpfTratado == null || cpfTratado.isEmpty())
            throw new IllegalArgumentException("CPF inválido.");
        if (!ValidaCPF.isValido(cpfTratado))
            throw new IllegalArgumentException("CPF inválido.");

        String sql = "SELECT 1 FROM usuario WHERE cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpfTratado);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar CPF do usuário.", e);
        }
    }

    public Usuario autenticar(String cpf, String senhaInformada) {
        String cpfTratado = cpf == null ? null : cpf.trim();

        if (cpfTratado == null || cpfTratado.isEmpty())
            throw new IllegalArgumentException("CPF inválido.");
        if (!ValidaCPF.isValido(cpfTratado))
            throw new IllegalArgumentException("CPF inválido.");
        if (senhaInformada == null || senhaInformada.trim().isEmpty())
            throw new IllegalArgumentException("Senha inválida.");

        Usuario usuario = buscarPorCpf(cpfTratado);

        if (usuario == null)
            return null;
        if (!usuario.isAtivo())
            return null;

        return usuario.validarSenha(senhaInformada) ? usuario : null;
    }

    public void atualizarUltimoLogin(int idUsuario) {
        if (idUsuario <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        String sql = "UPDATE usuario SET ultimo_login = ? WHERE id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new Date(System.currentTimeMillis()));
            stmt.setInt(2, idUsuario);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0)
                throw new RuntimeException("Nenhum usuário foi atualizado no último login.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar último login do usuário.", e);
        }
    }

    private void preencherStatement(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setInt(1, usuario.getAlunoId());
        stmt.setInt(2, usuario.getFuncionarioId());
        stmt.setInt(3, usuario.getPaiId());
        stmt.setInt(4, usuario.getProfessorId());
        stmt.setString(5, usuario.getCpf());
        stmt.setString(6, usuario.getSenhaHash());
        stmt.setBoolean(7, usuario.isAtivo());
        stmt.setDate(8, Date.valueOf(usuario.getDataCriacao()));

        if (usuario.getUltimoLogin() != null)
            stmt.setDate(9, Date.valueOf(usuario.getUltimoLogin()));
        else
            stmt.setNull(9, Types.DATE);

        stmt.setString(10, usuario.getTipoUsuario().name());
    }

    private Usuario montarUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setAlunoId(rs.getInt("aluno_id"));
        usuario.setFuncionarioId(rs.getInt("funcionario_id"));
        usuario.setPaiId(rs.getInt("pai_id"));
        usuario.setProfessorId(rs.getInt("professor_id"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setAtivo(rs.getBoolean("ativo"));

        Date dataCriacao = rs.getDate("data_criacao");
        if (dataCriacao != null)
            usuario.setDataCriacao(dataCriacao.toLocalDate());

        Date ultimoLogin = rs.getDate("ultimo_login");
        if (ultimoLogin != null)
            usuario.setUltimoLogin(ultimoLogin.toLocalDate());

        usuario.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipo_usuario")));

        return usuario;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null)
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        if (usuario.getCpf() == null || usuario.getCpf().trim().isEmpty())
            throw new IllegalArgumentException("CPF do usuário é obrigatório.");
        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().trim().isEmpty())
            throw new IllegalArgumentException("Hash da senha é obrigatório.");
        if (usuario.getDataCriacao() == null)
            throw new IllegalArgumentException("Data de criação é obrigatória.");
        if (usuario.getTipoUsuario() == null)
            throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
    }
}