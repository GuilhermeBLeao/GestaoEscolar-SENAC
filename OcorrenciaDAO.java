//igor

package dao;

import model.Ocorrencia;
import variaveisEnum.TipoOcorrencia;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Classe de acesso a dados para persistir ocorrências no banco SQLite.
public class OcorrenciaDAO {

    // Conexão com o banco que será usada para todas as operações.
    private final Connection conn;

    public OcorrenciaDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

    // Insere uma nova ocorrência na tabela de ocorrências.
    public void inserir(Ocorrencia ocorrencia) throws SQLException {
        validarOcorrenciaNaoNula(ocorrencia);

        final String sql = """
            INSERT INTO ocorrencia (
                funcionario_id,
                aluno_id,
                tipo_ocorrencia,
                descricao,
                atendente_nome,
                data_ocorrencia
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ocorrencia.getFuncionarioId());

            if (ocorrencia.getAlunoId() > 0) {
                stmt.setInt(2, ocorrencia.getAlunoId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            stmt.setString(3, ocorrencia.getTipoOcorrencia().name());
            stmt.setString(4, ocorrencia.getDescricao());
            stmt.setString(5, ocorrencia.getAtendenteNome());
            stmt.setDate(6, Date.valueOf(ocorrencia.getDataOcorrencia()));

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir ocorrência. Nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ocorrencia.setIdOcorrencia(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir ocorrência. ID não retornado.");
                }
            }
        }
    }

    // Atualiza uma ocorrência existente no banco.
    public void atualizar(Ocorrencia ocorrencia) throws SQLException {
        validarOcorrenciaNaoNula(ocorrencia);

        if (ocorrencia.getIdOcorrencia() <= 0) {
            throw new IllegalArgumentException("ID da ocorrência inválido.");
        }

        final String sql = """
            UPDATE ocorrencia
               SET funcionario_id = ?,
                   aluno_id = ?,
                   tipo_ocorrencia = ?,
                   descricao = ?,
                   atendente_nome = ?,
                   data_ocorrencia = ?
             WHERE id_ocorrencia = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ocorrencia.getFuncionarioId());

            if (ocorrencia.getAlunoId() > 0) {
                stmt.setInt(2, ocorrencia.getAlunoId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            stmt.setString(3, ocorrencia.getTipoOcorrencia().name());
            stmt.setString(4, ocorrencia.getDescricao());
            stmt.setString(5, ocorrencia.getAtendenteNome());
            stmt.setDate(6, Date.valueOf(ocorrencia.getDataOcorrencia()));
            stmt.setInt(7, ocorrencia.getIdOcorrencia());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar ocorrência. Nenhuma linha afetada.");
            }
        }
    }

    // Recupera uma ocorrência pelo ID.
    public Ocorrencia buscarPorId(int idOcorrencia) throws SQLException {
        final String sql = """
            SELECT id_ocorrencia,
                   funcionario_id,
                   aluno_id,
                   tipo_ocorrencia,
                   descricao,
                   atendente_nome,
                   data_ocorrencia
            FROM ocorrencia
            WHERE id_ocorrencia = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idOcorrencia);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearOcorrencia(rs);
                }
            }
        }

        return null;
    }

    // Lista todas as ocorrências ordenadas da mais recente para a mais antiga.
    public List<Ocorrencia> listar() throws SQLException {
        final String sql = """
            SELECT id_ocorrencia,
                   funcionario_id,
                   aluno_id,
                   tipo_ocorrencia,
                   descricao,
                   atendente_nome,
                   data_ocorrencia
            FROM ocorrencia
            ORDER BY data_ocorrencia DESC, id_ocorrencia DESC
            """;

        List<Ocorrencia> ocorrencias = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ocorrencias.add(mapearOcorrencia(rs));
            }
        }

        return ocorrencias;
    }

    // Lista ocorrências registradas por um funcionário específico.
    public List<Ocorrencia> listarPorFuncionario(int funcionarioId) throws SQLException {
        final String sql = """
            SELECT id_ocorrencia,
                   funcionario_id,
                   aluno_id,
                   tipo_ocorrencia,
                   descricao,
                   atendente_nome,
                   data_ocorrencia
            FROM ocorrencia
            WHERE funcionario_id = ?
            ORDER BY data_ocorrencia DESC, id_ocorrencia DESC
            """;

        List<Ocorrencia> ocorrencias = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, funcionarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ocorrencias.add(mapearOcorrencia(rs));
                }
            }
        }

        return ocorrencias;
    }

    // Mapeia o resultado da query para o objeto Ocorrencia.
    private Ocorrencia mapearOcorrencia(ResultSet rs) throws SQLException {
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdOcorrencia(rs.getInt("id_ocorrencia"));
        ocorrencia.setFuncionarioId(rs.getInt("funcionario_id"));

        int alunoId = rs.getInt("aluno_id");
        if (!rs.wasNull()) {
            ocorrencia.setAlunoId(alunoId);
        }

        String tipo = rs.getString("tipo_ocorrencia");
        if (tipo != null && !tipo.isBlank()) {
            ocorrencia.setTipoOcorrencia(TipoOcorrencia.valueOf(tipo));
        }

        ocorrencia.setDescricao(rs.getString("descricao"));
        ocorrencia.setAtendenteNome(rs.getString("atendente_nome"));
        ocorrencia.setDataOcorrencia(rs.getDate("data_ocorrencia").toLocalDate());

        return ocorrencia;
    }

    // Valida se o objeto de ocorrência não é nulo antes de operar.
    private void validarOcorrenciaNaoNula(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            throw new IllegalArgumentException("Ocorrência não pode ser nula.");
        }
    }
}
