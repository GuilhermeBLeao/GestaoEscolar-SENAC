/*Arthur
Guilherme editou para adicionar a exclusão lógica(inativar ao invés de excluir)*/

package dao;

import model.Endereco;
import model.Funcionario;
import util.ValidaCPF;
import variaveisEnum.Estado;
import variaveisEnum.Perfil;
import variaveisEnum.Sexo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

	private final Connection conn;

	public FuncionarioDAO(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
		}
		this.conn = conn;
	}

	public boolean existeCpf(String cpf) throws SQLException {
		String cpfTratado = tratarCpf(cpf);

		final String sql = "SELECT 1 FROM funcionario WHERE cpf = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, cpfTratado);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	public void inserir(Funcionario funcionario) throws SQLException {
		validarFuncionarioNaoNulo(funcionario);
		validarEnderecoNaoNulo(funcionario.getEndereco());
		validarPerfilNaoNulo(funcionario);

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
				    data_nascimento,
				    data_contratacao,
				    perfil,
				    ativo
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			preencherFuncionarioParaInsert(stmt, funcionario);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				throw new SQLException("Falha ao inserir funcionário. Nenhum registro afetado.");
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
		validarPerfilNaoNulo(funcionario);

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
				       data_nascimento = ?,
				       data_contratacao = ?,
				       perfil = ?
				 WHERE id_funcionario = ?
				   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			preencherFuncionarioParaUpdate(stmt, funcionario);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaAtualizacao(funcionario.getIdFuncionario());
			}
		}

		if (existeEnderecoDoFuncionario(funcionario.getIdFuncionario())) {
			atualizarEndereco(funcionario.getEndereco(), funcionario.getIdFuncionario());
		} else {
			inserirEndereco(funcionario.getEndereco(), funcionario.getIdFuncionario());
		}
	}

	public Funcionario buscarPorId(int idFuncionario) throws SQLException {
		if (idFuncionario <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = """
				SELECT *
				FROM funcionario f
				LEFT JOIN endereco_funcionario ef
				    ON ef.funcionario_id = f.id_funcionario
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

	public Funcionario buscarPorNome(String nome) throws SQLException {
		if (nome == null || nome.trim().isEmpty()) {
			throw new IllegalArgumentException("Nome do funcionário inválido.");
		}

		final String sql = """
				SELECT *
				FROM funcionario f
				LEFT JOIN endereco_funcionario ef
				    ON ef.funcionario_id = f.id_funcionario
				WHERE f.nome = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, nome.trim());

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearFuncionario(rs);
				}
				return null;
			}
		}
	}

	public Funcionario buscarPorCpf(String cpf) throws SQLException {
		String cpfTratado = tratarCpf(cpf);

		final String sql = """
				SELECT *
				FROM funcionario f
				LEFT JOIN endereco_funcionario ef
				    ON ef.funcionario_id = f.id_funcionario
				WHERE f.cpf = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, cpfTratado);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearFuncionario(rs);
				}
				return null;
			}
		}
	}

	public List<Funcionario> listarTodos() throws SQLException {
		final String sql = """
				SELECT *
				FROM funcionario f
				LEFT JOIN endereco_funcionario ef
				    ON ef.funcionario_id = f.id_funcionario
				ORDER BY f.nome
				""";

		List<Funcionario> funcionarios = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				funcionarios.add(mapearFuncionario(rs));
			}
		}

		return funcionarios;
	}

	public List<Funcionario> listarAtivos() throws SQLException {
		final String sql = """
				SELECT *
				FROM funcionario f
				LEFT JOIN endereco_funcionario ef
				    ON ef.funcionario_id = f.id_funcionario
				WHERE f.ativo = true
				ORDER BY f.nome
				""";

		List<Funcionario> funcionarios = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				funcionarios.add(mapearFuncionario(rs));
			}
		}

		return funcionarios;
	}

	public List<Funcionario> listarInativos() throws SQLException {
		final String sql = """
				SELECT *
				FROM funcionario f
				LEFT JOIN endereco_funcionario ef
				    ON ef.funcionario_id = f.id_funcionario
				WHERE f.ativo = false
				ORDER BY f.nome
				""";

		List<Funcionario> funcionarios = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				funcionarios.add(mapearFuncionario(rs));
			}
		}

		return funcionarios;
	}

	public boolean inativar(int idFuncionario) throws SQLException {
		if (idFuncionario <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = """
				UPDATE funcionario
				   SET ativo = false
				 WHERE id_funcionario = ?
				   AND ativo = true
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idFuncionario);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaInativacao(idFuncionario);
			}

			return true;
		}
	}

	public boolean reativar(int idFuncionario) throws SQLException {
		if (idFuncionario <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = """
				UPDATE funcionario
				   SET ativo = true
				 WHERE id_funcionario = ?
				   AND ativo = false
				""";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idFuncionario);

			int linhasAfetadas = stmt.executeUpdate();
			if (linhasAfetadas == 0) {
				verificarFalhaReativacao(idFuncionario);
			}

			return true;
		}
	}

	private Funcionario mapearFuncionario(ResultSet rs) throws SQLException {
		Funcionario funcionario = new Funcionario();

		funcionario.setIdFuncionario(rs.getInt("id_funcionario"));
		funcionario.setNome(rs.getString("nome"));
		funcionario.setCpf(rs.getString("cpf"));
		funcionario.setCargo(rs.getString("cargo"));
		funcionario.setTelefone(rs.getString("telefone"));
		funcionario.setRg(rs.getString("rg"));

		String sexo = rs.getString("sexo");
		if (sexo != null) {
			funcionario.setSexo(Sexo.valueOf(sexo));
		}

		funcionario.setSetor(rs.getString("setor"));
		funcionario.setEmail(rs.getString("email"));
		funcionario.setAtivo(rs.getBoolean("ativo"));

		Date dataNascimento = rs.getDate("data_nascimento");
		if (dataNascimento != null) {
			funcionario.setDataNascimento(dataNascimento.toLocalDate());
		}

		Date dataContratacao = rs.getDate("data_contratacao");
		if (dataContratacao != null) {
			funcionario.setDataContratacao(dataContratacao.toLocalDate());
		}

		String perfil = rs.getString("perfil");
		if (perfil != null) {
			funcionario.setPerfil(Perfil.valueOf(perfil));
		}

		if (rs.getObject("id_endereco") != null) {
			funcionario.setEndereco(mapearEndereco(rs));
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

		String estado = rs.getString("estado");
		if (estado != null) {
			endereco.setEstado(Estado.valueOf(estado));
		}

		endereco.setCep(rs.getString("cep"));
		return endereco;
	}

	private void atualizarEndereco(Endereco endereco, int funcionarioId) throws SQLException {
		if (funcionarioId <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = """
				UPDATE endereco_funcionario
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
		if (funcionarioId <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = "SELECT 1 FROM endereco_funcionario WHERE funcionario_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, funcionarioId);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	private void inserirEndereco(Endereco endereco, int funcionarioId) throws SQLException {
		if (funcionarioId <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = """
				INSERT INTO endereco_funcionario (
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
				throw new SQLException("Falha ao inserir endereço do funcionário. Nenhum registro foi afetado.");
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

	private void preencherEndereco(PreparedStatement stmt, Endereco endereco) throws SQLException {
		stmt.setString(1, endereco.getRua());
		stmt.setString(2, endereco.getNumero());
		stmt.setString(3, endereco.getComplemento());
		stmt.setString(4, endereco.getBairro());
		stmt.setString(5, endereco.getCidade());
		stmt.setString(6, endereco.getEstado().name());
		stmt.setString(7, endereco.getCep());
	}

	private void preencherFuncionarioParaInsert(PreparedStatement stmt, Funcionario funcionario) throws SQLException {
		stmt.setString(1, funcionario.getNome());
		stmt.setString(2, tratarCpf(funcionario.getCpf()));
		stmt.setString(3, funcionario.getCargo());
		stmt.setString(4, funcionario.getTelefone());
		stmt.setString(5, funcionario.getRg());
		stmt.setString(6, funcionario.getSexo().name());
		stmt.setString(7, funcionario.getSetor());
		stmt.setString(8, funcionario.getEmail());
		stmt.setDate(9, Date.valueOf(funcionario.getDataNascimento()));
		stmt.setDate(10, Date.valueOf(funcionario.getDataContratacao()));
		stmt.setString(11, funcionario.getPerfil().name());
	}

	private void preencherFuncionarioParaUpdate(PreparedStatement stmt, Funcionario funcionario) throws SQLException {
		stmt.setString(1, funcionario.getNome());
		stmt.setString(2, funcionario.getCargo());
		stmt.setString(3, funcionario.getTelefone());
		stmt.setString(4, funcionario.getRg());
		stmt.setString(5, funcionario.getSexo().name());
		stmt.setString(6, funcionario.getSetor());
		stmt.setString(7, funcionario.getEmail());
		stmt.setDate(8, Date.valueOf(funcionario.getDataNascimento()));
		stmt.setDate(9, Date.valueOf(funcionario.getDataContratacao()));
		stmt.setString(10, funcionario.getPerfil().name());
		stmt.setInt(11, funcionario.getIdFuncionario());
	}

	private void verificarFalhaAtualizacao(int idFuncionario) throws SQLException {
		if (idFuncionario <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = "SELECT ativo FROM funcionario WHERE id_funcionario = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idFuncionario);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Funcionário não encontrado.");
				}

				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Funcionário está inativo e não pode ser atualizado.");
				}
			}
		}
	}

	private void verificarFalhaInativacao(int idFuncionario) throws SQLException {
		if (idFuncionario <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = "SELECT ativo FROM funcionario WHERE id_funcionario = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idFuncionario);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Funcionário não encontrado.");
				}

				if (!rs.getBoolean("ativo")) {
					throw new SQLException("Funcionário já está inativo.");
				}
			}
		}
	}

	private void verificarFalhaReativacao(int idFuncionario) throws SQLException {
		if (idFuncionario <= 0) {
			throw new IllegalArgumentException("ID do funcionário inválido.");
		}

		final String sql = "SELECT ativo FROM funcionario WHERE id_funcionario = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idFuncionario);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("Funcionário não encontrado.");
				}

				if (rs.getBoolean("ativo")) {
					throw new SQLException("Funcionário já está ativo.");
				}
			}
		}
	}

	private void validarEnderecoNaoNulo(Endereco endereco) {
		if (endereco == null) {
			throw new IllegalArgumentException("Endereço do funcionário não pode ser nulo.");
		}
	}

	private void validarFuncionarioNaoNulo(Funcionario funcionario) {
		if (funcionario == null) {
			throw new IllegalArgumentException("Funcionário não pode ser nulo.");
		}
	}

	private void validarPerfilNaoNulo(Funcionario funcionario) {
		if (funcionario.getPerfil() == null) {
			throw new IllegalArgumentException("Perfil do funcionário é obrigatório.");
		}
	}

	private String tratarCpf(String cpf) {
		if (cpf == null) {
			throw new IllegalArgumentException("CPF do funcionário inválido.");
		}

		String cpfTratado = cpf.trim().replaceAll("\\D", "");

		if (!ValidaCPF.isValido(cpfTratado)) {
			throw new IllegalArgumentException("CPF do funcionário inválido.");
		}

		return cpfTratado;
	}
}
