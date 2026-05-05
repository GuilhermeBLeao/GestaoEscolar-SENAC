//Igor

package controller;

import dao.PaisAlunoDAO;
import database.ConnectionFactory;
import model.PaisAluno;
import util.ValidaCPF;
import util.ValidaEmail;
import util.ValidaNome;
import util.ValidaTelefone;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PaisAlunoController {

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

    private void validarPaisAlunoNaoNulo(PaisAluno paisAluno) {
        if (paisAluno == null) {
            throw new IllegalArgumentException("Pais/Responsáveis não podem ser nulos.");
        }
    }

    private void normalizarPaisAluno(PaisAluno paisAluno) {
        paisAluno.setNomeMae(normalizarTextoOpcional(paisAluno.getNomeMae()));
        paisAluno.setEmailMae(normalizarEmail(paisAluno.getEmailMae()));
        paisAluno.setTelefoneMae(normalizarTelefone(paisAluno.getTelefoneMae()));
        paisAluno.setCpfMae(normalizarCpf(paisAluno.getCpfMae()));

        paisAluno.setNomePai(normalizarTextoOpcional(paisAluno.getNomePai()));
        paisAluno.setEmailPai(normalizarEmail(paisAluno.getEmailPai()));
        paisAluno.setTelefonePai(normalizarTelefone(paisAluno.getTelefonePai()));
        paisAluno.setCpfPai(normalizarCpf(paisAluno.getCpfPai()));
    }

    private void validarParaCadastro(PaisAluno paisAluno) {
        validarCamposBase(paisAluno);
    }

    private void validarParaAtualizacao(PaisAluno paisAtualizado, PaisAluno paisBanco) {
        validarCamposBase(paisAtualizado);
        validarCpfMaeImutavel(paisAtualizado, paisBanco);
        validarCpfPaiImutavel(paisAtualizado, paisBanco);
    }

    private void validarCamposBase(PaisAluno paisAluno) {
        boolean maeInformada = possuiAlgumDadoResponsavel(
                paisAluno.getNomeMae(),
                paisAluno.getEmailMae(),
                paisAluno.getTelefoneMae(),
                paisAluno.getCpfMae()
        );

        boolean paiInformado = possuiAlgumDadoResponsavel(
                paisAluno.getNomePai(),
                paisAluno.getEmailPai(),
                paisAluno.getTelefonePai(),
                paisAluno.getCpfPai()
        );

        if (!maeInformada && !paiInformado) {
            throw new IllegalArgumentException("Pelo menos um responsável (mãe ou pai) deve ser informado.");
        }

        if (maeInformada) {
            validarResponsavelCompleto(
                    paisAluno.getNomeMae(),
                    paisAluno.getEmailMae(),
                    paisAluno.getTelefoneMae(),
                    paisAluno.getCpfMae(),
                    "mãe"
            );
        }

        if (paiInformado) {
            validarResponsavelCompleto(
                    paisAluno.getNomePai(),
                    paisAluno.getEmailPai(),
                    paisAluno.getTelefonePai(),
                    paisAluno.getCpfPai(),
                    "pai"
            );
        }

        if (paisAluno.getCpfMae() != null
                && paisAluno.getCpfPai() != null
                && paisAluno.getCpfMae().equals(paisAluno.getCpfPai())) {
            throw new IllegalArgumentException("CPF da mãe e do pai não podem ser iguais.");
        }
    }

    private void validarResponsavelCompleto(
            String nome,
            String email,
            String telefone,
            String cpf,
            String tipoResponsavel
    ) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do(a) " + tipoResponsavel + " é obrigatório quando informado.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email do(a) " + tipoResponsavel + " é obrigatório quando informado.");
        }

        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone do(a) " + tipoResponsavel + " é obrigatório quando informado.");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do(a) " + tipoResponsavel + " é obrigatório quando informado.");
        }

        ValidaNome.validar(nome);

        if (!ValidaEmail.isValido(email)) {
            throw new IllegalArgumentException("Email do(a) " + tipoResponsavel + " inválido.");
        }

        if (!ValidaTelefone.isValido(telefone)) {
            throw new IllegalArgumentException("Telefone do(a) " + tipoResponsavel + " inválido.");
        }

        if (!ValidaCPF.isValido(cpf)) {
            throw new IllegalArgumentException("CPF do(a) " + tipoResponsavel + " inválido.");
        }
    }

    private void validarCpfMaeImutavel(PaisAluno paisAtualizado, PaisAluno paisBanco) {
        String cpfMaeBanco = normalizarCpf(paisBanco.getCpfMae());
        String cpfMaeAtualizado = normalizarCpf(paisAtualizado.getCpfMae());

        if (!cpfsIguais(cpfMaeBanco, cpfMaeAtualizado)) {
            throw new IllegalArgumentException("O CPF da mãe não pode ser alterado.");
        }
    }

    private void validarCpfPaiImutavel(PaisAluno paisAtualizado, PaisAluno paisBanco) {
        String cpfPaiBanco = normalizarCpf(paisBanco.getCpfPai());
        String cpfPaiAtualizado = normalizarCpf(paisAtualizado.getCpfPai());

        if (!cpfsIguais(cpfPaiBanco, cpfPaiAtualizado)) {
            throw new IllegalArgumentException("O CPF do pai não pode ser alterado.");
        }
    }

    private boolean cpfsIguais(String cpf1, String cpf2) {
        if (cpf1 == null && cpf2 == null) {
            return true;
        }

        if (cpf1 == null || cpf2 == null) {
            return false;
        }

        return cpf1.equals(cpf2);
    }

    private boolean possuiAlgumDadoResponsavel(String nome, String email, String telefone, String cpf) {
        return !isBlank(nome) || !isBlank(email) || !isBlank(telefone) || !isBlank(cpf);
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private PaisAluno mesclarDadosPermitidos(PaisAluno paisBanco, PaisAluno paisAtualizado) {
        paisBanco.setNomeMae(paisAtualizado.getNomeMae());
        paisBanco.setNomePai(paisAtualizado.getNomePai());
        paisBanco.setEmailMae(paisAtualizado.getEmailMae());
        paisBanco.setEmailPai(paisAtualizado.getEmailPai());
        paisBanco.setTelefoneMae(paisAtualizado.getTelefoneMae());
        paisBanco.setTelefonePai(paisAtualizado.getTelefonePai());
        return paisBanco;
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarTextoOpcional(String valor) {
        String texto = tratarTexto(valor);
        return (texto == null || texto.isBlank()) ? null : texto;
    }

    private String normalizarEmail(String email) {
        String valor = normalizarTextoOpcional(email);
        return valor == null ? null : valor.toLowerCase();
    }

    private String normalizarCpf(String cpf) {
        String valor = normalizarTextoOpcional(cpf);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarTelefone(String telefone) {
        String valor = normalizarTextoOpcional(telefone);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public void salvarPaisAluno(PaisAluno paisAluno) {
        validarPaisAlunoNaoNulo(paisAluno);
        normalizarPaisAluno(paisAluno);
        validarParaCadastro(paisAluno);

        executarEmTransacao(conn -> {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);

            if (paisAluno.getCpfMae() != null && paisAlunoDAO.existeCpfMae(paisAluno.getCpfMae())) {
                throw new IllegalArgumentException("Já existe cadastro com este CPF da mãe.");
            }

            if (paisAluno.getCpfPai() != null && paisAlunoDAO.existeCpfPai(paisAluno.getCpfPai())) {
                throw new IllegalArgumentException("Já existe cadastro com este CPF do pai.");
            }

            paisAlunoDAO.inserir(paisAluno);
            return null;
        }, "Erro ao salvar pais/responsáveis.");
    }

    public void atualizarPaisAluno(PaisAluno paisAtualizado) {
        validarPaisAlunoNaoNulo(paisAtualizado);

        if (paisAtualizado.getIdPais() <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        normalizarPaisAluno(paisAtualizado);

        executarEmTransacao(conn -> {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);

            PaisAluno paisBanco = paisAlunoDAO.buscarPorId(paisAtualizado.getIdPais());
            if (paisBanco == null) {
                throw new IllegalArgumentException("Pais/Responsáveis não encontrados.");
            }

            validarParaAtualizacao(paisAtualizado, paisBanco);

            PaisAluno paisParaSalvar = mesclarDadosPermitidos(paisBanco, paisAtualizado);
            paisAlunoDAO.atualizar(paisParaSalvar);
            return null;
        }, "Erro ao atualizar pais/responsáveis.");
    }

    public boolean excluirPaisAluno(int idPais) {
        if (idPais <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        return executarEmTransacao(conn -> {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);

            PaisAluno paisExistente = paisAlunoDAO.buscarPorId(idPais);
            if (paisExistente == null) {
                throw new IllegalArgumentException("Pais/Responsáveis não encontrados.");
            }

            return paisAlunoDAO.excluir(idPais);
        }, "Erro ao excluir pais/responsáveis.");
    }

    public PaisAluno buscarPaisAlunoPorId(int idPais) {
        if (idPais <= 0) {
            throw new IllegalArgumentException("ID de pais/responsáveis inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
            return paisAlunoDAO.buscarPorId(idPais);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pais/responsáveis por ID.", e);
        }
    }

    public PaisAluno buscarPaisAlunoPorCpfMae(String cpfMae) {
        String cpfTratado = normalizarCpf(cpfMae);

        if (cpfTratado == null || cpfTratado.isBlank()) {
            throw new IllegalArgumentException("CPF da mãe é obrigatório para busca");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF da mãe inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
            return paisAlunoDAO.buscarPorCpfMae(cpfTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pais/responsáveis por CPF da mãe", e);
        }
    }

    public PaisAluno buscarPaisAlunoPorCpfPai(String cpfPai) {
        String cpfTratado = normalizarCpf(cpfPai);

        if (cpfTratado == null || cpfTratado.isBlank()) {
            throw new IllegalArgumentException("CPF do pai é obrigatório para busca");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF do pai inválido");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
            return paisAlunoDAO.buscarPorCpfPai(cpfTratado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pais/responsáveis por CPF do pai", e);
        }
    }

    public List<PaisAluno> listarPaisAlunos() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            PaisAlunoDAO paisAlunoDAO = new PaisAlunoDAO(conn);
            return paisAlunoDAO.listar();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pais/responsáveis.", e);
        }
    }
}
