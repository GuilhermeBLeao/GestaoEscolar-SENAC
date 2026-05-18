//Guilherme

package controller;

import dao.AlunoDAO;
import dao.GeradorMatricula;
import dao.PaisAlunoDAO;
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
            throw new RuntimeException("Erro ao obter conexÃ£o com o banco de dados.", e);
        }
    }

    private void validarAlunoNaoNulo(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno nÃ£o pode ser nulo.");
        }
    }

    private void normalizarAluno(Aluno aluno) {
        aluno.setNome(tratarTexto(aluno.getNome()));
        aluno.setEmail(normalizarEmail(aluno.getEmail()));
        aluno.setTelefone(normalizarTelefone(aluno.getTelefone()));
        // Correcao: o CPF ja e normalizado quando entra no model e o setter e imutavel depois disso.
        // Reaplicar setCpf aqui fazia updates e ate cadastros falharem sem necessidade.
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
            throw new IllegalArgumentException("Email do aluno invÃ¡lido.");
        }

        if (!ValidaTelefone.isValido(aluno.getTelefone())) {
            throw new IllegalArgumentException("Telefone do aluno invÃ¡lido.");
        }

        if (!ValidaCPF.isValido(aluno.getCpf())) {
            throw new IllegalArgumentException("CPF do aluno invÃ¡lido.");
        }

        if (aluno.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento Ã© obrigatÃ³ria.");
        }

        if (aluno.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento nÃ£o pode ser futura.");
        }

        if (aluno.getIdPais() <= 0) {
            throw new IllegalArgumentException("ID dos pais/responsÃ¡veis Ã© obrigatÃ³rio.");
        }

        if (aluno.getIdTurma() <= 0) {
            throw new IllegalArgumentException("ID da turma Ã© obrigatÃ³rio.");
        }

        validarEndereco(aluno.getEndereco());
    }

    private void validarCamposControladosPeloSistemaNoCadastro(Aluno aluno) {
        if (aluno.getDataCadastro() != null) {
            throw new IllegalArgumentException("Data de cadastro Ã© controlada pelo sistema.");
        }

        if (aluno.getMatricula() != null && !aluno.getMatricula().isBlank()) {
            throw new IllegalArgumentException("MatrÃ­cula Ã© gerada automaticamente pelo sistema.");
        }
    }

    private void validarCpfImutavel(Aluno alunoAtualizado, Aluno alunoBanco) {
        if (!alunoBanco.getCpf().equals(alunoAtualizado.getCpf())) {
            throw new IllegalArgumentException("CPF do aluno nÃ£o pode ser alterado apÃ³s o cadastro.");
        }
    }

    private void validarDataCadastroImutavel(Aluno alunoAtualizado, Aluno alunoBanco) {
        if (alunoAtualizado.getDataCadastro() != null
                && !alunoAtualizado.getDataCadastro().equals(alunoBanco.getDataCadastro())) {
            throw new IllegalArgumentException("Data de cadastro nÃ£o pode ser alterada.");
        }
    }

    private void validarMatriculaImutavel(Aluno alunoAtualizado, Aluno alunoBanco) {
        if (alunoAtualizado.getMatricula() != null
                && !alunoAtualizado.getMatricula().equals(alunoBanco.getMatricula())) {
            throw new IllegalArgumentException("MatrÃ­cula nÃ£o pode ser alterada.");
        }
    }

    private void validarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("EndereÃ§o Ã© obrigatÃ³rio.");
        }

        ValidaCidade.validar(endereco.getCidade());

        if (!ValidaCEP.isValido(endereco.getCep())) {
            throw new IllegalArgumentException("CEP invÃ¡lido.");
        }
    }

    private void validarMatriculaParaBusca(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("MatrÃ­cula Ã© obrigatÃ³ria para busca.");
        }

        if (!matricula.matches("\\d{10}")) {
            throw new IllegalArgumentException("MatrÃ­cula deve conter exatamente 10 dÃ­gitos numÃ©ricos.");
        }
    }

    private void validarReferenciasRelacionadas(Connection conn, Aluno aluno) throws SQLException {
        PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
        TurmaDAO turmaDAO = new TurmaDAO(conn);

        // CorreÃ§Ã£o: alÃ©m de validar IDs > 0, agora confirmamos que os registros existem antes de persistir.
        if (paisAlunoDAO.buscarPorId(aluno.getIdPais()) == null) {
            throw new IllegalArgumentException("Pais/ResponsÃ¡veis informados nÃ£o foram encontrados.");
        }

        // CorreÃ§Ã£o: evita gravar aluno apontando para uma turma inexistente e falhar sÃ³ no banco.
        if (turmaDAO.buscarPorId(aluno.getIdTurma()) == null) {
            throw new IllegalArgumentException("Turma informada nÃ£o foi encontrada.");
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
            throw new IllegalArgumentException("EndereÃ§o atualizado Ã© obrigatÃ³rio.");
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
            throw new IllegalArgumentException("ID do aluno invÃ¡lido.");
        if (novaIdTurma <= 0)
            throw new IllegalArgumentException("ID da nova turma invÃ¡lido.");

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            TurmaDAO turmaDAO = new TurmaDAO(conn);

            Aluno aluno = alunoDAO.buscarPorId(idAluno);
            if (aluno == null)
                throw new IllegalArgumentException("Aluno nÃ£o encontrado.");

            if (aluno.getSituacao() != SituacaoAluno.ATIVO)
                throw new IllegalArgumentException("Somente alunos com situaÃ§Ã£o ATIVO podem ser transferidos de turma.");

            if (aluno.getIdTurma() == novaIdTurma)
                throw new IllegalArgumentException("O aluno jÃ¡ pertence Ã  turma informada.");

            if (turmaDAO.buscarPorId(novaIdTurma) == null)
                throw new IllegalArgumentException("Turma de destino nÃ£o encontrada.");

            alunoDAO.transferirTurma(idAluno, novaIdTurma);
            return null;
        }, "Erro ao transferir aluno de turma.");
    }

    public void transferirExterno(int idAluno) {
        if (idAluno <= 0)
            throw new IllegalArgumentException("ID do aluno invÃ¡lido.");

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            Aluno aluno = alunoDAO.buscarPorId(idAluno);
            if (aluno == null)
                throw new IllegalArgumentException("Aluno nÃ£o encontrado.");

            if (aluno.getSituacao() == SituacaoAluno.TRANSFERIDO)
                throw new IllegalArgumentException("Aluno jÃ¡ possui situaÃ§Ã£o TRANSFERIDO.");

            alunoDAO.atualizarSituacao(idAluno, SituacaoAluno.TRANSFERIDO);
            return null;
        }, "Erro ao registrar transferÃªncia externa do aluno.");
    }

    public void salvarAluno(Aluno aluno) {
        validarAlunoNaoNulo(aluno);
        normalizarAluno(aluno);
        validarParaCadastro(aluno);

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            if (alunoDAO.existeCpf(aluno.getCpf())) {
                throw new IllegalArgumentException("JÃ¡ existe aluno cadastrado com este CPF.");
            }

            validarReferenciasRelacionadas(conn, aluno);

            aluno.setDataCadastro(LocalDate.now());
            aluno.setMatricula(GeradorMatricula.gerar(conn));

            alunoDAO.inserir(aluno);
            return null;
        }, "Erro ao salvar aluno.");
    }

    public void atualizarAluno(Aluno alunoAtualizado) {
        validarAlunoNaoNulo(alunoAtualizado);

        if (alunoAtualizado.getIdAluno() <= 0) {
            throw new IllegalArgumentException("ID do aluno invÃ¡lido.");
        }

        normalizarAluno(alunoAtualizado);

        executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            Aluno alunoBanco = alunoDAO.buscarPorId(alunoAtualizado.getIdAluno());
            if (alunoBanco == null) {
                throw new IllegalArgumentException("Aluno nÃ£o encontrado.");
            }

            validarParaAtualizacao(alunoAtualizado, alunoBanco);
            validarReferenciasRelacionadas(conn, alunoAtualizado);

            Aluno alunoParaSalvar = mesclarDadosPermitidos(alunoBanco, alunoAtualizado);
            alunoDAO.atualizar(alunoParaSalvar);
            return null;
        }, "Erro ao atualizar aluno.");
    }

    public boolean excluirAluno(int idAluno) {
        if (idAluno <= 0) {
            throw new IllegalArgumentException("ID do aluno invÃ¡lido.");
        }

        return executarEmTransacao(conn -> {
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            Aluno alunoExistente = alunoDAO.buscarPorId(idAluno);
            if (alunoExistente == null) {
                throw new IllegalArgumentException("Aluno nÃ£o encontrado.");
            }

            // CorreÃ§Ã£o: a exclusÃ£o agora Ã© lÃ³gica, preservando histÃ³rico e impedindo delete fÃ­sico acidental.
            return alunoDAO.excluir(idAluno);
        }, "Erro ao excluir aluno.");
    }

    public Aluno buscarAlunoPorId(int idAluno) {
        if (idAluno <= 0) {
            throw new IllegalArgumentException("ID do aluno invÃ¡lido.");
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
            throw new IllegalArgumentException("CPF Ã© obrigatÃ³rio para busca.");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF invÃ¡lido.");
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
            throw new RuntimeException("Erro ao buscar aluno por matrÃ­cula.", e);
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


