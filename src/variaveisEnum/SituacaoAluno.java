//Guilherme

package variaveisEnum;

public enum SituacaoAluno {
    ATIVO,
    INATIVO,
    TRANCADO,
    TRANSFERIDO,
    CONCLUIDO;
    
    public boolean isAtivo() {
        return this == ATIVO;
    }
}