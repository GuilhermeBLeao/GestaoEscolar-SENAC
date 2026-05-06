/*Igor
Guilherme adicionou sexo, e exclusão lógica*/

package controller;

import dao.ProfessorDAO;
import database.ConnectionFactory;
import model.Endereco;
import model.Professor;
import util.ValidaCEP;
import util.ValidaCPF;
import util.ValidaCidade;
import util.ValidaNome;
import util.ValidaTelefone;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProfessorController {
    @FunctionalInterface private interface AcaoTransacional<T> { T executar(Connection conn) throws SQLException; }
    private <T> T executarEmTransacao(AcaoTransacional<T> acao, String mensagemOperacao) { try (Connection conn = ConnectionFactory.getConnection()) { conn.setAutoCommit(false); try { T r = acao.executar(conn); conn.commit(); return r; } catch (IllegalArgumentException e) { try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); } throw e; } catch (SQLException | RuntimeException e) { try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); } throw new RuntimeException(mensagemOperacao, e); } } catch (IllegalArgumentException e) { throw e; } catch (Exception e) { throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e); } }
    private void validarNaoNulo(Professor p) { if (p == null) throw new IllegalArgumentException("Professor não pode ser nulo."); }
    private String tratarTexto(String v) { return v == null ? null : v.trim(); }
    private String normalizarCpf(String v) { String t = tratarTexto(v); return t == null ? null : t.replaceAll("\\D", ""); }
    private String normalizarTelefone(String v) { String t = tratarTexto(v); return t == null ? null : t.replaceAll("\\D", ""); }
    private String normalizarCep(String v) { String t = tratarTexto(v); return t == null ? null : t.replaceAll("\\D", ""); }
    private void normalizar(Professor p) { p.setNome(tratarTexto(p.getNome())); p.setCpf(normalizarCpf(p.getCpf())); p.setFormacao(tratarTexto(p.getFormacao())); p.setTelefone(normalizarTelefone(p.getTelefone())); p.setRg(tratarTexto(p.getRg())); Endereco e = p.getEndereco(); if (e != null) { e.setRua(tratarTexto(e.getRua())); e.setNumero(tratarTexto(e.getNumero())); e.setComplemento(tratarTexto(e.getComplemento())); e.setBairro(tratarTexto(e.getBairro())); e.setCidade(tratarTexto(e.getCidade())); e.setCep(normalizarCep(e.getCep())); } }
    private void validarEndereco(Endereco e) { if (e == null) throw new IllegalArgumentException("Endereço é obrigatório."); ValidaCidade.validar(e.getCidade()); if (e.getEstado() == null) throw new IllegalArgumentException("Estado é obrigatório."); if (!ValidaCEP.isValido(e.getCep())) throw new IllegalArgumentException("CEP inválido."); }
    private void validarCamposBase(Professor p) { ValidaNome.validar(p.getNome()); if (p.getCpf() == null || !ValidaCPF.isValido(p.getCpf())) throw new IllegalArgumentException("CPF do professor inválido."); if (p.getFormacao() == null || p.getFormacao().isBlank()) throw new IllegalArgumentException("Formação do professor é obrigatória."); if (!ValidaTelefone.isValido(p.getTelefone())) throw new IllegalArgumentException("Telefone do professor inválido."); if (p.getDataNascimento() == null || p.getDataNascimento().isAfter(LocalDate.now())) throw new IllegalArgumentException("Data de nascimento do professor inválida."); if (p.getSexo() == null) throw new IllegalArgumentException("Sexo do professor é obrigatório."); validarEndereco(p.getEndereco()); }
    private void validarCpfImutavel(Professor a, Professor b) { if (!b.getCpf().equals(a.getCpf())) throw new IllegalArgumentException("CPF do professor não pode ser alterado após o cadastro."); }
    private Professor mesclar(Professor b, Professor a) { b.setNome(a.getNome()); b.setFormacao(a.getFormacao()); b.setTelefone(a.getTelefone()); b.setRg(a.getRg()); b.setDataNascimento(a.getDataNascimento()); b.setSexo(a.getSexo()); b.setEndereco(a.getEndereco()); return b; }
    private void informarSeInativo(Professor p) { if (p != null && !p.isAtivo()) System.out.println("ATENÇÃO: professor encontrado, porém está inativo."); }
    private Professor buscarEmTodosPorId(ProfessorDAO dao, int idProfessor) throws SQLException { for (Professor p : dao.listarTodos()) if (p.getIdProfessor() == idProfessor) return p; return null; }
    private Professor buscarEmTodosPorCpf(ProfessorDAO dao, String cpf) throws SQLException { for (Professor p : dao.listarTodos()) if (cpf.equals(p.getCpf())) return p; return null; }
    private Professor buscarEmTodosPorNome(ProfessorDAO dao, String nome) throws SQLException { for (Professor p : dao.listarTodos()) if (nome.equalsIgnoreCase(p.getNome())) return p; return null; }

    // Salva um novo professor após validar dados obrigatórios e impedir CPF duplicado.
    public void salvarProfessor(Professor professor) { validarNaoNulo(professor); normalizar(professor); validarCamposBase(professor); executarEmTransacao(conn -> { ProfessorDAO dao = new ProfessorDAO(conn); if (dao.existeCpf(professor.getCpf())) throw new IllegalArgumentException("Já existe professor cadastrado com este CPF."); dao.inserir(professor); return null; }, "Erro ao salvar professor."); }

    // Atualiza um professor ativo, mantendo o CPF imutável.
    public void atualizarProfessor(Professor professorAtualizado) { validarNaoNulo(professorAtualizado); if (professorAtualizado.getIdProfessor() <= 0) throw new IllegalArgumentException("ID do professor inválido."); normalizar(professorAtualizado); validarCamposBase(professorAtualizado); executarEmTransacao(conn -> { ProfessorDAO dao = new ProfessorDAO(conn); Professor banco = buscarEmTodosPorId(dao, professorAtualizado.getIdProfessor()); if (banco == null) throw new IllegalArgumentException("Professor não encontrado."); if (!banco.isAtivo()) throw new IllegalArgumentException("Não é possível atualizar professor inativo."); validarCpfImutavel(professorAtualizado, banco); dao.atualizar(mesclar(banco, professorAtualizado)); return null; }, "Erro ao atualizar professor."); }

    // Realiza exclusão lógica do professor, inativando o cadastro no banco.
    public boolean excluirProfessor(int idProfessor) { if (idProfessor <= 0) throw new IllegalArgumentException("ID do professor inválido."); return executarEmTransacao(conn -> { ProfessorDAO dao = new ProfessorDAO(conn); if (buscarEmTodosPorId(dao, idProfessor) == null) throw new IllegalArgumentException("Professor não encontrado."); return dao.inativar(idProfessor); }, "Erro ao excluir professor."); }

    // Reativa um professor previamente inativado.
    public boolean reativarProfessor(int idProfessor) { if (idProfessor <= 0) throw new IllegalArgumentException("ID do professor inválido."); return executarEmTransacao(conn -> new ProfessorDAO(conn).reativar(idProfessor), "Erro ao reativar professor."); }

    // Busca professor por ID, retornando também inativos e avisando quando o cadastro estiver inativo.
    public Professor buscarProfessorPorId(int idProfessor) { if (idProfessor <= 0) throw new IllegalArgumentException("ID do professor inválido."); try (Connection conn = ConnectionFactory.getConnection()) { ProfessorDAO dao = new ProfessorDAO(conn); Professor p = dao.buscarPorId(idProfessor); if (p == null) p = buscarEmTodosPorId(dao, idProfessor); if (p == null) throw new IllegalArgumentException("Professor não encontrado."); informarSeInativo(p); return p; } catch (SQLException e) { throw new RuntimeException("Erro ao buscar professor por ID.", e); } }

    // Busca professor por CPF, retornando também inativos e avisando quando o cadastro estiver inativo.
    public Professor buscarProfessorPorCpf(String cpf) { String c = normalizarCpf(cpf); if (c == null || !ValidaCPF.isValido(c)) throw new IllegalArgumentException("CPF do professor inválido."); try (Connection conn = ConnectionFactory.getConnection()) { ProfessorDAO dao = new ProfessorDAO(conn); Professor p = dao.buscarPorCpf(c); if (p == null) p = buscarEmTodosPorCpf(dao, c); if (p == null) throw new IllegalArgumentException("Professor não encontrado."); informarSeInativo(p); return p; } catch (SQLException e) { throw new RuntimeException("Erro ao buscar professor por CPF.", e); } }

    // Busca professor por nome, retornando também inativos e avisando quando o cadastro estiver inativo.
    public Professor buscarProfessorPorNome(String nome) { String n = tratarTexto(nome); if (n == null || n.isBlank()) throw new IllegalArgumentException("Nome do professor é obrigatório."); try (Connection conn = ConnectionFactory.getConnection()) { ProfessorDAO dao = new ProfessorDAO(conn); Professor p = dao.buscarPorNome(n); if (p == null) p = buscarEmTodosPorNome(dao, n); if (p == null) throw new IllegalArgumentException("Professor não encontrado."); informarSeInativo(p); return p; } catch (SQLException e) { throw new RuntimeException("Erro ao buscar professor por nome.", e); } }

    // Lista todos os professores, ativos e inativos.
    public List<Professor> listarProfessores() { return listarTodosProfessores(); }
    // Lista todos os professores, ativos e inativos.
    public List<Professor> listarTodosProfessores() { try (Connection conn = ConnectionFactory.getConnection()) { return new ProfessorDAO(conn).listarTodos(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar professores.", e); } }
    // Lista somente professores ativos.
    public List<Professor> listarProfessoresAtivos() { try (Connection conn = ConnectionFactory.getConnection()) { return new ProfessorDAO(conn).listarAtivos(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar professores ativos.", e); } }
    // Lista somente professores inativos.
    public List<Professor> listarProfessoresInativos() { try (Connection conn = ConnectionFactory.getConnection()) { return new ProfessorDAO(conn).listarInativos(); } catch (SQLException e) { throw new RuntimeException("Erro ao listar professores inativos.", e); } }
}
