//Guilherme

package controller;

import dao.AlunoDAO;
import dao.GeradorMatricula;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.Endereco;
import variaveisEnum.SituacaoAluno;
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
                T resultado = acao.executar(conn);
                conn.commit();
                return resultado;

            } catch (IllegalArgumentException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }

                throw new RuntimeException(mensagemOperacao, e);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter conexão com o banco de dados.", e);
        }
    }

    private void validarAlunoNaoNulo(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }
    }

    private void normalizarAluno(Aluno aluno) {
        aluno.setNome(tratarTexto(aluno.getNome()));
        aluno.setEmail(normalizarEmail(aluno.getEmail()));
        aluno.setTelefone(normalizarTelefone(aluno.getTelefone()));
        aluno.setCpf(normalizarCpf(aluno.getCpf()));
        aluno.setRg(tratarTexto(aluno.getRg()));
        aluno.setObsSaude(tratarTexto(aluno.getObsSaude()));

        Endereco endereco = aluno.getEndereco();
        if (endereco != null) {
            endereco.setRua(tratarTexto(endereco.getRua()));
            endereco.setNumero(tratarTexto(endereco.getNumero()));
            endereco.setComplemento(tratarTexto(endereco.getComplemento()));
            endereco.setBairro(tratarTexto(endereco.getBairro()));
            endereco.setCidade(tratarTexto(endereco.getCidade()));
            endereco.setCep(normalizarCep(endereco.getCep()));
        }
    }

    private void validarParaCadastro(Aluno aluno) {
        validarCamposBase(aluno);
        validarCamposControladosPeloSistemaNoCadastro(aluno);
    }

    private void validarParaAtualizacao(Aluno alunoAtualizado, Aluno alunoBanco) {
        validarCamposBase(alunoAtualizado);
        validarCpfImutavel(alunoAtualizado, alunoBanco);
        validarDataCadastroImutavel(alunoAtualizado, alunoBanco);
        validarMatriculaImutavel(alunoAtualizado, alunoBanco);
    }

    private void validarCamposBase(Aluno aluno) {
        ValidaNome.validar(aluno.getNome());

        if (!ValidaEmail.isValido(aluno.getEmail())) {
            throw new IllegalArgumentException("Email do aluno inválido.");
        }

        if (!ValidaTelefone.isValido(aluno.getTelefone())) {
            throw new IllegalArgumentException("Telefone do aluno inválido.");
        }

        if (!ValidaCPF.isValido(aluno.getCpf())) {
            throw new IllegalArgumentException("CPF do aluno inválido.");
        }

        if (aluno.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória.");
        }

        if (aluno.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser futura.");
        }

        if (aluno.getIdPais() <= 0) {
            throw new IllegalArgumentException("ID dos pais/responsáveis é obrigatório.");
        }

        if (aluno.getIdTurma() <= 0) {
            throw new IllegalArgumentException("ID da turma é obrigatório.");
        }

        validarEndereco(aluno.getEndereco());
    }

    private void validarCamposControladosPeloSistemaNoCadastro(Aluno aluno) {
        if (aluno.getDataCadastro() != null) {
            throw new IllegalArgumentException("Data de cadastro é controlada pelo sistema.");
        }

        if (aluno.getMatricula() != null && !aluno.getMatricula().isBlank()) {
            throw new IllegalArgumentException("Matrícula é gerada automaticamente pelo sistema.");
        }
    }

    private void validarCpfImutavel(Aluno alunoAtualizado, Aluno alunoBanco) {
        if (!alunoBanco.getCpf().equals(alunoAtualizado.getCpf())) {
            throw new IllegalArgumentException("CPF do aluno não pode ser alterado após o cadastro.");
        }
    }

    private void validarDataCadastroImutavel(Aluno alunoAtualizado, Aluno alunoBanco) {
        if (alunoAtualizado.getDataCadastro() != null
                && !alunoAtualizado.getDataCadastro().equals(alunoBanco.getDataCadastro())) {
            throw new IllegalArgumentException("Data de cadastro não pode ser alterada.");
        }
    }

    private void validarMatriculaImutavel(Aluno alunoAtualizado, Aluno alunoBanco) {
        if (alunoAtualizado.getMatricula() != null
                && !alunoAtualizado.getMatricula().equals(alunoBanco.getMatricula())) {
            throw new IllegalArgumentException("Matrícula não pode ser alterada.");
        }
    }

    private void validarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço é obrigatório.");
        }

        ValidaCidade.validar(endereco.getCidade());

        if (!ValidaCEP.isValido(endereco.getCep())) {
            throw new IllegalArgumentException("CEP inválido.");
        }
    }

    private void validarMatriculaParaBusca(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória para busca.");
        }

        if (!matricula.matches("\\d{10}")) {
            throw new IllegalArgumentException("Matrícula deve conter exatamente 10 dígitos numéricos.");
        }
    }

    private Aluno mesclarDadosPermitidos(Aluno alunoBanco, Aluno alunoAtualizado) {
        alunoBanco.setNome(alunoAtualizado.getNome());
        alunoBanco.setEmail(alunoAtualizado.getEmail());
        alunoBanco.setSituacao(alunoAtualizado.getSituacao());
        alunoBanco.setSexo(alunoAtualizado.getSexo());
        alunoBanco.setTelefone(alunoAtualizado.getTelefone());
        alunoBanco.setRg(alunoAtualizado.getRg());
        alunoBanco.setObsSaude(alunoAtualizado.getObsSaude());
        alunoBanco.setDataNascimento(alunoAtualizado.getDataNascimento());
        alunoBanco.setIdPais(alunoAtualizado.getIdPais());
        alunoBanco.setIdTurma(alunoAtualizado.getIdTurma());

        atualizarOuCriarEndereco(alunoBanco, alunoAtualizado);

        return alunoBanco;
    }

    private void atualizarOuCriarEndereco(Aluno alunoBanco, Aluno alunoAtualizado) {
        Endereco enderecoAtualizado = alunoAtualizado.getEndereco();
        if (enderecoAtualizado == null) {
            throw new IllegalArgumentException("Endereço atualizado é obrigatório.");
        }

        Endereco enderecoBanco = alunoBanco.getEndereco();

        if (enderecoBanco == null) {
            enderecoBanco = new Endereco();
            alunoBanco.setEndereco(enderecoBanco);
        }

        enderecoBanco.setRua(tratarTexto(enderecoAtualizado.getRua()));
        enderecoBanco.setNumero(tratarTexto(enderecoAtualizado.getNumero()));
        enderecoBanco.setComplemento(tratarTexto(enderecoAtualizado.getComplemento()));
        enderecoBanco.setBairro(tratarTexto(enderecoAtualizado.getBairro()));
        enderecoBanco.setCidade(tratarTexto(enderecoAtualizado.getCidade()));
        enderecoBanco.setEstado(enderecoAtualizado.getEstado());
        enderecoBanco.setCep(normalizarCep(enderecoAtualizado.getCep()));
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarEmail(String email) {
        String valor = tratarTexto(email);
        return valor == null ? null : valor.toLowerCase();
    }

    private String normalizarCpf(String cpf) {
        String valor = tratarTexto(cpf);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarTelefone(String telefone) {
        String valor = tratarTexto(telefone);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarCep(String cep) {
        String valor = tratarTexto(cep);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public void matricularAluno(Aluno aluno) {
        salvarAluno(aluno);
    }

    public void transferirParaTurma(int idAluno, int novaIdTurma) {
        if (idAluno <= 0)
            throw new IllegalArgumentException("ID do aluno inválido.");
        if (novaIdTurma <= 0)
            throw new IllegalArgumentException("ID da nova turma inválido.");

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            TurmaDAO turmaDAO = new TurmaDAO(conn);

            Aluno aluno = alunoDAO.buscarPorId(idAluno);
            if (aluno == null)
                throw new IllegalArgumentException("Aluno não encontrado.");

            if (aluno.getSituacao() != SituacaoAluno.ATIVO)
                throw new IllegalArgumentException("Somente alunos com situação ATIVO podem ser transferidos de turma.");

            if (aluno.getIdTurma() == novaIdTurma)
                throw new IllegalArgumentException("O aluno já pertence à turma informada.");

            if (turmaDAO.buscarPorId(novaIdTurma) == null)
                throw new IllegalArgumentException("Turma de destino não encontrada.");

            alunoDAO.transferirTurma(idAluno, novaIdTurma);
            return null;
        }, "Erro ao transferir aluno de turma.");
    }

    public void transferirExterno(int idAluno) {
        if (idAluno <= 0)
            throw new IllegalArgumentException("ID do aluno inválido.");

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            Aluno aluno = alunoDAO.buscarPorId(idAluno);
            if (aluno == null)
                throw new IllegalArgumentException("Aluno não encontrado.");

            if (aluno.getSituacao() == SituacaoAluno.TRANSFERIDO)
                throw new IllegalArgumentException("Aluno já possui situação TRANSFERIDO.");

            alunoDAO.atualizarSituacao(idAluno, SituacaoAluno.TRANSFERIDO);
            return null;
        }, "Erro ao registrar transferência externa do aluno.");
    }

    public void salvarAluno(Aluno aluno) {
        validarAlunoNaoNulo(aluno);
        normalizarAluno(aluno);
        validarParaCadastro(aluno);

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            if (alunoDAO.existeCpf(aluno.getCpf())) {
                throw new IllegalArgumentException("Já existe aluno cadastrado com este CPF.");
            }

            aluno.setDataCadastro(LocalDate.now());
            aluno.setMatricula(GeradorMatricula.gerar(conn));

            alunoDAO.inserir(aluno);
            return null;
        }, "Erro ao salvar aluno.");
    }

    public void atualizarAluno(Aluno alunoAtualizado) {
        validarAlunoNaoNulo(alunoAtualizado);

        if (alunoAtualizado.getIdAluno() <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        normalizarAluno(alunoAtualizado);

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            Aluno alunoBanco = alunoDAO.buscarPorId(alunoAtualizado.getIdAluno());
            if (alunoBanco == null) {
                throw new IllegalArgumentException("Aluno não encontrado.");
            }

            validarParaAtualizacao(alunoAtualizado, alunoBanco);

            Aluno alunoParaSalvar = mesclarDadosPermitidos(alunoBanco, alunoAtualizado);
            alunoDAO.atualizar(alunoParaSalvar);
            return null;
        }, "Erro ao atualizar aluno.");
    }

    public boolean excluirAluno(int idAluno) {
        if (idAluno <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        return executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            Aluno alunoExistente = alunoDAO.buscarPorId(idAluno);
            if (alunoExistente == null) {
                throw new IllegalArgumentException("Aluno não encontrado.");
            }

            return alunoDAO.excluir(idAluno);
        }, "Erro ao excluir aluno.");
    }

    public Aluno buscarAlunoPorId(int idAluno) {
        if (idAluno <= 0) {
            throw new IllegalArgumentException("ID do aluno inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.buscarPorId(idAluno);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por ID.", e);
        }
    }

    public Aluno buscarAlunoPorCpf(String cpf) {
        String cpfTratado = normalizarCpf(cpf);

        if (cpfTratado == null || cpfTratado.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório para busca.");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.buscarPorCpf(cpfTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por CPF.", e);
        }
    }

    public Aluno buscarAlunoPorMatricula(String matricula) {
        String matriculaTratada = tratarTexto(matricula);
        validarMatriculaParaBusca(matriculaTratada);

        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.buscarPorMatricula(matriculaTratada);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por matrícula.", e);
        }
    }

    public List<Aluno> listarAlunos() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            return alunoDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos.", e);
        }
    }
}