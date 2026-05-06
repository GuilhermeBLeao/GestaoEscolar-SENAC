//Guilherme

package variaveisEnum;

public enum TipoUsuario {
    ALUNO,
    RESPONSAVEL,
    PROFESSOR,
    SECRETARIA,
    DIRECAO,
    PEDAGOGICO;

    public boolean isAdministrativo() {
        return this == SECRETARIA || this == DIRECAO || this == PEDAGOGICO;
    }
}