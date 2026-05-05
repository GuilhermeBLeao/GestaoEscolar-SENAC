//Guilherme

package util;

import model.Usuario;

public final class SessaoUsuario {

    private static final ThreadLocal<Usuario> usuarioLogado = new ThreadLocal<>();
    /*
     * ThreadLocal cria uma "variável separada por thread".
     * Ou seja: cada execução (thread) terá seu próprio usuarioLogado.
     * Isso evita que um usuário sobrescreva o outro em outra thread.
     */

    private SessaoUsuario() {
        // Construtor privado para impedir instanciação
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado.get();
    }

    public static void setUsuarioLogado(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        if (existeUsuarioLogado()) {
            throw new IllegalStateException("Já existe uma sessão ativa. Encerre a sessão atual antes de iniciar outra.");
        }
        usuarioLogado.set(usuario);
    }

    public static boolean existeUsuarioLogado() {
        return usuarioLogado.get() != null;
    }

    public static void encerrarSessao() {
        usuarioLogado.remove();
    }
}
