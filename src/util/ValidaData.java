//Guilherme

package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class ValidaData {

    private static final DateTimeFormatter FORMATADOR =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                             .withResolverStyle(ResolverStyle.STRICT);

    private ValidaData() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não pode ser instanciada.");
    }

    public static boolean validar(String dataStr) {

        if (dataStr == null || dataStr.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate data = LocalDate.parse(dataStr.trim(), FORMATADOR);

            return !data.isAfter(LocalDate.now());

        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDate converter(String dataStr) {

        if (!validar(dataStr)) {
            return null;
        }

        return LocalDate.parse(dataStr.trim(), FORMATADOR);
    }

    public static boolean isDataFutura(String dataStr) {
        try {
            LocalDate data = LocalDate.parse(dataStr.trim(), FORMATADOR);
            return data.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}