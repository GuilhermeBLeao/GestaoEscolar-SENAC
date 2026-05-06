//Guilherme

package util;

import variaveisEnum.Estado;

public final class ValidaEstado {

    private ValidaEstado() {}

    public static boolean isValido(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return false;
        }

        try {
            Estado.valueOf(estado.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public static String getNomeCompleto(String estado) {
        validar(estado);
        return Estado.valueOf(estado.trim().toUpperCase()).getNomeCompleto();
    }

    public static void validar(String estado) {
        if (!isValido(estado)) {
            throw new IllegalArgumentException("Estado inválido.");
        }
    }
}