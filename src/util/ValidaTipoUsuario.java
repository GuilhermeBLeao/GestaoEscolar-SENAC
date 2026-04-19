//Guilherme

package util;

import variaveisEnum.TipoUsuario;

public class ValidaTipoUsuario {

    public static boolean isValido(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) //Remove os espaços e transforma tudo em maiúsculo
            return false;

        try {
            TipoUsuario.valueOf(tipo.trim().toUpperCase());//Tenta converter a string para enum
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}