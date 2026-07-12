// Guilherme

package util;

public final class ValidaCPF {

  private ValidaCPF() {}

  public static boolean isValido(String cpf) {
    if (cpf == null || cpf.trim().isEmpty()) {
      return false;
    }

    cpf = cpf.replaceAll("\\D", "");

    if (cpf.length() != 11) {
      return false;
    }

    if (cpf.chars().distinct().count() == 1) {
      return false;
    }

    try {
      int soma = 0;
      int resto;

      for (int i = 1; i <= 9; i++) {
        soma += Integer.parseInt(cpf.substring(i - 1, i)) * (11 - i);
      }

      resto = (soma * 10) % 11;
      if (resto == 10) {
        resto = 0;
      }

      if (resto != Integer.parseInt(cpf.substring(9, 10))) {
        return false;
      }

      soma = 0;

      for (int i = 1; i <= 10; i++) {
        soma += Integer.parseInt(cpf.substring(i - 1, i)) * (12 - i);
      }

      resto = (soma * 10) % 11;
      if (resto == 10) {
        resto = 0;
      }

      return resto == Integer.parseInt(cpf.substring(10, 11));

    } catch (Exception e) {
      return false;
    }
  }

  public static void validar(String cpf) {
    if (!isValido(cpf)) {
      throw new IllegalArgumentException("CPF inválido.");
    }
  }
}
