package controller;

import dao.ProfessorDAO;
import dao.UsuarioDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Professor;
import model.Usuario;
import util.ValidaCEP;
import util.ValidaCPF;
import util.ValidaCidade;
import util.ValidaNome;
import util.ValidaTelefone;
import variaveisEnum.TipoUsuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProfessorController {

	private static final String SENHA_PADRAO = "Senha@123";

	@FunctionalInterface
	private interface AcaoTransacional<T> {
		T executar(Connection conn) throws SQLException;
	}

	private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {

		try (Connection conn = ConnectionFactory.getConnection()) {

			conn.setAutoCommit(false);

			try {

				T resultado = acao.executar(conn);

				conn.commit();

				return resultado;

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

	private void validarNaoNulo(Professor professor) {

		if (professor == null) {
			throw new IllegalArgumentException("Professor não pode ser nulo.");
		}
	}

	private String tratarTexto(String valor) {

		return valor == null ? null : valor.trim();
	}

	private String normalizarCpf(String valor) {

		String texto = tratarTexto(valor);

		return texto == null ? null : texto.replaceAll("\\D", "");
	}

	private String normalizarTelefone(String valor) {

		String texto = tratarTexto(valor);

		return texto == null ? null : texto.replaceAll("\\D", "");
	}

	private void normalizarDadosEditaveis(Professor professor) {

		professor.setNome(tratarTexto(professor.getNome()));

		professor.setFormacao(tratarTexto(professor.getFormacao()));

		professor.setTelefone(normalizarTelefone(professor.getTelefone()));

		professor.setRg(tratarTexto(professor.getRg()));

		Endereco endereco = professor.getEndereco();

		if (endereco != null) {

			endereco.setRua(tratarTexto(endereco.getRua()));

			endereco.setNumero(tratarTexto(endereco.getNumero()));

			endereco.setComplemento(tratarTexto(endereco.getComplemento()));

			endereco.setBairro(tratarTexto(endereco.getBairro()));

			endereco.setCidade(tratarTexto(endereco.getCidade()));

			if (endereco.getCep() != null) {

				endereco.setCep(endereco.getCep().trim().replaceAll("\\D", ""));
			}
		}
	}

	private void validarEndereco(Endereco endereco) {

		if (endereco == null) {

			throw new IllegalArgumentException("Endereço é obrigatório.");
		}

		ValidaCidade.validar(endereco.getCidade());

		if (endereco.getEstado() == null) {

			throw new IllegalArgumentException("Estado é obrigatório.");
		}

		if (!ValidaCEP.isValido(endereco.getCep())) {

			throw new IllegalArgumentException("CEP inválido.");
		}
	}

	private void validarCamposBase(Professor professor) {

		ValidaNome.validar(professor.getNome());

		String cpfNormalizado = normalizarCpf(professor.getCpf());

		if (cpfNormalizado == null || !ValidaCPF.isValido(cpfNormalizado)) {

			throw new IllegalArgumentException("CPF do professor inválido.");
		}

		if (professor.getFormacao() == null || professor.getFormacao().isBlank()) {

			throw new IllegalArgumentException("Formação do professor é obrigatória.");
		}

		if (!ValidaTelefone.isValido(professor.getTelefone())) {

			throw new IllegalArgumentException("Telefone do professor inválido.");
		}

		if (professor.getSexo() == null) {

			throw new IllegalArgumentException("Sexo do professor é obrigatório.");
		}

		if (professor.getDataNascimento() == null || professor.getDataNascimento().isAfter(LocalDate.now())) {

			throw new IllegalArgumentException("Data de nascimento do professor inválida.");
		}

		validarEndereco(professor.getEndereco());
	}

	private void validarCpfImutavel(Professor professorAtualizado, Professor professorBanco) {

		String cpfAtualizado = normalizarCpf(professorAtualizado.getCpf());

		String cpfBanco = normalizarCpf(professorBanco.getCpf());

		if (!cpfBanco.equals(cpfAtualizado)) {

			throw new IllegalArgumentException("CPF do professor não pode ser alterado " + "após o cadastro.");
		}
	}

	private Professor mesclar(Professor professorBanco, Professor professorAtualizado) {

		professorBanco.setNome(professorAtualizado.getNome());

		professorBanco.setFormacao(professorAtualizado.getFormacao());

		professorBanco.setTelefone(professorAtualizado.getTelefone());

		professorBanco.setRg(professorAtualizado.getRg());

		professorBanco.setSexo(professorAtualizado.getSexo());

		professorBanco.setDataNascimento(professorAtualizado.getDataNascimento());

		professorBanco.setEndereco(professorAtualizado.getEndereco());

		return professorBanco;
	}

	private void informarSeInativo(Professor professor) {

		if (professor != null && !professor.isAtivo()) {

			System.out.println("ATENÇÃO: professor encontrado, " + "porém está inativo.");
		}
	}

	/**
	 * Cria uma nova conta para o professor ou reutiliza uma conta já existente com
	 * o mesmo CPF.
	 *
	 * Se a conta existente for de um responsável, o pai_id é preservado e o
	 * professor_id é acrescentado à mesma conta.
	 */
	private void criarOuVincularUsuarioProfessor(Connection conn, Professor professor) throws SQLException {

		UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

		String cpf = normalizarCpf(professor.getCpf());

		Usuario usuarioExistente = usuarioDAO.buscarPorCpf(cpf);

		if (usuarioExistente == null) {

			Usuario usuario = new Usuario();

			usuario.setCpf(cpf);
			usuario.setSenha(SENHA_PADRAO);
			usuario.setTipoUsuario(TipoUsuario.PROFESSOR);
			usuario.setProfessorId(professor.getIdProfessor());

			usuarioDAO.inserir(usuario);

			return;
		}

		if (usuarioExistente.getProfessorId() > 0 && usuarioExistente.getProfessorId() != professor.getIdProfessor()) {

			throw new IllegalArgumentException("Já existe um usuário vinculado a outro professor " + "com este CPF.");
		}

		/*
		 * Mantemos o paiId existente.
		 *
		 * Dessa forma, uma mesma pessoa pode ser:
		 *
		 * - responsável por aluno; - professor;
		 *
		 * utilizando uma única conta.
		 */
		usuarioExistente.setProfessorId(professor.getIdProfessor());

		usuarioExistente.setTipoUsuario(TipoUsuario.PROFESSOR);

		if (usuarioExistente.getSenhaHash() == null || usuarioExistente.getSenhaHash().isBlank()) {

			usuarioExistente.setSenha(SENHA_PADRAO);
		}

		if (!usuarioExistente.isAtivo()) {

			usuarioExistente.setAtivo(true);
		}

		usuarioDAO.atualizar(usuarioExistente);
	}

	private void inativarUsuarioProfessor(Connection conn, int idProfessor) throws SQLException {

		UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

		Usuario usuario = usuarioDAO.buscarPorProfessorId(idProfessor);

		if (usuario != null && usuario.isAtivo()) {

			usuarioDAO.inativar(usuario.getIdUsuario());
		}
	}

	private void reativarUsuarioProfessor(Connection conn, int idProfessor) throws SQLException {

		UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

		Usuario usuario = usuarioDAO.buscarPorProfessorId(idProfessor);

		if (usuario != null && !usuario.isAtivo()) {

			usuarioDAO.reativar(usuario.getIdUsuario());
		}
	}

	public void salvarProfessor(Professor professor) {

	    validarNaoNulo(professor);
	    normalizarDadosEditaveis(professor);
	    validarCamposBase(professor);

	    executarEmTransacao(conn -> {

	        ProfessorDAO dao = new ProfessorDAO(conn);

	        String cpf = normalizarCpf(professor.getCpf());

	        if (dao.existeCpf(cpf)) {

	            throw new IllegalArgumentException(
	                    "Já existe professor cadastrado com este CPF.");
	        }

	        /*
	         * Primeiro cadastramos o professor.
	         * O ID gerado será utilizado para criar/vincular o usuário.
	         */

	        dao.inserir(professor);

	        /*
	         * Cria uma nova conta ou reutiliza uma conta existente.
	         */

	        criarOuVincularUsuarioProfessor(conn, professor);

	        return null;

	    }, "Erro ao salvar professor.");
	}

	public void atualizarProfessor(Professor professorAtualizado) {

		validarNaoNulo(professorAtualizado);

		if (professorAtualizado.getIdProfessor() <= 0) {

			throw new IllegalArgumentException("ID do professor inválido.");
		}

		normalizarDadosEditaveis(professorAtualizado);

		validarCamposBase(professorAtualizado);

		executarEmTransacao(conn -> {

			ProfessorDAO dao = new ProfessorDAO(conn);

			Professor professorBanco = dao.buscarPorId(professorAtualizado.getIdProfessor());

			if (professorBanco == null) {

				throw new IllegalArgumentException("Professor não encontrado.");
			}

			if (!professorBanco.isAtivo()) {

				throw new IllegalArgumentException("Não é possível atualizar " + "professor inativo.");
			}

			validarCpfImutavel(professorAtualizado, professorBanco);

			dao.atualizar(mesclar(professorBanco, professorAtualizado));

			return null;

		}, "Erro ao atualizar professor.");
	}

	public boolean excluirProfessor(int idProfessor) {

		if (idProfessor <= 0) {

			throw new IllegalArgumentException("ID do professor inválido.");
		}

		return executarEmTransacao(conn -> {

			ProfessorDAO dao = new ProfessorDAO(conn);

			Professor professor = dao.buscarPorId(idProfessor);

			if (professor == null) {

				throw new IllegalArgumentException("Professor não encontrado.");
			}

			boolean resultado = dao.inativar(idProfessor);

			if (resultado) {

				inativarUsuarioProfessor(conn, idProfessor);
			}

			return resultado;

		}, "Erro ao excluir professor.");
	}

	public boolean reativarProfessor(int idProfessor) {

		if (idProfessor <= 0) {

			throw new IllegalArgumentException("ID do professor inválido.");
		}

		return executarEmTransacao(conn -> {

			ProfessorDAO dao = new ProfessorDAO(conn);

			boolean resultado = dao.reativar(idProfessor);

			if (resultado) {

				reativarUsuarioProfessor(conn, idProfessor);
			}

			return resultado;

		}, "Erro ao reativar professor.");
	}

	public Professor buscarProfessorPorId(int idProfessor) {

		if (idProfessor <= 0) {

			throw new IllegalArgumentException("ID do professor inválido.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			Professor professor = new ProfessorDAO(conn).buscarPorId(idProfessor);

			if (professor == null) {

				throw new IllegalArgumentException("Professor não encontrado.");
			}

			informarSeInativo(professor);

			return professor;

		} catch (SQLException e) {

			throw new RuntimeException("Erro ao buscar professor por ID.", e);
		}
	}

	public Professor buscarProfessorPorCpf(String cpf) {

		String cpfNormalizado = normalizarCpf(cpf);

		if (cpfNormalizado == null || !ValidaCPF.isValido(cpfNormalizado)) {

			throw new IllegalArgumentException("CPF do professor inválido.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			Professor professor = new ProfessorDAO(conn).buscarPorCpf(cpfNormalizado);

			if (professor == null) {

				throw new IllegalArgumentException("Professor não encontrado.");
			}

			informarSeInativo(professor);

			return professor;

		} catch (SQLException e) {

			throw new RuntimeException("Erro ao buscar professor por CPF.", e);
		}
	}

	public Professor buscarProfessorPorNome(String nome) {

		String nomeTratado = tratarTexto(nome);

		if (nomeTratado == null || nomeTratado.isBlank()) {

			throw new IllegalArgumentException("Nome do professor é obrigatório.");
		}

		try (Connection conn = ConnectionFactory.getConnection()) {

			Professor professor = new ProfessorDAO(conn).buscarPorNome(nomeTratado);

			if (professor == null) {

				throw new IllegalArgumentException("Professor não encontrado.");
			}

			informarSeInativo(professor);

			return professor;

		} catch (SQLException e) {

			throw new RuntimeException("Erro ao buscar professor por nome.", e);
		}
	}

	public List<Professor> listarProfessores() {

		return listarTodosProfessores();
	}

	public List<Professor> listarTodosProfessores() {

		try (Connection conn = ConnectionFactory.getConnection()) {

			return new ProfessorDAO(conn).listarTodos();

		} catch (SQLException e) {

			throw new RuntimeException("Erro ao listar professores.", e);
		}
	}

	public List<Professor> listarProfessoresAtivos() {

		try (Connection conn = ConnectionFactory.getConnection()) {

			return new ProfessorDAO(conn).listarAtivos();

		} catch (SQLException e) {

			throw new RuntimeException("Erro ao listar professores ativos.", e);
		}
	}

	public List<Professor> listarProfessoresInativos() {

		try (Connection conn = ConnectionFactory.getConnection()) {

			return new ProfessorDAO(conn).listarInativos();

		} catch (SQLException e) {

			throw new RuntimeException("Erro ao listar professores inativos.", e);
		}
	}
}