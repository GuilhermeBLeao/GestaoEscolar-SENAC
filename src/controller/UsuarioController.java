package controller;

import dao.UsuarioDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Funcionario;
import model.PaisAluno;
import model.Professor;
import model.Usuario;
import variaveisEnum.TipoUsuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsuarioController {

	private final AlunoController alunoController;
	private final FuncionarioController funcionarioController;
	private final ProfessorController professorController;
	private final PaisAlunoController paisAlunoController;

	public UsuarioController() {
		this.alunoController = new AlunoController();
		this.funcionarioController = new FuncionarioController();
		this.professorController = new ProfessorController();
		this.paisAlunoController = new PaisAlunoController();
	}

	/**
	 * Retorna o Nome real vinculado ao usuário de acordo com o perfil cadastrado
	 * (Aluno, Funcionário, Professor ou Pais/Responsáveis).
	 */
	public String buscarNomeDoUsuario(Usuario usuario) {
		if (usuario == null) {
			return "";
		}

		try {
			// 1. Aluno
			if (usuario.getAlunoId() > 0) {
				Aluno aluno = alunoController.buscarAlunoPorId(usuario.getAlunoId());
				if (aluno != null && aluno.getNome() != null && !aluno.getNome().isBlank()) {
					return aluno.getNome();
				}
			}

			// 2. Funcionário
			if (usuario.getFuncionarioId() > 0) {
				Funcionario func = funcionarioController.buscarFuncionarioPorId(usuario.getFuncionarioId());
				if (func != null && func.getNome() != null && !func.getNome().isBlank()) {
					return func.getNome();
				}
			}

			// 3. Professor
			if (usuario.getProfessorId() > 0) {
				Professor prof = professorController.buscarProfessorPorId(usuario.getProfessorId());
				if (prof != null && prof.getNome() != null && !prof.getNome().isBlank()) {
					return prof.getNome();
				}
			}

			// 4. Pais / Responsáveis
			if (usuario.getPaiId() > 0) {
				PaisAluno pais = paisAlunoController.buscarPaisAlunoPorId(usuario.getPaiId());
				if (pais != null) {
					String cpfUsuario = usuario.getCpf() != null ? usuario.getCpf().replaceAll("\\D", "") : "";
					String cpfMae = pais.getCpfMae() != null ? pais.getCpfMae().replaceAll("\\D", "") : "";
					String cpfPai = pais.getCpfPai() != null ? pais.getCpfPai().replaceAll("\\D", "") : "";

					if (!cpfUsuario.isEmpty() && cpfUsuario.equals(cpfMae) && pais.getNomeMae() != null) {
						return pais.getNomeMae();
					}
					if (!cpfUsuario.isEmpty() && cpfUsuario.equals(cpfPai) && pais.getNomePai() != null) {
						return pais.getNomePai();
					}
					if (pais.getNomeMae() != null && !pais.getNomeMae().isBlank()) {
						return pais.getNomeMae();
					}
					if (pais.getNomePai() != null && !pais.getNomePai().isBlank()) {
						return pais.getNomePai();
					}
				}
			}
		} catch (Exception e) {
			// Caso falhe na busca do perfil específico, retorna o fallback padrão
		}

		return "Usuário " + (usuario.getCpf() != null ? usuario.getCpf() : "");
	}

	/**
	 * Atualiza os dados da conta e o perfil correspondente. Restringe o
	 * preenchimento de RG, Data de Nascimento e Saúde exclusivamente para alunos.
	 */
	public void atualizarUsuarioEPerfil(Usuario usuario, String novoNome, String novoRg, String novaDataNasc,
			String novasObsSaude) {
		if (usuario == null) {
			throw new IllegalArgumentException("Usuário não pode ser nulo.");
		}

		boolean isAluno = (usuario.getTipoUsuario() == TipoUsuario.ALUNO || usuario.getAlunoId() > 0);

		boolean preencheuRg = (novoRg != null && !novoRg.isBlank());
		boolean preencheuDataNasc = (novaDataNasc != null && !novaDataNasc.isBlank());
		boolean preencheuObsSaude = (novasObsSaude != null && !novasObsSaude.isBlank());

		// Validação: RG, Data de Nascimento e Obs de Saúde são permitidos APENAS para
		// Alunos
		if (!isAluno && (preencheuRg || preencheuDataNasc || preencheuObsSaude)) {
			throw new IllegalArgumentException(
					"As informações de RG, Data de Nascimento e Observações de Saúde são exclusivas para o perfil de Alunos.");
		}

		// 1. Atualização para perfil ALUNO
		if (isAluno) {
			if (usuario.getAlunoId() <= 0) {
				throw new IllegalArgumentException("O usuário não possui um cadastro de aluno vinculado.");
			}

			Aluno aluno = alunoController.buscarAlunoPorId(usuario.getAlunoId());
			if (aluno == null) {
				throw new IllegalArgumentException("Cadastro de aluno não encontrado.");
			}

			if (novoNome != null && !novoNome.isBlank()) {
				aluno.setNome(novoNome.trim());
			}

			aluno.setRg(preencheuRg ? novoRg.trim() : null);
			aluno.setObsSaude(preencheuObsSaude ? novasObsSaude.trim() : null);

			if (preencheuDataNasc) {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					aluno.setDataNascimento(LocalDate.parse(novaDataNasc.trim(), formatter));
				} catch (Exception e) {
					throw new IllegalArgumentException("Data de nascimento inválida! Use o formato dd/MM/yyyy.");
				}
			}

			alunoController.atualizarAluno(aluno);

		}
		// 2. Atualização para perfil FUNCIONÁRIO
		else if (usuario.getFuncionarioId() > 0 || usuario.getTipoUsuario() == TipoUsuario.PEDAGOGICO
				|| usuario.getTipoUsuario() == TipoUsuario.SECRETARIA
				|| usuario.getTipoUsuario() == TipoUsuario.DIRECAO) {

			if (usuario.getFuncionarioId() > 0) {
				Funcionario func = funcionarioController.buscarFuncionarioPorId(usuario.getFuncionarioId());
				if (func != null) {
					if (novoNome != null && !novoNome.isBlank()) {
						func.setNome(novoNome.trim());
					}
					funcionarioController.atualizarFuncionario(func);
				}
			}

		}
		// 3. Atualização para perfil PROFESSOR
		else if (usuario.getProfessorId() > 0 || usuario.getTipoUsuario() == TipoUsuario.PROFESSOR) {

			if (usuario.getProfessorId() > 0) {
				Professor prof = professorController.buscarProfessorPorId(usuario.getProfessorId());
				if (prof != null) {
					if (novoNome != null && !novoNome.isBlank()) {
						prof.setNome(novoNome.trim());
					}
					professorController.atualizarProfessor(prof);
				}
			}

		}
		// 4. Atualização para perfil RESPONSÁVEL / PAIS
		else if (usuario.getPaiId() > 0 || usuario.getTipoUsuario() == TipoUsuario.RESPONSAVEL) {

			if (usuario.getPaiId() > 0) {
				PaisAluno pais = paisAlunoController.buscarPaisAlunoPorId(usuario.getPaiId());
				if (pais != null) {
					String cpfUsuario = usuario.getCpf() != null ? usuario.getCpf().replaceAll("\\D", "") : "";
					String cpfMae = pais.getCpfMae() != null ? pais.getCpfMae().replaceAll("\\D", "") : "";
					String cpfPai = pais.getCpfPai() != null ? pais.getCpfPai().replaceAll("\\D", "") : "";

					if (!cpfUsuario.isEmpty() && cpfUsuario.equals(cpfMae)) {
						if (novoNome != null && !novoNome.isBlank()) {
							pais.setNomeMae(novoNome.trim());
						}
					} else if (!cpfUsuario.isEmpty() && cpfUsuario.equals(cpfPai)) {
						if (novoNome != null && !novoNome.isBlank()) {
							pais.setNomePai(novoNome.trim());
						}
					} else if (novoNome != null && !novoNome.isBlank()) {
						if (pais.getNomeMae() != null && !pais.getNomeMae().isBlank()) {
							pais.setNomeMae(novoNome.trim());
						} else {
							pais.setNomePai(novoNome.trim());
						}
					}

					paisAlunoController.atualizarPaisAluno(pais);
				}
			}
		}

		// Atualiza o registro na tabela de usuários
		atualizarUsuario(usuario);
	}

	public void inserirUsuario(Usuario usuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			dao.inserir(usuario);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao inserir usuário: " + e.getMessage(), e);
		}
	}

	public void atualizarUsuario(Usuario usuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			dao.atualizar(usuario);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
		}
	}

	public Usuario autenticarLogin(String cpf, String senha) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.autenticar(cpf, senha);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao autenticar usuário: " + e.getMessage(), e);
		}
	}

	public void atualizarUltimoLogin(int idUsuario, LocalDateTime ultimoLogin) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			dao.atualizarUltimoLogin(idUsuario, ultimoLogin);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao atualizar último login: " + e.getMessage(), e);
		}
	}

	public void atualizarSenhaHash(int idUsuario, String senhaHash) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			dao.atualizarSenhaHash(idUsuario, senhaHash);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao atualizar senha: " + e.getMessage(), e);
		}
	}

	public Usuario buscarUsuarioPorCpf(String cpf) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.buscarPorCpf(cpf);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por CPF: " + e.getMessage(), e);
		}
	}

	public Usuario buscarUsuarioPorId(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.buscarPorId(idUsuario);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por ID: " + e.getMessage(), e);
		}
	}

	public Usuario buscarPorAlunoId(int alunoId) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.buscarPorAlunoId(alunoId);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar usuário por ID de aluno: " + e.getMessage(), e);
		}
	}

	public List<Usuario> listarTodosUsuarios() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.listarTodos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar todos os usuários: " + e.getMessage(), e);
		}
	}

	public List<Usuario> listarAtivos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.listarAtivos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar usuários ativos: " + e.getMessage(), e);
		}
	}

	public List<Usuario> listarInativos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.listarInativos();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao listar usuários inativos: " + e.getMessage(), e);
		}
	}

	public boolean excluirUsuario(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.inativar(idUsuario);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao inativar usuário: " + e.getMessage(), e);
		}
	}

	public boolean reativarUsuario(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.reativar(idUsuario);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao reativar usuário: " + e.getMessage(), e);
		}
	}

	public boolean existeCpf(String cpf) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDAO dao = new UsuarioDAO(conn);
			return dao.existeCpf(cpf);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao verificar existência do CPF: " + e.getMessage(), e);
		}
	}
}