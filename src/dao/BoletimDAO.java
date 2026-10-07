package dao;

import model.Boletim;
import model.BoletimItem;
import model.Trimestre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BoletimDAO {
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final Connection conn;

	public BoletimDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public List<Trimestre> listarTrimestres() throws SQLException {
		final String sql = "SELECT * FROM trimestre ORDER BY ano_letivo, numero";
		List<Trimestre> lista = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				Trimestre trimestre = new Trimestre();
				trimestre.setIdTrimestre(rs.getInt("id_trimestre"));
				trimestre.setNumero(rs.getInt("numero"));
				trimestre.setDataInicio(parseData(rs.getString("data_inicio"), "data de início do trimestre"));
				trimestre.setDataFim(parseData(rs.getString("data_fim"), "data de fim do trimestre"));
				trimestre.setAnoLetivo(rs.getInt("ano_letivo"));
				lista.add(trimestre);
			}
		}
		return lista;
	}

	public Boletim buscarPorAluno(int idAluno, int idTrimestre) throws SQLException {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		if (idTrimestre <= 0)
			throw new IllegalArgumentException("ID do trimestre inválido.");

		final String sql = """
					SELECT
					    a.id_aluno,
					    a.nome          AS nome_aluno,
					    a.matricula,
					    a.turma_id,
					    d.id_disciplina,
					    d.descricao     AS nome_disciplina,
					    AVG(n.nota)     AS media,
					    t.id_trimestre  AS trimestre_id,
					    t.numero        AS trimestre_numero,
					    t.data_inicio   AS trimestre_inicio,
					    t.data_fim      AS trimestre_fim,
					    t.ano_letivo    AS ano_letivo
					FROM aluno a
					INNER JOIN nota n       ON n.aluno_id      = a.id_aluno
					INNER JOIN disciplina d ON d.id_disciplina = n.disciplina_id
					INNER JOIN trimestre t  ON t.id_trimestre  = n.trimestre_id
					WHERE a.id_aluno     = ?
					  AND n.trimestre_id = ?
					GROUP BY
					    a.id_aluno, a.nome, a.matricula, a.turma_id,
					    d.id_disciplina, d.descricao,
					    t.id_trimestre, t.numero, t.data_inicio, t.data_fim, t.ano_letivo
					ORDER BY d.descricao
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idAluno);
			stmt.setInt(2, idTrimestre);
			try (ResultSet rs = stmt.executeQuery()) {
				Boletim boletim = null;
				while (rs.next()) {
					if (boletim == null)
						boletim = mapearCabecalho(rs);
					boletim.adicionarItem(mapearItem(rs));
				}
				return boletim;
			}
		}
	}

	public List<Boletim> buscarPorTurma(int idTurma, int idTrimestre) throws SQLException {
		if (idTurma <= 0)
			throw new IllegalArgumentException("ID da turma inválido.");
		if (idTrimestre <= 0)
			throw new IllegalArgumentException("ID do trimestre inválido.");

		final String sql = """
					SELECT
					    a.id_aluno,
					    a.nome          AS nome_aluno,
					    a.matricula,
					    a.turma_id,
					    d.id_disciplina,
					    d.descricao     AS nome_disciplina,
					    AVG(n.nota)     AS media,
					    t.id_trimestre  AS trimestre_id,
					    t.numero        AS trimestre_numero,
					    t.data_inicio   AS trimestre_inicio,
					    t.data_fim      AS trimestre_fim,
					    t.ano_letivo    AS ano_letivo
					FROM aluno a
					INNER JOIN nota n       ON n.aluno_id      = a.id_aluno
					INNER JOIN disciplina d ON d.id_disciplina = n.disciplina_id
					INNER JOIN trimestre t  ON t.id_trimestre  = n.trimestre_id
					WHERE a.turma_id     = ?
					  AND n.trimestre_id = ?
					GROUP BY
					    a.id_aluno, a.nome, a.matricula, a.turma_id,
					    d.id_disciplina, d.descricao,
					    t.id_trimestre, t.numero, t.data_inicio, t.data_fim, t.ano_letivo
					ORDER BY a.nome, d.descricao
				""";

		Map<Integer, Boletim> boletins = new LinkedHashMap<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idTurma);
			stmt.setInt(2, idTrimestre);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int idAluno = rs.getInt("id_aluno");

					Boletim boletim = boletins.get(idAluno);
					if (boletim == null) {
						boletim = mapearCabecalho(rs);
						boletins.put(idAluno, boletim);
					}
					boletim.adicionarItem(mapearItem(rs));
				}
			}
		}
		return new ArrayList<>(boletins.values());
	}

	private Boletim mapearCabecalho(ResultSet rs) throws SQLException {
		Trimestre trimestre = new Trimestre();
		trimestre.setIdTrimestre(rs.getInt("trimestre_id"));
		trimestre.setNumero(rs.getInt("trimestre_numero"));
		trimestre.setDataInicio(parseData(rs.getString("trimestre_inicio"), "data de início do trimestre"));
		trimestre.setDataFim(parseData(rs.getString("trimestre_fim"), "data de fim do trimestre"));
		trimestre.setAnoLetivo(rs.getInt("ano_letivo"));

		Boletim boletim = new Boletim();
		boletim.setIdAluno(rs.getInt("id_aluno"));
		boletim.setNomeAluno(rs.getString("nome_aluno"));
		boletim.setMatricula(rs.getString("matricula"));
		boletim.setIdTurma(rs.getInt("turma_id"));
		boletim.setDatageracao(LocalDate.now());
		boletim.setTrimestre(trimestre);
		return boletim;
	}

	private BoletimItem mapearItem(ResultSet rs) throws SQLException {
		BoletimItem item = new BoletimItem();
		item.setIdDisciplina(rs.getInt("id_disciplina"));
		item.setNomeDisciplina(rs.getString("nome_disciplina"));
		item.setMedia(rs.getDouble("media"));
		return item;
	}

	private LocalDate parseData(String valor, String campo) throws SQLException {
		if (valor == null || valor.isBlank()) {
			throw new SQLException("Data não informada para " + campo + ".");
		}

		String tratado = valor.trim();

		if (tratado.contains("T")) {
			tratado = tratado.substring(0, tratado.indexOf('T'));
		} else if (tratado.contains(" ")) {
			tratado = tratado.substring(0, tratado.indexOf(' '));
		}
		try {
			return LocalDate.parse(tratado, FORMATO_DATA);
		} catch (DateTimeParseException e) {
			throw new SQLException("Data inválida para " + campo + ". Valor recebido: " + valor, e);
		}
	}
}