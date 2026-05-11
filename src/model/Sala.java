//Luiz

package model;

public class Sala{
	private int idSala, capacidade;
	private boolean ativo = true;

	public int getIdSala() {return idSala;}
	public void setIdSala(int idSala) {
		if(idSala <= 0) {
			throw new IllegalArgumentException("ID da sala é inválido.");
		}
		this.idSala = idSala;
		}
	public int getCapacidade() {return capacidade;}
	public void setCapacidade(int capacidade){
		if(capacidade <= 0)
			throw new IllegalArgumentException("Campo capacidade é obrigatório.");
		this.capacidade = capacidade;
		}
	public boolean isAtivo() {return ativo;}
	public void setAtivo(boolean ativo) {this.ativo = ativo;}
}