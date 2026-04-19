//Guilherme

package util;

import java.util.regex.Pattern; //Biblioteca server para trabalhar com expressões regulares. Forma de verificar se um texto segue um padrão

public class ValidaEmail {

    // Regex simples e eficiente para validação de email
	private static final String EMAIL_REGEX =
	        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"; //$ significa "fim da string", ou seja o email precisa terminar seguindo este padrão.

    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX); /*Isso transforma a string do regex em um objeto Pattern, 
    																													que o Java usa para fazer a validação de forma mais eficiente.*/

    public static boolean isValido(String email) {
        if (email == null) //Se o valor recebido for nulo, retorna falso.
        		return false;

        email = email.trim();//Remove espaços em branco no início e no final do texto

        if (email.isEmpty())
        		return false;

        return pattern.matcher(email).matches();/*Cria um comparador entre o reges e o email informado
        																		.matches() verifica se o email inteiro seguiu o padrão*/
    }
}