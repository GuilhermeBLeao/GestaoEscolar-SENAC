//Guilherme

package util;

public final class ValidaCEP {

    private ValidaCEP() {}

    public static boolean isValido(String cep) {
        if (cep == null) {
            return false;
        }

        String valor = cep.replaceAll("\\D", "");

        if (valor.length() != 8) {
            return false;
        }

        return valor.chars().distinct().count() != 1;
    }

    public static void validar(String cep) {
        if (!isValido(cep)) {
            throw new IllegalArgumentException("CEP inválido.");
        }
    }
}