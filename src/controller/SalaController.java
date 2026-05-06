//Guilherme

package controller;

import dao.SalaDAO;
import database.ConnectionFactory;
import model.Sala;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SalaController {
    @FunctionalInterface private interface AcaoTransacional<T> { T executar(Connection conn) throws SQLException; }

    private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try { T r = acao.executar(conn); conn.commit(); return r; }
            catch (IllegalArgumentException e) { try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); } throw e; }
            catch (SQLException | RuntimeException e) { try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); } throw new RuntimeException(mensagemOperacao, e); }
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e); }
    }

    private void validarSalaNaoNula(Sala sala) { if (sala == null) throw new IllegalArgumentException("Sala não pode ser nula."); }
    private void validarCamposBase(Sala sala) { if (sala.getCapacidade() <= 0) throw new IllegalArgumentException("Capacidade da sala deve ser maior que zero."); }
    private Sala mesclarDadosPermitidos(Sala banco, Sala atualizada) { banco.setCapacidade(atualizada.getCapacidade()); return banco; }
    private void informarSeInativo(Sala sala) { if (sala != null && !sala.isAtivo()) System.out.println("ATENÇÃO: sala encontrada, porém está inativa."); }

    // Salva uma nova sala após validar a capacidade.
    public void salvarSala(Sala sala) {
        validarSalaNaoNula(sala); validarCamposBase(sala);
        executarEmTransacao(conn -> { new SalaDAO(conn).inserir(sala); return null; }, "Erro ao salvar sala.");
    }

    // Atualiza uma sala ativa, mantendo o ID original.
    public void atualizarSala(Sala salaAtualizada) {
        validarSalaNaoNula(salaAtualizada);
        if (salaAtualizada.getIdSala() <= 0) throw new IllegalArgumentException("ID da sala inválido.");
        validarCamposBase(salaAtualizada);
        executarEmTransacao(conn -> {
            SalaDAO dao = new SalaDAO(conn);
            Sala banco = dao.buscarPorId(salaAtualizada.getIdSala());
            if (banco == null) throw new IllegalArgumentException("Sala não encontrada.");
            if (!banco.isAtivo()) throw new IllegalArgumentException("Não é possível atualizar sala inativa.");
            dao.atualizar(mesclarDadosPermitidos(banco, salaAtualizada));
            return null;
        }, "Erro ao atualizar sala.");
    }

    // Realiza exclusão lógica da sala, inativando o cadastro no banco.
    public boolean excluirSala(int idSala) {
        if (idSala <= 0) throw new IllegalArgumentException("ID da sala inválido.");
        return executarEmTransacao(conn -> {
            SalaDAO dao = new SalaDAO(conn);
            if (dao.buscarPorId(idSala) == null) throw new IllegalArgumentException("Sala não encontrada.");
            return dao.inativar(idSala);
        }, "Erro ao excluir sala.");
    }

    // Reativa uma sala previamente inativada.
    public boolean reativarSala(int idSala) {
        if (idSala <= 0) throw new IllegalArgumentException("ID da sala inválido.");
        return executarEmTransacao(conn -> new SalaDAO(conn).reativar(idSala), "Erro ao reativar sala.");
    }

    // Busca sala por ID, retornando também inativas e avisando quando o cadastro estiver inativo.
    public Sala buscarSalaPorId(int idSala) {
        if (idSala <= 0) throw new IllegalArgumentException("ID da sala inválido.");
        try (Connection conn = ConnectionFactory.getConnection()) {
            Sala sala = new SalaDAO(conn).buscarPorId(idSala);
            if (sala == null) throw new IllegalArgumentException("Sala não encontrada.");
            informarSeInativo(sala);
            return sala;
        } catch (SQLException e) { throw new RuntimeException("Erro ao buscar sala por ID.", e); }
    }

    // Lista todas as salas, ativas e inativas.
    public List<Sala> listarSalas() { return listarTodasSalas(); }

    // Lista todas as salas, ativas e inativas.
    public List<Sala> listarTodasSalas() {
        try (Connection conn = ConnectionFactory.getConnection()) { return new SalaDAO(conn).listarTodas(); }
        catch (SQLException e) { throw new RuntimeException("Erro ao listar salas.", e); }
    }

    // Lista somente salas ativas.
    public List<Sala> listarSalasAtivas() {
        try (Connection conn = ConnectionFactory.getConnection()) { return new SalaDAO(conn).listarAtivas(); }
        catch (SQLException e) { throw new RuntimeException("Erro ao listar salas ativas.", e); }
    }

    // Lista somente salas inativas.
    public List<Sala> listarSalasInativas() {
        try (Connection conn = ConnectionFactory.getConnection()) { return new SalaDAO(conn).listarInativas(); }
        catch (SQLException e) { throw new RuntimeException("Erro ao listar salas inativas.", e); }
    }
}
