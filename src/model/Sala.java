//Luiz

package model;

public class Sala{
	private int idSala, capacidade;
	public int getIdSala() {return idSala;}
	public void setIdSala(int idSala) {this.idSala = idSala;}
	public int getCapacidade() {return capacidade;}
	public void setCapacidade(int capacidade) {
		if(capacidade <= 0)
			throw new IllegalArgumentException("Campo capacidade é obrigatório.");
		this.capacidade = capacidade;
		}
}