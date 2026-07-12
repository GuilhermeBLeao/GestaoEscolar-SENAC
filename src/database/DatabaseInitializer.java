package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.mindrot.jbcrypt.BCrypt;

public final class DatabaseInitializer {
	private static boolean initialized;
	private static final String SENHA_TESTE = "Senha@123";
	private static final String[] CPFS_USUARIOS_TESTE = { "11144477735", "15998925106", "12345678909", "81339499550",
			"93541134780", "84950340182", "23604734096" };

	private DatabaseInitializer() {
	}

	public static synchronized void initialize(Connection conn) throws SQLException {
		if (initialized) {
			return;
		}

		try (Statement stmt = conn.createStatement()) {
			stmt.execute("PRAGMA foreign_keys = ON");
		}

		criarTabelas(conn);
		semearDadosTeste(conn);
		atualizarSenhasUsuariosTeste(conn);
		initialized = true;
	}

	private static void criarTabelas(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS sala (id_sala INTEGER PRIMARY KEY AUTOINCREMENT, capacidade"
					+ " INTEGER NOT NULL, ativo BOOLEAN NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS turma (id_turma INTEGER PRIMARY KEY AUTOINCREMENT, sala_id"
					+ " INTEGER NOT NULL, descricao_turma TEXT NOT NULL, turno TEXT NOT NULL, ativo"
					+ " BOOLEAN NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS disciplina (id_disciplina INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " descricao TEXT NOT NULL, carga_horaria INTEGER NOT NULL, codigo INTEGER NOT NULL"
					+ " UNIQUE, ativo BOOLEAN NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS pais_aluno (id_pais INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " nome_mae TEXT NOT NULL, nome_pai TEXT NOT NULL, email_mae TEXT, email_pai TEXT,"
					+ " telefone_mae TEXT, telefone_pai TEXT, cpf_mae TEXT UNIQUE, cpf_pai TEXT UNIQUE,"
					+ " ativo BOOLEAN NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS aluno (id_aluno INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT"
					+ " NOT NULL, email TEXT, situacao TEXT NOT NULL, sexo TEXT NOT NULL, telefone TEXT,"
					+ " cpf TEXT NOT NULL UNIQUE, data_nascimento DATE NOT NULL, data_cadastro DATE NOT"
					+ " NULL, matricula TEXT NOT NULL UNIQUE, rg TEXT, obs_saude TEXT, pais_id INTEGER"
					+ " NOT NULL, turma_id INTEGER NOT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS endereco (id_endereco INTEGER PRIMARY KEY AUTOINCREMENT, rua"
					+ " TEXT NOT NULL, numero TEXT NOT NULL, complemento TEXT, bairro TEXT NOT NULL,"
					+ " cidade TEXT NOT NULL, estado TEXT NOT NULL, cep TEXT NOT NULL, aluno_id INTEGER"
					+ " NOT NULL UNIQUE)");
			stmt.execute("CREATE TABLE IF NOT EXISTS funcionario (id_funcionario INTEGER PRIMARY KEY"
					+ " AUTOINCREMENT, nome TEXT NOT NULL, cpf TEXT NOT NULL UNIQUE, cargo TEXT NOT NULL,"
					+ " telefone TEXT, rg TEXT, sexo TEXT NOT NULL, setor TEXT NOT NULL, email TEXT,"
					+ " data_nascimento DATE NOT NULL, data_contratacao DATE NOT NULL, perfil TEXT NOT"
					+ " NULL, ativo BOOLEAN NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS endereco_funcionario (id_endereco INTEGER PRIMARY KEY"
					+ " AUTOINCREMENT, rua TEXT NOT NULL, numero TEXT NOT NULL, complemento TEXT, bairro"
					+ " TEXT NOT NULL, cidade TEXT NOT NULL, estado TEXT NOT NULL, cep TEXT NOT NULL,"
					+ " funcionario_id INTEGER NOT NULL UNIQUE)");
			stmt.execute("CREATE TABLE IF NOT EXISTS professor (id_professor INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " nome TEXT NOT NULL, cpf TEXT NOT NULL UNIQUE, formacao TEXT NOT NULL, telefone"
					+ " TEXT, rg TEXT, data_nascimento DATE NOT NULL, sexo TEXT NOT NULL, ativo BOOLEAN"
					+ " NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS endereco_professor (id_endereco INTEGER PRIMARY KEY"
					+ " AUTOINCREMENT, rua TEXT NOT NULL, numero TEXT NOT NULL, complemento TEXT, bairro"
					+ " TEXT NOT NULL, cidade TEXT NOT NULL, estado TEXT NOT NULL, cep TEXT NOT NULL,"
					+ " professor_id INTEGER NOT NULL UNIQUE)");
			stmt.execute("CREATE TABLE IF NOT EXISTS usuario (id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, cpf"
					+ " TEXT NOT NULL UNIQUE, senha_hash TEXT NOT NULL, ativo BOOLEAN NOT NULL DEFAULT"
					+ " true, data_criacao DATE NOT NULL, ultimo_login TIMESTAMP, tipo_usuario TEXT NOT"
					+ " NULL, aluno_id INTEGER, funcionario_id INTEGER, pai_id INTEGER, professor_id" + " INTEGER)");
			stmt.execute("CREATE TABLE IF NOT EXISTS turma_disciplina (turma_id INTEGER NOT NULL, disciplina_id"
					+ " INTEGER NOT NULL, PRIMARY KEY (turma_id, disciplina_id))");
			stmt.execute("CREATE TABLE IF NOT EXISTS professor_disciplina (id_professor_disciplina INTEGER PRIMARY"
					+ " KEY AUTOINCREMENT, id_professor INTEGER NOT NULL, id_disciplina INTEGER NOT NULL,"
					+ " data_vinculacao DATE NOT NULL, data_desvinculacao DATE, ativo BOOLEAN NOT NULL"
					+ " DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS turma_aluno (id_turmaluno INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " turma_id INTEGER NOT NULL, aluno_id INTEGER NOT NULL, data_entrada DATE NOT NULL,"
					+ " data_saida DATE, ativo BOOLEAN NOT NULL DEFAULT true)");
			stmt.execute("CREATE TABLE IF NOT EXISTS trimestre (id_trimestre INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " numero INTEGER NOT NULL, data_inicio DATE NOT NULL, data_fim DATE NOT NULL,"
					+ " ano_letivo INTEGER NOT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS nota (notas_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " disciplina_id INTEGER NOT NULL, aluno_id INTEGER NOT NULL, atividade TEXT NOT"
					+ " NULL, nota REAL NOT NULL, data_lancamento DATE NOT NULL, trimestre_id INTEGER NOT" + " NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS presenca (id_presenca INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " aluno_id INTEGER NOT NULL, disciplina_id INTEGER NOT NULL, data_presenca DATE NOT"
					+ " NULL, presente BOOLEAN NOT NULL, falta_abonada BOOLEAN NOT NULL DEFAULT false,"
					+ " falta_justificada BOOLEAN NOT NULL DEFAULT false, motivo_abonada TEXT)");
			stmt.execute("CREATE TABLE IF NOT EXISTS advertencia (id_advertencia INTEGER PRIMARY KEY"
					+ " AUTOINCREMENT, aluno_id INTEGER NOT NULL, turma_id INTEGER NOT NULL, professor_id"
					+ " INTEGER NOT NULL, motivo TEXT NOT NULL, descricao TEXT, data_advertencia DATE NOT" + " NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS atividade_dia (id_atividade_dia INTEGER PRIMARY KEY"
					+ " AUTOINCREMENT, professor_id INTEGER NOT NULL, turma_id INTEGER NOT NULL,"
					+ " disciplina_id INTEGER NOT NULL, data DATE NOT NULL, descricao TEXT NOT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS ocorrencia (id_ocorrencia INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " funcionario_id INTEGER NOT NULL, aluno_id INTEGER, tipo_ocorrencia TEXT NOT NULL,"
					+ " descricao TEXT NOT NULL, atendente_nome TEXT NOT NULL, data_ocorrencia DATE NOT" + " NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS controle_matricula (ano INTEGER PRIMARY KEY, ultimo_numero"
					+ " INTEGER NOT NULL)");
		}
	}

	private static void semearDadosTeste(Connection conn) throws SQLException {
		if (existeRegistro(conn, "usuario")) {
			return;
		}

		String hash = BCrypt.hashpw(SENHA_TESTE, BCrypt.gensalt());
		LocalDate hoje = LocalDate.now();

		executar(conn, "INSERT INTO sala (id_sala, capacidade, ativo) VALUES (1, 35, true)");
		executar(conn, "INSERT INTO turma (id_turma, sala_id, descricao_turma, turno, ativo) VALUES (1, 1, '1 Ano"
				+ " A', 'MATUTINO', true)");
		executar(conn, "INSERT INTO disciplina (id_disciplina, descricao, carga_horaria, codigo, ativo) VALUES (1,"
				+ " 'Matematica', 80, 101, true)");
		executar(conn, "INSERT INTO disciplina (id_disciplina, descricao, carga_horaria, codigo, ativo) VALUES (2,"
				+ " 'Portugues', 80, 102, true)");
		executar(conn, "INSERT INTO turma_disciplina (turma_id, disciplina_id) VALUES (1, 1)");
		executar(conn, "INSERT INTO turma_disciplina (turma_id, disciplina_id) VALUES (1, 2)");
		executar(conn, "INSERT INTO trimestre (id_trimestre, numero, data_inicio, data_fim, ano_letivo) VALUES (1,"
				+ " 1, '2026-02-01', '2026-05-31', 2026)");
		executar(conn,
				"INSERT INTO pais_aluno (id_pais, nome_mae, nome_pai, email_mae, email_pai, telefone_mae,"
						+ " telefone_pai, cpf_mae, cpf_pai, ativo) VALUES (1, 'Maria Responsavel', 'Jose"
						+ " Responsavel', 'maria.responsavel@teste.com', 'jose.responsavel@teste.com',"
						+ " '48999990001', '48999990002', '93541134780', '52998224725', true)");
		executar(conn,
				"INSERT INTO pais_aluno (id_pais, nome_mae, nome_pai, email_mae, email_pai, telefone_mae,"
						+ " telefone_pai, cpf_mae, cpf_pai, ativo) VALUES (2, 'Patricia Professor Responsavel',"
						+ " 'Roberto Duplo Perfil', 'patricia.duplo@teste.com', 'roberto.duplo@teste.com',"
						+ " '48999990003', '48999990004', '84950340182', '29942651250', true)");
		executar(conn,
				"INSERT INTO aluno (id_aluno, nome, email, situacao, sexo, telefone, cpf, data_nascimento,"
						+ " data_cadastro, matricula, rg, obs_saude, pais_id, turma_id) VALUES (1, 'Joao"
						+ " Aluno', 'joao.aluno@teste.com', 'ATIVO', 'MASCULINO', '48999990005', '81339499550',"
						+ " '2012-03-15', '2026-02-01', '1234500001', '1234567', 'Sem observacoes', 1, 1)");
		executar(conn, "INSERT INTO endereco (rua, numero, complemento, bairro, cidade, estado, cep, aluno_id)"
				+ " VALUES ('Rua Escola', '100', 'Casa', 'Centro', 'Florianopolis', 'SC', '88000000'," + " 1)");
		executar(conn, "INSERT INTO turma_aluno (turma_id, aluno_id, data_entrada, data_saida, ativo) VALUES (1,"
				+ " 1, '2026-02-01', NULL, true)");
		executar(conn,
				"INSERT INTO professor (id_professor, nome, cpf, formacao, telefone, rg, data_nascimento,"
						+ " sexo, ativo) VALUES (1, 'Carlos Professor', '15998925106', 'Licenciatura em"
						+ " Matematica', '48999990006', '2345678', '1985-08-20', 'MASCULINO', true)");
		executar(conn,
				"INSERT INTO professor (id_professor, nome, cpf, formacao, telefone, rg, data_nascimento,"
						+ " sexo, ativo) VALUES (2, 'Patricia Professor Responsavel', '84950340182',"
						+ " 'Licenciatura em Letras', '48999990007', '3456789', '1988-11-10', 'FEMININO'," + " true)");
		executar(conn,
				"INSERT INTO endereco_professor (rua, numero, complemento, bairro, cidade, estado, cep,"
						+ " professor_id) VALUES ('Rua Professor', '200', 'Apto 1', 'Centro', 'Florianopolis',"
						+ " 'SC', '88000001', 1)");
		executar(conn,
				"INSERT INTO endereco_professor (rua, numero, complemento, bairro, cidade, estado, cep,"
						+ " professor_id) VALUES ('Rua Perfil Duplo', '250', 'Casa', 'Centro', 'Florianopolis',"
						+ " 'SC', '88000002', 2)");
		executar(conn, "INSERT INTO professor_disciplina (id_professor, id_disciplina, data_vinculacao, ativo)"
				+ " VALUES (1, 1, '2026-02-01', true)");
		executar(conn, "INSERT INTO professor_disciplina (id_professor, id_disciplina, data_vinculacao, ativo)"
				+ " VALUES (2, 2, '2026-02-01', true)");

		inserirFuncionario(conn, 1, "Admin Sistema", "11144477735", "Administrador", "Administrativo", "DIRETOR");
		inserirFuncionario(conn, 2, "Ana Secretaria", "12345678909", "Secretaria Escolar", "Secretaria", "SECRETARIA");
		inserirFuncionario(conn, 3, "Daniel Diretor", "23604734096", "Diretor Escolar", "Direcao", "DIRETOR");

		inserirUsuario(conn, "11144477735", hash, "ADMINISTRADOR", 0, 1, 0, 0, hoje);
		inserirUsuario(conn, "15998925106", hash, "PROFESSOR", 0, 0, 0, 1, hoje);
		inserirUsuario(conn, "12345678909", hash, "SECRETARIA", 0, 2, 0, 0, hoje);
		inserirUsuario(conn, "81339499550", hash, "ALUNO", 1, 0, 0, 0, hoje);
		inserirUsuario(conn, "93541134780", hash, "RESPONSAVEL", 0, 0, 1, 0, hoje);
		inserirUsuario(conn, "84950340182", hash, "PROFESSOR", 0, 0, 2, 2, hoje);
		inserirUsuario(conn, "23604734096", hash, "DIRECAO", 0, 3, 0, 0, hoje);

		executar(conn, "INSERT INTO nota (disciplina_id, aluno_id, atividade, nota, data_lancamento, trimestre_id)"
				+ " VALUES (1, 1, 'Avaliacao 1', 8.5, '2026-03-20', 1)");
		executar(conn, "INSERT INTO nota (disciplina_id, aluno_id, atividade, nota, data_lancamento, trimestre_id)"
				+ " VALUES (2, 1, 'Avaliacao 1', 9.0, '2026-03-21', 1)");
		executar(conn, "INSERT INTO presenca (aluno_id, disciplina_id, data_presenca, presente, falta_abonada,"
				+ " falta_justificada, motivo_abonada) VALUES (1, 1, '2026-03-20', true, false, false," + " NULL)");
		executar(conn,
				"INSERT INTO advertencia (aluno_id, turma_id, professor_id, motivo, descricao,"
						+ " data_advertencia) VALUES (1, 1, 1, 'Teste', 'Advertencia de exemplo para validacao"
						+ " das telas.', '2026-04-01')");
		executar(conn, "INSERT INTO atividade_dia (professor_id, turma_id, disciplina_id, data, descricao) VALUES"
				+ " (1, 1, 1, '2026-03-20', 'Revisao de matematica e lista de exercicios.')");
		executar(conn,
				"INSERT INTO ocorrencia (funcionario_id, aluno_id, tipo_ocorrencia, descricao,"
						+ " atendente_nome, data_ocorrencia) VALUES (2, 1, 'OUTRO', 'Atendimento de exemplo"
						+ " para validacao do painel administrativo.', 'Ana Secretaria', '2026-04-02')");
		executar(conn, "INSERT INTO controle_matricula (ano, ultimo_numero) VALUES (2026, 1)");
	}

	private static void atualizarSenhasUsuariosTeste(Connection conn) throws SQLException {
		String hash = BCrypt.hashpw(SENHA_TESTE, BCrypt.gensalt());
		String sql = "UPDATE usuario SET senha_hash = ? WHERE cpf = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (String cpf : CPFS_USUARIOS_TESTE) {
				stmt.setString(1, hash);
				stmt.setString(2, cpf);
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
	}

	private static void inserirFuncionario(Connection conn, int id, String nome, String cpf, String cargo, String setor,
			String perfil) throws SQLException {
		executar(conn,
				"INSERT INTO funcionario (id_funcionario, nome, cpf, cargo, telefone, rg, sexo, setor,"
						+ " email, data_nascimento, data_contratacao, perfil, ativo) VALUES (" + id + ", '" + nome
						+ "', '" + cpf + "', '" + cargo + "', '48999990100', '987654" + id + "', 'MASCULINO', '" + setor
						+ "', '" + cpf + "@teste.com', '1980-01-01', '2026-01-10', '" + perfil + "', true)");
		executar(conn,
				"INSERT INTO endereco_funcionario (rua, numero, complemento, bairro, cidade, estado, cep,"
						+ " funcionario_id) VALUES ('Rua Funcionarios', '" + id
						+ "', 'Sala', 'Centro', 'Florianopolis', 'SC', '88000003', " + id + ")");
	}

	private static void inserirUsuario(Connection conn, String cpf, String hash, String tipo, int alunoId,
			int funcionarioId, int paiId, int professorId, LocalDate dataCriacao) throws SQLException {
		String sql = "INSERT INTO usuario (cpf, senha_hash, ativo, data_criacao, ultimo_login, tipo_usuario,"
				+ " aluno_id, funcionario_id, pai_id, professor_id) VALUES (?, ?, true, ?, NULL, ?, ?," + " ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, cpf);
			stmt.setString(2, hash);
			stmt.setString(3, dataCriacao.toString());
			stmt.setString(4, tipo);
			setNullableInt(stmt, 5, alunoId);
			setNullableInt(stmt, 6, funcionarioId);
			setNullableInt(stmt, 7, paiId);
			setNullableInt(stmt, 8, professorId);
			stmt.executeUpdate();
		}
	}

	private static void setNullableInt(PreparedStatement stmt, int index, int valor) throws SQLException {
		if (valor <= 0) {
			stmt.setNull(index, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(index, valor);
		}
	}

	private static boolean existeRegistro(Connection conn, String tabela) throws SQLException {
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + tabela + " LIMIT 1")) {
			return rs.next();
		}
	}

	private static void executar(Connection conn, String sql) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}
}
