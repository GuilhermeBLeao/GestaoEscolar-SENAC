//Guilherme

package util;

public class ValidaCPF {

    public static boolean isValido(String cpf) {
        if (cpf == null) 
        		return false;

        cpf = cpf.replaceAll("\\D", ""); // Remove tudo que não for número

        if (cpf.length() != 11) //Verifica se CPF tem 11 números
        		return false; 

      //chars pega os caracteres da string, distinct remove repetidos, count conta quantos diferentes existem.
        if (cpf.chars().distinct().count() == 1)  //Se só existe um caratere diferente, significa que todos são iguais.
        		return false;

        try {
            int soma = 0, resto; //Soma guarda os cálculos e resto o dígito verificador calculado

            for (int i = 1; i <= 9; i++)//Laço que percorre os 9 primeiros dígitos do CPF
                soma += Integer.parseInt(cpf.substring(i - 1, i)) * (11 - i); //Transforma o número em inteiro, e multiplica pelo peso correspondente,
            																								  //Soma += vai somando tudo

            resto = (soma * 10) % 11; // Calcula o primeiro dígito verificador
            if (resto == 10) resto = 0; //Se o resultado for 10, a regra do CPF manda considerar 0.

            if (resto != Integer.parseInt(cpf.substring(9, 10))) //Compara o dígito calculado com o primeiro dígito verificador do CPF informado.
                return false;

            soma = 0; //Zera a soma para começar um novo cálculo

            for (int i = 1; i <= 10; i++)
                soma += Integer.parseInt(cpf.substring(i - 1, i)) * (12 - i);

            resto = (soma * 10) % 11;
            if (resto == 10) resto = 0;

            return resto == Integer.parseInt(cpf.substring(10, 11));

        } catch (Exception e) {
            return false;
        }
    }
}