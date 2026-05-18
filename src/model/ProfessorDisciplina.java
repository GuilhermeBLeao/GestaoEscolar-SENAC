//igor
//Classe modelo para representar a relação entre Professor e Disciplina

package model;

import java.time.LocalDate;

public class ProfessorDisciplina {
	//Atributo que armazena o identificador único da relação Professor-Disciplina
	private int idProfessorDisciplina;
	
	//Atributo que armazena o identificador do professor associado
	private int idProfessor;
	
	//Atributo que armazena o identificador da disciplina associada
	private int idDisciplina;
	
	//Atributo que armazena a data de início da associação entre professor e disciplina
	private LocalDate dataVinculacao;
	
	//Atributo que armazena a data de término da associação entre professor e disciplina
	private LocalDate dataDesvinculacao;
	
	//Atributo booleano que indica se a relação está ativa (true) ou inativa (false)
	private boolean ativo;

	//Método getter que retorna o identificador da relação Professor-Disciplina
	public int getIdProfessorDisciplina() {
		return idProfessorDisciplina;
	}

	//Método setter que define o identificador da relação Professor-Disciplina com validação
	public void setIdProfessorDisciplina(int idProfessorDisciplina) {
		//Valida se o ID é maior que zero antes de atribuir
		if (idProfessorDisciplina <= 0) {
			throw new IllegalArgumentException("ID da relação Professor-Disciplina é inválido.");
		}
		this.idProfessorDisciplina = idProfessorDisciplina;
	}

	//Método getter que retorna o identificador do professor
	public int getIdProfessor() {
		return idProfessor;
	}

	//Método setter que define o identificador do professor com validação
	public void setIdProfessor(int idProfessor) {
		//Valida se o ID do professor é maior que zero antes de atribuir
		if (idProfessor <= 0) {
			throw new IllegalArgumentException("ID do professor é inválido.");
		}
		this.idProfessor = idProfessor;
	}

	//Método getter que retorna o identificador da disciplina
	public int getIdDisciplina() {
		return idDisciplina;
	}

	//Método setter que define o identificador da disciplina com validação
	public void setIdDisciplina(int idDisciplina) {
		//Valida se o ID da disciplina é maior que zero antes de atribuir
		if (idDisciplina <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		this.idDisciplina = idDisciplina;
	}

	//Método getter que retorna a data de vinculação do professor com a disciplina
	public LocalDate getDataVinculacao() {
		return dataVinculacao;
	}

	//Método setter que define a data de vinculação com validação
	public void setDataVinculacao(LocalDate dataVinculacao) {
		//Valida se a data é nula antes de atribuir
		if (dataVinculacao == null) {
			throw new IllegalArgumentException("Data de vinculação é obrigatória.");
		}
		this.dataVinculacao = dataVinculacao;
	}

	//Método getter que retorna a data de desvinculação do professor com a disciplina
	public LocalDate getDataDesvinculacao() {
		return dataDesvinculacao;
	}

	//Método setter que define a data de desvinculação com validação
	public void setDataDesvinculacao(LocalDate dataDesvinculacao) {
		//Valida se a data de desvinculação não é anterior à data de vinculação
		if (dataDesvinculacao != null && dataVinculacao != null && dataDesvinculacao.isBefore(dataVinculacao)) {
			throw new IllegalArgumentException("Data de desvinculação não pode ser anterior à data de vinculação.");
		}
		this.dataDesvinculacao = dataDesvinculacao;
	}

	//Método getter que retorna o status da relação (ativa ou inativa)
	public boolean isAtivo() {
		return ativo;
	}

	//Método setter que define o status da relação (ativa = true, inativa = false)
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}
