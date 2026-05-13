//igor
//Classe Controller que orquestra as operações de cadastro e vinculação de Professor e Disciplina

package controller;

import dao.ProfessorDisciplinaDAO;
import dao.ProfessorDAO;
import dao.DisciplinaDAO;
import database.ConnectionFactory;
import model.ProfessorDisciplina;
import model.Professor;
import model.Disciplina;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProfessorDisciplinaController {

	//Interface funcional que define um contrato para operações transacionais
	@FunctionalInterface
	private interface AcaoTransacional<T> {
		//Método abstrato que define a ação a ser executada dentro da transação
		T executar(Connection conn) throws SQLException;
	}

	//Método genérico que executa operações dentro de uma transação no banco de dados
	private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			//Desativa o autocommit para gerenciar a transação manualmente
			conn.setAutoCommit(false);
			try {
				//Executa a ação passada como parâmetro
				T resultado = acao.executar(conn);
				//Confirma a transação se tudo correu bem
				conn.commit();
				//Retorna o resultado da ação
				return resultado;
			} catch (IllegalArgumentException e) {
				//Captura exceções de validação e faz rollback da transação
				try {
					conn.rollback();
				} catch (SQLException rollbackEx) {
					//Adiciona a exceção de rollback como suprimida
					e.addSuppressed(rollbackEx);
				}
				//Relança a exceção de validação
				throw e;
			} catch (SQLException | RuntimeException e) {
				//Captura exceções de SQL e runtime e faz rollback
				try {
					conn.rollback();
				} catch (SQLException rollbackEx) {
					//Adiciona a exceção de rollback como suprimida
					e.addSuppressed(rollbackEx);
				}
				//Lança uma nova exceção com mensagem contextualizada
				throw new RuntimeException(mensagemOperacao, e);
			}
		} catch (IllegalArgumentException e) {
			//Relança exceções de validação
			throw e;
		} catch (Exception e) {
			//Captura exceção de conexão e lança runtime exception
			throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
		}
	}

	//Método que valida se o objeto ProfessorDisciplina não é nulo
	private void validarNaoNula(ProfessorDisciplina profDisciplina) {
		//Lança exceção se o objeto for nulo
		if (profDisciplina == null)
			throw new IllegalArgumentException("Relação Professor-Disciplina não pode ser nula.");
	}

	//Método que valida os campos obrigatórios da relação Professor-Disciplina
	private void validarCamposBase(ProfessorDisciplina profDisciplina, Professor professor, Disciplina disciplina) {
		//Valida se o ID do professor é válido
		if (profDisciplina.getIdProfessor() <= 0)
			throw new IllegalArgumentException("ID do professor é inválido.");
		//Valida se o ID da disciplina é válido
		if (profDisciplina.getIdDisciplina() <= 0)
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		//Valida se a data de vinculação é válida
		if (profDisciplina.getDataVinculacao() == null)
			throw new IllegalArgumentException("Data de vinculação é obrigatória.");
		//Valida se o professor existe e está ativo
		if (professor == null || !professor.isAtivo())
			throw new IllegalArgumentException("Professor não encontrado ou inativo.");
		//Valida se a disciplina existe e está ativa
		if (disciplina == null || !disciplina.isAtivo())
			throw new IllegalArgumentException("Disciplina não encontrada ou inativa.");
	}

	//Método que valida se a data de vinculação não é futura
	private void validarDataVinculacao(LocalDate dataVinculacao) {
		//Valida se a data é posterior à data atual
		if (dataVinculacao.isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Data de vinculação não pode ser futura.");
	}

	//Método que valida se não existe uma vinculação duplicada entre professor e disciplina
	private void validarVinculacaoDuplicada(int idProfessor, int idDisciplina, Connection conn) throws SQLException {
		//Cria um DAO com a conexão fornecida
		ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
		//Verifica se já existe uma vinculação ativa
		if (dao.existeVinculacao(idProfessor, idDisciplina))
			throw new IllegalArgumentException("Este professor já está vinculado a esta disciplina.");
	}

	//Método público que realiza o cadastro de uma nova disciplina
	public void cadastrarDisciplina(Disciplina disciplina) {
		//Valida se o objeto não é nulo
		if (disciplina == null)
			throw new IllegalArgumentException("Disciplina não pode ser nula.");
		//Executa a operação em transação
		executarEmTransacao(conn -> {
			//Cria um controller de disciplina para realizar o cadastro
			DisciplinaController disciplinaCtrl = new DisciplinaController();
			//Chama o método de salvar disciplina do controller usando a mesma conexão/transação
			disciplinaCtrl.salvarDisciplina(conn, disciplina);
			//Retorna nulo pois a operação não retorna valor
			return null;
		}, "Erro ao cadastrar disciplina.");
	}

	//Método público que realiza a vinculação de um professor a uma disciplina
	public void vincularProfessorDisciplina(ProfessorDisciplina profDisciplina) {
		//Valida se o objeto não é nulo
		validarNaoNula(profDisciplina);
		//Executa a operação em transação
		executarEmTransacao(conn -> {
			//Cria um DAO de professor para validar existência
			ProfessorDAO profDAO = new ProfessorDAO(conn);
			//Busca o professor pelo ID
			Professor professor = profDAO.buscarPorId(profDisciplina.getIdProfessor());
			//Cria um DAO de disciplina para validar existência
			DisciplinaDAO discDAO = new DisciplinaDAO(conn);
			//Busca a disciplina pelo ID
			Disciplina disciplina = discDAO.buscarPorId(profDisciplina.getIdDisciplina());
			//Valida se professor e disciplina existem e estão ativos
			validarCamposBase(profDisciplina, professor, disciplina);
			//Valida se a data de vinculação é válida
			validarDataVinculacao(profDisciplina.getDataVinculacao());
			//Valida se não existe vinculação duplicada
			validarVinculacaoDuplicada(profDisciplina.getIdProfessor(), profDisciplina.getIdDisciplina(), conn);
			//Cria um DAO de professor-disciplina para realizar a inserção
			ProfessorDisciplinaDAO profDiscDAO = new ProfessorDisciplinaDAO(conn);
			//Insere a vinculação no banco de dados
			profDiscDAO.inserir(profDisciplina);
			//Retorna nulo pois a operação não retorna valor
			return null;
		}, "Erro ao vincular professor à disciplina.");
	}

	//Método público que busca todas as disciplinas de um professor
	public List<ProfessorDisciplina> obterDisciplinasProfessor(int idProfessor) {
		//Valida se o ID do professor é válido
		if (idProfessor <= 0)
			throw new IllegalArgumentException("ID do professor inválido.");
		//Executa a operação em transação
		return executarEmTransacao(conn -> {
			//Cria um DAO de professor-disciplina
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			//Busca e retorna todas as disciplinas associadas ao professor
			return dao.buscarPorProfessor(idProfessor);
		}, "Erro ao obter disciplinas do professor.");
	}

	//Método público que busca todos os professores de uma disciplina
	public List<ProfessorDisciplina> obterProfessoresDisciplina(int idDisciplina) {
		//Valida se o ID da disciplina é válido
		if (idDisciplina <= 0)
			throw new IllegalArgumentException("ID da disciplina inválido.");
		//Executa a operação em transação
		return executarEmTransacao(conn -> {
			//Cria um DAO de professor-disciplina
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			//Busca e retorna todos os professores associados à disciplina
			return dao.buscarPorDisciplina(idDisciplina);
		}, "Erro ao obter professores da disciplina.");
	}

	//Método público que desvincila um professor de uma disciplina (exclusão lógica)
	public boolean desvinularProfessorDisciplina(int idProfessorDisciplina) {
		//Valida se o ID é válido
		if (idProfessorDisciplina <= 0)
			throw new IllegalArgumentException("ID da relação Professor-Disciplina inválido.");
		//Executa a operação em transação
		return executarEmTransacao(conn -> {
			//Cria um DAO de professor-disciplina
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			//Busca a vinculação pelo ID para validar se existe
			ProfessorDisciplina profDisc = dao.buscarPorId(idProfessorDisciplina);
			//Valida se a vinculação foi encontrada
			if (profDisc == null)
				throw new IllegalArgumentException("Vinculação Professor-Disciplina não encontrada.");
			//Realiza a inativação da vinculação
			return dao.inativar(idProfessorDisciplina);
		}, "Erro ao desvincular professor da disciplina.");
	}

	//Método público que busca uma vinculação específica pelo ID
	public ProfessorDisciplina obterVinculacao(int idProfessorDisciplina) {
		//Valida se o ID é válido
		if (idProfessorDisciplina <= 0)
			throw new IllegalArgumentException("ID da relação Professor-Disciplina inválido.");
		//Executa a operação em transação
		return executarEmTransacao(conn -> {
			//Cria um DAO de professor-disciplina
			ProfessorDisciplinaDAO dao = new ProfessorDisciplinaDAO(conn);
			//Busca e retorna a vinculação pelo ID
			return dao.buscarPorId(idProfessorDisciplina);
		}, "Erro ao obter vinculação Professor-Disciplina.");
	}
}
