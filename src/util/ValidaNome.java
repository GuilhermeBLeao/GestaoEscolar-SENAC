//Guilherme

package util;

public final class ValidaNome {

	private ValidaNome() {
	}

	public static boolean isValido(String nome) {
		if (nome == null) {
			return false;
		}

		String valor = nome.trim();

		if (valor.length() < 3 || valor.length() > 100) {
			return false;
		}

		// letras + espaço + apóstrofo
		if (!valor.matches("^[\\p{L}' ]+$")) {
			return false;
		}

		// nome completo (tem pelo menos um espaço)
		return valor.contains(" ");
	}

	public static void validar(String nome) {
		if (!isValido(nome)) {
			throw new IllegalArgumentException(
					"Nome inválido. Use apenas letras, espaços e apóstrofo, e informe nome completo.");
		}
	}
}