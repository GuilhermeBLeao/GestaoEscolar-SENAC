//Guilherme

package util;

public final class ValidaSenha {

    private ValidaSenha() {}

    public static boolean isValido(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            return false;
        }

        return senha.matches("^(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    }

    public static void validar(String senha) {
        if (!isValido(senha)) {
            throw new IllegalArgumentException(
                    "Senha deve ter pelo menos 8 caracteres, 1 caractere especial e 1 número."
            );
        }
    }
}
