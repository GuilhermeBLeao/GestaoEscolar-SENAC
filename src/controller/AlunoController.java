//Guilherme

package controller;

import dao.AlunoDAO;
import dao.PaisAlunoDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Endereco;
import util.ValidaCEP;
import util.ValidaCPF;
import util.ValidaCidade;
import util.ValidaEmail;
import util.ValidaNome;
import util.ValidaTelefone;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AlunoController {
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

	private void validarNaoNulo(Aluno aluno) {
		if (aluno == null)
			throw new IllegalArgumentException("Aluno não pode ser nulo.");
	}

	private String tratarTexto(String v) {
		return v == null ? null : v.trim();
	}

	private String normalizarCpf(String v) {
		String t = tratarTexto(v);
		return t == null ? null : t.replaceAll("\\D", "");
	}

	private String normalizarEmail(String v) {
		String t = tratarTexto(v);
		return t == null ? null : t.toLowerCase();
	}

	private String normalizarTelefone(String v) {
		String t = tratarTexto(v);
		return t == null ? null : t.replaceAll("\\D", "");
	}

	private String normalizarCep(String v) {
		String t = tratarTexto(v);
		return t == null ? null : t.replaceAll("\\D", "");
	}

	private void normalizar(Aluno a) {
		// CPF não é setado novamente aqui porque Aluno.setCpf() é imutável após
		// definido.
		// O próprio setter do model já remove pontuação quando o CPF é definido.
		a.setNome(tratarTexto(a.getNome()));
		a.setEmail(normalizarEmail(a.getEmail()));
		a.setTelefone(normalizarTelefone(a.getTelefone()));
		a.setMatricula(tratarTexto(a.getMatricula()));
		a.setRg(tratarTexto(a.getRg()));
		a.setObsSaude(tratarTexto(a.getObsSaude()));
		Endereco e = a.getEndereco();
		if (e != null) {
			e.setRua(tratarTexto(e.getRua()));
			e.setNumero(tratarTexto(e.getNumero()));
			e.setComplemento(tratarTexto(e.getComplemento()));
			e.setBairro(tratarTexto(e.getBairro()));
			e.setCidade(tratarTexto(e.getCidade()));
			e.setCep(normalizarCep(e.getCep()));
		}
	}

	private void validarCamposBase(Aluno a) {
		ValidaNome.validar(a.getNome());
		if (a.getEmail() == null || !ValidaEmail.isValido(a.getEmail()))
			throw new IllegalArgumentException("Email do aluno inválido.");
		if (a.getCpf() == null || !ValidaCPF.isValido(a.getCpf()))
			throw new IllegalArgumentException("CPF do aluno inválido.");
		if (a.getMatricula() == null || a.getMatricula().isBlank())
			throw new IllegalArgumentException("Matrícula do aluno é obrigatória.");
		if (!ValidaTelefone.isValido(a.getTelefone()))
			throw new IllegalArgumentException("Telefone do aluno inválido.");
		if (a.getSituacao() == null)
			throw new IllegalArgumentException("Situação do aluno é obrigatória.");
		if (a.getSexo() == null)
			throw new IllegalArgumentException("Sexo do aluno é obrigatório.");
		if (a.getDataNascimento() == null || a.getDataNascimento().isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data de nascimento do aluno inválida.");
		if (a.getDataCadastro() == null || a.getDataCadastro().isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data de cadastro do aluno inválida.");
		if (a.getIdPais() <= 0)
			throw new IllegalArgumentException("ID de pais/responsáveis é obrigatório.");
		if (a.getIdTurma() <= 0)
			throw new IllegalArgumentException("ID da turma é obrigatório.");
		validarEndereco(a.getEndereco());
	}

	private void validarEndereco(Endereco e) {
		if (e == null)
			throw new IllegalArgumentException("Endereço do aluno é obrigatório.");
		ValidaCidade.validar(e.getCidade());
		if (!ValidaCEP.isValido(e.getCep()))
			throw new IllegalArgumentException("CEP do aluno inválido.");
		if (e.getEstado() == null)
			throw new IllegalArgumentException("Estado do endereço é obrigatório.");
	}

	private void validarImutaveis(Aluno atual, Aluno banco) {
		if (!banco.getCpf().equals(atual.getCpf()))
			throw new IllegalArgumentException("CPF do aluno não pode ser alterado após o cadastro.");
		if (!banco.getMatricula().equals(atual.getMatricula()))
			throw new IllegalArgumentException("Matrícula do aluno não pode ser alterada após o cadastro.");
	}

	private void validarRelacionamentos(Connection conn, Aluno a) throws SQLException {
		if (new PaisAlunoDAO(conn).buscarPorId(a.getIdPais()) == null)
			throw new IllegalArgumentException("Pais/responsáveis informados não existem.");
		if (new TurmaDAO(conn).buscarPorId(a.getIdTurma()) == null)
			throw new IllegalArgumentException("Turma informada não existe.");
	}

	private Aluno mesclar(Aluno b, Aluno a) {
		b.setNome(a.getNome());
		b.setEmail(a.getEmail());
		b.setSituacao(a.getSituacao());
		b.setSexo(a.getSexo());
		b.setTelefone(a.getTelefone());
		b.setRg(a.getRg());
		b.setObsSaude(a.getObsSaude());
		b.setDataNascimento(a.getDataNascimento());
		b.setIdPais(a.getIdPais());
		b.setIdTurma(a.getIdTurma());
		b.setEndereco(a.getEndereco());
		return b;
	}

	private void informarSeInativo(Aluno a) {
		if (a != null && !a.isAtivo())
			System.out.println("ATENÇÃO: aluno encontrado, porém está inativo.");
	}

	// Salva um novo aluno após validar dados, vínculos, CPF e matrícula únicos.
	public void salvarAluno(Aluno aluno) {
		validarNaoNulo(aluno);
		normalizar(aluno);
		validarCamposBase(aluno);
		executarEmTransacao(conn -> {
			AlunoDAO dao = new AlunoDAO(conn);
			if (dao.existeCpf(aluno.getCpf()))
				throw new IllegalArgumentException("Já existe aluno cadastrado com este CPF.");
			if (dao.buscarPorMatricula(aluno.getMatricula()) != null)
				throw new IllegalArgumentException("Já existe aluno cadastrado com esta matrícula.");
			validarRelacionamentos(conn, aluno);
			dao.inserir(aluno);
			return null;
		}, "Erro ao salvar aluno.");
	}

	// Atualiza um aluno ativo, mantendo CPF e matrícula imutáveis.
	public void atualizarAluno(Aluno alunoAtualizado) {
		validarNaoNulo(alunoAtualizado);
		if (alunoAtualizado.getIdAluno() <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		normalizar(alunoAtualizado);
		validarCamposBase(alunoAtualizado);
		executarEmTransacao(conn -> {
			AlunoDAO dao = new AlunoDAO(conn);
			Aluno banco = dao.buscarPorId(alunoAtualizado.getIdAluno());
			if (banco == null)
				throw new IllegalArgumentException("Aluno não encontrado.");
			if (!banco.isAtivo())
				throw new IllegalArgumentException("Não é possível atualizar aluno inativo.");
			validarImutaveis(alunoAtualizado, banco);
			validarRelacionamentos(conn, alunoAtualizado);
			dao.atualizar(mesclar(banco, alunoAtualizado));
			return null;
		}, "Erro ao atualizar aluno.");
	}

	// Realiza exclusão lógica do aluno, inativando o cadastro no banco.
	public boolean excluirAluno(int idAluno) {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		return executarEmTransacao(conn -> {
			AlunoDAO dao = new AlunoDAO(conn);
			if (dao.buscarPorId(idAluno) == null)
				throw new IllegalArgumentException("Aluno não encontrado.");
			return dao.inativar(idAluno);
		}, "Erro ao excluir aluno.");
	}

	// Reativa um aluno previamente inativado.
	public boolean reativarAluno(int idAluno) {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		return executarEmTransacao(conn -> new AlunoDAO(conn).reativar(idAluno), "Erro ao reativar aluno.");
	}

	// Busca aluno por ID, retornando também inativos e avisando quando o cadastro
	// estiver inativo.
	public Aluno buscarAlunoPorId(int idAluno) {
		if (idAluno <= 0)
			throw new IllegalArgumentException("ID do aluno inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Aluno a = new AlunoDAO(conn).buscarPorId(idAluno);
			if (a == null)
				throw new IllegalArgumentException("Aluno não encontrado.");
			informarSeInativo(a);
			return a;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar aluno por ID.", e);
		}
	}

	// Busca aluno por CPF, retornando também inativos e avisando quando o cadastro
	// estiver inativo.
	public Aluno buscarAlunoPorCpf(String cpf) {
		String c = normalizarCpf(cpf);
		if (c == null || !ValidaCPF.isValido(c))
			throw new IllegalArgumentException("CPF do aluno inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Aluno a = new AlunoDAO(conn).buscarPorCPF(c);
			if (a == null)
				throw new IllegalArgumentException("Aluno não encontrado.");
			informarSeInativo(a);
			return a;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar aluno por CPF.", e);
		}
	}

	// Busca aluno por matrícula, retornando também inativos e avisando quando o
	// cadastro estiver inativo.
	public Aluno buscarAlunoPorMatricula(String matricula) {
		String m = tratarTexto(matricula);
		if (m == null || m.isBlank())
			throw new IllegalArgumentException("Matrícula é obrigatória.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			Aluno a = new AlunoDAO(conn).buscarPorMatricula(m);
			if (a == null)
				throw new IllegalArgumentException("Aluno não encontrado.");
			informarSeInativo(a);
			return a;
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar aluno por matrícula.", e);
		}
	}

	// Lista todos os alunos, ativos e inativos.
	public List<Aluno> listarAlunos() {
		return listarTodosAlunos();
	}

	// Lista todos os alunos, ativos e inativos.
	public List<Aluno> listarTodosAlunos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new AlunoDAO(conn).listarTodos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar alunos.", e);
		}
	}

	// Lista somente alunos ativos.
	public List<Aluno> listarAlunosAtivos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new AlunoDAO(conn).listarAtivos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar alunos ativos.", e);
		}
	}

	// Lista somente alunos inativos.
	public List<Aluno> listarAlunosInativos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new AlunoDAO(conn).listarInativos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar alunos inativos.", e);
		}
	}

	// Lista alunos vinculados a uma turma.
	public List<Aluno> listarAlunosPorTurma(int idTurma) {
		if (idTurma <= 0)
			throw new IllegalArgumentException("ID da turma inválido.");
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new AlunoDAO(conn).listarPorTurma(idTurma);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar alunos por turma.", e);
		}
	}
}
