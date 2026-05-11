//Guilherme

package controller;

import dao.AlunoDAO;
import dao.FuncionarioDAO;
import dao.PaisAlunoDAO;
import dao.ProfessorDAO;
import dao.UsuarioDAO;
import database.ConnectionFactory;
import model.Usuario;
import util.ValidaCPF;
import util.ValidaSenha;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UsuarioController {
	@FunctionalInterface
	private interface AcaoTransacional<T> {
		T executar(Connection conn) throws SQLException;
	}

	private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			conn.setAutoCommit(false);
			try {
				T r = acao.executar(conn);
				conn.commit();
				return r;
			} catch (IllegalArgumentException e) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					e.addSuppressed(ex);
				}
				throw e;
			} catch (SQLException | RuntimeException e) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					e.addSuppressed(ex);
				}
				throw new RuntimeException(mensagemOperacao, e);
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
		}
	}

	private void validarNaoNulo(Usuario u) {
		if (u == null)
			throw new IllegalArgumentException("Usuário não pode ser nulo.");
	}

	private String tratarTexto(String v) {
		return v == null ? null : v.trim();
	}

	private String normalizarCpf(String v) {
		String t = tratarTexto(v);
		return t == null ? null : t.replaceAll("\\D", "");
	}

	private void normalizar(Usuario u) {
		/*
		 * CPF já é normalizado pelo Usuario.setCpf(). Não chamar setCpf novamente para
		 * preservar imutabilidade.
		 */ }

	private void validarCamposBase(Usuario u) {
		if (u.getCpf() == null || !ValidaCPF.isValido(u.getCpf()))
			throw new IllegalArgumentException("CPF do usuário inválido.");
		if (u.getTipoUsuario() == null)
			throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
	}

	private void validarHashObrigatorio(Usuario u) {
		if (u.getSenhaHash() == null || u.getSenhaHash().isBlank())
			throw new IllegalArgumentException("Senha do usuário é obrigatória.");
		validarHash(u.getSenhaHash());
	}

	private void validarHashSeInformado(Usuario u) {
		if (u.getSenhaHash() != null) {
			if (u.getSenhaHash().isBlank())
				throw new IllegalArgumentException("Hash da senha informado é inválido.");
			validarHash(u.getSenhaHash());
		}
	}

	private void validarHash(String hash) {
		if (!hash.matches("^\\$2[aby]\\$\\d{2}\\$.*$"))
			throw new IllegalArgumentException(
					"Hash da senha inválido. Informe um hash BCrypt válido ou use setSenha(...) no model.");
	}

	private void validarCpfImutavel(Usuario a, Usuario b) {
		if (!b.getCpf().equals(a.getCpf()))
			throw new IllegalArgumentException("CPF do usuário não pode ser alterado após o cadastro.");
	}

	private int contarVinculos(Usuario u) {
		int t = 0;
		if (u.getAlunoId() > 0)
			t++;
		if (u.getFuncionarioId() > 0)
			t++;
		if (u.getPaiId() > 0)
			t++;
		if (u.getProfessorId() > 0)
			t++;
		return t;
	}

	private void validarVinculoPorTipo(Usuario u) {
		if (contarVinculos(u) != 1)
			throw new IllegalArgumentException(
					"Usuário deve possuir exatamente um vínculo entre aluno, responsável, professor ou funcionário.");
		switch (u.getTipoUsuario().name()) {
		case "ALUNO":
			if (u.getAlunoId() <= 0 || u.getFuncionarioId() > 0 || u.getPaiId() > 0 || u.getProfessorId() > 0)
				throw new IllegalArgumentException("Usuário ALUNO deve possuir apenas alunoId válido.");
			break;
		case "RESPONSAVEL":
			if (u.getPaiId() <= 0 || u.getAlunoId() > 0 || u.getFuncionarioId() > 0 || u.getProfessorId() > 0)
				throw new IllegalArgumentException("Usuário RESPONSAVEL deve possuir apenas paiId válido.");
			break;
		case "PROFESSOR":
			if (u.getProfessorId() <= 0 || u.getAlunoId() > 0 || u.getFuncionarioId() > 0 || u.getPaiId() > 0)
				throw new IllegalArgumentException("Usuário PROFESSOR deve possuir apenas professorId válido.");
			break;
		case "SECRETARIA":
		case "DIRECAO":
		case "PEDAGOGICO":
			if (u.getFuncionarioId() <= 0 || u.getAlunoId() > 0 || u.getPaiId() > 0 || u.getProfessorId() > 0)
				throw new IllegalArgumentException("Usuário administrativo deve possuir apenas funcionarioId válido.");
			break;
		default:
			throw new IllegalArgumentException(
					"Tipo de usuário não mapeado no controller: " + u.getTipoUsuario().name() + ".");
		}
	}

	private void validarDatasCadastro(Usuario u) {
		if (u.getDataCriacao() == null)
			throw new IllegalArgumentException("Data de criação é obrigatória.");
		if (u.getDataCriacao().isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data de criação não pode ser futura.");
		if (u.getUltimoLogin() != null)
			throw new IllegalArgumentException("Último login não deve ser informado no cadastro.");
	}

	private void validarDatasAtualizacao(Usuario a, Usuario b) {
		if (a.getDataCriacao() != null && !a.getDataCriacao().equals(b.getDataCriacao()))
			throw new IllegalArgumentException("Data de criação é controlada pelo sistema e não pode ser alterada.");
		if (a.getUltimoLogin() != null
				&& (b.getUltimoLogin() == null || !a.getUltimoLogin().equals(b.getUltimoLogin())))
			throw new IllegalArgumentException("Último login só pode ser alterado pelo fluxo de autenticação.");
	}

	private void validarVinculosNoBanco(Connection conn, UsuarioDAO dao, Usuario u, int ignorarId) throws SQLException {
		if (u.getAlunoId() > 0) {
			if (new AlunoDAO(conn).buscarPorId(u.getAlunoId()) == null)
				throw new IllegalArgumentException("Aluno vinculado não existe.");
			if (dao.existeAlunoId(u.getAlunoId(), ignorarId))
				throw new IllegalArgumentException("Aluno já vinculado a outro usuário.");
		}
		if (u.getFuncionarioId() > 0) {
			if (new FuncionarioDAO(conn).buscarPorId(u.getFuncionarioId()) == null)
				throw new IllegalArgumentException("Funcionário vinculado não existe.");
			if (dao.existeFuncionarioId(u.getFuncionarioId(), ignorarId))
				throw new IllegalArgumentException("Funcionário já vinculado a outro usuário.");
		}
		if (u.getPaiId() > 0) {
			if (new PaisAlunoDAO(conn).buscarPorId(u.getPaiId()) == null)
				throw new IllegalArgumentException("Responsável vinculado não existe.");
			if (dao.existePaiId(u.getPaiId(), ignorarId))
				throw new IllegalArgumentException("Responsável já vinculado a outro usuário.");
		}
		if (u.getProfessorId() > 0) {
			ProfessorDAO pdao = new ProfessorDAO(conn);
			if (pdao.buscarPorId(u.getProfessorId()) == null
					&& pdao.listarTodos().stream().noneMatch(p -> p.getIdProfessor() == u.getProfessorId()))
				throw new IllegalArgumentException("Professor vinculado não existe.");
			if (dao.existeProfessorId(u.getProfessorId(), ignorarId))
				throw new IllegalArgumentException("Professor já vinculado a outro usuário.");
		}
	}

	private Usuario mesclar(Usuario b, Usuario a) {
		b.setTipoUsuario(a.getTipoUsuario());
		b.setAlunoId(a.getAlunoId());
		b.setFuncionarioId(a.getFuncionarioId());
		b.setPaiId(a.getPaiId());
		b.setProfessorId(a.getProfessorId());
		if (a.getSenhaHash() != null && !a.getSenhaHash().isBlank())
			b.setSenhaHash(a.getSenhaHash());
		return b;
	}

	private void informarSeInativo(Usuario u) {
		if (u != null && !u.isAtivo())
			System.out.println("ATENÇÃO: usuário encontrado, porém está inativo.");
	}

	// Salva um usuário novo que já possua senhaHash BCrypt válido.
	public void salvarUsuario(Usuario usuario) {
		cadastrarUsuario(usuario);
	}

	// Salva um usuário novo gerando o hash por meio do método setSenha do model.
	public void salvarUsuario(Usuario usuario, String senha) {
		cadastrarUsuario(usuario, senha);
	}

	// Cadastra um usuário novo que já possua senhaHash BCrypt válido.
	public void cadastrarUsuario(Usuario usuario) {
		validarNaoNulo(usuario);
		normalizar(usuario);
		validarCamposBase(usuario);
		validarHashObrigatorio(usuario);
		validarDatasCadastro(usuario);
		validarVinculoPorTipo(usuario);
		executarEmTransacao(conn -> {
			UsuarioDAO dao = new UsuarioDAO(conn);
			if (dao.existeCpf(usuario.getCpf()))
				throw new IllegalArgumentException("Já existe usuário cadastrado com este CPF.");
			validarVinculosNoBanco(conn, dao, usuario, 0);
			dao.inserir(usuario);
			return null;
		}, "Erro ao cadastrar usuário.");
	}

	// Cadastra um usuário novo recebendo a senha em texto puro e delegando o hash
	// ao model.
	public void cadastrarUsuario(Usuario usuario, String senha) {
		validarNaoNulo(usuario);
		ValidaSenha.validar(senha);
		usuario.setSenha(senha);
		cadastrarUsuario(usuario);
	}

	// Atualiza um usuário ativo, mantendo CPF, data de criação e último login
	// protegidos.
	public void atualizarUsuario(Usuario usuarioAtualizado) {
		validarNaoNulo(usuarioAtualizado);
		if (usuarioAtualizado.getIdUsuario() <= 0)
			throw new IllegalArgumentException("ID do usuário inválido.");
		normalizar(usuarioAtualizado);
		validarCamposBase(usuarioAtualizado);
		validarHashSeInformado(usuarioAtualizado);
		validarVinculoPorTipo(usuarioAtualizado);
		executarEmTransacao(conn -> {
			UsuarioDAO dao = new UsuarioDAO(conn);
			Usuario banco = dao.buscarPorId(usuarioAtualizado.getIdUsuario());
			if (banco == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			if (!banco.isAtivo())
				throw new IllegalArgumentException("Não é possível atualizar usuário inativo.");
			validarCpfImutavel(usuarioAtualizado, banco);
			validarDatasAtualizacao(usuarioAtualizado, banco);
			validarVinculosNoBanco(conn, dao, usuarioAtualizado, usuarioAtualizado.getIdUsuario());
			dao.atualizar(mesclar(banco, usuarioAtualizado));
			return null;
		}, "Erro ao atualizar usuário.");
	}

	// Atualiza a senha do usuário ativo a partir de um hash BCrypt válido.
	public void atualizarSenhaHash(int idUsuario, String senhaHash) {
		if (idUsuario <= 0)
			throw new IllegalArgumentException("ID do usuário inválido.");
		if (senhaHash == null || senhaHash.isBlank())
			throw new IllegalArgumentException("Hash da senha é obrigatório.");
		validarHash(senhaHash);
		executarEmTransacao(conn -> {
			new UsuarioDAO(conn).atualizarSenhaHash(idUsuario, senhaHash);
			return null;
		}, "Erro ao atualizar senha do usuário.");
	}

	// Atualiza o último login do usuário.
	public void atualizarUltimoLogin(int idUsuario, LocalDateTime ultimoLogin) {
		if (idUsuario <= 0)
			throw new IllegalArgumentException("ID do usuário inválido.");
		if (ultimoLogin == null || ultimoLogin.isAfter(LocalDateTime.now()))
			throw new IllegalArgumentException("Último login inválido.");
		executarEmTransacao(conn -> {
			new UsuarioDAO(conn).atualizarUltimoLogin(idUsuario, ultimoLogin);
			return null;
		}, "Erro ao atualizar último login.");
	}

	// Autentica um usuário pelo CPF e senha.
	public Usuario autenticarLogin(String cpf, String senha) {
		String c = normalizarCpf(cpf);
		if (c == null || !ValidaCPF.isValido(c))
			throw new IllegalArgumentException("CPF inválido.");
		if (senha == null || senha.isBlank())
			throw new IllegalArgumentException("Senha é obrigatória.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDAO(conn).autenticar(c, senha);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao autenticar usuário.", e);
		}
	}

	// Realiza exclusão lógica do usuário, inativando o cadastro no banco.
	public boolean excluirUsuario(int idUsuario) {
		if (idUsuario <= 0)
			throw new IllegalArgumentException("ID do usuário inválido.");
		return executarEmTransacao(conn -> {
			UsuarioDAO dao = new UsuarioDAO(conn);
			if (dao.buscarPorId(idUsuario) == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			return dao.inativar(idUsuario);
		}, "Erro ao excluir usuário.");
	}

	// Reativa um usuário previamente inativado.
	public boolean reativarUsuario(int idUsuario) {
		if (idUsuario <= 0)
			throw new IllegalArgumentException("ID do usuário inválido.");
		return executarEmTransacao(conn -> new UsuarioDAO(conn).reativar(idUsuario), "Erro ao reativar usuário.");
	}

	// Busca usuário por ID, retornando também inativos e avisando quando o cadastro
	// estiver inativo.
	public Usuario buscarUsuarioPorId(int idUsuario) {
		if (idUsuario <= 0)
			throw new IllegalArgumentException("ID do usuário inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Usuario u = new UsuarioDAO(conn).buscarPorId(idUsuario);
			if (u == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			informarSeInativo(u);
			return u;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por ID.", e);
		}
	}

	// Busca usuário por CPF, retornando também inativos e avisando quando o
	// cadastro estiver inativo.
	public Usuario buscarUsuarioPorCpf(String cpf) {
		String c = normalizarCpf(cpf);
		if (c == null || !ValidaCPF.isValido(c))
			throw new IllegalArgumentException("CPF inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Usuario u = new UsuarioDAO(conn).buscarPorCpf(c);
			if (u == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			informarSeInativo(u);
			return u;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por CPF.", e);
		}
	}

	// Busca usuário pelo ID do aluno vinculado.
	public Usuario buscarUsuarioPorAlunoId(int alunoId) {
		if (alunoId <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Usuario u = new UsuarioDAO(conn).buscarPorAlunoId(alunoId);
			if (u == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			informarSeInativo(u);
			return u;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por aluno.", e);
		}
	}

	// Busca usuário pelo ID do funcionário vinculado.
	public Usuario buscarUsuarioPorFuncionarioId(int funcionarioId) {
		if (funcionarioId <= 0)
			throw new IllegalArgumentException("ID do funcionário inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Usuario u = new UsuarioDAO(conn).buscarPorFuncionarioId(funcionarioId);
			if (u == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			informarSeInativo(u);
			return u;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por funcionário.", e);
		}
	}

	// Busca usuário pelo ID do responsável vinculado.
	public Usuario buscarUsuarioPorPaiId(int paiId) {
		if (paiId <= 0)
			throw new IllegalArgumentException("ID do responsável inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Usuario u = new UsuarioDAO(conn).buscarPorPaiId(paiId);
			if (u == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			informarSeInativo(u);
			return u;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por responsável.", e);
		}
	}

	// Busca usuário pelo ID do professor vinculado.
	public Usuario buscarUsuarioPorProfessorId(int professorId) {
		if (professorId <= 0)
			throw new IllegalArgumentException("ID do professor inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Usuario u = new UsuarioDAO(conn).buscarPorProfessorId(professorId);
			if (u == null)
				throw new IllegalArgumentException("Usuário não encontrado.");
			informarSeInativo(u);
			return u;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por professor.", e);
		}
	}

	// Lista todos os usuários, ativos e inativos.
	public List<Usuario> listarUsuarios() {
		return listarTodosUsuarios();
	}

	// Lista todos os usuários, ativos e inativos.
	public List<Usuario> listarTodosUsuarios() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDAO(conn).listarTodos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar usuários.", e);
		}
	}

	// Lista somente usuários ativos.
	public List<Usuario> listarUsuariosAtivos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDAO(conn).listarAtivos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar usuários ativos.", e);
		}
	}

	// Lista somente usuários inativos.
	public List<Usuario> listarUsuariosInativos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDAO(conn).listarInativos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar usuários inativos.", e);
		}
	}
}
