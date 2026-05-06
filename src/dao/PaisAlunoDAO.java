/*Igor
Guilherme editou adicionando a exclusão lógica (inativar ao invés de excluir)*/

//Define o pacote desta classe
package dao;

//Importa as classes de outros pacotes
import model.PaisAluno;
import util.ValidaCPF;

//Importa as bibliotecas a serem utilizadas
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaisAlunoDAO{

    private final Connection conn;

    public PaisAlunoDAO(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Erro ao conectar ao banco de dados.");
        }
        this.conn = conn;
    }

//Verifica a existência do CPF da mãe
    public boolean existeCpfMae(String cpfMae) throws SQLException {
    	String cpfMaeTratado = cpfMae.trim().replaceAll("\\D", "");
    	if(!ValidaCPF.isValido(cpfMaeTratado)) {
    		throw new IllegalArgumentException("CPF da mãe é inválido.");
    	}
        final String sql = "SELECT 1 FROM pais_aluno WHERE cpf_mae = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfMaeTratado);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

//Verifica a existência do CPF do pai
    public boolean existeCpfPai(String cpfPai) throws SQLException {
    	String cpfPaiTratado = cpfPai.trim().replaceAll("\\D", "");
    	if(!ValidaCPF.isValido(cpfPaiTratado)) {
    		throw new IllegalArgumentException("CPF do pai é inválido.");
    	}
    	final String sql = "SELECT 1 FROM pais_aluno WHERE cpf_pai = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfPaiTratado);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

//Insere os dados dos pais/responsáveis no banco
    public void inserir(PaisAluno paisAluno) throws SQLException {
        validarPaisAlunoNaoNulo(paisAluno);

        final String sql = """
            INSERT INTO pais_aluno (
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai,
                ativo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherPaisAluno(stmt, paisAluno);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir pais/responsáveis. Nenhum regisro afetado.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    paisAluno.setIdPais(rs.getInt(1));
                } else {
                    throw new SQLException("Falha ao inserir pais/responsáveis. ID não retornado.");
                }
            }
        }
    }

//Atualiza os dados dos pais/responsáveis no banco
    public void atualizar(PaisAluno paisAluno) throws SQLException {
        validarPaisAlunoNaoNulo(paisAluno);

        if (paisAluno.getIdPais() <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        final String sql = """
            UPDATE pais_aluno
               SET nome_mae = ?,
                   nome_pai = ?,
                   email_mae = ?,
                   email_pai = ?,
                   telefone_mae = ?,
                   telefone_pai = ?
             WHERE id_pais = ?
               AND ativo = true
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paisAluno.getNomeMae());
            stmt.setString(2, paisAluno.getNomePai());
            stmt.setString(3, paisAluno.getEmailMae());
            stmt.setString(4, paisAluno.getEmailPai());
            stmt.setString(5, paisAluno.getTelefoneMae());
            stmt.setString(6, paisAluno.getTelefonePai());
            stmt.setInt(7, paisAluno.getIdPais());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaAtualizacao(paisAluno.getIdPais());
            }
        }
    }

//Busca o cadastro pelo ID
    public PaisAluno buscarPorId(int idPais) throws SQLException {
        if (idPais <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            WHERE id_pais = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPaisAluno(rs);
                }
                return null;
            }
        }
    }
	
//Busca pelo CPF da mãe
	public PaisAluno buscarPorCpfMae(String cpfMae) throws SQLException {
        final String sql = """
            SELECT *
            FROM pais_aluno
            WHERE cpf_mae = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfMae);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPaisAluno(rs);
                }
                return null;
            }
        }
    }

//Busca pelo CPF do pai
    public PaisAluno buscarPorCpfPai(String cpfPai) throws SQLException {
        final String sql = """
            SELECT *
            FROM pais_aluno
            WHERE cpf_pai = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpfPai);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPaisAluno(rs);
                }
                return null;
            }
        }
    }

//Gera a listagem para listar todos os cadastros - ativos e inativos
    public List<PaisAluno> listarTodos() throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            ORDER BY id_pais
            """;

        List<PaisAluno> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPaisAluno(rs));
            }
        }

        return lista;
    }
    
//Gera a listagem para listar apenas os cadastros ativos
    public List<PaisAluno> listarAtivos() throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            WHERE ativo = true
            ORDER BY id_pais
            """;

        List<PaisAluno> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPaisAluno(rs));
            }
        }

        return lista;
    }
    
//Gera a listagem para listar apenas os cadastros inativos
    public List<PaisAluno> listarInativos() throws SQLException {
        final String sql = """
            SELECT
                id_pais,
                nome_mae,
                nome_pai,
                email_mae,
                email_pai,
                telefone_mae,
                telefone_pai,
                cpf_mae,
                cpf_pai
            FROM pais_aluno
            WHERE ativo = false
            ORDER BY id_pais
            """;

        List<PaisAluno> lista = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPaisAluno(rs));
            }
        }
        return lista;
    }

//Exclusão lógica - Inativar o cadastro
    public boolean inativar(int idPais) throws SQLException {
        if (idPais <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        final String sql = """
            UPDATE pais_aluno
               SET ativo = false
             WHERE id_pais = ?
               AND ativo = true
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaInativacao(idPais);
            }

            return true;
        }
    }

  //Exclusão lógica - Reativar o cadastro
    public boolean reativar(int idPais) throws SQLException {
        if (idPais <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        final String sql = """
            UPDATE pais_aluno
               SET ativo = true
             WHERE id_pais = ?
               AND ativo = false
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                verificarFalhaReativacao(idPais);
            }

            return true;
        }
    }

//Preenche pais/responsáveis
    private void preencherPaisAluno(PreparedStatement stmt, PaisAluno paisAluno) throws SQLException {
        stmt.setString(1, paisAluno.getNomeMae());
        stmt.setString(2, paisAluno.getNomePai());
        stmt.setString(3, paisAluno.getEmailMae());
        stmt.setString(4, paisAluno.getEmailPai());
        stmt.setString(5, paisAluno.getTelefoneMae());
        stmt.setString(6, paisAluno.getTelefonePai());
        stmt.setString(7, paisAluno.getCpfMae());
        stmt.setString(8, paisAluno.getCpfPai());
    }

 //Mapea pais/responsáveis
    private PaisAluno mapearPaisAluno(ResultSet rs) throws SQLException {
        PaisAluno paisAluno = new PaisAluno();

        paisAluno.setIdPais(rs.getInt("id_pais"));
        paisAluno.setNomeMae(rs.getString("nome_mae"));
        paisAluno.setNomePai(rs.getString("nome_pai"));
        paisAluno.setEmailMae(rs.getString("email_mae"));
        paisAluno.setEmailPai(rs.getString("email_pai"));
        paisAluno.setTelefoneMae(rs.getString("telefone_mae"));
        paisAluno.setTelefonePai(rs.getString("telefone_pai"));
        paisAluno.setCpfMae(rs.getString("cpf_mae"));
        paisAluno.setCpfPai(rs.getString("cpf_pai"));

        return paisAluno;
    }

//Verifica o motivo da falha na atualização - ID inválido ou cadastro inativo
    private void verificarFalhaAtualizacao(int idPais) throws SQLException {
        if(idPais <= 0) {
        	throw new IllegalArgumentException("ID inválido.");
        }
    	final String sql = "SELECT ativo FROM pais_aluno WHERE id_pais = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Pais/responsáveis não encontrados.");
                }

                if (!rs.getBoolean("ativo")) {
                    throw new SQLException("Pais/responsáveis estão inativos e não podem ser atualizados.");
                }
            }
        }
    }

 //Verifica o motivo da falha na inativação - ID inválido ou cadastro inativo
    private void verificarFalhaInativacao(int idPais) throws SQLException {
    	if(idPais <= 0) {
    		throw new IllegalArgumentException("ID inválido.");
    	}
        final String sql = "SELECT ativo FROM pais_aluno WHERE id_pais = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Pais/responsáveis não encontrados.");
                }

                if (!rs.getBoolean("ativo")) {
                    throw new SQLException("Pais/responsáveis já estão inativos.");
                }
            }
        }
    }
    
  //Verifica o motivo da falha na reativação - ID inválido ou cadastro ativo
    private void verificarFalhaReativacao(int idPais) throws SQLException {
    	if(idPais <= 0) {
    		throw new IllegalArgumentException("ID inválido.");
    	}
        final String sql = "SELECT ativo FROM pais_aluno WHERE id_pais = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPais);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Pais/responsáveis não encontrados.");
                }

                if (rs.getBoolean("ativo")) {
                    throw new SQLException("Pais/responsáveis já estão ativos.");
                }
            }
        }
    }

//Valida nulo
    private void validarPaisAlunoNaoNulo(PaisAluno paisAluno) {
        if (paisAluno == null) {
            throw new IllegalArgumentException("Pais/Responsáveis não podem ser nulos.");
        }
    }
}