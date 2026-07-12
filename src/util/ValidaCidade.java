// Guilherme

package util;

public final class ValidaCidade {

  private ValidaCidade() {}

  public static boolean isValido(String cidade) {
    if (cidade == null) {
      return false;
    }

    String valor = cidade.trim();

    if (valor.length() < 2 || valor.length() > 80) {
      return false;
    }

    // letras + espaço + apóstrofo
    return valor.matches("^[\\p{L}' ]+$");
  }

  public static void validar(String cidade) {
    if (!isValido(cidade)) {
      throw new IllegalArgumentException(
          "Cidade inválida. Use apenas letras, espaços e apóstrofo.");
    }
  }
}
