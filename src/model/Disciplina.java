//Guilherme

package model;

public class Disciplina{
	private String descricao;
	private int cargaHoraria, codigo, idDisciplina;
	private boolean ativo;
	
	public String getDescricao() {	return descricao;}
	public void setDescricao(String descricao) {
		if(descricao == null || descricao.trim().isEmpty()) 
			throw new IllegalArgumentException("Campo descrição é obrigatório. ");
		this.descricao = descricao;
		}
	public int getCargaHoraria() {return cargaHoraria;}
	public void setCargaHoraria(int cargaHoraria) {
		if(cargaHoraria <= 0 )
			throw new IllegalArgumentException("Campo carga horária é obrigatório. ");
		this.cargaHoraria = cargaHoraria;
		}
	public int getCodigo() {return codigo;}
	public void setCodigo(int codigo) {
		if(codigo <= 0)
			throw new IllegalArgumentException("Campo código é obrigatório. ");
		this.codigo = codigo;}
	public int getIdDisciplina() {return idDisciplina;}
	public void setIdDisciplina(int idDisciplina) {
		if(idDisciplina <= 0) {
			throw new IllegalArgumentException("ID da disciplina é inválido.");
		}
		this.idDisciplina = idDisciplina;
		}
	public boolean isAtivo() {return ativo;}
	public void setAtivo(boolean ativo) {this.ativo = ativo;}
}