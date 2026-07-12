package model;

public class TurmaDisciplina {
  private int turmaId;
  private int disciplinaId;

  public int getTurmaId() {
    return turmaId;
  }

  public void setTurmaId(int turmaId) {
    if (turmaId <= 0) {
      throw new IllegalArgumentException("ID da turma invalido.");
    }
    this.turmaId = turmaId;
  }

  public int getDisciplinaId() {
    return disciplinaId;
  }

  public void setDisciplinaId(int disciplinaId) {
    if (disciplinaId <= 0) {
      throw new IllegalArgumentException("ID da disciplina invalido.");
    }
    this.disciplinaId = disciplinaId;
  }
}
