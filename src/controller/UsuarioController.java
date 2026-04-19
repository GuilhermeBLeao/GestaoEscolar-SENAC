//Guilherme

package controller;

import dao.UsuarioDAO;
import model.Usuario;
import util.ValidaCPF;

import java.time.LocalDate;
import java.util.List;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController() {this.usuarioDAO = new UsuarioDAO();}

    public void cadastrarUsuario(Usuario usuario) {
        if (usuario == null) 
            throw new IllegalArgumentException("Usuário não pode ser nulo.");

        normalizarUsuario(usuario);
        validarUsuario(usuario);

        if (usuarioDAO.existeCpf(usuario.getCpf()))
            throw new IllegalArgumentException("Já existe usuário cadastrado com este CPF.");

        if (usuario.getDataCriacao() == null)
            usuario.setDataCriacao(LocalDate.now());

        usuarioDAO.inserir(usuario);
    }

    public Usuario autenticarLogin(String cpf, String senha) {
        String cpfTratado = tratarTexto(cpf);

        if (cpfTratado == null || cpfTratado.isEmpty())
            throw new IllegalArgumentException("CPF é obrigatório.");

        if (!ValidaCPF.isValido(cpfTratado))
            throw new IllegalArgumentException("CPF inválido.");


        if (senha == null || senha.trim().isEmpty())
            throw new IllegalArgumentException("Senha é obrigatória.");

        Usuario usuario = usuarioDAO.autenticar(cpfTratado, senha);

        if (usuario == null) 
            throw new IllegalArgumentException("CPF ou senha inválidos.");

        usuarioDAO.atualizarUltimoLogin(usuario.getIdUsuario());
        usuario.setUltimoLogin(LocalDate.now());

        return usuario;
    }

    public void atualizarUsuario(Usuario usuarioAtualizado) {
        if (usuarioAtualizado == null)
            throw new IllegalArgumentException("Usuário não pode ser nulo.");


        if (usuarioAtualizado.getIdUsuario() <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        normalizarUsuario(usuarioAtualizado);
        validarUsuario(usuarioAtualizado);

        Usuario usuarioBanco = usuarioDAO.buscarPorId(usuarioAtualizado.getIdUsuario());
        if (usuarioBanco == null)
            throw new IllegalArgumentException("Usuário não encontrado.");

        Usuario usuarioComMesmoCpf = usuarioDAO.buscarPorCpf(usuarioAtualizado.getCpf());
        if (usuarioComMesmoCpf != null
                && usuarioComMesmoCpf.getIdUsuario() != usuarioAtualizado.getIdUsuario())
            throw new IllegalArgumentException("Já existe outro usuário cadastrado com este CPF.");


        Usuario usuarioParaSalvar = mesclarDadosPermitidos(usuarioBanco, usuarioAtualizado);
        usuarioDAO.atualizar(usuarioParaSalvar);
    }

    public Usuario buscarUsuarioPorId(int idUsuario) {
        if (idUsuario <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        return usuarioDAO.buscarPorId(idUsuario);
    }

    public Usuario buscarUsuarioPorCpf(String cpf) {
        String cpfTratado = tratarTexto(cpf);

        if (cpfTratado == null || cpfTratado.isEmpty())
            throw new IllegalArgumentException("CPF é obrigatório para busca.");

        if (!ValidaCPF.isValido(cpfTratado))
            throw new IllegalArgumentException("CPF inválido.");

        return usuarioDAO.buscarPorCpf(cpfTratado);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listar();
    }

    public boolean excluirUsuario(int idUsuario) {
        if (idUsuario <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        return usuarioDAO.excluir(idUsuario);
    }

    public void atualizarUltimoLogin(int idUsuario) {
        if (idUsuario <= 0)
            throw new IllegalArgumentException("ID do usuário inválido.");

        usuarioDAO.atualizarUltimoLogin(idUsuario);
    }

    private Usuario mesclarDadosPermitidos(Usuario usuarioBanco, Usuario usuarioAtualizado) {
        usuarioBanco.setCpf(usuarioAtualizado.getCpf());
        usuarioBanco.setAlunoId(usuarioAtualizado.getAlunoId());
        usuarioBanco.setFuncionarioId(usuarioAtualizado.getFuncionarioId());
        usuarioBanco.setPaiId(usuarioAtualizado.getPaiId());
        usuarioBanco.setProfessorId(usuarioAtualizado.getProfessorId());
        usuarioBanco.setAtivo(usuarioAtualizado.isAtivo());
        usuarioBanco.setTipoUsuario(usuarioAtualizado.getTipoUsuario());

        if (usuarioAtualizado.getSenhaHash() != null && !usuarioAtualizado.getSenhaHash().trim().isEmpty())
            usuarioBanco.setSenhaHash(usuarioAtualizado.getSenhaHash());
        if (usuarioAtualizado.getDataCriacao() != null)
            usuarioBanco.setDataCriacao(usuarioAtualizado.getDataCriacao());
        if (usuarioAtualizado.getUltimoLogin() != null)
            usuarioBanco.setUltimoLogin(usuarioAtualizado.getUltimoLogin());
        return usuarioBanco;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario.getCpf() == null || usuario.getCpf().isEmpty())
            throw new IllegalArgumentException("CPF do usuário é obrigatório.");

        if (!ValidaCPF.isValido(usuario.getCpf())) 
            throw new IllegalArgumentException("CPF do usuário inválido.");

        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().isEmpty()) 
            throw new IllegalArgumentException("Senha do usuário é obrigatória.");

        if (usuario.getTipoUsuario() == null) 
            throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
    }

    private void normalizarUsuario(Usuario usuario) {
        usuario.setCpf(tratarTexto(usuario.getCpf()));
    }

    private String tratarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}