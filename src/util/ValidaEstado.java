//Guilherme

package util;

import java.util.Set;

public final class ValidaEstado {

    private static final Set<String> UFS = Set.of(
        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES",
        "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR",
        "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC",
        "SP", "SE", "TO"
    );

    private ValidaEstado() {}

    public static boolean isValido(String estado) {
        if (estado == null) 
            return false;

        String uf = estado.trim().toUpperCase();
        return UFS.contains(uf);
    }
}