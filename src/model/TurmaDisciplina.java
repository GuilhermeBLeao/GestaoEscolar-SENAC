//luiz

package model;

import java.time.LocalDateTime;

public class TurmaDisciplina {
    private int id_turmad;
    private int professores_id;
    private int turma_id;
    private int disciplina_id;
    private LocalDateTime HorarioInicio;
    private LocalDateTime HorarioTermino;
    
    
    
	public TurmaDisciplina() {
	}
	
	public int getId_turmad() {
		return id_turmad;
	}
	public void setId_turmad(int id_turmad) {
		this.id_turmad = id_turmad;
	}
	public int getProfessores_id() {
		return professores_id;
	}
	public void setProfessores_id(int professores_id) {
		this.professores_id = professores_id;
	}
	public int getTurma_id() {
		return turma_id;
	}
	public void setTurma_id(int turma_id) {
		this.turma_id = turma_id;
	}
	public int getDisciplina_id() {
		return disciplina_id;
	}
	public void setDisciplina_id(int disciplina_id) {
		this.disciplina_id = disciplina_id;
	}
	public LocalDateTime getHorarioInicio() {
		return HorarioInicio;
	}
	public void setHorarioInicio(LocalDateTime horarioInicio) {
		HorarioInicio = horarioInicio;
	}
	public LocalDateTime getHorarioTermino() {
		return HorarioTermino;
	}
	public void setHorarioTermino(LocalDateTime horarioTermino) {
		HorarioTermino = horarioTermino;
	}

    
}
