// Guilherme e Igor
// Esta classe representa o lançamento de notas como um todo

package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LancamentoNota {
  private int turmaId, disciplinaId, trimestreId; // CORRIGIDO: trimestreId adicionado
  private String atividade;
  private LocalDate dataLancamento;
  private List<LancamentoNotaItem> itens = new ArrayList<>();

  public int getTurmaId() {
    return turmaId;
  }

  public void setTurmaId(int turmaId) {
    if (turmaId <= 0) throw new IllegalArgumentException("ID da turma é inválido.");
    this.turmaId = turmaId;
  }

  public int getDisciplinaId() {
    return disciplinaId;
  }

  public void setDisciplinaId(int disciplinaId) {
    if (disciplinaId <= 0) throw new IllegalArgumentException("ID da disciplina é inválido.");
    this.disciplinaId = disciplinaId;
  }

  public int getTrimestreId() {
    return trimestreId;
  }

  public void setTrimestreId(int trimestreId) {
    if (trimestreId <= 0) throw new IllegalArgumentException("ID do trimestre é inválido.");
    this.trimestreId = trimestreId;
  }

  public LocalDate getDataLancamento() {
    return dataLancamento;
  }

  public void setDataLancamento(LocalDate dataLancamento) {
    if (dataLancamento == null)
      throw new IllegalArgumentException("Campo data de lançamento é obrigatória.");
    if (dataLancamento.isAfter(LocalDate.now()))
      throw new IllegalArgumentException("Data de lançamento não pode ser futura.");
    this.dataLancamento = dataLancamento;
  }

  public String getAtividade() {
    return atividade;
  }

  public void setAtividade(String atividade) {
    if (atividade == null || atividade.trim().isEmpty())
      throw new IllegalArgumentException("Atividade é obrigatória.");
    this.atividade = atividade;
  }

  public List<LancamentoNotaItem> getItens() {
    return itens;
  }

  public void setItens(List<LancamentoNotaItem> itens) {
    this.itens = itens == null ? new ArrayList<>() : itens;
  }
}
