// Guilherme

package model;

import java.time.LocalDate;

public class Trimestre {
  private int idTrimestre, numero, anoLetivo;
  private LocalDate dataInicio, dataFim;

  public int getIdTrimestre() {
    return idTrimestre;
  }

  public void setIdTrimestre(int idTrimestre) {
    if (idTrimestre <= 0) throw new IllegalArgumentException("ID do trimestre inválido.");
    this.idTrimestre = idTrimestre;
  }

  public int getNumero() {
    return numero;
  }

  public void setNumero(int numero) {
    if (numero < 1 || numero > 3)
      throw new IllegalArgumentException("Número do trimestre deve ser 1, 2 ou 3.");
    this.numero = numero;
  }

  public int getAnoLetivo() {
    return anoLetivo;
  }

  public void setAnoLetivo(int anoLetivo) {
    if (anoLetivo < 2000) throw new IllegalArgumentException("Ano letivo inválido.");
    this.anoLetivo = anoLetivo;
  }

  public LocalDate getDataInicio() {
    return dataInicio;
  }

  public void setDataInicio(LocalDate dataInicio) {
    if (dataInicio == null) throw new IllegalArgumentException("Data de início é obrigatória.");
    this.dataInicio = dataInicio;
  }

  public LocalDate getDataFim() {
    return dataFim;
  }

  public void setDataFim(LocalDate dataFim) {
    if (dataFim == null) throw new IllegalArgumentException("Data de fim é obrigatória.");
    if (dataInicio != null && dataFim.isBefore(dataInicio))
      throw new IllegalArgumentException("Data de fim deve ser posterior à data de início.");
    this.dataFim = dataFim;
  }
}
