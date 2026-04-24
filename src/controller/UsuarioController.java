//Guilherme

package controller;

import dao.UsuarioDAO;
import database.ConnectionFactory;
import model.Usuario;
import util.ValidaCPF;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UsuarioController {

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

    private void validarUsuarioNaoNulo(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }
    }

    private void normalizarUsuario(Usuario usuario) {
        if (usuario.getCpf() != null) {
            usuario.setCpf(normalizarCpf(usuario.getCpf()));
        }
    }

    private void validarParaCadastro(Usuario usuario) {
        validarCamposBase(usuario);
        validarSenhaObrigatoria(usuario);
        validarConsistenciaTemporalCadastro(usuario);
        validarConsistenciaVinculoPorTipo(usuario);
    }

    private void validarParaAtualizacao(Usuario usuarioAtualizado, Usuario usuarioBanco) {
        validarCamposBase(usuarioAtualizado);
        validarSenhaSeInformada(usuarioAtualizado);
        validarCpfImutavel(usuarioAtualizado, usuarioBanco);
        validarConsistenciaTemporalAtualizacao(usuarioAtualizado, usuarioBanco);
        validarConsistenciaVinculoPorTipo(usuarioAtualizado);
        validarCamposControladosPeloSistemaEmUpdate(usuarioAtualizado, usuarioBanco);
    }

    private void validarCamposBase(Usuario usuario) {
        if (usuario.getCpf() == null || usuario.getCpf().isBlank()) {
            throw new IllegalArgumentException("CPF do usuário é obrigatório.");
        }

        if (!ValidaCPF.isValido(usuario.getCpf())) {
            throw new IllegalArgumentException("CPF do usuário inválido.");
        }

        if (usuario.getTipoUsuario() == null) {
            throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
        }
    }

    private void validarSenhaObrigatoria(Usuario usuario) {
        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().isBlank()) {
            throw new IllegalArgumentException(
                    "Senha do usuário é obrigatória. Defina a senha antes de cadastrar.");
        }

        validarFormatoHashSeInformado(usuario.getSenhaHash());
    }

    private void validarSenhaSeInformada(Usuario usuario) {
        String senhaHash = usuario.getSenhaHash();

        if (senhaHash == null) {
            return;
        }

        if (senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha informado é inválido.");
        }

        validarFormatoHashSeInformado(senhaHash);
    }

    private void validarFormatoHashSeInformado(String senhaHash) {
        if (!senhaHash.matches("^\\$2[aby]\\$\\d{2}\\$.*$")) {
            throw new IllegalArgumentException(
                    "Hash da senha inválido. Informe um hash BCrypt válido ou use setSenha(...) no model.");
        }
    }

    private void validarCpfImutavel(Usuario usuarioAtualizado, Usuario usuarioBanco) {
        if (!usuarioBanco.getCpf().equals(usuarioAtualizado.getCpf())) {
            throw new IllegalArgumentException("CPF não pode ser alterado após o cadastro.");
        }
    }

    private void validarConsistenciaTemporalCadastro(Usuario usuario) {
        if (usuario.getDataCriacao() != null && usuario.getDataCriacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de criação não pode ser futura.");
        }

        if (usuario.getUltimoLogin() != null) {
            throw new IllegalArgumentException("Último login não deve ser informado no cadastro.");
        }
    }

    private void validarConsistenciaTemporalAtualizacao(Usuario usuarioAtualizado, Usuario usuarioBanco) {
        if (usuarioAtualizado.getDataCriacao() != null
                && usuarioAtualizado.getDataCriacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de criação não pode ser futura.");
        }

        if (usuarioAtualizado.getUltimoLogin() != null
                && usuarioAtualizado.getUltimoLogin().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Último login não pode ser futuro.");
        }

        LocalDate dataCriacaoEfetiva = usuarioBanco.getDataCriacao();

        if (usuarioAtualizado.getUltimoLogin() != null
                && dataCriacaoEfetiva != null
                && usuarioAtualizado.getUltimoLogin().toLocalDate().isBefore(dataCriacaoEfetiva)) {
            throw new IllegalArgumentException("Último login não pode ser anterior à data de criação.");
        }
    }

    private void validarCamposControladosPeloSistemaEmUpdate(Usuario usuarioAtualizado, Usuario usuarioBanco) {
        if (usuarioAtualizado.getDataCriacao() != null
                && !usuarioAtualizado.getDataCriacao().equals(usuarioBanco.getDataCriacao())) {
            throw new IllegalArgumentException("Data de criação é controlada pelo sistema e não pode ser alterada.");
        }

        if (usuarioAtualizado.getUltimoLogin() != null) {
            if (usuarioBanco.getUltimoLogin() == null
                    || !usuarioAtualizado.getUltimoLogin().equals(usuarioBanco.getUltimoLogin())) {
                throw new IllegalArgumentException("Último login só pode ser alterado pelo fluxo de autenticação.");
            }
        }
    }

    private void validarConsistenciaVinculoPorTipo(Usuario usuario) {
        int quantidadeVinculos = contarVinculosPreenchidos(usuario);

        if (quantidadeVinculos > 1) {
            throw new IllegalArgumentException(
                    "Usuário deve possuir no máximo um vínculo entre aluno, responsável, professor e funcionário.");
        }

        String tipo = usuario.getTipoUsuario().name();

        switch (tipo) {
            case "ALUNO":
                exigirSomenteVinculoAluno(usuario);
                break;
            case "RESPONSAVEL":
                exigirSomenteVinculoResponsavel(usuario);
                break;
            case "PROFESSOR":
                exigirSomenteVinculoProfessor(usuario);
                break;
            case "FUNCIONARIO":
                exigirSomenteVinculoFuncionario(usuario);
                break;
            case "ADMIN":
                exigirSemVinculo(usuario);
                break;
            default:
                throw new IllegalArgumentException("Tipo de usuário não mapeado no controller: " + tipo + ".");
        }
    }

    private int contarVinculosPreenchidos(Usuario usuario) {
        int total = 0;

        if (usuario.getAlunoId() > 0) total++;
        if (usuario.getPaiId() > 0) total++;
        if (usuario.getProfessorId() > 0) total++;
        if (usuario.getFuncionarioId() > 0) total++;

        return total;
    }

    private void exigirSomenteVinculoAluno(Usuario usuario) {
        if (usuario.getAlunoId() <= 0
                || usuario.getPaiId() > 0
                || usuario.getProfessorId() > 0
                || usuario.getFuncionarioId() > 0) {
            throw new IllegalArgumentException("Usuário do tipo ALUNO deve possuir apenas alunoId válido.");
        }
    }

    private void exigirSomenteVinculoResponsavel(Usuario usuario) {
        if (usuario.getPaiId() <= 0
                || usuario.getAlunoId() > 0
                || usuario.getProfessorId() > 0
                || usuario.getFuncionarioId() > 0) {
            throw new IllegalArgumentException("Usuário do tipo RESPONSAVEL deve possuir apenas paiId válido.");
        }
    }

    private void exigirSomenteVinculoProfessor(Usuario usuario) {
        if (usuario.getProfessorId() <= 0
                || usuario.getAlunoId() > 0
                || usuario.getPaiId() > 0
                || usuario.getFuncionarioId() > 0) {
            throw new IllegalArgumentException("Usuário do tipo PROFESSOR deve possuir apenas professorId válido.");
        }
    }

    private void exigirSomenteVinculoFuncionario(Usuario usuario) {
        if (usuario.getFuncionarioId() <= 0
                || usuario.getAlunoId() > 0
                || usuario.getPaiId() > 0
                || usuario.getProfessorId() > 0) {
            throw new IllegalArgumentException("Usuário do tipo FUNCIONARIO deve possuir apenas funcionarioId válido.");
        }
    }

    private void exigirSemVinculo(Usuario usuario) {
        if (usuario.getAlunoId() > 0
                || usuario.getPaiId() > 0
                || usuario.getProfessorId() > 0
                || usuario.getFuncionarioId() > 0) {
            throw new IllegalArgumentException(
                    "Usuário do tipo ADMIN não deve possuir vínculo com aluno, responsável, professor ou funcionário.");
        }
    }

    private Usuario mesclarDadosPermitidos(Usuario usuarioBanco, Usuario usuarioAtualizado) {
        usuarioBanco.setAlunoId(usuarioAtualizado.getAlunoId());
        usuarioBanco.setFuncionarioId(usuarioAtualizado.getFuncionarioId());
        usuarioBanco.setPaiId(usuarioAtualizado.getPaiId());
        usuarioBanco.setProfessorId(usuarioAtualizado.getProfessorId());
        usuarioBanco.setAtivo(usuarioAtualizado.isAtivo());
        usuarioBanco.setTipoUsuario(usuarioAtualizado.getTipoUsuario());

        if (usuarioAtualizado.getSenhaHash() != null && !usuarioAtualizado.getSenhaHash().isBlank()) {
            usuarioBanco.setSenhaHash(usuarioAtualizado.getSenhaHash());
        }

        return usuarioBanco;
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarCpf(String cpf) {
        String valor = tratarTexto(cpf);
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public void salvarUsuario(Usuario usuario) {
        cadastrarUsuario(usuario);
    }

    public void cadastrarUsuario(Usuario usuario) {
        validarUsuarioNaoNulo(usuario);
        normalizarUsuario(usuario);
        validarParaCadastro(usuario);

        if (usuario.getDataCriacao() == null) {
            usuario.setDataCriacao(LocalDate.now());
        }

        executarEmTransacao(conn -> {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            if (usuarioDAO.existeCpf(usuario.getCpf())) {
                throw new IllegalArgumentException("Já existe usuário cadastrado com este CPF.");
            }

            validarVinculoUnicoNoBanco(usuarioDAO, usuario, 0);

            usuarioDAO.inserir(usuario);
            return null;
        }, "Erro ao cadastrar usuário.");
    }

    public Usuario autenticarLogin(String cpf, String senha) {
        String cpfTratado = normalizarCpf(cpf);

        if (cpfTratado == null || cpfTratado.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }

        return executarEmTransacao(conn -> {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            Usuario usuario = usuarioDAO.autenticar(cpfTratado, senha);
            if (usuario == null) {
                throw new IllegalArgumentException("CPF ou senha inválidos.");
            }

            if (!usuario.isAtivo()) {
                throw new IllegalArgumentException("Usuário inativo. Login não permitido.");
            }

            LocalDateTime agora = LocalDateTime.now();
            usuarioDAO.atualizarUltimoLogin(usuario.getIdUsuario(), agora);
            usuario.setUltimoLogin(agora);

            return usuario;
        }, "Erro ao autenticar usuário.");
    }

    public void atualizarUsuario(Usuario usuarioAtualizado) {
        validarUsuarioNaoNulo(usuarioAtualizado);

        if (usuarioAtualizado.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        normalizarUsuario(usuarioAtualizado);

        executarEmTransacao(conn -> {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            Usuario usuarioBanco = usuarioDAO.buscarPorId(usuarioAtualizado.getIdUsuario());
            if (usuarioBanco == null) {
                throw new IllegalArgumentException("Usuário não encontrado.");
            }

            validarParaAtualizacao(usuarioAtualizado, usuarioBanco);
            validarVinculoUnicoNoBanco(usuarioDAO, usuarioAtualizado, usuarioAtualizado.getIdUsuario());

            Usuario usuarioParaSalvar = mesclarDadosPermitidos(usuarioBanco, usuarioAtualizado);
            usuarioDAO.atualizar(usuarioParaSalvar);
            return null;
        }, "Erro ao atualizar usuário.");
    }

    private void validarVinculoUnicoNoBanco(UsuarioDAO usuarioDAO, Usuario usuario, int idUsuarioAtual)
            throws SQLException {

        if (usuario.getAlunoId() > 0) {
            Usuario existente = usuarioDAO.buscarPorAlunoId(usuario.getAlunoId());
            if (existente != null && existente.getIdUsuario() != idUsuarioAtual) {
                throw new IllegalArgumentException("Já existe usuário vinculado a este aluno.");
            }
        }

        if (usuario.getPaiId() > 0) {
            Usuario existente = usuarioDAO.buscarPorPaiId(usuario.getPaiId());
            if (existente != null && existente.getIdUsuario() != idUsuarioAtual) {
                throw new IllegalArgumentException("Já existe usuário vinculado a este responsável.");
            }
        }

        if (usuario.getProfessorId() > 0) {
            Usuario existente = usuarioDAO.buscarPorProfessorId(usuario.getProfessorId());
            if (existente != null && existente.getIdUsuario() != idUsuarioAtual) {
                throw new IllegalArgumentException("Já existe usuário vinculado a este professor.");
            }
        }

        if (usuario.getFuncionarioId() > 0) {
            Usuario existente = usuarioDAO.buscarPorFuncionarioId(usuario.getFuncionarioId());
            if (existente != null && existente.getIdUsuario() != idUsuarioAtual) {
                throw new IllegalArgumentException("Já existe usuário vinculado a este funcionário.");
            }
        }
    }

    public void atualizarSenhaHash(int idUsuario, String novoHash) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        if (novoHash == null || novoHash.isBlank()) {
            throw new IllegalArgumentException("Novo hash da senha é obrigatório.");
        }

        validarFormatoHashSeInformado(novoHash);

        executarEmTransacao(conn -> {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            Usuario usuarioBanco = usuarioDAO.buscarPorId(idUsuario);
            if (usuarioBanco == null) {
                throw new IllegalArgumentException("Usuário não encontrado.");
            }

            usuarioDAO.atualizarSenhaHash(idUsuario, novoHash);
            return null;
        }, "Erro ao atualizar senha do usuário.");
    }

    public Usuario buscarUsuarioPorId(int idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            return usuarioDAO.buscarPorId(idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuário por ID.", e);
        }
    }

    public Usuario buscarUsuarioPorCpf(String cpf) {
        String cpfTratado = normalizarCpf(cpf);

        if (cpfTratado == null || cpfTratado.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório para busca.");
        }

        if (!ValidaCPF.isValido(cpfTratado)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            return usuarioDAO.buscarPorCpf(cpfTratado);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuário por CPF.", e);
        }
    }

    public List<Usuario> listarUsuarios() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            return usuarioDAO.listar();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar usuários.", e);
        }
    }

    public boolean excluirUsuario(int idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        return executarEmTransacao(conn -> {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            Usuario usuarioExistente = usuarioDAO.buscarPorId(idUsuario);
            if (usuarioExistente == null) {
                throw new IllegalArgumentException("Usuário não encontrado.");
            }

            return usuarioDAO.excluir(idUsuario);
        }, "Erro ao excluir usuário.");
    }

    public void atualizarUltimoLogin(int idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("ID do usuário inválido.");
        }

        executarEmTransacao(conn -> {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            Usuario usuarioExistente = usuarioDAO.buscarPorId(idUsuario);
            if (usuarioExistente == null) {
                throw new IllegalArgumentException("Usuário não encontrado.");
            }

            usuarioDAO.atualizarUltimoLogin(idUsuario, LocalDateTime.now());
            return null;
        }, "Erro ao atualizar último login do usuário.");
    }
}