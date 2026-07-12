// Guilherme

package variaveisEnum;

public enum TipoUsuario {
  ADMINISTRADOR,
  ALUNO,
  RESPONSAVEL,
  PROFESSOR,
  SECRETARIA,
  DIRECAO,
  PEDAGOGICO;

  public boolean isAdministrativo() {
    return this == ADMINISTRADOR || this == SECRETARIA || this == DIRECAO || this == PEDAGOGICO;
  }
}
