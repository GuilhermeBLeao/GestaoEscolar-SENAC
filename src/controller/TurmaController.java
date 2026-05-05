/*Igor
Guilherme adicionou exclusão lógica*/
package controller;

import dao.SalaDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Turma;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TurmaController {
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

    private void validarTurmaNaoNula(Turma turma) { if (turma == null) throw new IllegalArgumentException("Turma não pode ser nula."); }
    private String tratarTexto(String valor) { return valor == null ? null : valor.trim(); }
    private void normalizarTurma(Turma turma) { turma.setDescricaoTurma(tratarTexto(turma.getDescricaoTurma())); }
    private void validarCamposBase(Turma turma) {
        if (turma.getSalaId() <= 0) throw new IllegalArgumentException("ID da sala é obrigatório.");
        if (turma.getDescricaoTurma() == null || turma.getDescricaoTurma().isBlank()) throw new IllegalArgumentException("Descrição da turma é obrigatória.");
        if (turma.getTurno() == null) throw new IllegalArgumentException("Turno da turma é obrigatório.");
    }
    private Turma mesclarDadosPermitidos(Turma banco, Turma atualizada) { banco.setSalaId(atualizada.getSalaId()); banco.setDescricaoTurma(atualizada.getDescricaoTurma()); banco.setTurno(atualizada.getTurno()); return banco; }
    private void validarSalaExistente(SalaDAO salaDAO, int idSala) throws SQLException { if (salaDAO.buscarPorId(idSala) == null) throw new IllegalArgumentException("Sala informada não existe."); }
    private void informarSeInativo(Turma turma) { if (turma != null && !turma.isAtivo()) System.out.println("ATENÇÃO: turma encontrada, porém está inativa."); }

    // Salva uma nova turma após validar os dados e a existência da sala.
    public void salvarTurma(Turma turma) {
        validarTurmaNaoNula(turma); normalizarTurma(turma); validarCamposBase(turma);
        executarEmTransacao(conn -> { validarSalaExistente(new SalaDAO(conn), turma.getSalaId()); new TurmaDAO(conn).inserir(turma); return null; }, "Erro ao salvar turma.");
    }

    // Atualiza uma turma ativa, validando a sala vinculada.
    public void atualizarTurma(Turma turmaAtualizada) {
        validarTurmaNaoNula(turmaAtualizada);
        if (turmaAtualizada.getIdTurma() <= 0) throw new IllegalArgumentException("ID da turma inválido.");
        normalizarTurma(turmaAtualizada); validarCamposBase(turmaAtualizada);
        executarEmTransacao(conn -> {
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            Turma banco = turmaDAO.buscarPorId(turmaAtualizada.getIdTurma());
            if (banco == null) throw new IllegalArgumentException("Turma não encontrada.");
            if (!banco.isAtivo()) throw new IllegalArgumentException("Não é possível atualizar turma inativa.");
            validarSalaExistente(new SalaDAO(conn), turmaAtualizada.getSalaId());
            turmaDAO.atualizar(mesclarDadosPermitidos(banco, turmaAtualizada));
            return null;
        }, "Erro ao atualizar turma.");
    }

    // Realiza exclusão lógica da turma, inativando o cadastro no banco.
    public boolean excluirTurma(int idTurma) {
        if (idTurma <= 0) throw new IllegalArgumentException("ID da turma inválido.");
        return executarEmTransacao(conn -> { TurmaDAO dao = new TurmaDAO(conn); if (dao.buscarPorId(idTurma) == null) throw new IllegalArgumentException("Turma não encontrada."); return dao.inativar(idTurma); }, "Erro ao excluir turma.");
    }

    // Reativa uma turma previamente inativada.
    public boolean reativarTurma(int idTurma) {
        if (idTurma <= 0) throw new IllegalArgumentException("ID da turma inválido.");
        return executarEmTransacao(conn -> new TurmaDAO(conn).reativar(idTurma), "Erro ao reativar turma.");
    }

    // Busca turma por ID, retornando também inativas e avisando quando o cadastro estiver inativo.
    public Turma buscarTurmaPorId(int idTurma) {
        if (idTurma <= 0) throw new IllegalArgumentException("ID da turma inválido.");
        try (Connection conn = ConnectionFactory.getConnection()) { Turma t = new TurmaDAO(conn).buscarPorId(idTurma); if (t == null) throw new IllegalArgumentException("Turma não encontrada."); informarSeInativo(t); return t; }
        catch (SQLException e) { throw new RuntimeException("Erro ao buscar turma por ID.", e); }
    }

    // Busca turma por descrição, retornando também inativas e avisando quando o cadastro estiver inativo.
    public Turma buscarTurmaPorDescricao(String descricao) {
        String texto = tratarTexto(descricao); if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Descrição da turma é obrigatória.");
        try (Connection conn = ConnectionFactory.getConnection()) { Turma t = new TurmaDAO(conn).buscarPorDescricao(texto); if (t == null) throw new IllegalArgumentException("Turma não encontrada."); informarSeInativo(t); return t; }
        catch (SQLException e) { throw new RuntimeException("Erro ao buscar turma por descrição.", e); }
    }

    // Lista todas as turmas, ativas e inativas.
    public List<Turma> listarTurmas() { return listarTodasTurmas(); }

    // Lista todas as turmas, ativas e inativas.
    public List<Turma> listarTodasTurmas() { try (Connection conn = ConnectionFactory.getConnection()) { return new TurmaDAO(conn).listarTodos(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar turmas.", e); } }

    // Lista somente turmas ativas.
    public List<Turma> listarTurmasAtivas() { try (Connection conn = ConnectionFactory.getConnection()) { return new TurmaDAO(conn).listarAtivas(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar turmas ativas.", e); } }

    // Lista somente turmas inativas.
    public List<Turma> listarTurmasInativas() { try (Connection conn = ConnectionFactory.getConnection()) { return new TurmaDAO(conn).listarInativas(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar turmas inativas.", e); } }
}
