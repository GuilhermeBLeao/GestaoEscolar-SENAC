/*Igor
Guilherme editou adicionando sexo, e exclusão lógica(Inativar ao invés de excluir)*/

//Define o pacote desta classe
package dao;

//Importa as classes de outros pacotes
import model.Endereco;
import model.Professor;
import util.ValidaCPF;
import variaveisEnum.Estado;
import variaveisEnum.Sexo;

//Importa as bibliotecas a serem utilizados
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDAO {

	private final Connection conn;

	public ProfessorDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

//Verifica a existência de um cadastro para o CPF informado
	public boolean existeCpf(String cpf) throws SQLException {
		String cpfTratado = cpf.trim().replaceAll("\\D", "");
		if (!ValidaCPF.isValido(cpfTratado)) {
			throw new IllegalArgumentException("CPF inválido.");
		}

		final String sql = "SELECT 1 FROM professor WHERE cpf = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, cpfTratado);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

//Insere o professor no banco
	public void inserir(Professor professor) throws SQLException {
		validarProfessorNaoNulo(professor);
		validarEnderecoNaoNulo(professor.getEndereco());

		final String sql = """
				INSERT INTO professor (
				    nome,
				    cpf,
				    formacao,
				    telefone,
				    rg,
				    data_nascimento,
				    sexo,
				    ativo
				) VALUES (?, ?, ?, ?, ?, ?, ?, true)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, professor.getNome());
			stmt.setString(2, professor.getCpf());
			stmt.setString(3, professor.getFormacao());
			stmt.setString(4, professor.getTelefone());
			stmt.setString(5, professor.getRg());
			stmt.setDate(6, Date.valueOf(professor.getDataNascimento()));
			stmt.setString(7, professor.getSexo().name());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir professor. Nenhuma linha afetada.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					professor.setIdProfessor(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir professor. ID não retornado.");
				}
			}
		}
		inserirEndereco(professor.getEndereco(), professor.getIdProfessor());
	}

//Atualiza o professor no banco
	public void atualizar(Professor professor) throws SQLException {
		validarProfessorNaoNulo(professor);
		validarEnderecoNaoNulo(professor.getEndereco());

		if (professor.getIdProfessor() <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}

		final String sql = """
				UPDATE professor
				   SET nome = ?,
				       formacao = ?,
				       telefone = ?,
				       rg = ?,
				       data_nascimento = ?,
				       sexo = ?
				 WHERE id_professor = ?
				   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, professor.getNome());
			stmt.setString(2, professor.getFormacao());
			stmt.setString(3, professor.getTelefone());
			stmt.setString(4, professor.getRg());
			stmt.setDate(5, Date.valueOf(professor.getDataNascimento()));
			stmt.setString(6, professor.getSexo().name());
			stmt.setInt(7, professor.getIdProfessor());

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				verificarFalhaAtualizacao(professor.getIdProfessor());
			}
		}

		if (existeEnderecoDoProfessor(professor.getIdProfessor())) {
			atualizarEndereco(professor.getEndereco(), professor.getIdProfessor());
		} else {
			inserirEndereco(professor.getEndereco(), professor.getIdProfessor());
		}
	}

//Busca o cadastro pelo ID
	public Professor buscarPorId(int idProfessor) throws SQLException {
		if (idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor inválido.");
		}

		final String sql = sqlBaseSelect() + """
				WHERE p.id_professor = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProfessor(rs);
				}
				return null;
			}
		}
	}

//Busca o cadastro pelo CPF
	public Professor buscarPorCpf(String cpf) throws SQLException {
		String cpfTratado = cpf.trim().replaceAll("\\D", "");
		if (!ValidaCPF.isValido(cpfTratado)) {
			throw new IllegalArgumentException("CPF inválido.");
		}

		final String sql = sqlBaseSelect() + """
				WHERE p.cpf = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, cpfTratado);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProfessor(rs);
				}
				return null;
			}
		}
	}

//Buscar o cadastro pelo nome
	public Professor buscarPorNome(String nome) throws SQLException {
		final String sql = sqlBaseSelect() + """
				WHERE p.nome = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, nome);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearProfessor(rs);
				}
				return null;
			}
		}
	}

//Gera a lista para listagem de todos os professores - ativos e inativos
	public List<Professor> listarTodos() throws SQLException {
		final String sql = sqlBaseSelect() + """
				ORDER BY p.nome
				""";

		List<Professor> professores = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				professores.add(mapearProfessor(rs));
			}
		}
		return professores;
	}

//Gera a lista para listagem dos professores ativos
	public List<Professor> listarAtivos() throws SQLException {
		final String sql = sqlBaseSelect() + """
				WHERE p.ativo = true
				  ORDER BY p.nome
				  """;

		List<Professor> professores = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				professores.add(mapearProfessor(rs));
			}
		}
		return professores;
	}

//Gera a lista para listagem dos professores inativos
	public List<Professor> listarInativos() throws SQLException {
		final String sql = sqlBaseSelect() + """
				WHERE p.ativo = false
				  ORDER BY p.nome
				  """;

		List<Professor> professores = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				professores.add(mapearProfessor(rs));
			}
		}
		return professores;
	}

//Exclusão Lógica - Inativa o cadastro
	public boolean inativar(int idProfessor) throws SQLException {
		if (idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}

		final String sql = """
				UPDATE professor
				   SET ativo = false
				 WHERE id_professor = ?
				   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				verificarFalhaInativacao(idProfessor);
			}
			return true;
		}
	}

	// Exclusão Lógica - Reativa o cadastro
	public boolean reativar(int idProfessor) throws SQLException {
		if (idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}

		final String sql = """
				UPDATE professor
				   SET ativo = true
				 WHERE id_professor = ?
				   AND ativo = false
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				verificarFalhaReativacao(idProfessor);
			}
			return true;
		}
	}

//Insere o endereço
	private void inserirEndereco(Endereco endereco, int professorId) throws SQLException {
		final String sql = """
				INSERT INTO endereco_professor(
				    rua,
				    numero,
				    complemento,
				    bairro,
				    cidade,
				    estado,
				    cep,
				    professor_id
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			preencherEndereco(stmt, endereco);
			stmt.setInt(8, professorId);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir endereço do professor. Nenhuma linha afetada.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					endereco.setIdEndereco(rs.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir endereço do professor. ID não retornado.");
				}
			}
		}
	}

//Atualiza o endereço
	private void atualizarEndereco(Endereco endereco, int professorId) throws SQLException {
		final String sql = """
				UPDATE endereco_professor
				   SET rua = ?,
				       numero = ?,
				       complemento = ?,
				       bairro = ?,
				       cidade = ?,
				       estado = ?,
				       cep = ?
				 WHERE professor_id = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			preencherEndereco(stmt, endereco);
			stmt.setInt(8, professorId);

			int linhasAfetadas = stmt.executeUpdate();

			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao atualizar endereço do professor. Nenhuma linha afetada.");
			}
		}
	}

//Verifica a existência do endereço no cadastro
	private boolean existeEnderecoDoProfessor(int professorId) throws SQLException {
		final String sql = "SELECT 1 FROM endereco_professor WHERE professor_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, professorId);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

//Preenche o endereço
	private void preencherEndereco(PreparedStatement stmt, Endereco endereco) throws SQLException {
		stmt.setString(1, endereco.getRua());
		stmt.setString(2, endereco.getNumero());
		stmt.setString(3, endereco.getComplemento());
		stmt.setString(4, endereco.getBairro());
		stmt.setString(5, endereco.getCidade());
		stmt.setString(6, endereco.getEstado().name());
		stmt.setString(7, endereco.getCep());
	}

//Mapea o professor
	private Professor mapearProfessor(ResultSet rs) throws SQLException {
		Professor professor = new Professor();

		professor.setIdProfessor(rs.getInt("id_professor"));
		professor.setNome(rs.getString("nome"));
		professor.setCpf(rs.getString("cpf"));
		professor.setFormacao(rs.getString("formacao"));
		professor.setTelefone(rs.getString("telefone"));
		professor.setRg(rs.getString("rg"));
		professor.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
		professor.setSexo(Sexo.valueOf(rs.getString("sexo")));
		professor.setAtivo(rs.getBoolean("ativo"));

		if (rs.getObject("id_endereco") != null) {
			professor.setEndereco(mapearEndereco(rs));
		}
		return professor;
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

//Define o select base
	private String sqlBaseSelect() {
		return """
				SELECT *
				FROM professor p
				LEFT JOIN endereco_professor ep ON ep.professor_id = p.id_professor
				""";
	}

//Verifica o motivo da falha da atualização - ID inválido ou cadastro inativo
	private void verificarFalhaAtualizacao(int idProfessor) throws SQLException {
		final String sql = "SELECT ativo FROM professor WHERE id_professor = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Professor não encontrado.");
				}

				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Professor está inativo e não pode ser atualizado.");
				}
			}
		}
	}

//Verifica o motivo da falha da inativação - ID inválido ou cadastro inativo
	private void verificarFalhaInativacao(int idProfessor) throws SQLException {
		final String sql = "SELECT ativo FROM professor WHERE id_professor = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Professor não encontrado.");
				}
				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Professor já está inativo.");
				}
			}
		}
	}

	// Verifica o motivo da falha da reativação - ID inválido ou cadastro ativo
	private void verificarFalhaReativacao(int idProfessor) throws SQLException {
		final String sql = "SELECT ativo FROM professor WHERE id_professor = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idProfessor);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Professor não encontrado.");
				}
				if (rs.getBoolean("ativo")) {
					throw new SQLException("Professor já está ativo.");
				}
			}
		}
	}

//Valida nulo
	private void validarProfessorNaoNulo(Professor professor) {
		if (professor == null) {
			throw new IllegalArgumentException("Professor não pode ser nulo.");
		}
	}

//Valida nulo
	private void validarEnderecoNaoNulo(Endereco endereco) {
		if (endereco == null) {
			throw new IllegalArgumentException("Endereço do professor não pode ser nulo.");
		}
	}
}