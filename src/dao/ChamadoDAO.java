package dao;

import model.Chamado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ChamadoDAO {
	private final Connection conn;

	public ChamadoDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public void inserir(Chamado chamado) throws SQLException {
		validarChamadoNaoNulo(chamado);

		final String sql = """
					INSERT INTO chamado
					    (aluno_id,
					    categoria,
					    assunto,
					    descricao,
					    prioridade,
					    status)
					VALUES (?, ?, ?, ?, ?, ?)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, chamado.getAlunoId());
			stmt.setString(2, chamado.getCategoria());
			stmt.setString(3, chamado.getAssunto());
			stmt.setString(4, chamado.getDescricao());
			stmt.setString(5, chamado.getPrioridade());
			stmt.setString(6, chamado.getStatus());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir chamado. Nenhuma linha afetada.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					chamado.setIdChamado(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir chamado. ID não retornado.");
				}
			}
		}
	}

	public void atualizarAtendimento(int idChamado, String resposta, String novoStatus) throws SQLException {
		if (idChamado <= 0) {
			throw new IllegalArgumentException("ID do chamado inválido.");
		}

		final String sql = """
					UPDATE chamado
					   SET resposta = ?,
					       status = ?,
					       data_atualizacao = CURRENT_TIMESTAMP
					WHERE id_chamado = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, resposta);
			stmt.setString(2, novoStatus);
			stmt.setInt(3, idChamado);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao responder chamado. Nenhuma linha afetada.");
			}
		}
	}

	private static final String SELECT_COM_ALUNO = """
				SELECT c.id_chamado,
				       c.aluno_id,
				       c.categoria,
				       c.assunto,
				       c.descricao,
				       c.prioridade,
				       c.status,
				       c.resposta,
				       c.data_criacao,
				       c.data_atualizacao,
				       a.nome AS aluno_nome,
				       a.matricula AS aluno_matricula
				FROM chamado c
				LEFT JOIN aluno a
				       ON a.id_aluno = c.aluno_id
			""";

	public Chamado buscarPorId(int idChamado) throws SQLException {
		final String sql = SELECT_COM_ALUNO + " WHERE c.id_chamado = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idChamado);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? mapearChamado(rs) : null;
			}
		}
	}

	public List<Chamado> listar() throws SQLException {
		final String sql = SELECT_COM_ALUNO + " ORDER BY c.data_criacao DESC";
		List<Chamado> chamados = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				chamados.add(mapearChamado(rs));
			}
		}
		return chamados;
	}

	public List<Chamado> listarPorAluno(int idAluno) throws SQLException {
		final String sql = SELECT_COM_ALUNO + " WHERE c.aluno_id = ?" + " ORDER BY c.data_criacao DESC";
		List<Chamado> chamados = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idAluno);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					chamados.add(mapearChamado(rs));
				}
			}
		}
		return chamados;
	}

	public List<Chamado> listarPorStatus(String status) throws SQLException {
		final String sql = SELECT_COM_ALUNO + " WHERE c.status = ?" + " ORDER BY c.data_criacao DESC";
		List<Chamado> chamados = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, status);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					chamados.add(mapearChamado(rs));
				}
			}
		}
		return chamados;
	}

	public int contarAtendimentosHoje() throws SQLException {
		final String sql = "SELECT COUNT(*) " + "FROM chamado " + "WHERE date(data_criacao) = date('now')";
		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	public int contarAbertos() throws SQLException {
		final String sql = "SELECT COUNT(*) " + "FROM chamado " + "WHERE status = 'Aberto'";
		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	private Chamado mapearChamado(ResultSet rs) throws SQLException {
		Chamado chamado = new Chamado();
		chamado.setIdChamado(rs.getInt("id_chamado"));
		chamado.setAlunoId(rs.getInt("aluno_id"));
		chamado.setCategoria(rs.getString("categoria"));
		chamado.setAssunto(rs.getString("assunto"));
		chamado.setDescricao(rs.getString("descricao"));
		chamado.setPrioridade(rs.getString("prioridade"));
		chamado.setStatus(rs.getString("status"));
		chamado.setResposta(rs.getString("resposta"));
		chamado.setAlunoNome(rs.getString("aluno_nome"));
		chamado.setMatricula(rs.getString("aluno_matricula"));

		String dataCriacaoStr = rs.getString("data_criacao");
		if (dataCriacaoStr != null && !dataCriacaoStr.isBlank()) {
			try {
				LocalDateTime dataCriacao = parseTimestampSqlite(dataCriacaoStr);
				chamado.setDataCriacao(dataCriacao);
			} catch (DateTimeParseException e) {
				throw new SQLException("Data de criação do chamado inválida. " + "Valor recebido: " + dataCriacaoStr, e);
			}
		}

		String dataAtualizacaoStr = rs.getString("data_atualizacao");

		if (dataAtualizacaoStr != null && !dataAtualizacaoStr.isBlank()) {
			try {
				LocalDateTime dataAtualizacao = parseTimestampSqlite(dataAtualizacaoStr);
				chamado.setDataAtualizacao(dataAtualizacao);
			} catch (DateTimeParseException e) {
				throw new SQLException("Data de atualização do chamado inválida. " + "Valor recebido: " + dataAtualizacaoStr, e);
			}
		}
		return chamado;
	}

	private LocalDateTime parseTimestampSqlite(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}

		String tratado = valor.trim();
		try {
			return LocalDateTime.parse(tratado);
		} catch (DateTimeParseException ignored) {
			// Continua tentando os formatos do SQLite.
		}
		
		try {
			return LocalDateTime.parse(tratado, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		} catch (DateTimeParseException ignored) {
			// Continua tentando com milissegundos.
		}
		try {
			return LocalDateTime.parse(tratado, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		} catch (DateTimeParseException ignored) {
			throw new DateTimeParseException("Formato de data/hora não suportado: " + valor, tratado, 0);
		}
	}

	private void validarChamadoNaoNulo(Chamado chamado) {
		if (chamado == null) {
			throw new IllegalArgumentException("Chamado não pode ser nulo.");
		}
	}
}