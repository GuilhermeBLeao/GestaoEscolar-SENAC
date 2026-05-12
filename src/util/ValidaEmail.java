//Guilherme

package util;

import java.util.regex.Pattern;

public final class ValidaEmail {

	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

	private ValidaEmail() {
	}

	public static boolean isValido(String email) {
		if (email == null) {
			return false;
		}

		String valor = email.trim();

		if (valor.isEmpty()) {
			return false;
		}

		return PATTERN.matcher(valor).matches();
	}

	public static void validar(String email) {
		if (!isValido(email)) {
			throw new IllegalArgumentException("Email inválido.");
		}
	}
}