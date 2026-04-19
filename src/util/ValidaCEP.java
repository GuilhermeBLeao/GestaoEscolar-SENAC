//Guilherme

package util;

public class ValidaCEP {

    public static boolean isValido(String cep) {
        if (cep == null) return false;

        // Remove tudo que não for número
        cep = cep.replaceAll("\\D", "");

        // CEP no Brasil tem exatamente 8 dígitos
        if (cep.length() != 8) 
            return false;

        // Evita CEP com todos os números iguais (ex: 00000000)
        if (cep.chars().distinct().count() == 1)
            return false;

        return true;
    }
}