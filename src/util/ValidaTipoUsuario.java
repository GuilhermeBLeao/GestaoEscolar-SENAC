// Guilherme

package util;

import variaveisEnum.TipoUsuario;

public final class ValidaTipoUsuario {

  private ValidaTipoUsuario() {}

  public static boolean isValido(String tipo) {
    if (tipo == null || tipo.trim().isEmpty()) {
      return false;
    }

    try {
      TipoUsuario.valueOf(tipo.trim().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static void validar(String tipo) {
    if (!isValido(tipo)) {
      throw new IllegalArgumentException("Tipo de usuário inválido.");
    }
  }
}
