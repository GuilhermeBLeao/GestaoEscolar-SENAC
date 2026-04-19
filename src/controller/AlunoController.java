//Márcio

package controller;

import dao.AlunoDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Endereco;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AlunoController {

    public void salvarAluno(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }

        normalizarAluno(aluno);

        if (aluno.getDataCadastro() == null) {
            aluno.setDataCadastro(LocalDate.now());
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AlunoDAO alunoDAO = new AlunoDAO(conn);

                if (alunoDAO.existeCpf(aluno.getCpf())) {
                    throw new IllegalArgumentException("Já existe aluno cadastrado com este CPF.");
                }

                alunoDAO.inserir(aluno);
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar aluno.", e);
        }
    }

    public void atualizarAluno(Aluno alunoAtualizado) {
        if (alunoAtualizado == null) {
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }

        if (alunoAtualizado.getIdAluno() <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        normalizarAluno(alunoAtualizado);

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AlunoDAO alunoDAO = new AlunoDAO(conn);

                Aluno alunoBanco = alunoDAO.buscarPorId(alunoAtualizado.getIdAluno());
                if (alunoBanco == null) {
                    throw new IllegalArgumentException("Aluno não encontrado.");
                }

                Aluno alunoComMesmoCpf = alunoDAO.buscarPorCpf(alunoAtualizado.getCpf());
                if (alunoComMesmoCpf != null
                        && alunoComMesmoCpf.getIdAluno() != alunoAtualizado.getIdAluno()) {
                    throw new IllegalArgumentException("Já existe outro aluno cadastrado com este CPF.");
                }

                Aluno alunoParaSalvar = mesclarDadosPermitidos(alunoBanco, alunoAtualizado);

                alunoDAO.atualizar(alunoParaSalvar);
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar aluno.", e);
        }
    }

    public boolean excluirAluno(int idAluno) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AlunoDAO alunoDAO = new AlunoDAO(conn);
                boolean excluiu = alunoDAO.excluir(idAluno);
                conn.commit();
                return excluiu;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir aluno.", e);
        }
    }

    public Aluno buscarAlunoPorId(int idAluno) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.buscarPorId(idAluno);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por ID.", e);
        }
    }

    public Aluno buscarAlunoPorCpf(String cpf) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.buscarPorCpf(tratarTexto(cpf));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por CPF.", e);
        }
    }

    public Aluno buscarAlunoPorMatricula(String matricula) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.buscarPorMatricula(tratarTexto(matricula));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por matrícula.", e);
        }
    }

    public List<Aluno> listarAlunos() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.listarTodos();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos.", e);
        }
    }

    private Aluno mesclarDadosPermitidos(Aluno alunoBanco, Aluno alunoAtualizado) {
        alunoBanco.setNome(alunoAtualizado.getNome());
        alunoBanco.setEmail(alunoAtualizado.getEmail());
        alunoBanco.setSituacao(alunoAtualizado.getSituacao());
        alunoBanco.setSexo(alunoAtualizado.getSexo());
        alunoBanco.setTelefone(alunoAtualizado.getTelefone());
        alunoBanco.setCpf(alunoAtualizado.getCpf());
        alunoBanco.setRg(alunoAtualizado.getRg());
        alunoBanco.setObsSaude(alunoAtualizado.getObsSaude());
        alunoBanco.setDataNascimento(alunoAtualizado.getDataNascimento());
        alunoBanco.setIdPais(alunoAtualizado.getIdPais());
        alunoBanco.setIdTurma(alunoAtualizado.getIdTurma());

        atualizarEnderecoExistente(alunoBanco, alunoAtualizado);

        return alunoBanco;
    }

    private void atualizarEnderecoExistente(Aluno alunoBanco, Aluno alunoAtualizado) {
        if (alunoAtualizado.getEndereco() == null) {
            throw new IllegalArgumentException("Endereço do aluno é obrigatório.");
        }

        Endereco enderecoBanco = alunoBanco.getEndereco();
        Endereco enderecoAtualizado = alunoAtualizado.getEndereco();

        if (enderecoBanco == null) {
            throw new IllegalArgumentException("Aluno atual não possui endereço cadastrado.");
        }

        enderecoBanco.setRua(tratarTexto(enderecoAtualizado.getRua()));
        enderecoBanco.setNumero(tratarTexto(enderecoAtualizado.getNumero()));
        enderecoBanco.setComplemento(tratarTexto(enderecoAtualizado.getComplemento()));
        enderecoBanco.setBairro(tratarTexto(enderecoAtualizado.getBairro()));
        enderecoBanco.setCidade(tratarTexto(enderecoAtualizado.getCidade()));
        enderecoBanco.setEstado(tratarTexto(enderecoAtualizado.getEstado()));
        enderecoBanco.setCep(tratarTexto(enderecoAtualizado.getCep()));
    }

    private void normalizarAluno(Aluno aluno) {
        aluno.setNome(tratarTexto(aluno.getNome()));
        aluno.setEmail(tratarTexto(aluno.getEmail()));
        aluno.setTelefone(tratarTexto(aluno.getTelefone()));
        aluno.setCpf(tratarTexto(aluno.getCpf()));
        aluno.setRg(tratarTexto(aluno.getRg()));
        aluno.setObsSaude(tratarTexto(aluno.getObsSaude()));

        if (aluno.getEndereco() != null) {
            aluno.getEndereco().setRua(tratarTexto(aluno.getEndereco().getRua()));
            aluno.getEndereco().setNumero(tratarTexto(aluno.getEndereco().getNumero()));
            aluno.getEndereco().setComplemento(tratarTexto(aluno.getEndereco().getComplemento()));
            aluno.getEndereco().setBairro(tratarTexto(aluno.getEndereco().getBairro()));
            aluno.getEndereco().setCidade(tratarTexto(aluno.getEndereco().getCidade()));
            aluno.getEndereco().setEstado(tratarTexto(aluno.getEndereco().getEstado()));
            aluno.getEndereco().setCep(tratarTexto(aluno.getEndereco().getCep()));
        }
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}