// Guilherme

package util;

public final class ValidaTelefone {

  private ValidaTelefone() {}

  public static boolean isValido(String telefone) {
    if (telefone == null) {
      return false;
    }

    String valor = telefone.replaceAll("\\D", "");

    if (valor.length() < 10 || valor.length() > 11) {
      return false;
    }

    if (valor.chars().distinct().count() == 1) {
      return false;
    }

    return !valor.startsWith("0");
  }

  public static void validar(String telefone) {
    if (!isValido(telefone)) {
      throw new IllegalArgumentException("Telefone inválido.");
    }
  }
}
