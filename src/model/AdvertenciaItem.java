//Guilherme

package model;

public class AdvertenciaItem {
	private int idAdvertencia, idAluno;
	private Advertencia advertencia;
	private Aluno aluno;
	private String motivo, descricao, nomeAluno;
	
	public AdvertenciaItem(int idAluno,String nomeAluno) {
		this.idAluno = idAluno;
		this.nomeAluno = nomeAluno;
	}
	
	public int getIdAluno() {return idAluno;}
	public void setIdAluno(int idAluno) {
		if(idAluno <= 0) {
			throw new IllegalArgumentException("ID do aluno é inválido.");
		}
		this.idAluno = idAluno;
		}
	public int getIdAdvertencia() {return idAdvertencia;}
	public void setIdAdvertencia(int idAdvertencia) {
		if(idAdvertencia <= 0) {
			throw new IllegalArgumentException("ID da advertência é inválido.");
		}
		this.idAdvertencia = idAdvertencia;
		}
	public Advertencia getAdvertencia() {return advertencia;}
	public void setAdvertencia(Advertencia advertencia) {
		if(advertencia == null) {
			throw new IllegalArgumentException("Campo advertência é obrigatório.");
		}
		this.advertencia = advertencia;
		}
	public Aluno getAluno() {return aluno;}
	public void setAluno(Aluno aluno) {
		if(aluno == null) {
			throw new IllegalArgumentException("Campo aluno é obrigatório.");
		}
		this.aluno = aluno;
		}
	public String getMotivo() {return motivo;}
	public void setMotivo(String motivo) {
		if(motivo == null || motivo.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo motivo é obrigatório.");
		}
		this.motivo = motivo;
		}
	public String getDescricao() {return descricao;}
	public void setDescricao(String descricao) {
		if(descricao == null || descricao.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo descrição é obrigatório.");
		}
		this.descricao = descricao;
		}
	public String getNomeAluno() {return nomeAluno;}
	public void setNomeAluno(String nomeAluno) {
		if(nomeAluno == null || nomeAluno.trim().isEmpty()) {
			throw new IllegalArgumentException("Campo nome do aluno é obrigatório.");
		}
		this.nomeAluno = nomeAluno;
		}
}