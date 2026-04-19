//Guilherme

package util;

public class ValidaTelefone {

    public static boolean isValido(String telefone) {
        if (telefone == null)
        		return false;

        // Remove tudo que não for número
        telefone = telefone.replaceAll("\\D", "");

        /* Telefones válidos no Brasil:
        10 dígitos → fixo (com DDD)
        11 dígitos → celular (com DDD)*/
        if (telefone.length() < 10 || telefone.length() > 11) 
            return false;

        // Evita números todos iguais (ex: 11111111111)
        if (telefone.chars().distinct().count() == 1) 
            return false;

        // Validação básica do DDD (não pode começar com 0)
        if (telefone.startsWith("0"))
            return false;
        return true;
    }
}