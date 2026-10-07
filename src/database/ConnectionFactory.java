package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	private static final String URL = "jdbc:sqlite:./database/banco.db";

	public static Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(URL);
		criarTabelaChamado(conn);
		criarTabelaAviso(conn);
		criarTabelaLeituraAviso(conn);
		criarTabelaConfiguracao(conn);
		return conn;
	}

	private static void criarTabelaConfiguracao(Connection conn) throws SQLException {
		try (var stmt = conn.createStatement()) {
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS configuracao_sistema (chave TEXT PRIMARY KEY, valor TEXT NOT NULL)");
		}
	}

	private static void criarTabelaLeituraAviso(Connection conn) throws SQLException {
		final String sql = """
				CREATE TABLE IF NOT EXISTS aviso_leitura (
				    aviso_id INTEGER NOT NULL,
				    usuario_id INTEGER NOT NULL,
				    data_leitura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				    PRIMARY KEY (aviso_id, usuario_id),
				    FOREIGN KEY (aviso_id) REFERENCES aviso(id_aviso),
				    FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario)
				)
				""";
		try (var stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}

	private static void criarTabelaChamado(Connection conn) throws SQLException {
		final String sql = """
				CREATE TABLE IF NOT EXISTS chamado (
				    id_chamado INTEGER PRIMARY KEY AUTOINCREMENT,
				    aluno_id INTEGER NOT NULL,
				    categoria TEXT NOT NULL,
				    assunto TEXT NOT NULL,
				    descricao TEXT NOT NULL,
				    prioridade TEXT NOT NULL DEFAULT 'Baixa',
				    status TEXT NOT NULL DEFAULT 'Aberto',
				    resposta TEXT,
				    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				    data_atualizacao TIMESTAMP,
				    FOREIGN KEY (aluno_id) REFERENCES aluno(id_aluno)
				)
				""";
		try (var stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}

	private static void criarTabelaAviso(Connection conn) throws SQLException {
		final String sql = """
				CREATE TABLE IF NOT EXISTS aviso (
				    id_aviso INTEGER PRIMARY KEY AUTOINCREMENT,
				    titulo TEXT NOT NULL,
				    descricao TEXT NOT NULL,
				    categoria TEXT NOT NULL,
				    prioridade TEXT NOT NULL DEFAULT 'Baixa',
				    publico TEXT NOT NULL DEFAULT 'TODOS',
				    turma_id INTEGER,
				    lido BOOLEAN NOT NULL DEFAULT false,
				    data_aviso DATE NOT NULL DEFAULT CURRENT_DATE,
				    FOREIGN KEY (turma_id) REFERENCES turma(id_turma)
				)
				""";
		try (var stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}
}
